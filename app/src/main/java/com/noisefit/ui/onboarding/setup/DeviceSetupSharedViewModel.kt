package com.noisefit.ui.onboarding.setup

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.model.OtaUpdateModel
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.DownloadRepository
import com.noisefit.data.repository.abstraction.UpdateRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.data.repository.implementation.Download
import com.noisefit.session.SessionManager
import com.noisefit.ui.onboarding.onboardProfile.DefaultDate
import com.noisefit.ui.onboarding.onboardProfile.DefaultHeightInCm
import com.noisefit.ui.onboarding.onboardProfile.DefaultMonth
import com.noisefit.ui.onboarding.onboardProfile.DefaultWeightInKg
import com.noisefit.ui.onboarding.onboardProfile.DefaultYear
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.User
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.Gender
import com.noisefit_commans.models.UnitSystem
import com.noisefit_commans.models.UserGoals
import com.noisefit_commans.models.UserInfo
import com.noisefit_commans.models.WeightUnitSystem
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.toMakeTwoDecimal
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class DeviceSetupSharedViewModel @Inject constructor(
    val ringDataStore: RingDataStore,
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val downloadRepository: DownloadRepository,
    val updateRepository: UpdateRepository
) : BaseViewModel() {

    var updateOtaData: OtaUpdateModel? = null
    var connectionChecked = false

    var localFilePath: String? = null
    private val _updateFirmware = MutableLiveData<Event<File>>()
    val updateFirmware: LiveData<Event<File>> = _updateFirmware
    private val _firmwareDownloadProgress = MutableLiveData<Event<Int>>()
    val firmwareDownloadProgress: LiveData<Event<Int>> = _firmwareDownloadProgress

    val navigateToDeviceUpToDate = MutableLiveData<Event<Boolean>>()
    val navigateToUpdateAvailable = MutableLiveData<Event<Boolean>>()

    val navigateToDeviceSetupSuccess = MutableLiveData<Event<Boolean>>()

    val updateProgress1 = MutableLiveData<Int>(0)
    val updateProgress2 = MutableLiveData<Int>(0)
    val updateProgress3 = MutableLiveData<Int>(0)


    fun checkOtaVersionServer(pair: Pair<Int, Int>) {
        viewModelScope.launch(Dispatchers.IO) {

            val request = getOtaVersionRequest(pair)
            updateRepository.checkAppVersionV2(request).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        //setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        checkOtaVersionServer(pair)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }


                    is Resource.Success -> {
                        resource.data?.data?.let {
                            updateOtaData = it.firmwareVersion
                            if (it.firmwareVersion == null) {
                                navigateToDeviceUpToDate.postValue(Event(true))
                            } else {
                                navigateToUpdateAvailable.postValue(Event(true))
                            }
                            //updateRepository.saveNewOtaVersion(it.firmwareVersion, pair?.first)
                        }
                    }
                }
            }
        }


    }

    private fun getOtaVersionRequest(pair: Pair<Int, Int>): JsonObject {
        val ringDevice = ringDataStore.getRingDevice()
        val deviceType = ringDevice?.deviceType

        return JsonObject().apply {
            addProperty(
                "version",
                /*139*/pair.first
            )
            addProperty(
                "firmware_id",
                pair.second
            )
            addProperty("mac", ringDevice?.address)
            addProperty("device_type", deviceType)
            addProperty(
                "isOTARequired", false
            )
            addProperty("platform", "android")
        }
    }

    fun getConnectedDevice(): ColorFitDevice? {
        return ringDataStore.getRingDevice()
    }

    fun checkIfConnected(): Boolean {
        return (sessionManager.connectStateRing.value is ConnectState.ConnectSuccess
                || sessionManager.connectStateRing.value is ConnectState.DfuMode)
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

    /**
     * Updates device against user in case of logout-login
     */
    fun updateUserDevice(device: ColorFitDevice, forceRefresh: Boolean) {

        val deviceToken = localDataStore.getUserToken()
        if (deviceToken != null && !forceRefresh) {
            return
        }

        val request = JsonObject().apply {
            addProperty("address", device.address)
            addProperty("device_id", device.deviceId)
            addProperty("rssi", device.rssi)
            addProperty("serial_no", device.ringInfo?.serialNoRaw ?: "")
            if (device.watchToken.isNotEmpty()) {
                addProperty("ring_token", device.watchToken)
            }
            addProperty("platform", "android")
            addProperty("wearable_type", "ring")
        }
        viewModelScope.launch {
            userRepository.saveUserDevice(request).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            it.userDevice.deviceFeatures?.let { features ->
                                ringDataStore.saveDeviceFeatures(features)
                            }

                            localDataStore.updateUserToken(it.tokens)

                            ringDataStore.setUpdateUserDeviceStatus(true)
                            navigateToDeviceSetupSuccess.postValue(Event(true))

                        }
                    }

                    else -> {}
                }
            }
        }
    }

    fun getRingImage(): String? {
        return ringDataStore.getRingDevice()?.ringInfo?.image
    }

    fun getRingImage2(): String? {
        return ringDataStore.getRingDevice()?.ringInfo?.image2
    }

}