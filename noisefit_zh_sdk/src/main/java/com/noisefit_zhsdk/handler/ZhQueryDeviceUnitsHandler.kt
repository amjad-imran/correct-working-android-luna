package com.noisefit_zhsdk.handler


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.telephony.SmsManager
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.constants.CommonGlobals
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.enums.ApplicationType
import com.noisefit_commans.handler.MusicControlActionsEvents
import com.noisefit_commans.interfaces.IQueryDataCallback
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.models.BatteryData
import com.noisefit_commans.models.CaseInfoData
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.CustomReplyData
import com.noisefit_commans.models.DeviceFirmware
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.HandWashing
import com.noisefit_commans.models.HeartRateAlert
import com.noisefit_commans.models.ReminderList
import com.noisefit_commans.models.SedentaryData
import com.noisefit_commans.models.StockSymbol
import com.noisefit_commans.models.StockSymbolList
import com.noisefit_commans.models.SwitchSetting
import com.noisefit_commans.models.WorkoutRealTimeData
import com.noisefit_commans.models.WorldClockList
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.FileLogsUtils
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LocationClientClass
import com.noisefit_commans.utils.MusicUtil
import com.noisefit_zhsdk.BuildConfig
import com.noisefit_zhsdk.base.ZhApplicationHandler
import com.noisefit_zhsdk.log.ZhBleLogUtils
import com.zh.ble.wear.protobuf.MusicProtos
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.AgpsInfoBean
import com.zhapp.ble.bean.BodyTemperatureSettingBean
import com.zhapp.ble.bean.BreathingLightSettingsBean
import com.zhapp.ble.bean.ClassicBluetoothStateBean
import com.zhapp.ble.bean.ClockInfoBean
import com.zhapp.ble.bean.CommonReminderBean
import com.zhapp.ble.bean.ContinuousBloodOxygenSettingsBean
import com.zhapp.ble.bean.DeviceInfoBean
import com.zhapp.ble.bean.DoNotDisturbModeBean
import com.zhapp.ble.bean.EvDataInfoBean
import com.zhapp.ble.bean.EventInfoBean
import com.zhapp.ble.bean.FindWearSettingsBean
import com.zhapp.ble.bean.HeartRateMonitorBean
import com.zhapp.ble.bean.MusicInfoBean
import com.zhapp.ble.bean.NotificationSettingsBean
import com.zhapp.ble.bean.PressureModeBean
import com.zhapp.ble.bean.RealTimeBean
import com.zhapp.ble.bean.SchedulerBean
import com.zhapp.ble.bean.SchoolBean
import com.zhapp.ble.bean.ScreenDisplayBean
import com.zhapp.ble.bean.ScreenSettingBean
import com.zhapp.ble.bean.SimpleSettingSummaryBean
import com.zhapp.ble.bean.SleepModeBean
import com.zhapp.ble.bean.SleepReminder
import com.zhapp.ble.bean.StockSymbolBean
import com.zhapp.ble.bean.WidgetBean
import com.zhapp.ble.bean.WorldClockBean
import com.zhapp.ble.bean.WristScreenBean
import com.zhapp.ble.callback.AgpsCallBack
import com.zhapp.ble.callback.BehaviorLogCallBack
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.CallStateCallBack
import com.zhapp.ble.callback.ContactCallBack
import com.zhapp.ble.callback.DeviceBatteryReportingCallBack
import com.zhapp.ble.callback.DeviceInfoCallBack
import com.zhapp.ble.callback.DeviceLogCallBack
import com.zhapp.ble.callback.EmergencyContactsCallBack
import com.zhapp.ble.callback.FirmwareLogStateCallBack
import com.zhapp.ble.callback.MicroCallBack
import com.zhapp.ble.callback.MusicCallBack
import com.zhapp.ble.callback.QuickReplyCallBack
import com.zhapp.ble.callback.RealTimeDataCallBack
import com.zhapp.ble.callback.RequestClassicBleConnectStatusCallBack
import com.zhapp.ble.callback.SettingMenuCallBack
import com.zhapp.ble.callback.StockCallBack
import com.zhapp.ble.parsing.ParsingStateManager
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import javax.inject.Inject


