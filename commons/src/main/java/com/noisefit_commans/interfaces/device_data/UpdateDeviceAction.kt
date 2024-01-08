package com.noisefit_commans.interfaces.device_data

import android.net.Uri
import com.noisefit_commans.models.AlarmAction
import com.noisefit_commans.models.AlarmsList
import com.noisefit_commans.models.AppNotification
import com.noisefit_commans.models.AutoSleep
import com.noisefit_commans.models.Contact
import com.noisefit_commans.models.CustomReplyData
import com.noisefit_commans.models.CustomWatchFace
import com.noisefit_commans.models.DeviceUnits
import com.noisefit_commans.models.DiyCustomWatchFace
import com.noisefit_commans.models.DoNotDisturb
import com.noisefit_commans.models.HandWashing
import com.noisefit_commans.models.HeartRateAlert
import com.noisefit_commans.models.HeartRateInterval
import com.noisefit_commans.models.IncomingCall
import com.noisefit_commans.models.Language
import com.noisefit_commans.models.ManualMeasureType
import com.noisefit_commans.models.MenstrualData
import com.noisefit_commans.models.ReminderList
import com.noisefit_commans.models.SOSContact
import com.noisefit_commans.models.SedentaryData
import com.noisefit_commans.models.SleepReminder
import com.noisefit_commans.models.Spo2Data
import com.noisefit_commans.models.SportsModeList
import com.noisefit_commans.models.StartDayOfWeek
import com.noisefit_commans.models.StockInfoList
import com.noisefit_commans.models.StockSymbolList
import com.noisefit_commans.models.SwitchSetting
import com.noisefit_commans.models.TimeFormat
import com.noisefit_commans.models.UPIQRCode
import com.noisefit_commans.models.Units
import com.noisefit_commans.models.UserGoals
import com.noisefit_commans.models.UserInfo
import com.noisefit_commans.models.VibrationIntensity
import com.noisefit_commans.models.VisionOtaFiles
import com.noisefit_commans.models.WalkReminderData
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.models.WatchFaceLayout
import com.noisefit_commans.models.WatchFacesCustomHybrid
import com.noisefit_commans.models.WatchPassword
import com.noisefit_commans.models.WeatherData
import com.noisefit_commans.models.WeatherDataHourly
import com.noisefit_commans.models.Widget
import com.noisefit_commans.models.WorldClocksPushData
import com.noisefit_commans.models.WristLiftGesture
import java.util.Calendar

sealed class UpdateDeviceAction {


    class VisionUpdateFirmware(val visionOtaFiles: List<VisionOtaFiles>) : UpdateDeviceAction()
    class UpdateFirmware(val fileUri: String) : UpdateDeviceAction()
    class StartCameraMode(val status: Boolean) : UpdateDeviceAction()
    class UpdateAlarm(val alarm: AlarmsList, val alarmAction: AlarmAction) : UpdateDeviceAction()
    class UpdateLanguage(val language: Language) : UpdateDeviceAction()
    class FindDevice(val findDevice: SwitchSetting) : UpdateDeviceAction()
    class UpdateDND(val doNotDisturb: DoNotDisturb) : UpdateDeviceAction()
    class SetHeartRateInterval(val heartRateInterval: HeartRateInterval) : UpdateDeviceAction()
    class SetSedentaryData(val sedentaryData: SedentaryData) : UpdateDeviceAction()
    class SetUserInfo(val userInfo: UserInfo, val userGoals: UserGoals, val userName: String?) :
        UpdateDeviceAction()
    class SetManualMeasurement(val manualMeasureType: ManualMeasureType, val status: Boolean) :UpdateDeviceAction()
    class SetDeviceUnits(val units: DeviceUnits) : UpdateDeviceAction()
    class SetDeviceDateTime(val calender: Calendar, val units: TimeFormat) : UpdateDeviceAction()
    class SendAppNotification(val appNotification: AppNotification) : UpdateDeviceAction()
    class SetWristLiftGesture(val wristLiftGesture: WristLiftGesture) : UpdateDeviceAction()
    class SendErrorNotificationToWatch(val message: String) : UpdateDeviceAction()
    class SetScreenAwakeInterval(val interval: Int) : UpdateDeviceAction()
    class SetWatchFace(val watchFace: WatchFace) : UpdateDeviceAction()
    class SetWatchFaceCustom(val watchFace: CustomWatchFace) : UpdateDeviceAction()

    class SetDiyWatchFaceCustom(val watchFace: DiyCustomWatchFace) : UpdateDeviceAction()
    class SetWatchFaceCustomHybrid(val watchFace: WatchFacesCustomHybrid) : UpdateDeviceAction()
    class SetSwitchSetting(switchSetting: SwitchSetting) : UpdateDeviceAction()
    class SetWeatherData(val weatherDataList: List<WeatherData>, val unit: String) :
        UpdateDeviceAction()
    class SetWeatherDataHourly(val weatherDataList: List<WeatherData>, val weatherDataHourlyList: List<WeatherDataHourly>, val unit: String) :
        UpdateDeviceAction()

