package com.noisefit_zhsdk.handler


import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.constants.SyncEvents
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.interfaces.data.IUserActivityDataCallback
import com.noisefit_commans.interfaces.data.UserActivityCallback
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.LocationDataModel
import com.noisefit_commans.models.WeatherData
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.ConnectEvents
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LocationClientClass
import com.noisefit_commans.utils.LogEvents
import com.noisefit_zhsdk.base.ZhApplicationHandler
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.ActivityDurationBean
import com.zhapp.ble.bean.ContinuousBloodOxygenBean
import com.zhapp.ble.bean.ContinuousHeartRateBean
import com.zhapp.ble.bean.ContinuousPressureBean
import com.zhapp.ble.bean.ContinuousTemperatureBean
import com.zhapp.ble.bean.DailyBean
import com.zhapp.ble.bean.DevSportInfoBean
import com.zhapp.ble.bean.EffectiveStandingBean
import com.zhapp.ble.bean.ExaminationBean
import com.zhapp.ble.bean.OffEcgDataBean
import com.zhapp.ble.bean.OfflineBloodOxygenBean
import com.zhapp.ble.bean.OfflineHeartRateBean
import com.zhapp.ble.bean.OfflinePressureDataBean
import com.zhapp.ble.bean.OfflineTemperatureDataBean
import com.zhapp.ble.bean.OverallDayMovementData
import com.zhapp.ble.bean.PhoneSportDataBean
import com.zhapp.ble.bean.RealTimeBean
import com.zhapp.ble.bean.RingHealthScoreBean
import com.zhapp.ble.bean.RingSleepNapBean
import com.zhapp.ble.bean.RingSleepResultBean
import com.zhapp.ble.bean.SleepBean
import com.zhapp.ble.bean.SportRequestBean
import com.zhapp.ble.bean.SportResponseBean
import com.zhapp.ble.bean.SportStatusBean
import com.zhapp.ble.bean.TodayActiveTypeData
import com.zhapp.ble.bean.TodayRespiratoryRateData
import com.zhapp.ble.callback.AutoSportDataCallBack
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.FitnessDataCallBack
import com.zhapp.ble.callback.RealTimeDataCallBack
import com.zhapp.ble.callback.SportCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import javax.inject.Inject

private inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

