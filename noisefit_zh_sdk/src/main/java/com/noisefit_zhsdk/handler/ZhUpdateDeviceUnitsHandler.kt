package com.noisefit_zhsdk.handler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.constants.CommonGlobals
import com.noisefit_commans.constants.WatchFaceEventsConstants
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.db.abstraction.LocationDataSource
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.enums.ApplicationType
import com.noisefit_commans.interfaces.device_data.IUpdateDeviceDataCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallbacks
import com.noisefit_commans.interfaces.device_data.WorkoutFailReason
import com.noisefit_commans.models.AlarmAction
import com.noisefit_commans.models.AlarmsList
import com.noisefit_commans.models.AppNotification
import com.noisefit_commans.models.AutoSleep
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.Contact
import com.noisefit_commans.models.CustomReplyData
import com.noisefit_commans.models.DeviceUnits
import com.noisefit_commans.models.DiyCustomWatchFace
import com.noisefit_commans.models.DoNotDisturb
import com.noisefit_commans.models.Gender
import com.noisefit_commans.models.HandWashing
import com.noisefit_commans.models.HeartRateAlert
import com.noisefit_commans.models.HeartRateInterval
import com.noisefit_commans.models.IncomingCall
import com.noisefit_commans.models.Language
import com.noisefit_commans.models.LocationDataModel
import com.noisefit_commans.models.ManualMeasureType
import com.noisefit_commans.models.ManualMeasurement
import com.noisefit_commans.models.MenstrualData
import com.noisefit_commans.models.ReminderList
import com.noisefit_commans.models.SOSContact
import com.noisefit_commans.models.SedentaryData
import com.noisefit_commans.models.SleepReminder
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
import com.noisefit_commans.models.Widget
import com.noisefit_commans.models.WorldClocksPushData
import com.noisefit_commans.models.WristLiftGesture
import com.noisefit_commans.utils.AgpsEvents
import com.noisefit_commans.utils.AppConversionUtils
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.ImageUtil
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LocationClientClass
import com.noisefit_commans.location.LocationUtils
import com.noisefit_commans.utils.LogEvents
import com.noisefit_commans.utils.WatchFaceEvents
import com.noisefit_commans.utils.sizeInKb
import com.noisefit_zhsdk.base.ZhApplicationHandler
import com.zhapp.ble.BleCommonAttributes
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.ActiveMeasureResultBean
import com.zhapp.ble.bean.ActiveMeasureStatusBean
import com.zhapp.ble.bean.ActiveMeasuringBean
import com.zhapp.ble.bean.ClassicBluetoothStateBean
import com.zhapp.ble.bean.ClockInfoBean
import com.zhapp.ble.bean.ClockInfoBean.DataBean
import com.zhapp.ble.bean.CommonReminderBean
import com.zhapp.ble.bean.ContactBean
import com.zhapp.ble.bean.DoNotDisturbModeBean
import com.zhapp.ble.bean.EmergencyContactBean
import com.zhapp.ble.bean.EventInfoBean
import com.zhapp.ble.bean.HeartRateMonitorBean
import com.zhapp.ble.bean.RingAutoActiveSportConfigBean
import com.zhapp.ble.bean.RingSportStatusBean
import com.zhapp.ble.bean.SendRingSportStatusBean
import com.zhapp.ble.bean.SettingTimeBean
import com.zhapp.ble.bean.StockInfoBean
import com.zhapp.ble.bean.TimeBean
import com.zhapp.ble.bean.WeatherDayBean
import com.zhapp.ble.bean.WeatherPerHourBean
import com.zhapp.ble.bean.WorldClockBean
import com.zhapp.ble.callback.ActiveMeasureCallBack
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.DeviceLargeFileStatusListener
import com.zhapp.ble.callback.DeviceWatchFaceFileStatusListener
import com.zhapp.ble.callback.RingSportCallBack
import com.zhapp.ble.callback.UploadBigDataListener
import com.zhapp.ble.callback.WatchFaceInstallCallBack
import com.zhapp.ble.parsing.ParsingStateManager
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

private val TAG = "ZhUpdateDeviceUnitsHandler"

