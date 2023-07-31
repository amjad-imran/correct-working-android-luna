package com.noisefit.ui.npl.quiz

import android.os.CountDownTimer
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.luna.R
import com.noisefit_commans.data.model.NplQuizDataModel
import com.noisefit.data.model.SubmitAnswerDataModel
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.NPLRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.common.clearDrawables
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class NplQuizViewModel @Inject constructor(
    val nplRepository: NPLRepository,
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager
) : BaseViewModel() {
    val TAG = "NplQuizViewModel"
    private val _nplQuizData = MutableLiveData<NplQuizDataModel>()
    val nplQuizData: LiveData<NplQuizDataModel>
        get() = _nplQuizData


    val submitFailed = MutableLiveData<Event<Boolean>>()

    private val _answerData = MutableLiveData<SubmitAnswerDataModel>()
    val answerData: LiveData<SubmitAnswerDataModel>
        get() = _answerData

    private val _nextQuizTimer = MutableLiveData<Long>()
    val nextQuizTimer: LiveData<Long>
        get() = _nextQuizTimer

    private val _nextQuizTimerFinish = MutableLiveData<Boolean>()
    val nextQuizTimerFinish: LiveData<Boolean>
        get() = _nextQuizTimerFinish

    private val _quizTimer = MutableLiveData<Long>()
    val quizTimer: LiveData<Long>
        get() = _quizTimer

    private val _quizTimerFinish = MutableLiveData<Boolean>()
    val quizTimerFinish: LiveData<Boolean>
        get() = _quizTimerFinish


    var timer: CountDownTimer? = null
    var timerLeft: CountDownTimer? = null

    var selectedQuesPos: Int = 0
    var isIdle: Boolean = true
    var elapsedTimeAfterSubmitAnswer: Long = 0L
    private var correctAnswerCount = 0


    fun getIndicatorData(): ArrayList<Int> {
        val indicatorData = ArrayList<Int>()
        if (nplQuizData.value?.questions.isNullOrEmpty()) {
            for (i in 0..5) {
                indicatorData.add(0)
            }
        } else {
            var currentShown = false
            nplQuizData.value?.questions?.forEach { it ->
                if (it.userAnswer == 0L)
                    if (currentShown) {
                        indicatorData.add(0)
                    } else {
                        indicatorData.add(3)
                        currentShown = true
                    }
                else if (it.userAnswer == -1L) {
                    indicatorData.add(0)
                } else {
                    if (it.userAnswer == it.correctAns) {
                        indicatorData.add(1)
                    } else {
                        indicatorData.add(2)
                    }
                }
            }
        }
        return indicatorData
    }

    fun getNextQuizTimerData() {
        if (timerLeft != null) {
            timerLeft?.cancel()
        }
        timerLeft = object :
            CountDownTimer(
                (elapsedTimeAfterSubmitAnswer * 1000),
                1000
            ) {
            override fun onTick(millisUntilFinished: Long) {
                _nextQuizTimer.value = (millisUntilFinished)
            }

            override fun onFinish() {
                _nextQuizTimerFinish.postValue(true)
            }
        }
        timerLeft?.start()
    }

    fun startTimer() {
        if (timer != null) {
            timer?.cancel()
        }
        timer = object : CountDownTimer(AppConstants.QUIZ_RELOAD_TIMER, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _quizTimer.value = (millisUntilFinished)
            }

            override fun onFinish() {
                _quizTimerFinish.postValue(true)

            }
        }
        timer?.start()
    }


    fun submitAnswer() {

        correctAnswerCount = 0
        _nplQuizData.value?.questions?.forEach {
            if (it.userAnswer == it.correctAns) {
                correctAnswerCount += 1
            }
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("correct_answers", correctAnswerCount)
        jsonObject.addProperty("start_date", nplQuizData.value?.startDate ?: "")
        viewModelScope.launch {
            nplRepository.submitQuizAnswer(jsonObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                        localDataStore.clearQuizQuestionData()
                        localDataStore.setQuizApiCallTimeStamp(0)
                        submitFailed.postValue(Event(true))
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    submitAnswer()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            localDataStore.clearQuizQuestionData()
                            localDataStore.setQuizApiCallTimeStamp(0)
                            nplQuizData.value?.questions = ArrayList()
                            if (it.points == 0) {//midnight case
                                correctAnswerCount = 0
                            }
                            _answerData.postValue(it)
                            elapsedTimeAfterSubmitAnswer = it.elapsedTime
                            getNextQuizTimerData()
                        }
                    }
                }
            }
        }
    }

    fun getQuizOfflineData() {
        try {
            val currentDay =
                TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
            val lastApiCallDay =
                TimeUnit.MILLISECONDS.toDays(localDataStore.getQuizApiCallTimeStamp())

            if (currentDay == lastApiCallDay) {
                if (localDataStore.getQuizQuestion() != null) {
                    getOfflineQuizData()
                } else {
                    getQuizData()
                }
            } else
                getQuizData()
        } catch (e: Exception) {
            e.printStackTrace()
            getQuizData()
        }
    }

    fun getQuizData() {
        viewModelScope.launch {
            nplRepository.getNplQuizData().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getQuizData()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            localDataStore.setQuizQuestion(it)
                            if (!it.questions.isNullOrEmpty()) {
                                localDataStore.setQuizApiCallTimeStamp(System.currentTimeMillis())
                            } else {
                                localDataStore.setQuizApiCallTimeStamp(0)
                            }
                            _nplQuizData.postValue(it)

                            if (it.questions.isNullOrEmpty()) {
                                elapsedTimeAfterSubmitAnswer = it.elapsedTime
                                getNextQuizTimerData()
                            }
                        }
                    }
                }
            }
        }

    }

    private fun getOfflineQuizData() {
        if (localDataStore.getQuizQuestion() == null) {
            return
        }
        val tempData = localDataStore.getQuizQuestion()
        _nplQuizData.postValue(tempData!!)
    }

    fun setUserAnswer(quesId: Long, optionId: Long) {
        _nplQuizData.value?.questions?.forEach {
            if (it.quesId == quesId) {
                it.userAnswer = optionId
            }
        }
        _nplQuizData.value?.let {
            localDataStore.setQuizQuestion(it)

            LOGS.d(TAG, "SetUserAnswer $it")
        }
    }

    fun hasStartedQuiz(): Boolean {
        var hasAnswered = false
        nplQuizData.value?.questions?.forEach {
            if (it.userAnswer != 0L) {
                hasAnswered = true
            }

        }
        LOGS.d(TAG, "Has answered $hasAnswered")
        return hasAnswered
    }

    fun lastAnsweredQuesPos(): Int {
        var lastAnsweredPos = 0
        nplQuizData.value?.questions?.forEach {
            if (it.userAnswer != 0L) {
                lastAnsweredPos += 1
            }

        }
        LOGS.d(TAG, "Last answer position $lastAnsweredPos | ${nplQuizData.value}")
        return lastAnsweredPos
    }

    fun logOptionEvent(status: String, quesId: Long) {
        sessionManager.logInsiderAppEvent(
            InsiderAppEvents.NPL_QUIZ_OPTION_SELECT,
            HashMap<String, Any>().apply {
                this["status"] = status
                this["question_number"] = quesId
            })
    }

    fun logWinLoseEvent() {
        val wlStatus = if (correctAnswerCount > 0) {
            "Win"
        } else {
            "Loss"
        }
        sessionManager.logInsiderAppEvent(
            InsiderAppEvents.NPL_QUIZ_END_DONE_CLICK,
            HashMap<String, Any>().apply {
                this["status"] = wlStatus
            })
    }

    fun parseQuizTickerTime(it: Long?): String {
        var totalSecs = it?.div(1000)?.rem(60) ?: 0
        totalSecs += 1
        return if (totalSecs < 10)
            "0$totalSecs"
        else
            "$totalSecs"
    }

    fun parseNextQuizTickerTime(it: Long?): String {
        val totalSecs = it?.div(1000)
        val hours = totalSecs?.div(3600)
        val minutes = (totalSecs?.rem(3600))?.div(60)
        val seconds = totalSecs?.rem(60)
        val tempHour: String = if (hours!! < 10)
            "0$hours"
        else
            "$hours"
        val tempMin: String = if (minutes!! < 10)
            "0$minutes"
        else
            "$minutes"
        val tempSec: String = if (seconds!! < 10)
            "0$seconds"
        else
            "$seconds"
        return "$tempHour : $tempMin : $tempSec"
    }

    /**
     * Returns Pair <Anim, sound>
     */
    fun getWLTAnimFiles(typeValue: Int): Pair<Int, Int> {
        var sound = 0
        val randomAnim: Int = when (typeValue) {
            0 -> {
                sound = R.raw.sound_npl_wrong
                R.raw.anim_quiz_inc_you_got_stumped
            }
            1 -> {
                sound = R.raw.sound_npl_correct
                R.raw.anim_quiz_correct_perfect_delivery
            }
            else -> {
                sound = R.raw.sound_npl_wrong
                R.raw.anim_quiz_to_time_out
            }
        }
        return Pair(randomAnim, sound)
    }

    fun getWinLossRandomAnimFiles(isCorrect: Boolean): Int {
        val randomAnim: Int
        val correctAnim = arrayListOf(
            R.raw.anim_3_2_1_go,
            R.raw.anim_3_2_1_go,
            R.raw.anim_3_2_1_go,
            R.raw.anim_3_2_1_go
        )
        val incorrectAnim = arrayListOf(
            R.raw.anim_3_2_1_go,
            R.raw.anim_3_2_1_go,
            R.raw.anim_3_2_1_go,
            R.raw.anim_3_2_1_go
        )
        randomAnim = if (isCorrect) {
            val randomIndex = Random.nextInt(correctAnim.size);
            correctAnim[randomIndex]
        } else {
            val randomIndex = Random.nextInt(incorrectAnim.size);
            correctAnim[randomIndex]
        }
        return randomAnim
    }

    /**
     * Returns Pair <Anim, sound>
     */
    fun getAnimFilesBasedOnWinLoss(): Pair<Int, Int> {
        var sound = 0
        val randomAnim: Int = if (correctAnswerCount > 0) {
            sound = R.raw.sound_npl_winner
            R.raw.anim_quiz_winner
        } else {
            sound = R.raw.sound_npl_loser
            R.raw.anim_quiz_loser
        }
        return Pair(randomAnim, sound)
    }

    fun getDisplayMessageWinLoss(tvPoints: TextView): String {
        val msg: String = if (correctAnswerCount > 0) {
            "You won ${_answerData.value?.points}"
        } else {
            tvPoints.clearDrawables()
            "Keep Swinging!\n" +
                    "You'll Hit That Six Next Time"
        }
        return msg
    }

    fun getAnimFilesBasedOnAnswers(): Int {
        val randomAnim: Int = when (correctAnswerCount) {
            6 -> {
                R.raw.anim_3_2_1_go
            }
            3 -> {
                R.raw.anim_3_2_1_go
            }
            1, 2 -> {
                R.raw.anim_3_2_1_go
            }
            4, 5 -> {
                R.raw.anim_3_2_1_go
            }
            else -> R.raw.anim_3_2_1_go
        }
        return randomAnim
    }


}