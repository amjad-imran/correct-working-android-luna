package com.noisefit_cf2.dataconversions

import com.google.gson.Gson
import com.ido.ble.LocalDataManager
import com.ido.ble.data.manage.database.*
import com.ido.ble.protocol.model.*
import com.ido.ble.protocol.model.Units
import com.ido.ble.protocol.model.UserInfo
import com.noisefit_commans.enums.ApplicationType
import com.noisefit_commans.models.*
import com.noisefit_commans.models.HeartRateInterval
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import org.apache.commons.collections4.ListUtils
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@Suppress("NAME_SHADOWING")
class Colorfit2DataConverter
@Inject
constructor() {
    companion object {

        fun getBloodPressure(liveData: LiveData): BloodPressureData {
            return BloodPressureData(
                diastolicBloodPressure = liveData.dbp,
                systolicBloodPressure = liveData.sbp
            )
        }

        fun getWeatherTypeForWatch(type: String?): Int {
            if (type == null) WeatherInfo.WEATHER_TYPE_HAZE
            return when (type) {
                "clouds" -> WeatherInfo.WEATHER_TYPE_CLOUDY
                "clear" -> WeatherInfo.WEATHER_TYPE_CLEAR
                "fog" -> WeatherInfo.WEATHER_TYPE_HAZE
                "smoke" -> WeatherInfo.WEATHER_TYPE_HAZE
                "haze" -> WeatherInfo.WEATHER_TYPE_HAZE
                "thunderstorm" -> WeatherInfo.WEATHER_TYPE_COLD
                "drizzle" -> WeatherInfo.WEATHER_TYPE_RAINSTORM
                "rain" -> WeatherInfo.WEATHER_TYPE_RAIN
                "snow" -> WeatherInfo.WEATHER_TYPE_SNOW
                else -> WeatherInfo.WEATHER_TYPE_HAZE
            }
        }

        fun getHeartRate(liveData: LiveData): HeartRate {
            return HeartRate(
                averageHeartRate = liveData.heartRate,
                date = DateFormats.getDateFormat(Calendar.getInstance().time)
            )
        }

        fun getStepsData(liveData: LiveData): StepsData {
            return StepsData(
                totalSteps = liveData.totalStep,
                totalCalories = liveData.totalCalories,
                totalDistance = liveData.totalDistances,
                totalActiveTime = liveData.totalActiveTime,
                date = DateFormats.getDateFormat()
            )
        }

        fun parseAlarms(alarms: List<Alarm>): AlarmsList {
            val alarmsList = ArrayList<AlarmsList.Alarm>()
            for (alarm in alarms) {
                if (alarm.on_off) {
                    val newAlarm = AlarmsList.Alarm()
                    newAlarm.id = alarm.alarmId
                    newAlarm.hour = alarm.alarmHour
                    newAlarm.minute = alarm.alarmMinute
                    newAlarm.status = alarm.on_off
                    newAlarm.repeatDays = (alarm.weekRepeat?.toList() as ArrayList).apply {
                        add(0, alarm.on_off)
                    }
                    newAlarm.snoozeDuration = alarm.alarmSnoozeDuration
                    newAlarm.alarmType = when (alarm.alarmType) {
                        Alarm.TYPE_GETUP -> AlarmType.WAKE.type
                        Alarm.TYPE_SLEEP -> AlarmType.SLEEP.type
                        Alarm.TYPE_EXERCISE -> AlarmType.EXERCISE.type
                        Alarm.TYPE_MEDICINE -> AlarmType.MEDICINE.type
                        Alarm.TYPE_MEETING -> AlarmType.MEETING.type
                        else -> AlarmType.CUSTOM.type
                    }
                    alarmsList.add(newAlarm)
                }
            }
            return AlarmsList(alarms = alarmsList)
        }

        fun parseAlarmsV3(alarms: MutableList<AlarmV3>?): AlarmsList {
            val alarmsList = ArrayList<AlarmsList.Alarm>()
            if (alarms != null) {
                for (alarm in alarms) {
                    if (alarm.isOn_off) {
                        val newAlarm = AlarmsList.Alarm()
                        newAlarm.id = alarm.alarm_id
                        newAlarm.hour = alarm.hour
                        newAlarm.minute = alarm.minute
                        newAlarm.status = alarm.isOn_off
                        newAlarm.repeatDays = (alarm.weekRepeat?.toList() as ArrayList).apply {
                            add(0, alarm.isOn_off)
                        }
                        newAlarm.snoozeDuration = alarm.tsnooze_duration
                        newAlarm.alarmType = when (alarm.type) {
                            Alarm.TYPE_GETUP -> AlarmType.WAKE.type
                            Alarm.TYPE_SLEEP -> AlarmType.SLEEP.type
                            Alarm.TYPE_EXERCISE -> AlarmType.EXERCISE.type
                            Alarm.TYPE_MEDICINE -> AlarmType.MEDICINE.type
                            Alarm.TYPE_MEETING -> AlarmType.MEETING.type
                            else -> AlarmType.CUSTOM.type
                        }
                        alarmsList.add(newAlarm)
                    }
                }
            }
            return AlarmsList(alarms = alarmsList)
        }


        fun formatAlarms(alarm: AlarmsList): List<Alarm> {
            val list = ArrayList<Alarm>()
            alarm.alarms?.let {
                it.forEachIndexed { index, item ->

                    val days = item.repeatDays?.let { list ->
                        list.subList(1, list.size)
                    }

                    val newAlarm = Alarm()
                    newAlarm.alarmHour = item.hour
                    newAlarm.alarmMinute = item.minute
                    newAlarm.on_off = item.status
                    newAlarm.weekRepeat = days?.toBooleanArray()
                    newAlarm.alarmSnoozeDuration = item.snoozeDuration
//                    newAlarm.delay_min = item.snoozeDuration
//                    newAlarm.repeat_times = 5
//                    newAlarm.shock_on_off = 1
                    newAlarm.alarmType = when (item.alarmType) {
                        AlarmType.WAKE.type -> Alarm.TYPE_GETUP
                        AlarmType.SLEEP.type -> Alarm.TYPE_SLEEP
                        AlarmType.EXERCISE.type -> Alarm.TYPE_EXERCISE
                        AlarmType.MEDICINE.type -> Alarm.TYPE_MEDICINE
                        AlarmType.MEETING.type -> Alarm.TYPE_MEETING
                        else -> Alarm.TYPE_CUSTOMIZE
                    }
                    newAlarm.alarmId = index + 1
                    list.add(newAlarm)
                }
            }
            return list
        }

        fun formatAlarmsV3(alarm: AlarmsList, isDummy: Boolean): List<AlarmV3> {
            val list = ArrayList<AlarmV3>(10)
            alarm.alarms?.let {

                it.forEachIndexed { index, item ->

                    val days = item.repeatDays?.let { list ->
                        list.subList(1, list.size)
                    }


                    val newAlarm = AlarmV3()
                    newAlarm.hour = item.hour
                    newAlarm.minute = item.minute
                    newAlarm.isOn_off = item.status
                    newAlarm.weekRepeat = days?.toBooleanArray()
                    newAlarm.tsnooze_duration = item.snoozeDuration
                    newAlarm.delay_min = item.snoozeDuration
                    newAlarm.repeat_times = 5
                    newAlarm.shock_on_off = 1
                    newAlarm.type = when (item.alarmType) {
                        AlarmType.WAKE.type -> Alarm.TYPE_GETUP
                        AlarmType.SLEEP.type -> Alarm.TYPE_SLEEP
                        AlarmType.EXERCISE.type -> Alarm.TYPE_EXERCISE
                        AlarmType.MEDICINE.type -> Alarm.TYPE_MEDICINE
                        AlarmType.MEETING.type -> Alarm.TYPE_MEETING
                        else -> Alarm.TYPE_CUSTOMIZE
                    }
                    newAlarm.alarm_id = index + 1
                    list.add(newAlarm)
                }
                if (isDummy && it.size < 10) {
                    for (i in (it.size) until 10) {
                        val dummyAlarm = AlarmV3().apply {
                            hour = 0
                            minute = 0
                            isOn_off = false
                            weekRepeat =
                                booleanArrayOf(false, false, false, false, false, false, false)
                            alarm_id = i + 1
                            tsnooze_duration = 10
                            delay_min = 10
                            repeat_times = 5
                            shock_on_off = 1
                            type = Alarm.TYPE_CUSTOMIZE
                        }

                        list.add(dummyAlarm)
                    }
                }
            }
            return list
        }

        fun formatMenstrualData(menstrualData: MenstrualData): Menstrual {
            val menstrual = Menstrual()
            menstrual.menstrual_cycle = menstrualData.menstrualCycleLength
            menstrual.menstrual_length = menstrualData.menstrualLength
            menstrual.on_off = when (menstrualData.status) {
                true -> Menstrual.STATUS_ON
                false -> Menstrual.STATUS_OFF
            }
            menstrual.ovulation_after_day = menstrualData.ovulationAfter
            menstrual.ovulation_before_day = menstrualData.ovulationBefore
            menstrual.ovulation_interval_day = menstrualData.ovulationInterval
            val calendar = Calendar.getInstance()
            if (!menstrualData.lastMenstrualDate.isNullOrEmpty()) {
                calendar.time = DateFormats.dateFormat.parse(menstrualData.lastMenstrualDate)
            }
            menstrual.last_menstrual_day = calendar.get(Calendar.DAY_OF_MONTH)
            menstrual.last_menstrual_month = calendar.get(Calendar.MONTH)
            menstrual.last_menstrual_year = calendar.get(Calendar.YEAR)
            return menstrual
        }

        fun formatMenstrualReminder(menstrualReminder: MenstrualData.MenstrualReminder): MenstrualRemind {
            val reminder = MenstrualRemind()
            if (menstrualReminder != null) {
                reminder.ovulation_day = menstrualReminder.remindOvulationDayBefore
                reminder.start_day = menstrualReminder.remindStartDayBefore
                val calendar = Calendar.getInstance()
                if (menstrualReminder.reminderTime != null) {
                    calendar.time = DateFormats.timeFormat.parse(menstrualReminder.reminderTime)
                }
                reminder.hour = calendar.get(Calendar.HOUR_OF_DAY)
                reminder.minute = calendar.get(Calendar.MINUTE)
            }
            return reminder
        }

        fun parseMenstrualData(
            menstrualData: Menstrual?,
            menstrualRemind: MenstrualRemind?
        ): MenstrualData {
            return if (menstrualData != null && menstrualRemind != null) {
                MenstrualData(
                    status = when (menstrualData.on_off) {
                        Menstrual.STATUS_ON -> true
                        else -> false
                    },
                    lastMenstrualDate = "${menstrualData.last_menstrual_day}/${menstrualData.last_menstrual_month + 1}/${menstrualData.last_menstrual_year}",
                    menstrualCycleLength = menstrualData.menstrual_cycle,
                    menstrualLength = menstrualData.menstrual_length,
                    ovulationAfter = menstrualData.ovulation_after_day,
                    ovulationBefore = menstrualData.ovulation_before_day,
                    ovulationInterval = menstrualData.ovulation_interval_day,
                    menstrualReminder = MenstrualData.MenstrualReminder(
                        remindStartDayBefore = menstrualRemind.start_day,
                        remindOvulationDayBefore = menstrualRemind.ovulation_day,
                        reminderTime = "${menstrualRemind.hour}:${menstrualRemind.minute}"
                    )
                )
            } else {
                MenstrualData(
                    status = false,
                    lastMenstrualDate = "",
                    menstrualCycleLength = 0,
                    menstrualLength = 0,
                    menstrualReminder = MenstrualData.MenstrualReminder()
                )
            }
        }

        fun formatHeartRateInterval(heartRateInterval: HeartRateInterval): HeartRateMeasureMode {
            val heartRate = HeartRateMeasureMode()
            heartRate.mode = when (heartRateInterval.status) {
                true -> HeartRateMeasureMode.MODE_AUTO
                else -> HeartRateMeasureMode.MODE_MANUAL
            }
            heartRate.hasTimeRange = when (heartRateInterval.interval) {
                0 -> HeartRateMeasureMode.TIME_RANGE_OFF
                else -> HeartRateMeasureMode.TIME_RANGE_ON
            }
            heartRate.startHour = heartRateInterval.getStartTime().get(Calendar.HOUR_OF_DAY)
            heartRate.startMinute = heartRateInterval.getStartTime().get(Calendar.MINUTE)

            heartRate.endHour = heartRateInterval.getEndTime().get(Calendar.HOUR_OF_DAY)
            heartRate.endMinute = heartRateInterval.getEndTime().get(Calendar.MINUTE)

            return heartRate
        }

        fun formatHeartRateIntervalV3(heartRateInterval: HeartRateInterval): HeartRateMeasureModeV3 {
            val heartRate = HeartRateMeasureModeV3()
            heartRate.mode = when (heartRateInterval.status) {
                true -> 204
                else -> HeartRateMeasureMode.MODE_MANUAL
            }
            heartRate.hasTimeRange = when (heartRateInterval.status) {
                true -> HeartRateMeasureMode.TIME_RANGE_ON
                false -> HeartRateMeasureMode.TIME_RANGE_OFF
            }

            val arr = heartRateInterval.startTime.split(":")
            heartRate.startHour =
                arr.get(0).toInt() //heartRateInterval.getStartTime().get(Calendar.HOUR_OF_DAY)
            heartRate.startMinute =
                arr.get(1).toInt() //heartRateInterval.getStartTime().get(Calendar.MINUTE)
            val arr1 = heartRateInterval.endTime.split(":")
            heartRate.endHour =
                arr1.get(0).toInt() //heartRateInterval.getEndTime().get(Calendar.HOUR_OF_DAY)
            heartRate.endMinute =
                arr1.get(1).toInt() //heartRateInterval.getEndTime().get(Calendar.MINUTE)
            val calendar = Calendar.getInstance()
            val time = calendar.timeInMillis / 1000
            heartRate.updateTime = time.toInt()
            //heartRate.measurementInterval = heartRateInterval.interval * 60
            return heartRate
        }

        fun formatHeartRateIntervalActive(heartRateInterval: HeartRateInterval): HeartRateMeasureModeV3 {
            val heartRate = HeartRateMeasureModeV3()
            if (heartRateInterval.status) {
                heartRate.mode = 204
                heartRate.measurementInterval = heartRateInterval.interval * 60
            } else if (heartRateInterval.status2) {
                heartRate.mode = 204
                heartRate.measurementInterval = heartRateInterval.interval
            } else {
                heartRate.mode = HeartRateMeasureMode.MODE_MANUAL
            }

            heartRate.hasTimeRange = when (heartRateInterval.status) {
                true -> HeartRateMeasureMode.TIME_RANGE_ON
                false -> HeartRateMeasureMode.TIME_RANGE_OFF
            }

            val arr = heartRateInterval.startTime.split(":")
            heartRate.startHour =
                arr.get(0).toInt() //heartRateInterval.getStartTime().get(Calendar.HOUR_OF_DAY)
            heartRate.startMinute =
                arr.get(1).toInt() //heartRateInterval.getStartTime().get(Calendar.MINUTE)
            val arr1 = heartRateInterval.endTime.split(":")
            heartRate.endHour =
                arr1.get(0).toInt() //heartRateInterval.getEndTime().get(Calendar.HOUR_OF_DAY)
            heartRate.endMinute =
                arr1.get(1).toInt() //heartRateInterval.getEndTime().get(Calendar.MINUTE)

//            heartRate.measurementInterval = when (heartRateInterval.status) {
//                true -> heartRateInterval.interval * 60
//                else -> heartRateInterval.interval
//            }
//            heartRate.measurementInterval = when (heartRateInterval.status2) {
//                true -> heartRateInterval.interval
//                else -> heartRateInterval.interval
//            }

            val calendar = Calendar.getInstance()
            val time = calendar.timeInMillis / 1000
            heartRate.updateTime = time.toInt()
            return heartRate
        }


        fun formatHeartRateInterval(heartRateMeasureMode: HeartRateMeasureMode): HeartRateInterval {
            return HeartRateInterval(
                status = when (heartRateMeasureMode.mode) {
                    HeartRateMeasureMode.MODE_AUTO -> true
                    else -> false
                }, endTime = "${heartRateMeasureMode.endHour}:${heartRateMeasureMode.endMinute}",
                startTime = "${heartRateMeasureMode.startHour}:${heartRateMeasureMode.startMinute}"
            )
        }

        fun formatHeartRateIntervalV3(heartRateMeasureMode: HeartRateMeasureModeV3): HeartRateInterval {
            LOGS.d("HEART RATE:::"+heartRateMeasureMode.mode)
            return HeartRateInterval(
                status = when (heartRateMeasureMode.mode) {
                    HeartRateMeasureMode.MODE_AUTO, HeartRateMeasureMode.MODE_PERSISTENT, 204 -> true
                    else -> false
                },
                endTime = "${heartRateMeasureMode.endHour}:${heartRateMeasureMode.endMinute}",
                startTime = "${heartRateMeasureMode.startHour}:${heartRateMeasureMode.startMinute}",
                interval = heartRateMeasureMode.measurementInterval
            )
        }

        fun formatHeartRateIntervalActiveGet(heartRateMeasureMode: HeartRateMeasureModeV3): HeartRateInterval {
            var status = false
            var status2 = false
            if (heartRateMeasureMode.mode == 204) {
                if (heartRateMeasureMode.measurementInterval == 300) {
                    status = true
                    status2 = false
                } else if (heartRateMeasureMode.measurementInterval == 5) {
                    status = false
                    status2 = true
                } else {
                    status = false
                    status2 = false
                }
            }
            return HeartRateInterval(
                status = status,
                status2 = status2,
                endTime = "${heartRateMeasureMode.endHour}:${heartRateMeasureMode.endMinute}",
                startTime = "${heartRateMeasureMode.startHour}:${heartRateMeasureMode.startMinute}",
                interval = heartRateMeasureMode.measurementInterval
            )
        }

        fun parseDoNotDisturb(notDisturbPara: NotDisturbPara?): DoNotDisturb {
            val dndPara = DoNotDisturb()
            if (notDisturbPara != null) {
                dndPara.startHour = notDisturbPara.startHour
                dndPara.startMinute = notDisturbPara.startMinute
                dndPara.endHour = notDisturbPara.endHour
                dndPara.endMinute = notDisturbPara.endMinute
                dndPara.status = when (notDisturbPara.onOFf) {
                    NotDisturbPara.STATE_ON -> true
                    else -> false
                }
            }
            return dndPara
        }

        fun formatDoNotDisturb(doNotDisturb: DoNotDisturb): NotDisturbPara {
            val dnd = NotDisturbPara()
            dnd.onOFf = when (doNotDisturb.status) {
                true -> NotDisturbPara.STATE_ON
                else -> NotDisturbPara.STATE_OFF
            }
            if (doNotDisturb.status) {
                dnd.startHour = doNotDisturb.startHour
                dnd.startMinute = doNotDisturb.startMinute
                dnd.endHour = doNotDisturb.endHour
                dnd.endMinute = doNotDisturb.endMinute
            }
            return dnd
        }

        fun parseSedentaryData(longSit: LongSit): SedentaryData {
            val repeat = getIntFromBooleanArray(longSit.weeks)
            return SedentaryData(
                status = longSit.isOnOff,
                startHour = longSit.startHour,
                startMinute = longSit.startMinute,
                endHour = longSit.endHour,
                endMinute = longSit.endMinute,
                interval = longSit.interval,
                repeat = repeat
            )
        }

        fun parseWalkReminderData(longSit: WalkReminder): WalkReminderData {
            val repeat = getIntFromBooleanArray(longSit.weeks)
            val week = getIntArrayFromBooleanArray(longSit.weeks)
            return WalkReminderData(
                status = longSit.onOff == 1,
                startHour = longSit.startHour,
                startMinute = longSit.startMinute,
                endHour = longSit.endHour,
                endMinute = longSit.endMinute,
                goalSteps = longSit.goalStep,
                repeat = repeat,
                weeks = week
            )
        }

        fun parseDrinkWaterData(drinkWater: DrinkWaterReminder): SedentaryData {
            val repeat = getIntFromBooleanArray(drinkWater.weeks)
            return SedentaryData(
                status = drinkWater.isOnOff,
                startHour = drinkWater.startHour,
                startMinute = drinkWater.startMinute,
                endHour = drinkWater.endHour,
                endMinute = drinkWater.endMinute,
                interval = drinkWater.interval,
                repeat = repeat,
                repeatDays = getBooleanFromInt(repeat).toList()
            )
        }

        fun parseStressParam(stress: PressureParam): SedentaryData {
            val repeat = getIntFromBooleanArray(stress.weekRepeat)
            return SedentaryData(
                status = when (stress.onOff) {
                    PressureParam.STATE_ON -> true
                    else -> false
                },
                startHour = stress.startHour,
                startMinute = stress.startMinute,
                endHour = stress.endHour,
                endMinute = stress.endMinute,
                interval = stress.interval,
                repeat = repeat
            )
        }

        fun formatSedentaryData(sedentaryData: SedentaryData): LongSit {
            val sit = LongSit()
            sit.isOnOff = sedentaryData.status
            if (sit.isOnOff) {
                sit.startHour = sedentaryData.startHour
                sit.startMinute = sedentaryData.startMinute
                sit.endHour = sedentaryData.endHour
                sit.endMinute = sedentaryData.endMinute
                sit.interval = sedentaryData.interval
                sit.weeks = getBooleanFromInt(sedentaryData.repeat)
            }
            return sit
        }

        fun formatWalkReminderData(walkReminderData: WalkReminderData): WalkReminder {
            val sit = WalkReminder()
            sit.onOff = if (walkReminderData.status) 1 else 0
            if (sit.onOff == 1) {
                sit.startHour = walkReminderData.startHour
                sit.startMinute = walkReminderData.startMinute
                sit.endHour = walkReminderData.endHour
                sit.endMinute = walkReminderData.endMinute
                sit.goalStep = walkReminderData.goalSteps
                sit.weeks = walkReminderData.weeks?.let { getBooleanFromIntArray(it) }
            }
            return sit
        }

        fun formatDrinkReminderData(sedentaryData: SedentaryData): DrinkWaterReminder {
            val drink = DrinkWaterReminder()
            drink.isOnOff = sedentaryData.status
            if (drink.isOnOff) {
                drink.startHour = sedentaryData.startHour
                drink.startMinute = sedentaryData.startMinute
                drink.endHour = sedentaryData.endHour
                drink.endMinute = sedentaryData.endMinute
                drink.interval = sedentaryData.interval
                drink.weeks =
                    sedentaryData.repeatDays?.toBooleanArray()/*getBooleanFromInt(sedentaryData.repeat)*/
            }
            return drink
        }

        fun formatStressData(sedentaryData: SedentaryData): PressureParam {
            val stress = PressureParam()
            if (sedentaryData.status) {
                stress.onOff = PressureParam.STATE_ON
                stress.remindOnOff = PressureParam.STATE_ON
            } else {
                stress.onOff = PressureParam.STATE_OFF
                stress.remindOnOff = PressureParam.STATE_OFF
            }
            if (sedentaryData.status) {
                stress.startHour = sedentaryData.startHour
                stress.startMinute = sedentaryData.startMinute
                stress.endHour = sedentaryData.endHour
                stress.endMinute = sedentaryData.endMinute
                stress.interval = sedentaryData.interval
                stress.weekRepeat = getBooleanFromInt(sedentaryData.repeat)
                stress.highThreshold = 60
            }
            return stress
        }

        fun formatWashHandReminderData(handWashing: HandWashing): WashHandReminder {
            val washHand = WashHandReminder()
            if (handWashing.startWash)
                washHand.onOff = WashHandReminder.STATE_ON
            else
                washHand.onOff = WashHandReminder.STATE_OFF
            if (washHand.onOff == 1) {
                washHand.startHour = handWashing.startHour
                washHand.startMinute = handWashing.startMinute
                washHand.endHour = handWashing.endHour
                washHand.endMinute = handWashing.endMinute
                washHand.interval = handWashing.frequency
                washHand.repeat = 127
            }
            return washHand
        }

        fun getIntFromBooleanArray(bArray: BooleanArray): Int {
            var str = ""
            for (element in bArray) {
                if (element == true) {
                    str += "1"
                } else {
                    str += "0"
                }
            }
            val sedentaryRepeat = Integer.parseInt(str, 2) as Int
            return sedentaryRepeat
        }

        fun getIntArrayFromBooleanArray(bArray: BooleanArray): IntArray {
            val intList = intArrayOf(0, 0, 0, 0, 0, 0, 0)
            bArray.forEachIndexed { index, b ->
                if (b) {
                    intList[index] = 1
                }
            }
            return intList
        }

        fun getBooleanFromInt(cycle: Int): BooleanArray {
            val booleanList = booleanArrayOf(false, false, false, false, false, false, false)
            val list = String.format("%7s", Integer.toBinaryString(cycle)).replace(' ', '0')
            LOGS.d("noise_fit_event:colorfit_pro_2 : idleAlert:::" + list)
            var i = 0
            list.toCharArray().forEach { char ->
                if (char.toString() == "1") {
                    booleanList[i] = true
                } else {
                    booleanList[i] = false
                }
                i += 1
            }
            LOGS.d("noise_fit_event:colorfit_pro_2 : idleAlert:::" + booleanList.toString())
            return booleanList
        }

        fun getBooleanFromIntArray(intArray: IntArray): BooleanArray {
            val booleanList = booleanArrayOf(false, false, false, false, false, false, false)

            intArray.forEachIndexed { index, i ->
                if (i == 1) {
                    booleanList[index] = true
                }
            }

            return booleanList
        }

        fun parseSportsData(p0: HealthSport?, p1: MutableList<HealthSportItem>?): StepsData? {
            p0?.let { healthSport ->
                val stepsData = StepsData(
                    totalSteps = healthSport.totalStepCount,
                    totalActiveTime = healthSport.totalActiveTime,
                    totalDistance = healthSport.totalDistance,
                    totalCalories = healthSport.totalCalory,
                    date = DateFormats.dateFormat.format(healthSport.date)
                )
                val stepArray = ArrayList<StepsData.StepDataBreakup>()

                p1?.let { it1 ->

                    (0..96).forEachIndexed { index, _ ->
                        if (it1.size < index) {
                            it1.add(HealthSportItem())
                        }
                    }

                    val partedList = ListUtils.partition(p1, 4)

                    partedList.forEachIndexed { index, list ->
                        var totalSteps = 0
                        var totalDistance = 0
                        var totalCalories = 0
                        var totalActiveTime = 0
                        list.forEach { item ->
                            totalSteps += item.stepCount
                            totalDistance += item.distance
                            totalCalories += item.calory
                            totalActiveTime += item.activeTime
                        }

                        val stepData = StepsData.StepDataBreakup(
                            steps = totalSteps,
                            calories = totalCalories,
                            distance = totalDistance,
                            activeTime = totalActiveTime,
                            hourOfTheDay = index
                        )
                        stepArray.add(stepData)
                    }
                }

                stepsData.stepArray = stepArray
                return stepsData
            }
            return null
        }

        fun parseSportsDataV3(p0: HealthSportV3?, p1: MutableList<HealthSportV3Item>?): StepsData? {
            p0?.let { healthSport ->
                val hour = healthSport?.hour
                val minute = healthSport?.minute
                val day = healthSport.day
                val month = healthSport.month
                val year = healthSport?.year
                var dayString = day.toString()
                var monthString = month.toString()
                if (day < 10)
                    dayString = "0" + day
                if (month < 10)
                    monthString = "0" + month
                val stepsData = StepsData(
                    totalSteps = healthSport.total_step,
                    totalActiveTime = healthSport.total_active_time,
                    totalDistance = healthSport.total_distances,
                    totalCalories = healthSport.total_activity_calories,
                    hourOfTheDay = hour,
                    date = "$dayString/$monthString/$year"
                )
                val stepArray = ArrayList<StepsData.StepDataBreakup>()

                p1?.let { it1 ->

                    (0..96).forEachIndexed { index, _ ->
                        if (it1.size < index) {
                            it1.add(HealthSportV3Item())
                        }
                    }

                    val partedList = ListUtils.partition(p1, 4)

                    partedList.forEachIndexed { index, list ->
                        var totalSteps = 0
                        var totalDistance = 0
                        var totalCalories = 0
                        var totalActiveTime = 0
                        list.forEach { item ->
                            totalSteps += item.step_count
                            totalDistance += item.distance
                            totalCalories += item.activity_calories
                            totalActiveTime += item.active_time
                        }

                        val stepData = StepsData.StepDataBreakup(
                            steps = totalSteps,
                            calories = totalCalories,
                            distance = totalDistance,
                            activeTime = totalActiveTime,
                            hourOfTheDay = index
                        )
                        stepArray.add(stepData)
                    }
                }

                stepsData.stepArray = stepArray
                return stepsData
            }
            return null
        }

        fun parseHeartHistory(
            heartItems: List<HealthHeartRateItem>,
            healthHeartRate: HealthHeartRate?
        ): List<HeartRate> {
            val heartRateHistory = ArrayList<HeartRate>()
//            val heartRate = HeartRate()
            healthHeartRate?.let { healthHeartRate ->
                var totalOffset = healthHeartRate.startTime

                heartItems.forEach {
                    totalOffset += it.offsetMinute
                    val dateTime = Calendar.getInstance()
                    dateTime.time = it.date
                    dateTime.add(Calendar.MINUTE, totalOffset)
                    val date = DateFormats.dateFormat.format(dateTime.time)
                    val time = DateFormats.timeFormat.format(dateTime.time)
                    val timeStamp = DateFormats.convertDateTimeToTimeStamp(date, time)
                    heartRateHistory.add(
                        HeartRate(
                            averageHeartRate = it.HeartRaveValue,
                            date = date,
                            time = time,
                            timeStamp = timeStamp,
                            highestHeartRate = healthHeartRate.userMaxHr,
                            lowestHeartRate = healthHeartRate.silentHeart,
                            restingHeartRate = healthHeartRate.silentHeart
                        )
                    )
                }

            }
            return heartRateHistory
        }

        fun parseHeartHistoryV3(healthHeartRate: HealthHeartRateSecond?): List<HeartRate> {
            val heartRateHistory = ArrayList<HeartRate>()
//            val heartRate = HeartRate()
            var highest = 0
            var lowest = 0


            val dateTime1 = Calendar.getInstance()
            dateTime1.add(Calendar.MINUTE, -10)
            healthHeartRate?.let { healthHeartRate ->
                var totalOffset = healthHeartRate.startTime
                var myOffset = 0
                for (it in healthHeartRate.items) {
                    totalOffset += it.offset
                    if (it.heartRateVal == 0)
                        continue

                    if (totalOffset > myOffset) {
                        myOffset += 900
                        val dateTime = Calendar.getInstance()
                        dateTime.time = healthHeartRate.date
                        dateTime.add(Calendar.SECOND, totalOffset)
                        // LOGS.d("heart_rate_history ${healthHeartRate.five_min_max_data} ${healthHeartRate.five_min_max_data} ${healthHeartRate.five_min_avg_data}"  )
                        val date = DateFormats.dateFormat.format(dateTime.time)
                        val time = DateFormats.timeFormat.format(dateTime.time)

                        if (highest < it.heartRateVal) highest = it.heartRateVal

                        if (lowest == 0 && it.heartRateVal != 0) {
                            lowest = it.heartRateVal
                        } else if (it.heartRateVal != 0 && lowest > it.heartRateVal) {
                            lowest = it.heartRateVal
                        }
                        val timeStamp = DateFormats.convertDateTimeToTimeStamp(date, time)
                        heartRateHistory.add(
                            HeartRate(
                                averageHeartRate = it.heartRateVal,
                                date = date,
                                time = time,
                                timeStamp = timeStamp,
                                highestHeartRate = highest,
                                lowestHeartRate = lowest,
                                restingHeartRate = healthHeartRate.silentHR
                            )
                        )
                    }
                }
//                //heartRate.highestHeartRate = healthHeartRate.five_min_max_data
//                //heartRate.lowestHeartRate = healthHeartRate.five_min_min_data
//                heartRate.highestHeartRate = highest
//                heartRate.lowestHeartRate = lowest
//
//                heartRate.restingHeartRate = healthHeartRate.silentHR

            }
            //  LOGS.d("parseHeartHistoryV3 ${Gson().toJson(heartRateHistory)}")
            return heartRateHistory

        }

        fun parseHeartData(
            p0: HealthHeartRate?,
            p1: MutableList<HealthHeartRateItem>?
        ): HeartRate? {
            p1?.let {
                var highest = 0
                var lowest = 0
                var totalValue = 0
                var totalCount = 0
                var restingHR = 0
                for (item in p1) {
                    if (item.HeartRaveValue == 0) continue

                    if (highest < item.HeartRaveValue) highest = item.HeartRaveValue

                    if (lowest == 0) {
                        if (lowest < item.HeartRaveValue) lowest = item.HeartRaveValue
                    } else {
                        if (lowest > item.HeartRaveValue) lowest = item.HeartRaveValue
                    }

                    totalValue += item.HeartRaveValue
                    totalCount++
                }
                if (totalCount == 0) {
                    totalCount = 1
                }
                if (p0 != null) {
                    restingHR = p0.silentHeart
                }
                return HeartRate(lowestHeartRate = lowest,
                    highestHeartRate = highest,
                    averageHeartRate = (totalValue / totalCount),
                    restingHeartRate = restingHR,
                    date = p0?.let { DateFormats.dateFormat.format(it.date) }
                )
            }
            return null
        }

        fun parseHeartDataV3(p0: HealthHeartRateSecond?, p1: MutableList<Int>?): HeartRate? {
            p1?.let {
                var highest = 0
                var lowest = 0
                var totalValue = 0
                var totalCount = 0

                for (item in p1) {
                    if (item == 0) continue

                    if (highest < item) highest = item

                    if (lowest == 0) {
                        if (lowest < item) lowest = item
                    } else {
                        if (lowest > item) lowest = item
                    }

                    totalValue += item
                    totalCount++
                }
                if (totalCount == 0) {
                    totalCount = 1
                }
                return HeartRate(lowestHeartRate = p0?.five_min_min_data!!,
                    highestHeartRate = p0?.five_min_max_data!!,
                    restingHeartRate = p0?.silentHR!!,
                    averageHeartRate = p0?.five_min_avg_data!!,
                    date = p0?.let { DateFormats.dateFormat.format(it.date) }
                )
            }
            return null
        }

        fun parseHeartDataV3ForLast5Min(
            p0: HealthHeartRateSecond?,
            p1: List<HealthHeartRateSecondItem>?
        ): HeartRate? {
            p1?.let {
                var highest = 0
                var lowest = 0
                var totalValue = 0
                var totalCount = 0
                var offset = 0

                for (item in p1) {
                    if (item.offset == 255) continue

                    offset += item.offset

                    if (offset <= 300) {
                        totalValue += item.heartRateVal
                        totalCount++
                    }

                    if (highest < item.heartRateVal) highest = item.heartRateVal

                    if (lowest == 0 && item.heartRateVal != 0) {
                        lowest = item.heartRateVal
                    } else if (item.heartRateVal != 0 && lowest > item.heartRateVal) {
                        lowest = item.heartRateVal
                    }
                }
                if (totalCount == 0) {
                    totalCount = 1
                }
                return HeartRate(lowestHeartRate = lowest,
                    highestHeartRate = highest,
                    restingHeartRate = p0?.silentHR!!,
                    averageHeartRate = (totalValue / totalCount).toInt(),
                    date = p0?.let { DateFormats.dateFormat.format(it.date) }
                )
            }
            return null
        }

//        fun parseHeartDataV3(p0: HealthHeartRateSecond?, p1: MutableList<HealthHeartRateSecondItem>?): HeartRate? {
//            p1?.let {
//                var highest = 0
//                var lowest = 0
//                var totalValue = 0
//                var totalCount = 0
//
//                for (item in p1) {
//                    if (item.heartRateVal == 0) continue
//
//                    if (highest < item.heartRateVal) highest = item.heartRateVal
//
//                    if (lowest == 0) {
//                        if (lowest < item.heartRateVal) lowest = item.heartRateVal
//                    } else {
//                        if (lowest > item.heartRateVal) lowest = item.heartRateVal
//                    }
//
//                    totalValue += item.heartRateVal
//                    totalCount++
//                }
//                if(totalCount == 0){
//                    totalCount = 1
//                }
//                return HeartRate(lowestHeartRate = lowest, highestHeartRate = highest,
//                        restingHeartRate = p0?.silentHR!!, averageHeartRate = (totalValue / totalCount),
//                        date = p0?.let { DateFormats.dateTimeFormatWithoutZone.format(it.date) }
//                )
//            }
//            return null
//        }


        fun parseSleepData(p0: HealthSleep?, p1: MutableList<HealthSleepItem>?): SleepData? {
            p0?.let {
                val day = it.day
                val month = it.month
                val year = it.year
                var dayString = day.toString()
                var monthString = month.toString()
                if (day < 10)
                    dayString = "0" + day
                if (month < 10)
                    monthString = "0" + month
                val sleepData = SleepData(
                    light = it.lightSleepMinutes,
                    deep = it.deepSleepMinutes,
                    total = it.totalSleepMinutes,
                    awake = (it.totalSleepMinutes - it.lightSleepMinutes - it.deepSleepMinutes),
                    availableSleepTypes = "deep;light;awake",
                    date = "$dayString/$monthString/$year"
                )

                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, p0.sleepEndedTimeH)
                calendar.set(Calendar.MINUTE, p0.sleepEndedTimeM)
                sleepData.endDate = DateFormats.dateFormat.format(calendar.time)
                sleepData.endTime = DateFormats.timeFormat.format(calendar.time)

                calendar.add(Calendar.MINUTE, -p0.totalSleepMinutes)
                sleepData.startDate = DateFormats.dateFormat.format(calendar.time)
                sleepData.startTime = DateFormats.timeFormat.format(calendar.time)

                val sleepArray = ArrayList<SleepData.SleepDataBreakup>()
                p1?.let { list ->
                    list.forEachIndexed { index, hash ->

                        val sleep = SleepData.SleepDataBreakup(
                            sleepType = when (hash.sleepStatus) {
                                1 -> SleepType.AWAKE.type
                                2 -> SleepType.LIGHT.type
                                else -> SleepType.DEEP.type
                            }
                        )

                        val duration = hash.offsetMinute

                        sleep.duration = duration
                        when (index) {
                            0 -> {
                                sleep.startDate = sleepData.startDate
                                sleep.startTime = sleepData.startTime
                                calendar.add(Calendar.MINUTE, duration)
                                sleep.endDate = DateFormats.dateFormat.format(calendar.time)
                                sleep.endTime = DateFormats.timeFormat.format(calendar.time)
                            }
                            list.size - 1 -> {
                                sleep.endDate = sleepData.endDate
                                sleep.endTime = sleepData.endTime

                                val lastSleep = sleepArray[index - 1]
                                calendar.add(Calendar.MINUTE, 0)
                                sleep.startDate = lastSleep.endDate
                                sleep.startTime = lastSleep.endTime
                            }
                            else -> {
                                calendar.add(Calendar.MINUTE, 0)
                                sleep.startDate = DateFormats.dateFormat.format(calendar.time)
                                sleep.startTime = DateFormats.timeFormat.format(calendar.time)

                                calendar.add(Calendar.MINUTE, duration)
                                sleep.endDate = DateFormats.dateFormat.format(calendar.time)
                                sleep.endTime = DateFormats.timeFormat.format(calendar.time)
                            }
                        }
                        sleepArray.add(sleep)
                    }
                }
                sleepData.sleepArray = sleepArray
                return sleepData
            }
            return null
        }

        fun parseSleepDataV3(
            colorFitDevice: ColorFitDevice,
            p0: HealthSleepV3?,
            p1: MutableList<HealthSleepV3Item>?
        ): SleepData? {
            p0?.let {
                val day = it.get_up_day
                val month = it.get_up_month
                val year = it.get_up_year
                var dayString = day.toString()
                var monthString = month.toString()
                if (day < 10)
                    dayString = "0" + day
                if (month < 10)
                    monthString = "0" + month
                val sleepData = SleepData(
                    light = it.light_mins,
                    deep = it.deep_mins,
                    total = it.total_sleep_time_mins,
                    awake = (it.total_sleep_time_mins - it.light_mins - it.deep_mins - it.rem_mins),
                    remCount = it.rem_mins,
                    breathQuality = it.breath_quality,
                    sleepScore = it.sleep_score,
                    date = "$dayString/$monthString/$year"
                )
                val sleepArray = ArrayList<SleepData.SleepDataBreakup>()
                var calendar = Calendar.getInstance()
                colorFitDevice?.deviceType?.let { deviceType ->
                    if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType) {
                        sleepData.availableSleepTypes = "deep;light;rem;awake"
                        calendar.set(Calendar.DAY_OF_MONTH, p0.get_up_day)
                        calendar.set(Calendar.MONTH, p0.get_up_month - 1)
                        calendar.set(Calendar.YEAR, p0.get_up_year)
                        calendar.set(Calendar.HOUR_OF_DAY, p0.get_up_hour)
                        calendar.set(Calendar.MINUTE, p0.get_up_minte)
                        sleepData.endDate = DateFormats.dateFormat.format(calendar.time)
                        sleepData.endTime = DateFormats.timeFormat.format(calendar.time)

                        calendar = Calendar.getInstance()
                        calendar.set(Calendar.DAY_OF_MONTH, p0.fall_asleep_day)
                        calendar.set(Calendar.MONTH, p0.fall_asleep_month - 1)
                        calendar.set(Calendar.YEAR, p0.fall_asleep_year)
                        calendar.set(Calendar.HOUR_OF_DAY, p0.fall_asleep_hour)
                        calendar.set(Calendar.MINUTE, p0.fall_asleep_minte)
                        sleepData.startDate = DateFormats.dateFormat.format(calendar.time)
                        sleepData.startTime = DateFormats.timeFormat.format(calendar.time)


                        p1?.let { list ->
                            list.forEachIndexed { index, hash ->
                                val sleep = SleepData.SleepDataBreakup(
                                    sleepType = when (hash.stage) {
                                        1 -> SleepType.AWAKE.type
                                        2 -> SleepType.LIGHT.type
                                        3 -> SleepType.DEEP.type
                                        else -> SleepType.REM.type
                                    }
                                )

                                val duration = hash.duration
                                sleep.duration = duration
                                when (index) {
                                    0 -> {
                                        sleep.startDate = sleepData.startDate
                                        sleep.startTime = sleepData.startTime
                                        calendar.add(Calendar.MINUTE, duration)
                                        sleep.endDate = DateFormats.dateFormat.format(calendar.time)
                                        sleep.endTime = DateFormats.timeFormat.format(calendar.time)
                                    }
                                    list.size - 1 -> {
                                        sleep.endDate = sleepData.endDate
                                        sleep.endTime = sleepData.endTime

                                        val lastSleep = sleepArray[index - 1]
                                        calendar.add(Calendar.MINUTE, 0)
                                        sleep.startDate = lastSleep.endDate
                                        sleep.startTime = lastSleep.endTime
                                    }
                                    else -> {
                                        calendar.add(Calendar.MINUTE, 0)
                                        sleep.startDate =
                                            DateFormats.dateFormat.format(calendar.time)
                                        sleep.startTime =
                                            DateFormats.timeFormat.format(calendar.time)

                                        calendar.add(Calendar.MINUTE, duration)
                                        sleep.endDate = DateFormats.dateFormat.format(calendar.time)
                                        sleep.endTime = DateFormats.timeFormat.format(calendar.time)
                                    }
                                }
                                sleepArray.add(sleep)
                            }
                        }
                    } else {
                        sleepData.availableSleepTypes = "deep;light;awake"
                        calendar.set(Calendar.DAY_OF_MONTH, p0.get_up_day)
                        calendar.set(Calendar.MONTH, p0.get_up_month - 1)
                        calendar.set(Calendar.YEAR, p0.get_up_year)
                        calendar.set(Calendar.HOUR_OF_DAY, p0.get_up_hour)
                        calendar.set(Calendar.MINUTE, p0.get_up_minte)
                        sleepData.endDate = DateFormats.dateFormat.format(calendar.time)
                        sleepData.endTime = DateFormats.timeFormat.format(calendar.time)

                        //calendar = Calendar.getInstance()
//                        calendar.set(Calendar.DAY_OF_MONTH, p0.fall_asleep_day)
//                        calendar.set(Calendar.MONTH, p0.fall_asleep_month-1)
//                        calendar.set(Calendar.YEAR, p0.fall_asleep_year)
//                        calendar.set(Calendar.HOUR_OF_DAY, p0.fall_asleep_hour)
//                        calendar.set(Calendar.MINUTE, p0.fall_asleep_minte)
//                        sleepData.startDate = DateFormats.dateFormat.format(calendar.time)
//                        sleepData.startTime = DateFormats.timeFormat.format(calendar.time)
                        calendar.add(Calendar.MINUTE, -p0.total_sleep_time_mins)
                        sleepData.startDate = DateFormats.dateFormat.format(calendar.time)
                        sleepData.startTime = DateFormats.timeFormat.format(calendar.time)


                        p1?.let { list ->
                            list.forEachIndexed { index, hash ->
                                val sleep = SleepData.SleepDataBreakup(
                                    sleepType = when (hash.stage) {
                                        1 -> SleepType.AWAKE.type
                                        2 -> SleepType.LIGHT.type
                                        3 -> SleepType.DEEP.type
                                        else -> SleepType.REM.type
                                    }
                                )

                                val duration = hash.duration
                                sleep.duration = duration
                                when (index) {
                                    0 -> {
                                        sleep.startDate = sleepData.startDate
                                        sleep.startTime = sleepData.startTime
                                        calendar.add(Calendar.MINUTE, duration)
                                        sleep.endDate = DateFormats.dateFormat.format(calendar.time)
                                        sleep.endTime = DateFormats.timeFormat.format(calendar.time)
                                    }
                                    list.size - 1 -> {
                                        sleep.endDate = sleepData.endDate
                                        sleep.endTime = sleepData.endTime

                                        val lastSleep = sleepArray[index - 1]
                                        calendar.add(Calendar.MINUTE, 0)
                                        sleep.startDate = lastSleep.endDate
                                        sleep.startTime = lastSleep.endTime
                                    }
                                    else -> {
                                        calendar.add(Calendar.MINUTE, 0)
                                        sleep.startDate =
                                            DateFormats.dateFormat.format(calendar.time)
                                        sleep.startTime =
                                            DateFormats.timeFormat.format(calendar.time)

                                        calendar.add(Calendar.MINUTE, duration)
                                        sleep.endDate = DateFormats.dateFormat.format(calendar.time)
                                        sleep.endTime = DateFormats.timeFormat.format(calendar.time)
                                    }
                                }
                                sleepArray.add(sleep)
                            }
                        }
                    }
                }


                sleepData.sleepArray = sleepArray
                return sleepData
            }
            return null
        }

        fun parseUserInfo(info: UserInfo): com.noisefit_commans.models.UserInfo {
            return com.noisefit_commans.models.UserInfo(
                gender = when (info.gender) {
                    UserInfo.FEMALE -> Gender.FEMALE.type
                    else -> Gender.MALE.type
                }, height = info.height, weight = info.weight,
                dob = "${info.day}/${info.month}/${info.year}"
            )
        }

        fun formatUserInfo(userInfo: com.noisefit_commans.models.UserInfo): UserInfo {
            val deviceUserInfo = UserInfo()
            val calendar = Calendar.getInstance()
            calendar.time = DateFormats.dateFormat3.parse(userInfo.dob)
            deviceUserInfo.year = calendar.get(Calendar.YEAR)
            deviceUserInfo.month = calendar.get(Calendar.MONTH)
            deviceUserInfo.day = calendar.get(Calendar.DATE)
            deviceUserInfo.weight = userInfo.weight
            deviceUserInfo.height = userInfo.height
            deviceUserInfo.gender = when (userInfo.gender.lowercase()) {
                Gender.FEMALE.type.lowercase() -> UserInfo.FEMALE
                else -> UserInfo.MALE
            }
            return deviceUserInfo
        }

        fun parseLanguage(units: Units?): Language {
            if (units == null) {
                return Language(
                    language = DeviceLanguage.ENGLISH.type
                )
            }
            return Language(
                language = when (units.language) {
                    Units.LANG_ZH -> DeviceLanguage.CHINESE.type
                    Units.LANG_EN, 0 -> DeviceLanguage.ENGLISH.type
                    Units.LANG_HINDI -> DeviceLanguage.HINDI.type
                    else -> DeviceLanguage.ENGLISH.type
                }
            )
        }

        fun parseDeviceUnits(units: Units): DeviceUnits {
            return DeviceUnits(
                 unitSystem = when (units.weight) {
                    Units.WEIGHT_UNIT_KG -> UnitSystem.METRIC.type
                    else -> UnitSystem.IMPERIAL.type
                }
            )
        }

        fun formatUnits(deviceUnits: DeviceUnits): Units {
            val unit = Units()

            if(deviceUnits.unitSystem?.lowercase() == UnitSystem.METRIC.type.lowercase()){
                unit.weight = Units.WEIGHT_UNIT_KG
                unit.dist = Units.DIST_UNIT_KM
                unit.temp = Units.TEMP_UNIT_C
            }else{
                unit.weight = Units.WEIGHT_UNIT_LB
                unit.dist = Units.DIST_UNIT_MI
                unit.temp = Units.TEMP_UNIT_F
            }

            return unit
        }

        fun formatSystemTime(calender: Calendar): SystemTime {
//            LOGS.d(
//                "noise_fit_event:colorfit_pro_2 : idleAlert:::" + Gson().toJson(calender) + ":" + calender.get(
//                    Calendar.AM_PM
//                )
//            )
            val systemTime = SystemTime()
            systemTime.year = calender.get(Calendar.YEAR)
            systemTime.monuth = calender.get(Calendar.MONTH) + 1
            systemTime.day = calender.get(Calendar.DAY_OF_MONTH)
            systemTime.hour = calender.get(Calendar.HOUR_OF_DAY)
            systemTime.minute = calender.get(Calendar.MINUTE)
            systemTime.second = calender.get(Calendar.SECOND)
            systemTime.week = calender.get(Calendar.WEEK_OF_YEAR)
            return systemTime
        }

        fun formatDialPlate(watchFace: WatchFace): DialPlate {
            //val faceId = watchFace.faceId.roundToInt()
            val faceId = watchFace.faceType
            val dialPlate = DialPlate()
            dialPlate.dial_id = faceId!!
            return dialPlate
        }

        fun formatMessageInfo(appNotification: AppNotification): NewMessageInfo {
            val messageInfo = NewMessageInfo()
            messageInfo.type = when (appNotification.appType) {
                ApplicationType.SMS.type -> NewMessageInfo.TYPE_SMS
                ApplicationType.CALENDAR.type -> NewMessageInfo.TYPE_CALENDAR
                ApplicationType.EMAIL.type -> NewMessageInfo.TYPE_EMAIL
                ApplicationType.TWITTER.type -> NewMessageInfo.TYPE_TWITTER
                ApplicationType.WHATS_APP.type -> NewMessageInfo.TYPE_WHATSAPP
                ApplicationType.LINKED_IN.type -> NewMessageInfo.TYPE_LINKEDIN
                ApplicationType.INSTAGRAM.type -> NewMessageInfo.TYPE_INSTAGRAM
                ApplicationType.FB_MESSENGER.type -> NewMessageInfo.TYPE_MESSENGER
                ApplicationType.SKYPE.type -> NewMessageInfo.TYPE_SKYPE
                ApplicationType.TWITTER.type -> NewMessageInfo.TYPE_TWITTER
                ApplicationType.FACEBOOK.type -> NewMessageInfo.TYPE_FACEBOOK
                ApplicationType.VIBER.type -> NewMessageInfo.TYPE_VIBER
                ApplicationType.GMAIL.type -> NewMessageInfo.TYPE_EMAIL
                ApplicationType.OUTLOOK.type -> NewMessageInfo.TYPE_EMAIL
                ApplicationType.SNAPCHAT.type -> NewMessageInfo.TYPE_SNAPCHAT
                ApplicationType.TELEGRAM.type -> NewMessageInfo.TYPE_TELEGRAM
                ApplicationType.YOUTUBE.type -> NewMessageInfo.TYPE_YOUTUBE
                ApplicationType.PINTEREST.type -> NewMessageInfo.TYPE_PINTEREST_YAHOO
                ApplicationType.SPORT_EVENT.type ->57
                ApplicationType.NOISEFIT.type -> 57
                //ApplicationType.SLACK.type -> NewMessageInfo.TYPE_SLACK
                else -> NewMessageInfo.TYPE_OTHER
            }
            if (messageInfo.type != 0) {
                messageInfo.name = appNotification.name
                messageInfo.number = appNotification.number
                messageInfo.content = appNotification.message
            }
            return messageInfo
        }


        fun formatMessageInfoV3(appNotification: AppNotification): V3MessageNotice {
            val messageInfo = V3MessageNotice()
            messageInfo.evtType = when (appNotification.appType) {
                ApplicationType.SMS.type -> V3MessageNotice.TYPE_SMS
                ApplicationType.CALENDAR.type -> V3MessageNotice.TYPE_CALENDAR
                ApplicationType.EMAIL.type -> V3MessageNotice.TYPE_EMAIL
                ApplicationType.TWITTER.type -> V3MessageNotice.TYPE_TWITTER
                ApplicationType.WHATS_APP.type -> V3MessageNotice.TYPE_WHATSAPP
                ApplicationType.LINKED_IN.type -> V3MessageNotice.TYPE_LINKEDIN
                ApplicationType.INSTAGRAM.type -> V3MessageNotice.TYPE_INSTAGRAM
                ApplicationType.FB_MESSENGER.type -> V3MessageNotice.TYPE_MESSENGER
                ApplicationType.SKYPE.type -> V3MessageNotice.TYPE_SKYPE
                ApplicationType.TWITTER.type -> V3MessageNotice.TYPE_TWITTER
                ApplicationType.FACEBOOK.type -> V3MessageNotice.TYPE_FACEBOOK
                ApplicationType.VIBER.type -> V3MessageNotice.TYPE_VIBER
                ApplicationType.GMAIL.type -> V3MessageNotice.TYPE_GMAIL
                ApplicationType.OUTLOOK.type -> V3MessageNotice.TYPE_EMAIL
                ApplicationType.SNAPCHAT.type -> V3MessageNotice.TYPE_SNAPCHAT
                ApplicationType.TELEGRAM.type -> V3MessageNotice.TYPE_TELEGRAM
                ApplicationType.YOUTUBE.type -> V3MessageNotice.TYPE_YOUTUBE
                ApplicationType.PINTEREST.type -> V3MessageNotice.TYPE_PINTEREST_YAHOO
                //ApplicationType.SLACK.type -> V3MessageNotice.TYPE_SLACK
                ApplicationType.SPORT_EVENT.type -> V3MessageNotice.TYPE_QQ
                else -> V3MessageNotice.TYPE_QQ
            }
            if (messageInfo.evtType != 0) {
                messageInfo.contact = appNotification.name
                messageInfo.phoneNumber = appNotification.number
                messageInfo.dataText = appNotification.message
            }
            return messageInfo
        }

        fun formatTime(colorFitDevice: ColorFitDevice?, timeFormat: TimeFormat): Units? {
            var unit = LocalDataManager.getUnits()
            colorFitDevice?.deviceType?.let { deviceType ->
                if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType || deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType
                    || deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType || deviceType == DeviceType.NOISEFIT_AGILE.deviceType
                ) {
                    if (unit == null) {
                        unit = Units()
                    }
                }
            }
            if (unit == null) {
                unit = Units()
            }
            timeFormat.timeFormat?.let {
                when (it.lowercase()) {
                    TimeFormats.HOURS_12.type.lowercase() -> {
                        unit.timeMode = Units.TIME_MODE_12
                    }
                    else -> {
                        unit.timeMode = Units.TIME_MODE_24
                    }
                }
            }

            return unit
        }

        fun parseTimeFormat(units: Units): TimeFormat {
            return TimeFormat(
                timeFormat = when (units.timeMode) {
                    Units.TIME_MODE_12 -> TimeFormats.HOURS_12.type
                    else -> TimeFormats.HOURS_24.type
                }
            )
        }

        fun parseWristSenseData(gesture: UpHandGesture?): WristLiftGesture {
            return WristLiftGesture(
                status = when (gesture?.onOff) {
                    UpHandGesture.STATE_ON -> true
                    else -> false
                }, startHour = gesture?.startHour ?: 0, startMinute = gesture?.startMinute ?: 0,
                endHour = gesture?.endHour ?: 0, endMinute = gesture?.endMinute ?: 0,
                displayDuration = gesture?.showSecond ?: 5
            )
        }

        fun formatWristSenseData(
            colorFitDevice: ColorFitDevice?,
            wristLiftGesture: WristLiftGesture
        ): UpHandGesture {
            val gesture = UpHandGesture()
            gesture.onOff = when (wristLiftGesture.status) {
                true -> UpHandGesture.STATE_ON
                else -> UpHandGesture.STATE_OFF
            }
            colorFitDevice?.deviceType?.let { deviceType ->
                if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType || deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType || deviceType == DeviceType.COLORFIT_PRO_2.deviceType
                    || deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType || deviceType == DeviceType.NOISEFIT_AGILE.deviceType
                ) {
                    gesture.startHour = 0
                    gesture.startMinute = 0
                    gesture.endHour = 23
                    gesture.endMinute = 59
                    gesture.showSecond = 5
                } else {
                    gesture.startHour = wristLiftGesture.startHour
                    gesture.startMinute = wristLiftGesture.startMinute
                    gesture.endHour = wristLiftGesture.endHour
                    gesture.endMinute = wristLiftGesture.endMinute
                    gesture.showSecond = wristLiftGesture.displayDuration
                }
            }

            return gesture
        }

        fun parseStressData(
            p0: HealthPressure?,
            p1: MutableList<HealthPressureItem>?
        ): List<StressDataBreakup>? {
            val stressArray = ArrayList<StressDataBreakup>()
            p0?.let { it ->

//                val stressData = StressData(date = DateFormats.getDateFormat(it.date))


                var totalOffset = it.startTime
                p1?.let { it1 ->

                    it1.forEach { item ->
                        totalOffset += item.offset
                        val dateTime = Calendar.getInstance()
                        dateTime.time = item.date
                        dateTime.add(Calendar.MINUTE, totalOffset)
                        LOGS.d("spo2_history")
                        val date = DateFormats.dateFormat.format(dateTime.time)
                        val time = DateFormats.timeFormat.format(dateTime.time)
                        val timeStamp = DateFormats.convertDateTimeToTimeStamp(date, time)
                        val stressItem = StressDataBreakup(
                            value = item.value,
                            date = date, time = time, timeStamp = timeStamp
                        )
                        stressArray.add(stressItem)
                    }

//                    stressData.value = it1.get(it1.size - 1).value
                }

//                stressData.stressArray = stressArray

            }
            return stressArray
        }

        fun parseOxygenData(
            p0: HealthSpO2?,
            p1: MutableList<HealthSpO2Item>?
        ): List<BloodOxygenBreakup>? {
            val oxygenArray = ArrayList<BloodOxygenBreakup>()
            p0?.let { it ->
                // val oxygenData = BloodOxygen(date = DateFormats.getDateFormat(it.date))


                var totalOffset = it.startTime
                p1?.let { it1 ->

                    it1.forEach { item ->
                        totalOffset += item.offset
                        val dateTime = Calendar.getInstance()
                        dateTime.time = item.date
                        dateTime.add(Calendar.MINUTE, totalOffset)
                        LOGS.d("spo2_history")
                        val date = DateFormats.dateFormat.format(dateTime.time)
                        val time = DateFormats.timeFormat.format(dateTime.time)
                        val timeStamp = DateFormats.convertDateTimeToTimeStamp(date, time)
                        val oxygenItem = BloodOxygenBreakup(
                            value = item.value,
                            date = date, time = time, timeStamp = timeStamp
                        )
                        oxygenArray.add(oxygenItem)
                    }

//                    oxygenData.bloodOxygen = it1.get(it1.size - 1).value
                }

//                oxygenData.bloodOxygenArray = oxygenArray

            }
            return oxygenArray
        }

        fun parseSportsModeInfo(modes: QuickSportMode): SportsModeList {
            val sportsModeList = ArrayList<SportsModeList.SportsMode>()
            if (modes != null) {
                var i = 1
                if (modes.sport_type0_run) {
                    val sportMode =
                        SportsModeList.SportsMode(i, "Running", SportType.SPORT_TYPE_RUN, true)
                    sportsModeList.add(sportMode)
                    i++
                }

                if (modes.sport_type0_walk) {
                    val sportMode =
                        SportsModeList.SportsMode(i, "Walking", SportType.SPORT_TYPE_WALK, true)
                    sportsModeList.add(sportMode)
                    i++
                }

                if (modes.sport_type1_spinning) {
                    val sportMode =
                        SportsModeList.SportsMode(i, "Spinning", SportType.SOPRT_TYPE_HIIT, true)
                    sportsModeList.add(sportMode)
                    i++
                }

                if (modes.sport_type0_on_foot) {
                    val sportMode =
                        SportsModeList.SportsMode(i, "Hiking", SportType.SPORT_TYPE_ONFOOT, true)
                    sportsModeList.add(sportMode)
                    i++
                }

                if (modes.sport_type2_yoga) {
                    val sportMode =
                        SportsModeList.SportsMode(i, "Yoga", SportType.SPORT_TYPE_YOGA, true)
                    sportsModeList.add(sportMode)
                    i++
                }

                if (modes.sport_type1_fitness) {
                    val sportMode =
                        SportsModeList.SportsMode(i, "Workout", SportType.SPORT_TYPE_FITNESS, true)
                    sportsModeList.add(sportMode)
                    i++
                }

                if (modes.sport_type0_by_bike) {
                    val sportMode =
                        SportsModeList.SportsMode(i, "Biking", SportType.SPORT_TYPE_CYCLING, true)
                    sportsModeList.add(sportMode)
                    i++
                }

                if (modes.sport_type0_mountain_climbing) {
                    val sportMode =
                        SportsModeList.SportsMode(i, "Climbing", SportType.SPORT_TYPE_CLIMB, true)
                    sportsModeList.add(sportMode)
                    i++
                }

                if (modes.sport_type1_treadmill) {
                    val sportMode = SportsModeList.SportsMode(
                        i,
                        "Treadmill",
                        SportType.SPORT_TYPE_TREADMILL,
                        true
                    )
                    sportsModeList.add(sportMode)
                    i++
                }
            }
            return SportsModeList(sportsModes = sportsModeList)
        }

        fun parseSportsModeInfoV3(functionInfo: SportModeSortV3?): SportsModeList {
            val sportsModeList = ArrayList<SportsModeList.SportsMode>()
            functionInfo?.item?.let {
                it.forEachIndexed { i, item ->
                    val sportMode = SportsModeList.SportsMode(
                        i, when (item.type) {
                            SportType.SOPRT_TYPE_OUTDOOR_RUN -> SportActivityName.OUTDOOR_RUNNING
                            SportType.SPORT_TYPE_RUN -> SportActivityName.RUNNING
                            SportType.SPORT_TYPE_CYCLING -> SportActivityName.BICYCLING
                            SportType.SPORT_TYPE_CLIMB -> SportActivityName.CLIMBING
                            SportType.SPORT_TYPE_TREADMILL -> SportActivityName.TREADMILL
                            SportType.SPORT_TYPE_YOGA -> SportActivityName.YOGA
                            SportType.SPORT_TYPE_FITNESS -> SportActivityName.WORKOUT
                            SportType.SOPRT_TYPE_ROWER -> SportActivityName.ROWER
                            SportType.SOPRT_TYPE_ELLIPTICAL -> SportActivityName.ELLIPTICAL
                            SportType.SPORT_TYPE_BASKETBALL -> SportActivityName.BASKETBALL
                            SportType.SPORT_TYPE_SOCKER -> SportActivityName.FOOTBALL
                            SportType.SPORT_TYPE_TENNISBALL -> SportActivityName.TENNIS
                            SportType.SPORT_TYPE_DANCING -> SportActivityName.DANCE
                            SportType.SPORT_TYPE_BADMINTON -> SportActivityName.BADMINTON
                            SportType.SPORT_TYPE_SWIM -> SportActivityName.SWIMMING
                            SportType.SOPRT_TYPE_OUTDOOR_WALK -> SportActivityName.OUTDOOR_WALKING
                            SportType.SPORT_TYPE_DYNAMIC -> SportActivityName.SPINNING
                            SportType.SPORT_TYPE_ONFOOT -> SportActivityName.HIKING
                            SportType.SOPRT_TYPE_OUTDOOR_CYCLE -> SportActivityName.OUTDOOR_CYCLING
                            SportType.SOPRT_TYPE_INDOOR_WALK -> SportActivityName.INDOOR_WALKING
                            SportType.SOPRT_TYPE_INDOOR_RUN -> SportActivityName.INDOOR_RUNNING
                            SportType.SOPRT_TYPE_INDOOR_CYCLE -> SportActivityName.INDOOR_CYCLING
                            SportType.SPORT_TYPE_CRICKET -> SportActivityName.CRICKET
                            SportType.SOPRT_TYPE_POOL_SWIM -> SportActivityName.POOL_SWIMMING
                            SportType.SOPRT_TYPE_WATER_SWIM -> SportActivityName.OPEN_WATER_SWIMMING
                            SportType.SPORT_TYPE_OTHER -> SportActivityName.WORKOUT
                            else -> SportActivityName.WALKING
                        }, item.type, true
                    )

                    sportsModeList.add(sportMode)
                }
            }

            return SportsModeList(sportsModes = sportsModeList)
        }

        fun formatSportModeV3(data: SportsModeList): List<SportModeSortV3.SportModeSortItemV3> {
            val list = ArrayList<SportModeSortV3.SportModeSortItemV3>()
            data.sportsModes?.let {
                it.forEachIndexed { i, item ->
                    val mode = SportModeSortV3.SportModeSortItemV3()
                    mode.index = i + 1
                    mode.type = item.type!!
                    list.add(mode)
                }
            }
            return list
        }

        fun formatSportMode(data: SportsModeList): QuickSportMode {
            val quickSportMode = QuickSportMode()
            data.sportsModes?.let {
                it.forEachIndexed { i, item ->
                    when (item.type) {
                        SportType.SPORT_TYPE_RUN -> quickSportMode.sport_type0_run = true
                        SportType.SPORT_TYPE_WALK -> quickSportMode.sport_type0_walk = true
                        SportType.SOPRT_TYPE_HIIT -> quickSportMode.sport_type1_spinning = true
                        SportType.SPORT_TYPE_ONFOOT -> quickSportMode.sport_type0_on_foot = true
                        SportType.SPORT_TYPE_YOGA -> quickSportMode.sport_type2_yoga = true
                        SportType.SPORT_TYPE_FITNESS -> quickSportMode.sport_type1_fitness = true
                        SportType.SPORT_TYPE_CYCLING -> quickSportMode.sport_type0_by_bike = true
                        SportType.SPORT_TYPE_CLIMB -> quickSportMode.sport_type0_mountain_climbing =
                            true
                        SportType.SPORT_TYPE_TREADMILL -> quickSportMode.sport_type1_treadmill =
                            true
                    }
                }
            }
            return quickSportMode
        }

    }
}