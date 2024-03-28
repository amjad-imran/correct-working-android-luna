package com.noisefit_commans.models


enum class DeviceType(val deviceName: String, val deviceType: String) {

    NOISEFIT_LUNA("Luna Ring", "luna_ring");//24 july

    companion object {
        fun findDeviceType(type: String): DeviceType? =
            DeviceType.values().find { it.deviceType == type }
    }
}