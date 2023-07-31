package com.noisefit.colorfit_pro.handler

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat
import com.crrepa.ble.conn.bean.*
import com.crrepa.ble.conn.callback.*
import com.crrepa.ble.conn.listener.*
import com.crrepa.ble.conn.type.*
import com.noisefit.colorfit_pro.base.ProApplicationHandler
import com.noisefit.colorfit_pro.dataConversion.DataConverter
import com.noisefit.colorfit_pro.handler.ProUpdateDeviceUnitsHandler.Companion.mobileNumber
import com.noisefit.colorfit_pro.handler.connect.ProConnectHandler.Companion.bleConnection
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.constants.CommonGlobals
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.handler.MusicControlActionsEvents
import com.noisefit_commans.interfaces.IQueryDataCallback
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MusicUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


class ProQueryDeviceUnitsHandler
@Inject
constructor(
    var navPlusApplicationHandler: ProApplicationHandler,
    var dataConverter: DataConverter,
    var context: Context,
    var watchDataStore: WatchDataStore
) : QueryDeviceDataActions() {

    private val customReplyDataList = ArrayList<CustomReplyData.CustomReply>()
    private var songName: String? = null
    private var colorFitDevice: ColorFitDevice? = null
    private var iQueryDataCallback: IQueryDataCallback? = null
    private var previousVolume = 0
    private var wFaceIndex = 1

    override fun init() {
        super.init()
        removeCallbacks()
        attachCallbacks()
    }

    override fun attachCallbacks() {
        bleConnection?.setPhoneOperationListener(crpPhoneOperationListener)
        bleConnection?.setFindPhoneListener(crpFindPhoneListener)
        bleConnection?.setCameraOperationListener(crpCameraOperationListener)
        bleConnection?.setQuickResponsesListener(crpQuickResponsesChangeListener)
    }

    private val crpCameraOperationListener = CRPCameraOperationListener {

        iQueryDataCallback?.onQueryDataReceived(QueryCallback.OpenCameraActivity(false))
        iQueryDataCallback?.onQueryDataReceived(QueryCallback.ClickCameraImage)
    }

    private var quickReplyCount = 0
    private val crpQuickResponsesChangeListener = object : CRPQuickResponsesChangeListener {
        override fun onQuickResponsesCount(p0: CRPQuickResponsesCountInfo?) {
            quickReplyCount = p0?.count ?: 0
            for (index in 0..quickReplyCount) {
                bleConnection?.queryQuickResponses(index.toByte())
            }

//            LOGS.d("updateCustomReply onQuickResponsesCount ${Gson().toJson(p0)}")
        }

        override fun onQuickResponsesDetail(p0: CRPQuickResponsesDetailInfo?) {

            val id = p0?.id?.toInt() ?: 0
            customReplyDataList.add(CustomReplyData.CustomReply(p0?.message, id))
            LOGS.d("updateCustomReply size ${customReplyDataList.size} $quickReplyCount")
            if (customReplyDataList.size == quickReplyCount) {
                iQueryDataCallback?.onQueryDataReceived(
                    QueryCallback.CustomReplyObtained(CustomReplyData(customReplies = customReplyDataList))
                )
            }
//            LOGS.d("updateCustomReply onQuickResponsesDetail ${Gson().toJson(p0)}")
        }

        override fun onSendSms(p0: String?) {
            LOGS.i("QuickReply $p0  ")
            if(!mobileNumber.isNullOrEmpty() && !p0.isNullOrEmpty()){
                sendSMS(mobileNumber!!,p0)
            }

        }

    }

    private fun sendSMS(phoneNumber: String, text: String) {
        LOGS.i("QuickReply $phoneNumber : $text ")
        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.SEND_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                LOGS.i("QuickReply has no permission ")


                iQueryDataCallback?.onQueryDataReceived(
                    QueryCallback.UpdateCallStatus(
                        false, true
                    )
                )
                AppLogs.sendAppLogs("sent request for quick reply sms")
            } else {
                LOGS.i("QuickReply has permission ")
                iQueryDataCallback?.onQueryDataReceived(
                    QueryCallback.UpdateCallStatus(
                        false
                    )
                )
                AppLogs.sendAppLogs("sent request for quick reply sms")

//                val smsManager: SmsManager =
//                    context.getSystemService(SmsManager::class.java)
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, text, null, null)

            }

        } catch (e: Exception) {
            AppLogs.sendAppLogs("Quick reply error")
            LOGS.i("QuickReply error")
            e.printStackTrace()
        }
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

