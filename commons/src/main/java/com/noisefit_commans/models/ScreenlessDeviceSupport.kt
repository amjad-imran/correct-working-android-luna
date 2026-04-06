package com.noisefit_commans.models

object ScreenlessDeviceSupport {

    fun isScreenlessDeviceType(deviceType: String?): Boolean {
        return deviceType.equals(DeviceType.NOISEFIT_LUNA.deviceType, ignoreCase = true) ||
            deviceType.equals(DeviceType.LUNA_BAND.deviceType, ignoreCase = true)
    }

    fun isRingWearDetectionDeviceType(deviceType: String?): Boolean {
        return isScreenlessDeviceType(deviceType)
    }
}
