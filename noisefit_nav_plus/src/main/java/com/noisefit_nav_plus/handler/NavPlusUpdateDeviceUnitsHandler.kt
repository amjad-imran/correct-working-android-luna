package com.noisefit_nav_plus.handler

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.constants.WatchFaceEventsConstants
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.enums.ApplicationType
import com.noisefit_commans.interfaces.device_data.IUpdateDeviceDataCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallbacks
import com.noisefit_commans.models.AlarmAction
import com.noisefit_commans.models.AlarmsList
import com.noisefit_commans.models.AppNotification
import com.noisefit_commans.models.AutoSleep
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.Contact
import com.noisefit_commans.models.CustomReplyData
import com.noisefit_commans.models.DeviceLanguage
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.DeviceUnits
import com.noisefit_commans.models.DiyCustomWatchFace
import com.noisefit_commans.models.DoNotDisturb
import com.noisefit_commans.models.Gender
import com.noisefit_commans.models.HandWashing
import com.noisefit_commans.models.HeartRateAlert
import com.noisefit_commans.models.HeartRateInterval
import com.noisefit_commans.models.IncomingCall
import com.noisefit_commans.models.Language
import com.noisefit_commans.models.MenstrualData
import com.noisefit_commans.models.ReminderList
import com.noisefit_commans.models.SedentaryData
import com.noisefit_commans.models.StockInfoList
import com.noisefit_commans.models.SwitchSetting
import com.noisefit_commans.models.TimeFormat
import com.noisefit_commans.models.TimeFormats
import com.noisefit_commans.models.UnitSystem
import com.noisefit_commans.models.Units
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.UserGoals
import com.noisefit_commans.models.UserInfo
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.models.WatchUpdateStatus
import com.noisefit_commans.models.WeatherData
import com.noisefit_commans.models.WorldClocksPushData
import com.noisefit_commans.models.WristLiftGesture
import com.noisefit_commans.utils.AgpsEvents
import com.noisefit_commans.utils.AppConversionUtils
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LogEvents
import com.noisefit_commans.utils.OtaEvents
import com.noisefit_commans.utils.WatchFaceEvents
import com.noisefit_nav_plus.base.NavPlusApplicationHandler
import com.noisefit_nav_plus.handler.dataConversion.DataConverter
import com.zjw.zhbraceletsdk.bean.AlarmInfo
import com.zjw.zhbraceletsdk.bean.ContactsBean
import com.zjw.zhbraceletsdk.bean.DrinkInfo
import com.zjw.zhbraceletsdk.bean.EventReminder
import com.zjw.zhbraceletsdk.bean.HandWashingInfo
import com.zjw.zhbraceletsdk.bean.MedicalInfo
import com.zjw.zhbraceletsdk.bean.QuickReply
import com.zjw.zhbraceletsdk.bean.SitInfo
import com.zjw.zhbraceletsdk.bean.StockInfo
import com.zjw.zhbraceletsdk.bean.WeatherInfo2
import com.zjw.zhbraceletsdk.bean.WorldClock
import com.zjw.zhbraceletsdk.linstener.AGpsPrepareStatusListener
import com.zjw.zhbraceletsdk.linstener.BodyTemperatureUnitListener
import com.zjw.zhbraceletsdk.linstener.DeviceProtoOtaPrepareStatusListener
import com.zjw.zhbraceletsdk.linstener.DeviceProtoWatchPrepareStatusListener
import com.zjw.zhbraceletsdk.linstener.QuickEyeSwitchListener
import com.zjw.zhbraceletsdk.linstener.ReplySetStatusListener
import com.zjw.zhbraceletsdk.linstener.SetEventReminderListener
import com.zjw.zhbraceletsdk.linstener.SetQuickReplyListener
import com.zjw.zhbraceletsdk.linstener.SetWorldClockListener
import com.zjw.zhbraceletsdk.linstener.SimplePerformerListener
import com.zjw.zhbraceletsdk.linstener.UploadBigDataListener
import com.zjw.zhbraceletsdk.service.BleConstant
import com.zjw.zhbraceletsdk.service.ZhBraceletService
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToInt


class NavPlusUpdateDeviceUnitsHandler
@Inject
constructor(
    var dataConverter: DataConverter,
    var context: Context,
    var gson: Gson,
    var navPlusApplicationHandler: NavPlusApplicationHandler,
    var watchDataStore: WatchDataStore
) : UpdateDeviceDataActions() {

    private var updateDeviceDataCallbacks: UpdateDeviceDataCallbacks? = null


    private var mobileNumber: String? = null
    private var mBleService: ZhBraceletService? = null
    private var colorFitDevice: ColorFitDevice? = null

    private var testUpdateDeviceDataCallback: IUpdateDeviceDataCallback? = null

    override fun init() {
        super.init()
        mBleService = navPlusApplicationHandler.getZhBraceletService()
    }

    override fun <T> callbackListenerNew(callback: T) {
        testUpdateDeviceDataCallback = callback as IUpdateDeviceDataCallback
    }

    override fun setDevice(device: ColorFitDevice) {
        colorFitDevice = device
    }

    override fun <T> callbackListener(callback: T) {
        updateDeviceDataCallbacks = callback as UpdateDeviceDataCallbacks
    }


    override fun attachCallbacks() {
        mBleService?.let { zhBraceletService ->

            //turn off for pulse and beat
            if (!watchDataStore.getDefaultValue()) {
                colorFitDevice?.deviceType?.let { deviceType ->
                    if (deviceType == DeviceType.COLORFIT_PULSE.deviceType || deviceType == DeviceType.COLORFIT_BEAT.deviceType) {
                        watchDataStore.setDefaultValue(true)
                    } else {
                        mBleService?.enableAppSupportAuxiliary { p0 ->
                            if (p0) {
                                watchDataStore.setDefaultValue(true)
                            }
                        }
                    }
                }
            }
            initSingleQuickReplyListener()
            initDeviceSendMuteListener()
            initQuickEyeListener()
            zhBraceletService.removeSimplePerformerListenerLis(mPerformerListener)
            zhBraceletService.addSimplePerformerListenerLis(mPerformerListener)

            zhBraceletService.setReplySetStatusListener(object : ReplySetStatusListener {
                override fun onSynchronisedTime(state: Boolean) {

                }

                override fun onAlarmSetting(state: Boolean) {

                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.AlarmUpdated(
                            state
                        )
                    )
                }

                override fun onSedentaryReminder(state: Boolean) {

                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.SedentaryDataUpdated(
                            state
                        )
                    )

                }

                override fun onUnitSettings(state: Boolean) {

                }

                override fun onDrinkWaterReminder(state: Boolean) {

                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DrinkWaterUpdated(
                            state
                        )
                    )

                }

                override fun onLanguageSettings(state: Boolean) {

                }

                override fun onHeartRateWitch(state: Boolean) {

                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.HeartRateAlertUpdated(
                            state
                        )
                    )

                }

                override fun onHandWashingReminder(state: Boolean) {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.HandWashingUpdated(
                            state
                        )
                    )

                }
            })
        }
    }

    override fun setBodyTemperatureUnit(unit: Units) {
        mBleService?.setBodyTemperatureUnitListener(object : BodyTemperatureUnitListener {
            override fun onResult(isFahrenheit: Boolean) {

            }

            override fun onSuccess() {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.BodyTempDataUpdated(
                        true
                    )
                )
            }

            override fun onFail() {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.BodyTempDataUpdated(
                        false
                    )
                )
            }
        })
        var unitId = false
        if (unit == Units.IMPERIAL) {
            unitId = true
        }
        mBleService?.setBodyTemperatureUnit(unitId)
