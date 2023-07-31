package com.noisefit_cf2.handler

import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.ido.ble.BLEManager
import com.ido.ble.LocalDataManager
import com.ido.ble.business.sync.SyncPara
import com.ido.ble.callback.AppSendDataCallBack
import com.ido.ble.callback.BindCallBack
import com.ido.ble.callback.DeviceResponseCommonCallBack
import com.ido.ble.callback.SettingCallBack
import com.ido.ble.dfu.BleDFUConfig
import com.ido.ble.dfu.BleDFUState
import com.ido.ble.file.transfer.FileTransferConfig
import com.ido.ble.file.transfer.IFileTransferListener
import com.ido.ble.protocol.model.*
import com.ido.ble.protocol.model.Units
import com.ido.ble.protocol.model.WeatherInfo.WeatherFutureInfo
import com.ido.ble.watch.custom.WatchPlateSetConfig
import com.ido.ble.watch.custom.callback.WatchPlateCallBack
import com.ido.ble.watch.custom.model.DialPlateParam
import com.ido.ble.watch.custom.model.WatchPlateFileInfo
import com.ido.ble.watch.custom.model.WatchPlateScreenInfo
import com.noisefit_cf2.base.CF2Globals
import com.noisefit_cf2.dataconversions.Colorfit2DataConverter
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.constants.WatchFaceEventsConstants
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.interfaces.device_data.IUpdateDeviceDataCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.*
import com.noisefit_commans.models.HeartRateInterval
import com.noisefit_commans.models.UserInfo
import com.noisefit_commans.utils.*
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt


