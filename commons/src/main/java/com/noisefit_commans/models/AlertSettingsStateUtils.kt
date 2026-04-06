package com.noisefit_commans.models

object AlertSettingsStateUtils {

    fun requiresSnapshot(
        settings: LocalDeviceAlertSettings,
        feature: DeviceAlertFeature,
        deviceType: String?
    ): Boolean {
        return settings.requiresSnapshot(feature)
    }

    fun usesScreenlessMonitoringSnapshot(
        feature: DeviceAlertFeature,
        deviceType: String?
    ): Boolean {
        return false
    }

    fun heartRateMatches(
        local: HeartRateAlertSettings,
        device: HeartRateAlertSettings,
        workoutSupported: Boolean = true
    ): Boolean {
        return local.restingEnabled == device.restingEnabled &&
            (!local.restingEnabled || local.restingThreshold == device.restingThreshold) &&
            (!workoutSupported || (
                local.workoutEnabled == device.workoutEnabled &&
                    (!local.workoutEnabled || local.workoutThreshold == device.workoutThreshold)
                )) &&
            local.lowEnabled == device.lowEnabled &&
            (!local.lowEnabled || local.lowThreshold == device.lowThreshold)
    }

    fun mergeHeartRate(
        local: HeartRateAlertSettings,
        device: HeartRateAlertSettings,
        workoutSupported: Boolean = true
    ): HeartRateAlertSettings {
        return HeartRateAlertSettings(
            restingEnabled = device.restingEnabled,
            restingThreshold = if (device.restingEnabled) {
                device.restingThreshold
            } else {
                preserveLocalNumber(local.restingThreshold, device.restingThreshold)
            },
            workoutEnabled = if (workoutSupported) {
                device.workoutEnabled
            } else {
                false
            },
            workoutThreshold = if (workoutSupported && device.workoutEnabled) {
                device.workoutThreshold
            } else {
                preserveLocalNumber(local.workoutThreshold, device.workoutThreshold)
            },
            lowEnabled = device.lowEnabled,
            lowThreshold = if (device.lowEnabled) {
                device.lowThreshold
            } else {
                preserveLocalNumber(local.lowThreshold, device.lowThreshold)
            }
        )
    }

    fun spo2Matches(local: Spo2AlertSettings, device: Spo2AlertSettings): Boolean {
        return local.enabled == device.enabled &&
            (!local.enabled || local.threshold == device.threshold)
    }

    fun mergeSpo2(local: Spo2AlertSettings, device: Spo2AlertSettings): Spo2AlertSettings {
        return Spo2AlertSettings(
            enabled = device.enabled,
            threshold = if (device.enabled) {
                device.threshold
            } else {
                preserveLocalNumber(local.threshold, device.threshold)
            }
        )
    }

    fun highStressMatches(local: HighStressAlertSettings, device: HighStressAlertSettings): Boolean {
        return local.enabled == device.enabled &&
            (!local.enabled || local.threshold == device.threshold)
    }

    fun mergeHighStress(
        local: HighStressAlertSettings,
        device: HighStressAlertSettings
    ): HighStressAlertSettings {
        return HighStressAlertSettings(
            enabled = device.enabled,
            threshold = if (device.enabled) {
                device.threshold
            } else {
                preserveLocalNumber(local.threshold, device.threshold)
            }
        )
    }

    fun sleepReminderMatches(local: SleepReminder, device: SleepReminder): Boolean {
        return local.status == device.status &&
            (!local.status || (local.hour == device.hour && local.minute == device.minute))
    }

    fun mergeSleepReminder(local: SleepReminder, device: SleepReminder): SleepReminder {
        return if (device.status) {
            device.copy(second = 0, millisecond = 0)
        } else {
            local.copy(status = false, second = 0, millisecond = 0)
        }
    }

    fun sedentaryMatches(local: SedentaryData, device: SedentaryData): Boolean {
        return local.status == device.status &&
            (!local.status || (
                local.interval == device.interval &&
                    local.startHour == device.startHour &&
                    local.startMinute == device.startMinute &&
                    local.endHour == device.endHour &&
                    local.endMinute == device.endMinute
                ))
    }

    fun mergeSedentary(local: SedentaryData, device: SedentaryData): SedentaryData {
        return if (device.status) {
            device
        } else {
            local.copy(status = false)
        }
    }

    fun isScreenlessSedentaryReadbackAnomaly(
        deviceType: String?,
        local: SedentaryData,
        device: SedentaryData
    ): Boolean {
        return deviceType.equals(DeviceType.LUNA_BAND.deviceType, ignoreCase = true) &&
            local.status &&
            !device.status &&
            device.interval == 0 &&
            device.startHour == 0 &&
            device.startMinute == 0 &&
            device.endHour == 0 &&
            device.endMinute == 0
    }

    private fun preserveLocalNumber(local: Int, device: Int): Int {
        return if (local > 0) local else device
    }
}
