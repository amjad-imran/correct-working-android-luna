package com.noisefit.ui.onboarding.onboardProfile

import com.noisefit_commans.data.enums.Device
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConnectDeviceViewModel @Inject constructor(
    val localDataStore: DataStoredInterface,
    val ringDataStore: RingDataStore
) : BaseViewModel() {

    fun isDevicePaired(): Boolean {
        return localDataStore.getConnectedDevice() != null
    }

    fun isRingPaired(): Boolean {
        return ringDataStore.getRingDevice() != null
    }

    fun pairedDevice(): Device? {
        val pairedDevice = localDataStore.getPairDeviceType() ?: return null
        return if (pairedDevice == Device.RING) {
            if (isRingPaired()) Device.RING else null
        } else {
            if (isDevicePaired()) Device.SMARTWATCH else null
        }
    }

    fun setCurrentDevice(device: Device) {
        localDataStore.savePairDeviceType(device)
    }
}