class CF2UpdateDeviceUnitsHandler @Inject constructor(
    private val watchDataStore: WatchDataStore
) :
    UpdateDeviceDataActions() {

    private lateinit var cloudWatchFace: WatchFace
    private val fileNameToBeSet =
        "${NoisefitApplication.context!!.getExternalFilesDir(null)}/customfaces/colorfit_pro_2/fileToSet.png"
    private val fileNameToBeSetPro3 =
        "${NoisefitApplication.context!!.getExternalFilesDir(null)}/customfaces/colorfit_pro_3/fileToSet.png"
    private val fileNameToBeSetPro2Oxy =
        "${NoisefitApplication.context!!.getExternalFilesDir(null)}/customfaces/colorfit_pro_2_oxy/fileToSet.png"
    private val fileNameToBeSetNoisefitActive =
        "${NoisefitApplication.context!!.getExternalFilesDir(null)}/customfaces/noisefit_active/fileToSet.png"
    private val fileNameToBeSetNoisefitAgile =
        "${NoisefitApplication.context!!.getExternalFilesDir(null)}/customfaces/noisefit_agile/fileToSet.png"

    private var currentDialPlate: String = ""

    private var testUpdateDeviceDataCallback: IUpdateDeviceDataCallback? = null
    private var colorFitDevice: ColorFitDevice? = null


    override fun init() {
        super.init()
    }

    override fun <T> callbackListener(callback: T) {}
    override fun <T> callbackListenerNew(callback: T) {
        testUpdateDeviceDataCallback = callback as IUpdateDeviceDataCallback
    }

    override fun setDevice(device: ColorFitDevice) {
        colorFitDevice = device
    }

    override fun attachCallbacks() {
        super.attachCallbacks()
        removeCallbacks()
        BLEManager.registerSettingCallBack(settingsCallBack)
        BLEManager.registerDeviceResponseCommonCallBack(deviceResponseCallback)
        BLEManager.registerWatchOperateCallBack(watchOperateCallback)
        BLEManager.registerBindCallBack(bindCallBack)
        BLEManager.registerAppSendDataCallBack(appSendDataCallBack)
    }

    override fun removeCallbacks() {
        super.removeCallbacks()
        BLEManager.unregisterSettingCallBack(settingsCallBack)
        BLEManager.unregisterDeviceResponseCommonCallBack(deviceResponseCallback)
        BLEManager.unregisterWatchOperateCallBack(watchOperateCallback)
        BLEManager.unregisterBindCallBack(bindCallBack)
        BLEManager.unregisterAppSendDataCallBack(appSendDataCallBack)
    }

    override fun setCallBacks() {
        LOGS.d("noise_fit_event:colorfit_pro call back setup :")
        setOutdoorActivitiesPro3()
    }


    override fun setDeviceUnits(units: DeviceUnits) {
        LOGS.d("setDeviceUnits $units")
        val unit = Colorfit2DataConverter.formatUnits(units)
        if (BLEManager.isConnected()) {
            BLEManager.setUnit(unit)
        } else {
            BLEManager.setUnitPending(unit)
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.DeviceUnitsUpdated(
                    true
                )
            )
        }
    }

    override fun setBrightnessLevel(level: Int) {
        if (BLEManager.isConnected()) {
            BLEManager.setScreenBrightnessLevel(level * 20)
        } else {
            BLEManager.setScreenBrightnessLevelPending(level)
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.BrightnessLevelUpdated(
                    true
                )
            )
        }

        watchDataStore.updateScreenBrightness(level)
    }

    override fun setUserInfo(userInfo: UserInfo, userGoals: UserGoals, userName: String?) {
        try {
            val deviceUserInfo = Colorfit2DataConverter.formatUserInfo(userInfo)
            if (BLEManager.isConnected()) {
                BLEManager.setUserInfo(deviceUserInfo)
            } else {
                BLEManager.setUserInfoPending(deviceUserInfo)
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.UserInfoUpdated(
                        true
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val goal = Goal()
        goal.sport_step = userGoals.stepGoal
        //goal.sleep_time = goals.sleepGoal
        val otherGoals = CalorieAndDistanceGoal()
        otherGoals.calorie = userGoals.caloriesGoal
        otherGoals.distance = userGoals.distanceGoal

        if (BLEManager.isConnected()) {
            BLEManager.setGoal(goal)
            BLEManager.setCalorieAndDistanceGoal(otherGoals)
        } else {
            BLEManager.setGoalPending(goal)
            BLEManager.setCalorieAndDistanceGoalPending(otherGoals)

        }
    }

    override fun updateAlarm(alarm: AlarmsList, alarmAction: AlarmAction) {
        if (BLEManager.isConnected()) {
            colorFitDevice?.deviceType?.let { deviceType ->
                if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType || deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType

                ) {

                    val listAlarmV3 = Colorfit2DataConverter.formatAlarmsV3(alarm, true)
                    BLEManager.setAlarmV3(listAlarmV3)
                } else if (deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType
                    || deviceType == DeviceType.NOISEFIT_AGILE.deviceType
                ) {
                    val listAlarmV3 = Colorfit2DataConverter.formatAlarmsV3(alarm, true)
                    BLEManager.setAlarmV3(listAlarmV3)
                } else {
                    val listAlarm = Colorfit2DataConverter.formatAlarms(alarm)
                    BLEManager.setAlarm(listAlarm)
                }
            }
        } else {
            val listAlarm = Colorfit2DataConverter.formatAlarms(alarm)
            BLEManager.setAlarmPending(listAlarm)
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.AlarmUpdated(
                    true
                )
            )
        }
    }

    override fun updateLanguage(language: Language) {
        val units = LocalDataManager.getUnits()
        units.language = when (language.language) {
            DeviceLanguage.CHINESE.type -> Units.LANG_ZH
            DeviceLanguage.HINDI.type -> Units.LANG_HINDI
            else -> Units.LANG_EN
        }
        if (BLEManager.isConnected()) {
            BLEManager.setUnit(units)
        } else {
            BLEManager.setUnitPending(units)
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.LanguageUpdated(
                    true
                )
            )
        }
    }

    override fun findDevice(findDevice: SwitchSetting) {
        when (findDevice.status) {
            true -> BLEManager.startFindDevice()
            else -> BLEManager.stopFindDevice()
        }
    }

    override fun setDeviceDateTime(calender: Calendar, units: TimeFormat) {
        val systemTime = Colorfit2DataConverter.formatSystemTime(calender)
        LOGS.d("ConnectionService", "Settings Time in CF2")
        BLEManager.setTime(systemTime)

        val unit = Colorfit2DataConverter.formatTime(colorFitDevice, units)

        if (BLEManager.isConnected()) {
            BLEManager.setUnit(unit)
        } else {
            BLEManager.setUnitPending(unit)
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.DeviceTimeSynced(
                    true
                )
            )
        }


    }

    override fun setWatchFace(watchFace: WatchFace) {
        when (watchFace.imageType) {
            "in_built" -> {
                val dialPlate = Colorfit2DataConverter.formatDialPlate(watchFace)
                if (BLEManager.isConnected()) {
                    BLEManager.setDialPlate(dialPlate)
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.WatchFaceUpdated(
                            true
                        )
                    )
                } else {
                    BLEManager.setDialPlatePending(dialPlate)
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.WatchFaceUpdated(
                            true
                        )
                    )
                }
            }
            "cloud", "cloud_supplier" -> {
                updateCloudWatchFace(watchFace)
            }
            else -> {
                updateCloudWatchFace(watchFace)
            }
        }
    }

    private val watchOperateCallback = object : WatchPlateCallBack.IOperateCallBack {
        override fun onGetScreenInfo(p0: WatchPlateScreenInfo?) {
            LOGS.d("screen info", p0.toString())
        }

        override fun onSetPlate(p0: Boolean) {
            LOGS.d("set plate", p0.toString())
        }

        override fun onDeletePlate(p0: Boolean) {
            LOGS.d("delete plate", p0.toString())
            colorFitDevice?.deviceType?.let { deviceType ->
                if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType || deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType
                    || deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType
                    || deviceType == DeviceType.NOISEFIT_AGILE.deviceType
                ) {
                    if (p0) {
                        transferCloudWatchFace()
                    }
                }
            }
        }

        override fun onGetDialPlateParam(p0: DialPlateParam?) {

        }

        override fun onGetCurrentPlate(p0: String?) {
            currentDialPlate = p0 ?: ""
        }

        override fun onGetPlateFileInfo(p0: WatchPlateFileInfo?) {
            LOGS.d("get plate file info", p0.toString())
            colorFitDevice?.deviceType?.let { deviceType ->
                if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType || deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType
                    || deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType
                    || deviceType == DeviceType.NOISEFIT_AGILE.deviceType
                ) {
                    var isDeleted = false
                    p0?.fileNameList?.forEach {
                        if (!it.contains("local")) {
                            BLEManager.deleteWatchPlate(it)
                            isDeleted = true
                        }
                    }

                    if (!isDeleted) {
                        transferCloudWatchFace()
                    }
                } else {
                    p0?.fileNameList?.forEach {
                        BLEManager.deleteWatchPlate(it)
                    }
                    transferCloudWatchFace()
                }
            }

            //transferCloudWatchFace()
        }
    }

    fun transferCloudWatchFace() {
        try {
            if (this::cloudWatchFace.isInitialized) {
                val watchFace = cloudWatchFace
                val watchPlateSetConfig = WatchPlateSetConfig()
                watchPlateSetConfig.isOnlyTranslateWatchFile = true
                watchPlateSetConfig.filePath = watchFace.localFilePath?.split("///")?.last()
                watchPlateSetConfig.uniqueID = watchFace.getZipFileName()

                    /*if (watchFace.zipName != null) {
                    if (watchFace.zipName!!.contains(".zip")) watchFace.zipName!!.replace(
                        ".zip",
                        ""
                    ) else watchFace.zipName
                } else {
                    watchFace.faceId
                }*/
                //"watchface_${watchFace.id?.toString() ?: "123"}"
                watchPlateSetConfig.stateListener =
                    object : WatchPlateCallBack.IAutoSetPlateCallBack {
                        override fun onSuccess() {
                            colorFitDevice?.deviceType?.let { deviceType ->
                                if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType || deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType
                                    || deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType
                                    || deviceType == DeviceType.NOISEFIT_AGILE.deviceType
                                ) {
                                    Handler().postDelayed(Runnable {
                                        currentDialPlate =
                                            if (watchPlateSetConfig != null) (watchPlateSetConfig.uniqueID + ".iwf") else ""
                                        BLEManager.setWatchPlate(currentDialPlate)
                                    }, 1000);
                                }
                            }
                            LOGS.d(
                                "noise_fit_event:colorfit_pro_2",
                                "watch_face_change | onTransCompleted"
                            )

                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                    WatchUpdateStatus(status = UpdateStatus.COMPLETED,
                                    wStatus = WatchFaceEventsConstants.Complete)
                                )
                            )
                        }

                        override fun onFailed() {
                            LOGS.d(
                                "noise_fit_event:colorfit_pro_2",
                                "watch_face_change | onError"
                            )
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                    WatchUpdateStatus(status = UpdateStatus.ERROR,
                                        wStatus = WatchFaceEventsConstants.Failed)

                                )
                            )
                            AppLogs.sendAppLogs(
                                LogEvents.WatchFace,
                                WatchFaceEvents.TransferFailed
                            )
                        }

                        override fun onProgress(p0: Int) {
                            LOGS.d(
                                "noise_fit_event:colorfit_pro_2",
                                "watch_face_change | onTransProgressChanged : $p0"
                            )
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                    WatchUpdateStatus(
                                        status = UpdateStatus.PROGRESS,
                                        percentagePercentage = p0
                                    )

                                )
                            )
                        }

                        override fun onStart() {
                            LOGS.d(
                                "noise_fit_event:colorfit_pro_2",
                                "watch_face_change | onTransProgressStarting"
                            )
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                    WatchUpdateStatus(status = UpdateStatus.STARTED)

                                )
                            )
                        }
                    }
                BLEManager.startSetPlateFileToWatch(watchPlateSetConfig)
            }
        } catch (e: Exception) {

        }
    }

    override fun onDownloadedWatchFaceContents(watchFace: WatchFace) {
        this.cloudWatchFace = watchFace
        BLEManager.getWatchPlateList()
    }

    private fun updateCloudWatchFace(watchFace: WatchFace) {
        /*val updatedWatchFace =
            colorFitDevice?.deviceType?.let { deviceType ->
                if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType || deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType) {
                    CF2NetworkCalls.getWatchFaceDetailsTemp(watchFace)
                } else {
                    CF2NetworkCalls.getWatchFaceDetails(watchFace)
                }
            }*/

        this.cloudWatchFace = watchFace
        BLEManager.getWatchPlateList()
    }

    override fun updateFirmware(fileUri: String) {
        LOGS.d("noise_fit_event:colorfit_pro_2", "updateFirmware")
        val file1: File = File(Uri.parse(fileUri).path)

        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType) {
                val fileTransferConfig = FileTransferConfig.getDefaultApolloOTAConfig(file1.path,
                    object : IFileTransferListener {
                        override fun onSuccess() {
                            LOGS.d("noise_fit_event:colorfit_pro_2", "firmware_upgrade : success")
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                    WatchUpdateStatus(status = UpdateStatus.COMPLETED)
                                )
                            )
                        }

                        override fun onFailed(p0: String?) {
                            LOGS.d("noise_fit_event:colorfit_pro_2", "firmware_upgrade : failed")
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                    WatchUpdateStatus(status = UpdateStatus.ERROR, message = p0)
                                )
                            )
                            AppLogs.sendAppLogs(
                                LogEvents.Ota,
                                OtaEvents.TransferFailed
                            )
                        }

                        override fun onProgress(p0: Int) {
                            LOGS.d(
                                "noise_fit_event:colorfit_pro_2",
                                "firmware_upgrade : progress: $p0"
                            )
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                    WatchUpdateStatus(
                                        status = UpdateStatus.PROGRESS,
                                        percentagePercentage = p0
                                    )
                                )
                            )
                        }

                        override fun onStart() {
                            LOGS.d("noise_fit_event:colorfit_pro_2", "firmware_upgrade : started")
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                    WatchUpdateStatus(
                                        status = UpdateStatus.PROGRESS,
                                        percentagePercentage = 0
                                    )
                                )
                            )
                        }
                    })
                fileTransferConfig.maxRetryTimes = 0

                BLEManager.startTranCommonFile(fileTransferConfig)
            } else {
                //val basicInfo = LocalDataManager.getBasicInfo()
                val bleDFUConfig = BleDFUConfig()
                bleDFUConfig.deviceId = CF2Globals.basicInfo?.deivceId.toString()
                bleDFUConfig.filePath = file1.path
                bleDFUConfig.macAddress =
                    colorFitDevice?.address//LocalDataManager.getLastConnectedDeviceInfo().mDeviceAddress

                BLEManager.addDFUStateListener(object : BleDFUState.IListener {
                    override fun onPrepare() {
                        LOGS.d("noise_fit_event:colorfit_pro_2", "firmware_upgrade : started")
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                WatchUpdateStatus(
                                    status = UpdateStatus.PROGRESS,
                                    percentagePercentage = 0
                                )
                            )
                        )
                    }

                    override fun onDeviceInDFUMode() {}
                    override fun onProgress(progress: Int) {
                        LOGS.d(
                            "noise_fit_event:colorfit_pro_2",
                            "firmware_upgrade : progress: $progress"
                        )
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                WatchUpdateStatus(
                                    status = UpdateStatus.PROGRESS,
                                    percentagePercentage = progress
                                )
                            )
                        )
                    }

                    override fun onSuccess() {
                        LOGS.d("noise_fit_event:colorfit_pro_2", "firmware_upgrade : success")
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                WatchUpdateStatus(status = UpdateStatus.COMPLETED)
                            )
                        )
                    }

                    override fun onSuccessAndNeedToPromptUser() {
                        LOGS.d(
                            "noise_fit_event:colorfit_pro_2",
                            "firmware_upgrade : successAndPromptUser"
                        )
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                WatchUpdateStatus(status = UpdateStatus.COMPLETED)
                            )
                        )
                    }

                    override fun onFailed(failReason: BleDFUState.FailReason) {
                        LOGS.d("noise_fit_event:colorfit_pro_2", "firmware_upgrade : failed")
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                WatchUpdateStatus(
                                    status = UpdateStatus.ERROR,
                                    message = failReason.name
                                )
                            )
                        )
                        AppLogs.sendAppLogs(
                            LogEvents.Ota,
                            OtaEvents.TransferTimeout.apply {
                                comment = failReason.name
                            }
                        )
                    }

                    override fun onCanceled() {
                        LOGS.d("noise_fit_event:colorfit_pro_2", "firmware_upgrade : cancelled")
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                WatchUpdateStatus(status = UpdateStatus.ERROR)
                            )
                        )
                        AppLogs.sendAppLogs(
                            LogEvents.Ota,
                            OtaEvents.TransferFailed
                        )
                    }

                    override fun onRetry(count: Int) {
                        LOGS.d(
                            "noise_fit_event:colorfit_pro_2",
                            "firmware_upgrade : retry : $count"
                        )
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                WatchUpdateStatus(status = UpdateStatus.RETRY)
                            )
                        )
                        AppLogs.sendAppLogs(
                            LogEvents.Ota,
                            OtaEvents.Other.apply {
                                comment = "Retry"
                            }
                        )
                    }
                })
                BLEManager.startDFU(bleDFUConfig)
            }
        }
    }


    override fun startCameraMode(status: Boolean) {
        if (status) {
            BLEManager.enterCameraMode()
        } else {
            BLEManager.exitCameraMode()
        }
    }

    override fun sendAppNotification(appNotification: AppNotification) {
        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType) {
                val messageInfo = Colorfit2DataConverter.formatMessageInfoV3(appNotification)

                if (messageInfo.evtType != 0) {
                    BLEManager.setV3MessageNotice(messageInfo)
                }
            } else {
                val messageInfo = Colorfit2DataConverter.formatMessageInfo(appNotification)
                if (messageInfo.type != 0) {
                    BLEManager.setNewMessageDetailInfo(messageInfo)
                }
            }
        }
    }

    override fun setWristLiftGesture(wristLiftGesture: WristLiftGesture) {
        val gesture = Colorfit2DataConverter.formatWristSenseData(colorFitDevice, wristLiftGesture)
//        LOGS.d("setWristLiftGesture ${Gson().toJson(wristLiftGesture)}")
        if (BLEManager.isConnected()) {
            BLEManager.setUpHandGesture(gesture)
        } else {
            BLEManager.setUpHandGesturePending(gesture)
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.WristLiftGestureUpdated(
                    true
                )
            )
        }
    }

    override fun setFactoryReset() {
        LOGS.d("noise_fit_event:colorfit_pro_3", "factory reset::")
        BLEManager.setRestoreFactory()
    }

    override fun updateDND(doNotDisturb: DoNotDisturb) {
        val dnd = Colorfit2DataConverter.formatDoNotDisturb(doNotDisturb)
        if (BLEManager.isConnected()) {
            BLEManager.setNotDisturbPara(dnd)
        } else {
            BLEManager.setNotDisturbParaPending(dnd)
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.DoNotDisturbUpdated(
                    true
                )
            )
        }
    }

    override fun updateMenstrualData(menstrualData: MenstrualData) {
        val menstrualParams = Colorfit2DataConverter.formatMenstrualData(menstrualData)
        val menstrualReminder =
            Colorfit2DataConverter.formatMenstrualReminder(menstrualData.menstrualReminder!!)
        BLEManager.setMenstrualPending(menstrualParams)
        BLEManager.setMenstrualRemindPending(menstrualReminder)
        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.MenstrualDataUpdated(
                true
            )
        )
    }

    override fun setHeartRateInterval(heartRateInterval: HeartRateInterval) {

        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType) {
                val heartRateMeasureMode =
                    Colorfit2DataConverter.formatHeartRateIntervalV3(heartRateInterval)

                if (BLEManager.isConnected()) {
                    BLEManager.setHeartRateMeasureModeV3(heartRateMeasureMode)
                } else {
                    BLEManager.setHeartRateMeasureModeV3Pending(heartRateMeasureMode)
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.HeartRateMeasureIntervalSet(
                            true
                        )
                    )
                }
            } else if (deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType
                || deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType
                || deviceType == DeviceType.NOISEFIT_AGILE.deviceType
            ) {
                val heartRateMeasureMode =
                    Colorfit2DataConverter.formatHeartRateIntervalActive(heartRateInterval)

                if (BLEManager.isConnected()) {
                    BLEManager.setHeartRateMeasureModeV3(heartRateMeasureMode)
                } else {
                    BLEManager.setHeartRateMeasureModeV3Pending(heartRateMeasureMode)
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.HeartRateMeasureIntervalSet(
                            true
                        )
                    )
                }
            } else {
                val heartRateMeasureMode =
                    Colorfit2DataConverter.formatHeartRateInterval(heartRateInterval)
                if (BLEManager.isConnected()) {
                    BLEManager.setHeartRateMeasureMode(heartRateMeasureMode)
                } else {
                    BLEManager.setHeartRateMeasureModePending(heartRateMeasureMode)
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.HeartRateMeasureIntervalSet(
                            true
                        )
                    )
                }
            }
        }
    }

    override fun setHeartRateAlert(heartRateAlert: HeartRateAlert) {
        /* val hr = com.ido.ble.protocol.model.HeartRateInterval()
         hr.minHrRemind = heartRateAlert.min_hr
         hr.maxHrRemind = heartRateAlert.max_hr
         if (BLEManager.isConnected()) {
             BLEManager.setHeartRateInterval(hr)
         }
         else{
             BLEManager.setHeartRateIntervalPending(hr)
         }*/
    }

    override fun setSedentaryData(sedentaryData: SedentaryData) {
        val sit = Colorfit2DataConverter.formatSedentaryData(sedentaryData)
        if (BLEManager.isConnected()) {
            BLEManager.setLongSit(sit)
        } else {
            BLEManager.setLongSitPending(sit)
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.SedentaryDataUpdated(
                    true
                )
            )
        }
    }

    override fun setWalkReminderPro3(walkReminderData: WalkReminderData) {
        val sit = Colorfit2DataConverter.formatWalkReminderData(walkReminderData)
        if (BLEManager.isConnected()) {
            BLEManager.setWalkReminder(sit)
        } else {
            BLEManager.setWalkReminderPending(sit)
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.WalkReminderDataUpdated(
                    true
                )
            )
        }
    }

    override fun setDrinkWaterReminder(sedentaryData: SedentaryData) {
        val drink = Colorfit2DataConverter.formatDrinkReminderData(sedentaryData)
        if (BLEManager.isConnected()) {
            BLEManager.setDrinkWaterReminder(drink)
        } else {
            BLEManager.setDrinkWaterReminderPending(drink)
        }
//        testUpdateDeviceDataCallback?.onUpdateDataReceived(
//            UpdateDeviceDataCallback.DrinkWaterUpdated(
//                true
//            )
//        )
    }

    override fun setStressData(sedentaryData: SedentaryData) {
        val stress = Colorfit2DataConverter.formatStressData(sedentaryData)
        if (BLEManager.isConnected()) {
            BLEManager.setPressureParam(stress)
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.StressDataUpdated(
                    true
                )
            )
        } else {
            BLEManager.setPressureParamPending(stress)
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.StressDataUpdated(
                    true
                )
            )
        }
    }

    override fun setMusicSwitch(musicSwitch: SwitchSetting) {
        if (BLEManager.isConnected()) {
            BLEManager.setMusicSwitch(musicSwitch.status)
        } else {
            BLEManager.setMusicSwitchPending(musicSwitch.status)
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.MusicSwitchUpdated(
                    true
                )
            )
        }
    }

    override fun setActivityRecogniseSwitch(activitySwitch: SwitchSetting) {
        val actSwitch = ActivitySwitch()
        if (activitySwitch.walk_status) {
            actSwitch.autoIdentifySportWalk = ActivitySwitch.SWITCH_ON
            actSwitch.autoEndRemindOnOffOnOff = ActivitySwitch.SWITCH_ON
            actSwitch.autoPauseOnOff = ActivitySwitch.SWITCH_ON
        } else {
            actSwitch.autoIdentifySportWalk = ActivitySwitch.SWITCH_OFF
            actSwitch.autoEndRemindOnOffOnOff = ActivitySwitch.SWITCH_OFF
            actSwitch.autoPauseOnOff = ActivitySwitch.SWITCH_OFF
        }
        if (activitySwitch.run_status) {
            actSwitch.autoIdentifySportRun = ActivitySwitch.SWITCH_ON
            actSwitch.autoEndRemindOnOffOnOff = ActivitySwitch.SWITCH_ON
            actSwitch.autoPauseOnOff = ActivitySwitch.SWITCH_ON
        } else {
            actSwitch.autoIdentifySportRun = ActivitySwitch.SWITCH_OFF
            actSwitch.autoEndRemindOnOffOnOff = ActivitySwitch.SWITCH_OFF
            actSwitch.autoPauseOnOff = ActivitySwitch.SWITCH_OFF
        }
        if (activitySwitch.cycle_status) {
            actSwitch.autoIdentifySportBicycle = ActivitySwitch.SWITCH_ON
            actSwitch.autoEndRemindOnOffOnOff = ActivitySwitch.SWITCH_ON
            actSwitch.autoPauseOnOff = ActivitySwitch.SWITCH_ON
        } else {
            actSwitch.autoIdentifySportBicycle = ActivitySwitch.SWITCH_OFF
            actSwitch.autoEndRemindOnOffOnOff = ActivitySwitch.SWITCH_OFF
            actSwitch.autoPauseOnOff = ActivitySwitch.SWITCH_OFF
        }

        if (BLEManager.isConnected()) {
            BLEManager.setActivitySwitch(actSwitch)
        } else {
            BLEManager.setActivitySwitchPending(actSwitch)
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.ActivitySwitchUpdated(
                    true
                )
            )
        }
    }

    override fun setIncomingCallInfo(incomingCall: IncomingCall) {
        if (incomingCall.status) {
            val call = IncomingCallInfo()
            call.name = incomingCall.name
            call.phoneNumber = incomingCall.number
            BLEManager.setIncomingCallInfo(call)
        } else {
            BLEManager.setStopInComingCall()
        }
    }

    override fun setWeatherSwitch(switchSetting: SwitchSetting) {
        if (BLEManager.isConnected()) {
            BLEManager.setWeatherSwitch(switchSetting.status)
        } else {
            BLEManager.setWeatherSwitchPending(switchSetting.status)
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.WeatherSwitchUpdated(
                    true
                )
            )
        }
    }

    override fun setWeatherData(weatherData: List<WeatherData>, unit: String) {
        if (weatherData.isNotEmpty()) {

            val weather = WeatherInfo()
            weather.max_temp = weatherData[0].tempMax.roundToInt()
            weather.min_temp = weatherData[0].tempMin.roundToInt()
            weather.temp = weatherData[0].temp.roundToInt()
            weather.humidity = weatherData[0].humidity.roundToInt()
            val weatherType = weatherData[0].weatherType?.toLowerCase(DateFormats.defaultLocale)
//            LOGS.d(
//                "noise_fit_event:colorfit_pro_2",
//                "weather:: ${Gson().toJson(weatherData)}"
//            )
            weather.type = Colorfit2DataConverter.getWeatherTypeForWatch(weatherType)

            val items = arrayOfNulls<WeatherFutureInfo>(weatherData.size - 1)

            var countIndex = 0
            for (index in 1 until weatherData.size) {

                val weatherObject = weatherData[index]
                val weather1 = WeatherFutureInfo()
                weather1.max_temp = weatherObject.tempMax.roundToInt()
                weather1.min_temp = weatherObject.tempMin.roundToInt()
                val weatherType1 = weatherObject.weatherType?.toLowerCase(DateFormats.defaultLocale)
                weather1.type = Colorfit2DataConverter.getWeatherTypeForWatch(weatherType1)
                items[countIndex] = weather1
                countIndex++
            }

            weather.future = items

            BLEManager.setWeatherData(weather)
        }
    }

    private val appSendDataCallBack: AppSendDataCallBack.ICallBack =
        object : AppSendDataCallBack.ICallBack {

            override fun onSuccess(p0: AppSendDataCallBack.DataType?) {
                when (p0) {
                    AppSendDataCallBack.DataType.WEATHER ->
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.WeatherSwitchUpdated(
                                true
                            )
                        )
                    else -> {
                    }
                }
            }

            override fun onFailed(p0: AppSendDataCallBack.DataType) {
                when (p0) {
                    AppSendDataCallBack.DataType.WEATHER ->
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.WeatherSwitchUpdated(
                                false
                            )
                        )
                    else -> {
                    }
                }
            }
        }

    private val bindCallBack: BindCallBack.ICallBack = object : BindCallBack.ICallBack {

        override fun onSuccess() {
            LOGS.d("noise_fit_event:colorfit_pro_2", "notifications::: success")
        }

        override fun onCancel() {
            LOGS.d("noise_fit_event:colorfit_pro_2", "notifications::: cancel")
        }


        override fun onReject() {
            LOGS.d("noise_fit_event:colorfit_pro_2", "notifications::: reject")
        }

        override fun onNeedAuth(p0: Int) {
            LOGS.d("noise_fit_event:colorfit_pro_2", "notifications::: auth")
        }

        override fun onFailed(p0: BindCallBack.BindFailedError) {
            LOGS.d("noise_fit_event:colorfit_pro_2", "notifications::: " + p0)
        }
    }

    override fun setFindMyPhone(switchSetting: SwitchSetting) {
        if (BLEManager.isConnected()) {
            BLEManager.setFindPhoneSwitch(switchSetting.status)

        } else {
            BLEManager.setFindPhoneSwitchPending(switchSetting.status)
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.FindPhoneUpdated(
                    true
                )
            )
        }
    }

    private val settingsCallBack: SettingCallBack.ICallBack = object : SettingCallBack.ICallBack {

        override fun onSuccess(p0: SettingCallBack.SettingType?, p1: Any?) {
            LOGS.d("noise_fit_event:colorfit_pro_2 device control1::" + p0)
            when (p0) {
                SettingCallBack.SettingType.SCREEN_BRIGHTNESS ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.BrightnessLevelUpdated(
                            true
                        )
                    )
                SettingCallBack.SettingType.WEATHER_SWITCH ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.WeatherSwitchUpdated(
                            true
                        )
                    )
                SettingCallBack.SettingType.LONG_SIT ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.SedentaryDataUpdated(
                            true
                        )
                    )
                SettingCallBack.SettingType.ALARM ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.AlarmUpdated(
                            true
                        )
                    )
                SettingCallBack.SettingType.ALARM_V3 -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.AlarmUpdated(
                            true
                        )
                    )
                }
                SettingCallBack.SettingType.TIME ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DeviceTimeSynced(
                            true
                        )
                    )
                SettingCallBack.SettingType.MUSIC_SWITCH ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.MusicSwitchUpdated(
                            true
                        )
                    )
                SettingCallBack.SettingType.USER_INFO ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.UserInfoUpdated(
                            true
                        )
                    )
                SettingCallBack.SettingType.FIND_PHONE_SWITCH ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.FindPhoneUpdated(
                            true
                        )
                    )
                SettingCallBack.SettingType.UNIT -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DeviceUnitsUpdated(
                            true
                        )
                    )

                }
