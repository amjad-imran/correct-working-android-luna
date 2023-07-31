package com.noisefit.watch

import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.DeviceFeatures
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import javax.inject.Inject

class CallingWatchUtils
@Inject
constructor(
    private var localDataStore: DataStoredInterface
) {



    fun getBleCallStatus(): Triple<Boolean, String, Boolean> {
        val deviceFeatures = localDataStore.getDeviceFeatures() ?: return Triple(false, "", false)
        val bleName = getCallingWatchBleName(deviceFeatures)
        if (bleName.isNullOrEmpty()) {
            return Triple(false, "", false)
        }
        val isDialogAlreadyShown = localDataStore.isBluetoothDialogShown()
        if (isDialogAlreadyShown) {
            return Triple(true, "", true)
        }
        return Triple(true, bleName, false)
    }

    fun getCallingWatchBleName(deviceFeatures: DeviceFeatures?): String? {
        var features = deviceFeatures
        if (features == null) {
            features = localDataStore.getDeviceFeatures()
        }
        return features?.bleName
    }


}