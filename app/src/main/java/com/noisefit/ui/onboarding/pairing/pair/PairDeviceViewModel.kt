package com.noisefit.ui.onboarding.pairing.pair

import android.app.ActivityManager
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.LastSyncItems
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.AuthenticationRepository
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit.data.repository.abstraction.DownloadRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.data.repository.implementation.Download
import com.noisefit.session.SessionManager
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.enums.Device
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.response.UpdateResponse
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.BindingEvents
import com.noisefit_commans.utils.DateFormats.checkTimeDifferenceMoreThanN
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LogEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@HiltViewModel
class PairDeviceViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val localDataStore: DataStoredInterface,
    private val authRepository: AuthenticationRepository,
    private val downloadRepository: DownloadRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val lastSyncProvider: LastSyncProvider,
    private val ringDataStore: RingDataStore
) :
    BaseViewModel() {

    var currentDevice: Device = Device.SMARTWATCH

    var pairingTimeTaken: Long = 0
    var pairingSuccessEvent: Boolean = false
    var pairingFailedEvent: Boolean = false
    var pairState = MutableLiveData(PairState.PAIRING)

    private val _continueDeviceSetup = MutableLiveData<Event<Boolean>>()

    private val _deviceSetupSuccess = MutableLiveData<Event<Boolean>>()
    val deviceSetupSuccess: LiveData<Event<Boolean>> = _deviceSetupSuccess

    var colorFitDevice: ColorFitDevice? = null
    var tempColorFitDevice: ColorFitDevice? = null

    val continueDeviceSetup: LiveData<Event<Boolean>> = _continueDeviceSetup


    var mIsDevicePaired = false
    private val _updateInfo = MutableLiveData<Event<UpdateResponse>>()
    val updateInfo: LiveData<Event<UpdateResponse>> = _updateInfo
    private val _updateFirmware = MutableLiveData<Event<File>>()
    val updateFirmware: LiveData<Event<File>> = _updateFirmware
    var localFilePath: String? = null

    var isInDfuMode = false


    private fun saveColorFitDevice(colorFitDevice: ColorFitDevice) {
        val isSaved: Boolean = if (currentDevice == Device.RING) {
            ringDataStore.saveRingDevice(colorFitDevice)
        } else {
            localDataStore.saveConnectedDevice(colorFitDevice)
        }
        localDataStore.savePairDeviceType(currentDevice)
        LOGS.d("CONNECT_STATE", "Device Saved $isSaved")
    }



    fun removeWatchTokenFromServer(macAddress:String?) {

        if(macAddress.isNullOrEmpty()){
            LOGS.d("removeWatchTokenFromServer macAddress null")
            return
        }

        viewModelScope.launch {
            deviceRepository.removeWatchTokenFromServer(macAddress).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    removeWatchTokenFromServer(macAddress)
                                }

                                override fun no() {}
                            }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            tempColorFitDevice = null
                        }
                    }
                }
            }

        }
    }
    fun getDeviceFeatures() {

        if (colorFitDevice == null) {
            return
        }
        if (colorFitDevice!!.deviceType == null) {
            return
        }

        if (localDataStore.getUser() != null) {
            updateUserDevice(colorFitDevice!!)
            return
        }


        sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_register_df_start)

        viewModelScope.launch {
            deviceRepository.getDeviceFeature(colorFitDevice?.deviceId ?: -1).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_register_df_ge)
                        AppLogs.sendAppLogs(LogEvents.Binding, BindingEvents.NetworkIssue)
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_register_df_ne)
                        AppLogs.sendAppLogs(LogEvents.Binding, BindingEvents.NetworkIssue)
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getDeviceFeatures()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_register_df_complete)
                        resource.data?.data?.let {
                            localDataStore.saveDeviceFeatures(it.deviceFeatures)
                            saveColorFitDevice(colorFitDevice!!)
                            _deviceSetupSuccess.postValue(Event(true))

                        }
                    }
                }
            }
        }
    }

    fun updateUserDevice(device: ColorFitDevice) {
        val request = JsonObject().apply {
            addProperty("address", device.address)
            addProperty("device_id", device.deviceId)
            addProperty("rssi", device.rssi)
            if(device.watchToken.isNotEmpty()){
                addProperty("watch_token", device.watchToken)
            }
            addProperty("platform", "android")
            addProperty("wearable_type", if (currentDevice == Device.RING) "ring" else "watch")
        }

        sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_register_ud_start)
        viewModelScope.launch {
            userRepository.saveUserDevice(request).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_register_ud_ge)
                        pairState.postValue(PairState.FAILED)
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_register_ud_ne)
                        pairState.postValue(PairState.FAILED)
                        /*                        setApiErrors(resource.response.apply {
                                                    (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                                        override fun yes() {
                                                            updateUserDevice()
                                                        }

                                                        override fun no() {}
                                                    }
                                                })*/
                    }

                    is Resource.Success -> {
                        sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_register_ud_complete)
                        resource.data?.data?.let {

                            it.userDevice.deviceFeatures?.let { features ->
                                if (currentDevice == Device.RING) {
                                    ringDataStore.saveDeviceFeatures(features)
                                } else {
                                    localDataStore.saveDeviceFeatures(features)
                                }
                            }

                            //TODO save token here
                            //localDataStore.updateDeviceToken(it.userDevice.externalId)
                            localDataStore.updateUserToken(it.tokens)

                            saveColorFitDevice(colorFitDevice!!)

                            _deviceSetupSuccess.postValue(Event(true))


                        }
                    }
                }
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            authRepository.logoutUserLocally().collect {
                if (it) {
                    getDeviceFeatures()
                }
            }
        }
    }


    /*fun isServiceRunning(): Boolean {
        return localDataStore.getServiceState() != ServiceState.STOPPED
    }*/

    fun isMyServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


    fun checkForUpdates(colorFitDevice: ColorFitDevice, version: Int) {
        tempColorFitDevice = colorFitDevice
        val requestObject = JsonObject().apply {
            addProperty("version", 1)
            if (colorFitDevice.deviceType == DeviceType.NOISEFIT_ACTIVE_OTA.deviceType) {
                addProperty("firmware_id", 7252/*WatchInfoGlobals.firmwareDeviceId*/)
                addProperty("device_type", "noisefit_active"/*colorFitDevice.deviceType*/)
            } else if (colorFitDevice.deviceType == DeviceType.NOISE_EVOLVE_2.deviceType) {
                addProperty("version", version)
                addProperty("isOTARequired", true)
                addProperty("firmware_id", WatchInfoGlobals.EVOLVE_2_FIRMWARE_CONST_VERSION)
                addProperty("device_type", DeviceType.NOISE_EVOLVE_2.deviceType)
            } else if (colorFitDevice.deviceType == DeviceType.NOISE_EVOLVE_2_PLAY.deviceType) {
                addProperty("version", version)
                addProperty("isOTARequired", true)
                addProperty("firmware_id", WatchInfoGlobals.EVOLVE_PLAY_FIRMWARE_CONST_VERSION)
                addProperty("device_type", DeviceType.NOISE_EVOLVE_2_PLAY.deviceType)
            } else if (colorFitDevice.deviceType == DeviceType.COLORFIT_PULSE_2.deviceType) {
                addProperty("version", version)
                addProperty("isOTARequired", true)
                addProperty("firmware_id", WatchInfoGlobals.PULSE_2_FIRMWARE_CONST_VERSION)
                addProperty("device_type", DeviceType.COLORFIT_PULSE_2.deviceType)
            } else if (colorFitDevice.deviceType == DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType) {
                addProperty("version", version)
                addProperty("isOTARequired", true)
                addProperty("firmware_id", WatchInfoGlobals.PULSE_2_FIRMWARE_CONST_VERSION)
                addProperty("device_type", DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType)
            } else {
                addProperty("firmware_id", 7375/*WatchInfoGlobals.firmwareDeviceId*/)
                addProperty("device_type", "noisefit_agile"/*colorFitDevice.deviceType*/)
            }
            addProperty("platform", "android")
        }

        viewModelScope.launch {
            deviceRepository.checkForUpdates(requestObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        //sendMessage(resource.message)
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {

                    }
                    is Resource.Success -> {
                        resource.data?.data.let { response ->
                            if (response != null) {
                                _updateInfo.postValue(Event(response))
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
                        //_firmwareDownloadProgress.postValue(Event(it.percent))
                    }
                    is Download.Failed -> {
                        setApiErrors(
                            ErrorResponse(
                                UIComponentType.RetryApiDialog("Download Failed").apply {
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

    fun isProfileSetupComplete(): Boolean {
        val user = localDataStore.getUser() ?: return false
        if (user.userInfo?.dob.isNullOrEmpty() &&
            user.userInfo?.height == 0 &&
            user.userInfo?.weight == 0
        ) {
            return false
        }
        return true
    }


    fun shouldSendPairingFailLogs(sendLogs: (Boolean) -> Unit) {
        if (lastSyncProvider.getSyncTimeStamp(LastSyncItems.PAIRING_FEEDBACK)
                .checkTimeDifferenceMoreThanN(24)
        ) {
            lastSyncProvider.setSyncTimeStamp(LastSyncItems.PAIRING_FEEDBACK)
            sendLogs(true)
        } else {
            sendLogs(false)
        }
    }

}