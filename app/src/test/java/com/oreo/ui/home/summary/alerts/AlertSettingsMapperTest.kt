package com.oreo.ui.home.summary.alerts

import com.google.common.truth.Truth.assertThat
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
import com.noisefit_zhsdk.handler.AlertSettingsMapper
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
import org.junit.Test

class AlertSettingsMapperTest {

    @Test
    fun `heart-rate mapper round-trips thresholds and keeps read-only frequency unset`() {
        val bean = HeartRateMonitorBean().apply {
            mode = 7
            frequency = 13
            continuousHeartRateMode = 2
            isWarning = true
            warningValue = 151
            isSportWarning = true
            sportWarningValue = 176
            lowWarningValue = 49
        }

        val (settings, snapshot) = AlertSettingsMapper.fromHeartRateMonitor(bean)

        assertThat(settings).isEqualTo(
            HeartRateAlertSettings(
                restingEnabled = true,
                restingThreshold = 151,
                workoutEnabled = true,
                workoutThreshold = 176,
                lowEnabled = true,
                lowThreshold = 49
            )
        )
        assertThat(snapshot).isEqualTo(
            HeartRateAlertSnapshot(
                mode = 7,
                frequency = 13,
                continuousHeartRateMode = 2
            )
        )

        val updatedBean = AlertSettingsMapper.toHeartRateMonitor(
            settings = settings,
            snapshot = snapshot
        )

        assertThat(updatedBean.mode).isEqualTo(AlertSettingsMapper.HEART_RATE_MODE_AUTO)
        assertThat(updatedBean.frequency).isEqualTo(0)
        assertThat(updatedBean.continuousHeartRateMode).isEqualTo(2)
        assertThat(updatedBean.isWarning).isTrue()
        assertThat(updatedBean.warningValue).isEqualTo(151)
        assertThat(updatedBean.isSportWarning).isTrue()
        assertThat(updatedBean.sportWarningValue).isEqualTo(176)
        assertThat(updatedBean.lowWarningValue).isEqualTo(49)
    }

    @Test
    fun `heart-rate mapper keeps last monitor mode when all alerts are disabled`() {
        val bean = AlertSettingsMapper.toHeartRateMonitor(
            settings = HeartRateAlertSettings(
                restingEnabled = false,
                workoutEnabled = false,
                lowEnabled = false
            ),
            snapshot = HeartRateAlertSnapshot(
                mode = 9,
                frequency = 30,
                continuousHeartRateMode = 1
            )
        )

        assertThat(bean.mode).isEqualTo(9)
        assertThat(bean.frequency).isEqualTo(0)
        assertThat(bean.continuousHeartRateMode).isEqualTo(1)
        assertThat(bean.lowWarningValue).isEqualTo(0)
    }

    @Test
    fun `screenless heart-rate mapper round-trips high and low thresholds`() {
        val bean = SWHRMonitorBean().apply {
            isHeightWarning = true
            heightWarningValue = 145
            isLowWarning = true
            lowWarningValue = 48
        }

        val settings = AlertSettingsMapper.fromScreenlessHeartRateMonitor(bean)

        assertThat(settings).isEqualTo(
            HeartRateAlertSettings(
                restingEnabled = true,
                restingThreshold = 145,
                workoutEnabled = false,
                workoutThreshold = 175,
                lowEnabled = true,
                lowThreshold = 48
            )
        )

        val updatedBean = AlertSettingsMapper.toScreenlessHeartRateMonitor(settings)
        assertThat(updatedBean.isHeightWarning).isTrue()
        assertThat(updatedBean.heightWarningValue).isEqualTo(145)
        assertThat(updatedBean.isLowWarning).isTrue()
        assertThat(updatedBean.lowWarningValue).isEqualTo(48)
    }

