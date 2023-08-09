package com.oreo.ui.home.summary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.ManualMeasureType
import com.noisefit_commans.models.ManualMeasurement
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.ChartModel
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.health.ODashboardSleepModel
import com.oreo.data.model.health.OreoDashboardResponseModel
import com.oreo.data.repository.abstraction.OreoSyncRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OSummaryViewModel
@Inject
constructor(
    val watchDataStore: WatchDataStore,
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface,
    val ringDataStore: RingDataStore,
    private val syncRepository: OreoSyncRepository,
    private val userRepository: OreoUserActivityRepository,
) : BaseViewModel() {


    var summary = OSummary()

    private var _deviceConnected: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var deviceConnected = _deviceConnected


    fun initData() {
        summary.user = localDataStore.getUser()

        getDashboardDataFromServer(true, true)
    }


    fun getDashboardDataFromServer(forceRefresh: Boolean, hitActivityData: Boolean) {
        if (!summary.healthOverviewData.value.isNullOrEmpty() && !forceRefresh) {
            summary.healthOverviewData.postValue(summary.healthOverviewData.value)
            return
        }
        viewModelScope.launch {
            userRepository.getDashboardData().collect { resource ->
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
                                        getDashboardDataFromServer(forceRefresh, hitActivityData)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            getInitialOfflineData(it, hitActivityData)
                        }
                    }
                }
            }
        }
    }


