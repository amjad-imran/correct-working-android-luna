package com.noisefit.receiver.workManager

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit.R
import com.noisefit.data.repository.abstraction.DownloadRepository
import com.noisefit.data.repository.implementation.Download
import com.noisefit.session.SessionManager
import com.noisefit.util.UniqueWatchFaceSyncWorkName
import com.noisefit.util.notif.NotificationUtil
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.DiyCustomWatchFace
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.WatchUpdateStatus
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LogEvents
import com.noisefit_commans.utils.WatchFaceEvents
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.CoroutineContext


private inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

private const val TAG = "DiyWatchfaceWork"

@HiltWorker
class DiyWatchfaceWork
@AssistedInject
constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val sessionManager: SessionManager,
    private val downloadRepository: DownloadRepository
) : ListenableWorker(context, workerParams), LifecycleOwner {

    lateinit var job: Job
    private var mFuture: SettableFuture<Result>? = null
    private var watchFace: DiyCustomWatchFace? = null


    private lateinit var lifecycleRegistry: LifecycleRegistry

    private val syncDataScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext =
            Dispatchers.IO // no job added i.e + SupervisorJob()
    }

    private fun createForegroundInfo(title: String): ForegroundInfo {
        val notification = NotificationUtil.createDataSyncNotification(context, title)
        return ForegroundInfo(2140, notification)
    }


    @SuppressLint("RestrictedApi")
    override fun startWork(): ListenableFuture<Result> {
        mFuture = SettableFuture.create()

        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED

        setForegroundAsync(createForegroundInfo("Transferring Watchface"))

        tryCatch {
            watchFace =
                Gson().fromJson(inputData.getString("watchFace"), DiyCustomWatchFace::class.java)
        }



        if (!sessionManager.isDeviceConnected()) {
            mFuture!!.set(Result.failure())
            sessionManager.diyWatchFaceTransferStates.value =
                Event(DiyWatchFaceTransferStates.Failed(context.getString(R.string.text_device_not_connected)))
            sessionManager.diyWatchFaceTransferStates.value =
                Event(DiyWatchFaceTransferStates.Default)
            return mFuture!!
        }

      //  LOGS.d(TAG, " Work_Started ${Gson().toJson(watchFace)}")
        if (watchFace == null || watchFace?.binFile.isNullOrEmpty()) {
            mFuture!!.set(Result.failure())
            sessionManager.diyWatchFaceTransferStates.value =
                Event(DiyWatchFaceTransferStates.Failed(context.getString(R.string.text_something_went_wrong)))
            sessionManager.diyWatchFaceTransferStates.value =
                Event(DiyWatchFaceTransferStates.Default)
            return mFuture!!
        }
        sessionManager.diyWatchFaceTransferStates.value =
            Event(DiyWatchFaceTransferStates.Started(watchFace!!))



        job = syncDataScope.launch {
            downloadWatchFace(
                watchFace?.binFile ?: "",
                success = { file ->
                    syncDataScope.launch(Dispatchers.Main) {
                        transferWatchFace(file,
                            success = {
                                mFuture!!.set(Result.success())
                                lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
                                WorkManager.getInstance(context)
                                    .cancelUniqueWork(UniqueWatchFaceSyncWorkName)
                                LOGS.d(TAG, " Work Success")
                            },
                            fail = {
                                sessionManager.watchFaceTransferStates.value =
                                    Event(WatchFaceTransferStates.Failed("Error"))
                                sessionManager.watchFaceTransferStates.value =
                                    Event(WatchFaceTransferStates.Default)
                                mFuture!!.set(Result.failure())
                                lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
                                WorkManager.getInstance(context)
                                    .cancelUniqueWork(UniqueWatchFaceSyncWorkName)
                                LOGS.d(TAG, " Work Failed")
                            })
                    }
                },
                failed = { reason ->
                    syncDataScope.launch(Dispatchers.Main) {
                        sessionManager.watchFaceTransferStates.value =
                            Event(WatchFaceTransferStates.Failed(""))
                        sessionManager.watchFaceTransferStates.value =
                            Event(WatchFaceTransferStates.Default)
                        mFuture!!.set(Result.failure())
                        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
                        WorkManager.getInstance(context)
                            .cancelUniqueWork(UniqueWatchFaceSyncWorkName)
                        LOGS.d(TAG, " Work Failed")

                    }
                }
            )

        }

        return mFuture!!
    }

    private fun transferWatchFace(
        file: File, success: () -> Unit,
        fail: () -> Unit
    ) {

        LOGS.d("$TAG transferWatchFace")
        Handler(Looper.getMainLooper()).post {
            sessionManager.updateDeviceCallback.observe(this@DiyWatchfaceWork) { event ->
                event.getContent()?.let {
                    if (it is UpdateDeviceDataCallback.CustomizeWatchFaceProgress) {
                        LOGS.d("$TAG transferWatchFace inside")
                        updateWatchFaceStatus(it.watchUpdateStatus,
                            success = {
                                success()
                            },
                            fail = {
                                fail()
                            })

                    }
                }
            }
        }


        val fileUri = Uri.fromFile(file)?.toString()
        fileUri?.let {
            watchFace?.binFile = it
            sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetDiyWatchFaceCustom(watchFace!!))
        }

    }

    private fun updateWatchFaceStatus(
        watchUpdateStatus: WatchUpdateStatus,
        success: () -> Unit,
        fail: () -> Unit
    ) {
        when (watchUpdateStatus.status) {
            UpdateStatus.STARTED -> {
                LOGS.d("$TAG STARTED")
                sessionManager.diyWatchFaceTransferStates.value =
                    Event(
                        DiyWatchFaceTransferStates.Started(
                            watchFace!!
                        )
                    )
            }

            UpdateStatus.PROGRESS -> {

                sessionManager.diyWatchFaceTransferStates.value = Event(
                    DiyWatchFaceTransferStates.Progress(
                        watchUpdateStatus.percentagePercentage ?: 0
                    )
                )
            }

            UpdateStatus.COMPLETED -> {
                LOGS.d("$TAG COMPLETED")
                sessionManager.diyWatchFaceTransferStates.value =
                    Event(DiyWatchFaceTransferStates.Success(watchFace!!))
                sessionManager.diyWatchFaceTransferStates.value =
                    Event(DiyWatchFaceTransferStates.Default)
                success()
            }

            UpdateStatus.ERROR -> {
                LOGS.d("$TAG ERROR")
                var message = "Error"

                if(!watchUpdateStatus.message.isNullOrEmpty()){
                    message = watchUpdateStatus.message!!
                }
                sessionManager.diyWatchFaceTransferStates.value =
                    Event(DiyWatchFaceTransferStates.Failed(message))
                sessionManager.diyWatchFaceTransferStates.value =
                    Event(DiyWatchFaceTransferStates.Default)
                fail()
            }

            UpdateStatus.BATTERY_LOW -> {
                sessionManager.diyWatchFaceTransferStates.value =
                    Event(DiyWatchFaceTransferStates.Failed("Battery low"))
                sessionManager.diyWatchFaceTransferStates.value =
                    Event(DiyWatchFaceTransferStates.Default)
                fail()
            }

            else -> {}
        }
    }


    private suspend fun downloadWatchFace(
        binUrl: String,
        success: (File) -> Unit,
        failed: (String) -> Unit
    ) {

        var fileName: String? = "watchface.zip"
        val extension: String = binUrl.substring(binUrl.lastIndexOf("."))

        if (extension == ".tar") {
            fileName = getZipFileNameForRyeex(binUrl)
            LOGS.d("updateWatchface ${getZipFileNameForRyeex(binUrl)} $binUrl")
            if (fileName.isNullOrEmpty()) {
                failed(context.getString(R.string.text_something_went_wrong))
                return
            }

        }

        withContext(Dispatchers.Main) {
            sessionManager.diyWatchFaceTransferStates.value =
                Event(DiyWatchFaceTransferStates.Downloading(false))
        }


        downloadRepository.downloadFileFromUrl(
            binUrl,
            context.externalCacheDir ?: context.cacheDir,
            fileName!!
        ).collect {
            when (it) {
                is Download.Finished -> {
                    withContext(Dispatchers.Main) {
                        sessionManager.diyWatchFaceTransferStates.value =
                            Event(DiyWatchFaceTransferStates.Downloading(true))
                    }

                    //_setWatchFace.postValue(Event(it.file))
                    success(it.file)
                }

                is Download.Progress -> {
                    LOGS.i("${it.percent}")
                    //_watchFaceDownloadProgress.postValue(Event(it.percent))
                }

                is Download.Failed -> {

                    AppLogs.sendAppLogs(LogEvents.WatchFace, WatchFaceEvents.DownloadFileFailed)
                    failed("Download failed")
                }
            }
        }

    }

    private fun getZipFileNameForRyeex(zipFile: String): String? {
        val fileName = zipFile.split("/").last()
        return fileName.ifEmpty {
            null
        }
    }

    override fun onStopped() {
        super.onStopped()
        job.cancel()
    }


    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

}

sealed class DiyWatchFaceTransferStates {
    object Default : DiyWatchFaceTransferStates()
    data class Started(val watchFace: DiyCustomWatchFace) : DiyWatchFaceTransferStates()
    data class Downloading(val isCompleted: Boolean) : DiyWatchFaceTransferStates()
    object TransferStarted : DiyWatchFaceTransferStates()
    data class Success(val watchFace: DiyCustomWatchFace) : DiyWatchFaceTransferStates()
    data class Progress(val percent: Int) : DiyWatchFaceTransferStates()
    data class Failed(val reason: String) : DiyWatchFaceTransferStates()
}