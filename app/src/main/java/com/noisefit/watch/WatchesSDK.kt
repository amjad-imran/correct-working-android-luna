package com.noisefit.watch


import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.DeviceFeatures
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import javax.inject.Inject

class WatchesSDK
@Inject
constructor(
    private var localDataStore: DataStoredInterface,
    private val ringDataStore: RingDataStore
) {

    fun getWatchType(connectedDevice: ColorFitDevice?): SDKWatchType {
        var cDevice = connectedDevice

        if (cDevice == null) {
            cDevice = ringDataStore.getRingDevice()
        }

        when (cDevice?.deviceType) {

            DeviceType.NOISEFIT_LUNA.deviceType,DeviceType.LUNA_BAND.deviceType,-> {
                return SDKWatchType.SDK_ZH
            }



            else -> {
                throw IllegalArgumentException("Unsupported connected device found ${connectedDevice?.getDeviceLogInfo()}")
            }
        }
    }


    fun getWatchType(): SDKWatchType? {
        val connectedDevice = ringDataStore.getRingDevice() ?: return null

        return getWatchType(connectedDevice)
    }

    fun getDevice(): ColorFitDevice? {
        return ringDataStore.getRingDevice()
    }

    fun isCaloriesSupported(): Boolean {
        val watchesSDK = getWatchType()

        return true
    }

    /**
     * Returns minimum allowed battery level
     * for OTA/ Watchface/AGPS
     */
    fun getMinimumBatteryLevel(): Int {

        val watchType = getWatchType()
        return if (
            watchType == SDKWatchType.SDK_ZH
        ) {
            20
        } else {
            30
        }
    }

    /**
     * Return 0-> square
     * 1-> Circle
     */
    fun getWatchForm(): WatchForm {
        val deviceFeatures = localDataStore.getDeviceFeatures() ?: return WatchForm.SQUARE
        return getWatchForm(deviceFeatures)
    }

    private fun getWatchForm(deviceFeatures: DeviceFeatures?): WatchForm {
        val screenType = deviceFeatures?.screenType
        return when (screenType?.lowercase()) {
            "circular" -> {
                WatchForm.CIRCLE
            }

            "arc" -> {
                WatchForm.ARC
            }

            else -> {
                WatchForm.SQUARE
            }
        }
    }


}

enum class SDKWatchType {
    SDK_ZH
}

enum class WatchForm(val type: String) {
    SQUARE("square"), CIRCLE("circular"), ARC("arc")
}