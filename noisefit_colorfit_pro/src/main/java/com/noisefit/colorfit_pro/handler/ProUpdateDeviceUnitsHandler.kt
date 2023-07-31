package com.noisefit.colorfit_pro.handler

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.crrepa.ble.conn.bean.CRPAlarmInfo
import com.crrepa.ble.conn.bean.CRPContactConfigInfo
import com.crrepa.ble.conn.bean.CRPContactInfo
import com.crrepa.ble.conn.bean.CRPMessageInfo
import com.crrepa.ble.conn.bean.CRPPeriodTimeInfo
import com.crrepa.ble.conn.bean.CRPQuickResponsesDetailInfo
import com.crrepa.ble.conn.bean.CRPSedentaryReminderPeriodInfo
import com.crrepa.ble.conn.bean.CRPWatchFaceBackgroundInfo
import com.crrepa.ble.conn.bean.CRPWatchFaceInfo
import com.crrepa.ble.conn.bean.CRPWatchFaceLayoutInfo
import com.crrepa.ble.conn.listener.CRPBleFirmwareUpgradeListener
import com.crrepa.ble.conn.listener.CRPFileTransListener
import com.crrepa.ble.conn.listener.CRPWatchFaceTransListener
import com.crrepa.ble.conn.type.CRPBleMessageType
import com.crrepa.ble.conn.type.CRPDeviceLanguageType
import com.crrepa.ble.conn.type.CRPMetricSystemType
import com.crrepa.ble.conn.type.CRPTempUnit
import com.crrepa.ble.conn.type.CRPTimeSystemType
import com.crrepa.ble.conn.type.CRPWatchFaceLayoutType
import com.crrepa.ble.ota.realtek.RtkDfuController
import com.crrepa.ble.scan.bean.CRPScanRecordInfo
import com.noisefit.colorfit_pro.base.ProApplicationHandler
import com.noisefit.colorfit_pro.dataConversion.DataConverter
import com.noisefit.colorfit_pro.handler.connect.ProConnectHandler
import com.noisefit.colorfit_pro.handler.connect.ProConnectHandler.Companion.bleConnection
import com.noisefit_commans.constants.CommonGlobals
import com.noisefit_commans.constants.WatchFaceEventsConstants
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
import com.noisefit_commans.models.CustomReplyData
import com.noisefit_commans.models.DeviceLanguage
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.DeviceUnits
import com.noisefit_commans.models.DoNotDisturb
import com.noisefit_commans.models.HeartRateInterval
import com.noisefit_commans.models.IncomingCall
import com.noisefit_commans.models.Language
import com.noisefit_commans.models.MenstrualData
import com.noisefit_commans.models.SedentaryData
import com.noisefit_commans.models.Spo2Data
import com.noisefit_commans.models.SwitchSetting
import com.noisefit_commans.models.TimeFormat
import com.noisefit_commans.models.TimeFormats
import com.noisefit_commans.models.UPIQRCode
import com.noisefit_commans.models.UnitSystem
import com.noisefit_commans.models.Units
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.UserGoals
import com.noisefit_commans.models.UserInfo
import com.noisefit_commans.models.VibrationIntensity
import com.noisefit_commans.models.WalkReminderData
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.models.WatchFaceLayout
import com.noisefit_commans.models.WatchUpdateStatus
import com.noisefit_commans.models.WeatherData
import com.noisefit_commans.models.WristLiftGesture
import com.noisefit_commans.utils.ImageUtil
import com.noisefit_commans.utils.LOGS
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.Date
import javax.inject.Inject


