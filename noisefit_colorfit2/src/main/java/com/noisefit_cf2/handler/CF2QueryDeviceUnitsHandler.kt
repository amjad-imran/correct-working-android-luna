package com.noisefit_cf2.handler

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import com.ido.ble.BLEManager
import com.ido.ble.LocalDataManager
import com.ido.ble.callback.*
import com.ido.ble.callback.SyncCallBack.IConfigCallBack
import com.ido.ble.protocol.model.*
import com.noisefit_cf2.base.CF2Globals
import com.noisefit_cf2.dataconversions.Colorfit2DataConverter
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.constants.EventConstants
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.handler.MusicControlActionsEvents

import com.noisefit_commans.interfaces.IQueryDataCallback
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.models.*
import com.noisefit_commans.models.HeartRateInterval
import com.noisefit_commans.network.CF2NetworkCalls
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.PhoneRinger
import javax.inject.Inject


class CF2QueryDeviceUnitsHandler @Inject constructor(
    var watchDataStore: WatchDataStore
) : QueryDeviceDataActions() {


    private var testQueryDeviceDataCallback: IQueryDataCallback? = null

    private var previousVolume = 0
    private var info: BasicInfo? = null

    private var colorFitDevice: ColorFitDevice? = null

    override fun <T> callbackListener(callback: T) {}

    override fun <T> callbackListenerNew(callback: T) {
        testQueryDeviceDataCallback = callback as IQueryDataCallback
    }

    override fun setDevice(colorFitDevice: ColorFitDevice) {
        this.colorFitDevice = colorFitDevice
    }

    override fun setMusicStatus(status: Int, title: String?, sec: Int?) {
        if (title != null) {
            val info = MusicControlInfo()

            info.musicName = title
            if (sec != null) {
                info.totalTimeSecond = sec
            }
            if (status == 3) {
                info.status = MusicControlInfo.STATUS_STOP
            } else if (status == 2) {
                info.status = MusicControlInfo.STATUS_PAUSE
            } else if (status == 1) {
                info.status = MusicControlInfo.STATUS_PLAY
            }
            BLEManager.setMusicControlInfo(info)
        }
    }

    override fun init() {
        removeCallbacks()
        attachCallbacks()
    }

    override fun attachCallbacks() {
        removeCallbacks()
        BLEManager.registerGetDeviceInfoCallBack(callBack)
        BLEManager.registerSyncConfigCallBack(configCallBack)
        BLEManager.registerPhoneMsgNoticeCallBack(phoneMsgNoticeCallBack)
        BLEManager.registerDeviceControlAppCallBack(getDeviceControlAppCallback())
        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType ||
                deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType ||
                deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType ||
                deviceType == DeviceType.NOISEFIT_AGILE.deviceType
            ) {
                BLEManager.registerGetDeviceParaCallBack(appDeviceParaCallBack)
            }
        }
    }

    override fun removeCallbacks() {
        BLEManager.unregisterGetDeviceInfoCallBack(callBack)
        BLEManager.unregisterSyncConfigCallBack(configCallBack)
        BLEManager.unregisterDeviceControlAppCallBack(getDeviceControlAppCallback())
        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType || deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType
                || deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType || deviceType == DeviceType.NOISEFIT_AGILE.deviceType
            ) {
                BLEManager.unregisterGetDeviceParaCallBack(appDeviceParaCallBack)
            }
        }
    }

    override fun queryFirmwareVersion() {}


    override fun queryBatteryPower() {
        BLEManager.getBasicInfo()
    }

    override fun getBrightnessLevel() {
        //BLEManager.getScreenBrightness()
        val brightnessLevel = watchDataStore.getScreenBrightness()

        try {
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.BrightnessLevelObtained(
                    brightnessLevel / 20
                )
            )
        } catch (exp: Exception) {
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.BrightnessLevelObtained(
                    1
                )
            )
        }

    }

    override fun getUserInfo() {
        val info = LocalDataManager.getUserInfo()
        if (info != null) {
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.UserInfoReceived(
                    Colorfit2DataConverter.parseUserInfo(info)
                )
            )
        }
    }

    override fun getFindPhoneSwitch() {
        testQueryDeviceDataCallback?.onQueryDataReceived(
            QueryCallback.FindMyPhoneSwitchObtained(
                SwitchSetting(LocalDataManager.getFindPhoneSwitch())
            )
        )
    }

    override fun getLanguage() {
        val units = LocalDataManager.getUnits()
        units?.let {
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.LanguageReceived(
                    Colorfit2DataConverter.parseLanguage(it)
                )
            )
        }
    }

    override fun queryFirmwareUpgrade() {
        /*val response = FirmwareUpgradeHandler.checkForNewVersion()
        var deviceFirmware = DeviceFirmware(status = "update_not_available")
        response?.data?.let {
            when (response.resultCode) {
                1 -> {
                    deviceFirmware = when (response.data?.forceUpdate ?: false) {
                        true -> {
                            DeviceFirmware(status = "forced_update_available")
                        }
                        else -> {
                            DeviceFirmware(status = "update_available")
                        }
                    }
                    deviceFirmware.message = response.message
                    deviceFirmware.version = response.data?.version.toString()
                    CF2Globals.firmwareUrl = response.data?.url
                }
                else -> {
                    deviceFirmware = DeviceFirmware(status = "update_not_available")
                }
            }
        }
        testQueryDeviceDataCallback?.onQueryDataReceived(QueryCallback.FirmwareUpgradeAvailable(
            deviceFirmware
        ))*/
    }

    override fun queryFirmwareUpgradeNew(colorFitDevice: ColorFitDevice) {
        /* val response = FirmwareUpgradeHandler.checkForNewVersion()
         var deviceFirmware = DeviceFirmware(status = "update_not_available")
         response?.data?.let {
             when (response.resultCode) {
                 1 -> {
                     deviceFirmware = when (response.data?.forceUpdate ?: false) {
                         true -> {
                             DeviceFirmware(status = "forced_update_available")
                         }
                         else -> {
                             DeviceFirmware(status = "update_available")
                         }
                     }
                     deviceFirmware.message = response.message
                     deviceFirmware.version = response.data?.version.toString()
                     CF2Globals.firmwareUrl = response.data?.url
                 }
                 else -> {
                     deviceFirmware = DeviceFirmware(status = "update_not_available")
                 }
             }
         }
         testQueryDeviceDataCallback?.onQueryDataReceived(QueryCallback.FirmwareUpgradeAvailableNew(
             deviceFirmware
         ))*/
    }

    override fun getWatchFaces() {

        val currentDialPlate = LocalDataManager.getDialPlate()?.dial_id?.toString() ?: ""
        //BLEManager.getCurrentWatchPlate()

        AppLogs.sendAppLogs("CF2 : getWatchFaces() Device ID : ${CF2Globals.basicInfo?.deivceId}")
        colorFitDevice?.deviceType?.let { deviceType ->
            LOGS.d(
                "CF2",
                "getWatchFaces " + CF2Globals.basicInfo?.deivceId + ":" + CF2Globals.isCloudDialSupport!! + ":" + CF2Globals.isCustomDialSupport!!
            )

            CF2Globals.basicInfo?.deivceId?.let {
                getCloudWatchFaces(it)

/*                if (it == 7040 || it == 7154 || it == 7193 || it == 7209 || it == 7252 || it == 7324 || it == 7286 ||
                    it == 7287 || it == 7365 || it == 7409 || it == 7072 || it == 7454 || it == 7239)
                    getCloudWatchFaces(it)*/
            }


        }
    }


    private fun getCloudWatchFaces(deviceId: Int) {
        AppLogs.sendAppLogs("CF2 : getCloudWatchFaces() Device ID : $deviceId")
        val thread = Thread {
            try {
                val faces = CF2NetworkCalls.getWatchFaces(deviceId)
                val listWatchFaces = ArrayList<WatchFace>()
                faces?.data?.forEach { item ->
                    val watchFace = WatchFace(
                        id = item.id,
                        faceId = item.id.toString(),
                        imageUrl = item.image,
                        imageType = "cloud_supplier"
                    )
                    listWatchFaces.add(watchFace)
                }
                Handler(Looper.getMainLooper()).post {
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.WatchFacesObtained(
                            listWatchFaces
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread.start()


    }

    override fun getAlarms() {
        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType || deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType
                || deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType || deviceType == DeviceType.NOISEFIT_AGILE.deviceType
            ) {
                BLEManager.getAlarmV3()
            } else {
                val alarms = Colorfit2DataConverter.parseAlarms(LocalDataManager.getAlarm())
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.AlarmsObtained(
                        alarms
                    )
                )
            }
        }
    }

    override fun getDoNotDisturbData() {
        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType || deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType
                || deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType || deviceType == DeviceType.NOISEFIT_AGILE.deviceType
            ) {
                BLEManager.getDoNotDisturbPara()
            } else {
                if (LocalDataManager.getNotDisturbPara() != null) {
                    val doNotDisturb =
                        Colorfit2DataConverter.parseDoNotDisturb(LocalDataManager.getNotDisturbPara())

                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.DoNotDisturbObtained(
                            doNotDisturb
                        )
                    )
                } else {
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.DoNotDisturbObtained(
                            DoNotDisturb()
                        )
                    )
                }
            }
        }

    }

    override fun getMenstrualSettings() {
        val menstrualData = Colorfit2DataConverter.parseMenstrualData(
            LocalDataManager.getMenstrual(),
            LocalDataManager.getMenstrualRemind()
        )

        testQueryDeviceDataCallback?.onQueryDataReceived(
            QueryCallback.MenstrualSettingsObtained(
                menstrualData
            )
        )
    }

    override fun getHeartRateInterval() {
        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType) {
                BLEManager.registerSettingCallBack(settingsCallBack)
                val heartRateMeasureModeV3 = HeartRateMeasureModeV3()
                heartRateMeasureModeV3.updateTime = 0
                BLEManager.setHeartRateMeasureModeV3(heartRateMeasureModeV3)

            } else if (deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType
                || deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType
                || deviceType == DeviceType.NOISEFIT_AGILE.deviceType
            ) {
                if (LocalDataManager.getHeartRateModeV3() != null) {

                    val heartRateInterval =
                        Colorfit2DataConverter.formatHeartRateIntervalActiveGet(LocalDataManager.getHeartRateModeV3())

                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.HeartRateIntervalObtained(
                            heartRateInterval
                        )
                    )
                } else {
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.HeartRateIntervalObtained(
                            HeartRateInterval(
                                status = false
                            )
                        )
                    )
                }
            } else {

                if (LocalDataManager.getHeartRateMode() != null) {
                    val heartRateInterval =
                        Colorfit2DataConverter.formatHeartRateInterval(LocalDataManager.getHeartRateMode())

                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.HeartRateIntervalObtained(
                            heartRateInterval
                        )
                    )
                } else {
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.HeartRateIntervalObtained(
                            HeartRateInterval(
                                status = false
                            )
                        )
                    )
                }
            }
        }

    }

    private val settingsCallBack: SettingCallBack.ICallBack = object : SettingCallBack.ICallBack {

        override fun onSuccess(p0: SettingCallBack.SettingType?, p1: Any?) {

            if (p1 != null) {
                colorFitDevice?.deviceType?.let { deviceType ->
                    if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType || deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType) {
                        val heartRateInterval =
                            Colorfit2DataConverter.formatHeartRateIntervalV3(p1 as HeartRateMeasureModeV3)

                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.HeartRateIntervalObtained(
                                heartRateInterval
                            )
                        )
                    } else {

                        val heartRateInterval =
                            Colorfit2DataConverter.formatHeartRateIntervalActiveGet(p1 as HeartRateMeasureModeV3)

                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.HeartRateIntervalObtained(
                                heartRateInterval
                            )
                        )
                    }
                }
            }
        }

        override fun onFailed(type: SettingCallBack.SettingType) {

        }
    }

    override fun getSedentaryData() {
        if (LocalDataManager.getLongSit() != null) {
            val longSit = Colorfit2DataConverter.parseSedentaryData(LocalDataManager.getLongSit())
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.SedentaryDataObtained(
                    longSit
                )
            )
        } else {
            val idleAlert = SedentaryData(
                status = false,
                interval = 1,
                startHour = 10,
                startMinute = 0,
                endHour = 22,
                endMinute = 0
            )
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.SedentaryDataObtained(
                    idleAlert
                )
            )
        }

    }

    override fun getWalkReminderData() {
        LOGS.d("walkReminder inside")
        if (LocalDataManager.getWalkReminder() != null) {
            val walkData =
                Colorfit2DataConverter.parseWalkReminderData(LocalDataManager.getWalkReminder())
            LOGS.d("walkReminder ${walkData}")
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.WalkReminderDataObtained(
                    walkData
                )
            )
        } else {
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.WalkReminderDataObtained(
                    WalkReminderData(
                        status = false,
                        startHour = 10,
                        startMinute = 0,
                        endHour = 22,
                        endMinute = 0,
                        goalSteps = 50,
                        repeat = 255,
                        weeks = null
                    )

                )
            )

        }
    }

    override fun getDrinkWaterSettings() {

        if (LocalDataManager.getDrinkWaterReminder() != null && LocalDataManager.getDrinkWaterReminder()?.interval != 0) {
            val drink =
                Colorfit2DataConverter.parseDrinkWaterData(LocalDataManager.getDrinkWaterReminder())

            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.DrinkWaterDataObtained(
                    drink
                )
            )
        } else {

            var defaultInterval = 60
            when(colorFitDevice?.deviceType){
                DeviceType.NOISEFIT_AGILE_OTA.deviceType,
                DeviceType.NOISEFIT_AGILE.deviceType,
                DeviceType.NOISEFIT_AGILE_DFU.deviceType->{
                    defaultInterval = 1
                }
            }

            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.DrinkWaterDataObtained(
                    SedentaryData(
                        false,
                        defaultInterval,
                        10,
                        0,
                        22,
                        0,
                        0,
                        null,
                        arrayListOf(true, true, true, true, true, true, true)
                    )

                )
            )
        }
    }

    override fun getStressSettings() {
        if (LocalDataManager.getPressureParam() != null) {
            val stress =
                Colorfit2DataConverter.parseStressParam(LocalDataManager.getPressureParam())
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.StressParamObtained(
                    stress
                )
            )
        } else {
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.StressParamObtained(
                    SedentaryData()
                )
            )
        }
    }

    override fun getMusicControlSettings() {
        testQueryDeviceDataCallback?.onQueryDataReceived(
            QueryCallback.MusicControl(
                SwitchSetting(status = LocalDataManager.getMusicSwitch())
            )
        )
    }

    override fun getActivityRecogniseSettings() {
        if (LocalDataManager.getActivitySwitch() != null && LocalDataManager.getActivitySwitch().autoIdentifySportWalk != null) {
            val value1 = LocalDataManager.getActivitySwitch().autoIdentifySportWalk
            val value2 = LocalDataManager.getActivitySwitch().autoIdentifySportRun
            val value3 = LocalDataManager.getActivitySwitch().autoIdentifySportBicycle
            LOGS.d("noise_fit_event:colorfit_pro_2 getActivitySwitch | " + value1)

            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.ActivityRecognise(
                    SwitchSetting(
                        status = when (value1) {
                            1 -> true
                            else -> false
                        }, walk_status = when (value1) {
                            1 -> true
                            else -> false
                        }, run_status = when (value2) {
                            1 -> true
                            else -> false
                        }, cycle_status = when (value3) {
                            1 -> true
                            else -> false
                        }
                    )
                )
            )
        }
    }

    override fun getDeviceUnits() {
        val units = LocalDataManager.getUnits()
        testQueryDeviceDataCallback?.onQueryDataReceived(
            QueryCallback.DeviceUnitsObtained(
                Colorfit2DataConverter.parseDeviceUnits(
                    units
                )
            )
        )
    }

    override fun getUserGoals() {
        val goals = LocalDataManager.getGoal()
        testQueryDeviceDataCallback?.onQueryDataReceived(
            QueryCallback.UserGoalsObtained(
                UserGoals(goals.sport_step)
            )
        )
    }

    override fun getWeatherSwitchStatus() {
        testQueryDeviceDataCallback?.onQueryDataReceived(
            QueryCallback.WeatherSwitchStatus(
                SwitchSetting(LocalDataManager.getWeatherSwitch())
            )
        )
    }


    override fun getWristLiftGesture() {
        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType || deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType
                || deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType || deviceType == DeviceType.NOISEFIT_AGILE.deviceType
            ) {
                BLEManager.getUpHandGesture()
                //setOutdoorActivitiesPro3()
            } else {
                if (LocalDataManager.getUpHandGesture() != null) {
                    val gesture = LocalDataManager.getUpHandGesture()

                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.WristLiftGestureObtained(
                            Colorfit2DataConverter.parseWristSenseData(
                                gesture
                            )
                        )
                    )
                } else {
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.WristLiftGestureObtained(
                            WristLiftGesture(status = false)
                        )
                    )
                }
            }
        }
    }

    override fun getHandwashData() {
        val handWashData = watchDataStore.getHandWashData()
        if (handWashData == null) {
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.GetHandWashing(HandWashing(8, 0, 18, 0, 60, 5, false))
            )
        } else {
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.GetHandWashing(handWashData)
            )
        }

    }

    override fun getHeartRateAlert() {
        getHeartRateInterval()
    }

    override fun syncDeviceUnits() {
        val userInfo = LocalDataManager.getUserInfo()
        if (userInfo == null) {
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.Error(
                    ColorfitError(
                        message = "User info not available",
                        code = "user_info_missing", type = "user_info"
                    )
                )
            )
        } else {
            LOGS.d("noise_fit_event:colorfit_pro_2 getActivitySwitch 0 ")
            BLEManager.startSyncConfigInfo()
        }
    }


    private val configCallBack: IConfigCallBack = object : IConfigCallBack {
        override fun onStart() {
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.DeviceUnitsSync(
                    SyncDataStatus(status = EventConstants.UPDATE_STATUS_STARTED)
                )
            )
        }

        override fun onStop() {
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.DeviceUnitsSync(
                    SyncDataStatus(status = EventConstants.UPDATE_STATUS_INTERRUPTED)
                )
            )
        }

        override fun onSuccess() {
            colorFitDevice?.deviceType?.let { deviceType ->
                if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType || deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType
                    || deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType
                    || deviceType == DeviceType.NOISEFIT_AGILE.deviceType
                ) {
                    LOGS.d("noise_fit_event:colorfit_pro_2 getActivitySwitch 1 ")
                    setOutdoorActivitiesPro3()
                    getWalkReminderData()
                    getActivityRecogniseSettings()
                } else {
                    setOutdoorActivities()
//                    testQueryDeviceDataCallback?.onQueryDataReceived(
//                        QueryCallback.DeviceUnitsSync(
//                            SyncDataStatus(status = EventConstants.UPDATE_STATUS_SUCCESS)
//                        )
//                    )
                    getUserInfo()
                    getUserGoals()
                    getDoNotDisturbData()
                    getSedentaryData()
                    getMusicControlSettings()
                    queryBatteryPower()
                    getWristLiftGesture()
                    getHeartRateInterval()
                    getFindPhoneSwitch()
                }
            }
        }

        override fun onFailed() {
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.DeviceUnitsSync(
                    SyncDataStatus(status = EventConstants.UPDATE_STATUS_FAILED)
                )
            )
        }
    }

    private val callBack = object : GetDeviceInfoCallBack.ICallBack {
        override fun onGetBasicInfo(basicInfo: BasicInfo?) {
            basicInfo?.let {
                info = basicInfo

                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.BatteryDataObtained(
                        BatteryData(percentage = it.energe)
                    )
                )
                val version = it.firmwareVersion.toString()
                CF2Globals.basicInfo = it

                WatchInfoGlobals.firmwareVersion = version
                WatchInfoGlobals.firmwareVersionNumber = it.firmwareVersion
                WatchInfoGlobals.firmwareDeviceId = it.deivceId

                watchDataStore.logWatchInfo(" F_VERSION : ${WatchInfoGlobals.firmwareVersion} | F_VERSION_NUMBER : ${WatchInfoGlobals.firmwareVersionNumber} | F_DEVICE_ID : ${WatchInfoGlobals.firmwareDeviceId} ")

                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.FirmwareVersionObtained(
                        DeviceFirmware(version = version)
                    )
                )
            }
        }

        override fun onGetDeviceSummarySoftVersionInfo(p0: DeviceSummarySoftVersionInfo?) {}

        override fun onGetFunctionTable(supportFunctionInfo: SupportFunctionInfo?) {
            LOGS.d("dfdfdfdfddfd" + supportFunctionInfo?.multiDial + ":" + supportFunctionInfo?.ex_main7_photo_wallpaper)
            CF2Globals.isCloudDialSupport = supportFunctionInfo?.multiDial
            CF2Globals.isCustomDialSupport = supportFunctionInfo?.ex_main7_photo_wallpaper
        }

        override fun onGetTime(time: SystemTime?) {}
        override fun onGetMacAddress(p0: MacAddressInfo?) {

        }


        override fun onGetBatteryInfo(batteryInfo: BatteryInfo?) {}

        override fun onGetCanDownloadLangInfo(p0: CanDownLangInfo?) {}

        override fun onGetFlashBinInfo(p0: FlashBinInfo?) {}

        override fun onGetSNInfo(snInfo: SNInfo?) {}

        override fun onGetNoticeCenterSwitchStatus(info: NoticeSwitchInfo?) {}

        override fun onGetLiveData(liveData: LiveData?) {}

        override fun onGetHIDInfo(hidInfo: HIDInfo?) {}

        override fun onGetActivityCount(p0: ActivityDataCount?) {}

        override fun onGetCanDownloadLangInfoV3(p0: CanDownLangInfoV3?) {}
        override fun onGetNoticeReminderSwitchStatus(p0: NoticeReminderSwitchStatus?) {

        }


    }

    private val phoneMsgNoticeCallBack = object : PhoneMsgNoticeCallBack.ICallBack {
        override fun onUnReadMessage() {}

        override fun onV3MessageNotice(p0: Int) {}

        override fun onStopCall() {}

        override fun onNewMessage() {}

        override fun onCalling() {}
    }

    private val appDeviceParaCallBack: GetDeviceParaCallBack.ICallBack =
        object : GetDeviceParaCallBack.ICallBack {
            override fun onGetAlarmV3(p0: MutableList<AlarmV3>?) {
                val alarmData = Colorfit2DataConverter.parseAlarmsV3(p0)
                alarmData.let {

                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.AlarmsObtained(
                            it
                        )
                    )

                }
            }

            override fun onGetScheduleReminderV3(p0: MutableList<ScheduleReminderV3>?) {

            }


            override fun onGetDeviceUpgradeState(p0: DeviceUpgradeState?) {
            }

            override fun onGetMenuList(p0: MenuList.DeviceReturnInfo?) {

            }

            override fun onGetWalkReminder(p0: WalkReminder?) {

            }

            override fun onGetSportThreeCircleGoal(p0: CalorieAndDistanceGoal?, p1: String?) {

            }

            override fun onGetActivitySwitch(p0: ActivitySwitch?) {

            }

            override fun onGetAllHealthMonitorSwitch(p0: AllHealthMonitorSwitch?) {

            }

            override fun onGetFirmwareAndBt3Version(p0: FirmwareAndBt3Version?) {

            }

            override fun onGetPressCalibrationValue(p0: PressCalibrationValue?) {

            }

            override fun onGetBtA2dpHfpStatus(p0: BtA2dpHfpStatus?) {

            }

            override fun onGetContactReceiveTime(p0: Boolean) {

            }

            override fun onGetDeviceBeepInfo(p0: DeviceBeepInfo?) {

            }


            override fun onGetDoNotDisturbPara(p0: NotDisturbPara?) {
                val doNotDisturb = Colorfit2DataConverter.parseDoNotDisturb(p0)

                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.DoNotDisturbObtained(
                        doNotDisturb
                    )
                )
            }

            override fun onGetSupportSportInfoV3(p0: SupportSportInfoV3?) {
            }

            override fun onGetUpHandGesture(p0: UpHandGesture?) {
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.WristLiftGestureObtained(
                        Colorfit2DataConverter.parseWristSenseData(p0)
                    )
                )
            }

            override fun onGetScreenBrightness(p0: ScreenBrightness?) {

            }

        }

    private fun setOutdoorActivities() {
        val quickSportMode = QuickSportMode()
        quickSportMode.sport_type0_run = true
        quickSportMode.sport_type0_walk = true
        quickSportMode.sport_type1_spinning = true
        quickSportMode.sport_type0_on_foot = true
        quickSportMode.sport_type2_yoga = true
        quickSportMode.sport_type1_fitness = true
        quickSportMode.sport_type0_by_bike = true
        quickSportMode.sport_type0_mountain_climbing = true
        quickSportMode.sport_type1_treadmill = true

        //new sports mode

//        quickSportMode.sport_type0_badminton = true
//        quickSportMode.sport_type0_other = true
//        quickSportMode.sport_type0_swim = true
//        quickSportMode.sport_type1_dumbbell = true
//        quickSportMode.sport_type1_ellipsoid  = true
//        quickSportMode.sport_type1_push_up = true
//        quickSportMode.sport_type1_sit_up = true
//        quickSportMode.sport_type1_weightlifting = true
//        quickSportMode.sport_type2_basketball = true
//        quickSportMode.sport_type2_bodybuilding_exercise = true
//        quickSportMode.sport_type2_footballl = true
//        quickSportMode.sport_type2_rope_skipping = true
//        quickSportMode.sport_type2_table_tennis = true
//        quickSportMode.sport_type2_tennis = true
//        quickSportMode.sport_type2_volleyball = true
//        quickSportMode.sport_type3_baseball = true
//        quickSportMode.sport_type3_dance = true
//        quickSportMode.sport_type3_golf = true
//        quickSportMode.sport_type3_roller_skating = true
//        quickSportMode.sport_type3_skiing = true


        BLEManager.setQuickSportMode(quickSportMode)
    }

    fun setOutdoorActivitiesPro3() {
        val functionInfo = LocalDataManager.getSupportFunctionInfo()
        if (functionInfo != null && functionInfo.ex_table_main7_v3_sports_type) {
            val sportModeSortV3 = SportModeSortV3()
            sportModeSortV3.item = getAllAvailableSportInfo()
            sportModeSortV3.num = getAllAvailableSportInfo().size
            if (BLEManager.isConnected()) {
                BLEManager.setSportModeSortInfoV3(sportModeSortV3)
            }
        }
    }

    private fun getAllAvailableSportInfo(): List<SportModeSortV3.SportModeSortItemV3> {
        val list = arrayListOf<SportModeSortV3.SportModeSortItemV3>()
        val functionInfo = LocalDataManager.getSupportFunctionInfo()

        var i = 1
        if (functionInfo?.outdoor_walk == true) {
            val item = SportModeSortV3.SportModeSortItemV3()
            item.index = i
            item.type = SportType.SOPRT_TYPE_OUTDOOR_WALK
            list.add(item)
            i++
        }

        if (functionInfo?.outdoor_run == true) {
            val item = SportModeSortV3.SportModeSortItemV3()
            item.index = i
            item.type = SportType.SOPRT_TYPE_OUTDOOR_RUN
            list.add(item)
            i++
        }

        if (functionInfo?.outdoor_cycle == true) {
            val item = SportModeSortV3.SportModeSortItemV3()
            item.index = i
            item.type = SportType.SOPRT_TYPE_OUTDOOR_CYCLE
            list.add(item)
            i++
        }

        if (functionInfo?.indoor_walk == true) {
            val item = SportModeSortV3.SportModeSortItemV3()
            item.index = i
            item.type = SportType.SOPRT_TYPE_INDOOR_WALK
            list.add(item)
            i++
        }

        if (functionInfo?.indoor_run == true) {
            val item = SportModeSortV3.SportModeSortItemV3()
            item.index = i
            item.type = SportType.SOPRT_TYPE_INDOOR_RUN
            list.add(item)
            i++
        }

        if (functionInfo?.indoor_cycle == true) {
            val item = SportModeSortV3.SportModeSortItemV3()
            item.index = i
            item.type = SportType.SOPRT_TYPE_INDOOR_CYCLE
            list.add(item)
            i++
        }

        if (functionInfo?.elliptical == true) {
            val item = SportModeSortV3.SportModeSortItemV3()
            item.index = i
            item.type = SportType.SOPRT_TYPE_ELLIPTICAL
            list.add(item)
            i++
        }

        if (functionInfo?.rower == true) {
            val item = SportModeSortV3.SportModeSortItemV3()
            item.index = i
            item.type = SportType.SOPRT_TYPE_ROWER
            list.add(item)
            i++
        }

        if (functionInfo?.pool_swim == true) {
            val item = SportModeSortV3.SportModeSortItemV3()
            item.index = i
            item.type = SportType.SOPRT_TYPE_POOL_SWIM
            list.add(item)
            i++
        }

        if (functionInfo?.open_water_swim == true) {
            val item = SportModeSortV3.SportModeSortItemV3()
            item.index = i
            item.type = SportType.SOPRT_TYPE_WATER_SWIM
            list.add(item)
            i++
        }

        if (functionInfo?.cricket == true) {
            val item = SportModeSortV3.SportModeSortItemV3()
            item.index = i
            item.type = SportType.SPORT_TYPE_CRICKET
            list.add(item)
            i++
        }

        if (functionInfo?.sport_type0_on_foot == true) {
            val item = SportModeSortV3.SportModeSortItemV3()
            item.index = i
            item.type = SportType.SPORT_TYPE_ONFOOT
            list.add(item)
            i++
        }

        if (functionInfo?.sport_type2_yoga == true) {
            val item = SportModeSortV3.SportModeSortItemV3()
            item.index = i
            item.type = SportType.SPORT_TYPE_YOGA
            list.add(item)
            i++
        }
        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType) {
                if (functionInfo?.sport_type1_fitness == true) {
                    val item = SportModeSortV3.SportModeSortItemV3()
                    item.index = i
                    item.type = SportType.SPORT_TYPE_FITNESS
                    list.add(item)
                    i++
                }
            } else {
                if (functionInfo?.sport_type1_fitness == true) {
                    val item = SportModeSortV3.SportModeSortItemV3()
                    item.index = i
                    item.type = SportType.SPORT_TYPE_OTHER
                    list.add(item)
                    i++
                }
            }
        }


        return list
    }

