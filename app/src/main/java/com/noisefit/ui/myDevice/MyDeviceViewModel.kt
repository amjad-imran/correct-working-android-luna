package com.noisefit.ui.myDevice

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.R
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit.watch.CallingWatchUtils
import com.noisefit.watch.ConnectionHandler
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.enums.Device
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Timer
import java.util.TimerTask
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MyDeviceViewModel
@Inject
constructor(
    var connectionHandler: ConnectionHandler,
    val userRepository: UserRepository,
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface,
    val ringDataStore: RingDataStore,
    val watchDataStore: WatchDataStore,
    val deviceRepository: DeviceRepository,
    val watchesSDK: WatchesSDK,
    private val callingWatchUtils: CallingWatchUtils
) : BaseViewModel() {
    var watchFaceTimer: Timer? = null
    val showNextWatchFace = MutableLiveData<Event<Boolean>>()

    var tempColorFitDevice: ColorFitDevice? = null

    var nextAction: MyDeviceAction? = null
    private var _deviceConnected: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var deviceConnected = _deviceConnected


    var startOreoFlow: MutableLiveData<Event<Boolean>> = MutableLiveData<Event<Boolean>>()


    init {
        _deviceConnected.value = (localDataStore.getConnectedDevice() != null)
    }

    fun updateDeviceConnectedStatus() {
        _deviceConnected.value = (localDataStore.getConnectedDevice() != null)
    }

    fun removeWatchTokenFromServer() {
        val macAddress = tempColorFitDevice?.address


        if (macAddress.isNullOrEmpty()) {
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
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        removeWatchTokenFromServer()
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


    fun getBleCallingStatusMessage(): String? {
        val deviceFeatures = localDataStore.getDeviceFeatures() ?: return null
        val callingWatchName = callingWatchUtils.getCallingWatchBleName(deviceFeatures)
        if (callingWatchName.isNullOrEmpty()) {
            return null
        }
        return NoiseFitApplicationMain.context!!.getString(
            R.string.text_disconnect_calling_watch,
            callingWatchName
        )
    }

    fun isZhWatch(): Boolean {
        val watchType = watchesSDK.getWatchType(localDataStore.getConnectedDevice())
        if (watchType == SDKWatchType.SDK_ZH || watchType == SDKWatchType.SDK_NAV_PLUS) {
            return true
        }
        return false
    }

    fun hasWristSenseWithTiming(): Boolean {
        localDataStore.getConnectedDevice()?.let { device ->
            return device.deviceType == DeviceType.NOISE_EVOLVE_2.deviceType ||
                    device.deviceType == DeviceType.NOISEFIT_EVOLVE_SPORT.deviceType ||
                    device.deviceType == DeviceType.NOISE_EVOLVE_2_PLAY.deviceType
        }
        return false
    }

    fun shouldShowAgpsDialog(): Boolean {
        var returnValue = false
        if (watchesSDK.watchHasAGPS()) {
            val agpsState = localDataStore.getAGPSStatusState()
            returnValue =
                agpsState?.state?.status.equals(com.noisefit_commans.data.model.AGPSStatusEnum.EXPIRED.status)
        }
        return returnValue
    }


    fun startWatchFaceScroll() {

        if (watchFaceTimer == null) {
            watchFaceTimer = Timer()
            watchFaceTimer?.scheduleAtFixedRate(RemindTask(), 0, 3000)
        }
    }

    fun stopWatchFaceScroll() {
        watchFaceTimer?.cancel()
    }

    inner class RemindTask : TimerTask() {
        override fun run() {
            showNextWatchFace.postValue(Event(true))
        }
    }

    fun setTempColorFitDevice() {
        tempColorFitDevice = localDataStore.getConnectedDevice()
    }

    fun updateUserDevice(device: ColorFitDevice, forceRefresh: Boolean) {

        val deviceToken = localDataStore.getUserToken()
        if (deviceToken != null && !forceRefresh) {
            return
        }

        val request = JsonObject().apply {
            addProperty("address", device.address)
            addProperty("device_id", device.deviceId)
            addProperty("rssi", device.rssi)
            addProperty("watch_token", device.watchToken)
            addProperty("platform", "android")
            addProperty("wearable_type", "ring")
        }
        viewModelScope.launch {
            userRepository.saveUserDevice(request).collect { resource ->
                when (resource) {

                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        updateUserDevice(device, forceRefresh)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }


                    is Resource.Success -> {
                        resource.data?.data?.let {
                            it.userDevice.deviceFeatures?.let { features ->
                                ringDataStore.saveDeviceFeatures(features)
                            }
                            localDataStore.updateUserToken(it.tokens)

                            startOreoFlow.postValue(Event(true))
                        }
                    }
                }
            }
        }
    }


}

enum class MyDeviceAction {
    ADD_DEVICE, SWITCH_TO_RING, SWITCH_TO_WATCH
}