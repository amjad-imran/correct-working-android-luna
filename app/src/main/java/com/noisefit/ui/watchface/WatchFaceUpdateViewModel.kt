package com.noisefit.ui.watchface

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.luna.R
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.WatchFaceDownloadResponse
import com.noisefit.data.repository.LastSyncItems
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.DownloadRepository
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit.data.repository.abstraction.WatchFaceRepository
import com.noisefit.data.repository.implementation.Download
import com.noisefit.session.SessionManager
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.*
import com.noisefit_commans.utils.DateFormats.checkTimeDifferenceMoreThanN
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LogEvents
import com.noisefit_commans.utils.WatchFaceEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class WatchFaceUpdateViewModel @Inject constructor(
    private val watchFaceRepository: WatchFaceRepository,
    private val downloadRepository: DownloadRepository,
    private val resourcesProvider: ResourcesProvider,
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val watchesSDK: WatchesSDK,
    private val rewardsRepository: RewardsRepository,
    private val lastSyncProvider: LastSyncProvider
) : BaseViewModel() {

    var pairingTimeTaken:Long = 0
    var watchFaceId: Int? = null
    var watchFaceName: String? = null
    var watchFaceIdFavourite: Int? = null
    var localFilePath: String? = null


    private val _watchFace = MutableLiveData<WatchFace>()
    private val _watchFaceDownloadInfo = MutableLiveData<Event<WatchFaceDownloadResponse>>()
    private val _similarWatchFace = MutableLiveData<List<WatchFace>>()
    private val _watchFaceDownloadProgress = MutableLiveData<Event<Int>>()
    private val _setWatchFace = MutableLiveData<Event<File>>()
    private val _resetFavouriteState = MutableLiveData<Event<Boolean>>()

    private val _setWatchFaceButtonVisibility = MutableLiveData<Event<Boolean>>()

    val markedFavourite = MutableLiveData<Event<Boolean>>()

    val watchFace: LiveData<WatchFace> = _watchFace
    val watchFaceDownloadInfo: LiveData<Event<WatchFaceDownloadResponse>> = _watchFaceDownloadInfo
    val similarWatchFace: LiveData<List<WatchFace>> = _similarWatchFace
    val watchFaceDownloadProgress: LiveData<Event<Int>> = _watchFaceDownloadProgress
    val setWatchFace: LiveData<Event<File>> = _setWatchFace
    val resetFavouriteState: LiveData<Event<Boolean>> = _resetFavouriteState
    val downloadCancelled = MutableLiveData<Event<Boolean>>()

    val setWatchFaceButtonVisibility: LiveData<Event<Boolean>> = _setWatchFaceButtonVisibility

    var isWatchFaceAwardEarned = false

    init {
        isWatchFaceAwardEarned = localDataStore.getIsWatchFaceRewardEarned()
    }

    fun getWatchFaceData(watchFaceId: Int) {

        viewModelScope.launch {
            watchFaceRepository.getWatchFaceById(watchFaceId).collect { resource ->
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
                                    getWatchFaceData(watchFaceId)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let { response ->
                            _watchFace.postValue(response.face.apply {
                                //this.faceId = "${this.id}-${this.imageType}"
                                this.faceId = this.fileName?.replace(".zip", "")
                            })
                            _similarWatchFace.postValue(response.similarWatchFaces)
                        }
                    }
                }
            }
        }

    }



    fun getWatchFaceDownloadInfo(watchFaceId: Int) {
        _setWatchFaceButtonVisibility.value = Event(false)
        viewModelScope.launch {
            watchFaceRepository.getWatchFaceDownloadInfo(watchFaceId).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        _setWatchFaceButtonVisibility.value = Event(true)
                        sendMessage(resource.message)
                        AppLogs.sendAppLogs(LogEvents.WatchFace, WatchFaceEvents.DownloadFileFailed)
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

                                override fun no() {
                                    _setWatchFaceButtonVisibility.value = Event(true)
                                }
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let { response ->
                            _watchFaceDownloadInfo.postValue(Event(response))
                        }
                    }
                }
            }
        }
    }

    /*let obj = {
        face_id: req.body.face_id,->fid
        face_type: req.body.face_type,
        image_type: req.body.image_type,
        image_url: req.body.image_url,
        is_custom: req.body.is_custom,
        is_download_image: req.body.is_download_image,
        is_editable: req.body.is_editable,
        device_type: req.body.device_type,
    }*/


    fun downloadWatchFace(url: String, file: File, fileName: String) {
        viewModelScope.launch {
            sessionManager.logInsiderAppEvent(InsiderAppEvents.WatchFaceEvents.wn_face_download_start)
            downloadRepository.downloadFileFromUrl(url, file, fileName).collect {
                when (it) {
                    is Download.Finished -> {
                        sessionManager.logInsiderAppEvent(InsiderAppEvents.WatchFaceEvents.wn_face_download_complete)
                        _setWatchFace.postValue(Event(it.file))
                    }
                    is Download.Progress -> {
                        LOGS.i("${it.percent}")
                        _watchFaceDownloadProgress.postValue(Event(it.percent))
                    }
                    is Download.Failed -> {
                        sessionManager.logInsiderAppEvent(InsiderAppEvents.WatchFaceEvents.wn_face_download_failed)
                        AppLogs.sendAppLogs(LogEvents.WatchFace, WatchFaceEvents.DownloadFileFailed)
                        downloadCancelled.postValue(Event(true))
                        setApiErrors(
                            ErrorResponse(
                                UIComponentType.RetryApiDialog("Download Failed, Retry?").apply {
                                    callback = object : BinaryActionCallback {
                                        override fun yes() {
                                            downloadWatchFace(url, file, fileName)
                                        }

                                        override fun no() {
                                        }
                                    }
                                })
                        )
                    }
                }
            }
        }
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

    fun markFavourite(isFavourite: Boolean, watchFace: WatchFace) {
        viewModelScope.launch(Dispatchers.IO) {
            val requestObject = JsonObject().apply {
                addProperty("watchface_id", watchFace.id)
                addProperty("is_favourite", isFavourite)
            }
            watchFaceRepository.markAsFavourite(requestObject).collect { resource ->
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
                                    markFavourite(isFavourite, watchFace)
                                }

                                override fun no() {
                                    _resetFavouriteState.postValue(Event(!isFavourite))
                                }
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let { response ->
                            watchFaceIdFavourite = watchFace.id
                            markedFavourite.postValue(Event(isFavourite))
                            if (isFavourite) {
                                sendMessage(resourcesProvider.getString(R.string.text_watchface_added))
                                watchFaceRepository.addToFavourites(watchFace)
                            } else {
                                sendMessage(resourcesProvider.getString(R.string.text_watchface_removed))
                                watchFaceRepository.removeFromFavourites(watchFace.id ?: -1)
                            }

                        }
                    }
                }
            }
        }
    }

    fun setWatchFaceDownload() {
        viewModelScope.launch {
            val requestObject = JsonObject().apply {
                addProperty("watchface_id", watchFaceId)
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

    fun getZipFileNameForRyeex(zipFile: String): String? {
        val fileName = zipFile.split("/").last()
        return if (fileName.isNotEmpty()) {
            fileName
        } else {
            null
        }
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
}