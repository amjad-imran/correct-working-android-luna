package com.noisefit_zhsdk.handler

import android.content.Context
import android.location.Geocoder
import com.google.gson.Gson
import com.noisefit_commans.constants.CommonGlobals
import com.noisefit_commans.constants.SportActivityName
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.data.model.OreoStepsData
import com.noisefit_commans.data.model.RecordedWorkoutData
import com.noisefit_commans.enums.ApplicationType
import com.noisefit_commans.models.AlarmsList
import com.noisefit_commans.models.BloodOxygenBreakup
import com.noisefit_commans.models.BodyTemperatureBreakup
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.Contact
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.DoNotDisturb
import com.noisefit_commans.models.HeartRate
import com.noisefit_commans.models.ReminderList
import com.noisefit_commans.models.SOSContact
import com.noisefit_commans.models.SedentaryData
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.models.SleepReminder
import com.noisefit_commans.models.SleepType
import com.noisefit_commans.models.StepsData
import com.noisefit_commans.models.StressDataBreakup
import com.noisefit_commans.models.Widget
import com.noisefit_commans.models.WorldClockList
import com.noisefit_commans.utils.AppConversionUtils
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.zhapp.ble.bean.AutoActiveSportBean
import com.zhapp.ble.bean.AutoSportDataBean
import com.zhapp.ble.bean.ClockInfoBean
import com.zhapp.ble.bean.ContactBean
import com.zhapp.ble.bean.ContinuousHeartRateBean
import com.zhapp.ble.bean.ContinuousPressureBean
import com.zhapp.ble.bean.DailyBean
import com.zhapp.ble.bean.DevSportInfoBean
import com.zhapp.ble.bean.DoNotDisturbModeBean
import com.zhapp.ble.bean.EmergencyContactBean
import com.zhapp.ble.bean.EventInfoBean
import com.zhapp.ble.bean.OfflineBloodOxygenBean
import com.zhapp.ble.bean.OfflinePressureDataBean
import com.zhapp.ble.bean.OfflineTemperatureDataBean
import com.zhapp.ble.bean.PressureModeBean
import com.zhapp.ble.bean.RealTimeBean
import com.zhapp.ble.bean.SettingTimeBean
import com.zhapp.ble.bean.SleepBean
import com.zhapp.ble.bean.WidgetBean
import com.zhapp.ble.bean.WorldClockBean
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class DataConverter
@Inject
constructor(
    var context: Context,
    var gson: Gson,
    var watchDataStore: WatchDataStore,
    private val geoCoder: Geocoder
) {
    fun formatAlarmData(list: List<ClockInfoBean>): AlarmsList {
        val alarmsList = ArrayList<AlarmsList.Alarm>()
        list.forEach { alarmItem ->
            alarmsList.add(
                AlarmsList.Alarm(
                    hour = alarmItem.data.time.hour,
                    minute = alarmItem.data.time.minuter,
                    status = alarmItem.data.isEnable,
                    repeatDays = arrayListOf(
                        alarmItem.data.isEnable,
                        alarmItem.data.isMonday,
                        alarmItem.data.isTuesday, alarmItem.data.isWednesday,
                        alarmItem.data.isThursday, alarmItem.data.isFriday,
                        alarmItem.data.isSaturday, alarmItem.data.isSunday
                    ),
                    id = alarmItem.id
                )
            )
        }


        return AlarmsList(alarms = alarmsList)
    }

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
            1 -> {
                SportActivityName.WALKING
            }

            2 -> {
                SportActivityName.RUNNING
            }

            else -> {
                SportActivityName.WALKING
            }
        }

        /* return when (functionId) {
             3 -> SportActivityName.RUNNING
             66 -> {
                 when (noiseFitDevice.deviceType) {
                     DeviceType.COLORFIT_PRO_4_ALPHA.deviceType -> {
                         return SportActivityName.INDOOR_RUNNING
                     }

                     else -> {
                         SportActivityName.TREADMILL
                     }
                 }
             }

             2 -> SportActivityName.WALKING
             1 -> SportActivityName.RUNNING
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

             84 -> {
                 when (noiseFitDevice.deviceType) {
                     DeviceType.NOISEFIT_ARC.deviceType, DeviceType.NOISEFIT_TWIST.deviceType, DeviceType.NOISEFIT_CURVE.deviceType -> {
                         return SportActivityName.CROSS_TRAINING
                     }

                     else -> {
                         SportActivityName.CROSS_TRAINING_CROSSFIT
                     }
                 }

             }

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
             130 -> {
                 when (noiseFitDevice.deviceType) {
                     DeviceType.NOISEFIT_ARC.deviceType, DeviceType.NOISEFIT_TWIST.deviceType, DeviceType.NOISEFIT_CURVE.deviceType -> {
                         return SportActivityName.DOUBLE_BOARD_SKATING
                     }

                     else -> {
                         SportActivityName.DOUBLE_BOARD_SKIING
                     }
                 }
             }

             131 -> SportActivityName.FREE_EXERCISE
             132 -> {
                 when (noiseFitDevice.deviceType) {
                     DeviceType.NOISEFIT_ARC.deviceType, DeviceType.NOISEFIT_TWIST.deviceType, DeviceType.NOISEFIT_CURVE.deviceType -> {
                         return SportActivityName.PADDLEBOARDS
                     }

                     else -> {
                         SportActivityName.PADDLEBOARD_SURFING
                     }
                 }
             }

             133 -> SportActivityName.KABADDI
             200 -> SportActivityName.POOL_SWIMMING
             204 -> SportActivityName.TRIATHLON
             135 -> SportActivityName.INDOOR_WALKING
             136 -> SportActivityName.TABLE_FOOTBALL
             137 -> SportActivityName.SEVEN_STONES
             138 -> SportActivityName.KHO_KHO

             else -> ""
         }*/
    }


    fun parseStress(p0: PressureModeBean?): SedentaryData {
        val sedentaryData = SedentaryData(status = false)
        p0?.let {
            sedentaryData.status = it.pressureMode
        }
        return sedentaryData
    }

    fun convertSleepReminder(data: SleepReminder): com.zhapp.ble.bean.SleepReminder {
        val sReminder = com.zhapp.ble.bean.SleepReminder()
        sReminder.isOn = data.status
        val settingTimeBean = SettingTimeBean(data.hour, data.minute)
        sReminder.reminderTime = settingTimeBean
        return sReminder
    }

    fun formatSleepReminder(sleepReminder: com.zhapp.ble.bean.SleepReminder): SleepReminder {

        return SleepReminder(
            sleepReminder.isOn,
            sleepReminder.reminderTime.hour,
            sleepReminder.reminderTime.minuter,
            sleepReminder.reminderTime.second,
            sleepReminder.reminderTime.millisecond,
        )
    }

    fun parseAutoSport(
        data: AutoActiveSportBean,
        colorFitDevice: ColorFitDevice
    ): List<OreoAutoSportData> {
        val result = ArrayList<OreoAutoSportData>()

        data.sportData.forEach {
            val oreoAutoSportData = OreoAutoSportData()
            oreoAutoSportData.steps = it.autoActiveSteps
            oreoAutoSportData.startTime = it.autoActiveStartTime * 1000L
            oreoAutoSportData.intensity = it.autoActiveSportLevel
            oreoAutoSportData.isAccepted = false
            oreoAutoSportData.duration = it.autoActiveDuration
            oreoAutoSportData.calories = it.autoSctiveKcal
            oreoAutoSportData.type = getSportName(it.autoActiveSportType, colorFitDevice)
            result.add(oreoAutoSportData)
        }

        return result
    }


    fun parseAutoSport(
        p0: MutableList<AutoSportDataBean>?,
        colorFitDevice: ColorFitDevice
    ): List<OreoAutoSportData> {
        val data = ArrayList<OreoAutoSportData>()
        p0?.forEach {
            val oreoAutoSportData = OreoAutoSportData()
            oreoAutoSportData.steps = it.autoSportSteps
            oreoAutoSportData.startTime = it.autoSportStartTime * 1000L
            oreoAutoSportData.intensity = it.autoSportIntensity
            oreoAutoSportData.isAccepted = false
            oreoAutoSportData.duration = it.autoSportDuration
            oreoAutoSportData.calories = it.autoSportKcal
            oreoAutoSportData.type = getSportName(it.autoSportType, colorFitDevice)
            oreoAutoSportData.hrData = gson.toJson(it.hrData)
            data.add(oreoAutoSportData)
        }
        return data
    }

    fun convertStress(p0: SedentaryData): PressureModeBean {
        val pressureModeBean = PressureModeBean()
        pressureModeBean.pressureMode = p0.status
        pressureModeBean.relaxationReminder = true
        return pressureModeBean
    }

    fun convertWidgetList(
        widgetList: List<WidgetBean>?,
        noiseFitDevice: ColorFitDevice?
    ): List<Widget> {
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

    fun convertAppList(
        widgetList: List<WidgetBean>?
    ): List<Widget> {
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


    fun parseSleepData(bean: SleepBean): SleepData {

        val sleepData = SleepData(availableSleepTypes = "deep;light;awake;rem")

        val sleepArray = ArrayList<SleepData.SleepDataBreakup>()

        sleepData.startTime = DateFormats.convertTimestampToDate(
            bean.startSleepTimestamp * 1000,
            DateFormats.timeFormat
        )
        sleepData.endTime = DateFormats.convertTimestampToDate(
            bean.endSleepTimestamp * 1000,
            DateFormats.timeFormat
        )

        sleepData.sleepScore = bean.sleepScore
        sleepData.date = DateFormats.convertTimestampToDate(
            bean.endSleepTimestamp * 1000,
            DateFormats.dateFormat
        )

        sleepData.total = bean.sleepDuration
        sleepData.awake = bean.awakeTime
        sleepData.light = bean.lightSleepTime
        sleepData.deep = bean.deepSleepTime
        sleepData.remCount = bean.rapidEyeMovementTime

        bean.list.forEach { sleepDistributionData ->
            val sleepBreakup = SleepData.SleepDataBreakup(
                sleepType = when (sleepDistributionData.sleepDistributionType) {
                    0 -> SleepType.AWAKE.type
                    1 -> SleepType.LIGHT.type
                    2 -> SleepType.DEEP.type
                    3 -> SleepType.REM.type
                    else -> SleepType.AWAKE.type
                }
            )
            sleepBreakup.startTime = DateFormats.convertTimestampToDate(
                sleepDistributionData.startTimestamp * 1000,
                DateFormats.timeFormat
            )
            sleepBreakup.duration = sleepDistributionData.sleepDuration
            val endTime = DateFormats.addMinutesInMilliseconds(
                (sleepDistributionData.startTimestamp * 1000),
                sleepDistributionData.sleepDuration
            )

            sleepBreakup.endTime = DateFormats.convertTimestampToDate(
                endTime,
                DateFormats.timeFormat
            )
            sleepArray.add(sleepBreakup)
        }

        sleepData.sleepArray = sleepArray

        return sleepData
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

    fun parseBloodOxygenData(bean: OfflineBloodOxygenBean): ArrayList<BloodOxygenBreakup> {
        val oxygenArray = ArrayList<BloodOxygenBreakup>()
        bean.list.forEach { measureData ->

            if (measureData.measureData > 0) {
                val date = DateFormats.convertTimestampToDate(
                    measureData.measureTimestamp.toLong() * 1000,
                    DateFormats.dateFormat
                )
                val time = DateFormats.convertTimestampToDate(
                    measureData.measureTimestamp.toLong() * 1000,
                    DateFormats.timeFormat
                )
                val syncDate = DateFormats.convertDateTimeToTimeStamp(date, time)

                val oxygenItem = BloodOxygenBreakup(
                    value = measureData.measureData,
                    date = date,
                    time = time,
                    timeStamp = syncDate
                )
                oxygenArray.add(oxygenItem)
            }


        }

        return oxygenArray
    }

    fun parseStressData(bean: OfflinePressureDataBean): ArrayList<StressDataBreakup> {
        val stressArray = ArrayList<StressDataBreakup>()

        bean.list.forEach { measureData ->
            if (measureData.measureData > 0) {
                val date = DateFormats.convertTimestampToDate(
                    measureData.measureTimestamp.toLong() * 1000,
                    DateFormats.dateFormat
                )
                val time = DateFormats.convertTimestampToDate(
                    measureData.measureTimestamp.toLong() * 1000,
                    DateFormats.timeFormat
                )

                val syncDate = DateFormats.convertDateTimeToTimeStamp(date, time)
                val oxygenItem = StressDataBreakup(
                    value = measureData.measureData,
                    date = date,
                    time = time,
                    timeStamp = syncDate
                )
                stressArray.add(oxygenItem)
            }
        }
        return stressArray
    }

    fun parseStressData(bean: ContinuousPressureBean): ArrayList<StressDataBreakup> {
        val stressArray = ArrayList<StressDataBreakup>()

        var startDayTimeStamp =
            DateFormats.convertDateTimeToTimeStamp(bean.date, DateFormats.dateTimeFormat5)


        bean.pressureData.forEach { measureData ->
            startDayTimeStamp =
                DateFormats.addMinuteToTimeStamp(startDayTimeStamp!!, bean.pressureFrequency)
            if (measureData > 0) {
                val date = DateFormats.dateFormat.format(startDayTimeStamp)
                val time = DateFormats.timeFormat.format(startDayTimeStamp)

                val syncDate = DateFormats.convertDateTimeToTimeStamp(date, time)
                val oxygenItem = StressDataBreakup(
                    value = measureData,
                    date = date,
                    time = time,
                    timeStamp = syncDate
                )
                stressArray.add(oxygenItem)
            }
        }
        return stressArray
    }

    fun parseBodyTemperature(bean: OfflineTemperatureDataBean): ArrayList<BodyTemperatureBreakup> {
        val bodyTemperatureList = ArrayList<BodyTemperatureBreakup>()
        bean.list.forEach { measureData ->
            if (measureData.measureData > 0) {
                val date = DateFormats.convertTimestampToDate(
                    measureData.measureTimestamp.toLong() * 1000,
                    DateFormats.dateFormat
                )
                val time = DateFormats.convertTimestampToDate(
                    measureData.measureTimestamp.toLong() * 1000,
                    DateFormats.timeFormat
                )

                val syncDate = DateFormats.convertDateTimeToTimeStamp(date, time)
                bodyTemperatureList.add(
                    BodyTemperatureBreakup(
                        date = date, time = time,
                        value = AppConversionUtils.getCentigradeBody(measureData.measureData),
                        timeStamp = syncDate
                    )
                )
            }

        }

        return bodyTemperatureList
    }

    fun parseHeartRateData(bean: ContinuousHeartRateBean): List<HeartRate> {
        val heartRateList = ArrayList<HeartRate>()
        var startDayTimeStamp =
            DateFormats.convertDateTimeToTimeStamp(bean.date, DateFormats.dateTimeFormat5)

        bean.heartRateData?.let { healthHeartRate ->
            healthHeartRate.forEachIndexed { _, item ->
                startDayTimeStamp =
                    DateFormats.addMinuteToTimeStamp(
                        startDayTimeStamp!!,
                        bean.continuousHeartRateFrequency
                    )

                if (item as Int != 0) {
                    val time = DateFormats.timeFormat.format(startDayTimeStamp)
                    val heartRate = HeartRate()
                    heartRate.resetData = true
                    heartRate.averageHeartRate = item
                    heartRate.date = DateFormats.dateFormat.format(startDayTimeStamp)
                    heartRate.time = time
                    heartRate.timeStamp =
                        DateFormats.convertDateTimeToTimeStamp(heartRate.date!!, time)
                    heartRate.highestHeartRate = bean.max
                    heartRate.lowestHeartRate = bean.min

                    heartRateList.add(heartRate)
                }
            }
        }
        return heartRateList
    }


    fun parseStepsData(dailyBean: RealTimeBean): StepsData {
        LOGS.d("getStepsData $dailyBean")
        val dailyStepData = StepsData(
            date = DateFormats.getCurrentDate(
                DateFormats.dateFormat

            )
        )

        val stepArray = ArrayList<StepsData.StepDataBreakup>()
        var steps = 0
        var distance = 0
        var calories = 0
        LOGS.d("realTimeDAta ${dailyBean.stepsData?.size} ${dailyBean.distanceData?.size} ${dailyBean.calorieData?.size}")
        dailyBean.stepsData?.forEachIndexed { i, data ->

            val stepData = StepsData.StepDataBreakup(hourOfTheDay = i)
            stepData.steps = data
            stepData.calories = dailyBean.calorieData[i]
            stepData.distance = dailyBean.distanceData[i]
            calories += dailyBean.calorieData[i]
            distance += dailyBean.distanceData[i]
            steps += data
            stepArray.add(stepData)
        }
        dailyStepData.totalDistance = dailyBean.distance.toInt()
        dailyStepData.totalCalories = dailyBean.calories.toInt()
        dailyStepData.totalSteps = dailyBean.steps.toInt()
        dailyStepData.stepArray = stepArray
        LOGS.d("getStepsData $dailyStepData")
        return dailyStepData
    }

    fun parseStepsData(dailyBean: DailyBean): StepsData {
        LOGS.d("getStepsData $dailyBean")
        val dailyStepData = StepsData(
            date = DateFormats.getConvertToDateFormat(
                dailyBean.date,
                DateFormats.dateFormat3,
                DateFormats.dateFormat
            )
        )

        val stepArray = ArrayList<StepsData.StepDataBreakup>()
        var steps = 0
        var distance = 0
        var calories = 0

        dailyBean.stepsData?.forEachIndexed { i, data ->
            val stepData = StepsData.StepDataBreakup(hourOfTheDay = i)
            stepData.steps = data
            stepData.calories = dailyBean.calorieData[i]
            stepData.distance = dailyBean.distanceData[i]
            calories += dailyBean.calorieData[i]
            distance += dailyBean.distanceData[i]
            steps += data
            stepArray.add(stepData)
        }
        dailyStepData.totalDistance = distance
        dailyStepData.totalCalories = calories
        dailyStepData.totalSteps = steps
        dailyStepData.stepArray = stepArray
        LOGS.d("getStepsData $dailyStepData")
        return dailyStepData
    }

    fun parseStepsDataOreo(dailyBean: DailyBean): OreoStepsData {
        LOGS.d("getStepsData $dailyBean")
        val dailyStepData = OreoStepsData(
            date = DateFormats.getConvertToDateFormat(
                dailyBean.date,
                DateFormats.dateFormat3,
                DateFormats.dateFormat
            )
        )

        val stepArray = ArrayList<OreoStepsData.OreoStepDataBreakup>()
        var steps = 0
        var distance = 0
        var calories = 0

        dailyBean.stepsData?.forEachIndexed { i, data ->
            val stepData = OreoStepsData.OreoStepDataBreakup(hourOfTheDay = i)
            stepData.steps = data
            stepData.calories = dailyBean.calorieData[i]
            stepData.distance = dailyBean.distanceData[i]
            calories += dailyBean.calorieData[i]
            distance += dailyBean.distanceData[i]
            steps += data
            stepArray.add(stepData)
        }
        dailyStepData.totalDistance = distance
        dailyStepData.totalCalories = calories
        dailyStepData.totalSteps = steps
        dailyStepData.stepArray = stepArray
        LOGS.d("getStepsData $dailyStepData")
        return dailyStepData
    }


//    fun parseSportsDataGPS(
//        p1: DevSportInfoBean,
//        colorFitDevice: ColorFitDevice
//    ): SportsModeListGPS {
//        val sportsModeList = SportsModeListGPS(responseType = "final")
//        val activities = ArrayList<SportsModeResponse>()
////        LOGS.d("GPS_DATA", "getGPSData onfail" + Gson().toJson(p1))
////        LOGS.d("GPS_DATA", "getGPSData onfail" + Gson().toJson(p1.map_data))
////        LOGS.d("GPS_DATA", "getGPSData onfail" + Gson().toJson(p1.recordPointSportData))
//        p1.let {
//            val calorieData: ArrayList<Int> = ArrayList()
//            val hrData: ArrayList<Int> = ArrayList()
//
//            if (it.recordPointSportData != null && it.recordPointSportData.isNotEmpty()) {
//                it.recordPointSportData.forEach { recordPointSportData ->
//                    calorieData.add(recordPointSportData.cal)
//                    if (recordPointSportData.heart != 0) {
//                        hrData.add(recordPointSportData.heart)
//                    }
//                }
//            }
//
//            val startCalendar = Calendar.getInstance()
//            val calendar = Calendar.getInstance(DateFormats.defaultLocale);
//            val offset =
//                -(calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000)
//            startCalendar.timeInMillis = (p1.reportSportStartTime + offset)
//            val endCalendar = Calendar.getInstance()
//            endCalendar.timeInMillis = (p1.reportSportEndTime)
//            val duration = p1.reportDuration.toInt()
//            var cadence = 0
//            if(it.reportTotalStep != 0L){
//                cadence = ((it.reportTotalStep / duration) * 60).toInt()
//            }
//
//
//            val sportsModeResponse = SportsModeResponse(
//                type = getSportName(p1.recordPointSportType, colorFitDevice),
//                calories = p1.reportCal,
//                avgStepStride = p1.reportRecoveryTime.toInt(),
//                distance = p1.reportDistance,
//                heartRateAvg = p1.reportAvgHeart,
//                endTime = DateFormats.timeFormat.format(endCalendar.time),
//                date = DateFormats.dateFormat.format(startCalendar.time),
//                time = DateFormats.formatDateTime(
//                    startCalendar.time,
//                    DateFormats.dateTimeFormatISO
//                ),
//                cadence = cadence,
//                duration = duration.toLong(),
//                heartRateData = hrData.handleHrData(duration),
//                calorieData = calorieData.handleCaloriesData(duration)
//            )
//
//            //   val timeData = p1.recordGpsTime.split(",")
//
////            val gpsDataLinkedList = getGpsMapsData(p1.map_data)
//            val gpsParseData = parseGpsMapsData(p1.map_data, p1.recordGpsTime)
//
//
////            LOGS.d("gpsParseData ${Gson().toJson(gpsParseData)}")
//            //  watchDataStore.getWeatherDataModel(startCalendar.timeInMillis, endCalendar.timeInMillis)
//            if (gpsParseData.isEmpty()) {
//                sportsModeResponse.gpsCoordinate = null
//            } else {
//                sportsModeResponse.gpsCoordinate = listToJson(gpsParseData)
//            }
//
//
//
//
//            sportsModeResponse.hrZoneInSeconds = 1
//            sportsModeResponse.aerobic = (p1.reportHeartAerobic).toInt()
//            sportsModeResponse.anaerobic = (p1.reportHeartAnaerobic).toInt()
//            sportsModeResponse.fatBurn = (p1.reportHeartFatBurning).toInt()
//            sportsModeResponse.warmUp = (p1.reportHeartWarmUp).toInt()
//
//            if (p1.reportTotalStep.toInt() != 0) {
//                sportsModeResponse.cadence = p1.reportMaxStepSpeed
//                sportsModeResponse.steps = p1.reportTotalStep.toInt()
//            }
//
//            sportsModeResponse.distance = p1.reportDistance
//
//            if (p1.reportDistance.toInt() != 0 && duration != 0) {
//                val df = DecimalFormat("#.#")
//                val speed = p1.reportFastSpeed
//                sportsModeResponse.speed = df.format(speed)?.toFloat()
//            } else {
//                sportsModeResponse.speed = 0F
//                sportsModeResponse.cadence = 0
//            }
//
//
//            activities.add(sportsModeResponse)
//        }
//
////        LOGS.d("GPS_DATA", "getGPSData onfail" + Gson().toJson(activities))
//        sportsModeList.activities = activities
//        return sportsModeList
//    }


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

    fun formatSOSContact(emergencyContactBean: EmergencyContactBean?): SOSContact {
        val sosContact = SOSContact()
        val contactList = ArrayList<Contact>()
        emergencyContactBean?.contactList?.forEach { contactsBean ->
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
        sosContact.sosSwitch = emergencyContactBean?.sosSwitch ?: false
        sosContact.contactList = contactList
        return sosContact
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

    fun parseRecordedData(it: DevSportInfoBean): RecordedWorkoutData {

        AppLogs.sendAppLogs("Recorded Workout $it")

        val hrData: ArrayList<Int> = ArrayList()
        val intensity: ArrayList<Int> = ArrayList()

        if (it.ringPointData != null && it.ringPointData.isNotEmpty()) {
            it.ringPointData.forEach { ringPointData ->
                hrData.add(ringPointData.heartRate)
                intensity.add(ringPointData.exerciseIntensity)
            }
        }

        val duration = it.reportDuration.toInt() / 60
        val date = DateFormats.convertTimestampToDate(
            it.reportSportStartTime, DateFormats.dateFormat3
        )


        var cadence = 0L
        if (it.reportTotalStep != 0L) {
            cadence = ((it.reportTotalStep.toFloat() / duration)).roundToLong()
        }

        return RecordedWorkoutData(
            recoveryTime = it.reportRecoveryTime,
            distance = it.reportDistance,
            cadence = cadence,
            isSynced = false,
            isAccepted = false,
            duration = duration,
            intensity = 0,
            calories = it.reportCal.toInt(),
            startTime = it.reportSportStartTime,
            endTime = it.reportSportEndTime,
            steps = it.reportTotalStep.toInt(),
            type = it.recordPointSportType,
            hrData = Gson().toJson(hrData),
            intensityList = Gson().toJson(intensity),
            date = date
        )
    }
}

