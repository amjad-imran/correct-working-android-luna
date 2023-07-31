package com.noisefit_ryeex_sdk.handler

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.text.format.DateUtils
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.noisefit_commans.constants.WatchFaceEventsConstants
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.enums.ApplicationType
import com.noisefit_commans.interfaces.device_data.IUpdateDeviceDataCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.AlarmAction
import com.noisefit_commans.models.AlarmsList
import com.noisefit_commans.models.AppNotification
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.Contact
import com.noisefit_commans.models.CustomWatchFace
import com.noisefit_commans.models.DeviceLanguage
import com.noisefit_commans.models.DeviceUnits
import com.noisefit_commans.models.DiyCustomWatchFace
import com.noisefit_commans.models.DoNotDisturb
import com.noisefit_commans.models.Gender
import com.noisefit_commans.models.HeartRateAlert
import com.noisefit_commans.models.HeartRateInterval
import com.noisefit_commans.models.IncomingCall
import com.noisefit_commans.models.Language
import com.noisefit_commans.models.SedentaryData
import com.noisefit_commans.models.SportsModeList
import com.noisefit_commans.models.SwitchSetting
import com.noisefit_commans.models.TimeFormat
import com.noisefit_commans.models.TimeFormats
import com.noisefit_commans.models.UnitSystem
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.UserGoals
import com.noisefit_commans.models.UserInfo
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.models.WatchUpdateStatus
import com.noisefit_commans.models.WeatherData
import com.noisefit_commans.models.WeatherDataHourly
import com.noisefit_commans.models.Widget
import com.noisefit_commans.models.WristLiftGesture
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DistanceUtil
import com.noisefit_commans.utils.LOGS
import com.noisefit_ryeex_sdk.base.RyeexApplicationHandler
import com.noisefit_ryeex_sdk.dataConversion.DataConverter
import com.noisefit_ryeex_sdk.dataConversion.RyeexConst
import com.noisefit_ryeex_sdk.dataConversion.deleteWatchface
import com.noisefit_ryeex_sdk.dataConversion.prepareTarResource
import com.noisefit_ryeex_sdk.utils.GZipUtil
import com.noisefit_ryeex_sdk.utils.ZipUtil
import com.ryeex.ble.common.model.entity.FirmwareUpdateInfo
import com.ryeex.ble.common.model.entity.FirmwareUpdateInfo.UpdateItem
import com.ryeex.ble.common.model.entity.HeartRateSetting
import com.ryeex.ble.common.model.entity.Height
import com.ryeex.ble.common.model.entity.SitRemindSetting
import com.ryeex.ble.common.model.entity.WeatherInfo
import com.ryeex.ble.common.model.entity.WeatherInfo.WeatherDetail
import com.ryeex.ble.common.model.entity.WeatherInfo.WeatherDetail.DailyWeather
import com.ryeex.ble.common.model.entity.WeatherInfo.WeatherDetail.DailyWeather.DailyItem
import com.ryeex.ble.common.model.entity.WeatherInfo.WeatherDetail.SectionWeather
import com.ryeex.ble.common.model.entity.WeatherInfo.WeatherDetail.SectionWeather.SectionItem
import com.ryeex.ble.common.model.entity.Weight
import com.ryeex.ble.common.utils.FileUtil
import com.ryeex.ble.connector.callback.AsyncBleCallback
import com.ryeex.ble.connector.error.BleError
import com.ryeex.ble.connector.handler.BleHandler
import com.ryeex.watch.adapter.device.WatchDevice
import com.ryeex.watch.adapter.model.entity.DeviceSurfaceInfo
import com.ryeex.watch.adapter.model.entity.DrinkWaterRemindSetting
import com.ryeex.watch.adapter.model.entity.Units
import com.ryeex.watch.bt.BTHelper
import com.ryeex.watch.protocol.pb.entity.PBProperty
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject
import kotlin.math.roundToInt


private const val TAG = "RyeexUpdateDeviceUnitsHandler"

