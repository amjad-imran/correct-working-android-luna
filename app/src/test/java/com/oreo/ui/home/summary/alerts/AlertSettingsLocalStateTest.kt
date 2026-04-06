package com.oreo.ui.home.summary.alerts

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.noisefit_commans.models.AlertEvent
import com.noisefit_commans.models.AlertEventSource
import com.noisefit_commans.models.AlertSettingsStateUtils
import com.noisefit_commans.models.DeviceAlertFeature
import com.noisefit_commans.models.HeartRateAlertSettings
import com.noisefit_commans.models.HeartRateAlertSnapshot
import com.noisefit_commans.models.HighStressAlertSettings
import com.noisefit_commans.models.LocalDeviceAlertPendingSync
import com.noisefit_commans.models.LocalDeviceAlertSettings
import com.noisefit_commans.models.LocalDeviceAlertSnapshots
import com.noisefit_commans.models.LocalDeviceAlertSupport
import com.noisefit_commans.models.PressureModeSettings
import com.noisefit_commans.models.PressureModeSnapshot
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.ScreenlessHeartRateRealtimeSnapshot
import com.noisefit_commans.models.ScreenlessSpo2MonitoringSnapshot
import com.noisefit_commans.models.SedentaryData
import com.noisefit_commans.models.SedentaryReminderSnapshot
import com.noisefit_commans.models.ScreenlessDeviceSupport
import com.noisefit_commans.models.SleepReminder
import com.noisefit_commans.models.Spo2AlertSettings
import com.noisefit_commans.models.WearDetectionStatus
import com.oreo.ui.home.summary.AlertSettingsValidation
import org.junit.Test

class AlertSettingsLocalStateTest {

    private val gson = Gson()

    @Test
    fun `local alert settings gson round-trip preserves pending support and snapshots`() {
        val settings = LocalDeviceAlertSettings(
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            heartRate = HeartRateAlertSettings(
                restingEnabled = true,
                restingThreshold = 148,
                workoutEnabled = true,
                workoutThreshold = 174,
                lowEnabled = true,
                lowThreshold = 52
            ),
            spo2 = Spo2AlertSettings(enabled = true, threshold = 78),
            pressureMode = PressureModeSettings(
                stressMonitoringEnabled = true,
                relaxationPromptEnabled = true
            ),
            highStress = HighStressAlertSettings(enabled = true, threshold = 82),
            sleepReminder = SleepReminder(
                status = true,
                hour = 23,
                minute = 5,
                second = 0,
                millisecond = 0
            ),
            sedentaryReminder = SedentaryData(
                status = true,
                interval = 120,
                startHour = 9,
                startMinute = 0,
                endHour = 18,
                endMinute = 30
            ),
            pendingSync = LocalDeviceAlertPendingSync(
                heartRate = true,
                spo2 = false,
                highStressIndex = true,
                relaxationPrompt = true,
                sleepReminder = false,
                sedentaryReminder = true
            ),
            support = LocalDeviceAlertSupport(
                heartRate = true,
                heartRateWorkout = false,
                spo2 = false,
                relaxationPrompt = true,
                sleepReminder = true,
                sedentaryReminder = true,
                highStressIndex = false,
                wearDetection = false
            ),
            wearDetectionStatus = WearDetectionStatus(
                isWorn = true,
                lastUpdatedAt = 12345L,
                source = "sensor_sample",
                observedValue = 77
            ),
            recentAlerts = arrayListOf(
                AlertEvent(
                    id = "evt_1",
                    timestamp = 9999L,
                    feature = DeviceAlertFeature.SPO2,
                    title = "Low SpO2",
                    message = "74% crossed the 75% limit.",
                    observedValue = 74,
                    threshold = 75,
                    source = AlertEventSource.MIRROR_PUSH,
                    bandSendState = "dispatched"
                )
            ),
            snapshots = LocalDeviceAlertSnapshots(
                heartRate = HeartRateAlertSnapshot(
                    mode = 4,
                    frequency = 15,
                    continuousHeartRateMode = 2
                ),
                screenlessHeartRateRealtime = ScreenlessHeartRateRealtimeSnapshot(
                    status = true,
                    frequency = 3,
                    overtime = 30
                ),
                pressureMode = PressureModeSnapshot(
                    relaxationWarningValue = 3
                ),
                screenlessSpo2Monitoring = ScreenlessSpo2MonitoringSnapshot(
                    mode = 0,
                    frequency = 60,
                    startHour = 0,
                    startMinute = 0,
                    endHour = 23,
                    endMinute = 59
                ),
                sedentaryReminder = SedentaryReminderSnapshot(
                    noDisturbInLaunch = false,
                    startNoonHour = 12,
                    startNoonMinute = 15,
                    endNoonHour = 13,
                    endNoonMinute = 15
                )
            )
        )

        val restored = gson.fromJson(gson.toJson(settings), LocalDeviceAlertSettings::class.java)

        assertThat(restored).isEqualTo(settings)
        assertThat(restored.isPending(DeviceAlertFeature.HEART_RATE)).isTrue()
        assertThat(restored.isPending(DeviceAlertFeature.SEDENTARY_REMINDER)).isTrue()
        assertThat(restored.isPending(DeviceAlertFeature.HIGH_STRESS_INDEX)).isTrue()
        assertThat(restored.isSupported(DeviceAlertFeature.SPO2)).isFalse()
        assertThat(restored.requiresSnapshot(DeviceAlertFeature.HEART_RATE)).isFalse()
        assertThat(restored.requiresSnapshot(DeviceAlertFeature.RELAXATION_PROMPT)).isFalse()
        assertThat(restored.requiresSnapshot(DeviceAlertFeature.SEDENTARY_REMINDER)).isFalse()
        assertThat(restored.recentAlerts).hasSize(1)
        assertThat(restored.wearDetectionStatus.source).isEqualTo("sensor_sample")
    }

