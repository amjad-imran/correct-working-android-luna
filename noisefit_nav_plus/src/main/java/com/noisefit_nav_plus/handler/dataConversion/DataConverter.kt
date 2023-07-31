package com.noisefit_nav_plus.handler.dataConversion

import android.content.Context
import com.google.gson.Gson
import com.noisefit_commans.common.handleCaloriesData
import com.noisefit_commans.common.handleHrData
import com.noisefit_commans.constants.SportActivityName
import com.noisefit_commans.models.*
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.utils.AppConversionUtils
import com.noisefit_commans.utils.DateFormats
import com.zjw.zhbraceletsdk.bean.*
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.floor

class DataConverter
@Inject
constructor(
    var context: Context,
    var gson: Gson
) {

    fun parseBodyTemperature(p0: MutableList<MeasureTempInfo>?): ArrayList<BodyTemperatureBreakup> {
        val bodyTemperatureList = ArrayList<BodyTemperatureBreakup>()
        p0?.forEach { item ->
            val date = DateFormats.getConvertToDateFormat(
                item.measureTime,
                DateFormats.dateTimeFormat5,
                DateFormats.dateFormat
            )
            val time = DateFormats.getConvertToDateFormat(
                item.measureTime,
                DateFormats.dateTimeFormat5,
                DateFormats.timeFormat
            )

            val syncDate = DateFormats.convertDateTimeToTimeStamp(date!!, time!!)
            bodyTemperatureList.add(
                BodyTemperatureBreakup(
                    date = date, time = time,
                    value = AppConversionUtils.getCentigradeBody(item.measureWristTemp.toInt()),
                    timeStamp = syncDate
                )
            )
        }


        return bodyTemperatureList
    }

    fun formatAlarmData(p1: ArrayList<AlarmInfo>): AlarmsList {
        val alarmsList = ArrayList<AlarmsList.Alarm>()
        p1.forEach { alarmItem ->
            alarmsList.add(
                AlarmsList.Alarm(
                    hour = alarmItem.alarmtHour,
                    minute = alarmItem.alarmtMin,
                    status = true,
                    repeatDays = binaryStrToBooleanArray(alarmItem.alarmtData),
                    id = alarmItem.alarmId
                )
            )
        }
//        timberLogs.debugMessage("")
        return AlarmsList(alarms = alarmsList)
    }

    private fun binaryStrToBooleanArray(my_byte: Int): List<Boolean> {
        val result = ArrayList<Boolean>()
        result.addAll(listOf(false, false, false, false, false, false, false, false))
        val binStr = ByteArray(1)
        binStr[0] = my_byte.toByte()
        val byteStr: String = binary(binStr, 2)
        for (i in byteStr.length - 1 downTo 0) {
            result[i + 8 - byteStr.length] = Integer.valueOf(byteStr.substring(i, i + 1)) == 1
        }

        val subList = ArrayList(result.subList(1, result.size))
        subList.reverse()
        subList.add(0, result[0])
        return subList
    }

    private fun binary(bytes: ByteArray?, radix: Int): String {
        return BigInteger(1, bytes).toString(radix)
    }


    fun getStepsData(mMotionInfo: MotionInfo): StepsData {
        val dailyStepData = StepsData(
            date = DateFormats.getConvertToDateFormat(
                mMotionInfo.motionDate,
                DateFormats.dateFormat3,
                DateFormats.dateFormat
            )
        )
        dailyStepData.totalSteps = mMotionInfo.motionStep
        dailyStepData.totalCalories = floor(mMotionInfo.motionCalorie.toDouble()).toInt()
        dailyStepData.totalDistance = (mMotionInfo.motionDistance * 1000).toInt()

        val stepArray = ArrayList<StepsData.StepDataBreakup>()
        var stepLength = 0f
        if (mMotionInfo.motionStep != 0 && mMotionInfo.motionDistance != 0f) {
            stepLength = ((mMotionInfo.motionDistance * 1000)) / mMotionInfo.motionStep
        }
        mMotionInfo.motionData?.forEachIndexed { i, e ->
            val stepData = StepsData.StepDataBreakup(hourOfTheDay = i)
            stepData.steps = e as Int
            stepData.distance = (stepLength * e).toInt()

            stepArray.add(stepData)
        }
        dailyStepData.stepArray = stepArray

        //LOGS.d(stepArray)
        //  LOGS.d("Daily_steps ${Gson().toJson(stepArray)}")
        return dailyStepData
    }

    fun getSleepData(mSleepInfo: SleepInfo?): SleepData {

        val sleepData = SleepData(availableSleepTypes = "deep;light;awake;rem")

        val sleepArray = ArrayList<SleepData.SleepDataBreakup>()

        mSleepInfo?.let {
            var totalAwake = 0

            (mSleepInfo.sleepData as ArrayList<*>).forEachIndexed { index, item ->
                val sleepDataFromWatch = item as com.zjw.zhbraceletsdk.bean.SleepData
                when (index) {
                    0 -> {
                        sleepData.startTime = sleepDataFromWatch.startTime
                    }
                    mSleepInfo.sleepData.size - 1 -> {
                        sleepData.endTime = sleepDataFromWatch.startTime
                    }
                }
                if (index != mSleepInfo.sleepData.size - 1) {
                    val nextSleepDataFromWatch =
                        mSleepInfo.sleepData[index + 1] as com.zjw.zhbraceletsdk.bean.SleepData
                    val sleep = SleepData.SleepDataBreakup(
                        sleepType = when (sleepDataFromWatch.sleep_type) {
                            "4" -> SleepType.AWAKE.type
                            "2" -> SleepType.LIGHT.type
                            "3" -> SleepType.DEEP.type
                            "6" -> SleepType.REM.type
                            else -> SleepType.AWAKE.type
                        }
                    )
                    try {
                        val startDateText =
                            mSleepInfo.sleepDate + " " + sleepDataFromWatch.startTime;
                        var startDate =
                            SimpleDateFormat("yyyy-MM-dd HH:mm", DateFormats.defaultLocale).parse(
                                startDateText
                            )
                        val calendar = Calendar.getInstance()
                        calendar.time = startDate;
                        if (calendar.get(Calendar.HOUR_OF_DAY) >= 15) {
                            startDate = Date(startDate.time - (24 * 60 * 60 * 1000))
                        }

                        val endDateText =
                            mSleepInfo.sleepDate + " " + nextSleepDataFromWatch.startTime;
                        var endDate =
                            SimpleDateFormat("yyyy-MM-dd HH:mm", DateFormats.defaultLocale).parse(
                                endDateText
                            )
                        val calendar2 = Calendar.getInstance()
                        calendar2.time = endDate;
                        if (calendar2.get(Calendar.HOUR_OF_DAY) >= 15) {
                            endDate = Date(endDate.time - (24 * 60 * 60 * 1000))
                        }

                        val differenceMinutes = (endDate.time - startDate.time) / (1000 * 60)
                        sleep.duration = differenceMinutes.toInt()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    sleep.startTime = sleepDataFromWatch.startTime
                    sleep.endTime = nextSleepDataFromWatch.startTime

                    sleepArray.add(sleep)
                }
            }
            sleepData.sleepArray = sleepArray
            sleepData.light = mSleepInfo.sleepLightTime
            sleepData.deep = mSleepInfo.sleepDeepTime
            sleepData.awake = mSleepInfo.wakeUpTime
            sleepData.remCount = mSleepInfo.remTotalMin
            sleepData.total =
                sleepData.light + sleepData.deep + sleepData.remCount//mSleepInfo.sleepTotalTime
            sleepData.date = DateFormats.getConvertToDateFormat(
                mSleepInfo.sleepDate,
                DateFormats.dateFormat3,
                DateFormats.dateFormat
            )
        }

        return sleepData
    }

    private fun parseGpsMapsData(mapsData: String?): ArrayList<DoubleArray> {
        val gpsList = ArrayList<DoubleArray>()
        mapsData?.let { mapData ->
            val mapsDataArray = mapData.split(";")
            var lastLat = 0.0
            var lastLog = 0.0
            mapsDataArray.forEach { mapDataArray ->
                val splitMapDataArray = mapDataArray.split(",")
                if (splitMapDataArray.size == 2) {
                    val dataList = ArrayList<Double>()
                    val lat = splitMapDataArray[1].toDouble()
                    val log = splitMapDataArray[0].toDouble()

                    if (lastLat != lat && lastLog != log) {
                        dataList.add(lat)
                        dataList.add(log)
                        gpsList.add(dataList.toDoubleArray())
                        lastLat = lat
                        lastLog = log
                    }


                }
            }
        }

        return gpsList
    }

    private fun getGpsMapsData(mapsData: String?): LinkedList<GPSDataResponse> {
        val gpsDataLinkedList = LinkedList<GPSDataResponse>()
        mapsData?.let { mapData ->
            val mapsDataArray = mapData.split(";")
            mapsDataArray.forEach { mapDataArray ->
                val splitMapDataArray = mapDataArray.split(",")
                if (splitMapDataArray.size == 2) {
                    val lat = splitMapDataArray[1].toDouble()
                    val log = splitMapDataArray[0].toDouble()
                    gpsDataLinkedList.add(GPSDataResponse(latitude = lat, longitude = log))

                }
            }
        }
        return gpsDataLinkedList
    }

    fun parseSportsDataGPS(p1: SportModleInfo, colorFitDevice: ColorFitDevice?): SportsModeListGPS {
        val sportsModeList = SportsModeListGPS(responseType = "final")
        val activities = ArrayList<SportsModeResponse>()
//        timberLogs.debugMessage(
//            "getGPSData onfail" + p1.recordPointSportType + ":" + p1.reportMaxStepSpeed + ":" + p1.reportSportEndTime + ":" + p1.reportHeartWarmUp
//        )
        p1.let {
            val calorieData: ArrayList<Int> = ArrayList()
            val hrData: ArrayList<Int> = ArrayList()

            if (it.recordPointSportData != null && it.recordPointSportData.isNotEmpty()) {
                it.recordPointSportData.forEach { recordPointSportData ->
                    calorieData.add(recordPointSportData.cal)
                    if (recordPointSportData.heart != 0) {
                        hrData.add(recordPointSportData.heart)
                    }
                }
            }

            val startCalendar = Calendar.getInstance()
            val calendar = Calendar.getInstance(DateFormats.defaultLocale);
            val offset =
                -(calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000)
            startCalendar.timeInMillis = (p1.reportSportStartTime + offset)
            val endCalendar = Calendar.getInstance()
            endCalendar.timeInMillis = (p1.reportSportEndTime)
            val duration = p1.reportDuration.toInt()
            val sportsModeResponse = SportsModeResponse(
                type = when (p1.recordPointSportType) {
                    3, 66 -> SportActivityName.TREADMILL
                    2 -> {
                        colorFitDevice?.let { device ->
                            if (device.deviceType.equals(
                                    DeviceType.COLORFIT_ULTRA_2.deviceType,
                                    true
                                ) || device.deviceType.equals(
                                    DeviceType.XFIT.deviceType,
                                    true
                                ) || device.deviceType.equals(
                                    DeviceType.XFIT2.deviceType,
                                    true
                                )
                            ) {
                                SportActivityName.OUTDOOR_WALKING
                            } else SportActivityName.WALKING
                        } ?: SportActivityName.WALKING
                    }
                    1 -> SportActivityName.OUTDOOR_RUNNING
                    4 -> SportActivityName.TREKKING
                    5 -> SportActivityName.TRAIL_RUN

                    15 -> SportActivityName.HUNTING
                    36 -> SportActivityName.FISHING
                    17 -> SportActivityName.SKATEBOARDING

                    102 -> SportActivityName.FENCING
                    56 -> SportActivityName.BOXING
                    59 -> SportActivityName.TAI_CHI

                    6 -> SportActivityName.OUTDOOR_CYCLING
                    7 -> SportActivityName.INDOOR_CYCLING
                    14 -> SportActivityName.BMX

                    37 -> SportActivityName.CURLING
                    19 -> SportActivityName.OUTDOOR_SKATING
                    38 -> SportActivityName.INDOOR_SKATING

                    65 -> SportActivityName.ARCHERY
                    20 -> SportActivityName.EQUESTRIAN


                    39 -> SportActivityName.CRICKET
                    9 -> SportActivityName.BASKETBALL
                    12 -> SportActivityName.BADMINTON
                    13 -> SportActivityName.OUTDOOR_HIKING

                    10 -> SportActivityName.FOOTBALL

                    47 -> SportActivityName.BALLET
                    49 -> SportActivityName.SQUARE_DANCE
                    53 -> SportActivityName.ZUMBA

                    24 -> SportActivityName.MIXED_AEROBICS
                    25 -> SportActivityName.STRENGTH_TRAINING
                    26 -> SportActivityName.STRETCHING

                    30 -> SportActivityName.INDOOR_FITNESS
                    34 -> SportActivityName.ELLIPTICAL_MACHINE
                    35 -> SportActivityName.YOGA
                    27 -> SportActivityName.CLIMBING_MACHINE

                    29 -> SportActivityName.FLEXIBILITY_TRAINING
                    31 -> SportActivityName.STEPPER
                    32 -> SportActivityName.STEP_TRAINING
                    33 -> SportActivityName.GYMNASTICS
                    8 -> SportActivityName.FREESTYLE
                    23 -> SportActivityName.CORE_TRAINING

                    16 -> SportActivityName.SAILING
                    18 -> SportActivityName.ROLLER_SKATING
                    40 -> SportActivityName.BASEBALL
                    41 -> SportActivityName.BOWLING
                    42 -> SportActivityName.SQUASH
                    43 -> SportActivityName.SOFTBALL
                    44 -> SportActivityName.CROQUET
                    45 -> SportActivityName.VOLLEYBALL
                    46 -> SportActivityName.HANDBALL
                    11 -> SportActivityName.PINGPONG
                    48 -> SportActivityName.BELLY_DANCE
                    50 -> SportActivityName.STREET_DANCE
                    51 -> SportActivityName.BALLROOM_DANCING
                    52 -> SportActivityName.DANCE

                    55 -> SportActivityName.KARATE
                    57 -> SportActivityName.JUDO
                    58 -> SportActivityName.WRESTLING
                    60 -> SportActivityName.MUAY_THAI
                    61 -> SportActivityName.TAEKWONDO
                    62 -> SportActivityName.MARTIAL_ARTS
                    63 -> SportActivityName.FREE_SPARRING
                    21 -> SportActivityName.POOL_SWIMMING


                    122, 203 -> SportActivityName.ROPE_SKIPPING
                    121, 202 -> SportActivityName.ROWING_MACHINE
                    201 -> SportActivityName.OPEN_WATER

                    123 -> SportActivityName.TRIATHLON

                    54 -> SportActivityName.KENDO
                    28 -> SportActivityName.PILATES
                    94 -> SportActivityName.FUNCTIONAL_TRAINING
                    93 -> SportActivityName.SIT_UPS
                    88 -> SportActivityName.DUMBBELL_TRAINING
                    89 -> SportActivityName.BARBELL_TRAINING
                    90 -> SportActivityName.WEIGHTLIFTING
                    64 -> SportActivityName.HIIT
                    91 -> SportActivityName.DEADLIFT

                    114 -> SportActivityName.DARTS
                    118 -> SportActivityName.FRISBEE
                    117 -> SportActivityName.KITE_FLYING
                    115 -> SportActivityName.TUG_OF_WAR
                    107 -> SportActivityName.SHUTTLECOCK

                    22 -> SportActivityName.OPEN_WATER
                    67 -> SportActivityName.PADDLE_BOARD
                    68 -> SportActivityName.WATER_POLO
                    69 -> SportActivityName.WATER_SPORTS
                    70 -> SportActivityName.WATER_SKIING
                    71 -> SportActivityName.KAYAKING
                    72 -> SportActivityName.KAYAK_RAFTING
                    73 -> SportActivityName.MOTORBOAT
                    74 -> SportActivityName.FIN_SWIMMING
                    75 -> SportActivityName.DIVING
                    76 -> SportActivityName.SYNCHRONIZED_SWIMMING
                    77 -> SportActivityName.SNORKELING
                    78 -> SportActivityName.KITE_SURFING
                    79 -> SportActivityName.ROCK_CLIMBING
                    80 -> SportActivityName.PARKOUR
                    81 -> SportActivityName.ATV
                    82 -> SportActivityName.PARAGLIDER
                    83 -> SportActivityName.CLIMB_THE_STAIRS
                    84 -> SportActivityName.CROSS_TRAINING_CROSSFIT
                    85 -> SportActivityName.AEROBICS
                    86 -> SportActivityName.PHYSICAL_TRAINING
                    87 -> SportActivityName.WALL_BALL
                    92 -> SportActivityName.BOBBY_JUMP
                    95 -> SportActivityName.UPPER_LIMB_TRAINING
                    96 -> SportActivityName.LOWER_LIMB_TRAINING
                    97 -> SportActivityName.WAIST_AND_ABDOMEN_TRAINING
                    98 -> SportActivityName.BACK_TRAINING
                    99 -> SportActivityName.NATIONAL_DANCE
                    100 -> SportActivityName.JAZZ_DANCE
                    101 -> SportActivityName.LATIN_DANCE
                    103 -> SportActivityName.RUGBY
                    104 -> SportActivityName.HOCKEY
                    105 -> SportActivityName.TENNIS
                    106 -> SportActivityName.BILLIARDS
                    108 -> SportActivityName.SEPAK_TAKRAW
                    109 -> SportActivityName.SNOW_SPORTS
                    110 -> SportActivityName.SNOWMOBILE
                    111 -> SportActivityName.PUCK
                    112 -> SportActivityName.SNOW_CAR
                    113 -> SportActivityName.SLED
                    116 -> SportActivityName.HULA_HOOP
                    119 -> SportActivityName.TRACK_AND_FIELD
                    120 -> SportActivityName.RACING_CAR
                    124 -> SportActivityName.MOUNTAIN_CYCLING
                    125 -> SportActivityName.KICKBOXING
                    126 -> SportActivityName.SKIING
                    127 -> SportActivityName.CROSS_COUNTRY_SKIING
                    128 -> SportActivityName.SNOWBOARDING
                    129 -> SportActivityName.ALPINE_SKIING
                    130 -> SportActivityName.DOUBLE_BOARD_SKIING
                    131 -> SportActivityName.FREE_EXERCISE
                    132 -> SportActivityName.PADDLEBOARD_SURFING
                    133 -> SportActivityName.KABADDI
                    200 -> SportActivityName.POOL_SWIMMING
                    204 -> SportActivityName.TRIATHLON

                    else -> ""
                },
                calories = p1.reportCal,
                heartRateAvg = p1.reportAvgHeart,
                endTime = DateFormats.timeFormat.format(endCalendar.time),
                date = DateFormats.dateFormat.format(startCalendar.time),
                time = DateFormats.formatDateTime(
                    startCalendar.time,
                    DateFormats.dateTimeFormatISO
                ),
                duration = duration.toLong(),
                heartRateData = hrData.handleHrData(duration),
                calorieData = calorieData.handleCaloriesData(duration)
            )


//            val gpsDataLinkedList = getGpsMapsData(p1.map_data)
            val gpsParseData = parseGpsMapsData(p1.map_data)

//            LOGS.d("gpsParseData ${Gson().toJson(gpsParseData)}")
            if (gpsParseData.isEmpty()) {
                sportsModeResponse.gpsCoordinate = null
            } else {
                sportsModeResponse.gpsCoordinate = listToJson(gpsParseData)
            }



            sportsModeResponse.aerobic = (p1.reportHeartAerobic).toInt()
            sportsModeResponse.anaerobic = (p1.reportHeartAnaerobic).toInt()
            sportsModeResponse.fatBurn = (p1.reportHeartFatBurning).toInt()
            sportsModeResponse.warmUp = (p1.reportHeartWarmUp).toInt()
            sportsModeResponse.hrZoneInSeconds = 1
            if (p1.reportTotalStep.toInt() != 0) {
                sportsModeResponse.cadence = p1.reportMaxStepSpeed
                sportsModeResponse.steps = p1.reportTotalStep.toInt()
            }
            colorFitDevice?.let { device ->
                if ((device.deviceType.equals(
                        DeviceType.COLORFIT_PULSE.deviceType,
                        true
                    ) || device.deviceType.equals(DeviceType.COLORFIT_BEAT.deviceType, true)) &&
                    (p1.recordPointSportType == 6 || p1.recordPointSportType == 4 || p1.recordPointSportType == 3 || p1.recordPointSportType == 66)
                ) {
                    sportsModeResponse.distance = 0
                } else {
                    sportsModeResponse.distance = p1.reportDistance
                }
            }


            if (sportsModeResponse.distance?.toInt() != 0 && duration != 0) {
                val df = DecimalFormat("#.#")
                val speed = p1.reportFastSpeed
                sportsModeResponse.speed = df.format(speed)?.toFloat()
            } else {
                sportsModeResponse.speed = 0F
                sportsModeResponse.cadence = 0
            }


            activities.add(sportsModeResponse)
        }

        // LoggerHelper.printVerbose("GPS DATA", "getGPSData onfail"+Gson().toJson(activities))
        sportsModeList.activities = activities
        return sportsModeList
    }


    private fun listToJson(value: ArrayList<DoubleArray>) = Gson().toJson(value)


    fun formatReminderData(p1: ArrayList<EventReminder>): ReminderList {
        val alarmsList = ArrayList<ReminderList.Reminder>()
        p1.forEach { item ->
            val reminder = ReminderList.Reminder(
                hour = item.hour,
                minute = item.minute,
                year = item.year,
                month = item.month,
                day = item.day,
                label = item.content
            )
            alarmsList.add(reminder)
        }
        return ReminderList(reminders = alarmsList)
    }

    fun formatContactList(contactBeanList: List<ContactsBean>): ArrayList<Contact> {
        val contactList = ArrayList<Contact>()
        contactBeanList.forEach { contactsBean ->
            contactList.add(
                Contact(
                    contactsBean.number,
                    contactsBean.name,
                    true,
                    null,
                    arrayListOf(contactsBean.number)
                )
            )
        }

        return contactList
    }

    fun formatMenstrualData(menstrualData: MenstrualData): MenstrualCycleBean {
        val menstrual = MenstrualCycleBean()
        menstrual.menstrualSafetyPeriod = menstrualData.menstrualCycleLength
        menstrual.menstrualPeriod = menstrualData.menstrualLength
        menstrual.isSwitch = menstrualData.status
        menstrual.fertilePeriod = 5
        menstrual.safePeriodOfFertility = 10
        val calendar = Calendar.getInstance()
        menstrualData.lastMenstrualDate?.let {
            calendar.time = DateFormats.dateFormat.parse(it)!!
        }
        menstrual.day = calendar.get(Calendar.DAY_OF_MONTH)
        menstrual.month = calendar.get(Calendar.MONTH) + 1
        menstrual.year = calendar.get(Calendar.YEAR)
        return menstrual
    }

    fun parseSedentaryData(sitInfo: SitInfo): SedentaryData {
        return SedentaryData(
            status = sitInfo.sitEnable,
            startHour = sitInfo.sitStartHour,
            startMinute = sitInfo.sitStartMin,
            endHour = sitInfo.sitEndHour,
            endMinute = sitInfo.sitEndMin,
            interval = sitInfo.sitPeriod
        )
    }

    fun parseDrinkWater(drinkInfo: DrinkInfo): SedentaryData {

        return SedentaryData(
            status = drinkInfo.drinkEnable,
            startHour = drinkInfo.drinkStartHour,
            startMinute = drinkInfo.drinkStartMin,
            endHour = drinkInfo.drinkEndHour,
            endMinute = drinkInfo.drinkEndMin,
            interval = drinkInfo.drinkPeriod
        )
    }

    fun parseHandWashData(handWashingInfo: HandWashingInfo): HandWashing {
        return HandWashing(
            startWash = handWashingInfo.enable,
            startHour = handWashingInfo.startHour,
            startMinute = handWashingInfo.startMin,
            endHour = handWashingInfo.endHour,
            endMinute = handWashingInfo.endMin,
            frequency = handWashingInfo.period
        )
    }

    fun dummyHandWashData(): HandWashing {
        return HandWashing(
            startWash = false,
            startHour = 10,
            startMinute = 0,
            endHour = 20,
            endMinute = 0,
            frequency = 1
        )
    }

    fun getIntFromBooleanArray(bArray: BooleanArray): Int {
        var str = ""
        for (element in bArray) {
            str += if (element) {
                "1"
            } else {
                "0"
            }
        }
        return Integer.parseInt(str, 2)
    }

    fun getBooleanFromInt(cycle: Int): BooleanArray {
        val booleanList = booleanArrayOf(false, false, false, false, false, false, false)
        val list = String.format("%7s", Integer.toBinaryString(cycle)).replace(' ', '0')
//        LoggerHelper.printVerbose("noise_fit_event:colorfit_pro_2 : ", "idleAlert:::" + list)
        var i = 0
        list.toCharArray().forEach { char ->
            booleanList[i] = char.toString() == "1"
            i += 1
        }
//        LoggerHelper.printVerbose(
//            "noise_fit_event:colorfit_pro_2 : ",
//            "idleAlert:::" + booleanList.toString()
//        )
        return booleanList
    }

    fun formatNotificationMessage(message: String): String {
        val emoji = "🎥"
        return message.replace(emoji, "")
    }

}

