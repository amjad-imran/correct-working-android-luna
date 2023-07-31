package com.noisefit_nav_plus.handler


import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.handler.MusicControlActionsEvents
import com.noisefit_commans.interfaces.IQueryDataCallback
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.*
import com.noisefit_nav_plus.base.NavPlusApplicationHandler
import com.noisefit_nav_plus.handler.dataConversion.DataConverter
import com.zjw.zhbraceletsdk.bean.*
import com.zjw.zhbraceletsdk.linstener.*
import com.zjw.zhbraceletsdk.service.BleConstant
import com.zjw.zhbraceletsdk.service.ZhBraceletService
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.timerTask
import kotlin.math.roundToInt


class NavPlusQueryDeviceUnitsHandler
@Inject
constructor(
    var navPlusApplicationHandler: NavPlusApplicationHandler,
    var dataConverter: DataConverter,
    var context: Context,
    var gson: Gson,
    var watchDataStore: WatchDataStore
) : QueryDeviceDataActions() {

    companion object {
        const val LOCATION_BROADCAST_RECEIVER = "LOCATION_BROADCAST_RECEIVER"
        const val LAT_LONG = "LAT_LONG"
    }

    private var songName: String? = null
    private var previousVolume = 0
    private var locationClientClass: LocationClientClass? = null
    private var firstLocation = true
    private var mBleService: ZhBraceletService? = null
    private var musicInfo: MusicInfo = MusicInfo()
    private var testQueryDeviceDataCallback: IQueryDataCallback? = null
    private var currentGpsSportState = -1
    private var noiseFitDevice: ColorFitDevice? = null

    override fun <T> callbackListener(callback: T) {
    }

    override fun <T> callbackListenerNew(callback: T) {
        testQueryDeviceDataCallback = callback as IQueryDataCallback
    }

    override fun setDevice(device: ColorFitDevice) {
        noiseFitDevice = device

        //initLogListener()
    }


    override fun setVolume(currentVolume: Int, maxVolume: Int) {
        if (maxVolume == 0) {
            return
        }
        if (previousVolume == currentVolume) {
            return
        }
        previousVolume = currentVolume
        val offset = 10000 / maxVolume
        val ss = (offset * currentVolume)

        musicInfo.volumeLevel = ss
        mBleService?.syncMusicInfo(musicInfo)
    }


    override fun setMusicStatus(status: Int, title: String?, sec: Int?) {

        LOGS.d("setMusicStatus $status $title")
        musicInfo = getMusicInfo()
        musicInfo.musicName = title
        songName = title
        when (status) {
            3 -> {
                musicInfo.playState = MusicInfo.TAG_PLAY_SATE_PAUSED
            }
            2 -> {
                musicInfo.playState = MusicInfo.TAG_PLAY_SATE_PAUSED
            }
            1 -> {
                musicInfo.playState = MusicInfo.TAG_PLAY_SATE_PLAYING
            }
        }

        mBleService?.syncMusicInfo(musicInfo)
    }

    private fun getMusicInfo(): MusicInfo {
        return musicInfo
    }

    override fun init() {
        super.init()
        mBleService = navPlusApplicationHandler.getZhBraceletService()

        removeCallbacks()
        attachCallbacks()
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

    private fun sendErrorMessageToApp(message: String, title: String) {
        noiseFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.COLORFIT_BRIO.deviceType || deviceType == DeviceType.XFIT.deviceType || deviceType == DeviceType.COLORFIT_CALIBER.deviceType || deviceType == DeviceType.COLORFIT_CALIBER_2.deviceType || deviceType == DeviceType.COLORFIT_GRAND.deviceType ||
                deviceType == DeviceType.XFIT2.deviceType ||deviceType == DeviceType.COLORFIT_BRIO_PRO.deviceType
            ) {
                mBleService?.thirdPartyPush(
                    BleConstant.NOTIFY_TYPE_NOISE, title,
                    message
                )
            } else if (deviceType == DeviceType.COLORFIT_ULTRA_2.deviceType || deviceType == DeviceType.COLORFIT_ULTRA_BUZZ.deviceType ||
                deviceType == DeviceType.COLORFIT_VISION_BUZZ.deviceType
            ) {
                mBleService?.thirdPartyPush(
                    BleConstant.NOTIFY_TYPE_NOISE, title,
                    message
                )
            } else {
                mBleService?.setRemind(
                    message, BleConstant.NotifaceMsgNoise
                )
            }
        }
    }

    override fun attachCallbacks() {
        removeCallbacks()

        mBleService?.setContactsListListener { contactBeanList ->
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.ContactListObtained(
                    dataConverter.formatContactList(contactBeanList)
                )
            )
        }

        mBleService?.setBodyTemperatureUnitListener(object : BodyTemperatureUnitListener {
            override fun onResult(isFahrenheit: Boolean) {

                var unit = Units.METRIC
                if (isFahrenheit) {
                    unit = Units.IMPERIAL
                }
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.BodyTempUnit(
                        unit
                    )
                )
            }

            override fun onSuccess() {

            }

            override fun onFail() {

            }
        })
        mBleService?.let { zhBleService ->
            initQuickEyeListener()
            //initLogListener()
            setStockListener()

            mBleService?.setDeviceGpsSportListener(object : DeviceGpsSportListener {
                override fun sportStart() {

                    if (hasPermission()) {
                        currentGpsSportState = 0
                        LOGS.d("GPS Started")
                        enableLocation()
                    } else {
                        sendErrorMessageToApp(
                            "Please enable location permission from activity screen in app, before starting a new run.",
                            "Alert"
                        )
                        watchDataStore.setAskForPermission(true)

                    }


                }

                override fun sportPause() {
                    currentGpsSportState = 1
                    LOGS.d("GPS Pause")
                }

                override fun sportResume() {
                    currentGpsSportState = 2
                    LOGS.d("GPS Resume")
                }

                override fun sportStop() {
                    firstLocation = true
                    disableLocation()
                    LOGS.d("GPS STOP")
                }
            })
            zhBleService.addSimplePerformerListenerLis(mPerformerListener)
            zhBleService.setRequestAlarmReminderListener { alarmList ->
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.AlarmsObtained(
                        dataConverter.formatAlarmData(alarmList)
                    )
                )
            }
        }
    }

    override fun getContactList() {
        mBleService?.getContactsList()
    }

    override fun removeCallbacks() {
        try {
            mBleService?.removeSimplePerformerListenerLis(mPerformerListener)
        } catch (exp: Exception) {
            exp.printStackTrace()
        }
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
    }

    private fun disableLocation() {
        NoisefitApplication.context?.let {
            locationClientClass?.removeLocationUpdates(it)
            LocalBroadcastManager
                .getInstance(it)
                .unregisterReceiver(locationReceiver)
        }
    }

    private var locationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (currentGpsSportState == 0 || currentGpsSportState == 2) {
                val locationArrayList =
                    intent.getParcelableArrayListExtra<LocationDataModel>(LAT_LONG)

                if (firstLocation) {
                    mBleService?.setGpsReady(0x00)
                    firstLocation = false
                }
                // val gson = Gson()

                if (locationArrayList.isNullOrEmpty()) {
                    return
                }

                locationArrayList.forEach { location ->

                    //  LOGS.d("LOCATION", "location receiver ${gson.toJson(location)}")

                    val gpsInfo = GpsInfo()
                    gpsInfo.altitude = location.altitude
                    gpsInfo.bearing = location.bearing
                    gpsInfo.longitude = location.longitude
                    gpsInfo.latitude = location.latitude
                    gpsInfo.timestamp = location.time
                    gpsInfo.speed = location.speed
                    gpsInfo.gpsAccuracy = location.accuracy.roundToInt()

                    mBleService?.sendGpsInfo(gpsInfo)
                }

            }

        }
    }

    override fun queryFirmwareVersion() {
        mBleService?.getDeviceInfo()
    }


    override fun getBluetoothCallStatus() {
        mBleService?.queryBluetoothCallState { connected ->
            LOGS.d("isCallingWatch ${connected}")
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.BluetoothCallStatus(
                    connected
                )
            )

        }
    }

    private fun initLogListener() {

        mBleService?.setLogListener(object : LogListener {
            override fun i(tag: String, msg: String) {
                LOGS.i(" $tag :$msg")
                FileLogsUtils.saveILogs(noiseFitDevice, tag, msg, FileLogsUtils.LogType.Watch)


            }

            override fun w(tag: String, msg: String) {
                LOGS.w(" $tag :$msg")
                FileLogsUtils.saveWLogs(noiseFitDevice, tag, msg, FileLogsUtils.LogType.Watch)
            }

            override fun e(tag: String, msg: String) {
                LOGS.e(" $tag :$msg")
                FileLogsUtils.saveELogs(noiseFitDevice, tag, msg, FileLogsUtils.LogType.Watch)
            }
        })

    }

    override fun queryBatteryPower() {
        mBleService?.getDeviceInfo()
    }

    override fun getUserInfo() {

    }

    override fun getLanguage() {

    }


    override fun queryFirmwareUpgrade() {

//        val response = FirmwareUpgradeHandler.checkForNewVersion(
//            NavPlusGlobals.firmwareVersionNumber,
//            NavPlusGlobals.firmwareDeviceId
//        )
//        var deviceFirmware = DeviceFirmware(status = "update_not_available")
//        response?.data?.let {
//            when (response.resultCode) {
//                1 -> {
//                    deviceFirmware = when (response.data?.forceUpdate ?: false) {
//                        true -> {
//                            DeviceFirmware(status = "forced_update_available")
//                        }
//                        else -> {
//                            DeviceFirmware(status = "update_available")
//                        }
//                    }
//                    deviceFirmware.message = response.message
//                    deviceFirmware.version = response.data?.descriptionEnglish
//                    NavPlusGlobals.firmwareUrl = response.data?.url
//                }
//                else -> {
//                    deviceFirmware = DeviceFirmware(status = "update_not_available")
//                }
//            }
//        }
//        queryDeviceDataCallback?.onFirmwareUpgradeAvailable(deviceFirmware)
    }

    override fun queryFirmwareUpgradeNew(colorFitDevice: ColorFitDevice) {
//        val response = FirmwareUpgradeHandler.checkForNewVersion(
//            NavPlusGlobals.firmwareVersionNumber,
//            NavPlusGlobals.firmwareDeviceId
//        )
//        var deviceFirmware = DeviceFirmware(status = "update_not_available")
//        response?.data?.let {
//            when (response.resultCode) {
//                1 -> {
//                    deviceFirmware = when (response.data?.forceUpdate ?: false) {
//                        true -> {
//                            DeviceFirmware(status = "forced_update_available")
//                        }
//                        else -> {
//                            DeviceFirmware(status = "update_available")
//                        }
//                    }
//                    deviceFirmware.message = response.message
//                    deviceFirmware.version = response.data?.descriptionEnglish
//                    NavPlusGlobals.firmwareUrl = response.data?.url
//                }
//                else -> {
//                    deviceFirmware = DeviceFirmware(status = "update_not_available")
//                }
//            }
//        }
//        queryDeviceDataCallbacks?.onFirmwareUpgradeAvailableNew(deviceFirmware)
    }


    override fun getWatchFaces() {
//        SharedPreferenceHelper.getConnectedDevice()?.deviceType?.let { deviceType ->
//            getCloudWatchFaces(deviceType)
//        }
    }


    override fun getMedicineReminders() {
        //mBleService.me
    }


    override fun getReminders() {

        mBleService?.setRequestEventReminderListener { p0 ->
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.RemindersObtained(
                    dataConverter.formatReminderData(p0)
                )
            )
        }

    }

    override fun getBodyTempUnit() {


        mBleService?.getBodyTemperatureUnit()
    }

    override fun getAlarms() {
        mBleService?.getAlarmReminder()

    }

    /*override fun getSwitchSetting() {
        //BluetoothSDK.getSwitchSetting(resultCallBack)
    }*/

    override fun getAutoSleep() {
        //BluetoothSDK.getAutoSleep(resultCallBack)
    }

    override fun getHeartRateInterval() {
        //BluetoothSDK.getAutoHeartRateFrequency(resultCallBack)
    }

    override fun getHeartRateAlert() {

        noiseFitDevice?.let {
            if (it.deviceType.equals(DeviceType.COLORFIT_NAV_PLUS.deviceType, true)) {

                val heartRateInterval = watchDataStore.getHeartRateInterval()
                val status = heartRateInterval?.status ?: false

                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.HeartRateAlertDataObtained(
                        HeartRateAlert(
                            status,
                            0,
                            140
                        )
                    )
                )

                return
            }
        }


        mBleService?.getHeartRateMonitor { isOpen, warningValue ->
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.HeartRateAlertDataObtained(
                    HeartRateAlert(
                        isOpen,
                        0,
                        warningValue
                    )
                )
            )
        }
    }

    override fun getSedentaryData() {
//        SharedPreferenceHelper.getConnectedDevice()?.deviceType?.let { deviceType ->
//            if (deviceType == "colorfit_nav_plus") {
//                return
//            }
//        }

        noiseFitDevice?.deviceType?.let {
            if (it.equals(DeviceType.COLORFIT_PULSE.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_BEAT.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_CALIBER.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_CALIBER_2.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_GRAND.deviceType, true) ||
                it.equals(DeviceType.XFIT2.deviceType, true)
            ) {
                var idleAlert = watchDataStore.getIdleAlert()
                if (idleAlert == null) {
                    idleAlert = SedentaryData(
                        status = false,
                        interval = 1,
                        startHour = 10,
                        startMinute = 0,
                        endHour = 22,
                        endMinute = 0
                    )
                }
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.SedentaryDataObtained(
                        idleAlert
                    )
                )
                return
            }
        }

        mBleService?.setRequestSedentaryReminderListener { p0 ->
            val longSit = p0?.let { dataConverter.parseSedentaryData(it) }
            if (longSit != null) {
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.SedentaryDataObtained(longSit)
                )
            }
        }
        mBleService?.getSedentaryReminder()

    }

    override fun getDeviceUnits() {
        //BluetoothSDK.getUnit(resultCallBack)
    }

    override fun getScreenAwakeInterval() {
        //BluetoothSDK.getBrightScreenTime(resultCallBack)
    }


    override fun getHandwashData() {

        noiseFitDevice?.deviceType?.let {
            if (it.equals(DeviceType.COLORFIT_CALIBER.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_GRAND.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_CALIBER_2.deviceType, true) ||
                it.equals(DeviceType.XFIT2.deviceType, true)
            ) {
                val handWashData =
                    watchDataStore.getHandWashData() ?: dataConverter.dummyHandWashData()
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.GetHandWashing(handWashData)
                )
                return
            }
        }
        mBleService?.setRequestHandWashingReminderListener { p0 ->
            val longSit = p0?.let { dataConverter.parseHandWashData(it) }
            if (longSit != null) {
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.GetHandWashing(longSit)
                )
            }
        }
        mBleService?.getHandWashingReminder()

    }


    private val mPerformerListener: SimplePerformerListener = object : SimplePerformerListener() {
        override fun onResponseDeviceInfo(mDeviceInfo: DeviceInfo) {

            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.BatteryDataObtained(
                    BatteryData(percentage = mDeviceInfo.deviceBattery)
                )
            )
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.FirmwareVersionObtained(
                    DeviceFirmware(version = mDeviceInfo.deviceVersionName as String)
                )
            )
            WatchInfoGlobals.firmwareVersion = mDeviceInfo.deviceVersionName as String
            WatchInfoGlobals.firmwareVersionNumber = mDeviceInfo.deviceVersionNumber
            WatchInfoGlobals.firmwareDeviceId = mDeviceInfo.deviceType

            watchDataStore.logWatchInfo(" F_VERSION : ${WatchInfoGlobals.firmwareVersion} | F_VERSION_NUMBER : ${WatchInfoGlobals.firmwareVersionNumber} | F_DEVICE_ID : ${WatchInfoGlobals.firmwareDeviceId} ")
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.FirmwareVersionObtained(DeviceFirmware(version = WatchInfoGlobals.firmwareVersion))
            )
        }

        override fun onResponseMotionInfo(mMotionInfo: MotionInfo) {

        }

        override fun onResponseSleepInfo(mSleepInfo: SleepInfo) {

        }

        override fun onResponseWoHeartInfo(mWoHeartInfo: WoHeartInfo) {

        }

        override fun onResponseComplete() {
//
        }

        override fun onResponsePhoto() {
            testQueryDeviceDataCallback?.onQueryDataReceived(QueryCallback.ClickCameraImage)
        }

        override fun onResponseFindPhone() {
            PhoneRinger.enableRing(true)
            Timer().schedule(timerTask {
                PhoneRinger.enableRing(false)
            }, 2000)
        }

        override fun onResponseCloseCall() {
            testQueryDeviceDataCallback?.onQueryDataReceived(QueryCallback.UpdateCallStatus(false))
        }

        override fun onResponseHeartInfo(mHeartInfo: HeartInfo) {

        }

        override fun onResponseMusicControlCmd(p0: Int) {
            musicInfo = getMusicInfo()
            if (MusicUtil.isMusicActive()) {
                musicInfo.playState = MusicInfo.TAG_PLAY_SATE_PLAYING
            } else {
                musicInfo.playState = MusicInfo.TAG_PLAY_SATE_PAUSED
            }
            LOGS.d("setMusicStatus onResponseMusicControlCmd $p0 ${gson.toJson(musicInfo)}")

            when (p0) {
                0 -> {


                    musicInfo.volumeLevel = returnVolume()
                    musicInfo.musicName = MusicUtil.getSongName(songName)

                    if (mBleService != null) {
                        mBleService?.syncMusicInfo(
                            musicInfo
                        )
                    }
                }
                1 -> {
                    if (musicInfo.playState == MusicInfo.TAG_PLAY_SATE_PAUSED) {

                        musicInfo.playState = MusicInfo.TAG_PLAY_SATE_PLAYING
                        if (mBleService != null) {
                            mBleService?.syncMusicInfo(
                                musicInfo
                            )
                        }
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.OnMusicEventChanged(
                                MusicControlActionsEvents.PLAY
                            )
                        )
                    } else {
                        musicInfo.playState = MusicInfo.TAG_PLAY_SATE_PAUSED
                        if (mBleService != null) {
                            mBleService?.syncMusicInfo(
                                musicInfo
                            )
                        }
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.OnMusicEventChanged(
                                MusicControlActionsEvents.PAUSE
                            )
                        )
                    }
                }
                2 -> {

                }
                3 -> {
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.OnMusicEventChanged(
                            MusicControlActionsEvents.PREVIOUS
                        )
                    )
                }
                4 -> {
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.OnMusicEventChanged(
                            MusicControlActionsEvents.NEXT
                        )
                    )
                }
                5 -> {
                    updateVolume(true)
                }
                6 -> {
                    updateVolume(false)
                }
            }

        }
    }

    private fun returnVolume(): Int {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val sb2value = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val cur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        return ((10000 / sb2value) * cur)
    }


    private fun updateVolume(isIncreaseVolume: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC, when (isIncreaseVolume) {
                true -> AudioManager.ADJUST_RAISE
                else -> AudioManager.ADJUST_LOWER
            }, AudioManager.FLAG_SHOW_UI
        )
        val sb2value = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val cur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        if (previousVolume == cur) {
            return
        }
        previousVolume = cur
        val offset = 10000 / sb2value
        val ss = (offset * cur)
