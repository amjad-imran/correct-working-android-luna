package com.noisefit_zhsdk.handler

import android.content.Context
import android.location.Geocoder
import com.google.gson.Gson
import com.noisefit_commans.common.averageWithoutZero
import com.noisefit_commans.common.handleCaloriesData
import com.noisefit_commans.common.handleHrData
import com.noisefit_commans.common.upTo1Decimal
import com.noisefit_commans.constants.CommonGlobals
import com.noisefit_commans.constants.SportActivityName
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.data.model.DayTimeMovementBreakup
import com.noisefit_commans.data.model.OreoBloodOxygenBreakup
import com.noisefit_commans.data.model.OreoBodyStressData
import com.noisefit_commans.data.model.OreoBodyTemperatureBreakup
import com.noisefit_commans.data.model.OreoHeartRate
import com.noisefit_commans.data.model.OreoNapData
import com.noisefit_commans.data.model.OreoRespiratoryData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.data.model.OreoStepsData
import com.noisefit_commans.data.model.OreoStressDataBreakup
import com.noisefit_commans.enums.ApplicationType
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.Contact
import com.noisefit_commans.models.DoNotDisturb
import com.noisefit_commans.models.ManualMeasureType
import com.noisefit_commans.models.ReminderList
import com.noisefit_commans.models.SedentaryData
import com.noisefit_commans.models.SleepMovementType
import com.noisefit_commans.models.SleepType
import com.noisefit_commans.models.SportsModeListGPS
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.models.Widget
import com.noisefit_commans.models.WorldClockList
import com.noisefit_commans.utils.AppConversionUtils
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_zhsdk.handler.ZhUserActivityHandler.Companion.TRACK_TAG
import com.zhapp.ble.bean.ActiveMeasureParamsBean
import com.zhapp.ble.bean.ContactBean
import com.zhapp.ble.bean.ContinuousBloodOxygenBean
import com.zhapp.ble.bean.ContinuousHeartRateBean
import com.zhapp.ble.bean.ContinuousPressureBean
import com.zhapp.ble.bean.ContinuousTemperatureBean
import com.zhapp.ble.bean.DailyBean
import com.zhapp.ble.bean.DevSportInfoBean
import com.zhapp.ble.bean.DoNotDisturbModeBean
import com.zhapp.ble.bean.EventInfoBean
import com.zhapp.ble.bean.OverallDayMovementData
import com.zhapp.ble.bean.PressureModeBean
import com.zhapp.ble.bean.RingSleepNapBean
import com.zhapp.ble.bean.RingSleepResultBean
import com.zhapp.ble.bean.RingStressDetectionBean
import com.zhapp.ble.bean.TodayRespiratoryRateData
import com.zhapp.ble.bean.WidgetBean
import com.zhapp.ble.bean.WorldClockBean
import com.zhapp.ble.callback.ActiveMeasureCallBack
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

