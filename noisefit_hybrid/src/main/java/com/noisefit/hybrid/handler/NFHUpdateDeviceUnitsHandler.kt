package com.noisefit.hybrid.handler

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.text.TextUtils
import android.util.Size
import androidx.annotation.RequiresApi
import cn.appscomm.bluetoothsdk.app.BluetoothSDK
import cn.appscomm.bluetoothsdk.app.BluetoothSDK.jumpToTakePhoto
import cn.appscomm.bluetoothsdk.app.SettingType
import cn.appscomm.bluetoothsdk.interfaces.ResultCallBack
import cn.appscomm.bluetoothsdk.model.CustomizeWatchFaceExData
import cn.appscomm.bluetoothsdk.model.ReminderData
import cn.appscomm.bluetoothsdk.model.ReminderExData
import cn.appscomm.bluetoothsdk.model.WeatherDataEx
import com.noisefit.hybrid.base.VisionCommands
import com.noisefit.hybrid.dataconversions.DataConverter
import com.noisefit.hybrid.utils.*
import com.noisefit.hybrid.utils.watchface.CustomWatchface
import com.noisefit.hybrid.utils.watchface.CustomWatchfaceManager
import com.noisefit.hybrid.utils.watchface.Widget
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.enums.ApplicationType
import com.noisefit_commans.interfaces.device_data.IUpdateDeviceDataCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.AppConversionUtils.getAgeFromDOB
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.math.truncate


