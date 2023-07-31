package com.noisefit.hybrid.handler

import android.content.Context
import android.media.AudioManager
import cn.appscomm.bluetooth.mode.Customize
import cn.appscomm.bluetoothsdk.app.BluetoothSDK
import cn.appscomm.bluetoothsdk.app.SettingType
import cn.appscomm.bluetoothsdk.interfaces.ResultCallBack
import cn.appscomm.bluetoothsdk.model.CustomizeReply
import cn.appscomm.bluetoothsdk.model.SwitchType
import com.noisefit.hybrid.base.VisionCommands
import com.noisefit.hybrid.dataconversions.DataConverter
import com.noisefit.hybrid.utils.BitwiseHelperUtils
import com.noisefit.hybrid.utils.BitwiseUtils
import com.noisefit.hybrid.utils.BluetoothSDK_Exp
import com.noisefit_commans.NoisefitApplication
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
import org.json.JSONObject
import javax.inject.Inject


//reminder alert in minutes
class NFHQueryDeviceUnitsHandler @Inject constructor(
    private val bitwiseUtils: BitwiseUtils,
    private val dataConverter: DataConverter,
    private val bitwiseHelperUtils: BitwiseHelperUtils,
    private val watchDataStore: WatchDataStore,
    private val visionCommands: VisionCommands,
    private val context: Context,
    private val bluetoothsdkExp: BluetoothSDK_Exp,
) : QueryDeviceDataActions() {

    companion object {

        var alarmsList: AlarmsList? = null
        var remindersList: ReminderList? = null
    }

    private var previousVolume = 0
    private var songName: String? = null
    private var testQueryDeviceDataCallback: IQueryDataCallback? = null
    private var colorFitDevice: ColorFitDevice? = null

    override fun <T> callbackListener(callback: T) {}

    override fun <T> callbackListenerNew(callback: T) {
        testQueryDeviceDataCallback = callback as IQueryDataCallback
    }

    override fun setDevice(colorFitDevice: ColorFitDevice) {
        this.colorFitDevice = colorFitDevice
    }

    override fun init() {

    }

    override fun attachCallbacks() {
        BluetoothSDK.setFindPhoneCallBack(resultCallBack)
        BluetoothSDK.setMusicCallBack(resultCallBack)
        BluetoothSDK.setVolumeCallBack(resultCallBack)
        BluetoothSDK.setTakePhotoCallBack(resultCallBack)
    }

    override fun queryFirmwareVersion() {
        BluetoothSDK.getDeviceVersion(resultCallBack)
    }

    override fun getDoNotDisturbData() {
        BluetoothSDK.getDoNotDisturb(resultCallBack)
    }

    override fun queryBatteryPower() {
        BluetoothSDK.getBatteryPower(resultCallBack)
    }

    override fun getUserInfo() {
        BluetoothSDK.getUserInfo(resultCallBack)
    }

    override fun getLanguage() {
        BluetoothSDK.getLanguage(resultCallBack)
    }

    /*override fun getSwitchSettings() {
        BluetoothSDK.getSwitchSetting(resultCallBack)
    }*/

    override fun queryFirmwareUpgrade() {
        /* val newVersion = FirmwareUpgradeHandler.checkForNewVersion(
             productCode = when (colorFitDevice.deviceType) {
                 DeviceType.NOISEFIT_HYBRID.deviceType -> "W007HA"
                 else -> "L42A+_GoNoise"
             }
         )
         if (NFHybridGlobals.firmwareVersion != null)
             queryDeviceDataCallbacks?.onFirmwareUpgradeAvailable(getFirmwareUpdate(newVersion))*/
    }

    override fun queryFirmwareUpgradeNew(colorFitDevice: ColorFitDevice) {
        /* val newVersion = FirmwareUpgradeHandler.checkForNewVersion(
             productCode = when (colorFitDevice.deviceType) {
                 DeviceType.NOISEFIT_HYBRID.deviceType -> "W007HA"
                 else -> "L42A+_GoNoise"
             }
         )
         if (NFHybridGlobals.firmwareVersion != null)
             queryDeviceDataCallbacks?.onFirmwareUpgradeAvailableNew(getFirmwareUpdate(newVersion))*/
    }

    override fun getCustomReplies() {
        try {
            colorFitDevice?.let {
                if (it.deviceType == DeviceType.COLORFIT_VISION.deviceType) {

                    bluetoothsdkExp.getCustomizeReply(object :
                        BluetoothSDK_Exp.CustomReplyCallback() {
                        override fun onFail(code: Int) {
                            AppLogs.sendAppLogs("Failed to fetch custom replies")
                            set8002CallbackNull()
                            testQueryDeviceDataCallback?.onQueryDataReceived(
                                QueryCallback.CustomReplyObtained(
                                    CustomReplyData(ArrayList())
                                )
                            )
                        }

                        override fun onSuccess(customizeReplyList: List<Customize>?) {
                            val customReplyData = CustomReplyData(customReplies = ArrayList())

                            customizeReplyList?.forEach { customReplyItem ->
                                val customReply = CustomReplyData.CustomReply(
                                    content = customReplyItem.content,
                                    index = customReplyItem.index - 1,
                                    crc = customReplyItem.crc
                                )
                                customReplyData.customReplies.add(customReply)
                            }

                            set8002CallbackNull()


                            testQueryDeviceDataCallback?.onQueryDataReceived(
                                QueryCallback.CustomReplyObtained(
                                    customReplyData
                                )
                            )

                            AppLogs.sendAppLogs("Success to fetch custom replies")
                        }
                    })
                } else {
                    BluetoothSDK.getCustomizeReply(resultCallBack)
                }
            }
        } catch (e: Exception) {
            set8002CallbackNull()
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.CustomReplyObtained(
                    CustomReplyData(ArrayList())
                )
            )
        }
        //
    }


    override fun setMusicStatus(status: Int, title: String?, sec: Int?) {

        var songPlaying = false
        if (status == 1) {
            songPlaying = true
        }

        songName = title
        BluetoothSDK.sendSongName(songPlaying, MusicUtil.getSongName(songName))
        AppLogs.sendAppLogs("Set music title")

    }

    override fun getReminders() {
        when (colorFitDevice?.deviceType) {
            DeviceType.NOISEFIT_HYBRID.deviceType -> {
                BluetoothSDK.getReminder(object : ResultCallBack {
                    override fun onSuccess(p0: Int, p1: Array<out Any>?) {
                        AppLogs.sendAppLogs(
                            "noise_fit_event:rn_command : Reminder | $p0 | ) $p1"
                        )
                        remindersList = p1?.let {
                            dataConverter.formatReminderData(it)
                        }
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.RemindersObtained(
                                remindersList ?: ReminderList()
                            )
                        )
                    }

                    override fun onFail(p0: Int) {
                        AppLogs.sendAppLogs("Reminder data fetch failed")
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.RemindersObtained(ReminderList())
                        )
                    }
                }, SettingType.REMINDER_PROTOCOL_BASE_WITH_DATE)
            }
            DeviceType.COLORFIT_NAV.deviceType -> {
                BluetoothSDK.getReminderEx(object : ResultCallBack {
                    override fun onSuccess(p0: Int, p1: Array<out Any>?) {
                        AppLogs.sendAppLogs(
                            "noise_fit_event:rn_command : Reminder | $p0 | ) $p1"
                        )
                        remindersList = p1?.let {
                            dataConverter.formatNavReminderData(it)
                        }
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.RemindersObtained(
                                remindersList ?: ReminderList()
                            )
                        )
                    }

                    override fun onFail(p0: Int) {
                        AppLogs.sendAppLogs("Reminder data fetch failed")
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.RemindersObtained(ReminderList())
                        )
                    }
                }, SettingType.REMINDER_EX_CHECK_TYPE_GET_REMIND_DATA)
            }
            DeviceType.COLORFIT_VISION.deviceType -> {
                BluetoothSDK.getReminderEx(object : ResultCallBack {
                    override fun onSuccess(p0: Int, p1: Array<out Any>?) {
                        AppLogs.sendAppLogs(
                            "noise_fit_event:rn_command : Reminder | $p0 | ) $p1"
                        )
                        remindersList = p1?.let {
                            dataConverter.formatVisionReminderData(it)
                        }
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.RemindersObtained(
                                remindersList ?: ReminderList()
                            )
                        )
                    }

                    override fun onFail(p0: Int) {
                        AppLogs.sendAppLogs("Reminder data fetch failed")
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.RemindersObtained(ReminderList())
                        )
                    }
                }, SettingType.REMINDER_EX_CHECK_TYPE_GET_REMIND_DATA)
            }
        }
    }

    override fun getAlarms() {
        AppLogs.sendAppLogs("Fetch Alarm data")
        when (colorFitDevice?.deviceType) {
            DeviceType.NOISEFIT_HYBRID.deviceType -> BluetoothSDK.getReminder(
                resultCallBack,
                SettingType.REMINDER_PROTOCOL_BASE_WITH_DATE
            )
            else -> BluetoothSDK.getReminderEx(
                resultCallBack,
                SettingType.REMINDER_EX_CHECK_TYPE_GET_ALARM_DATA
            )
        }
    }

    /*override fun getSwitchSetting() {
        BluetoothSDK.getSwitchSetting(resultCallBack)
    }*/

    override fun getAutoSleep() {
        BluetoothSDK.getAutoSleep(resultCallBack)
    }

    override fun getHeartRateInterval() {
        BluetoothSDK.getAutoHeartRateFrequency(resultCallBack)
    }

    override fun getHeartRateAlert() {
        BluetoothSDK.getHeartRateAlarmThreshold(resultCallBack)
    }

    override fun getSedentaryData() {
        BluetoothSDK.getInactivityAlert(resultCallBack)
    }

    override fun getDeviceUnits() {
        BluetoothSDK.getUnit(resultCallBack)
    }

    override fun getScreenAwakeInterval() {
        BluetoothSDK.getBrightScreenTime(resultCallBack)
    }

    private fun set8002CallbackNull() {
        BluetoothSDK.set8002CallBack(null)
    }

    override fun getHandwashData() {
        AppLogs.sendAppLogs("Fetch Hand wash Data")
        when (colorFitDevice?.deviceType) {
            DeviceType.COLORFIT_VISION.deviceType -> {
                var isHandResponseFirst = false
                var drinkHandResponse = ""



                BluetoothSDK.set8002CallBack(object : ResultCallBack {
                    override fun onSuccess(i: Int, objects: Array<Any>) {
                        val bytes = objects[0] as ByteArray
                        val hexString = bitwiseHelperUtils.bytesArrayResult(objects)

                        if (hexString.contains(visionCommands.HANDWASH_RESPONSE) || isHandResponseFirst) {

                            drinkHandResponse += " " + bitwiseUtils.byteArrayToHexString(bytes)
                            if (isHandResponseFirst) {
                                isHandResponseFirst = false

                                // clear callback
                                set8002CallbackNull()
                                testQueryDeviceDataCallback?.onQueryDataReceived(
                                    QueryCallback.GetHandWashing(
                                        bitwiseHelperUtils.parseHandWashResponse(
                                            drinkHandResponse.trim()
                                        )
                                    )
                                )

                                //0x6F, 0x9D, 0x71, 0x15, 0x00, 0x01, 0x01, 0x00, 0x02, 0x06, 0x00, 0x01, 0x02, 0x15, 0x00, 0x02, 0x02, 0xEC, 0x13, 0x03, 0x02, 0x00, 0x32, 0x04, 0x01, 0x85, 0x8F,

                                drinkHandResponse = ""
                            } else {
                                isHandResponseFirst = true
                            }


                            return
                        }


                    }

                    override fun onFail(i: Int) {
                        set8002CallbackNull()
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.GetHandWashing(
                                HandWashing()
                            )
                        )

                        AppLogs.sendAppLogs("Error:: getHandwashData 8002 recv fail")
                    }
                })
                visionCommands.sendCommand(visionCommands.HANDWASH_QUERY_CMD)

            }
            else -> {
                BluetoothSDK.getHandWashing(object : ResultCallBack {
                    override fun onSuccess(p0: Int, p1: Array<out Any>?) {

                        p1?.let {
                            // LOGS.d("noise_fit_event:rn_command", "getHandWashing | $p0 | ${Gson().toJson(p1)})")
                            testQueryDeviceDataCallback?.onQueryDataReceived(
                                QueryCallback.GetHandWashing(
                                    HandWashing(
                                        startHour = it[1] as Int,
                                        startMinute = it[2] as Int,
                                        endHour = it[3] as Int,
                                        endMinute = it[4] as Int,
                                        frequency = (it[5] as Int / 60),
                                        duration = it[6] as Int,
                                        startWash = it[0] as Boolean
                                    )
                                )
                            )
                        }
                    }

                    override fun onFail(p0: Int) {}
                });


            }
        }
    }

    override fun getDrinkWaterSettings() {
        AppLogs.sendAppLogs("getDrinkWaterSettings")
        var isDrinkResponseFirst = false
        var drinkHandResponse: String = ""



        BluetoothSDK.set8002CallBack(object : ResultCallBack {
            override fun onSuccess(i: Int, objects: Array<Any>) {
                val bytes = objects[0] as ByteArray
                val hexString = bitwiseHelperUtils.bytesArrayResult(objects)

                if (hexString.contains(visionCommands.DRINK_RESPONSE) || isDrinkResponseFirst) {
                    drinkHandResponse += " " + bitwiseUtils.byteArrayToHexString(bytes)
                    if (isDrinkResponseFirst) {
                        isDrinkResponseFirst = false
                        LOGS.d(
                            "setDrinkWaterReminder 1 ::  ${
                                bitwiseHelperUtils.parseDrinkResponse(
                                    drinkHandResponse.trim()
                                )
                            }"
                        )
                        // clear callback
                        set8002CallbackNull()
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.DrinkWaterDataObtained(
                                bitwiseHelperUtils.parseDrinkResponse(
                                    drinkHandResponse.trim()
                                )
                            )
                        )

                        drinkHandResponse = ""
                    } else {
                        isDrinkResponseFirst = true
                    }

                    return
                }
                AppLogs.sendAppLogs("Success:: getDrinkWaterSettings 8002 receive ")


            }

            override fun onFail(i: Int) {
                set8002CallbackNull()
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.DrinkWaterDataObtained(
                        SedentaryData()
                    )
                )
                AppLogs.sendAppLogs("Error:: getDrinkWaterSettings 8002 receive fail")
            }
        })
        visionCommands.sendCommand(visionCommands.DRINK_QUERY_CMD)
    }

    override fun getWorldClock() {
        val worldClockList = watchDataStore.getWorldClockData()
        val data = ArrayList<WorldClockList.WClock>()
        worldClockList?.let {

            worldClockList.worldClocks.forEach { item ->
                data.add(
                    WorldClockList.WClock(
                        timeZone = item.timeZone,
                        content = item.content
                    )
                )
            }

        }
        LOGS.d("world clock data $data")
        AppLogs.sendAppLogs("Fetch world clock data")
        testQueryDeviceDataCallback?.onQueryDataReceived(
            QueryCallback.WorldClockDataObtained(WorldClockList(worldClocks = data))
        )
    }


    override fun getWatchPassword() {
        BluetoothSDK.set8002CallBack(object : ResultCallBack {
            override fun onSuccess(i: Int, objects: Array<Any>) {
                LOGS.d("WATCH PASSWORD OBTAINED 12 ${bitwiseHelperUtils.bytesArrayResult(objects)}")
                set8002CallbackNull()
                if (bitwiseHelperUtils.bytesArrayResult(objects)
                        .contains(visionCommands.QUERY_PASSWORD_RESPONSE)
                ) {
                    AppLogs.sendAppLogs("WATCH PASSWORD OBTAINED")
                    // clear callback

                    val password =
                        bitwiseHelperUtils.getPassword(bitwiseHelperUtils.bytesArrayResult(objects))
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.WatchPasswordObtained(password)
                    )

                }
            }

            override fun onFail(i: Int) {
                set8002CallbackNull()
                AppLogs.sendAppLogs("WATCH PASSWORD DENIED")
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.WatchPasswordObtained(WatchPassword())
                )
            }
        })

        AppLogs.sendAppLogs("WATCH PASSWORD sendCommand")
        visionCommands.sendCommand(visionCommands.QUERY_PASSWORD_CMD)

    }

    private var resultCallBack: ResultCallBack = object : ResultCallBack {
        override fun onSuccess(p0: Int, p1: Array<out Any>?) {
            when (p0) {
                ResultCallBack.TYPE_GET_DO_NOT_DISTURB -> {
                    AppLogs.sendAppLogs("Get DND data")
                    p1?.let {
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.DoNotDisturbObtained(
                                DoNotDisturb(status = it[0] as Boolean)
                            )
                        )
                    }
                }
                ResultCallBack.TYPE_GET_BATTERY_POWER -> {
                    AppLogs.sendAppLogs("Get Battery Power data")
                    p1?.let {
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.BatteryDataObtained(
                                BatteryData(percentage = it[0] as Int)
                            )
                        )
                    }
                }
                ResultCallBack.TYPE_GET_BRIGHT_SCREEN_TIME -> {
                    AppLogs.sendAppLogs("Get Screen Time data")
                    p1?.let {
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.ScreenAwakeIntervalObtained(
                                it[0] as Int
                            )
                        )
                    }
                }
                ResultCallBack.TYPE_DEVICE_SET_VOLUME -> {
                    if (!p1.isNullOrEmpty() && p1[0] is Int) {
                        val result = p1[0] as Int
                        //val audioManager = NoisefitApplication.context?.applicationContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        //val sb2value = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        //val voluem = sb2value/20
                        BluetoothSDK.sendVolume(result)
                        AppLogs.sendAppLogs("Set Volume data")
                        updateVolumeNew(result)
                    }
                }
                ResultCallBack.TYPE_DEVICE_SET_VOLUME_INCREASE -> {
                    AppLogs.sendAppLogs("Volume increase")
                    updateVolume(true)
                }
                ResultCallBack.TYPE_DEVICE_SET_VOLUME_REDUCE -> {
                    AppLogs.sendAppLogs("Volume decrease")
                    updateVolume(false)
                }
                ResultCallBack.TYPE_GET_AUTO_SLEEP -> {
                    AppLogs.sendAppLogs("Get auto sleep data")
                    p1?.let {
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.AutoSleepObtained(
                                AutoSleep(
                                    startHour = it[0] as Int,
                                    startMinute = it[1] as Int,
                                    endHour = it[2] as Int,
                                    endMinute = it[3] as Int,
                                    status = when (it[4] as Int) {
                                        0 -> false
                                        else -> true
                                    },
                                    repeat = it[4] as Int,
                                    weeks = dataConverter.getArrayFromInt(it[4] as Int)
                                )
                            )
                        )
                    }
                }
                ResultCallBack.TYPE_GET_USER_INFO -> {
                    AppLogs.sendAppLogs("Get User info data")
                    p1?.let {
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.UserInfoReceived(
                                UserInfo(
                                    gender = when (it[0] as Int) {
                                        1 -> Gender.FEMALE.type
                                        else -> Gender.MALE.type
                                    },
                                    dob = "",
                                    height = it[2] as Int,
                                    weight = (it[3] as Float).toInt()
                                )
                            )
                        )
                    }
                }
                ResultCallBack.TYPE_GET_SWITCH_SETTING -> {
                    p1?.let {
                        val switchType = p1[0] as SwitchType
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.SwitchSettingObtained(
                                SwitchSetting(
                                    settingType = "anti_lost",
                                    status = switchType.antiLostSwitch
                                )
                            )
                        )
                    }
                }
                ResultCallBack.TYPE_GET_AUTO_HEART_RATE_FREQUENCY -> {
                    AppLogs.sendAppLogs("Get auto heart rate frequency data")
                    p1?.let {
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.HeartRateIntervalObtained(
                                when (it[0] as Int) {
                                    0 -> HeartRateInterval(status = false)
                                    else -> HeartRateInterval(
                                        status = true,
                                        interval = it[0] as Int
                                    )
                                }
                            )
                        )
                    }
                }
                ResultCallBack.TYPE_GET_LANGUAGE -> {
                    AppLogs.sendAppLogs("Get language data")
                    p1?.let {
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.LanguageReceived(
                                Language(
                                    language = when (it[0] as String) {
                                        SettingType.LANGUAGE_ZH -> DeviceLanguage.CHINESE.type
                                        else -> DeviceLanguage.ENGLISH.type
                                    }
                                )
                            )
                        )
                    }
                }
                ResultCallBack.TYPE_GET_INACTIVITY_ALERT -> {
                    LOGS.d("noise_fit_event:rn_command alert ${p1?.get(1)}")
                    p1?.let {
                        val interval = p1[2] as Int
                        var repeat = 127
                        if (p1[1] as Int != 0) {
                            repeat = p1[1] as Int
                        }
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.SedentaryDataObtained(
                                SedentaryData(
                                    status = when (p1[0] as Int) {
                                        1 -> true
                                        else -> false
                                    },
                                    interval = (interval),
                                    startHour = p1[3] as Int,
                                    startMinute = p1[4] as Int,
                                    endHour = p1[5] as Int,
                                    endMinute = p1[6] as Int,
                                    repeat = repeat,
                                    repeatDays = AlarmsList.binaryStrToBooleanArrayHybrid(repeat)
                                        .reversed()
                                )
                            )
                        )
                    }
                }
                ResultCallBack.TYPE_DEVICE_CHECK_MUSIC_STATUS -> {
                    AppLogs.sendAppLogs("Set Music player state")
                    setMusicPlayerState()
                }
                ResultCallBack.TYPE_DEVICE_SET_NEXT_SONG -> {
                    setMusicPlayerState()
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.OnMusicEventChanged(
                            MusicControlActionsEvents.NEXT
                        )
                    )
                    AppLogs.sendAppLogs("Next music click callback")
                }
                ResultCallBack.TYPE_DEVICE_START_FIND_PHONE -> {
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.OpenFindMyPhoneActivity(
                            true
                        )
                    )
                    AppLogs.sendAppLogs("Start find phone ringing")
                }
                ResultCallBack.TYPE_DEVICE_END_FIND_PHONE -> {
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.OpenFindMyPhoneActivity(
                            false
                        )
                    )
                    AppLogs.sendAppLogs("End find phone ringing")
                }
                ResultCallBack.TYPE_DEVICE_SET_PRE_SONG -> {
                    setMusicPlayerState()
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.OnMusicEventChanged(
                            MusicControlActionsEvents.PREVIOUS
                        )
                    )
                }
                ResultCallBack.TYPE_DEVICE_SET_PLAY_SONG -> {
                    setPlayPauseMusic(true)
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.OnMusicEventChanged(
                            MusicControlActionsEvents.PLAY
                        )
                    )
                }
                ResultCallBack.TYPE_DEVICE_SET_PAUSE_SONG -> {
                    setPlayPauseMusic(false)
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.OnMusicEventChanged(
                            MusicControlActionsEvents.PAUSE
                        )
                    )
                }
                ResultCallBack.TYPE_GET_DEVICE_VERSION -> {

                    p1?.let {
                        WatchInfoGlobals.firmwareVersion = parseFirmware(p1[0] as String)
                        WatchInfoGlobals.firmwareDeviceId = 331023

                        watchDataStore.logWatchInfo(" F_VERSION : ${WatchInfoGlobals.firmwareVersion} | F_DEVICE_ID : ${WatchInfoGlobals.firmwareDeviceId} ")

                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.FirmwareVersionObtained(
                                getDisplayFirmware(
                                    WatchInfoGlobals.firmwareVersion
                                )
                            )
                        )
                    }
                }
                ResultCallBack.TYPE_GET_REMINDER -> {
                    alarmsList = p1?.let {
                        when (colorFitDevice?.deviceType) {
                            DeviceType.NOISEFIT_HYBRID.deviceType -> dataConverter.formatAlarmData(
                                it
                            )
                            else -> dataConverter.formatNavAlarmData(it)
                        }
                    }
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.AlarmsObtained(
                            alarmsList ?: AlarmsList(
                                alarms = ArrayList<AlarmsList.Alarm>()
                            )
                        )
                    )
                    AppLogs.sendAppLogs("Get reminder data")
                }
                ResultCallBack.TYPE_GET_REMINDER_EX, ResultCallBack.TYPE_NEW_REMINDER_EX -> {
                    alarmsList = p1?.let {
                        when (colorFitDevice?.deviceType) {
                            DeviceType.NOISEFIT_HYBRID.deviceType -> dataConverter.formatAlarmData(
                                it
                            )
                            else -> dataConverter.formatNavAlarmData(it)
                        }
                    }

                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.AlarmsObtained(
                            alarmsList ?: AlarmsList()
                        )
                    )
                }
                ResultCallBack.TYPE_GET_TIME_SURFACE -> {
                    p1?.let { }
                    val units = ArrayList<String>()
                }
                ResultCallBack.TYPE_DEVICE_START_TAKE_PHOTO -> {
                    AppLogs.sendAppLogs("take photo call")
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.ClickCameraImage
                    )
                }
                ResultCallBack.TYPE_DEVICE_END_TAKE_PHOTO -> {
//                    testQueryDeviceDataCallback?.onQueryDataReceived(
//                        QueryCallback.SwitchCameraView(
//                            false
//                        )
//                    )
                }
                ResultCallBack.TYPE_GET_CUSTOMIZE_REPLY -> {
                    p1?.let { items ->
                        val customReplyData = CustomReplyData(customReplies = ArrayList())
                        (items[0] as List<*>).forEach { item ->
                            val customReplyItem = item as CustomizeReply
                            val customReply = CustomReplyData.CustomReply(
                                content = customReplyItem.content,
                                index = customReplyItem.index,
                                crc = customReplyItem.crc
                            )
                            customReplyData.customReplies.add(customReply)
                        }

                        LOGS.d("Quick reply $customReplyData")
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.CustomReplyObtained(
                                customReplyData
                            )
                        )
                        AppLogs.sendAppLogs("Get quick reply data")
                    }
                }
                ResultCallBack.TYPE_GET_HEART_RATE_ALARM_THRESHOLD -> {
                    LOGS.d("noise_fit_event:rn_command $p1")
                    p1?.let {
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.HeartRateAlertDataObtained(
                                HeartRateAlert(
                                    status = when (p1[0] as Int) {
                                        1 -> true
                                        else -> false
                                    }, min_hr = p1[2] as Int, max_hr = p1[1] as Int
                                )
                            )
                        )
                    }
                }
            }
        }

        override fun onFail(p0: Int) {
            AppLogs.sendAppLogs("Query Failed $p0")
            when (p0) {
                ResultCallBack.TYPE_GET_REMINDER_EX -> {
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.AlarmsObtained(
                            AlarmsList(
                                alarms = ArrayList()
                            )
                        )
                    )
                }
                ResultCallBack.TYPE_GET_DO_NOT_DISTURB -> {
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.DoNotDisturbObtained(
                            DoNotDisturb(status = false)
                        )
                    )
                }
            }
        }
    }


    override fun getStressSettings() {


        BluetoothSDK.set8002CallBack(object : ResultCallBack {
            override fun onSuccess(i: Int, objects: Array<Any>) {
                if (bitwiseHelperUtils.bytesArrayResult(objects)
                        .contains(visionCommands.QUERY_RESPONSE)
                ) {
                    // clear callback
                    set8002CallbackNull()
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.StressParamObtained(
                            SedentaryData(
                                status = bitwiseHelperUtils.getStressStatus(
                                    bitwiseHelperUtils.bytesArrayResult(objects)
                                )
                            )
                        )
                    )
                }
                AppLogs.sendAppLogs("Stress setting success")

            }

            override fun onFail(p0: Int) {
                set8002CallbackNull()
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.StressParamObtained(
                        SedentaryData(
                            status = false
                        )
                    )
                )
                AppLogs.sendAppLogs("Stress setting fail")
            }

        })
        visionCommands.sendCommand(visionCommands.STRESS_QUERY_CMD)
    }

    private fun setPlayPauseMusic(isPlay: Boolean) {
        LOGS.d("PLAY SONG $isPlay")
        val musicName = MusicUtil.getSongName(songName)


        BluetoothSDK.sendSongName(isPlay, musicName)

    }

    private fun setMusicPlayerState() {
        var isMusicOn = false
        val musicName = MusicUtil.getSongName(songName)

        if (MusicUtil.isMusicActive()) {
            isMusicOn = true

        }



        BluetoothSDK.sendSongName(isMusicOn, musicName)

    }

    private fun updateVolumeNew(volume: Int) {
        if (volume == 0) {
            val audioManager = NoisefitApplication.context?.applicationContext?.getSystemService(
                Context.AUDIO_SERVICE
            ) as AudioManager
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                volume,
                AudioManager.FLAG_SHOW_UI
            )
        } else {
            var isIncreaseVolume = false
            if (previousVolume <= volume) {
                isIncreaseVolume = true
            }
            val audioManager = NoisefitApplication.context?.applicationContext?.getSystemService(
                Context.AUDIO_SERVICE
            ) as AudioManager
            //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,volume, AudioManager.FLAG_SHOW_UI)
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC, when (isIncreaseVolume) {
                    true -> AudioManager.ADJUST_RAISE
                    else -> AudioManager.ADJUST_LOWER
                }, AudioManager.FLAG_SHOW_UI
            )
            previousVolume = volume
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
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val cur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val offset = 100 / max
        val ss = (offset * cur)
        BluetoothSDK.sendVolume(ss)

    }


    private fun getDisplayFirmware(firmwareVersion: String?): DeviceFirmware {
        firmwareVersion?.let {
            val json = JSONObject(firmwareVersion)
            val version = json.getString("A")
            LOGS.d(
                "noise_fit_event:rn_command",
                "version Check | $firmwareVersion"
            )
            if (json.has("B")) {
                json.getString("B")?.let { b ->
                    val cleanVersion = version.replace(".", "")
                    try {
                        WatchInfoGlobals.firmwareVersionNumber = cleanVersion.toInt()
                        WatchInfoGlobals.firmwareBuildNumber = b.toInt()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    return DeviceFirmware(version = "$version(${b.split(".").last()})")
                }
            }
            return DeviceFirmware(version = version)
        }
        return DeviceFirmware(version = null)
    }

    private fun parseFirmware(firmware: String): String {
        LOGS.d("noise_fit_event:rn_command", "version Check | $firmware")
        val builder = StringBuilder()
        val json = JSONObject()
        var char: String? = null
        for (element in firmware) {
            if (!Character.isLetter(element)) {
                builder.append(element)
                json.put(char, builder.toString())
            } else {
                char = element.toString()
                builder.setLength(0)
            }
        }
        return json.toString()
    }
}