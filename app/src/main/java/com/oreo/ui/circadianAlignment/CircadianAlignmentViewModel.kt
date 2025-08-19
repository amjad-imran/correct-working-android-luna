package com.oreo.ui.circadianAlignment

import android.graphics.Color
import android.os.CountDownTimer
import androidx.core.graphics.toColorInt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
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
import com.oreo.ui.custom.ClockEvent
import com.oreo.ui.custom.ClockEventType
import com.oreo.ui.stress.help.StressImageModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
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

    val howItWorksDataList = MutableLiveData<List<StressImageModel>>()

    private val timerMap = mutableMapOf<String, CountDownTimer>()

    fun initData() {

      /*  val data =
            "{ \"activities\": [ { \"type\": \"light_exposure\", \"goal\": 120, \"time\": -25364, \"status\": false }, { \"type\": \"meal_window\", \"time\": 7035, \"status\": false }, { \"type\": \"caffeine_window\", \"time\": -7364, \"status\": false }, { \"type\": \"workout\", \"time\": 3435, \"goal\": 660, \"status\": false }, { \"type\": \"daily_steps\", \"goal\": 10000, \"time\": 3435, \"progress\": 4910 } ], \"activity_monitor\": [ { \"type\": \"light_exposure\", \"status\": \"partial\" }, { \"type\": \"daily_steps\", \"status\": \"partial\" }, { \"type\": \"meal_window\", \"status\": \"partial\" }, { \"type\": \"caffeine_window\", \"status\": \"partial\" }, { \"type\": \"workout\", \"status\": \"partial\" } ], \"circadian_mid_point\": { \"start_time\": \"2025-08-19 05:30:00\", \"end_time\": \"2025-08-19 07:30:00\", \"circadian_midpoint\": \"2025-08-19 04:18:30\", \"avg_now\": \"2025-08-19 04:55:06\", \"avg_before\": \"2025-08-19 05:09:06\", \"nudge\": { \"title\": \"Embrace Morning Momentum: Tune Your Day with Your Natural Rhythm\", \"description\": \"You're in the morning window; use this calm peak to soak in light and plan small actions. If you haven’t logged your rhythm today, jot a sunlight moment or a 3-minute stretch now to honor your rhythm,\" } }, \"chronotype\": { \"type\": \"Definitely evening type\", \"description\": \"You're in the morning window; use this calm peak to soak in light and plan small actions. If you hav\" }, \"graph_data\": { \"caffeine_window_graph\": { \"start_time\": \"09:32\", \"end_time\": \"15:32\" }, \"melatonin_prep_phase_window_graph\": { \"start_time\": \"19:32\", \"end_time\": \"22:32\" }, \"dlmo_phase_window_graph\": { \"start_time\": \"22:32\", \"end_time\": \"23:32\" }, \"cortisol_peak_window_graph\": { \"start_time\": \"07:32\", \"end_time\": \"08:02\" }, \"light_anchoring_phase_window_graph\": { \"start_time\": \"08:32\", \"end_time\": \"10:32\" }, \"first_focus_peak_window_graph\": { \"start_time\": \"10:32\", \"end_time\": \"13:32\", \"peak_time\": \"12:02\" }, \"second_focus_peak_window_graph\": { \"start_time\": \"15:32\", \"end_time\": \"18:32\", \"peak_time\": \"17:02\" }, \"sleep_window_opens_graph\": { \"start_time\": \"23:32\" }, \"gh_pulse_window_graph\": { \"start_time\": \"05:32\" }, \"activity_window_graph\": { \"start_time\": \"07:32\", \"end_time\": \"18:32\" }, \"circadian_mid_point\": { \"start_time\": \"2025-08-18 05:30:00\", \"end_time\": \"2025-08-18 07:30:00\", \"circadian_midpoint\": \"2025-08-18 04:18:30\", \"avg_now\": \"2025-08-18 04:55:06\", \"avg_before\": \"2025-08-18 05:09:06\", \"nudge\": { \"title\": \"Embrace Morning Momentum: Tune Your Day with Your Natural Rhythm\", \"description\": \"You're in the morning window; use this calm peak to soak in light and plan small actions. If you haven’t logged your rhythm today, jot a sunlight moment or a 3-minute stretch now to honor your rhythm,\" } }, \"energy_graph\": [ { \"start_time\": \"07:32\", \"energy\": 0 }, { \"start_time\": \"08:32\", \"energy\": 0 }, { \"start_time\": \"09:32\", \"energy\": 0 }, { \"start_time\": \"10:32\", \"energy\": 0 }, { \"start_time\": \"11:32\", \"energy\": 0 }, { \"start_time\": \"12:32\", \"energy\": 0 }, { \"start_time\": \"13:32\", \"energy\": 0 }, { \"start_time\": \"14:32\", \"energy\": 0 }, { \"start_time\": \"15:32\", \"energy\": 0 }, { \"start_time\": \"16:32\", \"energy\": 0 }, { \"start_time\": \"17:32\", \"energy\": 0.029 }, { \"start_time\": \"18:32\", \"energy\": 0.411 }, { \"start_time\": \"19:32\", \"energy\": 1 }, { \"start_time\": \"20:32\", \"energy\": 0.425 }, { \"start_time\": \"21:32\", \"energy\": 0.109 }, { \"start_time\": \"22:32\", \"energy\": 0.098 }, { \"start_time\": \"23:32\", \"energy\": 0.26 } ], \"sleep_data\": { \"bed_time\": \"2025-08-19 01:05:00\", \"wake_time\": \"2025-08-19 07:32:00\" }, \"start_time\": \"2025-08-18 07:32:00\", \"end_time\": \"2025-08-18 23:32:00\" } }"
        circadianResponseData.postValue(
            Gson().fromJson(
                data,
                CircadianResponseModel::class.java
            )
        )
        return*/

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
                                        initData()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            circadianResponseData.postValue(it)
                            it.activities?.let { it1 ->
                                prepareCorrectiveActivitiesData(
                                    it1,
                                    it.graphData != null
                                )
                            }
                            LOGS.d("abcjacjcab Posting data: $it")
                        }
                    }
                }
            }
        }
    }

    private fun prepareCorrectiveActivitiesData(
        activitiesData: List<Activity>?,
        shouldShowFooter: Boolean
    ) {

        lightExposureData.value = CorrectiveActivitiesModel(
            key = light_exposure_key,
            bgMainImg = R.drawable.bg_light_exposure_corrective_activities,
            title = resourceProvider.getString(R.string.text_light_exposure),
            desc = resourceProvider.getString(R.string.text_corrective_activities_circadian_desc_1),
            onlyImgWithText = OnlyImgWithText(
                img = R.drawable.ic_sun_activity_monitor,
                txt = "45\nmins"
            ),
        )

        dailyStepsData.value = CorrectiveActivitiesModel(
            key = daily_steps_key,
            bgMainImg = R.drawable.bg_daily_steps_corrective_activities,
            title = resourceProvider.getString(R.string.text_daily_steps),
            desc = resourceProvider.getString(R.string.text_corrective_activities_circadian_desc_2),
            progressBarLytData = ProgressBarLytData(
                totalProgress = 2334,
                currentProgress = 1634,
                img = R.drawable.ic_shoe_corrective_activities,
                txt = "2334"
            ),
            /*logStatus = null,
            time = timeLeft,
            timeInSec = it.time*/
        )

        mealWindowData.value = CorrectiveActivitiesModel(
            key = meal_window_key,
            bgMainImg = R.drawable.bg_meal_window_corrective_activities,
            title = resourceProvider.getString(R.string.text_meal_window),
            desc = resourceProvider.getString(R.string.text_corrective_activities_circadian_desc_3),
            onlyOnlyImgLytData = R.drawable.ic_meal_corrective_activities,
        )

        workoutData.value = CorrectiveActivitiesModel(
            key = workout_key,
            bgMainImg = R.drawable.bg_workout_corrective_activities,
            title = resourceProvider.getString(R.string.text_workout),
            desc = resourceProvider.getString(R.string.text_corrective_activities_circadian_desc_4),
            progressBarLytData = ProgressBarLytData(
                totalProgress = 23,
                currentProgress = 9,
                img = R.drawable.ic_workout_corrective_activities,
                txt = "23 mins"
            )
        )

        caffeineWindowData.value = CorrectiveActivitiesModel(
            key = caffeine_window_key,
            bgMainImg = R.drawable.bg_caffeine_window_corrective_activities,
            title = resourceProvider.getString(R.string.text_caffeine_window2),
            desc = resourceProvider.getString(R.string.text_corrective_activities_circadian_desc_5),
            onlyOnlyImgLytData = R.drawable.ic_caffeine_corrective_activities,
        )

        if (shouldShowFooter) {
            var isLogged: Boolean?
            var timeLeft: String?

            activitiesData?.forEach {

                isLogged = it.status
                timeLeft =
                    if (it.time == null || it.time == 0) {
                        null
                    } else if (it.time <= 0) {
                        "0"
                    } else {
                        val calcTime = formatSecondsToHHMM(it.time)
                        resourceProvider.getString(R.string.text_timeval_left, calcTime)
                    }

                val curData = it
                when (it.type) {
                    // Light Exposure Data
                    light_exposure_key -> {
                        lightExposureData.value?.apply {
                            onlyImgWithText = OnlyImgWithText(
                                img = R.drawable.ic_sun_activity_monitor,
                                txt = if ((curData.time
                                        ?: 0) <= 0
                                ) "-" else "${curData.goal ?: 45}\nmins"
                            )
                            showFooter = true
                            logStatus = isLogged
                            time = timeLeft
                            timeInSec = it.time
                        }
                    }

                    daily_steps_key -> {
                        // Daily Steps Data
                        val goal = it.goal?.toInt()
                        dailyStepsData.value?.apply {
                            progressBarLytData = ProgressBarLytData(
                                totalProgress = goal ?: 0,
                                currentProgress = goal?.let { curData.progress ?: 0 } ?: 0,
                                img = R.drawable.ic_shoe_corrective_activities,
                                txt = it.goal ?: "-"
                            )

                            showFooter = true
                            logStatus = isLogged
                            time = timeLeft
                            timeInSec = it.time
                        }
                    }

                    meal_window_key -> {
                        // Meal Window Data
                        mealWindowData.value?.apply {
                            showFooter = true
                            logStatus = isLogged
                            time = timeLeft
                            timeInSec = it.time
                        }
                    }

                    workout_key -> {
                        // Workout Data
                        val goal = it.goal?.toInt()?.div(60)
                        workoutData.value?.apply {
                            progressBarLytData = ProgressBarLytData(
                                totalProgress = goal ?: 0,
                                currentProgress = goal?.let { ((it / 60) * 0.1).toInt() } ?: 0,
                                img = R.drawable.ic_workout_corrective_activities,
                                txt = if ((curData.time
                                        ?: 0) <= 0
                                ) "-" else "${goal ?: 23} mins" /* goal?.let { "$it mins" } ?: "-"*/
                            )
                            showFooter = true
                            logStatus = isLogged
                            time = timeLeft
                            timeInSec = it.time
                        }
                    }

                    caffeine_window_key -> {
                        // Caffeine Window Data
                        caffeineWindowData.value?.apply {
                            showFooter = true
                            logStatus = isLogged ?: false
                            time = timeLeft
                            timeInSec = it.time
                        }
                    }
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
                    val totalMillis = card.timeInSec!! * 1000L

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
                    addProperty("date", LocalDate.now().toString())
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

    fun getScrollGraphList(
        circadianGraphData: CircadianGraphData?,
    ): List<TimeWindow> {
        val data = ArrayList<TimeWindow>()
        circadianGraphData?.caffeineWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let
            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#A1734E".toColorInt(),
                    "#D6A176".toColorInt(),
                    "#FFFFFF".toColorInt(),
                    rowIndex = 1,
                    label = "Caffeine Window Open"//todo change to string
                )
            )
        }

        circadianGraphData?.dlmoPhaseWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let
            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#9E6FC7".toColorInt(),
                    "#4C4192".toColorInt(),
                    "#EBAFFF".toColorInt(),
                    rowIndex = 0,
                    label = "Dim-light Phase"
                )
            )
        }

        circadianGraphData?.lightAnchoringPhaseWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let
            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#B2E6EE".toColorInt(),
                    "#FFE0BC".toColorInt(),
                    "#99000000".toColorInt(),
                    rowIndex = 0,
                    label = "Natural Light"
                )
            )
        }
        circadianGraphData?.sleepData?.let {
            if (it.wakeTime == null || it.bedTime == null) return@let

            //2025-08-18 01:05:00
            val startTime = LocalDateTime.parse(
                it.wakeTime,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ).format(DateTimeFormatter.ofPattern("HH:mm"))
            val endTime = LocalDateTime.parse(
                it.bedTime,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ).format(DateTimeFormatter.ofPattern("HH:mm"))


            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(endTime),
                    getCircadianTimeFloatValue(startTime),
                    "#33296F".toColorInt(),
                    "#634ED5".toColorInt(),
                    "#9E91E8".toColorInt(),
                    rowIndex = 0,
                    label = "Sleep"
                )
            )
        }

        circadianGraphData?.melatoninPrepPhaseWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let
            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#613644".toColorInt(),
                    "#BD6FC7".toColorInt(),
                    "#FFAFF2".toColorInt(),
                    rowIndex = 0,
                    label = "Evening Wind-Down"
                )
            )

            circadianGraphData?.lightAnchoringPhaseWindowGraph?.let { it1 ->

                data.add(
                    TimeWindow(
                        getCircadianTimeFloatValue(it1.endTime),
                        getCircadianTimeFloatValue(it.startTime),
                        "#33646464".toColorInt(),
                        "#33646464".toColorInt(),
                        "#5A5A5A".toColorInt(),
                        rowIndex = 0,
                        label = "Neutral Light Zone"
                    )
                )
            }


        }

        return data
    }

    private fun getCircadianTimeFloatValue(time: String?): Float {
        if (time == null) {
            return -1f
        }


        val timee = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
        return timee.hour + timee.minute / 60f
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
                startColor = "#CC2E2422".toColorInt(),
                endColor = "#CC2E2422".toColorInt(),
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
                startColor = "#CC2E2422".toColorInt(),
                endColor = "#CC2E2422".toColorInt(),
                textColor = "#D69B92B2".toColorInt(),
                label = "Avoid Caffeine"
            )
        )

    fun initHowItWorksData() {
        viewModelScope.launch(Dispatchers.IO) {
            howItWorksDataList.postValue(
                arrayListOf(
                    StressImageModel(
                        resourceProvider.getString(R.string.text_what_is_circadian_alignment),
                        resourceProvider.getString(R.string.text_circadian_hiw_desc_1),
                        R.drawable.image_circadian_hiw_1
                    ),
                    StressImageModel(
                        resourceProvider.getString(R.string.text_how_does_the_luna_ring),
                        resourceProvider.getString(R.string.text_stress_2),
                        R.drawable.image_s_hw_2
                    ),
                )
            )
        }
    }

    fun generateClockEvents(circadianGraphData: CircadianGraphData?): List<ClockEvent> {

        val response = ArrayList<ClockEvent>()

        circadianGraphData?.caffeineWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let

            response.add(
                ClockEvent(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    ClockEventType.LINE,
                    Color.parseColor("#B5845D"),
                    Color.parseColor("#B5845D"),
                    textColor = "#CC242424".toColorInt(),
                    ""
                )
            )
        }

        circadianGraphData?.sleepData?.let {
            if (it.wakeTime == null || it.bedTime == null) return@let

            //2025-08-18 01:05:00
            val startTime = LocalDateTime.parse(
                it.wakeTime,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ).format(DateTimeFormatter.ofPattern("HH:mm"))
            val endTime = LocalDateTime.parse(
                it.bedTime,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ).format(DateTimeFormatter.ofPattern("HH:mm"))


            response.add(
                ClockEvent(
                    getCircadianTimeFloatValue(endTime),
                    getCircadianTimeFloatValue(startTime),
                    ClockEventType.ARCH,
                    "#33296F".toColorInt(),
                    "#634ED5".toColorInt(),
                    "#9E91E8".toColorInt(),
                    label = "Sleep"
                )
            )
        }

        circadianGraphData?.sleepWindowOpensGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let

            response.add(
                ClockEvent(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    ClockEventType.ARCH,
                    Color.parseColor("#2E246E"),
                    Color.parseColor("#4E3ABC"),
                    textColor = "#CAC1FF".toColorInt(),
                    resourceProvider.getString(R.string.text_sleep)
                )
            )
        }

        circadianGraphData?.lightAnchoringPhaseWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let

            response.add(
                ClockEvent(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    ClockEventType.ARCH,
                    Color.parseColor("#B4E6EC"),
                    Color.parseColor("#FBE0BE"),
                    textColor = "#CC242424".toColorInt(),
                    "Natural Light"/*resourceProvider.getString(R.string.text_sleep)*/
                )
            )
        }

        circadianGraphData?.melatoninPrepPhaseWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let

            response.add(
                ClockEvent(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    ClockEventType.ARCH,
                    Color.parseColor("#55313E"),
                    Color.parseColor("#995CA0"),
                    textColor = "#FC9CFF".toColorInt(),
                    "Wind-Down"
                )
            )
            circadianGraphData?.lightAnchoringPhaseWindowGraph?.let { it1 ->

                response.add(
                    ClockEvent(
                        getCircadianTimeFloatValue(it1.endTime),
                        getCircadianTimeFloatValue(it.startTime),
                        ClockEventType.ARCH,
                        "#181A1F".toColorInt(),
                        "#181A1F".toColorInt(),
                        "#858585".toColorInt(),
                        label = "Neutral Light"
                    )
                )
            }
        }

        circadianGraphData?.dlmoPhaseWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let

            response.add(
                ClockEvent(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    ClockEventType.ARCH,
                    Color.parseColor("#8F5EBA"),
                    Color.parseColor("#443A7B"),
                    textColor = "#E0BEFF".toColorInt(),
                    "Dim-light"
                )
            )
        }

        return response
    }

    fun generateValuesData(energyValues: kotlin.collections.List<Float>?): ArrayList<Float> {
        val energyArray = ArrayList<Float>()
        if (energyValues.isNullOrEmpty()) {
            repeat(24, {
                energyArray.add(0f)
            })
        } else if (energyValues.size < 24) {
            val valuesToAdd = 24 - energyValues.size
            repeat(valuesToAdd, {
                energyArray.add(0f)
            })
        } else if (energyValues.size > 24) {
            energyArray.addAll(energyValues.subList(0, 24))
        } else {
            energyArray.addAll(energyValues)
        }
        return energyArray
    }

    fun getCaffeineState(data: CircadianResponseModel): String {
        var isOpen = false
        data.graphData?.caffeineWindowGraph?.let {
            val startTime = LocalTime.parse(it.startTime, DateTimeFormatter.ofPattern("HH:mm"))
            val endTime = LocalTime.parse(it.endTime, DateTimeFormatter.ofPattern("HH:mm"))

            val currentTime = LocalTime.now()


            if (currentTime in startTime..endTime) {
                isOpen = true
            }
        }

        return if (isOpen) {
            resourceProvider.getString(R.string.text_caffeine_window_open)
        } else {
            resourceProvider.getString(R.string.text_caffeine_window_closed)
        }
    }

}

enum class CorrectiveActivitiesEnum {
    LIGHT_EXPOSURE, DAILY_STEPS, MEAL_WINDOW, WORKOUT, CAFFEINE
}