class ZhUserActivityHandler
@Inject
constructor(
    private var dataConverter: DataConverter,
    private var oreoDataConverter: OreoDataConverter,
    val context: Context,
    var watchDataStore: WatchDataStore,
    val zhApplicationHandler: ZhApplicationHandler
) : UserActivityDataActions() {

    companion object {
        const val LOCATION_BROADCAST_RECEIVER = "LOCATION_BROADCAST_RECEIVER"
        const val LAT_LONG = "LAT_LONG"
    }

    private val TAG = "ZhUserActivityHandler"
    private val TRACK_TAG = "LUNA->"
    private var userActivityDataCallbacks: IUserActivityDataCallback? = null
    private var sportModleInfoList = ArrayList<DevSportInfoBean>()
    private var isSyncProtoSportSyncing = false

    private var isPause = false
    private var locationClientClass: LocationClientClass? = null
    private var firstLocation = false
    private var currentGpsSportState = -1
    private var mLatitude = 0.0
    private var mLongitude = 0.0

    //上次发送辅助定位数据的时间戳,经纬度
    private var mLastTime = 0L

    //上次发送的定位经纬度
    private var mLastLat = 0.0
    private var mLastLon = 0.0


    override fun <T> callbackListener(callback: T) {
        //userActivityDataCallbacks = callback as IUserActivityDataCallback
    }

    override fun <T> callbackListenerNew(callback: T) {
        userActivityDataCallbacks = callback as IUserActivityDataCallback
    }

    private var colorFitDevice: ColorFitDevice? = null

    override fun attachCallbacks() {
        LOGS.d(TAG, "Activity Callback attached")




        CallBackUtils.fitnessDataCallBack = fitnessDataCallBack
        CallBackUtils.autoSportDataCallBack = autoSportsCallback
        CallBackUtils.realTimeDataCallback = realDataCallback
        CallBackUtils.setSportCallBack(object : SportCallBack {
            override fun onDevSportInfo(data: DevSportInfoBean) {
                LOGS.d(TAG, "onDevSportInfo $data")
                sportModleInfoList.add(data)
            }

            override fun onSportStatus(statusBean: SportStatusBean) {
                LOGS.d(TAG, "onSportStatus $statusBean")
            }

            override fun onSportRequest(requestBean: SportRequestBean) {
                LOGS.d(TAG, "onSportRequest $requestBean")
                deviceRequest(requestBean)
            }
        })

        CallBackUtils.setSportParsingProgressCallBack { progress, total ->
            LOGS.d(TAG, "setSportParsingProgressCallBack $progress $total")
            if (progress == total) {
                sportModleInfoList.forEach { sportModleInfo ->
                    sportModleInfo.let {
                        dataConverter.parseSportsDataGPS(it, colorFitDevice!!)
                    }
                        .let {
                            userActivityDataCallbacks?.onUserActivityDataReceived(
                                UserActivityCallback.SportsModeDataObtainedGPS(it)
                            )
                        }
                }
                LOGS.i(TAG, "Activity Sync:: success Size: ${sportModleInfoList.size}")

                sportModleInfoList.clear()
                isSyncProtoSportSyncing = false
                userActivityDataCallbacks?.onUserActivityDataReceived(UserActivityCallback.SportsModeDataSyncSuccess())
            }
        }

    }

    private fun deviceRequest(devSportRequest: SportRequestBean) {
        //{"sportType":1,"state":0,"supportVersions":0,"timestamp":1637229490}
        //GPS开启定位，进行预定位
        if (devSportRequest.state == 0) {
            // 回复设备SportResponseBean
            sendSportResponseBean()
        }
        when (devSportRequest.state) {
            1 -> {
                currentGpsSportState = 0

                isPause = false
                //未定位，开启定位
                /*if(!LocationService.binder.service.isLocationDoing){
                    LocationService.binder.service.startLocation()
                }*/
                enableLocation()

            }

            2 -> {
                currentGpsSportState = 1
                isPause = true
            }

            3 -> {
                currentGpsSportState = 2
                isPause = false
                //未定位，开启定位
                /*if(!LocationService.binder.service.isLocationDoing){
                    LocationService.binder.service.startLocation()
                }*/enableLocation()
            }

            4 -> {
                currentGpsSportState = -1

                isPause = false
                //不在app运动，关闭定位
                /*if(!LocationService.binder.service.isAppSport){
                    LocationService.binder.service.stopLocation()
                }*/disableLocation()
                /*mLastTime = 0L
                mLastLat = 0.0
                mLastLon = 0.0*/
            }
        }
    }

    private fun hasPermission(): Boolean {
        val permissionAccessCoarseLocationApproved =
            (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)

        val backgroundLocationPermissionApproved =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            } else {
                true
            }

        if (permissionAccessCoarseLocationApproved && backgroundLocationPermissionApproved) {
            return true
        }

        return false

    }

