package com.noisefit_evolve2.handler

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.noisefit_commans.common.convertCorner
import com.noisefit_commans.constants.WatchFaceEventsConstants
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.enums.ApplicationType
import com.noisefit_commans.interfaces.device_data.IUpdateDeviceDataCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallbacks
import com.noisefit_commans.models.AlarmAction
import com.noisefit_commans.models.AlarmsList
import com.noisefit_commans.models.AppNotification
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.Contact
import com.noisefit_commans.models.CustomReplyData
import com.noisefit_commans.models.DeviceLanguage
import com.noisefit_commans.models.DeviceUnits
import com.noisefit_commans.models.DiyCustomWatchFace
import com.noisefit_commans.models.DoNotDisturb
import com.noisefit_commans.models.Gender
import com.noisefit_commans.models.HeartRateAlert
import com.noisefit_commans.models.HeartRateInterval
import com.noisefit_commans.models.IncomingCall
import com.noisefit_commans.models.Language
import com.noisefit_commans.models.MenstrualData
import com.noisefit_commans.models.ReminderList
import com.noisefit_commans.models.SedentaryData
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
import com.noisefit_commans.models.WristLiftGesture
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LogEvents
import com.noisefit_commans.utils.OtaEvents
import com.noisefit_commans.utils.WatchFaceEvents
import com.noisefit_evolve2.base.Evolve2ApplicationHandler
import com.noisefit_evolve2.dataConversion.DataConverter
import com.touchgui.sdk.TGCallback
import com.touchgui.sdk.TGClient
import com.touchgui.sdk.TGCloudDial
import com.touchgui.sdk.TGDialManager
import com.touchgui.sdk.TGDialManager.OnSyncDialListener
import com.touchgui.sdk.TGLanguage
import com.touchgui.sdk.TGOTACallback
import com.touchgui.sdk.TGOTAManager
import com.touchgui.sdk.TGPhotoDialBuilder
import com.touchgui.sdk.bean.TGAlarm
import com.touchgui.sdk.bean.TGContacts
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
import com.touchgui.sdk.bean.TGTarget
import com.touchgui.sdk.bean.TGUnitConfig
import com.touchgui.sdk.bean.TGWeather
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlin.math.roundToInt


const val AUTO_MODE = 0x88
private const val HAND_MODE = 0xAA
private const val CLOSE = 0x55

