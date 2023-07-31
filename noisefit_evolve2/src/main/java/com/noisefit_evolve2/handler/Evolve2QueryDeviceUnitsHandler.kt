package com.noisefit_evolve2.handler

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.os.Build
import android.telephony.SmsManager
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
import com.noisefit_commans.models.BatteryData
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.CustomReplyData
import com.noisefit_commans.models.DeviceFirmware
import com.noisefit_commans.models.DeviceLanguage
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.DoNotDisturb
import com.noisefit_commans.models.Gender
import com.noisefit_commans.models.HeartRateAlert
import com.noisefit_commans.models.HeartRateInterval
import com.noisefit_commans.models.Language
import com.noisefit_commans.models.LocationDataModel
import com.noisefit_commans.models.MenstrualData
import com.noisefit_commans.models.SwitchSetting
import com.noisefit_commans.models.UserInfo
import com.noisefit_commans.models.WristLiftGesture
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LocationClientClass
import com.noisefit_commans.utils.MusicUtil
import com.noisefit_commans.utils.StringUtils.DefaultStartHour
import com.noisefit_commans.utils.StringUtils.DefaultStopHour
import com.noisefit_evolve2.base.Evolve2ApplicationHandler
import com.noisefit_evolve2.dataConversion.DataConverter
import com.noisefit_evolve2.handler.Evolve2UpdateDeviceUnitsHandler.Companion.mobileNumber
import com.touchgui.sdk.TGCallback
import com.touchgui.sdk.TGClient
import com.touchgui.sdk.TGEventListener
import com.touchgui.sdk.TGLanguage
import com.touchgui.sdk.bean.TGAlarm
import com.touchgui.sdk.bean.TGBatteryInfo
import com.touchgui.sdk.bean.TGHeartRateMonitoringModeConfig
import com.touchgui.sdk.bean.TGHeartRateRangeConfig
import com.touchgui.sdk.bean.TGMessage
import com.touchgui.sdk.bean.TGNotDisturbConfig
import com.touchgui.sdk.bean.TGPhysiologicalCycle
import com.touchgui.sdk.bean.TGProfile
import com.touchgui.sdk.bean.TGQuickReply
import com.touchgui.sdk.bean.TGRaiseWristConfig
import com.touchgui.sdk.bean.TGRemindDrinking
import com.touchgui.sdk.bean.TGSedentaryConfig
import com.touchgui.sdk.bean.TGSportStatus
import com.touchgui.sdk.bean.TGSportStatusEvent
import com.touchgui.sdk.bean.TGUnitConfig
import com.touchgui.sdk.bean.TGVersionInfo
import javax.inject.Inject