//        testUpdateDeviceDataCallback?.onUpdateDataReceived(
//            UpdateDeviceDataCallback.BodyTempDataUpdated(
//                true
//            )
//        )


    }

    override fun syncStockInfoList(stockInfoList: StockInfoList) {
        val stockArray = ArrayList<StockInfo>()
        val timeStamp = System.currentTimeMillis().toInt()
        stockInfoList.stockInfoList.forEachIndexed { index, item ->
            val bean = StockInfo()
            bean.symbol = item.symbol
            bean.market = item.market
            bean.name = item.name
            bean.latestPrice = item.latestPrice ?: 0.0f
            bean.preClose = item.previousClose ?: 0.0f
            bean.halted = item.halted
            bean.timestamp = timeStamp
            bean.delayMintue = item.delayMintue
            stockArray.add(bean)
        }
        mBleService?.syncStockInfoList(stockArray)
        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.SyncStockInfoList(
                true
            )
        )
    }

    override fun deleteStock(symbol: String) {
        mBleService?.deleteStock(symbol)


        colorFitDevice?.let {
            if (!it.deviceType.equals(DeviceType.COLORFIT_ULTRA_BUZZ.deviceType) || !it.deviceType.equals(
                    DeviceType.COLORFIT_VISION_BUZZ.deviceType
                )
            ) {

                Handler(Looper.getMainLooper()).postDelayed({
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DeleteStock(
                            true
                        )
                    )
                }, 1000)
            }
        }


    }


    override fun setUserInfo(userInfo: UserInfo, userGoals: UserGoals, userName: String?) {
        mBleService?.setUserInfo(
            com.zjw.zhbraceletsdk.bean.UserInfo(
                userInfo.height,
                userInfo.weight,
                AppConversionUtils.getAgeFromDOB(userInfo.dob),
                when (userInfo.gender.lowercase()) {
                    Gender.FEMALE.type.lowercase() -> {
                        true
                    }
                    else -> {
                        false
                    }
                }
            )
        )

        mBleService?.setUserTargetStep(userGoals.stepGoal)
        mBleService?.setGoal(0, userGoals.caloriesGoal)
        mBleService?.setGoal(1, userGoals.distanceGoal)
        mBleService?.setGoal(3, userGoals.sleepGoal)

        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.UserInfoUpdated(
                true
            )
        )
    }


    override fun setDeviceUnits(units: DeviceUnits) {


        mBleService?.setUnit(
            when (units.unitSystem?.lowercase()) {
                UnitSystem.IMPERIAL.type.lowercase() -> false
                else -> true
            }
        )

        mBleService?.setLanguagen(0)
        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.DeviceUnitsUpdated(
                true
            )
        )


    }


    override fun updateAlarm(alarm: AlarmsList, alarmAction: AlarmAction) {

        val listData = ArrayList<AlarmInfo>()
        mBleService?.setAlarmData(listData)

        alarm.alarms?.forEachIndexed { index, item ->
            val subList = ArrayList(item.repeatDays!!.subList(1, item.repeatDays!!.size))
            subList.reverse()
            subList.add(0, item.repeatDays!![0])
            val alarmInfo = AlarmInfo()
            alarmInfo.alarmId = index
            alarmInfo.alarmtHour = item.hour
            alarmInfo.alarmtMin = item.minute
            alarmInfo.alarmtData = booleanArrayToBinaryString(subList)
            alarmInfo.isRepeat = true
            listData.add(alarmInfo)
        }

        LOGS.d(alarm)
        mBleService?.setAlarmData(listData)
        //testUpdateDeviceDataCallback?.onUpdateDataReceived(UpdateDeviceDataCallback.AlarmUpdated(true))

        /**
         * Added for watches that does not return status update
         */
        colorFitDevice?.deviceType?.let {
            if (it.equals(DeviceType.COLORFIT_PULSE.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_BEAT.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_CALIBER.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_CALIBER_2.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_GRAND.deviceType, true) ||
                it.equals(DeviceType.XFIT2.deviceType, true)
            ) {
                Handler(Looper.getMainLooper())
                    .postDelayed({
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.AlarmUpdated(
                                true
                            )
                        )
                    }, 2500)
            }
        }


    }

    private fun booleanArrayToBinaryString(booleanList: List<Boolean>?): Int {
        var result = 0
        for (i in 7 downTo 0) {
            booleanList?.let { boolList ->
                if (boolList[i]) {
                    result += 2.0.pow((7 - i).toDouble()).roundToInt()
                }
            }

        }
        return result
    }


    override fun setContactList(contactList: List<Contact>) {
        val contactBeanList = ArrayList<ContactsBean>()
        contactList.forEach { contact ->
            val contactsBean = ContactsBean()
            contactsBean.name = contact.name
            contactsBean.number = contact.number[0]
            contactBeanList.add(contactsBean)
        }
        mBleService?.setContactsList(contactBeanList)
        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.ContactListUpdated(
                true
            )
        )
    }

    override fun deleteAlarm(alarm: AlarmsList) {

    }

    override fun deleteReminders(reminderList: ReminderList) {
        val listData = ArrayList<EventReminder>()
        reminderList.reminders?.forEach { item ->
            val eventReminder = EventReminder()
            eventReminder.content = item.label
            eventReminder.day = item.day
            eventReminder.month = item.month
            eventReminder.year = item.year
            eventReminder.hour = item.hour
            eventReminder.minute = item.minute
            listData.add(eventReminder)
        }

        mBleService?.setEventReminderListener(listData, object : SetEventReminderListener {
            override fun onSuccess() {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.DeleteReminder(
                        true
                    )
                )

            }

            override fun onFailure() {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.DeleteReminder(
                        false
                    )
                )

            }

        })

    }

    override fun updateMenstrualData(menstrualData: MenstrualData) {
        val menstrualParams = dataConverter.formatMenstrualData(menstrualData)
        LOGS.d(menstrualParams)
        mBleService?.setMenstrualCycle(menstrualParams)
        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.MenstrualDataUpdated(
                true
            )
        )

    }

    override fun addReminder(reminder: ReminderList.Reminder) {
        val listData = ArrayList<EventReminder>()

        val eventReminder = EventReminder()
        eventReminder.content = reminder.label
        eventReminder.day = reminder.day
        eventReminder.month = reminder.month
        eventReminder.year = reminder.year
        eventReminder.hour = reminder.hour
        eventReminder.minute = reminder.minute
        listData.add(eventReminder)

        mBleService?.setEventReminderListener(listData, object : SetEventReminderListener {
            override fun onSuccess() {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.AddReminder(
                        true
                    )
                )

            }

            override fun onFailure() {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.AddReminder(
                        false
                    )
                )
            }

        })

    }

    override fun updateLanguage(language: Language) {
        mBleService?.setLanguagen(
            when (language.language) {
                DeviceLanguage.CHINESE.type -> 1
                else -> 0
            }
        )
        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.LanguageUpdated(
                true
            )
        )


    }

    override fun findDevice(findDevice: SwitchSetting) {
        if (findDevice.status) {
            mBleService?.findDevice()
        }

    }

    override fun setDeviceDateTime(calender: Calendar, units: TimeFormat) {
        mBleService?.syncTime()
        setDeviceTime(units)
    }

    private fun setDeviceTime(units: TimeFormat) {
        if (mBleService != null) {
            mBleService?.setTimeFormat(
                when (units.timeFormat?.lowercase()) {
                    TimeFormats.HOURS_12.type.lowercase() -> false
                    else -> true
                }
            )
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.DeviceTimeSynced(
                    true
                )
            )
            val sdfZone = SimpleDateFormat("Z", DateFormats.defaultLocale)
            val timezone = sdfZone.format(Date())
            if (timezone.length < 4) {
                return
            }
            mBleService?.setTimeZone(timezone)


        }
    }

    /**
     * 0=Ready, 1=Busy, 2=Already up to date, 3=Insufficient memory, 4=Low battery, 5=Failed
     */
    override fun onDownloadedWatchFaceContents(watchFace: WatchFace) {
        try {
            val themeId = "170"
            val themeSize = 549884

            val file1 = File(Uri.parse(watchFace.localFilePath).path!!)
            mBleService!!.getDeviceProtoWatchStatusEvent(
                themeId,
                themeSize,
                object : DeviceProtoWatchPrepareStatusListener {
                    override fun onSuccess(status: Int) {
//                        updateDeviceDataCallbacks?.onCustomizeWatchFaceProgress(
//                            WatchFaceStatus(
//                                status = "started"
//                            )
//                        )
//                        LoggerHelper.printVerbose(
//                            "noise_fit_event:noisefit_hybrid",
//                            "WatchFace : Event Success"
//                        )
                        when (status) {
                            0 -> {
                                val fileByte: ByteArray = file1.readBytes()
                                mBleService!!.startUploadBigData(
                                    BleConstant.UPLOAD_BIG_DATA_WATCH,
                                    fileByte,
                                    object : UploadBigDataListener {
                                        override fun onSuccess() {
                                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                    WatchUpdateStatus(status = UpdateStatus.COMPLETED, wStatus = WatchFaceEventsConstants.Complete)
                                                )
                                            )
                                        }

                                        override fun onProgress(
                                            curPiece: Int,
                                            dataPackTotalPieceLength: Int
                                        ) {
                                            val progressPercent = try {
                                                curPiece * 100 / dataPackTotalPieceLength
                                            } catch (exp: Exception) {
                                                0
                                            }
                                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                    WatchUpdateStatus(
                                                        status = UpdateStatus.PROGRESS,
                                                        percentagePercentage = progressPercent
                                                    )
                                                )
                                            )

                                            LOGS.i("WatchFace : " + (curPiece * 100 / dataPackTotalPieceLength))
                                        }

                                        override fun onTimeout() {
                                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                    WatchUpdateStatus(status = UpdateStatus.ERROR,
                                                        wStatus = "set timeout")
                                                )
                                            )
                                            AppLogs.sendAppLogs(
                                                LogEvents.WatchFace,
                                                WatchFaceEvents.TransferTimeout
                                            )
                                            LOGS.i(
                                                "WatchFace : onTimeout"
                                            )
                                        }
                                    })
                            }
                            1 -> {
                                LOGS.i("WatchFace : failed $status")
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                        WatchUpdateStatus(status = UpdateStatus.ERROR,
                                            wStatus = WatchFaceEventsConstants.Busy)
                                    )
                                )
                                AppLogs.sendAppLogs(
                                    LogEvents.WatchFace,
                                    WatchFaceEvents.TransferFailed.apply {
                                        comment = "1"
                                    })

                            }
                            2, 5, 3 -> {
                                LOGS.i(
                                    "WatchFace : failed $status"
                                )
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                        WatchUpdateStatus(status = UpdateStatus.ERROR,
                                        wStatus = status.toString())
                                    )
                                )
                                AppLogs.sendAppLogs(
                                    LogEvents.WatchFace,
                                    WatchFaceEvents.TransferFailed.apply {
                                        comment = "2,5,3"
                                    })
                            }
                            4 -> {
                                LOGS.i(
                                    "WatchFace : failed $status"
                                )
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                        WatchUpdateStatus(status = UpdateStatus.BATTERY_LOW,
                                            wStatus = WatchFaceEventsConstants.Battery_low)
                                    )
                                )
                                AppLogs.sendAppLogs(
                                    LogEvents.WatchFace,
                                    WatchFaceEvents.TransferFailed.apply {
                                        comment = "4"
                                    })
                            }
                        }
                    }

                    override fun timeOut() {
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                WatchUpdateStatus(status = UpdateStatus.ERROR,
                                wStatus = "device proto timeout")
                            )
                        )
                        AppLogs.sendAppLogs(
                            LogEvents.WatchFace,
                            WatchFaceEvents.TransferTimeout
                        )
                        LOGS.i("WatchFace : time out")
                    }
                })
        } catch (e: Exception) {
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                    WatchUpdateStatus(status = UpdateStatus.ERROR,
                    wStatus = "exception")
                )
            )
            AppLogs.sendAppLogs(
                LogEvents.WatchFace,
                WatchFaceEvents.TransferFailed.apply {
                    comment = "${e.message}"
                }
            )
            LOGS.i("WatchFace : exception")
            e.printStackTrace()
        }

    }




    override fun setWatchFace(watchFace: WatchFace) {
        LOGS.d(watchFace)
        onDownloadedWatchFaceContents(watchFace)
    }


    override fun setDiyWatchFaceCustom(watchFace: DiyCustomWatchFace) {

        try {
            var r = 255
            var g = 255
            var b = 255
            watchFace.color?.let {
                r = Color.red(it)
                g = Color.green(it)
                b = Color.blue(it)
            }

            val binFile = File(Uri.parse(watchFace.binFile).path!!)
            NoisefitApplication.context?.applicationContext?.let {
                val sourceData: ByteArray = binFile.readBytes()
                val textBitmap: Bitmap? = watchFace.textLayer

                val bitmapTemp = watchFace.image


                if (watchFace.binFile == null || bitmapTemp == null) {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                            WatchUpdateStatus(
                                status = UpdateStatus.ERROR,
                                wStatus = "File missing!!"
                            )
                        )
                    )
                    return@let
                }

                val bgBitmap: Bitmap = bitmapTemp
                mBleService?.getMyCustomClockDialData(sourceData, r, g, b, bgBitmap, textBitmap)


            }
        } catch (e: Exception) {
            e.printStackTrace()

            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                    WatchUpdateStatus(status = UpdateStatus.ERROR, wStatus = "exception")
                )
            )
            AppLogs.sendAppLogs(
                LogEvents.WatchFace,
                WatchFaceEvents.TransferFailed.apply {
                    comment = "${e.message}"
                }
            )
        }

    }


    private val mPerformerListener: SimplePerformerListener = object : SimplePerformerListener() {
        override fun onResponseCustomDialEffectImg(bitmap: Bitmap?) {
            LOGS.d("NAVFace>>>>>>", "==============onResponseCustomDialEffectImg==============")
        }

        //返回自定义表盘数据  Return custom watch face data
        override fun onResponseCustomDialData(data: ByteArray) {
            LOGS.d("NAVFace>>>>>>", "==============onResponseCustomDialData==============")
            LOGS.d("NAVFace>>>>>>", "onResponseCustomDialData data = " + data.size)
            uploadWatch(data)
        }
    }

    fun uploadWatch(data: ByteArray) {
        if (mBleService != null) {
            val themeId = "170"
            val themeSize = data.size
            mBleService?.getDeviceProtoWatchStatusEvent(
                themeId,
                themeSize,
                object : DeviceProtoWatchPrepareStatusListener {
                    override fun onSuccess(status: Int) {
                        LOGS.d(
                            "NAVFace>>>>>>",
                            "getDeviceProtoWatchStatusEvent onSuccess " + status
                        )
                        when (status) {
                            0 -> {
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                        WatchUpdateStatus(status = UpdateStatus.STARTED)
                                    )
                                )
                                sendWatchData(data)
                            }
                            1 -> {
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                        WatchUpdateStatus(status = UpdateStatus.ERROR,
                                        wStatus = WatchFaceEventsConstants.Busy)
                                    )
                                )
                                AppLogs.sendAppLogs(
                                    LogEvents.WatchFace,
                                    WatchFaceEvents.TransferFailed.apply {
                                        comment = "Custom | 1"
                                    }
                                )
                            }
                            2, 5, 3 -> {
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                        WatchUpdateStatus(status = UpdateStatus.ERROR,
                                        wStatus = status.toString())
                                    )
                                )
                                AppLogs.sendAppLogs(
                                    LogEvents.WatchFace,
                                    WatchFaceEvents.TransferFailed.apply {
                                        comment = "Custom | 2,5,3"
                                    }
                                )
                            }
                            4 -> {
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                        WatchUpdateStatus(status = UpdateStatus.ERROR,
                                        wStatus = WatchFaceEventsConstants.Battery_low)
                                    )
                                )
                                AppLogs.sendAppLogs(
                                    LogEvents.WatchFace,
                                    WatchFaceEvents.TransferFailed.apply {
                                        comment = "Custom | 4"
                                    }
                                )
                            }
                        }
                    }

                    override fun timeOut() {
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                WatchUpdateStatus(status = UpdateStatus.ERROR,
                                wStatus = "upload watch timeout")
                            )
                        )
                        AppLogs.sendAppLogs(
                            LogEvents.WatchFace,
                            WatchFaceEvents.TransferTimeout.apply {
                                comment = "Custom"
                            }
                        )
                        LOGS.d("NAVFace>>>>>>", "getDeviceProtoWatchStatusEvent timeOut")
                    }
                })
        }
    }

    fun sendWatchData(data: ByteArray?) {
        mBleService?.startUploadBigData(
            BleConstant.UPLOAD_BIG_DATA_WATCH,
            data,
            object : UploadBigDataListener {
                override fun onSuccess() {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                            WatchUpdateStatus(status = UpdateStatus.COMPLETED,
                            wStatus = WatchFaceEventsConstants.Complete)
                        )
                    )
                    AppLogs.sendAppLogs("Custom Watchface Transfer Success")
                }

                override fun onProgress(curPiece: Int, dataPackTotalPieceLength: Int) {
                    val progressPercent = try {
                        curPiece * 100 / dataPackTotalPieceLength
                    } catch (exp: Exception) {
                        0
                    }
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.CustomizeWatchFaceProgress(

                            WatchUpdateStatus(
                                status = UpdateStatus.PROGRESS,
                                percentagePercentage = progressPercent
                            )
                        )
                    )

                }

                override fun onTimeout() {

                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                            WatchUpdateStatus(
                                status = UpdateStatus.ERROR,
                                wStatus = "send watch timeout"
                            )
                        )
                    )
                    AppLogs.sendAppLogs(
                        LogEvents.WatchFace,
                        WatchFaceEvents.TransferTimeout.apply {
                            comment = "Custom"
                        }
                    )
                }
            })
    }


    override fun updateFirmware(fileUri: String) {

        val file1: File = File(Uri.parse(fileUri).path)
        if (mBleService != null) {
            val isForce = true
            val version = "443"
            val md5 = "121412511"
            mBleService!!.getDeviceProtoOtaPrepareStatus(
                isForce,
                version,
                md5,
                object : DeviceProtoOtaPrepareStatusListener {
                    override fun onSuccess(status: Int) {
                        LOGS.d(
                            "noise_fit_event:noisefit_hybrid",
                            "updateAPGSData : Success"
                        )
                        when (status) {
                            0 -> {
                                val fileByte: ByteArray = file1.readBytes()
                                mBleService!!.startUploadBigData(
                                    BleConstant.UPLOAD_BIG_DATA_OTA,
                                    fileByte,
                                    object : UploadBigDataListener {
                                        override fun onSuccess() {
                                            LOGS.d(
                                                "firmware_upgrade : completed"
                                            )
                                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                                UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                                    WatchUpdateStatus(status = UpdateStatus.COMPLETED)
                                                )
                                            )
                                        }

                                        override fun onProgress(
                                            curPiece: Int,
                                            dataPackTotalPieceLength: Int
                                        ) {
                                            WatchInfoGlobals.isWatchDataUpdating = true
                                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                                UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                                    WatchUpdateStatus(
                                                        status = UpdateStatus.PROGRESS,
                                                        percentagePercentage = (curPiece * 100 / dataPackTotalPieceLength)
                                                    )
                                                )
                                            )
                                            LOGS.d(
                                                "firmware_upgrade : " + (curPiece * 100 / dataPackTotalPieceLength)
                                            )
                                        }

                                        override fun onTimeout() {
                                            WatchInfoGlobals.isWatchDataUpdating = false
                                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                                UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                                    WatchUpdateStatus(status = UpdateStatus.ERROR)
                                                )
                                            )
                                            AppLogs.sendAppLogs(
                                                LogEvents.Ota,
                                                OtaEvents.TransferTimeout
                                            )
                                            LOGS.d(
                                                "firmware_upgrade : error"
                                            )
                                        }
                                    })
                            }
                            1 -> {
                                WatchInfoGlobals.isWatchDataUpdating = false
                                LOGS.d(
                                    "firmware_upgrade : failed"
                                )
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                        WatchUpdateStatus(status = UpdateStatus.ERROR)
                                    )
                                )
                                AppLogs.sendAppLogs(
                                    LogEvents.Ota,
                                    OtaEvents.TransferFailed.apply {
                                        comment = "1"
                                    }
                                )
                            }
                            2, 5, 3 -> {
                                WatchInfoGlobals.isWatchDataUpdating = false
                                LOGS.d(
                                    "noise_fit_event:colorfit_pro_2",
                                    "firmware_upgrade : failed"
                                )
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                        WatchUpdateStatus(status = UpdateStatus.ERROR)
                                    )
                                )
                                AppLogs.sendAppLogs(
                                    LogEvents.Ota,
                                    OtaEvents.TransferFailed.apply {
                                        comment = "2,5,3"
                                    }
                                )
                            }
                            4 -> {
                                WatchInfoGlobals.isWatchDataUpdating = false
                                LOGS.d(
                                    "firmware_upgrade : failed"
                                )
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                        WatchUpdateStatus(status = UpdateStatus.ERROR)
                                    )
                                )
                                AppLogs.sendAppLogs(
                                    LogEvents.Ota,
                                    OtaEvents.TransferFailed.apply {
                                        comment = "4"
                                    }
                                )
                            }
                        }
                    }

                    override fun timeOut() {
                        WatchInfoGlobals.isWatchDataUpdating = false
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                WatchUpdateStatus(status = UpdateStatus.ERROR)
                            )
                        )
                        LOGS.d(
                            "updateAPGSData : time out"
                        )
                        AppLogs.sendAppLogs(
                            LogEvents.Ota,
                            OtaEvents.TransferTimeout
                        )
                    }
                })

        }
    }

    override fun startCameraMode(status: Boolean) {
        if (status) {
            mBleService?.openPhoto()
        } else {
            mBleService?.closePhoto()
        }
    }



    override fun sendAppNotification(appNotification: AppNotification) {


        if (mBleService != null) {
            //🎥

            LOGS.d(appNotification)
            val message = dataConverter.formatNotificationMessage(appNotification.message)

            colorFitDevice?.deviceType?.let { deviceType ->
                if (deviceType == DeviceType.COLORFIT_NAV_PLUS.deviceType ||
                    deviceType == DeviceType.COLORFIT_PULSE.deviceType ||
                    deviceType == DeviceType.COLORFIT_BEAT.deviceType
                ) {

                    if (appNotification.appType == ApplicationType.SMS.type) {
                        mBleService?.setMessagePush(
                            2,
                            appNotification.name ?: appNotification.number,
                            message
                        )
                        mobileNumber = try {
                            appNotification.number
                        } catch (e: Exception) {
                            null
                        }
                        return
                    }
                } else if (deviceType == DeviceType.COLORFIT_CALIBER.deviceType ||
                    deviceType == DeviceType.COLORFIT_GRAND.deviceType ||
                    deviceType == DeviceType.COLORFIT_CALIBER_2.deviceType ||
                    deviceType == DeviceType.XFIT2.deviceType
                ) {
                    if (appNotification.appType == ApplicationType.SMS.type) {
                        mBleService?.setMessagePush(
                            2,
                            appNotification.number,
                            (appNotification.name ?: appNotification.number) + ":" + message
                        )
                        mobileNumber = try {

                            appNotification.number
                        } catch (e: Exception) {
                            null
                        }
                        return
                    } else if (appNotification.appType == ApplicationType.MISSEDCALL.type) {
                        mBleService?.setMessagePush(
                            1,
                            appNotification.name ?: appNotification.number,
                            appNotification.name ?: (appNotification.number + ":" + message)
                        )
                        mobileNumber = try {

                            appNotification.number
                        } catch (e: Exception) {
                            null
                        }
                        return
                    }
                } else {
                    if (appNotification.appType == ApplicationType.SMS.type) {
                        mBleService?.setMessagePush(
                            2,
                            appNotification.name ?: appNotification.number,
                            message
                        )
                        mobileNumber = try {

                            appNotification.number
                        } catch (e: Exception) {
                            null
                        }
                        return
                    } else if (appNotification.appType == ApplicationType.MISSEDCALL.type) {
                        mBleService?.setMessagePush(
                            1,
                            appNotification.name ?: appNotification.number,
                            appNotification.name ?: (appNotification.number + ":" + message)
                        )
                        mobileNumber = try {

                            appNotification.number
                        } catch (e: Exception) {
                            null
                        }
                        return
                    }
                }
            }


            var notificationMsg = message
            if (appNotification.name != null && !appNotification.name.equals("")) {
                notificationMsg = appNotification.name + ": " + notificationMsg
            }
            colorFitDevice?.deviceType?.let { deviceType ->
                if (deviceType == DeviceType.COLORFIT_BRIO.deviceType ||
                    deviceType == DeviceType.COLORFIT_BRIO_PRO.deviceType ||
                    deviceType == DeviceType.XFIT.deviceType ||
                    deviceType == DeviceType.COLORFIT_CALIBER.deviceType ||
                    deviceType == DeviceType.COLORFIT_CALIBER_2.deviceType ||
                    deviceType == DeviceType.COLORFIT_GRAND.deviceType ||
                    deviceType == DeviceType.XFIT2.deviceType
                ) {
                    mBleService?.thirdPartyPush(
                        when (appNotification.appType) {
                            ApplicationType.MISSEDCALL.type -> 1
                            ApplicationType.WHATS_APP.type -> BleConstant.NOTIFY_TYPE_WHATSAPP
                            ApplicationType.LINKED_IN.type -> BleConstant.NOTIFY_TYPE_LINK
                            ApplicationType.INSTAGRAM.type -> BleConstant.NOTIFY_TYPE_INSTAGRAM
                            ApplicationType.FB_MESSENGER.type -> BleConstant.NOTIFY_TYPE_FACEBOOK_MESSAGE
                            ApplicationType.SKYPE.type -> BleConstant.NOTIFY_TYPE_SKYPE
                            ApplicationType.TWITTER.type -> BleConstant.NOTIFY_TYPE_TWITTER
                            ApplicationType.FACEBOOK.type -> BleConstant.NOTIFY_TYPE_FACEBOOK
                            ApplicationType.VIBER.type -> BleConstant.NOTIFY_TYPE_VIBER
                            ApplicationType.GMAIL.type -> BleConstant.NOTIFY_TYPE_GMAIL
                            ApplicationType.OUTLOOK.type -> BleConstant.NOTIFY_TYPE_OUTLOOK
                            ApplicationType.SNAPCHAT.type -> BleConstant.NOTIFY_TYPE_SNAPCHAT
                            ApplicationType.CALENDAR.type -> BleConstant.NOTIFY_TYPE_CALENDAR
                            ApplicationType.TELEGRAM.type -> BleConstant.NOTIFY_TYPE_TELEGRAM
                            ApplicationType.NOISEFIT.type -> BleConstant.NOTIFY_TYPE_NOISE
                            ApplicationType.PINTEREST.type -> BleConstant.NOTIFY_TYPE_PINTREST
                            ApplicationType.SPORT_EVENT.type -> BleConstant.NOTIFY_TYPE_NOISE
                            else -> BleConstant.NotifaceOther
                        }, appNotification.name ?: appNotification.number,
                        message
                    )
                } else if (deviceType == DeviceType.COLORFIT_ULTRA_2.deviceType ||
                    deviceType == DeviceType.COLORFIT_ULTRA_BUZZ.deviceType ||
                    deviceType == DeviceType.COLORFIT_VISION_BUZZ.deviceType
                ) {
                    mBleService?.thirdPartyPush(
                        when (appNotification.appType) {
                            ApplicationType.MISSEDCALL.type -> 1
                            ApplicationType.WHATS_APP.type -> BleConstant.NOTIFY_TYPE_WHATSAPP
                            ApplicationType.LINKED_IN.type -> BleConstant.NOTIFY_TYPE_LINK
                            ApplicationType.INSTAGRAM.type -> BleConstant.NOTIFY_TYPE_INSTAGRAM
                            ApplicationType.FB_MESSENGER.type -> BleConstant.NOTIFY_TYPE_FACEBOOK_MESSAGE
                            ApplicationType.SKYPE.type -> BleConstant.NOTIFY_TYPE_SKYPE
                            ApplicationType.TWITTER.type -> BleConstant.NOTIFY_TYPE_TWITTER
                            ApplicationType.FACEBOOK.type -> BleConstant.NOTIFY_TYPE_FACEBOOK
                            ApplicationType.VIBER.type -> BleConstant.NOTIFY_TYPE_VIBER
                            ApplicationType.GMAIL.type -> BleConstant.NOTIFY_TYPE_GMAIL
                            ApplicationType.OUTLOOK.type -> BleConstant.NOTIFY_TYPE_OUTLOOK
                            ApplicationType.SNAPCHAT.type -> BleConstant.NOTIFY_TYPE_SNAPCHAT
                            ApplicationType.NAUKRI.type -> BleConstant.NOTIFY_TYPE_NAUKRI
                            ApplicationType.INSHORTS.type -> BleConstant.NOTIFY_TYPE_INSHOT
                            ApplicationType.CALENDAR.type -> BleConstant.NOTIFY_TYPE_CALENDAR
                            ApplicationType.TELEGRAM.type -> BleConstant.NOTIFY_TYPE_TELEGRAM
                            ApplicationType.NOISEFIT.type -> BleConstant.NOTIFY_TYPE_NOISE
                            ApplicationType.AMAZON.type -> BleConstant.NOTIFY_TYPE_AMZAON
                            ApplicationType.FLIPKART.type -> BleConstant.NOTIFY_TYPE_FLIPKAR
                            ApplicationType.GOOGLE_NEWS.type -> BleConstant.NOTIFY_TYPE_GOOGLE_NEWS
                            ApplicationType.GOOGLE_PAY.type -> BleConstant.NOTIFY_TYPE_GOOGLE_PAY
                            ApplicationType.HANGOUTS.type -> BleConstant.NOTIFY_TYPE_HANGOUT
                            ApplicationType.OLA.type -> BleConstant.NOTIFY_TYPE_OLA
                            ApplicationType.PAYTM.type -> BleConstant.NOTIFY_TYPE_PAYTM
                            ApplicationType.PHONEPE.type -> BleConstant.NOTIFY_TYPE_PHONEPE
                            ApplicationType.PINTEREST.type -> BleConstant.NOTIFY_TYPE_PINTREST
                            ApplicationType.UBER.type -> BleConstant.NOTIFY_TYPE_UBER
                            ApplicationType.SPORT_EVENT.type -> BleConstant.NOTIFY_TYPE_NOISE
                            ApplicationType.WHATS_APP_BUSINESS.type -> BleConstant.NOTIFY_TYPE_WHATSAPP_BUSINESS
                            else -> BleConstant.NotifaceOther
                        }, appNotification.name ?: appNotification.number,
                        message
                    )
                } else {
                    mBleService?.setRemind(
                        notificationMsg, when (appNotification.appType) {
                            ApplicationType.SMS.type -> BleConstant.NotifaceMsgMsg
                            ApplicationType.EMAIL.type -> BleConstant.NotifaceGmail
                            ApplicationType.WHATS_APP.type -> BleConstant.NotifaceMsgWhatsapp
                            ApplicationType.LINKED_IN.type -> BleConstant.NotifaceMsgLink
                            ApplicationType.INSTAGRAM.type -> BleConstant.NotifaceInstagram
                            ApplicationType.FB_MESSENGER.type -> BleConstant.NotifaceMsgFacebookMessenger
                            ApplicationType.SKYPE.type -> BleConstant.NotifaceMsgSkype
                            ApplicationType.TWITTER.type -> BleConstant.NotifaceMsgTwitter
                            ApplicationType.FACEBOOK.type -> BleConstant.NotifaceMsgFacebook
                            ApplicationType.VIBER.type -> BleConstant.NotifaceMsgViber
                            ApplicationType.GMAIL.type -> BleConstant.NotifaceGmail
                            ApplicationType.OUTLOOK.type -> BleConstant.NotifaceOutLook
                            ApplicationType.SNAPCHAT.type -> BleConstant.NotifaceSnapchat
                            ApplicationType.WE_CHAT.type -> BleConstant.NotifaceMsgWx
                            ApplicationType.CALENDAR.type -> BleConstant.NotifaceMsgCalendar
                            ApplicationType.TELEGRAM.type -> BleConstant.NotifaceMsgTelegram
                            ApplicationType.NOISEFIT.type -> BleConstant.NotifaceMsgNoise
                            ApplicationType.SPORT_EVENT.type -> BleConstant.NotifaceMsgNoise
                            else -> BleConstant.NotifaceOther
                        }
                    )
                }
            }

        }

//        if (mBleService != null) {
//
//
//            if (appNotification.appType == ApplicationType.OTHER.type || appNotification.appType == ApplicationType.WE_CHAT.type) return
//
//            colorFitDevice?.deviceType?.let { deviceType ->
//                if (deviceType == DeviceType.COLORFIT_NAV_PLUS.deviceType ||
//                    deviceType == DeviceType.COLORFIT_PULSE.deviceType ||
//                    deviceType == DeviceType.COLORFIT_BEAT.deviceType
//                ) {
//                    if (appNotification.appType == ApplicationType.SMS.type) {
//                        mBleService?.setMessagePush(
//                            2,
//                            appNotification.name ?: appNotification.number,
//                            message
//                        )
//                        return
//                    }
//                } else if (deviceType == DeviceType.COLORFIT_CALIBER.deviceType || deviceType == DeviceType.COLORFIT_GRAND.deviceType) {
//                    if (appNotification.appType == ApplicationType.SMS.type) {
//                        mBleService?.setMessagePush(
//                            2,
//                            appNotification.number,
//                            (appNotification.name
//                                ?: appNotification.number) + ":" + message
//                        )
//                        mobileNumber = try {
//
//                            appNotification.number
//                        } catch (e: Exception) {
//                            null
//                        }
//                        return
//                    } else if (appNotification.appType == ApplicationType.MISSEDCALL.type) {
//                        mBleService?.setMessagePush(
//                            1,
//                            appNotification.name ?: appNotification.number,
//                            appNotification.name
//                                ?: appNotification.number + ":" + message
//                        )
//                        mobileNumber = try {
//
//                            appNotification.number
//                        } catch (e: Exception) {
//                            null
//                        }
//                        return
//                    }
//
//
//                } else {
//                    if (appNotification.appType == ApplicationType.SMS.type) {
//                        mBleService?.setMessagePush(
//                            2,
//                            appNotification.name ?: appNotification.number,
//                            message
//                        )
//                        mobileNumber = try {
//                            appNotification.number
//                        } catch (e: Exception) {
//                            null
//                        }
//                        return
//                    }
//                }
//            }
//
//
//            var notificationMsg = message
//            if (appNotification.name != null && !appNotification.name.equals("")) {
//                notificationMsg = appNotification.name + ": " + notificationMsg
//            }
//
//            colorFitDevice?.deviceType?.let { deviceType ->
//
//
//                if (deviceType == DeviceType.COLORFIT_BRIO.deviceType ||
//                    deviceType == DeviceType.XFIT.deviceType ||
//                    deviceType == DeviceType.COLORFIT_CALIBER.deviceType) {
//                    mBleService?.thirdPartyPush(
//                        when (appNotification.appType) {
//                            ApplicationType.WHATS_APP.type -> BleConstant.NOTIFY_TYPE_WHATSAPP
//                            ApplicationType.LINKED_IN.type -> BleConstant.NOTIFY_TYPE_LINK
//                            ApplicationType.INSTAGRAM.type -> BleConstant.NOTIFY_TYPE_INSTAGRAM
//                            ApplicationType.FB_MESSENGER.type -> BleConstant.NOTIFY_TYPE_FACEBOOK_MESSAGE
//                            ApplicationType.SKYPE.type -> BleConstant.NOTIFY_TYPE_SKYPE
//                            ApplicationType.TWITTER.type -> BleConstant.NOTIFY_TYPE_TWITTER
//                            ApplicationType.FACEBOOK.type -> BleConstant.NOTIFY_TYPE_FACEBOOK
//                            ApplicationType.VIBER.type -> BleConstant.NOTIFY_TYPE_VIBER
//                            ApplicationType.GMAIL.type -> BleConstant.NOTIFY_TYPE_GMAIL
//                            ApplicationType.OUTLOOK.type -> BleConstant.NOTIFY_TYPE_OUTLOOK
//                            ApplicationType.SNAPCHAT.type -> BleConstant.NOTIFY_TYPE_SNAPCHAT
//                            ApplicationType.CALENDAR.type -> BleConstant.NOTIFY_TYPE_CALENDAR
//                            ApplicationType.TELEGRAM.type -> BleConstant.NOTIFY_TYPE_TELEGRAM
//                            ApplicationType.NOISEFIT.type -> BleConstant.NOTIFY_TYPE_NOISE
//                            else -> BleConstant.NotifaceOther
//                        }, appNotification.name ?: appNotification.number,
//                        message
//                    )
//                } else if(){
//
//                }else{
//                    LOGS.d("NOTIFICATION_CLASS $notificationMsg")
//                    mBleService?.setRemind(
//                        notificationMsg, when (appNotification.appType) {
//                            ApplicationType.SMS.type -> BleConstant.NotifaceMsgMsg
//                            ApplicationType.EMAIL.type -> BleConstant.NotifaceGmail
//                            ApplicationType.WHATS_APP.type -> BleConstant.NotifaceMsgWhatsapp
//                            ApplicationType.LINKED_IN.type -> BleConstant.NotifaceMsgLink
//                            ApplicationType.INSTAGRAM.type -> BleConstant.NotifaceInstagram
//                            ApplicationType.FB_MESSENGER.type -> BleConstant.NotifaceMsgFacebook
//                            ApplicationType.SKYPE.type -> BleConstant.NotifaceMsgSkype
//                            ApplicationType.TWITTER.type -> BleConstant.NotifaceMsgTwitter
//                            ApplicationType.FACEBOOK.type -> BleConstant.NotifaceMsgFacebook
//                            ApplicationType.VIBER.type -> BleConstant.NotifaceMsgViber
//                            ApplicationType.GMAIL.type -> BleConstant.NotifaceGmail
//                            ApplicationType.OUTLOOK.type -> BleConstant.NotifaceOutLook
//                            ApplicationType.SNAPCHAT.type -> BleConstant.NotifaceSnapchat
//                            ApplicationType.WE_CHAT.type -> BleConstant.NotifaceMsgWx
//                            else -> BleConstant.NotifaceOther
//                        }
//                    )
//                }
//            }
//        }

    }

    override fun setWristLiftGesture(wristLiftGesture: WristLiftGesture) {
        mBleService?.setBrightScreent(wristLiftGesture.status)
        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.WristLiftGestureUpdated(
                true
            )
        )

    }

