package com.noisefit_ryeex_sdk.handler

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
import com.noisefit_commans.BuildConfig
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.handler.MusicControlActionsEvents
import com.noisefit_commans.interfaces.IQueryDataCallback
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.models.*
import com.noisefit_commans.models.SwitchSetting
import com.noisefit_commans.utils.*
import com.noisefit_ryeex_sdk.base.RyeexApplicationHandler
import com.noisefit_ryeex_sdk.dataConversion.DataConverter
import com.noisefit_ryeex_sdk.dataConversion.removeBond
import com.noisefit_ryeex_sdk.utils.ZipUtil
import com.ryeex.ble.common.model.entity.*
import com.ryeex.ble.common.utils.FileUtil
import com.ryeex.ble.connector.callback.AsyncBleCallback
import com.ryeex.ble.connector.error.BleError
import com.ryeex.ble.connector.handler.BleHandler
import com.ryeex.ble.connector.utils.BleUtil
import com.ryeex.watch.adapter.device.IWatchDeviceRequestListener
import com.ryeex.watch.adapter.device.WatchDevice
import com.ryeex.watch.adapter.model.entity.*
import com.ryeex.watch.adapter.model.entity.Contact
import com.ryeex.watch.adapter.model.entity.Sport.Pause
import com.ryeex.watch.adapter.model.entity.Sport.UpdateResult
import java.io.File
import javax.inject.Inject

private const val TAG = "RyeexQueryDeviceUnitsHandler"