//                SettingCallBack.SettingType.GOAL ->
//                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
//                        UpdateDeviceDataCallback.UserGoalsUpdated(
//                            true
//                        )
//                    )
                SettingCallBack.SettingType.NOT_DISTURB ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DoNotDisturbUpdated(
                            true
                        )
                    )
                SettingCallBack.SettingType.HEART_RATE_MEASURE_MODE ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.HeartRateMeasureIntervalSet(
                            true
                        )
                    )
                SettingCallBack.SettingType.UP_HAND_GESTURE ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.WristLiftGestureUpdated(
                            true
                        )
                    )
                SettingCallBack.SettingType.ACTIVITY_SWITCH ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.ActivitySwitchUpdated(
                            true
                        )
                    )
                SettingCallBack.SettingType.DRINK_WATER_REMINDER ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DrinkWaterUpdated(
                            true
                        )
                    )
                SettingCallBack.SettingType.RESTORE_FACTORY ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.FactoryReset(
                            true
                        )
                    )
                SettingCallBack.SettingType.WASH_HAND_REMINDER ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.HandWashingUpdated(
                            true
                        )
                    )
                SettingCallBack.SettingType.HEART_RATE_MEASURE_MODE_V3 ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.HeartRateMeasureIntervalSet(
                            true
                        )
                    )
                SettingCallBack.SettingType.WALK_REMINDER ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.WalkReminderDataUpdated(
                            true
                        )
                    )
                SettingCallBack.SettingType.SPORT_SORT_V3 ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.SportModeDataUpdated(
                            true
                        )
                    )
                SettingCallBack.SettingType.QUICK_SPORT_MODE ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.SportModeDataUpdated(
                            true
                        )
                    )
                //SettingCallBack.SettingType.MUSIC_CONTROL_INFO -> Log.e("Music Control Info", "success")
                else -> {
                }
            }
        }

        override fun onFailed(type: SettingCallBack.SettingType) {
            LOGS.d("noise_fit_event:colorfit_pro_2 device control2::" + type)
            when (type) {
                SettingCallBack.SettingType.WEATHER_SWITCH ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.WeatherSwitchUpdated(
                            false
                        )
                    )
                SettingCallBack.SettingType.LONG_SIT ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.SedentaryDataUpdated(
                            false
                        )
                    )
                SettingCallBack.SettingType.ALARM ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.AlarmUpdated(
                            false
                        )
                    )
                SettingCallBack.SettingType.TIME ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DeviceTimeSynced(
                            false
                        )
                    )
                SettingCallBack.SettingType.MUSIC_SWITCH ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.MusicSwitchUpdated(
                            false
                        )
                    )
                SettingCallBack.SettingType.USER_INFO ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.UserInfoUpdated(
                            false
                        )
                    )
                SettingCallBack.SettingType.FIND_PHONE_SWITCH ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.FindPhoneUpdated(
                            false
                        )
                    )
                SettingCallBack.SettingType.UNIT -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DeviceUnitsUpdated(
                            false
                        )
                    )

                }
