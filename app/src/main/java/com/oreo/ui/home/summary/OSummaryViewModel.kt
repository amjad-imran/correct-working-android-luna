package com.oreo.ui.home.summary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.fromJson
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
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.AlertType
import com.oreo.data.model.ChartModel
import com.oreo.data.model.DashAlert
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.TapMeasureState
import com.oreo.data.model.health.ODashboardActivityScoreModel
import com.oreo.data.model.health.ODashboardReadinessScoreModel
import com.oreo.data.model.health.ODashboardSleepModel
import com.oreo.data.model.health.ODashboardSleepScoreModel
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
    val userRepository: OreoUserActivityRepository,
) : BaseViewModel() {


    val stateHeaderCard = MutableLiveData<Pair<String, String>>()//Name,Date
    val stateHeartRateCard = MutableLiveData<OHealthOverview.HeartRate?>()
    val statePairDeviceCard = MutableLiveData<Boolean>()
    val stateDashAlerts = MutableLiveData<HashMap<AlertType, DashAlert>>()
    val stateSleepAvgCard =
        MutableLiveData<Pair<ODashboardSleepScoreModel?, ODashboardActivityScoreModel?>>()
    val stateReadinessAvgCard = MutableLiveData<ODashboardReadinessScoreModel?>()
    val stateWorkouts = MutableLiveData<List<OActivityListModal>>()

    var sleepScoreInfo: String? = null
    var readinessScoreInfo: String? = null
    var activityScoreInfo: String? = null


    var summary = OSummary()

    private var _deviceConnected: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var deviceConnected = _deviceConnected


    fun initData() {

        viewModelScope.launch(Dispatchers.IO) {
            summary.user = localDataStore.getUser()
            stateHeaderCard.postValue(
                Pair(
                    getGreetingMessageValue(),
                    DateFormats.getCurrentDate(DateFormats.dateTimeFormatWithWeekWithoutYear)
                )
            )
            val device = getDeviceConnected()
            statePairDeviceCard.postValue(device == null)
            stateHeartRateCard.postValue(userRepository.getSummaryHRHealthOverview().apply {
                this?.infoContent = ""
                if (device == null) {
                    this?.measureState = TapMeasureState.NO_DEVICE
                }
            })
        }

        updateAlerts()




        getDashboardDataFromServer(false)
    }

    fun updateAlerts() {
        val dashAlert = HashMap<AlertType, DashAlert>()

        val btState = sessionManager.bluetoothStateDash.value
        val devicePaired = ringDataStore.getRingDevice()
        if (btState == false && devicePaired != null) {
            if (sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess) {
                dashAlert[AlertType.BLUETOOTH] =
                    DashAlert("Authorize Bluetooth connectivity for Luna", false)
            }
        }

        if (sessionManager.forceOtaResponseRing != null) {
            dashAlert[AlertType.OTA_UPDATE] =
                DashAlert("Ring firmware update available", false)
        }


        stateDashAlerts.postValue(dashAlert)
    }


    fun getDashboardDataFromServer(forceRefresh: Boolean) {
        LOGS.d("FORCE_REFRESH $forceRefresh")

        viewModelScope.launch {
            userRepository.getDashboardData(forceRefresh).collect { resource ->
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
                                        getDashboardDataFromServer(forceRefresh)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            getInitialOfflineData(it)
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

    fun convertIntToChartModel(data: List<Int>?): ArrayList<ChartModel> {
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

    fun getGreetingMessageValue(): String {
        return "${getGreetingMessage()}, ${
            summary.user?.getOnlyFirstName()?.trim()?.ifEmpty { "Stranger" }
        }"
    }

    private fun getInitialOfflineData(data: OreoDashboardResponseModel) {


        viewModelScope.launch(Dispatchers.IO) {

            val userActivities = ArrayList<OHealthOverview>()
            sleepScoreInfo = data.sleep_score
            readinessScoreInfo = data.readiness_score
            activityScoreInfo = data.activity_score


            val autoSportCount = userRepository.getSummaryAutoWorkoutCount()
            if (autoSportCount > 0) {
                userActivities.add(OHealthOverview.AutoSport(autoSportCount))
            }
            //userActivities.add(OHealthOverview.AutoSport(2))


            ringDataStore.setRegisterDay(data.registerDate ?: -1)


            LOGS.w("RESPONSE___ ${Gson().toJson(data)}")


            when (getDaySlot()) {
                0 -> {
                    //sleep
                    if (data.sleep?.sleepScore != null) {
                        if (data.registerDate != 0) {
                            data.readiness?.let {
                                userActivities.add(OHealthOverview.Readiness(data.readiness))
                            }
                            userActivities.add(
                                OHealthOverview.Sleep(
                                    data.sleep,
                                    makeSleepArray(data.sleep),
                                    data.sleep.sleepStage.firstOrNull()?.startTime ?: "",
                                    data.sleep.sleepStage.lastOrNull()?.endTime ?: ""
                                )
                            )
                        }
                    } else {
                        userActivities.add(OHealthOverview.SleepWaiting)
                    }
                }

                1 -> {

                    //sleep
                    if (data.sleep?.sleepScore != null) {
                        if (data.registerDate != 0) {
                            data.readiness?.let {
                                userActivities.add(OHealthOverview.Readiness(data.readiness))
                            }
                            userActivities.add(
                                OHealthOverview.Sleep(
                                    data.sleep,
                                    makeSleepArray(data.sleep),
                                    data.sleep.sleepStage.firstOrNull()?.startTime ?: "",
                                    data.sleep.sleepStage.lastOrNull()?.endTime ?: ""
                                )
                            )
                        }
                    } else {
                        userActivities.add(OHealthOverview.SleepWaiting)
                    }

                    //Activity
                    if (data.activity?.activeCalories != null) {
                        val activeCalories = data.activity.activeCalories
                        if (activeCalories in 1..49) {
                            val caloriesGoal = summary.user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.ActivityMinimal(
                                    data.activity,
                                    caloriesGoal
                                )
                            )
                        } else if (activeCalories >= 50) {
                            val caloriesGoal = summary.user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.Activity(
                                    data.activity,
                                    caloriesGoal
                                )
                            )
                        } else {
                        }
                    }
                }

                2 -> {
                    if (data.registerDate != 0) {
                        data.readiness?.let {
                            userActivities.add(OHealthOverview.Readiness(data.readiness))
                        }

                        data.sleep?.let {
                            userActivities.add(
                                OHealthOverview.Sleep(
                                    data.sleep,
                                    makeSleepArray(data.sleep),
                                    data.sleep.sleepStage.firstOrNull()?.startTime ?: "",
                                    data.sleep.sleepStage.lastOrNull()?.endTime ?: ""
                                )
                            )
                        }
                    }

                    data.activity?.let {

                        val activeCalories = data.activity.activeCalories ?: 0
                        if (activeCalories in 0..49) {
                            val caloriesGoal = summary.user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.ActivityMinimal(
                                    data.activity,
                                    caloriesGoal
                                )
                            )
                        } else {
                            val caloriesGoal = summary.user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.Activity(
                                    data.activity,
                                    caloriesGoal
                                )
                            )
                        }
                    }


                }

                else -> {
                    data.activity?.let {

                        val activeCalories = data.activity.activeCalories ?: 0
                        if (activeCalories in 0..49) {
                            val caloriesGoal = summary.user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.ActivityMinimal(
                                    data.activity,
                                    caloriesGoal
                                )
                            )
                        } else {
                            val caloriesGoal = summary.user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.Activity(
                                    data.activity,
                                    caloriesGoal
                                )
                            )
                        }
                    }

                    if (data.registerDate != 0) {
                        if (data.sleep?.sleepScore != null) {
                            data.readiness?.let {
                                userActivities.add(OHealthOverview.ReadinessMinimal(data.readiness))
                            }
                            userActivities.add(
                                OHealthOverview.SleepMinimal(
                                    data.sleep,
                                    makeSleepArray(data.sleep)
                                )
                            )
                        } else {
                            data.readiness?.let {
                                userActivities.add(OHealthOverview.Readiness(data.readiness))
                            }
                            data.sleep?.let {
                                userActivities.add(
                                    OHealthOverview.Sleep(
                                        data.sleep,
                                        makeSleepArray(data.sleep),
                                        data.sleep.sleepStage.firstOrNull()?.startTime ?: "",
                                        data.sleep.sleepStage.lastOrNull()?.endTime ?: ""
                                    )
                                )
                            }
                        }
                    }
                }
            }


            /* if (isMorningTime()) {
                 if (data.registerDate != 0) {
                     data.readiness?.let {
                         userActivities.add(OHealthOverview.Readiness(data.readiness))
                     }


                     data.sleep?.let {
                         userActivities.add(
                             OHealthOverview.Sleep(
                                 data.sleep,
                                 makeSleepArray(data.sleep),
                                 data.sleep.sleepStage.firstOrNull()?.startTime ?: "",
                                 data.sleep.sleepStage.lastOrNull()?.endTime ?: ""
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
                                 makeSleepArray(data.sleep),
                                 data.sleep.sleepStage.firstOrNull()?.startTime ?: "",
                                 data.sleep.sleepStage.lastOrNull()?.endTime ?: ""
                             )
                         )
                     }
                 }

             }*/

            stateSleepAvgCard.postValue(Pair(data.sleepScoreAvg, data.activityScoreAvg))
            stateReadinessAvgCard.postValue(data.readinessScoreAvg)

            summary.healthOverviewData.postValue(userActivities)

            val device = ringDataStore.getRingDevice()
            stateHeartRateCard.postValue(userRepository.getSummaryHRHealthOverview().apply {
                this?.infoContent = data.hr_graph_dash
                if (device == null) {
                    this?.measureState = TapMeasureState.NO_DEVICE
                }
            })
            getRecentWorkoutList()

        }
    }

    private fun makeSleepArray(data: ODashboardSleepModel?): ArrayList<SleepData.SleepDataBreakup> {
        val sleepArray: ArrayList<SleepData.SleepDataBreakup> = ArrayList()

        if (data == null) {
            return sleepArray
        }

        var duration = 0

        data.sleepStage.forEachIndexed { index, data1 ->
            val type = data1.sleepType


            if (type?.lowercase() == "awake") {
                if (duration != 0) {

                    sleepArray.add(
                        SleepData.SleepDataBreakup(
                            startTime = data1.startTime,
                            endTime = data1.endTime,
                            sleepType = "DEEP",
                            duration = duration
                        )
                    )
                    duration = 0
                }
                sleepArray.add(
                    SleepData.SleepDataBreakup(
                        startTime = data1.startTime,
                        endTime = data1.endTime,
                        sleepType = "AWAKE",
                        duration = data1.duration ?: 0
                    )
                )
            } else {
                duration += (data1.duration?.toInt()) ?: 0
            }

            if (index == data.sleepStage.size - 1 && duration != 0) {

                sleepArray.add(
                    SleepData.SleepDataBreakup(
                        startTime = data1.startTime,
                        endTime = data1.endTime,
                        sleepType = "DEEP",
                        duration = duration
                    )
                )
                duration = 0
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
        /*   val index = summary.healthOverviewData.value?.indexOfFirst {
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

           summary.healthOverviewData.postValue(summary.healthOverviewData.value)*/


    }

    fun updateManualValue() {
        val manualMeasurement = ringDataStore.getManualMeasurementValue()
        if (manualMeasurement != null && manualMeasurement.manualMeasureType == ManualMeasureType.HEART_RATE) {


            if (manualMeasurement.isError) {
                stateHeartRateCard.value?.measureState = TapMeasureState.ERROR
            } else {
                if (manualMeasurement.isMeasuring) {
                    stateHeartRateCard.value?.measureState = TapMeasureState.MEASURING
                } else {
                    stateHeartRateCard.value?.measureState = TapMeasureState.LAST_MEASURED
                    stateHeartRateCard.value?.lastTime = "Last measured just now"
                }
                stateHeartRateCard.value?.value = manualMeasurement.value.toString()
            }
            stateHeartRateCard.postValue(stateHeartRateCard.value)


            /*    val index = summary.healthOverviewData.value?.indexOfFirst {
                    it is OHealthOverview.HeartRate
                }
                if (index != null) {
                    LOGS.d("dsasddsadsdads ${manualMeasurement.isError} ${manualMeasurement.isMeasuring}")
                    val data = summary.healthOverviewData.value!![index] as OHealthOverview.HeartRate

                    if (manualMeasurement.isError) {
                        *//*data.errorMessage = "Unable to measure, try again"
                    data.value = "0"
                    data.isMeasuring = false*//*

                    data.measureState = TapMeasureState.ERROR
                } else {

                    if (manualMeasurement.isMeasuring) {
                        data.measureState = TapMeasureState.MEASURING
                    } else {
                        data.measureState = TapMeasureState.LAST_MEASURED
                    }

                    //data.lastTime = "Last measure now"
                    *//*data.errorMessage = null
                    data.isMeasuring = manualMeasurement.isMeasuring*//*
                    data.value = manualMeasurement.value.toString()
                }
                summary.refreshPosition = index
                summary.healthOverviewData.postValue(summary.healthOverviewData.value)
            }*/
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

                            stateWorkouts.postValue(workoutList)


                            /*  val index = summary.healthOverviewData.value?.indexOfFirst {
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
                              }*/
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
        stateHeartRateCard.value?.measureState = TapMeasureState.MEASURING
        stateHeartRateCard.postValue(stateHeartRateCard.value)


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


    /**
     * Return day slots
     * 1->00:00 - 08:00
     * 2->08:00 - 12:000
     * 3->12:00 - 24:00
     */
    private fun getDaySlot(): Int {
        val currentTime = DateFormats.getTimeFormat()
        return if (DateFormats.isTimeBetween(currentTime, "00:00", "03:59")) {
            0
        } else if (DateFormats.isTimeBetween(currentTime, "04:00", "07:59")) {
            1
        } else if (DateFormats.isTimeBetween(currentTime, "08:00", "11:59")) {
            2
        } else {
            3
        }
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

    fun updateBluetoothStateInList(it: Boolean) {
        if (it) {
            stateDashAlerts.value?.remove(AlertType.BLUETOOTH)
        } else {
            val ringDevice = getDeviceConnected()
            if (ringDevice != null) {
                if (sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess) {
                    stateDashAlerts.value?.set(
                        AlertType.BLUETOOTH,
                        DashAlert("Authorize Bluetooth connectivity for Luna", false)
                    )
                } else {
                    stateDashAlerts.value?.remove(AlertType.BLUETOOTH)
                }
            }
        }
        stateDashAlerts.postValue(stateDashAlerts.value)
    }
}