class ProUpdateDeviceUnitsHandler
@Inject
constructor(
    val dataConverter: DataConverter,
    val context: Context,
    val proConnectHandler: ProConnectHandler,
    val proApplicationHandler: ProApplicationHandler,
    val watchDataStore: WatchDataStore
) : UpdateDeviceDataActions() {
    private var iUpdateDeviceDataCallback: IUpdateDeviceDataCallback? = null
    private var colorFitDevice: ColorFitDevice? = null

    companion object {
        var mobileNumber: String? = null

    }

    override fun setDevice(device: ColorFitDevice) {
        colorFitDevice = device
    }

    override fun <T> callbackListener(callback: T) {

    }

    override fun <T> callbackListenerNew(callback: T) {
        iUpdateDeviceDataCallback = callback as IUpdateDeviceDataCallback
    }


    override fun attachCallbacks() {

        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.NOISE_QUBE_O2.deviceType ||
                deviceType == DeviceType.NOISE_QUBE.deviceType ||
                deviceType == DeviceType.COLORFIT_QUAD_CALL.deviceType ||
                deviceType == DeviceType.ICON_2.deviceType ||
                deviceType == DeviceType.ICON_MAX.deviceType||
                deviceType == DeviceType.NOISE_THRIVE.deviceType||
                deviceType == DeviceType.NOISE_ICON_BUZZ.deviceType ||
                deviceType == DeviceType.NOISE_ICON_PLUS.deviceType ||
                deviceType == DeviceType.FORCE.deviceType ||
                deviceType == DeviceType.COLORFIT_ICON_2_VISTA.deviceType ||
                deviceType == DeviceType.QUBE_2.deviceType ||
                deviceType == DeviceType.ICON_3.deviceType ||
                deviceType == DeviceType.NOISE_BOUNCE.deviceType ||
                deviceType == DeviceType.NOISE_SPRINT.deviceType ||
                deviceType == DeviceType.COLORFIT_SPARK.deviceType ||
                deviceType == DeviceType.NOISEFIT_CANVAS.deviceType ||
                deviceType == DeviceType.COLORFIT_ICON_2_VISTA.deviceType ||
                deviceType == DeviceType.FORCE.deviceType ||
                deviceType == DeviceType.COLORFIT_VIVID_CALL.deviceType ||
                deviceType == DeviceType.NOISE_ICON_PLUS.deviceType ||
                deviceType == DeviceType.NOISE_ICON_BUZZ.deviceType ||
                deviceType == DeviceType.COLORFIT_THRILL.deviceType ||
                deviceType == DeviceType.NOISEFIT_TRIUMPH.deviceType ||
                deviceType == DeviceType.COLORFIT_CALIBER3_PLUS.deviceType ||
                deviceType == DeviceType.COLORFIT_MACRO.deviceType ||
                deviceType == DeviceType.NOISEFIT_VENTURE.deviceType
            ) {
                bleConnection?.enableTimingMeasureHeartRate(6)
            }
        }


    }


    private fun getNameAvatar(
        crpContactConfig: CRPContactConfigInfo,
        contact: Contact
    ): Bitmap? {
        val name = contact.name ?: "N"
        val generator = ColorGenerator.MATERIAL // or use DEFAULT

        val drawable = TextDrawable.builder()
            .buildRoundRect(name.substring(0, 1), generator.randomColor, 20) // radius in px

        return ImageUtil.drawableToBitmap(
            drawable,
            crpContactConfig.width,
            crpContactConfig.height
        )
    }

    private fun getProfileImage(
        crpContactConfig: CRPContactConfigInfo,
        contact: Contact
    ): Bitmap {
        var bitmap: Bitmap? = null
        if (contact.photoUri.isNullOrEmpty()) {
            bitmap = getNameAvatar(crpContactConfig, contact)

        } else {
            bitmap = MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                contact.photoUri!!.toUri()
            )

            bitmap = if (bitmap != null && bitmap.width != 0) {
                ImageUtil.changeBitmapSize(
                    bitmap,
                    crpContactConfig.width,
                    crpContactConfig.height
                )
            } else {
                getNameAvatar(crpContactConfig, contact)
            }


        }

        return bitmap!!
    }


    override fun setScreenAwakeInterval(interval: Int) {
        bleConnection?.sendDisplayTime(interval)
        iUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.ScreenAwakeIntervalUpdated(
                success = true
            )
        )
    }

    private fun saveAvatar(
        crpContactConfig: CRPContactConfigInfo,
        contact: Contact,
        success: () -> Unit,
        failed: () -> Unit
    ) {


        val index = contact.id!!.toInt()
        val cRPContactInfo = CRPContactInfo()
        cRPContactInfo.id = index
        cRPContactInfo.name = contact.name
        cRPContactInfo.number = contact.number[0]
        cRPContactInfo.height = crpContactConfig.height
        cRPContactInfo.width = crpContactConfig.width


        bleConnection?.sendContact(cRPContactInfo)

        if (crpContactConfig.width == 0 && crpContactConfig.height == 0) {
            return success.invoke()
        } else {
            bleConnection?.sendContactAvatar(
                index,
                ImageUtil.getCircularBitmap(getProfileImage(crpContactConfig, contact)),
                30,
                object : CRPFileTransListener {
                    override fun onTransProgressStarting() {

                    }

                    override fun onTransProgressChanged(p0: Int) {
                    }

                    override fun onTransCompleted() {
                        LOGS.d("Contact COMPLETED")
                        return success.invoke()

                    }

                    override fun onError(p0: Int) {
                        LOGS.d("Contact ERROR $p0")
                        return failed.invoke()

                    }

                })
        }


    }


    override fun setClearUPIQRCode(id: Int) {
        bleConnection?.deleteElectronicCard(id)
        iUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.ClearUPIQRCodeUpdated(
                success = true
            )
        )

    }

    override fun setUPIQRCode(uPIQRCode: List<UPIQRCode>) {

        uPIQRCode.forEach {
            LOGS.d("qr_code : ${it.id}  ${it.title} ${it.url}")
            bleConnection?.sendElectronicCard(dataConverter.convertQRPayment(it))
        }

        iUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.UPIQRCodeUpdated(
                success = true
            )
        )
    }


    override fun setContactList(contactList: List<Contact>) {

        //   LOGS.d("Contact_list ${Gson().toJson(contactList)}")

        //bleConnection?.clearContact()
        bleConnection?.checkSupportQuickContact { crpContactConfig ->
            val savedContactList = ArrayList<Contact>()
            contactList.forEach { contact ->

                //handling fake data to delete last contact
                if (contact.id.isNullOrEmpty()) {
                    bleConnection?.clearContact()
                    watchDataStore.setContactNumberList(ArrayList())
                    iUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.ContactListUpdated(
                            false
                        )
                    )
                    return@checkSupportQuickContact
                }

                if (contact.id == "0") {
                    bleConnection?.clearContact()
                    watchDataStore.setContactNumberList(ArrayList())
                }
                GlobalScope.launch {


                    saveAvatar(
                        crpContactConfig, contact,
                        success = {
                            savedContactList.add(contact)
                            if (savedContactList.size == contactList.size) {

                                val contactNumberList = watchDataStore.getContactNumberList()
                                contactNumberList.add(contact)

                                watchDataStore.setContactNumberList(contactNumberList)
                                iUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.ContactListUpdated(
                                        true
                                    )
                                )
                            }
                            LOGS.d("SAVED CONTACT")
                        },
                        failed = {
                            iUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.ContactListUpdated(
                                    false
                                )
                            )
                            LOGS.d("FAILED CONTACT")
                        }
                    )
                }


            }
        }


    }

    override fun setWalkReminderPro3(walkReminderData: WalkReminderData) {
        bleConnection?.sendSedentaryReminder(walkReminderData.status)

        iUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.WalkReminderDataUpdated(success = walkReminderData.status)
        )

    }

    override fun setCallBacks() {

    }


    override fun setDeviceUnits(units: DeviceUnits) {

        units.unitSystem?.lowercase().let {
            bleConnection?.sendMetricSystem(
                when (it) {
                    UnitSystem.METRIC.type.lowercase() -> CRPMetricSystemType.METRIC_SYSTEM
                    else -> CRPMetricSystemType.IMPERIAL_SYSTEM
                }
            )
        }
        iUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.DeviceUnitsUpdated(
                success = true
            )
        )

    }

    override fun updateMenstrualData(menstrualData: MenstrualData) {
        bleConnection?.sendPhysiologcalPeriod(dataConverter.setMenstrualData(menstrualData))
        iUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.MenstrualDataUpdated(
                success = true
            )
        )

    }

    override fun setWeatherData(weatherDataList: List<WeatherData>, unit: String) {
        if (weatherDataList.isNotEmpty()) {
            val proWeatherData = dataConverter.formatWeatherData(weatherDataList[0], unit)
            bleConnection?.sendTodayWeather(proWeatherData)
            colorFitDevice?.deviceType?.let { deviceType ->
                if (deviceType == DeviceType.NOISE_QUBE.deviceType ||
                    deviceType == DeviceType.NOISE_QUBE_O2.deviceType ||
                    deviceType == DeviceType.NOISEFIT_ENDURE.deviceType ||
                    deviceType == DeviceType.COLORFIT_QUAD_CALL.deviceType ||
                    deviceType == DeviceType.ICON_2.deviceType ||
                    deviceType == DeviceType.ICON_MAX.deviceType||
                    deviceType == DeviceType.NOISE_THRIVE.deviceType||
                    deviceType == DeviceType.COLORFIT_VIVID_CALL.deviceType ||
                    deviceType == DeviceType.ICON_3.deviceType ||
                    deviceType == DeviceType.NOISEFIT_CANVAS.deviceType ||
                    deviceType == DeviceType.COLORFIT_SPARK.deviceType||
                    deviceType == DeviceType.NOISE_SPRINT.deviceType ||
                    deviceType == DeviceType.NOISE_BOUNCE.deviceType ||
                    deviceType == DeviceType.NOISE_ICON_BUZZ.deviceType ||
                    deviceType == DeviceType.NOISE_ICON_PLUS.deviceType ||
                    deviceType == DeviceType.FORCE.deviceType ||
                    deviceType == DeviceType.COLORFIT_ICON_2_VISTA.deviceType ||
                    deviceType == DeviceType.QUBE_2.deviceType ||
                    deviceType == DeviceType.NOISEFIT_TRIUMPH.deviceType ||
                    deviceType == DeviceType.COLORFIT_THRILL.deviceType ||
                    deviceType == DeviceType.COLORFIT_CALIBER3_PLUS.deviceType ||
                    deviceType == DeviceType.COLORFIT_MACRO.deviceType ||
                    deviceType == DeviceType.NOISEFIT_VENTURE.deviceType
                ) {
                    bleConnection?.sendFutureWeather(
                        dataConverter.formatFutureWeatherData(
                            weatherDataList, unit
                        )
                    )
                }

            }
        }
    }


    override fun setUserInfo(userInfo: UserInfo, userGoals: UserGoals, userName: String?) {
        bleConnection?.sendUserInfo(dataConverter.getCRPUserInfo(userInfo))

        bleConnection?.sendGoalSteps(userGoals.stepGoal)

        iUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.UserInfoUpdated(
                true
            )
        )
    }

    override fun setStressData(sedentaryData: SedentaryData) {
        if (sedentaryData.status) {
            bleConnection?.enableTimingStress()
        } else {
            bleConnection?.disableTimingStress()
        }
        iUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.StressDataUpdated(
                true
            )
        )
    }

    override fun updateCustomReply(customReplyData: CustomReplyData) {


//        LOGS.d("updateCustomReply ${customReplyData.customReplies}")

        bleConnection?.enableQuickResponses()
        customReplyData.customReplies.forEach { item ->
            bleConnection?.sendQuickResponses(
                CRPQuickResponsesDetailInfo(
                    item.index.toByte(),
                    item.content
                )
            )
        }
        iUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.CustomizeReplyUpdated(
                true
            )
        )


    }


    override fun updateAlarm(alarm: AlarmsList, alarmAction: AlarmAction) {
        alarm.alarms?.let { alarmList ->

            //"alarms":[{"alarm_type":"custom","hour":10,"id":11,"minute":19,"repeat_days":[false,true,true,true,true,true,false],"snooze_duration":10,"label":true}]}
            alarmList.forEachIndexed { index, item ->
                val weeklyRepeatList =
                    ArrayList(item.repeatDays!!.subList(1, item.repeatDays!!.size))
                val sundayStatus = weeklyRepeatList[weeklyRepeatList.size - 1]

                weeklyRepeatList.removeAt(weeklyRepeatList.size - 1)

                val newWeeklySubList = ArrayList<Boolean>()
                newWeeklySubList.add(sundayStatus)
                newWeeklySubList.addAll(weeklyRepeatList)
                newWeeklySubList.reverse()


                val alarmClockInfo = CRPAlarmInfo()
                alarmClockInfo.id = item.id

                alarmClockInfo.repeatMode = dataConverter.parseAlarmDaysToBytes(newWeeklySubList)
                alarmClockInfo.hour = item.hour
                alarmClockInfo.minute = item.minute
                alarmClockInfo.isEnable = item.status
                alarmClockInfo.date = Date()
                bleConnection?.sendAlarm(alarmClockInfo)
            }
            iUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.AlarmUpdated(
                    success = true
                )
            )
        }
    }

    override fun updateLanguage(language: Language) {

        bleConnection?.sendDeviceLanguage(
            when (language.language) {
                DeviceLanguage.CHINESE.type -> CRPDeviceLanguageType.LANGUAGE_CHINESE
                else -> CRPDeviceLanguageType.LANGUAGE_ENGLISH
            }
        )
        iUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.LanguageUpdated(
                success = true
            )
        )

    }

    override fun findDevice(findDevice: SwitchSetting) {
        if (findDevice.status) {
            bleConnection?.findDevice()
        } else {
            bleConnection?.stopFindPhone()
        }

    }

    override fun setDeviceDateTime(calender: Calendar, units: TimeFormat) {
        LOGS.d("INSIDE DEVICE setDeviceDateTime")
        bleConnection?.syncTime()
        units.timeFormat?.let {
            bleConnection?.sendTimeSystem(
                when (it.lowercase()) {
                    TimeFormats.HOURS_12.type.lowercase() -> CRPTimeSystemType.TIME_SYSTEM_12
                    else -> CRPTimeSystemType.TIME_SYSTEM_24
                }
            )
        }

        iUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.DeviceTimeSynced(
                success = true
            )
        )

    }


    override fun updateFirmware(fileUri: String) {


        if (!colorFitDevice?.mcuPlatform.isNullOrEmpty()) {
            when (colorFitDevice?.mcuPlatform?.lowercase()) {
                CRPScanRecordInfo.McuPlatform.PLATFORM_GOODIX.name.lowercase() -> {
                    bleConnection?.startFirmwareUpgrade(true, mFirmwareUpgradeListener)
                }
                CRPScanRecordInfo.McuPlatform.PLATFORM_REALTEK.name.lowercase() -> {
                    proApplicationHandler.getBleDevice(noiseFitDevice = colorFitDevice)
                        ?.disconnect()
                    val controller = RtkDfuController()
                    controller.setUpgradeListener(mFirmwareUpgradeListener)
                    controller.start(colorFitDevice?.address)
                }
                else -> {
                    bleConnection?.startFirmwareUpgrade(false, mFirmwareUpgradeListener)
                }
            }
        } else {
            //fallback for old users
            bleConnection?.startFirmwareUpgrade(false, mFirmwareUpgradeListener)
        }

    }

    override fun startCameraMode(status: Boolean) {
        LOGS.d("startCameraMode $status")
        if (status) {
            bleConnection?.enterCameraView()
        } else {
            bleConnection?.exitCameraView()
        }

    }

    override fun setTemperatureUnit(unit: String) {
        if (unit.equals(Units.IMPERIAL.name, true)) {
            bleConnection?.sendTempUnit(CRPTempUnit.FAHRENHEIT.toByte())
        } else {
            bleConnection?.sendTempUnit(CRPTempUnit.CELSIUS.toByte())
        }

    }


    override fun sendAppNotification(appNotification: AppNotification) {
        val info = CRPMessageInfo()
        info.type = when (appNotification.appType) {
            ApplicationType.FACEBOOK.type -> CRPBleMessageType.MESSAGE_FACEBOOK
            ApplicationType.SMS.type -> CRPBleMessageType.MESSAGE_SMS
            ApplicationType.WE_CHAT.type -> CRPBleMessageType.MESSAGE_WECHAT
            ApplicationType.TWITTER.type -> CRPBleMessageType.MESSAGE_TWITTER
            ApplicationType.WHATS_APP.type -> CRPBleMessageType.MESSAGE_WHATSAPP
            ApplicationType.INSTAGRAM.type -> CRPBleMessageType.MESSAGE_INSTAGREM
            ApplicationType.SKYPE.type -> CRPBleMessageType.MESSAGE_SKYPE
            ApplicationType.LINE.type -> CRPBleMessageType.MESSAGE_LINE
            ApplicationType.SPORT_EVENT.type -> CRPBleMessageType.MESSAGE_OTHER
            else -> CRPBleMessageType.MESSAGE_OTHER
        }
        info.message =
            if (appNotification.name != null) appNotification.name + ":" + appNotification.message else appNotification.message
        info.versionCode = getVersionInt()

        bleConnection?.sendMessage(info)
    }

    override fun setWristLiftGesture(wristLiftGesture: WristLiftGesture) {
        bleConnection?.sendQuickView(wristLiftGesture.status)
        iUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.WristLiftGestureUpdated(
                wristLiftGesture.status
            )
        )
    }

    override fun setIncomingCallInfo(incomingCall: IncomingCall) {

        if (incomingCall.status) {
            val info = CRPMessageInfo()
            info.type = CRPBleMessageType.MESSAGE_PHONE
            info.message = incomingCall.name ?: incomingCall.number
            mobileNumber = incomingCall.number
            info.versionCode = getVersionInt()
            bleConnection?.sendMessage(info)
        } else {
            bleConnection?.sendMessage(null)
            bleConnection?.sendCall0ffHook()

        }
    }

    override fun updateDND(doNotDisturb: DoNotDisturb) {
        val info = CRPPeriodTimeInfo()
        when (doNotDisturb.status) {
            true -> {
                info.startHour = 0
                info.startMinute = 0
                info.endHour = 23
                info.endMinute = 59
            }
            else -> {
                info.startHour = 0
                info.startMinute = 0
                info.endHour = 0
                info.endMinute = 0
            }
        }
        bleConnection?.sendDoNotDistrubTime(info)
        iUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.DoNotDisturbUpdated(
                success = true
            )
        )

    }

    override fun setHeartRateInterval(heartRateInterval: HeartRateInterval) {


        var interval = heartRateInterval.interval
        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.NOISE_QUBE_O2.deviceType ||
                deviceType == DeviceType.NOISE_QUBE.deviceType ||
                deviceType == DeviceType.NOISEFIT_ENDURE.deviceType ||
                deviceType == DeviceType.COLORFIT_QUAD_CALL.deviceType ||
                deviceType == DeviceType.ICON_2.deviceType ||
                deviceType == DeviceType.ICON_MAX.deviceType ||
                deviceType == DeviceType.NOISE_THRIVE.deviceType||
                deviceType == DeviceType.NOISE_ICON_BUZZ.deviceType ||
                deviceType == DeviceType.NOISE_ICON_PLUS.deviceType ||
                deviceType == DeviceType.FORCE.deviceType ||
                deviceType == DeviceType.COLORFIT_ICON_2_VISTA.deviceType ||
                deviceType == DeviceType.QUBE_2.deviceType ||
                deviceType == DeviceType.COLORFIT_MACRO.deviceType
            ) {
                interval = 6
            } else {
                interval /= 5
            }
        }
        LOGS.d("setHeartRateInterval $interval ")
        when (heartRateInterval.status) {
            true -> bleConnection?.enableTimingMeasureHeartRate(interval)
            else -> bleConnection?.disableTimingMeasureHeartRate()
        }
        watchDataStore.updateHeartRateStatus(heartRateInterval.status)
        iUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.HeartRateMeasureIntervalSet(
                success = true
            )
        )

    }

    override fun setSpo2Settings(data: Spo2Data) {
        //dividing value by 5
        val interval = data.interval / 5
        when (data.status) {
            true -> bleConnection?.enableTimingMeasureBloodOxygen(interval)
            else -> bleConnection?.disableTimingMeasureBloodOxygen()
        }
        watchDataStore.updateBloodOxygenStatus(Pair(data.status, interval))
        iUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.Spo2SettingsUpdated(
                success = true
            )
        )
    }

    override fun setCustomBackgroundWithLayout(imagePath: Uri, watchFaceLayout: WatchFaceLayout) {
        if (colorFitDevice?.deviceType == DeviceType.NOISEFIT_ENDURE.deviceType) {
            setWatchFaceLayout(watchFaceLayout, imagePath)
        } else {
            bleConnection?.queryWatchFaceLayout { info ->
                sendWatchFaceBg(
                    info,
                    imagePath,
                    watchFaceLayout
                )
            }
        }
    }

    override fun setWatchFaceLayout(watchFaceLayout: WatchFaceLayout, imagePath: Uri) {
        val watchFaceLayoutInfo = CRPWatchFaceLayoutInfo()
        watchFaceLayoutInfo.backgroundPictureMd5 = CRPWatchFaceLayoutType.DEFAULT_WATCH_FACE_BG_MD5
        when (watchFaceLayout.timePosition?.lowercase()) {
            "above" -> watchFaceLayoutInfo.timePosition = CRPWatchFaceLayoutType.WATCH_FACE_TIME_TOP
            else -> watchFaceLayoutInfo.timePosition = CRPWatchFaceLayoutType.WATCH_FACE_TIME_BOTTOM
        }

        watchFaceLayoutInfo.timeTopContent = when (watchFaceLayout.timeTopContent?.lowercase()) {
            "steps" -> CRPWatchFaceLayoutType.WATCH_FACE_CONTENT_STEP
            "sleep" -> CRPWatchFaceLayoutType.WATCH_FACE_CONTENT_SLEEP
            "heart rate" -> CRPWatchFaceLayoutType.WATCH_FACE_CONTENT_HEART_RATE
            "date" -> CRPWatchFaceLayoutType.WATCH_FACE_CONTENT_DATE
            else -> CRPWatchFaceLayoutType.WATCH_FACE_CONTENT_CLOSE
        }

        watchFaceLayoutInfo.timeBottomContent =
            when (watchFaceLayout.timeBottomContent?.lowercase()) {
                "steps" -> CRPWatchFaceLayoutType.WATCH_FACE_CONTENT_STEP
                "sleep" -> CRPWatchFaceLayoutType.WATCH_FACE_CONTENT_SLEEP
                "heart rate" -> CRPWatchFaceLayoutType.WATCH_FACE_CONTENT_HEART_RATE
                "date" -> CRPWatchFaceLayoutType.WATCH_FACE_CONTENT_DATE
                else -> CRPWatchFaceLayoutType.WATCH_FACE_CONTENT_CLOSE
            }


        watchFaceLayout.textColor?.let {
            watchFaceLayoutInfo.textColor = it
        }


        if (colorFitDevice?.deviceType == DeviceType.NOISEFIT_ENDURE.deviceType) {
            bleConnection?.queryDisplayWatchFace { faceType ->
                bleConnection?.sendDisplayWatchFace(faceType.toByte())
                bleConnection?.sendWatchFaceLayout(watchFaceLayoutInfo)
                bleConnection?.queryWatchFaceLayout { info ->
                    sendWatchFaceBg(
                        info,
                        imagePath,
                        watchFaceLayout
                    )
                }
            }
        } else {
            bleConnection?.sendWatchFaceLayout(watchFaceLayoutInfo)
        }
    }

    override fun setSedentaryData(sedentaryData: SedentaryData) {
        bleConnection?.sendSedentaryReminder(sedentaryData.status)
        val sedentaryInfo = CRPSedentaryReminderPeriodInfo()
        sedentaryInfo.period = sedentaryData.interval.toByte()
        sedentaryInfo.startHour = sedentaryData.startHour.toByte()
        sedentaryInfo.endHour = sedentaryData.endHour.toByte()
        bleConnection?.sendSedentaryReminderPeriod(sedentaryInfo)
        bleConnection?.querySedentaryReminder { status ->
            iUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.SedentaryDataUpdated(
                    success = status == sedentaryData.status
                )
            )
        }
    }

    override fun setWatchFace(watchFace: WatchFace) {

        val file = File(watchFace.localFilePath.split("///").last())

        // bleConnection?.sendDisplayWatchFace(0)
        val crpCustomizeWatchFaceInfo =
            CRPWatchFaceInfo(file, CRPWatchFaceInfo.WacthFaceType.DEFAULT)
//        val crpCustomizeWatchFaceInfo = CRPCustomizeWatchFaceInfo(watchFace.faceId!!.toInt(), file)
        bleConnection?.sendWatchFace(
            crpCustomizeWatchFaceInfo,
            crpWatchFaceTransListener,
            30
        )

    }


    private fun sendWatchFaceBg(
        info: CRPWatchFaceLayoutInfo,
        imagePath: Uri,
        watchFaceLayout: WatchFaceLayout?
    ) {
        try {

            var bitmap = MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                imagePath
            )


            val height = info.height
            val width = info.width
            LOGS.d("sendWatchFaceBg $height $width")
            bitmap = ImageUtil.changeBitmapSize(bitmap, width, height)
            val thumWidth = info.thumWidth
            val thumHeight = info.thumHeight


            var thumBitmap: Bitmap? = null
            if (0 < thumHeight && 0 < thumWidth) {
                thumBitmap =
                    ImageUtil.changeBitmapSize(bitmap, thumWidth, thumHeight)
            }

            val watchFaceInfo = CRPWatchFaceBackgroundInfo(
                bitmap, thumBitmap, info.compressionType
            )
            if (colorFitDevice?.deviceType != DeviceType.NOISEFIT_ENDURE.deviceType) {
                watchFaceLayout?.let {
                    setWatchFaceLayout(it, imagePath)
                }
            }

            when (colorFitDevice?.deviceType) {

                DeviceType.COLORFIT_QUAD_CALL.deviceType-> {
                    bleConnection?.sendDisplayWatchFace(2)
                }
                DeviceType.NOISEFIT_ENDURE.deviceType,
                DeviceType.ICON_3.deviceType,
                DeviceType.COLORFIT_THRILL.deviceType,
               // DeviceType.NOISEFIT_TRIUMPH.deviceType
                -> {
                    bleConnection?.sendDisplayWatchFace(3)
                }
                DeviceType.ICON_2.deviceType,
                DeviceType.ICON_MAX.deviceType,
                DeviceType.NOISE_THRIVE.deviceType,
                //DeviceType.COLORFIT_QUAD_CALL.deviceType
                -> {
                    bleConnection?.sendDisplayWatchFace(1)
                }
                DeviceType.NOISE_ICON_BUZZ.deviceType,
                DeviceType.FORCE.deviceType,
                DeviceType.COLORFIT_VIVID_CALL.deviceType,
                DeviceType.ICON_3.deviceType,
                DeviceType.NOISE_ICON_BUZZ.deviceType,
                DeviceType.NOISE_ICON_PLUS.deviceType,
                DeviceType.COLORFIT_ICON_2_VISTA.deviceType -> {
                    bleConnection?.sendDisplayWatchFace(5)
                }
                else -> {
                    bleConnection?.sendDisplayWatchFace(4)
                }

            }

//            when {

//                colorFitDevice?.deviceType.equals(DeviceType.NOISE_ICON_BUZZ.deviceType) -> {
//                    bleConnection?.sendDisplayWatchFace(5)
//                }
//                colorFitDevice?.deviceType.equals(DeviceType.FORCE.deviceType) -> {
//                    bleConnection?.sendDisplayWatchFace(5)
//                }
//                colorFitDevice?.deviceType.equals(DeviceType.COLORFIT_ICON_2_VISTA.deviceType) -> {
//                    bleConnection?.sendDisplayWatchFace(5)
//                }
//                else -> {
//                    bleConnection?.sendDisplayWatchFace(4)
//                }
//            }

            bleConnection?.sendWatchFaceBackground(watchFaceInfo, object : CRPFileTransListener {
                override fun onTransProgressStarting() {
                    LOGS.d("sendWatchFaceBg start")
                    iUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                            WatchUpdateStatus(status = UpdateStatus.STARTED)
                        )
                    )
                }


                override fun onTransProgressChanged(p0: Int) {
                    LOGS.d("sendWatchFaceBg change $p0")
                    iUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                            WatchUpdateStatus(
                                status = UpdateStatus.PROGRESS,
                                percentagePercentage = p0
                            )
                        )
                    )

                }

                override fun onTransCompleted() {
                    LOGS.d("sendWatchFaceBg complete")

                    iUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                            WatchUpdateStatus(
                                status = UpdateStatus.COMPLETED,
                                wStatus = WatchFaceEventsConstants.Complete
                            )
                        )
                    )

                }

                override fun onError(p0: Int) {
                    LOGS.d("sendWatchFaceBg $p0")
                    bleConnection?.abortWatchFaceBackground()

                    iUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                            WatchUpdateStatus(
                                status = UpdateStatus.ERROR,
                                wStatus = p0.toString()
                            )
                        )
                    )
                }

            })

        } catch (e: Exception) {
            bleConnection?.abortWatchFaceBackground()
            e.printStackTrace()

        }
    }

    override fun setCustomBackground(imagePath: Uri, firmware: String) {
        bleConnection?.queryWatchFaceLayout { info -> sendWatchFaceBg(info, imagePath, null) }
    }


    private val crpWatchFaceTransListener = object : CRPWatchFaceTransListener {
        override fun onTransProgressChanged(p0: Int) {
            CommonGlobals.isWatchDataUpdating = true
            iUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                    WatchUpdateStatus(status = UpdateStatus.PROGRESS, percentagePercentage = p0)
                )
            )


        }

        override fun onTransCompleted() {
            CommonGlobals.isWatchDataUpdating = false
            iUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                    WatchUpdateStatus(
                        status = UpdateStatus.COMPLETED,
                        wStatus = WatchFaceEventsConstants.Complete
                    )
                )
            )
        }

        override fun onTransProgressStarting() {
            CommonGlobals.isWatchDataUpdating = true
            iUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                    WatchUpdateStatus(status = UpdateStatus.STARTED)
                )
            )
        }

        override fun onError(p0: Int) {
            bleConnection?.abortWatchFace()
            LOGS.d("Transfer Failed $p0")
            CommonGlobals.isWatchDataUpdating = false
            iUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                    WatchUpdateStatus(status = UpdateStatus.ERROR,
                    wStatus = p0.toString())
                )
            )

        }

        override fun onInstallStateChange(p0: Boolean) {

        }
    }


    private fun getVersionInt(): Int {
        try {
            CommonGlobals.version?.let {
                val version = it.split("-")
                return version.last().replace(".", "").toInt()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    override fun setVibrationIntensity(vibrationIntensity: VibrationIntensity) {
        bleConnection?.sendVibrationStrength(dataConverter.convertVibration(vibrationIntensity.vibrationIntensityEnum))
        iUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.VibrationIntensityUpdated(
                true
            )
        )
    }

    private val mFirmwareUpgradeListener: CRPBleFirmwareUpgradeListener =
        object : CRPBleFirmwareUpgradeListener {
            override fun onFirmwareDownloadStarting() {
                LOGS.d("mFirmwareUpgradeListener start")

                iUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                        WatchUpdateStatus(status = UpdateStatus.STARTED)
                    )
                )

            }

            override fun onFirmwareDownloadComplete() {
                LOGS.d("mFirmwareUpgradeListener complete")
//                iUpdateDeviceDataCallback?.onUpdateDataReceived(
//                    UpdateDeviceDataCallback.FirmwareUpgradeProgress(
//                        WatchUpdateStatus(status = UpdateStatus.COMPLETED)
//                    )
//                )

            }

            override fun onUpgradeProgressStarting(p0: Boolean) {
                LOGS.d("mFirmwareUpgradeListener progress $p0")
//                iUpdateDeviceDataCallback?.onUpdateDataReceived(
//                    UpdateDeviceDataCallback.FirmwareUpgradeProgress(
//                        WatchUpdateStatus(status = UpdateStatus.STARTED)
//                    )
//                )
            }


            override fun onUpgradeProgressChanged(percent: Int, speed: Float) {
                LOGS.d("mFirmwareUpgradeListener change $percent $speed")
                iUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                        WatchUpdateStatus(
                            status = UpdateStatus.PROGRESS,
                            percentagePercentage = percent
                        )
                    )
                )


            }

            override fun onUpgradeCompleted() {
                LOGS.d("mFirmwareUpgradeListener complete")
                iUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                        WatchUpdateStatus(status = UpdateStatus.COMPLETED)
                    )
                )


            }

            override fun onUpgradeAborted() {
                LOGS.d("mFirmwareUpgradeListener abort")
                bleConnection?.abortFirmwareUpgrade()
                iUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                        WatchUpdateStatus(status = UpdateStatus.ERROR)
                    )
                )

            }

            override fun onError(errorType: Int, message: String) {
                LOGS.d("mFirmwareUpgradeListener error $errorType $message")
                iUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                        WatchUpdateStatus(status = UpdateStatus.ERROR, message = message)
                    )
                )
            }
        }


    override fun setFactoryReset() {
        bleConnection?.reset()
    }

}