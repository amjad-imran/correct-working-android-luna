package com.noisefit_zhsdk.handler

import com.noisefit_commans.models.HeartRateAlertSettings
import com.noisefit_commans.models.HeartRateAlertSnapshot
import com.noisefit_commans.models.HighStressAlertSettings
import com.noisefit_commans.models.PressureModeSettings
import com.noisefit_commans.models.PressureModeSnapshot
import com.noisefit_commans.models.ScreenlessHeartRateRealtimeSnapshot
import com.noisefit_commans.models.ScreenlessSpo2MonitoringSnapshot
import com.noisefit_commans.models.SedentaryData
import com.noisefit_commans.models.SedentaryReminderSnapshot
import com.noisefit_commans.models.SleepReminder
import com.noisefit_commans.models.Spo2AlertSettings
import com.zhapp.ble.bean.CommonReminderBean
import com.zhapp.ble.bean.ContinuousBloodOxygenSettingsBean
import com.zhapp.ble.bean.HeartRateMonitorBean
import com.zhapp.ble.bean.PressureModeBean
import com.zhapp.ble.bean.RealTimeHeartRateConfigBean
import com.zhapp.ble.bean.SWBRMonitorBean
import com.zhapp.ble.bean.SWHRMonitorBean
import com.zhapp.ble.bean.SWHRVMonitorBean
import com.zhapp.ble.bean.SWSPO2MonitorBean
import com.zhapp.ble.bean.SettingTimeBean
import com.zhapp.ble.callback.SettingMenuCallBack

object AlertSettingsMapper {

    const val HEART_RATE_MODE_AUTO = 0
    const val HEART_RATE_MODE_OFF = 1
    const val DEFAULT_SCREENLESS_RELAXATION_WARNING_VALUE = 1
    const val DEFAULT_SCREENLESS_REALTIME_HR_FREQUENCY = 3
    const val DEFAULT_SCREENLESS_REALTIME_HR_OVERTIME = 30
    const val DEFAULT_SCREENLESS_SPO2_FREQUENCY = 60
    const val DEFAULT_SCREENLESS_START_HOUR = 0
    const val DEFAULT_SCREENLESS_START_MINUTE = 0
    const val DEFAULT_SCREENLESS_END_HOUR = 23
    const val DEFAULT_SCREENLESS_END_MINUTE = 59

    fun fromHeartRateMonitor(bean: HeartRateMonitorBean): Pair<HeartRateAlertSettings, HeartRateAlertSnapshot> {
        return Pair(
            HeartRateAlertSettings(
                restingEnabled = bean.isWarning,
                restingThreshold = bean.warningValue,
                workoutEnabled = bean.isSportWarning,
                workoutThreshold = bean.sportWarningValue,
                lowEnabled = bean.lowWarningValue > 0,
                lowThreshold = bean.lowWarningValue
            ),
            HeartRateAlertSnapshot(
                mode = bean.mode,
                frequency = bean.frequency,
                continuousHeartRateMode = bean.continuousHeartRateMode
            )
        )
    }

    fun toHeartRateMonitor(
        settings: HeartRateAlertSettings,
        snapshot: HeartRateAlertSnapshot?
    ): HeartRateMonitorBean {
        return HeartRateMonitorBean().apply {
            mode = if (settings.hasAnyEnabledAlert()) {
                HEART_RATE_MODE_AUTO
            } else {
                snapshot?.mode ?: HEART_RATE_MODE_OFF
            }
            continuousHeartRateMode = snapshot?.continuousHeartRateMode ?: 0
            isWarning = settings.restingEnabled
            warningValue = settings.restingThreshold
            isSportWarning = settings.workoutEnabled
            sportWarningValue = settings.workoutThreshold
            lowWarningValue = if (settings.lowEnabled) settings.lowThreshold else 0
        }
    }

    fun fromScreenlessHeartRateMonitor(bean: SWHRMonitorBean?): HeartRateAlertSettings {
        return HeartRateAlertSettings(
            restingEnabled = bean?.isHeightWarning ?: false,
            restingThreshold = bean?.heightWarningValue ?: 150,
            workoutEnabled = false,
            workoutThreshold = 175,
            lowEnabled = bean?.isLowWarning ?: false,
            lowThreshold = bean?.lowWarningValue ?: 50
        )
    }

