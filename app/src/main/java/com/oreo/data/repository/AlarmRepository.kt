package com.oreo.data.repository

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import com.google.gson.Gson
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit.data.local.db.fromJson
import com.noisefit_commans.data.model.KeyValue
import com.noisefit_commans.data.model.PlannerAlarmData
import com.noisefit_commans.data.model.SleepPlannerData
import com.noisefit_commans.utils.AppLogs
import com.oreo.util.alarm.AlarmUtil
import com.oreo.util.alarm.AlarmUtil.Companion.getAlarmToneByKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


class AlarmRepository @Inject constructor(
    private val keyValueDataSource: KeyValueDataSource,
    private val gson: Gson,
    private val alarmUtil: AlarmUtil,
) {
    suspend fun getAlarmsData(): PlannerAlarmData? {
        val localData =
            keyValueDataSource.getData("", KeyValueDataType.SLEEP_PLANNER) ?: return null

        val data = localData.value?.let {
            Gson().fromJson<SleepPlannerData>(
                it
            )
        }
        return data?.alarms
    }

    suspend fun updateAlarms(alarms: PlannerAlarmData) {
        val localData =
            keyValueDataSource.getData("", KeyValueDataType.SLEEP_PLANNER)

        val savedData = localData?.value?.let {
            Gson().fromJson<SleepPlannerData>(
                it
            )
        }

        if (savedData != null) {
            savedData.alarms = alarms


            keyValueDataSource.removeDataByKey("", KeyValueDataType.SLEEP_PLANNER)
            keyValueDataSource.insertData(
                KeyValue(
                    key = "",
                    value = gson.toJson(savedData),
                    type = KeyValueDataType.SLEEP_PLANNER.name
                )
            )

            //TODO check
            /*keyValueDataSource.updateData(
                KeyValue(
                    key = "",
                    value = gson.toJson(savedData),
                    type = KeyValueDataType.SLEEP_PLANNER.name
                )
            )*/
        }

        scheduleAlarms(savedData?.alarms)
    }

    fun rescheduleAlarms() {
        GlobalScope.launch(Dispatchers.IO) {
            scheduleAlarms(getAlarmsData())
        }
    }

    fun cancelAllAlarms() {
        alarmUtil.cancelAllAlarms()
    }

    private fun scheduleAlarms(alarmsData: PlannerAlarmData?) {
        cancelAllAlarms()

        if (hasExactAlarmPermission().not()) {
            AppLogs.sendAppLogs("Alarm permission not granted. cannot schedule alarms")
            return
        }

        alarmsData?.getNonNullAlarms()?.forEach {
            val wakeTime =
                LocalTime.parse(it.second.wake_time, DateTimeFormatter.ofPattern("HH:mm:ss"))
            val bedTime =
                LocalTime.parse(it.second.bed_time, DateTimeFormatter.ofPattern("HH:mm:ss"))

            alarmUtil.scheduleWeeklyAlarm(
                it.first, wakeTime.hour, wakeTime.minute,
                getAlarmToneByKey(it.second.audio ?: 1), wakeTime, bedTime
            )
        }
    }

    private fun hasExactAlarmPermission(): Boolean {
        val notificationManager =
            NoiseFitApplicationMain.context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        if (notificationManager == null) return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

}