//    override fun insertHeartRatesIntoGoogleFit(data: HeartGoogleFitData) {
//        super.insertHeartRatesIntoGoogleFit(data)
//        NoisefitApplication.context?.applicationContext?.let { context ->
//            GoogleFitHandler.insertHeartRates(context, data)
//        }
//    }
//
//    override fun insertSessionIntoGoogleFit(data: SportsGoogleFitData) {
//        super.insertSessionIntoGoogleFit(data)
//        NoisefitApplication.context?.applicationContext?.let { context ->
//            GoogleFitHandler.insertSessions(context, data)
//        }
//
//    }
//
//    override fun insertStepsIntoGoogleFit(data: StepGoogleFitData) {
//        super.insertStepsIntoGoogleFit(data)
//        NoisefitApplication.context?.applicationContext?.let { context ->
//            GoogleFitHandler.insertSteps(context, data)
//        }
//    }
//
//    override fun insertSleepIntoGoogleFit(data: SleepGoogleFitData) {
//        super.insertSleepIntoGoogleFit(data)
//        NoisefitApplication.context?.applicationContext?.let { context ->
//            GoogleFitHandler.insertSleep(context, data)
//        }
//    }

    override fun setWeatherData(weatherDataList: List<WeatherData>, unit: String) {
        if (weatherDataList.isNotEmpty()) {
            colorFitDevice?.deviceType?.let { deviceType ->
                if (deviceType == DeviceType.COLORFIT_NAV_PLUS.deviceType) {
                    val weatherData = weatherDataList[0]

                    if (mBleService != null) {
                        val listData = ArrayList<WeatherInfo2>()
                        val weather1 = WeatherInfo2()
                        weather1.curTemperature = weatherData.temp.roundToInt()
                        weather1.humidity = weatherData.humidity.roundToInt()
                        weather1.maxTemperature = weatherData.tempMax.roundToInt()
                        weather1.minTemperature = weatherData.tempMin.roundToInt()

                        val weatherType =
                            weatherData.weatherType?.lowercase(DateFormats.defaultLocale)
                        LOGS.d(weatherData)
                        when (weatherType) {
                            "clear" -> {
                                weather1.type = 12 //Calm (daytime)
                            }
                            "clouds" -> {
                                weather1.type = 19 //Partly Cloudy
                            }
                            "thunderstorm" -> {
                                weather1.type = 21 // shower
                            }
                            "drizzle" -> {
                                weather1.type = 16 //Drizzle
                            }
                            "rain" -> {
                                weather1.type = 21 //Shower
                            }
                            "snow" -> {
                                weather1.type = 26 //Snowfall
                            }
                            "haze" -> {
                                weather1.type = 5 //Haze
                            }
                            "smoke" -> {
                                weather1.type = 6 //Smoke
                            }
                            "dust" -> {
                                weather1.type = 3 //Dust
                            }
                            "fog" -> {
                                weather1.type = 4 //Fog
                            }
                            "sand" -> {
                                weather1.type = 3 //Dust
                            }
                            "tornado" -> {
                                weather1.type = 0 //Tornado
                            }

                            else -> {
                                weather1.type = 12 //(253 for unknown type currently set to clear)
                            }
                        }

                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", DateFormats.defaultLocale)
                        val currentDate = dateFormat.format(Date())
                        weather1.date = currentDate

                        listData.add(weather1)

                        LOGS.d(listData)

                        mBleService?.setWeather2(
                            weatherData.pressure.toFloat(),
                            weatherData.city,
                            listData
                        )
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.WeatherSwitchUpdated(
                                true
                            )
                        )

                    }
                } else {
                    val weatherData1 = weatherDataList[0]

                    if (mBleService != null) {
                        val listData = ArrayList<WeatherInfo2>()
                        weatherDataList.forEachIndexed { index, weatherData ->
                            val weather = WeatherInfo2()
                            weather.curTemperature = weatherData.temp.roundToInt()
                            weather.humidity = weatherData.humidity.roundToInt()
                            weather.maxTemperature = weatherData.tempMax.roundToInt()
                            weather.minTemperature = weatherData.tempMin.roundToInt()

                            val weatherType =
                                weatherData.weatherType?.lowercase(DateFormats.defaultLocale)
                            LOGS.d(weatherData)
                            when (weatherType) {
                                "clear" -> {
                                    weather.type = 12 //Calm (daytime)
                                }
                                "clouds" -> {
                                    weather.type = 19 //Partly Cloudy
                                }
                                "thunderstorm" -> {
                                    weather.type = 21 // shower
                                }
                                "drizzle" -> {
                                    weather.type = 16 //Drizzle
                                }
                                "rain" -> {
                                    weather.type = 21 //Shower
                                }
                                "snow" -> {
                                    weather.type = 26 //Snowfall
                                }
                                "haze" -> {
                                    weather.type = 5 //Haze
                                }
                                "smoke" -> {
                                    weather.type = 6 //Smoke
                                }
                                "dust" -> {
                                    weather.type = 3 //Dust
                                }
                                "fog" -> {
                                    weather.type = 4 //Fog
                                }
                                "sand" -> {
                                    weather.type = 3 //Dust
                                }
                                "tornado" -> {
                                    weather.type = 0 //Tornado
                                }

                                else -> {
                                    weather.type =
                                        12 //(253 for unknown type currently set to clear)
                                }
                            }

                            val dateFormat =
                                SimpleDateFormat("yyyy-MM-dd", DateFormats.defaultLocale)
                            var dt = Date()
                            val c = Calendar.getInstance()
                            c.time = dt
                            c.add(Calendar.DATE, index)
                            dt = c.time
                            val currentDate = dateFormat.format(dt)
                            weather.date = currentDate

                            listData.add(weather)
                        }

                        LOGS.d(listData)
                        mBleService?.setWeather2(
                            weatherData1.pressure.toFloat(),
                            weatherData1.city,
                            listData
                        )
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.WeatherSwitchUpdated(
                                true
                            )
                        )

                    }
                }

            }
        }

    }

    override fun closeFindPhoneFromWatch(status: Boolean) {

    }
    override fun setIncomingCallInfo(incomingCall: IncomingCall) {


        colorFitDevice?.deviceType?.let { deviceType ->

            val number =
                if (deviceType == DeviceType.COLORFIT_PULSE.deviceType || deviceType == DeviceType.COLORFIT_BEAT.deviceType) {
                    incomingCall.name ?: incomingCall.number
                } else {
                    incomingCall.number
                }

//            if (isPhoneMuted) {
//                isPhoneMuted = false
//                CallHandler.silenceRinger(isPhoneMuted)
//            }
            mobileNumber = incomingCall.number

            if (mBleService != null) {
                if (deviceType == DeviceType.COLORFIT_CALIBER.deviceType ||
                    deviceType == DeviceType.COLORFIT_CALIBER_2.deviceType ||
                    deviceType == DeviceType.COLORFIT_GRAND.deviceType ||
                    deviceType == DeviceType.XFIT2.deviceType
                ) {
                    when (incomingCall.status) {
                        true -> mBleService?.setMessagePush(
                            0, number, incomingCall.name
                                ?: incomingCall.number
                        )
                        else -> {
                            mBleService?.closeCall()
                        }
                    }
                } else {
                    when (incomingCall.status) {
                        true -> mBleService?.setMessagePush(
                            0, incomingCall.name
                                ?: incomingCall.number, number
                        )
                        else -> {
                            mBleService?.closeCall()
                        }
                    }
                    //FileLogsUtils.saveILogs(colorFitDevice, "Incoming Call", ""+incomingCall.status+":"+incomingCall.number, FileLogsUtils.LogType.Watch)
                }
            }
        }

    }

    override fun updateDND(doNotDisturb: DoNotDisturb) {
        LOGS.d(doNotDisturb)
        mBleService?.setNotDisturb(doNotDisturb.status)
        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.DoNotDisturbUpdated(
                true
            )
        )

    }

    override fun setHeartRateInterval(heartRateInterval: HeartRateInterval) {
        mBleService?.setContinuousHr(heartRateInterval.status)
        watchDataStore.updateHeartRateInterval(heartRateInterval)
        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.HeartRateMeasureIntervalSet(
                true
            )
        )

    }


    override fun setSedentaryData(sedentaryData: SedentaryData) {
        LOGS.d(sedentaryData)

        colorFitDevice?.deviceType?.let {
            if (it.equals(DeviceType.COLORFIT_PULSE.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_BEAT.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_CALIBER.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_CALIBER_2.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_GRAND.deviceType, true) ||
                it.equals(DeviceType.XFIT2.deviceType, true)
            ) {
                watchDataStore.updateIdleAlert(sedentaryData)

                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.SedentaryDataUpdated(
                        true
                    )
                )
            }

        }

        mBleService?.setSitInfo(
            SitInfo(
                sedentaryData.startHour,
                sedentaryData.startMinute,
                sedentaryData.endHour,
                sedentaryData.endMinute,
                when (sedentaryData.interval) {
                    1 -> SitInfo.SitPU1
                    2 -> SitInfo.SitPU2
                    3 -> SitInfo.SitPU3
                    4 -> SitInfo.SitPU4
                    else -> SitInfo.SitPU4
                },
                sedentaryData.status
            )
        )
        colorFitDevice?.deviceType?.let { deviceType ->
            if (deviceType == DeviceType.COLORFIT_NAV_PLUS.deviceType) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.SedentaryDataUpdated(
                        true
                    )
                )

            }
        }


    }

    override fun setAutoSleep(autoSleep: AutoSleep) {

    }

    override fun setSwitchSetting(switchSetting: SwitchSetting) {

    }


    override fun setScreenAwakeInterval(interval: Int) {
    }

    private fun onSwitchSettingUpdated(status: Boolean, p1: Array<out Any>?) {
        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.SwitchSettingUpdated(
                SwitchSetting(status)
            )
        )

    }

    override fun setHandWashing(handWashing: HandWashing) {


        mBleService?.setHandWashingInfo(
            HandWashingInfo(
                handWashing.startHour,
                handWashing.startMinute,
                handWashing.endHour,
                handWashing.endMinute,
                when (handWashing.frequency) {
                    1 -> HandWashingInfo.PU1
                    2 -> HandWashingInfo.PU2
                    3 -> HandWashingInfo.PU3
                    4 -> HandWashingInfo.PU4
                    else -> HandWashingInfo.PU1
                },
                handWashing.startWash
            )
        )


        colorFitDevice?.deviceType?.let {
            if (it.equals(DeviceType.COLORFIT_CALIBER.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_CALIBER_2.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_GRAND.deviceType, true) ||
                it.equals(DeviceType.XFIT2.deviceType, true)
            ) {
                handWashing.frequency = handWashing.frequency
                watchDataStore.updateHandWashData(handWashing)
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.HandWashingUpdated(
                        true
                    )
                )
            }
        }


    }

    override fun updateAPGSData(data1: Uri, data2: Uri) {
        val file1: File = File(data1.path)
        val file2: File = File(data2.path)
        LOGS.d(
            "noise_fit_event:Nav+",
            "updateAPGSData | " + file1.exists() + "::" + file2.exists()
        )

        if (file1.exists()) {
            if (mBleService != null) {
                mBleService!!.getAGpsPrepareStatus(object : AGpsPrepareStatusListener {
                    override fun timeOut() {
                        WatchInfoGlobals.isWatchDataUpdating = false
                        LOGS.d("noise_fit_event:noisefit_nav+", "updateAPGSData : onTimeout")
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.AGPSUpdateProgress(
                                status = UpdateStatus.ERROR
                            )
                        )
                        AppLogs.sendAppLogs(
                            LogEvents.Agps,
                            AgpsEvents.TransferTimeout
                        )

                    }

                    override fun onSuccess(needGpsInfo: Boolean) {
                        LOGS.d("noise_fit_event:noisefit_nav+", "updateAPGSData Success")
                        if (needGpsInfo) {
                            val fileByte: ByteArray = file1.readBytes()
                            mBleService!!.startUploadBigData(
                                BleConstant.UPLOAD_BIG_DATA_LTO,
                                fileByte,
                                object : UploadBigDataListener {
                                    override fun onSuccess() {
                                        WatchInfoGlobals.isWatchDataUpdating = false
                                        LOGS.d(
                                            "noise_fit_event:noisefit_nav+",
                                            "updateAPGSData : Success"
                                        )
                                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                            UpdateDeviceDataCallback.AGPSUpdateProgress(
                                                status = UpdateStatus.COMPLETED
                                            )
                                        )
                                    }

                                    override fun onProgress(
                                        curPiece: Int,
                                        dataPackTotalPieceLength: Int
                                    ) {
                                        WatchInfoGlobals.isWatchDataUpdating = true
                                        val percent= (curPiece * 100 / dataPackTotalPieceLength)
                                        LOGS.d(
                                            "noise_fit_event:noisefit_nav+",
                                            "updateAPGSData $percent"
                                        )

                                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                            UpdateDeviceDataCallback.AGPSUpdateProgress(
                                                status = UpdateStatus.PROGRESS,
                                                progress = percent
                                            )
                                        )
                                    }

                                    override fun onTimeout() {
                                        WatchInfoGlobals.isWatchDataUpdating = false
                                        LOGS.d(
                                            "noise_fit_event:noisefit_nav+",
                                            "updateAPGSData : onTimeout"
                                        )

                                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                            UpdateDeviceDataCallback.AGPSUpdateProgress(
                                                status = UpdateStatus.ERROR
                                            )
                                        )
                                        AppLogs.sendAppLogs(
                                            LogEvents.Agps,
                                            AgpsEvents.TransferTimeout
                                        )
                                    }
                                })
                        }
                    }
                })
            }
        }

    }

    override fun setHeartRateAlert(heartRateAlert: HeartRateAlert) {

        LOGS.d(heartRateAlert)
        mBleService?.setHeartRateMonitor(
            heartRateAlert.status,
            heartRateAlert.status,
            heartRateAlert.max_hr
        )

    }


    override fun setFactoryReset() {
        mBleService?.factorySetting()
        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.FactoryReset(
                true
            )
        )


    }

    override fun setRestartDevice() {
//        LoggerHelper.printVerbose("noise_fit_event:colorfit_hybrid", "restart")
//        BluetoothSDK.restartSDK()
    }

    override fun setDrinkWaterReminder(sedentaryData: SedentaryData) {
        LOGS.d(sedentaryData)

        colorFitDevice?.deviceType?.let {
            if (it.equals(DeviceType.COLORFIT_PULSE.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_BEAT.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_CALIBER.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_CALIBER_2.deviceType, true) ||
                it.equals(DeviceType.COLORFIT_GRAND.deviceType, true) ||
                it.equals(DeviceType.XFIT2.deviceType, true)
            ) {
                watchDataStore.updateWaterReminder(sedentaryData)

                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.DrinkWaterUpdated(
                        true
                    )
                )
            }
        }

        mBleService?.setDrinkInfo(
            DrinkInfo(
                sedentaryData.startHour,
                sedentaryData.startMinute,
                sedentaryData.endHour,
                sedentaryData.endMinute,
                when (sedentaryData.interval) {
                    1 -> DrinkInfo.DrinkPU1
                    2 -> DrinkInfo.DrinkPU2
                    3 -> DrinkInfo.DrinkPU3
                    4 -> DrinkInfo.DrinkPU4
                    else -> DrinkInfo.DrinkPU4
                },
                sedentaryData.status
            )
        )

        colorFitDevice?.let { fitDevice ->
            if (fitDevice.deviceType == DeviceType.COLORFIT_NAV_PLUS.deviceType) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.DrinkWaterUpdated(
                        true
                    )
                )

            }
        }


    }

    override fun setMedicineReminder(sedentaryData: SedentaryData) {
        val medicalInfo = MedicalInfo(
            sedentaryData.startHour,
            sedentaryData.startMinute,
            sedentaryData.endHour,
            sedentaryData.endMinute,
            when (sedentaryData.interval) {
                4 -> MedicalInfo.MedicalPU4
                6 -> MedicalInfo.MedicalPU6
                8 -> MedicalInfo.MedicalPU8
                12 -> MedicalInfo.MedicalPU12
                else -> MedicalInfo.MedicalPU4
            },
            sedentaryData.status
        )
        mBleService?.setMedicalInfo(medicalInfo)

        watchDataStore.updateMedicineReminder(sedentaryData)

        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.MedicineDataUpdated(
                true
            )
        )
    }

    override fun setMealReminder(sedentaryData: SedentaryData) {
        LOGS.d("MealReminder ${sedentaryData}")
        mBleService?.setMealReminders(
            sedentaryData.status,
            when (sedentaryData.interval) {
                1 -> DrinkInfo.DrinkPU1
                2 -> DrinkInfo.DrinkPU2
                3 -> DrinkInfo.DrinkPU3
                4 -> DrinkInfo.DrinkPU4
                else -> DrinkInfo.DrinkPU4
            },
            sedentaryData.startHour,
            sedentaryData.startMinute,
            sedentaryData.endHour,
            sedentaryData.endMinute
        )


        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.MealDataUpdated(
                true
            )
        )
    }

    override fun updateCustomReply(customReplyData: CustomReplyData) {

        val quickReplys: MutableList<QuickReply> = ArrayList<QuickReply>()
        customReplyData.customReplies.forEach { item ->
            quickReplys.add(QuickReply(item.content))
        }
        mBleService?.setQuickReplyListener(quickReplys, object : SetQuickReplyListener {
            override fun onSuccess() {
                // LOGS.i("Quick reply Success","v abnvcsvbncd"+Gson().toJson(customReplyData))
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.CustomizeReplyUpdated(
                        true
                    )
                )
            }

            override fun onFailure() {
                LOGS.i("Quick reply Failed")
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.CustomizeReplyUpdated(
                        false
                    )
                )
            }
        })

    }


    private fun initSingleQuickReplyListener() {
        mBleService?.let { zhBraceletService ->
            zhBraceletService.setSingleQuickReplyListener { name, content ->
                LOGS.i("QuickReply $name : $content ")
                try {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.SEND_SMS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.UpdateCallStatus(
                                false
                            )
                        )
                    } else {
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.UpdateCallStatus(
                                false
                            )
                        )
                        colorFitDevice?.deviceType?.let { deviceType ->
                            if (deviceType == DeviceType.COLORFIT_CALIBER.deviceType ||
                                deviceType == DeviceType.COLORFIT_CALIBER_2.deviceType ||
                                deviceType == DeviceType.COLORFIT_GRAND.deviceType ||
                                deviceType == DeviceType.XFIT2.deviceType
                            ) {
                                name?.let { name ->
                                    LOGS.d("QuickReplys Sending SMS: $name")
                                    val smsManager = SmsManager.getDefault()
                                    smsManager.sendTextMessage(name, null, content, null, null)
                                }
                            } else {
                                mobileNumber?.let { number ->
                                    LOGS.d("QuickReplys Sending SMS: $number")
                                    val smsManager = SmsManager.getDefault()
                                    smsManager.sendTextMessage(number, null, content, null, null)
                                }
                            }
                        }


                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }

    private fun initDeviceSendMuteListener() {

        mBleService?.let {
            it.setDeviceSendMuteListener {

                testUpdateDeviceDataCallback?.onUpdateDataReceived(UpdateDeviceDataCallback.MuteDevice())


            }
        }
    }

    override fun setWorldClock(data: WorldClocksPushData) {
        val list = ArrayList<WorldClock>()
        data.worldClocks.forEach { item ->
            list.add(WorldClock(item.timeZone, item.content))
        }
        mBleService?.setWorldClockListener(
            data.localTimeZone,
            list,
            object : SetWorldClockListener {
                override fun onSuccess() {
                    LOGS.i("setWorldClock onSuccess")
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.WorldClockSet(
                            true
                        )
                    )
                }

                override fun onFailure() {
                    LOGS.i("setWorldClock onFailure")
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.WorldClockSet(
                            false
                        )
                    )

                }
            })
    }

    override fun setTemperatureUnit(unit: String) {
        var unitId = 0
        var unitInImperial = false
        if (unit.equals(Units.IMPERIAL.name, true)) {
            unitId = 1
            unitInImperial = true
        }

        colorFitDevice?.deviceType?.let {
            if (it.equals(DeviceType.COLORFIT_CALIBER.deviceType, true)||
                it.equals(DeviceType.COLORFIT_CALIBER_2.deviceType, true)
            ) {
                mBleService?.setWeatherTemperatureUnit(unitInImperial)
            } else {
                mBleService?.setTemperatureUnit(unitId)
            }
        }


    }

    override fun setQuickEyeMovementSwitch(status: Boolean) {
        mBleService?.setQuickEyeSwitch(status)

    }

    private fun initQuickEyeListener() {

        mBleService?.setQuickEyeSwitchListener(
            object : QuickEyeSwitchListener {
                override fun onSuccess() {
                    LOGS.i("setQuickEyeMovementSwitch success")
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.QuickEyeMovementSwitchUpdated(
                            true
                        )
                    )
                }

                override fun onFail() {
                    LOGS.i("setQuickEyeMovementSwitch failed")
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.QuickEyeMovementSwitchUpdated(
                            false
                        )
                    )
                }

                override fun onResponseData(isOpen: Boolean) {

                }
            })
    }

}