    fun toScreenlessHeartRateMonitor(settings: HeartRateAlertSettings): SWHRMonitorBean {
        return SWHRMonitorBean().apply {
            isHeightWarning = settings.restingEnabled
            heightWarningValue = settings.restingThreshold
            isLowWarning = settings.lowEnabled
            lowWarningValue = if (settings.lowEnabled) settings.lowThreshold else 0
        }
    }

    fun fromScreenlessHeartRateRealtime(bean: RealTimeHeartRateConfigBean?): ScreenlessHeartRateRealtimeSnapshot {
        return ScreenlessHeartRateRealtimeSnapshot(
            status = bean?.status ?: false,
            frequency = bean?.frequency ?: DEFAULT_SCREENLESS_REALTIME_HR_FREQUENCY,
            overtime = bean?.overtime ?: DEFAULT_SCREENLESS_REALTIME_HR_OVERTIME
        )
    }

    fun toScreenlessHeartRateRealtime(
        settings: HeartRateAlertSettings,
        snapshot: ScreenlessHeartRateRealtimeSnapshot?
    ): RealTimeHeartRateConfigBean {
        return RealTimeHeartRateConfigBean().apply {
            status = if (settings.hasAnyEnabledAlert()) {
                true
            } else {
                snapshot?.status ?: false
            }
            frequency = snapshot?.frequency ?: DEFAULT_SCREENLESS_REALTIME_HR_FREQUENCY
            overtime = snapshot?.overtime ?: DEFAULT_SCREENLESS_REALTIME_HR_OVERTIME
        }
    }

    fun fromHighStressMonitor(bean: SWHRVMonitorBean?): HighStressAlertSettings {
        return HighStressAlertSettings(
            enabled = bean?.isWarning ?: false,
            threshold = bean?.warningValue ?: 85
        )
    }

    fun toHighStressMonitor(settings: HighStressAlertSettings): SWHRVMonitorBean {
        return SWHRVMonitorBean().apply {
            isWarning = settings.enabled
            warningValue = settings.threshold
        }
    }

    fun fromPressureMode(bean: PressureModeBean?): PressureModeSettings {
        return PressureModeSettings(
            stressMonitoringEnabled = bean?.pressureMode ?: false,
            relaxationPromptEnabled = bean?.relaxationReminder ?: false
        )
    }

    fun toPressureMode(settings: PressureModeSettings): PressureModeBean {
        return PressureModeBean().apply {
            pressureMode = if (settings.relaxationPromptEnabled) {
                true
            } else {
                settings.stressMonitoringEnabled
            }
            relaxationReminder = settings.relaxationPromptEnabled
        }
    }

    fun fromScreenlessBreathingRelaxation(bean: SWBRMonitorBean?): Pair<PressureModeSettings, PressureModeSnapshot> {
        val enabled = bean?.isWarning ?: false
        return Pair(
            PressureModeSettings(
                stressMonitoringEnabled = enabled,
                relaxationPromptEnabled = enabled
            ),
            PressureModeSnapshot(
                relaxationWarningValue = bean?.warningValue ?: DEFAULT_SCREENLESS_RELAXATION_WARNING_VALUE
            )
        )
    }

    fun toScreenlessBreathingRelaxation(
        settings: PressureModeSettings,
        snapshot: PressureModeSnapshot?
    ): SWBRMonitorBean {
        return SWBRMonitorBean().apply {
            isWarning = settings.relaxationPromptEnabled
            warningValue = snapshot?.relaxationWarningValue ?: DEFAULT_SCREENLESS_RELAXATION_WARNING_VALUE
        }
    }

    fun fromSpo2Monitor(bean: SWSPO2MonitorBean?): Spo2AlertSettings {
        return Spo2AlertSettings(
            enabled = bean?.isWarning ?: false,
            threshold = bean?.warningValue ?: 75
        )
    }

    fun toSpo2Monitor(settings: Spo2AlertSettings): SWSPO2MonitorBean {
        return SWSPO2MonitorBean().apply {
            isWarning = settings.enabled
            warningValue = settings.threshold
        }
    }

