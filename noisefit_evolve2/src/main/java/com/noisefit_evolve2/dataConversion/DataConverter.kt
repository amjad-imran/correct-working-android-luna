package com.noisefit_evolve2.dataConversion

import android.content.Context
import com.google.gson.Gson
import com.noisefit_commans.common.convertMinuteIntoSeconds
import com.noisefit_commans.constants.SportActivityName
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.StringUtils
import com.touchgui.sdk.bean.*
import java.math.BigInteger
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToInt

class DataConverter
@Inject
constructor(
    var context: Context,
    var gson: Gson
) {

    fun formatAlarmData(p1: ArrayList<TGAlarm>): AlarmsList {
        val alarmsList = ArrayList<AlarmsList.Alarm>()

        p1.forEach { item ->

            val alarmItem = item as TGAlarm
            if (alarmItem.isShow) {
                alarmsList.add(
                    AlarmsList.Alarm(
                        hour = alarmItem.hour,
                        minute = alarmItem.minute,
                        status = alarmItem.isShow,
                        repeatDays = binstrToBooleanArrayAlarm(alarmItem.repeat),
                        id = alarmItem.id,
                        alarmAction = AlarmAction.ALARM_CHANGE
                    )
                )
            }

        }


        return AlarmsList(alarms = alarmsList)
    }


    fun booleanArrayToBinaryString(booleanList: List<Boolean>?): Int {
        var result = 0
        for (i in 7 downTo 0) {
            booleanList?.let { boolList ->
                if (boolList[i]) {
                    result += 2.0.pow((7 - i).toDouble()).roundToInt()
                }
            }

        }
        return result
    }

    private fun binstrToBooleanArrayAlarm(my_byte: Int): List<Boolean> {
        val result = ArrayList<Boolean>()
        result.addAll(listOf(false, false, false, false, false, false, false, false))
        val binStr = ByteArray(1)
        binStr[0] = my_byte.toByte()
        val byteStr: String = binary(binStr, 2)
        for (i in byteStr.length - 1 downTo 0) {
            result[i + 8 - byteStr.length] = Integer.valueOf(byteStr.substring(i, i + 1)) == 1
        }

        val subList = ArrayList(result.subList(0, result.size))
        subList.reverse()
        return subList
    }

    private fun binstrToBooleanArrayReminder(my_byte: Int): List<Boolean> {
        val result = ArrayList<Boolean>()
        result.addAll(listOf(false, false, false, false, false, false, false, false))
        val binStr = ByteArray(1)
        binStr[0] = my_byte.toByte()
        val byteStr: String = binary(binStr, 2)
        for (i in byteStr.length - 1 downTo 0) {
            result[i + 8 - byteStr.length] = Integer.valueOf(byteStr.substring(i, i + 1)) == 1
        }

        val subList = ArrayList(result.subList(0, result.size))
        subList.reverse()
        subList.removeAt(0)
        return subList
    }


    private fun binary(bytes: ByteArray?, radix: Int): String {
        return BigInteger(1, bytes).toString(radix)
    }


    private fun getCalorieFormat(value: Float): String {
        val df = DecimalFormat()
        df.maximumFractionDigits = 0
        df.groupingSize = 0
        df.roundingMode = RoundingMode.FLOOR
        return df.format(value.toDouble())
    }

    fun getStepsData(mMotionInfo: TGStepData): StepsData {
        val dailyStepData = StepsData(date = DateFormats.getDateFormat(mMotionInfo.date))
        dailyStepData.totalSteps = mMotionInfo.totalSteps
        dailyStepData.totalCalories = mMotionInfo.totalCal
        dailyStepData.totalDistance = mMotionInfo.totalDistance
        dailyStepData.totalActiveTime = mMotionInfo.totalActiveTime
        val stepArray = ArrayList<StepsData.StepDataBreakup>()
//            val calendar = Calendar.getInstance()
//            val year = calendar[Calendar.YEAR]
//            val month = calendar[Calendar.MONTH]
//            val day = calendar[Calendar.DATE]
//            calendar.set(year, month, day, 0,0,0)
        var totalTime = 0
        var steps = 0
        var distance = 0
        var calories = 0

        mMotionInfo.items?.forEachIndexed { i, e ->
            //calendar.add(Calendar.MINUTE, e.activeTime)
            //val time = DateFormats.timeFormat.format(calendar.time)
            totalTime += e.activeTime
            steps += e.stepCount
            distance += e.distance
            calories += e.calories
            if (totalTime % 60 == 0) {
                val index = totalTime / 60
                val stepData = StepsData.StepDataBreakup(hourOfTheDay = index - 1)
                stepData.steps = steps
                stepData.distance = distance
                stepData.calories = calories
                //stepData.activeTime = e.activeTime
                stepArray.add(stepData)
                steps = 0
                distance = 0
                calories = 0
            }
            if (i == mMotionInfo.itemCount - 1) {
                val index = totalTime / 60
                val stepData = StepsData.StepDataBreakup(hourOfTheDay = index)
                stepData.steps = steps
                stepData.distance = distance
                stepData.calories = calories
                //stepData.activeTime = e.activeTime
                stepArray.add(stepData)
                steps = 0
                distance = 0
                calories = 0
            }
        }
        dailyStepData.stepArray = stepArray

        return dailyStepData
    }


    private fun getFormattedTime(time: Int): String {
        if (time <= 9) {
            return "0$time"
        }
        return time.toString()
    }

    fun getSleepData(mSleepInfo: TGSleepData): SleepData {
        val sleepData = SleepData(availableSleepTypes = "deep;light;awake;rem")
        val sleepArray = ArrayList<SleepData.SleepDataBreakup>()



        mSleepInfo.let {
            var totalAwake = 0

            val allDay = 24 * 60
            val sleepOffset: Int
            val awakeOffset: Int = mSleepInfo.endHour * 60 + mSleepInfo.endMinute
            if (mSleepInfo.totalMinute >= awakeOffset) {
                // Cross zero
                sleepOffset = allDay + awakeOffset - mSleepInfo.totalMinute
            } else {
                sleepOffset = awakeOffset - mSleepInfo.totalMinute
            }

            val endHour = mSleepInfo.endHour
            val endMinute = mSleepInfo.endMinute
            val startHour = sleepOffset / 60
            val startMinute = sleepOffset % 60
            sleepData.startTime = "${getFormattedTime(startHour)}:${getFormattedTime(startMinute)}"
            sleepData.endTime = "${getFormattedTime(endHour)}:${getFormattedTime(endMinute)}"


            var offset = sleepOffset
            (mSleepInfo.items as? ArrayList<*>)?.forEachIndexed { index, item ->
                val sleepDataFromWatch = item as TGSleepData.ItemBean

                val sleep = SleepData.SleepDataBreakup(
                    sleepType = when (sleepDataFromWatch.status) {
                        1 -> SleepType.AWAKE.type
                        2 -> SleepType.LIGHT.type
                        3 -> SleepType.DEEP.type
                        4 -> SleepType.REM.type
                        else -> SleepType.AWAKE.type
                    }
                )
                try {
                    val startOffset = if (offset >= allDay) offset - allDay else offset
                    val sHour = startOffset / 60
                    val sMinute = startOffset % 60
                    offset += item.duration
                    val endOffset = if (offset >= allDay) offset - allDay else offset
                    val eHour = endOffset / 60
                    val eMinute = endOffset % 60
                    sleep.duration = sleepDataFromWatch.duration
                    sleep.startTime = "${getFormattedTime(sHour)}:${getFormattedTime(sMinute)}"
                    sleep.endTime = "${getFormattedTime(eHour)}:${getFormattedTime(eMinute)}"
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                sleepArray.add(sleep)

            }
            sleepData.sleepArray = sleepArray
            sleepData.light = mSleepInfo.lightMinute
            sleepData.deep = mSleepInfo.deepMinute

            sleepData.remCount = mSleepInfo.eyeMoveMinute
            sleepData.total = mSleepInfo.totalMinute

            sleepData.awake =
                mSleepInfo.totalMinute - (mSleepInfo.lightMinute + mSleepInfo.deepMinute + mSleepInfo.eyeMoveMinute)
            sleepData.date = DateFormats.getDateFormat(mSleepInfo.date)

            sleepData.sleepScore = mSleepInfo.sleepScore
        }
        return sleepData
    }

    fun appendPrefixToTime(time: Int): String {
        if (time < 10) {
            return "0${time}"
        }
        return time.toString()
    }

    //
    fun parseSportsDataGPS(sportList: MutableList<TGWorkoutRecord>): SportsModeListGPS {
        val sportsModeList = SportsModeListGPS(responseType = "final")
        val activities = ArrayList<SportsModeResponse>()
        sportList.forEach { sportRecordMerge ->

            val startCalendar = Calendar.getInstance()
            val calendar = Calendar.getInstance(DateFormats.defaultLocale);
            val offset =
                -(calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000)
            startCalendar.timeInMillis = (sportRecordMerge.summary.date.time)
            val endCalendar = Calendar.getInstance()
            //endCalendar.timeInMillis = (sportsData.endTimeStamp + 9000) * 1000
            endCalendar.timeInMillis =
                (sportRecordMerge.summary.date.time + sportRecordMerge.summary.duration * 60 * 1000)


            val duration = sportRecordMerge.summary.duration
            val sportsModeResponse = SportsModeResponse(
                type = when (sportRecordMerge.summary.type) {
                    TGWorkoutType.OUTDOOR_WALK, TGWorkoutType.OUTDOOR_WALK_NO_GPS -> SportActivityName.OUTDOOR_WALK
                    TGWorkoutType.INDOOR_WALK -> SportActivityName.INDOOR_WALK
                    TGWorkoutType.OUTDOOR_RUN, TGWorkoutType.OUTDOOR_RUN_NO_GPS -> SportActivityName.OUTDOOR_RUN
                    TGWorkoutType.INDOOR_RUN -> SportActivityName.INDOOR_RUN
                    TGWorkoutType.SPINNING -> SportActivityName.INDOOR_CYCLE
                    TGWorkoutType.FREE_WORKOUT -> SportActivityName.FREE_TRAINING
                    TGWorkoutType.OUTDOOR_CYCLING, TGWorkoutType.OUTDOOR_CYCLING_NO_GPS -> SportActivityName.OUTDOOR_CYCLE
                    TGWorkoutType.CRICKET -> SportActivityName.CRICKET
                    TGWorkoutType.BASKETBALL -> SportActivityName.BASKETBALL
                    TGWorkoutType.HIKING, TGWorkoutType.HIKING_NO_GPS -> SportActivityName.HIKING
                    TGWorkoutType.FOOTBALL -> SportActivityName.FOOTBALL
                    TGWorkoutType.STRENGTH_TRAINING -> SportActivityName.STRENGTH_TRAINING
                    TGWorkoutType.ELLIPTICAL -> SportActivityName.ELLIPTICAL
                    TGWorkoutType.YOGA -> SportActivityName.YOGA
                    TGWorkoutType.SWIMMING -> SportActivityName.SWIMMING
                    TGWorkoutType.ROWING -> SportActivityName.ROWING
                    TGWorkoutType.CROSS_FITTING -> SportActivityName.CROSS_FIT
                    TGWorkoutType.BADMINTON -> SportActivityName.BADMINTON
                    TGWorkoutType.BASEBALL -> SportActivityName.BASEBALL
                    TGWorkoutType.FOLK_DANCE -> SportActivityName.FOLK_DANCE
                    TGWorkoutType.PILATES -> SportActivityName.PILATES
                    TGWorkoutType.BURPEES -> SportActivityName.BURPEES
                    TGWorkoutType.GOLF -> SportActivityName.GOLF
                    TGWorkoutType.TENNIS -> SportActivityName.TENNIS
                    TGWorkoutType.MODERN_DANCE -> SportActivityName.MODERN_DANCE
                    TGWorkoutType.STEP_TRAINING -> SportActivityName.STEPPER
                    TGWorkoutType.ROCK_CLIMBING -> SportActivityName.ROCK_CLIMBING
                    TGWorkoutType.PARKOUR -> SportActivityName.PARKOUR
                    TGWorkoutType.HOCKEY -> SportActivityName.HOCKEY
                    TGWorkoutType.JAZZ_DANCE -> SportActivityName.JAZZ_DANCE
                    TGWorkoutType.CORE_TRAINING -> SportActivityName.CORE_TRAINING
                    TGWorkoutType.SKATEBOARDING -> SportActivityName.SKATEBOARDING
                    TGWorkoutType.VOLLEYBALL -> SportActivityName.VOLLEYBALL
                    TGWorkoutType.BOXING -> SportActivityName.BOXING
                    TGWorkoutType.ROLLER_SKATING -> SportActivityName.SKATEBOARDING
                    TGWorkoutType.SKATING -> SportActivityName.SKATING
                    TGWorkoutType.RUGBY -> SportActivityName.RUGBY
                    TGWorkoutType.HANDBALL -> SportActivityName.HANDBALL
                    TGWorkoutType.LOCKING -> SportActivityName.LOCKING
                    TGWorkoutType.GYMNASTICS -> SportActivityName.GYMNASTICS
                    TGWorkoutType.TABLE_TENNIS -> SportActivityName.TABLE_TENNIS
                    TGWorkoutType.LATIN_DANCE -> SportActivityName.LATIN_DANCE
                    TGWorkoutType.DUMBBELL -> SportActivityName.DUMBBELL
                    TGWorkoutType.HIIT -> SportActivityName.HIIT
                    TGWorkoutType.AEROBICS -> SportActivityName.AEROBICS
                    TGWorkoutType.BALLET -> SportActivityName.BALLET
                    TGWorkoutType.ZUMBA -> SportActivityName.ZUMBA
                    TGWorkoutType.BELLY_DANCE -> SportActivityName.BELLY_DANCE
                    TGWorkoutType.BREAKING -> SportActivityName.POPING
                    TGWorkoutType.AEROBICS_EXERCISE -> SportActivityName.AEROBICS_EXERCISE
                    TGWorkoutType.CROSS_TRAINING -> SportActivityName.CROSS_TRAINING
                    TGWorkoutType.SOFTBALL -> SportActivityName.SOFTBALL
                    TGWorkoutType.PICKLE_BALL -> SportActivityName.PICKLE_BALL
                    TGWorkoutType.LACROSSE -> SportActivityName.LACROSSE
                    TGWorkoutType.SHUTTLECOCK -> SportActivityName.SHUTTLECOCK
                    TGWorkoutType.ICE_HOCKEY -> SportActivityName.ICE_HOCKEY
                    TGWorkoutType.PLATE_SHOT -> SportActivityName.SHOT_PUT
                    TGWorkoutType.SOLID_BALL -> SportActivityName.SOLID_BALL
                    TGWorkoutType.DODGE_BALL -> SportActivityName.DODGE_BALL
                    TGWorkoutType.YOYO_BALL -> SportActivityName.YOYO
                    TGWorkoutType.BOWLING -> SportActivityName.BOWLING
                    TGWorkoutType.AMERICAN_FOOTBALL -> SportActivityName.AMERICAN_FOOTBALL
                    TGWorkoutType.FENCING -> SportActivityName.FENCING
                    TGWorkoutType.CLIMB_THE_STAIRS -> SportActivityName.STAIRS
                    TGWorkoutType.TAEKWONDO -> SportActivityName.TAEKWONDO
                    TGWorkoutType.KARATE -> SportActivityName.KARATE
                    TGWorkoutType.FLEXIBILITY -> SportActivityName.FLEXIBILITY
                    TGWorkoutType.FITNESS_GAME -> SportActivityName.FITNESS_GAMING
                    TGWorkoutType.PARALLEL_BARS -> SportActivityName.PARALLEL_BARS
                    TGWorkoutType.HULA_HOOP -> SportActivityName.HULA_HOOP
                    TGWorkoutType.LONG_JUMP -> SportActivityName.LONG_JUMP
                    TGWorkoutType.HIGH_JUMP -> SportActivityName.HIGH_JUMP
                    TGWorkoutType.CURLING -> SportActivityName.CURLING
                    TGWorkoutType.SIT_UPS -> SportActivityName.SIT_UP
                    TGWorkoutType.PUSH_UP -> SportActivityName.PUSH_UPS
                    TGWorkoutType.TREADMILL -> SportActivityName.TREADMILL
                    TGWorkoutType.BATTLE_ROPE -> SportActivityName.BATTLE_ROPE
                    TGWorkoutType.SMITH_MACHINE -> SportActivityName.SMITH_MACHINE
                    TGWorkoutType.PULL_UP -> SportActivityName.PULL_UPS
                    TGWorkoutType.PLATE_KABADI -> SportActivityName.KABADDI
                    TGWorkoutType.KENDO -> SportActivityName.KENDO
                    TGWorkoutType.GO_KARTS -> SportActivityName.KARTING
                    TGWorkoutType.HUNTING -> SportActivityName.HUNTING
                    TGWorkoutType.SNOWBOARDING_SINGLE -> SportActivityName.SNOWBOARDING
                    TGWorkoutType.FISHING -> SportActivityName.FISHING
                    TGWorkoutType.JAVELIN -> SportActivityName.JAVELIN
                    TGWorkoutType.WRESTLING -> SportActivityName.WRESTLING
                    TGWorkoutType.TAI_CHI -> SportActivityName.TAI_CHI
                    TGWorkoutType.MARTIAL_ARTS -> SportActivityName.MARTIAL_ARTS
                    TGWorkoutType.SNOW_SPORTS -> SportActivityName.SNOW_SPORTS
                    TGWorkoutType.DARTS -> SportActivityName.DARTS
                    TGWorkoutType.HORSEBACK_RIDING -> SportActivityName.HORSE_RIDING
                    TGWorkoutType.SQUASH -> SportActivityName.RACQUETBALL
                    TGWorkoutType.SKIPPING_ROPE -> SportActivityName.JUMP_ROPE
                    TGWorkoutType.HORIZONTAL_BAR -> SportActivityName.SINGLE_BAR
                    TGWorkoutType.GROUP_GYMNASTICS -> SportActivityName.GROUP_TRAINING
                    TGWorkoutType.FOAM_SHAFT_FASCIA_RELAXES -> SportActivityName.ROLLING
                    TGWorkoutType.AEROBICS -> SportActivityName.AEROBICS_GYMS
                    TGWorkoutType.LUMBAR_TRAINING -> SportActivityName.WAIST_TRAINING
                    TGWorkoutType.PLATE_SUPPORT -> SportActivityName.PLANK_SUPPORT
                    TGWorkoutType.FRISBEE -> SportActivityName.FRISBEE
                    TGWorkoutType.ATHLETIC -> SportActivityName.TRACK_FIELD
                    TGWorkoutType.LEISURE_SPORTS -> SportActivityName.PLAY
                    TGWorkoutType.FIGHTING_EXERCISE -> SportActivityName.CARDIO_BOXING
                    TGWorkoutType.SQUARE_DANCE -> SportActivityName.SQUARE_DANCE
                    TGWorkoutType.ARCHERY -> SportActivityName.ARCHERY
                    TGWorkoutType.HANDCAR -> SportActivityName.HAND_CYCLING
                    TGWorkoutType.SOOTHING_MED_EXERCISES -> SportActivityName.MIND_AND_BODY
                    else -> ""
                },
                calories = sportRecordMerge.summary.calories.toLong(),
                heartRateAvg = sportRecordMerge.summary.avgHr,
                endTime = DateFormats.timeFormat.format(endCalendar.time),
                date = DateFormats.dateFormat.format(startCalendar.time),
                /*time = DateFormats.getFormattedTime(startCalendar.time),*/
                time = DateFormats.formatDateTime(
                    startCalendar.time,
                    DateFormats.dateTimeFormatISO
                ),
                duration = duration.toLong()
            )

//            LOGS.d(">>>>GPS data<<< "+Gson().toJson(sportsModeResponse.gpsData))
            val gpsParseData = parseGpsMapsData(sportRecordMerge.gpsData)

//                LOGS.d(">>>GpsParseData<<<< ${Gson().toJson(gpsParseData)}")
            if (gpsParseData.isEmpty()) {
                sportsModeResponse.gpsCoordinate = null
            } else {
                sportsModeResponse.gpsCoordinate = gson.toJson(gpsParseData)
            }

            sportsModeResponse.extremeMin =
                (sportRecordMerge.summary.extremeExercise.convertMinuteIntoSeconds())
            sportsModeResponse.aerobic =
                (sportRecordMerge.summary.aerobicExercise.convertMinuteIntoSeconds())
            sportsModeResponse.anaerobic =
                (sportRecordMerge.summary.anaerobicExercise.convertMinuteIntoSeconds())
            sportsModeResponse.fatBurn =
                (sportRecordMerge.summary.fatBurning.convertMinuteIntoSeconds())
            sportsModeResponse.warmUp = (sportRecordMerge.summary.warmUp.convertMinuteIntoSeconds())
            sportsModeResponse.hrZoneInSeconds = 1
            if (sportRecordMerge.summary.step != 0) {
                sportsModeResponse.steps = sportRecordMerge.summary.step
                sportsModeResponse.cadence = sportRecordMerge.summary.avgStrideFrequency
                sportsModeResponse.speed = sportRecordMerge.summary.avgSpeed.toFloat()
                sportsModeResponse.avgStepStride = sportRecordMerge.summary.avgStrideLength
            }

            sportsModeResponse.distance = sportRecordMerge.summary.distance.toLong()
            sportsModeResponse.pace = sportRecordMerge.summary.avgPaceSecs.toFloat()

            if (sportRecordMerge.summary.distance != 0 && duration != 0) {
                val df = DecimalFormat("#.#")
                val speed = sportRecordMerge.summary.maxSpeed
                sportsModeResponse.speed = df.format(speed)?.toFloat()
            } else {
                sportsModeResponse.speed = 0F
                sportsModeResponse.cadence = 0
            }
            activities.add(sportsModeResponse)

        }


        sportsModeList.activities = activities
//        LOGS.d(">>>data packet list<<<", Gson().toJson(sportsModeList))
        return sportsModeList
    }

    private fun parseGpsMapsData(tgSyncGps: MutableList<out TGWorkoutRecord.Gps>?): ArrayList<DoubleArray> {
        val gpsList = ArrayList<DoubleArray>()
        var lastLat = 0.0
        var lastLog = 0.0
        tgSyncGps?.forEach {
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

    //
//        fun formatReminderData(p1: ArrayList<EventReminder>) : ReminderList {
//            val alarmsList = ArrayList<ReminderList.Reminder>()
//
//            p1.forEach{ item ->
//                val remin = ReminderList.Reminder(hour = item.hour, minute = item.minute,year = item.year, month = item.month, day = item.day, label = item.content)
//                alarmsList.add(remin)
//            }
//            return ReminderList(reminders = alarmsList)
//        }
//
//        fun formatMenstrualData(menstrualData: MenstrualData): MenstrualCycleBean {
//            val menstrual = MenstrualCycleBean()
//            menstrual.menstrualSafetyPeriod = menstrualData.menstrualCycleLength
//            menstrual.menstrualPeriod = menstrualData.menstrualLength
//            menstrual.isSwitch = menstrualData.status
//            menstrual.fertilePeriod = 5
//            menstrual.safePeriodOfFertility = 10
//            val calendar = Calendar.getInstance()
//            if(menstrualData.lastMenstrualDate!=null) {
//                calendar.time = DateFormats.dateFormat.parse(menstrualData.lastMenstrualDate)
//            }
//            menstrual.day = calendar.get(Calendar.DAY_OF_MONTH)
//            menstrual.month = calendar.get(Calendar.MONTH)+1
//            menstrual.year = calendar.get(Calendar.YEAR)
//            return menstrual
//        }
    fun parseSedentaryData(longSit: TGSedentaryConfig): SedentaryData {

        val repeatDays = binstrToBooleanArrayAlarm(longSit.repeat)

        var startHour = longSit.startHour
        var stopHour = longSit.stopHour
        if (!repeatDays[0] && startHour == 0 && stopHour == 0) {
            startHour = StringUtils.DefaultStartHour
            stopHour = StringUtils.DefaultStopHour
        }
        return SedentaryData(
            status = repeatDays[0],
            startHour = startHour,
            startMinute = longSit.startMinute,
            endHour = stopHour,
            endMinute = longSit.stopMinute,
            interval = longSit.interval,
            repeatDays = ArrayList(repeatDays.subList(1, repeatDays.size))
        )
    }

    fun parseDrinkWater(longSit: TGRemindDrinking): SedentaryData {
//        val repeatDays = binstrToBooleanArrayAlarm(longSit.repeat)

//        LOGS.d("parseDrinkWater $${Gson().toJson(longSit)}")
        var status = true
        if (longSit.repeat == 0) {
            status = false
        }


        var startHour = longSit.startHour
        var stopHour = longSit.stopHour
        if (!status && startHour == 0 && stopHour == 0) {
            startHour = StringUtils.DefaultStartHour
            stopHour = StringUtils.DefaultStopHour
        }
        return SedentaryData(
            status = status,
            startHour = startHour,
            startMinute = longSit.startMinute,
            endHour = stopHour,
            endMinute = longSit.stopMinute,
            interval = longSit.interval,// returning interval in seconds
//            repeatDays = ArrayList(repeatDays.subList(1, repeatDays.size))
        )
    }

    //
//        fun parseHandWashData(longSit: HandWashingInfo): HandWashing {
//            return HandWashing(startWash = longSit.enable, startHour = longSit.startHour, startMinute = longSit.startMin,
//                    endHour = longSit.endHour, endMinute = longSit.endMin, frequency = longSit.period)
//        }
//
//        fun getIntFromBooleanArray(bArray : BooleanArray) : Int{
//            var str = ""
//            for(element in bArray){
//                if(element == true){
//                    str += "1"
//                }
//                else{
//                    str += "0"
//                }
//            }
//            val sedentaryRepeat = Integer.parseInt(str, 2) as Int
//            return sedentaryRepeat
//        }
//        fun getBooleanFromInt(cycle : Int) : BooleanArray {
//            val booleanList = booleanArrayOf(false,false,false,false,false,false,false)
//            val list = String.format("%7s", Integer.toBinaryString(cycle)).replace(' ', '0')
//            LoggerHelper.printVerbose("noise_fit_event:colorfit_pro_2 : ", "idleAlert:::"+list)
//            var i = 0
//            list.toCharArray().forEach { char ->
//                if(char.toString() == "1"){
//                    booleanList[i] = true
//                }
//                else{
//                    booleanList[i] = false
//                }
//                i = i+1
//            }
//            LoggerHelper.printVerbose("noise_fit_event:colorfit_pro_2 : ", "idleAlert:::"+booleanList.toString())
//            return booleanList
//        }
//
    fun getOffset(): Int {
        val calendar = Calendar.getInstance(DateFormats.defaultLocale);
        val offset =
            -(calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000)
        return offset
    }
}


