package com.noisefit_commans.data.local.abstraction

import com.noisefit_commans.models.Contact
import com.noisefit_commans.models.CustomReplyData
import com.noisefit_commans.models.HandWashing
import com.noisefit_commans.models.HeartRateInterval
import com.noisefit_commans.models.LocationDataModel
import com.noisefit_commans.models.SedentaryData
import com.noisefit_commans.models.WatchFirmwareDetails
import com.noisefit_commans.models.WeatherDataModel
import com.noisefit_commans.models.WorldClocksPushData


interface WatchDataStore {

    fun setWatchIgnoreVersion(number: Int)
    fun getWatchIgnoreVersion(): Int

    fun setLastUpdatedTimeStamp(timeStamp: Long)
    fun getLastUpdateCheckTimeStamp(): Long

    fun getDeviceFirmwareDetails(): WatchFirmwareDetails?
    fun saveDeviceFirmwareDetails(data: WatchFirmwareDetails)

    fun getBatteryPercent(): Int
    fun updateBatteryPercent(percent: Int?)

    fun getBatteryPercentRing(): Int
    fun updateBatteryPercentRing(percent: Int?)

    fun getHeartRateStatus(): Boolean
    fun updateHeartRateStatus(status: Boolean)

    fun getBloodOxygenStatus(): Pair<Boolean, Int?>
    fun updateBloodOxygenStatus(data: Pair<Boolean, Int?>)

    fun getFirmwareVersion(): String
    fun updateFirmwareVersion(firmwareVersion: String)

    fun updateHeartRateInterval(interval: HeartRateInterval?)
    fun getHeartRateInterval(): HeartRateInterval?


    fun updateHandWashData(handWashing: HandWashing?)
    fun getHandWashData(): HandWashing?

    fun updateScreenBrightness(level: Int)
    fun getScreenBrightness(): Int


    fun updateIdleAlert(data: SedentaryData?)
    fun getIdleAlert(): SedentaryData?

    fun updateWaterReminder(data: SedentaryData?)
    fun getWaterReminder(): SedentaryData?


    fun saveWorldClockData(data: WorldClocksPushData?)
    fun getWorldClockData(): WorldClocksPushData?

    fun saveLogPathName(data: String)
    fun getLogPathName(): String?

    fun getCustomReplies(): CustomReplyData?
    fun setCustomReplies(customReplyData: CustomReplyData)
    fun getUniqueIdForWatchFaces(): Int
    fun setUniqueIdForWatchFaces()
    fun clearWatchData()

    fun setAskForPermission(status: Boolean)
    fun getAskForPermission(): Boolean
    fun getMedicineReminder(): SedentaryData?
    fun updateMedicineReminder(data: SedentaryData)
    fun isInitialOtaChecked(): Boolean
    fun setInitialOtaChecked(isChecked: Boolean)

    fun setDefaultValue(status: Boolean)
    fun getDefaultValue(): Boolean

    fun setContactNumberList(dataList: List<Contact>)
    fun getContactNumberList(): ArrayList<Contact>

    fun logWatchInfo(s: String)
    fun getWatchInfo(): String

    fun setRyeexWatchToken(s: String)
    fun getRyeexWatchToken(): String

    fun clearLocationData(sessionId: Long)
    fun saveAndGetLocation(sessionId: Long, data: List<LocationDataModel>): List<LocationDataModel>

    fun getLocationDataModel(sessionId: Long): List<LocationDataModel>?

    fun saveWeatherDataModel(data: WeatherDataModel)

    fun getWeatherDataModel(startTimeStamp: Long, endTimeStamp: Long): WeatherDataModel?
    fun updateSerialNo(serialNumberRing: String)
    fun getSerialNo(): String?

    fun resetChargingNotificationData()

    fun setChargingNotificationShown(level: ChargingNotificationLevel)
    fun getChargingNotificationsShown(): HashMap<String, Boolean>

    fun getLastSavedAverageHrv(): Int
    fun setLastSavedAverageHrv(value: Int)
}

enum class ChargingNotificationLevel {
    LEVEL_5, LEVEL_10, LEVEL_15, LEVEL_20
}

//WeatherDataModel