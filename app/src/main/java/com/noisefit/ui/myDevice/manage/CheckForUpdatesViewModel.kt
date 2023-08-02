package com.noisefit.ui.myDevice.manage

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.UpdateResponse
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit.data.repository.abstraction.DownloadRepository
import com.noisefit.data.repository.implementation.Download
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.VisionOtaFiles
import com.noisefit_commans.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CheckForUpdatesViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val downloadRepository: DownloadRepository,
    val localDataStore: DataStoredInterface,
    val ringDataSore: RingDataStore,
    var watchDataStore: WatchDataStore,
    var sessionManager: SessionManager,
    var watchesSDK: WatchesSDK
) : BaseViewModel() {

    var forceUpdate: Boolean = false
    private val _networkError = MutableLiveData<Event<Boolean>>()
    private val _updateAvailable = MutableLiveData<Event<Boolean>>()
    val _updateInfo = MutableLiveData<Event<UpdateResponse>>()
    private val _updateInfoDash = MutableLiveData<Event<UpdateResponse>>()
    private val _updateFirmware = MutableLiveData<Event<File>>()
    private val _visionUpdateFirmware = MutableLiveData<Event<List<VisionOtaFiles>>>()
    private val _firmwareDownloadProgress = MutableLiveData<Event<Int>>()
    private val _agpsFile1 = MutableLiveData<Event<File>>()
    private val _agpsFile2 = MutableLiveData<Event<File>>()


    val networkError: LiveData<Event<Boolean>> = _networkError
    val updateAvailable: LiveData<Event<Boolean>> = _updateAvailable
    val updateInfo: LiveData<Event<UpdateResponse>> = _updateInfo
    val updateInfoDash: LiveData<Event<UpdateResponse>> = _updateInfoDash
    val updateFirmware: LiveData<Event<File>> = _updateFirmware
    val visionUpdateFirmware: LiveData<Event<List<VisionOtaFiles>>> = _visionUpdateFirmware
    val agpsFile1: LiveData<Event<File>> = _agpsFile1
    val agpsFile2: LiveData<Event<File>> = _agpsFile2
    val firmwareDownloadProgress: LiveData<Event<Int>> = _firmwareDownloadProgress

    var localFilePath: String? = null

    var visionOtaFilePathList = ArrayList<VisionOtaFiles>()

    var agpsFileLocation: File? = null

    fun setUpdateAvailable(status: Boolean) {
        _updateAvailable.postValue(Event(status))
    }

    fun checkForUpdates(postOnDash: Boolean) {
        _networkError.postValue(Event(false))
        val lastUpdateCheckTime = watchDataStore.getLastUpdateCheckTimeStamp()
        val difference = kotlin.math.abs(DateFormats.getTimeStamp() - lastUpdateCheckTime)
        if (difference < 30 * 1000L) {
            setUpdateAvailable(false)
            return
        }

        val deviceType = ringDataSore.getRingDevice()?.deviceType

        val requestObject = JsonObject().apply {
            addProperty(
                "version",
                WatchInfoGlobals.firmwareVersionNumberRing
            )
            addProperty(
                "firmware_id",
                WatchInfoGlobals.firmwareDeviceIdRing
            )
            addProperty("device_type", deviceType)
            addProperty("platform", "android")
            addProperty("isOTARequired", sessionManager.needDfuUpdate.value?.peekContent() ?: false)
        }

        viewModelScope.launch {
            deviceRepository.checkForUpdates(requestObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        _networkError.postValue(Event(true))
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        checkForUpdates(postOnDash)
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        watchDataStore.setInitialOtaChecked(true)

                        resource.data?.data.let { response ->
                            if (response == null) {
                                setUpdateAvailable(false)
                            } else {
                                if (postOnDash) {
                                    setUpdateAvailable(true)
                                    _updateInfoDash.postValue(Event(response))
                                } else {
                                    _updateInfo.postValue(Event(response))
                                    setUpdateAvailable(true)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun downloadFirmware(url: String, file: File, fileName: String) {
        viewModelScope.launch {
            downloadRepository.downloadFileFromUrl(url, file, fileName).collect {
                when (it) {
                    is Download.Finished -> {
                        _updateFirmware.postValue(Event(it.file))
                    }

                    is Download.Progress -> {
                        LOGS.i("${it.percent}")
                        _firmwareDownloadProgress.value = (Event(it.percent))
                    }

                    is Download.Failed -> {
                        setApiErrors(
                            ErrorResponse(
                                UIComponentType.RetryApiDialog("Download Failed, Retry?").apply {
                                    callback = object : BinaryActionCallback {
                                        override fun yes() {
                                            downloadFirmware(url, file, fileName)
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

    fun downloadVisionFirmware(list: List<VisionOtaFiles>) {
        viewModelScope.launch {

            val visionFirmwareList = ArrayList<VisionOtaFiles>()
            list.forEach { multipleFilesDownload ->
                downloadRepository.downloadFileFromUrl(
                    multipleFilesDownload.url,
                    multipleFilesDownload.file,
                    multipleFilesDownload.fileName
                ).collect {
                    when (it) {
                        is Download.Finished -> {
                            visionFirmwareList.add(
                                VisionOtaFiles(
                                    multipleFilesDownload.url,
                                    it.file,
                                    multipleFilesDownload.fileName,
                                    multipleFilesDownload.fileType
                                )
                            )
                            if (visionFirmwareList.size == list.size) {
                                _visionUpdateFirmware.postValue(Event(visionFirmwareList))
                            }
                        }

                        is Download.Progress -> {
                            LOGS.i("${it.percent}")
                            _firmwareDownloadProgress.postValue(Event(it.percent))
                        }

                        is Download.Failed -> {
                            setApiErrors(
                                ErrorResponse(
                                    UIComponentType.RetryApiDialog("Download Failed, Retry?")
                                        .apply {
                                            callback = object : BinaryActionCallback {
                                                override fun yes() {
                                                    downloadFirmware(
                                                        multipleFilesDownload.url,
                                                        multipleFilesDownload.file,
                                                        multipleFilesDownload.fileName
                                                    )
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
    }

    fun deleteTempFile() {
        visionOtaFilePathList.forEach {
            val fileUri = Uri.fromFile(it.file).toString()
            deleteFile(fileUri)
        }
        if (localFilePath == null) return

        deleteFile(localFilePath!!)

    }

    private fun deleteFile(localFilePath: String) {
        try {
            val cacheFile = File(Uri.parse(localFilePath).toString())
            cacheFile.deleteRecursively()
        } catch (exp: Exception) {
            LOGS.d("Delete Failed")
        }
    }

    fun downloadAgpsFile1(url: String, file: File, fileName: String) {
        //setLoading(true)
        viewModelScope.launch {
            downloadRepository.downloadFileFromUrl(url, file, fileName).collect {
                when (it) {
                    is Download.Finished -> {
                        _agpsFile1.postValue(Event(it.file))
                    }

                    is Download.Progress -> {
                        LOGS.i("${it.percent}")
                        //_firmwareDownloadProgress.postValue(Event(it.percent))
                    }

                    is Download.Failed -> {
                        AppLogs.sendAppLogs(LogEvents.Agps, AgpsEvents.DownloadFileFailed)
                        setLoading(false)
                        setApiErrors(
                            ErrorResponse(
                                UIComponentType.RetryApiDialog("Download Failed, Retry?").apply {
                                    callback = object : BinaryActionCallback {
                                        override fun yes() {
                                            downloadAgpsFile1(url, file, fileName)
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

    fun downloadAgpsFile2(url: String, file: File, fileName: String) {
        //setLoading(true)
        viewModelScope.launch {
            downloadRepository.downloadFileFromUrl(url, file, fileName).collect {
                when (it) {
                    is Download.Finished -> {
                        _agpsFile2.postValue(Event(it.file))
                        //_updateFirmware.postValue(Event(it.file))
                    }

                    is Download.Progress -> {
                        LOGS.i("${it.percent}")
                        //_firmwareDownloadProgress.postValue(Event(it.percent))
                    }

                    is Download.Failed -> {
                        AppLogs.sendAppLogs(LogEvents.Agps, AgpsEvents.DownloadFileFailed)
                        setLoading(false)
                        setApiErrors(
                            ErrorResponse(
                                UIComponentType.RetryApiDialog("Download Failed, Retry?").apply {
                                    callback = object : BinaryActionCallback {
                                        override fun yes() {
                                            downloadAgpsFile2(url, file, fileName)
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


    fun saveWatchUpdateLogs(context: Context) {
        val updateData = sessionManager.forceOtaResponse?.descriptionEnglish
        val logText = if (!updateData.isNullOrEmpty()) {
            updateData
        } else {
            context.getString(R.string.text_no_watch_update_logs)
        }

        localDataStore.saveWatchUpdateLogs(logText)
    }

    fun getLastUpdateLog(context: Context?): String {
        if (context == null) return ""
        val updateLog = localDataStore.getWatchUpdateLogs()
        return if (updateLog.isNullOrEmpty()) {
            context.getString(R.string.text_no_watch_update_logs)
        } else {
            updateLog
        }
    }

    fun resetPostOnDash() {
        _updateInfoDash.value = Event(null)
    }
}