//                SettingCallBack.SettingType.GOAL ->
//
//                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
//                        UpdateDeviceDataCallback.UserInfoUpdated(
//                            false
//                        )
//                    )
                SettingCallBack.SettingType.NOT_DISTURB ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DoNotDisturbUpdated(
                            false
                        )
                    )
                SettingCallBack.SettingType.HEART_RATE_MEASURE_MODE ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.HeartRateMeasureIntervalSet(
                            false
                        )
                    )
                SettingCallBack.SettingType.UP_HAND_GESTURE ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.WristLiftGestureUpdated(
                            false
                        )
                    )
                SettingCallBack.SettingType.ACTIVITY_SWITCH ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.ActivitySwitchUpdated(
                            false
                        )
                    )
                SettingCallBack.SettingType.DRINK_WATER_REMINDER ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DrinkWaterUpdated(
                            false
                        )
                    )
                SettingCallBack.SettingType.RESTORE_FACTORY ->

                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.FactoryReset(
                            false
                        )
                    )
                SettingCallBack.SettingType.WASH_HAND_REMINDER ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.HandWashingUpdated(
                            false
                        )
                    )
                SettingCallBack.SettingType.HEART_RATE_MEASURE_MODE_V3 ->

                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.HeartRateMeasureIntervalSet(
                            false
                        )
                    )
                SettingCallBack.SettingType.WALK_REMINDER ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.WalkReminderDataUpdated(
                            false
                        )
                    )
                SettingCallBack.SettingType.SPORT_SORT_V3 ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.SportModeDataUpdated(
                            false
                        )
                    )
                SettingCallBack.SettingType.QUICK_SPORT_MODE ->
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.SportModeDataUpdated(
                            false
                        )
                    )
                //SettingCallBack.SettingType.MUSIC_CONTROL_INFO -> Log.e("Music Control Info", "failure")
                else -> {
                }
            }
        }
    }


    override fun setCustomBackground(imagePath: Uri, firmware: String) {

        updateCustomBackground(imagePath)
    }

    override fun setHandWashing(handWashing: HandWashing) {
        val washhand = Colorfit2DataConverter.formatWashHandReminderData(handWashing)
        if (BLEManager.isConnected()) {
            watchDataStore.updateHandWashData(handWashing)
            BLEManager.setWashHandReminder(washhand)
        }

        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.HandWashingUpdated(
                true
            )
        )
        /*testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.HandWashingUpdated(true))*/
    }

    private fun updateCustomBackground(uri: Uri) {
        LOGS.d("noise_fit_event:colorfit_pro_2 : watch_face_change; " + uri)
        createCustomFacesDir()
        val filePath = uri.path!!.split("///").last()
        convertToPng(filePath)
        var fileToBeSet = ""
        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType) {
                fileToBeSet = fileNameToBeSetPro3
            } else if (deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType) {
                fileToBeSet = fileNameToBeSetPro2Oxy
            } else if (deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType) {
                fileToBeSet = fileNameToBeSetNoisefitActive
            } else if (deviceType == DeviceType.NOISEFIT_AGILE.deviceType) {
                fileToBeSet = fileNameToBeSetNoisefitAgile
            } else {
                fileToBeSet = fileNameToBeSet
            }
        }

        val config = WallpaperFileCreateConfig()
        config.format = 5
        config.outFilePath = fileToBeSet
        config.sourceFilePath = filePath

        BLEManager.createPlateWallpaperFile(config)

        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                WatchUpdateStatus(status = UpdateStatus.STARTED)
            )
        )
        Handler(Looper.getMainLooper()).postDelayed({ updateBackground(config) }, 2000)

    }

    //TODO Move to File Utils
    private fun createCustomFacesDir() {
        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType) {
                createDirectory("/customfaces/colorfit_pro_3/")
            } else if (deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType) {
                createDirectory("/customfaces/colorfit_pro_2_oxy/")
            } else if (deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType) {
                createDirectory("/customfaces/noisefit_active/")
            } else if (deviceType == DeviceType.NOISEFIT_AGILE.deviceType) {
                createDirectory("/customfaces/noisefit_agile/")
            } else {
                createDirectory("/customfaces/colorfit_pro_2/")
            }
        }
    }

    private fun createDirectory(dirName: String) {
        val fileDir = File(
            NoisefitApplication.context!!.applicationContext.getExternalFilesDir(null),
            dirName
        )
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
    }

    private fun convertToPng(localImagePath: String) {
        if (localImagePath.contains(".png").not()) {
            val bitmap = MediaStore.Images.Media.getBitmap(
                NoisefitApplication.context?.contentResolver,
                Uri.parse(localImagePath)
            )
            try {
                var fileToBeSet = ""
                colorFitDevice?.deviceType?.let { deviceType ->
                    if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType) {
                        fileToBeSet = fileNameToBeSetPro3
                    } else if (deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType) {
                        fileToBeSet = fileNameToBeSetPro2Oxy
                    } else if (deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType) {
                        fileToBeSet = fileNameToBeSetNoisefitActive
                    } else if (deviceType == DeviceType.NOISEFIT_AGILE.deviceType) {
                        fileToBeSet = fileNameToBeSetNoisefitAgile
                    } else {
                        fileToBeSet = fileNameToBeSet
                    }
                }
                val out = FileOutputStream(File(fileToBeSet))
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateBackground(config: WallpaperFileCreateConfig) {
        BLEManager.stopTranCommonFile()
        var fileToBeSet = ""
        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType) {
                fileToBeSet = fileNameToBeSetPro3
            } else if (deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType) {
                fileToBeSet = fileNameToBeSetPro2Oxy
            } else if (deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType) {
                fileToBeSet = fileNameToBeSetNoisefitActive
            } else if (deviceType == DeviceType.NOISEFIT_AGILE.deviceType) {
                fileToBeSet = fileNameToBeSetNoisefitAgile
            } else {
                fileToBeSet = fileNameToBeSet
            }
        }
        BLEManager.startTranCommonFile(
            FileTransferConfig.getDefaultTransPictureConfig(
                "$fileToBeSet.lz", object : IFileTransferListener {
                    override fun onSuccess() {
                        LOGS.d(
                            "noise_fit_event:colorfit_pro_2 : watch_face_change; onSuccess"
                        )
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                WatchUpdateStatus(
                                    status = UpdateStatus.COMPLETED,
                                    wStatus = WatchFaceEventsConstants.Complete
                                )
                            )
                        )
                    }

                    override fun onFailed(p0: String?) {
                        LOGS.d(
                            "noise_fit_event:colorfit_pro_2 : watch_face_change; onFailed : $p0"
                        )
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                WatchUpdateStatus(
                                    status = UpdateStatus.ERROR,
                                    wStatus = p0
                                )
                            )
                        )
                        AppLogs.sendAppLogs(
                            LogEvents.WatchFace,
                            WatchFaceEvents.TransferTimeout
                        )
                    }

                    override fun onProgress(p0: Int) {
                        LOGS.d(
                            "noise_fit_event:colorfit_pro_2 : watch_face_change; onProgress : $p0"
                        )

                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                WatchUpdateStatus(
                                    status = UpdateStatus.PROGRESS,
                                    percentagePercentage = p0
                                )
                            )
                        )
                    }

                    override fun onStart() {
                        LOGS.d("noise_fit_event:colorfit_pro_2 : watch_face_change; onStart")
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                WatchUpdateStatus(
                                    status = UpdateStatus.STARTED
                                )
                            )
                        )
                    }
                })
        )
    }

    private val deviceResponseCallback = object : DeviceResponseCommonCallBack.ICallBack {


        override fun onResponse(p0: Int, p1: String?) {

        }
    }

    interface FirmwareUpgradeStatus {
        fun onUpdate(firmware: DeviceFirmware)
    }

    override fun setSportSyncParamPro3() {
        val mSyncPara = SyncPara()
        mSyncPara.isNeedSyncConfigData = true
        BLEManager.syncAllData(mSyncPara)
    }

    override fun setSportModeInfo(data: SportsModeList) {

        LOGS.d("setSportModeInfo ${data.sportsModes?.size}")
        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.COLORFIT_PRO_3.deviceType || deviceType == DeviceType.COLORFIT_PRO_2_OXY.deviceType
                || deviceType == DeviceType.NOISEFIT_ACTIVE.deviceType || deviceType == DeviceType.NOISEFIT_AGILE.deviceType
            ) {
                val mode = Colorfit2DataConverter.formatSportModeV3(data)
                val sportModeSortV3 = SportModeSortV3()
                sportModeSortV3.item = mode
                sportModeSortV3.num = mode.size
                if (BLEManager.isConnected()) {
                    BLEManager.setSportModeSortInfoV3(sportModeSortV3)
                } else {
                    BLEManager.setSportModeSortInfoV3Pending(sportModeSortV3)
                    setSportSyncParamPro3()
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.SportModeDataUpdated(
                            true
                        )
                    )
                }
            } else {
                val mode = Colorfit2DataConverter.formatSportMode(data)
                if (BLEManager.isConnected()) {
                    BLEManager.setQuickSportMode(mode)
                    /*testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.SportModeDataUpdated(
                            true
                        )
                    )*/
                } else {
                    BLEManager.setQuickSportModePending(mode)
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.SportModeDataUpdated(
                            true
                        )
                    )
                }
            }
        }
    }

    override fun setRestartDevice() {
        if (BLEManager.isConnected()) {
            BLEManager.reBoot()
        }
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
}

class PhotoBean(
    @SerializedName("fileName") val fileName: String? = null,
    @SerializedName("saveFileName") val saveFileName: String? = null,
    @SerializedName("format") val format: Int = 0
) : Serializable