class NFHUpdateDeviceUnitsHandler
@Inject
constructor(
    private val context: Context,
    private val bitwiseUtils: BitwiseUtils,
    private val dataConverter: DataConverter,
    private val bitwiseHelperUtils: BitwiseHelperUtils,
    private val watchDataStore: WatchDataStore,
    private val onlineWatchFacesVision: OnlineWatchFacesVision,
    private val bluetoothsdkExp: BluetoothSDK_Exp,
    private val visionCommands: VisionCommands
) : UpdateDeviceDataActions() {


    private var testUpdateDeviceDataCallback: IUpdateDeviceDataCallback? = null
    private var colorFitDevice: ColorFitDevice? = null
    private var mobileNumber: String? = null

    override fun <T> callbackListener(callback: T) {}
    override fun <T> callbackListenerNew(callback: T) {
        testUpdateDeviceDataCallback = callback as IUpdateDeviceDataCallback
    }

    override fun setDevice(device: ColorFitDevice) {
        colorFitDevice = device
    }

    private fun set8002CallbackNull() {
        BluetoothSDK.set8002CallBack(null)
    }


    override fun setDeviceUnits(units: DeviceUnits) {


        BluetoothSDK.setUnit(
            resultCallBack, when (units.unitSystem?.lowercase()) {
                UnitSystem.METRIC.type.lowercase() -> SettingType.UNIT_METRIC
                else -> SettingType.UNIT_INCH
            }
        )

        BluetoothSDK.setWeatherUnit(
            resultCallBack, when (units.unitSystem?.lowercase()) {
                UnitSystem.METRIC.type.lowercase() -> SettingType.UNIT_CELSIUS
                else -> SettingType.UNIT_FAHRENHEIT
            }
        )
        updateLanguage(Language())
    }

    override fun setDeviceDateTime(calender: Calendar, units: TimeFormat) {
        LOGS.d("INSIDE setDeiceDateTime")

        val timeZoneOffset = dataConverter.getTimeZoneOffset()

        BluetoothSDK.setDeviceTime(
            resultCallBack,
            calender.get(Calendar.YEAR),
            calender.get(Calendar.MONTH) + 1,
            calender.get(Calendar.DAY_OF_MONTH),
            calender.get(Calendar.HOUR_OF_DAY),
            calender.get(Calendar.MINUTE),
            calender.get(Calendar.SECOND),
            1,  //0 set the time normally, 1 adjust the pointer to the current time
            timeZoneOffset.first,
            timeZoneOffset.second,
            timeZoneOffset.third
        )
        BluetoothSDK.setTimeFormat(
            resultCallBack, when (units.timeFormat?.lowercase()) {
                TimeFormats.HOURS_12.type.lowercase() -> SettingType.TIME_FORMAT_12
                else -> SettingType.TIME_FORMAT_24
            }
        )


    }

    private fun fromHexToRgba(hexString: String): Int {
        if (hexString.length < 6) {
            return 0
        }
        val r = Integer.parseInt(hexString.substring(0, 2), 16)
        val g = Integer.parseInt(hexString.substring(2, 4), 16)
        val b = Integer.parseInt(hexString.substring(4, 6), 16)
        val a = 0xff

        val color = a + b shl 16 + g shl 32 + r shl 48
        return color
    }

    private fun fromRgbToRgba(r: Int, g: Int, b: Int): Int {

        val a = 0xff

        val color = a + b shl 16 + g shl 32 + r shl 48
        return color
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun setWatchFaceCustom(watchFace: CustomWatchFace) {


        var r = 255
        var g = 255
        var b = 255
        var a = 255
        watchFace.color?.let {
            a = Color.alpha(it)
            r = Color.red(it)
            g = Color.green(it)
            b = Color.blue(it)
        }

        val imagePath = Uri.parse(watchFace.selectedImagePath).path?.split("//")?.last()
        var color = String.format("#%02x%02x%02x", r, g, b)
        //LOGS.d("updateWatchface__ watchFace 1 ${fromRgbToRgba(r,g,b)}")
        color = "${color}ff" //hack adding alpha manually
        val customWatchFace = CustomWatchface()

        val widget = Widget()
        widget.type = 13
        widget.position = Point(18, 36)
        widget.style = 2

        widget.color = Color.parseColor(color)

        val widgetList = ArrayList<Widget>()
        widgetList.add(widget)

        customWatchFace.imagePath = imagePath
        customWatchFace.size = Size(368, 448)
        customWatchFace.thumbnailSize = Size(180, 219)
        customWatchFace.widgetList = widgetList

        var maxProgress = 0
        CustomWatchfaceManager.getInstance()
            .setCustomWatchface(customWatchFace, object : ResultCallBack {
                override fun onSuccess(p0: Int, p1: Array<out Any>?) {
                    when (p0) {
                        ResultCallBack.TYPE_UPLOAD_IMAGE_PROGRESS -> {
                            if (!p1.isNullOrEmpty() && p1[0] is Int) {
                                var progress = p1[0] as Int
                                if (progress > 0) {
                                    if (maxProgress > 100) {
                                        progress =
                                            ((progress.toDouble() / maxProgress) * 100).toInt()
                                    }
                                    LOGS.d("updateWatchface__", "imagePath=====" + progress)
                                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                        UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                            WatchUpdateStatus(
                                                status = UpdateStatus.PROGRESS,
                                                percentagePercentage = progress
                                            )
                                        )
                                    )
                                }
                            }
                        }
                        ResultCallBack.TYPE_UPLOAD_IMAGE_MAX -> {
                            if (!p1.isNullOrEmpty() && p1[0] is Int) {
                                val progress = p1[0] as Int
                                if (progress > 0) {
                                    maxProgress = progress
                                    LOGS.d(
                                        "updateWatchface__",
                                        "imagePath=====max::" + progress
                                    )
                                    //baseUpdateDeviceDataCallbacks?.onCustomizeWatchFaceProgress(WatchFaceStatus(status = "progress", percentagePercentage = progress))
                                }
                            }
                        }
                        ResultCallBack.TYPE_UPLOAD_IMAGE_RESULT -> {
                            if (!p1.isNullOrEmpty() && p1[0] is Boolean) {
                                val result = p1[0] as Boolean
                                if (result) {
                                    AppLogs.sendAppLogs("Custom watchface update completed ")
                                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                        UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                            WatchUpdateStatus(status = UpdateStatus.COMPLETED)
                                        )
                                    )
                                } else {
                                    AppLogs.sendAppLogs("Custom watchface update error ")
                                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                        UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                            WatchUpdateStatus(status = UpdateStatus.ERROR)
                                        )
                                    )
                                }
                            }

                        }
                        ResultCallBack.TYPE_SET_CUSTOMIZE_WATCH_FACE_EX -> {
                            LOGS.d("updateWatchface__", "imagePath=====" + "completed==2")
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                    WatchUpdateStatus(status = UpdateStatus.COMPLETED)
                                )
                            )
                        }
                    }
                }

                override fun onFail(p0: Int) {
                    AppLogs.sendAppLogs("Custom watchface update fail $p0")
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                            WatchUpdateStatus(status = UpdateStatus.ERROR)
                        )
                    )
                }

            })


    }

    override fun setUserInfo(userInfo: UserInfo, userGoals: UserGoals, userName: String?) {
        BluetoothSDK.setStepGoal(resultCallBack, userGoals.stepGoal)
        BluetoothSDK.setCaloriesGoal(resultCallBack, userGoals.caloriesGoal)
        BluetoothSDK.setDistanceGoal(resultCallBack, userGoals.distanceGoal / 1000)
        BluetoothSDK.setSleepGoal(resultCallBack, 8)

        BluetoothSDK.setUserInfo(
            resultCallBack, when (userInfo.gender.lowercase()) {
                Gender.FEMALE.type.lowercase() -> 1
                else -> 0
            }, getAgeFromDOB(userInfo.dob), userInfo.height, userInfo.weight
        )
    }

    override fun updateAlarm(alarm: AlarmsList, alarmAction: AlarmAction) {

        val oldAlarms = NFHQueryDeviceUnitsHandler.alarmsList?.alarms
        alarm.alarms?.forEach { item ->
            when (colorFitDevice?.deviceType) {
                DeviceType.NOISEFIT_HYBRID.deviceType -> {

                    val subList = ArrayList(item.repeatDays!!.subList(1, item.repeatDays!!.size))
                    subList.reverse()
                    //subList.add(0, item.repeatDays!![0])


                    val newReminder = ReminderData(
                        item.id,
                        SettingType.REMINDER_AWAKE,
                        item.hour,
                        item.minute,
                        AlarmsList.getIntFromBoolean(subList),
                        item.status,
                        "Alarm"
                    )

                    val oldReminderAlarm =
                        oldAlarms?.firstOrNull { alarm -> alarm.id == newReminder.id }
                    var oldReminder = ReminderData()

                    val action = when (item.alarmAction) {
                        AlarmAction.ALARM_CHANGE -> SettingType.REMINDER_ACTION_CHANGE
                        AlarmAction.ALARM_DELETE -> SettingType.REMINDER_ACTION_DELETE_ONE
                        AlarmAction.ALARM_ADD -> SettingType.REMINDER_ACTION_NEW
                        else -> SettingType.REMINDER_ACTION_NEW
                    }


                    if (oldReminderAlarm != null) {
                        oldReminder = ReminderData(
                            oldReminderAlarm.id,
                            SettingType.REMINDER_AWAKE,
                            oldReminderAlarm.hour,
                            oldReminderAlarm.minute,
                            AlarmsList.getIntFromBoolean(subList),
                            oldReminderAlarm.status,
                            "Alarm"
                        )
                    }

                    BluetoothSDK.setReminder(
                        resultCallBack,
                        action,
                        SettingType.REMINDER_PROTOCOL_BASE_WITH_DATE,
                        oldReminder,
                        newReminder
                    )
                }

                DeviceType.COLORFIT_NAV.deviceType, DeviceType.COLORFIT_VISION.deviceType -> {
                    val time = ReminderExData.Time()
                    time.hour = item.hour
                    time.min = item.minute
                    val list = ArrayList<ReminderExData.Time>()
                    list.add(time)
                    val calendar = Calendar.getInstance()
                    val subList = ArrayList(item.repeatDays!!.subList(1, item.repeatDays!!.size))
                    subList.reverse()
                    subList.add(0, item.repeatDays!![0])
                    val reminderExData = ReminderExData.Builder()
                        .id(item.id)
                        .type(SettingType.REMINDER_AWAKE)
                        .cycle(AlarmsList.booleanArrayToBinaryString(subList))
                        .enable(item.status)
                        .date(
                            ReminderExData.Date(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH) + 1,
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )
                        )
                        .timeList(list)
                        .repeat(ReminderExData.Repeat(SettingType.REPEAT_TYPE_DAY, 1))
                        .customType("Alarm")
                        .shockRingType(SettingType.SHOCK_TYPE_ALARM)
                        .build()


                    val action = when (item.alarmAction) {
                        AlarmAction.ALARM_CHANGE -> SettingType.REMINDER_EX_ACTION_CHANGE
                        AlarmAction.ALARM_DELETE -> SettingType.REMINDER_EX_ACTION_DELETE_ONE
                        AlarmAction.ALARM_ADD -> SettingType.REMINDER_EX_ACTION_NEW
                        else -> SettingType.REMINDER_EX_ACTION_NEW
                    }



                    BluetoothSDK.setReminderEx(resultCallBack, action, reminderExData)
                }
            }
        }
    }


    override fun deleteAlarm(alarm: AlarmsList) {
        LOGS.d("Reminders $alarm")
        val oldAlarms = NFHQueryDeviceUnitsHandler.alarmsList?.alarms
        alarm.alarms?.forEach { item ->
            when (colorFitDevice?.deviceType) {
                DeviceType.NOISEFIT_HYBRID.deviceType -> {
                    val newReminder = ReminderData(
                        item.id,
                        SettingType.REMINDER_CUSTOM,
                        item.hour,
                        item.minute,
                        AlarmsList.getIntFromBoolean(item.repeatDays),
                        item.status,
                        "Reminder"
                    )

                    val oldReminderAlarm =
                        oldAlarms?.firstOrNull { alarm -> alarm.id == newReminder.id }
                    var oldReminder = ReminderData()
                    val action = if (oldReminderAlarm == null) {
                        if (newReminder.status) {
                            SettingType.REMINDER_ACTION_NEW
                        } else {
                            SettingType.REMINDER_ACTION_DELETE_ONE
                        }
                    } else {
                        SettingType.REMINDER_ACTION_CHANGE
                    }

                    if (oldReminderAlarm != null) {
                        oldReminder = ReminderData(
                            oldReminderAlarm.id,
                            SettingType.REMINDER_AWAKE,
                            oldReminderAlarm.hour,
                            oldReminderAlarm.minute,
                            AlarmsList.getIntFromBoolean(item.repeatDays),
                            oldReminderAlarm.status,
                            "Reminder"
                        )
                    }

                    BluetoothSDK.setReminder(
                        resultCallBack,
                        SettingType.REMINDER_ACTION_DELETE_ONE,
                        SettingType.REMINDER_PROTOCOL_BASE_WITH_DATE,
                        oldReminder,
                        newReminder
                    )
                }

                DeviceType.COLORFIT_NAV.deviceType, DeviceType.COLORFIT_VISION.deviceType -> {
                    val time = ReminderExData.Time()
                    time.hour = item.hour
                    time.min = item.minute
                    val list = ArrayList<ReminderExData.Time>()
                    list.add(time)
                    val calendar = Calendar.getInstance()

                    val reminderExData = ReminderExData.Builder()
                        .id(item.id)
                        .type(SettingType.REMINDER_AWAKE)
                        .cycle(AlarmsList.getIntFromBoolean(item.repeatDays))
                        .enable(item.status)
                        .date(
                            ReminderExData.Date(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH) + 1,
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )
                        )
                        .timeList(list)
                        .repeat(ReminderExData.Repeat(SettingType.REPEAT_TYPE_DAY, 1))
                        .customType("Alarm")
                        .shockRingType(SettingType.SHOCK_TYPE_ALARM)
                        .build()
                    val oldReminderAlarm =
                        oldAlarms?.firstOrNull { alarm -> alarm.id == reminderExData.id }

                    val action = if (oldReminderAlarm == null) {
                        if (reminderExData.enable) {
                            SettingType.REMINDER_EX_ACTION_NEW
                        } else {
                            SettingType.REMINDER_EX_ACTION_DELETE_ONE
                        }
                    } else {
                        SettingType.REMINDER_EX_ACTION_CHANGE
                    }

                    BluetoothSDK.setReminderEx(
                        resultCallBack,
                        SettingType.REMINDER_EX_ACTION_DELETE_ONE,
                        reminderExData
                    )
                }
            }
        }
        AppLogs.sendAppLogs("delete alarm success ")

    }

    override fun deleteReminders(reminders: ReminderList) {

        when (colorFitDevice?.deviceType) {
            DeviceType.NOISEFIT_HYBRID.deviceType -> {
                reminders.reminders?.forEach { reminder ->
                    var type: Int = 0
                    var value: Int = 0
                    if (reminder.repeatMode.equals("Never", true)) {
                        type = SettingType.REPEAT_TYPE_NO_REPEAT
                        value = 0
                    } else if (reminder.repeatMode.equals("Every Day", true)) {
                        type = SettingType.REPEAT_TYPE_DAY
                        value = 1
                    } else if (reminder.repeatMode.equals("Every Week", true)) {
                        type = SettingType.REPEAT_TYPE_WEEK
                        value = 2
                    } else if (reminder.repeatMode.equals("Every Month", true)) {
                        type = SettingType.REPEAT_TYPE_MONTH
                        value = 3
                    } else if (reminder.repeatMode.equals("Every Year", true)) {
                        type = SettingType.REPEAT_TYPE_YEAR
                        value = 4
                    }

                    val reminderData = ReminderData(
                        reminder.id,
                        SettingType.REMINDER_CUSTOM,
                        reminder.year,
                        reminder.month,
                        reminder.day,
                        reminder.hour,
                        reminder.minute,
                        SettingType.SHOCK_TYPE_ALARM,
                        0,
                        true,
                        type,
                        value,
                        reminder.label
                    )

                    BluetoothSDK.setReminder(
                        object : ResultCallBack {
                            override fun onSuccess(p0: Int, p1: Array<out Any>?) {
                                when (p0) {
                                    ResultCallBack.TYPE_DELETE_A_REMINDER -> {
                                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                            UpdateDeviceDataCallback.DeleteReminder(
                                                true
                                            )
                                        )
                                    }
                                }
                            }

                            override fun onFail(p0: Int) {
                                when (p0) {
                                    ResultCallBack.TYPE_DELETE_A_REMINDER -> {
                                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                            UpdateDeviceDataCallback.DeleteReminder(
                                                false
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        SettingType.REMINDER_EX_ACTION_DELETE_ONE,
                        SettingType.REMINDER_PROTOCOL_BASE_WITH_DATE,
                        null,
                        reminderData
                    )
                }
            }

            DeviceType.COLORFIT_NAV.deviceType, DeviceType.COLORFIT_VISION.deviceType -> {
                reminders.reminders?.forEach { reminder ->
                    val time = ReminderExData.Time()
                    time.hour = reminder.hour
                    time.min = reminder.minute
                    val list = ArrayList<ReminderExData.Time>()
                    list.add(time)

                    val repeat_mode = ReminderExData.Repeat()
                    if (reminder.repeatMode.equals("Never", true)) {
                        repeat_mode.type = SettingType.REPEAT_TYPE_NO_REPEAT
                        repeat_mode.value = 0
                    } else if (reminder.repeatMode.equals("Every Day", true)) {
                        repeat_mode.type = SettingType.REPEAT_TYPE_DAY
                        repeat_mode.value = 1
                    } else if (reminder.repeatMode.equals("Every Week", true)) {
                        repeat_mode.type = SettingType.REPEAT_TYPE_WEEK
                        repeat_mode.value = 2
                    } else if (reminder.repeatMode.equals("Every Month", true)) {
                        repeat_mode.type = SettingType.REPEAT_TYPE_MONTH
                        repeat_mode.value = 3
                    } else if (reminder.repeatMode.equals("Every Year", true)) {
                        repeat_mode.type = SettingType.REPEAT_TYPE_YEAR
                        repeat_mode.value = 4
                    }
                    val reminderExData = ReminderExData.Builder()
                        .id(reminder.id)
                        .type(SettingType.REMINDER_CUSTOM)
                        .enable(true)
                        .date(ReminderExData.Date(reminder.year, reminder.month, reminder.day))
                        .timeList(list)
                        .repeat(repeat_mode)
                        .customType(reminder.label)
                        .shockRingType(SettingType.SHOCK_TYPE_ALARM)
                        .build()

                    LOGS.d("noise_delete reminder :-- ", " $reminderExData::")
                    BluetoothSDK.setReminderEx(
                        resultCallBackNew,
                        SettingType.REMINDER_EX_ACTION_DELETE_ONE,
                        reminderExData
                    )
                }
            }
        }
        AppLogs.sendAppLogs("delete reminder success ")

    }

    override fun addReminder(reminder: ReminderList.Reminder) {
        try {

            when (colorFitDevice?.deviceType) {
                DeviceType.NOISEFIT_HYBRID.deviceType -> {

                    var type: Int = 0
                    var value: Int = 0
                    if (reminder.repeatMode == "Never") {
                        type = SettingType.REPEAT_TYPE_NO_REPEAT
                        value = 0
                    } else if (reminder.repeatMode == "Every Day") {
                        type = SettingType.REPEAT_TYPE_DAY
                        value = 1
                    } else if (reminder.repeatMode == "Every Week") {
                        type = SettingType.REPEAT_TYPE_WEEK
                        value = 2
                    } else if (reminder.repeatMode == "Every Month") {
                        type = SettingType.REPEAT_TYPE_MONTH
                        value = 3
                    } else if (reminder.repeatMode == "Every Year") {
                        type = SettingType.REPEAT_TYPE_YEAR
                        value = 4
                    }

                    val reminderData = ReminderData(
                        reminder.id,
                        SettingType.REMINDER_CUSTOM,
                        reminder.year,
                        reminder.month,
                        reminder.day,
                        reminder.hour,
                        reminder.minute,
                        SettingType.SHOCK_TYPE_ALARM,
                        0,
                        true,
                        type,
                        value,
                        reminder.label
                    )

                    var reminderType = if (reminder.id != 0) {
                        SettingType.REMINDER_ACTION_CHANGE
                    } else {
                        SettingType.REMINDER_ACTION_NEW
                    }


                    BluetoothSDK.setReminder(
                        object : ResultCallBack {
                            override fun onSuccess(p0: Int, p1: Array<out Any>?) {
                                when (p0) {
                                    ResultCallBack.TYPE_NEW_REMINDER -> {
                                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                            UpdateDeviceDataCallback.AddReminder(
                                                true
                                            )
                                        )
                                    }
                                    ResultCallBack.TYPE_CHANGE_REMINDER -> {
                                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                            UpdateDeviceDataCallback.AddReminder(
                                                false
                                            )
                                        )
                                    }
                                }
                                AppLogs.sendAppLogs("Add reminder success ")

                            }

                            override fun onFail(p0: Int) {
                                AppLogs.sendAppLogs("Add reminder failed ")

                                when (p0) {
                                    ResultCallBack.TYPE_NEW_REMINDER -> {
                                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                            UpdateDeviceDataCallback.AddReminder(
                                                false
                                            )
                                        )
                                    }
                                    ResultCallBack.TYPE_CHANGE_REMINDER -> {
                                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                            UpdateDeviceDataCallback.AddReminder(
                                                false
                                            )
                                        )
                                    }
                                }

                            }
                        },
                        reminderType,
                        SettingType.REMINDER_PROTOCOL_BASE_WITH_DATE,
                        null,
                        reminderData
                    )

                }

                DeviceType.COLORFIT_NAV.deviceType -> {
                    val time = ReminderExData.Time()
                    time.hour = reminder.hour
                    time.min = reminder.minute
                    val list = ArrayList<ReminderExData.Time>()
                    list.add(time)

                    val repeat_mode = ReminderExData.Repeat()
                    if (reminder.repeatMode.equals("Never", true)) {
                        repeat_mode.type = SettingType.REPEAT_TYPE_NO_REPEAT
                        repeat_mode.value = 0
                    } else if (reminder.repeatMode.equals("Every Day", true)) {
                        repeat_mode.type = SettingType.REPEAT_TYPE_DAY
                        repeat_mode.value = 1
                    } else if (reminder.repeatMode.equals("Every Week", true)) {
                        repeat_mode.type = SettingType.REPEAT_TYPE_WEEK
                        repeat_mode.value = 2
                    } else if (reminder.repeatMode.equals("Every Month", true)) {
                        repeat_mode.type = SettingType.REPEAT_TYPE_MONTH
                        repeat_mode.value = 3
                    } else if (reminder.repeatMode.equals("Every Year", true)) {
                        repeat_mode.type = SettingType.REPEAT_TYPE_YEAR
                        repeat_mode.value = 4
                    }
                    if (reminder.id != 0) {
                        val reminderExData = ReminderExData.Builder()
                            .id(reminder.id)
                            .type(SettingType.REMINDER_CUSTOM)
                            .enable(true)
                            .date(ReminderExData.Date(reminder.year, reminder.month, reminder.day))
                            .timeList(list)
                            .repeat(repeat_mode)
                            .customType(reminder.label)
                            .shockRingType(SettingType.SHOCK_TYPE_ALARM)
                            .build()
                        BluetoothSDK.setReminderEx(
                            resultCallBackNew,
                            SettingType.REMINDER_EX_ACTION_CHANGE,
                            reminderExData
                        )
                    } else {
                        val reminderExData = ReminderExData.Builder()
                            .type(SettingType.REMINDER_CUSTOM)
                            .enable(true)
                            .date(ReminderExData.Date(reminder.year, reminder.month, reminder.day))
                            .timeList(list)
                            .repeat(repeat_mode)
                            .customType(reminder.label)
                            .shockRingType(SettingType.SHOCK_TYPE_ALARM)
                            .build()
                        BluetoothSDK.setReminderEx(
                            resultCallBackNew,
                            SettingType.REMINDER_EX_ACTION_NEW,
                            reminderExData
                        )
                    }
                }
                DeviceType.COLORFIT_VISION.deviceType -> {
                    val time = ReminderExData.Time()
                    time.hour = reminder.hour
                    time.min = reminder.minute
                    val list = ArrayList<ReminderExData.Time>()
                    list.add(time)

//                    val repeat_mode = ReminderExData.Repeat()
//
//                    repeat_mode.type = SettingType.REPEAT_TYPE_NO_REPEAT
//                    repeat_mode.value = 0
//                    val repeat_mode = ReminderExData.Repeat()
//                    if (reminder.repeatMode.equals("Never", true)) {
//                        repeat_mode.type = SettingType.REPEAT_TYPE_NO_REPEAT
//                        repeat_mode.value = 0
//                    } else if (reminder.repeatMode.equals("Every Day", true)) {
//                        repeat_mode.type = SettingType.REPEAT_TYPE_DAY
//                        repeat_mode.value = 1
//                    } else if (reminder.repeatMode.equals("Every Week", true)) {
//                        repeat_mode.type = SettingType.REPEAT_TYPE_WEEK
//                        repeat_mode.value = 2
//                    } else if (reminder.repeatMode.equals("Every Month", true)) {
//                        repeat_mode.type = SettingType.REPEAT_TYPE_MONTH
//                        repeat_mode.value = 3
//                    } else if (reminder.repeatMode.equals("Every Year", true)) {
//                        repeat_mode.type = SettingType.REPEAT_TYPE_YEAR
//                        repeat_mode.value = 4
//                    }


                    val subList =
                        ArrayList(reminder.repeatDays!!.subList(0, reminder.repeatDays!!.size))
//                    LOGS.d("Reminder___ ${Gson().toJson(subList)}")
//                    subList.reverse()
                    if (reminder.id != 0) {
                        val reminderExData = ReminderExData.Builder()
                            .id(reminder.id)
                            .type(SettingType.REMINDER_CUSTOM)
                            .enable(true)
                            .date(ReminderExData.Date(reminder.year, reminder.month, reminder.day))
                            .timeList(list)
                            .cycle(AlarmsList.getIntFromBoolean(subList))
                            .customType(reminder.label)
                            .shockRingType(SettingType.SHOCK_TYPE_ALARM)
                            .build()
                        BluetoothSDK.setReminderEx(
                            resultCallBackNew,
                            SettingType.REMINDER_EX_ACTION_CHANGE,
                            reminderExData
                        )
                    } else {
                        val reminderExData = ReminderExData.Builder()
                            .type(SettingType.REMINDER_CUSTOM)
                            .enable(true)
                            .date(ReminderExData.Date(reminder.year, reminder.month, reminder.day))
                            .timeList(list)
                            .cycle(AlarmsList.getIntFromBoolean(subList))
                            .customType(reminder.label)
                            .shockRingType(SettingType.SHOCK_TYPE_ALARM)
                            .build()
                        BluetoothSDK.setReminderEx(
                            resultCallBackNew,
                            SettingType.REMINDER_EX_ACTION_NEW,
                            reminderExData
                        )
                    }
                }
            }
        } catch (exp: Exception) {
            exp.printStackTrace()
        }

    }


    override fun setWorldClock(data: WorldClocksPushData) {
        LOGS.d("setWorldClock $data")

        var addedWorldClock = false



        BluetoothSDK.set8002CallBack(object : ResultCallBack {
            override fun onSuccess(i: Int, objects: Array<Any>) {

                if (bitwiseHelperUtils.bytesArrayResult(objects)
                        .contains(visionCommands.WORLD_CLOCK_SUCCESS)
                ) {

                    set8002CallbackNull()
                    if (!addedWorldClock) {
                        watchDataStore.saveWorldClockData(null)
                        addedWorldClock = true
                        addWorldClock(data)
                    }

                }


            }

            override fun onFail(i: Int) {
                set8002CallbackNull()
                AppLogs.sendAppLogs("Error in : setWorldClock 8002 recv fail")
            }
        })

        visionCommands.sendCommand(visionCommands.DELETE_ALL_WORLD_CLOCK)
    }

    private fun addWorldClock(data: WorldClocksPushData) {

        //negative: west 0x00
        //positive: east 0x01

        data.worldClocks.forEachIndexed { index, wClock ->

            val offsetNew1 = (wClock.timeZone * 15)
            var hours = truncate(offsetNew1 / 60f).toInt()
            var minutes = offsetNew1 % 60;
            if (minutes < 0) {
                minutes *= -1
            }

            var timeZoneOffset = "0x01"
            if (hours < 0) {
                hours = -(hours)
                timeZoneOffset = "0x00"
            }

            LOGS.d("World clock time zone ::: $hours $minutes")

            val id = index + 1
            val startCmd = "0x6F,"
            val CMD = "0x40,"
            val cmdType = "0x71,"
            val end = "0x8F"
            val addOperation = "0x00,"
            val ind = bitwiseHelperUtils.getHexValue(id)
            val worldClockId = "${ind.first}, ${ind.second},"
            val addTime = "0x00,"
            val timeLength = "0x03,"
            val timeData = "$timeZoneOffset, ${
                bitwiseHelperUtils.getHexValue(
                    hours
                ).first
            }, ${
                bitwiseHelperUtils.getHexValue(
                    minutes
                ).first
            },"
            val placeName =
                bitwiseUtils.asciiToHex(wClock.content.replace(",", " ").replace(".", " "))
            val placeNameCmd = "0x01,"
            val placeLength = bitwiseHelperUtils.getHexValue(
                bitwiseHelperUtils.getHexLength(placeName)
            ).first


            val placeWithCmd =
                "$addOperation $worldClockId $addTime $timeLength $timeData $worldClockId $placeNameCmd $placeLength $placeName"

            val totalLength = bitwiseHelperUtils.getHexValue(
                bitwiseHelperUtils.getHexLength(placeWithCmd)
            )
            LOGS.d("World clock place cmd ::: $placeWithCmd")

            val cmd =
                "$startCmd $CMD $cmdType ${totalLength.first}, ${totalLength.second}, $placeWithCmd, $end"

            LOGS.d("World clock $cmd")
            BluetoothSDK.set8002CallBack(object : ResultCallBack {
                override fun onSuccess(i: Int, objects: Array<Any>) {
                    set8002CallbackNull()
                    AppLogs.sendAppLogs("Set world clock 8002 success ")

                }

                override fun onFail(i: Int) {
                    set8002CallbackNull()
                    LOGS.e("Error in : setWorldClock 8002 recv fail")
                }
            })
            visionCommands.sendCommand(cmd)
        }

        watchDataStore.saveWorldClockData(data)

    }

    override fun updateFirmware(fileUri: String) {

    }

    override fun setStressData(sedentaryData: SedentaryData) {
        var cmd = visionCommands.STRESS_ENABLE_CMD
        if (!sedentaryData.status) {
            cmd = visionCommands.STRESS_DISABLE_CMD
        }



        BluetoothSDK.set8002CallBack(object : ResultCallBack {
            override fun onSuccess(i: Int, objects: Array<Any>) {
                if (bitwiseHelperUtils.bytesArrayResult(objects)
                        .contains(visionCommands.STRESS_RESPONSE)
                ) {
                    // clear callback
                    set8002CallbackNull()
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.StressDataUpdated(
                            true
                        )
                    )
                }
                AppLogs.sendAppLogs("Set stress 8002 success ")


            }

            override fun onFail(i: Int) {
                set8002CallbackNull()
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.StressDataUpdated(
                        false
                    )
                )
                AppLogs.sendAppLogs("Set stress 8002 failed ")

            }
        })
        visionCommands.sendCommand(cmd)
    }

    override fun updateLanguage(language: Language) {
        BluetoothSDK.setLanguage(
            resultCallBack, when (language.language) {
                DeviceLanguage.CHINESE.type -> SettingType.LANGUAGE_ZH
                else -> SettingType.LANGUAGE_EN
            }
        )
    }

    override fun attachCallbacks() {
        BluetoothSDK.setPhoneCallBack(resultCallBack)
        BluetoothSDK.setSMSReplyCallBack(resultCallBack)
        BluetoothSDK.setWorkoutGPSCallBack(resultCallBack)
        BluetoothSDK.setRequestSyncWeatherCallBack(resultCallBack)

    }

    override fun findDevice(findDevice: SwitchSetting) {
        when (colorFitDevice?.deviceType) {
            DeviceType.COLORFIT_VISION.deviceType -> {
                BluetoothSDK.set8002CallBack(object : ResultCallBack {
                    override fun onSuccess(i: Int, objects: Array<Any>) {
                        set8002CallbackNull()
                        AppLogs.sendAppLogs("Find device 8002 success")


                    }

                    override fun onFail(i: Int) {
                        set8002CallbackNull()
                        AppLogs.sendAppLogs("Find device 8002 fail")

                    }
                })
                visionCommands.sendCommand(
                    visionCommands.FIND_DEVICE_CMD
                )

            }
            else -> {
                when (findDevice.status) {
                    true -> BluetoothSDK.startFindDevice(resultCallBack)
                    else -> BluetoothSDK.endFindDevice(resultCallBack)
                }

            }
        }


    }


    override fun setWatchFace(watchFace: WatchFace) {
        LOGS.d("noise_watch_faces=== ${watchFace.faceType}")
        when (watchFace.imageType) {
            "in_built" -> BluetoothSDK.setDefaultWatchFace(resultCallBack, watchFace.faceType!!)
            "cloud" -> updateCloudImage(watchFace)

        }
    }


    override fun setWatchFaceCustomHybrid(watchFace: WatchFacesCustomHybrid) {

        var watchFaceImageSize = 320
        var thumbnailImageSize = 256

        colorFitDevice?.deviceType?.let {
            if (DeviceType.COLORFIT_VISION.deviceType == it) {
                watchFaceImageSize = 368
                thumbnailImageSize = 180

            }

        }

        val customizeWatchFaceExData = CustomizeWatchFaceExData()
        customizeWatchFaceExData.componentList = ArrayList<CustomizeWatchFaceExData.Component>()
        if (watchFace.backgroundColor != null) {
            customizeWatchFaceExData.componentList.add(
                CustomizeWatchFaceExData.Component(SettingType.CUSTOMIZE_WATCH_FACE_TYPE_BACKGROUND_COLOR.toInt())
                    .setColor(Color.parseColor(watchFace.backgroundColor))
            )
        }

        watchFace.watchFacesSizeCoor?.forEach { item ->
            customizeWatchFaceExData.componentList.add(
                CustomizeWatchFaceExData.Component(
                    when (item.typeTxt) {
                        1 -> SettingType.CUSTOMIZE_WATCH_FACE_TYPE_STEP.toInt()
                        2 -> SettingType.CUSTOMIZE_WATCH_FACE_TYPE_CALORIES.toInt()
                        3 -> SettingType.CUSTOMIZE_WATCH_FACE_TYPE_DISTANCE.toInt()
                        4 -> SettingType.CUSTOMIZE_WATCH_FACE_TYPE_DATE.toInt()
                        5 -> SettingType.CUSTOMIZE_WATCH_FACE_TYPE_DIAL.toInt()
                        6 -> SettingType.CUSTOMIZE_WATCH_FACE_TYPE_WEATHER.toInt()
                        7 -> SettingType.CUSTOMIZE_WATCH_FACE_TYPE_POINTER.toInt()
                        8 -> SettingType.CUSTOMIZE_WATCH_FACE_TYPE_BATTERY.toInt()
                        9 -> SettingType.CUSTOMIZE_WATCH_FACE_TYPE_ACTIVE_TIME.toInt()
                        10 -> SettingType.CUSTOMIZE_WATCH_FACE_TYPE_TIME_ZONE.toInt()
                        12 -> SettingType.CUSTOMIZE_WATCH_FACE_TYPE_ACTIVE_GRAPH.toInt()
                        13 -> SettingType.CUSTOMIZE_WATCH_FACE_TYPE_TIME.toInt()
                        14 -> SettingType.CUSTOMIZE_WATCH_FACE_TYPE_REMIND.toInt()
                        else -> item.typeTxt
                    }
                )
                    .setCoordinate(Point(item.coordinateX, item.coordinateY))
                    .setSize(Point(item.pointX, item.pointY))
                    .setColor(Color.parseColor(item.color))
            )

        }

//        if (customizeWatchFaceExData.componentList != null && customizeWatchFaceExData.componentList.size > 0) {
//            //BluetoothSDK.setCustomizeWatchFaceEx(customizeWatchFaceCallBack, customizeWatchFaceExData)
//            BluetoothSDK.setCustomizeWatchFaceEx(customizeWatchFaceCallBack, customizeWatchFaceExData)
//        }
        if (watchFace.localImagePath != null) {
            val imagePath = Uri.parse(watchFace.localImagePath).path?.split("//")?.last()
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                    WatchUpdateStatus(status = UpdateStatus.STARTED)
                )
            )
            var maxProgress = 0
            BluetoothSDK.setCustomizeWatchFaceEx(
                object : ResultCallBack {
                    override fun onSuccess(p0: Int, p1: Array<out Any>?) {
                        when (p0) {

                            ResultCallBack.TYPE_UPLOAD_IMAGE_PROGRESS -> {
                                if (!p1.isNullOrEmpty() && p1[0] is Int) {
                                    var progress = p1[0] as Int
                                    if (progress > 0) {
                                        if (maxProgress > 100) {
                                            progress =
                                                ((progress.toDouble() / maxProgress) * 100).toInt()
                                        }
                                        LOGS.d("imagePath", "imagePath=====" + progress)
                                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                WatchUpdateStatus(
                                                    status = UpdateStatus.PROGRESS,
                                                    percentagePercentage = progress
                                                )
                                            )
                                        )
                                    }
                                }
                            }
                            ResultCallBack.TYPE_UPLOAD_IMAGE_MAX -> {
                                if (!p1.isNullOrEmpty() && p1[0] is Int) {
                                    val progress = p1[0] as Int
                                    if (progress > 0) {
                                        maxProgress = progress
                                        LOGS.d("imagePath", "imagePath=====max::" + progress)
                                        //baseUpdateDeviceDataCallbacks?.onCustomizeWatchFaceProgress(WatchFaceStatus(status = "progress", percentagePercentage = progress))
                                    }
                                }
                            }
                            ResultCallBack.TYPE_UPLOAD_IMAGE_RESULT -> {
                                if (!p1.isNullOrEmpty() && p1[0] is Boolean) {
                                    val result = p1[0] as Boolean
                                    if (result) {
                                        LOGS.d("imagePath", "imagePath=====" + "completed")
                                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                WatchUpdateStatus(status = UpdateStatus.COMPLETED)
                                            )
                                        )
                                    } else {
                                        LOGS.d("imagePath", "imagePath=====" + "error")
                                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                WatchUpdateStatus(status = UpdateStatus.ERROR)
                                            )
                                        )
                                    }
                                }

                            }
                            ResultCallBack.TYPE_SET_CUSTOMIZE_WATCH_FACE_EX -> {
                                LOGS.d("imagePath", "imagePath=====" + "completed==2")
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                        WatchUpdateStatus(status = UpdateStatus.COMPLETED)
                                    )
                                )
                            }
                        }
                    }

                    override fun onFail(p0: Int) {
                        LOGS.d("imagePath", "imagePath=====" + "error=====2" + p0)
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                WatchUpdateStatus(status = UpdateStatus.ERROR)
                            )
                        )
                    }

                },
                -1,
                imagePath,
                watchFaceImageSize,
                thumbnailImageSize,
                true,
                customizeWatchFaceExData
            )

        } else {
            if (customizeWatchFaceExData.componentList != null && customizeWatchFaceExData.componentList.size > 0) {
                //BluetoothSDK.setCustomizeWatchFaceEx(customizeWatchFaceCallBack, customizeWatchFaceExData)
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                        WatchUpdateStatus(status = UpdateStatus.STARTED)
                    )
                )
                try {
                    BluetoothSDK.setCustomizeWatchFaceEx(
                        customizeWatchFaceCallBack,
                        customizeWatchFaceExData
                    )
                } catch (exp: Exception) {
                    exp.printStackTrace()
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                            WatchUpdateStatus(status = UpdateStatus.ERROR)
                        )
                    )
                }

            }
        }
    }

    private val customizeWatchFaceCallBack: ResultCallBack = object : ResultCallBack {

        override fun onSuccess(p0: Int, p1: Array<out Any>?) {
            AppLogs.sendAppLogs("noise_watch_faces_custom====callback===onSuccess1:result==" + p0)
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                    WatchUpdateStatus(status = UpdateStatus.COMPLETED)
                )
            )
        }

        override fun onFail(p0: Int) {
            AppLogs.sendAppLogs("noise_watch_faces_custom====callback===failed : result== $p0")

        }

    }

    override fun setWatchPassword(watchPassword: WatchPassword) {

        var cmd = visionCommands.CLEAR_PASSWORD_CMD
        if (watchPassword.status) {
            val passCmd =
                "${visionCommands.SET_PASSWORD_CMD}${bitwiseUtils.asciiToHex(watchPassword.password)}, 0x8F"

            cmd = passCmd


        }

        BluetoothSDK.set8002CallBack(object : ResultCallBack {
            override fun onSuccess(i: Int, objects: Array<Any>) {
                if (bitwiseHelperUtils.bytesArrayResult(objects) == visionCommands.SET_PASSWORD_SUCCESS_RESPONSE) {
                    // clear callback
                    set8002CallbackNull()
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.WatchPasswordUpdated(
                            true
                        )
                    )

                } else if (bitwiseHelperUtils.bytesArrayResult(objects) == visionCommands.CLEAR_PASSWORD_SUCCESS_RESPONSE) {
                    // clear callback
                    set8002CallbackNull()
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.WatchPasswordUpdated(
                            true
                        )
                    )
                }
            }

            override fun onFail(i: Int) {
                set8002CallbackNull()
                LOGS.e("Error in setWatchPassword :: 8002 recv fail")
            }
        })
        visionCommands.sendCommand(cmd)
    }


    private fun updateCloudImage(watchFace: WatchFace) {
        LOGS.d("Watch faces $watchFace")
        colorFitDevice?.deviceType?.let {
            if (DeviceType.COLORFIT_VISION.deviceType == it) {
                val imagePath = watchFace.localFilePath.split("//").last()

                var maxProgress = 100
                onlineWatchFacesVision.otaOnlineWatchface(
                    watchDataStore.getUniqueIdForWatchFaces(),
                    imagePath,
                    object : ResultCallBack {
                        override fun onSuccess(p0: Int, p1: Array<out Any>?) {
                            if (p0 == 190808084) {
                                if (!p1.isNullOrEmpty() && p1[0] is Boolean) {
                                    val result = p1[0] as Boolean
                                    if (result) {
                                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                WatchUpdateStatus(status = UpdateStatus.COMPLETED)
                                            )
                                        )

                                    } else {
                                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                WatchUpdateStatus(status = UpdateStatus.ERROR)
                                            )
                                        )

                                    }
                                }


                            } else if (p0 == 190808083) {
                                if (!p1.isNullOrEmpty() && p1[0] is Int) {
                                    maxProgress = p1[0] as Int

                                }
                            } else if (p0 == 190808082) {
                                if (!p1.isNullOrEmpty() && p1[0] is Int) {
                                    val progress = p1[0] as Int
                                    val currentPercentage = (progress * 100 / maxProgress)

                                    if (currentPercentage > 0) {
                                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                WatchUpdateStatus(
                                                    status = UpdateStatus.PROGRESS,
                                                    percentagePercentage = currentPercentage
                                                )
                                            )
                                        )


                                    }
                                }
                            }

                        }

                        override fun onFail(p0: Int) {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                    WatchUpdateStatus(status = UpdateStatus.ERROR)
                                )
                            )

                        }

                    }
                )
                watchDataStore.setUniqueIdForWatchFaces()
            } else {
                try {
                    val deviceName = colorFitDevice?.bluetoothName
                    val otaName =
                        "W007GA" + deviceName?.subSequence(deviceName.length - 5, deviceName.length)
                    val imgSize = 240
                    val thumbnailSize = 180
                    val imagePath = watchFace.localImagePath.split("//").last()

                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                            WatchUpdateStatus(status = UpdateStatus.STARTED)
                        )
                    )

                    BluetoothSDK.setCustomizeWatchFace(object : ResultCallBack {
                        override fun onSuccess(p0: Int, p1: Array<out Any>?) {
                            when (p0) {

                                ResultCallBack.TYPE_UPLOAD_IMAGE_PROGRESS -> {
                                    if (!p1.isNullOrEmpty() && p1[0] is Int) {
                                        val progress = p1[0] as Int
                                        if (progress > 0) {
                                            LOGS.d(
                                                "imagePath",
                                                "imagePath=====" + "progress"
                                            )
                                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                    WatchUpdateStatus(
                                                        status = UpdateStatus.PROGRESS,
                                                        percentagePercentage = progress
                                                    )
                                                )
                                            )
                                        }
                                    }
                                }
                                ResultCallBack.TYPE_UPLOAD_IMAGE_MAX -> {
                                }
                                ResultCallBack.TYPE_UPLOAD_IMAGE_RESULT -> {
                                    if (!p1.isNullOrEmpty() && p1[0] is Boolean) {
                                        val result = p1[0] as Boolean
                                        if (result) {
                                            LOGS.d(
                                                "imagePath",
                                                "imagePath=====" + "completed"
                                            )
                                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                    WatchUpdateStatus(status = UpdateStatus.COMPLETED)
                                                )
                                            )
                                        } else {
                                            LOGS.d(
                                                "imagePath",
                                                "imagePath=====" + "error"
                                            )
                                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                    WatchUpdateStatus(status = UpdateStatus.ERROR)
                                                )
                                            )
                                        }
                                    }

                                }
                                ResultCallBack.TYPE_SET_CUSTOMIZE_WATCH_FACE -> {
                                    LOGS.d(
                                        "imagePath",
                                        "imagePath=====" + "completed==2"
                                    )
                                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                        UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                            WatchUpdateStatus(status = UpdateStatus.COMPLETED)
                                        )
                                    )
                                }
                            }
                        }

                        override fun onFail(p0: Int) {
                            LOGS.d("imagePath", "imagePath=====" + "error=====2" + p0)
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                    WatchUpdateStatus(status = UpdateStatus.ERROR)
                                )
                            )
                        }

                    }, otaName, -1, imagePath, imgSize, thumbnailSize)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }


    }


    override fun visionUpdateFirmware(visionOtaFiles: List<VisionOtaFiles>) {
        if (colorFitDevice == null) {
            return
        }


        var firmwarePath: String? = null
        var hrPath: String? = null
        var touchPath: String? = null
        val imagePath = ArrayList<String>()
        visionOtaFiles.forEach {
            when (it.fileType) {
                VisionOtaFileTypes.Touch -> {
                    touchPath = it.file.path
                }
                VisionOtaFileTypes.Heart -> {
                    hrPath = it.file.path
                }
                VisionOtaFileTypes.Image -> {
                    imagePath.add(it.file.path)
                }
                VisionOtaFileTypes.Firmware -> {
                    firmwarePath = it.file.path
                }
            }
        }
        BluetoothSDK.startUpdate(
            updateCallBack,
            colorFitDevice?.address,
            touchPath,
            imagePath.toTypedArray(),
            hrPath,
            firmwarePath
        )
    }

    private var updateCallBack: ResultCallBack = object : ResultCallBack {
        var maxProgress = 0
        override fun onSuccess(p0: Int, p1: Array<out Any>?) {

            when (p0) {
                ResultCallBack.TYPE_OTA_UPDATE_MAX -> {

                    if (!p1.isNullOrEmpty() && p1.get(0) is Int) {
                        maxProgress = p1[0] as Int

                    }
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                            WatchUpdateStatus(
                                status = UpdateStatus.STARTED
                            )
                        )
                    )

                }
                ResultCallBack.TYPE_OTA_UPDATE_PROGRESS -> {
                    p1?.get(0)?.let {
                        if (it is Int) {


                            val progress = (it * 100 / maxProgress)

                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                    WatchUpdateStatus(
                                        status = UpdateStatus.PROGRESS,
                                        percentagePercentage = progress
                                    )
                                )
                            )
                        }
                    }
                }
                ResultCallBack.TYPE_OTA_UPDATE_RESULT -> {
                    if (p1?.get(0) is Boolean) {
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                WatchUpdateStatus(
                                    status = UpdateStatus.COMPLETED
                                )
                            )
                        )
                    } else {
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                WatchUpdateStatus(
                                    status = UpdateStatus.ERROR
                                )
                            )
                        )
                    }
                }
            }
        }

        override fun onFail(p0: Int) {
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                    WatchUpdateStatus(
                        status = UpdateStatus.ERROR
                    )
                )
            )
        }
    }

    override fun startCameraMode(status: Boolean) {

        when (status) {
            true -> {
                jumpToTakePhoto(resultCallBack)
            }
            false -> {
                bluetoothsdkExp.jumpOutTakePhoto(object : BluetoothSDK_Exp.BoolCallback() {
                    override fun onSuccess() {
                        set8002CallbackNull()
                    }

                    override fun onFail(code: Int) {
                        set8002CallbackNull()
                    }

                })
            }

        }
    }


    override fun updateCustomReply(customReplyData: CustomReplyData) {

        colorFitDevice?.let { deviceType ->
            if (deviceType.deviceType == DeviceType.COLORFIT_VISION.deviceType) {
                val bluetoothsdkExp = BluetoothSDK_Exp()
                customReplyData.customReplies.forEach { reply ->
                    //  LOGS.d("updateCustomReply ::--- ", Gson().toJson(reply))
                    bluetoothsdkExp.editCustomizeReply(
                        reply.index,
                        reply.content,
                        object : BluetoothSDK_Exp.BoolCallback() {
                            override fun onSuccess() {
                                set8002CallbackNull()
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.CustomizeReplyUpdated(
                                        true
                                    )
                                )
                                AppLogs.sendAppLogs("Update custom reply success")
                            }

                            override fun onFail(code: Int) {
                                set8002CallbackNull()
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.CustomizeReplyUpdated(
                                        false
                                    )
                                )
                                AppLogs.sendAppLogs("Update custom reply fail")
                            }
                        })

                }

            } else {
                customReplyData.customReplies.forEach {
                    BluetoothSDK.setCustomizeReply(
                        resultCallBack, SettingType.CUSTOMIZE_REPLY_CHANGE,
                        it.index, it.crc, it.content
                    )
                }
            }
        }

    }


    override fun sendAppNotification(appNotification: AppNotification) {
//        if (appNotification.appType == ApplicationType.OTHER.type) return
        BluetoothSDK.sendMessagePushEx(
            resultCallBack, appNotification.name ?: appNotification.number, appNotification.message,
            Calendar.getInstance().time, when (appNotification.appType) {
                ApplicationType.SMS.type -> SettingType.MESSAGE_SMS
                ApplicationType.EMAIL.type -> SettingType.MESSAGE_EMAIL
                ApplicationType.WHATS_APP.type -> SettingType.MESSAGE_WHATSAPP
                ApplicationType.LINKED_IN.type -> SettingType.MESSAGE_LINKEDIN
                ApplicationType.INSTAGRAM.type -> SettingType.MESSAGE_INSTAGRAM
                ApplicationType.FB_MESSENGER.type -> SettingType.MESSAGE_MESSENGER
                ApplicationType.SKYPE.type -> SettingType.MESSAGE_SKYPE
                ApplicationType.TWITTER.type -> SettingType.MESSAGE_TWITTER
                ApplicationType.FACEBOOK.type -> SettingType.MESSAGE_FACEBOOK
                ApplicationType.VIBER.type -> SettingType.MESSAGE_VIBER
                ApplicationType.GMAIL.type -> SettingType.MESSAGE_GMAIL
                ApplicationType.OUTLOOK.type -> SettingType.MESSAGE_OUTLOOK
                ApplicationType.SNAPCHAT.type -> SettingType.MESSAGE_SNAPCHAT
                ApplicationType.TELEGRAM.type -> SettingType.MESSAGE_TELEGRAM
                ApplicationType.SLACK.type -> SettingType.MESSAGE_SLACK
                ApplicationType.YOUTUBE.type -> SettingType.MESSAGE_YOUTUBE
                ApplicationType.PINTEREST.type -> SettingType.MESSAGE_PINTREST
                ApplicationType.WE_CHAT.type -> SettingType.MESSAGE_WECHAT
                ApplicationType.HANGOUTS.type -> SettingType.MESSAGE_HANGOUTS
                ApplicationType.MISSEDCALL.type -> SettingType.MESSAGE_MISS_CALL
                ApplicationType.SPORT_EVENT.type -> SettingType.MESSAGE_SOCIAL
                else -> -1
            }, 1, SettingType.SHOCK_MODE_TWO_SOUND, true
        )
    }

    override fun setWristLiftGesture(wristLiftGesture: WristLiftGesture) {}

    override fun setWeatherData(weatherDataList: List<WeatherData>, unit: String) {

        if (weatherDataList.isNotEmpty()) {
            val weatherData = weatherDataList[0]
            when (colorFitDevice?.deviceType) {
                DeviceType.NOISEFIT_HYBRID.deviceType -> {
                    val list = ArrayList<cn.appscomm.bluetoothsdk.model.WeatherData>()
                    if (weatherDataList.size > 3) {
                        for (index in 0 until 4) {
                            val weatherData = weatherDataList[index]
                            val weather = cn.appscomm.bluetoothsdk.model.WeatherData(
                                when (weatherData.unit) {
                                    "celsius" -> SettingType.UNIT_CELSIUS
                                    "metric" -> SettingType.UNIT_CELSIUS
                                    else -> SettingType.UNIT_FAHRENHEIT
                                },
                                weatherData.temp.toInt(),
                                weatherData.tempMin.toInt(),
                                weatherData.tempMax.toInt(),
                                when (weatherData.weatherType?.toLowerCase(DateFormats.defaultLocale)) {
                                    "clouds" -> SettingType.WEATHER_CLOUDY
                                    "clear" -> SettingType.WEATHER_SUNNY
                                    "fog" -> SettingType.WEAHTER_FOGGY
                                    "thunderstorm" -> SettingType.WEAHTER_THUNDERSHOWERS
                                    "drizzle" -> SettingType.WEATHER_DRIZZLE
                                    "rain" -> SettingType.WEATHER_RAINY
                                    "snow" -> SettingType.WEATHER_SNOW
                                    "atmosphere" -> SettingType.WEATHER_CLOUDY
                                    else -> SettingType.WEATHER_SUNNY
                                }
                            )
                            list.add(weather)
                        }
                    } else {
                        val weather = cn.appscomm.bluetoothsdk.model.WeatherData(
                            when (weatherData.unit) {
                                "celsius" -> SettingType.UNIT_CELSIUS
                                "metric" -> SettingType.UNIT_CELSIUS
                                else -> SettingType.UNIT_FAHRENHEIT
                            },
                            weatherData.temp.toInt(),
                            weatherData.tempMin.toInt(),
                            weatherData.tempMax.toInt(),
                            when (weatherData.weatherType?.toLowerCase(DateFormats.defaultLocale)) {
                                "clouds" -> SettingType.WEATHER_CLOUDY
                                "clear" -> SettingType.WEATHER_SUNNY
                                "fog" -> SettingType.WEAHTER_FOGGY
                                "thunderstorm" -> SettingType.WEAHTER_THUNDERSHOWERS
                                "drizzle" -> SettingType.WEATHER_DRIZZLE
                                "rain" -> SettingType.WEATHER_RAINY
                                "snow" -> SettingType.WEATHER_SNOW
                                "atmosphere" -> SettingType.WEATHER_CLOUDY
                                else -> SettingType.WEATHER_SUNNY
                            }
                        )

                        list.add(weather)
                    }

                    BluetoothSDK.sendWeather(resultCallBack, list, weatherData.city)
                }
                DeviceType.COLORFIT_NAV.deviceType -> {
                    val weather = WeatherDataEx(
                        when (weatherData.unit) {
                            "celsius" -> SettingType.UNIT_CELSIUS
                            "metric" -> SettingType.UNIT_CELSIUS
                            else -> SettingType.UNIT_FAHRENHEIT
                        },
                        weatherData.temp.toInt(),
                        weatherData.tempMin.toInt(),
                        weatherData.tempMax.toInt(),
                        when (weatherData.weatherType?.toLowerCase(DateFormats.defaultLocale)) {
                            "clouds" -> SettingType.WEAHTER_CLOUDY
                            "clear" -> SettingType.WEATHER_SUNNY
                            "fog" -> SettingType.WEAHTER_FOGGY
                            "thunderstorm" -> SettingType.WEAHTER_THUNDERSHOWERS
                            "drizzle" -> SettingType.WEATHER_DRIZZLE
                            "rain" -> SettingType.WEATHER_RAINY
                            "snow" -> SettingType.WEATHER_SNOW
                            "atmosphere" -> SettingType.WEATHER_CLOUDY
                            else -> SettingType.WEATHER_SUNNY
                        }
                    )
                    weather.place = weatherData.city
                    weather.humidity = weatherData.humidity.toInt()
                    weather.windSpeed = weatherData.windSpeed.toInt()
                    BluetoothSDK.setWeatherUnit(resultCallBack, weather.unit)
                    BluetoothSDK.sendWeatherEx(resultCallBack, weather)

                }
                DeviceType.COLORFIT_VISION.deviceType -> {
                    val weatherIcon = dataConverter.getWeatherCode(weatherData.weatherType)
                    val weatherDataEx = WeatherDataEx(
                        when (weatherData.unit) {
                            "celsius" -> SettingType.UNIT_CELSIUS
                            "metric" -> SettingType.UNIT_CELSIUS
                            else -> SettingType.UNIT_FAHRENHEIT
                        },
                        weatherData.temp.toInt(),
                        weatherData.tempMin.toInt(),
                        weatherData.tempMax.toInt(),
                        weatherIcon
                    )

                    val weatherDataExDailyList = ArrayList<WeatherDataEx.DailyInfo>()

                    weatherDataList.drop(1).forEachIndexed { index, wData ->
                        val dailyObject = WeatherDataEx.DailyInfo()
                        dailyObject.day = index + 1
                        dailyObject.min = wData.tempMin.toInt()
                        dailyObject.max = wData.tempMax.toInt()
                        dailyObject.weatherCode =
                            dataConverter.getWeatherCode(wData.weatherType)
                        weatherDataExDailyList.add(dailyObject)

                    }

                    weatherDataEx.dailyInfoList = weatherDataExDailyList
                    weatherDataEx.place = weatherData.city
                    weatherDataEx.max = weatherData.tempMax.toInt()
                    weatherDataEx.min = weatherData.tempMin.toInt()
                    weatherDataEx.humidity = weatherData.humidity.toInt()
                    weatherDataEx.weatherCode = weatherIcon
                    weatherDataEx.windSpeed = weatherData.windSpeed.toInt()
                    BluetoothSDK.setWeatherUnit(resultCallBack, weatherDataEx.unit)
                    BluetoothSDK.sendWeatherEx(resultCallBack, weatherDataEx)

                }
            }
        }
    }


    override fun setTemperatureUnit(unit: String) {

        var unitId = SettingType.UNIT_CELSIUS
        if (unit.equals(Units.IMPERIAL.name, true)) {
            unitId = SettingType.UNIT_FAHRENHEIT
        }
        LOGS.d("setTemperatureUnit ${unit}")
        BluetoothSDK.setWeatherUnit(resultCallBack, unitId)
        AppLogs.sendAppLogs("Temperature unit set")

    }


    override fun setIncomingCallInfo(incomingCall: IncomingCall) {
        when (incomingCall.status) {
            true -> {
                BluetoothSDK.sendMessagePushEx(
                    resultCallBack,
                    incomingCall.name ?: incomingCall.number,
                    incomingCall.number,
                    Calendar.getInstance().time,
                    SettingType.MESSAGE_INCOME_CALL,
                    1,
                    SettingType.SHOCK_MODE_SIGNAL_LONG_SHOCK,
                    true
                )
                AppLogs.sendAppLogs("Incoming call")
            }
            else -> {BluetoothSDK.sendMessagePushEx(
                resultCallBack,
                "Call End",
                incomingCall.number,
                Calendar.getInstance().time,
                SettingType.MESSAGE_OFFHOOK,
                1,
                SettingType.SHOCK_MODE_NO_SHOCK,
                true
            )
                AppLogs.sendAppLogs("Incoming call end")}

        }
        mobileNumber = incomingCall.number
    }

    override fun updateDND(doNotDisturb: DoNotDisturb) {
        BluetoothSDK.setDoNotDisturb(
            resultCallBack,
            doNotDisturb.status,
            doNotDisturb.startHour,
            doNotDisturb.startMinute,
            doNotDisturb.endHour,
            doNotDisturb.endMinute
        )
    }

    override fun setHeartRateInterval(heartRateInterval: HeartRateInterval) {
        BluetoothSDK.setAutoHeartRateFrequency(resultCallBack, heartRateInterval.interval)
    }

    override fun setSedentaryData(sedentaryData: SedentaryData) {
        BluetoothSDK.setInactivityAlert(
            resultCallBack,
            sedentaryData.status,
            sedentaryData.getIntFromBoolean(sedentaryData.repeatDays),
            sedentaryData.interval,
            sedentaryData.startHour,
            sedentaryData.startMinute,
            sedentaryData.endHour,
            sedentaryData.endMinute,
            300
        )
    }

    override fun setAutoSleep(autoSleep: AutoSleep) {
        when (autoSleep.status) {
            true -> {
                BluetoothSDK.setSwitchSetting(
                    resultCallBack,
                    SettingType.SWITCH_SLEEP,
                    autoSleep.status
                )
                BluetoothSDK.setAutoSleep(
                    resultCallBack,
                    autoSleep.startHour,
                    autoSleep.startMinute,
                    autoSleep.endHour,
                    autoSleep.endMinute,
                    /* autoSleep.repeat*/
                    autoSleep.getRepeatValue(autoSleep.weeks)
                )
            }
            else -> {
                BluetoothSDK.setSwitchSetting(
                    resultCallBack,
                    SettingType.SWITCH_SLEEP,
                    autoSleep.status
                )
                BluetoothSDK.setAutoSleep(resultCallBack, 0, 0, 0, 0, 0)
            }
        }
    }

    override fun setSwitchSetting(switchSetting: SwitchSetting) {
        BluetoothSDK.setSwitchSetting(
            resultCallBack, when (switchSetting.settingType) {
                "anti_lost" -> SettingType.SWITCH_ANTI_LOST
                else -> SettingType.SWITCH_ANTI_LOST
            }, true
        )
    }

    private var resultCallBack: ResultCallBack = object : ResultCallBack {
        override fun onSuccess(p0: Int, p1: Array<out Any>?) {
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.UserInfoUpdated(
                    true
                )
            )
            when (p0) {
                ResultCallBack.TYPE_SET_SCREEN_FORMAT_AND_STYLE -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.WatchFaceUpdated(
                            true
                        )
                    )
                }
                ResultCallBack.TYPE_SET_DEFAULT_WATCH_FACE -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.WatchFaceUpdated(
                            true
                        )
                    )
                }
                ResultCallBack.TYPE_SET_DEVICE_TIME -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DeviceTimeSynced(
                            true
                        )
                    )
                }
                ResultCallBack.TYPE_SET_USER_INFO -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.UserInfoUpdated(
                            true
                        )
                    )
                }
