package com.noisefit.receiver.workManager

import android.annotation.SuppressLint
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import com.noisefit.session.SessionManager
import com.noisefit.util.notif.NotificationUtil
import com.noisefit.watch.DeviceQueryHandler
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.utils.DateFormats.checkTimeDifferenceMoreThanN
import com.noisefit_commans.utils.LOGS
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


private const val TAG = "AgpsReminderWork"

@HiltWorker
class AgpsReminderWork
@AssistedInject
constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val localDataStore: DataStoredInterface,
    private val sessionManager: SessionManager,
    private val deviceQueryHandler: DeviceQueryHandler,
) : ListenableWorker(context, workerParams) {

    private var queryDeviceDataActions: QueryDeviceDataActions? = null
    private var mFuture: SettableFuture<Result>? = null
    private val syncDataScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext =
            Dispatchers.IO // no job added i.e + SupervisorJob()
    }

    lateinit var job: Job

    private fun returnSuccess(success: () -> Unit) {
        return success.invoke()
    }

    private fun clearJob() {
        if (::job.isInitialized) {
            job.cancel()
            LOGS.d("SyncDataWork: Google fit Sync complete")
        }
    }

    private suspend fun getSyncData(success: () -> Unit, failed: () -> Unit) {

        if (sessionManager == null || !sessionManager.isDeviceConnected()) {
            LOGS.d(TAG, "$TAG: Device is disconnected")
            return returnSuccess(success)
        }

        job = syncDataScope.launch {
            supervisorScope {
                LOGS.d(TAG, "$TAG: inside job")

                var agpsStatusRequired = false
                val localAgpsStatusState = localDataStore.getAGPSStatusState()

                LOGS.d(TAG, "$TAG: ${Gson().toJson(localAgpsStatusState)}")
                agpsStatusRequired =
                    if (localAgpsStatusState?.state == null || localAgpsStatusState.lastUpdated == null) {
                        true
                    } else {
                        localAgpsStatusState.lastUpdated!!.checkTimeDifferenceMoreThanN(24)
                    }

                LOGS.d(TAG, "$TAG: agpsStatusRequired status:: $agpsStatusRequired")
                if (!agpsStatusRequired) {
                    if (localAgpsStatusState?.state?.status.equals(com.noisefit_commans.data.model.AGPSStatusEnum.EXPIRED.status)) {
                        NotificationUtil.postAGPSNotification(context)
                    }
                    returnSuccess(success)
                } else {
                    sessionManager.sendQueryAction(QueryAction.GetAgpsState)
                }


            }
        }
        returnSuccess(success)
    }

    @SuppressLint("RestrictedApi")
    override fun startWork(): ListenableFuture<Result> {
        mFuture = SettableFuture.create()
        job = syncDataScope.launch {
            getSyncData(
                success = {
                    LOGS.d("$TAG: success")
                    clearJob()
                    mFuture?.set(Result.success())
                },
                failed = {
                    LOGS.d("$TAG: failed")
                    clearJob()
                    mFuture?.set(Result.failure())
                }
            )
        }


        return mFuture!!
    }
}