class ZhQueryDeviceUnitsHandler
@Inject
constructor(
    var zhApplicationHandler: ZhApplicationHandler,
    var dataConverter: DataConverter,
    var context: Context,
    var gson: Gson,
    var watchDataStore: WatchDataStore
) : QueryDeviceDataActions() {

    companion object {

        const val TAG = "ZhQueryDeviceUnitHandler"
        var reminders: ReminderList? = null
    }

    private var songName: String? = null
    private var previousVolume = 0
    private var locationClientClass: LocationClientClass? = null
    private var firstLocation = true
    private var zhService: ControlBleTools? = null
    private var testQueryDeviceDataCallback: IQueryDataCallback? = null
    private var currentGpsSportState = -1
    private var noiseFitDevice: ColorFitDevice? = null

    override fun <T> callbackListener(callback: T) {
    }

    override fun <T> callbackListenerNew(callback: T) {
        testQueryDeviceDataCallback = callback as IQueryDataCallback
    }

    override fun setDevice(colorFitDevice: ColorFitDevice) {
        noiseFitDevice = colorFitDevice

        initLogListener()
        //AppLogs.sendAppLogs("Log listener initialized")
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
        var isMusicOn = false
        val musicName = MusicUtil.getSongName(songName)

        if (MusicUtil.isMusicActive()) {
            isMusicOn = true

        }


        syncMusic(musicName, isMusicOn, maxVolume, currentVolume)
        AppLogs.sendAppLogs(" Set volume control triggered")
    }


    override fun setMusicStatus(status: Int, title: String?, sec: Int?) {
        LOGS.d(TAG, "syncMusicData in watch $status $title")

        var songPlaying = false
        if (status == 1) {
            songPlaying = true
        }

        val volume = returnVolume()
        songName = title

        syncMusic(MusicUtil.getSongName(songName), songPlaying, volume.second, volume.first)
        AppLogs.sendAppLogs("Set music status")


    }


    override fun init() {
        super.init()
        zhService = zhApplicationHandler.getZhService()
        removeCallbacks()
        attachCallbacks()

        CallBackUtils.setStockCallBack(MyStockCallBack(testQueryDeviceDataCallback))
        AppLogs.sendAppLogs("Initialized app handler")

    }

    override fun restartDevice() {
        ControlBleTools.getInstance().restartByProduction()
    }

    override fun resetTrigger() {
        ControlBleTools.getInstance().unbindDeviceWaitConfirmation(object : SendCmdStateListener() {
            override fun onState(state: SendCmdState) {
                LOGS.d("resetTrigger", " State $state")
                if (state == SendCmdState.SUCCEED) {

                } else {

                }
            }
        })
    }

    override fun getAgpsState() {
        CallBackUtils.agpsCallBack = object : AgpsCallBack {


            override fun onRequestState(p0: AgpsInfoBean?) {
                //    testQueryDeviceDataCallback?.onQueryDataReceived(QueryCallback.AgpsRequestState(p0))
            }
        }

    }

    private fun returnVolume(): Pair<Int, Int> {
        val audioManager =
            NoisefitApplication.context?.applicationContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val sb2value = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val cur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return Pair(cur, sb2value)
    }

    private fun setMusicPlayerState() {
        var isMusicOn = false
        val musicName = MusicUtil.getSongName(songName)

        if (MusicUtil.isMusicActive()) {
            isMusicOn = true

        }

        val volume = returnVolume()
        syncMusic(musicName, isMusicOn, volume.second, volume.first)


    }

    override fun getSleepReminder() {
        ControlBleTools.getInstance().getSleepReminder(object : SendCmdStateListener() {
            override fun onState(p0: SendCmdState?) {

            }

        })
    }

    override fun getBleCallingSwitch() {
        ControlBleTools.getInstance()
            .getClassicBluetoothState(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            AppLogs.sendAppLogs("calling success")
                        }

                        SendCmdState.TIMEOUT -> {
                            testQueryDeviceDataCallback?.onQueryDataReceived(
                                QueryCallback.BleCallingSwitchObtained(false)
                            )
                            AppLogs.sendAppLogs("calling timeout")
                        }

                        else -> {}
                    }
                }
            })
    }

    private fun setPlayPauseMusic(isPlay: Boolean) {

        val musicName = MusicUtil.getSongName(songName)


        val volume = returnVolume()

        syncMusic(musicName, isPlay, volume.second, volume.first)
    }

    private fun syncMusic(musicName: String, isPlay: Boolean, maxVolume: Int, currentVolume: Int) {

        var musicState = MusicProtos.SEPlayerInfo.SEState.PAUSE_VALUE
        if (!MusicUtil.isNotificationServiceRunning(context)) {
            LOGS.d("syncMusic no permission")
            musicState = MusicProtos.SEPlayerInfo.SEState.NO_PERMISSION_VALUE
        } else if (isPlay) {
            LOGS.d("syncMusic has permission")
            musicState = MusicProtos.SEPlayerInfo.SEState.PLAYING_VALUE
        }


        val musicInfoBean = MusicInfoBean(
            musicState,
            MusicUtil.getSongName(musicName),
            currentVolume,
            maxVolume,
            true
        );

        LOGS.d("syncMusic music sync ")
        LOGS.d(musicInfoBean)

        ControlBleTools.getInstance().syncMusicInfo(musicInfoBean, object :
            SendCmdStateListener() {
            override fun onState(state: SendCmdState) {
                LOGS.d(state)

            }
        })

    }

    override fun getStressSettings() {
        ControlBleTools.getInstance().getPressureMode(null)
    }

    private fun compareMusicData(music1: MusicInfoBean, music2: MusicInfoBean): Boolean {
        if (music1.currentVolume == music2.currentVolume &&
            music1.state == music2.state &&
            music1.totalVolume == music2.totalVolume &&
            music1.songTitle.lowercase().equals(music2.songTitle.lowercase())
        )
            return true
        return false
    }

    private val batteryAlertCallback = DeviceBatteryReportingCallBack {
        testQueryDeviceDataCallback?.onQueryDataReceived(
            QueryCallback.BatteryAlertObtained(
                it.deviceBatteryValue
            )
        )
    }

    private val realDataCallback = object : RealTimeDataCallBack {
        override fun onResult(p0: RealTimeBean?) {

            if (p0 == null) return
            val chargeStatus = try {
                p0.batteryInfo.chargeStatus.toInt()
            } catch (exp: Exception) {
                2
            }
            val capacity = try {
                p0.batteryInfo.capacity.toIntOrNull()
            } catch (exp: Exception) {
                null
            }


            if (p0.nfcSleepErr == 1) {
                AppLogs.sendAppLogs("RealTimeBean NFC Sleep Err ${p0.nfcSleepErr}")
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.NfcSleepErr()
                )
            }


            var isCharging = false
            if (chargeStatus == 1) {
                isCharging = true
            }
            LOGS.d("Realtime Data battery Info : ${p0.batteryInfo} Steps: ${p0.steps} Calories: ${p0.calories}")

            /*if (p0.steps != null || p0.calories != null || p0.heartRate != null) {
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.WorkoutRealTimeDataObtained(
                        WorkoutRealTimeData(
                            timestamp = System.currentTimeMillis(),
                            steps = try {
                                p0.steps.toIntOrNull()
                            }catch (exp:Exception){null},
                            distance = try {
                                p0.distance.toLongOrNull()
                            }catch (exp:Exception){null},
                            calorieValue =try {
                                p0.calories.toIntOrNull()
                            }catch (exp:Exception){null},
                            hrValue =try {
                                p0.heartRate.toIntOrNull()
                            }catch (exp:Exception){null},

                        )
                    )
                )

            }*/


            if (capacity != null) {
                val caseInfoData = if((p0.ringChargingCaseInfoBean?.battLevel ?: -1) > 0){
                    CaseInfoData(
                        isOpen = p0.ringChargingCaseInfoBean.isOpen,
                        battLevel = p0.ringChargingCaseInfoBean.battLevel,
                        serialNumber = p0.ringChargingCaseInfoBean.serialNums,
                        isRingCharging = isCharging
                    )
                }else null

                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.BatteryDataObtained(
                        BatteryData(percentage = capacity, isCharging = isCharging, caseInfoData = caseInfoData)
                    )
                )
            }


            /*if (!colorFitDevice?.deviceType.equals(DeviceType.NOISEFIT_LUNA.deviceType, true)) {
                p0?.let {
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.RealStepsDataObtained(
                            dataConverter.parseStepsData(
                                p0
                            )
                        )
                    )
                }
            }*/

        }

        override fun onFail() {

        }

    }


    override fun attachCallbacks() {
        removeCallbacks()
        initClassicBluetoothStateCallBack()

        CallBackUtils.realTimeDataCallback = realDataCallback
        CallBackUtils.setDeviceBatteryReportingCallBack(batteryAlertCallback)


        /**
         * 设备端处理来电回复回调
         */
        CallBackUtils.callStateCallBack = object : CallStateCallBack {
            override fun onState(state: Int) {
                LOGS.d("incomingcall", "state $state")
                when (state) {
                    0 -> {
                        //Answer
                        /* testQueryDeviceDataCallback?.onQueryDataReceived(
                             QueryCallback.UpdateCallStatus(
                                 false
                             )
                         )*/
                    }

                    1 -> {
                        //Reject
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.UpdateCallStatus(
                                false
                            )
                        )
                        AppLogs.sendAppLogs("$TAG call reject")
                    }

                    2 -> {
                        //Mute
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.MuteDevice()
                        )
                        AppLogs.sendAppLogs("$TAG call mute")
                    }
                }
            }
        }


        CallBackUtils.deviceInfoCallBack = object : DeviceInfoCallBack {

            override fun onBatteryInfo(capacity: Int, chargeStatus: Int) {

                var isCharging = false
                if (chargeStatus == 1) {
                    isCharging = true
                }
                LOGS.d("onBatteryInfo ${chargeStatus} $isCharging $capacity")

                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.BatteryDataObtained(
                        BatteryData(percentage = capacity, isCharging = isCharging, isCaseDataAvailable = false)
                    )
                )
                //AppLogs.sendAppLogs("Battery info get")
                /*var state = "未知"
                when (chargeStatus) {
                    0 -> {
                        state = "未知"//Unknown
                    }
                    1 -> {
                        state = "充电中"//charging
                    }
                    2 -> {
                        state = "没充电"//not charging
                    }
                    3 -> {
                        state = "充满状态"//full
                    }
                }*/
            }

            override fun onDeviceInfo(deviceInfoBean: DeviceInfoBean) {
                LOGS.d(
                    TAG, "${deviceInfoBean.firmwareVersion} ${deviceInfoBean.equipmentNumber} " +
                            "S.No ${deviceInfoBean.serialNumber}"
                )
                if (noiseFitDevice?.deviceType.equals(DeviceType.NOISEFIT_LUNA.deviceType, true)) {
                    WatchInfoGlobals.firmwareVersionRing = deviceInfoBean.firmwareVersion
                    WatchInfoGlobals.serialNumberRing = deviceInfoBean.serialNumber
                    WatchInfoGlobals.firmwareVersionNumberRing = try {
                        deviceInfoBean.firmwareVersion.replace(".", "").toIntOrNull() ?: 0
                    } catch (exp: Exception) {
                        0
                    }
                    WatchInfoGlobals.firmwareDeviceIdRing =
                        deviceInfoBean.equipmentNumber.toIntOrNull() ?: 0

                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.FirmwareVersionObtained(DeviceFirmware(version = WatchInfoGlobals.firmwareVersionRing))
                    )
                } else {
                    WatchInfoGlobals.firmwareVersion = deviceInfoBean.firmwareVersion
                    WatchInfoGlobals.serialNumber = deviceInfoBean.serialNumber
                    WatchInfoGlobals.firmwareVersionNumber = try {
                        deviceInfoBean.firmwareVersion.replace(".", "").toIntOrNull() ?: 0
                    } catch (exp: Exception) {
                        0
                    }
                    WatchInfoGlobals.firmwareDeviceId =
                        deviceInfoBean.equipmentNumber.toIntOrNull() ?: 0

                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.FirmwareVersionObtained(DeviceFirmware(version = WatchInfoGlobals.firmwareVersion))
                    )
                }


                //AppLogs.sendAppLogs("Get device info")
            }
        }

        CallBackUtils.emergencyContactsCallBack =
            EmergencyContactsCallBack { p0 ->
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.SOSContactObtained(
                        dataConverter.formatSOSContact(p0)
                    )
                )
            }

        /**
         * 处理音乐相关回调
         */
        CallBackUtils.musicCallBack = object : MusicCallBack {
            override fun onRequestMusic() { //设备进入音乐界面，请求音乐信息，此时发送音乐信息设备才有反应
                //syncMusic()
                setMusicPlayerState()
                AppLogs.sendAppLogs("$TAG : musicCallBack onRequestMusic")

            }

            override fun onSyncMusic(errorCode: Int) { //syncMusic结果
                AppLogs.sendAppLogs("TAG : musicCallBack onSyncMusic")
            }

            override fun onQuitMusic() {
                AppLogs.sendAppLogs("TAG : musicCallBack onQuitMusic")
            }

            override fun onSendMusicCmd(command: Int) { //设备控制指令
                LOGS.d(TAG, "command $command")
                when (command) {
                    MusicProtos.SEPlayerControlCommand.PLAYING_VALUE -> {
                        setPlayPauseMusic(true)
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.OnMusicEventChanged(
                                MusicControlActionsEvents.PLAY
                            )
                        )
                        AppLogs.sendAppLogs("Send music play command")
                    }

                    MusicProtos.SEPlayerControlCommand.PAUSE_VALUE -> {
                        setPlayPauseMusic(false)
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.OnMusicEventChanged(
                                MusicControlActionsEvents.PAUSE
                            )
                        )
                        AppLogs.sendAppLogs("Send music pause command")
                    }

                    MusicProtos.SEPlayerControlCommand.PREV_VALUE -> {
                        setMusicPlayerState()
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.OnMusicEventChanged(
                                MusicControlActionsEvents.PREVIOUS
                            )
                        )
                        AppLogs.sendAppLogs("Send music previous command")
                    }

                    MusicProtos.SEPlayerControlCommand.NEXT_VALUE -> {
                        setMusicPlayerState()
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.OnMusicEventChanged(
                                MusicControlActionsEvents.NEXT
                            )
                        )
                        AppLogs.sendAppLogs("Send music next command")
                    }

                    MusicProtos.SEPlayerControlCommand.ADJUST_VOLUME_UP_VALUE -> {
                        updateVolume(true)
                        AppLogs.sendAppLogs("Send music volume up command")
                    }

                    MusicProtos.SEPlayerControlCommand.ADJUST_VOLUME_DOWN_VALUE -> {
                        updateVolume(
                            false
                        )
                        AppLogs.sendAppLogs("Send music volume down command")
                    }
                }
            }
        }

        /**
         * 设备设置相关
         */
        CallBackUtils.settingMenuCallBack = object : SettingMenuCallBack {
            override fun onVibrationResult(model: Int) {
            }

            override fun onVibrationDurationResult(p0: Int) {

            }

            override fun onPowerSavingResult(isOpen: Boolean) {
            }

            override fun onOverlayScreenResult(isOpen: Boolean) {
            }

            override fun onRapidEyeMovementResult(isOpen: Boolean) {
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.QuickEyeMovementSwitchObtained(
                        SwitchSetting(
                            status = isOpen
                        )
                    )
                )
            }

            override fun onWristScreenResult(bean: WristScreenBean) {
            }

            override fun onDoNotDisturbModeResult(bean: DoNotDisturbModeBean) {

                val doNotDisturb =
                    dataConverter.parseDoNotDisturb(bean)

                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.DoNotDisturbObtained(
                        doNotDisturb
                    )
                )
            }

            override fun onHeartRateMonitorResult(bean: HeartRateMonitorBean) {
                var status = true
                if (bean.mode == 1) {
                    status = false
                }
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.HeartRateAlertDataObtained(
                        HeartRateAlert(
                            status,
                            0,
                            bean.warningValue
                        )
                    )
                )
                AppLogs.sendAppLogs("sent request for heart rate monitor result")

            }

            override fun onScreenDisplayResult(bean: ScreenDisplayBean) {
            }

            override fun onScreenSettingResult(bean: ScreenSettingBean) {
                LOGS.d(TAG, "$bean")
                CommonGlobals.screenDuration = bean.duration
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.BrightnessLevelObtained(
                        bean.level * 20
                    )
                )
                AppLogs.sendAppLogs("sent request for screen brightness level result")
            }

            override fun onSedentaryReminderResult(bean: CommonReminderBean) {

                LOGS.d("ZhQueryDeviceUnitHandler", "$bean")

                val idleAlert = SedentaryData(
                    status = bean.isOn,
                    interval = bean.frequency / 60,
                    startHour = bean.startTime.hour,
                    startMinute = bean.startTime.minuter,
                    endHour = bean.endTime.hour,
                    endMinute = bean.endTime.minuter
                )

                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.SedentaryDataObtained(
                        idleAlert
                    )
                )
                AppLogs.sendAppLogs("sent request for sedentary data result")

            }

            override fun onDrinkWaterReminderResult(bean: CommonReminderBean) {


                val idleAlert = SedentaryData(
                    status = bean.isOn,
                    interval = bean.frequency / 60,
                    startHour = bean.startTime.hour,
                    startMinute = bean.startTime.minuter,
                    endHour = bean.endTime.hour,
                    endMinute = bean.endTime.minuter
                )

                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.DrinkWaterDataObtained(
                        idleAlert
                    )
                )
                AppLogs.sendAppLogs("sent request for drink reminder result")

            }

            override fun onMedicationReminderResult(bean: CommonReminderBean) {
                val idleAlert = SedentaryData(
                    status = bean.isOn,
                    interval = bean.frequency / 60,
                    startHour = bean.startTime.hour,
                    startMinute = bean.startTime.minuter,
                    endHour = bean.endTime.hour,
                    endMinute = bean.endTime.minuter
                )

                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.MedicineDataObtained(
                        idleAlert
                    )
                )
                AppLogs.sendAppLogs("sent request for medicine reminder result")

            }

            override fun onHaveMealsReminderResult(p0: CommonReminderBean?) {
                val mealsReminder = p0?.let { bean ->
                    SedentaryData(
                        status = bean.isOn,
                        interval = bean.frequency / 60,
                        startHour = bean.startTime.hour,
                        startMinute = bean.startTime.minuter,
                        endHour = bean.endTime.hour,
                        endMinute = bean.endTime.minuter
                    )
                }
                if (mealsReminder != null) {
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.MealDataObtained(
                            mealsReminder
                        )
                    )
                    AppLogs.sendAppLogs("sent request for meals reminder result")

                }
            }

            override fun onWashHandReminderResult(p0: CommonReminderBean?) {
                val handWashParsed = p0?.let { handWashingInfo ->
                    HandWashing(
                        startWash = handWashingInfo.isOn,
                        startHour = handWashingInfo.startTime.hour,
                        startMinute = handWashingInfo.startTime.minuter,
                        endHour = handWashingInfo.endTime.hour,
                        endMinute = handWashingInfo.endTime.minuter,
                        frequency = handWashingInfo.frequency / 60
                    )
                }
                if (handWashParsed != null) {
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.GetHandWashing(handWashParsed)
                    )
                    AppLogs.sendAppLogs("sent request for hand wash reminder result")

                }
            }

            override fun onSleepReminder(p0: SleepReminder?) {
                p0?.let {

                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.SleepReminderObtained(
                            dataConverter.formatSleepReminder(p0)
                        )
                    )
                    AppLogs.sendAppLogs("sent request for event reminder result")
                }

            }

            override fun onEventInfoResult(list: List<EventInfoBean>, max: Int) {
                val reminders = dataConverter.formatReminderData(list)
                ZhQueryDeviceUnitsHandler.reminders = reminders
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.RemindersObtained(
                        reminders
                    )
                )
                AppLogs.sendAppLogs("sent request for event reminder result")

            }

            override fun onClockInfoResult(list: List<ClockInfoBean>, max: Int) {
                LOGS.d("Max alarms $max $list")
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.AlarmsObtained(
                        dataConverter.formatAlarmData(list)
                    )
                )
                AppLogs.sendAppLogs("sent request for clock info result")

            }

            override fun onSimpleSettingResult(simpleSettingSummaryBean: SimpleSettingSummaryBean) {} //endregion
            override fun onMotionRecognitionResult(p0: Boolean, p1: Boolean) {

            }

            override fun onWorldClockResult(p0: MutableList<WorldClockBean>?) {
                LOGS.d("Max alarms $p0")
                val data = ArrayList<WorldClockList.WClock>()
                p0?.forEach { item ->
                    data.add(
                        WorldClockList.WClock(
                            timeZone = item.offset,
                            content = item.cityName
                        )
                    )
                }
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.WorldClockDataObtained(WorldClockList(worldClocks = data))
                )
                AppLogs.sendAppLogs("sent request for world clock result")


            }

            override fun onBodyTemperatureSettingResult(p0: BodyTemperatureSettingBean?) {

            }

            override fun onClassicBleStateSetting(p0: ClassicBluetoothStateBean?) {
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.BleCallingSwitchObtained(p0?.isSwitch ?: false)
                )
            }

            override fun onSchoolModeResult(p0: SchoolBean?) {

            }

            override fun onSchedulerResult(p0: SchedulerBean?) {

            }

            override fun onSleepModeResult(p0: SleepModeBean?) {

            }

            override fun onPressureModeResult(p0: PressureModeBean?) {
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.StressParamObtained(
                        dataConverter.parseStress(p0)
                    )
                )
            }

            override fun onNotificationSetting(p0: NotificationSettingsBean?) {
            }

            override fun onContinuousBloodOxygenSetting(p0: ContinuousBloodOxygenSettingsBean?) {

            }

            override fun onFindWearSettings(p0: FindWearSettingsBean?) {

            }

            override fun onBreathingLightSettings(p0: BreathingLightSettingsBean?) {

            }

            override fun onEvDataInfo(p0: EvDataInfoBean?) {

            }

            override fun onEvRemindType(p0: Int) {
            }

            override fun onCustomizeLeftClickSettings(p0: Int) {

            }


        }

        /**
         * 小功能综合
         */
        CallBackUtils.setMicroCallBack(object : MicroCallBack {
            override fun onWearSendFindPhone(mode: Int) {

                var isRing = false
                if (mode == 0) {
                    isRing = true
                }

                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.OpenFindMyPhoneActivity(
                        isRing
                    )
                )

                AppLogs.sendAppLogs("$isRing request for find phone")

            }


            override fun onPhotograph(p0: Int) {
                LOGS.d("onPhotograph $p0")
                when (p0) {
                    0 -> {
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.CloseCameraShutterActivity(
                                false
                            )
                        )

                    }

                    1 -> {
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.CloseCameraShutterActivity(
                                true
                            )
                        )
                        AppLogs.sendAppLogs("sent request for close camera shutter")
                    }

                    2 -> {
                        testQueryDeviceDataCallback?.onQueryDataReceived(QueryCallback.ClickCameraImage)
                        AppLogs.sendAppLogs("sent request for click camera image")
                    }
                }

            }

            override fun onWidgetList(list: List<WidgetBean>) {
                val convertedSports = dataConverter.convertWidgetList(list, noiseFitDevice)
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.WidgetSortListObtained(
                        convertedSports
                    )
                )
                // LOGS.d("onWidgetList ${gson.toJson(list)}")
                AppLogs.sendAppLogs("sent request for widget sort list")
            }

            override fun onApplicationList(list: List<WidgetBean>) {

                val appList = dataConverter.convertAppList(list)
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.AppListObtained(
                        appList
                    )
                )


            }

            override fun onSportTypeIconList(p0: MutableList<WidgetBean>?) {

            }

            override fun onSportTypeOtherList(p0: MutableList<WidgetBean>?) {

            }

            override fun onQuickWidgetList(p0: MutableList<WidgetBean>?) {


            }

            override fun onSportWidgetSortList(p0: MutableList<WidgetBean>?) {
                val convertedSports = dataConverter.parseSportsMode(p0, noiseFitDevice!!)
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.SportWidgetSortList(
                        convertedSports
                    )
                )
                AppLogs.sendAppLogs("sent request for sport sort list")


            }

            override fun onNfcSleepErr(p0: Int) {//1 exception, 0 Normal
                LOGS.i("onNfcSleepErr $p0")
                AppLogs.sendAppLogs("onNfcSleepErr $p0")
                if (p0 == 1) {
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.NfcSleepErr()
                    )
                }
            }

            override fun onRingWearingStatus(p0: Int) {

            }
        })

        /**
         * 设备快捷回复相关
         */

        CallBackUtils.quickReplyCallBack = object : QuickReplyCallBack {
            override fun onQuickReplyResult(data: java.util.ArrayList<String>) {
                val customReplyData = ArrayList<CustomReplyData.CustomReply>()
                data.forEach { item ->
                    val customReply = CustomReplyData.CustomReply()
                    customReply.content = item
                    customReplyData.add(customReply)
                }
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.CustomReplyObtained(CustomReplyData(customReplies = customReplyData))
                )
                AppLogs.sendAppLogs("sent request for custom reply")
            }

            override fun onMessage(phone_number: String, text: String) {
                sendSMS(phone_number, text)
            }
        }
        CallBackUtils.contactCallBack = ContactCallBack { data ->
            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.ContactListObtained(
                    dataConverter.formatContactList(data)
                )
            )
            AppLogs.sendAppLogs("sent request for contact list")
        }


    }

    override fun getSleepException() {
        LOGS.i("onNfcSleepErr getSleepException")

        ControlBleTools.getInstance().getRingNFCSleepErr(object : SendCmdStateListener() {
            override fun onState(state: SendCmdState) {
                when (state) {
                    SendCmdState.SUCCEED -> {
                        LOGS.i("onNfcSleepErr getSleepException success")

                        //context.showShortToast("Firmware logs generated")
                    }

                    else -> {
                        LOGS.i("onNfcSleepErr getSleepException failed")

                        //context.showShortToast("Firmware logs generation failed")
                    }
                }
            }
        })
    }

    override fun getFirmwareLogs() {

        ControlBleTools.getInstance().getFirmwareLog(object : SendCmdStateListener() {
            override fun onState(state: SendCmdState) {
                when (state) {
                    SendCmdState.SUCCEED -> {
                        //context.showShortToast("Firmware logs generated")
                    }

                    else -> {
                        //context.showShortToast("Firmware logs generation failed")
                    }
                }
            }
        })

    }

    private fun sendErrorMessageToApp(message: String, title: String) {
        LOGS.i("QuickReply sendErrorMessageToApp")
        val appNotification = ApplicationType.NOISEFIT.type
        ControlBleTools.getInstance()
            .sendAppNotification(
                appNotification.replace("_", " "),
                dataConverter.getPackageName(appNotification),
                title,
                message,
                "t", null
            )
    }

    private fun sendSMS(phoneNumber: String, text: String) {
        LOGS.i("QuickReply $phoneNumber : $text ")
        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.SEND_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ControlBleTools.getInstance().sendCallState(1, null)
                sendErrorMessageToApp(
                    "Please enable both sms and phone alerts in NoiseFit app",
                    "Permission Error"
                )
                LOGS.i("QuickReply has no permission ")
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.UpdateCallStatus(
                        false, true
                    )
                )
                AppLogs.sendAppLogs("sent request for quick reply sms")
            } else {
                LOGS.i("QuickReply has permission ")
                testQueryDeviceDataCallback?.onQueryDataReceived(
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


    override fun removeCallbacks() {

    }

    override fun getSportWidgetSortList() {
        ControlBleTools.getInstance().getSportWidgetSortList(null)
    }

    override fun queryFirmwareVersion() {
        zhService?.getDeviceInfo(null)
    }


    override fun getBrightnessLevel() {
        ControlBleTools.getInstance().getScreenSetting(null)
    }

    private fun initClassicBluetoothStateCallBack() {
        CallBackUtils.classicBleConnectStatusCallBack =
            RequestClassicBleConnectStatusCallBack { status ->
                LOGS.d("ClassicBluetooth ${status.isConnect} ${status.isSwitch} ${status.mac}")

            }
    }

    override fun getSOSContactList() {
        ControlBleTools.getInstance().getEmergencyContacts(object : SendCmdStateListener() {
            override fun onState(state: SendCmdState?) {
                when (state) {
                    SendCmdState.SUCCEED -> {
                        AppLogs.sendAppLogs("Emergency contact succeed")
                    }

                    SendCmdState.TIMEOUT -> {
                        AppLogs.sendAppLogs("Emergency contact timeout")
                    }

                    else -> {}
                }
            }

        })
    }

    override fun getBluetoothCallStatus() {

        ControlBleTools.getInstance()
            .getInquiryClassicBleConnectStatus(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            AppLogs.sendAppLogs("BLE call connection succeed")
                        }

                        SendCmdState.TIMEOUT -> {
                            AppLogs.sendAppLogs("BLE call connection timeout")
                        }

                        else -> {}
                    }
                }
            })
    }


    private fun initLogListener() {
        CallBackUtils.firmwareLogStateCallBack = object : FirmwareLogStateCallBack {
            override fun onFirmwareLogState(state: Int) {
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.UpdateFirmwareLogStatus(
                        state
                    )
                )
                /*if (state == FirmwareLogStateCallBack.FirmwareLogState.START.state) {

                } else if (state == FirmwareLogStateCallBack.FirmwareLogState.UPLOADING.state) {

                } else if (state == FirmwareLogStateCallBack.FirmwareLogState.END.state) {

                }*/
            }

            override fun onFirmwareLogFilePath(filePath: String?) {
                filePath?.let {
                    watchDataStore.saveFirmwareLogPath(it)
                }
            }
        }

        ZhBleLogUtils.initLogger(
            NoisefitApplication.context!!.applicationContext,
            isWriteLog = BuildConfig.DEBUG,
            isRelease = false   //Is the log storage path (true inside the system) or (false inside the app)?
        )

        ControlBleTools.getInstance().deviceLogCallBack = object : DeviceLogCallBack {
            override fun onLogI(tag: String?, msg: String?, p2: String?) {
                if (BuildConfig.DEBUG) {
                    Log.i(msg, p2 ?: "")
                }
                FileLogsUtils.saveILogs(
                    noiseFitDevice,
                    "$tag $msg",
                    p2 ?: "",
                    FileLogsUtils.LogType.Watch
                )
                ZhBleLogUtils.bleLog(msg, p2)
            }

            override fun onLogV(tag: String?, msg: String?, p2: String?) {
                if (BuildConfig.DEBUG) {
                    Log.v(msg, p2 ?: "")
                }
                FileLogsUtils.saveWLogs(
                    noiseFitDevice,
                    "$tag $msg",
                    p2 ?: "",
                    FileLogsUtils.LogType.Watch
                )
                ZhBleLogUtils.bleLog(msg, p2)
            }

            override fun onLogE(tag: String?, msg: String?, p2: String?) {
                if (BuildConfig.DEBUG) {
                    Log.e(msg, p2 ?: "")
                }
                FileLogsUtils.saveELogs(
                    noiseFitDevice,
                    "$tag $msg",
                    p2 ?: "",
                    FileLogsUtils.LogType.Watch
                )
                ZhBleLogUtils.bleLog(msg, p2)
            }

            override fun onLogD(tag: String?, msg: String?, p2: String?) {
                if (BuildConfig.DEBUG) {
                    Log.d(msg, p2 ?: "")
                }
                FileLogsUtils.saveDLogs(
                    noiseFitDevice,
                    "$tag $msg",
                    p2 ?: "",
                    FileLogsUtils.LogType.Watch
                )
                ZhBleLogUtils.bleLog(msg, p2)
            }

            override fun onLogW(tag: String?, msg: String?, p2: String?) {
                if (BuildConfig.DEBUG) {
                    Log.w(msg, p2 ?: "")
                }
                FileLogsUtils.saveWLogs(
                    noiseFitDevice,
                    "$tag $msg",
                    p2 ?: "",
                    FileLogsUtils.LogType.Watch
                )
                ZhBleLogUtils.bleLog(msg, p2)
            }

        }

        CallBackUtils.behaviorLogCallBack = object : BehaviorLogCallBack {
            override fun onLog(p0: String?, p1: String?, p2: String?) {
                ZhBleLogUtils.behaviorLog(p0, p1, p2)
            }
        }

    }

    override fun queryBatteryPower() {
        //TODO ZH FIX
        // When the App enters the foreground, real-time data change reporting has been
        // enabled through setRealTimeDataState(true). If the power and charging status change,
        // realDataCallback will report the latest power data to the app in real time,
        // and there is no need to actively call it.
//        zhService?.getDeviceBattery(null)
//        AppLogs.sendAppLogs("sent request for battery power")
    }

    override fun getDoNotDisturbData() {
        ControlBleTools.getInstance()
            .getDoNotDisturbMode(null)
    }

    override fun getUserInfo() {

    }

    override fun getLanguage() {

    }


    override fun queryFirmwareUpgrade() {
    }

    override fun queryFirmwareUpgradeNew(colorFitDevice: ColorFitDevice) {
    }


    override fun getWatchFaces() {
    }


    override fun getWidgetList() {
        ControlBleTools.getInstance().getWidgetList(object : SendCmdStateListener() {
            override fun onState(state: SendCmdState) {
                LOGS.d("APP_SORT_CMD ${state.name}")
            }

        })
        AppLogs.sendAppLogs("sent request for widget list")
    }

    override fun getAppList() {
        ControlBleTools.getInstance().getApplicationList(object : SendCmdStateListener() {
            override fun onState(state: SendCmdState) {
                LOGS.d("APP_SORT_CMD ${state.name}")
            }

        })
        AppLogs.sendAppLogs("sent request for app list")

    }

    override fun getMedicineReminders() {}


    override fun getReminders() {
        ControlBleTools.getInstance().getEventInfoList(null)
    }

    override fun getBodyTempUnit() {

    }

    override fun getAlarms() {
        ControlBleTools.getInstance().getClockInfoList(null)
    }


    override fun getAutoSleep() {
    }

    override fun getHeartRateInterval() {

    }

    override fun getHeartRateAlert() {
        ControlBleTools.getInstance().getHeartRateMonitor(null)
    }

    override fun getSedentaryData() {
        ControlBleTools.getInstance().getSedentaryReminder(null)
    }

    override fun getDeviceUnits() {
    }

    override fun getScreenAwakeInterval() {
    }

    override fun getHandwashData() {
        ControlBleTools.getInstance().getWashHandReminder(null)
    }


    override fun getMedicineReminderSettings() {
        ControlBleTools.getInstance().getMedicationReminder(null)
    }


    override fun getMealReminderSettings() {
        ControlBleTools.getInstance().getHaveMealsReminder(null)
    }

    override fun getDrinkWaterSettings() {
        ControlBleTools.getInstance().getDrinkWaterReminder(null)

    }

    override fun getCustomReplies() {
        ControlBleTools.getInstance()
            .getDevShortReplyData(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    LOGS.d("Custom reply $state")
                }
            })
        AppLogs.sendAppLogs("sent request for custom replies")

    }

    class MyStockCallBack(private val testQueryDeviceDataCallback: IQueryDataCallback?) :
        StockCallBack {

        override fun onStockInfoList(list: List<StockSymbolBean>) {
            LOGS.d("stock_list ${list.size}")
            val stockArray = ArrayList<StockSymbol>()
            list.forEach { item ->
                val stock = item.symbol?.let {
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
            AppLogs.sendAppLogs("sent request for stock info list")
        }

        override fun onWearRequestStock() {

            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.SyncStock()
            )
        }

    }

    override fun getWorldClock() {
        ControlBleTools.getInstance().getWorldClockList(object : SendCmdStateListener() {
            override fun onState(state: SendCmdState) {

            }
        })
        AppLogs.sendAppLogs("sent request for world clock list")

    }


    override fun getStockList() {

        ControlBleTools.getInstance()
            .getStockSymbolList(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                }
            })
        AppLogs.sendAppLogs("sent request for stock symbol list")

    }


    override fun getQuickEyeMovementSwitch() {
        ControlBleTools.getInstance()
            .getRapidEyeMovement(null)
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

        var isMusicOn = false
        val musicName = MusicUtil.getSongName(songName)

        if (MusicUtil.isMusicActive()) {
            isMusicOn = true

        }
        if (previousVolume == cur) {
            return
        }
        previousVolume = cur



        LOGS.d("Current_volume $sb2value $cur")
        syncMusic(musicName, isMusicOn, sb2value, cur)
    }

    override fun getContactList() {
        ControlBleTools.getInstance().getContactList(object : SendCmdStateListener() {
            override fun onState(state: SendCmdState) {

            }
        })
        AppLogs.sendAppLogs("sent request for stock info list")

    }


}