//                ResultCallBack.TYPE_SET_STEP_GOAL -> {
//                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
//                        UpdateDeviceDataCallback.UserGoalsUpdated(
//                            true
//                        )
//                    )
//                }
                ResultCallBack.TYPE_SET_UNIT -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DeviceUnitsUpdated(
                            true
                        )
                    )
                }
                ResultCallBack.TYPE_SET_DO_NOT_DISTURB -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DoNotDisturbUpdated(
                            true
                        )
                    )
                }
                ResultCallBack.TYPE_SET_LANGUAGE -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.LanguageUpdated(
                            true
                        )
                    )
                }
                ResultCallBack.TYPE_SET_INACTIVITY_ALERT -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.SedentaryDataUpdated(
                            true
                        )
                    )
                }
                ResultCallBack.TYPE_SET_TIME_FORMAT -> {

                }
                ResultCallBack.TYPE_CHANGE_REMINDER,
                ResultCallBack.TYPE_DELETE_A_REMINDER,
                ResultCallBack.TYPE_CHANGE_REMINDER_EX,
                ResultCallBack.TYPE_DELETE_A_REMINDER_EX,
                ResultCallBack.TYPE_NEW_REMINDER -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.AlarmUpdated(
                            true
                        )
                    )
                }
                ResultCallBack.TYPE_SET_AUTO_HEART_RATE_FREQUENCY -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.HeartRateMeasureIntervalSet(
                            true
                        )
                    )
                }
                ResultCallBack.TYPE_SET_AUTO_SLEEP -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.AutoSleepUpdated(
                            true
                        )
                    )
                }
                ResultCallBack.TYPE_SET_BRIGHT_SCREEN_TIME -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.ScreenAwakeIntervalUpdated(
                            true
                        )
                    )
                }
                ResultCallBack.TYPE_SET_CUSTOMIZE_REPLY -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.CustomizeReplyUpdated(
                            true
                        )
                    )
                }
                ResultCallBack.TYPE_SET_SWITCH_SETTING -> onSwitchSettingUpdated(true, p1)
                ResultCallBack.TYPE_DEVICE_SMS_REPLY_DEFAULT -> onDeviceSMSReplyDefault(p1)
                ResultCallBack.TYPE_DEVICE_SMS_REPLY_CUSTOMIZE -> onDeviceSMSReply(p1)

                ResultCallBack.TYPE_DEVICE_ACCEPT_CALL -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.UpdateCallStatus(
                            true
                        )
                    )
                }
                ResultCallBack.TYPE_DEVICE_REJECT_CALL -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.UpdateCallStatus(
                            false
                        )
                    )
                }
                ResultCallBack.TYPE_DEVICE_REQUEST_LOCATION -> {
                    LOGS.d("snmxcb smnbs", "GPS:::: true")
                    /* var fusedLocationProviderClient: FusedLocationProviderClient =
                         FusedLocationProviderClient(NoisefitApplication.context!!.applicationContext)
                     //if (ActivityCompat.checkSelfPermission(NoisefitApplication.context!!.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NoisefitApplication.context!!.applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                     fusedLocationProviderClient.lastLocation
                         .addOnSuccessListener { location: Location? ->
                             if (location != null) {
                                 var longitude = location?.longitude
                                 var long1: Long
                                 var lat1: Long
                                 var alt1: Long
                                 if (longitude != null)
                                     long1 = longitude.toLong()
                                 else
                                     long1 = 0L
                                 var latitude = location?.latitude
                                 if (latitude != null)
                                     lat1 = latitude.toLong()
                                 else
                                     lat1 = 0L
                                 var altitude = location?.altitude
                                 if (altitude != null)
                                     alt1 = altitude.toLong()
                                 else
                                     alt1 = 0L
                                 LOGS.d("snmxcb smnbs", "GPS:::: true" + long1)
                                 BluetoothSDK.sendWorkoutGPSLocation(
                                     System.currentTimeMillis(),
                                     long1,
                                     lat1,
                                     alt1,
                                     0L
                                 )
                             }
                          }*/

                    return
                    //}
//                    else{
//                        LOGS.d("snmxcb smnbs","GPS:::: false")
//                    }

                }
                ResultCallBack.TYPE_DEVICE_SYNC_WEATHER -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.WeatherUpdateRequest(
                        )
                    )
                }
                ResultCallBack.TYPE_NEW_REMINDER_EX -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.AddReminder(
                            true
                        )
                    )
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.AlarmUpdated(
                            true
                        )
                    )
                }
                ResultCallBack.TYPE_RESTORE_FACTORY -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.FactoryReset(
                            true
                        )
                    )
                }

            }
        }

        override fun onFail(p0: Int) {
            when (p0) {
                ResultCallBack.TYPE_SET_SCREEN_FORMAT_AND_STYLE -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.WatchFaceLayoutUpdated(
                            false
                        )
                    )
                }
                ResultCallBack.TYPE_SET_DEFAULT_WATCH_FACE -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.WatchFaceUpdated(
                            false
                        )
                    )
                }
                ResultCallBack.TYPE_SET_DEVICE_TIME -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DeviceTimeSynced(
                            false
                        )
                    )
                }
                ResultCallBack.TYPE_SET_USER_INFO -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.UserInfoUpdated(
                            false
                        )
                    )
                }
