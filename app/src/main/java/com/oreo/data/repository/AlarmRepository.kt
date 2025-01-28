package com.oreo.data.repository

import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.PlannerAlarmData
import com.noisefit_commans.utils.LOGS
import com.oreo.util.alarm.AlarmUtil
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


class AlarmRepository @Inject constructor(
    private val localDataStore: DataStoredInterface,
    private val alarmUtil: AlarmUtil,
) {


    fun getAlarmsData(): PlannerAlarmData? {
        return localDataStore.getSleepPlannerData()?.alarms
    }

    fun updateAlarms(alarms: PlannerAlarmData) {
        val savedData = localDataStore.getSleepPlannerData()
        if (savedData != null) {
            savedData.alarms = alarms
            localDataStore.setSleepPlannerData(savedData)
        }
        scheduleAlarms(savedData?.alarms)
    }

    fun rescheduleAlarms() {
        scheduleAlarms(getAlarmsData())
    }

    private fun scheduleAlarms(alarmsData: PlannerAlarmData?) {
        alarmUtil.cancelAllAlarms()

        alarmsData?.getNonNullAlarms()?.forEach {
            val wakeTime =
                LocalTime.parse(it.second.wake_time, DateTimeFormatter.ofPattern("HH:mm:ss"))
            alarmUtil.scheduleWeeklyAlarm(it.first, wakeTime.hour, wakeTime.minute)
        }
    }

}