//        var isMusicOn = false
        val musicName = MusicUtil.getSongName(songName)

//        if (MusicUtil.isMusicActive()) {
//            isMusicOn = true
//
//        }
        if (previousVolume == cur) {
            return
        }
        previousVolume = cur



        LOGS.d("Current_volume $sb2value $cur")
        syncMusic(musicName, sb2value, cur)
    }


    private val crpPhoneOperationListener = CRPPhoneOperationListener { event ->
//cf2
        when (event.toByte()) {

            CRPPhoneOperationType.MUSIC_PLAY_OR_PAUSE -> {
                musicPlayPause()
            }
            CRPPhoneOperationType.MUSIC_NEXT -> {
                setMusicPlayerState()
                nextPreviousSong(true)
            }
            CRPPhoneOperationType.MUSIC_PREVIOUS -> {
                setMusicPlayerState()
                nextPreviousSong(false)
            }
            CRPPhoneOperationType.VOLUME_DOWN -> {
                updateVolume(false)
            }
            CRPPhoneOperationType.VOLUME_UP -> {
                updateVolume(true)
            }
            CRPPhoneOperationType.MUSIC_PLAY -> musicPlayPause()
            CRPPhoneOperationType.MUSIC_PAUSE -> musicPlayPause()
            CRPPhoneOperationType.REJECT_INCOMING -> {
                iQueryDataCallback?.onQueryDataReceived(QueryCallback.UpdateCallStatus(false))
            }

            else -> {
            }
        }
    }

    override fun getCustomReplies() {
        quickReplyCount = 0
        customReplyDataList.clear()
        LOGS.d("updateCustomReply getCustomReplies")
        bleConnection?.queryQuickResponsesCount()
    }

    override fun setVolume(currentVolume: Int, maxVolume: Int) {
        //  val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (maxVolume == 0) {
            return
        }
        if (previousVolume == currentVolume) {
            return
        }
        previousVolume = currentVolume

        bleConnection?.sendCurrentVolume(currentVolume)
        bleConnection?.sendMaxVolume(maxVolume)


    }

    private val crpFindPhoneListener = object : CRPFindPhoneListener {
        override fun onFindPhoneComplete() {

            iQueryDataCallback?.onQueryDataReceived(
                QueryCallback.OpenFindMyPhoneActivity(
                    false
                )
            )
        }

        override fun onFindPhone() {
            iQueryDataCallback?.onQueryDataReceived(
                QueryCallback.OpenFindMyPhoneActivity(
                    true
                )
            )
        }

    }

    private fun nextPreviousSong(nextSong: Boolean) {
        if (nextSong) {
            iQueryDataCallback?.onQueryDataReceived(
                QueryCallback.OnMusicEventChanged(
                    MusicControlActionsEvents.NEXT
                )
            )
        } else {
            iQueryDataCallback?.onQueryDataReceived(
                QueryCallback.OnMusicEventChanged(
                    MusicControlActionsEvents.PREVIOUS
                )
            )
        }


    }


    override fun getScreenAwakeInterval() {
        LOGS.d("getScreenAwakeInterval inside")
        bleConnection?.queryDisplayTime(object : CRPDeviceDisplayTimeCallback {
            override fun onSupportAlwayOn(p0: Boolean) {

            }

            override fun onDisplayTime(p0: Int) {
                LOGS.d("getScreenAwakeInterval $p0")
                iQueryDataCallback?.onQueryDataReceived(
                    QueryCallback.ScreenAwakeIntervalObtained(
                        p0
                    )
                )
            }

        })

    }

    override fun getContactList() {

        iQueryDataCallback?.onQueryDataReceived(
            QueryCallback.ContactListObtained(
                dataConverter.formatContactList(watchDataStore.getContactNumberList())
            )
        )
    }

    private fun returnVolume(): Pair<Int, Int> {
        val audioManager =
            NoisefitApplication.context?.applicationContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val cur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return Pair(cur, max)
    }

    private fun setMusicPlayerState() {
//        var isMusicOn = false
        val musicName = MusicUtil.getSongName(songName)

//        if (MusicUtil.isMusicActive()) {
//            isMusicOn = true
//
//        }

        val volume = returnVolume()
        syncMusic(musicName, volume.second, volume.first)


    }

    private fun musicPlayPause() {
        var isMusicOn = false
        if (MusicUtil.isMusicActive()) {
            isMusicOn = true
        }
        val volume = returnVolume()

        syncMusic(MusicUtil.getSongName(songName), volume.second, volume.first)

        if (isMusicOn) {

            bleConnection?.setMusicPlayerState(CRPMusicPlayerStateType.MUSIC_PLAYER_PAUSE)
            iQueryDataCallback?.onQueryDataReceived(
                QueryCallback.OnMusicEventChanged(
                    MusicControlActionsEvents.PAUSE
                )
            )
        } else {

            bleConnection?.setMusicPlayerState(CRPMusicPlayerStateType.MUSIC_PLAYER_PLAY)
            iQueryDataCallback?.onQueryDataReceived(
                QueryCallback.OnMusicEventChanged(
                    MusicControlActionsEvents.PLAY
                )
            )
        }

    }

    private fun syncMusic(musicName: String, maxVolume: Int, currentVolume: Int) {
        bleConnection?.sendSongTitle(musicName)
        bleConnection?.sendCurrentVolume(currentVolume)
        bleConnection?.sendMaxVolume(maxVolume)
    }


    override fun setMusicStatus(status: Int, title: String?, sec: Int?) {

        songName = title
        val volume = returnVolume()

        when (status) {
            3 -> {
                bleConnection?.setMusicPlayerState(CRPMusicPlayerStateType.MUSIC_PLAYER_PAUSE)
            }
            2 -> {
                bleConnection?.setMusicPlayerState(CRPMusicPlayerStateType.MUSIC_PLAYER_PAUSE)
            }
            1 -> {
                bleConnection?.setMusicPlayerState(CRPMusicPlayerStateType.MUSIC_PLAYER_PLAY)
            }
        }

        syncMusic(MusicUtil.getSongName(songName), volume.second, volume.first)


    }

    override fun getVibrationIntensity() {

        bleConnection?.queryVibrationStrength {
            iQueryDataCallback?.onQueryDataReceived(
                QueryCallback.VibrationIntensityObtained(
                    VibrationIntensity(dataConverter.parseVibration(it))
                )
            )
        }
    }
    private fun getIconBuzzVersion(version: String) {

        //only for old icon buzz firmware
        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.NOISE_ICON_BUZZ.deviceType ||
                deviceType == DeviceType.NOISE_ICON_PLUS.deviceType
            ) {
                try {

                    val versionArray = version.split("-")
                    if (versionArray.size == 3) {
                        val iconBuzzVersion =
                            versionArray[versionArray.size - 1].replace(".", "").toInt()
                        if (!version.contains("HAO", true) && iconBuzzVersion >= 232) {
                            CommonGlobals.hasIconBuzzWatchFaces = true
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }
    }

    override fun queryFirmwareVersion() {
        bleConnection?.queryFrimwareVersion { version ->
            LOGS.d("Firmware version ${version}")

            getIconBuzzVersion(version)

            CommonGlobals.version = version
            val deviceFirmware = DeviceFirmware(status = "firmware_version", version = version)
            iQueryDataCallback?.onQueryDataReceived(
                QueryCallback.FirmwareVersionObtained(
                    deviceFirmware = deviceFirmware
                )
            )
            WatchInfoGlobals.firmwareVersion = version
//            WatchInfoGlobals.firmwareVersionNumber = mDeviceInfo.deviceVersionNumber
//            WatchInfoGlobals.firmwareDeviceId = mDeviceInfo.deviceType
            watchDataStore.logWatchInfo(" F_VERSION : ${WatchInfoGlobals.firmwareVersion}")

            iQueryDataCallback?.onQueryDataReceived(
                QueryCallback.FirmwareVersionObtained(DeviceFirmware(version = WatchInfoGlobals.firmwareVersion))
            )
        }
    }

    override fun getWeatherSwitchStatus() {
        iQueryDataCallback?.onQueryDataReceived(
            QueryCallback.WeatherSwitchStatus(
                SwitchSetting(
                    false
                )
            )
        )
    }

    override fun getLanguage() {
        bleConnection?.queryDeviceLanguage { type, _ ->
            iQueryDataCallback?.onQueryDataReceived(
                QueryCallback.LanguageReceived(
                    Language(
                        language = when (type) {
                            CRPDeviceLanguageType.LANGUAGE_CHINESE.toInt() -> DeviceLanguage.CHINESE.type
                            CRPDeviceLanguageType.LANGUAGE_ENGLISH.toInt() -> DeviceLanguage.ENGLISH.type
                            else -> null
                        }
                    )
                )
            )

        }
    }

    override fun queryFirmwareUpgrade() {
        bleConnection?.checkFirmwareVersion(
            object : CRPDeviceNewFirmwareVersionCallback {
                override fun onNewFirmwareVersion(info: CRPFirmwareVersionInfo) {
                    val version = info.version
                    val deviceFirmware =
                        DeviceFirmware(status = "update_available", version = version)
                    iQueryDataCallback?.onQueryDataReceived(
                        QueryCallback.FirmwareUpgradeAvailable(deviceFirmware)
                    )
                }

                override fun onLatestVersion() {
                    val deviceFirmware = DeviceFirmware(status = "update_not_available")
                    iQueryDataCallback?.onQueryDataReceived(
                        QueryCallback.FirmwareUpgradeAvailable(deviceFirmware)
                    )
                }
            },
            CommonGlobals.version,
            CRPFirmwareUpgradeType.NORMAL_UPGEADE_TYPE
        )
    }

    override fun queryFirmwareUpgradeNew(colorFitDevice: ColorFitDevice) {
        bleConnection?.checkFirmwareVersion(
            object : CRPDeviceNewFirmwareVersionCallback {
                override fun onNewFirmwareVersion(info: CRPFirmwareVersionInfo) {
                    val version = info.version
                    val deviceFirmware =
                        DeviceFirmware(status = "update_available", version = version)
                    iQueryDataCallback?.onQueryDataReceived(
                        QueryCallback.FirmwareUpgradeAvailableNew(deviceFirmware)
                    )
                }

                override fun onLatestVersion() {
                    val deviceFirmware = DeviceFirmware(status = "update_not_available")
                    iQueryDataCallback?.onQueryDataReceived(
                        QueryCallback.FirmwareUpgradeAvailableNew(deviceFirmware)
                    )
                }
            },
            CommonGlobals.version,
            CRPFirmwareUpgradeType.NORMAL_UPGEADE_TYPE
        )
    }

    override fun queryBatteryPower() {
        bleConnection?.subscribeDeviceBattery()
        bleConnection?.setDeviceBatteryListener(batteryListener)
    }

    private val batteryListener = object : CRPDeviceBatteryListener {
        override fun onSubscribe(p0: Boolean) {
            bleConnection?.queryDeviceBattery()
        }

        override fun onDeviceBattery(p0: Int) {

            var batteryPercentage = p0
            if (p0 > 100) {
                batteryPercentage = 100
            }
            iQueryDataCallback?.onQueryDataReceived(
                QueryCallback.BatteryDataObtained(
                    BatteryData(percentage = batteryPercentage)
                )
            )

        }

    }

    override fun getUserInfo() {

    }


    override fun setDevice(device: ColorFitDevice) {
        colorFitDevice = device
    }

    override fun <T> callbackListener(callback: T) {

    }

    override fun getAlarms() {
        bleConnection?.queryAllAlarm(object : CRPAlarmCallback {
            override fun onAlarmList(p0: MutableList<CRPAlarmInfo>?) {
                iQueryDataCallback?.onQueryDataReceived(
                    QueryCallback.AlarmsObtained(dataConverter.getAlarmList(p0))
                )
            }

            override fun onNewAlarmList(p0: MutableList<CRPAlarmInfo>?) {

            }

        })

    }

    override fun getDoNotDisturbData() {
        val dnd = DoNotDisturb()
        bleConnection?.queryDoNotDistrubTime { i, crpPeriodTimeInfo ->

            when (i) {
                CRPDevicePeriodTimeCallback.DO_NOT_DISTRUB_TYPE -> {
                    dnd.status =
                        (crpPeriodTimeInfo.endHour == 0 && crpPeriodTimeInfo.endMinute == 0).not()
                    iQueryDataCallback?.onQueryDataReceived(
                        QueryCallback.DoNotDisturbObtained(dnd)
                    )
                }

            }
        }
    }


    override fun getUPIQRCode() {
        bleConnection?.queryElectronicCardCount { cRPElectronicCardCountInfo ->
            LOGS.d("getQRPaymentgetQRPayment ${cRPElectronicCardCountInfo}")
            if (cRPElectronicCardCountInfo.savedIdList.isEmpty()) {
                iQueryDataCallback?.onQueryDataReceived(
                    QueryCallback.UPIQRCodeObtained(
                        null
                    )
                )
            } else {
                val response = ArrayList<UPIQRCode>()
                cRPElectronicCardCountInfo.savedIdList.forEach {
                    bleConnection?.queryElectronicCard(
                        it
                    ) { p0 ->
                        response.add(dataConverter.parseQRPayment(p0))

                    }
                }

                Thread {
                    var isSame = false
                    while (!isSame) {
                        if (response.size == cRPElectronicCardCountInfo.savedIdList.size) {
                            isSame = true
                            iQueryDataCallback?.onQueryDataReceived(
                                QueryCallback.UPIQRCodeObtained(
                                    response
                                )
                            )
                        }
                        Thread.sleep(200L)
                    }
                }.start()
            }
        }

    }

    override fun getWalkReminderData() {

        bleConnection?.querySedentaryReminder { status ->
            bleConnection?.querySedentaryReminderPeriod { info ->
                LOGS.d("Update walk reminder - query")
                iQueryDataCallback?.onQueryDataReceived(
                    QueryCallback.WalkReminderDataObtained(
                        dataConverter.getSedentaryReminderPeriod(status, info)
                    )
                )
            }
        }

    }

    override fun getMenstrualSettings() {
        bleConnection?.queryPhysiologcalPeriod { crpDevicePhysiologcal ->

            iQueryDataCallback?.onQueryDataReceived(
                QueryCallback.MenstrualSettingsObtained(
                    dataConverter.getMenstrualSettings(crpDevicePhysiologcal)
                )
            )
        }
    }

    override fun getHeartRateInterval() {
        bleConnection?.queryTimingMeasureHeartRate { p0 ->
            val interval = p0 * 5
            iQueryDataCallback?.onQueryDataReceived(
                QueryCallback.HeartRateIntervalObtained(
                    HeartRateInterval(
                        interval = interval,
                        status = watchDataStore.getHeartRateStatus()
                    )
                )
            )
        }

    }


    override fun getSpo2Settings() {

        //multiply interval by 5
        val data = watchDataStore.getBloodOxygenStatus()
        val interval = if (data.second != null) {
            data.second!! * 5
        } else {
            5
        }

        iQueryDataCallback?.onQueryDataReceived(
            QueryCallback.Spo2SettingsObtained(
                Spo2Data(
                    status = data.first,
                    interval = interval

                )
            )
        )
    }

    private fun getWatchFaces(
        info: CRPSupportWatchFaceInfo,
        version: String,
        pageIndex: Int,
        success: (watchFaceList: ArrayList<WatchFace>, totalWCount: Int) -> Unit,
        failed: () -> Unit
    ) {

        bleConnection?.queryWatchFaceStore(
            info.supportWatchFaceList, version, 100, pageIndex,
            object : CRPDeviceWatchFaceStoreCallback {
                override fun onWatchFaceStoreChange(p0: CRPWatchFaceStoreInfo?) {
                    if (p0 != null) {
                        LOGS.d("FETCHING_WATCH_FACE get data ${p0.total}")
                        return success.invoke(
                            dataConverter.convertWatchStoreToWatchFacesList(p0),
                            p0.total
                        )
                    }
                }

                override fun onError(s: String) {
                    LOGS.d("FETCHING_WATCH_FACE Error getting watch faces $s")
                    return failed.invoke()
                }
            })

    }


    override fun getWatchFaces() {

        bleConnection?.querySupportWatchFace(object : CRPDeviceSupportWatchFaceCallback {
            override fun onSupportWatchFace(info: CRPSupportWatchFaceInfo) {
                wFaceIndex = 1
                val watchFaceList = ArrayList<WatchFace>()
                bleConnection?.queryFrimwareVersion { version ->
                    CommonGlobals.version = version
                    GlobalScope.launch {
                        fetchSupplierWatchFace(info, version, watchFaceList)
                    }

                }
            }

            override fun onSifliSupportWatchFace(p0: CRPSifliSupportWatchFaceInfo?) {

            }

        })

    }


    private fun fetchSupplierWatchFace(
        info: CRPSupportWatchFaceInfo,
        version: String,
        watchFaceList: ArrayList<WatchFace>
    ) {

        getWatchFaces(info, version, wFaceIndex, success = { list, totalWCount ->
            watchFaceList.addAll(list)
            if (watchFaceList.size == totalWCount) {
                LOGS.d("FETCHING_WATCH_FACE count: ${totalWCount}")
                iQueryDataCallback?.onQueryDataReceived(
                    QueryCallback.WatchFacesObtained(
                        watchFaceList
                    )
                )
            } else {


                wFaceIndex += 1
                LOGS.d("FETCHING_WATCH_FACE index: ${wFaceIndex}")
                fetchSupplierWatchFace(info, version, watchFaceList)

            }

        }, failed = {
            getWatchFaces()
        })

    }

    override fun getStressSettings() {
        bleConnection?.queryTimingStressState()
    }

    override fun getWatchFaceLayout() {
        val watchFaceLayout = WatchFaceLayout()
        bleConnection?.queryWatchFaceLayout { info ->
            when (info.timePosition) {
                CRPWatchFaceLayoutType.WATCH_FACE_TIME_TOP -> watchFaceLayout.timePosition = "top"
                else -> watchFaceLayout.timePosition = "bottom"
            }

            when (info.timeTopContent) {
                CRPWatchFaceLayoutType.WATCH_FACE_CONTENT_STEP -> watchFaceLayout.timeTopContent =
                    "step"
                CRPWatchFaceLayoutType.WATCH_FACE_CONTENT_SLEEP -> watchFaceLayout.timeTopContent =
                    "sleep"
                CRPWatchFaceLayoutType.WATCH_FACE_CONTENT_HEART_RATE -> watchFaceLayout.timeTopContent =
                    "heart_rate"
                else -> watchFaceLayout.timeTopContent = "step"
            }

            when (info.timeBottomContent) {
                CRPWatchFaceLayoutType.WATCH_FACE_CONTENT_STEP -> watchFaceLayout.timeBottomContent =
                    "step"
                CRPWatchFaceLayoutType.WATCH_FACE_CONTENT_SLEEP -> watchFaceLayout.timeBottomContent =
                    "sleep"
                CRPWatchFaceLayoutType.WATCH_FACE_CONTENT_HEART_RATE -> watchFaceLayout.timeBottomContent =
                    "heart_rate"
                else -> watchFaceLayout.timeBottomContent = "step"
            }

//            watchFaceLayout.textColor = when (info.textColor) {
//                0 -> "#000000"
//                else -> "#ffffff"
//            }

            iQueryDataCallback?.onQueryDataReceived(
                QueryCallback.WatchFaceLayoutObtained(
                    watchFaceLayout
                )
            )
        }
    }

    override fun getSedentaryData() {
        bleConnection?.querySedentaryReminder { status ->
            bleConnection?.querySedentaryReminderPeriod { info ->
                iQueryDataCallback?.onQueryDataReceived(
                    QueryCallback.SedentaryDataObtained(
                        SedentaryData(
                            status = status,
                            startHour = info.startHour.toInt(),
                            endHour = info.endHour.toInt(),
                            interval = info.period.toInt()
                        )
                    )
                )
            }
        }
    }


    override fun getUserGoals() {
        bleConnection?.queryGoalStep { stepGoal ->
            QueryCallback.UserGoalsObtained(UserGoals(stepGoal = stepGoal))
        }
    }


    override fun getWristLiftGesture() {
        bleConnection?.queryQuickView { var1 ->
            iQueryDataCallback?.onQueryDataReceived(
                QueryCallback.WristLiftGestureObtained(WristLiftGesture(status = var1))
            )
        }
    }

    override fun <T> callbackListenerNew(callback: T) {
        iQueryDataCallback = callback as IQueryDataCallback
    }

}