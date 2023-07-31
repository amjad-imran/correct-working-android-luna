package com.noisefit.ui.watchface.mode2

import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.OfflineResult
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.WatchFaceDownloadResponse
import com.noisefit_commans.data.response.WatchFaceZip
import com.noisefit.data.repository.LastSyncItems
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.DownloadRepository
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit.data.repository.abstraction.WatchFaceRepository
import com.noisefit.data.repository.implementation.Download
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit.watch.WatchForm
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.network.CF2NetworkCalls
import com.noisefit_commans.utils.DateFormats.checkTimeDifferenceMoreThanN
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class WatchFaceListViewModel
@Inject constructor(
    private val watchFaceRepository: WatchFaceRepository,
    val localDataStore: DataStoredInterface,
    private val downloadRepository: DownloadRepository,
    private val lastSyncProvider: LastSyncProvider,
    var sessionManager: SessionManager,
    private val rewardsRepository: RewardsRepository,
    var watchesSDK: WatchesSDK
) : BaseViewModel() {

    var applyingCustomWf = false
    var isCircle = false
    var pairingTimeTaken:Long = 0
    init {

        if (watchesSDK.getWatchForm() == WatchForm.CIRCLE) {
            isCircle = true
        }
    }

    var mLastClickTime: Long? = null
    var watchFaceId: Int? = null
    var localFilePath: String? = null
    private val _facesList = MutableLiveData<List<WatchFace>>()

    val facesList: LiveData<List<WatchFace>> = _facesList

    val resetBottomSheet = MutableLiveData<Event<Boolean>>()


    private val _watchFace = MutableLiveData<WatchFace>()
    val watchFace: LiveData<WatchFace> = _watchFace

    private val _watchFaceDownloadInfo = MutableLiveData<Event<WatchFaceDownloadResponse>>()
    val watchFaceDownloadInfo: LiveData<Event<WatchFaceDownloadResponse>> = _watchFaceDownloadInfo

    private val _setWatchFace = MutableLiveData<Event<File>>()
    val setWatchFace: LiveData<Event<File>> = _setWatchFace

    private val _watchFaceDownloadProgress = MutableLiveData<Event<Int>>()
    val watchFaceDownloadProgress: LiveData<Event<Int>> = _watchFaceDownloadProgress

    val getSdkWatchFaces = MutableLiveData<Event<Boolean>>()

    var isWatchFaceAwardEarned = false

    init {
        isWatchFaceAwardEarned = localDataStore.getIsWatchFaceRewardEarned()
    }


    fun earnRewardsPoints() {
        if (isWatchFaceAwardEarned) return

        viewModelScope.launch {
            val request = JsonObject().apply {
                this.addProperty("task_enum", "1st_custom_watch-face")
            }
            rewardsRepository.earnRewardsPoints(request).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        isWatchFaceAwardEarned = true
                        localDataStore.setIsWatchFaceRewardEarned(true)
                    }
                    else -> {}
                }
            }
        }
    }

    fun getCloudWatchFaces() {
        val deviceType = localDataStore.getConnectedDevice()?.deviceType ?: return

        viewModelScope.launch {
            watchFaceRepository.getCloudWatchFaces(deviceType).collect { resource ->
                when (resource) {
                    is OfflineResult.Success -> {
                        _facesList.postValue(resource.value!!)
                    }
                    is OfflineResult.GenericError -> {
                        sendMessage(resource.errorMessage)
                    }
                    is OfflineResult.Loading -> {
                        setLoading(resource.loading)
                    }
                    is OfflineResult.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getCloudWatchFaces()
                                }

                                override fun no() {}
                            }
                        })
                    }
                }

            }


        }
    }

    fun getSdkCloudWatchFaces() {
        viewModelScope.launch {
            watchFaceRepository.getSdkCloudWatchFace().collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        if (!resource.value.isNullOrEmpty()) {
                            _facesList.postValue(resource.value)
                        } else {
                            getSdkWatchFaces.postValue(Event(true))
                        }
                    }
                    is CacheResult.GenericError -> {
                        getSdkWatchFaces.postValue(Event(true))
                    }
                }
            }
        }
    }

    fun getWatchFaceDownloadInfo(watchFaceId: Int) {
        viewModelScope.launch {
            watchFaceRepository.getWatchFaceDownloadInfo(watchFaceId).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getWatchFaceDownloadInfo(watchFaceId)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let { response ->
                            resetBottomSheet.value = Event(true)
                            _watchFaceDownloadInfo.value = (Event(response))
                        }
                    }
                }
            }
        }
    }

    fun getWatchFaceDownloadInfoCF2(watchFace: WatchFace) {
        setLoading(true)
        val thread = Thread {
            try {
                val response = CF2NetworkCalls.getWatchFaceDetails(watchFace)
                setLoading(false)
                LOGS.d("Response : $response")
                Handler(Looper.getMainLooper()).post {
                    LOGS.d("WatchfaceCheck", "${watchFace.fileUrl} ${watchFace.zipName}")
                    if (watchFace.fileUrl != null && watchFace.zipName != null) {
                        val watchFaceZip = WatchFaceZip(watchFace.fileUrl!!, watchFace.zipName!!)
                        _watchFaceDownloadInfo.value = (Event(
                            WatchFaceDownloadResponse(
                                image = watchFace.imageUrl!!,
                                imageName = watchFace.zipName!!,
                                zip = watchFaceZip
                            )
                        ))
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        thread.start()
    }

    fun downloadWatchFace(url: String, file: File, fileName: String) {
        viewModelScope.launch {
            _watchFaceDownloadProgress.value = (Event(0))
            sessionManager.logInsiderAppEvent(InsiderAppEvents.WatchFaceEvents.wn_face_download_start)
            downloadRepository.downloadFileFromUrl(url, file, fileName).collect {
                when (it) {
                    is Download.Finished -> {
                        sessionManager.logInsiderAppEvent(InsiderAppEvents.WatchFaceEvents.wn_face_download_complete)
                        _setWatchFace.value = (Event(it.file))
                    }

                    is Download.Progress -> {
                        LOGS.i("${it.percent}")
                        _watchFaceDownloadProgress.value = (Event(it.percent))
                    }
                    is Download.Failed -> {
                        sessionManager.logInsiderAppEvent(InsiderAppEvents.WatchFaceEvents.wn_face_download_failed)
                        resetBottomSheet.value = (Event(true))
                        setApiErrors(
                            ErrorResponse(
                                UIComponentType.RetryApiDialog("Download Failed, Retry?").apply {
                                    callback = object : BinaryActionCallback {
                                        override fun yes() {
                                            downloadWatchFace(url, file, fileName)
                                        }

                                        override fun no() {}
                                    }
                                })
                        )
                    }
                }
            }
        }
    }

    fun getBackgroundImageHeight(): Int {
        return 240
    }

    fun getBackgroundImageWidth(): Int {
        return 240
    }

    //TODO Not working, test
    fun deleteTempFile() {
        if (localFilePath == null) return

        try {
            val cacheFile = File(Uri.parse(localFilePath).toString())
            cacheFile.deleteRecursively()
        } catch (exp: Exception) {
            LOGS.d("Delete Failed")
        }
    }


    fun setWatchFaceList(watchList: List<WatchFace>) {

        viewModelScope.launch {
            watchFaceRepository.saveCloudWatchFace(watchList).collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {
                        _facesList.postValue(watchList)
                    }
                    is CacheResult.GenericError -> {

                    }
                }

            }
        }


    }

    fun setWatchFace(watchFace: WatchFace) {
        _watchFace.value = watchFace
    }

    fun saveCurrentWatchface() {
        watchFace.value?.let {
            localDataStore.saveLastWatchFace(it)
        }
    }

    fun getLastWatchFace(): WatchFace? {
        return localDataStore.getLastWatchFace()
    }

    fun setRecentWatchFace(watchFace: WatchFace) {
        viewModelScope.launch {
            val isDownloadImage = if (watchFace.imageType.equals("in_built", true)) {
                0
            } else if (watchFace.imageType.equals("supplier", true)) {
                1
            } else if (watchFace.imageType.equals("cloud_supplier", true)) {
                1
            } else {
                0
            }

            val jsonObject = JsonObject().apply {
                this.addProperty("fid", watchFace.id)
                this.addProperty("face_type", watchFace.faceType)
                this.addProperty("image_type", watchFace.imageType)
                this.addProperty("image_url", watchFace.imageUrl)
                this.addProperty("is_custom", watchFace.custom)
                this.addProperty("is_editable", watchFace.editable)
                this.addProperty("is_download_image", isDownloadImage) //0>internal, external ->1
            }
            watchFaceRepository.setRecentWatchFace(jsonObject).collect { resource ->
                LOGS.d("$resource")
            }
        }
    }

    fun shouldSendWatchFailureLogs(sendLogs: (Boolean) -> Unit) {
        if (lastSyncProvider.getSyncTimeStamp(LastSyncItems.WATCHFACE_FEEDBACK)
                .checkTimeDifferenceMoreThanN(24)
        ) {
            lastSyncProvider.setSyncTimeStamp(LastSyncItems.WATCHFACE_FEEDBACK)
            sendLogs(true)
        } else {
            sendLogs(false)
        }
    }


}