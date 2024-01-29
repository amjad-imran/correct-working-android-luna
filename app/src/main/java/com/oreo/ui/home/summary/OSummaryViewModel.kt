package com.oreo.ui.home.summary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit_commans.common.checkDayDifferenceMoreNMinutes
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
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.checkDayDifferenceMoreOne
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.oreo.data.model.AlertType
import com.oreo.data.model.DashAlert
import com.oreo.data.model.OContributorResponseModal
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.TapMeasureState
import com.oreo.data.model.health.ODashboardActivityScoreModel
import com.oreo.data.model.health.ODashboardReadinessScoreModel
import com.oreo.data.model.health.ODashboardSleepScoreModel
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
    val userRepository: OreoUserActivityRepository,
) : BaseViewModel() {


    val stateDashRingBattery = MutableLiveData<Pair<Boolean, ColorFitDevice?>>()

    var contributorInfo: OContributorResponseModal? = null

    var summary = OSummary()

    private var _deviceConnected: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var deviceConnected = _deviceConnected





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


    fun checkBatteryPercentage() {
        sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
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


    fun getDeviceConnected(): ColorFitDevice? {
        return ringDataStore.getRingDevice()
    }

    fun updateDeviceConnectedStatus() {
        _deviceConnected.value = (ringDataStore.getRingDevice() != null)
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

}

data class PushLocalNotification(val title: String, val content: String, val key: String)