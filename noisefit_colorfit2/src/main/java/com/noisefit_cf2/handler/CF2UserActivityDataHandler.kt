package com.noisefit_cf2.handler

import com.ido.ble.BLEManager
import com.ido.ble.LocalDataManager
import com.ido.ble.business.sync.ISyncDataListener
import com.ido.ble.business.sync.ISyncProgressListener
import com.ido.ble.business.sync.SyncPara
import com.ido.ble.callback.AppExchangeDataCallBack
import com.ido.ble.data.manage.database.*
import com.ido.ble.gps.database.HealthGps
import com.ido.ble.gps.database.HealthGpsItem
import com.ido.ble.protocol.model.*
import com.noisefit_cf2.base.ColorFit2ApplicationHandler
import com.noisefit_cf2.dataconversions.Colorfit2DataConverter
import com.noisefit_commans.common.convertMinuteIntoSeconds
import com.noisefit_commans.common.handleHrData
import com.noisefit_commans.constants.EventConstants
import com.noisefit_commans.interfaces.data.IUserActivityDataCallback
import com.noisefit_commans.interfaces.data.UserActivityCallback
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.*
import java.text.DecimalFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt


class CF2UserActivityDataHandler
@Inject
constructor(
    private var dataConverter: Colorfit2DataConverter,
    private var colorFit2ApplicationHandler: ColorFit2ApplicationHandler
) : UserActivityDataActions() {
    private var userActivityDataCallbacks: IUserActivityDataCallback? = null
    private var colorFitDevice: ColorFitDevice? = null

    companion object {
        const val TAG = "CF2UserActivity"
    }

    override fun openGmailApp() {

    }

    override fun setDevice(colorFitDevice: ColorFitDevice) {
        this.colorFitDevice = colorFitDevice
    }


    override fun getBodyTemperatureData() {
    }


    private val isSyncProgressListener = object : ISyncProgressListener {
        override fun onStart() {
            LOGS.d(TAG, "start " + " start time : " + Date().time)
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.UserDataSyncUpdated(
                    SyncDataStatus(status = EventConstants.UPDATE_STATUS_STARTED)
                )
            )
        }

        override fun onProgress(p0: Int) {
            LOGS.d(TAG, "Progress $p0")
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.UserDataSyncUpdated(
                    SyncDataStatus(
                        progress = p0,
                        status = EventConstants.UPDATE_STATUS_IN_PROGRESS
                    )
                )
            )
        }

        override fun onSuccess() {
            LOGS.d(TAG, "Sync Success")
            AppLogs.sendAppLogs("Sync Data Success")
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.UserDataSyncUpdated(
                    SyncDataStatus(status = EventConstants.UPDATE_STATUS_SUCCESS)
                )
            )
            userActivityDataCallbacks?.onUserActivityDataReceived(UserActivityCallback.SportsModeDataSyncSuccess())
        }

        override fun onFailed() {
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.UserDataSyncUpdated(
                    SyncDataStatus(status = EventConstants.UPDATE_STATUS_FAILED)
                )
            )
            AppLogs.sendAppLogs(LogEvents.SyncData, SyncDataEvents.Other)
        }

    }

    private val iSyncDataListener = object : ISyncDataListener {
        override fun onGetSportData(
            p0: HealthSport?,
            p1: MutableList<HealthSportItem>?,
            p2: Boolean
        ) {
            LOGS.d(TAG, "onGetSportData: ")
            val stepsData = Colorfit2DataConverter.parseSportsData(p0, p1)
            stepsData?.let {
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.StepsDataObtained(it)
                )
            }
        }

        override fun onGetSleepData(p0: HealthSleep?, p1: MutableList<HealthSleepItem>?) {
            val sleepData = Colorfit2DataConverter.parseSleepData(p0, p1)
            sleepData?.let {
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.SleepDataObtained(it)
                )
            }
        }

        override fun onGetHeartRateData(
            p0: HealthHeartRate?,
            p1: MutableList<HealthHeartRateItem>?,
            p2: Boolean
        ) {
            if (p0 != null && !p1.isNullOrEmpty()) {
                val heartRateHistory =
                    Colorfit2DataConverter.parseHeartHistory(p1, p0)
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.HeartHistoryObtained(
                        heartRateHistory
                    )
                )
            }

        }

        override fun onGetBloodPressureData(
            p0: HealthBloodPressed?,
            p1: MutableList<HealthBloodPressedItem>?,
            p2: Boolean
        ) {

        }

        override fun onGetActivityData(healthActivity: HealthActivity) {
            LOGS.d(TAG, "onGetActivityData: ")

            val type = getActivityTypeString(healthActivity.type)
            val sportsModeList = SportsModeRequestList()

            val sportsModeResponse = SportsModeResponse(
                calories = healthActivity.calories.toLong(),
                duration = healthActivity.durations.toLong(),
                aerobic = healthActivity.aerobic_mins.convertMinuteIntoSeconds(),
                anaerobic = healthActivity.anaerobicMins.convertMinuteIntoSeconds(),
                fatBurn = healthActivity.burn_fat_mins.convertMinuteIntoSeconds(),
                warmUp = healthActivity.warmUpMins.convertMinuteIntoSeconds(),
                hrZoneInSeconds = 1,
                heartRateAvg = healthActivity.avg_hr_value,
                heartRateMax = healthActivity.max_hr_value,
                heartRateData = healthActivity.hr_data_vlaue?.handleHrData(healthActivity.durations),
                type = type,
                /* time = DateFormats.getFormattedTime(healthActivity.date),*/
                time = DateFormats.formatDateTime(
                    healthActivity.date,
                    DateFormats.dateTimeFormatISO
                ),
                date = DateFormats.dateFormat.format(healthActivity.date),
                heartRateAvailable = 1
            )

//            when (isDeviceSync) {
//                true -> {
//                    sportsModeList.responseType = "sync"
//                }
//                else -> {
//                    sportsModeList.responseType = "final"
//                }
//            }

            if (healthActivity.distance != 0 && healthActivity.durations != 0) {
                sportsModeResponse.distance = healthActivity.distance.toLong()
                val df = DecimalFormat("#.#")
                val pace = healthActivity.durations.toFloat()
                    .div(healthActivity.distance.toFloat())
                val speed = healthActivity.distance.toFloat()
                    .div(healthActivity.durations.toFloat())
                sportsModeResponse.pace = df.format(pace)?.toFloat()
                sportsModeResponse.speed = df.format(speed)?.toFloat()
                if (healthActivity.step != 0) {
                    sportsModeResponse.cadence =
                        (healthActivity.step / healthActivity.durations) * 60
                }
            } else {
                sportsModeResponse.pace = 0F
                sportsModeResponse.speed = 0F
                sportsModeResponse.cadence = 0
            }
            if (healthActivity.step != 0) {
                sportsModeResponse.steps = healthActivity.step
                sportsModeResponse.cadence =
                    (healthActivity.step / healthActivity.durations) * 60
            }

            val list = ArrayList<SportsModeResponse>()
            list.add(sportsModeResponse)
            sportsModeList.activities = list
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.SportsModeDataObtained(
                    sportsModeList
                )
            )


        }

        override fun onGetGpsData(p0: HealthGps?, p1: MutableList<HealthGpsItem>?, p2: Boolean) {

        }

        override fun onGetHealthSpO2Data(
            p0: HealthSpO2?,
            p1: MutableList<HealthSpO2Item>?,
            p2: Boolean
        ) {
            LOGS.d("CF2", "BloodOxygenObtained")

            val oxygenData = Colorfit2DataConverter.parseOxygenData(p0, p1)
            oxygenData?.let {
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.BloodOxygenObtained(it)
                )
            }
        }

        override fun onGetHealthPressureData(
            p0: HealthPressure?,
            p1: MutableList<HealthPressureItem>?,
            p2: Boolean
        ) {
            LOGS.d("CF2", "StressDataObtained")
            val stressData = Colorfit2DataConverter.parseStressData(p0, p1)
            stressData?.let {
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.StressDataObtained(it)
                )
            }
        }

        override fun onGetHealthHeartRateSecondData(p0: HealthHeartRateSecond?, p1: Boolean) {
            if (p0 != null) {
                val heartRateHistory = Colorfit2DataConverter.parseHeartHistoryV3(p0)
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.HeartHistoryObtained(
                        heartRateHistory
                    )
                )
            }
        }

        override fun onGetHealthSwimmingData(p0: HealthSwimming?) {
            LOGS.d(TAG, "onGetHealthSwimmingData: ")


            val type = getActivityTypeSwimming(p0?.type)
            val sportsModeList = SportsModeRequestList()

            val hour = p0?.hour
            val minute = p0?.minute
            val second = p0?.second
            val day = p0?.day
            val month = p0?.month
            val year = p0?.year

            val calendar = Calendar.getInstance()
            if (year != null && month != null && day != null
                && hour != null && minute != null && second != null
            ) {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, second)
            }


            val time = DateFormats.formatDateTime(
                calendar.time,
                DateFormats.dateTimeFormatISO
            )
            val sportsModeResponse = SportsModeResponse(
                calories = p0?.calories?.toLong(),
                duration = p0?.duration?.toLong(),
                hrZoneInSeconds = 1,
                aerobic = 0,
                anaerobic = 0,
                fatBurn = 0,
                warmUp = 0,
                heartRateAvg = 0,
                heartRateMax = 0, heartRateData = null, type = type,
                time = time, date = "$day/$month/$year",
                heartRateAvailable = 0
            )

            when (type) {
                "pool_swimming", "open_water_swimming" -> {
                    sportsModeResponse.distance = p0?.distance?.toLong()
                    if (p0?.distance != 0) {
                        sportsModeResponse.pace = 0F
                        sportsModeResponse.speed = (p0?.avg_speed)?.toFloat()
                        sportsModeResponse.cadence = 0

                    } else {
                        sportsModeResponse.pace = 0F
                        sportsModeResponse.speed = 0F
                        sportsModeResponse.cadence = 0
                    }
                    sportsModeResponse.avgSWOLF = p0?.averageSWOLF
                    sportsModeResponse.avgStepStride = p0?.totalStrokesNumber

                }
            }

            val list = ArrayList<SportsModeResponse>()
            list.add(sportsModeResponse)
            sportsModeList.activities = list
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.SportsModeDataObtained(
                    sportsModeList
                )
            )

            userActivityDataCallbacks?.onUserActivityDataReceived(UserActivityCallback.SportsModeDataSyncSuccess())

        }

        override fun onGetHealthActivityV3Data(healthActivity: HealthActivityV3?) {
            LOGS.d(TAG, "onGetHealthActivityV3Data: ")
            //  isDeviceSync = false
            val type = getActivityTypeStringV3(healthActivity?.type)
            val sportsModeList = SportsModeRequestList()
            val hour = healthActivity?.hour
            val minute = healthActivity?.minute
            val second = healthActivity?.second
            val day = healthActivity?.day
            val month = healthActivity?.month
            val year = healthActivity?.year

            val calendar = Calendar.getInstance()
            if (year != null && month != null && day != null
                && hour != null && minute != null && second != null
            ) {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, second)
            }


            val time = DateFormats.formatDateTime(
                calendar.time,
                DateFormats.dateTimeFormatISO
            )
            val sportsModeResponse = SportsModeResponse(
                calories = healthActivity?.calories?.toLong(),
                duration = healthActivity?.durations?.toLong(),
                aerobic = healthActivity?.aerobic_mins.convertMinuteIntoSeconds(),
                anaerobic = healthActivity?.anaerobicMins.convertMinuteIntoSeconds(),
                fatBurn = healthActivity?.burn_fat_mins.convertMinuteIntoSeconds(),
                warmUp = healthActivity?.warmUpMins.convertMinuteIntoSeconds(),
                hrZoneInSeconds = 1,
                heartRateAvg = healthActivity?.avg_hr_value,
                heartRateMax = healthActivity?.max_hr_value,
                heartRateData = healthActivity?.hr_data_vlaue?.handleHrData(healthActivity.durations),
                type = type,
                time = time,
                date = "$day/$month/$year",
                heartRateAvailable = 0
            )


            if (healthActivity?.distance != 0 && healthActivity?.durations != 0) {
                sportsModeResponse.distance = healthActivity?.distance?.toLong()
                val df = DecimalFormat("#.#")
                val pace = healthActivity?.durations?.toFloat()!!
                    .div(healthActivity.distance.toFloat())
                val speed = healthActivity.distance.toFloat()
                    .div(healthActivity.durations.toFloat())
                sportsModeResponse.pace = df.format(pace)?.toFloat()
                sportsModeResponse.speed = df.format(speed)?.toFloat()

                if (healthActivity.step != 0) {
                    sportsModeResponse.steps = healthActivity.step
                    val cadenceDouble: Double =
                        (healthActivity.step.toDouble() / healthActivity.durations.toDouble()) * (60.0)
                    LOGS.d("AverageCadence", "$cadenceDouble")
                    sportsModeResponse.cadence = try {
                        cadenceDouble.roundToInt()
                    } catch (exp: Exception) {
                        0
                    }

                    LOGS.d("AverageCadence", "${sportsModeResponse.cadence}")
                }
            } else {
                sportsModeResponse.pace = 0F
                sportsModeResponse.speed = 0F
                sportsModeResponse.cadence = 0
            }


            sportsModeResponse.avgStepFrequency = healthActivity.avg_step_frequency
            sportsModeResponse.avgStepStride = healthActivity.avg_step_stride
            sportsModeResponse.maxStepFrequency = healthActivity.max_step_frequency
            sportsModeResponse.maxStepStride = healthActivity.max_step_stride

            val list = ArrayList<SportsModeResponse>()
            list.add(sportsModeResponse)
            sportsModeList.activities = list
            LOGS.d("SportsModeDataObtained ")
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.SportsModeDataObtained(
                    sportsModeList
                )
            )
            userActivityDataCallbacks?.onUserActivityDataReceived(UserActivityCallback.SportsModeDataSyncSuccess())


        }

        override fun onGetHealthSportV3Data(p0: HealthSportV3?) {
            LOGS.d(TAG, "onGetHealthSportV3Data")
            val stepsData = Colorfit2DataConverter.parseSportsDataV3(p0, p0?.items)
            stepsData?.let {
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.StepsDataObtained(it)
                )
            }
        }

        override fun onGetHealthSleepV3Data(p0: HealthSleepV3?) {
            LOGS.d(TAG, "onGetHealthSleepV3Data")
            colorFitDevice?.let {
                val sleepData = Colorfit2DataConverter.parseSleepDataV3(it, p0, p0?.items)
                // sleepData?.let { if(it.total>0) {userActivityDataCallbacks.onSleepDataObtained(it)} }
                sleepData?.let {
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.SleepDataObtained(it)
                    )
                }
            }
        }

        override fun onGetHealthGpsV3Data(p0: HealthGpsV3?) {

        }

        override fun onGetHealthNoiseData(p0: HealthNoise?) {

        }

        override fun onGetHealthTemperature(p0: HealthTemperature?) {

        }

        override fun onGetHealthBloodPressure(p0: HealthBloodPressureV3?) {
            
        }

        override fun onGetHealthRespiratoryRate(p0: HealthRespiratoryRate?) {
            
        }

        override fun onGetHealthBodyPower(p0: HealthBodyPower?) {
            
        }

        override fun onGetHealthHRV(p0: HealthHRVdata?) {

        }


    }

    private fun syncData() {
        try {
            BLEManager.getFunctionTables()
            val syncPara = SyncPara()
            syncPara.iSyncDataListener = iSyncDataListener
            syncPara.iSyncProgressListener = isSyncProgressListener
            BLEManager.syncAllData(syncPara)
        }catch (e : Exception){

        }
    }

    override fun getStepsData(date: String) {

    }


    override fun getSleepData(date: String) {

    }

    override fun getHeartRate() {


    }

    override fun init() {
        super.init()
        //BLEManager.init()

    }

    override fun getBloodOxygenLevel() {
        //val abc = LocalDataManager.getHealthSportItemByDay(2020,11,11)
        startSyncHealth()
    }

    override fun getBloodPressure() {}

    override fun getStressCount() {

    }


    override fun <T> callbackListener(callback: T) {
        //
    }

    override fun <T> callbackListenerNew(callback: T) {
        userActivityDataCallbacks = callback as IUserActivityDataCallback
    }

    override fun syncUserActivity(date: String, isRefresh: Boolean) {
        LOGS.d("CF2", "syncUserActivity")
        syncData()

        // getHeartHistory(Calendar.getInstance())
    }

    override fun attachCallbacks() {
        try {
            removeCallbacks()
            BLEManager.registerAppExchangeDataCallBack(appExchangeDataCallBack)
        } catch (e: Exception) {
            LOGS.d("Report to supplier please")
            e.printStackTrace()
        }

    }

    override fun removeCallbacks() {
        super.removeCallbacks()
        BLEManager.unregisterAppExchangeDataCallBack(appExchangeDataCallBack)
    }

    override fun updateSportsMode(sportsModeRequest: SportsModeRequest) {

        LOGS.d(TAG, "updateSportsMode $sportsModeRequest")
        when (sportsModeRequest.status) {
            "start" -> {
                val para = AppExchangeDataStartPara()
                para.day = sportsModeRequest.day
                para.hour = sportsModeRequest.hour
                para.minute = sportsModeRequest.minute
                para.second = 0
                colorFitDevice?.deviceType?.let { deviceType ->
                    if (deviceType == DeviceType.COLORFIT_PRO_2.deviceType) {
                        para.sportType = when (sportsModeRequest.activityType?.lowercase()) {
                            "running", "outdoor_running" -> AppExchangeDataStartPara.SPORT_TYPE_RUN
                            "bicycling", "biking" -> AppExchangeDataStartPara.SPORT_TYPE_CYCLING
                            "climbing" -> AppExchangeDataStartPara.SPORT_TYPE_CLIMB
                            "treadmill" -> AppExchangeDataStartPara.SPORT_TYPE_TREADMILL
                            "yoga" -> AppExchangeDataStartPara.SPORT_TYPE_YOGA
                            "workout" -> AppExchangeDataStartPara.SPORT_TYPE_FITNESS
                            "rowing_machine" -> AppExchangeDataStartPara.SOPRT_TYPE_ROWER
                            "elliptical_machine" -> AppExchangeDataStartPara.SOPRT_TYPE_ELLIPTICAL
                            "basketball" -> AppExchangeDataStartPara.SPORT_TYPE_BASKETBALL
                            "football" -> AppExchangeDataStartPara.SPORT_TYPE_SOCKER
                            "tennis" -> AppExchangeDataStartPara.SPORT_TYPE_TENNISBALL
                            "dance" -> AppExchangeDataStartPara.SPORT_TYPE_DANCING
                            "badminton" -> AppExchangeDataStartPara.SPORT_TYPE_BADMINTON
                            "swimming" -> AppExchangeDataStartPara.SPORT_TYPE_SWIM
                            "walking", "outdoor_walking" -> AppExchangeDataStartPara.SPORT_TYPE_WALK
                            "outdoor_cycling" -> AppExchangeDataStartPara.SOPRT_TYPE_OUTDOOR_CYCLE
                            "indoor_cycling" -> AppExchangeDataStartPara.SOPRT_TYPE_INDOOR_CYCLE
                            "indoor_walking" -> AppExchangeDataStartPara.SOPRT_TYPE_INDOOR_WALK
                            "indoor_running" -> AppExchangeDataStartPara.SOPRT_TYPE_INDOOR_RUN
                            "elliptical" -> AppExchangeDataStartPara.SOPRT_TYPE_ELLIPTICAL
                            "rower" -> AppExchangeDataStartPara.SOPRT_TYPE_ROWER
                            "cricket" -> AppExchangeDataStartPara.SPORT_TYPE_CRICKET
                            "pool_swimming" -> AppExchangeDataStartPara.SOPRT_TYPE_POOL_SWIM
                            "open_water_swimming" -> AppExchangeDataStartPara.SOPRT_TYPE_WATER_SWIM
                            "hiking" -> AppExchangeDataStartPara.SPORT_TYPE_ONFOOT
                            "spinning" -> AppExchangeDataStartPara.SPORT_TYPE_DYNAMIC
                            else -> AppExchangeDataStartPara.SOPRT_TYPE_OUTDOOR_WALK
                        }
                    } else {
                        para.sportType = when (sportsModeRequest.activityType?.lowercase()) {
                            "running", "outdoor_running" -> AppExchangeDataStartPara.SOPRT_TYPE_OUTDOOR_RUN
                            "bicycling", "biking" -> AppExchangeDataStartPara.SPORT_TYPE_CYCLING
                            "climbing" -> AppExchangeDataStartPara.SPORT_TYPE_CLIMB
                            "treadmill" -> AppExchangeDataStartPara.SPORT_TYPE_TREADMILL
                            "yoga" -> AppExchangeDataStartPara.SPORT_TYPE_YOGA
                            "workout" -> AppExchangeDataStartPara.SPORT_TYPE_FITNESS
                            "rowing_machine" -> AppExchangeDataStartPara.SOPRT_TYPE_ROWER
                            "elliptical_machine" -> AppExchangeDataStartPara.SOPRT_TYPE_ELLIPTICAL
                            "basketball" -> AppExchangeDataStartPara.SPORT_TYPE_BASKETBALL
                            "football" -> AppExchangeDataStartPara.SPORT_TYPE_SOCKER
                            "tennis" -> AppExchangeDataStartPara.SPORT_TYPE_TENNISBALL
                            "dance" -> AppExchangeDataStartPara.SPORT_TYPE_DANCING
                            "badminton" -> AppExchangeDataStartPara.SPORT_TYPE_BADMINTON
                            "swimming" -> AppExchangeDataStartPara.SPORT_TYPE_SWIM
                            "walking", "outdoor_walking" -> AppExchangeDataStartPara.SOPRT_TYPE_OUTDOOR_WALK
                            "outdoor_cycling" -> AppExchangeDataStartPara.SOPRT_TYPE_OUTDOOR_CYCLE
                            "indoor_cycling" -> AppExchangeDataStartPara.SOPRT_TYPE_INDOOR_CYCLE
                            "indoor_walking" -> AppExchangeDataStartPara.SOPRT_TYPE_INDOOR_WALK
                            "indoor_running" -> AppExchangeDataStartPara.SOPRT_TYPE_INDOOR_RUN
                            "elliptical" -> AppExchangeDataStartPara.SOPRT_TYPE_ELLIPTICAL
                            "rower" -> AppExchangeDataStartPara.SOPRT_TYPE_ROWER
                            "cricket" -> AppExchangeDataStartPara.SPORT_TYPE_CRICKET
                            "pool_swimming" -> AppExchangeDataStartPara.SOPRT_TYPE_POOL_SWIM
                            "open_water_swimming" -> AppExchangeDataStartPara.SOPRT_TYPE_WATER_SWIM
                            "hiking" -> AppExchangeDataStartPara.SPORT_TYPE_ONFOOT
                            "spinning" -> AppExchangeDataStartPara.SPORT_TYPE_DYNAMIC
                            else -> AppExchangeDataStartPara.SOPRT_TYPE_OUTDOOR_WALK
                        }
                    }
                }

                para.force_start = AppExchangeDataStartPara.FORCE_START_VALID

                BLEManager.appExchangeDataStart(para)
            }
            "pause" -> {
                val para = AppExchangeDataPausePara()
                para.day = sportsModeRequest.day
                para.hour = sportsModeRequest.hour
                para.minute = sportsModeRequest.minute
                para.second = 0
                BLEManager.appExchangeDataPause(para)
            }

            "resume" -> {
                val para = AppExchangeDataResumePara()
                para.day = sportsModeRequest.day
                para.hour = sportsModeRequest.hour
                para.minute = sportsModeRequest.minute
                para.second = 0
                BLEManager.appExchangeDataResume(para)
            }

            "stop" -> {
                val para = AppExchangeDataStopPara()
                para.day = sportsModeRequest.day
                para.hour = sportsModeRequest.hour
                para.minute = sportsModeRequest.minute
                para.second = 0

                para.durations = sportsModeRequest.duration
                para.calories = sportsModeRequest.calories
                para.distance = sportsModeRequest.distance
                para.sport_type = AppExchangeDataStartPara.SPORT_TYPE_DANCING
                para.is_save = AppExchangeDataStopPara.IS_SAVE_YES

                BLEManager.appExchangeDataStop(para)
            }
        }
    }


    override fun getHeartHistory(calendar: Calendar) {
        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType || deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType
                || deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType || deviceType == DeviceType.NOISEFIT_AGILE.deviceType
            ) {
                LOGS.d("CF2", "HeartHistoryObtained")
                val healthHeartRateV3 = LocalDataManager.getHealthHeartRateSecondByDay(
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH] + 1,
                    calendar[Calendar.DAY_OF_MONTH]
                )
                val heartRateHistory = Colorfit2DataConverter.parseHeartHistoryV3(healthHeartRateV3)
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.HeartHistoryObtained(
                        heartRateHistory
                    )
                )
            } else {

                val healthHeartRate = LocalDataManager.getHealthHeartRateByDay(
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH] + 1,
                    calendar[Calendar.DAY_OF_MONTH]
                )
                val heartList = LocalDataManager.getHealthHeartRateItemByDay(
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH] + 1,
                    calendar[Calendar.DAY_OF_MONTH]
                )
                val heartRateHistory =
                    Colorfit2DataConverter.parseHeartHistory(heartList, healthHeartRate)
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.HeartHistoryObtained(
                        heartRateHistory
                    )
                )
            }
        }
    }

    private val appExchangeDataCallBack: AppExchangeDataCallBack.ICallBack =
        object : AppExchangeDataCallBack.ICallBack {
            override fun onReplyExchangeDataStart(data: AppExchangeDataStartDeviceReplyData) {
                LOGS.d(TAG, "onReplyExchangeDataStart")
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.SportsModeStatusChange(
                        SyncDataStatus(status = "start")
                    )
                )
            }

            override fun onReplyExchangeDateIng(data: AppExchangeDataIngDeviceReplyData) {
                LOGS.d(TAG, "onReplyExchangeDateIng")

                val sportsModeResponse = SportsModeResponse(
                    heartRateCurrent = data.cur_hr_value,
                    calories = data.calories.toLong(),
                    distance = data.distance.toLong(),
                    heartRateData = data.hr_value?.handleHrData(data.interval_second),
                    duration = data.interval_second.toLong()
                )

                sportsModeResponse.steps = data.step
                val list = ArrayList<SportsModeResponse>()
                list.add(sportsModeResponse)
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.SportsModeDataObtained(
                        SportsModeRequestList(responseType = "update", activities = list)
                    )
                )
                userActivityDataCallbacks?.onUserActivityDataReceived(UserActivityCallback.SportsModeDataSyncSuccess())
            }

            override fun onReplyExchangeDateStop(data: AppExchangeDataStopDeviceReplyData) {
                LOGS.d(TAG, "onReplyExchangeDateStop")
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.SportsModeStatusChange(
                        SyncDataStatus(status = "stop")
                    )
                )
                //BLEManager.startSyncActivityData()
            }

            override fun onReplyExchangeDatePause(data: AppExchangeDataPauseDeviceReplyData) {
                LOGS.d(TAG, "onReplyExchangeDatePause")
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.SportsModeStatusChange(
                        SyncDataStatus(status = "pause")
                    )
                )
            }

            override fun onReplyExchangeDateResume(data: AppExchangeDataResumeDeviceReplyData) {
                LOGS.d(TAG, "onReplyExchangeDateResume")
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.SportsModeStatusChange(
                        SyncDataStatus(status = "resume")
                    )
                )
            }

            override fun onDeviceNoticeAppStop(para: DeviceNoticeAppExchangeDataStopPara) {
                LOGS.d(TAG, "onDeviceNoticeAppStop")
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.SportsModeStatusChange(
                        SyncDataStatus(status = "stop")
                    )
                )
                val data = DeviceNoticeAppExchangeDataStopAppReplyData()
                data.err_code = DeviceNoticeAppExchangeDataStopAppReplyData.CODE_SUCCESS
                BLEManager.replyDeviceNoticeAppExchangeDataStop(data)
                //BLEManager.startSyncActivityData()
            }

            override fun onDeviceNoticeAppPause(para: DeviceNoticeAppExchangeDataPausePara) {
                LOGS.d(TAG, "onDeviceNoticeAppPause")
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.SportsModeStatusChange(
                        SyncDataStatus(status = "pause")
                    )
                )
                val data = DeviceNoticeAppExchangeDataPauseAppReplyData()
                data.err_code = DeviceNoticeAppExchangeDataPauseAppReplyData.CODE_SUCCESS
                BLEManager.replyDeviceNoticeAppExchangeDataPause(data)
            }

            override fun onDeviceNoticeAppResume(para: DeviceNoticeAppExchangeDataResumePara) {
                LOGS.d(TAG, "onDeviceNoticeAppResume")
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.SportsModeStatusChange(
                        SyncDataStatus(status = "resume")
                    )
                )
                val data = DeviceNoticeAppExchangeDataResumeAppReplyData()
                data.err_code = DeviceNoticeAppExchangeDataResumeAppReplyData.CODE_SUCCESS
                BLEManager.replyDeviceNoticeAppExchangeDataResume(data)
            }
        }

    override fun refresh(sportsModeRequest: SportsModeRequest) {
        val para = AppExchangeDataIngPara()
        para.day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        para.hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        para.minute = Calendar.getInstance().get(Calendar.MINUTE)
        para.distance = sportsModeRequest.distance
        para.second = Calendar.getInstance().get(Calendar.SECOND)

        para.status = AppExchangeDataIngPara.STATUS_ALL_VALID
        para.duration = 0
        para.calories = 0
        // para.distance = 0

        BLEManager.appExchangeDataIng(para)
//        updateSportsModeData(sportsModeRequest)
//        colorFitDevice?.deviceType?.let { deviceType ->
//            if (deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType
//                || deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType || deviceType == DeviceType.NOISEFIT_AGILE.deviceType
//            ) {
//                val para = V3AppExchangeDataIngPara()
//                para.type = when (sportsModeRequest.activityType?.lowercase()) {
//                    "running", "outdoor_running" -> AppExchangeDataStartPara.SOPRT_TYPE_OUTDOOR_RUN
//                    "bicycling", "biking" -> AppExchangeDataStartPara.SPORT_TYPE_CYCLING
//                    "climbing" -> AppExchangeDataStartPara.SPORT_TYPE_CLIMB
//                    "treadmill" -> AppExchangeDataStartPara.SPORT_TYPE_TREADMILL
//                    "yoga" -> AppExchangeDataStartPara.SPORT_TYPE_YOGA
//                    "workout" -> AppExchangeDataStartPara.SPORT_TYPE_FITNESS
//                    "rowing_machine" -> AppExchangeDataStartPara.SOPRT_TYPE_ROWER
//                    "elliptical_machine" -> AppExchangeDataStartPara.SOPRT_TYPE_ELLIPTICAL
//                    "basketball" -> AppExchangeDataStartPara.SPORT_TYPE_BASKETBALL
//                    "football" -> AppExchangeDataStartPara.SPORT_TYPE_SOCKER
//                    "tennis" -> AppExchangeDataStartPara.SPORT_TYPE_TENNISBALL
//                    "dance" -> AppExchangeDataStartPara.SPORT_TYPE_DANCING
//                    "badminton" -> AppExchangeDataStartPara.SPORT_TYPE_BADMINTON
//                    "swimming" -> AppExchangeDataStartPara.SPORT_TYPE_SWIM
//                    "walking", "outdoor_walking" -> AppExchangeDataStartPara.SOPRT_TYPE_OUTDOOR_WALK
//                    "outdoor_cycling" -> AppExchangeDataStartPara.SOPRT_TYPE_OUTDOOR_CYCLE
//                    "indoor_cycling" -> AppExchangeDataStartPara.SOPRT_TYPE_INDOOR_CYCLE
//                    "indoor_walking" -> AppExchangeDataStartPara.SOPRT_TYPE_INDOOR_WALK
//                    "indoor_running" -> AppExchangeDataStartPara.SOPRT_TYPE_INDOOR_RUN
//                    "elliptical" -> AppExchangeDataStartPara.SOPRT_TYPE_ELLIPTICAL
//                    "rower" -> AppExchangeDataStartPara.SOPRT_TYPE_ROWER
//                    "cricket" -> AppExchangeDataStartPara.SPORT_TYPE_CRICKET
//                    "pool_swimming" -> AppExchangeDataStartPara.SOPRT_TYPE_POOL_SWIM
//                    "open_water_swimming" -> AppExchangeDataStartPara.SOPRT_TYPE_WATER_SWIM
//                    "hiking" -> AppExchangeDataStartPara.SPORT_TYPE_ONFOOT
//                    else -> AppExchangeDataStartPara.SOPRT_TYPE_OUTDOOR_WALK
//                }
////                para.status = AppExchangeDataIngPara.STATUS_ALL_VALID
////                para.duration = 0
////                para.calories = 0
//                para.distance = this.sportsMode?.distance ?: 0
//                LOGS.d("updateSportsModeData $para")
//                BLEManager.v3AppExchangeDataIng(para)
//            } else {
//                val para = AppExchangeDataIngPara()
//                para.day = this.sportsMode?.day ?: 0
//                para.hour = this.sportsMode?.hour ?: 0
//                para.minute = this.sportsMode?.minute ?: 0
//                para.distance = this.sportsMode?.distance ?: 0
//                para.second = 0
//
//                para.status = AppExchangeDataIngPara.STATUS_ALL_VALID
//                para.duration = 0
//                para.calories = 0
//                // para.distance = 0
//
//                BLEManager.appExchangeDataIng(para)
//            }
//        }

    }


    private fun getActivityTypeString(type: Int?): String {
        return when (type) {
            AppExchangeDataStartPara.SOPRT_TYPE_OUTDOOR_RUN,
            AppExchangeDataStartPara.SPORT_TYPE_RUN -> "running"
            AppExchangeDataStartPara.SPORT_TYPE_CYCLING -> "bicycling"
            AppExchangeDataStartPara.SPORT_TYPE_CLIMB -> "climbing"
            AppExchangeDataStartPara.SPORT_TYPE_TREADMILL -> "treadmill"
            AppExchangeDataStartPara.SPORT_TYPE_YOGA -> "yoga"
            AppExchangeDataStartPara.SPORT_TYPE_FITNESS -> "workout"
            AppExchangeDataStartPara.SOPRT_TYPE_ROWER -> "rowing_machine"
            AppExchangeDataStartPara.SOPRT_TYPE_ELLIPTICAL -> "elliptical_machine"
            AppExchangeDataStartPara.SPORT_TYPE_BASKETBALL -> "basketball"
            AppExchangeDataStartPara.SPORT_TYPE_SOCKER -> "football"
            AppExchangeDataStartPara.SPORT_TYPE_TENNISBALL -> "tennis"
            AppExchangeDataStartPara.SPORT_TYPE_DANCING -> "dance"
            AppExchangeDataStartPara.SPORT_TYPE_BADMINTON -> "badminton"
            AppExchangeDataStartPara.SPORT_TYPE_SWIM -> "swimming"
            AppExchangeDataStartPara.SOPRT_TYPE_OUTDOOR_WALK -> "walking"
            AppExchangeDataStartPara.SPORT_TYPE_DYNAMIC -> "spinning"
            AppExchangeDataStartPara.SPORT_TYPE_ONFOOT -> "hiking"
            else -> "walking"
        }
    }

    private fun getActivityTypeStringV3(type: Int?): String {
        return when (type) {
            AppExchangeDataStartPara.SOPRT_TYPE_OUTDOOR_RUN -> "outdoor_running"
            AppExchangeDataStartPara.SPORT_TYPE_RUN -> "running"
            AppExchangeDataStartPara.SPORT_TYPE_CYCLING -> "bicycling"
            AppExchangeDataStartPara.SPORT_TYPE_CLIMB -> "climbing"
            AppExchangeDataStartPara.SPORT_TYPE_TREADMILL -> "treadmill"
            AppExchangeDataStartPara.SPORT_TYPE_YOGA -> "yoga"
            AppExchangeDataStartPara.SPORT_TYPE_FITNESS -> "workout"
            AppExchangeDataStartPara.SPORT_TYPE_BASKETBALL -> "basketball"
            AppExchangeDataStartPara.SPORT_TYPE_SOCKER -> "football"
            AppExchangeDataStartPara.SPORT_TYPE_TENNISBALL -> "tennis"
            AppExchangeDataStartPara.SPORT_TYPE_DANCING -> "dance"
            AppExchangeDataStartPara.SPORT_TYPE_BADMINTON -> "badminton"
            AppExchangeDataStartPara.SPORT_TYPE_SWIM -> "swimming"
            AppExchangeDataStartPara.SOPRT_TYPE_OUTDOOR_WALK -> "outdoor_walking"
            AppExchangeDataStartPara.SPORT_TYPE_DYNAMIC -> "spinning"
            AppExchangeDataStartPara.SPORT_TYPE_ONFOOT -> "hiking"
            AppExchangeDataStartPara.SOPRT_TYPE_OUTDOOR_CYCLE -> "outdoor_cycling"
            AppExchangeDataStartPara.SOPRT_TYPE_INDOOR_WALK -> "indoor_walking"
            AppExchangeDataStartPara.SOPRT_TYPE_INDOOR_RUN -> "indoor_running"
            AppExchangeDataStartPara.SOPRT_TYPE_INDOOR_CYCLE -> "indoor_cycling"
            AppExchangeDataStartPara.SOPRT_TYPE_ELLIPTICAL -> "elliptical"
            AppExchangeDataStartPara.SOPRT_TYPE_ROWER -> "rower"
            AppExchangeDataStartPara.SPORT_TYPE_CRICKET -> "cricket"
            AppExchangeDataStartPara.SOPRT_TYPE_POOL_SWIM -> "pool_swimming"
            AppExchangeDataStartPara.SOPRT_TYPE_WATER_SWIM -> "open_water_swimming"
            else -> "walking"
        }
    }

    private fun getActivityTypeSwimming(type: Int?): String {
        return when (type) {
            1 -> "pool_swimming"
            2 -> "open_water_swimming"
            else -> "invalid"
        }
    }


    override fun syncSportsActivity(date: String) {
        LOGS.d(TAG, "syncSportsActivity")
        //SyncDataWork Called in its place for this SDK
    }

    private fun startSyncHealth() {
        LOGS.d(TAG, "startSyncHealth")
        BLEManager.getFunctionTables()
    }

    override fun pushGPSData(gpsSignal: Int, distance: Int) {
        LOGS.d(TAG, "pushGPSData")
        val para = V3AppExchangeDataIngPara()
        para.signalFlag = gpsSignal
        para.distance = distance
        BLEManager.v3AppExchangeDataIng(para)
    }

    override fun disableEnableBluetooth() {

    }
}

