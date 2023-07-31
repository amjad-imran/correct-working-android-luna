package com.noisefit.receiver.workManager

import android.annotation.SuppressLint
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import com.google.common.util.concurrent.ListenableFuture
import com.noisefit.luna.R
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.VersionCheckResponse
import com.noisefit.data.repository.abstraction.SyncRepository
import com.noisefit.session.SessionManager
import com.noisefit.util.*
import com.noisefit.util.moveToServer.SleepNotificationUtils
import com.noisefit.util.notif.NotificationUtil
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.UserActivityHandler
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.constants.EventConstants
import com.noisefit_commans.interfaces.data.IUserActivityDataCallback
import com.noisefit_commans.interfaces.data.UserActivityAction
import com.noisefit_commans.interfaces.data.UserActivityCallback
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.SyncDataStatus
import com.noisefit_commans.models.TimeFormat
import com.noisefit_commans.models.TimeFormats
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.enums.Device
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.schedule
import kotlin.coroutines.CoroutineContext

private const val SyncingTimeOut: Long = 10000
private const val SyncWithServerTime: Long = 10800000 //10800000

@HiltWorker
class SyncDataWork
@AssistedInject
constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val localDataStore: DataStoredInterface,
    private val sessionManager: SessionManager,
    private val syncRepository: SyncRepository,
    private val userActivityHandler: UserActivityHandler,
    private val watchesSdk: WatchesSDK,
    private val sleepNotificationUtils: SleepNotificationUtils
) : ListenableWorker(context, workerParams) {

    private val TAG = "SyncDataWork"

    private var mFuture: SettableFuture<Result>? = null
    private var userActivityDataActions: UserActivityDataActions? = null
    private val syncDataScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext =
            Dispatchers.IO // no job added i.e + SupervisorJob()
    }

    lateinit var job: Job

    private fun shouldSync(): Boolean {
        if (sessionManager.forceSyncDataWithServer) {
            LOGS.d(TAG, "SyncDataWork: session is hacked!! Allowing syncing data with server")
            return true
        }
        val lastSyncTime = localDataStore.getLastSyncWithServer()
        if (kotlin.math.abs(DateFormats.getTimeStamp() - lastSyncTime) > SyncWithServerTime) {
            return true
        }

        return false
    }

    //TODO: get feature list here and hit only feature apis that is supported by bounded watch
    private fun returnSuccess(success: () -> Unit) {
        sessionManager.setShowSyncOfflineData(Event(HealthOverviewDataType.ALL))
        if (localDataStore.getUserToken() == null) {
            LOGS.d(TAG, "SyncDataWork: user is not logged in!!")
//            sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Completed_Without_Logged_In.name, eventProperty)
            return success.invoke()
        }

        if (!ApplicationUtils.isInternetConnected()) {
            LOGS.d(TAG, "SyncDataWork: No Internet Access!!")
//            sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Completed_Without_Internet.name, eventProperty)
            return success.invoke()
        }

        if (!shouldSync()) {
//            sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Completed_Without_Uploading.name, eventProperty)
            LOGS.d(
                TAG, "SyncDataWork: No time to sync(die)" +
                        "current timestamp: ${DateFormats.getTimeStamp()} " +
                        "last Sync at: ${localDataStore.getLastSyncTimeStamp()}!!"
            )
            return success.invoke()
        }

//        sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Start_Uploading_Data.name, eventProperty)
        syncDataScope.launch {
            LOGS.d(TAG, "SyncDataWork: Sync start")
            supervisorScope {
                val userActivities = syncRepository.getUnSyncUserActivities()

                syncRepository.getTodaySleepData().collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            sleepNotificationUtils.handleSleepData(resource.value)
                        }

                        else -> {}
                    }
                }

                val call1 = async {
                    syncRepository.postDataToServer(userActivities.first)?.collect { resource ->
                        when (resource) {
                            is Resource.GenericError -> {
//                                sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Error_Uploading_Data.name, eventProperty)
                                LOGS.d(TAG, "SyncDataWork: combinedData1 " + resource.message)
                            }

                            is Resource.Loading -> {

                            }

                            is Resource.NetworkError -> {
//                                sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Error_Uploading_Data.name, eventProperty)
                                LOGS.d(TAG, "SyncDataWork: combinedData1 " + resource.response)
                            }

                            is Resource.Success -> {

//                                sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Completed_Uploading_Data.name, eventProperty)
//                                sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Completed.name, eventProperty)
//                                localDataStore.setLastStepsSyncWithServer(DateFormats.getTimeStamp())
                                resource.data?.data?.let {
                                    handleAppVersion(context, it)

                                }
                                syncDataScope.launch {
                                    syncRepository.deleteServerSyncData(userActivities.second)
                                }

                                syncRepository.updateHashForLastSyncData(userActivities.first)
                                if (!userActivities.first.stepsDataList.isNullOrEmpty()) {
                                    sessionManager.needToUpdateStreakData.postValue(
                                        Event(
                                            Pair(
                                                System.currentTimeMillis(),
                                                true
                                            )
                                        )
                                    )
                                }
                                LOGS.d(TAG, "SyncDataWork::: combinedData1 " + resource.data)
                            }
                        }
                    }
                }

                val call2 = if (!userActivities.first.sleepData.isNullOrEmpty()) {
                    async {

                        syncRepository.postSleepHistoryData(userActivities.first)
                            ?.collect { resource ->
                                when (resource) {
                                    is Resource.GenericError -> {

                                        LOGS.d(
                                            TAG,
                                            "SyncDataWork: sleep " + resource.message
                                        )
                                    }

                                    is Resource.Loading -> {

                                    }

                                    is Resource.NetworkError -> {
                                        LOGS.d(
                                            TAG,
                                            "SyncDataWork: sleep " + resource.response
                                        )
                                    }

                                    is Resource.Success -> {
                                        syncDataScope.launch {
                                            syncRepository.deleteSleepServerSyncData(userActivities.second)
                                        }
                                        syncRepository.updateSleepHashForLastSyncData(userActivities.first)

                                        LOGS.d(
                                            TAG,
                                            "SyncDataWork::: sleep " + resource.data
                                        )
                                    }
                                }
                            }
                    }
                } else null




                try {
                    call1.await()
                    call2?.await()
                } catch (e: Exception) {

                }


                localDataStore.setLastSyncWithServer(DateFormats.getTimeStamp())

                if (::job.isInitialized) {
                    job.cancel()
                }
                LOGS.d(TAG, "SyncDataWork: Sync complete")
            }


        }

        return success.invoke()
    }


    private suspend fun getSyncData(success: () -> Unit, failed: () -> Unit) {
        sessionManager.setSyncCompletedState(Event(SyncDataStatus(status = EventConstants.UPDATE_STATUS_STARTED)))
        //sessionManager.setShowSyncOfflineData(Event(false))
//        if (sessionManager == null || !sessionManager.isDeviceConnected()) {
//            LOGS.d(TAG, "SyncDataWork: Device is disconnected")
////            sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Device_Disconnected.name, eventProperty)
//            return failed.invoke()
//        }

        if (sessionManager.transferInProgress) {
            LOGS.d(TAG, "Watchface/Ota Transfer in progress")
//            sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Error_Ota_Transfer.name, eventProperty)
            return success.invoke()
        }


        val timer = Timer("DelayConnection", false).schedule(SyncingTimeOut) {
//            sessionManager.logAppEvent(FunnelEvents.SyncEvents.Syncing_Completed.name, eventProperty)
            //syncTime()
            return@schedule returnSuccess(success)
        }


//        sessionManager.logAppEvent(FunnelEvents.SyncEvents.Syncing_Start.name, eventProperty)
        localDataStore.getConnectedDevice()?.let {
            userActivityDataActions = userActivityHandler.getUserActivityActions(it)
            userActivityDataActions?.callbackListenerNew(object : IUserActivityDataCallback {
                override fun onUserActivityDataReceived(userActivityCallback: UserActivityCallback) {
                    LOGS.d(TAG, "Received data $userActivityCallback")
                    when (userActivityCallback) {
                        is UserActivityCallback.StepsDataObtained -> {
//                            LOGS.d("SyncDataWork: ${userActivityCallback.stepsData}")
                            job = syncDataScope.launch {
                                syncRepository.saveStepsData(userActivityCallback.stepsData)
                                    .collect { resource ->
                                        when (resource) {
                                            is CacheResult.Success -> {
                                                LOGS.d(TAG, "SyncDataWork: steps ${resource.value}")
                                                sessionManager.setShowSyncOfflineData(
                                                    Event(
                                                        HealthOverviewDataType.STEPS
                                                    )
                                                )
                                            }

                                            is CacheResult.GenericError -> {
//                                                failed.invoke()
                                                LOGS.e(TAG, "SyncDataWork: Error $it")

                                            }
                                        }
                                    }


                            }


                        }

                        is UserActivityCallback.HeartHistoryObtained -> {
//                            LOGS.d("SyncDataWork: ${Gson().toJson(userActivityCallback.heartRateData)}")
                            job = syncDataScope.launch {
                                syncRepository.saveHeartRateData(userActivityCallback.heartRateData)
                                    .collect { resource ->
                                        when (resource) {
                                            is CacheResult.Success -> {
                                                LOGS.d(TAG, "SyncDataWork: Heart ${resource.value}")
                                                sessionManager.setShowSyncOfflineData(
                                                    Event(
                                                        HealthOverviewDataType.HEART
                                                    )
                                                )
                                            }

                                            is CacheResult.GenericError -> {
//                                                failed.invoke()
                                                LOGS.e("SyncDataWork: Error $it")

                                            }
                                        }
                                    }


                            }


                        }

                        is UserActivityCallback.SleepDataObtained -> {
                            //      LOGS.d("SyncDataWork: sleep data ${Gson().toJson(userActivityCallback.sleepData)}")
                            job = syncDataScope.launch {
                                syncRepository.saveSleepData(userActivityCallback.sleepData)
                                    .collect { resource ->
                                        when (resource) {
                                            is CacheResult.Success -> {

                                                LOGS.d(TAG, "SyncDataWork: sleep ${resource.value}")
                                                sessionManager.setShowSyncOfflineData(
                                                    Event(
                                                        HealthOverviewDataType.SLEEP
                                                    )
                                                )

                                            }

                                            is CacheResult.GenericError -> {
//                                                failed.invoke()
                                                LOGS.e(TAG, "SyncDataWork: Error $it")

                                            }
                                        }
                                    }


                            }


                        }

                        is UserActivityCallback.BloodOxygenObtained -> {
//                            LOGS.d("SyncDataWork: ${userActivityCallback.bloodOxygen}")
                            job = syncDataScope.launch {
                                syncRepository.saveBloodOxygenData(userActivityCallback.bloodOxygen)
                                    .collect { resource ->
                                        when (resource) {
                                            is CacheResult.Success -> {
                                                LOGS.d(TAG, "SyncDataWork: blood ${resource.value}")
                                                sessionManager.setShowSyncOfflineData(
                                                    Event(
                                                        HealthOverviewDataType.BLOOD
                                                    )
                                                )
                                            }

                                            is CacheResult.GenericError -> {
//                                                failed.invoke()
                                                LOGS.e(TAG, "SyncDataWork: Error $it")

                                            }
                                        }
                                    }


                            }


                        }

                        is UserActivityCallback.StressDataObtained -> {
//                            LOGS.d("SyncDataWork: ${userActivityCallback.stressData}")
                            job = syncDataScope.launch {
                                syncRepository.saveStressData(userActivityCallback.stressData)
                                    .collect { resource ->
                                        when (resource) {
                                            is CacheResult.Success -> {
                                                LOGS.d(
                                                    TAG,
                                                    "SyncDataWork: stress ${resource.value}"
                                                )
                                                sessionManager.setShowSyncOfflineData(
                                                    Event(
                                                        HealthOverviewDataType.STRESS
                                                    )
                                                )

                                            }

                                            is CacheResult.GenericError -> {
//                                                failed.invoke()
                                                LOGS.e(TAG, "SyncDataWork: Error $it")

                                            }
                                        }
                                    }


                            }


                        }

                        is UserActivityCallback.BodyTemperatureObtained -> {
                            LOGS.d(
                                TAG,
                                "SyncDataWork:::: BodyTemperatureObtained"
                            )
//                            LOGS.d("SyncDataWork: ${userActivityCallback.stressData}")
                            job = syncDataScope.launch {
                                syncRepository.saveBodyTemperatureData(userActivityCallback.bodyTemperatureBreakupData)
                                    .collect { resource ->
                                        when (resource) {
                                            is CacheResult.Success -> {
                                                LOGS.d(
                                                    TAG,
                                                    "SyncDataWork:::: BodyTemperatureObtained success"
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
                                                    "SyncDataWork:::: BodyTemperatureObtained failed"
                                                )

                                            }
                                        }
                                    }


                            }


                        }

                        is UserActivityCallback.UserDataSyncUpdated -> {
                            if (userActivityCallback.syncDataStatus.status == EventConstants.UPDATE_STATUS_SUCCESS) {
                                timer.cancel()
                                //  syncTime()
//                                sessionManager.logAppEvent(FunnelEvents.SyncEvents.Syncing_Completed.name, eventProperty)
                                returnSuccess(success)
                            }
                        }

                        else -> {}
                    }
                    sessionManager.setUserActivityCallback(userActivityCallback)
                }

            })
        }

        sessionManager.sendUserActivityAction(
            UserActivityAction.SyncUserActivity(
                "",
                false
            )
        )
//        }

    }

    private fun createForegroundInfo(title: String): ForegroundInfo {
        val notification = NotificationUtil.createDataSyncNotification(context, title)
        return ForegroundInfo(1, notification)
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

        if (versionCheckResponse.testMode.equals("1")) {
            localDataStore.setIsTestModeOn(true)
        } else {
            localDataStore.setIsTestModeOn(false)
        }
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
        job = syncDataScope.launch {
            getSyncData(
                success = {
//                    sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Success.name, eventProperty)
                    if (localDataStore.isEnableGoogleFit()) {
                        syncDataScope.launch {
                            ApplicationUtils.startGoogleFitSyncScheduler(context)
                        }
                    }


                    localDataStore.getConnectedDevice()?.let {
                        when (watchesSdk.getWatchType(it)) {
                            SDKWatchType.SDK_NAV_PLUS,
                            SDKWatchType.SDK_RYEEX,
                            SDKWatchType.SDK_ZH -> {
                                syncDataScope.launch {
                                    ApplicationUtils.startActivitySyncScheduler(context)
                                }
                            }

                            SDKWatchType.SDK_QUBE -> {
                                syncTime()
                            }

                            else -> {

                            }
                        }


                        /*if (watchesSdk.watchHasAGPS(it)) {
                            syncDataScope.launch {
                                ApplicationUtils.startAGPSScheduler(context)
                            }
                        }*/

                    }



                    sessionManager.forceSyncDataWithServer = false

                    sessionManager.saveLastSyncTime(Device.SMARTWATCH, DateFormats.getTimeStamp())
                    sessionManager.setSyncCompletedState(Event(SyncDataStatus(status = EventConstants.UPDATE_STATUS_SUCCESS)))
                    LOGS.d(TAG, "SyncDataWork: Completedz")
                    mFuture!!.set(Result.success())
                    //  job.cancel()
                },
                failed = {
//                    sessionManager.logAppEvent(FunnelEvents.SyncEvents.Sync_Error.name, eventProperty)
                    sessionManager.forceSyncDataWithServer = false
                    sessionManager.setSyncCompletedState(Event(SyncDataStatus(status = EventConstants.UPDATE_STATUS_FAILED)))
                    mFuture!!.set(Result.failure())
                }
            )
        }


        return mFuture!!
    }
}

enum class HealthOverviewDataType {
    STEPS, HEART, SLEEP, BLOOD, STRESS, TEMPERATURE, ALL, ACTIVITY, SERVER_SYNC_SUCCESS
}