//    private fun getTempFromLatLng(lat: Double, log: Double) {
//
//        Timer("DelayConnection", false)
//            .schedule(5000) {
//                LOGS.d("getTemp $lat $log")
//                userActivityDataCallbacks?.onUserActivityDataReceived(
//                    UserActivityCallback.GetTempFromLatLog(
//                        lat, log
//                    )
//                )
//            }
//
//    }

    override fun setWeatherData(weatherDataList: List<WeatherData>) {
//        if (weatherDataList.isNotEmpty()) {
//            LOGS.d("setWeatherData ${weatherDataList}")
//            val weatherData = weatherDataList[0]
//            val weatherDataModel = WeatherDataModel()
//            weatherDataModel.temp = weatherData.temp
//            weatherDataModel.uvIndex = weatherData.uvi
//            weatherDataModel.humidity = weatherData.humidity
//            weatherDataModel.timeStamp = System.currentTimeMillis()
//            watchDataStore.saveWeatherDataModel(weatherDataModel)
//        }

    }

    private fun enableLocation() {
        locationClientClass = LocationClientClass()
        NoisefitApplication.context?.let {
            locationClientClass?.initialize(it)
            locationClientClass?.requestLocationUpdates(it)
            LocalBroadcastManager
                .getInstance(it)
                .registerReceiver(
                    locationReceiver,
                    IntentFilter(LOCATION_BROADCAST_RECEIVER)
                )
        }
        AppLogs.sendAppLogs(
            LogEvents.Connect,
            ConnectEvents.Other.apply { comment = "Location receiver register" })

    }

    private fun disableLocation() {
        NoisefitApplication.context?.let {
            locationClientClass?.removeLocationUpdates(it)
            LocalBroadcastManager
                .getInstance(it)
                .unregisterReceiver(locationReceiver)
        }
        AppLogs.sendAppLogs(
            LogEvents.Connect,
            ConnectEvents.Failed.apply { comment = "Location receiver disabled" })

    }

    /**
     * 回复设备当前状态
     */
    private fun sendSportResponseBean() {
        val response = SportResponseBean()
        response.code = getResponseCode()
        response.gpsAccuracy = 1
        ControlBleTools.getInstance()
            .replyDevSportRequest(response, object : SendCmdStateListener(null) {
                override fun onState(state: SendCmdState) {
                    LOGS.d(TAG, "$state")
                }
            })
        //开始辅助运动
        if (response.code == 0) {

            if (hasPermission()) {
                firstLocation = false
                currentGpsSportState = 0
                LOGS.d("GPS Started")
                enableLocation()
                sendErrorMessageToApp(
                    "Please make sure before starting a new run your phone must not be in low battery mode and battery optimization should be turn off for noiseFit app.",
                    "Alert"
                )
            } else {
                sendErrorMessageToApp(
                    "Please enable location permission from activity screen in app, before starting a new run.",
                    "Alert"
                )
                watchDataStore.setAskForPermission(true)

            }
        }
    }

    private fun sendErrorMessageToApp(message: String, title: String) {

        ControlBleTools.getInstance()
            .sendAppNotification(
                "noisefit",
                dataConverter.getPackageName("noisefit"),
                title,
                message,
                "t", null
            )

    }

    /**
     * 回复状态码
     * @return
     */
    private fun getResponseCode(): Int {
        //状态回应 0 OK; 1 设备正忙; 2 恢复/暂停类型不匹配; 3 没有位置权限;
        // 4 运动不支持; 5 精确gps关闭或后台无gps许可; 6 充电中; 7 低电量 ; 10 未知
        return if (currentGpsSportState == 0 || currentGpsSportState == 2) {
            2
        } else 0
        //        if (!PermissionUtils.isGranted(*PermissionUtils.PERMISSION_GROUP_LOCATION)) {
//            return 3
//        }
//        if(!AppUtils.isGPSOpen(BaseApplication.mContext)){
//            return 5
//        }
    }

    private var locationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (currentGpsSportState == 0 || currentGpsSportState == 2) {
                val locationArrayList =
                    intent.getParcelableArrayListExtra<LocationDataModel>(LAT_LONG)

                if (locationArrayList.isNullOrEmpty()) {
                    return
                }

                locationArrayList.forEach { location ->
                    mLatitude = location.latitude
                    mLongitude = location.longitude

//                    if (!firstLocation) {
//                        getTempFromLatLng(mLatitude, mLongitude)
//                        firstLocation = true
//                    }

                    var phoneSportDataBean: PhoneSportDataBean? = null

                    if (mLastTime == 0L || mLastLat == 0.0 || mLastLon == 0.0) {
                        //初次定位发送
                        phoneSportDataBean = PhoneSportDataBean()
                        phoneSportDataBean.gpsAccuracy = 1
                        phoneSportDataBean.timestamp = (System.currentTimeMillis() / 1000).toInt()
                        phoneSportDataBean.latitude = mLatitude
                        phoneSportDataBean.longitude = mLongitude
                    } else {
                        //5s | 定位有变化
                        if (System.currentTimeMillis() - mLastTime >= 5000 ||
                            mLatitude != mLastLat || mLongitude != mLastLon
                        ) {
                            phoneSportDataBean = PhoneSportDataBean()
                            phoneSportDataBean.gpsAccuracy = 1
                            phoneSportDataBean.timestamp =
                                (System.currentTimeMillis() / 1000).toInt()
                            phoneSportDataBean.latitude = mLatitude
                            phoneSportDataBean.longitude = mLongitude
                        }
                    }


                    if (phoneSportDataBean != null) {
                        mLastTime = System.currentTimeMillis()
                        mLastLat = mLatitude
                        mLastLon = mLongitude
                        ControlBleTools.getInstance().sendPhoneSportData(
                            phoneSportDataBean,
                            object : SendCmdStateListener(null) {
                                override fun onState(state: SendCmdState) {
                                    when (state) {
                                        SendCmdState.SUCCEED ->
                                            LOGS.d(TAG, "Location sent $mLatitude $mLongitude")

                                        else -> {
                                            LOGS.d(TAG, "Failed location setting")
                                        }
                                    }
                                }
                            })
                    }

                }


            }

        }
    }

    private val autoSportsCallback: AutoSportDataCallBack = AutoSportDataCallBack { p0 ->

        //[{"autoSportDuration":340,"autoSportIntensity":0,"autoSportKcal":5,"autoSportStartTime":1690863212,"autoSportSteps":601,"autoSportType":1,"hrData":[]}]

//        val dataList = ArrayList<OreoAutoSportData>()
//        val timestamp = 1690863212 * 1000L
//        dataList.add(OreoAutoSportData(0,false,false,3400,0,5,timestamp,601,"running",null))
//        dataList.add(OreoAutoSportData(0,false,false,1400,1,15,1690692397000,1201,"walking",null))
//        dataList.add(OreoAutoSportData(0,false,false,1000,0,25,1690778797000,1901,"other",null))
//        dataList.add(OreoAutoSportData(0,false,false,1400,1,5,1690778797000,61,"running",null))
//        dataList.add(OreoAutoSportData(0,false,false,1300,0,120,1690865197000,600,"running",null))
//        dataList.add(OreoAutoSportData(0,false,false,1800,2,500,1690958797000,6010,"running",null))
//
//        LOGS.d(TAG, "onAutoSportData ${Gson().toJson(p0)}")

//        userActivityDataCallbacks?.onUserActivityDataReceived(
//            UserActivityCallback.AutoSportDataObtained(
//                dataList
//            )
//        )
//        AppLogs.sendAppLogs("onAutoSportData Sync data complete ${Gson().toJson(p0)}")
//        colorFitDevice?.let {
//            userActivityDataCallbacks?.onUserActivityDataReceived(
//                UserActivityCallback.AutoSportDataObtained(
//                    dataConverter.parseAutoSport(
//                        p0,
//                        it
//                    )
//                )
//            )
//        }
    }


    private val realDataCallback = object : RealTimeDataCallBack {
        override fun onResult(p0: RealTimeBean?) {
            if (!colorFitDevice?.deviceType.equals(DeviceType.NOISEFIT_LUNA.deviceType, true)) {
                p0?.let {
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.RealStepsDataObtained(
                            dataConverter.parseStepsData(
                                p0
                            )
                        )
                    )
                }
            }

        }

        override fun onFail() {

        }

    }


    //日常数据回调
    private val fitnessDataCallBack: FitnessDataCallBack =
        object : FitnessDataCallBack {

            override fun onProgress(progress: Int, total: Int) {
                LOGS.d(TAG, "onProgress : 进度 $progress  总数 $total")
                AppLogs.sendAppLogs("$TRACK_TAG on progress $progress  总数 $total")
                when (progress) {
                    0 -> {
                        userActivityDataCallbacks?.onUserActivityDataReceived(
                            UserActivityCallback.UserDataSyncUpdated(
                                SyncEvents.Started(progress, total)
                            )
                        )
                        AppLogs.sendAppLogs("$TRACK_TAG Sync data start")

                    }

                    total -> {
                        userActivityDataCallbacks?.onUserActivityDataReceived(
                            UserActivityCallback.UserDataSyncUpdated(
                                SyncEvents.Success(progress, total)
                            )
                        )
                        AppLogs.sendAppLogs("$TRACK_TAG Sync data complete")
                    }

                    else -> {
                        userActivityDataCallbacks?.onUserActivityDataReceived(
                            UserActivityCallback.UserDataSyncUpdated(
                                SyncEvents.InProgress(progress, total)
                            )
                        )
                    }
                }
            }

            override fun onDailyData(data: DailyBean) {
                LOGS.d(TAG, "onDailyData : $data ${data.date}")


                if (colorFitDevice?.deviceType.equals(DeviceType.NOISEFIT_LUNA.deviceType, true)) {
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.StepsDataObtainedOreo(
                            oreoDataConverter.parseStepsData(
                                data
                            )
                        )
                    )
                } else {
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.StepsDataObtained(
                            dataConverter.parseStepsData(
                                data
                            )
                        )
                    )
                }


                AppLogs.sendAppLogs("$TRACK_TAG onDailyData : $data ${data.date}")
                AppLogs.sendAppLogs("sent  data start")
            }

            override fun onSleepData(data: SleepBean) {
                LOGS.d(TAG, "onSleepData : $data ${data.date}")
                AppLogs.sendAppLogs("$TRACK_TAG onSleepData $data ${data.date}")


                val sleepDataParsed = dataConverter.parseSleepData(data)
                AppLogs.sendAppLogs("$TRACK_TAG Parsed Sleep Data $sleepDataParsed")
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.SleepDataObtained(
                        sleepDataParsed
                    )
                )
            }

            override fun onContinuousHeartRateData(data: ContinuousHeartRateBean) {
                LOGS.d(TAG, "onContinuousHeartRateData : $data ${data.date}")

                if (colorFitDevice?.deviceType.equals(DeviceType.NOISEFIT_LUNA.deviceType, true)) {
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.HeartHistoryObtainedOreo(
                            oreoDataConverter.parseHeartRateData(data)
                        )
                    )
                } else {
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.HeartHistoryObtained(
                            dataConverter.parseHeartRateData(data)
                        )
                    )
                }

                AppLogs.sendAppLogs("$TRACK_TAG onContinuousHeartRateData : $data ${data.date}")
                AppLogs.sendAppLogs("Sent heart rate data")
            }

            override fun onOfflineHeartRateData(data: OfflineHeartRateBean) {
                LOGS.d(TAG, "onOfflineHeartRateData : $data ${data.date}")

            }

            override fun onContinuousBloodOxygenData(data: ContinuousBloodOxygenBean) {
                LOGS.d(TAG, "onContinuousBloodOxygenData : $data ${data.date}")
                AppLogs.sendAppLogs("$TRACK_TAG onContinuousBloodOxygenData : $data ${data.date}")
                if (colorFitDevice?.deviceType.equals(DeviceType.NOISEFIT_LUNA.deviceType, true)) {
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.OreoBloodOxygenObtained(
                            oreoDataConverter.parseContinuousBloodOxygenData(data)
                        )
                    )
                }
            }

            override fun onOfflineBloodOxygenData(data: OfflineBloodOxygenBean) {
                LOGS.d(TAG, "onOfflineBloodOxygenData : $data ${data.date}")
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.BloodOxygenObtained(
                        dataConverter.parseBloodOxygenData(data)
                    )
                )
                AppLogs.sendAppLogs("$TRACK_TAG onOfflineBloodOxygenData : $data ${data.date}")
                AppLogs.sendAppLogs("Sent Offline Blood Oxygen Data")
            }

            override fun onContinuousPressureData(data: ContinuousPressureBean) {
                LOGS.d(TAG, "onOfflinePressureData : $data ${data.date}")
                if (colorFitDevice?.deviceType.equals(DeviceType.NOISEFIT_LUNA.deviceType, true)) {
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.StressDataObtainedOreo(
                            oreoDataConverter.parseStressData(data)
                        )
                    )
                } else {
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.StressDataObtained(
                            dataConverter.parseStressData(data)
                        )
                    )
                }
                AppLogs.sendAppLogs("$TRACK_TAG onOfflinePressureData : $data ${data.date}")
                AppLogs.sendAppLogs("Sent Offline Pressure Data")
            }

            override fun onOfflinePressureData(data: OfflinePressureDataBean) {
                LOGS.d(TAG, "onOfflinePressureData : $data ${data.date}")
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.StressDataObtained(
                        dataConverter.parseStressData(data)
                    )
                )
                AppLogs.sendAppLogs("$TRACK_TAG onOfflinePressureData : $data ${data.date}")
                AppLogs.sendAppLogs("Sent Offline Pressure Data")
            }

            override fun onContinuousTemperatureData(data: ContinuousTemperatureBean) {
                LOGS.d(TAG, "onContinuousTemperatureData : $data")
                if (colorFitDevice?.deviceType.equals(DeviceType.NOISEFIT_LUNA.deviceType, true)) {
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.BodyTemperatureObtainedOreo(
                            oreoDataConverter.parseBodyTemperature(data)
                        )
                    )
                }
                AppLogs.sendAppLogs("$TRACK_TAG onContinuousTemperatureData : $data ${data.date}")

            }

            override fun onOfflineTemperatureData(data: OfflineTemperatureDataBean) {
                LOGS.d(TAG, "onOfflineTemperatureData : $data ${data.date}")

                if (colorFitDevice?.deviceType.equals(DeviceType.NOISEFIT_LUNA.deviceType, true)) {
                    //oreo data in onContinuousTemperatureData
                } else {
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.BodyTemperatureObtained(
                            dataConverter.parseBodyTemperature(data)
                        )
                    )
                }





                AppLogs.sendAppLogs("$TRACK_TAG onOfflineTemperatureData : $data ${data.date}")
                AppLogs.sendAppLogs("Sent Offline Temperature Data")
            }

            override fun onEffectiveStandingData(data: EffectiveStandingBean) {
                LOGS.d(TAG, "onEffectiveStandingData : $data ${data.date}")

            }

            override fun onActivityDurationData(data: ActivityDurationBean) {

            }

            override fun onOffEcgData(p0: OffEcgDataBean?) {

            }

            override fun onExaminationData(p0: ExaminationBean?) {

            }

            override fun onRingTodayActiveTypeData(p0: TodayActiveTypeData?) {
                LOGS.d(TAG, "onRingTodayActiveTypeData : $p0")
                AppLogs.sendAppLogs("$TRACK_TAG onRingTodayActiveTypeData : $p0")
            }

            override fun onRingOverallDayMovementData(p0: OverallDayMovementData?) {
                LOGS.d(TAG, "onRingOverallDayMovementData : $p0")
                AppLogs.sendAppLogs("$TRACK_TAG onRingOverallDayMovementData : $p0")
                if (p0 == null) return

                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.OreoRingDayTimeMovementObtained(
                        oreoDataConverter.parseDayTimeMovementData(p0)
                    )
                )


            }

            override fun onRingTodayRespiratoryRateData(p0: TodayRespiratoryRateData?) {
                LOGS.d(TAG, "onRingTodayRespiratoryRateData : $p0")
                AppLogs.sendAppLogs("$TRACK_TAG onRingTodayRespiratoryRateData : $p0")
                if (p0 == null) return

                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.OreoRespiratoryDataObtained(
                        oreoDataConverter.parseRespiratoryData(p0)
                    )
                )
            }

            override fun onRingHealthScore(p0: RingHealthScoreBean?) {
                LOGS.d(TAG, "onRingHealthScore : $p0")
                AppLogs.sendAppLogs("$TRACK_TAG onRingHealthScore : $p0")
                if (p0 == null) return

                val startDayTimeStamp =
                    DateFormats.convertDateTimeToTimeStamp(p0.date, DateFormats.dateTimeFormat5)

                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.HealthScoreObtainedOreo(
                        p0.healthScore, DateFormats.dateFormat3.format(startDayTimeStamp)
                    )
                )
            }

            override fun onRingSleepResult(p0: RingSleepResultBean?) {
                LOGS.d(TAG, "onRingSleepResult : $p0")
                AppLogs.sendAppLogs("$TRACK_TAG onRingSleepResult : $p0")
                if (p0 == null) return
                /*val testDataString =
                    "{\"isExistSleep\":true, \"entryTime\":1690481040, \"exitTime\":1690499700, \"sleepDuration\":14730, \"timeInBedTime\":18660, \"sleepLatency\":9, \"sleepEfficiency\":78, \"sleepScore\":67, \"awakeTime\":3930, \"lightSleepTime\":4200, \"deepSleepTime\":10530, \"rapidEyeMovementTime\":0, \"sleepDistributionData\":[{\"startTimestamp\":1690481040, \"sleepDuration\":570, \"sleepDistributionType\":0}, {\"startTimestamp\":1690481610, \"sleepDuration\":1050, \"sleepDistributionType\":1}, {\"startTimestamp\":1690482660, \"sleepDuration\":2520, \"sleepDistributionType\":2}, {\"startTimestamp\":1690485180, \"sleepDuration\":690, \"sleepDistributionType\":0}, {\"startTimestamp\":1690485870, \"sleepDuration\":1050, \"sleepDistributionType\":1}, {\"startTimestamp\":1690486920, \"sleepDuration\":3000, \"sleepDistributionType\":2}, {\"startTimestamp\":1690489920, \"sleepDuration\":2070, \"sleepDistributionType\":0}, {\"startTimestamp\":1690491990, \"sleepDuration\":1050, \"sleepDistributionType\":1}, {\"startTimestamp\":1690493040, \"sleepDuration\":1560, \"sleepDistributionType\":2}, {\"startTimestamp\":1690494600, \"sleepDuration\":570, \"sleepDistributionType\":0}, {\"startTimestamp\":1690495170, \"sleepDuration\":1050, \"sleepDistributionType\":1}, {\"startTimestamp\":1690496220, \"sleepDuration\":3450, \"sleepDistributionType\":2}, {\"startTimestamp\":1690499670, \"sleepDuration\":30, \"sleepDistributionType\":0}], \"sleepMovementsData\":[{\"startTimestamp\":1690481040, \"sleepDuration\":60, \"sleepMovementsType\":0}, {\"startTimestamp\":1690481100, \"sleepDuration\":30, \"sleepMovementsType\":1}, {\"startTimestamp\":1690481130, \"sleepDuration\":570, \"sleepMovementsType\":0}, {\"startTimestamp\":1690481700, \"sleepDuration\":60, \"sleepMovementsType\":1}, {\"startTimestamp\":1690481760, \"sleepDuration\":720, \"sleepMovementsType\":0}, {\"startTimestamp\":1690482480, \"sleepDuration\":60, \"sleepMovementsType\":1}, {\"startTimestamp\":1690482540, \"sleepDuration\":90, \"sleepMovementsType\":0}, {\"startTimestamp\":1690482630, \"sleepDuration\":30, \"sleepMovementsType\":1}, {\"startTimestamp\":1690482660, \"sleepDuration\":120, \"sleepMovementsType\":0}, {\"startTimestamp\":1690482780, \"sleepDuration\":30, \"sleepMovementsType\":1}, {\"startTimestamp\":1690482810, \"sleepDuration\":90, \"sleepMovementsType\":0}, {\"startTimestamp\":1690482900, \"sleepDuration\":30, \"sleepMovementsType\":1}, {\"startTimestamp\":1690482930, \"sleepDuration\":210, \"sleepMovementsType\":0}, {\"startTimestamp\":1690483140, \"sleepDuration\":60, \"sleepMovementsType\":1}, {\"startTimestamp\":1690483200, \"sleepDuration\":90, \"sleepMovementsType\":0}, {\"startTimestamp\":1690483290, \"sleepDuration\":30, \"sleepMovementsType\":1}, {\"startTimestamp\":1690483320, \"sleepDuration\":240, \"sleepMovementsType\":0}, {\"startTimestamp\":1690483560, \"sleepDuration\":30, \"sleepMovementsType\":1}, {\"startTimestamp\":1690483590, \"sleepDuration\":780, \"sleepMovementsType\":0}, {\"startTimestamp\":1690484370, \"sleepDuration\":30, \"sleepMovementsType\":1}, {\"startTimestamp\":1690484400, \"sleepDuration\":810, \"sleepMovementsType\":0}, {\"startTimestamp\":1690485210, \"sleepDuration\":60, \"sleepMovementsType\":1}, {\"startTimestamp\":1690485270, \"sleepDuration\":60, \"sleepMovementsType\":0}, {\"startTimestamp\":1690485330, \"sleepDuration\":60, \"sleepMovementsType\":1}, {\"startTimestamp\":1690485390, \"sleepDuration\":30, \"sleepMovementsType\":0}, {\"startTimestamp\":1690485420, \"sleepDuration\":60, \"sleepMovementsType\":1}, {\"startTimestamp\":1690485480, \"sleepDuration\":360, \"sleepMovementsType\":0}, {\"startTimestamp\":1690485840, \"sleepDuration\":30, \"sleepMovementsType\":1}, {\"startTimestamp\":1690485870, \"sleepDuration\":300, \"sleepMovementsType\":0}, {\"startTimestamp\":1690486170, \"sleepDuration\":30, \"sleepMovementsType\":1}, {\"startTimestamp\":1690486200, \"sleepDuration\":120, \"sleepMovementsType\":0}, {\"startTimestamp\":1690486320, \"sleepDuration\":60, \"sleepMovementsType\":1}, {\"startTimestamp\":1690486380, \"sleepDuration\":30, \"sleepMovementsType\":0}, {\"startTimestamp\":1690486410, \"sleepDuration\":30, \"sleepMovementsType\":1}, {\"startTimestamp\":1690486440, \"sleepDuration\":300, \"sleepMovementsType\":0}, {\"startTimestamp\":1690486740, \"sleepDuration\":30, \"sleepMovementsType\":1}, {\"startTimestamp\":1690486770, \"sleepDuration\":120, \"sleepMovementsType\":0}, {\"startTimestamp\":1690486890, \"sleepDuration\":30, \"sleepMovementsType\":1}, {\"startTimestamp\":1690486920, \"sleepDuration\":30, \"sleepMovementsType\":3}, {\"startTimestamp\":1690486950, \"sleepDuration\":600, \"sleepMovementsType\":0}, {\"startTimestamp\":1690487550, \"sleepDuration\":60, \"sleepMovementsType\":1}, {\"startTimestamp\":1690487610, \"sleepDuration\":240, \"sleepMovementsType\":0}, {\"startTimestamp\":1690487850, \"sleepDuration\":30, \"sleepMovementsType\":1}, {\"startTimestamp\":1690487880, \"sleepDuration\":810, \"sleepMovementsType\":0}, {\"startTimestamp\":1690488690, \"sleepDuration\":60, \"sleepMovementsType\":1}, {\"startTimestamp\":1690488750, \"sleepDuration\":210, \"sleepMovementsType\":0}, {\"startTimestamp\":1690488960, \"sleepDuration\":30, \"sleepMovementsType\":1}, {\"startTimestamp\":1690488990, \"sleepDuration\":330, \"sleepMovementsType\":0}, {\"startTimestamp\":1690489320, \"sleepDuration\":30, \"sleepMovementsType\":1}, {\"startTimestamp\":1690489350, \"sleepDuration\":30, \"sleepMovementsType\":2}, {\"startTimestamp\":1690489380, \"sleepDuration\":30, \"sleepMovementsType\":0}, {\"startTimestamp\":1690489410, \"sleepDuration\":30, \"sleepMovementsType\":1}, {\"startTimestamp\":1690489440, \"sleepDuration\":180, \"sleepMovementsType\":0}, {\"startTimestamp\":1690489620, \"sleepDuration\":120, \"sleepMovementsType\":1}, {\"startTimestamp\":1690489740, \"sleepDuration\":90, \"sleepMovementsType\":0}, {\"startTimestamp\":1690489830, \"sleepDuration\":30, \"sleepMovementsType\":2}, {\"startTimestamp\":1690489860, \"sleepDuration\":510, \"sleepMovementsType\":0}]}"

                val testDataSleep = Gson().fromJson<RingSleepResultBean>(
                    testDataString
                )*/

                val sleepDataParsed = oreoDataConverter.parseSleepData(p0)
                AppLogs.sendAppLogs("$TRACK_TAG Parsed Sleep Data $sleepDataParsed")
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.SleepDataObtainedOreo(
                        sleepDataParsed
                    )
                )
            }

            override fun onRingSleepNAP(p0: MutableList<RingSleepNapBean>?) {
                LOGS.d(TAG, "onRingSleepNAP : $p0")
                AppLogs.sendAppLogs("$TRACK_TAG onRingSleepNAP : $p0")
            }


        }


    override fun syncUserActivity(date: String, isRefresh: Boolean) {
        try {

            ControlBleTools.getInstance().getDailyHistoryData(null)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun syncSportsActivity(date: String) {
        if (!isSyncProtoSportSyncing) {
            isSyncProtoSportSyncing = true
            sportModleInfoList.clear()
            //ControlBleTools.getInstance().getSportStatus(null)
            LOGS.d(TAG, "syncSportsActivity")
            ControlBleTools.getInstance().getFitnessSportIdsData(null)
            ControlBleTools.getInstance().getAutoSportData(null)
            AppLogs.sendAppLogs("SyncSportActivity")
        }

    }

    override fun getStepsData(date: String) {
    }

    override fun getSleepData(date: String) {
    }

    override fun getHeartRate() {
    }

    override fun getBloodOxygenLevel() {
    }

    override fun getBloodPressure() {
    }

    override fun getStressCount() {
    }

    override fun getBodyTemperatureData() {
    }

    override fun setDevice(colorFitDevice: ColorFitDevice) {
        this.colorFitDevice = colorFitDevice
    }


    override fun init() {
        super.init()
    }
}
