package com.oreo.ui.device

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.R
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit.ui.myDevice.MyDeviceAction
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
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OMyDeviceViewModel @Inject constructor(
    var connectionHandler: ConnectionHandler,
    val userRepository: UserRepository,
    val sessionManager: SessionManager,
    val ringDataStore: RingDataStore,
    val localDataStore: DataStoredInterface,
    val watchDataStore: WatchDataStore,
    val deviceRepository: DeviceRepository,
    val watchesSDK: WatchesSDK
) : BaseViewModel() {
    private var _deviceConnected: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var deviceConnected = _deviceConnected

    var nextAction: MyDeviceAction? = null
    var startWatchFlow: MutableLiveData<Event<Boolean>> = MutableLiveData<Event<Boolean>>()


    init {
        _deviceConnected.value = (ringDataStore.getRingDevice() != null)
    }

    fun updateDeviceConnectedStatus() {
        _deviceConnected.value = (ringDataStore.getRingDevice() != null)
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
            addProperty("wearable_type", "watch")
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
                                localDataStore.saveDeviceFeatures(features)
                            }
                            localDataStore.updateUserToken(it.tokens)

                            startWatchFlow.postValue(Event(true))
                        }
                    }
                }
            }
        }
    }


}