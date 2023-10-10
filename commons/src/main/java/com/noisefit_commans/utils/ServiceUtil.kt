package com.noisefit_commans.utils

import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.data.UserActivityAction
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions

object ServiceUtil {


    fun setQueryMethod(it: QueryAction, queryAction: QueryDeviceDataActions) {
        when (it) {
            QueryAction.QueryBatteryPower -> {
                queryAction.queryBatteryPower()
            }
            QueryAction.ResetTrigger -> {
                queryAction.resetTrigger()
            }
            QueryAction.RestartDevice -> {
                queryAction.restartDevice()
            }
            QueryAction.GetContactList -> {
                queryAction.getContactList()
            }
            QueryAction.GetSpo2Settings -> {
                queryAction.getSpo2Settings()
            }
            QueryAction.GetQuickBleCallingSwitch -> {
                queryAction.getBleCallingSwitch()
            }
            QueryAction.GetSleepReminder -> {
                queryAction.getSleepReminder()
            }
            QueryAction.GetApplicationList -> {
                queryAction.getAppList()
            }

            QueryAction.GetWidgetSortList -> {
                queryAction.getWidgetList()
            }
            QueryAction.GetActivityRecogniseSettings -> {
                queryAction.getActivityRecogniseSettings()
            }
            QueryAction.GetAlarms -> {
                queryAction.getAlarms()
            }
            QueryAction.GetAutoSleep -> {
                queryAction.getAutoSleep()
            }
            QueryAction.GetAgpsState -> {
                queryAction.getAgpsState()
            }
            QueryAction.GetCustomReplies -> {
                queryAction.getCustomReplies()
            }
            QueryAction.GetDeviceUnits -> {
                queryAction.getDeviceUnits()
            }
            QueryAction.GetDoNotDisturbData -> {
                queryAction.getDoNotDisturbData()
            }
            QueryAction.GetDrinkWaterSettings -> {
                queryAction.getDrinkWaterSettings()
            }
            QueryAction.GetMealReminderSettings -> {
                queryAction.getMealReminderSettings()
            }
            QueryAction.GetMedicineReminderSettings -> {
                queryAction.getMedicineReminderSettings()
            }
            QueryAction.GetFindPhoneSwitch -> {
                queryAction.getFindPhoneSwitch()
            }
            QueryAction.GetFirmwareLogs -> {
                queryAction.getFirmwareLogs()
            }
            QueryAction.GetHandWashData -> {
                queryAction.getHandwashData()
            }
            QueryAction.GetHeartRateAlert -> {
                queryAction.getHeartRateAlert()
            }
            QueryAction.GetHeartRateInterval -> {
                queryAction.getHeartRateInterval()
            }

            QueryAction.GetBluetoothCallStatus -> {
                queryAction.getBluetoothCallStatus()
            }
            QueryAction.GetSOSContactList -> {
                queryAction.getSOSContactList()
            }
            QueryAction.GetLanguage -> {
                queryAction.getLanguage()
            }
            QueryAction.GetMenstrualSettings -> {
                queryAction.getMenstrualSettings()
            }
            QueryAction.GetMusicControlSettings -> {
                queryAction.getMusicControlSettings()
            }
            QueryAction.GetBrightnessLevelSettings -> {
                queryAction.getBrightnessLevel()
            }
            QueryAction.GetCameraSwitchSettings -> {
                queryAction.getCameraSwitchSettings()
            }
            QueryAction.GetQuickEyeMovementSwitch -> {
                queryAction.getQuickEyeMovementSwitch()
            }
            QueryAction.GetReminders -> {
                queryAction.getReminders()
            }
            QueryAction.GetBodyTempUnit -> {
                queryAction.getBodyTempUnit()
            }
            QueryAction.GetScreenAwakeInterval -> {
                queryAction.getScreenAwakeInterval()
            }
            QueryAction.GetSedentaryData -> {
                queryAction.getSedentaryData()
            }
            is QueryAction.UpdateVolume -> {
                queryAction.setVolume(it.current, it.maxVolume)
            }
            QueryAction.GetSportModeInfo -> {
                queryAction.getSportModeInfo()
            }
            QueryAction.GetSportWidgetSortList -> {
                queryAction.getSportWidgetSortList()
            }
            QueryAction.GetStockList -> {
                queryAction.getStockList()
            }
            QueryAction.GetStressSettings -> {
                queryAction.getStressSettings()
            }
            QueryAction.GetUserGoals -> TODO()
            QueryAction.GetUserInfo -> TODO()
            QueryAction.GetWalkReminderData -> {
                queryAction.getWalkReminderData()
            }
            QueryAction.GetWatchFaceLayout -> TODO()
            is QueryAction.GetWatchFaces -> {
                queryAction.getWatchFaces()
            }
            QueryAction.GetWeatherSwitchStatus -> TODO()
            QueryAction.GetWorldClock -> {
                queryAction.getWorldClock()
            }
            QueryAction.GetWatchPassword -> {
                queryAction.getWatchPassword()
            }
            QueryAction.GetVibrationIntensity -> {
                queryAction.getVibrationIntensity()
            }
            QueryAction.GetUPIQRCode -> {
                queryAction.getUPIQRCode()
            }
            QueryAction.GetWristLiftGesture -> {
                queryAction.getWristLiftGesture()
            }
            is QueryAction.MusicEventChanged -> TODO()
            is QueryAction.QueryFirmwareUpgrade -> {
                queryAction.queryFirmwareUpgrade()
            }
            is QueryAction.QueryFirmwareUpgradeNew -> TODO()
            QueryAction.QueryFirmwareVersion -> {
                queryAction.queryFirmwareVersion()
            }
            QueryAction.SyncDeviceUnits -> {
                queryAction.syncDeviceUnits()
            }
            is QueryAction.SendSongName -> {
                queryAction.setMusicStatus(it.status, it.title, it.sec)
            }
            else -> {
                LOGS.d("Query Device Action not defined in ServiceUtil")
            }
        }

    }

