package com.noisefit.hybrid.dataconversions

import cn.appscomm.bluetoothsdk.app.SettingType
import cn.appscomm.bluetoothsdk.model.RealTimeSportData
import cn.appscomm.bluetoothsdk.model.ReminderData
import cn.appscomm.bluetoothsdk.model.ReminderExData
import cn.appscomm.bluetoothsdk.model.SportData
import com.google.gson.Gson
import com.noisefit_commans.constants.SportActivityName
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.pow


class DataConverter
@Inject constructor(

) {


    fun getParseDays(data: Int): Int {
        val result = ArrayList<Boolean>()
        result.addAll(listOf(false, false, false, false, false, false, false))
        for (i in 0..6) {
            if (data.and(1 shl i) > 0) {
                result[i] = true
            }
        }

        return booleanArrayToBinStr(result)
    }

    private fun booleanArrayToBinStr(list: List<Boolean>?): Int {
        var result = 0
        for (i in 6 downTo 0) {
            if (list != null) {
                if (list[i]) {
                    result += 2.0.pow((6 - i).toDouble()).toInt()
                }
            }
        }
        return result
    }

    fun getWeatherCode(weatherType: String?): Int {
        if (weatherType == null) {
            return 32
        }
        when (weatherType.lowercase()) {
            "clear" -> {
                return 32 //Calm (daytime)
            }
            "clouds" -> {
                return 26 //Partly Cloudy
            }
            "thunderstorm" -> {
                return 3 // shower
            }
            "drizzle" -> {
                return 9 //Drizzle
            }
            "rain" -> {
                return 10 //Shower
            }
            "snow" -> {
                return 16 //Snowfall
            }
            "haze" -> {
                return 21 //Haze
            }
            "smoke" -> {
                return 22 //Smoke
            }
            "dust" -> {
                return 19 //Dust
            }
            "fog" -> {
                return 20 //Fog
            }
            "sand" -> {
                return 19 //Dust
            }
            "tornado" -> {
                return 0 //Tornado
            }

            else -> {
                return 32 //(253 for unknown type currently set to clear)
            }
        }

    }

    fun toParseDay(my_byte: Int): Int {
        val daysList = binstrToBooleanArray(my_byte)

        //  print(daysList)
        var interval = 0x00
        daysList.forEachIndexed { index, b ->

            if (index == 0 && b) {
                interval = interval.or(0x01)
//                     print(interval)
            } else if (index == 1 && b) {
                interval = interval.or(0x02)
                // print(interval)
            } else if (index == 2 && b) {
                interval = interval.or(0x04)

            } else if (index == 3 && b) {
                interval = interval.or(0x08)

            } else if (index == 4 && b) {
                interval = interval.or(0x10)

            } else if (index == 5 && b) {
                interval = interval.or(0x20)

            } else if (index == 6 && b) {
                interval = interval.or(0x40)
                // print(interval)

            }
        }

        return interval
    }

    private fun binstrToBooleanArray(my_byte: Int): List<Boolean> {
        val result = ArrayList<Boolean>()
        result.addAll(listOf(false, false, false, false, false, false, false))
        val binStr = ByteArray(1)
        binStr[0] = my_byte.toByte()
        val byteStr: String = binary(binStr)
        for (i in byteStr.length - 1 downTo 0) {
            result[i + 7 - byteStr.length] = Integer.valueOf(byteStr.substring(i, i + 1)) == 1
        }
        return result
    }


    fun formatAlarmData(p1: Array<out Any>?): AlarmsList {
        val alarmsList = ArrayList<AlarmsList.Alarm>()

        (p1?.get(0) as LinkedList<*>).forEach { item ->
            val alarmItem = item as ReminderData

            if (item.type != SettingType.REMINDER_CUSTOM) {
                alarmsList.add(
                    AlarmsList.Alarm(
                        hour = alarmItem.hour,
                        minute = alarmItem.min,
                        status = alarmItem.status,
                        repeatDays = (AlarmsList.binaryStrToBooleanArrayHybrid(alarmItem.cycle) as ArrayList).apply {
                            add(0, alarmItem.status)
                        },
                        id = alarmItem.id,
                        alarmAction = AlarmAction.ALARM_CHANGE
                    )
                )
            }
        }
        return AlarmsList(alarms = alarmsList)
    }

    fun formatNavAlarmData(p1: Array<out Any>?): AlarmsList {
        val alarmsList = ArrayList<AlarmsList.Alarm>()

        (p1?.get(0) as LinkedList<*>).forEach { item ->
            val alarmItem = item as ReminderExData
            val time = item.timeList.first()
            alarmsList.add(
                AlarmsList.Alarm(
                    hour = time.hour,
                    minute = time.min,
                    status = alarmItem.enable,
                    repeatDays = AlarmsList.binaryStrToBooleanArray(alarmItem.cycle),
                    id = alarmItem.id,
                    alarmAction = AlarmAction.ALARM_CHANGE
                )
            )
        }




        return AlarmsList(alarms = alarmsList)
    }

    fun formatReminderData(p1: Array<out Any>?): ReminderList {
        val alarmsList = ArrayList<ReminderList.Reminder>()

        (p1?.get(0) as LinkedList<*>).forEach { item ->

            val alarmItem = item as ReminderData
            if (item.type == SettingType.REMINDER_CUSTOM) {
                val remin = ReminderList.Reminder(
                    hour = item.hour,
                    minute = item.min,
                    id = alarmItem.id,
                    repeatMode = when (item.repeatValue) {
                        0 -> "Never"
                        1 -> "Every Day"
                        2 -> "Every Week"
                        3 -> "Every Month"
                        4 -> "Every Year"
                        else -> "Never"

                    },
                    year = item.year,
                    month = item.month,
                    day = item.day,
                    label = item.content
                )
                alarmsList.add(remin)
            }

        }
        return ReminderList(reminders = alarmsList)
    }

    fun formatNavReminderData(p1: Array<out Any>?): ReminderList {
        val alarmsList = ArrayList<ReminderList.Reminder>()

        (p1?.get(0) as LinkedList<*>).forEach { item ->

            val alarmItem = item as ReminderExData
            //if(item.type == 7 && item.customType != "Alarm"){
            val time = item.timeList.first()
            val repeat = item.repeat
            val remin = ReminderList.Reminder(
                hour = time.hour,
                minute = time.min,
                id = alarmItem.id,
                repeatMode = when (repeat.value) {
                    0 -> "Never"
                    1 -> "Every Day"
                    2 -> "Every Week"
                    3 -> "Every Month"
                    4 -> "Every Year"
                    else -> "Never"

                },
                year = item.date.year,
                month = item.date.month,
                day = item.date.day,
                label = item.customType
            )
            alarmsList.add(remin)
            //}

        }
        return ReminderList(reminders = alarmsList)
    }

    fun formatVisionReminderData(p1: Array<out Any>?): ReminderList {
        val alarmsList = ArrayList<ReminderList.Reminder>()

        (p1?.get(0) as LinkedList<*>).forEach { item ->

            val alarmItem = item as ReminderExData
            //if(item.type == 7 && item.customType != "Alarm"){
            val time = item.timeList.first()
            val repeat = item.repeat
            val remin = ReminderList.Reminder(
                hour = time.hour,
                minute = time.min,
                id = alarmItem.id,
                repeatMode = when (repeat.value) {
                    0 -> "Never"
                    1 -> "Every Day"
                    2 -> "Every Week"
                    3 -> "Every Month"
                    4 -> "Every Year"
                    else -> "Never"

                },
                repeatDays = AlarmsList.binaryStrToBooleanArrayVision(item.cycle),
                label = item.customType,
                day = 0,
                month = 0,
                year = 0
            )
            alarmsList.add(remin)
            //}

        }
        return ReminderList(reminders = alarmsList)
    }

    fun getStepsData(p1: Array<out Any>?): StepsData {
        val dailyStepData = StepsData(date = DateFormats.getTodaysDateString(7))
        val stepArray = ArrayList<StepsData.StepDataBreakup>()
        for (i in 0..23) {
            stepArray.add(StepsData.StepDataBreakup(0, 0, 0, 0, i))
        }
        p1?.let {

            (p1[0] as LinkedList<SportData>).filter { sportData ->
                isSelectedDate(
                    sportData.timestamp
                )
            }
                .groupBy { sportData ->
                    return@groupBy getCalenderTime(
                        sportData.timestamp
                    ).get(Calendar.HOUR_OF_DAY)
                }
                .forEach { map ->
                    val stepData = StepsData.StepDataBreakup(hourOfTheDay = map.key)
                    map.value.forEach { oldBreakup ->
                        stepData.steps += oldBreakup.step
                        stepData.calories += oldBreakup.calories / 1000
                        stepData.distance += oldBreakup.distance
                        stepData.activeTime += oldBreakup.sportTime
                    }
                    stepArray.add(map.key, stepData)
                    dailyStepData.totalSteps += stepData.steps
                    dailyStepData.totalCalories += stepData.calories
                    dailyStepData.totalDistance += stepData.distance
                    dailyStepData.totalActiveTime += stepData.activeTime
                }

            dailyStepData.totalCalories = dailyStepData.totalCalories

        }

        dailyStepData.stepArray = stepArray



        return dailyStepData
    }


    private fun getCalenderTime(timeStamp: Long): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeStamp * 1000
        return calendar
    }

    fun parseSportsData(p1: Array<out Any>?): SportsModeRequestList {
        val sportsModeRequestList = SportsModeRequestList(responseType = "final")
        val activities = ArrayList<SportsModeResponse>()


        p1?.let {
            (p1[0] as LinkedList<*>).forEach { item ->
                val sportsData = item as RealTimeSportData

                val startCalendar = Calendar.getInstance()
                startCalendar.timeInMillis = sportsData.startTimeStamp * 1000

                val endCalendar = Calendar.getInstance()
                endCalendar.timeInMillis = sportsData.endTimeStamp * 1000

                val duration = (sportsData.endTimeStamp - sportsData.startTimeStamp).toInt()
                LOGS.d("Sports------type ${sportsData.type}")
                val sportsModeResponse = SportsModeResponse(
                    type = when (sportsData.type) {
                        0 -> SportActivityName.WORKOUT
                        1 -> SportActivityName.WALKING
                        2 -> SportActivityName.RUNNING
                        3 -> SportActivityName.WARM_UP_EXERCISE
                        4 -> SportActivityName.POOL_SWIMMING
                        5 -> SportActivityName.OUTDOOR_CYCLING

                        7 -> SportActivityName.MOUNTAINEERING

                        10 -> SportActivityName.INDOOR_CYCLING
                        11 -> SportActivityName.WEIGHTLIFTING
                        12 -> SportActivityName.MIXED_AEROBICS
                        13 -> SportActivityName.INDOOR_WALKING
                        14 -> SportActivityName.INDOOR_RUNNING
                        15 -> SportActivityName.YOGA
                        16 -> SportActivityName.STRENGTH_TRAINING
                        17 -> SportActivityName.ELLIPTICAL
                        18 -> SportActivityName.STAIR_STEPPER
                        19 -> SportActivityName.DANCE
                        20 -> SportActivityName.BADMINTON
                        21 -> SportActivityName.BASKETBALL
                        22 -> SportActivityName.FREE_EXERCISE
                        23 -> SportActivityName.HIKING
                        24 -> SportActivityName.CROSS_COUNTRY
                        25 -> SportActivityName.ROWING_MACHINE
                        26 -> SportActivityName.CLIMBING
                        27 -> SportActivityName.FOOTBALL
                        28 -> SportActivityName.PINGPONG
                        29 -> SportActivityName.RUGBY
                        30 -> SportActivityName.TENNIS
                        31 -> SportActivityName.VOLLEYBALL
                        32 -> SportActivityName.WRESTLING
                        33 -> SportActivityName.BOXING
                        34 -> SportActivityName.PILATES

                        35 -> SportActivityName.HIPHOP

                        36 -> SportActivityName.GOLF
                        37 -> SportActivityName.SURFING
                        38 -> SportActivityName.CANOEING
                        39 -> SportActivityName.ICE_SKATING
                        40 -> SportActivityName.TREADMILL
                        41 -> SportActivityName.HUNTING
                        42 -> SportActivityName.FISHING
                        43 -> SportActivityName.SKATEBOARDING
                        44 -> SportActivityName.KARATE
                        45 -> SportActivityName.ROPE_SKIPPING
                        46 -> SportActivityName.CRICKET
                        47 -> SportActivityName.STRETCHING

                        48 -> SportActivityName.JUDO
                        49 -> SportActivityName.MARTIAL_ARTS
                        else -> SportActivityName.WORKOUT
                    },
                    calories = (sportsData.calories / 1000).toLong(),
                    heartRateAvg = sportsData.heartRateAvg,
                    endTime = DateFormats.timeFormat.format(endCalendar.time),
                    date = DateFormats.dateFormat.format(startCalendar.time),
                    /* time = DateFormats.getFormattedTime(startCalendar.time),*/
                    time = DateFormats.dateTimeFormatISO.format(startCalendar.time),
                    duration = duration.toLong()
                )

                if (sportsData.step != 0) {
                    sportsModeResponse.steps = sportsData.step
                    sportsModeResponse.cadence =
                        (sportsData.step / duration) * 60
                }


                sportsModeResponse.distance = sportsData.distance.toLong()
                sportsModeResponse.pace = (sportsData.pace).toFloat()

                if (sportsData.distance != 0 && duration != 0) {
                    sportsModeResponse.speed =
                        (sportsData.distance / duration).toFloat()
                } else {
                    sportsModeResponse.speed = 0F
                    sportsModeResponse.cadence = 0
                }

                activities.add(sportsModeResponse)
            }
        }

        LOGS.d("Activity data $activities")
        sportsModeRequestList.activities = activities
        return sportsModeRequestList
    }

    private fun parseGpsMapsData(mapsData: List<GPSDataResponse>?): ArrayList<DoubleArray> {
        val gpsList = ArrayList<DoubleArray>()
        var lastLat = 0.0
        var lastLog = 0.0
        mapsData?.forEach {
            val dataList = ArrayList<Double>()
            val lat = it.latitude
            val log = it.longitude
            if (lastLat != lat && lastLog != log) {
                dataList.add(lat)
                dataList.add(log)
                gpsList.add(dataList.toDoubleArray())
                lastLat = lat
                lastLog = log
            }
        }
        return gpsList
    }

    fun parseSportsDataGPS(
        p1: Array<out Any>?,
        p2: List<List<GPSDataResponse>>
    ): SportsModeListGPS {
        val sportsModeList = SportsModeListGPS(responseType = "final")
        val activities = ArrayList<SportsModeResponse>()

        p1?.let {
            var index = -1
            (p1[0] as LinkedList<*>).forEach { item ->
                val sportsData = item as RealTimeSportData

                index += 1
                val startCalendar = Calendar.getInstance()
                //startCalendar.timeInMillis = (sportsData.startTimeStamp + 9000) * 1000
                startCalendar.timeInMillis = (sportsData.startTimeStamp) * 1000
                val endCalendar = Calendar.getInstance()
                //endCalendar.timeInMillis = (sportsData.endTimeStamp + 9000) * 1000
                endCalendar.timeInMillis = (sportsData.endTimeStamp) * 1000

                val duration = (sportsData.endTimeStamp - sportsData.startTimeStamp).toInt()

                val sportsModeResponse = SportsModeResponse(
                    type = when (sportsData.type) {
                        0 -> SportActivityName.WORKOUT
                        1 -> SportActivityName.OUTDOOR_WALKING
                        2 -> SportActivityName.OUTDOOR_RUNNING
                        3 -> SportActivityName.WARM_UP_EXERCISE
                        4 -> SportActivityName.POOL_SWIMMING
                        5 -> SportActivityName.OUTDOOR_CYCLING

                        7 -> SportActivityName.MOUNTAINEERING

                        10 -> SportActivityName.INDOOR_CYCLING
                        11 ->SportActivityName.WEIGHT_TRAINING
                        12 -> SportActivityName.MIXED_AEROBICS
                        13 -> SportActivityName.INDOOR_WALKING
                        14 -> SportActivityName.INDOOR_RUNNING
                        15 -> SportActivityName.YOGA
                        16 -> SportActivityName.STRENGTH_TRAINING
                        17 -> SportActivityName.ELLIPTICAL_TRAINING
                        18 -> SportActivityName.STAIR_STEPPER
                        19 -> SportActivityName.DANCE
                        20 -> SportActivityName.BADMINTON
                        21 -> SportActivityName.BASKETBALL
                        22 -> SportActivityName.FREE_EXERCISE
                        23 -> SportActivityName.HIKING
                        24 -> SportActivityName.CROSS_COUNTRY
                        25 -> SportActivityName.ROWING_MACHINE
                        26 -> SportActivityName.CLIMBING
                        27 -> SportActivityName.FOOTBALL
                        28 -> SportActivityName.PINGPONG
                        29 -> SportActivityName.RUGBY
                        30 -> SportActivityName.TENNIS
                        31 -> SportActivityName.VOLLEYBALL
                        32 -> SportActivityName.WRESTLING
                        33 -> SportActivityName.BOXING
                        34 -> SportActivityName.PILATES

                        35 -> SportActivityName.HIPHOP

                        36 -> SportActivityName.GOLF
                        37 -> SportActivityName.SURFING
                        38 -> SportActivityName.CANOEING
                        39 -> SportActivityName.ICE_SKATING
                        40 -> SportActivityName.TREADMILL
                        41 -> SportActivityName.HUNTING
                        42 -> SportActivityName.FISHING
                        43 -> SportActivityName.SKATEBOARDING
                        44 -> SportActivityName.KARATE
                        45 -> SportActivityName.ROPE_SKIPPING
                        46 -> SportActivityName.CRICKET
                        47 -> SportActivityName.STRETCHING

                        48 -> SportActivityName.JUDO
                        49 -> SportActivityName.MARTIAL_ARTS
                        else -> SportActivityName.WORKOUT
                    },
                    calories = (sportsData.calories / 1000).toLong(),
                    heartRateAvg = sportsData.heartRateAvg,
                    endTime = DateFormats.timeFormat.format(endCalendar.time),
                    date = DateFormats.dateFormat.format(startCalendar.time),
                    time = DateFormats.formatDateTime(
                        startCalendar.time,
                        DateFormats.dateTimeFormatISO
                    ),
                    duration = duration.toLong()
                )

                val gpsParseData = parseGpsMapsData(p2[index])

                //LOGS.d("gpsParseData ${Gson().toJson(gpsParseData)}")
                if (gpsParseData.isEmpty()) {
                    sportsModeResponse.gpsCoordinate = null
                } else {
                    sportsModeResponse.gpsCoordinate = listToJson(gpsParseData)
                }


                sportsModeResponse.distance = sportsData.distance.toLong()
                sportsModeResponse.pace = (sportsData.pace).toFloat()


                if (sportsData.step != 0) {
                    sportsModeResponse.steps = sportsData.step
                    sportsModeResponse.cadence = (sportsData.step / duration) * 60
                }

                if (sportsData.distance != 0 && duration != 0) {
                    val df = DecimalFormat("#.#")
                    val speed = sportsData.distance / duration
                    sportsModeResponse.speed = df.format(speed)?.toFloat()
                } else {
                    sportsModeResponse.speed = 0F
                    sportsModeResponse.cadence = 0
                }


                activities.add(sportsModeResponse)
            }
        }

        /*p2?.let {
            val data = p2[0] as LongSparseArray<LinkedList<GPSData>>
            for (i in 0..data.size()-1) {
                val key = data.keyAt(i)
                (data.get(key) as LinkedList<GPSData>).forEach { item ->
                    val gpsData = item as GPSData

                    val gpsDataResponse = GPSDataResponse(latitude = gpsData.latitude ,longitude = gpsData.longitude)

                    gps_data.add(gpsDataResponse)
                }
                all_gps_data.add(gps_data)
            }
        }*/
        sportsModeList.activities = activities
        sportsModeList.gpsData = null
        return sportsModeList
    }

    fun getSleepData(p1: Array<out Any>?): SleepData {
        val calendar = Calendar.getInstance()
        calendar.time = DateFormats.dateFormat.parse(DateFormats.getDateFormat())
        val sleepDate = calendar
        val sleepData = SleepData(availableSleepTypes = "deep;light;awake")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", DateFormats.defaultLocale)

        p1?.let {

            var totalDeep = 0
            var totalLight = 0
            var totalAwake = 0

            (p1[0] as LinkedList<*>).forEach { item ->
                val sleepData = item as cn.appscomm.bluetoothsdk.model.SleepData
                val selectedDate = Calendar.getInstance()
                selectedDate.time = dateFormat.parse(sleepData.date)
                if (isSelectedDate(selectedDate, sleepDate)) {
                    totalDeep += sleepData.deep
                    totalLight += sleepData.light
                    totalAwake += sleepData.awake
                }
            }

            sleepData.light = totalLight
            sleepData.deep = totalDeep
            sleepData.awake = totalAwake
            sleepData.total = totalLight + totalDeep + totalAwake
            sleepData.date = DateFormats.dateFormat.format(sleepDate?.time)
        }
        return sleepData
    }

    private fun isSelectedDate(selectedDate: Calendar, stepDate: Calendar?): Boolean {
        return stepDate?.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR) &&
                stepDate.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
    }

    private fun isSelectedDate(timeStamp: Long): Boolean {
        return DateFormats.convertTimestampToDate(
            timeStamp * 1000,
            DateFormats.dateFormat
        ) == DateFormats.getDateFormat()
    }

    private fun listToJson(value: ArrayList<DoubleArray>) = Gson().toJson(value)




    fun getArrayFromInt(my_byte: Int): IntArray {
        val result = intArrayOf(0, 0, 0, 0, 0, 0, 0)//ArrayList<Int>()
        val binStr = ByteArray(1)
        binStr[0] = my_byte.toByte()
        val byteStr: String = binary(binStr)
        for (i in byteStr.length - 1 downTo 0) {
            result[i + 7 - byteStr.length] = Integer.valueOf(byteStr.substring(i, i + 1))
        }

        return result
    }


    private fun binary(bytes: ByteArray?): String {
        return BigInteger(1, bytes).toString(2)
    }

    fun getTimeZoneOffset(): Triple<Int, Int, Int> {
        val offsetInMillis = TimeZone.getDefault()
            .getOffset(GregorianCalendar.getInstance(TimeZone.getDefault()).timeInMillis)
        val timeZoneHourOffset = abs(offsetInMillis / 3600000)
        val timeZoneMinuteOffset = abs(offsetInMillis / 60000 % 60)
        var timeZoneUnit = 1
        if (timeZoneHourOffset < 0) {
            timeZoneUnit = 0
        }
        return Triple(timeZoneUnit, timeZoneHourOffset, timeZoneMinuteOffset)
    }


}