//        LOGS.d("Current volume -", "$cur $sb2value $ss")
        musicInfo.volumeLevel = ss
        mBleService?.syncMusicInfo(musicInfo)

    }


    private fun initQuickEyeListener() {

        mBleService?.setQuickEyeSwitchListener(
            object : QuickEyeSwitchListener {
                override fun onSuccess() {

//                    NavPlusUpdateDeviceUnitsHandler.baseUpdateDeviceDataCallbacks?.onQuickEyeMovementSwitchUpdated(
//                        true
//                    )
                }

                override fun onFail() {

//                    NavPlusUpdateDeviceUnitsHandler.baseUpdateDeviceDataCallbacks?.onQuickEyeMovementSwitchUpdated(
//                        false
//                    )
                }

                override fun onResponseData(isOpen: Boolean) {
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.QuickEyeMovementSwitchObtained(
                            SwitchSetting(
                                status = isOpen
                            )
                        )
                    )
                }
            })
    }

    override fun getMedicineReminderSettings() {
        var medicineData = watchDataStore.getMedicineReminder()
        if (medicineData == null) {
            medicineData = SedentaryData(
                status = false,
                interval = MedicalInfo.MedicalPU4,
                startHour = 8,
                startMinute = 0,
                endHour = 22,
                endMinute = 0
            )
        }
        testQueryDeviceDataCallback?.onQueryDataReceived(
            QueryCallback.MedicineDataObtained(
                medicineData
            )
        )
    }


    override fun getMealReminderSettings() {
        mBleService?.setMealRemindersListener(object : MealRemindersListener {
            override fun onSuccess() {

            }

            override fun onFail() {

            }

            override fun onResponseData(
                p0: Boolean,
                p1: Int,
                p2: Int,
                p3: Int,
                p4: Int,
                p5: Int
            ) {
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.MealDataObtained(
                        SedentaryData(
                            status = p0,
                            startHour = p2,
                            startMinute = p3,
                            endHour = p4,
                            endMinute = p5,
                            interval = p1
                        )
                    )
                )
            }
        })
        mBleService?.getMealReminders()
    }

    override fun getDrinkWaterSettings() {

        noiseFitDevice?.deviceType?.let {
            if (it.equals(DeviceType.COLORFIT_PULSE.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_BEAT.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_CALIBER.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_CALIBER_2.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_GRAND.deviceType, true) ||
                it.equals(DeviceType.XFIT2.deviceType, true)
            ) {
                var idleAlert = watchDataStore.getWaterReminder()
                if (idleAlert == null) {
                    idleAlert = SedentaryData(
                        status = false,
                        interval = 1,
                        startHour = 10,
                        startMinute = 0,
                        endHour = 22,
                        endMinute = 0
                    )
                } else {
                    idleAlert.interval = idleAlert.interval
                }
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.DrinkWaterDataObtained(
                        idleAlert
                    )
                )
                return
            }
        }

        mBleService?.setRequestDrinkReminderListener { p0 ->
            val longSit = p0?.let { dataConverter.parseDrinkWater(it) }
            if (longSit != null) {
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.DrinkWaterDataObtained(longSit)
                )
            }
        }
        mBleService?.getDrinkReminder()
    }

    override fun getCustomReplies() {

        mBleService?.setRequestQuickReplyListener { quickReply ->
            val customReplyData = ArrayList<CustomReplyData.CustomReply>()
            quickReply?.forEach { item ->
                val customReply = CustomReplyData.CustomReply()
                customReply.content = item.content
                customReplyData.add(customReply)
            }
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.CustomReplyObtained(CustomReplyData(customReplies = customReplyData))
            )
        }

    }

    override fun getWorldClock() {

        mBleService?.setRequestWorldClockListener { _, worldClock ->
            val data = ArrayList<WorldClockList.WClock>()
            worldClock.forEach { item ->
                data.add(
                    WorldClockList.WClock(
                        timeZone = item.timeZone,
                        content = item.content
                    )
                )
            }
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.WorldClockDataObtained(WorldClockList(worldClocks = data))
            )
        }

    }

    override fun getStockList() {

        /*mBleService?.setSyncStockListener(object : SyncStockListener {
            override fun deviceRequestSync() {
                LOGS.d("deviceRequestSync")
            }

            override fun syncStockListFromDevice(list: List<StockSymbolBean?>) {
                LOGS.d("getStockList", "syncStockListFromDevice")
                val stockArray = ArrayList<StockSymbol>()
                list.forEachIndexed { index, item ->
                    val stock = item?.symbol?.let {
                        StockSymbol(
                            symbol = it,
                            order = item.order,
                            isWidget = item.isWidget
                        )
                    }
                    stock?.let { stockArray.add(it) }
                }
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.StockListDataObtained(
                        StockSymbolList(stockSymbolList = stockArray)
                    )
                )

            }
        })*/

        mBleService?.getStockListInfo()

    }

    private fun setStockListener() {

        mBleService?.setSyncStockListener(object : SyncStockListener {
            override fun deviceRequestSync() {
                LOGS.d("deviceRequestSync")
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.SyncStock()
                )
            }

            override fun syncStockListFromDevice(list: List<StockSymbolBean?>) {
                LOGS.d("getStockList", "syncStockListFromDevice")
                val stockArray = ArrayList<StockSymbol>()
                list.forEachIndexed { index, item ->
                    val stock = item?.symbol?.let {
                        StockSymbol(
                            symbol = it,
                            order = item.order,
                            isWidget = item.isWidget
                        )
                    }
                    stock?.let { stockArray.add(it) }
                }
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.StockListDataObtained(
                        StockSymbolList(stockSymbolList = stockArray)
                    )
                )

            }

            override fun syncStockInfoListSuccess() {

            }

            override fun deleteStockSuccess() {
                LOGS.d("getStockList", "deleteStockSuccess")
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.DeleteStock(
                        true
                    )
                )
            }

            override fun setStockListSuccess() {

            }

            override fun getStockListInfoSuccess() {

            }
        })
    }

    override fun getQuickEyeMovementSwitch() {
        mBleService?.getQuickEyeSwitch()
    }
}