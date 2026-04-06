package com.noisefit_commans.models

object AlertSettingsDeviceDefaults {

    fun apply(settings: LocalDeviceAlertSettings, deviceType: String?): LocalDeviceAlertSettings {
        if (ScreenlessDeviceSupport.isScreenlessDeviceType(deviceType)) {
            settings.support.heartRateWorkout = false
            settings.heartRate.workoutEnabled = false
            settings.support.highStressIndex = true
            settings.support.wearDetection = true
        }
        if (!ScreenlessDeviceSupport.isScreenlessDeviceType(deviceType)) {
            settings.support.highStressIndex = false
            settings.support.wearDetection = false
            settings.wearDetectionStatus = settings.wearDetectionStatus.copy(
                isWorn = null,
                lastUpdatedAt = -1L,
                source = null,
                observedValue = null
            )
        }
        return settings
    }
}