    @Test
    fun `screenless device helper covers Luna ring and Luna band only`() {
        assertThat(ScreenlessDeviceSupport.isScreenlessDeviceType("luna_ring")).isTrue()
        assertThat(ScreenlessDeviceSupport.isScreenlessDeviceType("LUNA_BAND")).isTrue()
        assertThat(ScreenlessDeviceSupport.isScreenlessDeviceType("watch")).isFalse()

        assertThat(ScreenlessDeviceSupport.isRingWearDetectionDeviceType("luna_ring")).isTrue()
        assertThat(ScreenlessDeviceSupport.isRingWearDetectionDeviceType("luna_band")).isTrue()
    }

    @Test
    fun `alert settings validation enforces documented numeric ranges`() {
        assertThat(AlertSettingsValidation.isHeartRateThresholdValid(1)).isTrue()
        assertThat(AlertSettingsValidation.isHeartRateThresholdValid(250)).isTrue()
        assertThat(AlertSettingsValidation.isHeartRateThresholdValid(0)).isFalse()
        assertThat(AlertSettingsValidation.isHeartRateThresholdValid(251)).isFalse()

        assertThat(AlertSettingsValidation.isSpo2ThresholdValid(1)).isTrue()
        assertThat(AlertSettingsValidation.isSpo2ThresholdValid(100)).isTrue()
        assertThat(AlertSettingsValidation.isSpo2ThresholdValid(0)).isFalse()
        assertThat(AlertSettingsValidation.isSpo2ThresholdValid(101)).isFalse()

        assertThat(AlertSettingsValidation.isStressThresholdValid(1)).isTrue()
        assertThat(AlertSettingsValidation.isStressThresholdValid(100)).isTrue()
        assertThat(AlertSettingsValidation.isStressThresholdValid(0)).isFalse()
        assertThat(AlertSettingsValidation.isStressThresholdValid(101)).isFalse()

        assertThat(AlertSettingsValidation.isSedentaryIntervalValid(60)).isTrue()
        assertThat(AlertSettingsValidation.isSedentaryIntervalValid(120)).isTrue()
        assertThat(AlertSettingsValidation.isSedentaryIntervalValid(30)).isFalse()
        assertThat(AlertSettingsValidation.isSedentaryIntervalValid(0)).isFalse()

        assertThat(
            AlertSettingsValidation.isHeartRateSettingsValid(
                HeartRateAlertSettings(
                    restingEnabled = true,
                    restingThreshold = 150,
                    workoutEnabled = true,
                    workoutThreshold = 175,
                    lowEnabled = false,
                    lowThreshold = 0
                )
            )
        ).isTrue()
        assertThat(
            AlertSettingsValidation.isHeartRateSettingsValid(
                HeartRateAlertSettings(
                    restingEnabled = true,
                    restingThreshold = 150,
                    workoutEnabled = false,
                    workoutThreshold = 0,
                    lowEnabled = true,
                    lowThreshold = 0
                )
            )
        ).isFalse()
    }