    fun setUpdateMethods(it: UpdateDeviceAction?, updateAction: UpdateDeviceDataActions) {
        when (it) {
            is UpdateDeviceAction.StartCameraMode -> {
                updateAction.startCameraMode(it.status)
            }
            is UpdateDeviceAction.UpdateApplicationList -> {
                updateAction.updateApplicationList(it.data)
            }
            is UpdateDeviceAction.UpdateWidgetSortList -> {
                updateAction.updateWidgetList(it.data)
            }
            is UpdateDeviceAction.SetSpo2Settings -> {
                updateAction.setSpo2Settings(it.spo2Data)
            }
            is UpdateDeviceAction.FindDevice -> {
                updateAction.findDevice(it.findDevice)
            }
            is UpdateDeviceAction.SetBleCallingSwitch -> {
                updateAction.setBleCallingSwitch(it.status)
            }
            is UpdateDeviceAction.SetVibrationIntensity -> {
                updateAction.setVibrationIntensity(it.vibrationIntensity)
            }
            is UpdateDeviceAction.UpdateAlarm -> {
                updateAction.updateAlarm(it.alarm, it.alarmAction)
            }
            is UpdateDeviceAction.SetDrinkWaterReminder -> {
                updateAction.setDrinkWaterReminder(it.sedentaryData)
            }
            is UpdateDeviceAction.SetMealReminder -> {
                updateAction.setMealReminder(it.sedentaryData)
            }

            is UpdateDeviceAction.SetSportWidgetSortList -> {
                updateAction.setSportWidgetSortList(it.data)
            }

            is UpdateDeviceAction.UpdateSleepReminder -> {
                updateAction.updateSleepReminder(it.sleepReminder)
            }
            is UpdateDeviceAction.SetMedicineReminder -> {
                updateAction.setMedicineReminder(it.sedentaryData)
            }
            is UpdateDeviceAction.SetSedentaryData -> {
                updateAction.setSedentaryData(it.sedentaryData)
            }
            is UpdateDeviceAction.SetUserInfo -> {
                updateAction.setUserInfo(it.userInfo, it.userGoals, it.userName)
            }

            is UpdateDeviceAction.SetHandWashing -> {
                updateAction.setHandWashing(it.handWashing)
            }
            is UpdateDeviceAction.SendAppNotification -> {
                updateAction.sendAppNotification(it.appNotification)
            }
            is UpdateDeviceAction.SendErrorNotificationToWatch -> {
                updateAction.sendErrorNotificationToWatch(it.message)
            }
            is UpdateDeviceAction.UpdateCustomReply -> {
                updateAction.updateCustomReply(it.customReplyData)
            }
            is UpdateDeviceAction.SetDeviceDateTime -> {
                updateAction.setDeviceDateTime(it.calender, it.units)
            }
            is UpdateDeviceAction.SetWatchPassword -> {
                updateAction.setWatchPassword(it.watchPassword)
            }
            is UpdateDeviceAction.DeleteReminders -> {
                updateAction.deleteReminders(it.reminderList)
            }
            is UpdateDeviceAction.SetContactList -> {
                updateAction.setContactList(it.contactList)
            }
            is UpdateDeviceAction.SetSOSContact -> {
                updateAction.setSOSContact(it.sosContact)
            }
            is UpdateDeviceAction.SetUPIQRCode -> {
                updateAction.setUPIQRCode(it.uPIQRCode)
            }
            is UpdateDeviceAction.ClearUPIQRCode -> {
                updateAction.setClearUPIQRCode(it.id)
            }
            is UpdateDeviceAction.SetWeatherSwitch -> {
                updateAction.setWeatherSwitch(it.switchSetting)
            }
            is UpdateDeviceAction.SetWeatherData -> {
                updateAction.setWeatherData(it.weatherDataList, it.unit)
                updateAction.setTemperatureUnit(it.unit)
            }
            is UpdateDeviceAction.SetWeatherDataHourly -> {
                updateAction.setWeatherDataHourly(it.weatherDataList, it.weatherDataHourlyList, it.unit)
            }
            is UpdateDeviceAction.SetTemperatureUnit -> {
                updateAction.setTemperatureUnit(it.unit)
            }
            is UpdateDeviceAction.SetHeartRateInterval -> {
                updateAction.setHeartRateInterval(it.heartRateInterval)
            }
            is UpdateDeviceAction.SetHeartRateAlert -> {
                updateAction.setHeartRateAlert(it.heartRateAlert)
            }
            is UpdateDeviceAction.SetQuickEyeMovementSwitch -> {
                updateAction.setQuickEyeMovementSwitch(it.status)
            }
            is UpdateDeviceAction.SetWorldClock -> {
                updateAction.setWorldClock(it.data)
            }
            is UpdateDeviceAction.SyncStockInfoList -> {
                updateAction.syncStockInfoList(it.stockInfoList)
            }
            is UpdateDeviceAction.DeleteStock -> {
                updateAction.deleteStock(it.symbol)
            }
            is UpdateDeviceAction.SetWatchFaceCustomHybrid -> {
                updateAction.setWatchFaceCustomHybrid(it.watchFace)
            }
            is UpdateDeviceAction.SetMusicSwitch -> {
                updateAction.setMusicSwitch(it.musicSwitch)
            }
            is UpdateDeviceAction.SetBrightnessLevel -> {
                updateAction.setBrightnessLevel(it.level)
            }
            is UpdateDeviceAction.UpdateDND -> {
                updateAction.updateDND(it.doNotDisturb)
            }
            is UpdateDeviceAction.SetWristLiftGesture -> {
                updateAction.setWristLiftGesture(it.wristLiftGesture)
            }
            is UpdateDeviceAction.SetFindMyPhone -> {
                updateAction.setFindMyPhone(it.switchSetting)
            }
            is UpdateDeviceAction.SetWalkReminderPro3 -> {
                updateAction.setWalkReminderPro3(it.walkReminderData)
            }
            is UpdateDeviceAction.SetActivityRecogniseSwitch -> {
                updateAction.setActivityRecogniseSwitch(it.activitySwitch)
            }
            is UpdateDeviceAction.SetStressData -> {
                updateAction.setStressData(it.sedentaryData)
            }
            is UpdateDeviceAction.SetFactoryReset -> {
                updateAction.setFactoryReset()
            }
            is UpdateDeviceAction.SetSportModeInfo -> {
                updateAction.setSportModeInfo(it.data)
            }
            is UpdateDeviceAction.UpdateMenstrualData -> {
                updateAction.updateMenstrualData(it.menstrualData)
            }
            is UpdateDeviceAction.SetCustomBackground -> {
                AppLogs.sendAppLogs("Custom Watchface transfer started")
                updateAction.setCustomBackground(it.imagePath, it.firmware)
            }
            is UpdateDeviceAction.SetCustomBackgroundWithLayout -> {
                updateAction.setCustomBackgroundWithLayout(it.imagePath, it.watchFaceLayout)
            }
            is UpdateDeviceAction.SetAutoSleep -> {
                updateAction.setAutoSleep(it.autoSleep)
            }

            is UpdateDeviceAction.SetScreenAwakeInterval -> {
                updateAction.setScreenAwakeInterval(it.interval)
            }
            is UpdateDeviceAction.UpdateLanguage -> {
                updateAction.updateLanguage(it.language)
            }
            is UpdateDeviceAction.SetBodyTemperatureUnit -> {
                updateAction.setBodyTemperatureUnit(it.unit)
            }
            is UpdateDeviceAction.AddReminder -> {
                updateAction.addReminder(it.reminder)
            }
            is UpdateDeviceAction.UpdateAPGSData -> {
                AppLogs.sendAppLogs("APGS transfer started")
                updateAction.updateAPGSData(it.data1, it.data2)
            }

            is UpdateDeviceAction.SetRestartDevice -> {
                updateAction.setRestartDevice()
            }

            is UpdateDeviceAction.SetDeviceUnits -> {
                updateAction.setDeviceUnits(it.units)
            }

            is UpdateDeviceAction.SetManualMeasurement -> {
                updateAction.setManualMeasurement(it.manualMeasureType, it.status)
            }

            is UpdateDeviceAction.CloseFindPhoneFromWatch -> {
                updateAction.closeFindPhoneFromWatch(it.status)
            }

            else -> {
                LOGS.d("Update Device Action not defined in ServiceUtil $it")
            }
        }

    }