    class UpdateMenstrualData(val menstrualData: MenstrualData) : UpdateDeviceAction()
    class SetStartDayOfWeek(startDayOfWeek: StartDayOfWeek) : UpdateDeviceAction()
    class SetMusicSwitch(val musicSwitch: SwitchSetting) : UpdateDeviceAction()
    class SetBrightnessLevel(val level: Int) : UpdateDeviceAction()
    class SetWeatherSwitch(val switchSetting: SwitchSetting) : UpdateDeviceAction()
    class OnCameraShutterClicked() : UpdateDeviceAction()
    class SetWatchPassword(val watchPassword: WatchPassword) : UpdateDeviceAction()
    class SetCustomBackground(val imagePath: Uri, val firmware: String) : UpdateDeviceAction()
    class SetCustomBackgroundWithLayout(val imagePath: Uri, val watchFaceLayout: WatchFaceLayout) :
        UpdateDeviceAction()

    class SetIncomingCallInfo(val incomingCall: IncomingCall) : UpdateDeviceAction()
    class UpdateCustomReply(val customReplyData: CustomReplyData) : UpdateDeviceAction()
    class SetAutoSleep(val autoSleep: AutoSleep) : UpdateDeviceAction()
    class SetWatchFaceLayout(watchFaceLayout: WatchFaceLayout) : UpdateDeviceAction()
    class SetFindMyPhone(val switchSetting: SwitchSetting) : UpdateDeviceAction()
    class OnMusicEventChanged(val event: String) : UpdateDeviceAction()
    class OnDownloadedWatchFaceContents(watchFace: WatchFace) : UpdateDeviceAction()
    class SetHandWashing(val handWashing: HandWashing) : UpdateDeviceAction()
    class UpdateAPGSData(val data1: Uri, val data2: Uri) : UpdateDeviceAction()
    class OnWeatherUpdateRequest() : UpdateDeviceAction()
    class SetHeartRateAlert(val heartRateAlert: HeartRateAlert) : UpdateDeviceAction()
    class AddReminder(val reminder: ReminderList.Reminder) : UpdateDeviceAction()
    class OnAddReminder(success: Boolean) : UpdateDeviceAction()
    class DeleteAlarm(alarm: AlarmsList) : UpdateDeviceAction()
    class DeleteReminders(val reminderList: ReminderList) : UpdateDeviceAction()
    class OnDeleteReminder(success: Boolean) : UpdateDeviceAction()
    class SetActivityRecogniseSwitch(val activitySwitch: SwitchSetting) : UpdateDeviceAction()
    class SetVibrationIntensity(val vibrationIntensity: VibrationIntensity) : UpdateDeviceAction()
    class SetDrinkWaterReminder(val sedentaryData: SedentaryData) : UpdateDeviceAction()
    class SetMealReminder(val sedentaryData: SedentaryData) : UpdateDeviceAction()
    class SetMedicineReminder(val sedentaryData: SedentaryData) : UpdateDeviceAction()
    object SetFactoryReset : UpdateDeviceAction()
    class SetStressData(val sedentaryData: SedentaryData) : UpdateDeviceAction()
    class SetSpo2Settings(val spo2Data: Spo2Data) : UpdateDeviceAction()

    class GetWatchFacePro3(type: String) : UpdateDeviceAction()
    class SetSportSyncParamPro3() : UpdateDeviceAction()
    class SetWalkReminderPro3(val walkReminderData: WalkReminderData) : UpdateDeviceAction()

    class SetRestartDevice() : UpdateDeviceAction()
    class SetCallBacks() : UpdateDeviceAction()

    class SetSportModeInfo(val data: SportsModeList) : UpdateDeviceAction()
    class SetSportWidgetSortList(val data: List<Widget>) : UpdateDeviceAction()

    class UpdateApplicationList(val data: List<Widget>) : UpdateDeviceAction()
    class UpdateWidgetSortList(val data: List<Widget>) : UpdateDeviceAction()
    class SetWorldClock(val data: WorldClocksPushData) : UpdateDeviceAction()
    class SetTemperatureUnit(val unit: String) : UpdateDeviceAction()
    class SetBodyTemperatureUnit(val unit: Units) : UpdateDeviceAction()
    class DeleteStock(val symbol: String) : UpdateDeviceAction()
    class SetStockList(stockSymbolList: StockSymbolList) : UpdateDeviceAction()
    class SyncStockInfoList(val stockInfoList: StockInfoList) : UpdateDeviceAction()
    class SetQuickEyeMovementSwitch(val status: Boolean) : UpdateDeviceAction()
    class Default() : UpdateDeviceAction()
    class SetContactList(val contactList: List<Contact>) : UpdateDeviceAction()

    class UpdateSleepReminder(val sleepReminder: SleepReminder) : UpdateDeviceAction()
    class SetSOSContact(val sosContact: SOSContact) : UpdateDeviceAction()
    class SetBleCallingSwitch(val status: Boolean) : UpdateDeviceAction()
    class SetUPIQRCode(val uPIQRCode: List<UPIQRCode>) : UpdateDeviceAction()
    class ClearUPIQRCode(val id: Int) : UpdateDeviceAction()
    class CloseFindPhoneFromWatch(val status: Boolean) : UpdateDeviceAction()

    class CheckOngoingWorkout() : UpdateDeviceAction()
    class StartWorkout(val sportType: Int,val sportStartTime: Long) : UpdateDeviceAction()
    class UpdateOngoingWorkout(val sportType: Int,val sportTimeStamp: Long,val action:Int) : UpdateDeviceAction()
}
