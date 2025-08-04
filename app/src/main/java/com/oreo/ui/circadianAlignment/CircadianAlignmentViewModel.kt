package com.oreo.ui.circadianAlignment

import android.graphics.Color
import android.os.CountDownTimer
import androidx.core.graphics.toColorInt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.circadian.CircadianGraphData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.CorrectiveActivitiesModel
import com.oreo.data.model.OnlyImgWithText
import com.oreo.data.model.ProgressBarLytData
import com.oreo.data.model.TimeWindow
import com.oreo.data.model.circadian.Activity
import com.oreo.data.model.circadian.CircadianResponseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CircadianAlignmentViewModel
@Inject constructor(
    private val resourceProvider: ResourcesProvider,
    private val userRepository: UserRepository,
    private val localDataStore: DataStoredInterface,
) : BaseViewModel() {

    companion object {
        const val light_exposure_key = "light_exposure"
        const val daily_steps_key = "daily_steps"
        const val meal_window_key = "meal_window"
        const val caffeine_window_key = "caffeine_window"
        const val workout_key = "workout"
        val actMonStatusList = listOf("partial", "done", "not-done")
    }

    val lightExposureData = MutableLiveData<CorrectiveActivitiesModel>()
    val dailyStepsData = MutableLiveData<CorrectiveActivitiesModel>()
    val mealWindowData = MutableLiveData<CorrectiveActivitiesModel>()
    val workoutData = MutableLiveData<CorrectiveActivitiesModel>()
    val caffeineWindowData = MutableLiveData<CorrectiveActivitiesModel>()

    val circadianResponseData = MutableLiveData<CircadianResponseModel>()

    val correctiveActivitiesListData = MutableLiveData<ArrayList<CorrectiveActivitiesModel>>()

    private val timerMap = mutableMapOf<String, CountDownTimer>()

    fun initData() {
        viewModelScope.launch {
            userRepository.getCircadianData().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {

                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            circadianResponseData.postValue(it)
                            it.activities?.let { it1 -> prepareCorrectiveActivitiesData(it1) }
                            LOGS.d("abcjacjcab Posting data: $it")
                        }
                    }
                }
            }
        }
    }

    private fun prepareCorrectiveActivitiesData(activitiesData: List<Activity>) {
        activitiesData.forEach {

            val isLogged = it.status?.let { ss -> ss == "Logged" }
            val timeLeft =
                if (it.time == null || it.time == 0) {
                    null
                } else if (it.time <= 0) {
                    "0"
                } else {
                    val calcTime = formatSecondsToHHMM(it.time)
                    resourceProvider.getString(R.string.text_timeval_left, calcTime)
                }

            when (it.type) {
                // Light Exposure Data
                light_exposure_key -> {
                    lightExposureData.value = CorrectiveActivitiesModel(
                        key = light_exposure_key,
                        bgMainImg = R.drawable.bg_light_exposure_corrective_activities,
                        title = resourceProvider.getString(R.string.text_light_exposure),
                        desc = resourceProvider.getString(R.string.text_soak_in_some_natural_light_it_s_a_powerful_cue_for_your_body_to_wake_up),
                        onlyImgWithText = OnlyImgWithText(
                            img = R.drawable.ic_sun_activity_monitor,
                            txt = "45\nmins"
                        ),
                        progressBarLytData = null,
                        logStatus = isLogged,
                        time = timeLeft,
                        timeInSec = it.time
                    )
                }

                daily_steps_key -> {
                    // Daily Steps Data
                    dailyStepsData.value = CorrectiveActivitiesModel(
                        key = daily_steps_key,
                        bgMainImg = R.drawable.bg_daily_steps_corrective_activities,
                        title = resourceProvider.getString(R.string.text_daily_steps),
                        desc = resourceProvider.getString(R.string.text_soak_in_some_natural_light_it_s_a_powerful_cue_for_your_body_to_wake_up),
                        onlyImgWithText = null,
                        progressBarLytData = ProgressBarLytData(
                            totalProgress = 100,
                            currentProgress = 60,
                            img = R.drawable.ic_shoe_corrective_activities,
                            txt = "2334"
                        ),
                        logStatus = null,
                        time = timeLeft,
                        timeInSec = it.time
                    )
                }

                meal_window_key -> {
                    // Meal Window Data
                    mealWindowData.value = CorrectiveActivitiesModel(
                        key = meal_window_key,
                        bgMainImg = R.drawable.bg_meal_window_corrective_activities,
                        title = resourceProvider.getString(R.string.text_meal_window),
                        desc = resourceProvider.getString(R.string.text_soak_in_some_natural_light_it_s_a_powerful_cue_for_your_body_to_wake_up),
                        onlyImgWithText = OnlyImgWithText(
                            img = R.drawable.ic_meal_corrective_activities,
                            txt = null
                        ),
                        progressBarLytData = null,
                        logStatus = isLogged,
                        time = timeLeft,
                        timeInSec = it.time
                    )
                }

                workout_key -> {
                    // Workout Data
                    workoutData.value = CorrectiveActivitiesModel(
                        key = workout_key,
                        bgMainImg = R.drawable.bg_workout_corrective_activities,
                        title = resourceProvider.getString(R.string.text_workout),
                        desc = resourceProvider.getString(R.string.text_soak_in_some_natural_light_it_s_a_powerful_cue_for_your_body_to_wake_up),
                        onlyImgWithText = null,
                        progressBarLytData = ProgressBarLytData(
                            totalProgress = 100,
                            currentProgress = 60,
                            img = R.drawable.ic_workout_corrective_activities,
                            txt = "23 mins"
                        ),
                        logStatus = null,
                        time = timeLeft,
                        timeInSec = it.time
                    )
                }

                caffeine_window_key -> {
                    // Caffeine Window Data
                    caffeineWindowData.value = CorrectiveActivitiesModel(
                        key = caffeine_window_key,
                        bgMainImg = R.drawable.bg_caffeine_window_corrective_activities,
                        title = resourceProvider.getString(R.string.text_caffeine_window2),
                        desc = resourceProvider.getString(R.string.text_soak_in_some_natural_light_it_s_a_powerful_cue_for_your_body_to_wake_up),
                        onlyImgWithText = OnlyImgWithText(
                            img = R.drawable.ic_caffeine_corrective_activities,
                            txt = null
                        ),
                        progressBarLytData = null,
                        logStatus = isLogged,
                        time = timeLeft,
                        timeInSec = it.time
                    )
                }
            }
        }

        val correctiveActivitiesList:
                ArrayList<CorrectiveActivitiesModel> = ArrayList()

        lightExposureData.value?.let {
            correctiveActivitiesList.add(it)

        }

        dailyStepsData.value?.let {
            correctiveActivitiesList.add(it)

        }

        mealWindowData.value?.let {
            correctiveActivitiesList.add(it)

        }

        workoutData.value?.let {
            correctiveActivitiesList.add(it)

        }

        caffeineWindowData.value?.let {
            correctiveActivitiesList.add(it)

        }

        correctiveActivitiesListData.postValue(correctiveActivitiesList)
        startTimers(correctiveActivitiesList)
    }

    fun formatSecondsToHHMM(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return String.format("%02d:%02d", hours, minutes)
    }

    fun startTimers(list: ArrayList<CorrectiveActivitiesModel>) {
        viewModelScope.launch {

            list.forEach { card ->
                if (card.time != null && card.timeInSec != null) {
                    val totalMillis = card.timeInSec * 1000L

                    timerMap[card.key]?.cancel() // Cancel existing if any

                    val timer = object : CountDownTimer(totalMillis, 60_000L) {
                        override fun onTick(millisUntilFinished: Long) {
                            val remainingSeconds = (millisUntilFinished / 1000).toInt()
                            updateCard(card.key, formatSecondsToHHMM(remainingSeconds), false)
                        }

                        override fun onFinish() {
                            updateCard(card.key, "00:00", true)
                        }
                    }

                    timer.start()
                    timerMap[card.key] = timer
                }
            }
        }
    }

    private fun updateCard(cardkey: String, formattedTime: String, isFinished: Boolean) {
        getCorrectiveActivitiesLiveData(cardkey)?.let { liveData ->

            val time = if (isFinished) "0"
            else resourceProvider.getString(R.string.text_timeval_left, formattedTime)
            liveData.postValue(
                liveData.value?.copy(
                    time = time
                )
            )
        }
    }

    fun postLogData(key: String?, isLogged: Boolean?) {
        viewModelScope.launch {
            if (!key.isNullOrEmpty() && isLogged != null) {
                val reqData = JsonObject().apply {
                    addProperty("date", "")
                    addProperty(key, isLogged)
                }

                userRepository.submitLogCircadianData(reqData).collect {
                    when (it) {
                        is Resource.GenericError -> {}
                        is Resource.Loading -> {}
                        is Resource.NetworkError -> {}
                        is Resource.Success<*> -> {
                            getCorrectiveActivitiesLiveData(key)?.let { livaDataObj ->
                                livaDataObj.postValue(
                                    livaDataObj.value?.copy(
                                        logStatus = true
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun getCorrectiveActivitiesLiveData(key: String): MutableLiveData<CorrectiveActivitiesModel>? {
        return when (key) {
            light_exposure_key -> lightExposureData
            daily_steps_key -> dailyStepsData
            meal_window_key -> mealWindowData
            caffeine_window_key -> caffeineWindowData
            workout_key -> workoutData
            else -> null
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerMap.values.forEach { it.cancel() }
    }

    fun getScrollGraphList(circadianGraphData: CircadianGraphData?): List<TimeWindow> {
        val data = ArrayList<TimeWindow>()
        circadianGraphData?.activityWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let
            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#9E6FC7".toColorInt(),
                    "#4C4192".toColorInt(),
                    "#D69B92".toColorInt(),
                    rowIndex = 0,
                    label = "Activity"
                )
            )
        }

        circadianGraphData?.caffeineWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let
            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#2E2422".toColorInt(),
                    "#2E2422".toColorInt(),
                    "#D69B92".toColorInt(),
                    rowIndex = 0,
                    label = resourceProvider.getString(R.string.text_caffeine_open)
                )
            )
        }

        circadianGraphData?.cortisolPeakWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let
            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#33296F".toColorInt(),
                    "#634ED5".toColorInt(),
                    "#D69B92".toColorInt(),
                    rowIndex = 0,
                    label = "Cortisol Peak Window"
                )
            )
        }

        circadianGraphData?.dlmoPhaseWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let
            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#613644".toColorInt(),
                    "#BD6FC7".toColorInt(),
                    "#D69B92".toColorInt(),
                    rowIndex = 0,
                    label = resourceProvider.getString(R.string.text_dim_light_melatonin_onset)
                )
            )
        }

        circadianGraphData?.firstFocusPeakWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let
            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#A1734E".toColorInt(),
                    "#D6A176".toColorInt(),
                    "#D69B92".toColorInt(),
                    rowIndex = 0,
                    label = "First Focus Peak"
                )
            )
        }

        circadianGraphData?.ghPulseWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let
            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#2E2422".toColorInt(),
                    "#2E2422".toColorInt(),
                    "#D69B92".toColorInt(),
                    rowIndex = 0,
                    label = "GH Pulse"
                )
            )
        }

        circadianGraphData?.lightAnchoringPhaseWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let
            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#2E2422".toColorInt(),
                    "#2E2422".toColorInt(),
                    "#D69B92".toColorInt(),
                    rowIndex = 0,
                    label = "Light Anchoring"
                )
            )
        }

        circadianGraphData?.melatoninPrepPhaseWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let
            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#2E2422".toColorInt(),
                    "#2E2422".toColorInt(),
                    "#D69B92".toColorInt(),
                    rowIndex = 0,
                    label = "melatonin Phase"
                )
            )
        }

        circadianGraphData?.secondFocusPeakWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let
            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#2E2422".toColorInt(),
                    "#2E2422".toColorInt(),
                    "#D69B92".toColorInt(),
                    rowIndex = 0,
                    label = "Second Focus Peak"
                )
            )
        }

        circadianGraphData?.sleepWindowOpensGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let
            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#2E2422".toColorInt(),
                    "#2E2422".toColorInt(),
                    "#D69B92".toColorInt(),
                    rowIndex = 0,
                    label = resourceProvider.getString(R.string.text_sleep)
                )
            )
        }

        return data
    }

    private fun getCircadianTimeFloatValue(time: String?): Float {
        if (time == null) {
            return -1f
        }
        val timee = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
//        timee.minute.toFloat()
        return timee.hour.toFloat()
    }

    fun isChatSplashShown(): Boolean {
        return localDataStore.isAiChatSplashShown()
    }

    fun dummyList() =
        listOf(
            TimeWindow(
                startHour = 9.0f,
                endHour = 12.0f,
                rowIndex = 0,
                startColor = "#2E2422CC".toColorInt(),
                endColor = "#2E2422CC".toColorInt(),
                textColor = "#D69B92B2".toColorInt(),
                label = "Avoid Caffeine"
            ),
            TimeWindow(
                startHour = 13.0f,
                endHour = 15.0f,
                rowIndex = 1,
                startColor = "#A1734E".toColorInt(),
                endColor = "#D6A176".toColorInt(),
                textColor = "#FFFFFF".toColorInt(),
                label = "Caffeine Window Open"
            ),
            TimeWindow(
                startHour = 16.0f,
                endHour = 18.0f,
                rowIndex = 0,
                startColor = "#2E2422CC".toColorInt(),
                endColor = "#2E2422CC".toColorInt(),
                textColor = "#D69B92B2".toColorInt(),
                label = "Avoid Caffeine"
            )
        )

}

enum class CorrectiveActivitiesEnum {
    LIGHT_EXPOSURE, DAILY_STEPS, MEAL_WINDOW, WORKOUT, CAFFEINE
}