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
import com.noisefit_commans.data.model.circadian.NudgeCircadianGraph
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.delay
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.CorrectiveActivitiesModel
import com.oreo.data.model.OnlyImgWithText
import com.oreo.data.model.ProgressBarLytData
import com.oreo.data.model.TimeWindow
import com.oreo.data.model.circadian.Activity
import com.oreo.data.model.circadian.CircadianResponseModel
import com.oreo.ui.custom.ClockEvent
import com.oreo.ui.custom.ClockEventType
import com.oreo.ui.stress.help.HowItWorksModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.exp
import kotlin.math.pow

@HiltViewModel
class CircadianAlignmentViewModel
@Inject constructor(
    private val resourceProvider: ResourcesProvider,
    private val userRepository: UserRepository,
    val localDataStore: DataStoredInterface,
) : BaseViewModel() {

    companion object {
        const val light_exposure_key = "light_exposure"
        const val daily_steps_key = "daily_steps"
        const val meal_window_key = "meal_window"
        const val caffeine_window_key = "caffeine_window"
        const val workout_key = "workout"
        const val sleep_key = "sleep"
        val actMonStatusList = listOf("partial", "done", "not-done")
    }

    val lightExposureData = MutableLiveData<CorrectiveActivitiesModel>()
    val dailyStepsData = MutableLiveData<CorrectiveActivitiesModel>()
    val mealWindowData = MutableLiveData<CorrectiveActivitiesModel>()
    val workoutData = MutableLiveData<CorrectiveActivitiesModel>()
    val caffeineWindowData = MutableLiveData<CorrectiveActivitiesModel>()

    val circadianResponseData = MutableLiveData<CircadianResponseModel>()

    val correctiveActivitiesListData = MutableLiveData<ArrayList<CorrectiveActivitiesModel>>()

    val howItWorksDataList = MutableLiveData<List<HowItWorksModel.CircadianHowItWorksModel>>()

    val nudgeData = MutableLiveData<Pair<NudgeCircadianGraph?, Boolean>>()

    private val timerMap = mutableMapOf<String, CountDownTimer>()

    fun initData() {

       /* val data =
            "{ \"activities\": [ { \"type\": \"light_exposure\", \"goal\": 120, \"time\": 7267, \"status\": true }, { \"type\": \"meal_window\", \"time\": 41467, \"status\": true }, { \"type\": \"caffeine_window\", \"time\": 27067, \"status\": true }, { \"type\": \"workout\", \"time\": 36067, \"goal\": 440, \"status\": false, \"progress\": 836, \"active_calories\": 836, \"total_calories\": 1632 }, { \"type\": \"daily_steps\", \"goal\": 10000, \"time\": 36067, \"progress\": 1641 } ], \"activity_monitor\": [ { \"type\": \"light_exposure\", \"status\": \"partial\" }, { \"type\": \"daily_steps\", \"status\": \"partial\" }, { \"type\": \"meal_window\", \"status\": \"done\" }, { \"type\": \"caffeine_window\", \"status\": \"done\" }, { \"type\": \"workout\", \"status\": \"partial\" } ], \"chronotype\": { \"type\": \"Moderately evening type\", \"introduction\": \"You’re naturally inclined to be a night owl.\", \"description\": \"Your creativity and focus peak in the afternoon or early evening. Plan for a gentler start and build momentum into your afternoon routines.\" }, \"graph_data\": { \"caffeine_window_graph\": { \"start_time\": \"09:40\", \"end_time\": \"15:40\" }, \"melatonin_prep_phase_window_graph\": { \"start_time\": \"18:10\", \"end_time\": \"21:10\" }, \"dlmo_phase_window_graph\": { \"start_time\": \"21:10\", \"end_time\": \"23:40\" }, \"cortisol_peak_window_graph\": { \"start_time\": \"07:40\", \"end_time\": \"08:10\" }, \"light_anchoring_phase_window_graph\": { \"start_time\": \"07:40\", \"end_time\": \"09:10\" }, \"first_focus_peak_window_graph\": { \"start_time\": \"09:10\", \"end_time\": \"12:10\", \"peak_time\": \"10:40\" }, \"second_focus_peak_window_graph\": { \"start_time\": \"14:10\", \"end_time\": \"17:10\", \"peak_time\": \"15:40\" }, \"sleep_window_opens_graph\": { \"start_time\": \"23:40\" }, \"gh_pulse_window_graph\": { \"start_time\": \"04:10\" }, \"activity_window_graph\": { \"start_time\": \"07:40\", \"end_time\": \"17:10\" }, \"circadian_mid_point\": { \"start_time\": \"2025-08-25 00:45:00\", \"end_time\": \"2025-08-25 03:15:00\", \"circadian_midpoint\": \"2025-08-25 04:27:00\", \"avg_now\": \"2025-08-25 04:21:42\", \"avg_before\": \"2025-08-25 04:20:22\", \"chronotype\": \"Moderately morning type\", \"nudge\": { \"title\": \"Stay Active During Your Focus Window\", \"description\": \"Incorporate light physical activities like walking or stretching during your focus window to boost a\" } }, \"energy_graph\": [ { \"start_time\": \"07:40\", \"energy\": 0.011 }, { \"start_time\": \"08:40\", \"energy\": 0.135 }, { \"start_time\": \"09:40\", \"energy\": 0.607 }, { \"start_time\": \"10:40\", \"energy\": 1.004 }, { \"start_time\": \"11:40\", \"energy\": 0.639 }, { \"start_time\": \"12:40\", \"energy\": 0.23 }, { \"start_time\": \"13:40\", \"energy\": 0.181 }, { \"start_time\": \"14:40\", \"energy\": 0.397 }, { \"start_time\": \"15:40\", \"energy\": 0.604 }, { \"start_time\": \"16:40\", \"energy\": 0.364 }, { \"start_time\": \"17:40\", \"energy\": 0.081 }, { \"start_time\": \"18:40\", \"energy\": 0.007 }, { \"start_time\": \"19:40\", \"energy\": 0 }, { \"start_time\": \"20:40\", \"energy\": 0 }, { \"start_time\": \"21:40\", \"energy\": 0 }, { \"start_time\": \"22:40\", \"energy\": 0 }, { \"start_time\": \"23:40\", \"energy\": 0 } ], \"sleep_data\": { \"bed_time\": \"2025-08-25 23:40:00\", \"wake_time\": \"2025-08-25 07:40:00\" }, \"start_time\": \"2025-08-25 07:40:00\", \"end_time\": \"2025-08-25 23:40:00\" }, \"is_locked\": false }"
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
                            prepareCorrectiveActivitiesData(
                                it.activities,
                                it.isLockedCircularView!=true && it.graphData?.sleepData != null
                            )

                            delay(5000,{
                                getNudgeCircadianData(it)
                            })
                            LOGS.d("abcjacjcab Posting data: $it")
                        }
                    }
                }
            }
        }
    }

    private fun getNudgeCircadianData(data: CircadianResponseModel) {
        viewModelScope.launch {
            nudgeData.postValue(
                Pair(
                    null,
                    true
                )
            )
            if(data.isLockedCircularView == true){
                nudgeData.postValue(
                    Pair(
                        NudgeCircadianGraph(
                            title = resourceProvider.getString(R.string.text_start_fresh_today),
                            description = resourceProvider.getString(R.string.text_focus_window_desc1)
                        ),
                        false
                    )
                )
            }else{
                val graphData = data.graphData
                if(graphData?.startTime == null || graphData.endTime == null ||
                    graphData.sleepData?.wakeTime == null || graphData.sleepData?.bedTime == null){
                    nudgeData.postValue(
                        Pair(
                            NudgeCircadianGraph(
                                title = resourceProvider.getString(R.string.text_guidance_resumes_soon),
                                description = resourceProvider.getString(R.string.text_focus_window_desc2)
                            ),
                            false
                        )
                    )
                }else{
                    val reqObj = JsonObject().apply {
                        this.addProperty("type", "circadian")
                    }
                    userRepository.getNudgeCircadianData(reqObj).collect{resource ->
                        when (resource) {
                            is Resource.GenericError -> {
                                /*sendMessage(resource.message)*/
                            }

                            is Resource.Loading -> {
                                /*setLoading(resource.loading)*/
                            }

                            is Resource.NetworkError -> {
                                /*setApiErrors(resource.response.apply {
                                    (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                        object : BinaryActionCallback {
                                            override fun yes() {
                                                getNudgeCircadianData(data)
                                            }

                                            override fun no() {}
                                        }
                                })*/
                            }

                            is Resource.Success -> {
                                resource.data?.data?.let {
                                    nudgeData.postValue(Pair(it, false))
                                }
                            }
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
                txt = "-"
            ),
        )

        dailyStepsData.value = CorrectiveActivitiesModel(
            key = daily_steps_key,
            bgMainImg = R.drawable.bg_daily_steps_corrective_activities,
            title = resourceProvider.getString(R.string.text_daily_steps),
            desc = resourceProvider.getString(R.string.text_corrective_activities_circadian_desc_2),
            progressBarLytData = ProgressBarLytData(
                totalProgress = null,
                currentProgress = 0,
                img = R.drawable.ic_shoe_corrective_activities,
                txt = "-"
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
                totalProgress = null,
                currentProgress = 0,
                img = R.drawable.ic_workout_corrective_activities,
                txt = "-"
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
                                ) "-" else curData.goal?.let { "$it\nmins" } ?: "-"
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
                                totalProgress = goal,
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
                                totalProgress = goal,
                                currentProgress = goal?.let { ((it / 60) * 0.1).toInt() } ?: 0,
                                img = R.drawable.ic_workout_corrective_activities,
                                txt = if ((curData.time
                                        ?: 0) <= 0
                                ) "-" else  curData.goal?.let { "$it kcal" } ?: "-"
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
                    label = resourceProvider.getString(R.string.text_caffeine_window_open)
                )
            )
            circadianGraphData?.sleepData?.let { sleepTime->
                if (sleepTime.wakeTime == null || sleepTime.bedTime == null) return@let

                val wakeTime = LocalDateTime.parse(
                    sleepTime.wakeTime,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                ).plusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm"))
                val bedTime = LocalDateTime.parse(
                    sleepTime.bedTime,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                ).format(DateTimeFormatter.ofPattern("HH:mm"))

                data.add(
                    TimeWindow(
                        getCircadianTimeFloatValue(wakeTime),
                        getCircadianTimeFloatValue(it.startTime),
                        "#CC2E2422".toColorInt(),
                        "#CC2E2422".toColorInt(),
                        "#B2D69B92".toColorInt(),
                        rowIndex = 1,
                        label = resourceProvider.getString(R.string.text_avoid_caffeine)
                    )
                )

                data.add(
                    TimeWindow(
                        getCircadianTimeFloatValue(it.endTime),
                        getCircadianTimeFloatValue(bedTime),
                        "#CC2E2422".toColorInt(),
                        "#CC2E2422".toColorInt(),
                        "#B2D69B92".toColorInt(),
                        rowIndex = 1,
                        label = resourceProvider.getString(R.string.text_avoid_caffeine)
                    )
                )




            }


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
                    label = resourceProvider.getString(R.string.text_dim_light_phase)
                )
            )
        }

        circadianGraphData?.lightAnchoringPhaseWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let


            val startTime = LocalTime.parse(it.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                .plusMinutes(1)
                .format(DateTimeFormatter.ofPattern("HH:mm"))

            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#B2E6EE".toColorInt(),
                    "#FFE0BC".toColorInt(),
                    "#99000000".toColorInt(),
                    rowIndex = 0,
                    label = resourceProvider.getString(R.string.text_natural_light)
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
                    label = resourceProvider.getString(R.string.text_sleep)
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
                    label = resourceProvider.getString(R.string.text_evening_wind_down)
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
                        label = resourceProvider.getString(R.string.text_neutral_light_zone)
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

    fun initHowItWorksData() {
        viewModelScope.launch(Dispatchers.IO) {
            howItWorksDataList.postValue(
                arrayListOf(
                    HowItWorksModel.CircadianHowItWorksModel(
                        title = resourceProvider.getString(R.string.text_what_is_circadian_alignment),
                        image = R.drawable.image_circadian_hiw_1,
                    ),
                    HowItWorksModel.CircadianHowItWorksModel(
                        title = resourceProvider.getString(R.string.text_hiw_circadian_card_title_2),
                        image = R.drawable.image_circadian_hiw_2,
                    ),
                    HowItWorksModel.CircadianHowItWorksModel(
                        title = resourceProvider.getString(R.string.text_hiw_circadian_card_title_3),
                        image = R.drawable.image_circadian_hiw_3,
                    ),
                    HowItWorksModel.CircadianHowItWorksModel(
                        title = resourceProvider.getString(R.string.text_hiw_circadian_card_title_4),
                        image = R.drawable.image_circadian_hiw_4,
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
                    label = resourceProvider.getString(R.string.text_sleep)
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
                    resourceProvider.getString(R.string.text_natural_light)/*resourceProvider.getString(R.string.text_sleep)*/
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
                    resourceProvider.getString(R.string.text_wind_down)
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
                        label = resourceProvider.getString(R.string.text_neutral_light)
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
                    resourceProvider.getString(R.string.text_dim_light)
                )
            )
        }

        return response
    }

    fun generateValuesData(energyValues: List<Float>?): ArrayList<Float> {
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

    fun getCaffeineState(data: CircadianResponseModel): String? {
        val currentTime = LocalTime.now()
        try {
            data.graphData?.let {

                it.firstFocusPeakWindowGraph?.let {
                    val startTime =
                        LocalTime.parse(it.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                    val endTime = LocalTime.parse(it.endTime, DateTimeFormatter.ofPattern("HH:mm"))

                    if (currentTime in startTime..endTime) {
                        return resourceProvider.getString(R.string.text_high_focus_peak)
                    }
                }

                it.secondFocusPeakWindowGraph?.let {
                    val startTime =
                        LocalTime.parse(it.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                    val endTime = LocalTime.parse(it.endTime, DateTimeFormatter.ofPattern("HH:mm"))

                    if (currentTime in startTime..endTime) {
                        return resourceProvider.getString(R.string.text_moderate_focus_peak)
                    }
                }

                it.caffeineWindowGraph?.let {
                    val startTime = LocalTime.parse(it.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                    val endTime = LocalTime.parse(it.endTime, DateTimeFormatter.ofPattern("HH:mm"))

                    if (currentTime in startTime..endTime) {
                        return resourceProvider.getString(R.string.text_caffeine_window2)
                    }
                }

                it.lightAnchoringPhaseWindowGraph?.let {
                    val startTime =
                        LocalTime.parse(it.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                    val endTime = LocalTime.parse(it.endTime, DateTimeFormatter.ofPattern("HH:mm"))

                    if (currentTime in startTime..endTime) {
                        return resourceProvider.getString(R.string.text_neutral_light_zone)
                    }
                }

                it.melatoninPrepPhaseWindowGraph?.let {
                    val startTime =
                        LocalTime.parse(it.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                    val endTime = LocalTime.parse(it.endTime, DateTimeFormatter.ofPattern("HH:mm"))

                    if (currentTime in startTime..endTime) {
                        return resourceProvider.getString(R.string.text_evening_wind_down)
                    }
                }

                it.dlmoPhaseWindowGraph?.let {
                    val startTime =
                        LocalTime.parse(it.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                    val endTime = LocalTime.parse(it.endTime, DateTimeFormatter.ofPattern("HH:mm"))

                    if (currentTime in startTime..endTime) {
                        return resourceProvider.getString(R.string.text_dim_light_phase)
                    }
                }

                it.sleepWindowOpensGraph?.let { sleepWindowOpensData ->
                    val startTime =
                        LocalTime.parse(sleepWindowOpensData.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                    val endTime = (LocalDateTime.parse(it.sleepData?.wakeTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).toLocalTime()
                    if (currentTime in startTime..endTime) {
                        return resourceProvider.getString(R.string.text_sleep_window)
                    }
                }

            }
        }catch (e: Exception){
            LOGS.d("CIRCADIAN_CURRENT_WINDOW_EXP: $e")
        }

        return null
    }

    fun getEnergyValues(graphData: CircadianGraphData?,rotate: Boolean): List<Float> {
        if(graphData?.firstFocusPeakWindowGraph==null && graphData?.secondFocusPeakWindowGraph==null){
            return ArrayList()
        }

        val graphStart = graphData.startTime
        val graphEnd = graphData.sleepData?.wakeTime

        if(graphStart==null || graphEnd==null) return ArrayList()


        val totalMinutes = 24 * 60
        val energyValues = MutableList(totalMinutes) { 0f }

        val windows = listOfNotNull(
            graphData.firstFocusPeakWindowGraph,
            graphData.secondFocusPeakWindowGraph
        )

        for (window in windows) {
            val startMin = timeToMinutes(window.startTime)
            val endMin = timeToMinutes(window.endTime)
            val peakMin = timeToMinutes(window.peakTime)

            if (startMin != null && endMin != null && peakMin != null) {
                for (minute in startMin..endMin) {
                    val dist = (minute - peakMin).toFloat()
                    // Gaussian-like curve: highest at peak, lower at edges
                    val sigma = (endMin - startMin) / 6f // spread factor
                    val energy = exp(-0.5f * (dist / sigma).pow(2))
                    energyValues[minute % totalMinutes] += energy.toFloat()
                }
            }
        }

        val maxVal = energyValues.maxOrNull() ?: 1f
        val values =  energyValues.map { it / maxVal }
        if(rotate){
            return trimArrayByTime(values, graphStart)
        }else{
            return values
        }

    }

    fun trimArrayByTime(
        array: List<Float>,
        startTime: String
    ): List<Float> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val start = LocalDateTime.parse(startTime, formatter)

        val startIndex = start.hour * 60 + start.minute

        if (array.size != 24 * 60) {
            return ArrayList()
        }

        return array.drop(startIndex) + array.take(startIndex)
    }

    private fun timeToMinutes(time: String?): Int? {
        if (time == null) return null
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = sdf.parse(time)
            val cal = Calendar.getInstance()
            cal.time = date!!
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        } catch (e: Exception) {
            null
        }
    }

}

enum class CorrectiveActivitiesEnum {
    LIGHT_EXPOSURE, DAILY_STEPS, MEAL_WINDOW, WORKOUT, CAFFEINE
}