//    fun getUserActivities(healthOverviewDataType: HealthOverviewDataType, forceRefresh: Boolean) {
//        if (summary.healthOverviewData.value.isNullOrEmpty() || forceRefresh) {
//            getDashboardDataFromServer(forceRefresh)
//            return
//        }
//        viewModelScope.launch(Dispatchers.IO) {
//            val userActivities = userRepository.getHealthOverview(
//                healthOverviewDataType, ArrayList(summary.healthOverviewData.value!!)
//            )
//            summary.refreshPosition = userActivities.second
//
//            summary.healthOverviewData.postValue(userActivities.first)
//        }
//    }

    private fun convertIntToChartModel(data: List<Int>?): ArrayList<ChartModel> {
        val list = ArrayList<ChartModel>()
        val chartModel1 = ChartModel()
        chartModel1.date = ""
        chartModel1.index = ""
        chartModel1.value = 0
        list.add(chartModel1)
        data?.forEach {
            val chartModel = ChartModel()
            var value = it
            if (value < 0) {
                value = 0
            }
            chartModel.value = value//(10..100).random()
            chartModel.date = ""
            chartModel.index = ""
            list.add(chartModel)
        }

        return list
    }

    private fun getInitialOfflineData(data: OreoDashboardResponseModel, hitActivityData: Boolean) {


        viewModelScope.launch(Dispatchers.IO) {
            val userName = "${getGreetingMessage()}, ${
                summary.user?.getOnlyFirstName()?.trim()?.ifEmpty { "Stranger" }
            }"
            val userActivities = ArrayList<OHealthOverview>()

            val hrValue = userRepository.getSummaryHRHealthOverview()

            userActivities.add(
                0, OHealthOverview.Header(
                    userName,
                    DateFormats.getCurrentDate(DateFormats.dateTimeFormatWithWeekWithoutYear)
                )
            )
//            val autoSportCount = userRepository.getSummaryAutoWorkoutCount()
//            if (autoSportCount > 0) {
//                userActivities.add(OHealthOverview.AutoSport(autoSportCount))
//            }

//            userActivities.add(1, OHealthOverview.WAlert(2))
            if (ringDataStore.getRingDevice() == null) {
                userActivities.add(OHealthOverview.PairDevice())
            }

            ringDataStore.setRegisterDay(data.registerDate ?: -1)
            if (isMorningTime()) {
                if (data.registerDate != 0) {
                    data.readiness?.let {
                        userActivities.add(OHealthOverview.Readiness(data.readiness))
                    }


                    data.sleep?.let {
                        userActivities.add(
                            OHealthOverview.Sleep(
                                data.sleep,
                                makeSleepArray(data.sleep)
                            )
                        )
                    }
                }

                data.activity?.let {
                    val caloriesGoal = summary.user?.userGoals?.caloriesGoal ?: 0
                    userActivities.add(OHealthOverview.Activity(data.activity, caloriesGoal))
                }
            } else {

                data.activity?.let {
                    val caloriesGoal = summary.user?.userGoals?.caloriesGoal ?: 0
                    userActivities.add(OHealthOverview.Activity(data.activity, caloriesGoal))
                }

                if (data.registerDate != 0) {
                    data.readiness?.let {
                        userActivities.add(OHealthOverview.Readiness(data.readiness))
                    }
                    data.sleep?.let {
                        userActivities.add(
                            OHealthOverview.Sleep(
                                data.sleep,
                                makeSleepArray(data.sleep)
                            )
                        )
                    }
                }

            }

            if (hrValue != null) {
                userActivities.add(hrValue)
            }

            if (data.activityScoreAvg != null && data.sleepScoreAvg != null) {

                userActivities.add(
                    OHealthOverview.SleepActivityScore(
                        data.sleepScoreAvg.sleepScore,
                        data.sleepScoreAvg.trend,
                        convertIntToChartModel(data.sleepScoreAvg.value),
                        data.activityScoreAvg.activityScore,
                        data.activityScoreAvg.trend,
                        convertIntToChartModel(data.activityScoreAvg.value)
                    )
                )
            }

            if (data.readinessScoreAvg != null) {
                userActivities.add(
                    OHealthOverview.ReadinessScore(
                        data.readinessScoreAvg.readinessScore,
                        data.readinessScoreAvg.trend,
                        convertIntToChartModel(data.readinessScoreAvg.value)
                    )
                )
            }

            summary.healthOverviewData.postValue(userActivities)
            getRecentWorkoutList()

        }
    }

    private fun makeSleepArray(data: ODashboardSleepModel?): ArrayList<SleepData.SleepDataBreakup> {
        val sleepArray: ArrayList<SleepData.SleepDataBreakup> = ArrayList()

        if (data == null) {
            return sleepArray
        }

        data.sleepStage.forEach {
            val type = it.sleepType
            LOGS.d("makeSleepArray $type")
            if (type?.lowercase() == "awake") {
                sleepArray.add(
                    SleepData.SleepDataBreakup(
                        startTime = it.startTime,
                        endTime = it.endTime,
                        sleepType = "AWAKE",
                        duration = it.duration ?: 0
                    )
                )
            } else {
                sleepArray.add(
                    SleepData.SleepDataBreakup(
                        startTime = it.startTime,
                        endTime = it.endTime,
                        sleepType = "DEEP",
                        duration = it.duration ?: 0
                    )
                )
            }

        }
        return sleepArray
    }

    fun removeAutoWorkoutCard() {
        val index = summary.healthOverviewData.value?.indexOfFirst {
            it is OHealthOverview.AutoSport
        }
        if (index != null && index != -1) {
            summary.healthOverviewData.value?.removeAt(index)
            summary.healthOverviewData.postValue(summary.healthOverviewData.value)
        }
    }

    fun handleUnPairState() {
        val index = summary.healthOverviewData.value?.indexOfFirst {
            it is OHealthOverview.PairDevice
        }

         val autoSportIndex = summary.healthOverviewData.value?.indexOfFirst {
             it is OHealthOverview.TodayWorkout
         }

         if (autoSportIndex != null && autoSportIndex != -1) {
             summary.healthOverviewData.value?.removeAt(autoSportIndex)
         }
        if (index == -1) {
            summary.healthOverviewData.value?.add(1, OHealthOverview.PairDevice())
        }

        summary.healthOverviewData.postValue(summary.healthOverviewData.value)
    }

    fun updateManualValue() {
        val manualMeasurement = ringDataStore.getManualMeasurementValue()
        if (manualMeasurement.manualMeasureType == ManualMeasureType.HEART_RATE) {
            val index = summary.healthOverviewData.value?.indexOfFirst {
                it is OHealthOverview.HeartRate
            }
            if (index != null) {
                LOGS.d("dsasddsadsdads ${manualMeasurement.isError} ${manualMeasurement.isMeasuring}")
                val data = summary.healthOverviewData.value!![index] as OHealthOverview.HeartRate

                if (manualMeasurement.isError) {
                    data.errorMessage = "Unable to measure, try again"
                    data.value = "0"
                    data.isMeasuring = false
                } else {
                    data.lastTime = "Last measure now"
                    data.errorMessage = null
                    data.isMeasuring = manualMeasurement.isMeasuring
                    data.value = manualMeasurement.value.toString()
                }
                summary.refreshPosition = index
                summary.healthOverviewData.postValue(summary.healthOverviewData.value)
            }
        }
    }


    fun getRecentWorkoutList() {
        viewModelScope.launch {
            userRepository.getRecentWorkoutList(true).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
//                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getRecentWorkoutList()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let { workoutList ->
                            val index = summary.healthOverviewData.value?.indexOfFirst {
                                it is OHealthOverview.TodayWorkout
                            }
                            val isRingConnected = ringDataStore.getRingDevice() != null

                            if (index != null && index != -1) {
                                val data =
                                    summary.healthOverviewData.value!![index] as OHealthOverview.TodayWorkout
                                data.value = "1"
                                data.listData = workoutList
                                data.isRingConnected = isRingConnected
                                summary.healthOverviewData.postValue(summary.healthOverviewData.value)
                            } else {

                                if (isRingConnected || workoutList.isNotEmpty()) {
                                    summary.healthOverviewData.value?.add(
                                        OHealthOverview.TodayWorkout(
                                            "1",
                                            isRingConnected,
                                            workoutList
                                        )
                                    )
                                    summary.healthOverviewData.postValue(summary.healthOverviewData.value)
                                }
                            }
                        }
                    }
                }
            }
        }


    }


    fun checkBatteryPercentage() {
        sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
    }

    fun measureHr(status: Boolean) {
        LOGS.d("manual HR")
        LOGS.d("onMeasuring manual HR")
        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetManualMeasurement(
                ManualMeasureType.HEART_RATE, status
            )
        )

    }

    private fun isMorningTime(): Boolean {
        val currentTime = DateFormats.getTimeFormat()
        if (DateFormats.isTimeBetween(currentTime, "06:00", "12:00")) {
            return true
        }
        return false
    }

    private fun getGreetingMessage(): String {
        val currentTime = DateFormats.getTimeFormat()
        if (DateFormats.isTimeBetween(currentTime, "04:00", "11:59")) {
            return "Good morning"
        } else if (DateFormats.isTimeBetween(currentTime, "12:00", "16:59")) {
            return "Good afternoon"
        } else if (DateFormats.isTimeBetween(currentTime, "17:00", "20:59")) {
            return "Good evening"
        } else if (DateFormats.isTimeBetween(
                currentTime, "21:00", "23:59"
            ) || DateFormats.isTimeBetween(currentTime, "00:00", "03:59")
        ) {
            return "Hi"
        }

        return "Hi"
    }

    fun isDeviceConnected(): Boolean {
        if (getDeviceConnected() == null) {
            return false
        }

        if (sessionManager.connectStateRing.value is ConnectState.ConnectSuccess) {
            return true
        }
        return false
    }

    fun deleteAllAutoWorkout() {
        viewModelScope.launch {
            syncRepository.deleteAllAutoWorkoutData().collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }
        }
    }

    fun getDeviceConnected(): ColorFitDevice? {
        return ringDataStore.getRingDevice()
    }

    fun updateDeviceConnectedStatus() {
        _deviceConnected.value = (ringDataStore.getRingDevice() != null)
    }
}