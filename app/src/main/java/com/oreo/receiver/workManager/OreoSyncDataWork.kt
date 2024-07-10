package com.oreo.receiver.workManager

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Looper
import androidx.hilt.work.HiltWorker
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import com.google.common.util.concurrent.ListenableFuture
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit.util.ApplicationUtils
import com.noisefit.util.moveToServer.SleepNotificationUtils
import com.noisefit.util.notif.NotificationUtil
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.UserActivityHandler
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.common.checkDayDifferenceMoreNMinutes
import com.noisefit_commans.constants.SyncEvents
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.data.response.VersionCheckResponse
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.data.IUserActivityDataCallback
import com.noisefit_commans.interfaces.data.UserActivityAction
import com.noisefit_commans.interfaces.data.UserActivityCallback
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.TimeFormat
import com.noisefit_commans.models.TimeFormats
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageAppEventParams
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import com.oreo.data.repository.abstraction.OreoSyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask
import java.util.logging.Handler
import kotlin.concurrent.schedule
import kotlin.coroutines.CoroutineContext


private const val SyncingTimeOut: Long = 40000
private const val SyncWithServerTime: Long = 10800000 //10800000

//255 - no value
//0 - inactive
// 1 -low
// 2 - medium
// 3,4 - high


@HiltWorker
class OreoSyncDataWork
@AssistedInject
constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val localDataStore: DataStoredInterface,
    private val ringDataStore: RingDataStore,
    private val sessionManager: SessionManager,
    private val syncRepository: OreoSyncRepository,
    private val userActivityHandler: UserActivityHandler,
    private val watchesSdk: WatchesSDK,
    private val keyValueDataSource: KeyValueDataSource,
    private val userHealthDataDataSource: OreoUserHealthDataDataSource,
    private val sleepNotificationUtils: SleepNotificationUtils
) : ListenableWorker(context, workerParams) {

    private val TAG = "OreoSyncDataWork"

    private var mFuture: SettableFuture<Result>? = null
    private var userActivityDataActions: UserActivityDataActions? = null
    var isDataReceived = false
    private val syncDataScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext =
            Dispatchers.IO // no job added i.e + SupervisorJob()
    }

    lateinit var job: Job

    private var lastTimeStamp: Long = 0
    var timer: TimerTask? = null
    private fun shouldSync(): Boolean {
        LOGS.d(TAG, "shouldSync() ${sessionManager.forceSyncDataWithServer}")
        if (sessionManager.forceSyncDataWithServer) {
            LOGS.d(TAG, "OreoSyncDataWork: session is hacked!! Allowing syncing data with server")
            return true
        }
        val lastSyncTime = ringDataStore.getLastSyncWithServer()
        if (kotlin.math.abs(DateFormats.getTimeStamp() - lastSyncTime) > SyncWithServerTime) {
            return true
        }

        return false
    }

    //TODO: get feature list here and hit only feature apis that is supported by bounded watch
    private fun returnSuccess(success: () -> Unit) {
        sessionManager.setShowSyncOfflineData(Event(HealthOverviewDataType.ALL))
        if (localDataStore.getUserToken() == null) {
            LOGS.d(TAG, "OreoSyncDataWork: user is not logged in!!")
            AppLogs.sendAppLogs("OreoSyncDataWork: user is not logged in!!")
            return success.invoke()
        }

        if (!ApplicationUtils.isInternetConnected()) {
            LOGS.d(TAG, "OreoSyncDataWork: No Internet Access!!")
            AppLogs.sendAppLogs("OreoSyncDataWork: No Internet Access!!, Sync to server failed")
            return success.invoke()
        }

        /*if (!shouldSync()) {
            LOGS.d(
                TAG, "OreoSyncDataWork: No time to sync(die)" +
                        "current timestamp: ${DateFormats.getTimeStamp()} " +
                        "last Sync at: ${ringDataStore.getLastSyncTimeStamp()}!!"
            )
            return success.invoke()
        }*/

        //sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Start_Uploading_Data.name, eventProperty)
        sessionManager.setSyncCompletedState(Event(SyncEvents.ServerSyncStarted))
        var datesToRemove: List<String>? = null
        syncDataScope.launch {
            LOGS.d(TAG, "OreoSyncDataWork: Sync start")
            supervisorScope {
                val userActivities = syncRepository.getUnSyncUserActivities()

                /*syncRepository.getTodaySleepData().collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            sleepNotificationUtils.handleSleepData(resource.value)
                        }

                        else -> {}
                    }
                }*/

                val call1 = async {
                    syncRepository.postDataToServer(userActivities.first)?.collect { resource ->
                        when (resource) {
                            is Resource.GenericError -> {
//                                sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Error_Uploading_Data.name, eventProperty)
                                LOGS.d(TAG, "OreoSyncDataWork: combinedData1 " + resource.message)
                                AppLogs.sendAppLogs("OreoSyncDataWork postDataToServer GenericError ${resource.errorCode} ${resource.message}")
                            }

                            is Resource.Loading -> {

                            }

                            is Resource.NetworkError -> {
//                                sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Error_Uploading_Data.name, eventProperty)
                                LOGS.d(TAG, "OreoSyncDataWork: combinedData1 " + resource.response)
                                tryCatch {
                                    val dialog =
                                        resource.response.uiComponentType as UIComponentType.RetryApiDialog
                                    AppLogs.sendAppLogs("OreoSyncDataWork postDataToServer NetworkError ${dialog.message}")
                                }
                            }

                            is Resource.Success -> {

//                                sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Completed_Uploading_Data.name, eventProperty)
//                                sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Completed.name, eventProperty)
//                                localDataStore.setLastStepsSyncWithServer(DateFormats.getTimeStamp())
                                resource.data?.data?.let {
                                    handleAppVersion(context, it)
                                    datesToRemove = it.dates
                                }

                                syncDataScope.launch {
                                    syncRepository.markDataSynced(userActivities.second)
                                    syncRepository.deleteSleepServerSyncData(userActivities.second)


                                    //syncRepository.deleteServerSyncData(userActivities.second)
                                }

                                AppLogs.sendAppLogs("OreoSyncDataWork Server sync success")

                                //syncRepository.updateHashForLastSyncData(userActivities.first)

                                LOGS.d(TAG, "OreoSyncDataWork::: combinedData1 " + resource.data)
                            }
                        }
                    }
                }

                try {
                    call1.await()
                    //call2?.await()
                } catch (e: Exception) {

                }

                datesToRemove?.let {

                    userHealthDataDataSource.clearDataByDates(it)
                    delay(200)
                }

                val logsSync = shouldSyncAutoLogs()
                var isLogQuerySent = false
                if (logsSync) {
                    val status = ApplicationUtils.startFeedbackSubmitWorker(context)
                    isLogQuerySent = true
                    sessionManager.sendQueryAction(QueryAction.GetFirmwareLogs)
                }
                if (BuildConfig.DEBUG && !isLogQuerySent) {
                    sessionManager.sendQueryAction(QueryAction.GetFirmwareLogs)
                }

                AppLogs.sendAppLogs("OreoSyncDataWork server call complete")

                ringDataStore.setLastSyncWithServer(DateFormats.getTimeStamp())
                sessionManager.setSyncCompletedState(Event(SyncEvents.ServerSyncSuccess))
                //sessionManager.setShowSyncOfflineData(Event(HealthOverviewDataType.SERVER_SYNC_SUCCESS))

                if (::job.isInitialized) {
                    job.cancel()
                }
                LOGS.d(TAG, "OreoSyncDataWork: Sync complete")
            }


        }

        return success.invoke()
    }


    private fun updateDeviceDateTime() {
        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetDeviceDateTime(
                Calendar.getInstance(),
                TimeFormat(TimeFormats.HOURS_12.type)
            )
        )
    }

    private suspend fun getSyncData(success: () -> Unit, failed: () -> Unit) {
        LOGS.d(TAG, "getSyncData() ")
        updateDeviceDateTime()
        sessionManager.setSyncCompletedState(Event(SyncEvents.Started(0, 0)))
        //sessionManager.setShowSyncOfflineData(Event(false))
//        if (sessionManager == null || !sessionManager.isDeviceConnected()) {
//            LOGS.d(TAG, "OreoSyncDataWork: Device is disconnected")
////            sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Device_Disconnected.name, eventProperty)
//            return failed.invoke()
//        }

        if (sessionManager.transferInProgress) {
            LOGS.d(TAG, "Watchface/Ota Transfer in progress")
//            sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Error_Ota_Transfer.name, eventProperty)
            return success.invoke()
        }


        timer = Timer("DelayConnection", false).schedule(SyncingTimeOut) {
//            sessionManager.logAppEvent(FunnelEvents.SyncEvents.Syncing_Completed.name, eventProperty)
            //syncTime()
            AppLogs.sendAppLogs("OreoSyncDataWork timer time out SyncingTimeOut : $SyncingTimeOut  isDataReceived: $isDataReceived")
            if (isDataReceived) {
                return@schedule returnSuccess(success)
            } else {
                return@schedule returnSuccess(failed)
            }
        }


//        sessionManager.logAppEvent(FunnelEvents.SyncEvents.Syncing_Start.name, eventProperty)
        ringDataStore.getRingDevice()?.let {
            userActivityDataActions = userActivityHandler.getUserActivityActions(it)
            userActivityDataActions?.callbackListenerNew(object : IUserActivityDataCallback {
                override fun onUserActivityDataReceived(userActivityCallback: UserActivityCallback) {
//                    LOGS.d(TAG, "Received data $userActivityCallback")
                    isDataReceived = true
                    when (userActivityCallback) {
                        is UserActivityCallback.StepsDataObtainedOreo -> {
                            LOGS.d("OreoSyncDataWork: ${userActivityCallback.stepsData}")
                            AppLogs.sendAppLogs("SAVING_STEPS_DATA Started")
                            syncDataScope.launch {
                                syncRepository.saveStepsData(userActivityCallback.stepsData)
                                    .collect { resource ->
                                        when (resource) {
                                            is CacheResult.Success -> {
                                                AppLogs.sendAppLogs("SAVING_STEPS_DATA Success")
                                                LOGS.d(
                                                    TAG,
                                                    "OreoSyncDataWork: steps ${resource.value}"
                                                )
                                                sessionManager.setShowSyncOfflineData(
                                                    Event(
                                                        HealthOverviewDataType.STEPS
                                                    )
                                                )
                                            }

                                            is CacheResult.GenericError -> {
//                                                failed.invoke()
                                                LOGS.e(TAG, "OreoSyncDataWork: Error $it")

                                            }
                                        }
                                    }


                            }


                        }

                        is UserActivityCallback.HeartHistoryObtainedOreo -> {
//                            LOGS.d("OreoSyncDataWork: ${Gson().toJson(userActivityCallback.heartRateData)}")
                            syncDataScope.launch {
                                syncRepository.saveHeartRateData(userActivityCallback.heartRateData)
                                    .collect { resource ->
                                        when (resource) {
                                            is CacheResult.Success -> {
                                                LOGS.d(
                                                    TAG,
                                                    "OreoSyncDataWork: Heart ${resource.value}"
                                                )
                                                sessionManager.setShowSyncOfflineData(
                                                    Event(
                                                        HealthOverviewDataType.HEART
                                                    )
                                                )
                                            }

                                            is CacheResult.GenericError -> {
//                                                failed.invoke()
                                                LOGS.e("OreoSyncDataWork: Error $it")

                                            }
                                        }
                                    }


                            }


                        }

                        is UserActivityCallback.HealthScoreObtainedOreo -> {
                            syncDataScope.launch {
                                syncRepository.saveHealthScoreData(
                                    userActivityCallback.score,
                                    userActivityCallback.date
                                )
                                    .collect { resource ->
                                        when (resource) {
                                            is CacheResult.Success -> {

                                                LOGS.d(
                                                    TAG,
                                                    "OreoSyncDataWork: saveHealthScoreData ${resource.value}"
                                                )

                                            }

                                            is CacheResult.GenericError -> {
//                                                failed.invoke()
                                                LOGS.e(TAG, "OreoSyncDataWork: Error $it")

                                            }
                                        }
                                    }


                            }
                        }

                        is UserActivityCallback.NapObtainedOreo -> {
                            syncDataScope.launch {
                                syncRepository.saveNapData(userActivityCallback.napList)
                                    .collect { resource ->
                                        when (resource) {
                                            is CacheResult.Success -> {

                                                LOGS.d(
                                                    TAG,
                                                    "OreoSyncDataWork: nap ${resource.value}"
                                                )
                                                /* sessionManager.setShowSyncOfflineData(
                                                     Event(
                                                         HealthOverviewDataType.SLEEP
                                                     )
                                                 )*/

                                            }

                                            is CacheResult.GenericError -> {
//                                                failed.invoke()
                                                LOGS.e(TAG, "OreoSyncDataWork: Error $it")

                                            }
                                        }
                                    }

                            }
                        }

                        is UserActivityCallback.SleepDataObtainedOreo -> {
                            //      LOGS.d("OreoSyncDataWork: sleep data ${Gson().toJson(userActivityCallback.sleepData)}")
                            syncDataScope.launch {
                                syncRepository.saveSleepData(userActivityCallback.sleepData)
                                    .collect { resource ->
                                        when (resource) {
                                            is CacheResult.Success -> {

                                                LOGS.d(
                                                    TAG,
                                                    "OreoSyncDataWork: sleep ${resource.value}"
                                                )
                                                sessionManager.setShowSyncOfflineData(
                                                    Event(
                                                        HealthOverviewDataType.SLEEP
                                                    )
                                                )

                                            }

                                            is CacheResult.GenericError -> {
//                                                failed.invoke()
                                                LOGS.e(TAG, "OreoSyncDataWork: Error $it")

                                            }
                                        }
                                    }


                            }


                        }

                        is UserActivityCallback.OreoBloodOxygenObtained -> {
                            syncDataScope.launch {
                                syncRepository.saveBloodOxygenData(userActivityCallback.bloodOxygen)
                                    .collect { resource ->
                                        when (resource) {
                                            is CacheResult.Success -> {
                                                LOGS.d(
                                                    TAG,
                                                    "OreoSyncDataWork: blood ${resource.value}"
                                                )
                                                sessionManager.setShowSyncOfflineData(
                                                    Event(
                                                        HealthOverviewDataType.BLOOD
                                                    )
                                                )
                                            }

                                            is CacheResult.GenericError -> {
//                                                failed.invoke()
                                                LOGS.e(TAG, "OreoSyncDataWork: Error $it")

                                            }
                                        }
                                    }
                            }
                        }

                        is UserActivityCallback.OreoRingDayTimeMovementObtained -> {
                            syncDataScope.launch {
                                syncRepository.saveDayTimeMovementData(userActivityCallback.dayTimeMovement)
                                    .collect { resource ->
                                        when (resource) {
                                            is CacheResult.Success -> {
                                                LOGS.d(
                                                    TAG,
                                                    "OreoSyncDataWork: saveDayTimeMovementData ${resource.value}"
                                                )
                                                sessionManager.setShowSyncOfflineData(
                                                    Event(
                                                        HealthOverviewDataType.BLOOD
                                                    )
                                                )
                                            }

                                            is CacheResult.GenericError -> {
//                                                failed.invoke()
                                                LOGS.e(TAG, "OreoSyncDataWork: Error $it")

                                            }
                                        }
                                    }
                            }
                        }

                        is UserActivityCallback.OreoRespiratoryDataObtained -> {
                            syncDataScope.launch {
                                syncRepository.saveRespiratoryData(userActivityCallback.respiratoryData)
                                    .collect { resource ->
                                        when (resource) {
                                            is CacheResult.Success -> {
                                                LOGS.d(
                                                    TAG,
                                                    "OreoSyncDataWork: blood ${resource.value}"
                                                )
                                                sessionManager.setShowSyncOfflineData(
                                                    Event(
                                                        HealthOverviewDataType.BLOOD
                                                    )
                                                )
                                            }

                                            is CacheResult.GenericError -> {
//                                                failed.invoke()
                                                LOGS.e(TAG, "OreoSyncDataWork: Error $it")

                                            }
                                        }
                                    }
                            }
                        }

                        is UserActivityCallback.OreoBodyStressDataObtained -> {
                            syncDataScope.launch {
                                syncRepository.saveBodyStressData(userActivityCallback.bodyStressData)
                                    .collect { resource ->
                                        when (resource) {
                                            is CacheResult.Success -> {
                                                LOGS.d(
                                                    TAG,
                                                    "OreoSyncDataWork: BODY_STRESS ${resource.value}"
                                                )
                                                sessionManager.setShowSyncOfflineData(
                                                    Event(
                                                        HealthOverviewDataType.BODY_STRESS
                                                    )
                                                )
                                            }

                                            is CacheResult.GenericError -> {
//                                                failed.invoke()
                                                LOGS.e(TAG, "OreoSyncDataWork: Error $it")

                                            }
                                        }
                                    }
                            }
                        }


                        is UserActivityCallback.StressDataObtainedOreo -> {
//                            LOGS.d("OreoSyncDataWork: ${userActivityCallback.stressData}")
                            syncDataScope.launch {
                                syncRepository.saveStressData(userActivityCallback.stressData)
                                    .collect { resource ->
                                        when (resource) {
                                            is CacheResult.Success -> {
                                                LOGS.d(
                                                    TAG,
                                                    "OreoSyncDataWork: stress ${resource.value}"
                                                )
                                                sessionManager.setShowSyncOfflineData(
                                                    Event(
                                                        HealthOverviewDataType.STRESS
                                                    )
                                                )

                                            }

                                            is CacheResult.GenericError -> {
//                                                failed.invoke()
                                                LOGS.e(TAG, "OreoSyncDataWork: Error $it")

                                            }
                                        }
                                    }


                            }


                        }

                        is UserActivityCallback.BodyTemperatureObtainedOreo -> {
                            LOGS.d(
                                TAG,
                                "OreoSyncDataWork:::: BodyTemperatureObtained"
                            )
//                            LOGS.d("OreoSyncDataWork: ${userActivityCallback.stressData}")
                            syncDataScope.launch {
                                syncRepository.saveBodyTemperatureData(userActivityCallback.bodyTemperatureBreakupData)
                                    .collect { resource ->
                                        when (resource) {
                                            is CacheResult.Success -> {
                                                LOGS.d(
                                                    TAG,
                                                    "OreoSyncDataWork:::: BodyTemperatureObtained success"
                                                )
                                                sessionManager.setShowSyncOfflineData(
                                                    Event(
                                                        HealthOverviewDataType.TEMPERATURE
                                                    )
                                                )
                                            }

                                            is CacheResult.GenericError -> {
//                                                failed.invoke()
                                                LOGS.d(
                                                    TAG,
                                                    "OreoSyncDataWork:::: BodyTemperatureObtained failed"
                                                )

                                            }
                                        }
                                    }


                            }


                        }

                        is UserActivityCallback.UserDataSyncUpdated -> {
                            if (userActivityCallback.syncStatus is SyncEvents.Success) {
                                timer?.cancel()

                                AppLogs.sendAppLogs("RING SYNC TIME => ${System.currentTimeMillis() - lastTimeStamp}")
                                android.os.Handler(Looper.getMainLooper()).postDelayed({
                                    sessionManager.setSyncCompletedState(Event(userActivityCallback.syncStatus))
                                    returnSuccess(success)
                                }, 1000)

                            } else if (userActivityCallback.syncStatus is SyncEvents.Started) {
                                val total =
                                    (userActivityCallback.syncStatus as SyncEvents.Started).total
                                timer?.cancel()
                                val syncTime =
                                    if (total < 20)
                                        40 * 1000L
                                    else if (total in 20..49)
                                        70 * 1000L
                                    else
                                        100 * 1000L
                                timer = Timer("DelayConnection", false).schedule(syncTime) {
                                    //syncTime()
                                    AppLogs.sendAppLogs("OreoSyncDataWork inner timer time out SyncingTimeOut : $syncTime  isDataReceived: $isDataReceived")
                                    if (isDataReceived) {
                                        return@schedule returnSuccess(success)
                                    } else {
                                        return@schedule returnSuccess(failed)
                                    }
                                }
                                sessionManager.setSyncCompletedState(Event(userActivityCallback.syncStatus))
                            } else {
                                sessionManager.setSyncCompletedState(Event(userActivityCallback.syncStatus))
                            }

                        }

                        else -> {}
                    }
                    sessionManager.setUserActivityCallback(userActivityCallback)
                }

            })
        }
        lastTimeStamp = System.currentTimeMillis()
        sessionManager.sendUserActivityAction(
            UserActivityAction.SyncUserActivity(
                "",
                false
            )
        )
//        }
        LOGS.d(TAG, "getSyncData() Sync request sent")

    }

    private fun createForegroundInfo(title: String): ForegroundInfo {
        val notification = NotificationUtil.createDataSyncNotification(context, title)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(1, notification)
        }

    }

    private fun getTimeFormat(): String {
        var savedTimeFormat = localDataStore.getTimeFormat()
        if (savedTimeFormat.isNullOrEmpty()) {
            savedTimeFormat = TimeFormats.HOURS_12.type
            localDataStore.setTimeFormat(savedTimeFormat)
        }
        return savedTimeFormat
    }

    private fun syncTime() {
//        viewModel.setLoading(true)
        LOGS.d("$TAG syncing time")
        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetDeviceDateTime(
                Calendar.getInstance(), TimeFormat(getTimeFormat())
            )
        )


    }

    private fun handleAppVersion(context: Context, versionCheckResponse: VersionCheckResponse) {
        LOGS.d("handleAppVersion")
        var showNotification = false
        var description = versionCheckResponse.description
        if (description.isNullOrEmpty()) {
            description = context.getString(R.string.text_update_now_to_enjoy_new_features)
        }
        if (versionCheckResponse.upgradeType?.lowercase() == "force_upgrade") {
            showNotification = true
        } else if (versionCheckResponse.upgradeType?.lowercase() == "soft_upgrade" &&
            localDataStore.getIgnoreVersion() != versionCheckResponse.currentVersion
        ) {
            showNotification = true
        }

        /*if (versionCheckResponse.testMode.equals("1")) {
            localDataStore.setIsTestModeOn(true)
        } else {
            localDataStore.setIsTestModeOn(false)
        }*/
        if (showNotification) {
            LOGS.d("handleAppVersion show notification")
            val title = context.getString(R.string.text_update_app)
            NotificationUtil.showAppUpdateNotification(context, title, description)
        }


    }


    @SuppressLint("RestrictedApi")
    override fun startWork(): ListenableFuture<Result> {

//        sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Start.name, eventProperty)
        mFuture = SettableFuture.create()
        setForegroundAsync(createForegroundInfo("Syncing User Activity."))
        LOGS.d(TAG, "startWork()")



        if (sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess) {
            mFuture!!.set(Result.failure())
        }


        job = syncDataScope.launch {
            getSyncData(
                success = {
//                    sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Success.name, eventProperty)
                    if (localDataStore.isEnableGoogleFit()) {
                        syncDataScope.launch {
                            ApplicationUtils.startGoogleFitSyncScheduler(context)
                        }
                    }


                    ringDataStore.getRingDevice()?.let {
                        when (watchesSdk.getWatchType(it)) {
                            SDKWatchType.SDK_ZH -> {
                                syncDataScope.launch {
                                    //ApplicationUtils.startActivitySyncScheduler(context)
                                }
                            }

                            else -> {

                            }
                        }
                    }



                    sessionManager.forceSyncDataWithServer = false

                    sessionManager.saveLastSyncTime(DateFormats.getTimeStamp())
                    sessionManager.setSyncCompletedState(Event(SyncEvents.Success(100, 100)))
                    LOGS.d(TAG, "OreoSyncDataWork: Completedz")
                    mFuture!!.set(Result.success())
                    //  job.cancel()
                },
                failed = {
//                    sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Error.name, eventProperty)
                    sessionManager.forceSyncDataWithServer = false
                    sessionManager.setSyncCompletedState(Event(SyncEvents.Failed))
                    mFuture!!.set(Result.failure())
                }
            )
        }


        return mFuture!!
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
}

enum class HealthOverviewDataType {
    STEPS, HEART, SLEEP, BLOOD, BODY_STRESS, STRESS, TEMPERATURE, ALL, ACTIVITY, SERVER_SYNC_SUCCESS, AUTO_WORKOUT
}