class RyeexQueryDeviceUnitsHandler
@Inject constructor(
    private var ryeexApplicationHandler: RyeexApplicationHandler,
    private var dataConverter: DataConverter,
    private var context: Context,
    private val watchDataStore: WatchDataStore
) : QueryDeviceDataActions() {
    private var locationClientClass: LocationClientClass? = null

    companion object {
        const val LOCATION_BROADCAST_RECEIVER = "LOCATION_BROADCAST_RECEIVER"
        const val LAT_LONG = "LAT_LONG"
    }

    private var previousVolume = 0
    private var songName: String? = null
    private var queryDeviceDataCallback: IQueryDataCallback? = null
    private var noiseFitDevice: ColorFitDevice? = null
    private var watchDevice: WatchDevice? = null
    private var totalDistance = 0.0
    private var lastDistance = 0.0
    private var isPause = false
    private var sessionId = 0L

    override fun init() {
        super.init()
        watchDevice = ryeexApplicationHandler.getWatchDevice()
        removeCallbacks()
        attachCallbacks()
    }

    override fun attachCallbacks() {
        watchDevice?.deviceRequestListener = object : IWatchDeviceRequestListener {

            override fun onUnbind() {
                LOGS.i(TAG, "deviceRequest onUnbind")
                watchDataStore.setRyeexWatchToken("")
                noiseFitDevice?.removeBond()
                ryeexApplicationHandler.setWatchDevice(null)
                ryeexApplicationHandler.connectionCallbacks?.onConnect(
                    ConnectState.DisconnectSuccess(
                        noiseFitDevice
                    )
                )
            }

            override fun onFinePhone(findPhoneAlert: FindPhoneAlert) {
                queryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.OpenFindMyPhoneActivity(
                        findPhoneAlert.alertType != FindPhoneAlert.AlertType.ABORT
                    )
                )
            }

            override fun onWeatherRefresh(
                cityIds: List<String>, responseCallback: AsyncBleCallback<WeatherInfo, BleError>?
            ) {
                LOGS.d(TAG, "onWeatherRefresh")
            }

            override fun onMusicControl(musicControl: LibMusicControl) {
//                LOGS.i(TAG, "onMusicControl musicControl=${Gson().toJson(musicControl)}")
                musicControl.cmd?.let {
                    when (it) {
                        LibMusicControl.Cmd.PAUSE -> {
                            setPlayPauseMusic(false)
                            queryDeviceDataCallback?.onQueryDataReceived(
                                QueryCallback.OnMusicEventChanged(
                                    MusicControlActionsEvents.PAUSE
                                )
                            )
                        }
                        LibMusicControl.Cmd.PLAY -> {
                            setPlayPauseMusic(true)
                            queryDeviceDataCallback?.onQueryDataReceived(
                                QueryCallback.OnMusicEventChanged(
                                    MusicControlActionsEvents.PLAY
                                )
                            )
                        }
                        LibMusicControl.Cmd.PREVIOUS -> {
                            setMusicPlayerState()
                            queryDeviceDataCallback?.onQueryDataReceived(
                                QueryCallback.OnMusicEventChanged(
                                    MusicControlActionsEvents.PREVIOUS
                                )
                            )
                        }
                        LibMusicControl.Cmd.NEXT -> {
                            setMusicPlayerState()
                            queryDeviceDataCallback?.onQueryDataReceived(
                                QueryCallback.OnMusicEventChanged(
                                    MusicControlActionsEvents.NEXT
                                )
                            )
                        }
                        LibMusicControl.Cmd.VOLUME -> {
                            if (musicControl.volume == 0) {
                                updateVolume(false)
                            } else {
                                updateVolume(true)
                            }

                        }
                    }
                }
            }

            override fun onCheckPhoneLocation(callback: AsyncBleCallback<PhoneLocation, BleError>?) {
                LOGS.d(TAG, "onCheckPhoneLocation")
                val phoneLocation = PhoneLocation()
                phoneLocation.isGpsOpen = hasLocationPermission()
                callback?.sendSuccessMessage(phoneLocation)
            }

            override fun onSportStart(
                start: Sport.Start, callback: AsyncBleCallback<Void, BleError>?
            ) {
//                LOGS.d(TAG, "onSportStart" + Gson().toJson(start))
                totalDistance = 0.0
                lastDistance = 0.0
                sessionId = start.sessionId.toLong()
                enableLocation()
                callback?.sendSuccessMessage(null)
            }

            override fun onSportUpdate(
                update: Sport.Update, callback: AsyncBleCallback<UpdateResult, BleError>?
            ) {
                enableLocation()
                sessionId = update.sessionId.toLong()

                val updateResult = UpdateResult()
                updateResult.isGpsOpen = hasLocationPermission()
                updateResult.distance = (totalDistance - lastDistance).toFloat()
                updateResult.sessionId = update.sessionId
                callback?.sendSuccessMessage(updateResult)
                lastDistance = totalDistance
//                LOGS.d(
//                    TAG,
//                    "onSportUpdate totalDistance=$totalDistance " + Gson().toJson(updateResult)
//                )
//                AppLogs.sendAppLogs(
//                    "$TAG onSportUpdate totalDistance=$totalDistance " + Gson().toJson(
//                        updateResult
//                    )
//                )
            }

            override fun onSportPause(pause: Pause, callback: AsyncBleCallback<Void, BleError>?) {
//                LOGS.d(TAG, "onSportPause" + Gson().toJson(pause))
                isPause = true
                enableLocation()
                callback?.sendSuccessMessage(null)
            }

            override fun onSportResume(
                resume: Sport.Resume, callback: AsyncBleCallback<Void, BleError>?
            ) {
//                LOGS.d(TAG, "onSportResume" + Gson().toJson(resume))
                isPause = false
                enableLocation()
                callback?.sendSuccessMessage(null)
            }

            override fun onSportStop(
                stop: Sport.Stop, callback: AsyncBleCallback<Void, BleError>?
            ) {
//                LOGS.d(TAG, "onSportStop" + Gson().toJson(stop))
                disableLocation()

                callback?.sendSuccessMessage(null)
            }

            override fun onCamera(state: CameraState) {
                LOGS.d(TAG, "onCamera state=$state")
                when (state) {
                    CameraState.ENTER -> queryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.OpenCameraActivity(
                            false
                        )
                    )
                    CameraState.CLICK -> queryDeviceDataCallback?.onQueryDataReceived(QueryCallback.ClickCameraImage)
                    CameraState.EXIT -> queryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.CloseCameraShutterActivity(
                            true
                        )
                    )
                }
            }

            override fun onRejectCall() {
                LOGS.d(TAG, "onRejectCall")
                queryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.UpdateCallStatus(
                        false
                    )
                )
            }

            override fun onMuteCall(number: String?) {
                LOGS.d(TAG, "onMuteCall: $number")
                queryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.MuteDevice()
                )
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
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

    private var locationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val locationArrayList =
                intent.getParcelableArrayListExtra<LocationDataModel>(LAT_LONG)

            if (locationArrayList.isNullOrEmpty()) {
                return
            }

            locationArrayList.forEach {
                it.isRunning = isPause

            }

            val locationList = watchDataStore.saveAndGetLocation(sessionId, locationArrayList)
            totalDistance = DistanceUtil.getCalculatedDistanceWithPause(locationList)
            LOGS.d(TAG, "onSportUpdate $totalDistance ")

