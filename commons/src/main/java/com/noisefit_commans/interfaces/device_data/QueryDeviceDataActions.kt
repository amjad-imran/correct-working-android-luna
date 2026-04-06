package com.noisefit_commans.interfaces.device_data


import com.noisefit_commans.interfaces.base.BaseActions
import com.noisefit_commans.models.ColorFitDevice

abstract class QueryDeviceDataActions() :
    BaseActions() {

    abstract fun queryFirmwareVersion()
    abstract fun queryFirmwareUpgrade()
    abstract fun queryFirmwareUpgradeNew(colorFitDevice: ColorFitDevice)
    abstract fun queryBatteryPower()
    abstract fun getUserInfo()
    abstract fun getAlarms()
    abstract fun getHeartRateInterval()
    abstract fun getSedentaryData()


    open fun restartDevice() {}
    open fun resetTrigger() {}
    open fun getAgpsState() {}
    open fun getAppList() {}
    open fun getWidgetList() {}
    open fun getBluetoothCallStatus() {}
    open fun getBrightnessLevel() {}
    open fun getDoNotDisturbData() {}
    open fun getFindPhoneSwitch() {}
    open fun getWatchFaces() {}
    open fun getUserGoals() {}
    open fun getWristLiftGesture() {}
    open fun getLanguage() {}
    open fun getWeatherSwitchStatus() {}
    open fun getDeviceUnits() {}
    open fun getMusicControlSettings() {}
    open fun getCameraSwitchSettings() {}
    open fun getMenstrualSettings() {}
    open fun syncDeviceUnits() {}
    open fun getAutoSleep() {}
    open fun setMusicStatus(status: Int, title: String?, sec: Int?) {}

    open fun getCustomReplies() {}
    open fun getWatchFaceLayout() {}

    open fun getScreenAwakeInterval() {}

    open fun getHandwashData() {}
    open fun getHeartRateAlert() {}
    open fun getHeartRateAlertSettings() {}
    open fun getMedicineReminders() {}
    open fun getReminders() {}
    open fun getActivityRecogniseSettings() {}
    open fun getDrinkWaterSettings() {}
    open fun getMealReminderSettings() {}
    open fun getMedicineReminderSettings() {}
    open fun getStressSettings() {}
    open fun getPressureModeSettings() {}
    open fun getBodyTempUnit() {}
    open fun getWalkReminderData() {}
    open fun getSportModeInfo() {}
    open fun getSportWidgetSortList() {}
    open fun getFirmwareLogs() {}
    open fun getSleepException() {}
    open fun onMusicEventChanged(event: String) {
        // MusicPlayerControlsHandler.onEvent(event)
    }

    open fun setVolume(currentVolume: Int, maxVolume: Int) {}
    open fun getWorldClock() {}
    open fun getWatchPassword() {}
    open fun getVibrationIntensity() {}
    open fun getStockList() {}

    open fun getUPIQRCode() {}

    open fun getQuickEyeMovementSwitch() {}
    open fun getBleCallingSwitch() {}

    open fun getSleepReminder() {}
    open fun getRingWearingStatus() {}
    open fun getContactList() {}
    open fun getSpo2Settings() {}
    open fun getSpo2AlertSettings() {}
    open fun getHighStressAlertSettings() {}

    open fun getSOSContactList() {}
    abstract fun setDevice(colorFitDevice: ColorFitDevice)
}
