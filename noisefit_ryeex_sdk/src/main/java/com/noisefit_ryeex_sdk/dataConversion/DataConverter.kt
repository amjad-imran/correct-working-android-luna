package com.noisefit_ryeex_sdk.dataConversion

import android.content.Context
import com.google.gson.Gson
import com.noisefit_commans.common.handleCaloriesData
import com.noisefit_commans.common.handleHrData
import com.noisefit_commans.common.roundToNearestDecimalUp
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_ryeex_sdk.utils.TempStep24DataWrapper
import com.noisefit_ryeex_sdk.utils.TempStepDataWrapper
import com.ryeex.watch.adapter.model.entity.*
import com.ryeex.watch.protocol.pb.entity.PBDevApp
import com.ryeex.watch.protocol.pb.entity.PBSport
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class DataConverter
@Inject
constructor(
    var context: Context,
    var watchDataStore: WatchDataStore,
    var gson: Gson
) {


    fun parseStepsData(dateKey: String, tempStepDataWrapper: TempStepDataWrapper): StepsData {
        val stepsData = StepsData()
        stepsData.totalSteps = tempStepDataWrapper.stepHealthDomain?.total ?: 0
        val interval = tempStepDataWrapper.stepHealthDomain?.interval ?: 300
        stepsData.totalDistance = tempStepDataWrapper.distanceHealthDomain?.total ?: 0
        stepsData.totalCalories =
            ((tempStepDataWrapper.calorieHealthDomain?.total ?: 0) / 1000f).roundToInt()
        val date = DateFormats.getConvertToDateFormat(
            dateKey,
            DateFormats.dateFormat5,
            DateFormats.dateFormat
        )
        stepsData.date = date
        val startDayTimeStamp =
            DateFormats.convertDateTimeToTimeStamp(dateKey, DateFormats.dateFormat5)

        val hmSteps = HashMap<Int, TempStep24DataWrapper>()
        tempStepDataWrapper.stepHealthDomain?.items?.forEach { data ->
            val tempStartDayTimeStamp =
                DateFormats.addSecondsInMilliseconds(startDayTimeStamp!!, data.offset * interval)
            val hKey = DateFormats.convertTimeStampToHourOfDay(tempStartDayTimeStamp)
            if (hmSteps.containsKey(hKey)) {
                val tempStep24DataWrapper = hmSteps[hKey]!!
                val step = tempStep24DataWrapper.step ?: 0
                tempStep24DataWrapper.step = step + data.value

                hmSteps[hKey] = tempStep24DataWrapper
            } else {
                hmSteps[hKey] = TempStep24DataWrapper(step = data.value)
            }

        }
        tempStepDataWrapper.distanceHealthDomain?.items?.forEach { data ->
            val tempStartDayTimeStamp =
                DateFormats.addSecondsInMilliseconds(startDayTimeStamp!!, data.offset * interval)

            val hKey = DateFormats.convertTimeStampToHourOfDay(tempStartDayTimeStamp)
            if (hmSteps.containsKey(hKey)) {
                val tempStep24DataWrapper = hmSteps[hKey]!!
                val distance = tempStep24DataWrapper.distance ?: 0
                tempStep24DataWrapper.distance = distance + data.value

                hmSteps[hKey] = tempStep24DataWrapper
            } else {
                hmSteps[hKey] = TempStep24DataWrapper(distance = data.value)
            }

        }
        tempStepDataWrapper.calorieHealthDomain?.items?.forEach { data ->
            val tempStartDayTimeStamp =
                DateFormats.addSecondsInMilliseconds(startDayTimeStamp!!, data.offset * interval)
            val hKey = DateFormats.convertTimeStampToHourOfDay(tempStartDayTimeStamp)

            if (hmSteps.containsKey(hKey)) {
                val tempStep24DataWrapper = hmSteps[hKey]!!
                val calories = tempStep24DataWrapper.calories ?: 0
                tempStep24DataWrapper.calories = calories + data.value
                hmSteps[hKey] = tempStep24DataWrapper
            } else {
                hmSteps[hKey] = TempStep24DataWrapper(calories = data.value)
            }

        }


        val stepArray = ArrayList<StepsData.StepDataBreakup>()
        for (hour in 0..23) {
            val stepData = StepsData.StepDataBreakup(hourOfTheDay = hour)
            stepData.steps = hmSteps[hour]?.step ?: 0
            stepData.calories = ((hmSteps[hour]?.calories ?: 0) / 1000f).roundToInt()
            stepData.distance = hmSteps[hour]?.distance ?: 0
            stepArray.add(stepData)
//            LOGS.d("stepArrayDATA :::::: ${Gson().toJson(stepData)}")
        }
        stepsData.stepArray = stepArray
//        LOGS.d("parse stepsData=${gson.toJson(stepsData)}")
        return stepsData
    }

    fun parseSportsData(
        bean: LibSportDomain
    ): SportsModeListGPS {
        val sportsModeList = SportsModeListGPS(responseType = "final")
        val activities = ArrayList<SportsModeResponse>()


        val calorieData: ArrayList<Int> = ArrayList()
        val hrData: ArrayList<Int> = ArrayList()

        bean.heartRates.forEach {
            if (it.value != 0) {
                hrData.add(it.value)
            }

        }
//        if (it.recordPointSportData != null && it.recordPointSportData.isNotEmpty()) {
//            it.recordPointSportData.forEach { recordPointSportData ->
//                calorieData.add(recordPointSportData.cal)
//                if (recordPointSportData.heart != 0) {
//                    hrData.add(recordPointSportData.heart)
//                }
//            }
//        }


        val startTime = bean.startTime.toLong() * 1000
        val endTime = bean.endTime.toLong() * 1000

//        LOGS.d("START_TIME $startTime ${DateFormats.dateFormat.format(startTime)}")
//        LOGS.d("START_TIME $endTime ${DateFormats.timeFormat.format(endTime)}")

        val duration = bean.duration
        val sportsModeResponse = SportsModeResponse(
            type = getSportName(bean.type),
            calories = (bean.calorie / 1000f).roundToLong(),
            heartRateAvg = bean.avgHeartRate,
            endTime = DateFormats.timeFormat.format(endTime),
            date = DateFormats.dateFormat.format(startTime),
            time = DateFormats.formatDateTime(
                Date(startTime),
                DateFormats.dateTimeFormatISO
            ),
            duration = duration.toLong(),
            heartRateData = hrData.handleHrData(duration),
            calorieData = calorieData.handleCaloriesData(duration)
        )

        if (isOutdoorSport(bean.type)) {
            watchDataStore.getLocationDataModel(bean.sessionId.toLong())?.let {
                sportsModeResponse.gpsCoordinate = Gson().toJson(parseGpsMapsData(it))
                watchDataStore.clearLocationData(bean.sessionId.toLong())
            }

        }


        sportsModeResponse.hrZoneInSeconds = 1

        if (bean.heartRateSections.count() == 6) {
            sportsModeResponse.warmUp = bean.heartRateSections[0] + bean.heartRateSections[1]
            sportsModeResponse.fatBurn = bean.heartRateSections[2]
            sportsModeResponse.aerobic = bean.heartRateSections[3]
            sportsModeResponse.anaerobic = bean.heartRateSections[4]
            sportsModeResponse.extremeMin = bean.heartRateSections[5]
        }


        if (bean.step != 0) {
            sportsModeResponse.steps = bean.step
            sportsModeResponse.cadence = bean.stepFrequency
            sportsModeResponse.avgStepStride = bean.stepStrides
        }


//        if (sportRecordMerge.summary.distance != 0 && duration != 0) {
//            val df = DecimalFormat("#.#")
//            val speed = bean
//            sportsModeResponse.speed = df.format(speed)?.toFloat()
//        } else {
//            sportsModeResponse.speed = 0F
//            sportsModeResponse.cadence = 0
//        }

        sportsModeResponse.distance = bean.distance.toLong()

        sportsModeResponse.speed = bean.avgSpeed.roundToNearestDecimalUp()
        sportsModeResponse.pace = bean.avgPace.toFloat()

        activities.add(sportsModeResponse)


//        LOGS.d("GPS_DATA", "getGPSData onfail" + Gson().toJson(activities))
        sportsModeList.activities = activities
        return sportsModeList
    }

    private fun parseGpsMapsData(mapsData: List<LocationDataModel>): ArrayList<DoubleArray> {
        val gpsList = ArrayList<DoubleArray>()

        var lastLat = 0.0
        var lastLog = 0.0

        mapsData.forEach { mapDataArray ->
            val dataList = ArrayList<Double>()
            val lat = mapDataArray.latitude
            val log = mapDataArray.longitude

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


    fun parseHeartRateData(bean: LibHealthHeartRateDomain): List<HeartRate> {
        val heartRateList = ArrayList<HeartRate>()
        val startDayTimeStamp =
            DateFormats.convertDateTimeToTimeStamp(bean.date, DateFormats.dateFormat5)

        bean.items.forEach { item ->
            if (item.value != 0) {
                val tempStartDayTimeStamp =
                    DateFormats.addSecondsInMilliseconds(startDayTimeStamp!!, item.offset)

                val time = DateFormats.timeFormat.format(tempStartDayTimeStamp)
                val heartRate = HeartRate()
                heartRate.resetData = true
                heartRate.averageHeartRate = item.value
                heartRate.date = DateFormats.getConvertToDateFormat(
                    bean.date,
                    DateFormats.dateFormat5,
                    DateFormats.dateFormat
                )
                heartRate.time = time
                heartRate.timeStamp =
                    DateFormats.convertDateTimeToTimeStamp(heartRate.date!!, time)
                heartRate.highestHeartRate = bean.max
                heartRate.lowestHeartRate = bean.min

                // LOGS.d("syncUserActivity :HEART_RATE_DATA ${item.value} ${heartRate.date} ${heartRate.time}")
                heartRateList.add(heartRate)
            }
        }

//        LOGS.d("parse heartRateList=${gson.toJson(heartRateList)}")
        return heartRateList
    }

    //Granularity interval, unit: seconds
    fun parseBloodOxygenData(bean: LibHealthBloodOxygenDomain): ArrayList<BloodOxygenBreakup> {
        val oxygenArray = ArrayList<BloodOxygenBreakup>()
        val startDayTimeStamp =
            DateFormats.convertDateTimeToTimeStamp(bean.date, DateFormats.dateFormat5)

        bean.items.forEach { measureData ->

            if (measureData.value > 0) {

                val tempStartDayTimeStamp =
                    DateFormats.addSecondsInMilliseconds(startDayTimeStamp!!, measureData.offset)
                val date = DateFormats.dateFormat.format(tempStartDayTimeStamp)
                val time = DateFormats.timeFormat.format(tempStartDayTimeStamp)
                val syncDate = DateFormats.convertDateTimeToTimeStamp(date, time)

                val oxygenItem = BloodOxygenBreakup(
                    value = measureData.value,
                    date = date,
                    time = time,
                    timeStamp = syncDate
                )
                oxygenArray.add(oxygenItem)
            }


        }
//        LOGS.d("parse oxygenArray=${gson.toJson(oxygenArray)}")
        return oxygenArray
    }


    fun parseSleepData(bean: LibHealthSleepDomain): SleepData {

        val sleepData = SleepData(availableSleepTypes = "deep;light;awake;rem")

        val sleepArray = ArrayList<SleepData.SleepDataBreakup>()

        sleepData.startTime = DateFormats.convertTimestampToDate(
            bean.startTime.toLong() * 1000,
            DateFormats.timeFormat
        )
        sleepData.endTime = DateFormats.convertTimestampToDate(
            bean.endTime.toLong() * 1000,
            DateFormats.timeFormat
        )

        sleepData.sleepScore = 0

        sleepData.date = DateFormats.getConvertToDateFormat(
            bean.date,
            DateFormats.dateFormat5,
            DateFormats.dateFormat
        )


        sleepData.total = bean.totalDuration / 60
        sleepData.deep = bean.deepDuration / 60
        sleepData.awake = bean.wakeDuration / 60
        sleepData.remCount = bean.eyeDuration / 60
        sleepData.light = sleepData.total - sleepData.deep - sleepData.awake - sleepData.remCount

        var sleepDistributionStartTime = 0L
        bean.items.forEach { sleepDistributionData ->
            val sleepBreakup = SleepData.SleepDataBreakup(
                sleepType = when (sleepDistributionData.status) {
                    SleepStatus.WAKE -> SleepType.AWAKE.type
                    SleepStatus.LIGHT -> SleepType.LIGHT.type
                    SleepStatus.DEEP -> SleepType.DEEP.type
                    SleepStatus.EYE -> SleepType.REM.type
                    else -> SleepType.AWAKE.type
                }
            )

            if (sleepDistributionStartTime == 0L) {
                sleepDistributionStartTime = bean.startTime.toLong() * 1000
            }

            sleepBreakup.startTime = DateFormats.convertTimestampToDate(
                sleepDistributionStartTime,
                DateFormats.timeFormat
            )

            sleepBreakup.duration = sleepDistributionData.duration / 60

            val endTime = DateFormats.addSecondToTimeStamp(
                sleepDistributionStartTime,
                sleepDistributionData.duration
            )

            sleepBreakup.endTime = DateFormats.convertTimestampToDate(
                endTime,
                DateFormats.timeFormat
            )

            sleepDistributionStartTime = endTime

            sleepArray.add(sleepBreakup)
        }
        // LOGS.d("syncUserActivity :HEART_RATE ${Gson().toJson(sleepData)}")
        sleepData.sleepArray = sleepArray

//        LOGS.d("parse sleepData=${gson.toJson(sleepData)}")
        AppLogs.sendAppLogs("parse sleepData=${gson.toJson(sleepData)}")
        return sleepData
    }

    fun parseSportsModeInfo(functionInfo: MutableList<Int>): SportsModeList {
        val sportsModeList = ArrayList<SportsModeList.SportsMode>()
        functionInfo.let { sportIds ->
            sportIds.forEachIndexed { i, item ->
                val sportMode = parseSportMode(item)
                if (!sportMode.name.isNullOrEmpty()) {
                    sportMode.index = i
                    sportsModeList.add(sportMode)
                }
            }
        }
        return SportsModeList(sportsModes = sportsModeList)
    }


    fun getAppName(id: Int): String {
        return when (id) {
            PBDevApp.DevAppID.APPID_SYSTEM_ACITIVITY_VALUE -> RyeexConst.WATCH_APP_ACTIVITY
            PBDevApp.DevAppID.APPID_SYSTEM_HRM_VALUE -> RyeexConst.WATCH_APP_HEART_RATE
            PBDevApp.DevAppID.APPID_SYSTEM_SPO_VALUE -> RyeexConst.WATCH_APP_BLOOD_OXYGEN
            PBDevApp.DevAppID.APPID_SYSTEM_SLEEP_VALUE -> RyeexConst.WATCH_APP_SLEEP
            PBDevApp.DevAppID.APPID_SYSTEM_SPORT_VALUE -> RyeexConst.WATCH_APP_SPORTS
            PBDevApp.DevAppID.APPID_SYSTEM_SPORT_RECORD_VALUE -> RyeexConst.WATCH_APP_SPORTS_RECORD
            PBDevApp.DevAppID.APPID_SYSTEM_BREATHE_VALUE -> RyeexConst.WATCH_APP_BREATHE
            PBDevApp.DevAppID.APPID_SYSTEM_ALARM_VALUE -> RyeexConst.WATCH_APP_ALARM_CLOCK
            PBDevApp.DevAppID.APPID_SYSTEM_WEATHER_VALUE -> RyeexConst.WATCH_APP_WEATHER
            PBDevApp.DevAppID.APPID_SYSTEM_WECHATPAY_VALUE -> RyeexConst.WATCH_APP_WC_PAY
            PBDevApp.DevAppID.APPID_SYSTEM_STOPWATCH_VALUE -> RyeexConst.WATCH_APP_SECOND
            PBDevApp.DevAppID.APPID_SYSTEM_TIMER_VALUE -> RyeexConst.WATCH_APP_TIMER
            PBDevApp.DevAppID.APPID_SYSTEM_MUSIC_VALUE -> RyeexConst.WATCH_APP_MUSIC_CONTROL
            PBDevApp.DevAppID.APPID_SYSTEM_TAKEPHOTO_VALUE -> RyeexConst.WATCH_APP_CAMERA
            PBDevApp.DevAppID.APPID_SYSTEM_FIND_PHONE_VALUE -> RyeexConst.WATCH_APP_FIND_PHONE
            PBDevApp.DevAppID.APPID_SYSTEM_SETTING_VALUE -> RyeexConst.WATCH_APP_SETTING
            PBDevApp.DevAppID.APPID_SYSTEM_ALIPAY_VALUE -> RyeexConst.WATCH_APP_ALIPAY
            PBDevApp.DevAppID.APPID_SYSTEM_STRESS_VALUE -> RyeexConst.WATCH_APP_PRESSURE
            PBDevApp.DevAppID.APPID_SYSTEM_PHONE_VALUE -> RyeexConst.WATCH_APP_PHONE
            PBDevApp.DevAppID.APPID_SYSTEM_CONTACTS_VALUE -> RyeexConst.WATCH_APP_CONTACTS
            PBDevApp.DevAppID.APPID_SYSTEM_CALCULATOR_VALUE -> RyeexConst.WATCH_APP_CALCULATOR
            PBDevApp.DevAppID.APPID_SYSTEM_FLASHLIGHT_VALUE -> RyeexConst.WATCH_APP_FLASHLIGHT
            PBDevApp.DevAppID.APPID_SYSTEM_BODY_TEMPERATURE_VALUE -> RyeexConst.WATCH_APP_TEMP
            else -> ""
        }
    }


    fun parseWeatherType(code: Int): Int {
        return when (code) {
            201, 202, 210, 211, 212, 221, 230, 231, 232 -> 4
            in 300..302, in 310..312, 500 -> 7
            313, 314, 321, in 520..531 -> 3
            501 -> 8
            502 -> 9
            503 -> 10
            504 -> 12
            511 -> 19
            600, 601 -> 32
            602 -> 16
            611, 612, 615, 616, in 620..622 -> 6
            613 -> 13
            701, 711, 741 -> 18
            721 -> 25
            731 -> 45
            751 -> 21
            761, 762 -> 20
            771 -> 42
            781 -> 43
            800 -> 0
            in 801..803 -> 1
            804 -> 2
            else -> {
                2
            }
        }
    }

    private fun isOutdoorSport(sportId: Int): Boolean {
        return when (sportId) {
            PBSport.Type.OUTDOOR_RUN_VALUE, PBSport.Type.OUTDOOR_WALK_VALUE, PBSport.Type.OUTDOOR_CYCLE_VALUE -> true
            else -> false
        }
    }

    companion object {
        fun parseSportMode(sportId: Int): SportsModeList.SportsMode {
            return SportsModeList.SportsMode(
                name = getSportName(sportId),
                type = sportId,
                value = true,
                remove = when (sportId) {
                    PBSport.Type.OUTDOOR_RUN_VALUE, PBSport.Type.OUTDOOR_WALK_VALUE -> false
                    else -> true
                }
            )
        }

        private fun getSportName(sportId: Int): String {
            return when (sportId) {
                1 -> RyeexConst.OUTDOOR_RUN
                2 -> RyeexConst.OUTDOOR_WALK
                3 -> RyeexConst.OUTDOOR_CYCLE
                4 -> RyeexConst.INDOOR_RUN
                5 -> RyeexConst.INDOOR_WALK
                6 -> RyeexConst.INDOOR_CYCLE
                7 -> RyeexConst.FREE
                8 -> RyeexConst.INDOOR_SWIM
                9 -> RyeexConst.YOGA
                10 -> RyeexConst.SOCCER
                11 -> RyeexConst.STRENGTH_TRAINING
                12 -> RyeexConst.BASKETBALL
                13 -> RyeexConst.TABLE_TENNIS // 乒乓球
                14 -> RyeexConst.BADMINTON // 羽毛球
                15 -> RyeexConst.ELLIPTICAL // 椭圆机
                16 -> RyeexConst.CRICKET // 板球
                17 -> RyeexConst.MOUNTAINEERING // 登山
                18 -> RyeexConst.TRAIL_RUNNING // 越野跑
                19 -> RyeexConst.SKIING // 滑雪
                20 -> RyeexConst.SPINNING // 动感单车
                21 -> RyeexConst.ROWER // 划船机
                22 -> RyeexConst.AIR_WALKER // 漫步机
                23 -> RyeexConst.HIKING // 徒步
                24 -> RyeexConst.TENNIS // 网球
                25 -> RyeexConst.FOLK_DANCE // 民族舞
                26 -> RyeexConst.DANCE // 舞蹈
                27 -> RyeexConst.COOL_DOWN // 整理放松
                28 -> RyeexConst.CROSS_TRAINING // 交叉训练
                29 -> RyeexConst.PILATES // 普拉提
                30 -> RyeexConst.CROSS_FIT // 交叉配合
                31 -> RyeexConst.FUNCTIONAL_TRAINING // 功能性训练
                32 -> RyeexConst.PHYSICAL_TRAINING // 体能训练
                33 -> RyeexConst.MIXED_CARDIO // 混合有氧
                34 -> RyeexConst.LATIN_DANCE // 拉丁舞
                35 -> RyeexConst.STREET_DANCE // 街舞
                36 -> RyeexConst.KICKBOXING // 自由搏击
                37 -> RyeexConst.BARRE // 芭蕾
                38 -> RyeexConst.AUSTRALIAN_FOOTBALL // 澳式足球
                39 -> RyeexConst.BASEBALL // 棒球
                40 -> RyeexConst.BOWLING // 保龄球
                41 -> RyeexConst.RACQUETBALL // 壁球
                42 -> RyeexConst.CURLING // 冰壶
                43 -> RyeexConst.HUNTING // 打猎
                44 -> RyeexConst.SNOWBOARDING // 单板滑雪
                45 -> RyeexConst.FISHING // 钓鱼
                46 -> RyeexConst.DISC_SPORTS // 飞盘运动
                47 -> RyeexConst.RUGBY // 橄榄球
                48 -> RyeexConst.GOLF // 高尔夫
                49 -> RyeexConst.DOWNHILL_SKIING // 高山滑雪
                50 -> RyeexConst.CORE_TRAINING // 核心训练
                51 -> RyeexConst.SKATING // 滑冰
                52 -> RyeexConst.FITNESS_GAMING // 健身游戏
                53 -> RyeexConst.AEROBICS // 健身操
                54 -> RyeexConst.GROUP_TRAINING // 团体操
                55 -> RyeexConst.KENDO // 搏击操
                56 -> RyeexConst.FENCING // 剑术/击剑
                57 -> RyeexConst.SOFTBALL // 垒球
                58 -> RyeexConst.STAIRS // 爬楼
                59 -> RyeexConst.AMERICAN_FOOTBALL // 美式橄榄球
                60 -> RyeexConst.VOLLEYBALL // 排球
                61 -> RyeexConst.ROLLING // 泡沫轴筋膜放松
                62 -> RyeexConst.PICKLEBALL // 匹克球
                63 -> RyeexConst.HOCKEY // 曲棍球
                64 -> RyeexConst.BOXING // 拳击
                65 -> RyeexConst.TAEKWONDO // 跆拳道
                66 -> RyeexConst.KARATE // 空手道
                67 -> RyeexConst.FLEXIBILITY // 柔韧度
                68 -> RyeexConst.HANDBALL // 手球
                69 -> RyeexConst.HAND_CYCLING // 手摇车
                70 -> RyeexConst.MIND_BODY // 舒缓冥想类运动
                71 -> RyeexConst.WRESTLING // 摔跤
                72 -> RyeexConst.STEP_TRAINING // 踏步训练
                73 -> RyeexConst.TAI_CHI // 太极
                74 -> RyeexConst.GYMNASTICS // 体操
                75 -> RyeexConst.TRACK_FIELD // 田径
                76 -> RyeexConst.JUMP_ROPE // 跳绳
                77 -> RyeexConst.MARTIAL_ARTS // 武术
                78 -> RyeexConst.PLAY // 休闲运动
                79 -> RyeexConst.SNOW_SPORTS // 雪上运动
                80 -> RyeexConst.LACROSSE // 长曲棍球
                81 -> RyeexConst.SINGLE_BAR // 单杠
                82 -> RyeexConst.PARALLEL_BARS // 双杠
                83 -> RyeexConst.ROLLER_SKATING // 轮滑
                84 -> RyeexConst.HULA_HOOP // 呼啦圈
                85 -> RyeexConst.DARTS // 飞镖
                86 -> RyeexConst.ARCHERY // 射箭
                87 -> RyeexConst.HORSE_RIDING // 骑马
                88 -> RyeexConst.SHUTTLECOCK // 毽球
                89 -> RyeexConst.ICE_HOCKEY // 冰球
                90 -> RyeexConst.SIT_UP // 仰卧起坐
                91 -> RyeexConst.WAIST_TRAINING // 腰腹训练
                92 -> RyeexConst.PARKOUR // 跑酷
                93 -> RyeexConst.CLIMB // 攀岩
                94 -> RyeexConst.KITE // 放风筝
                95 -> RyeexConst.TUG_OF_WAR // 拔河
                96 -> RyeexConst.BILLIARDS // 台球
                97 -> RyeexConst.DRIFTING // 漂流
                98 -> RyeexConst.DRAGON_BOAT // 龙舟
                99 -> RyeexConst.MOTORBOAT // 摩托艇
                100 -> RyeexConst.RACING_CAR // 赛车
                101 -> RyeexConst.JUDO // 柔道
                102 -> RyeexConst.HIIT // HIIT
                103 -> RyeexConst.PLANK // 平板支撑
                104 -> RyeexConst.PULLUP // 引体向上
                105 -> RyeexConst.PUSHUPS // 俯卧撑
                106 -> RyeexConst.SHOOTING // 射击
                107 -> RyeexConst.HIGH_JUMP // 跳高
                108 -> RyeexConst.BALANCE_CAR // 平衡车
                109 -> RyeexConst.LONG_JUMP // 跳远
                110 -> RyeexConst.MARATHON // 马拉松
                111 -> RyeexConst.TREADMILL // 跑步机
                112 -> RyeexConst.TRAMPOLINE // 蹦床
                113 -> RyeexConst.BUNGEE_JUMPING // 蹦极
                114 -> RyeexConst.SKATEBOARD // 滑板
                115 -> RyeexConst.BOATING // 划船
                116 -> RyeexConst.KABADDI // 卡巴迪
                117 -> RyeexConst.SQUAT // 深蹲
                118 -> RyeexConst.BURPEES // 波比跳
                else -> ""
            }
        }
    }


}