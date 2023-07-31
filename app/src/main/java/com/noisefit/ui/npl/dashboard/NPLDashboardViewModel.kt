package com.noisefit.ui.npl.dashboard

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit.data.model.Faces
import com.noisefit.data.model.ScoreCardData
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.NPLRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.models.TaskEnums
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.response.LiveMatch
import com.noisefit_commans.data.response.NplLeague
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

@HiltViewModel
class NPLDashboardViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val nplRepository: NPLRepository,
    val localDataStore: DataStoredInterface
) :
    BaseViewModel() {

    var categoryId: Int? = null
    var categoryName: String? = null
    private val _scoreCardData = MutableLiveData<ScoreCardData>()
    val scoreCardData: LiveData<ScoreCardData>
        get() = _scoreCardData

    val showKonfettiAnim = MutableLiveData<Event<Boolean>>()

    private val _ongoingMatchData = MutableLiveData<LiveMatch?>()
    val ongoingMatchData: LiveData<LiveMatch?>
        get() = _ongoingMatchData

    private val _nplLeagueData = MutableLiveData<NplLeague?>()
    val nplLeagueData: LiveData<NplLeague?>
        get() = _nplLeagueData

    private val _userWinsData = MutableLiveData<List<LiveMatch>>()
    val userWinsData: LiveData<List<LiveMatch>>
        get() = _userWinsData

    private val _predictWinnerData = MutableLiveData<List<LiveMatch>>()
    val predictWinnerData: LiveData<List<LiveMatch>>
        get() = _predictWinnerData

    private val _collectQuizReward = MutableLiveData<Event<Boolean>>()
    val collectQuizReward: LiveData<Event<Boolean>>
        get() = _collectQuizReward


    val showNextImage = MutableLiveData<Event<Boolean>>()
    val showNextImageHoF = MutableLiveData<Event<Boolean>>()


    fun getScoreCard() {
        viewModelScope.launch {
            nplRepository.getNplDashScoreCard().collect { resource ->
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
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getScoreCard()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _scoreCardData.postValue(it)

                            val lastWinsCount = localDataStore.getLastWinsCount()
                            val userCurrentWins = it.scoreCard?.userWins ?: 0
                            var showAnim = false
                            if (lastWinsCount == -1) {
                                if (userCurrentWins > 0) {
                                    showAnim = true
                                }
                            } else {
                                if (userCurrentWins > lastWinsCount) {
                                    showAnim = true
                                }
                            }

                            if (showAnim) {
                                showKonfettiAnim.postValue(Event(true))
                                localDataStore.setLastWinsCount(userCurrentWins)
                            }
                        }
                    }
                }
            }
        }

    }

    fun getDashboardMatches() {
        viewModelScope.launch {
            nplRepository.getMatchesData().collect { resource ->
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
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getDashboardMatches()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _ongoingMatchData.postValue(it.live_match)
                            _userWinsData.postValue(it.won_match)
                            _predictWinnerData.postValue(it.upcomingMatch)
                        }

                    }
                }
            }
        }

    }


    var imageTimer: Timer? = null
    fun startBannerTimer() {
        if (imageTimer == null) {
            imageTimer = Timer()
            imageTimer?.scheduleAtFixedRate(RemindTask(), 0, 4000)
        }
    }

    var imageTimerHoF: Timer? = null
    fun startBannerTimerHoF() {
        if (imageTimerHoF == null) {
            imageTimerHoF = Timer()
            imageTimerHoF?.scheduleAtFixedRate(RemindTaskHoF(), 0, 4000)
        }
    }

    inner class RemindTask : TimerTask() {
        override fun run() {
            showNextImage.postValue(Event(true))
        }
    }

    inner class RemindTaskHoF : TimerTask() {
        override fun run() {
            showNextImageHoF.postValue(Event(true))
        }
    }


    fun predictWinner(
        matchId: Long?,
        userSelectedTeamId: Long?
    ) {
        viewModelScope.launch {
            val requestObject = JsonObject().apply {
                this.addProperty("match_id", matchId)
                this.addProperty("team_id", userSelectedTeamId)
            }

            nplRepository.predictWinner(requestObject).collect { resource ->
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
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        predictWinner(matchId, userSelectedTeamId)
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            getDashboardMatches()
                        }

                    }
                }
            }
        }

    }

    fun removeCollectedReward(id: Long) {
        val wonMatches = _userWinsData.value

        wonMatches?.let {
            val index = it.indexOfFirst { match ->
                match.prediction_id == id
            }
            (it as ArrayList).removeAt(index)
            _userWinsData.postValue(it)
        }
    }

    var timerLeft: CountDownTimer? = null

    var elapsedTimeAfterSubmitAnswer: Long = 0L

    private val _nextQuizTimer = MutableLiveData<Long>()
    val nextQuizTimer: LiveData<Long>
        get() = _nextQuizTimer

    private val _nextQuizTimerFinish = MutableLiveData<Boolean>()
    val nextQuizTimerFinish: LiveData<Boolean>
        get() = _nextQuizTimerFinish

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

    fun collectQuizReward() {
        viewModelScope.launch {
            val requestObject = JsonObject().apply {
                this.addProperty("task_enum", TaskEnums.NPL_150.type)
            }

            nplRepository.collectNplQuizReward(requestObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                        _collectQuizReward.postValue(Event(false))
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    collectQuizReward()
                                }

                                override fun no() {}
                            }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _collectQuizReward.postValue(Event(true))
                        }

                    }
                }
            }
        }


    }

    fun getDummyWatchData(): ArrayList<Faces> {
        val temp = ArrayList<Faces>()
        temp.add(Faces(imageUrl = "https://images.gonoise.com/npl/production/banners/Noise+Coins.webp"))
        temp.add(Faces(imageUrl = "https://images.gonoise.com/npl/production/banners/Noise+Coins.webp"))
        temp.add(Faces(imageUrl = "https://images.gonoise.com/npl/production/banners/Noise+Coins.webp"))
        temp.add(Faces(imageUrl = "https://images.gonoise.com/npl/production/banners/Noise+Coins.webp"))
        return temp
    }
}