class ZhUpdateDeviceUnitsHandler
@Inject
constructor(
    var dataConverter: DataConverter,
    var oreoDataConverter: OreoDataConverter,
    var context: Context,
    var gson: Gson,
    var zhApplicationHandler: ZhApplicationHandler,
    var watchDataStore: WatchDataStore,
) : UpdateDeviceDataActions() {


    private var sessionId = 0L
    private var currentGpsSportState = -1

    companion object {
        const val LAT_LONG = "LAT_LONG"
    }

    private var zhService: ControlBleTools? = null
    private var updateDeviceDataCallbacks: UpdateDeviceDataCallbacks? = null


    private var colorFitDevice: ColorFitDevice? = null

    private var testUpdateDeviceDataCallback: IUpdateDeviceDataCallback? = null

    override fun init() {
        super.init()
        zhService = zhApplicationHandler.getZhService()
        AppLogs.sendAppLogs("Initialize zh service")
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
        CallBackUtils.watchFaceInstallCallBack = watchFaceInstallCallBack
        CallBackUtils.ringSportCallBack = ringSportCallback

    }

    private val activeMeasureCallBack: ActiveMeasureCallBack = object : ActiveMeasureCallBack {
        override fun onMeasureStatus(p0: ActiveMeasureStatusBean?) {
//            LOGS.d(TAG, "onMeasuring nMeasureStatus ${Gson().toJson(p0)}")
        }

        override fun onMeasuring(p0: ActiveMeasuringBean?) {
//            LOGS.d(TAG, "onMeasuring ${Gson().toJson(p0)}")
        }

        override fun onMeasureResult(p0: ActiveMeasureResultBean?) {
//            LOGS.d(TAG, "onMeasuring onMeasureResult ${Gson().toJson(p0)}")

            if (p0 == null) {
                return
            }

            var isError = false
            if (p0.errorReason > 0) {
                isError = true
            }

            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.ManualMeasurementObtained(
                    ManualMeasurement(
                        false,
                        isError,
                        p0.measureValue,
                        oreoDataConverter.getMeasureType(p0.measureType),
                        System.currentTimeMillis()
                    )
                )
            )


        }


    }

    /**
     * status->true -> enable
     */
    override fun setRealTimeDataState(status: Boolean) {
        ControlBleTools.getInstance().realTimeDataSwitch(status, null)
    }


    override fun setStressData(sedentaryData: SedentaryData) {
        ControlBleTools.getInstance().setPressureMode(
            dataConverter.convertStress(sedentaryData),
            object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.StressDataUpdated(
                                    true
                                )
                            )

                        }

                        else -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.StressDataUpdated(
                                    false
                                )
                            )

                        }
                    }
                }
            })
    }

    override fun setUserInfo(userInfo: UserInfo, userGoals: UserGoals, userName: String?) {
        LOGS.d("setUserInfo $userName")
        var date = Date()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        try {
            date = simpleDateFormat.parse(userInfo.dob)
        } catch (e: ParseException) {
            e.printStackTrace()
            date.time = 0
        }
        val bean = com.zhapp.ble.bean.UserInfo()
        bean.age = AppConversionUtils.getAgeFromDOB(userInfo.dob)
        bean.height = userInfo.height
        bean.userName = userName
        bean.weight = userInfo.weight.toFloat()
        bean.birthday = date.time.toInt()
        bean.sex = when (userInfo.gender.lowercase()) {
            Gender.FEMALE.type.lowercase() -> 2
            else -> 1
        }
        bean.standingTimesGoal = 18
        bean.calGoal = userGoals.caloriesGoal
        bean.stepGoal = userGoals.stepGoal
        bean.distanceGoal = userGoals.distanceGoal
        bean.standingTimesGoal = 18
        ControlBleTools.getInstance().setUserProfile(bean, null)


        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.UserInfoUpdated(
                true
            )
        )
        AppLogs.sendAppLogs("set user info")

    }

    override fun setManualMeasurement(manualMeasureType: ManualMeasureType, status: Boolean) {
        CallBackUtils.activeMeasureCallBack = activeMeasureCallBack
        LOGS.d(
            "onMeasuring setManualMeasurement ${
                oreoDataConverter.getManualMeasurement(
                    manualMeasureType,
                    status
                )
            }"
        )
        ControlBleTools.getInstance().activeMeasurementStart(
            oreoDataConverter.getManualMeasurement(
                manualMeasureType,
                status
            ), object : SendCmdStateListener(null) {
                override fun onState(state: SendCmdState) {
                    LOGS.d(ZhQueryDeviceUnitsHandler.TAG, "$state")

                    if (state == SendCmdState.NOT_SUPPORT) {
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.ManualMeasurementObtained(
                                ManualMeasurement(
                                    false,
                                    true,
                                    0,
                                    manualMeasureType,
                                    System.currentTimeMillis()
                                )
                            )
                        )
                    }
                }
            })
    }

    override fun checkOngoingWorkout() {
        ControlBleTools.getInstance()
            .getRingSportStatus(object : ParsingStateManager.SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    when (state) {
                        SendCmdState.SUCCEED -> {}
                        else -> {}
                    }
                }
            })
    }

    override fun setAutoWorkoutStatus(status: Boolean) {
        ControlBleTools.getInstance().getRingAutoActiveSportConfig(null)
        ControlBleTools.getInstance().setRingAutoActiveSportConfig(
            RingAutoActiveSportConfigBean(status), null
        )
    }


    private var locationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            var isPause = true
            if (currentGpsSportState == 1 || currentGpsSportState == 3) {
                isPause = false
            }

            LOGS.d("startWorkout location inside isPause: $isPause ($currentGpsSportState) sessionId: $sessionId")
            val locationArrayList =
                intent.getParcelableArrayListExtra<LocationDataModel>(LAT_LONG)

            if (locationArrayList.isNullOrEmpty()) {
                return
            }


            locationArrayList.forEach {
                it.isRunning = isPause

            }

            if (sessionId == 0L) {
                LOGS.d("workout sessionId can't be zero")
                AppLogs.sendAppLogs("workout sessionId can't be zero")
            } else {
                watchDataStore.saveAndGetLocation(sessionId, locationArrayList)
            }


        }
    }

    private fun startLocationTracking() {
        LOGS.d("LOCATION_lOG Start Location tracking")
        AppLogs.sendAppLogs("Start Location tracking")
        LocationUtils.startLocationService()
    }

    private fun stopLocationTracking() {
        LOGS.d("LOCATION_lOG Stop Location tracking")
        AppLogs.sendAppLogs("Stop Location tracking")
        LocationUtils.stopLocationService()
    }

    override fun startWorkout(sportType: Int, sportStartTime: Long, startGps: Boolean) {

        val bean = SendRingSportStatusBean(
            sportType,
            RingSportCallBack.RingSportStatus.SPORT_STATUS_START.status,
            sportStartTime
        )
        ControlBleTools.getInstance()
            .sendRingSportStatus(bean, object : ParsingStateManager.SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            //turn off auto workout recording
                            setAutoWorkoutStatus(false)

                            LOGS.d("startWorkout started ")
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.WorkoutStartState(true)
                            )
                            if (startGps) {
                                startLocationTracking()
                            }
                        }

                        else -> {
                            LOGS.d("startWorkout failed")
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.WorkoutStartState(
                                    false,
                                    WorkoutFailReason.FROM_RING
                                )
                            )
                        }
                    }
                }
            })
    }

    /**
     * @param action 2->Pause. 3-> Resume 4->Stop
     *
     */
    override fun updateOngoingWorkout(sportType: Int, sportTimeStamp: Long, action: Int) {
        LOGS.d("startWorkout updateOngoingWorkout ${sportType} -------> $action")
        val status = when (action) {
            2 -> RingSportCallBack.RingSportStatus.SPORT_STATUS_PAUSE.status
            3 -> RingSportCallBack.RingSportStatus.SPORT_STATUS_RESUME.status

            else -> RingSportCallBack.RingSportStatus.SPORT_STATUS_END.status
        }

        val bean = SendRingSportStatusBean(sportType, status, sportTimeStamp)
        ControlBleTools.getInstance().sendRingSportStatus(bean, object : SendCmdStateListener() {
            override fun onState(state: SendCmdState?) {
                when (state) {
                    SendCmdState.SUCCEED -> {
                        when (action) {
                            2 -> {
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.WorkoutPaused(true)
                                )
                            }

                            3 -> {
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.WorkoutResumed(true)
                                )
                            }

                            4 -> {
                                stopLocationTracking()
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.WorkoutStopped(true)
                                )
                                ControlBleTools.getInstance().getFitnessSportIdsData(null)
                                setAutoWorkoutStatus(true)
                            }
                        }
                    }

                    else -> {
                        when (action) {
                            2 -> {
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.WorkoutPaused(false)
                                )
                            }

                            3 -> {
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.WorkoutResumed(false)
                                )
                            }

                            4 -> {
                                stopLocationTracking()
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.WorkoutStopped(false)
                                )
                                ControlBleTools.getInstance().getFitnessSportIdsData(null)
                                setAutoWorkoutStatus(true)

                            }
                        }
                    }
                }
            }
        })

    }

    fun stopWorkout(error: String) {
        stopLocationTracking()
        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.WorkoutStoppedByRing(error)
        )
        setAutoWorkoutStatus(true)
        //ControlBleTools.getInstance().getFitnessSportIdsData(null)
    }

    private val ringSportCallback = object : RingSportCallBack {
        override fun onRingSportStatus(bean: RingSportStatusBean?) {
            LOGS.d(TAG, "onRingSportStatus ${Gson().toJson(bean)}")
            LOGS.d("startWorkout onRingSportStatus bean::::  ${Gson().toJson(bean)}")
            if (bean == null) return


            if (bean.startResult == RingSportCallBack.RingSportStartResult.SPORT_START_RESULT_NONE.result && bean.isSporting &&
                bean.sportStatus != RingSportCallBack.RingSportStatus.SPORT_STATUS_END.status
            ) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.OngoingWorkoutData(
                        bean.duration,
                        bean.sportStatus,
                        bean.sportType,
                        bean.startTime
                    )
                )
            }

            if (bean.startResult != RingSportCallBack.RingSportStartResult.SPORT_START_RESULT_NONE.result) {
                setAutoWorkoutStatus(true)
                when (bean.startResult) {
                    RingSportCallBack.RingSportStartResult.SPORT_START_RESULT_LOW_POWER.result -> {
                        /*testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.WorkoutEndFromRingState("Low Battery")
                        )*/
                        AppLogs.sendAppLogs("Workout failed from ring Reason: Low Battery")
                        stopWorkout("Low Battery")
                    }

                    RingSportCallBack.RingSportStartResult.SPORT_START_RESULT_UN_WEAR.result -> {
                        /*testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.WorkoutEndFromRingState("Device not worn")
                        )*/
                        AppLogs.sendAppLogs("Workout failed from ring Reason: Device not worn")

                        //stopWorkout("Device not worn")
                    }

                    RingSportCallBack.RingSportStartResult.SPORT_START_RESULT_CHARGING.result -> {
                        /*testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.WorkoutEndFromRingState("Ring on Charging")
                        )*/
                        AppLogs.sendAppLogs("Workout failed from ring Reason: Ring on Charging")

                        //stopWorkout("Ring on Charging")

                    }
                }
            }

            if (bean.sportStatus == RingSportCallBack.RingSportStatus.SPORT_STATUS_END.status) {
                setAutoWorkoutStatus(true)

                if (bean.endReason != RingSportCallBack.RingSportEndReason.SPORT_END_REASON_NONE.reason) {
                    when (bean.endReason) {
                        RingSportCallBack.RingSportEndReason.SPORT_END_REASON_LOW_POWER.reason -> {
                            /*testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.WorkoutEndFromRingState("Low Battery")
                            )*/
                            AppLogs.sendAppLogs("Workout failed from ring Reason: Low Battery")

                            stopWorkout("Low Battery")
                        }

                        RingSportCallBack.RingSportEndReason.SPORT_END_REASON_TIMEOUT.reason -> {
                            /*testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.WorkoutEndFromRingState(
                                    "Exercise 8 hours timeout"
                                )
                            )*/
                            AppLogs.sendAppLogs("Workout failed from ring Reason: Exercise 8 hours timeout")

                            //stopWorkout("Exercise 8 hours timeout")

                        }

                        RingSportCallBack.RingSportEndReason.SPORT_END_REASON_NO_MEMORY.reason -> {
                            /*testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.WorkoutEndFromRingState(
                                    "Insufficient device memory"
                                )
                            )*/
                            AppLogs.sendAppLogs("Workout failed from ring Reason: Insufficient device memory")

                            //stopWorkout("Insufficient device memory")
                        }
                    }
                }

                /*if (bean.isSportNoSync) {
                    ControlBleTools.getInstance()
                        .getFitnessSportIdsData(null)
                }*/
            }

            /*testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.OngoingWorkoutData(
                    WorkoutData(
                        steps = p0?.steps,
                        calories = p0?.calories,
                        sportLevel = p0?.sportLevel,
                        distance = p0?.distance,
                        heartRate = p0?.heartRate
                    )
                )
            )*/
        }

    }

    override fun closeFindPhoneFromWatch(status: Boolean) {
        LOGS.d("closeFindPhoneFromWatch $status")
        if (status) {
            ControlBleTools.getInstance().sendCloseFindPhone(null)
        }
    }

    override fun setDeviceUnits(units: DeviceUnits) {


        ControlBleTools.getInstance().setDistanceUnit(
            when (units.unitSystem?.lowercase()) {
                UnitSystem.IMPERIAL.type.lowercase() -> {
                    1
                }

                else -> {
                    0
                }
            }, null
        )
        ControlBleTools.getInstance().setLanguage(0, null)

        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.DeviceUnitsUpdated(
                true
            )
        )
        AppLogs.sendAppLogs("set device unit")


    }


    override fun setSportWidgetSortList(dataList: List<Widget>) {

        ControlBleTools.getInstance().setSportWidgetSortList(
            dataConverter.convertAppToWidgetList(dataList),
            object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {

                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.SportWidgetSortDataUpdated(
                                    true
                                )
                            )
                            AppLogs.sendAppLogs("sport widget data update succeed")
                        }

                        else -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.SportWidgetSortDataUpdated(
                                    false
                                )
                            )
                            AppLogs.sendAppLogs("sport widget data not update")
                        }
                    }
                }
            })
    }


    override fun updateSleepReminder(sleepReminder: SleepReminder) {
        ControlBleTools.getInstance()
            .setSleepReminder(dataConverter.convertSleepReminder(sleepReminder),
                object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState?) {
                        when (state) {
                            SendCmdState.SUCCEED -> {
                                LOGS.d("updateSleepReminder SUCCEED")
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.SleepReminderUpdated(
                                        true
                                    )
                                )
                                AppLogs.sendAppLogs("sleep reminder update succeed")
                            }

                            else -> {
                                LOGS.d("updateSleepReminder Error")
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.SleepReminderUpdated(
                                        false
                                    )
                                )
                                AppLogs.sendAppLogs("sleep reminder update not succeed")
                            }
                        }
                    }

                })

    }

    override fun updateAlarm(alarm: AlarmsList, alarmAction: AlarmAction) {
        val clockInfoBeans = ArrayList<ClockInfoBean>()
        alarm.alarms?.forEachIndexed { index, item ->
            val clockInfo = ClockInfoBean()
            val data = DataBean()
            data.time = SettingTimeBean(item.hour, item.minute)
            data.clockName = "Clock ${index + 1}"
            data.isEnable = item.status
            data.isMonday = item.repeatDays?.get(1) ?: false
            data.isTuesday = item.repeatDays?.get(2) ?: false
            data.isWednesday = item.repeatDays?.get(3) ?: false
            data.isThursday = item.repeatDays?.get(4) ?: false
            data.isFriday = item.repeatDays?.get(5) ?: false
            data.isSaturday = item.repeatDays?.get(6) ?: false
            data.isSunday = item.repeatDays?.get(7) ?: false
            data.calculateWeekDays()
            clockInfo.id = 0
            clockInfo.data = data
            clockInfoBeans.add(clockInfo)
        }

        ControlBleTools.getInstance()
            .setClockInfoList(clockInfoBeans, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.AlarmUpdated(
                                    true
                                )
                            )
                            AppLogs.sendAppLogs("alarm data update succeed")
                        }

                        else -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.AlarmUpdated(
                                    false
                                )
                            )
                            AppLogs.sendAppLogs("alarm data update not succeed")
                        }
                    }
                }
            })
    }


    override fun setContactList(contactList: List<Contact>) {
        LOGS.d("contacts list::" + contactList.toString())
        try {
            val contactBeans = ArrayList<ContactBean>()
            contactList.forEach {
                val contact = ContactBean()
                contact.contacts_name = it.name
                contact.contacts_number = it.number[0]
                contactBeans.add(contact)
            }
            ControlBleTools.getInstance()
                .setContactList(contactBeans, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState) {
                        when (state) {
                            SendCmdState.SUCCEED -> {
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.ContactListUpdated(
                                        true
                                    )
                                )
                                AppLogs.sendAppLogs("contact list data update succeed")
                            }

                            else -> {
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.ContactListUpdated(
                                        true
                                    )
                                )

                            }
                        }
                    }
                })
        } catch (e: Exception) {

        }
    }

    override fun setSOSContact(sosContact: SOSContact) {
        try {
            val emergencyContactBean = EmergencyContactBean()
            val contactBeans = ArrayList<ContactBean>()
            sosContact.contactList.forEach {
                val contact = ContactBean()
                contact.contacts_name = it.name
                contact.contacts_number = it.number[0]
                contactBeans.add(contact)
            }
            emergencyContactBean.sosSwitch = sosContact.sosSwitch
            emergencyContactBean.contactList = contactBeans
            ControlBleTools.getInstance()
                .setEmergencyContacts(emergencyContactBean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState) {
                        when (state) {
                            SendCmdState.SUCCEED -> {
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.SOSContactUpdated(
                                        true
                                    )
                                )
                                AppLogs.sendAppLogs("sos contact list data update succeed")
                            }

                            else -> {
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.SOSContactUpdated(
                                        true
                                    )
                                )

                            }
                        }
                    }
                })
        } catch (e: Exception) {

        }
    }

    override fun deleteAlarm(alarm: AlarmsList) {

    }

    override fun deleteReminders(reminderList: ReminderList) {
        val eventInfoBeans = ArrayList<EventInfoBean>()
        reminderList.reminders?.forEach {
            val infoBean = EventInfoBean()
            val timeBean: TimeBean
            val des: String = it.label ?: ""
            try {
                timeBean = TimeBean(
                    it.year,
                    it.month,
                    it.day,
                    it.hour,
                    it.minute,
                    0
                )
                infoBean.time = timeBean
                infoBean.description = des
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                return
            }
            eventInfoBeans.add(infoBean)
        }

        ControlBleTools.getInstance()
            .setEventInfoList(eventInfoBeans, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.DeleteReminder(
                                    true
                                )
                            )
                            AppLogs.sendAppLogs("delete reminder data update succeed")
                        }

                        else -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.DeleteReminder(
                                    true
                                )
                            )
                        }
                    }
                }
            })
    }

    override fun updateWidgetList(data: List<Widget>) {
        LOGS.d("updateWidgetList ${gson.toJson(data)}")
        ControlBleTools.getInstance().setWidgetList(
            dataConverter.convertAppToWidgetList(data),
            object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.WidgetSortListUpdated(
                                    true
                                )
                            )
                            AppLogs.sendAppLogs("app list data update succeed")
                        }

                        else -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.WidgetSortListUpdated(
                                    true
                                )
                            )
                        }
                    }
                }
            })
    }

    override fun updateApplicationList(data: List<Widget>) {
        ControlBleTools.getInstance().setApplicationList(
            dataConverter.convertAppToWidgetList(data),
            object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.UpdateAppList(
                                    true
                                )
                            )
                            AppLogs.sendAppLogs("app list data update succeed")
                        }

                        else -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.UpdateAppList(
                                    true
                                )
                            )
                        }
                    }
                }
            })
    }

    private fun updateAppListSorting() {
//        ControlBleTools.getInstance().setApplicationList()
//        ControlBleTools.getInstance().getApplicationList(object : SendCmdStateListener() {
//            override fun onState(state: SendCmdState) {
//                LOGS.d("APP_SORT_CMD ${state.name}")
//            }
//
//        })
    }

    override fun updateMenstrualData(menstrualData: MenstrualData) {


    }

    override fun syncStockInfoList(stockInfoList: StockInfoList) {
        val stockArray = ArrayList<StockInfoBean>()
        val timeStamp = System.currentTimeMillis().toInt()
        stockInfoList.stockInfoList.forEachIndexed { index, item ->
            val bean = StockInfoBean()
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

        ControlBleTools.getInstance()
            .syncStockInfoList(stockArray, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.SyncStockInfoList(
                                    true
                                )
                            )
                            AppLogs.sendAppLogs("Sync Stock data succeed")
                        }

                        SendCmdState.TIMEOUT -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.SyncStockInfoList(
                                    false
                                )
                            )
                            AppLogs.sendAppLogs("sync stock data timeout")
                        }

                        else -> {}
                    }
                }
            })


    }

    override fun deleteStock(symbol: String) {

        ControlBleTools.getInstance().deleteStockBySymbol(
            symbol,
            object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            Handler(Looper.getMainLooper()).postDelayed({
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.DeleteStock(
                                        true
                                    )
                                )
                                AppLogs.sendAppLogs("delete stock succeed")
                            }, 1000)
                        }

                        SendCmdState.TIMEOUT -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.DeleteStock(
                                    false
                                )
                            )
                            AppLogs.sendAppLogs("delete stock timeout")
                        }

                        else -> {}
                    }
                }
            })


    }

    override fun addReminder(reminder: ReminderList.Reminder) {
        val eventInfoBeans = ArrayList<EventInfoBean>()
        if (ZhQueryDeviceUnitsHandler.reminders == null) {
            ZhQueryDeviceUnitsHandler.reminders = ReminderList(arrayListOf(reminder))
        }
        (ZhQueryDeviceUnitsHandler.reminders?.reminders as ArrayList).add(0, reminder)
        ZhQueryDeviceUnitsHandler.reminders?.reminders?.forEach {
            val infoBean = EventInfoBean()
            val timeBean: TimeBean
            val des: String = it.label ?: ""
            try {
                timeBean = TimeBean(
                    it.year,
                    it.month,
                    it.day,
                    it.hour,
                    it.minute,
                    0
                )
                infoBean.time = timeBean
                infoBean.description = des
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                return
            }
            eventInfoBeans.add(infoBean)
        }

        ControlBleTools.getInstance()
            .setEventInfoList(eventInfoBeans, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.AddReminder(
                                    true
                                )
                            )
                            AppLogs.sendAppLogs("add reminder update succeed")
                        }

                        else -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.AddReminder(
                                    true
                                )
                            )
                        }
                    }
                }
            })
    }

    override fun updateLanguage(language: Language) {


    }

    override fun setBrightnessLevel(level: Int) {

//        val screenSetting = ScreenSettingBean()
//        try {
//            screenSetting.isSwitch = false
//            screenSetting.level = level / 20
//            //screenSetting.duration = 5
//            screenSetting.duration = CommonGlobals.screenDuration
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return
//        }
//        ControlBleTools.getInstance()
//            .setScreenSetting(screenSetting, object : SendCmdStateListener() {
//                override fun onState(state: SendCmdState) {
//                    when (state) {
//                        SendCmdState.SUCCEED -> {
//                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
//                                UpdateDeviceDataCallback.BrightnessLevelUpdated(
//                                    true
//                                )
//                            )
//                        }
//                        else -> {
//                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
//                                UpdateDeviceDataCallback.BrightnessLevelUpdated(
//                                    false
//                                )
//                            )
//                        }
//                    }
//                }
//            })
    }

    override fun findDevice(findDevice: SwitchSetting) {
        LOGS.d("findDevice", "findDevice Command sent" + findDevice.status)
        if (findDevice.status) {
            ControlBleTools.getInstance().sendFindWear(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    LOGS.d("findDevice", "findDevice $state")
                    AppLogs.sendAppLogs("findDevice $state")
                }

            })
        }
    }

    override fun setDeviceDateTime(calender: Calendar, units: TimeFormat) {
        if (ControlBleTools.getInstance().isConnect) {
            ControlBleTools.getInstance().setTime(System.currentTimeMillis(), null)
            units.timeFormat?.let { setDeviceTime(it) }
        }
    }


    private fun setDeviceTime(timeFormat: String) {
        LOGS.d("deviceTime $timeFormat")
        ControlBleTools.getInstance().setTimeFormat(
            when (timeFormat.lowercase()) {
                TimeFormats.HOURS_12.type.lowercase() -> true
                else -> false
            }, null
        )
        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.DeviceTimeSynced(
                true
            )
        )
        AppLogs.sendAppLogs("device time sync")
    }


    override fun onDownloadedWatchFaceContents(watchFace: WatchFace) {


    }

    private val watchFaceInstallCallBack = WatchFaceInstallCallBack { p0 ->
        when (p0?.code) {
            2 -> {
                LOGS.d("watchface installation : success")
                AppLogs.sendAppLogs("watchface installation : success")
            }

            1 -> {
                LOGS.d("watchface installation : failed")
                AppLogs.sendAppLogs("watchface installation : failed")
            }

            0 -> {
                LOGS.d("watchface installation : verification failed")
                AppLogs.sendAppLogs("watchface installation : verification failed")
            }
        }
    }

    override fun setWatchFace(watchFace: WatchFace) {
        LOGS.d(watchFace)

        if (!ControlBleTools.getInstance().isConnect) return

//        CallBackUtils.watchFaceInstallCallBack = WatchFaceInstallCallBack {
//            //“WatchFaceInstallResultBean.code” Installation result status code // 0: Verification failed 1: Installation failed 2: Installation succeeded
//            if(it.code == 2)
//                AppLogs.sendAppLogs("watchface installation : success")
//            else if(it.code == 1)
//                AppLogs.sendAppLogs("watchface installation : failed")
//            else if(it.code == 0)
//                AppLogs.sendAppLogs("watchface installation : verification failed")
//        }
        val version = "443"
        val md5 = "1305828"
        val file1 = File(Uri.parse(watchFace.localFilePath).path!!)
        //val md5 = calculateMD5(file1)//"1305828"

        ControlBleTools.getInstance()
            .getDeviceWatchFace(
                watchFace.id.toString(),
                file1.sizeInKb.toInt(),
                true,
                object : DeviceWatchFaceFileStatusListener {
                    override fun onSuccess(statusValue: Int, statusName: String) {
                        LOGS.i("statusName : $statusName")
                        when (statusName) {
                            "READY" -> {
                                val fileByte: ByteArray = file1.readBytes()
                                ControlBleTools.getInstance().startUploadBigData(
                                    BleCommonAttributes.UPLOAD_BIG_DATA_WATCH,
                                    fileByte,
                                    object : UploadBigDataListener {
                                        override fun onSuccess() {
                                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                    WatchUpdateStatus(
                                                        status = UpdateStatus.COMPLETED,
                                                        wStatus = WatchFaceEventsConstants.Complete
                                                    )
                                                )
                                            )

                                            AppLogs.sendAppLogs("watchface transfer : completed")
                                        }

                                        override fun onProgress(
                                            curPiece: Int,
                                            dataPackTotalPieceLength: Int
                                        ) {
                                            val percentage =
                                                curPiece * 100 / dataPackTotalPieceLength
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

                                            LOGS.i("WatchFace : $percentage")
                                        }

                                        override fun onTimeout() {

                                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                                    WatchUpdateStatus(
                                                        status = UpdateStatus.ERROR,
                                                        wStatus = statusName
                                                    )
                                                )
                                            )
                                            AppLogs.sendAppLogs(
                                                LogEvents.WatchFace,
                                                WatchFaceEvents.TransferTimeout
                                            )
                                        }
                                    })
                            }

                            "LOW_BATTERY" -> {
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                        WatchUpdateStatus(
                                            status = UpdateStatus.BATTERY_LOW,
                                            wStatus = statusName
                                        )
                                    )
                                )
                                AppLogs.sendAppLogs(
                                    LogEvents.WatchFace,
                                    WatchFaceEvents.TransferFailed.apply {
                                        comment = "LOW_BATTERY"
                                    })
                            }

                            else -> {


                                var errorMessage = ""
                                if (statusName.lowercase() == "busy") {
                                    errorMessage = "Device is busy. Please try again later"
                                }


                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                        WatchUpdateStatus(
                                            status = UpdateStatus.ERROR,
                                            message = errorMessage,
                                            wStatus = statusName
                                        )
                                    )
                                )

                                AppLogs.sendAppLogs(
                                    LogEvents.WatchFace,
                                    WatchFaceEvents.TransferFailed.apply {
                                        comment = statusName
                                    })
                            }
                            /*"BUSY" -> {
                            }
                            "DOWNGRADE", "DUPLICATED", "LOW_STORAGE" -> {
                            }
                            "LOW_BATTERY" -> {
                            }*/
                        }
                    }

                    override fun timeOut() {
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                WatchUpdateStatus(
                                    status = UpdateStatus.ERROR,
                                    wStatus = "device file status failed"
                                )
                            )
                        )
                        AppLogs.sendAppLogs(
                            LogEvents.WatchFace,
                            WatchFaceEvents.TransferTimeout
                        )
                    }
                })
    }


    override fun setBleCallingSwitch(status: Boolean) {
        val bean = ClassicBluetoothStateBean(status, status)
        ControlBleTools.getInstance()
            .setClassicBluetoothState(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.BleCallingSwitchUpdated(true)
                            )
                            AppLogs.sendAppLogs("BLE calling succeed")
                        }

                        SendCmdState.TIMEOUT -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.BleCallingSwitchUpdated(false)
                            )
                            AppLogs.sendAppLogs("BLE calling timeout")
                        }

                        else -> {}
                    }
                }
            })
    }

    private fun zoomImg(bm: Bitmap?, newWidth: Int, newHeight: Int): Bitmap? {
        return Bitmap.createScaledBitmap(bm!!, newWidth, newHeight, true)
    }

    private fun combineBitmap(background: Bitmap?, foreground: Bitmap?, x: Int, y: Int): Bitmap? {
        if (background == null || foreground == null) {
            return null
        }
        val bgWidth = background.width
        val bgHeight = background.height
        val newmap = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newmap)
        canvas.drawBitmap(background, 0f, 0f, null)
        canvas.drawBitmap(foreground, x.toFloat(), y.toFloat(), null)
        canvas.save()
        canvas.restore()
        return newmap
    }

    private fun getCoverBitmap(
        inputBitmap: Bitmap,
        isRoundDialWatch: Boolean
    ): Bitmap? {

        val width = inputBitmap.width
        val height = inputBitmap.height
        var auxiliaryBitmap =
            if (isRoundDialWatch) {
                ImageUtil.getBitmapFromAsset(com.noisefit_commans.R.drawable.clock_dial_2_1_bg)
            } else {
                ImageUtil.getBitmapFromAsset(com.noisefit_commans.R.drawable.bg_rounded_corner)
            }


        auxiliaryBitmap = zoomImg(auxiliaryBitmap, width, height)
        return combineBitmap(inputBitmap, auxiliaryBitmap, 0, 0)
    }

    override fun setDiyWatchFaceCustom(watchFace: DiyCustomWatchFace) {
        LOGS.d("setDiyWatchFaceCustom $watchFace")
        var isRoundDialWatch = false
        var isRoundedCorner = false
        when (watchFace.screenType.lowercase()) {
            "circular" -> {
                isRoundDialWatch = true
            }

            "arc" -> {
                isRoundedCorner = true
            }
        }

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

            val bgBitmap: Bitmap? = if (isRoundDialWatch) {
                getCoverBitmap(bitmapTemp, true)
            } else if (isRoundedCorner) {
                getCoverBitmap(bitmapTemp, false)
            } else {
                bitmapTemp
            }

            if (bgBitmap == null) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                        WatchUpdateStatus(
                            status = UpdateStatus.ERROR,
                            wStatus = "Bg file missing!!"
                        )
                    )
                )
                return@let
            }

            try {
                ControlBleTools.getInstance().newCustomClockDialData(
                    sourceData, r, g,
                    b, bgBitmap, textBitmap,
                    { data -> uploadWatch(data) }, true
                )

            } catch (exp: Exception) {
                exp.printStackTrace()
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                        WatchUpdateStatus(status = UpdateStatus.ERROR, wStatus = "exception")
                    )
                )
                AppLogs.sendAppLogs("custom watchface : error")
            }

        }
    }


    private fun uploadWatch(data: ByteArray) {
        ControlBleTools.getInstance()
            .getDeviceWatchFace(
                "180",
                data.size,
                true,
                object : DeviceWatchFaceFileStatusListener {
                    override fun onSuccess(statusValue: Int, statusName: String) {
                        when (statusName) {
                            "READY" -> {
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                        WatchUpdateStatus(
                                            status = UpdateStatus.STARTED,
                                            wStatus = WatchFaceEventsConstants.Complete
                                        )
                                    )
                                )
                                sendWatchData(data)
                                AppLogs.sendAppLogs("custom watchface update : started")
                            }

                            else -> {

                                var errorMessage = ""
                                if (statusName.lowercase() == "busy") {
                                    errorMessage = "Device is busy. Please try again later"
                                }
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                        WatchUpdateStatus(
                                            status = UpdateStatus.ERROR,
                                            message = errorMessage,
                                            wStatus = statusName
                                        )
                                    )
                                )

                                AppLogs.sendAppLogs("custom watchface : error")
                            }
                            /*"DUPLICATED" -> {
                            }
                            "LOW_BATTERY" -> {
                            }
                            "BUSY" -> {
                            }
                            "DOWNGRADE" -> {
                            }
                            "LOW_STORAGE" -> {
                            }*/
                        }
                    }

                    override fun timeOut() {
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                                WatchUpdateStatus(
                                    status = UpdateStatus.ERROR,
                                    wStatus = "device watch face failed"
                                )
                            )
                        )
                    }
                })
    }

    private fun sendWatchData(data: ByteArray) {
        ControlBleTools.getInstance().startUploadBigData(
            BleCommonAttributes.UPLOAD_BIG_DATA_WATCH,
            data, object : UploadBigDataListener {
                override fun onSuccess() {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                            WatchUpdateStatus(status = UpdateStatus.COMPLETED)
                        )
                    )
                    AppLogs.sendAppLogs("custom watchface transfer: completed")
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
                            WatchUpdateStatus(status = UpdateStatus.ERROR)
                        )
                    )
                    AppLogs.sendAppLogs("custom watchface : error")
                }
            })
    }


    override fun updateFirmware(fileUri: String) {
        val file1: File = File(Uri.parse(fileUri).path)
        val version = "443"
        val md5 = "1305828"
        ControlBleTools.getInstance()
            .getDeviceLargeFileState(
                true,
                version,
                md5,
                object : DeviceLargeFileStatusListener {
                    override fun onSuccess(statusValue: Int, statusName: String) {
                        when (statusName) {
                            "READY" -> {
                                val fileByte: ByteArray = file1.readBytes()
                                ControlBleTools.getInstance().startUploadBigData(
                                    BleCommonAttributes.UPLOAD_BIG_DATA_OTA,
                                    fileByte,
                                    object : UploadBigDataListener {
                                        override fun onSuccess() {
                                            WatchInfoGlobals.isWatchDataUpdating = false
                                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                                UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                                    WatchUpdateStatus(status = UpdateStatus.COMPLETED)
                                                )
                                            )
                                            AppLogs.sendAppLogs("update firmware : completed")
                                        }

                                        override fun onProgress(
                                            curPiece: Int,
                                            dataPackTotalPieceLength: Int
                                        ) {
                                            val percentage =
                                                curPiece * 100 / dataPackTotalPieceLength
                                            WatchInfoGlobals.isWatchDataUpdating = true
                                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                                UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                                    WatchUpdateStatus(
                                                        status = UpdateStatus.PROGRESS,
                                                        percentagePercentage = percentage
                                                    )
                                                )
                                            )
                                            LOGS.d("firmware_upgrade : $percentage")
                                        }

                                        override fun onTimeout() {
                                            WatchInfoGlobals.isWatchDataUpdating = false
                                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                                UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                                    WatchUpdateStatus(status = UpdateStatus.ERROR)
                                                )
                                            )
                                            AppLogs.sendAppLogs("update firmware : error")
                                        }
                                    })
                            }

                            else -> {
                                WatchInfoGlobals.isWatchDataUpdating = false
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                        WatchUpdateStatus(status = UpdateStatus.ERROR)
                                    )
                                )
                                AppLogs.sendAppLogs("update firmware : error")
                            }
                            /*"BUSY" ->
                            "DOWNGRADE", "DUPLICATED", "LOW_STORAGE" ->
                            "LOW_BATTERY" -> */
                        }
                    }

                    override fun timeOut() {
                        WatchInfoGlobals.isWatchDataUpdating = false
                        testUpdateDeviceDataCallback?.onUpdateDataReceived(
                            UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                                WatchUpdateStatus(status = UpdateStatus.ERROR)
                            )
                        )
                        AppLogs.sendAppLogs("update firmware : timeout error")
                    }
                })
    }

    override fun startCameraMode(status: Boolean) {
        LOGS.d("startCamereMode $status")
        ControlBleTools.getInstance()
            .sendPhonePhotogragh(if (status) 0 else 1, null)
    }


    override fun sendAppNotification(appNotification: AppNotification) {
        LOGS.d("notification $appNotification")
        when (appNotification.appType) {
            ApplicationType.MISSEDCALL.type -> {
                ControlBleTools.getInstance().sendSystemNotification(
                    1,
                    appNotification.number ?: "",
                    appNotification.name ?: appNotification.number ?: "",
                    appNotification.message,
                    null
                )
                AppLogs.sendAppLogs("Missed call app notification")
            }

            ApplicationType.SMS.type -> {
                ControlBleTools.getInstance().sendSystemNotification(
                    2,
                    appNotification.number ?: "",
                    appNotification.name ?: appNotification.number ?: "",
                    appNotification.message,
                    null
                )
                AppLogs.sendAppLogs("SMS app notification")
            }

            else -> {
                val message =
                    dataConverter.formatNotificationMessage(appNotification.message)

                LOGS.d(
                    "sendAppNotification $appNotification ${
                        dataConverter.getPackageName(
                            appNotification.appType
                        )
                    }"
                )
                ControlBleTools.getInstance()
                    .sendAppNotification(
                        appNotification.appType.replace("_", " "),
                        dataConverter.getPackageName(appNotification.appType),
                        appNotification.name ?: appNotification.number ?: "",
                        message,
                        "t", null
                    )
                AppLogs.sendAppLogs("send app notification")
            }
        }
    }

    override fun setWristLiftGesture(wristLiftGesture: WristLiftGesture) {


    }

    override fun setWeatherSwitch(switchSetting: SwitchSetting) {
        ControlBleTools.getInstance().setAppWeatherSwitch(switchSetting.status, null)
    }

    override fun setWeatherData(weatherDataList: List<WeatherData>, unit: String) {
        if (weatherDataList.isEmpty()) return

        val todayWeatherData = weatherDataList[0]

        val bean = WeatherDayBean()
        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis()
        cal.add(Calendar.DAY_OF_MONTH, +1)
        bean.year = cal[Calendar.YEAR]
        bean.month = cal[Calendar.MONTH] + 1
        bean.day = cal[Calendar.DAY_OF_MONTH]
        bean.hour = cal[Calendar.HOUR_OF_DAY]
        bean.minute = cal[Calendar.MINUTE]
        bean.second = cal[Calendar.SECOND]
        bean.cityName = todayWeatherData.city
        bean.locationName = todayWeatherData.country

        var size = weatherDataList.size

        if (size > 6) {
            size = 6

        } else {
            if (size > 5) {
                size = 5
            }
        }

        for (i in 1 until size) {
            val weatherForecast = weatherDataList[i]
            val listBean = WeatherDayBean.Data()
            //listBean.aqi = 80
            listBean.now_temperature = weatherForecast.temp.roundToInt()
            listBean.low_temperature = weatherForecast.tempMin.roundToInt()
            listBean.high_temperature = weatherForecast.tempMax.roundToInt()
            listBean.humidity = weatherForecast.humidity.roundToInt()
            listBean.weather_id = weatherForecast.weatherId ?: 721//Hazy default
            listBean.weather_name =
                weatherForecast.weatherType?.lowercase(DateFormats.defaultLocale)
            listBean.Wind_speed = weatherForecast.windSpeed.roundToInt()
            //listBean.wind_info = 252 + i
            //listBean.Probability_of_rainfall = 4 + i
            listBean.sun_rise = ""
            listBean.sun_set = ""
            bean.list.add(listBean)
        }
        ControlBleTools.getInstance().sendWeatherDailyForecast(bean, null)
        set4DayHourWeather(todayWeatherData)
    }

    private fun set4DayHourWeather(todayWeatherData: WeatherData) {
        val bean = WeatherPerHourBean()
        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis()
        bean.year = cal[Calendar.YEAR]
        bean.month = cal[Calendar.MONTH] + 1
        bean.day = cal[Calendar.DAY_OF_MONTH]
        bean.hour = cal[Calendar.HOUR_OF_DAY]
        bean.minute = cal[Calendar.MINUTE]
        bean.second = cal[Calendar.SECOND]
        bean.cityName = todayWeatherData.city
        bean.locationName = todayWeatherData.country
        for (i in 0..23) {
            val listBean = WeatherPerHourBean.Data()
            listBean.now_temperature = todayWeatherData.temp.roundToInt()
            listBean.low_temperature = todayWeatherData.tempMin.roundToInt()
            listBean.high_temperature = todayWeatherData.tempMax.roundToInt()
            listBean.humidity = todayWeatherData.humidity.roundToInt()
            listBean.weather_id = todayWeatherData.weatherId ?: 721//Hazy default
            listBean.Wind_speed = todayWeatherData.windSpeed.roundToInt()
            listBean.weather_name =
                todayWeatherData.weatherType?.lowercase(DateFormats.defaultLocale)
            //listBean.wind_info = 252 + i
            //listBean.Probability_of_rainfall = 4 + i
            bean.list.add(listBean)
        }
        ControlBleTools.getInstance()
            .sendWeatherPreHour(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            AppLogs.sendAppLogs("send weather/hour : succeed")
                        }

                        SendCmdState.PARAM_ERROR, SendCmdState.UNKNOWN, SendCmdState.FAILED, SendCmdState.TIMEOUT -> {
                            AppLogs.sendAppLogs("send weather/hour : error")
                        }

                        else -> {}
                    }
                }
            })
    }


    override fun setIncomingCallInfo(incomingCall: IncomingCall) {
        LOGS.d("incomingcall $incomingCall")
        when (incomingCall.status) {
            true -> {
//                ControlBleTools.getInstance().sendCallState(0, null)
                ControlBleTools.getInstance().sendSystemNotification(
                    0,
                    incomingCall.number ?: "",
                    incomingCall.name ?: "",
                    incomingCall.name ?: "", null
                )
                AppLogs.sendAppLogs("Incoming call : succeed")
            }

            else -> {
                ControlBleTools.getInstance().sendCallState(1, null)
                AppLogs.sendAppLogs("Incoming call : error")
//                ControlBleTools.getInstance().sendSystemNotification(
//                    1,
//                    incomingCall.number ?: "",
//                    incomingCall.name ?: "",
//                    incomingCall.name ?: ""
//                )

            }
        }
    }

    override fun updateDND(doNotDisturb: DoNotDisturb) {
        LOGS.d(doNotDisturb)

        val doNotDisturbMode = DoNotDisturbModeBean()
        try {
            doNotDisturbMode.isSwitch = doNotDisturb.status
            doNotDisturbMode.isSmartSwitch = CommonGlobals.smartDndSwitch
            doNotDisturbMode.startTime = SettingTimeBean(21, 0)
            doNotDisturbMode.endTime = SettingTimeBean(6, 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return
        }
        ControlBleTools.getInstance()
            .setDoNotDisturbMode(doNotDisturbMode, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.DoNotDisturbUpdated(
                                    true
                                )
                            )
                            AppLogs.sendAppLogs("DND Reminder : Succeed")
                        }

                        else -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.DoNotDisturbUpdated(
                                    false
                                )
                            )
                            AppLogs.sendAppLogs("DND Reminder : Failed")
                        }
                    }
                }
            }
            )

    }

    override fun setHeartRateInterval(heartRateInterval: HeartRateInterval) {

    }


    override fun setSedentaryData(sedentaryData: SedentaryData) {
        LOGS.d(sedentaryData)

        val reminder = CommonReminderBean()
        reminder.isOn = sedentaryData.status
        reminder.noDisturbInLaunch = true
        try {
            val stH: Int = sedentaryData.startHour
            val stM: Int = sedentaryData.startMinute
            val etH: Int = sedentaryData.endHour
            val etM: Int = sedentaryData.endMinute
            val frequency: Int = sedentaryData.interval * 60
            reminder.startTime = SettingTimeBean(stH, stM)
            reminder.endTime = SettingTimeBean(etH, etM)
            reminder.frequency = frequency
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        ControlBleTools.getInstance()
            .setSedentaryReminder(reminder, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.SedentaryDataUpdated(
                                    true
                                )
                            )
                            AppLogs.sendAppLogs("SedentaryDataUpdated Reminder : Succeed")
                        }

                        else -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.SedentaryDataUpdated(
                                    false
                                )
                            )
                            AppLogs.sendAppLogs("SedentaryDataUpdated Reminder: Failed")
                        }
                    }
                }
            })
    }


    override fun setAutoSleep(autoSleep: AutoSleep) {

    }

    override fun setSwitchSetting(switchSetting: SwitchSetting) {

    }


    override fun setScreenAwakeInterval(interval: Int) {
    }


    override fun setHandWashing(handWashing: HandWashing) {

        val reminder = CommonReminderBean()
        reminder.isOn = handWashing.startWash
        reminder.noDisturbInLaunch = true
        try {
            val stH: Int = handWashing.startHour
            val stM: Int = handWashing.startMinute
            val etH: Int = handWashing.endHour
            val etM: Int = handWashing.endMinute
            val frequency: Int = handWashing.frequency * 60
            reminder.startTime = SettingTimeBean(stH, stM)
            reminder.endTime = SettingTimeBean(etH, etM)
            reminder.frequency = frequency
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        ControlBleTools.getInstance()
            .setWashHandReminder(reminder, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.HandWashingUpdated(
                                    true
                                )
                            )
                            AppLogs.sendAppLogs("HandWashingUpdated Reminder: Succeed")
                        }

                        else -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.HandWashingUpdated(
                                    false
                                )
                            )
                            AppLogs.sendAppLogs("HandWashingUpdated Reminder: Failed")

                        }
                    }
                }
            })
    }

    override fun updateAPGSData(data1: Uri, data2: Uri) {
        val file1: File = File(data1.path)
        val version = "443"
        val md5 = "1305828"
        ControlBleTools.getInstance()
            .getDeviceLargeFileState(
                true,
                version,
                md5,
                object : DeviceLargeFileStatusListener {
                    override fun onSuccess(statusValue: Int, statusName: String) {
                        when (statusName) {
                            "READY" -> {
                                val fileByte: ByteArray = file1.readBytes()
                                ControlBleTools.getInstance().startUploadBigData(
                                    BleCommonAttributes.UPLOAD_BIG_DATA_LTO,
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
                                                    UpdateStatus.COMPLETED
                                                )
                                            )
                                        }

                                        override fun onProgress(
                                            curPiece: Int,
                                            dataPackTotalPieceLength: Int
                                        ) {//todo here
                                            WatchInfoGlobals.isWatchDataUpdating = true


                                            val percentage =
                                                curPiece * 100 / dataPackTotalPieceLength
                                            LOGS.d(
                                                "noise_fit_event:noisefit_nav+",
                                                "updateAPGSData $percentage"
                                            )

                                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                                UpdateDeviceDataCallback.AGPSUpdateProgress(
                                                    UpdateStatus.PROGRESS,
                                                    progress = percentage
                                                )
                                            )

                                        }

                                        override fun onTimeout() {
                                            WatchInfoGlobals.isWatchDataUpdating = false
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

                            else -> {
                                WatchInfoGlobals.isWatchDataUpdating = false
                                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                    UpdateDeviceDataCallback.AGPSUpdateProgress(
                                        status = UpdateStatus.ERROR
                                    )
                                )
                                AppLogs.sendAppLogs(
                                    LogEvents.Agps,
                                    AgpsEvents.Other
                                )
                            }
                            /*"BUSY" ->
                            "DOWNGRADE", "DUPLICATED", "LOW_STORAGE" ->
                            "LOW_BATTERY" -> */
                        }
                    }

                    override fun timeOut() {
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

    override fun setHeartRateAlert(heartRateAlert: HeartRateAlert) {
        LOGS.d("setHeartRateAlert 1212")
        val heartRateMonitorBean = HeartRateMonitorBean()
        if (heartRateAlert.status) {
            heartRateMonitorBean.mode = 0
            heartRateMonitorBean.isWarning = true
        } else {
            heartRateMonitorBean.mode = 1
            heartRateMonitorBean.isWarning = false
        }
        heartRateMonitorBean.frequency = 5
        heartRateMonitorBean.warningValue = heartRateAlert.max_hr


        ControlBleTools.getInstance().setHeartRateMonitor(
            heartRateMonitorBean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            LOGS.d("setHeartRateAlert success")
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.HeartRateAlertUpdated(
                                    true
                                )
                            )
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.HeartRateMeasureIntervalSet(
                                    true
                                )
                            )
                            AppLogs.sendAppLogs("HeartRateAlert : Succeed")
                        }

                        else -> {
                            LOGS.d("setHeartRateAlert failed")
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.HeartRateAlertUpdated(
                                    false
                                )
                            )
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.HeartRateMeasureIntervalSet(
                                    false
                                )
                            )
                            AppLogs.sendAppLogs("HeartRateAlert : Failed")
                        }
                    }
                }
            })
    }


    override fun setFactoryReset() {
        ControlBleTools.getInstance().unbindDeviceWaitConfirmation(object : SendCmdStateListener() {
            override fun onState(state: SendCmdState) {
                if (state == SendCmdState.SUCCEED) {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.FactoryReset(
                            true
                        )
                    )
                } else {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.FactoryReset(
                            false
                        )
                    )
                }
            }
        })
    }

    override fun setRestartDevice() {
    }

    override fun setDrinkWaterReminder(sedentaryData: SedentaryData) {
        LOGS.d(sedentaryData)

        val reminder = CommonReminderBean()
        reminder.isOn = sedentaryData.status
        reminder.noDisturbInLaunch = true
        try {
            val stH: Int = sedentaryData.startHour
            val stM: Int = sedentaryData.startMinute
            val etH: Int = sedentaryData.endHour
            val etM: Int = sedentaryData.endMinute
            val frequency: Int = sedentaryData.interval * 60
            reminder.startTime = SettingTimeBean(stH, stM)
            reminder.endTime = SettingTimeBean(etH, etM)
            reminder.frequency = frequency
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        ControlBleTools.getInstance()
            .setDrinkWaterReminder(reminder, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.DrinkWaterUpdated(
                                    true
                                )
                            )
                            AppLogs.sendAppLogs("DrinkWater Alert: Succeed")
                        }

                        else -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.DrinkWaterUpdated(
                                    false
                                )
                            )
                            AppLogs.sendAppLogs("DrinkWater Alert: Failed")
                        }
                    }
                }
            })
    }

    override fun setMedicineReminder(sedentaryData: SedentaryData) {
        val reminder = CommonReminderBean()
        reminder.isOn = sedentaryData.status
        reminder.noDisturbInLaunch = true
        try {
            val stH: Int = sedentaryData.startHour
            val stM: Int = sedentaryData.startMinute
            val etH: Int = sedentaryData.endHour
            val etM: Int = sedentaryData.endMinute
            val frequency: Int = sedentaryData.interval * 60
            reminder.startTime = SettingTimeBean(stH, stM)
            reminder.endTime = SettingTimeBean(etH, etM)
            reminder.frequency = frequency
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        ControlBleTools.getInstance()
            .setMedicationReminder(reminder, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.MedicineDataUpdated(
                                    true
                                )
                            )
                            AppLogs.sendAppLogs("MedicineReminder Alert: Succeed")
                        }

                        else -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.MedicineDataUpdated(
                                    false
                                )
                            )
                            AppLogs.sendAppLogs("MedicineReminder Alert: Failed")
                        }
                    }
                }
            })
    }

    override fun setMealReminder(sedentaryData: SedentaryData) {

        val reminder = CommonReminderBean()
        reminder.isOn = sedentaryData.status
        reminder.noDisturbInLaunch = true
        try {
            val stH: Int = sedentaryData.startHour
            val stM: Int = sedentaryData.startMinute
            val etH: Int = sedentaryData.endHour
            val etM: Int = sedentaryData.endMinute
            val frequency: Int = sedentaryData.interval * 60
            reminder.startTime = SettingTimeBean(stH, stM)
            reminder.endTime = SettingTimeBean(etH, etM)
            reminder.frequency = frequency
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        ControlBleTools.getInstance()
            .setHaveMealsWaterReminder(reminder, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.MealDataUpdated(
                                    true
                                )
                            )
                            AppLogs.sendAppLogs("MealReminder: Succeed")
                        }

                        else -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.MealDataUpdated(
                                    false
                                )
                            )
                            AppLogs.sendAppLogs("MealReminder: Failed")
                        }
                    }
                }
            })
    }

    override fun updateCustomReply(customReplyData: CustomReplyData) {


        val replys = ArrayList<String>()
        customReplyData.customReplies.forEach { item ->
            replys.add(item.content ?: "")
        }
        ControlBleTools.getInstance()
            .setDevShortReplyData(replys, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.CustomizeReplyUpdated(
                                    true
                                )
                            )
                            AppLogs.sendAppLogs("CustomReply Reminder: Succeed")
                        }

                        else -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.CustomizeReplyUpdated(
                                    false
                                )
                            )
                            AppLogs.sendAppLogs("CustomReply Reminder: Failed")
                        }
                    }
                }
            })


    }


    private fun initSingleQuickReplyListener() {

    }

    private fun initDeviceSendMuteListener() {

    }

    override fun setWorldClock(data: WorldClocksPushData) {
        val list = ArrayList<WorldClockBean>()
        data.worldClocks.forEach { item ->
            val clock = WorldClockBean()
            clock.cityName = item.content
            clock.offset = item.timeZone
            list.add(clock)
        }
        ControlBleTools.getInstance()
            .setWorldClockList(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.WorldClockSet(
                                    true
                                )
                            )
                            AppLogs.sendAppLogs("WorldClock Reminder: Succeed")
                        }

                        SendCmdState.TIMEOUT -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.WorldClockSet(
                                    false
                                )
                            )
                            AppLogs.sendAppLogs("WorldClock Reminder: Failed")
                        }

                        else -> {}
                    }
                }
            })
    }

    override fun setTemperatureUnit(unit: String) {

        var unitInImperial = 0
        if (unit.equals(Units.IMPERIAL.name, true)) {
            unitInImperial = 1
        }

        ControlBleTools.getInstance().setTemperatureUnit(unitInImperial, null)

    }

    override fun setQuickEyeMovementSwitch(status: Boolean) {
        ControlBleTools.getInstance().setRapidEyeMovement(
            status,
            object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    when (state) {
                        SendCmdState.SUCCEED -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.QuickEyeMovementSwitchUpdated(
                                    true
                                )
                            )
                            AppLogs.sendAppLogs("QuickEyeMovement Reminder: Failed")
                        }

                        else -> {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.QuickEyeMovementSwitchUpdated(
                                    false
                                )
                            )
                            AppLogs.sendAppLogs("QuickEyeMovement Reminder: Failed")
                        }
                    }
                }
            })

    }

    private fun initQuickEyeListener() {

    }

}