    @Test
    fun `screenless heart-rate realtime mapper preserves measurement cadence`() {
        val bean = RealTimeHeartRateConfigBean().apply {
            status = true
            frequency = 5
            overtime = 40
        }

        val snapshot = AlertSettingsMapper.fromScreenlessHeartRateRealtime(bean)

        assertThat(snapshot).isEqualTo(
            ScreenlessHeartRateRealtimeSnapshot(
                status = true,
                frequency = 5,
                overtime = 40
            )
        )

        val updatedBean = AlertSettingsMapper.toScreenlessHeartRateRealtime(
            settings = HeartRateAlertSettings(
                restingEnabled = true,
                restingThreshold = 145,
                lowEnabled = true,
                lowThreshold = 48
            ),
            snapshot = snapshot
        )

        assertThat(updatedBean.status).isTrue()
        assertThat(updatedBean.frequency).isEqualTo(5)
        assertThat(updatedBean.overtime).isEqualTo(40)
    }

    @Test
    fun `screenless heart-rate realtime mapper preserves existing status when alerts are off`() {
        val updatedBean = AlertSettingsMapper.toScreenlessHeartRateRealtime(
            settings = HeartRateAlertSettings(
                restingEnabled = false,
                workoutEnabled = false,
                lowEnabled = false
            ),
            snapshot = ScreenlessHeartRateRealtimeSnapshot(
                status = true,
                frequency = 3,
                overtime = 30
            )
        )

        assertThat(updatedBean.status).isTrue()
        assertThat(updatedBean.frequency).isEqualTo(3)
        assertThat(updatedBean.overtime).isEqualTo(30)
    }

    @Test
    fun `spo2 mapper round-trips warning flag and threshold`() {
        val bean = SWSPO2MonitorBean().apply {
            isWarning = true
            warningValue = 77
        }

        val settings = AlertSettingsMapper.fromSpo2Monitor(bean)
        assertThat(settings).isEqualTo(Spo2AlertSettings(enabled = true, threshold = 77))

        val updatedBean = AlertSettingsMapper.toSpo2Monitor(settings)
        assertThat(updatedBean.isWarning).isTrue()
        assertThat(updatedBean.warningValue).isEqualTo(77)
    }

    @Test
    fun `screenless spo2 monitoring mapper preserves current schedule`() {
        val bean = ContinuousBloodOxygenSettingsBean().apply {
            mode = SettingMenuCallBack.ContinuousBloodOxygenMode.AUTO.mode
            frequency = 90
            startTime = SettingTimeBean(8, 30)
            endTime = SettingTimeBean(22, 15)
        }

        val snapshot = AlertSettingsMapper.fromScreenlessSpo2Monitoring(bean)

        assertThat(snapshot).isEqualTo(
            ScreenlessSpo2MonitoringSnapshot(
                mode = SettingMenuCallBack.ContinuousBloodOxygenMode.AUTO.mode,
                frequency = 90,
                startHour = 8,
                startMinute = 30,
                endHour = 22,
                endMinute = 15
            )
        )

        val updatedBean = AlertSettingsMapper.toScreenlessSpo2Monitoring(
            settings = Spo2AlertSettings(enabled = true, threshold = 80),
            snapshot = snapshot
        )

        assertThat(updatedBean.mode).isEqualTo(SettingMenuCallBack.ContinuousBloodOxygenMode.AUTO.mode)
        assertThat(updatedBean.frequency).isEqualTo(90)
        assertThat(updatedBean.startTime.hour).isEqualTo(8)
        assertThat(updatedBean.startTime.minuter).isEqualTo(30)
        assertThat(updatedBean.endTime.hour).isEqualTo(22)
        assertThat(updatedBean.endTime.minuter).isEqualTo(15)
    }

    @Test
    fun `screenless spo2 monitoring mapper preserves existing mode when alert is off`() {
        val updatedBean = AlertSettingsMapper.toScreenlessSpo2Monitoring(
            settings = Spo2AlertSettings(enabled = false, threshold = 75),
            snapshot = ScreenlessSpo2MonitoringSnapshot(
                mode = SettingMenuCallBack.ContinuousBloodOxygenMode.AUTO.mode,
                frequency = 60,
                startHour = 0,
                startMinute = 0,
                endHour = 23,
                endMinute = 59
            )
        )

        assertThat(updatedBean.mode).isEqualTo(SettingMenuCallBack.ContinuousBloodOxygenMode.AUTO.mode)
    }