//    override fun getSportModeInfo() {
//        SharedPreferenceHelper.getConnectedDevice()?.deviceType?.let { deviceType ->
//            if(deviceType == "colorfit_pro_3"){
//                val modes = Colorfit2DataConverter.parseSportsModeInfoV3(LocalDataManager.getSupportFunctionInfo())
//                queryDeviceDataCallbacks?.onSportModeInfoObtained(modes)
//            } else {
//                val modes = Colorfit2DataConverter.parseSportsModeInfo(LocalDataManager.getQuickSportMode())
//                queryDeviceDataCallbacks?.onSportModeInfoObtained(modes)
//
//            }
//        }
//    }

    override fun getSportModeInfo() {
        colorFitDevice?.deviceType?.let { deviceType ->
            LOGS.d("deviceResponseCallback sports mode:::" + deviceType)
            if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType || deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType
                || deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType
                || deviceType == DeviceType.NOISEFIT_AGILE.deviceType
            ) {
                if (LocalDataManager.getSportModeSortV3() != null) {
                    val modes =
                        Colorfit2DataConverter.parseSportsModeInfoV3(LocalDataManager.getSportModeSortV3())
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.SportModeInfoObtained(
                            modes
                        )
                    )
                } else {
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.SportModeInfoObtained(
                            null
                        )
                    )
                }
            } else {
                if (LocalDataManager.getQuickSportMode() != null) {
                    val modes =
                        Colorfit2DataConverter.parseSportsModeInfo(LocalDataManager.getQuickSportMode())
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.SportModeInfoObtained(
                            modes
                        )
                    )
                } else {
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.SportModeInfoObtained(
                            null
                        )
                    )
                }
            }
        }
    }

    override fun getFirmwareLogs() {

//        val file = FilePathUtils.getLogPath(colorFitDevice)
//        if (!file.exists()) {
//            file.mkdir()
//        }
//        try {
//            val logFile = File(file, logFileName)
//            if (logFile.exists()) {
//                logFile.delete()
//            }
//            logFile.createNewFile()
//
//            BLEManager.collectDeviceFlashLog(logFile.path, 50, object : ICollectFlashLogListener {
//                override fun onStart() {
//                    LOGS.i("Logs", "Log started")
//                }
//
//                override fun onFinish() {
//                    LOGS.i("Logs", "Log finished")
//                    testQueryDeviceDataCallback?.onQueryDataReceived(
//                        QueryCallback.FirmwareLogObtained(
//                            logFile.absolutePath
//                        )
//                    )
//                }
//
//            })
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    companion object {
        private var deviceControlsCallback: DeviceControlAppCallBack.ICallBack? = null
    }

    private fun getDeviceControlAppCallback(): DeviceControlAppCallBack.ICallBack? {
        if (deviceControlsCallback == null) {
            deviceControlsCallback = object : DeviceControlAppCallBack.ICallBack {
                override fun onControlEvent(
                    deviceControlEventType: DeviceControlAppCallBack.DeviceControlEventType,
                    p1: Int
                ) {
                    LOGS.d(
                        "noise_fit_event:colorfit_pro_2 device control::" + deviceControlEventType
                    )
                    when (deviceControlEventType) {
                        DeviceControlAppCallBack.DeviceControlEventType.START -> {
                            testQueryDeviceDataCallback?.onQueryDataReceived(
                                QueryCallback.OnMusicEventChanged(
                                    MusicControlActionsEvents.PLAY
                                )
                            )
                            //onMusicEventChanged(MusicControlActionsEvents.PLAY)
                        }
                        DeviceControlAppCallBack.DeviceControlEventType.PAUSE -> {
                            testQueryDeviceDataCallback?.onQueryDataReceived(
                                QueryCallback.OnMusicEventChanged(
                                    MusicControlActionsEvents.PAUSE
                                )
                            )
                            //onMusicEventChanged(MusicControlActionsEvents.PAUSE)
                        }
                        DeviceControlAppCallBack.DeviceControlEventType.STOP -> {
                            testQueryDeviceDataCallback?.onQueryDataReceived(
                                QueryCallback.OnMusicEventChanged(
                                    MusicControlActionsEvents.STOP
                                )
                            )
                            //onMusicEventChanged(MusicControlActionsEvents.PAUSE)
                        }
                        DeviceControlAppCallBack.DeviceControlEventType.NEXT -> {
                            testQueryDeviceDataCallback?.onQueryDataReceived(
                                QueryCallback.OnMusicEventChanged(
                                    MusicControlActionsEvents.NEXT
                                )
                            )
                            //onMusicEventChanged(MusicControlActionsEvents.NEXT)
                            LOGS.d("noise_fit_event:colorfit_pro_2 music mode::")
                        }
                        DeviceControlAppCallBack.DeviceControlEventType.PREVIOUS -> {
                            testQueryDeviceDataCallback?.onQueryDataReceived(
                                QueryCallback.OnMusicEventChanged(
                                    MusicControlActionsEvents.PREVIOUS
                                )
                            )
                            //onMusicEventChanged(MusicControlActionsEvents.PREVIOUS)
                        }
                        DeviceControlAppCallBack.DeviceControlEventType.REJECT_PHONE -> {
                            testQueryDeviceDataCallback?.onQueryDataReceived(
                                QueryCallback.UpdateCallStatus(false)
                            )
                        }
                        DeviceControlAppCallBack.DeviceControlEventType.VOLUME_DOWN -> {
                            updateVolume(false)
                        }
                        DeviceControlAppCallBack.DeviceControlEventType.VOLUME_UP -> {
                            updateVolume(true)
                        }
                        DeviceControlAppCallBack.DeviceControlEventType.TAKE_ONE_PHOTO -> {
                            testQueryDeviceDataCallback?.onQueryDataReceived(QueryCallback.ClickCameraImage)
                        }
                        else -> {
                        }
                    }
                }


                override fun onFindPhone(b: Boolean, l: Long) {
                    PhoneRinger.enableRing(b)
                }

                override fun onOneKeySOS(b: Boolean, l: Long) {}
                override fun onAntiLostNotice(b: Boolean, l: Long) {}
            }

        }
        return deviceControlsCallback
    }

    override fun setVolume(currentVolume: Int, maxVolume: Int) {
        if (maxVolume == 0) {
            return
        }
        if (previousVolume == currentVolume) {
            return
        }
        previousVolume = currentVolume

        val mm = PhoneVoice()
        mm.now_voice = currentVolume
        mm.total_voice = maxVolume
       // if (BLEManager.isConnected()) {
        BLEManager.setPhoneVoice(mm)
        //}


    }

    private fun updateVolume(isIncreaseVolume: Boolean) {
        val audioManager =
            NoisefitApplication.context?.applicationContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC, when (isIncreaseVolume) {
                true -> AudioManager.ADJUST_RAISE
                else -> AudioManager.ADJUST_LOWER
            }, AudioManager.FLAG_SHOW_UI
        )
        val sb2value = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        val cur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        LOGS.d("suffused$sb2value:$cur")
        val mm = PhoneVoice()
        mm.now_voice = cur
        mm.total_voice = sb2value
        BLEManager.setPhoneVoice(mm)

    }

}