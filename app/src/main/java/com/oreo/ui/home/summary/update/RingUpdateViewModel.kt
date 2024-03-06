package com.oreo.ui.home.summary.update

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.model.OtaUpdateModel
import com.noisefit.data.repository.abstraction.DownloadRepository
import com.noisefit.data.repository.abstraction.UpdateRepository
import com.noisefit.data.repository.implementation.Download
import com.noisefit.session.SessionManager
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RingUpdateViewModel @Inject constructor(
    var sessionManager: SessionManager,
    val ringDataStore: RingDataStore,
    private val updateRepository: UpdateRepository,
    private val downloadRepository: DownloadRepository,
    var watchesSDK: WatchesSDK
) : BaseViewModel() {

    var otaData: OtaUpdateModel? = null

    var localFilePath: String? = null

    private val _updateFirmware = MutableLiveData<Event<File>>()
    val updateFirmware: LiveData<Event<File>> = _updateFirmware

    private val _firmwareDownloadProgress = MutableLiveData<Event<Int>>()
    val firmwareDownloadProgress: LiveData<Event<Int>> = _firmwareDownloadProgress


    fun checkIfConnected(): Boolean {
        return (sessionManager.connectStateRing.value is ConnectState.ConnectSuccess
                || sessionManager.connectStateRing.value is ConnectState.DfuMode)
    }

    fun getConnectedDevice(): ColorFitDevice? {
        return ringDataStore.getRingDevice()
    }

    fun getMinBatteryPercent(): Int {
        return 20
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


    fun deleteTempFile() {
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

    fun clearNewOtaUpdateData() {
        ringDataStore.cleaNewOtaVersion()
    }


}