//            LOGS.d(
//                TAG,
//                "locationReceiver locationModel= $totalDistance ${Gson().toJson(locationList)}"
//            )


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


    }


    private fun syncMusic(musicName: String, isPlay: Boolean, maxVolume: Int, currentVolume: Int) {


        LOGS.d(TAG, "setMusicStatus status=$isPlay title=$musicName ")

        val musicInfo = LibMusicInfo()
        musicInfo.title = musicName
        musicInfo.maxVolume = maxVolume
        musicInfo.currentVolume = currentVolume
        musicInfo.duration = 0L
        musicInfo.playState = if (isPlay) {
            LibMusicInfo.PlayState.PLAY
        } else {
            LibMusicInfo.PlayState.PAUSE
        }
        watchDevice?.pushMusicInfo(musicInfo, object : AsyncBleCallback<Void, BleError>() {
            override fun onSuccess(p0: Void?) {
                LOGS.d(TAG, "setMusicStatus onSuccess")

            }

            override fun onFailure(error: BleError?) {
                LOGS.e(TAG, "setMusicStatus onFailure:$error")

            }
        })

    }

    private fun setPlayPauseMusic(isPlay: Boolean) {

        val musicName = MusicUtil.getSongName(songName)


        val volume = returnVolume()

        syncMusic(musicName, isPlay, volume.second, volume.first)
    }

    private fun volume(volume: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val type = AudioManager.STREAM_MUSIC
        val flag = AudioManager.FLAG_PLAY_SOUND
        // 获取最小音量
        val minVolume = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            audioManager.getStreamMinVolume(type)
        } else {
            0
        }
        // 获取最大音量
        val maxVolume = audioManager.getStreamMaxVolume(type)
        // 计算新音量
        val newVolume = when {
            volume < minVolume -> minVolume
            volume > maxVolume -> maxVolume
            else -> volume
        }
        // 设置音量
        audioManager.setStreamVolume(type, newVolume, flag)
    }

    override fun removeCallbacks() {
        watchDevice?.deviceRequestListener = null
    }

    //9945
    override fun queryFirmwareVersion() {


        //  val version = "0.1.0.115"
        var isFullRequired = false
        if (watchDevice?.checkStatus == 1) {
            isFullRequired = true
        }
        val firmwareVersion = watchDevice?.version?.replace(" ", "")
        WatchInfoGlobals.firmwareVersion = firmwareVersion
        WatchInfoGlobals.firmwareFullRequired = isFullRequired
        WatchInfoGlobals.firmwareVersionNumber = try {
            firmwareVersion?.replace(".", "")?.toIntOrNull() ?: 0
        } catch (exp: Exception) {
            exp.printStackTrace()
            0
        }
        WatchInfoGlobals.firmwareDeviceId = getDeviceId()

        LOGS.d("mighty_firmware_id ${WatchInfoGlobals.firmwareDeviceId}")
        queryDeviceDataCallback?.onQueryDataReceived(
            QueryCallback.FirmwareVersionObtained(DeviceFirmware(version = firmwareVersion))
        )
    }

    private fun getDeviceId(): Int {
        return 10000 + when (noiseFitDevice?.deviceType) {
            DeviceType.COLORFIT_MIGHTY.deviceType -> 39
            DeviceType.NOISEFIT_NOVA.deviceType -> 35
            else -> 0
        }
    }

    override fun queryFirmwareUpgrade() {

    }

    override fun queryFirmwareUpgradeNew(colorFitDevice: ColorFitDevice) {

    }

    override fun queryBatteryPower() {
        watchDevice?.getDeviceProperty(object : AsyncBleCallback<DeviceProperty, BleError>() {
            override fun onSuccess(deviceProperty: DeviceProperty?) {
                queryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.BatteryDataObtained(
                        BatteryData(percentage = deviceProperty?.power)
                    )
                )
            }

            override fun onFailure(p0: BleError?) {

            }
        })
    }

    override fun getUserInfo() {

    }

    override fun getAppList() {
        watchDevice?.getDeviceAppList(object : AsyncBleCallback<MutableList<Int>, BleError>() {
            override fun onSuccess(appList: MutableList<Int>?) {

                appList?.let {
                    val list = mutableListOf<Widget>()
                    for (i in it.indices) {

                        val app = Widget(
                            functionId = it[i],
                            name = dataConverter.getAppName(it[i]),
                            isEnable = true,
                            haveHide = false,
                            sortable = true,
                            order = i
                        )
                        if (app.name.isNotEmpty()) {
                            list.add(app)
                        }
                    }
                    LOGS.d(TAG, "getWidgetList onSuccess ${list.size}")
                    queryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.AppListObtained(
                            list
                        )
                    )
                }
            }

            override fun onFailure(error: BleError?) {
                LOGS.e(TAG, "getWidgetList onFailure:$error")
                queryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.AppListObtained(
                        mutableListOf()
                    )
                )
            }
        })

    }

    override fun getAlarms() {

    }

    override fun getHeartRateInterval() {

    }

    override fun getSedentaryData() {
        watchDevice?.getSitRemindSetting(object : AsyncBleCallback<SitRemindSetting, BleError>() {
            override fun onSuccess(sitRemindSetting: SitRemindSetting?) {
                LOGS.d(TAG, "getSitRemindSetting=$sitRemindSetting")
                sitRemindSetting?.let {
                    queryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.SedentaryDataObtained(
                            SedentaryData(
                                status = it.isEnable,
                                startHour = it.startTimeHour,
                                startMinute = it.startTimeMinute,
                                endHour = it.endTimeHour,
                                endMinute = it.endTimeMinute,
                                interval = it.timeThreshold / 60
                            )
                        )
                    )
                }
            }

            override fun onFailure(p0: BleError?) {

            }

        })
    }

    override fun getUserGoals() {
        LOGS.d(TAG, "getUserGoals")
        watchDevice?.getTargetStep(object : AsyncBleCallback<Int, BleError>() {
            override fun onSuccess(stepGoal: Int?) {
                QueryCallback.UserGoalsObtained(UserGoals(stepGoal = stepGoal ?: 0))
            }

            override fun onFailure(p0: BleError?) {
            }
        })

        watchDevice?.getTargetCalorie(object : AsyncBleCallback<Int, BleError>() {
            override fun onSuccess(caloriesGoal: Int?) {
                QueryCallback.UserGoalsObtained(
                    UserGoals(
                        caloriesGoal = caloriesGoal ?: 0
                    )
                )
            }

            override fun onFailure(p0: BleError?) {
            }
        })

        watchDevice?.getTargetDistance(object : AsyncBleCallback<Int, BleError>() {
            override fun onSuccess(distanceGoal: Int?) {
                QueryCallback.UserGoalsObtained(
                    UserGoals(
                        distanceGoal = distanceGoal ?: 0
                    )
                )
            }

            override fun onFailure(p0: BleError?) {
            }
        })
    }

    override fun getDrinkWaterSettings() {
        LOGS.d(TAG, "getDrinkWaterSettings")
        watchDevice?.getDrinkWaterRemindSetting(object :
            AsyncBleCallback<DrinkWaterRemindSetting, BleError>() {
            override fun onSuccess(drinkWaterRemindSetting: DrinkWaterRemindSetting?) {
                LOGS.i(TAG, "getDrinkWaterRemindSetting onSuccess:$drinkWaterRemindSetting")
                drinkWaterRemindSetting?.let {
                    queryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.DrinkWaterDataObtained(
                            SedentaryData(
                                status = it.isEnable,
                                interval = it.interval / 60,
                                startHour = it.startTimeHour,
                                startMinute = it.startTimeMinute,
                                endHour = it.endTimeHour,
                                endMinute = it.endTimeMinute,
                            )
                        )
                    )
                }
            }

            override fun onFailure(error: BleError?) {
                LOGS.e(TAG, "getDrinkWaterRemindSetting onFailure:$error")
            }
        })
    }


    override fun getHeartRateAlert() {
        watchDevice?.getHeartRateDetect(object : AsyncBleCallback<HeartRateSetting, BleError>() {
            override fun onSuccess(heartRateSetting: HeartRateSetting?) {
//                LOGS.i(TAG, "getHeartRateAlert onSuccess=${Gson().toJson(heartRateSetting)}")
                heartRateSetting?.let {
                    queryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.HeartRateAlertDataObtained(
                            HeartRateAlert(
                                status = it.isEnable, min_hr = it.min, max_hr = it.max
                            )
                        )
                    )
                }
            }

            override fun onFailure(error: BleError?) {
                LOGS.e(TAG, "getHeartRateAlert onFailure:$error")
            }
        })
    }

    override fun getSportModeInfo() {
        LOGS.d(TAG, "getSportModeInfo")
        watchDevice?.getSportList(object : AsyncBleCallback<MutableList<Int>, BleError>() {
            override fun onSuccess(sportIds: MutableList<Int>?) {
                LOGS.d(TAG, "getSportModeInfo sportIds=$sportIds")
                queryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.SportModeInfoObtained(
                        dataConverter.parseSportsModeInfo(sportIds ?: mutableListOf())
                    )
                )
            }

            override fun onFailure(error: BleError?) {
                LOGS.e(TAG, "getSportModeInfo onFailure=$error")
                queryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.SportModeInfoObtained(
                        dataConverter.parseSportsModeInfo(mutableListOf())
                    )
                )
            }
        })
    }

    override fun getMusicControlSettings() {
        LOGS.d(TAG, "getMusicControlSettings")
        watchDevice?.getSwitchSetting(object :
            AsyncBleCallback<List<com.ryeex.watch.adapter.model.entity.SwitchSetting>, BleError>() {
            override fun onSuccess(list: List<com.ryeex.watch.adapter.model.entity.SwitchSetting>) {
                val switchSetting = list.find {
                    it.type == com.ryeex.watch.adapter.model.entity.SwitchSetting.Type.MUSIC_CONTROL
                }
                switchSetting?.let {
                    queryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.MusicControl(
                            SwitchSetting(status = it.isEnable)
                        )
                    )
                }
            }

            override fun onFailure(error: BleError?) {
                LOGS.e(TAG, "getMusicControlSettings onFailure:$error")
                queryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.MusicControl(
                        SwitchSetting(status = false)
                    )
                )
            }
        })
    }


    private fun returnVolume(): Pair<Int, Int> {
        val audioManager =
            NoisefitApplication.context?.applicationContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val sb2value = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val cur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return Pair(cur, sb2value)
    }

    override fun getWidgetList() {
    }

    override fun getContactList() {
        watchDevice?.getContactList(object : AsyncBleCallback<List<Contact>, BleError>() {
            override fun onSuccess(contacts: List<Contact>?) {
                contacts?.let {
                    val list = mutableListOf<com.noisefit_commans.models.Contact>()
                    it.forEach { contact ->
                        list.add(
                            com.noisefit_commans.models.Contact(
                                id = contact.number,
                                name = contact.name,
                                true,
                                null,
                                arrayListOf(contact.number)
                            )
                        )
                    }
                    queryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.ContactListObtained(
                            list
                        )
                    )
                }
            }

            override fun onFailure(error: BleError?) {
                LOGS.e(TAG, "getContactList onFailure:$error")
                queryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.ContactListObtained(
                        mutableListOf()
                    )
                )
            }
        })
    }

    override fun getBleCallingSwitch() {
        LOGS.i(TAG, "getBleCallingSwitch")
        val device = BleUtil.getSystemBondedDevices().find {
            watchDevice?.mac == it.address
        }
        if (device == null) {
            LOGS.i(TAG, "getBleCallingSwitch have no bound and set false")
            val list = mutableListOf<com.ryeex.watch.adapter.model.entity.SwitchSetting>()
            val switchSetting = com.ryeex.watch.adapter.model.entity.SwitchSetting()
            switchSetting.isEnable = false
            switchSetting.type = com.ryeex.watch.adapter.model.entity.SwitchSetting.Type.BT_CALL
            list.add(switchSetting)
            watchDevice?.setSwitchSetting(list, object : AsyncBleCallback<Void, BleError>() {
                override fun onSuccess(p0: Void?) {
                    queryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.BleCallingSwitchObtained(
                            false
                        )
                    )
                }

                override fun onFailure(p0: BleError?) {
                    queryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.BleCallingSwitchObtained(
                            false
                        )
                    )
                }
            })
            return
        }

        watchDevice?.getSwitchSetting(object :
            AsyncBleCallback<List<com.ryeex.watch.adapter.model.entity.SwitchSetting>, BleError>() {
            override fun onSuccess(list: List<com.ryeex.watch.adapter.model.entity.SwitchSetting>) {
//                LOGS.i(TAG, "getBleCallingSwitch " + Gson().toJson(list))
                val switchSetting = list.find {
                    it.type == com.ryeex.watch.adapter.model.entity.SwitchSetting.Type.BT_CALL
                }
                switchSetting?.let {
                    queryDeviceDataCallback?.onQueryDataReceived(
                        QueryCallback.BleCallingSwitchObtained(
                            it.isEnable
                        )
                    )
                }
            }

            override fun onFailure(error: BleError?) {
                LOGS.e(TAG, "getBleCallingSwitch onFailure:$error")
                queryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.BleCallingSwitchObtained(
                        false
                    )
                )
            }
        })
    }


    override fun getFirmwareLogs() {
        LOGS.d("getFirmwareLogs")
        AppLogs.sendAppLogs("$TAG getFirmwareLogs")
        val file = FileLogsUtils.getFile(context, noiseFitDevice, "ryeex_device_log")
        file?.mkdirs()
        if (file?.exists() == false) {
            LOGS.d("$TAG getFirmwareLogs logFile is not exist")
            AppLogs.sendAppLogs("$TAG getFirmwareLogs logFile is not exist")
            BleHandler.getUiHandler().post {
                queryDeviceDataCallback?.onQueryDataReceived(
                    QueryCallback.FirmwareLogObtained(
                        ""
                    )
                )
            }
            return
        }
        file?.let {
            val logFile = File(it.absolutePath + ".zip")
            AppLogs.sendAppLogs("$TAG getFirmwareLogs logFile:${logFile.name}")
            watchDevice?.getDeviceLogFile(
                it.absolutePath,
                object : AsyncBleCallback<String, BleError>() {
                    override fun onSuccess(path: String?) {
                        AppLogs.sendAppLogs("$TAG getFirmwareLogs onSuccess:$path")
                        BleHandler.getWorkerHandler().post {
                            ZipUtil.zip(logFile, it)
                            FileUtil.deleteDirectory(it.absolutePath, true)
                            BleHandler.getUiHandler().post {
                                queryDeviceDataCallback?.onQueryDataReceived(
                                    QueryCallback.FirmwareLogObtained(
                                        logFile.name
                                    )
                                )
                            }
                        }
                    }

                    override fun onFailure(error: BleError?) {
                        AppLogs.sendAppLogs("$TAG getFirmwareLogs onFailure:$error")
                        queryDeviceDataCallback?.onQueryDataReceived(
                            QueryCallback.FirmwareLogObtained(
                                logFile.absolutePath
                            )
                        )
                    }
                })
        }
    }

    override fun getWeatherSwitchStatus() {
//        watchDevice?.getWeatherNotifyStatus()
        queryDeviceDataCallback?.onQueryDataReceived(
            QueryCallback.WeatherSwitchStatus(SwitchSetting(status = true))
        )
    }



    override fun setDevice(colorFitDevice: ColorFitDevice) {
        noiseFitDevice = colorFitDevice
    }

    override fun <T> callbackListener(callback: T) {

    }

    override fun <T> callbackListenerNew(callback: T) {
        queryDeviceDataCallback = callback as IQueryDataCallback
    }
}