    fun fromScreenlessSpo2Monitoring(bean: ContinuousBloodOxygenSettingsBean?): ScreenlessSpo2MonitoringSnapshot {
        return ScreenlessSpo2MonitoringSnapshot(
            mode = bean?.mode ?: SettingMenuCallBack.ContinuousBloodOxygenMode.OFF.mode,
            frequency = bean?.frequency ?: DEFAULT_SCREENLESS_SPO2_FREQUENCY,
            startHour = bean?.startTime?.hour ?: DEFAULT_SCREENLESS_START_HOUR,
            startMinute = bean?.startTime?.minuter ?: DEFAULT_SCREENLESS_START_MINUTE,
            endHour = bean?.endTime?.hour ?: DEFAULT_SCREENLESS_END_HOUR,
            endMinute = bean?.endTime?.minuter ?: DEFAULT_SCREENLESS_END_MINUTE
        )
    }

    fun toScreenlessSpo2Monitoring(
        settings: Spo2AlertSettings,
        snapshot: ScreenlessSpo2MonitoringSnapshot?
    ): ContinuousBloodOxygenSettingsBean {
        return ContinuousBloodOxygenSettingsBean().apply {
            mode = if (settings.enabled) {
                SettingMenuCallBack.ContinuousBloodOxygenMode.AUTO.mode
            } else {
                snapshot?.mode ?: SettingMenuCallBack.ContinuousBloodOxygenMode.OFF.mode
            }
            frequency = snapshot?.frequency ?: DEFAULT_SCREENLESS_SPO2_FREQUENCY
            startTime = SettingTimeBean(
                snapshot?.startHour ?: DEFAULT_SCREENLESS_START_HOUR,
                snapshot?.startMinute ?: DEFAULT_SCREENLESS_START_MINUTE
            )
            endTime = SettingTimeBean(
                snapshot?.endHour ?: DEFAULT_SCREENLESS_END_HOUR,
                snapshot?.endMinute ?: DEFAULT_SCREENLESS_END_MINUTE
            )
        }
    }

    fun fromSedentaryReminder(bean: CommonReminderBean): Pair<SedentaryData, SedentaryReminderSnapshot> {
        val keepLunchWindow = bean.noDisturbInLaunch
        return Pair(
            SedentaryData(
                status = bean.isOn,
                interval = bean.frequency,
                startHour = bean.startTime.hour,
                startMinute = bean.startTime.minuter,
                endHour = bean.endTime.hour,
                endMinute = bean.endTime.minuter
            ),
            SedentaryReminderSnapshot(
                noDisturbInLaunch = bean.noDisturbInLaunch,
                startNoonHour = if (keepLunchWindow) bean.startNoonTime?.hour else null,
                startNoonMinute = if (keepLunchWindow) bean.startNoonTime?.minuter else null,
                endNoonHour = if (keepLunchWindow) bean.endNoonTime?.hour else null,
                endNoonMinute = if (keepLunchWindow) bean.endNoonTime?.minuter else null
            )
        )
    }

    fun toSedentaryReminder(
        data: SedentaryData,
        snapshot: SedentaryReminderSnapshot?
    ): CommonReminderBean {
        return CommonReminderBean().apply {
            isOn = data.status
            noDisturbInLaunch = snapshot?.noDisturbInLaunch ?: false
            startTime = SettingTimeBean(data.startHour, data.startMinute)
            endTime = SettingTimeBean(data.endHour, data.endMinute)
            frequency = data.interval
            if (noDisturbInLaunch && snapshot?.startNoonHour != null && snapshot.startNoonMinute != null) {
                startNoonTime = SettingTimeBean(snapshot.startNoonHour!!, snapshot.startNoonMinute!!)
            }
            if (noDisturbInLaunch && snapshot?.endNoonHour != null && snapshot.endNoonMinute != null) {
                endNoonTime = SettingTimeBean(snapshot.endNoonHour!!, snapshot.endNoonMinute!!)
            }
        }
    }

    fun normalizedSleepReminder(data: SleepReminder): SleepReminder {
        return data.copy(second = 0, millisecond = 0)
    }
}
