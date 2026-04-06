package com.oreo.ui.home.summary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.AlertSettingsDeviceDefaults
import com.noisefit_commans.models.AlertSettingsStateUtils
import com.noisefit_commans.models.DeviceAlertFeature
import com.noisefit_commans.models.HeartRateAlertSettings
import com.noisefit_commans.models.HighStressAlertSettings
import com.noisefit_commans.models.LocalDeviceAlertSettings
import com.noisefit_commans.models.PressureModeSettings
import com.noisefit_commans.models.ScreenlessDeviceSupport
import com.noisefit_commans.models.SedentaryData
import com.noisefit_commans.models.SleepReminder
import com.noisefit_commans.models.Spo2AlertSettings
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.AlertDebugLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AlertSettingsViewModel @Inject constructor(
    val sessionManager: SessionManager,
    private val ringDataStore: RingDataStore,
    private val watchDataStore: WatchDataStore
) : BaseViewModel() {

    private enum class ScreenSyncPhase {
        SNAPSHOT_QUERY,
        UPDATE_SENT,
        VERIFY_QUERY
    }

    private data class ScreenSyncState(
        val feature: DeviceAlertFeature,
        val phase: ScreenSyncPhase,
        val operationId: Long
    )

    private val _alertSettings = MutableLiveData<LocalDeviceAlertSettings>()
    val alertSettings: LiveData<LocalDeviceAlertSettings> = _alertSettings

    private val screenQueuedFeatures = linkedSetOf<DeviceAlertFeature>()
    private val blockedScreenFeatures = linkedSetOf<DeviceAlertFeature>()
    private val featureOperationIds = mutableMapOf<DeviceAlertFeature, Long>()
    private var screenSyncState: ScreenSyncState? = null
    private var wasConnected = false

    fun loadState() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = getScopedAlertSettings()
            AlertDebugLogger.logValue("AlertVM", "loadState", state)
            saveState(state)
            withContext(Dispatchers.Main) {
                _alertSettings.value = state
            }
        }
    }

    fun onConnectedStateChanged(connectState: ConnectState?) {
        AlertDebugLogger.log(
            "AlertVM",
            "onConnectedStateChanged state=${connectState?.javaClass?.simpleName} wasConnected=$wasConnected"
        )
        if (connectState is ConnectState.ConnectSuccess) {
            val becameConnected = !wasConnected
            wasConnected = true
            val state = getScopedAlertSettings()
            restoreQueuedFeatures(state)
            if (screenQueuedFeatures.isNotEmpty() && (becameConnected || screenSyncState == null)) {
                syncQueuedFeatures()
            } else if (becameConnected) {
                refreshFromBand()
            }
        } else {
            wasConnected = false
            clearScreenSyncState()
        }
    }

    fun refreshFromBand() {
        if (sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess) {
            AlertDebugLogger.log("AlertVM", "refreshFromBand skipped disconnected")
            return
        }
        AlertDebugLogger.log("AlertVM", "refreshFromBand dispatching threshold/reminder alert queries")
        sessionManager.sendQueryAction(QueryAction.GetHeartRateAlertSettings)
        sessionManager.sendQueryAction(QueryAction.GetSpo2AlertSettings)
        sessionManager.sendQueryAction(QueryAction.GetHighStressAlertSettings)
        sessionManager.sendQueryAction(QueryAction.GetPressureModeSettings)
        sessionManager.sendQueryAction(QueryAction.GetSleepReminder)
        sessionManager.sendQueryAction(QueryAction.GetSedentaryData)
    }

    fun saveHeartRateSettings(heartRateAlertSettings: HeartRateAlertSettings) {
        AlertDebugLogger.logValue("AlertVM", "saveHeartRateSettings requested", heartRateAlertSettings)
        if (!AlertSettingsValidation.isHeartRateSettingsValid(heartRateAlertSettings)) {
            AlertDebugLogger.logValue("AlertVM", "saveHeartRateSettings validationFailed", heartRateAlertSettings)
            sendMessage("Enter a valid BPM between 1 and 250 for enabled heart-rate alerts.")
            return
        }
        persistFeature(DeviceAlertFeature.HEART_RATE) { settings ->
            settings.heartRate = heartRateAlertSettings
        }
    }

    fun saveSpo2Settings(spo2AlertSettings: Spo2AlertSettings) {
        AlertDebugLogger.logValue("AlertVM", "saveSpo2Settings requested", spo2AlertSettings)
        if (!AlertSettingsValidation.isSpo2ThresholdValid(spo2AlertSettings.threshold)) {
            AlertDebugLogger.logValue("AlertVM", "saveSpo2Settings validationFailed", spo2AlertSettings)
            sendMessage("Enter a valid SpO2 threshold between 1 and 100.")
            return
        }
        persistFeature(DeviceAlertFeature.SPO2) { settings ->
            settings.spo2 = spo2AlertSettings
        }
    }

    fun saveHighStressSettings(highStressAlertSettings: HighStressAlertSettings) {
        AlertDebugLogger.logValue("AlertVM", "saveHighStressSettings requested", highStressAlertSettings)
        if (highStressAlertSettings.enabled && !AlertSettingsValidation.isStressThresholdValid(highStressAlertSettings.threshold)) {
            AlertDebugLogger.logValue("AlertVM", "saveHighStressSettings validationFailed", highStressAlertSettings)
            sendMessage("Enter a valid stress threshold between 1 and 100.")
            return
        }
        persistFeature(DeviceAlertFeature.HIGH_STRESS_INDEX) { settings ->
            settings.highStress = highStressAlertSettings
        }
    }

    fun savePressureModeSettings(pressureModeSettings: PressureModeSettings) {
        AlertDebugLogger.logValue("AlertVM", "savePressureModeSettings requested", pressureModeSettings)
        val normalizedSettings = if (ScreenlessDeviceSupport.isScreenlessDeviceType(ringDataStore.getRingDevice()?.deviceType)) {
            PressureModeSettings(
                stressMonitoringEnabled = pressureModeSettings.relaxationPromptEnabled,
                relaxationPromptEnabled = pressureModeSettings.relaxationPromptEnabled
            )
        } else {
            pressureModeSettings
        }
        persistFeature(DeviceAlertFeature.RELAXATION_PROMPT) { settings ->
            settings.pressureMode = normalizedSettings
        }
    }

    fun saveSleepReminder(sleepReminder: SleepReminder) {
        AlertDebugLogger.logValue("AlertVM", "saveSleepReminder requested", sleepReminder)
        persistFeature(DeviceAlertFeature.SLEEP_REMINDER) { settings ->
            settings.sleepReminder = sleepReminder.copy(second = 0, millisecond = 0)
        }
    }

    fun saveSedentaryReminder(sedentaryData: SedentaryData) {
        AlertDebugLogger.logValue("AlertVM", "saveSedentaryReminder requested", sedentaryData)
        if (!AlertSettingsValidation.isSedentaryIntervalValid(sedentaryData.interval)) {
            AlertDebugLogger.logValue("AlertVM", "saveSedentaryReminder validationFailed", sedentaryData)
            sendMessage("Sedentary interval must be a positive multiple of 60 minutes.")
            return
        }
        persistFeature(DeviceAlertFeature.SEDENTARY_REMINDER) { settings ->
            settings.sedentaryReminder = sedentaryData
        }
    }

    fun refreshWearDetectionStatus() {
        if (sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess) {
            AlertDebugLogger.log("AlertVM", "refreshWearDetectionStatus skipped disconnected")
            sendMessage("Connect the device to refresh wear status.")
            return
        }
        val deviceType = ringDataStore.getRingDevice()?.deviceType
        if (!ScreenlessDeviceSupport.isScreenlessDeviceType(deviceType)) {
            AlertDebugLogger.log("AlertVM", "refreshWearDetectionStatus unsupportedDeviceType=$deviceType")
            val state = getScopedAlertSettings()
            state.setSupport(DeviceAlertFeature.WEAR_DETECTION, false)
            state.wearDetectionStatus = state.wearDetectionStatus.copy(
                isWorn = null,
                lastUpdatedAt = -1L
            )
            saveAndPublish(state)
            sendMessage(getUnsupportedMessage(DeviceAlertFeature.WEAR_DETECTION))
            return
        }
        AlertDebugLogger.log("AlertVM", "refreshWearDetectionStatus dispatched deviceType=$deviceType")
        sessionManager.sendQueryAction(QueryAction.GetRingWearingStatus)
    }

    fun handleQueryCallback(queryCallback: QueryCallback) {
        AlertDebugLogger.log("AlertVM", "handleQueryCallback type=${queryCallback.javaClass.simpleName}")
        when (queryCallback) {
            is QueryCallback.AlertFeatureSupportObtained -> {
                val keepScreenlessWearOptimistic =
                    queryCallback.feature == DeviceAlertFeature.WEAR_DETECTION &&
                        !queryCallback.supported &&
                        ScreenlessDeviceSupport.isScreenlessDeviceType(ringDataStore.getRingDevice()?.deviceType)
                AlertDebugLogger.log(
                    "AlertVM",
                    "support feature=${queryCallback.feature} supported=${queryCallback.supported} keepScreenlessWearOptimistic=$keepScreenlessWearOptimistic"
                )
                val state = getScopedAlertSettings()
                state.setSupport(queryCallback.feature, queryCallback.supported || keepScreenlessWearOptimistic)
                if (queryCallback.feature == DeviceAlertFeature.WEAR_DETECTION &&
                    !queryCallback.supported &&
                    !keepScreenlessWearOptimistic
                ) {
                    state.wearDetectionStatus = state.wearDetectionStatus.copy(
                        isWorn = null,
                        lastUpdatedAt = -1L
                    )
                }
                if (!queryCallback.supported && !keepScreenlessWearOptimistic) {
                    state.setPending(queryCallback.feature, false)
                    screenQueuedFeatures.remove(queryCallback.feature)
                    blockedScreenFeatures.remove(queryCallback.feature)
                    if (screenSyncState?.feature == queryCallback.feature) {
                        screenSyncState = null
                    }
                }
                saveAndPublish(state)
                if (!queryCallback.supported && !keepScreenlessWearOptimistic) {
                    sendMessage(getUnsupportedMessage(queryCallback.feature))
                    syncQueuedFeatures()
                }
            }

            is QueryCallback.HeartRateAlertSettingsObtained -> {
                val feature = DeviceAlertFeature.HEART_RATE
                val state = getScopedAlertSettings()
                val syncState = getScreenSyncState(feature)
                val syncPhase = syncState?.phase
                val staleResponse = isStaleResponse(feature, syncState)
                val matchesStored = AlertSettingsStateUtils.heartRateMatches(
                    state.heartRate,
                    queryCallback.heartRateAlertSettings,
                    workoutSupported = queryCallback.workoutSupported
                )
                AlertDebugLogger.log(
                    "AlertVM",
                    "heartRate query matchesStored=$matchesStored pending=${state.isPending(feature)} phase=$syncPhase stale=$staleResponse"
                )
                AlertDebugLogger.logValue("AlertVM", "heartRate query value", queryCallback.heartRateAlertSettings)
                state.support.heartRateWorkout = queryCallback.workoutSupported
                if (!queryCallback.workoutSupported) {
                    state.heartRate.workoutEnabled = false
                }
                state.snapshots.heartRate = queryCallback.snapshot
                state.setSupport(feature, true)
                when {
                    staleResponse -> {
                        AlertDebugLogger.logAlertFlow("AlertVM", feature, syncState?.operationId, "stale_response", "ignored heart-rate threshold query")
                    }

                    syncPhase == ScreenSyncPhase.SNAPSHOT_QUERY -> {
                        AlertDebugLogger.logAlertFlow("AlertVM", feature, syncState?.operationId, "threshold_api", "heart-rate snapshot query completed")
                    }

                    syncPhase == ScreenSyncPhase.UPDATE_SENT -> {
                        AlertDebugLogger.logAlertFlow("AlertVM", feature, syncState?.operationId, "threshold_api", "heart-rate write in flight, preserving local value")
                    }

                    syncPhase == ScreenSyncPhase.VERIFY_QUERY -> {
                        if (matchesStored) {
                            state.heartRate = AlertSettingsStateUtils.mergeHeartRate(
                                state.heartRate,
                                queryCallback.heartRateAlertSettings,
                                workoutSupported = queryCallback.workoutSupported
                            )
                        } else {
                            AlertDebugLogger.log("AlertVM", "heartRate verify mismatch preserving local value")
                        }
                    }

                    state.isPending(feature) -> {
                        AlertDebugLogger.log("AlertVM", "heartRate passive query preserving pending local value")
                    }

                    else -> {
                        state.heartRate = AlertSettingsStateUtils.mergeHeartRate(
                            state.heartRate,
                            queryCallback.heartRateAlertSettings,
                            workoutSupported = queryCallback.workoutSupported
                        )
                    }
                }
                saveAndPublish(state)
                completeOrRestartAfterQuery(feature, syncState, matchesStored)
            }

            is QueryCallback.HeartRateIntervalObtained -> {
                AlertDebugLogger.logAlertFlowValue(
                    "AlertVM",
                    DeviceAlertFeature.HEART_RATE,
                    getScreenSyncState(DeviceAlertFeature.HEART_RATE)?.operationId,
                    "background_probe",
                    "heartRateInterval",
                    queryCallback.interval
                )
            }

            is QueryCallback.RealTimeHeartRateSampleObtained -> Unit

            is QueryCallback.Spo2AlertSettingsObtained -> {
                val feature = DeviceAlertFeature.SPO2
                val state = getScopedAlertSettings()
                val syncState = getScreenSyncState(feature)
                val syncPhase = syncState?.phase
                val staleResponse = isStaleResponse(feature, syncState)
                val matchesStored = AlertSettingsStateUtils.spo2Matches(
                    state.spo2,
                    queryCallback.spo2AlertSettings
                )
                AlertDebugLogger.log(
                    "AlertVM",
                    "spo2 query matchesStored=$matchesStored pending=${state.isPending(feature)} phase=$syncPhase stale=$staleResponse"
                )
                AlertDebugLogger.logValue("AlertVM", "spo2 query value", queryCallback.spo2AlertSettings)
                state.setSupport(feature, true)
                when {
                    staleResponse -> {
                        AlertDebugLogger.logAlertFlow("AlertVM", feature, syncState?.operationId, "stale_response", "ignored spo2 threshold query")
                    }

                    syncPhase == ScreenSyncPhase.UPDATE_SENT -> {
                        AlertDebugLogger.logAlertFlow("AlertVM", feature, syncState?.operationId, "threshold_api", "spo2 write in flight, preserving local value")
                    }

                    syncPhase == ScreenSyncPhase.VERIFY_QUERY && !matchesStored -> {
                        AlertDebugLogger.log("AlertVM", "spo2 verify mismatch preserving local value")
                    }

                    state.isPending(feature) && syncPhase == null -> {
                        AlertDebugLogger.log("AlertVM", "spo2 passive query preserving pending local value")
                    }

                    else -> {
                        state.spo2 = AlertSettingsStateUtils.mergeSpo2(
                            state.spo2,
                            queryCallback.spo2AlertSettings
                        )
                    }
                }
                saveAndPublish(state)
                completeOrRestartAfterQuery(feature, syncState, matchesStored)
            }

            is QueryCallback.Spo2SettingsObtained -> {
                AlertDebugLogger.logAlertFlowValue(
                    "AlertVM",
                    DeviceAlertFeature.SPO2,
                    getScreenSyncState(DeviceAlertFeature.SPO2)?.operationId,
                    "background_probe",
                    "spo2Monitoring",
                    queryCallback.spo2Data
                )
            }

            is QueryCallback.HighStressAlertSettingsObtained -> {
                val feature = DeviceAlertFeature.HIGH_STRESS_INDEX
                val state = getScopedAlertSettings()
                val syncState = getScreenSyncState(feature)
                val syncPhase = syncState?.phase
                val staleResponse = isStaleResponse(feature, syncState)
                val matchesStored = AlertSettingsStateUtils.highStressMatches(
                    state.highStress,
                    queryCallback.highStressAlertSettings
                )
                AlertDebugLogger.log(
                    "AlertVM",
                    "highStress query matchesStored=$matchesStored pending=${state.isPending(feature)} phase=$syncPhase stale=$staleResponse"
                )
                AlertDebugLogger.logValue("AlertVM", "highStress query value", queryCallback.highStressAlertSettings)
                state.setSupport(feature, true)
                when {
                    staleResponse -> {
                        AlertDebugLogger.logAlertFlow("AlertVM", feature, syncState?.operationId, "stale_response", "ignored high-stress threshold query")
                    }

                    syncPhase == ScreenSyncPhase.UPDATE_SENT -> {
                        AlertDebugLogger.logAlertFlow("AlertVM", feature, syncState?.operationId, "threshold_api", "high-stress write in flight, preserving local value")
                    }

                    syncPhase == ScreenSyncPhase.VERIFY_QUERY && !matchesStored -> {
                        AlertDebugLogger.log("AlertVM", "highStress verify mismatch preserving local value")
                    }

                    state.isPending(feature) && syncPhase == null -> {
                        AlertDebugLogger.log("AlertVM", "highStress passive query preserving pending local value")
                    }

                    else -> {
                        state.highStress = AlertSettingsStateUtils.mergeHighStress(
                            state.highStress,
                            queryCallback.highStressAlertSettings
                        )
                    }
                }
                saveAndPublish(state)
                completeOrRestartAfterQuery(feature, syncState, matchesStored)
            }

            is QueryCallback.PressureModeSettingsObtained -> {
                val feature = DeviceAlertFeature.RELAXATION_PROMPT
                val state = getScopedAlertSettings()
                val syncState = getScreenSyncState(feature)
                val syncPhase = syncState?.phase
                val staleResponse = isStaleResponse(feature, syncState)
                val matchesStored = state.pressureMode == queryCallback.pressureModeSettings
                AlertDebugLogger.log(
                    "AlertVM",
                    "pressure query matchesStored=$matchesStored pending=${state.isPending(feature)} phase=$syncPhase stale=$staleResponse"
                )
                AlertDebugLogger.logValue("AlertVM", "pressure query value", queryCallback.pressureModeSettings)
                state.snapshots.pressureMode = queryCallback.snapshot
                state.setSupport(feature, true)
                when {
                    staleResponse -> {
                        AlertDebugLogger.logAlertFlow("AlertVM", feature, syncState?.operationId, "stale_response", "ignored pressure threshold query")
                    }

                    syncPhase == ScreenSyncPhase.UPDATE_SENT -> {
                        AlertDebugLogger.logAlertFlow("AlertVM", feature, syncState?.operationId, "threshold_api", "pressure write in flight, preserving local value")
                    }

                    syncPhase == ScreenSyncPhase.VERIFY_QUERY && !matchesStored -> {
                        AlertDebugLogger.log("AlertVM", "pressure verify mismatch preserving local value")
                    }

                    state.isPending(feature) && syncPhase == null -> {
                        AlertDebugLogger.log("AlertVM", "pressure passive query preserving pending local value")
                    }

                    else -> {
                        state.pressureMode = queryCallback.pressureModeSettings
                    }
                }
                saveAndPublish(state)
                completeOrRestartAfterQuery(feature, syncState, matchesStored)
            }

            is QueryCallback.SleepReminderObtained -> {
                val feature = DeviceAlertFeature.SLEEP_REMINDER
                val state = getScopedAlertSettings()
                val syncState = getScreenSyncState(feature)
                val syncPhase = syncState?.phase
                val staleResponse = isStaleResponse(feature, syncState)
                val matchesStored = AlertSettingsStateUtils.sleepReminderMatches(
                    state.sleepReminder,
                    queryCallback.sleepReminder
                )
                AlertDebugLogger.log(
                    "AlertVM",
                    "sleepReminder query matchesStored=$matchesStored pending=${state.isPending(feature)} phase=$syncPhase stale=$staleResponse"
                )
                AlertDebugLogger.logValue("AlertVM", "sleepReminder query value", queryCallback.sleepReminder)
                state.setSupport(feature, true)
                when {
                    staleResponse -> {
                        AlertDebugLogger.logAlertFlow("AlertVM", feature, syncState?.operationId, "stale_response", "ignored sleep reminder query")
                    }

                    syncPhase == ScreenSyncPhase.UPDATE_SENT -> {
                        AlertDebugLogger.logAlertFlow("AlertVM", feature, syncState?.operationId, "threshold_api", "sleep reminder write in flight, preserving local value")
                    }

                    syncPhase == ScreenSyncPhase.VERIFY_QUERY && !matchesStored -> {
                        AlertDebugLogger.log("AlertVM", "sleepReminder verify mismatch preserving local value")
                    }

                    state.isPending(feature) && syncPhase == null -> {
                        AlertDebugLogger.log("AlertVM", "sleepReminder passive query preserving pending local value")
                    }

                    else -> {
                        state.sleepReminder = AlertSettingsStateUtils.mergeSleepReminder(
                            state.sleepReminder,
                            queryCallback.sleepReminder
                        )
                    }
                }
                saveAndPublish(state)
                completeOrRestartAfterQuery(feature, syncState, matchesStored)
            }

            is QueryCallback.SedentaryReminderSettingsObtained -> {
                val feature = DeviceAlertFeature.SEDENTARY_REMINDER
                val state = getScopedAlertSettings()
                val syncState = getScreenSyncState(feature)
                val syncPhase = syncState?.phase
                val staleResponse = isStaleResponse(feature, syncState)
                val readbackAnomaly = AlertSettingsStateUtils.isScreenlessSedentaryReadbackAnomaly(
                    deviceType = ringDataStore.getRingDevice()?.deviceType,
                    local = state.sedentaryReminder,
                    device = queryCallback.sedentaryData
                )
                val matchesStored = AlertSettingsStateUtils.sedentaryMatches(
                    state.sedentaryReminder,
                    queryCallback.sedentaryData
                ) || readbackAnomaly
                AlertDebugLogger.log(
                    "AlertVM",
                    "sedentary query matchesStored=$matchesStored pending=${state.isPending(feature)} phase=$syncPhase stale=$staleResponse"
                )
                if (readbackAnomaly) {
                    AlertDebugLogger.log(
                        "AlertVM",
                        "sedentary readback anomaly detected deviceType=${ringDataStore.getRingDevice()?.deviceType}"
                    )
                }
                AlertDebugLogger.logValue("AlertVM", "sedentary query value", queryCallback.sedentaryData)
                state.snapshots.sedentaryReminder = queryCallback.snapshot
                state.setSupport(feature, true)
                when {
                    staleResponse -> {
                        AlertDebugLogger.logAlertFlow("AlertVM", feature, syncState?.operationId, "stale_response", "ignored sedentary reminder query")
                    }

                    syncPhase == ScreenSyncPhase.SNAPSHOT_QUERY -> {
                        AlertDebugLogger.log("AlertVM", "sedentary query using snapshot only")
                    }

                    syncPhase == ScreenSyncPhase.UPDATE_SENT -> {
                        AlertDebugLogger.logAlertFlow("AlertVM", feature, syncState?.operationId, "threshold_api", "sedentary write in flight, preserving local value")
                    }

                    syncPhase == ScreenSyncPhase.VERIFY_QUERY && !matchesStored -> {
                        AlertDebugLogger.log("AlertVM", "sedentary verify mismatch preserving local value")
                    }

                    state.isPending(feature) && syncPhase == null -> {
                        AlertDebugLogger.log("AlertVM", "sedentary passive query preserving pending local value")
                    }

                    readbackAnomaly -> {
                        AlertDebugLogger.log("AlertVM", "sedentary verify using local value after zeroed readback")
                    }

                    else -> {
                        state.sedentaryReminder = AlertSettingsStateUtils.mergeSedentary(
                            state.sedentaryReminder,
                            queryCallback.sedentaryData
                        )
                    }
                }
                saveAndPublish(state)
                completeOrRestartAfterQuery(feature, syncState, matchesStored)
            }

            is QueryCallback.RingWearingStatusObtained -> {
                val state = getScopedAlertSettings()
                AlertDebugLogger.logValue("AlertVM", "wearDetection query value", queryCallback.wearDetectionStatus)
                state.wearDetectionStatus = queryCallback.wearDetectionStatus
                state.setSupport(DeviceAlertFeature.WEAR_DETECTION, true)
                saveAndPublish(state)
            }

            else -> Unit
        }
    }

    fun handleUpdateCallback(dataCallback: UpdateDeviceDataCallback) {
        AlertDebugLogger.log("AlertVM", "handleUpdateCallback type=${dataCallback.javaClass.simpleName}")
        when (dataCallback) {
            is UpdateDeviceDataCallback.HeartRateAlertSettingsUpdated -> {
                AlertDebugLogger.log("AlertVM", "heartRate update success=${dataCallback.success}")
                handleScreenUpdateCompletion(DeviceAlertFeature.HEART_RATE, dataCallback.success)
            }

            is UpdateDeviceDataCallback.Spo2AlertSettingsUpdated -> {
                AlertDebugLogger.log("AlertVM", "spo2 update success=${dataCallback.success}")
                handleScreenUpdateCompletion(DeviceAlertFeature.SPO2, dataCallback.success)
            }

            is UpdateDeviceDataCallback.HighStressAlertSettingsUpdated -> {
                AlertDebugLogger.log("AlertVM", "highStress update success=${dataCallback.success}")
                handleScreenUpdateCompletion(DeviceAlertFeature.HIGH_STRESS_INDEX, dataCallback.success)
            }

            is UpdateDeviceDataCallback.PressureModeSettingsUpdated -> {
                AlertDebugLogger.log("AlertVM", "pressure update success=${dataCallback.success}")
                handleScreenUpdateCompletion(DeviceAlertFeature.RELAXATION_PROMPT, dataCallback.success)
            }

            is UpdateDeviceDataCallback.SleepReminderUpdated -> {
                AlertDebugLogger.log("AlertVM", "sleepReminder update success=${dataCallback.success}")
                handleScreenUpdateCompletion(DeviceAlertFeature.SLEEP_REMINDER, dataCallback.success)
            }

            is UpdateDeviceDataCallback.SedentaryDataUpdated -> {
                AlertDebugLogger.log("AlertVM", "sedentary update success=${dataCallback.success}")
                handleScreenUpdateCompletion(DeviceAlertFeature.SEDENTARY_REMINDER, dataCallback.success)
            }

            is UpdateDeviceDataCallback.AlertFeatureSupportResolved -> {
                val keepScreenlessWearOptimistic =
                    dataCallback.feature == DeviceAlertFeature.WEAR_DETECTION &&
                        !dataCallback.supported &&
                        ScreenlessDeviceSupport.isScreenlessDeviceType(ringDataStore.getRingDevice()?.deviceType)
                AlertDebugLogger.log(
                    "AlertVM",
                    "supportResolved feature=${dataCallback.feature} supported=${dataCallback.supported} keepScreenlessWearOptimistic=$keepScreenlessWearOptimistic"
                )
                val state = getScopedAlertSettings()
                state.setSupport(dataCallback.feature, dataCallback.supported || keepScreenlessWearOptimistic)
                if (dataCallback.feature == DeviceAlertFeature.WEAR_DETECTION &&
                    !dataCallback.supported &&
                    !keepScreenlessWearOptimistic
                ) {
                    state.wearDetectionStatus = state.wearDetectionStatus.copy(
                        isWorn = null,
                        lastUpdatedAt = -1L
                    )
                }
                if (!dataCallback.supported && !keepScreenlessWearOptimistic) {
                    state.setPending(dataCallback.feature, false)
                    screenQueuedFeatures.remove(dataCallback.feature)
                    blockedScreenFeatures.remove(dataCallback.feature)
                    if (screenSyncState?.feature == dataCallback.feature) {
                        screenSyncState = null
                    }
                    sendMessage(getUnsupportedMessage(dataCallback.feature))
                }
                saveAndPublish(state)
                if (!dataCallback.supported && !keepScreenlessWearOptimistic) {
                    syncQueuedFeatures()
                }
            }

            else -> Unit
        }
    }

    private fun persistFeature(
        feature: DeviceAlertFeature,
        block: (LocalDeviceAlertSettings) -> Unit
    ) {
        val state = getScopedAlertSettings()
        val operationId = nextOperationId(feature)
        blockedScreenFeatures.remove(feature)
        block(state)
        state.setPending(feature, true)
        restoreQueuedFeatures(state)
        AlertDebugLogger.logAlertFlow(
            "AlertVM",
            feature,
            operationId,
            "local_save",
            "connected=${sessionManager.connectStateRing.value is ConnectState.ConnectSuccess}"
        )
        AlertDebugLogger.logValue("AlertVM", "persistFeature state", state)
        saveAndPublish(state)
        screenQueuedFeatures.add(feature)
        if (sessionManager.connectStateRing.value is ConnectState.ConnectSuccess) {
            syncQueuedFeatures()
        } else {
            sendMessage("Saved locally. The device will be updated when it reconnects.")
        }
    }

    private fun syncQueuedFeatures() {
        if (sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess) {
            AlertDebugLogger.log("AlertVM", "syncQueuedFeatures skipped disconnected")
            return
        }
        if (screenSyncState != null) {
            AlertDebugLogger.log("AlertVM", "syncQueuedFeatures skipped activePhase=${screenSyncState?.phase} feature=${screenSyncState?.feature}")
            return
        }
        val state = getScopedAlertSettings()
        val feature = screenQueuedFeatures.firstOrNull { queuedFeature ->
            state.isPending(queuedFeature) &&
                state.isSupported(queuedFeature) &&
                !blockedScreenFeatures.contains(queuedFeature)
        } ?: return
        val operationId = ensureOperationId(feature)
        val requiresSnapshot = AlertSettingsStateUtils.requiresSnapshot(
            settings = state,
            feature = feature,
            deviceType = ringDataStore.getRingDevice()?.deviceType
        )
        AlertDebugLogger.logAlertFlow(
            "AlertVM",
            feature,
            operationId,
            "threshold_api",
            "syncQueuedFeatures requiresSnapshot=$requiresSnapshot"
        )
        if (requiresSnapshot) {
            screenSyncState = ScreenSyncState(feature, ScreenSyncPhase.SNAPSHOT_QUERY, operationId)
            sendQuery(feature)
        } else {
            screenSyncState = ScreenSyncState(feature, ScreenSyncPhase.UPDATE_SENT, operationId)
            sendUpdate(feature, state)
        }
    }

    private fun handleScreenQueryCompletion(
        feature: DeviceAlertFeature,
        matchesStored: Boolean
    ) {
        val syncState = screenSyncState ?: return
        if (syncState.feature != feature) {
            return
        }
        if (isStaleResponse(feature, syncState)) {
            handleStaleScreenResponse(feature, syncState)
            return
        }
        AlertDebugLogger.logAlertFlow(
            "AlertVM",
            feature,
            syncState.operationId,
            if (syncState.phase == ScreenSyncPhase.VERIFY_QUERY) "verify_query" else "threshold_api",
            "handleScreenQueryCompletion matchesStored=$matchesStored phase=${syncState.phase}"
        )
        when (syncState.phase) {
            ScreenSyncPhase.SNAPSHOT_QUERY -> {
                screenSyncState = ScreenSyncState(feature, ScreenSyncPhase.UPDATE_SENT, syncState.operationId)
                sendUpdate(feature, getScopedAlertSettings())
            }

            ScreenSyncPhase.VERIFY_QUERY -> {
                val state = getScopedAlertSettings()
                if (matchesStored) {
                    state.setPending(feature, false)
                    saveAndPublish(state)
                    screenQueuedFeatures.remove(feature)
                    blockedScreenFeatures.remove(feature)
                    sendMessage(getSuccessMessage(feature))
                } else {
                    blockedScreenFeatures.add(feature)
                    sendMessage("Saved locally, but the device did not confirm this alert yet.")
                }
                screenSyncState = null
                syncQueuedFeatures()
            }

            ScreenSyncPhase.UPDATE_SENT -> Unit
        }
    }

    private fun handleScreenUpdateCompletion(feature: DeviceAlertFeature, success: Boolean) {
        val syncState = screenSyncState ?: return
        if (syncState.feature != feature || syncState.phase != ScreenSyncPhase.UPDATE_SENT) {
            return
        }
        if (isStaleResponse(feature, syncState)) {
            handleStaleScreenResponse(feature, syncState)
            return
        }
        AlertDebugLogger.logAlertFlow(
            "AlertVM",
            feature,
            syncState.operationId,
            "threshold_api",
            "handleScreenUpdateCompletion success=$success"
        )
        if (!success) {
            blockedScreenFeatures.add(feature)
            screenSyncState = null
            sendMessage("Saved locally. The device update failed and will need another sync attempt.")
            syncQueuedFeatures()
            return
        }
        if (shouldSkipVerification(feature)) {
            val state = getScopedAlertSettings()
            state.setPending(feature, false)
            saveAndPublish(state)
            screenQueuedFeatures.remove(feature)
            blockedScreenFeatures.remove(feature)
            screenSyncState = null
            sendMessage(getSuccessMessage(feature))
            syncQueuedFeatures()
            return
        }
        screenSyncState = ScreenSyncState(feature, ScreenSyncPhase.VERIFY_QUERY, syncState.operationId)
        val verifyDelayMs = getVerifyDelayMs(feature)
        AlertDebugLogger.logAlertFlow(
            "AlertVM",
            feature,
            syncState.operationId,
            "verify_query",
            "scheduleVerify delayMs=$verifyDelayMs"
        )
        if (verifyDelayMs <= 0L) {
            sendQuery(feature)
        } else {
            viewModelScope.launch {
                kotlinx.coroutines.delay(verifyDelayMs)
                if (screenSyncState?.feature == feature && screenSyncState?.phase == ScreenSyncPhase.VERIFY_QUERY) {
                    sendQuery(feature)
                }
            }
        }
    }

    private fun sendQuery(feature: DeviceAlertFeature) {
        val syncState = getScreenSyncState(feature)
        val action = when (feature) {
            DeviceAlertFeature.HEART_RATE -> QueryAction.GetHeartRateAlertSettings
            DeviceAlertFeature.SPO2 -> QueryAction.GetSpo2AlertSettings
            DeviceAlertFeature.HIGH_STRESS_INDEX -> QueryAction.GetHighStressAlertSettings
            DeviceAlertFeature.RELAXATION_PROMPT -> QueryAction.GetPressureModeSettings
            DeviceAlertFeature.SLEEP_REMINDER -> QueryAction.GetSleepReminder
            DeviceAlertFeature.SEDENTARY_REMINDER -> QueryAction.GetSedentaryData
            DeviceAlertFeature.WEAR_DETECTION -> null
        }
        AlertDebugLogger.logAlertFlow(
            "AlertVM",
            feature,
            syncState?.operationId,
            if (syncState?.phase == ScreenSyncPhase.VERIFY_QUERY) "verify_query" else "threshold_api",
            "sendQuery action=$action"
        )
        action?.let(sessionManager::sendQueryAction)
    }

    private fun sendUpdate(feature: DeviceAlertFeature, state: LocalDeviceAlertSettings) {
        AlertDebugLogger.logAlertFlow(
            "AlertVM",
            feature,
            getScreenSyncState(feature)?.operationId ?: currentOperationId(feature),
            "threshold_api",
            "sendUpdate"
        )
        AlertDebugLogger.logValue("AlertVM", "sendUpdate state", state)
        when (feature) {
            DeviceAlertFeature.HEART_RATE -> {
                sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetHeartRateAlertSettings(state.heartRate))
            }
            DeviceAlertFeature.SPO2 -> {
                sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetSpo2AlertSettings(state.spo2))
            }
            DeviceAlertFeature.HIGH_STRESS_INDEX -> {
                sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.SetHighStressAlertSettings(state.highStress)
                )
            }
            DeviceAlertFeature.RELAXATION_PROMPT -> {
                sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.SetPressureModeSettings(state.pressureMode)
                )
            }
            DeviceAlertFeature.SLEEP_REMINDER -> {
                sessionManager.sendUpdateQueryAction(UpdateDeviceAction.UpdateSleepReminder(state.sleepReminder))
            }
            DeviceAlertFeature.SEDENTARY_REMINDER -> {
                sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetSedentaryData(state.sedentaryReminder))
            }
            DeviceAlertFeature.WEAR_DETECTION -> Unit
        }
    }

    private fun getScopedAlertSettings(): LocalDeviceAlertSettings {
        val storedState = watchDataStore.getLocalDeviceAlertSettings()
        val deviceAddress = ringDataStore.getRingDevice()?.address
        val scopedState = when {
            storedState == null -> LocalDeviceAlertSettings(deviceAddress = deviceAddress)
            storedState.deviceAddress.isNullOrEmpty() -> storedState.apply {
                deviceAddress?.let { this.deviceAddress = it }
            }
            deviceAddress != null && storedState.deviceAddress != deviceAddress -> {
                LocalDeviceAlertSettings(deviceAddress = deviceAddress)
            }
            else -> storedState
        }
        return AlertSettingsDeviceDefaults.apply(
            scopedState,
            ringDataStore.getRingDevice()?.deviceType
        )
    }

    private fun saveAndPublish(state: LocalDeviceAlertSettings) {
        saveState(state)
        _alertSettings.postValue(state)
    }

    private fun saveState(state: LocalDeviceAlertSettings) {
        watchDataStore.updateLocalDeviceAlertSettings(state)
    }

    private fun getScreenSyncState(feature: DeviceAlertFeature): ScreenSyncState? {
        return screenSyncState?.takeIf { it.feature == feature }
    }

    private fun restoreQueuedFeatures(state: LocalDeviceAlertSettings) {
        listOf(
            DeviceAlertFeature.HEART_RATE,
            DeviceAlertFeature.SPO2,
            DeviceAlertFeature.HIGH_STRESS_INDEX,
            DeviceAlertFeature.RELAXATION_PROMPT,
            DeviceAlertFeature.SLEEP_REMINDER,
            DeviceAlertFeature.SEDENTARY_REMINDER
        ).forEach { feature ->
            if (state.isPending(feature)) {
                screenQueuedFeatures.add(feature)
                ensureOperationId(feature)
            } else {
                screenQueuedFeatures.remove(feature)
            }
        }
    }

    private fun clearScreenSyncState() {
        screenSyncState = null
        screenQueuedFeatures.clear()
        blockedScreenFeatures.clear()
    }

    private fun currentOperationId(feature: DeviceAlertFeature): Long {
        return featureOperationIds[feature] ?: 0L
    }

    private fun ensureOperationId(feature: DeviceAlertFeature): Long {
        val current = currentOperationId(feature)
        if (current > 0L) {
            return current
        }
        featureOperationIds[feature] = 1L
        return 1L
    }

    private fun nextOperationId(feature: DeviceAlertFeature): Long {
        val next = currentOperationId(feature) + 1L
        featureOperationIds[feature] = next
        return next
    }

    private fun isStaleResponse(feature: DeviceAlertFeature, syncState: ScreenSyncState?): Boolean {
        if (syncState == null || syncState.feature != feature) {
            return false
        }
        val currentOperationId = currentOperationId(feature)
        return currentOperationId > 0L && syncState.operationId != currentOperationId
    }

    private fun completeOrRestartAfterQuery(
        feature: DeviceAlertFeature,
        syncState: ScreenSyncState?,
        matchesStored: Boolean
    ) {
        if (syncState == null) {
            return
        }
        if (isStaleResponse(feature, syncState)) {
            handleStaleScreenResponse(feature, syncState)
        } else {
            handleScreenQueryCompletion(feature, matchesStored)
        }
    }

    private fun handleStaleScreenResponse(feature: DeviceAlertFeature, syncState: ScreenSyncState) {
        AlertDebugLogger.logAlertFlow(
            "AlertVM",
            feature,
            syncState.operationId,
            "stale_response",
            "phase=${syncState.phase} current_op_id=${currentOperationId(feature)}"
        )
        if (screenSyncState?.feature == feature && screenSyncState?.operationId == syncState.operationId) {
            screenSyncState = null
        }
        syncQueuedFeatures()
    }

    private fun getSuccessMessage(feature: DeviceAlertFeature): String {
        return when (feature) {
            DeviceAlertFeature.HEART_RATE -> "Heart-rate alerts updated on the device."
            DeviceAlertFeature.SPO2 -> "SpO2 alert updated on the device."
            DeviceAlertFeature.HIGH_STRESS_INDEX -> "High-stress alert updated on the device."
            DeviceAlertFeature.RELAXATION_PROMPT -> "Relaxation prompt updated on the device."
            DeviceAlertFeature.SLEEP_REMINDER -> "Bedtime reminder updated on the device."
            DeviceAlertFeature.SEDENTARY_REMINDER -> "Sedentary reminder updated on the device."
            DeviceAlertFeature.WEAR_DETECTION -> "Alert setting updated."
        }
    }

    private fun getUnsupportedMessage(feature: DeviceAlertFeature): String {
        return when (feature) {
            DeviceAlertFeature.HEART_RATE -> "This device does not support heart-rate alert settings."
            DeviceAlertFeature.SPO2 -> "This device does not support low SpO2 alerts."
            DeviceAlertFeature.HIGH_STRESS_INDEX -> "This device does not support high-stress alerts."
            DeviceAlertFeature.RELAXATION_PROMPT -> "This device does not support relaxation prompts."
            DeviceAlertFeature.SLEEP_REMINDER -> "This device does not support bedtime reminders."
            DeviceAlertFeature.SEDENTARY_REMINDER -> "This device does not support sedentary reminders."
            DeviceAlertFeature.WEAR_DETECTION -> "Wear status is not available on this device."
        }
    }

    private fun getVerifyDelayMs(feature: DeviceAlertFeature): Long {
        return when (feature) {
            DeviceAlertFeature.RELAXATION_PROMPT,
            DeviceAlertFeature.SLEEP_REMINDER,
            DeviceAlertFeature.SEDENTARY_REMINDER -> 1500L
            DeviceAlertFeature.HEART_RATE,
            DeviceAlertFeature.SPO2,
            DeviceAlertFeature.HIGH_STRESS_INDEX,
            DeviceAlertFeature.WEAR_DETECTION -> 0L
        }
    }

    private fun shouldSkipVerification(feature: DeviceAlertFeature): Boolean {
        return feature == DeviceAlertFeature.SLEEP_REMINDER &&
            ScreenlessDeviceSupport.isScreenlessDeviceType(ringDataStore.getRingDevice()?.deviceType)
    }
}
