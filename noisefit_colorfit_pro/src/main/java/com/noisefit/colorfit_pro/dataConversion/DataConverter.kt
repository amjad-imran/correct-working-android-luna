package com.noisefit.colorfit_pro.dataConversion

import android.content.Context
import com.crrepa.ble.conn.bean.*
import com.crrepa.ble.conn.type.CRPVibrationStrength
import com.crrepa.ble.conn.type.CRPWeatherId
import com.google.gson.Gson
import com.noisefit_commans.common.handleHrData
import com.noisefit_commans.constants.SportActivityName
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.AppConversionUtils
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import java.math.BigInteger
import java.text.DecimalFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToInt

class DataConverter
@Inject
constructor(
    var context: Context
) {

    private fun celToFeh(cels: Double): Double {
        return (cels * 9.0 / 5.0) + 32.0
    }

    fun parseVibration(data: CRPVibrationStrength): VibrationIntensityEnum {
        return when (data) {
            CRPVibrationStrength.LOW -> {
                VibrationIntensityEnum.Weak
            }
            CRPVibrationStrength.MEDIUM -> {
                VibrationIntensityEnum.Medium
            }
            CRPVibrationStrength.STRONG -> {
                VibrationIntensityEnum.Strong
            }
        }
    }

    fun convertVibration(data: VibrationIntensityEnum): CRPVibrationStrength {
        return when (data) {
            VibrationIntensityEnum.Weak -> {
                CRPVibrationStrength.LOW
            }
            VibrationIntensityEnum.Medium -> {
                CRPVibrationStrength.MEDIUM
            }
            VibrationIntensityEnum.Strong -> {
                CRPVibrationStrength.STRONG
            }
        }
    }

    fun convertWatchStoreToWatchFacesList(p0: CRPWatchFaceStoreInfo?): ArrayList<WatchFace> {
        val watchFaceList = ArrayList<WatchFace>()
        p0?.list?.forEach { watchFace12 ->
            val watchFace = WatchFace(
                0, System.currentTimeMillis(),
                0,
                1,
                1,
                watchFace12.preview,
                watchFace12.id.toString(),
                watchFace12.id.toString(),
                "",
                "",
                ",",
                1,
                watchFace12.id.toString(),
                "1000",
                "",
                1,
                watchFace12.id.toString(),
                0,
                "",
                "1000", "1000",
                watchFace12.id, "supplier",
                watchFace12.file
            )
            watchFaceList.add(watchFace)
        }

        return watchFaceList
    }

    fun formatContactList(contactBeanList: List<Contact>): ArrayList<Contact> {
        val contactList = ArrayList<Contact>()
        contactBeanList.forEach { contactsBean ->
            contactList.add(
                Contact(
                    contactsBean.number[0],
                    contactsBean.name,
                    true,
                    null,
                    contactsBean.number
                )
            )
        }

        return contactList
    }


    private fun getSportType(sport: Int): String {
        return when (sport.toByte()) {
            CRPMovementHeartRateInfo.WALK_TYPE -> SportActivityName.WALKING
            CRPMovementHeartRateInfo.RUN_TYPE -> SportActivityName.RUNNING
            CRPMovementHeartRateInfo.INDOOR_CYCLING_TYPE -> SportActivityName.INDOOR_CYCLING
            CRPMovementHeartRateInfo.OUTDOOR_CYCLING_TYPE -> SportActivityName.CYCLING
            CRPMovementHeartRateInfo.BASKETBALL_TYPE -> SportActivityName.BASKETBALL
            CRPMovementHeartRateInfo.FOOTBALL_TYPE -> SportActivityName.FOOTBALL
            CRPMovementHeartRateInfo.SWIM_TYPE -> SportActivityName.SWIMMING
            CRPMovementHeartRateInfo.MOUNTAINEERING_TYPE -> SportActivityName.CLIMBING
            CRPMovementHeartRateInfo.TENNIS_TYPE -> SportActivityName.TENNIS
            CRPMovementHeartRateInfo.BADMINTON_TYPE -> SportActivityName.BADMINTON
            CRPMovementHeartRateInfo.RUGBY_TYPE -> SportActivityName.RUGBY
            CRPMovementHeartRateInfo.GOLF_TYPE -> SportActivityName.GOLF
            CRPMovementHeartRateInfo.ROPE_TYPE -> SportActivityName.SKIPPING
            CRPMovementHeartRateInfo.WORKOUT_TYPE -> SportActivityName.FITNESS
            CRPMovementHeartRateInfo.YOGA_TYPE -> SportActivityName.YOGA
            CRPMovementHeartRateInfo.INDOOR_RUN_TYPE -> SportActivityName.INDOOR_RUNNING
            CRPMovementHeartRateInfo.ON_FOOT_TYPE -> SportActivityName.ON_FOOT
            CRPMovementHeartRateInfo.ELLIPTICAL_TYPE -> SportActivityName.ELLIPTICAL
            CRPMovementHeartRateInfo.FREE_TRAINING_TYPE -> SportActivityName.FREE_TRAINING
            CRPMovementHeartRateInfo.TRAIL_RUNNING_TYPE -> SportActivityName.TRAIL_RUNNING
            CRPMovementHeartRateInfo.SKI_TYPE -> SportActivityName.SKIING
            CRPMovementHeartRateInfo.BOWLING_TYPE -> SportActivityName.BOWLING
            CRPMovementHeartRateInfo.DUMBBELLS_TYPE -> SportActivityName.DUMBBELLS
            CRPMovementHeartRateInfo.SIT_UPS_TYPE -> SportActivityName.SIT_UPS
            CRPMovementHeartRateInfo.INDOOR_WALK_TYPE -> SportActivityName.INDOOR_WALKING
            CRPMovementHeartRateInfo.CRICKET_TYPE -> SportActivityName.CRICKET
            CRPMovementHeartRateInfo.KABADDI_TYPE -> SportActivityName.KABADDI
            CRPMovementHeartRateInfo.DANCE_TYPE -> SportActivityName.DANCING
            CRPMovementHeartRateInfo.BASEBALL_TYPE -> SportActivityName.BASEBALL
            127.toByte() -> SportActivityName.AUTO_RACING
            126.toByte() -> SportActivityName.ATHLETICS
            114.toByte() -> SportActivityName.ICE_HOCKEY
            110.toByte() -> SportActivityName.ICE_SKATING
            108.toByte() -> SportActivityName.BEACH_VOLLEYBALL
            107.toByte() -> SportActivityName.BEACH_SOCCER
            103.toByte() -> SportActivityName.HANDBALL
            102.toByte() -> SportActivityName.TABLE_TENNIS
            101.toByte() -> SportActivityName.HOCKEY
            100.toByte() -> SportActivityName.SOFTBALL
            99.toByte() -> SportActivityName.VOLLEYBALL
            98.toByte() -> SportActivityName.KENDO
            97.toByte() -> SportActivityName.FENCING
            96.toByte() -> SportActivityName.KICKBOXING
            95.toByte() -> SportActivityName.KARATE
            94.toByte() -> SportActivityName.TAEKWONDO
            93.toByte() -> SportActivityName.JUDO
            90.toByte() -> SportActivityName.MARTIAL_ARTS
            89.toByte() -> SportActivityName.WRESTLING
            88.toByte() -> SportActivityName.BOXING
            87.toByte() -> SportActivityName.JAZZ_DANCE
            86.toByte() -> SportActivityName.FOLK_DANCE
            85.toByte() -> SportActivityName.STREET_DANCE
            84.toByte() -> SportActivityName.LATIN_DANCE
            83.toByte() -> SportActivityName.ZUMBA
            82.toByte() -> SportActivityName.BALLET
            81.toByte() -> SportActivityName.BELLY_DANCE
            80.toByte() -> SportActivityName.SQUARE_DANCING
            79.toByte() -> SportActivityName.WEIGHTLIFTING
            78.toByte() -> SportActivityName.HIIT
            77.toByte() -> SportActivityName.ABS
            71.toByte() -> SportActivityName.DEADLIFT
            67.toByte() -> SportActivityName.AEROBICS
            58.toByte() -> SportActivityName.STAIR_CLIMBING
            57.toByte() -> SportActivityName.STAIR_CLIMBER
            52.toByte() -> SportActivityName.ROLLER_SKATING
            51.toByte() -> SportActivityName.SKATEBOARDING
            50.toByte() -> SportActivityName.ROCK_CLIMBING
            49.toByte() -> SportActivityName.OPEN_WATER_SWIM
            41.toByte() -> SportActivityName.KAYAK_RAFTING
            39.toByte() -> SportActivityName.WATER_SKIING
            45.toByte() -> SportActivityName.DIVING
            53.toByte() -> SportActivityName.PARKOUR
            54.toByte() -> SportActivityName.ATV
            55.toByte() -> SportActivityName.PARAGLIDING
            19.toByte() -> SportActivityName.ROWING_MACHINE
            35.toByte() -> SportActivityName.SAILING
            36.toByte() -> SportActivityName.WATER_POLO
            37.toByte() -> SportActivityName.OTHER_WATER_SPORTS
            38.toByte() -> SportActivityName.PADDLE_BOARDING
            40.toByte() -> SportActivityName.KAYAKING
            43.toByte() -> SportActivityName.POWERBOATING
            48.toByte() -> SportActivityName.KITE_SURFING
            111.toByte() -> SportActivityName.CURLING
            112.toByte() -> SportActivityName.OTHER_WINTER_SPORTS
            117.toByte() -> SportActivityName.ARCHERY
            118.toByte() -> SportActivityName.DARTS
            119.toByte() -> SportActivityName.TUG_OF_WAR
            120.toByte() -> SportActivityName.HULA_HOOP
            121.toByte() -> SportActivityName.KITE_FLYING
            122.toByte() -> SportActivityName.HORSE_RIDING
            123.toByte() -> SportActivityName.FRISBEE
            124.toByte() -> SportActivityName.FISHING
            125.toByte() -> SportActivityName.EQUESTRIAN_SPORTS
            113.toByte() -> SportActivityName.SNOWMOBILE
            115.toByte() -> SportActivityName.BOBSLEIGH
            116.toByte() -> SportActivityName.SLEDDING
            104.toByte() -> SportActivityName.SQUASH
            105.toByte() -> SportActivityName.BILLIARDS
            106.toByte() -> SportActivityName.SHUTTLECOCK
            109.toByte() -> SportActivityName.SEPAK_TAKRAW
            91.toByte() -> SportActivityName.TAI_CHI
            92.toByte() -> SportActivityName.MUAY_THAI
            76.toByte() -> SportActivityName.LOWER_BODY
            75.toByte() -> SportActivityName.BACK
            74.toByte() -> SportActivityName.BURPEES
            73.toByte() -> SportActivityName.FUNCTIONAL_TRAINING
            72.toByte() -> SportActivityName.UPPER_BODY
            70.toByte() -> SportActivityName.BARBELL
            69.toByte() -> SportActivityName.WALL_BALL
            68.toByte() -> SportActivityName.PHYSICAL_TRAINING
            66.toByte() -> SportActivityName.CROSS_TRAINING
            65.toByte() -> SportActivityName.STRENGTH
            64.toByte() -> SportActivityName.STRETCHING
            63.toByte() -> SportActivityName.GYMNASTICS
            62.toByte() -> SportActivityName.PILATES
            61.toByte() -> SportActivityName.FLEXIBILITY
            60.toByte() -> SportActivityName.CORE_TRAINING
            59.toByte() -> SportActivityName.STEPPER
            56.toByte() -> SportActivityName.TRIATHLON
            47.toByte() -> SportActivityName.SNORKELING
            46.toByte() -> SportActivityName.ARTISTIC_SWIMMING
            44.toByte() -> SportActivityName.FIN_SWIMMING
            42.toByte() -> SportActivityName.ROWING

            else -> SportActivityName.ACTIVITY
        }
    }

    fun parseSportsMode(list: List<CRPMovementHeartRateInfo>): SportsModeListGPS {
        val sportsList = ArrayList<SportsModeResponse>()
        list.forEach { sport ->
//            LOGS.d("sportType ${sport.type}")
//            LOGS.d("sportType ${Gson().toJson(sport)}")
            if (sport.startTime < sport.endTime) {
                val startTime = Calendar.getInstance()
                startTime.timeInMillis = sport.startTime
                val endTime = Calendar.getInstance()
                endTime.timeInMillis = sport.endTime

                val duration = ((sport.endTime - sport.startTime) / 1000).toInt()
//hiking, climbing, treadmil, spinning
                val sportsModeResponse = SportsModeResponse(
                    time = DateFormats.formatDateTime(
                        startTime.time,
                        DateFormats.dateTimeFormatISO
                    ),
                    /*time = DateFormats.timeWithSecond.format(startTime.time),*/
                    date = DateFormats.dateFormat.format(startTime.time),
                    endTime = DateFormats.timeFormat.format(endTime.time),
                    calories = sport.calories.toLong(),
                    type = getSportType(sport.type),
                    duration = duration.toLong(),
                    heartRateAvailable = 0
                )

                sportsModeResponse.steps = sport.steps
                if (sport.steps != 0) {
                    sportsModeResponse.cadence = (sport.steps / duration) * 60
                }

                if (sport.distance != 0) {
                    sportsModeResponse.distance = sport.distance.toLong()

                    if (sport.distance != 0 && duration != 0) {
                        val df = DecimalFormat("#.#")
                        val pace = duration / sport.distance
                        val speed = sport.distance / duration
                        sportsModeResponse.pace = df.format(pace)?.toFloat()
                        sportsModeResponse.speed = df.format(speed)?.toFloat()

                    } else {
                        sportsModeResponse.pace = 0F
                        sportsModeResponse.speed = 0F
                        sportsModeResponse.cadence = 0
                    }
                }


//                LOGS.d("parseSportsMode ${Gson().toJson(sportsModeResponse)}")
                sportsList.add(sportsModeResponse)
            }
        }


        val sportsModeList = SportsModeListGPS()
        sportsModeList.activities = sportsList
        return sportsModeList
    }

    fun parseCRPTrainingSportsMode(list: List<CRPTrainingInfo>): SportsModeListGPS {
        val sportsList = ArrayList<SportsModeResponse>()
        list.forEach { sport ->


            if (sport.startTime < sport.endTime) {

                val cleanHrList = ArrayList<Int>()
                sport.hrList.forEach { hrValue ->
                    if (hrValue != 0) {
                        cleanHrList.add(hrValue)
                    }
                }
                val startTime = Calendar.getInstance()
                startTime.timeInMillis = sport.startTime
                val endTime = Calendar.getInstance()
                endTime.timeInMillis = sport.endTime

                val duration = ((sport.endTime - sport.startTime) / 1000).toInt()

                val sportsModeResponse = SportsModeResponse(
                    time = DateFormats.formatDateTime(
                        startTime.time,
                        DateFormats.dateTimeFormatISO
                    ),
                    date = DateFormats.dateFormat.format(startTime.time),
                    endTime = DateFormats.timeFormat.format(endTime.time),
                    calories = sport.calories.toLong(),
                    type = getSportType(sport.type),
                    duration = duration.toLong(),
                    heartRateAvailable = 0,
                    heartRateData = cleanHrList.handleHrData(duration)
                )

                sportsModeResponse.steps = sport.steps
                if (sport.steps != 0) {
                    sportsModeResponse.cadence = (sport.steps / duration) * 60
                }

                if (sport.distance != 0) {
                    sportsModeResponse.distance = sport.distance.toLong()

                    if (sport.distance != 0 && duration != 0) {
                        val df = DecimalFormat("#.#")
                        val pace = duration / sport.distance
                        val speed = sport.distance / duration
                        sportsModeResponse.pace = df.format(pace)?.toFloat()
                        sportsModeResponse.speed = df.format(speed)?.toFloat()

                    } else {
                        sportsModeResponse.pace = 0F
                        sportsModeResponse.speed = 0F
                        sportsModeResponse.cadence = 0
                    }
                }


                sportsList.add(sportsModeResponse)
            }
        }


        val sportsModeList = SportsModeListGPS()
        sportsModeList.activities = sportsList
        return sportsModeList
    }

    fun parseHeartHistory(info: CRPHeartRateInfo): List<HeartRate> {
        val heartList = ArrayList<HeartRate>()
        val calendar = Calendar.getInstance()
//        calendar.set(Calendar.HOUR_OF_DAY, 0)
//        calendar.set(Calendar.MINUTE, 0)
//        calendar.set(Calendar.SECOND, 0)
        info.heartRateList?.let { list ->
            calendar.timeInMillis = info.startTime
            list.forEach {
                calendar.add(Calendar.MINUTE, info.timeInterval)
                if (it != 0) {
                    val date = DateFormats.dateFormat.format(calendar.time)
                    val time = DateFormats.timeFormat.format(calendar.time)
                    val syncDate = DateFormats.convertDateTimeToTimeStamp(date, time)
                    heartList.add(
                        HeartRate(
                            averageHeartRate = it,
                            date = date,
                            time = time,
                            timeStamp = syncDate,
                            resetData = true
                        )
                    )
                }
            }

//            val heartRate = HeartRate(averageHeartRate = if(sortedList.size > 0) sortedList.last() else 0)
//
//            sortedList.sort()
//            heartRate.date = DateFormats.getDateFormat()
//            heartRate.highestHeartRate = if(sortedList.size > 0) sortedList.last() else 0
//            heartRate.lowestHeartRate = if(sortedList.size > 0) sortedList.first() else 0


        }
        return heartList
    }

    fun parseBloodOxygenHistory(info: CRPBloodOxygenInfo?): List<BloodOxygenBreakup> {
        val dataList = ArrayList<BloodOxygenBreakup>()
        val calendar = Calendar.getInstance()

        info?.list?.let { list ->
            calendar.timeInMillis = info.startTime
            list.forEach {
                calendar.add(Calendar.MINUTE, info.timeInterval)
                if (it != 0) {
                    val date = DateFormats.dateFormat.format(calendar.time)
                    val time = DateFormats.timeFormat.format(calendar.time)

                    val syncDate = DateFormats.convertDateTimeToTimeStamp(date, time)
                    dataList.add(
                        BloodOxygenBreakup(
                            value = it,
                            date = date,
                            time = time,
                            timeStamp = syncDate,
                            resetData = true
                        )
                    )
                }
            }

        }
        return dataList
    }

    fun getSleepData(info: CRPSleepInfo,colorFitDevice: ColorFitDevice?): SleepData {
        val sleepArray = ArrayList<SleepData.SleepDataBreakup>()

       // LOGS.d("sleepsfghjjjjj ${Gson().toJson(info)}")
        info.details?.forEach { item ->
            val sleepDataBreakup = SleepData.SleepDataBreakup(
                sleepType = when (item.type) {
                    CRPSleepInfo.SLEEP_STATE_RESTFUL -> SleepType.DEEP.type
                    CRPSleepInfo.SLEEP_STATE_LIGHT -> SleepType.LIGHT.type
                    CRPSleepInfo.SLEEP_STATE_REM -> SleepType.REM.type
                    CRPSleepInfo.SLEEP_STATE_SOBER -> SleepType.SOBER.type
                    else -> SleepType.AWAKE.type
                }
            )
            sleepDataBreakup.duration = item.totalTime
            sleepDataBreakup.startTime = getTimeFromMinutes(item.startTime)
            sleepDataBreakup.endTime = getTimeFromMinutes(item.endTime)
            sleepArray.add(sleepDataBreakup)
        }

        var sleepData = SleepData(availableSleepTypes = "deep;light;awake")
        when (colorFitDevice?.deviceType){
            DeviceType.ICON_3.deviceType->{
                 sleepData = SleepData(availableSleepTypes = "deep;light;awake;rem")
            }
        }
        sleepData.startTime = getTimeFromMinutes(info.details.firstOrNull()?.startTime ?: 0)
        sleepData.endTime = getTimeFromMinutes(info.details.lastOrNull()?.endTime ?: 0)
        sleepData.deep = info.restfulTime
        sleepData.light = info.lightTime
        sleepData.remCount = info.remTime
        sleepData.awake = info.soberTime
        sleepData.date = DateFormats.getDateFormat()
        sleepData.total = info.totalTime
        sleepData.sleepArray = sleepArray

        return sleepData
    }

    private fun getTimeFromMinutes(minutes: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        calendar.add(Calendar.MINUTE, minutes)

        return DateFormats.timeFormat.format(calendar.time)
    }

    fun formatFutureWeatherData(
        weatherDataList: List<WeatherData>,
        unit: String
    ): CRPFutureWeatherInfo {
        val futureWeatherInfo = CRPFutureWeatherInfo()
        val list: MutableList<CRPFutureWeatherInfo.FutureBean> = java.util.ArrayList()
        for (i in weatherDataList.indices) {
            val bean = CRPFutureWeatherInfo.FutureBean()
            bean.weatherId = when (weatherDataList[i].weatherType) {
                "Clouds" -> CRPWeatherId.CLOUDY
                "Thunderstorm" -> CRPWeatherId.RAINY
                "Drizzle" -> CRPWeatherId.OVERCAST
                "Rain" -> CRPWeatherId.RAINY
                "Snow" -> CRPWeatherId.SNOWY
                else -> CRPWeatherId.SUNNY
            }
            var lowTemp = weatherDataList[i].tempMin.roundToInt()
            var highTemp = weatherDataList[i].tempMax.roundToInt()
            if (unit.equals(Units.IMPERIAL.name, true)) {
                lowTemp = celToFeh(lowTemp.toDouble()).roundToInt()
                highTemp = celToFeh(highTemp.toDouble()).roundToInt()
            }
            bean.lowTemperature = lowTemp
            bean.highTemperature = highTemp
            list.add(bean)
        }
        futureWeatherInfo.future = list

        return futureWeatherInfo
    }


    fun formatWeatherData(weatherData: WeatherData, unit: String): CRPTodayWeatherInfo {
        var temp = weatherData.temp.roundToInt()
        if (unit.equals(Units.IMPERIAL.name, true)) {
            temp = celToFeh(temp.toDouble()).roundToInt()
        }
        val crpTodayWeatherInfo = CRPTodayWeatherInfo()
        crpTodayWeatherInfo.city = weatherData.city
        crpTodayWeatherInfo.temp = temp
        crpTodayWeatherInfo.weatherId = when (weatherData.weatherType) {
            "Clouds" -> CRPWeatherId.CLOUDY
            "Thunderstorm" -> CRPWeatherId.RAINY
            "Drizzle" -> CRPWeatherId.OVERCAST
            "Rain" -> CRPWeatherId.RAINY
            "Snow" -> CRPWeatherId.SNOWY
            else -> CRPWeatherId.SUNNY
        }
        return crpTodayWeatherInfo
    }

    fun setMenstrualData(menstrualData: MenstrualData): CRPPhysiologcalPeriodInfo {
        val reminderTime = Calendar.getInstance()
        val calender = Calendar.getInstance()
        calender.time = DateFormats.getDateFormatFromString(menstrualData.lastMenstrualDate)
        reminderTime.time =
            DateFormats.timeFormat.parse(menstrualData.menstrualReminder?.reminderTime)
        return CRPPhysiologcalPeriodInfo(
            menstrualData.menstrualCycleLength, menstrualData.menstrualLength,
            calender.time, true, true, true, true, reminderTime.get(Calendar.HOUR_OF_DAY),
            reminderTime.get(Calendar.MINUTE)
        )
    }


    fun getCRPUserInfo(userInfo: UserInfo): CRPUserInfo {
        return CRPUserInfo(
            userInfo.weight,
            userInfo.height,
            when (userInfo.gender.lowercase()) {
                Gender.FEMALE.type.lowercase() -> CRPUserInfo.FEMALE
                else -> CRPUserInfo.MALE
            },
            AppConversionUtils.getAgeFromDOB(userInfo.dob)
        )
    }


    private fun binStringToBooleanArray(my_byte: Int, isEnable: Boolean): List<Boolean> {

        val result = ArrayList<Boolean>()
        result.addAll(listOf(false, false, false, false, false, false, false))
        val binStr = ByteArray(1)
        binStr[0] = my_byte.toByte()
        val byteStr: String = binary(binStr, 2)
        for (i in byteStr.length - 1 downTo 0) {
            result[i + 7 - byteStr.length] = Integer.valueOf(byteStr.substring(i, i + 1)) == 1
        }

//        val subList = ArrayList(result.subList(0, result.size))

        result.reverse()

        //In qube week starts from sunday so we have to change it accordingly to our app flow
        val sundayStatus = result[0]
        result.removeAt(0)
        result.add(sundayStatus)

        val subList = ArrayList<Boolean>()
        subList.add(isEnable)
        subList.addAll(result)
        // subList.reverse()
        LOGS.d("alarm list $subList")
        LOGS.d("alarm list $result")
        return subList
    }

    private fun binary(bytes: ByteArray?, radix: Int): String {
        return BigInteger(1, bytes).toString(radix)
    }


    fun parseAlarmDaysToBytes(alarmList: List<Boolean>?): Int {
        var result = 0
        for (i in 6 downTo 0) {
            alarmList?.let { boolList ->
                if (boolList[i]) {
                    result += 2.0.pow((6 - i).toDouble()).roundToInt()
                }
            }

        }
        return result
    }

    fun parseStressData(p0: MutableList<CRPHistoryStressInfo>?): ArrayList<StressDataBreakup> {
        val stressDataList = ArrayList<StressDataBreakup>()
// //[{"date":"Nov 18, 2022 13:29:24","stress":28}]

        try {
            p0?.forEach { crpHistoryStressInfo ->
                if (crpHistoryStressInfo.stress > 0) {
                    val date = DateFormats.convertTimestampToDate(
                        crpHistoryStressInfo.date.time,
                        DateFormats.dateFormat
                    )
                    val time = DateFormats.convertTimestampToDate(
                        crpHistoryStressInfo.date.time,
                        DateFormats.timeFormat
                    )

                    val syncDate = DateFormats.convertDateTimeToTimeStamp(date, time)
                    val stressItem = StressDataBreakup(
                        value = crpHistoryStressInfo.stress,
                        date = date,
                        time = time,
                        timeStamp = syncDate
                    )
                    stressDataList.add(stressItem)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        return stressDataList
    }

    fun getAlarmList(crpDeviceAlarmsList: List<CRPAlarmInfo>?): AlarmsList {
        val alarms = ArrayList<AlarmsList.Alarm>()
        val alarmsList = AlarmsList()
        crpDeviceAlarmsList?.forEach { alarmClockInfo ->
//            if (alarmClockInfo.hour != 0 && alarmClockInfo.minute != 0) {

            LOGS.d("alarm list ${alarmClockInfo.repeatMode}")
            val repeatMode =
                binStringToBooleanArray(alarmClockInfo.repeatMode, alarmClockInfo.isEnable)
//            //LOGS.d("get Alarms ------ ${Gson().toJson(repeatMode)}")
//            val result = ArrayList<Boolean>()
//            result.add(alarmClockInfo.isEnable)
//            result.addAll(repeatMode)
            val alarm = AlarmsList.Alarm()
            alarm.hour = alarmClockInfo.hour
            alarm.id = alarmClockInfo.id
            alarm.minute = alarmClockInfo.minute
            alarm.status = alarmClockInfo.isEnable
            alarm.repeatDays = repeatMode
            alarms.add(alarm)
//            }
        }
        alarmsList.alarms = alarms

        return alarmsList
    }


    fun parseQRPayment(cRPElectronicCardInfo: CRPElectronicCardInfo?): UPIQRCode {
        var uPIQRCode = UPIQRCode()
        cRPElectronicCardInfo?.let {
            uPIQRCode = UPIQRCode(id = it.id, title = it.title, url = it.url)
        }
        return uPIQRCode
    }

    fun convertQRPayment(qRPayment: UPIQRCode): CRPElectronicCardInfo {

        return CRPElectronicCardInfo(qRPayment.id ?: 1, qRPayment.title, qRPayment.url)
    }

    fun getSedentaryReminderPeriod(
        status: Boolean,
        crpSedentaryReminderPeriodInfo: CRPSedentaryReminderPeriodInfo
    ): WalkReminderData {
        return WalkReminderData(
            status = status,
            goalSteps = crpSedentaryReminderPeriodInfo.steps.toInt(),
            startHour = 10,
            endHour = 22,
            repeat = 64
        )
    }

    fun getMenstrualSettings(cRPPhysiologcalPeriodInfo: CRPPhysiologcalPeriodInfo): MenstrualData {
        val calender = Calendar.getInstance()
        calender.set(Calendar.HOUR_OF_DAY, cRPPhysiologcalPeriodInfo.reminderHour)
        calender.set(Calendar.MINUTE, cRPPhysiologcalPeriodInfo.reminderMinute)
        return MenstrualData(
            status = true,
            menstrualCycleLength = cRPPhysiologcalPeriodInfo.physiologcalPeriod,
            menstrualLength = cRPPhysiologcalPeriodInfo.menstrualPeriod,
            lastMenstrualDate = DateFormats.dateFormat.format(cRPPhysiologcalPeriodInfo.startDate),
            menstrualReminder = MenstrualData.MenstrualReminder(
                reminderTime = DateFormats.timeFormat.format(
                    calender.time
                )
            )
        )
    }

}