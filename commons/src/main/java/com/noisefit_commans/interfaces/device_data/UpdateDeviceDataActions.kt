package com.noisefit_commans.interfaces.device_data

import android.net.Uri
import com.noisefit_commans.interfaces.base.BaseActions
import com.noisefit_commans.models.*

import java.util.*


abstract class UpdateDeviceDataActions() :
    BaseActions() {


    open fun visionUpdateFirmware(visionOtaFiles: List<VisionOtaFiles>) {}
    abstract fun updateFirmware(fileUri: String)
    abstract fun startCameraMode(status: Boolean)
    abstract fun updateAlarm(alarm: AlarmsList, alarmAction: AlarmAction)
    abstract fun updateLanguage(language: Language)
    abstract fun findDevice(findDevice: SwitchSetting)
    abstract fun updateDND(doNotDisturb: DoNotDisturb)
    abstract fun setHeartRateInterval(heartRateInterval: HeartRateInterval)
    abstract fun setSedentaryData(sedentaryData: SedentaryData)
    abstract fun setUserInfo(userInfo: UserInfo, userGoals: UserGoals, userName: String?)
    abstract fun setDeviceUnits(units: DeviceUnits)
    abstract fun setDeviceDateTime(calender: Calendar, units: TimeFormat)
    abstract fun sendAppNotification(appNotification: AppNotification)
    abstract fun setWristLiftGesture(wristLiftGesture: WristLiftGesture)
    open fun sendErrorNotificationToWatch(message: String) {}
    open fun setScreenAwakeInterval(interval: Int) {}
    open fun setWatchFace(watchFace: WatchFace) {}
    open fun setWatchFaceCustom(watchFace: CustomWatchFace) {}
    open fun setDiyWatchFaceCustom(watchFace: DiyCustomWatchFace) {}
    open fun setWatchFaceCustomHybrid(watchFace: WatchFacesCustomHybrid) {}
    open fun setSwitchSetting(switchSetting: SwitchSetting) {}
    open fun setWeatherData(weatherDataList: List<WeatherData>, unit: String) {}
    open fun setWeatherDataHourly(weatherDataList: List<WeatherData>, hourlyWeatherList: List<WeatherDataHourly>, unit: String) {}
    open fun updateMenstrualData(menstrualData: MenstrualData) {}
    open fun setStartDayOfWeek(startDayOfWeek: StartDayOfWeek) {}
    open fun setMusicSwitch(musicSwitch: SwitchSetting) {}
    open fun setCameraSwitch(cameraSwitch: SwitchSetting) {}
    open fun setWeatherSwitch(switchSetting: SwitchSetting) {}
    open fun onCameraShutterClicked() {}

    open fun setWatchPassword(watchPassword: WatchPassword) {}
    open fun setCustomBackground(imagePath: Uri, firmware: String) {}
    open fun setCustomBackgroundWithLayout(imagePath: Uri, watchFaceLayout: WatchFaceLayout) {}
    open fun setIncomingCallInfo(incomingCall: IncomingCall) {}
    open fun updateCustomReply(customReplyData: CustomReplyData) {}
    open fun setAutoSleep(autoSleep: AutoSleep) {}
    open fun setAutoWorkoutStatus(status: Boolean) {}
    open fun setWatchFaceLayout(watchFaceLayout: WatchFaceLayout, imagePath: Uri) {}
    open fun setFindMyPhone(switchSetting: SwitchSetting) {}
    open fun onMusicEventChanged(event: String) {
        //  MusicPlayerControlsHandler.onEvent(event)
    }

    open fun onDownloadedWatchFaceContents(watchFace: WatchFace) {}
    open fun setHandWashing(handWashing: HandWashing) {}
    open fun updateAPGSData(data1: Uri, data2: Uri) {}
    open fun onWeatherUpdateRequest() {}
    open fun setHeartRateAlert(heartRateAlert: HeartRateAlert) {}
    open fun addReminder(reminder: ReminderList.Reminder) {}
    open fun onAddReminder(success: Boolean) {}
    open fun deleteAlarm(alarm: AlarmsList) {}
    open fun deleteReminders(reminderList: ReminderList) {}
    open fun onDeleteReminder(success: Boolean) {}
    open fun setActivityRecogniseSwitch(activitySwitch: SwitchSetting) {}
    open fun setDrinkWaterReminder(sedentaryData: SedentaryData) {}
    open fun setMealReminder(sedentaryData: SedentaryData) {}
    open fun setSportWidgetSortList(dataList: List<Widget>) {}
    open fun setMedicineReminder(sedentaryData: SedentaryData) {}
    open fun setFactoryReset() {}
    open fun setStressData(sedentaryData: SedentaryData) {}
    open fun setRealTimeDataState(status: Boolean) {}

    open fun getWatchFacePro3(type: String) {}
    open fun setSportSyncParamPro3() {}
    open fun setWalkReminderPro3(walkReminderData: WalkReminderData) {}

    open fun setRestartDevice() {}
    open fun setCallBacks() {}

    open fun setSportModeInfo(data: SportsModeList) {}
    open fun setBleCallingSwitch(status: Boolean) {}
    open fun setWorldClock(data: WorldClocksPushData) {}
    open fun setTemperatureUnit(unit: String) {}
    open fun setBodyTemperatureUnit(unit: Units) {}
    open fun deleteStock(symbol: String) {}
    open fun setBrightnessLevel(level: Int) {}
    open fun setStockList(stockSymbolList: StockSymbolList) {}
    open fun syncStockInfoList(stockInfoList: StockInfoList) {}
    open fun setQuickEyeMovementSwitch(status: Boolean) {}

    open fun closeFindPhoneFromWatch(status: Boolean) {}
    open fun startWorkout(sportType: Int, sportStartTime: Long) {}
    open fun checkOngoingWorkout() {}
    open fun updateOngoingWorkout(sportType: Int, sportTimeStamp: Long, action: Int) {}

    abstract fun setDevice(device: ColorFitDevice)
    open fun setContactList(contactList: List<Contact>) {}

    open fun setSOSContact(sosContact: SOSContact) {}
    open fun updateApplicationList(data: List<Widget>) {}
    open fun updateWidgetList(data: List<Widget>) {}
    open fun setUPIQRCode(uPIQRCode: List<UPIQRCode>) {}
    open fun setSpo2Settings(data: Spo2Data) {}
    open fun setUPIQRCode(uPIQRCode: UPIQRCode) {}
    open fun setClearUPIQRCode(id: Int) {}
    open fun setVibrationIntensity(vibrationIntensity: VibrationIntensity) {}
//    open fun insertSessionIntoGoogleFit(data: SportsGoogleFitData) {}
//    open fun insertStepsIntoGoogleFit(data: StepGoogleFitData) {}
//    open fun insertHeartRatesIntoGoogleFit(data: HeartGoogleFitData) {}
//    open fun insertSleepIntoGoogleFit(data: SleepGoogleFitData) {}

    open fun setManualMeasurement(manualMeasureType: ManualMeasureType, status: Boolean) {}
    open fun updateSleepReminder(sleepReminder: SleepReminder) {}
}