class RyeexUpdateDeviceUnitsHandler
@Inject
constructor(
    private var dataConverter: DataConverter,
    private var context: Context,
    private var ryeexApplicationHandler: RyeexApplicationHandler,
    private val watchDataStore: WatchDataStore
) : UpdateDeviceDataActions() {


    private var updateDeviceDataCallback: IUpdateDeviceDataCallback? = null
    private var noiseFitDevice: ColorFitDevice? = null
    private var watchDevice: WatchDevice? = null

    override fun init() {
        super.init()
        watchDevice = ryeexApplicationHandler.getWatchDevice()
        removeCallbacks()
        attachCallbacks()
    }

    override fun updateFirmware(fileUri: String) {
        LOGS.d("updateFirmware ${fileUri} ${WatchInfoGlobals.firmwareFullRequired}")
        val zipFile = File(Uri.parse(fileUri).path)

        if (!zipFile.exists()) {
            updateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                    WatchUpdateStatus(status = UpdateStatus.ERROR)
                )
            )
            return
        }

        LOGS.d("updateFirmware file exist")
        val dir = File(zipFile.parent, zipFile.name.substring(0, zipFile.name.lastIndexOf(".")))
        LOGS.i("updateFirmware dir=" + dir.absolutePath)
        ZipUtil.unzip(zipFile, dir)
        val otaFiles = dir.listFiles()
        if (otaFiles != null && otaFiles.isNotEmpty()) {
            val firmwareUpdateInfo = FirmwareUpdateInfo()
            firmwareUpdateInfo.isForce = false
            val items: MutableList<UpdateItem> = ArrayList()
            for (file in otaFiles) {
                LOGS.i(TAG, "file path:" + file.absolutePath)
                if (file.name.equals("config.json", ignoreCase = true)) {
                    val configJson = FileUtil.readTxtFromFile(file.absolutePath)
                    LOGS.i(TAG, "configJson:$configJson")
                    if (TextUtils.isEmpty(configJson)) {
                        firmwareUpdateInfo.version = "1.3.0.1"
                        continue
                    }
                    val config = JsonParser.parseString(configJson).asJsonObject
                    if (config != null) {
                        firmwareUpdateInfo.version = config.get("version").asString
                        firmwareUpdateInfo.isResFull = config.get("isFullRes").asBoolean
                    }
                } else if (file.name.endsWith(".res")) {
                    val updateItem = UpdateItem()
                    updateItem.id = 0
                    updateItem.localPath = file.absolutePath
                    val localFileMd5 = FileUtil.getFileMD5(file)
                    updateItem.md5 = localFileMd5
                    updateItem.length = file.length().toInt()
                    items.add(updateItem)
                } else if (file.name.endsWith(".fw.bin")) {
                    val updateItem = UpdateItem()
                    updateItem.id = 1
                    updateItem.localPath = file.absolutePath
                    val localFileMd5 = FileUtil.getFileMD5(file)
                    updateItem.md5 = localFileMd5
                    updateItem.length = file.length().toInt()
                    items.add(updateItem)
                } else if (file.name.endsWith(".boot.bin")) {
                    val updateItem = UpdateItem()
                    updateItem.id = 2
                    updateItem.localPath = file.absolutePath
                    val localFileMd5 = FileUtil.getFileMD5(file)
                    updateItem.md5 = localFileMd5
                    updateItem.length = file.length().toInt()
                    items.add(updateItem)
                }
            }
            firmwareUpdateInfo.urlList = items
//            LOGS.i(TAG, "startOta firmwareUpdateInfo" + Gson().toJson(firmwareUpdateInfo))
            watchDevice?.updateFirmware(
                firmwareUpdateInfo,
                object : AsyncBleCallback<Void?, BleError>() {
                    override fun onProgress(progress: Float) {
                        LOGS.i(TAG, "updateFirmware onProgress:$progress")
                        updateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                WatchUpdateStatus(
                                    status = UpdateStatus.PROGRESS,
                                    percentagePercentage = (progress * 100).toInt()
                                )
                            )
                        )
                    }

                    override fun onSuccess(result: Void?) {
                        LOGS.i(TAG, "updateFirmware onSuccess")
                        FileUtil.deleteDirectory(dir.absolutePath)
                        updateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                WatchUpdateStatus(status = UpdateStatus.COMPLETED)
                            )
                        )
                    }

                    override fun onFailure(error: BleError) {
                        LOGS.e(TAG, "updateFirmware onFailure:$error")
                        updateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                WatchUpdateStatus(status = UpdateStatus.ERROR)
                            )
                        )
                    }
                })
        }

    }

    override fun setContactList(contactList: List<Contact>) {
        val contactBeanList = ArrayList<com.ryeex.watch.adapter.model.entity.Contact>()
        contactList.forEach { contact ->
            val contactsBean =
                com.ryeex.watch.adapter.model.entity.Contact(contact.number[0], contact.name)
            contactBeanList.add(contactsBean)
        }

        watchDevice?.setContactList(contactBeanList, object : AsyncBleCallback<Void, BleError>() {
            override fun onSuccess(p0: Void?) {
                updateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.ContactListUpdated(
                        true
                    )
                )
            }

            override fun onFailure(p0: BleError?) {
                updateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.ContactListUpdated(
                        false
                    )
                )
            }

        })

    }


    override fun startCameraMode(status: Boolean) {
    }

    private fun getZipFileName(zipFile: String): String? {
        val fileName = zipFile.split("/").last()
        return fileName.ifEmpty {
            null
        }
    }

    override fun setWatchFace(watchFace: WatchFace) {
        //watchface_10054.tar
//        LOGS.d("setWatchFace ${Gson().toJson(watchFace)}")
        AppLogs.sendAppLogs("$TAG setWatchFace  ${watchFace}")
        var id = ""
        var watchFaceName = ""
        val splitName = watchFace.zip_file?.let { getZipFileName(it)?.split("_") }
        LOGS.d("setWatchFace ${splitName?.size} ${Gson().toJson(splitName)}")
        if (!splitName.isNullOrEmpty() && splitName.size == 2) {
            id = splitName[1].replace(".tar", "")
            watchFaceName = "watchface_${id}"
            LOGS.d(TAG, "setWatchFace  $id")
        } else {
            LOGS.d(TAG, "setWatchFace  $splitName ${splitName?.size}")
            updateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                    WatchUpdateStatus(
                        status = UpdateStatus.ERROR,
                        wStatus = WatchFaceEventsConstants.Empty_File
                    )
                )
            )
            return
        }
        LOGS.d(TAG, "setWatchFace  ${watchFace.watchface_type}")
        val file1 = File(Uri.parse(watchFace.localFilePath).path!!)
        val deviceSurfaceInfoSurface = DeviceSurfaceInfo.Surface()
        deviceSurfaceInfoSurface.id = id.toInt()//watchFace.watchfaceCatId ?: 1
        deviceSurfaceInfoSurface.version = 2
        deviceSurfaceInfoSurface.isSelected = true

        deviceSurfaceInfoSurface.surfaceType =
            if (watchFace.watchface_type.lowercase() == "dynamic") {
                DeviceSurfaceInfo.Surface.SurfaceType.DYNAMIC
            } else {
                DeviceSurfaceInfo.Surface.SurfaceType.STATIC
            }

        val resource = DeviceSurfaceInfo.Surface.Resource()
        resource.name = watchFaceName//watchFace.name
        resource.type = DeviceSurfaceInfo.Surface.Resource.Type.TAR
        val gzipFile = "${file1.absolutePath}.gz"
        GZipUtil.compressFile(file1.absolutePath, gzipFile)
        val bytes = if (FileUtil.fileExists(gzipFile)) {
            FileUtil.readBytes(gzipFile)
        } else {
            file1.readBytes()
        }
        resource.bytes = bytes
        val resources: MutableList<DeviceSurfaceInfo.Surface.Resource> = ArrayList()
        resources.add(resource)
        deviceSurfaceInfoSurface.resources = resources
        LOGS.d(TAG, "installDeviceWatchSurface $deviceSurfaceInfoSurface")
        watchDevice?.getSurfaceList(object : AsyncBleCallback<DeviceSurfaceInfo, BleError>() {
            override fun onSuccess(deviceSurfaceInfo: DeviceSurfaceInfo?) {
                deviceSurfaceInfo?.let { deviceSurface ->
                    if (deviceSurface.surfaceList.isNotEmpty()) {
                        val installed = deviceSurface.surfaceList.find {
                            it.id == deviceSurfaceInfoSurface.id
                        }
                        //already install
                        if (installed != null) {
                            LOGS.e(
                                TAG,
                                "installDeviceWatchSurface invalid id=${deviceSurfaceInfoSurface.id}"
                            )
                            AppLogs.sendAppLogs("$TAG Watchface already installed")
                            updateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                    WatchUpdateStatus(
                                        status = UpdateStatus.ERROR,
                                        message = "Watchface already installed",
                                        wStatus = WatchFaceEventsConstants.Already_Installed
                                    )
                                )
                            )
                            return
                        }
                        watchDevice?.deleteWatchface(
                            deviceSurfaceInfoSurface.surfaceType == DeviceSurfaceInfo.Surface.SurfaceType.DYNAMIC,
                            deviceSurface,
                            object : AsyncBleCallback<Int, BleError>() {
                                override fun onSuccess(deletedId: Int) {
                                    watchDevice?.installSurface(deviceSurfaceInfoSurface,
                                        object : AsyncBleCallback<Void?, BleError?>() {
                                            override fun onProgress(progress: Float) {
                                                LOGS.d("setWatchFace ${progress}")
                                                updateDeviceDataCallback?.onUpdateDataReceived(
                                                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                        WatchUpdateStatus(
                                                            status = UpdateStatus.PROGRESS,
                                                            percentagePercentage = (progress * 100).toInt()
                                                        )
                                                    )
                                                )
                                            }

                                            override fun onSuccess(result: Void?) {
                                                LOGS.d("setWatchFace onSuccess")
                                                AppLogs.sendAppLogs("$TAG onSuccess")
                                                updateDeviceDataCallback?.onUpdateDataReceived(
                                                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                        WatchUpdateStatus(
                                                            status = UpdateStatus.COMPLETED,
                                                            wStatus = WatchFaceEventsConstants.Complete
                                                        )
                                                    )
                                                )

                                            }

                                            override fun onFailure(bleError: BleError?) {
                                                AppLogs.sendAppLogs("$TAG onFailed ${bleError?.message}")
                                                LOGS.d("setWatchFace ${bleError?.message}")
                                                updateDeviceDataCallback?.onUpdateDataReceived(
                                                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                        WatchUpdateStatus(
                                                            status = UpdateStatus.ERROR,
                                                            wStatus = bleError?.message
                                                        )
                                                    )
                                                )
                                            }
                                        })
                                }

                                override fun onFailure(p0: BleError?) {
                                    AppLogs.sendAppLogs("$TAG deleteFailed ${p0?.message}")
                                    updateDeviceDataCallback?.onUpdateDataReceived(
                                        UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                            WatchUpdateStatus(
                                                status = UpdateStatus.ERROR,
                                                wStatus = p0?.message
                                            )
                                        )
                                    )
                                }
                            })
                    }
                }
            }

            override fun onFailure(bleError: BleError?) {
                AppLogs.sendAppLogs("$TAG queryFailed ${bleError?.message}")
                updateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                        WatchUpdateStatus(status = UpdateStatus.ERROR, wStatus = bleError?.message)
                    )
                )
            }
        })
    }


    override fun setDiyWatchFaceCustom(watchFace: DiyCustomWatchFace) {
        LOGS.i(TAG, "setDiyWatchFaceCustom watchFace=$watchFace")
        BleHandler.getWorkerHandler().post {
            watchDevice?.getSurfaceList(object : AsyncBleCallback<DeviceSurfaceInfo, BleError>() {
                override fun onSuccess(deviceSurfaceInfo: DeviceSurfaceInfo?) {
                    deviceSurfaceInfo?.let { deviceSurface ->
                        if (deviceSurface.surfaceList.isNotEmpty()) {
                            val surface = deviceSurface.surfaceList.last()
                            surface?.let {
                                var newId = it.id
                                if (newId in 101..198) {
                                    newId += 1
                                } else {
                                    newId = 111
                                }

                                val tempDir = File(RyeexConst.getWatchFaceCacheDir(context))
                                if (!tempDir.exists()) {
                                    tempDir.mkdirs()
                                }

                                val surfaceInstall = DeviceSurfaceInfo.Surface()
                                surfaceInstall.prepareTarResource(
                                    context,
                                    noiseFitDevice,
                                    newId,
                                    watchFace,
                                    tempDir
                                )
                                LOGS.i(TAG, "setDiyWatchFaceCustom surfaceInfo=$surfaceInstall")
                                watchDevice?.deleteWatchface(
                                    false,
                                    deviceSurface,
                                    object : AsyncBleCallback<Int, BleError>() {
                                        override fun onSuccess(p0: Int?) {
                                            watchDevice?.installSurface(surfaceInstall,
                                                object : AsyncBleCallback<Void, BleError>() {
                                                    override fun onProgress(progress: Float) {
                                                        updateDeviceDataCallback?.onUpdateDataReceived(
                                                            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                                WatchUpdateStatus(
                                                                    status = UpdateStatus.PROGRESS,
                                                                    percentagePercentage = (progress * 100).toInt()
                                                                )
                                                            )
                                                        )
                                                    }

                                                    override fun onSuccess(p0: Void?) {
                                                        LOGS.i(TAG, "installSurface onSuccess")
                                                        FileUtil.deleteDirectory(
                                                            tempDir.absolutePath,
                                                            true
                                                        )
                                                        updateDeviceDataCallback?.onUpdateDataReceived(
                                                            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                                WatchUpdateStatus(
                                                                    status = UpdateStatus.COMPLETED,
                                                                    wStatus = WatchFaceEventsConstants.Complete
                                                                )
                                                            )
                                                        )
                                                    }

                                                    override fun onFailure(error: BleError?) {
                                                        LOGS.e(
                                                            TAG,
                                                            "installSurface onFailure=$error"
                                                        )
                                                        updateDeviceDataCallback?.onUpdateDataReceived(
                                                            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                                WatchUpdateStatus(
                                                                    status = UpdateStatus.ERROR,
                                                                    wStatus = error?.message
                                                                )
                                                            )
                                                        )
                                                    }
                                                })
                                        }

                                        override fun onFailure(p0: BleError?) {
                                            LOGS.e(
                                                TAG,
                                                "installSurface onFailure 1=$p0"
                                            )
                                            updateDeviceDataCallback?.onUpdateDataReceived(
                                                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                    WatchUpdateStatus(
                                                        status = UpdateStatus.ERROR,
                                                        wStatus = p0?.message
                                                    )
                                                )
                                            )
                                        }
                                    })
                            }
                        }
                    }
                }

                override fun onFailure(p0: BleError?) {
                    LOGS.e(
                        TAG,
                        "installSurface onFailure 2=$p0"
                    )
                    updateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                            WatchUpdateStatus(
                                status = UpdateStatus.ERROR,
                                wStatus = p0?.message
                            )
                        )
                    )
                }
            })
        }
    }



    override fun updateAlarm(alarm: AlarmsList, alarmAction: AlarmAction) {

    }

    override fun updateLanguage(language: Language) {
        watchDevice?.setDeviceLanguage(
            if (language.language == DeviceLanguage.CHINESE.type) {
                PBProperty.LanguageParamPropVal.LANG_TYPE.CHINESE
            } else {
                PBProperty.LanguageParamPropVal.LANG_TYPE.ENGLISH
            }, object : AsyncBleCallback<Void, BleError>() {
                override fun onSuccess(p0: Void?) {
                    updateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.LanguageUpdated(
                            success = true
                        )
                    )
                }

                override fun onFailure(p0: BleError?) {
                    updateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.LanguageUpdated(
                            success = false
                        )
                    )
                }
            })
    }

    override fun findDevice(findDevice: SwitchSetting) {
        watchDevice?.findDevice(findDevice.status, object : AsyncBleCallback<Void, BleError>() {
            override fun onSuccess(p0: Void?) {
                LOGS.d("Find device success")
            }

            override fun onFailure(p0: BleError?) {
                LOGS.d("Find device error $p0")
                //
            }

        })
    }

    override fun updateDND(doNotDisturb: DoNotDisturb) {

    }

    override fun setHeartRateInterval(heartRateInterval: HeartRateInterval) {
        LOGS.d(TAG, "setHeartRateInterval heartRateInterval=$heartRateInterval")

    }

    override fun setHeartRateAlert(heartRateAlert: HeartRateAlert) {
        LOGS.d(TAG, "setHeartRateAlert $heartRateAlert")
        val heartRateSetting = HeartRateSetting()
        heartRateSetting.isEnable = heartRateAlert.status
        heartRateSetting.min = heartRateAlert.min_hr
        heartRateSetting.max = heartRateAlert.max_hr
        watchDevice?.setHeartRateDetect(
            heartRateSetting,
            object : AsyncBleCallback<Void, BleError>() {
                override fun onSuccess(p0: Void?) {
                    LOGS.i(TAG, "setHeartRateAlert setHeartRateDetect onSuccess")

                }

                override fun onFailure(error: BleError?) {
                    LOGS.e(TAG, "setHeartRateAlert setHeartRateDetect onFailure=$error")

                }

            })
    }

    override fun setSedentaryData(sedentaryData: SedentaryData) {
        LOGS.d(TAG, "setSedentaryData $sedentaryData")
        val reminder = SitRemindSetting()
        reminder.isEnable = sedentaryData.status
        reminder.isForbidEnable = false
        reminder.startTimeHour = sedentaryData.startHour
        reminder.startTimeMinute = sedentaryData.startMinute
        reminder.endTimeHour = sedentaryData.endHour
        reminder.endTimeMinute = sedentaryData.endMinute
        reminder.timeThreshold = sedentaryData.interval * 60

        watchDevice?.setSitRemindSetting(reminder, object : AsyncBleCallback<Void, BleError>() {
            override fun onSuccess(p0: Void?) {
                updateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.SedentaryDataUpdated(
                        true
                    )
                )
            }

            override fun onFailure(p0: BleError?) {
                updateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.SedentaryDataUpdated(
                        false
                    )
                )
            }
        })
    }

    override fun setUserInfo(userInfo: UserInfo, userGoals: UserGoals, userName: String?) {
        LOGS.d(TAG, "setUserInfo $userInfo  $userGoals  $userName")
        //  gender=0: female, 1: male
        LOGS.d(userInfo)
        val gender = when (userInfo.gender.lowercase()) {
            Gender.FEMALE.type.lowercase() -> {
                0
            }
            else -> {
                1
            }
        }

        val height = Height()
        height.unit = Height.Unit.CM

        val weight = Weight()
        weight.unit = Weight.Unit.KG

        when (userGoals.getUnit()) {
            com.noisefit_commans.models.Units.METRIC -> {
                height.height = userInfo.height.toFloat()
                weight.weight = userInfo.weight.toFloat()
            }
            else -> {
                height.height = DistanceUtil.convertInchToCms(userInfo.height).toFloat()
                weight.weight = DistanceUtil.convertLbsToKg(userInfo.weight).toFloat()
            }
        }

        watchDevice?.setUserGender(gender, null)
        watchDevice?.setUserHeight(height, null)
        watchDevice?.setUserWeight(weight, null)

        watchDevice?.setTargetStep(userGoals.stepGoal, null)
        watchDevice?.setTargetCalorie(userGoals.caloriesGoal, null)
        watchDevice?.setTargetDistance(userGoals.distanceGoal, null)


        updateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.UserInfoUpdated(
                true
            )
        )

    }

    override fun setDeviceUnits(units: DeviceUnits) {
        val unitSetting = Units()
        when (units.unitSystem?.lowercase()) {
            UnitSystem.IMPERIAL.type.lowercase() -> {
                unitSetting.distance = Units.DistanceType.MILE
                unitSetting.temperature = Units.TemperatureType.F
                unitSetting.weight = Units.WeightType.LB
            }
            else -> {
                unitSetting.distance = Units.DistanceType.KM
                unitSetting.temperature = Units.TemperatureType.C
                unitSetting.weight = Units.WeightType.KG
            }
        }
//        LOGS.d("$TAG setDeviceUnits: ${Gson().toJson(unitSetting)}")
        watchDevice?.setUnitSetting(unitSetting, object : AsyncBleCallback<Void, BleError>() {
            override fun onSuccess(p0: Void?) {
                updateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.DeviceUnitsUpdated(
                        true
                    )
                )
            }

            override fun onFailure(p0: BleError?) {
                updateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.DeviceUnitsUpdated(
                        false
                    )
                )
            }

        })
    }

    override fun setDeviceDateTime(calender: Calendar, units: TimeFormat) {
        val format = units.timeFormat?.let {
            when (it.lowercase()) {
                TimeFormats.HOURS_24.type.lowercase() -> true
                else -> false
            }
        } ?: false
        watchDevice?.updateHourFormat(format, object : AsyncBleCallback<Void, BleError>() {
            override fun onSuccess(p0: Void?) {
                LOGS.d(TAG, "updateHourFormat success")
                watchDevice?.updateTime(object : AsyncBleCallback<Void, BleError>() {
                    override fun onSuccess(p0: Void?) {
                        LOGS.d(TAG, "updateTime success")
                        updateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.DeviceTimeSynced(
                                true
                            )
                        )
                    }

                    override fun onFailure(p0: BleError?) {
                        LOGS.d(TAG, "updateTime onFailure:$p0")
                        updateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.DeviceTimeSynced(
                                false
                            )
                        )
                    }
                })
            }

            override fun onFailure(p0: BleError?) {
                LOGS.d(TAG, "updateHourFormat onFailure:$p0")
                updateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.DeviceTimeSynced(
                        false
                    )
                )
            }
        })
    }

    override fun setDrinkWaterReminder(sedentaryData: SedentaryData) {
        LOGS.d("setDrinkWaterReminder= $sedentaryData")

        val reminder = DrinkWaterRemindSetting()
        reminder.isEnable = sedentaryData.status
        reminder.isForbidEnable = true

        reminder.startTimeHour = sedentaryData.startHour
        reminder.startTimeMinute = sedentaryData.startMinute
        reminder.endTimeHour = sedentaryData.endHour
        reminder.endTimeMinute = sedentaryData.endMinute
        reminder.interval = sedentaryData.interval * 60


        watchDevice?.setDrinkWaterRemindSetting(reminder,
            object : AsyncBleCallback<Void, BleError>() {
                override fun onSuccess(p0: Void?) {
                    updateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DrinkWaterUpdated(
                            true
                        )
                    )
                }

                override fun onFailure(p0: BleError?) {
                    updateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DrinkWaterUpdated(
                            false
                        )
                    )
                }

            })
    }

    override fun sendAppNotification(appNotification: AppNotification) {
        LOGS.d("$TAG sendAppNotification appNotification=$appNotification")
        val info = com.ryeex.ble.common.model.entity.AppNotification()
        if (appNotification.appType == ApplicationType.SMS.type) {
            info.type = com.ryeex.ble.common.model.entity.AppNotification.Type.SMS
            val sms = com.ryeex.ble.common.model.entity.AppNotification.Sms()
            val name = if (appNotification.name.isNullOrEmpty()) {
                appNotification.number
            } else {
                appNotification.name
            }
            sms.sender = name
            sms.content = appNotification.message
            info.sms = sms
        } else {
            info.type = com.ryeex.ble.common.model.entity.AppNotification.Type.APP_MESSAGE
            val appMessage = com.ryeex.ble.common.model.entity.AppNotification.AppMessage()
            appMessage.appId = when (appNotification.appType) {
                ApplicationType.FACEBOOK.type -> RyeexConst.KEY_APP_FACEBOOK
//            ApplicationType.SMS.type -> CRPBleMessageType.MESSAGE_SMS
                ApplicationType.WE_CHAT.type -> RyeexConst.KEY_APP_WX
                ApplicationType.TWITTER.type -> RyeexConst.KEY_APP_TWITTER
                ApplicationType.WHATS_APP.type -> RyeexConst.KEY_APP_WHATSAPP
                ApplicationType.INSTAGRAM.type -> RyeexConst.KEY_APP_INSTAGRAM
                ApplicationType.SKYPE.type -> RyeexConst.KEY_APP_SKYPE
                ApplicationType.LINE.type -> RyeexConst.KEY_APP_LINE
                ApplicationType.GOOGLE_MAPS.type -> RyeexConst.KEY_APP_GOOGLE_MAP
                ApplicationType.GMAIL.type -> RyeexConst.KEY_APP_GMAIL
                ApplicationType.FB_MESSENGER.type -> RyeexConst.KEY_APP_MESSENGER
                ApplicationType.TELEGRAM.type -> RyeexConst.KEY_APP_TELEGRAM
                ApplicationType.SNAPCHAT.type -> RyeexConst.KEY_APP_VIBER
                ApplicationType.CALENDAR.type -> RyeexConst.KEY_APP_CALENDAR
                ApplicationType.OUTLOOK.type -> RyeexConst.KEY_APP_OUTLOOK
                ApplicationType.YOUTUBE.type -> RyeexConst.KEY_APP_YOUTUBE
                else -> RyeexConst.KEY_APP_OTHERS
            }
            appMessage.title = appNotification.name
            appMessage.text = appNotification.message
            info.appMessage = appMessage
        }

        watchDevice?.sendNotification(info, object : AsyncBleCallback<Void, BleError>() {
            override fun onSuccess(p0: Void?) {
                LOGS.d(TAG, "sendAppNotification onSuccess")
                /*updateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.DeviceNotificationsUpdated(true)
                )*/
            }

            override fun onFailure(error: BleError?) {
                LOGS.e(TAG, "sendAppNotification onFailure:$error")
                /*updateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.DeviceNotificationsUpdated(false)
                )*/
            }
        })
    }

    override fun setIncomingCallInfo(incomingCall: IncomingCall) {
        LOGS.d(TAG, "setIncomingCallInfo incomingCall=$incomingCall")
        val info = com.ryeex.ble.common.model.entity.AppNotification()
        info.type = com.ryeex.ble.common.model.entity.AppNotification.Type.TELEPHONY
        val tel = com.ryeex.ble.common.model.entity.AppNotification.Telephony()
        tel.contact = incomingCall.name
        tel.number = incomingCall.number
        if (incomingCall.status) {
            tel.status =
                com.ryeex.ble.common.model.entity.AppNotification.Telephony.Status.RINGING_UNANSWERABLE
        } else {
            tel.status =
                com.ryeex.ble.common.model.entity.AppNotification.Telephony.Status.DISCONNECTED
        }
        info.telephony = tel
        watchDevice?.sendNotification(info, object : AsyncBleCallback<Void, BleError>() {
            override fun onSuccess(p0: Void?) {
                LOGS.d(TAG, "setIncomingCallInfo sendNotification onSuccess")
            }

            override fun onFailure(p0: BleError?) {
                LOGS.e(TAG, "setIncomingCallInfo sendNotification onFailure:$p0")
            }
        })
    }

    override fun setWristLiftGesture(wristLiftGesture: WristLiftGesture) {

    }

    override fun setWeatherDataHourly(
        weatherDataList: List<WeatherData>,
        hourlyWeatherList: List<WeatherDataHourly>,
        unit: String
    ) {
        LOGS.i(TAG, "setWeatherDataHourly unit=${unit}")
//        LOGS.i(TAG, "setWeatherDataHourly weatherDataList=${Gson().toJson(weatherDataList)}")
//        LOGS.i(TAG, "setWeatherDataHourly hourlyWeatherList=${Gson().toJson(hourlyWeatherList)}")
        if (weatherDataList.isEmpty() || hourlyWeatherList.isEmpty()) {
            return
        }

        val watchWeatherInfo = WeatherInfo()
        val weatherDetail = WeatherDetail()
        watchWeatherInfo.weatherDetail = weatherDetail

        val sectionWeather = SectionWeather()
        val sectionItemList = mutableListOf<SectionItem>()
        sectionWeather.items = sectionItemList
        sectionWeather.interval = 3600
        weatherDetail.sectionWeather = sectionWeather

        val dailyWeather = DailyWeather()
        val dailyItemList = mutableListOf<DailyItem>()
        dailyWeather.items = dailyItemList
        weatherDetail.dailyWeather = dailyWeather

        val weatherDataHourly = hourlyWeatherList[0]
        val cityNames = weatherDataHourly.city?.split(",")
        val name = cityNames?.let {
            it[0]
        }
        watchWeatherInfo.cityName = name
        dailyWeather.cityName = name
        sectionWeather.cityName = name
        sectionWeather.startTime = weatherDataHourly.dt.toInt()

        for (hourlyWeather in hourlyWeatherList) {
            val sectionItem = SectionItem()
            sectionItem.type = dataConverter.parseWeatherType(hourlyWeather.weatherId ?: 804)
            sectionItem.humidity = hourlyWeather.humidity.toInt()
            sectionItem.temperature = hourlyWeather.temp.toInt()
            sectionItem.windSpeed = hourlyWeather.windSpeed.toInt()
            sectionItem.sunriseTime = hourlyWeather.sunrise
            sectionItem.sunsetTime = hourlyWeather.sunset
            sectionItem.uv = hourlyWeather.uvi.roundToInt()
            sectionItemList.add(sectionItem)
        }


        var weatherData: WeatherData?
        for (i in weatherDataList.indices) {
            if (i == 7) {
                break
            }
            weatherData = weatherDataList[i]
            val dailyItem = DailyItem()
            dailyItem.date = weatherData.dt.toInt()
            dailyItem.type = dataConverter.parseWeatherType(weatherData.weatherId ?: 804)
            dailyItem.minTemperature = weatherData.tempMin.toInt()
            dailyItem.maxTemperature = weatherData.tempMax.toInt()
            dailyItemList.add(dailyItem)
        }

        weatherDetail.deviation = TimeZone.getDefault().getOffset(Calendar.DST_OFFSET.toLong())
        weatherDetail.daylight = if (TimeZone.getDefault().inDaylightTime(Date())) {
            1
        } else {
            0
        }

        val units = Units()
        units.temperature = if (unit.lowercase() == "metric") {
            Units.TemperatureType.C
        } else {
            Units.TemperatureType.F
        }
        watchDevice?.setUnitSetting(units, object : AsyncBleCallback<Void, BleError>() {
            override fun onSuccess(p0: Void?) {
                LOGS.d(TAG, "setUnitSetting onSuccess")
            }

            override fun onFailure(bleError: BleError?) {
                LOGS.e(TAG, "setUnitSetting onFailure$bleError")
            }
        })
        watchDevice?.updateWeatherInfo(
            watchWeatherInfo,
            object : AsyncBleCallback<Void?, BleError?>() {
                override fun onSuccess(result: Void?) {
                    LOGS.d(TAG, "setWeatherData onSuccess")
                }

                override fun onFailure(bleError: BleError?) {
                    LOGS.e(TAG, "setWeatherData onFailure:$bleError")
                }
            })
    }

    override fun setSportModeInfo(sportsModeList: SportsModeList) {
        LOGS.i(TAG, "setSportModeInfo $sportsModeList")
        val sportList = mutableListOf<Int>()
        sportsModeList.sportsModes?.forEach {
            it.type?.let { sportId ->
                sportList.add(sportId)
            }
        }
        LOGS.i(TAG, "setSportModeInfo sportList=$sportList")
        watchDevice?.setSportList(sportList, object : AsyncBleCallback<Void, BleError>() {
            override fun onSuccess(p0: Void?) {
                LOGS.i(TAG, "setSportModeInfo onSuccess")
                updateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.SportModeDataUpdated(
                        true
                    )
                )
            }

            override fun onFailure(error: BleError?) {
                LOGS.e(TAG, "setSportModeInfo onFailure:$error")
                updateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.SportModeDataUpdated(
                        false
                    )
                )
            }
        })
    }

    override fun setMusicSwitch(musicSwitch: SwitchSetting) {
        val list = mutableListOf<com.ryeex.watch.adapter.model.entity.SwitchSetting>()
        val switchSetting = com.ryeex.watch.adapter.model.entity.SwitchSetting()
        switchSetting.isEnable = musicSwitch.status
        switchSetting.type = com.ryeex.watch.adapter.model.entity.SwitchSetting.Type.MUSIC_CONTROL
        list.add(switchSetting)
        watchDevice?.setSwitchSetting(list, object : AsyncBleCallback<Void, BleError>() {
            override fun onSuccess(p0: Void?) {
                updateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.MusicSwitchUpdated(
                        true
                    )
                )
            }

            override fun onFailure(p0: BleError?) {
                updateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.MusicSwitchUpdated(
                        false
                    )
                )
            }
        })
    }

    override fun updateApplicationList(data: List<Widget>) {
        val list = mutableListOf<Int>()
        data.forEach {
            list.add(it.functionId)
        }
        LOGS.i(TAG, "updateWidgetList list:$list")
        watchDevice?.setDeviceAppList(list, object : AsyncBleCallback<Void, BleError>() {
            override fun onSuccess(p0: Void?) {
                updateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.UpdateAppList(
                        true
                    )
                )
            }

            override fun onFailure(p0: BleError?) {
                updateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.UpdateAppList(
                        false
                    )
                )
            }
        })
    }

    override fun updateWidgetList(data: List<Widget>) {

    }

    override fun setBleCallingSwitch(status: Boolean) {
        LOGS.i(TAG, "setBleCallingSwitch status:$status")
        val list = mutableListOf<com.ryeex.watch.adapter.model.entity.SwitchSetting>()
        val switchSetting = com.ryeex.watch.adapter.model.entity.SwitchSetting()
        switchSetting.isEnable = status
        switchSetting.type = com.ryeex.watch.adapter.model.entity.SwitchSetting.Type.BT_CALL
        list.add(switchSetting)
        watchDevice?.setSwitchSetting(list, object : AsyncBleCallback<Void, BleError>() {
            override fun onSuccess(p0: Void?) {
                watchDevice?.let {
                    BTHelper().startPair(
                        it.mac,
                        30 * DateUtils.SECOND_IN_MILLIS,
                        object : BTHelper.OnConnectCallback {
                            override fun onSuccess(mac: String) {
                                LOGS.i(TAG, "pair BT success mac=$mac")
                                AppLogs.sendAppLogs("$TAG pair BT success mac=$mac")

                            }

                            override fun onFailure(mac: String) {
                                LOGS.e(TAG, "pair BT fail")
                                AppLogs.sendAppLogs("$TAG pair BT fail mac=$mac")
                            }
                        })
                }
                updateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.BleCallingSwitchUpdated(
                        true
                    )
                )
            }

            override fun onFailure(error: BleError?) {
                LOGS.e(TAG, "setBleCallingSwitch onFailure:$error")
                updateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.BleCallingSwitchUpdated(
                        false
                    )
                )
            }
        })
    }


    override fun setDevice(device: ColorFitDevice) {
        noiseFitDevice = device
    }

    override fun <T> callbackListener(callback: T) {
    }

    override fun <T> callbackListenerNew(callback: T) {
        updateDeviceDataCallback = callback as IUpdateDeviceDataCallback
    }
}

//set distance goal