    @Test
    fun `device comparison ignores disabled alert thresholds and times`() {
        assertThat(
            AlertSettingsStateUtils.heartRateMatches(
                local = HeartRateAlertSettings(
                    restingEnabled = false,
                    restingThreshold = 150,
                    workoutEnabled = false,
                    workoutThreshold = 175,
                    lowEnabled = false,
                    lowThreshold = 50
                ),
                device = HeartRateAlertSettings(
                    restingEnabled = false,
                    restingThreshold = 220,
                    workoutEnabled = false,
                    workoutThreshold = 0,
                    lowEnabled = false,
                    lowThreshold = 0
                ),
                workoutSupported = false
            )
        ).isTrue()

        assertThat(
            AlertSettingsStateUtils.spo2Matches(
                local = Spo2AlertSettings(enabled = false, threshold = 75),
                device = Spo2AlertSettings(enabled = false, threshold = 100)
            )
        ).isTrue()

        assertThat(
            AlertSettingsStateUtils.sleepReminderMatches(
                local = SleepReminder(status = false, hour = 22, minute = 30),
                device = SleepReminder(status = false, hour = 0, minute = 0)
            )
        ).isTrue()

        assertThat(
            AlertSettingsStateUtils.sedentaryMatches(
                local = SedentaryData(
                    status = false,
                    interval = 60,
                    startHour = 9,
                    startMinute = 0,
                    endHour = 18,
                    endMinute = 0
                ),
                device = SedentaryData(
                    status = false,
                    interval = 0,
                    startHour = 0,
                    startMinute = 0,
                    endHour = 0,
                    endMinute = 0
                )
            )
        ).isTrue()
    }

    @Test
    fun `device merge preserves local display values when alert is disabled`() {
        assertThat(
            AlertSettingsStateUtils.mergeHeartRate(
                local = HeartRateAlertSettings(
                    restingEnabled = false,
                    restingThreshold = 150,
                    workoutEnabled = false,
                    workoutThreshold = 175,
                    lowEnabled = false,
                    lowThreshold = 50
                ),
                device = HeartRateAlertSettings(
                    restingEnabled = false,
                    restingThreshold = 220,
                    workoutEnabled = false,
                    workoutThreshold = 0,
                    lowEnabled = false,
                    lowThreshold = 0
                ),
                workoutSupported = false
            )
        ).isEqualTo(
            HeartRateAlertSettings(
                restingEnabled = false,
                restingThreshold = 150,
                workoutEnabled = false,
                workoutThreshold = 175,
                lowEnabled = false,
                lowThreshold = 50
            )
        )

        assertThat(
            AlertSettingsStateUtils.mergeSleepReminder(
                local = SleepReminder(status = false, hour = 22, minute = 30),
                device = SleepReminder(status = false, hour = 0, minute = 0)
            )
        ).isEqualTo(
            SleepReminder(status = false, hour = 22, minute = 30, second = 0, millisecond = 0)
        )
    }

    @Test
    fun `screenless snapshot requirement uses hidden monitoring snapshots`() {
        val settings = LocalDeviceAlertSettings(
            snapshots = LocalDeviceAlertSnapshots(
                heartRate = HeartRateAlertSnapshot(
                    mode = null,
                    frequency = null,
                    continuousHeartRateMode = null
                ),
                screenlessHeartRateRealtime = ScreenlessHeartRateRealtimeSnapshot(),
                screenlessSpo2Monitoring = ScreenlessSpo2MonitoringSnapshot()
            )
        )

        assertThat(
            AlertSettingsStateUtils.requiresSnapshot(
                settings = settings,
                feature = DeviceAlertFeature.HEART_RATE,
                deviceType = DeviceType.LUNA_BAND.deviceType
            )
        ).isTrue()
        assertThat(
            AlertSettingsStateUtils.requiresSnapshot(
                settings = settings,
                feature = DeviceAlertFeature.SPO2,
                deviceType = DeviceType.LUNA_BAND.deviceType
            )
        ).isFalse()

        settings.snapshots.heartRate = HeartRateAlertSnapshot(
            mode = 0,
            frequency = 0,
            continuousHeartRateMode = 0
        )

        assertThat(
            AlertSettingsStateUtils.requiresSnapshot(
                settings = settings,
                feature = DeviceAlertFeature.HEART_RATE,
                deviceType = DeviceType.LUNA_BAND.deviceType
            )
        ).isFalse()
        assertThat(
            AlertSettingsStateUtils.requiresSnapshot(
                settings = settings,
                feature = DeviceAlertFeature.SPO2,
                deviceType = DeviceType.LUNA_BAND.deviceType
            )
        ).isFalse()
    }

    @Test
    fun `luna band zero sedentary readback is treated as known anomaly`() {
        assertThat(
            AlertSettingsStateUtils.isScreenlessSedentaryReadbackAnomaly(
                deviceType = DeviceType.LUNA_BAND.deviceType,
                local = SedentaryData(
                    status = true,
                    interval = 60,
                    startHour = 9,
                    startMinute = 0,
                    endHour = 18,
                    endMinute = 0
                ),
                device = SedentaryData(
                    status = false,
                    interval = 0,
                    startHour = 0,
                    startMinute = 0,
                    endHour = 0,
                    endMinute = 0
                )
            )
        ).isTrue()
    }
}