    fun setActivityMethods(it: UserActivityAction, activityAction: UserActivityDataActions) {
        when (it) {
            is UserActivityAction.GetStepsData -> {
                activityAction.getStepsData(it.date)
            }

            is UserActivityAction.SetWeatherData -> {
                activityAction.setWeatherData(it.weatherDataList)
            }

            is UserActivityAction.GetSleepData -> {
                activityAction.getSleepData(it.date)
            }
            UserActivityAction.GetHeartRate -> {
                activityAction.getHeartRate()
            }
            UserActivityAction.GetBloodOxygenLevel -> {
                activityAction.getBloodOxygenLevel()
            }
            UserActivityAction.GetBloodPressure -> {
                activityAction.getBloodPressure()
            }
            UserActivityAction.GetStressCount -> {
                activityAction.getStressCount()
            }
            is UserActivityAction.GetHeartHistory -> {
                activityAction.getHeartHistory(it.calendar)
            }
            is UserActivityAction.SyncUserActivity -> {
                AppLogs.sendAppLogs("Sync Started")
                activityAction.syncUserActivity(it.date, it.isRefresh)
            }
            is UserActivityAction.UpdateSportsMode -> {
                activityAction.updateSportsMode(it.sportsModeRequest)
            }

            is UserActivityAction.Refresh -> {
                activityAction.refresh(it.sportsModeRequest)
            }

            is UserActivityAction.SetSpo2Measurement -> {
                activityAction.setSpo2MeasurementData(it.status)
            }

            is UserActivityAction.SyncSportsActivity -> {
                activityAction.syncSportsActivity(it.date)
            }


            is UserActivityAction.PushGPSData -> {
            }

            else -> {
                LOGS.d("Activity Action not defined in ServiceUtil")

            }
        }

    }

}