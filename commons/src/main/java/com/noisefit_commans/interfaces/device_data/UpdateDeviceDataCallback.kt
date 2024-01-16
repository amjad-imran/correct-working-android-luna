package com.noisefit_commans.interfaces.device_data

import com.noisefit_commans.models.ColorfitError
import com.noisefit_commans.models.ManualMeasurement
import com.noisefit_commans.models.SwitchSetting
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.models.WatchUpdateStatus

sealed class UpdateDeviceDataCallback {

    class WorkoutStartState(val success: Boolean, val errorMessage: String? = null) :
        UpdateDeviceDataCallback()

    class OngoingWorkoutData(val duration: Int,val sportStatus: Int,val sportType: Int,
                             val startTimeStamp: Long) : UpdateDeviceDataCallback()

    class WorkoutPaused(val success: Boolean) : UpdateDeviceDataCallback()
    class WorkoutResumed(val success: Boolean) : UpdateDeviceDataCallback()
    class WorkoutStopped(val success: Boolean) : UpdateDeviceDataCallback()

    class AlarmUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class LanguageUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class WatchFaceUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class DoNotDisturbUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class DeviceNotificationsUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class MenstrualDataUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class HeartRateMeasureIntervalSet(val success: Boolean) : UpdateDeviceDataCallback()
    class Spo2SettingsUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class SedentaryDataUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class FirstDayUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class AGPSUpdateProgress(val status: UpdateStatus, val progress: Int? = 0) :
        UpdateDeviceDataCallback()

    class MusicSwitchUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class CallSwitchUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class UserInfoUpdated(val success: Boolean) : UpdateDeviceDataCallback()

    class DeviceUnitsUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class BrightnessLevelUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class VibrationIntensityUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class DeviceTimeSynced(val success: Boolean) : UpdateDeviceDataCallback()
    class WeatherSwitchUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class FindPhoneUpdated(val success: Boolean) : UpdateDeviceDataCallback()

    class OnMusicEventChanged(val event: String) : UpdateDeviceDataCallback()

    class FirmwareUpgradeProgress(val watchUpdateStatus: WatchUpdateStatus) :
        UpdateDeviceDataCallback()

    class CustomizeWatchFaceProgress(val watchUpdateStatus: WatchUpdateStatus) :
        UpdateDeviceDataCallback()

    class WatchPasswordUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class FirmwareUpgraded(val success: Boolean) : UpdateDeviceDataCallback()
    class WristLiftGestureUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class AutoSleepUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class ScreenAwakeIntervalUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class WatchFaceLayoutUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class CustomizeReplyUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class SwitchSettingUpdated(val switchSetting: SwitchSetting) : UpdateDeviceDataCallback()
    class ClickCameraImage() : UpdateDeviceDataCallback()
    class SwitchCameraView(val status: Boolean) : UpdateDeviceDataCallback()
    class DownloadWatchFaceContents(val watchFace: WatchFace) :
        UpdateDeviceDataCallback()

    class UpdateAppList(val success: Boolean) : UpdateDeviceDataCallback()
    class WidgetSortListUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class ContactListUpdated(val success: Boolean) : UpdateDeviceDataCallback()

    class SOSContactUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class HandWashingUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class WeatherUpdateRequest() : UpdateDeviceDataCallback()
    class HeartRateAlertUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class AddReminder(val success: Boolean) : UpdateDeviceDataCallback()
    class DeleteReminder(val success: Boolean) : UpdateDeviceDataCallback()
    class Error(val colorfitError: ColorfitError) : UpdateDeviceDataCallback()
    class ActivitySwitchUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class DrinkWaterUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class MealDataUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class MedicineDataUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class FactoryReset(val success: Boolean) : UpdateDeviceDataCallback()
    class StressDataUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class BodyTempDataUpdated(val success: Boolean) : UpdateDeviceDataCallback()

    class Pro3WatchFaceUpdate(val watchFace: WatchFace) : UpdateDeviceDataCallback()
    class WalkReminderDataUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class SportModeDataUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class BleCallingSwitchUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class WorldClockSet(val success: Boolean) : UpdateDeviceDataCallback()
    class DeleteStock(val success: Boolean) : UpdateDeviceDataCallback()
    class StockListSet(val success: Boolean) : UpdateDeviceDataCallback()
    class SyncStockInfoList(val success: Boolean) : UpdateDeviceDataCallback()
    class QuickEyeMovementSwitchUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class UpdateCallStatus(val success: Boolean) : UpdateDeviceDataCallback()
    class MuteDevice() : UpdateDeviceDataCallback()
    class CameraModeUpdate(val success: Boolean) : UpdateDeviceDataCallback()
    class UPIQRCodeUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class ClearUPIQRCodeUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class SportWidgetSortDataUpdated(val success: Boolean) : UpdateDeviceDataCallback()
    class ManualMeasurementObtained(val manualMeasurement: ManualMeasurement) :
        UpdateDeviceDataCallback()

    class SleepReminderUpdated(val success: Boolean) : UpdateDeviceDataCallback()
}