//                ResultCallBack.TYPE_SET_STEP_GOAL -> {
//                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
//                        UpdateDeviceDataCallback.UserGoalsUpdated(
//                            false
//                        )
//                    )
//                }
                ResultCallBack.TYPE_SET_UNIT -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DeviceUnitsUpdated(
                            false
                        )
                    )
                }
                ResultCallBack.TYPE_SET_DO_NOT_DISTURB -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DoNotDisturbUpdated(
                            false
                        )
                    )
                }
                ResultCallBack.TYPE_SET_LANGUAGE -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.LanguageUpdated(
                            false
                        )
                    )
                }
                ResultCallBack.TYPE_SET_INACTIVITY_ALERT -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.SedentaryDataUpdated(
                            false
                        )
                    )
                }
                ResultCallBack.TYPE_SET_TIME_FORMAT -> {

                }
                ResultCallBack.TYPE_SET_AUTO_SLEEP -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.AutoSleepUpdated(
                            false
                        )
                    )
                }
                ResultCallBack.TYPE_SET_CUSTOMIZE_REPLY -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.CustomizeReplyUpdated(
                            false
                        )
                    )
                }
                ResultCallBack.TYPE_CHANGE_REMINDER, ResultCallBack.TYPE_DELETE_A_REMINDER,
                ResultCallBack.TYPE_NEW_REMINDER_EX, ResultCallBack.TYPE_CHANGE_REMINDER_EX, ResultCallBack.TYPE_DELETE_A_REMINDER_EX,
                ResultCallBack.TYPE_NEW_REMINDER -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.AlarmUpdated(
                            false
                        )
                    )
                }
                ResultCallBack.TYPE_SET_AUTO_HEART_RATE_FREQUENCY -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.HeartRateMeasureIntervalSet(
                            false
                        )
                    )
                }
                ResultCallBack.TYPE_SET_BRIGHT_SCREEN_TIME -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.ScreenAwakeIntervalUpdated(
                            false
                        )
                    )
                }
                ResultCallBack.TYPE_RESTORE_FACTORY -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.FactoryReset(
                            false
                        )
                    )
                }
                ResultCallBack.TYPE_DEVICE_START_TAKE_PHOTO -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.ClickCameraImage()
                    )
                }
                ResultCallBack.TYPE_DEVICE_END_TAKE_PHOTO -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.SwitchCameraView(
                            false
                        )
                    )
                }
            }
        }
    }

    private var resultCallBackNew: ResultCallBack = object : ResultCallBack {
        override fun onSuccess(p0: Int, p1: Array<out Any>?) {

            when (p0) {
                ResultCallBack.TYPE_NEW_REMINDER_EX -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.AddReminder(
                            true
                        )
                    )
                }
                ResultCallBack.TYPE_CHANGE_REMINDER_EX -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.AddReminder(
                            true
                        )
                    )
                }
                ResultCallBack.TYPE_DELETE_A_REMINDER_EX -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DeleteReminder(
                            true
                        )
                    )
                }
            }
        }

        override fun onFail(p0: Int) {
            when (p0) {
                ResultCallBack.TYPE_NEW_REMINDER_EX -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.AddReminder(
                            false
                        )
                    )
                }
                ResultCallBack.TYPE_CHANGE_REMINDER_EX -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.AddReminder(
                            false
                        )
                    )
                }
                ResultCallBack.TYPE_DELETE_A_REMINDER_EX -> {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DeleteReminder(
                            false
                        )
                    )
                }
            }
        }
    }

    private fun onDeviceSMSReplyDefault(objects: Array<out Any>?) {
        val emoji: String = objects?.get(0).toString() // 表情
        val crc = objects?.get(3) // crc(对应setCustomizeReply里面设置的crc值)
        val nameOrNumber: String = objects?.get(4).toString() // 号码或姓名

//        LOGS.d(
//            "onDeviceSMSReplyDefault",
//            "nameOrNumber : $nameOrNumber language : ${objects?.get(1)} ${Gson().toJson(objects)}"
//        )
        if (crc == 65535) {                                                                 // emoji 或 设备默认的预设回复
            var str: String?
            if (TextUtils.isEmpty(emoji)) {
                LOGS.d("onDeviceSMSReplyDefault", "SMS Type(index) index : " + objects.get(2)
                    .also {
                        str = it.toString()
                    })
            } else {                                                                        // 自定义的预设回复
                LOGS.d("onDeviceSMSReplyDefault", "SMS Type(emoji) emoji : " + emoji.also {
                    str = it
                })
            }
            // Toast.makeText(this@SendData, str, Toast.LENGTH_SHORT).show()
            BluetoothSDK.sendReplyResponse(0, true)
        } else {
            LOGS.d("onDeviceSMSReplyDefault", "SMS Type(custom) crc : $crc")
//            sendSms(crc = "HEY THERE ")
            BluetoothSDK.sendReplyResponse(0, false)
        }
    }


    private fun onDeviceSMSReply(p1: Array<out Any>?) {
        // LOGS.d("onDeviceSMSReplyDefault", "inside onDeviceSMSReply" + Gson().toJson(p1))
        p1?.let {

            val number = it[0] as Int
            val crc = it[1] as String
            try {
                mobileNumber?.let { number ->
                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(number, null, crc, null, null)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun setScreenAwakeInterval(interval: Int) {
        BluetoothSDK.setBrightScreenTime(resultCallBack, interval)
    }

    private fun onSwitchSettingUpdated(status: Boolean, p1: Array<out Any>?) {
        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.SwitchSettingUpdated(
                SwitchSetting(status)
            )
        )
    }


    override fun setDrinkWaterReminder(sedentaryData: SedentaryData) {
        LOGS.d("setDrinkWaterReminder ::  $sedentaryData")
        //(startHour=6, startMinute=0, endHour=21, endMinute=0, frequency=7200, duration=5, startWash=false)
        //setDrinkWaterReminder | {"end_hour":18,"end_minute":0,"interval":30,"repeat_count":127,"start_hour":8,"start_minute":0,"status":true}
        val cmd = bitwiseHelperUtils.getDrinkWaterCmd(sedentaryData)

        BluetoothSDK.set8002CallBack(object : ResultCallBack {
            override fun onSuccess(i: Int, objects: Array<Any>) {

                if (bitwiseHelperUtils.bytesArrayResult(objects)
                        .contains(visionCommands.DRINK_HAND_RESPONSE)
                ) {
                    // clear callback
                    set8002CallbackNull()
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DrinkWaterUpdated(
                            true
                        )
                    )
                }

            }

            override fun onFail(i: Int) {
                set8002CallbackNull()
                LOGS.e("Error :: setDrinkWaterReminder 8002 recv fail")
            }
        })

        visionCommands.sendCommand(cmd)
    }

    override fun setHandWashing(handWashing: HandWashing) {

        LOGS.d("Handwashing $handWashing")
        when (colorFitDevice?.deviceType) {
            DeviceType.COLORFIT_VISION.deviceType -> {
                val cmd = bitwiseHelperUtils.getHandwashCmd(handWashing)

                BluetoothSDK.set8002CallBack(object : ResultCallBack {
                    override fun onSuccess(i: Int, objects: Array<Any>) {

                        if (bitwiseHelperUtils.bytesArrayResult(objects)
                                .contains(visionCommands.DRINK_HAND_RESPONSE)
                        ) {
                            // clear callback
                            set8002CallbackNull()
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.HandWashingUpdated(
                                    true
                                )
                            )
                        }

                    }

                    override fun onFail(i: Int) {
                        set8002CallbackNull()
                        LOGS.e("Error:: setHandWashing 8002 recv fail")
                    }
                })
                visionCommands.sendCommand(cmd)
            }
            else -> {
                BluetoothSDK.setHandWashing(
                    object : ResultCallBack {
                        override fun onSuccess(p0: Int, p1: Array<out Any>?) {
                            LOGS.d("noise_fit_event:noisefit_hybrid", "setHandWashing | onSuccess")
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.HandWashingUpdated(
                                    true
                                )
                            )
                        }

                        override fun onFail(p0: Int) {
                            LOGS.d("noise_fit_event:noisefit_hybrid", "setHandWashing | onError")
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.HandWashingUpdated(
                                    false
                                )
                            )
                        }
                    },
                    handWashing.startWash,
                    handWashing.startHour,
                    handWashing.startMinute,
                    handWashing.endHour,
                    handWashing.endMinute,
                    handWashing.frequency * 60,
                    handWashing.duration
                )
            }
        }
    }

    override fun updateAPGSData(data1: Uri, data2: Uri) {
        val file1: File = File(data1.path)
        //File("/storage/emulated/0/Android/data/${CommonAppConfigHandler.getInstance().applicationId}/files/AGPS/mgaoffline.ubx")
        val file2: File = File(data2.path)
        //File("/storage/emulated/0/Android/data/${CommonAppConfigHandler.getInstance().applicationId}/files/AGPS/mgaoffline1.ubx")
        LOGS.d(
            "noise_fit_event:noisefit_hybrid",
            "updateAPGSData | " + file1.exists() + "::" + file2.exists()
        )
        if (file1.exists() && file2.exists()) {


            APGSOta.updateAGPS(file2, file1, 0, object : ResultCallBack {
                override fun onSuccess(p0: Int, p1: Array<out Any>?) {
                    LOGS.d(
                        "noise_fit_event:noisefit_hybrid",
                        "updateAPGSData | onSuccess" + p0 + ":" + p1
                    )
                    when (p0) {
                        ResultCallBack.TYPE_UPDATE_AGPS_PROGRESS -> {
                            if (!p1.isNullOrEmpty() && p1[0] is Int) {
                                val result = p1[0] as Int
                                LOGS.d(
                                    "noise_fit_event:noisefit_hybrid",
                                    "updateAPGSData | progress $result"
                                )
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.AGPSUpdateProgress(
                                        status = UpdateStatus.PROGRESS,
                                        progress = result
                                    )
                                )
                            }
                        }
                        ResultCallBack.TYPE_UPDATE_AGPS_MAX -> {
                            if (!p1.isNullOrEmpty() && p1[0] is Int) {
                                val result = p1[0] as Int
                                LOGS.d(
                                    "noise_fit_event:noisefit_hybrid",
                                    "updateAPGSData | max $result"
                                )
                            }
                        }
                        ResultCallBack.TYPE_UPDATE_AGPS_RESULT -> {
                            LOGS.d(
                                "noise_fit_event:noisefit_hybrid",
                                "updateAPGSData | success"
                            )

                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.AGPSUpdateProgress(
                                    status = UpdateStatus.COMPLETED
                                )
                            )
                        }
                    }
                }

                override fun onFail(p0: Int) {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.AGPSUpdateProgress(
                            status = UpdateStatus.ERROR
                        )
                    )
                }
            })


            /* BluetoothSDK.updateAPGSData(object : ResultCallBack {
                 override fun onSuccess(p0: Int, p1: Array<out Any>?) {
                     LOGS.d("noise_fit_event:noisefit_hybrid", "updateAPGSData | onSuccess")
                     when (p0) {
                         ResultCallBack.TYPE_UPDATE_AGPS_PROGRESS -> {
                             if (!p1.isNullOrEmpty() && p1?.get(0) is Int) {
                                 val result = p1[0] as Int
                                 LOGS.d(
                                     "noise_fit_event:noisefit_hybrid",
                                     "updateAPGSData | progress $result"
                                 )
                             }
                         }
                         ResultCallBack.TYPE_UPDATE_AGPS_MAX -> {
                             if (!p1.isNullOrEmpty() && p1?.get(0) is Int) {
                                 val result = p1[0] as Int
                                 LOGS.d(
                                     "noise_fit_event:noisefit_hybrid",
                                     "updateAPGSData | max $result"
                                 )
                             }
                         }
                         ResultCallBack.TYPE_UPDATE_AGPS_RESULT -> {
                             LOGS.d("noise_fit_event:noisefit_hybrid", "updateAPGSData | success")
                             testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                 UpdateDeviceDataCallback.AGPSUpdated(
                                     true
                                 )
                             )
                         }
                     }
                 }

                 override fun onFail(p0: Int) {
                     LOGS.d("noise_fit_event:noisefit_hybrid", "updateAPGSData | onError")
                     when (p0) {
                         ResultCallBack.TYPE_UPDATE_AGPS_FILE_NO_EXIST -> {
                             LOGS.d("noise_fit_event:noisefit_hybrid", "updateAPGSData | onError1")
                             testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                 UpdateDeviceDataCallback.AGPSUpdated(
                                     false
                                 )
                             )
                         }

                         ResultCallBack.TYPE_UPDATE_AGPS_RESULT -> {
                             LOGS.d("noise_fit_event:noisefit_hybrid", "updateAPGSData | onError2")
                             testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                 UpdateDeviceDataCallback.AGPSUpdated(
                                     false
                                 )
                             )
                         }
                     }
                 }
             }, file1, file2, 0)*/
        }

    }


    override fun setHeartRateAlert(heartRateAlert: HeartRateAlert) {
        BluetoothSDK.setHeartRateAlarmThreshold(
            object : ResultCallBack {
                override fun onSuccess(p0: Int, p1: Array<out Any>?) {
                    LOGS.d("noise_fit_event:noisefit_hybrid", "setHeartRateAlert | onSuccess")
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.HeartRateAlertUpdated(
                            true
                        )
                    )
                }

                override fun onFail(p0: Int) {
                    LOGS.d("noise_fit_event:noisefit_hybrid", "setHeartRateAlert | onError")
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.HeartRateAlertUpdated(
                            false
                        )
                    )
                }
            }, when (heartRateAlert.status) {
                true -> 1
                else -> 0
            }, heartRateAlert.min_hr, heartRateAlert.max_hr
        )
    }


    override fun setFactoryReset() {
        LOGS.d("noise_fit_event:colorfit_hybrid", "factory reset::")
        BluetoothSDK.restoreFactory(resultCallBack)
    }

    override fun setRestartDevice() {
        LOGS.d("noise_fit_event:colorfit_hybrid", "restart")
        BluetoothSDK.restartSDK()
    }
}