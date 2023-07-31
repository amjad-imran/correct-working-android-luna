package com.noisefit_commans.interfaces.device_data

import com.noisefit_commans.models.*

interface UpdateDeviceDataCallbacks {
    fun onAlarmUpdated(success : Boolean)
    fun onLanguageUpdated(success : Boolean)
    fun onWatchFaceUpdated(success : Boolean)
    fun onDoNotDisturbUpdated(success : Boolean)
    fun onDeviceNotificationsUpdated(success : Boolean)
    fun onMenstrualDataUpdated(success : Boolean)
    fun onHeartRateMeasureIntervalSet(success : Boolean)
    fun onSedentaryDataUpdated(success : Boolean)
    fun onFirstDayUpdated(success : Boolean)
    fun onMusicSwitchUpdated(success : Boolean)
    fun onCallSwitchUpdated(success : Boolean)
    fun onUserInfoUpdated(success : Boolean)
    fun onUserGoalsUpdated(success : Boolean)
    fun onDeviceUnitsUpdated(success : Boolean)
    fun onDeviceTimeSynced(success : Boolean)
    fun onWeatherSwitchUpdated(success : Boolean)
    fun onFindPhoneUpdated(success : Boolean)
    fun onTimeFormatUpdated(success : Boolean)

    fun onFirmwareUpgradeProgress(deviceFirmware : DeviceFirmware)
    fun onCustomizeWatchFaceProgress(watchUpdateStatus: WatchUpdateStatus)
    fun onFirmwareUpgraded(success : Boolean)
    fun onWristLiftGestureUpdated(success : Boolean)
    fun onAutoSleepUpdated(success : Boolean)
    fun onScreenAwakeIntervalUpdated(success : Boolean)
    fun onWatchFaceLayoutUpdated(success : Boolean)
    fun onCustomizeReplyUpdated(success : Boolean)
    fun onSwitchSettingUpdated(switchSetting: SwitchSetting)
    fun onClickCameraImage()
    fun switchCameraView(status : Boolean) {}
    fun downloadWatchFaceContents(watchFace: WatchFace) {}

    fun onHandWashingUpdated(success : Boolean)
    fun onWeatherUpdateRequest()
    fun onHeartRateAlertUpdated(success : Boolean)
    fun onAddReminder(success: Boolean)
    fun onDeleteReminder(success: Boolean)
    fun onError(colorfitError: ColorfitError)
    fun onActivitySwitchUpdated(success : Boolean)
    fun onDrinkWaterUpdated(success: Boolean)
    fun onFactoryReset(success: Boolean)
    fun onStressDataUpdated(success: Boolean)

    fun onPro3WatchFaceUpdate(watchFace: WatchFace)
    fun onWalkReminderDataUpdated(success : Boolean)
    fun onSportModeDataUpdated(success : Boolean)

    fun onWorldClockSet(success : Boolean)
    fun onDeleteStock(success : Boolean)
    fun onStockListSet(success : Boolean)
    fun onSyncStockInfoList(success : Boolean)
    fun onQuickEyeMovementSwitchUpdated(success: Boolean)
}