class OreoDataConverter
@Inject
constructor(
    var context: Context,
    var gson: Gson,
    var watchDataStore: WatchDataStore,
    private val geoCoder: Geocoder
) {
//    fun formatAlarmData(list: List<ClockInfoBean>): AlarmsList {
//        val alarmsList = ArrayList<AlarmsList.Alarm>()
//        list.forEach { alarmItem ->
//            alarmsList.add(
//                AlarmsList.Alarm(
//                    hour = alarmItem.data.time.hour,
//                    minute = alarmItem.data.time.minuter,
//                    status = alarmItem.data.isEnable,
//                    repeatDays = arrayListOf(
//                        alarmItem.data.isEnable,
//                        alarmItem.data.isMonday,
//                        alarmItem.data.isTuesday, alarmItem.data.isWednesday,
//                        alarmItem.data.isThursday, alarmItem.data.isFriday,
//                        alarmItem.data.isSaturday, alarmItem.data.isSunday
//                    ),
//                    id = alarmItem.id
//                )
//            )
//        }
//
//
//        return AlarmsList(alarms = alarmsList)
//    }

    private fun getFunctionName(functionId: Int): String {
        return when (functionId) {
            0 -> "Workout"
            1 -> "Records"
            2 -> "Activity"
            3 -> "Heart rate"
            4 -> "Sleep"
            5 -> "Blood oxygen"
            6 -> "Alarm"
            7 -> "Event reminder"
            8 -> "Stopwatch"
            9 -> "Timer"
            10 -> "Music"
            11 -> "Weather"
            12 -> "Breathing"
            13 -> "Notifications"
            14 -> "Find phone"
            15 -> "Settings"
            16 -> "Women's health"
            17 -> "Stress"
            18 -> "World clock"
            19 -> "Stocks"
            20 -> "Air pressure"
            21 -> "Compass"
            22 -> "ECG"
            23 -> "Temperature"
            24 -> "Phone"
            25 -> "Contacts"
            26 -> "Frequent contacts"
            27 -> "Remote camera"
            32 -> "Activity"
            else -> ""
        }
    }

    private fun getSportName(functionId: Int, noiseFitDevice: ColorFitDevice): String {
        LOGS.d("getSportName $functionId")
        return when (functionId) {
            3 -> SportActivityName.INDOOR_RUNNING
            66 -> {
                SportActivityName.TREADMILL
            }

            2 -> SportActivityName.OUTDOOR_WALKING
            1 -> SportActivityName.OUTDOOR_RUNNING
            4 -> SportActivityName.TREKKING
            5 -> SportActivityName.TRAIL_RUNNING

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
            134 -> SportActivityName.GOLF

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
            135 -> SportActivityName.INDOOR_WALKING
            136 -> SportActivityName.TABLE_FOOTBALL
            137 -> SportActivityName.SEVEN_STONES
            138 -> SportActivityName.KHO_KHO

            else -> ""
        }
    }


    fun parseStress(p0: PressureModeBean?): SedentaryData {
        val sedentaryData = SedentaryData(status = false)
        p0?.let {
            sedentaryData.status = it.pressureMode
        }
        return sedentaryData
    }

    fun convertStress(p0: SedentaryData): PressureModeBean {
        val pressureModeBean = PressureModeBean()
        pressureModeBean.pressureMode = p0.status
        pressureModeBean.relaxationReminder = true
        return pressureModeBean
    }

    fun convertAppList(widgetList: List<WidgetBean>?): List<Widget> {
        val appList = ArrayList<Widget>()
        widgetList?.forEach { widgetBean ->
            LOGS.d("convertAppList ${getFunctionName(widgetBean.functionId)} ${widgetBean.functionId}")
            appList.add(
                Widget(
                    widgetBean.functionId,
                    getFunctionName(widgetBean.functionId),
                    widgetBean.haveHide,
                    widgetBean.isEnable,
                    widgetBean.order,
                    widgetBean.sortable
                )
            )
        }
        return appList
    }


    fun parseSportsMode(
        functionInfo: MutableList<WidgetBean>?,
        noiseFitDevice: ColorFitDevice
    ): ArrayList<Widget> {
        val sportsModeList = ArrayList<Widget>()
        functionInfo?.forEach { widgetBean ->
            sportsModeList.add(
                Widget(
                    widgetBean.functionId,
                    getSportName(widgetBean.functionId, noiseFitDevice),
                    widgetBean.haveHide,
                    widgetBean.isEnable,
                    widgetBean.order,
                    widgetBean.sortable
                )
            )
        }

        return sportsModeList
    }

    private fun defaultSportMode(functionId: Int): Boolean {
        return when (functionId) {
            2, 25, 1, 66, 6, 7, 12, 23, 35, 24 -> {
                true
            }

            else -> {
                false
            }
        }
    }

    fun convertAppToWidgetList(appList: List<Widget>): List<WidgetBean> {
        val widgetList = ArrayList<WidgetBean>()

        appList.forEachIndexed { index, app ->
            val widgetBean = WidgetBean()
            widgetBean.functionId = app.functionId
            widgetBean.haveHide = app.haveHide
            widgetBean.isEnable = app.isEnable
            widgetBean.order = index + 1
            widgetBean.sortable = app.sortable
            widgetList.add(widgetBean)
        }
        return widgetList
    }


    fun formatReminderData(list: List<EventInfoBean>): ReminderList {
        val alarmsList = ArrayList<ReminderList.Reminder>()
        list.forEachIndexed { index, item ->
            val reminder = ReminderList.Reminder(
                hour = item.time.hour,
                minute = item.time.minute,
                year = item.time.year,
                month = item.time.month,
                day = item.time.day,
                label = item.description,
                id = index + 1
            )
            alarmsList.add(reminder)
        }
        return ReminderList(reminders = alarmsList)
    }

    fun parseDoNotDisturb(bean: DoNotDisturbModeBean): DoNotDisturb {
        val dndPara = DoNotDisturb()
        dndPara.startHour = bean.startTime.hour
        dndPara.startMinute = bean.startTime.minuter
        dndPara.endHour = bean.endTime.hour
        dndPara.endMinute = bean.endTime.minuter
        dndPara.status = bean.isSwitch
        CommonGlobals.smartDndSwitch = bean.isSmartSwitch
        return dndPara
    }

    fun getMeasureType(type: Int): ManualMeasureType {
        return when (type) {
            ActiveMeasureCallBack.MeasureType.BLOOD_OXYGEN.type -> {
                ManualMeasureType.BLOOD_OXYGEN
            }

            ActiveMeasureCallBack.MeasureType.HEART_RATE.type -> {
                ManualMeasureType.HEART_RATE
            }

            ActiveMeasureCallBack.MeasureType.BODY_TEMPERATURE.type -> {
                ManualMeasureType.BODY_TEMPERATURE
            }

            ActiveMeasureCallBack.MeasureType.STRESS.type -> {
                ManualMeasureType.STRESS
            }

            else -> {
                ManualMeasureType.HEART_RATE
            }
        }
    }

    fun getManualMeasurement(
        manualMeasureType: ManualMeasureType,
        status: Boolean
    ): ActiveMeasureParamsBean {
        val activeMeasureParamsBean = ActiveMeasureParamsBean()
        activeMeasureParamsBean.isSwitchMeasure = status

        val type = when (manualMeasureType) {
            ManualMeasureType.BLOOD_OXYGEN -> {
                ActiveMeasureCallBack.MeasureType.BLOOD_OXYGEN.type
            }

            ManualMeasureType.HEART_RATE -> {
                ActiveMeasureCallBack.MeasureType.HEART_RATE.type
            }

            ManualMeasureType.BODY_TEMPERATURE -> {
                ActiveMeasureCallBack.MeasureType.BODY_TEMPERATURE.type
            }

            ManualMeasureType.STRESS -> {
                ActiveMeasureCallBack.MeasureType.STRESS.type
            }
        }

        activeMeasureParamsBean.measureType = type

        return activeMeasureParamsBean
    }


    fun parseContinuousBloodOxygenData(bean: ContinuousBloodOxygenBean): OreoBloodOxygenBreakup {
        val startDayTimeStamp =
            DateFormats.convertDateTimeToTimeStamp(bean.date, DateFormats.dateTimeFormat5())

        val stressData = OreoBloodOxygenBreakup()
        stressData.date = DateFormats.dateFormat3().format(startDayTimeStamp)
        stressData.breakUp = gson.toJson(bean.bloodOxygenData)
        return stressData

    }


    fun parseStressData(bean: ContinuousPressureBean): OreoStressDataBreakup {
        val startDayTimeStamp =
            DateFormats.convertDateTimeToTimeStamp(bean.date, DateFormats.dateTimeFormat5())

        val stressData = OreoStressDataBreakup()
        stressData.date = DateFormats.dateFormat3().format(startDayTimeStamp)

        //stressData.breakUp = gson.toJson(bean.pressureData)
        val averageOutData = getAveragedOutHrvData(bean.pressureData)
        stressData.breakUp = gson.toJson(averageOutData)

        /*val lastDayDate = DateFormats.getYesterdayDate()
        LOGS.w("lastDayDate $lastDayDate")

        if (lastDayDate.equals(stressData.date, true)) {
            val avgData = averageOutData.averageWithoutZero()
            AppLogs.sendAppLogs("Saving last day average data :$avgData for ${stressData.date}")
            watchDataStore.setLastSavedAverageHrv(avgData)
        }*/

        return stressData
    }

    private fun getAveragedOutHrvData(pressureData: MutableList<Int>): List<Int> {
        val filteredData = ArrayList<Int>()

        val lastSavedHrv = watchDataStore.getLastSavedAverageHrv()

        filteredData.addAll(algoAvgLastThreeZeroValues(pressureData, lastSavedHrv))

       /* val stringBuilder = StringBuilder()

        filteredData.forEachIndexed { index, i ->

            stringBuilder.append("${pressureData[index]} -> $i\n")
        }*/
        //LOGS.w("filtered_data -> $stringBuilder")
        //AppLogs.sendAppLogs("geAveragedOutHrvData -> $stringBuilder")

        return filteredData
    }

    private fun algoAvgLastThreeZeroValues(response: List<Int>, lastAvgValue: Int = 0): List<Int> {
        val filteredData = ArrayList<Int>()

        response.forEachIndexed { index, value ->
            if (index in 0..2) {
                if (value > 120) {//120 is max value
                    filteredData.add(lastAvgValue)//replace with last day average hrv if available
                } else {
                    filteredData.add(value)
                }
            } else {
                if (value > 0) {
                    val lastValueFilteredData = filteredData[index - 1]

                    if (value >= (lastValueFilteredData * 1.5) && lastValueFilteredData > 0) {
                        val result = getLastThreeNonZeroValues(filteredData, index)
                        if (result == -1) {
                            filteredData.add(value)
                        } else {
                            filteredData.add(result)
                        }
                    } else {
                        filteredData.add(value)
                    }
                } else {
                    filteredData.add(value)
                }
            }
        }
        return filteredData
    }

    private fun getLastThreeNonZeroValues(arr: List<Int>, currentPosition: Int): Int {
        val result = mutableListOf<Int>()
        var count = 0
        var index = currentPosition
        try {
            while (count < 3 && index > 0) {
                val value = arr[index - 1]
                if (value != 0) {
                    result.add(value)
                    count++
                }
                index--
            }
            return (1.2f * result.averageWithoutZero()).roundToInt()
        } catch (exp: Exception) {
            exp.printStackTrace()
            return -1
        }
    }

    fun parseRespiratoryData(bean: TodayRespiratoryRateData): OreoRespiratoryData {
        val startDayTimeStamp =
            DateFormats.convertDateTimeToTimeStamp(bean.date, DateFormats.dateTimeFormat5())

        val respiratoryData = OreoRespiratoryData()
        respiratoryData.date = DateFormats.dateFormat3().format(startDayTimeStamp)
        respiratoryData.breakUp = gson.toJson(bean.data)
        return respiratoryData
    }

    fun parseBodyStressData(bean: RingStressDetectionBean): OreoBodyStressData {
        val startDayTimeStamp =
            DateFormats.convertDateTimeToTimeStamp(bean.date, DateFormats.dateTimeFormat5())

        val bodyStressData = OreoBodyStressData()
        bodyStressData.date = DateFormats.dateFormat3().format(startDayTimeStamp)
        bodyStressData.breakUp = gson.toJson(bean.data)
        return bodyStressData
    }


    fun parseDayTimeMovementData(bean: OverallDayMovementData): DayTimeMovementBreakup {
        val startDayTimeStamp =
            DateFormats.convertDateTimeToTimeStamp(bean.date, DateFormats.dateTimeFormat5())

        val data = DayTimeMovementBreakup()
        data.date = DateFormats.dateFormat3().format(startDayTimeStamp)
        data.breakUp = gson.toJson(bean.data)
        return data
    }

    fun parseBodyTemperature(bean: ContinuousTemperatureBean): OreoBodyTemperatureBreakup {
        val startDayTimeStamp =
            DateFormats.convertDateTimeToTimeStamp(bean.date, DateFormats.dateTimeFormat5())

        val celciusTempList = bean.temperatureData

        val fahrenheitList = celciusTempList.map {
            AppConversionUtils.celsiusToFahrenheit(
                AppConversionUtils.getOreoCentigradeBody(it)
            ).upTo1Decimal()
        }

        val oxygenItem = OreoBodyTemperatureBreakup()
        oxygenItem.date = DateFormats.dateFormat3().format(startDayTimeStamp)
        oxygenItem.breakUp = gson.toJson(fahrenheitList)

        return oxygenItem
    }


    fun parseHeartRateData(bean: ContinuousHeartRateBean): OreoHeartRate {
        val startDayTimeStamp =
            DateFormats.convertDateTimeToTimeStamp(bean.date, DateFormats.dateTimeFormat5())

        val heartRate = OreoHeartRate()
        heartRate.date = DateFormats.dateFormat3().format(startDayTimeStamp)
        heartRate.breakUp = gson.toJson(bean.heartRateData)
        return heartRate
    }


    fun parseStepsData(dailyBean: DailyBean): OreoStepsData {
        LOGS.d("getStepsData $dailyBean")
        val dailyStepData = OreoStepsData(
            date = DateFormats.getConvertToDateFormat(
                dailyBean.date,
                DateFormats.dateTimeFormat5(),
                DateFormats.dateFormat3()
            )
        )

        val stepArray = ArrayList<OreoStepsData.OreoStepDataBreakup>()
        var steps = 0
        var distance = 0
        var calories = 0
        var activeCalories = 0

        dailyBean.stepsData?.forEachIndexed { i, data ->
            val stepData = OreoStepsData.OreoStepDataBreakup(hourOfTheDay = i)
            stepData.steps = data
            stepData.calories = dailyBean.todayOuraCalorieHourlyData[i]
            stepData.distance = dailyBean.distanceData[i]
            stepData.activeCalories = dailyBean.todaySportCalorieHourlyData[i]
            distance += dailyBean.distanceData[i]
            //activeCalories += dailyBean.todaySportCalorieHourlyData[i]
            steps += data
            stepArray.add(stepData)
        }
        calories = dailyBean.todayOuraCalorieData
        dailyStepData.totalDistance = distance
        dailyStepData.totalCalories = calories
        dailyStepData.totalSteps = steps
        dailyStepData.activeCalories = dailyBean.todaySportCalorieData/*activeCalories*/
        dailyStepData.stepArray = stepArray
        LOGS.d("getStepsData $dailyStepData")
        return dailyStepData
    }


    fun parseSportsDataGPS(
        p1: DevSportInfoBean,
        colorFitDevice: ColorFitDevice
    ): SportsModeListGPS {
        val sportsModeList = SportsModeListGPS(responseType = "final")
        val activities = ArrayList<SportsModeResponse>()
//        LOGS.d("GPS_DATA", "getGPSData onfail" + Gson().toJson(p1))
//        LOGS.d("GPS_DATA", "getGPSData onfail" + Gson().toJson(p1.map_data))
//        LOGS.d("GPS_DATA", "getGPSData onfail" + Gson().toJson(p1.recordPointSportData))
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
                type = getSportName(p1.recordPointSportType, colorFitDevice),
                calories = p1.reportCal,
                heartRateAvg = p1.reportAvgHeart,
                endTime = DateFormats.timeFormat().format(endCalendar.time),
                date = DateFormats.dateFormatOld().format(startCalendar.time),
                time = DateFormats.formatDateTime(
                    startCalendar.time,
                    DateFormats.dateTimeFormatISO()
                ),
                duration = duration.toLong(),
                heartRateData = hrData.handleHrData(duration),
                calorieData = calorieData.handleCaloriesData(duration)
            )

            //   val timeData = p1.recordGpsTime.split(",")

//            val gpsDataLinkedList = getGpsMapsData(p1.map_data)
            val gpsParseData = parseGpsMapsData(p1.map_data, p1.recordGpsTime)


//            LOGS.d("gpsParseData ${Gson().toJson(gpsParseData)}")
            //  watchDataStore.getWeatherDataModel(startCalendar.timeInMillis, endCalendar.timeInMillis)
            if (gpsParseData.isEmpty()) {
                sportsModeResponse.gpsCoordinate = null
            } else {
                sportsModeResponse.gpsCoordinate = listToJson(gpsParseData)
            }




            sportsModeResponse.hrZoneInSeconds = 1
            sportsModeResponse.aerobic = (p1.reportHeartAerobic).toInt()
            sportsModeResponse.anaerobic = (p1.reportHeartAnaerobic).toInt()
            sportsModeResponse.fatBurn = (p1.reportHeartFatBurning).toInt()
            sportsModeResponse.warmUp = (p1.reportHeartWarmUp).toInt()

            if (p1.reportTotalStep.toInt() != 0) {
                sportsModeResponse.cadence = p1.reportMaxStepSpeed
                sportsModeResponse.steps = p1.reportTotalStep.toInt()
            }

            sportsModeResponse.distance = p1.reportDistance

            if (p1.reportDistance.toInt() != 0 && duration != 0) {
                val df = DecimalFormat("#.#", DecimalFormatSymbols(Locale.US))
                val speed = p1.reportFastSpeed
                sportsModeResponse.speed = df.format(speed)?.toFloat()
            } else {
                sportsModeResponse.speed = 0F
                sportsModeResponse.cadence = 0
            }


            activities.add(sportsModeResponse)
        }