    @Test
    fun `pressure-mode mapper enables monitoring when relaxation prompt is enabled`() {
        val bean = AlertSettingsMapper.toPressureMode(
            PressureModeSettings(
                stressMonitoringEnabled = false,
                relaxationPromptEnabled = true
            )
        )

        assertThat(bean.pressureMode).isTrue()
        assertThat(bean.relaxationReminder).isTrue()

        assertThat(AlertSettingsMapper.fromPressureMode(bean)).isEqualTo(
            PressureModeSettings(
                stressMonitoringEnabled = true,
                relaxationPromptEnabled = true
            )
        )
    }

    @Test
    fun `high-stress mapper round-trips warning flag and threshold`() {
        val bean = SWHRVMonitorBean().apply {
            isWarning = true
            warningValue = 88
        }

        val settings = AlertSettingsMapper.fromHighStressMonitor(bean)

        assertThat(settings).isEqualTo(
            HighStressAlertSettings(
                enabled = true,
                threshold = 88
            )
        )

        val updatedBean = AlertSettingsMapper.toHighStressMonitor(settings)
        assertThat(updatedBean.isWarning).isTrue()
        assertThat(updatedBean.warningValue).isEqualTo(88)
    }

    @Test
    fun `screenless breathing mapper preserves warning value snapshot`() {
        val bean = SWBRMonitorBean().apply {
            isWarning = true
            warningValue = 7
        }

        val (settings, snapshot) = AlertSettingsMapper.fromScreenlessBreathingRelaxation(bean)

        assertThat(settings).isEqualTo(
            PressureModeSettings(
                stressMonitoringEnabled = true,
                relaxationPromptEnabled = true
            )
        )
        assertThat(snapshot).isEqualTo(
            PressureModeSnapshot(
                relaxationWarningValue = 7
            )
        )

        val updatedBean = AlertSettingsMapper.toScreenlessBreathingRelaxation(
            settings = PressureModeSettings(
                stressMonitoringEnabled = false,
                relaxationPromptEnabled = true
            ),
            snapshot = snapshot
        )

        assertThat(updatedBean.isWarning).isTrue()
        assertThat(updatedBean.warningValue).isEqualTo(7)
    }

    @Test
    fun `sedentary mapper keeps frequency in minutes and preserves noon snapshot data`() {
        val bean = CommonReminderBean().apply {
            isOn = true
            noDisturbInLaunch = false
            startTime = SettingTimeBean(9, 15)
            endTime = SettingTimeBean(18, 45)
            frequency = 120
            startNoonTime = SettingTimeBean(12, 0)
            endNoonTime = SettingTimeBean(13, 0)
        }

        val (data, snapshot) = AlertSettingsMapper.fromSedentaryReminder(bean)

        assertThat(data).isEqualTo(
            SedentaryData(
                status = true,
                interval = 120,
                startHour = 9,
                startMinute = 15,
                endHour = 18,
                endMinute = 45
            )
        )
        assertThat(snapshot).isEqualTo(
            SedentaryReminderSnapshot(
                noDisturbInLaunch = false,
                startNoonHour = null,
                startNoonMinute = null,
                endNoonHour = null,
                endNoonMinute = null
            )
        )

        val updatedBean = AlertSettingsMapper.toSedentaryReminder(data, snapshot)
        assertThat(updatedBean.isOn).isTrue()
        assertThat(updatedBean.frequency).isEqualTo(120)
        assertThat(updatedBean.noDisturbInLaunch).isFalse()
        assertThat(updatedBean.startTime.hour).isEqualTo(9)
        assertThat(updatedBean.startTime.minuter).isEqualTo(15)
        assertThat(updatedBean.endTime.hour).isEqualTo(18)
        assertThat(updatedBean.endTime.minuter).isEqualTo(45)
        assertThat(updatedBean.startNoonTime).isNull()
        assertThat(updatedBean.endNoonTime).isNull()
    }

    @Test
    fun `sleep reminder normalization clears seconds and milliseconds`() {
        assertThat(
            AlertSettingsMapper.normalizedSleepReminder(
                SleepReminder(
                    status = true,
                    hour = 22,
                    minute = 30,
                    second = 27,
                    millisecond = 400
                )
            )
        ).isEqualTo(
            SleepReminder(
                status = true,
                hour = 22,
                minute = 30,
                second = 0,
                millisecond = 0
            )
        )
    }
}
