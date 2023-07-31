package com.noisefit.receiver.workManager

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.Observer
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import com.google.common.util.concurrent.ListenableFuture
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.LastSyncItems
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.utils.Event
import com.noisefit.util.notif.NotificationUtil
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.interfaces.data.UserActivityAction
import com.noisefit_commans.interfaces.data.UserActivityCallback
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.models.SportsModeRequestList
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.schedule
import kotlin.coroutines.CoroutineContext

private const val SyncingTimeOut: Long = 30000

@HiltWorker
class ActivitySyncWork @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val localDataStore: DataStoredInterface,
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val lastSyncProvider: LastSyncProvider
) : ListenableWorker(context, workerParams) {

    private val TAG = "ActivitySyncWork"

    private var mFuture: SettableFuture<Result>? = null
    private var userActivityDataActions: UserActivityDataActions? = null
    private val syncDataScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext =
            Dispatchers.IO // no job added i.e + SupervisorJob()
    }

    lateinit var job: Job
    private var dataObserver: Observer<Event<UserActivityCallback>>? = null

    //TODO: get feature list here and hit only feature apis that is supported by bounded watch
    private fun returnSuccess(success: () -> Unit) {
        if (localDataStore.getUserToken() == null) {
            LOGS.d(TAG, "ActivitySyncWork: user is not logged in!!")
            return success.invoke()
        }

        if (!ApplicationUtils.isInternetConnected()) {
            LOGS.d(TAG, "ActivitySyncWork: No Internet Access!!")
            return success.invoke()
        }

        syncDataScope.launch {
            LOGS.d(TAG, "ActivitySyncWork: Sync start")
            supervisorScope {

                val activities = userRepository.getUnSyncedActivities()
                if (activities.isEmpty()) {
                    LOGS.d(TAG, "ActivitySyncWork: Activities empty")
                    return@supervisorScope
                }

                val requestObject = SportsModeRequestList()
                activities.forEach {
                    it.time = DateFormats.formatDateTime(
                        it.time,
                        DateFormats.dateTimeFormatISO,
                        DateFormats.timeWithSecond
                    )
                }

                requestObject.activities = activities

                val call = async {
                    userRepository.postActivities(requestObject).collect { resource ->
                        when (resource) {
                            is Resource.GenericError -> {
                                LOGS.d(TAG, "ActivitySyncWork: postActivities " + resource.message)
                            }
                            is Resource.Loading -> {
                            }
                            is Resource.NetworkError -> {
                                LOGS.d(TAG, "ActivitySyncWork: postActivities " + resource.response)
                            }
                            is Resource.Success -> {
                                resource.data?.let {
                                    if (it.success == true) {
                                        LOGS.d(
                                            TAG,
                                            "ActivitySyncWork: Activities Synced Successfully"
                                        )
                                        userRepository.setActivitiesSynced()
                                        sessionManager.setShowSyncOfflineData(
                                            Event(
                                                HealthOverviewDataType.ACTIVITY
                                            )
                                        )
                                    }
                                    lastSyncProvider.removeSyncTimeStamp(LastSyncItems.RECENT_ACTIVITIES)
                                }
                            }
                        }
                    }
                }
                try {
                    call.await()
                } catch (e: Exception) {

                }

                if (::job.isInitialized) {
                    job.cancel()
                }
                LOGS.d(TAG, "Job finished")
            }


        }

        return success.invoke()
    }


    private fun getSyncData(success: () -> Unit, failed: () -> Unit) {
        if (sessionManager == null || !sessionManager.isDeviceConnected()) {
            LOGS.d(TAG, "ActivitySyncWork: Device is disconnected")
            return failed.invoke()
        }
        if (sessionManager.transferInProgress) {
            LOGS.d(TAG, "Watchface/Ota Transfer in progress")
            return failed.invoke()
        }


        val timer = Timer("DelayConnection", false).schedule(SyncingTimeOut) {
            return@schedule returnSuccess(success)
        }

        dataObserver = Observer<Event<UserActivityCallback>> { event ->
            event.peekContent()?.let {
                if (it is UserActivityCallback.SportsModeDataSyncSuccess) {
                    LOGS.d(TAG, "SportsModeDataSyncSuccess")
                    Handler(Looper.getMainLooper()).postDelayed({
                        timer.cancel()
                        returnSuccess(success)
                        dataObserver?.let { it1 ->
                            sessionManager.userActivityCallback.removeObserver(
                                it1
                            )
                        }
                    }, 2000)
                }
            }
        }


        Handler(Looper.getMainLooper()).post {

            dataObserver?.let {
                sessionManager.userActivityCallback.observeForever(it)
            }


            /*sessionManager.userActivityCallback.observeForever() { event ->
                event.peekContent()?.let {
                    if (it is UserActivityCallback.SportsModeDataSyncSuccess) {
                        LOGS.d(TAG, "SportsModeDataSyncSuccess")
                        Handler(Looper.getMainLooper()).postDelayed({
                            timer.cancel()
                            returnSuccess(success)
                            sessionManager.userActivityCallback.removeObserver()

                        }, 2000)
                    }
                }
            }*/
        }


        sessionManager.sendUserActivityAction(UserActivityAction.SyncSportsActivity(""))
    }

    private fun createForegroundInfo(title: String): ForegroundInfo {
        val notification = NotificationUtil.createDataSyncNotification(context, title)
        return ForegroundInfo(1, notification)
    }



    @SuppressLint("RestrictedApi")
    override fun startWork(): ListenableFuture<Result> {
        mFuture = SettableFuture.create()
        setForegroundAsync(createForegroundInfo("Syncing User Activities"))
        job = syncDataScope.launch {
            getSyncData(
                success = {
                    Handler(Looper.getMainLooper()).post {
                        dataObserver?.let { sessionManager.userActivityCallback.removeObserver(it) }
                    }
                    LOGS.d(TAG, "ActivitySyncWork: Completed")
                    mFuture!!.set(Result.success())
                    //  job.cancel()
                },
                failed = {

                    mFuture!!.set(Result.failure())
                }
            )
        }


        return mFuture!!
    }
}