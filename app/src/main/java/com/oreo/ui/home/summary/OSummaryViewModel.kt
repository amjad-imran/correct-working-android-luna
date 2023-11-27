package com.oreo.ui.home.summary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit.util.notif.NotificationEventsClass
import com.noisefit.util.notif.NotificationUtil
import com.noisefit_commans.common.checkDayDifferenceMoreNMinutes
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.enums.DashInfoCard
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
import com.noisefit_commans.ui.checkDayDifferenceMoreOne
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.AlertType
import com.oreo.data.model.ChartModel
import com.oreo.data.model.DashAlert
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OContributorResponseModal
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.TapMeasureState
import com.oreo.data.model.VideoInfoType
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
import java.time.LocalTime
import javax.inject.Inject


@HiltViewModel
class OSummaryViewModel
@Inject
constructor(
    val watchDataStore: WatchDataStore,
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface,
    val ringDataStore: RingDataStore,
    val userActivityRepository: OreoUserActivityRepository,
    private val syncRepository: OreoSyncRepository,
    val userRepository: OreoUserActivityRepository,
) : BaseViewModel() {


    val stateHeaderCard = MutableLiveData<Pair<String, String>>()//Name,Date
    val stateHeartRateCard = MutableLiveData<OHealthOverview.HeartRate?>()
    val statePairDeviceCard = MutableLiveData<Boolean>()
    val stateDashAlerts = MutableLiveData<HashMap<AlertType, DashAlert>>()
    val stateDashRingBattery = MutableLiveData<Pair<Boolean, ColorFitDevice?>>()

    val stateSleepAvgCard =
        MutableLiveData<Pair<ODashboardSleepScoreModel?, ODashboardActivityScoreModel?>>()
    val stateReadinessAvgCard = MutableLiveData<ODashboardReadinessScoreModel?>()
    val stateWorkouts = MutableLiveData<List<OActivityListModal>>()
    var contributorInfo: OContributorResponseModal? = null

    val hrInfo = MutableLiveData<Event<String>>()
    var sleepScoreInfo = MutableLiveData<Event<String>>()
    var readinessScoreInfo = MutableLiveData<Event<String>>()
    var activityScoreInfo = MutableLiveData<Event<String>>()



    var summary = OSummary()

    private var _deviceConnected: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var deviceConnected = _deviceConnected


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

    fun markWorkoutSyncedAll() {
        viewModelScope.launch {
            syncRepository.markWorkoutSyncedAll().collect { resource ->
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


    fun shouldSendLogs(): Boolean {
        val lastTimeStamp = ringDataStore.getAutoLogsTimeStamp()
        if (lastTimeStamp == 0L) {
            return true
        }
        return lastTimeStamp.checkDayDifferenceMoreOne()
    }

    fun shouldSyncLogsAfter12(): Boolean {
        val dayDifferenceGreaterThan1 = shouldSendLogs()
        if (!dayDifferenceGreaterThan1) return false

        val currentTime = LocalTime.now()
        val targetTime = LocalTime.of(12, 0)

        return currentTime.isAfter(targetTime)

    }

    fun shouldSyncAutoLogs(): Boolean {
        val lastTimeStamp = ringDataStore.getAutoLogsTimeStamp()
        val logSyncInterval = localDataStore.getLogSyncInterval()

        if (logSyncInterval == 0) return false

        if (lastTimeStamp == 0L) {
            ringDataStore.saveAutoLogsTimeStamp()
            return false
        }

        return lastTimeStamp.checkDayDifferenceMoreNMinutes(logSyncInterval * 60)
    }

    fun handleBatteryAlert(noiseFitDevice: ColorFitDevice) {
        viewModelScope.launch(Dispatchers.IO) {
            val isAlertShown = localDataStore.getIsBatteryAlertShown()
            if (!isAlertShown) {
                stateDashRingBattery.postValue(Pair(true, noiseFitDevice))
            } else {
                stateDashRingBattery.postValue(Pair(false, null))
            }
        }
    }

    fun setRingBatteryInfoState() {
        stateDashRingBattery.postValue(Pair(false, null))
        viewModelScope.launch(Dispatchers.IO) {
            localDataStore.setBatteryAlertShown()
        }
    }

}

data class PushLocalNotification(val title: String, val content: String, val key: String)