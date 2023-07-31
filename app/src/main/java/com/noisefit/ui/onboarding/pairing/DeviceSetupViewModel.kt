package com.noisefit.ui.onboarding.pairing

import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.luna.R
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.watch.WatchForm
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.enums.Device
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.models.ColorFitDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceSetupViewModel @Inject
constructor(
    val localDataStore: DataStoredInterface,
    val ringDataStore: RingDataStore,
    private val userRepository: UserRepository,
    private val watchesSDK: WatchesSDK,
) : BaseViewModel() {

    var watchForm = WatchForm.SQUARE
    var currentAnimation: Int? = null

    /**
     * Updates device against user in case of logout-login
     */
    fun updateUserDevice(device: ColorFitDevice, forceRefresh: Boolean) {
        val deviceType = localDataStore.getPairDeviceType()

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
            addProperty("wearable_type", if (deviceType == Device.RING) "ring" else "watch")
        }
        viewModelScope.launch {
            userRepository.saveUserDevice(request).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            it.userDevice.deviceFeatures?.let { features ->
                                if (deviceType == Device.RING) {
                                    ringDataStore.saveDeviceFeatures(features)
                                } else {
                                    localDataStore.saveDeviceFeatures(features)
                                }
                            }

                            localDataStore.updateUserToken(it.tokens)

                            //TODO save token here
                            /*if (deviceType == Device.RING) {
                                ringDataStore.updateDeviceToken(it.userDevice.externalId)
                            } else {
                                localDataStore.updateDeviceToken(it.userDevice.externalId)
                            }*/
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    fun getDeviceType() {
        watchForm = watchesSDK.getWatchForm()
    }

    /**
     * @param watchForm Watch form factor
     * @param animationType 0 ->waiting, 1->Setting up. 2-> Setup Success
     */
    fun getAnimation(animationType: Int): Int {
        return when (watchForm) {
            WatchForm.SQUARE, WatchForm.ARC -> {
                when (animationType) {
                    0 -> R.raw.anim_setup_waiting_rect
                    1 -> R.raw.anim_setup_setting_up_rect
                    2 -> R.raw.anim_setup_success_rect
                    else -> {
                        return 0
                    }
                }
            }

            WatchForm.CIRCLE -> {
                when (animationType) {
                    0 -> R.raw.anim_setup_waiting_circle
                    1 -> R.raw.anim_setup_setting_up_circle
                    2 -> R.raw.anim_setup_success_circle
                    else -> {
                        return 0
                    }
                }
            }
        }
    }

}