class Evolve2QueryDeviceUnitsHandler
@Inject
constructor(
    private var evolve2ApplicationHandler: Evolve2ApplicationHandler,
    private var dataConverter: DataConverter,
    private var context: Context,
    private val watchDataStore: WatchDataStore
) : QueryDeviceDataActions() {

    private var previousVolume = 0
    private var songName: String? = null
    private var currentGpsSportState = -1
    private var firstLocation = true
    private val LAT_LONG = "LAT_LONG"
    private var locationClientClass: LocationClientClass? = null
    private val LOCATION_BROADCAST_RECEIVER = "LOCATION_BROADCAST_RECEIVER"
    private var testQueryDeviceDataCallback: IQueryDataCallback? = null
    private var noiseFitDevice: ColorFitDevice? = null

    override fun <T> callbackListener(callback: T) {
    }

    override fun <T> callbackListenerNew(callback: T) {
        testQueryDeviceDataCallback = callback as IQueryDataCallback
    }

    override fun setDevice(colorFitDevice: ColorFitDevice) {
        noiseFitDevice = colorFitDevice
    }

    private var mClient: TGClient? = null
    override fun init() {
        super.init()
        mClient = evolve2ApplicationHandler.getTGBleClient()
        removeCallbacks()
        attachCallbacks()
    }

    private fun enableLocation() {
        locationClientClass = LocationClientClass()
        disableLocation()
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

    private fun setPositioningMessage(status: Int) {
        // 1 - Successful positioning
// 0 - Positioning failed
//        val status = 1

// 0 - LOW
// 1 - NORMAL
// 2 - High
        val rssi = 1
        mClient?.commandBuilder?.syncAppGpsStatus(status, rssi)
            ?.execute(object : TGCallback<Void> {
                override fun onSuccess(p0: Void?) {

                }

                override fun onFailure(p0: Throwable?) {

                }

            })
    }

    private var locationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (currentGpsSportState == TGSportStatusEvent.EVENT_READY || currentGpsSportState == TGSportStatusEvent.EVENT_START || currentGpsSportState == TGSportStatusEvent.EVENT_RESUME) {
                val locationArrayList =
                    intent.getParcelableArrayListExtra<LocationDataModel>(LAT_LONG)

                if (firstLocation) {
                    setPositioningMessage(1)
                    firstLocation = false
                }
                //  val gson = Gson()
                if (locationArrayList.isNullOrEmpty()) {
                    return
                }

//                locationArrayList.forEach { locationDataModal1 ->

                val locationDataModal = locationArrayList[0]

//                LOGS.d("LOCATION pulse 2 ", "location receiver ${Gson().toJson(locationDataModal)}")

                val rssi = 1

                val location1 = Location(LocationManager.GPS_PROVIDER)
                location1.reset()
                location1.longitude = locationDataModal.longitude
                location1.latitude = locationDataModal.latitude
                location1.accuracy = locationDataModal.accuracy
                location1.speed = locationDataModal.speed
                location1.altitude = locationDataModal.altitude
                location1.bearing = locationDataModal.bearing
                location1.time = locationDataModal.time
                mClient?.commandBuilder?.syncAppGpsData(
                    location1, rssi
                )?.execute(object : TGCallback<Void?> {
                    override fun onSuccess(data: Void?) {
//                        LOGS.d("LOCATION pulse 2 success ", "location receiver ${Gson().toJson(locationDataModal)}")


                    }

                    override fun onFailure(throwable: Throwable) {
//                        LOGS.d("LOCATION pulse 2 faliure ", "location receiver ${Gson().toJson(locationDataModal)}")
                    }
                })
            }

//            }

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

    private fun sendErrorMessageToApp(message: String, title: String) {
        val tgMessage = TGMessage()
        tgMessage.name = title
        tgMessage.phoneNumber = ""
        tgMessage.content = message
        tgMessage.type = TGMessage.COMMON_MSG
        mClient?.commandBuilder?.syncMessage(
            tgMessage
        )?.execute(object : TGCallback<Int> {
            override fun onFailure(p0: Throwable?) {

            }

            override fun onSuccess(p0: Int?) {

            }

        })
    }

    private fun listenForGpsSports2(event: TGSportStatusEvent?) {
        if (event?.isNeedAppGpsData == true) {
            if (hasPermission()) {
                LOGS.d("listenForGpsSports GPS Started")
                enableLocation()
            } else {
                sendErrorMessageToApp(
                    "Please enable location permission from activity screen in app, before starting a new run.",
                    "Alert"
                )
                setPositioningMessage(0)
                watchDataStore.setAskForPermission(true)
                currentGpsSportState = 4
            }
        }
    }


    private fun listenForGpsSports() {
        mClient?.commandBuilder?.querySportStatus()?.execute(object : TGCallback<TGSportStatus> {
            override fun onSuccess(p0: TGSportStatus?) {
                val sportType = p0?.sportType
                LOGS.d("listenForGpsSports", "$sportType ${p0?.isNeedAppGpsData}")
                if (p0?.isNeedAppGpsData == true) {
                    if (hasPermission()) {
                        LOGS.d("listenForGpsSports GPS Started")
                        enableLocation()
                    } else {
                        sendErrorMessageToApp(
                            "Please enable location permission from activity screen in app, before starting a new run.",
                            "Alert"
                        )
                        setPositioningMessage(0)
                        watchDataStore.setAskForPermission(true)
                        currentGpsSportState = 4
                    }
                }
            }

            override fun onFailure(p0: Throwable?) {

            }

        })

    }

    override fun getLanguage() {
        mClient?.commandBuilder?.unit?.execute(object :
            TGCallback<TGUnitConfig> {
            override fun onFailure(p0: Throwable?) {
                LOGS.d("getLanguage onFailure")
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.LanguageReceived(
                        Language(
                            language = DeviceLanguage.ENGLISH.type
                        )
                    )
                )
            }

            override fun onSuccess(p0: TGUnitConfig?) {
                LOGS.d("getLanguage onSuccess")
                testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.LanguageReceived(
                        Language(
                            language = when (p0?.language) {
                                TGLanguage.hi -> DeviceLanguage.HINDI.type
                                else -> DeviceLanguage.ENGLISH.type
                            }
                        )
                    )
                )
            }

        })

    }


    private val tgEventListener: TGEventListener = object : TGEventListener() {
        override fun onFindPhone(p0: Int) {
            var isRing = false
            when (p0) {
                START_FIND_PHONE -> isRing = true
                STOP_FIND_PHONE -> isRing = false
            }


            testQueryDeviceDataCallback?.onQueryDataReceived(
                QueryCallback.OpenFindMyPhoneActivity(
                    isRing
                )
            )
        }

        override fun onSportStatusEvent(p0: TGSportStatusEvent?) {
            currentGpsSportState = p0?.event ?: -1
//            listenForGpsSports()
            listenForGpsSports2(p0)
            LOGS.d("listenForGpsSports sport started $currentGpsSportState")
            if (currentGpsSportState == TGSportStatusEvent.EVENT_STOP) {
                firstLocation = true
                disableLocation()
            }

        }

        override fun onQuickReply(p0: TGQuickReply?) {
            if (p0 != null) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.SEND_SMS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.UpdateCallStatus(
                                false
                            )
                        )
                    } else {
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.UpdateCallStatus(
                                false
                            )
                        )
                        mobileNumber?.let { number ->
                            val smsManager = SmsManager.getDefault()
                            smsManager.sendTextMessage(number, null, p0.content, null, null)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }

        override fun onEvent(eventId: Int) {
            when (eventId) {
                PLAY -> {
                    setPlayPauseMusic(true)
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.OnMusicEventChanged(
                            MusicControlActionsEvents.PLAY
                        )
                    )
                }
                PAUSE -> {
                    setPlayPauseMusic(false)
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.OnMusicEventChanged(
                            MusicControlActionsEvents.PAUSE
                        )
                    )
                }
                PREVIOUS -> {
                    setMusicPlayerState()
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.OnMusicEventChanged(
                            MusicControlActionsEvents.PREVIOUS
                        )
                    )
                }
                NEXT -> {
                    setMusicPlayerState()
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.OnMusicEventChanged(
                            MusicControlActionsEvents.NEXT
                        )
                    )
                }
                VOLUME_UP -> updateVolume(true)
                VOLUME_DOWN -> updateVolume(false)
                REFUSE -> testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.UpdateCallStatus(
                        false
                    )
                )
                ANSWER -> testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.UpdateCallStatus(
                        true
                    )
                )
                SILENT -> testQueryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.MuteDevice()
                )
            }
        }
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

    override fun setVolume(currentVolume: Int, maxVolume: Int) {

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

    private fun setPlayPauseMusic(isPlay: Boolean) {

        val musicName = MusicUtil.getSongName(songName)

        val volume = returnVolume()

        syncMusic(musicName, isPlay, volume.second, volume.first)
    }

    private fun syncMusic(musicName: String, isPlay: Boolean, maxVolume: Int, currentVolume: Int) {
        mClient?.commandBuilder?.syncMusic(musicName, isPlay, maxVolume, currentVolume)
            ?.execute(object :
                TGCallback<Int> {

                override fun onFailure(p0: Throwable?) {

                }

                override fun onSuccess(p0: Int?) {

                }

            })
    }

    override fun setMusicStatus(status: Int, title: String?, sec: Int?) {

        var songPlaying = false
        if (status == 1) {
            songPlaying = true
        }

        val volume = returnVolume()
        songName = title

        syncMusic(MusicUtil.getSongName(songName), songPlaying, volume.second, volume.first)


    }


    private fun returnVolume(): Pair<Int, Int> {
        val audioManager =
            NoisefitApplication.context?.applicationContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val sb2value = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val cur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return Pair(cur, sb2value)
    }


    override fun attachCallbacks() {

//        mClient?.logManager?.addListener(logListener)
        mClient?.addEventListener(tgEventListener)

    }


    override fun removeCallbacks() {
        // mClient?.logManager?.removeListener(logListener)
        mClient?.removeEventListener(tgEventListener)
    }

    override fun queryFirmwareVersion() {
        if (mClient != null) {
            mClient?.commandBuilder?.versionInfo?.execute(object : TGCallback<TGVersionInfo> {
                override fun onSuccess(p0: TGVersionInfo?) {
                    if (p0 != null) {
                        when (noiseFitDevice?.deviceType) {
                            DeviceType.NOISE_EVOLVE_2.deviceType -> {
                                WatchInfoGlobals.firmwareDeviceId =
                                    WatchInfoGlobals.EVOLVE_2_FIRMWARE_CONST_VERSION
                            }
                            DeviceType.COLORFIT_PULSE_2.deviceType -> {
                                WatchInfoGlobals.firmwareDeviceId =
                                    WatchInfoGlobals.PULSE_2_FIRMWARE_CONST_VERSION
                            }
                            DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType -> {
                                WatchInfoGlobals.firmwareDeviceId =
                                    WatchInfoGlobals.PULSE_2_FIRMWARE_CONST_VERSION
                            }
                            DeviceType.NOISE_EVOLVE_2_PLAY.deviceType -> {
                                WatchInfoGlobals.firmwareDeviceId =
                                    WatchInfoGlobals.EVOLVE_PLAY_FIRMWARE_CONST_VERSION
                            }
                        }

                        val last: String? =
                            p0.detailVersion?.substring(
                                p0.detailVersion?.lastIndexOf('.')?.plus(1) ?: 0
                            )

                        LOGS.d("noise_fit_event:rn_command", "FirmwareInfo$last")
                        if (last != null) {
                            WatchInfoGlobals.firmwareVersionNumber = last.toInt()
                        }
                        WatchInfoGlobals.firmwareVersion = p0.detailVersion
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.FirmwareVersionObtained(
                                DeviceFirmware(version = p0.detailVersion)
                            )
                        )

                    }
                }

                override fun onFailure(p0: Throwable?) {

                }
            })
        }
    }


    override fun queryBatteryPower() {

        mClient?.commandBuilder?.batteryInfo?.execute(object : TGCallback<TGBatteryInfo> {
            override fun onSuccess(p0: TGBatteryInfo?) {
                if (p0 != null) {

                    var isCharging = false
                    if (p0.type == 1) {
                        isCharging = true
                    }

                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.BatteryDataObtained(
                                BatteryData(percentage = p0.level, isCharging = isCharging)
                            )
                        )

                    }
                }

                override fun onFailure(p0: Throwable?) {

                }

            })

    }

    override fun getUserInfo() {
        if (mClient != null) {
            mClient?.commandBuilder?.profile?.execute(object : TGCallback<TGProfile> {
                override fun onSuccess(p0: TGProfile?) {
                    if (p0 != null) {
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            (
                                    QueryCallback.UserInfoReceived(
                                        UserInfo(
                                            weight = p0.weight,
                                            height = p0.height,
                                            gender = when (p0.gender) {
                                                TGProfile.FEMALE -> {
                                                    Gender.FEMALE.type
                                                }
                                                else -> {
                                                    Gender.MALE.type
                                                }
                                            },
                                            dob = p0.birthday.toString()
                                        )
                                    )
                                    )
                        )
                    }
                }

                override fun onFailure(p0: Throwable?) {

                }

            })
        }
    }


    override fun getWristLiftGesture() {
        if (mClient != null) {
            mClient?.commandBuilder?.raiseWrist?.execute(object : TGCallback<TGRaiseWristConfig> {
                override fun onSuccess(p0: TGRaiseWristConfig?) {
                    if (p0 != null) {
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.WristLiftGestureObtained(
                                WristLiftGesture(
                                    status = p0.isOn,
                                    startMinute = p0.startMinute,
                                    startHour = p0.startHour,
                                    endMinute = p0.stopMinute,
                                    endHour = p0.stopHour
                                )
                            )
                        )


                    }
                }

                override fun onFailure(p0: Throwable?) {

                }

            })
        }
    }

    override fun getMenstrualSettings() {
        mClient?.commandBuilder?.physiologicalCycle?.execute(object :
            TGCallback<TGPhysiologicalCycle> {
            override fun onFailure(p0: Throwable?) {

            }

            override fun onSuccess(p0: TGPhysiologicalCycle?) {
                if (p0 != null) {
                    val menstrualData = MenstrualData(
                        status = p0.isEnable,
                        lastMenstrualDate = DateFormats.formatDateTime(
                            p0.lastDate,
                            DateFormats.dateFormat
                        ),
                        menstrualCycleLength = p0.menstrualCycleDays,
                        menstrualLength = p0.menstrualDuration,
                        ovulationAfter = p0.remindOvulation,
                        ovulationBefore = p0.remindOvulation,
                        ovulationInterval = p0.remindOvulation,
                        menstrualReminder = MenstrualData.MenstrualReminder(
                            remindStartDayBefore = p0.remindMenstrual,
                            remindOvulationDayBefore = p0.remindOvulation,
                            reminderTime = "${p0.remindHour}:${p0.remindMinute}"
                        )
                    )
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.MenstrualSettingsObtained(
                            menstrualData
                        )
                    )
                }
            }

        })

    }

    override fun getMusicControlSettings() {
        if (mClient != null) {
            mClient?.commandBuilder?.musicOnOff?.execute(object : TGCallback<Boolean> {
                override fun onSuccess(p0: Boolean?) {
                    if (p0 != null) {
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.MusicControl(
                                SwitchSetting(status = p0)
                            )
                        )

                    }
                }

                override fun onFailure(p0: Throwable?) {

                }

            })
        }
    }

    override fun getDoNotDisturbData() {
        if (mClient != null) {
            mClient?.commandBuilder?.notDisturbMode?.execute(object :
                TGCallback<TGNotDisturbConfig> {
                override fun onSuccess(p0: TGNotDisturbConfig?) {
                    if (p0 != null) {
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.DoNotDisturbObtained(
                                DoNotDisturb(
                                    status = p0.isOn,
                                    startHour = p0.startHour,
                                    startMinute = p0.startMinute,
                                    endHour = p0.stopHour,
                                    endMinute = p0.stopMinute
                                )
                            )
                        )

                    }
                }

                override fun onFailure(p0: Throwable?) {

                }

            })
        }
    }


    override fun queryFirmwareUpgrade() {

//        val response = FirmwareUpgradeHandler.checkForNewVersion(
//            Evolve2Globals.firmwareVersionNumber,
//            Evolve2Globals.firmwareDeviceId
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
//                    Evolve2Globals.firmwareUrl = response.data?.url
//                }
//                else -> {
//                    deviceFirmware = DeviceFirmware(status = "update_not_available")
//                }
//            }
//        }
//        queryDeviceDataCallbacks?.onFirmwareUpgradeAvailable(deviceFirmware)
    }

    override fun queryFirmwareUpgradeNew(colorFitDevice: ColorFitDevice) {

    }


    private fun getVersionFromString(version: String): Int {
        val versionString = version.split(".").last()
        return versionString.toInt()
    }


    override fun getAlarms() {

        if (mClient != null)
            mClient?.commandBuilder?.syncAlarms()?.execute(object : TGCallback<List<TGAlarm>> {
                override fun onSuccess(p0: List<TGAlarm>?) {
                    val alarms = dataConverter.formatAlarmData(p0 as ArrayList<TGAlarm>)
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.AlarmsObtained(
                            alarms
                        )
                    )


                }

                override fun onFailure(p0: Throwable?) {

                }

            })
    }

    override fun getAutoSleep() {
        //BluetoothSDK.getAutoSleep(resultCallBack)
    }


    override fun getHeartRateInterval() {
        if (mClient != null) {
            mClient?.commandBuilder?.heartRateMonitoringMode?.execute(object :
                TGCallback<TGHeartRateMonitoringModeConfig> {
                override fun onSuccess(p0: TGHeartRateMonitoringModeConfig?) {


                    var startHour = p0?.startHour ?: 0
                    val startMinute = p0?.startMinute ?: 0
                    var stopHour = p0?.stopHour ?: 0
                    val stopMinute = p0?.stopMinute ?: 0

                    var status = false
                    if (p0?.isHasRange == true) {
                        status = true
                    }

                    if (!status && startHour == 0 && stopHour == 0) {
                        startHour = DefaultStartHour
                        stopHour = DefaultStopHour
                    }
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.HeartRateIntervalObtained(
                            HeartRateInterval(
                                status = status,
                                startTime = "${dataConverter.appendPrefixToTime(startHour)}:${
                                    dataConverter.appendPrefixToTime(
                                        startMinute
                                    )
                                }",
                                endTime = "${dataConverter.appendPrefixToTime(stopHour)}:${
                                    dataConverter.appendPrefixToTime(
                                        stopMinute
                                    )
                                }",
                                interval = p0?.interval ?: 0
                            )
                        )
                    )


                }

                override fun onFailure(p0: Throwable?) {

                }

            })
        }
    }

    override fun getHeartRateAlert() {
        if (mClient != null) {
            mClient?.commandBuilder?.heartRateRange?.execute(object :
                TGCallback<TGHeartRateRangeConfig> {
                override fun onSuccess(p0: TGHeartRateRangeConfig?) {
                    if (p0 != null) {
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.HeartRateAlertDataObtained(
                                HeartRateAlert(
                                    status = watchDataStore.getHeartRateStatus(),
                                    min_hr = p0.minHr,
                                    max_hr = p0.maxHr
                                )
                            )
                        )

                    }
                }

                override fun onFailure(p0: Throwable?) {

                }

            })
        }
    }

    override fun getSedentaryData() {
        if (mClient != null) {
            mClient?.commandBuilder?.sedentary?.execute(object : TGCallback<TGSedentaryConfig> {
                override fun onSuccess(p0: TGSedentaryConfig?) {
                    val longSit = p0?.let { dataConverter.parseSedentaryData(it) }
                    if (longSit != null) {
                        LOGS.d("setSedentaryData get data $longSit")
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.SedentaryDataObtained(longSit)
                        )
                    }
                }

                override fun onFailure(p0: Throwable?) {

                }

            })
        }
    }

    override fun getDeviceUnits() {
        //BluetoothSDK.getUnit(resultCallBack)
//        if (mClient != null) {
//            mClient?.commandBuilder?.unit?.execute(object : TGCallback<TGUnitConfig> {
//                override fun onSuccess(p0: TGUnitConfig?) {
//                    if (p0 != null) {
//
//                        queryDeviceDataCallbacks?.onDeviceUnitsObtained(
//                            DeviceUnits(
//                                unitSystem = when (p0?.distance) {
//                                    TGUnitConfig.METRIC_SYSTEM -> UnitSystem.METRIC.type
//                                    else -> UnitSystem.IMPERIAL.type
//                                }, timeFormat = when (p0?.timeMode) {
//                                    TGUnitConfig.HOUR24 -> TimeFormats.HOURS_24.type
//                                    else -> TimeFormats.HOURS_12.type
//                                }
//                            )
//                        )
//                    }
//                }
//
//                override fun onFailure(p0: Throwable?) {
//
//                }
//
//            })
//        }
    }


    override fun getCustomReplies() {
        testQueryDeviceDataCallback?.onQueryDataReceived(
            QueryCallback.CustomReplyObtained(
                watchDataStore.getCustomReplies() ?: CustomReplyData(
                    customReplies = ArrayList()
                )
            )
        )

    }

    override fun getContactList() {
        testQueryDeviceDataCallback?.onQueryDataReceived(
            QueryCallback.ContactListObtained(
                watchDataStore.getContactNumberList()
            )
        )
    }

    //
    override fun getCameraSwitchSettings() {
        if (mClient != null) {
            mClient?.commandBuilder?.cameraOnOff?.execute(object : TGCallback<Boolean> {
                override fun onSuccess(p0: Boolean?) {
                    if (p0 != null)
                        testQueryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.CameraSwitchObtained(SwitchSetting(status = p0))
                        )

                }

                override fun onFailure(p0: Throwable?) {

                }

            })
        }
    }


    override fun getDrinkWaterSettings() {//seconds
        mClient?.commandBuilder?.remindDrinking?.execute(object : TGCallback<TGRemindDrinking> {
            override fun onSuccess(p0: TGRemindDrinking?) {
                val water = p0?.let { dataConverter.parseDrinkWater(it) }
                if (water != null) {
                    LOGS.d("setDrinkWaterReminder get $water")
                    testQueryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.DrinkWaterDataObtained(water)
                    )
                }
            }

            override fun onFailure(p0: Throwable?) {

            }

        })
    }


}