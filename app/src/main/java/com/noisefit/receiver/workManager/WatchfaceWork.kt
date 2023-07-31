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
import androidx.lifecycle.viewModelScope
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.noisefit.R
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.data.repository.abstraction.DownloadRepository
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit.data.repository.abstraction.WatchFaceRepository
import com.noisefit.data.repository.implementation.Download
import com.noisefit.session.SessionManager
import com.noisefit.util.UniqueWatchFaceSyncWorkName
import com.noisefit.util.notif.NotificationUtil
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.WatchFace
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.CoroutineContext

private inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

@HiltWorker
class WatchfaceWork
@AssistedInject
constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val sessionManager: SessionManager,
    private val localDataStore: DataStoredInterface,
    private val rewardsRepository: RewardsRepository,
    private val downloadRepository: DownloadRepository,
    private val watchFaceRepository: WatchFaceRepository,
) : ListenableWorker(context, workerParams), LifecycleOwner {

    lateinit var job: Job
    private var mFuture: SettableFuture<Result>? = null
    var watchFace: Watchface2? = null

    private val TAG = "WatchfaceWork"


    private lateinit var lifecycleRegistry: LifecycleRegistry

    private val syncDataScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext =
            Dispatchers.IO // no job added i.e + SupervisorJob()
    }

    private fun createForegroundInfo(title: String): ForegroundInfo {
        val notification = NotificationUtil.createDataSyncNotification(context, title)
        return ForegroundInfo(2139, notification)
    }


    @SuppressLint("RestrictedApi")
    override fun startWork(): ListenableFuture<Result> {
        mFuture = SettableFuture.create()
     //   sessionManager.watchFaceTransferStates.value = null
        lifecycleRegistry = LifecycleRegistry(this)

        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED

        setForegroundAsync(createForegroundInfo("Transferring Watchface"))

        tryCatch {
            watchFace = null
            watchFace =
                Gson().fromJson(inputData.getString("watchFace"), Watchface2::class.java)
            LOGS.d("setWatchFace watchFace ${Gson().toJson(watchFace)}")
        }



        if (!sessionManager.isDeviceConnected()) {
            mFuture!!.set(Result.failure())
            sessionManager.diyWatchFaceTransferStates.value =
                Event(DiyWatchFaceTransferStates.Failed(context.getString(R.string.text_device_not_connected)))
            sessionManager.diyWatchFaceTransferStates.value =
                Event(DiyWatchFaceTransferStates.Default)
            return mFuture!!
        }

//        LOGS.d(TAG, " Work Started ${Gson().toJson(watchFace)}")
        if (watchFace == null || watchFace?.zipFile.isNullOrEmpty()) {
            mFuture!!.set(Result.failure())
            sessionManager.watchFaceTransferStates.value =
                Event(WatchFaceTransferStates.Failed(context.getString(R.string.text_something_went_wrong)))
            sessionManager.watchFaceTransferStates.value = Event(WatchFaceTransferStates.Default)
            return mFuture!!
        }
        sessionManager.watchFaceTransferStates.value =
            Event(WatchFaceTransferStates.Started(watchFace!!))



        job = syncDataScope.launch {
            downloadWatchFace(
                watchFace?.zipFile ?: "",
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
                                LOGS.d(TAG, "Download Work Failed")
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


        Handler(Looper.getMainLooper()).post {
            sessionManager.updateDeviceCallback.observe(this) { event ->
                event.getContent()?.let {
                    if (it is UpdateDeviceDataCallback.CustomizeWatchFaceProgress) {

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
            val watchFace = WatchFace().apply {
                id = watchFace?.wId
                this.localFilePath = it
                zip_file = watchFace?.zipFile
                watchface_type = watchFace?.watchface_type ?: ""
            }
            sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetWatchFace(watchFace))
        }

    }

    private fun updateWatchFaceStatus(
        watchUpdateStatus: WatchUpdateStatus,
        success: () -> Unit,
        fail: () -> Unit
    ) {
        when (watchUpdateStatus.status) {
            UpdateStatus.STARTED -> {
                sessionManager.watchFaceTransferStates.value =
                    Event(
                        WatchFaceTransferStates.Started(
                            watchFace!!
                        )
                    )
            }

            UpdateStatus.PROGRESS -> {
                sessionManager.watchFaceTransferStates.value = Event(
                    WatchFaceTransferStates.Progress(
                        watchUpdateStatus.percentagePercentage ?: 0
                    )
                )
            }

            UpdateStatus.COMPLETED -> {

                sessionManager.watchFaceTransferStates.value =
                    Event(WatchFaceTransferStates.Success(watchFace!!))
                sessionManager.watchFaceTransferStates.value =
                    Event(WatchFaceTransferStates.Default)
                success()
            }

            UpdateStatus.ERROR -> {

                var message = "Error"

                if(!watchUpdateStatus.message.isNullOrEmpty()){
                    message = watchUpdateStatus.message!!
                }
                sessionManager.diyWatchFaceTransferStates.value =
                    Event(DiyWatchFaceTransferStates.Failed(message))
                sessionManager.watchFaceTransferStates.value =
                    Event(WatchFaceTransferStates.Default)
                fail()
            }

            UpdateStatus.BATTERY_LOW -> {
                sessionManager.watchFaceTransferStates.value =
                    Event(WatchFaceTransferStates.Failed("Battery low"))
                sessionManager.watchFaceTransferStates.value =
                    Event(WatchFaceTransferStates.Default)
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
            sessionManager.watchFaceTransferStates.value =
                Event(WatchFaceTransferStates.Downloading(false))
        }


        downloadRepository.downloadFileFromUrl(
            binUrl,
            context.externalCacheDir ?: context.cacheDir,
            fileName!!
        ).collect {
            when (it) {
                is Download.Finished -> {
                    withContext(Dispatchers.Main) {
                        LOGS.i("download_percentage finished")
                        sessionManager.watchFaceTransferStates.value =
                            Event(WatchFaceTransferStates.Downloading(true))
                    }
                    earnRewardsPoints()
                    setWatchFaceDownload()
                    //_setWatchFace.postValue(Event(it.file))
                    success(it.file)
                }

                is Download.Progress -> {
                    LOGS.i("download_percentage ${it.percent}")
                    //_watchFaceDownloadProgress.postValue(Event(it.percent))
                }

                is Download.Failed -> {
                    LOGS.i("download_percentage failed")
                    AppLogs.sendAppLogs(LogEvents.WatchFace, WatchFaceEvents.DownloadFileFailed)
                    failed("Download failed")
                }
            }
        }

    }

   private fun getZipFileNameForRyeex(zipFile: String): String? {
        val fileName = zipFile.split("/").last()
        return if (fileName.isNotEmpty()) {
            fileName
        } else {
            null
        }
    }

    override fun onStopped() {
        super.onStopped()
LOGS.d("dsaksdakldsasaddsa")
        job.cancel()
    }


    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    fun setWatchFaceDownload() {
        GlobalScope.launch {
            val requestObject = JsonObject().apply {
                addProperty("watchface_id", watchFace?.wId)
            }
            watchFaceRepository.setWatchFaceDownload(requestObject).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.data?.let { response ->

                        }
                    }
                    else -> {
                        LOGS.d("setWatchFaceDownload: Something went wrong")
                    }
                }
            }
        }
    }
    fun earnRewardsPoints() {
        if (localDataStore.getIsWatchFaceRewardEarned()) return

        GlobalScope.launch {
            val request = JsonObject().apply {
                this.addProperty("task_enum", "1st_custom_watch-face")
            }
            rewardsRepository.earnRewardsPoints(request).collect { resource ->
                when (resource) {
                    is Resource.Success -> {

                        localDataStore.setIsWatchFaceRewardEarned(true)
                    }

                    else -> {}
                }
            }
        }
    }
}

sealed class WatchFaceTransferStates {
    object Default : WatchFaceTransferStates()
    data class Started(val watchFace: Watchface2) : WatchFaceTransferStates()
    data class Downloading(val isCompleted: Boolean) : WatchFaceTransferStates()

    object TransferStarted : WatchFaceTransferStates()
    data class Success(val watchFace: Watchface2) : WatchFaceTransferStates()
    data class Progress(val percent: Int) : WatchFaceTransferStates()
    data class Failed(val reason: String) : WatchFaceTransferStates()
}