//        LOGS.d("GPS_DATA", "getGPSData onfail" + Gson().toJson(activities))
        sportsModeList.activities = activities
        return sportsModeList
    }


    private fun parseGpsMapsData(
        mapsData: String?,
        gpsTimePoints: String?
    ): ArrayList<DoubleArray> {

        val gpsList = ArrayList<DoubleArray>()
        mapsData?.let { mapData ->
            val mapsDataArray = mapData.split(";")
//            val timePointArray = gpsTimePoints?.split(",")
            var lastLat = 0.0
            var lastLog = 0.0
//            var currentDistance = 0
            mapsDataArray.forEachIndexed { index, mapDataArray ->
                val splitMapDataArray = mapDataArray.split(",")
                if (splitMapDataArray.size == 2) {
                    val dataList = ArrayList<Double>()
                    val lat = splitMapDataArray[1].toDouble()
                    val log = splitMapDataArray[0].toDouble()
//                    val gpsTime = timePointArray?.get(index)?.toDouble() ?: 0.0
//                    LOGS.d("sjhkadshkjsadhksa ${gpsTime} ${timePointArray?.get(index)}")
                    if (lastLat != lat && lastLog != log) {
                        dataList.add(lat)
                        dataList.add(log)
//                        dataList.add(gpsTime)
                        gpsList.add(dataList.toDoubleArray())
                        lastLat = lat
                        lastLog = log
                    }


                }
            }
        }

        return gpsList
    }

    private fun validate(s: String): Double {
        val str = "1011.99"
        val amount1 = BigDecimal(str).toDouble()
        try {
            return java.lang.Double.valueOf(s)
        } catch (nfe: NumberFormatException) {
            return 0.0
            // you can throw your custom exception here.
        }
    }

    private fun listToJson(value: ArrayList<DoubleArray>) = Gson().toJson(value)
    fun formatNotificationMessage(message: String): String {
        val emoji = "🎥"
        return message.replace(emoji, "")
    }

    fun getPackageName(appType: String): String {
        return when (appType) {
            ApplicationType.WHATS_APP_BUSINESS.type -> "com.whatsapp.w4b"
            ApplicationType.WHATS_APP.type -> "com.whatsapp"
            ApplicationType.TELEGRAM.type -> "org.telegram.messenger"
            ApplicationType.SNAPCHAT.type -> "com.snapchat.android"
            ApplicationType.SLACK.type -> "com.Slack"
            ApplicationType.YOUTUBE.type -> "com.google.android.youtube"
            ApplicationType.OUTLOOK.type -> "com.microsoft.office.outlook"
            ApplicationType.INSTAGRAM.type -> "com.instagram.android"
            ApplicationType.LINKED_IN.type -> "com.linkedin.android"
            ApplicationType.GMAIL.type -> "com.google.android.gm"
            ApplicationType.VIBER.type -> "com.viber.voip"
            ApplicationType.FACEBOOK.type -> "com.facebook.katana"
            ApplicationType.FB_MESSENGER.type -> "com.facebook.orca"
            ApplicationType.SKYPE.type -> "com.skype.raider"
            ApplicationType.TWITTER.type -> "com.twitter.android"
            ApplicationType.WE_CHAT.type -> "com.tencent.mm"
            ApplicationType.GOOGLE_MAPS.type -> "com.google.android.apps.maps"
            ApplicationType.CALENDAR.type -> "com.google.android.calendar"
            ApplicationType.PINTEREST.type -> "com.pinterest"
            ApplicationType.AMAZON.type -> "in.amazon.mShop.android.shopping"
            ApplicationType.FLIPKART.type -> "com.flipkart.android"
            ApplicationType.GOOGLE_NEWS.type -> "com.google.android.apps.magazines"
            ApplicationType.GOOGLE_PAY.type -> "com.google.android.apps.nbu.paisa.user"
            ApplicationType.HANGOUTS.type -> "com.google.android.talk"
            ApplicationType.OLA.type -> "com.olacabs.customer"
            ApplicationType.PAYTM.type -> "net.one97.paytm"
            ApplicationType.PHONEPE.type -> "com.phonepe.app"
            ApplicationType.UBER.type -> "com.ubercab"
            ApplicationType.NAUKRI.type -> "naukriApp.appModules.login"
            ApplicationType.INSHORTS.type -> "com.nis.app"
            ApplicationType.NOISEFIT.type -> "com.noisefit"
            else -> ""
        }
    }

    fun formatContactList(contactBeanList: List<ContactBean>): ArrayList<Contact> {
        val contactList = ArrayList<Contact>()
        contactBeanList.forEach { contactsBean ->
            contactList.add(
                Contact(
                    contactsBean.contacts_number,
                    contactsBean.contacts_name,
                    true,
                    null,
                    arrayListOf(contactsBean.contacts_number)
                )
            )
        }

        return contactList
    }

    fun formatWorldClockList(clockBeanList: List<WorldClockBean>): ArrayList<WorldClockList.WClock> {
        val data = ArrayList<WorldClockList.WClock>()
        clockBeanList.forEach { item ->
            data.add(
                WorldClockList.WClock(
                    timeZone = item.offset,
                    content = item.cityName
                )
            )
        }
        return data
    }

    //onRingSleepNAP : [RingSleepNapBean{existSleepNap=true, asleepNapTime=1705284882,
    // wakeupNapTime=1705286418, sleepNapDuration=1536, date='2024-01-15 00:00:00'}]
    fun parseNapData(naps: List<RingSleepNapBean>): List<OreoNapData> {
        val returnNaps = ArrayList<OreoNapData>()
        val napAddedTimeStamps = HashMap<Long, Int>()

        val date = DateFormats.getDateFromTimeStamp(
            DateFormats.subtractDate(
                System.currentTimeMillis(),
                1
            )
        )
        val minTimestamp =
            DateFormats.convertDateTimeToTimeStamp(date ?: "", DateFormats.dateFormat3())

        naps.forEach {
            val startTimeStamp = it.asleepNapTime.toLong() * 1000

            val nap = OreoNapData().apply {
                this.startTime = DateFormats.convertTimestampToDate(
                    startTimeStamp,
                    DateFormats.dateTimeFormat5()
                )
                this.endTime = DateFormats.convertTimestampToDate(
                    it.wakeupNapTime.toLong() * 1000,
                    DateFormats.dateTimeFormat5()
                )
                this.duration = it.sleepNapDuration / 60
                this.date = DateFormats.convertTimestampToDate(
                    it.asleepNapTime.toLong() * 1000,
                    DateFormats.dateFormat3()
                )
            }

            if (startTimeStamp >= minTimestamp) {
                val oldDuration = napAddedTimeStamps[startTimeStamp]
                if (oldDuration != null) {
                    val newDuration = nap.duration

                    if (newDuration > oldDuration) {

                        val removeIndex = returnNaps.indexOfFirst {
                            it.startTime.equals(nap.startTime)
                        }
                        if (removeIndex != -1) {
                            returnNaps.removeAt(removeIndex)
                            returnNaps.add(nap)
                            napAddedTimeStamps[startTimeStamp] = newDuration
                            AppLogs.sendAppLogs("Nap replaced, $newDuration - $oldDuration ")
                        }
                    } else {
                        AppLogs.sendAppLogs("Nap ignored Duration low $startTimeStamp")
                    }
                } else {
                    returnNaps.add(nap)
                    napAddedTimeStamps[startTimeStamp] = nap.duration
                }
            } else {
                AppLogs.sendAppLogs("Nap ignored old date $it ")
            }

            /*try {
                val startHour = DateFormats.convertTimestampToDate(
                    it.asleepNapTime.toLong() * 1000,
                    DateFormats.timeFormatHour
                ).toInt()

                if (it.asleepNapTime.toLong() * 1000 >= timestamp) {
                   *//* if (startHour in 10..19) {*//*
                        returnNaps.add(nap)
                    *//*} else {
                        AppLogs.sendAppLogs("Nap ignored $it")
                    }*//*
                } else {
                    AppLogs.sendAppLogs("Nap ignored old date $it ")
                }
            } catch (exp: Exception) {
                returnNaps.add(nap)
            }*///OLD condition
        }
        return returnNaps
    }

    fun parseSleepData(bean: RingSleepResultBean): OreoSleepData? {
        val sleepData = OreoSleepData(availableSleepTypes = "deep;light;awake;rem")

        val sleepArray = ArrayList<OreoSleepData.OreoSleepDataBreakup>()

        /*val sleepStartInBtw = DateFormats.convertTimestampToDate(
            bean.entryTime.toLong() * 1000,
            DateFormats.timeFormatHour()
        ).toInt() //00,01,02....23

        if (sleepStartInBtw in 8..18) {
            AppLogs.sendAppLogs("$TRACK_TAG Parsed Sleep Data invalid interval $bean")
            return null
        }*/
        sleepData.startTime = DateFormats.convertTimestampToDate(
            bean.entryTime.toLong() * 1000,
            DateFormats.timeFormatSleepTime()
        )
        sleepData.endTime = DateFormats.convertTimestampToDate(
            bean.exitTime.toLong() * 1000,
            DateFormats.timeFormatSleepTime()
        )

        sleepData.sleepScore = bean.sleepScore
        sleepData.sleepLatency = bean.sleepLatency
        sleepData.sleepEfficiency = bean.sleepEfficiency

        sleepData.date = DateFormats.convertTimestampToDate(
            bean.exitTime.toLong() * 1000,
            DateFormats.dateFormat3()
        )

        sleepData.total = bean.sleepDuration
        sleepData.timeInBedTime = bean.timeInBedTime
        sleepData.awake = bean.awakeTime
        sleepData.light = bean.lightSleepTime
        sleepData.deep = bean.deepSleepTime
        sleepData.remCount = bean.rapidEyeMovementTime
        sleepData.startTimeStamp = bean.entryTime.toLong() * 1000

        bean.sleepDistributionData.forEach { sleepDistributionData ->
            val sleepBreakup = OreoSleepData.OreoSleepDataBreakup(
                sleepType = when (sleepDistributionData.sleepDistributionType) {
                    0 -> SleepType.AWAKE.type
                    1 -> SleepType.LIGHT.type
                    2 -> SleepType.DEEP.type
                    3 -> SleepType.REM.type
                    else -> SleepType.AWAKE.type
                }
            )
            sleepBreakup.startTime = DateFormats.convertTimestampToDate(
                sleepDistributionData.startTimestamp.toLong() * 1000,
                DateFormats.dateTimeFormat5()
            )
            sleepBreakup.duration = sleepDistributionData.sleepDuration
            val endTime = DateFormats.addSecondsInMilliseconds(
                (sleepDistributionData.startTimestamp.toLong() * 1000),
                sleepDistributionData.sleepDuration
            )

            sleepBreakup.endTime = DateFormats.convertTimestampToDate(
                endTime,
                DateFormats.dateTimeFormat5()
            )
            sleepArray.add(sleepBreakup)
        }


        val nightTimeMovement = ArrayList<OreoSleepData.OreoSleepMovementDataBreakup>()

        bean.sleepMovementsData.forEach { nightMovementData ->

            val sleepBreakup = OreoSleepData.OreoSleepMovementDataBreakup(
                movementType = when (nightMovementData.sleepMovementsType) {
                    0 -> SleepMovementType.NO_MOVEMENT.name
                    1 -> SleepMovementType.LOW.name
                    2 -> SleepMovementType.MEDIUM.name
                    3 -> SleepMovementType.INTENSE.name
                    else -> SleepMovementType.NO_MOVEMENT.name
                }
            )
            sleepBreakup.startTime = DateFormats.convertTimestampToDate(
                nightMovementData.startTimestamp.toLong() * 1000,
                DateFormats.dateTimeFormat5()
            )
            sleepBreakup.duration = nightMovementData.sleepDuration
            val endTime = DateFormats.addSecondsInMilliseconds(
                (nightMovementData.startTimestamp.toLong() * 1000),
                nightMovementData.sleepDuration
            )

            sleepBreakup.endTime = DateFormats.convertTimestampToDate(
                endTime,
                DateFormats.dateTimeFormat5()
            )
            nightTimeMovement.add(sleepBreakup)

        }

        sleepData.sleepArray = sleepArray
        sleepData.nightTimeMovement = nightTimeMovement

        return sleepData
    }

}