class Evolve2UpdateDeviceUnitsHandler
@Inject
constructor(
    private var dataConverter: DataConverter,
    private var context: Context,
    private var evolve2ApplicationHandler: Evolve2ApplicationHandler,
    private val watchDataStore: WatchDataStore
) : UpdateDeviceDataActions() {

    companion object {
        var mobileNumber: String? = null
        var lastMapsNotification: String? = null
    }

    private var updateDeviceDataCallbacks: UpdateDeviceDataCallbacks? = null

    private var mClient: TGClient? = null
    private var colorFitDevice: ColorFitDevice? = null

    private var testUpdateDeviceDataCallback: IUpdateDeviceDataCallback? = null

    override fun init() {
        super.init()
        mClient = evolve2ApplicationHandler.getTGBleClient()
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


    override fun setMusicSwitch(musicSwitch: SwitchSetting) {
        if (mClient != null) {
            mClient?.commandBuilder?.setMusicOnOff(musicSwitch.status)?.execute(object :
                TGCallback<Void> {
                override fun onSuccess(p0: Void?) {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.MusicSwitchUpdated(
                            true
                        )
                    )

                }

                override fun onFailure(p0: Throwable?) {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.MusicSwitchUpdated(
                            false
                        )
                    )
                }
            })
        }
    }

    override fun setDeviceUnits(units: DeviceUnits) {

        val config = TGUnitConfig()
        config.distance = when (units.unitSystem) {
            UnitSystem.IMPERIAL.type -> TGUnitConfig.IMPERIAL_SYSTEM
            else -> TGUnitConfig.METRIC_SYSTEM
        }
        config.weight = when (units.unitSystem?.lowercase()) {
            UnitSystem.IMPERIAL.type.lowercase() -> TGUnitConfig.IMPERIAL_SYSTEM
            else -> TGUnitConfig.METRIC_SYSTEM
        }

        // config.language = TGLanguage.en
        /*config.temp = when (units.unitSystem) {
            UnitSystem.IMPERIAL.type -> TGUnitConfig.FAHRENHEIT
            else -> TGUnitConfig.CELSIUS
        }*/

        val sdfZone = SimpleDateFormat("Z", DateFormats.defaultLocale)
        val timezone = sdfZone.format(Date())

        if (timezone.length < 4) {
            return
        }
        mClient?.commandBuilder?.setUnit(config)?.execute(object :
            TGCallback<Void> {
            override fun onSuccess(p0: Void?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.DeviceUnitsUpdated(
                        true
                    )
                )
            }

            override fun onFailure(p0: Throwable?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.DeviceUnitsUpdated(
                        false
                    )
                )

            }
        })
    }


    private fun setDeviceUnitsNew(units: TimeFormat) {

        LOGS.d("INSIDE SET DEVICE TIME")

        mClient?.commandBuilder?.syncTime()?.execute(object : TGCallback<Void> {
            override fun onSuccess(p0: Void?) {
//                testUpdateDeviceDataCallback?.onUpdateDataReceived(
//                    UpdateDeviceDataCallback.DeviceTimeSynced(
//                        true
//                    )
//                )

            }

            override fun onFailure(p0: Throwable?) {
//                testUpdateDeviceDataCallback?.onUpdateDataReceived(
//                    UpdateDeviceDataCallback.DeviceTimeSynced(
//                        false
//                    )
//                )
            }
        })

        units.timeFormat?.let {
            val config = TGUnitConfig()
            config.timeMode = when (it.lowercase()) {
                TimeFormats.HOURS_12.type.lowercase() -> TGUnitConfig.HOUR12
                else -> TGUnitConfig.HOUR24
            }

            val sdfZone = SimpleDateFormat("Z", DateFormats.defaultLocale)
            val timezone = sdfZone.format(Date())

            if (timezone.length < 4) {
                return
            }
            mClient?.commandBuilder?.setUnit(config)?.execute(object :
                TGCallback<Void> {
                override fun onSuccess(p0: Void?) {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DeviceTimeSynced(
                            true
                        )
                    )
                    //baseUpdateDeviceDataCallbacks?.onDeviceUnitsUpdated(true)
                }

                override fun onFailure(p0: Throwable?) {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.DeviceTimeSynced(
                            false
                        )
                    )
                    //baseUpdateDeviceDataCallbacks?.onDeviceUnitsUpdated(false)
                }
            })


        }


    }


    override fun setUserInfo(userInfo: UserInfo, userGoals: UserGoals, userName: String?) {


        try {

            val profile = TGProfile()
            profile.weight = userInfo.weight
            profile.height = userInfo.height
            profile.birthday = DateFormats.dateFormat3.parse(userInfo.dob)
            profile.gender = when (userInfo.gender.lowercase()) {
                Gender.FEMALE.type.lowercase() -> {
                    TGProfile.FEMALE
                }
                else -> {
                    TGProfile.MAN
                }
            }
            mClient?.commandBuilder?.setProfile(profile)?.execute(object :
                TGCallback<Void> {
                override fun onSuccess(p0: Void?) {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.UserInfoUpdated(
                            true
                        )
                    )

                }

                override fun onFailure(p0: Throwable?) {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.UserInfoUpdated(
                            false
                        )
                    )
                }

            })

            val target = TGTarget()
            target.distance = userGoals.distanceGoal
            target.step = userGoals.stepGoal
            target.calorie = userGoals.caloriesGoal
            target.sleepHour = 8
            target.sleepMinute = 0
            mClient?.commandBuilder?.setTarget(target)?.execute(object :
                TGCallback<Void> {
                override fun onSuccess(p0: Void?) {


                }

                override fun onFailure(p0: Throwable?) {

                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun updateAlarm(alarm: AlarmsList, alarmAction: AlarmAction) {

        val listData = ArrayList<TGAlarm>()
        alarm.alarms?.forEachIndexed { _, item ->

            val subList = ArrayList(item.repeatDays!!.subList(0, item.repeatDays!!.size))
            subList.reverse()
            val mAlarmInfo1 = TGAlarm()
            mAlarmInfo1.id = item.id
            mAlarmInfo1.hour = item.hour
            mAlarmInfo1.minute = item.minute
            mAlarmInfo1.isShow = alarmAction != AlarmAction.ALARM_DELETE
            mAlarmInfo1.repeat = dataConverter.booleanArrayToBinaryString(subList)

            listData.add(mAlarmInfo1)
        }
        mClient?.commandBuilder?.setAlarms(listData)?.execute(object : TGCallback<Int> {
            override fun onSuccess(p0: Int?) {
                mClient?.commandBuilder?.syncAlarms()
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.AlarmUpdated(
                        true
                    )
                )

            }

            override fun onFailure(p0: Throwable?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.AlarmUpdated(
                        false
                    )
                )
            }

        })

    }


    override fun deleteAlarm(alarm: AlarmsList) {


        val listData = ArrayList<TGAlarm>()
        alarm.alarms?.forEach { item ->
            val subList = ArrayList(item.repeatDays!!.subList(0, item.repeatDays!!.size))
            subList.reverse()
            val mAlarmInfo1 = TGAlarm()
            mAlarmInfo1.id = item.id
            mAlarmInfo1.hour = item.hour
            mAlarmInfo1.minute = item.minute
            mAlarmInfo1.repeat = dataConverter.booleanArrayToBinaryString(subList)
            mAlarmInfo1.isShow = item.status
            listData.add(mAlarmInfo1)
        }
        mClient?.commandBuilder?.setAlarms(listData)?.execute(object : TGCallback<Int> {
            override fun onSuccess(p0: Int?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.AlarmUpdated(
                        true
                    )
                )
            }

            override fun onFailure(p0: Throwable?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.AlarmUpdated(
                        false
                    )
                )
            }

        })

    }

    override fun deleteReminders(reminderList: ReminderList) {

    }

    override fun updateMenstrualData(menstrualData: MenstrualData) {
        LOGS.d("menstrula set",Gson().toJson(menstrualData))
        val config = TGPhysiologicalCycle()
        config.isEnable = menstrualData.status
        config.lastDate = DateFormats.dateFormat.parse(menstrualData.lastMenstrualDate)
        config.menstrualCycleDays = menstrualData.menstrualCycleLength
        config.menstrualDuration = menstrualData.menstrualLength
        config.remindMenstrual = menstrualData.menstrualReminder?.remindStartDayBefore!!
        config.remindOvulation = menstrualData.menstrualReminder?.remindOvulationDayBefore!!
        val calendar = Calendar.getInstance()
        menstrualData.menstrualReminder?.reminderTime?.let {
            calendar.time = DateFormats.timeFormat.parse(it)
        }

        config.remindHour = calendar.get(Calendar.HOUR_OF_DAY)
        config.remindMinute = calendar.get(Calendar.MINUTE)
        mClient?.commandBuilder?.setPhysiologicalCycle(config)?.execute(object :
            TGCallback<Void> {
            override fun onSuccess(p0: Void?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.MenstrualDataUpdated(
                        true
                    )
                )
            }

            override fun onFailure(p0: Throwable?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.MenstrualDataUpdated(
                        false
                    )
                )

            }
        })
    }

    override fun addReminder(reminder: ReminderList.Reminder) {

    }

    override fun updateLanguage(language: Language) {

        val config = TGUnitConfig()
        when (language.language) {
            DeviceLanguage.HINDI.type -> {
                config.language = TGLanguage.hi

            }
            else -> config.language = TGLanguage.en
        }
        mClient?.commandBuilder?.setUnit(config)?.execute(object :
            TGCallback<Void> {
            override fun onSuccess(p0: Void?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.LanguageUpdated(
                        true
                    )
                )
            }

            override fun onFailure(p0: Throwable?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.LanguageUpdated(
                        false
                    )
                )

            }
        })
    }


    override fun findDevice(findDevice: SwitchSetting) {

        findPhoneListener()

    }

    private fun findPhoneListener() {

        mClient?.commandBuilder?.setFindPhoneOnOff(true, 10)?.execute(object :
            TGCallback<Void> {
            override fun onSuccess(p0: Void?) {

            }

            override fun onFailure(p0: Throwable?) {

            }

        })
    }

    override fun setDeviceDateTime(calender: Calendar, units: TimeFormat) {
        setDeviceUnitsNew(units)
    }


    private val syncDialProcessListener: OnSyncDialListener = object : OnSyncDialListener {
        override fun onProgress(p0: Int) {
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                    WatchUpdateStatus(status = UpdateStatus.PROGRESS, percentagePercentage = p0)
                )
            )

        }

        override fun onCompleted() {
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                    WatchUpdateStatus(status = UpdateStatus.COMPLETED,
                    wStatus = WatchFaceEventsConstants.Complete)
                )
            )

        }

        override fun onError(p0: Throwable?) {
            p0?.printStackTrace()
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                    WatchUpdateStatus(status = UpdateStatus.ERROR,
                    wStatus = p0?.message)
                )
            )
            AppLogs.sendAppLogs(
                LogEvents.WatchFace,
                WatchFaceEvents.TransferFailed.apply {
                    comment = "${p0?.message}"
                }
            )
        }
    }


    override fun setWatchFace(watchFace: WatchFace) {
        try {
            mClient?.dialManager?.removeOnSyncDialListener(syncDialProcessListener)
            mClient?.dialManager?.addOnSyncDialListener(syncDialProcessListener)

            val filePath = watchFace.localFilePath.split("///").last()


            mClient?.dialManager?.syncDial(TGCloudDial(watchFace.id!!, filePath))
        } catch (e: Exception) {
            e.printStackTrace()
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                    WatchUpdateStatus(
                        status = UpdateStatus.ERROR,
                        wStatus = e.message
                    )
                )
            )
        }


    }


    override fun updateFirmware(fileUri: String) {
        val filePath = fileUri.split("///").last()
        val manager: TGOTAManager? = mClient?.otaManager
        manager?.setCallback(object : TGOTACallback {
            override fun onProgress(p0: Int) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                        WatchUpdateStatus(status = UpdateStatus.PROGRESS, percentagePercentage = p0)
                    )
                )
            }

            override fun onCompleted() {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                        WatchUpdateStatus(status = UpdateStatus.COMPLETED)
                    )
                )

            }

            override fun onError(p0: Throwable?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                        WatchUpdateStatus(status = UpdateStatus.ERROR)
                    )
                )
                AppLogs.sendAppLogs(
                    LogEvents.Ota,
                    OtaEvents.TransferFailed.apply {
                        comment = "${p0?.message}"
                    }
                )
            }
        })
        manager?.start(filePath, false)
    }

    override fun setContactList(contactList: List<Contact>) {
        LOGS.d("contacts list::$contactList")
        try {
            val contactBeans = java.util.ArrayList<TGContacts>()
            contactList.forEach {
                val contact = TGContacts()
                contact.name = it.name
                contact.phoneNum = it.number.get(0)
                contactBeans.add(contact)
            }
            mClient?.commandBuilder?.setContacts(contactBeans)?.execute(object :TGCallback<Int>{
                override fun onSuccess(p0: Int?) {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.ContactListUpdated(
                            true
                        )
                    )
                }

                override fun onFailure(p0: Throwable?) {

                }
            })

        } catch (e: Exception) {

        }
    }

    override fun startCameraMode(status: Boolean) {
        mClient?.commandBuilder?.setCameraOnOff(status)?.execute(object : TGCallback<Void> {
            override fun onSuccess(p0: Void?) {
                if (status) {
                    colorFitDevice?.address?.let { address ->
                        val device: BluetoothDevice =
                            BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address)
                        device.createBond()
                    }

                }

                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.CameraModeUpdate(
                        true
                    )
                )

            }

            override fun onFailure(p0: Throwable?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.CameraModeUpdate(
                        false
                    )
                )
            }

        })

    }




    override fun sendAppNotification(appNotification: AppNotification) {


        LOGS.d("MAPS_DATA $lastMapsNotification")
        if (appNotification.appType == ApplicationType.GOOGLE_MAPS.type) {
            if (lastMapsNotification != null && lastMapsNotification == appNotification.message) {
                return
            } else {
                lastMapsNotification = appNotification.message
            }
        }

        val tgMessage = TGMessage()
        tgMessage.type = when (appNotification.appType) {
            ApplicationType.SMS.type -> TGMessage.MMS
            ApplicationType.WHATS_APP.type -> TGMessage.WHATSAPP
            ApplicationType.LINKED_IN.type -> TGMessage.LINK
            ApplicationType.INSTAGRAM.type -> TGMessage.INSTAGRAM
            ApplicationType.FB_MESSENGER.type -> TGMessage.FACEBOOK
            ApplicationType.SKYPE.type -> TGMessage.SKYPE
            ApplicationType.TWITTER.type -> TGMessage.TWITTER
            ApplicationType.FACEBOOK.type -> TGMessage.FACEBOOK
            ApplicationType.VIBER.type -> TGMessage.VIBER
            ApplicationType.GMAIL.type -> TGMessage.GMAIL
            ApplicationType.OUTLOOK.type -> TGMessage.OUTLOOK
            ApplicationType.SNAPCHAT.type -> TGMessage.SNAPCHAT
            ApplicationType.CALENDAR.type -> TGMessage.CALENDAR
            ApplicationType.TELEGRAM.type -> TGMessage.TELEGRAM
            ApplicationType.SPORT_EVENT.type -> TGMessage.COMMON_MSG
            ApplicationType.WE_CHAT.type -> TGMessage.WECHAT
            ApplicationType.GOOGLE_MAPS.type -> TGMessage.GOOGLE_MAPS
            else -> TGMessage.COMMON_MSG
        }
        tgMessage.content = appNotification.message
        tgMessage.name = appNotification.name
        tgMessage.phoneNumber = appNotification.number
        mClient?.commandBuilder?.syncMessage(tgMessage)?.execute(object : TGCallback<Int> {
            override fun onFailure(p0: Throwable?) {

            }

            override fun onSuccess(p0: Int?) {

            }

        })


    }

    override fun setWristLiftGesture(wristLiftGesture: WristLiftGesture) {
        val config = TGRaiseWristConfig()
        config.isOn = wristLiftGesture.status
        config.startHour = wristLiftGesture.startHour
        config.startMinute = wristLiftGesture.startMinute
        config.stopHour = wristLiftGesture.endHour
        config.stopMinute = wristLiftGesture.endMinute
        config.isHasRange = true
        mClient?.commandBuilder?.setRaiseWrist(config)?.execute(object :
            TGCallback<Void> {
            override fun onSuccess(p0: Void?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.WristLiftGestureUpdated(
                        true
                    )
                )
            }

            override fun onFailure(p0: Throwable?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.WristLiftGestureUpdated(
                        false
                    )
                )
            }
        })

    }


    override fun setWeatherSwitch(switchSetting: SwitchSetting) {
        mClient?.commandBuilder?.setWeatherOnOff(switchSetting.status)
            ?.execute(object : TGCallback<Void> {
                override fun onSuccess(p0: Void?) {
                }

                override fun onFailure(p0: Throwable?) {
                }

            })
    }

    override fun setWeatherData(weatherDataList: List<WeatherData>, unit: String) {
        if (weatherDataList.isNotEmpty()) {
            if (mClient != null) {
                weatherDataList[0].city?.let {
                    mClient?.commandBuilder?.setCity(it)?.execute(object : TGCallback<Void> {
                        override fun onSuccess(p0: Void?) {
                            val weatherData = weatherDataList[0]
                            val weather = TGWeather()
                            weather.currentTemp = weatherData.temp.roundToInt()
                            weather.maxTemp = weatherData.tempMax.roundToInt()
                            weather.minTemp = weatherData.tempMin.roundToInt()
                            weather.humidity = weatherData.humidity.roundToInt()
                            weatherData.weatherType?.let {
                                weather.type = getWeatherType(it.lowercase())
                            }
                            val futureList = ArrayList<TGWeather.FutureWeather>()

                            val future1 = TGWeather.FutureWeather()
                            var weatherData1 = weatherDataList[1]
                            future1.maxTemp = weatherData1.tempMax.roundToInt()
                            future1.minTemp = weatherData1.tempMin.roundToInt()

                            weatherData1.weatherType?.let {
                                future1.type = getWeatherType(it.lowercase())
                            }

                            futureList.add(future1)

                           // future1 = TGWeather.FutureWeather()
                            weatherData1 = weatherDataList[2]
                            future1.maxTemp = weatherData1.tempMax.roundToInt()
                            future1.minTemp = weatherData1.tempMin.roundToInt()
                            weatherData1.weatherType?.let {
                                future1.type = getWeatherType(it.lowercase())
                            }
                            futureList.add(future1)

                           // future1 = TGWeather.FutureWeather()
                            weatherData1 = weatherDataList[3]
                            future1.maxTemp = weatherData1.tempMax.roundToInt()
                            future1.minTemp = weatherData1.tempMin.roundToInt()
                            weatherData1.weatherType?.let {
                                future1.type = getWeatherType(it.lowercase())
                            }
                            futureList.add(future1)
                            weather.futureWeatherList = futureList

                            mClient?.commandBuilder?.setWeather(weather)?.execute(object :
                                TGCallback<Void> {
                                override fun onSuccess(p0: Void?) {
                                    setTemperatureUnit(unit)
                                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                        UpdateDeviceDataCallback.WeatherSwitchUpdated(
                                            true
                                        )
                                    )

                                }

                                override fun onFailure(p0: Throwable?) {
                                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                        UpdateDeviceDataCallback.WeatherSwitchUpdated(
                                            false
                                        )
                                    )
                                }

                            })
                        }

                        override fun onFailure(p0: Throwable?) {
                            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                                UpdateDeviceDataCallback.WeatherSwitchUpdated(
                                    false
                                )
                            )
                        }
                    })
                }

            }
        }
    }

    private fun getWeatherType(weatherType: String): Int {
        when (weatherType) {
            "clear" -> {
                return TGWeather.SUNNY
            }
            "clouds" -> {
                return TGWeather.CLOUDY //Partly Cloudy
            }
            "thunderstorm" -> {
                return TGWeather.THUNDER_SHOWER // shower
            }
            "drizzle" -> {
                return TGWeather.SHOWER //Drizzle
            }
            "rain" -> {
                return TGWeather.RAINY //Shower
            }
            "snow" -> {
                return TGWeather.SNOWY //Snowfall
            }
            "haze", "smoke", "dust", "fog" -> {
                return TGWeather.MIST //Haze
            }
            "sand" -> {
                return TGWeather.SANDSTORM //Dust
            }
            "tornado" -> {
                return TGWeather.TYPHOON //Tornado
            }

            else -> {
                return TGWeather.MIST //(253 for unknown type currently set to clear)
            }
        }
    }

    override fun setIncomingCallInfo(incomingCall: IncomingCall) {

        when (incomingCall.status) {
            true -> {

                mClient?.commandBuilder?.remindCall(incomingCall.name, incomingCall.number)
                    ?.execute(object :
                        TGCallback<Void> {
                        override fun onSuccess(p0: Void?) {
                            mobileNumber = incomingCall.number
                        }

                        override fun onFailure(p0: Throwable?) {

                        }

                    })
            }

            else -> {

                val status = 2 // refuse
                //val status = 1  // Answer
                mClient?.commandBuilder?.syncCallStatus(status)?.execute(object :
                    TGCallback<Void> {
                    override fun onSuccess(p0: Void?) {

                    }

                    override fun onFailure(p0: Throwable?) {

                    }

                })
            }

        }
    }

    override fun updateDND(doNotDisturb: DoNotDisturb) {

        val config = TGNotDisturbConfig()
        config.isOn = doNotDisturb.status
        config.startHour = 0
        config.startMinute = 0
        config.stopHour = 23
        config.stopMinute = 59

        mClient?.commandBuilder?.setNotDisturbMode(config)?.execute(object :
            TGCallback<Void> {
            override fun onSuccess(p0: Void?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.DoNotDisturbUpdated(
                        true
                    )
                )
            }

            override fun onFailure(p0: Throwable?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.DoNotDisturbUpdated(
                        false
                    )
                )
            }
        })

    }

    override fun setHeartRateInterval(heartRateInterval: HeartRateInterval) {

        val heartRate = TGHeartRateMonitoringModeConfig()
        heartRate.interval = heartRateInterval.interval

        if (heartRateInterval.status) {
            heartRate.isHasRange = true
            heartRate.mode = AUTO_MODE
            val arr = heartRateInterval.startTime.split(":")
            heartRate.startHour = arr[0].toInt()
            heartRate.startMinute = arr[1].toInt()
            val arr1 = heartRateInterval.endTime.split(":")
            heartRate.stopHour = arr1[0].toInt()
            heartRate.stopMinute = arr1[1].toInt()
        } else {
            heartRate.isHasRange = false
            heartRate.mode = HAND_MODE
        }
//        LOGS.d("sdaadsas_12 ${Gson().toJson(heartRate)}")
        mClient?.commandBuilder?.setHeartRateMonitoringMode(heartRate)?.execute(object :
            TGCallback<Void> {
            override fun onSuccess(p0: Void?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.HeartRateMeasureIntervalSet(
                        true
                    )
                )

            }

            override fun onFailure(p0: Throwable?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.HeartRateMeasureIntervalSet(
                        false
                    )
                )
            }

        })


    }


    override fun setSedentaryData(sedentaryData: SedentaryData) {

        val subList = ArrayList<Boolean>()
        subList.add(0, sedentaryData.status)
        subList.addAll(sedentaryData.repeatDays!!.toList())
        subList.reverse()
//        LOGS.d("setSedentaryData :: ${subList}")
//        LOGS.d("setSedentaryData :: ${dataConverter.booleanArrayToBinaryString(subList)}")
        val sedentary = TGSedentaryConfig()

        sedentary.startHour = sedentaryData.startHour
        sedentary.startMinute = sedentaryData.startMinute
        sedentary.stopHour = sedentaryData.endHour
        sedentary.stopMinute = sedentaryData.endMinute
        sedentary.repeat = dataConverter.booleanArrayToBinaryString(subList)
        sedentary.interval = sedentaryData.interval



        mClient?.commandBuilder?.setSedentary(sedentary)?.execute(object :
            TGCallback<Void> {
            override fun onSuccess(p0: Void?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.SedentaryDataUpdated(
                        true
                    )
                )

            }

            override fun onFailure(p0: Throwable?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.SedentaryDataUpdated(
                        false
                    )
                )

            }

        })

    }


    override fun setHeartRateAlert(heartRateAlert: HeartRateAlert) {
        val config = TGHeartRateRangeConfig()
        config.isEnableMax = true
        config.isEnableMin = true
        config.maxHr = heartRateAlert.max_hr
        config.minHr = heartRateAlert.min_hr
        config.range1 = 0x62;
// Fat Burning Heart Rate Threshold
        config.range2 = 0x97;
// Aerobic heart rate threshold
        config.range3 = 0x8A;
// Anaerobic exercise heart rate thresholdconfig.setRange4(0x9E);
// Extreme sports heart rate threshold
        config.range5 = 0xB1;
        watchDataStore.updateHeartRateStatus(heartRateAlert.status)

        if (mClient != null) {
            mClient?.commandBuilder?.setHeartRateRange(config)?.execute(object :
                TGCallback<Void> {
                override fun onSuccess(p0: Void?) {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.HeartRateAlertUpdated(
                            true
                        )
                    )

                }

                override fun onFailure(p0: Throwable?) {
                    testUpdateDeviceDataCallback?.onUpdateDataReceived(
                        UpdateDeviceDataCallback.HeartRateAlertUpdated(
                            false
                        )
                    )
                }

            })
        }
    }


    override fun setDrinkWaterReminder(sedentaryData: SedentaryData) {
        LOGS.d("setDrinkWaterReminder $sedentaryData")
//        val subList = ArrayList<Boolean>()
//        subList.add(0, sedentaryData.status)
//        subList.addAll(sedentaryData.repeatDays!!.toList())
//        subList.reverse()
//        LOGS.d("setDrinkWaterReminder :: ${subList}")
//        LOGS.d("setDrinkWaterReminder :: ${dataConverter.booleanArrayToBinaryString(subList)}")
        val drink = TGRemindDrinking()
        drink.interval = sedentaryData.interval  // passing in minutes
        // drink.isOnOff = sedentaryData.status
        drink.startHour = sedentaryData.startHour
        drink.startMinute = sedentaryData.startMinute
        drink.stopHour = sedentaryData.endHour
        drink.stopMinute = sedentaryData.endMinute
        var repeat = 0x00
        if (sedentaryData.status) {
            repeat = 0xFF
        }
        //  drink.repeat = dataConverter.booleanArrayToBinaryString(subList)

        drink.repeat = repeat
        mClient?.commandBuilder?.setRemindDrinking(drink)?.execute(object : TGCallback<Void> {
            override fun onSuccess(p0: Void?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.DrinkWaterUpdated(
                        true
                    )
                )

            }

            override fun onFailure(p0: Throwable?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.DrinkWaterUpdated(
                        false
                    )
                )
            }

        })

    }

    override fun updateCustomReply(customReplyData: CustomReplyData) {

//        LOGS.d("Quick_replies $customReplyData")

        val quickReplies = ArrayList<TGQuickReply>()

        customReplyData.customReplies.forEachIndexed { index, item ->
            val quick = TGQuickReply()
            quick.content = item.content
            quick.msgIndex = index + 1
            //quick.msgType = item.crc + 1
            quickReplies.add(quick)
            //count = count + 1

        }

//        LOGS.d("Quick_replies___ ${Gson().toJson(quickReplies)}")
        mClient?.commandBuilder?.syncQuickReply(quickReplies)?.execute(object : TGCallback<Void> {

            override fun onFailure(p0: Throwable?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.CustomizeReplyUpdated(
                        false
                    )
                )
            }

            override fun onSuccess(p0: Void?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.CustomizeReplyUpdated(
                        true
                    )
                )
                watchDataStore.setCustomReplies(customReplyData)
            }

        })


    }


    override fun setTemperatureUnit(unit: String) {
        val config = TGUnitConfig()
        config.temp = when (unit.lowercase()) {
            UnitSystem.IMPERIAL.type.lowercase() -> {
                TGUnitConfig.FAHRENHEIT
            }
            else -> TGUnitConfig.CELSIUS
        }
        mClient?.commandBuilder?.setUnit(config)?.execute(object :
            TGCallback<Void> {
            override fun onSuccess(p0: Void?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.DeviceUnitsUpdated(
                        true
                    )
                )

            }

            override fun onFailure(p0: Throwable?) {
                testUpdateDeviceDataCallback?.onUpdateDataReceived(
                    UpdateDeviceDataCallback.DeviceUnitsUpdated(
                        false
                    )
                )
            }
        })

    }

    private val callback: OnSyncDialListener = object : OnSyncDialListener {
        override fun onProgress(progress: Int) {
            Timber.d("progress=%s", progress)
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                    WatchUpdateStatus(
                        status = UpdateStatus.PROGRESS,
                        percentagePercentage = progress
                    )
                )
            )
        }

        override fun onCompleted() {
            LOGS.d("DiyWatchfaceWork onCompleted")
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                    WatchUpdateStatus(
                        status = UpdateStatus.COMPLETED,
                        wStatus = WatchFaceEventsConstants.Complete
                    )
                )
            )
        }

        override fun onError(throwable: Throwable) {
            LOGS.d("DiyWatchfaceWork onError")
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                    WatchUpdateStatus(
                        status = UpdateStatus.ERROR,
                        wStatus = throwable.message
                    )
                )
            )
            AppLogs.sendAppLogs(
                LogEvents.WatchFace,
                WatchFaceEvents.TransferFailed
            )
        }
    }

    override fun setDiyWatchFaceCustom(watchFace: DiyCustomWatchFace) {

        try {
            mClient?.dialManager?.removeOnSyncDialListener(callback)
            mClient?.dialManager?.addOnSyncDialListener(callback)
            LOGS.d("setDiyWatchFaceCustom evolve inside")
            val backgroundBmp = watchFace.image?.convertCorner(0f)
            val binFile = File(Uri.parse(watchFace.binFile).path!!)
            val dial = TGPhotoDialBuilder()
                .setFilePath(binFile.absolutePath)
                .setBackground(backgroundBmp)
                //.setColor(Color.parseColor("#000000"))
//                .setTimePosition(timePosition)
                .build()

            val manager: TGDialManager? = mClient?.dialManager
            manager?.syncDial(dial)
        } catch (e: Exception) {
            e.printStackTrace()
            testUpdateDeviceDataCallback?.onUpdateDataReceived(
                UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                    WatchUpdateStatus(status = UpdateStatus.ERROR,
                        wStatus = "exception")
                )
            )
        }
    }

//    override fun updateWearType(status: Boolean) {
//        if (mClient != null) {
//            var wearType = TGWearType.LEFT
//            if (status) {
//                wearType = TGWearType.LEFT
//            } else {
//                wearType = TGWearType.RIGHT
//            }
//            mClient?.commandBuilder?.setWearType(wearType)?.execute(object : TGCallback<Void> {
//                override fun onSuccess(p0: Void?) {
//                    baseUpdateDeviceDataCallbacks?.onWearTypeUpdated(true)
//                }
//
//                override fun onFailure(p0: Throwable?) {
//                    baseUpdateDeviceDataCallbacks?.onWearTypeUpdated(false)
//                }
//
//            })
//        }
//    }


}