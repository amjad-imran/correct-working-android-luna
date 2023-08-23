package com.noisefit_commans.data.local.implementation

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.models.Contact
import com.noisefit_commans.models.CustomReplyData
import com.noisefit_commans.models.HandWashing
import com.noisefit_commans.models.HeartRateInterval
import com.noisefit_commans.models.LocationDataModel
import com.noisefit_commans.models.SedentaryData
import com.noisefit_commans.models.WatchFirmwareDetails
import com.noisefit_commans.models.WeatherDataModel
import com.noisefit_commans.models.WorldClocksPushData
import com.noisefit_commans.utils.LOGS
import javax.inject.Inject


private const val FEATURE_HEART_RATE_INTERVAL = "FEATURE_HEART_RATE_INTERVAL"
private const val FEATURE_HAND_WASH = "FEATURE_HAND_WASH"
private const val FEATURE_BRIGHTNESS = "FEATURE_BRIGHTNESS"
private const val FEATURE_IDLE_ALERT = "FEATURE_IDLE_ALERT"
private const val FEATURE_MEDICINE_REMINDER = "FEATURE_MEDICINE_REMINDER"
private const val FEATURE_WATER_REMINDER = "FEATURE_WATER_REMINDER"
private const val WORLD_CLOCK_DATA = "WORLD_CLOCK_DATA"
private const val SAVE_UNIQUE_WATCH_FACE_ID = "SAVE_UNIQUE_WATCH_FACE_ID"
private const val SAVE_CUSTOM_REPLIES = "SAVE_CUSTOM_REPLIES"
private const val SAVE_LOGS_PATH_NAME = "SAVE_LOGS_PATH_NAME"
private const val FIRMWARE_VERSION = "FIRMWARE_VERSION"
private const val BATTERY_PERCENT = "BATTERY_PERCENT"
private const val BATTERY_PERCENT_RING = "BATTERY_PERCENT_RING"
private const val HEART_RATE_STATUS = "HEART_RATE_STATUS"
private const val BLOOD_OXYGEN_STATUS = "BLOOD_OXYGEN_STATUS"
private const val BLOOD_OXYGEN_INTERVAL = "BLOOD_OXYGEN_INTERVAL"
private const val ASK_PERMISSION = "ASK_PERMISSION"
private const val WATCH_FIRMWARE_DETAILS = "WATCH_FIRMWARE_DETAILS"
private const val WATCH_IGNORE_VERSION_NUMBER = "WATCH_IGNORE_VERSION_NUMBER"
private const val WATCH_INITIAL_OTA_CHECK = "WATCH_INITIAL_OTA_CHECK"
private const val WATCH_CONTACT_LIST = "WATCH_CONTACT_LIST"
private const val WATCH_INFO_LOG = "WATCH_INFO_LOG"
private const val UPDATE_OTA_TIMESTAMP = "UPDATE_OTA_TIMESTAMP"
private const val WATCH_MAPS_LAT_LONG = "WATCH_MAPS_LAT_LONG"
private const val DEFAULT_VALUE = "DEFAULT_VALUE"
private const val SERIAL_NO = "SERIAL_NO"

private const val RYEEX_WATCH_TOKEN_ARG = "RYEEX_WATCH_TOKEN_ARG"
private const val WEATHER_SPORT_DATA_KEY = "WEATHER_SPORT_DATA_KEY_2"


private inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

class WatchDataStoreImpl
@Inject
constructor(
    private val gson: Gson,
    private val mPrefs: SharedPreferences
) : WatchDataStore {

    override fun getSerialNo(): String? {
        return mPrefs.getString(SERIAL_NO, null)
    }

    override fun updateSerialNo(serialNumberRing: String) {
        mPrefs.edit()
            ?.putString(SERIAL_NO, serialNumberRing)
            ?.apply()
    }

    override fun setLastUpdatedTimeStamp(timeStamp: Long) {
        mPrefs.edit()
            ?.putLong(UPDATE_OTA_TIMESTAMP, timeStamp)
            ?.apply()
    }

    override fun getLastUpdateCheckTimeStamp(): Long {
        return mPrefs.getLong(UPDATE_OTA_TIMESTAMP, 0)
    }

    override fun logWatchInfo(s: String) {
        mPrefs.edit()
            ?.putString(WATCH_INFO_LOG, s)
            ?.apply()
    }

    override fun getWatchInfo(): String {
        return mPrefs.getString(WATCH_INFO_LOG, "") ?: ""
    }

    override fun setRyeexWatchToken(s: String) {
        mPrefs.edit()
            ?.putString(RYEEX_WATCH_TOKEN_ARG, s)
            ?.apply()
    }

    override fun getRyeexWatchToken(): String {
        return mPrefs.getString(RYEEX_WATCH_TOKEN_ARG, "") ?: ""
    }

    override fun setInitialOtaChecked(isChecked: Boolean) {
        mPrefs.edit()
            ?.putBoolean(WATCH_INITIAL_OTA_CHECK, isChecked)
            ?.apply()
    }

    override fun clearLocationData(sessionId: Long) {
        val key = "${WATCH_MAPS_LAT_LONG}_$sessionId"
        mPrefs.edit().remove(key).commit()
    }

    override fun getLocationDataModel(sessionId: Long): List<LocationDataModel>? {
        val key = "${WATCH_MAPS_LAT_LONG}_$sessionId"
        val hasRun = mPrefs.getString(key, null)
        val type = object : TypeToken<java.util.ArrayList<LocationDataModel?>?>() {}.type
        return gson.fromJson<java.util.ArrayList<LocationDataModel>>(hasRun, type)
    }

    override fun saveWeatherDataModel(data: WeatherDataModel) {
        val key = WEATHER_SPORT_DATA_KEY
        val gson = Gson()
        val hasRun = mPrefs.getString(key, null)
        val edit = mPrefs.edit()
        if (hasRun == null) {
            val dataList = ArrayList<WeatherDataModel>()
            dataList.add(data)
            edit.putString(key, gson.toJson(dataList))
        } else {
            val type = object : TypeToken<java.util.ArrayList<WeatherDataModel?>?>() {}.type
            val arrayList = gson.fromJson<java.util.ArrayList<WeatherDataModel>>(hasRun, type)
            arrayList.add(data)
            edit.putString(key, gson.toJson(arrayList))
        }

//        LOGS.d("GPS_DATA", "getWeatherDataModel set" + Gson().toJson(data))

        edit.commit()
    }//2,4,8

    override fun getWeatherDataModel(startTimeStamp: Long, endTimeStamp: Long): WeatherDataModel? {
        val key = WEATHER_SPORT_DATA_KEY
        var weatherDataModel: WeatherDataModel? = null
        try {
            val hasRun = mPrefs.getString(key, null)
            val edit = mPrefs.edit()
            val type = object : TypeToken<java.util.ArrayList<WeatherDataModel?>?>() {}.type
            val weatherList = gson.fromJson<java.util.ArrayList<WeatherDataModel>>(hasRun, type)

            var timeIndex = -1
            weatherList?.forEachIndexed { index, weatherDataModel1 ->
                if (weatherDataModel1.timeStamp in startTimeStamp..endTimeStamp) {
                    timeIndex = index
                    weatherDataModel = weatherDataModel1
                }
            }
            if (timeIndex != -1) {
                weatherList.removeAt(timeIndex)
                edit.putString(key, gson.toJson(weatherList))
                edit.commit()
            }

//            LOGS.d("GPS_DATA", "getWeatherDataModel onfail" + Gson().toJson(weatherList))
            LOGS.d("GPS_DATA", "getWeatherDataModel onfail" + startTimeStamp)
            LOGS.d("GPS_DATA", "getWeatherDataModel onfail" + endTimeStamp)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return weatherDataModel
    }

    override fun saveAndGetLocation(
        sessionId: Long,
        data: List<LocationDataModel>
    ): List<LocationDataModel> {
        val key = "${WATCH_MAPS_LAT_LONG}_$sessionId"
        val finalLocationList = java.util.ArrayList<LocationDataModel>()
        val gson = Gson()
        val hasRun = mPrefs.getString(key, null)
        val edit = mPrefs.edit()
        if (hasRun == null) {
            finalLocationList.addAll(data)
            edit.putString(key, gson.toJson(data))
        } else {
            val type = object : TypeToken<java.util.ArrayList<LocationDataModel?>?>() {}.type
            val arrayList = gson.fromJson<java.util.ArrayList<LocationDataModel>>(hasRun, type)
            arrayList.addAll(data)
            edit.putString(key, gson.toJson(arrayList))
            finalLocationList.addAll(arrayList)
        }
        edit.commit()
        return finalLocationList
    }

    override fun isInitialOtaChecked(): Boolean {
        return mPrefs.getBoolean(WATCH_INITIAL_OTA_CHECK, false)
    }

    override fun setWatchIgnoreVersion(number: Int) {
        mPrefs.edit()
            ?.putInt(WATCH_IGNORE_VERSION_NUMBER, number)
            ?.apply()
    }

    override fun getWatchIgnoreVersion(): Int {
        return mPrefs.getInt(WATCH_IGNORE_VERSION_NUMBER, 0)

    }

    override fun getDeviceFirmwareDetails(): WatchFirmwareDetails? {
        return gson.fromJson(
            mPrefs.getString(WATCH_FIRMWARE_DETAILS, null),
            WatchFirmwareDetails::class.java
        )

    }

    override fun saveDeviceFirmwareDetails(data: WatchFirmwareDetails) {
        mPrefs.edit()
            ?.putString(WATCH_FIRMWARE_DETAILS, gson.toJson(data))
            ?.commit()
    }

    override fun updateScreenBrightness(level: Int) {
        mPrefs.edit()
            ?.putInt(FEATURE_BRIGHTNESS, level)
            ?.commit()
    }

    override fun getScreenBrightness(): Int {
        return mPrefs.getInt(FEATURE_BRIGHTNESS, 60)
    }

    override fun updateWaterReminder(data: SedentaryData?) {
        mPrefs.edit()
            ?.putString(FEATURE_WATER_REMINDER, gson.toJson(data))
            ?.commit()
    }

    override fun getWaterReminder(): SedentaryData? {
        return gson.fromJson(
            mPrefs.getString(FEATURE_WATER_REMINDER, null),
            SedentaryData::class.java
        )
    }

    override fun updateIdleAlert(data: SedentaryData?) {
        mPrefs.edit()
            ?.putString(FEATURE_IDLE_ALERT, gson.toJson(data))
            ?.commit()
    }

    override fun getIdleAlert(): SedentaryData? {
        return gson.fromJson(
            mPrefs.getString(FEATURE_IDLE_ALERT, null),
            SedentaryData::class.java
        )
    }

    override fun updateMedicineReminder(data: SedentaryData) {
        mPrefs.edit()
            ?.putString(FEATURE_MEDICINE_REMINDER, gson.toJson(data))
            ?.commit()
    }

    override fun setDefaultValue(status: Boolean) {
        mPrefs.edit()?.putBoolean(DEFAULT_VALUE, status)
            ?.commit()
    }

    override fun getDefaultValue(): Boolean {
        return mPrefs.getBoolean(DEFAULT_VALUE, false)
    }

    override fun setContactNumberList(dataList: List<Contact>) {
        mPrefs.edit()
            ?.putString(WATCH_CONTACT_LIST, gson.toJson(dataList))
            ?.commit()
    }

    override fun getContactNumberList(): ArrayList<Contact> {
        return mPrefs.getString(WATCH_CONTACT_LIST, null)
            ?.let { Gson().fromJson<ArrayList<Contact>>(it) } ?: ArrayList()

    }

    override fun getMedicineReminder(): SedentaryData? {
        return gson.fromJson(
            mPrefs.getString(FEATURE_MEDICINE_REMINDER, null),
            SedentaryData::class.java
        )
    }


    override fun getBatteryPercent(): Int {
        return mPrefs.getInt(BATTERY_PERCENT, 0)
    }

    override fun getBatteryPercentRing(): Int {
        return mPrefs.getInt(BATTERY_PERCENT_RING, 0)
    }

    override fun updateBatteryPercent(percent: Int?) {
        percent?.let {
            mPrefs.edit()?.putInt(BATTERY_PERCENT, it)
                ?.commit()
        }
    }

    override fun updateBatteryPercentRing(percent: Int?) {
        percent?.let {
            mPrefs.edit()?.putInt(BATTERY_PERCENT_RING, it)
                ?.commit()
        }
    }

    override fun getHeartRateStatus(): Boolean {
        return mPrefs.getBoolean(HEART_RATE_STATUS, false)
    }

    override fun updateHeartRateStatus(status: Boolean) {
        mPrefs.edit()?.putBoolean(HEART_RATE_STATUS, status)
            ?.commit()
    }

    override fun getBloodOxygenStatus(): Pair<Boolean, Int?> {
        val status = mPrefs.getBoolean(BLOOD_OXYGEN_STATUS, false)
        val interval = mPrefs.getInt(BLOOD_OXYGEN_INTERVAL, 5)
        return Pair(status, interval)
    }

    override fun updateBloodOxygenStatus(data: Pair<Boolean, Int?>) {
        mPrefs.edit()?.putBoolean(BLOOD_OXYGEN_STATUS, data.first)
            ?.commit()
        data.second?.let {
            mPrefs.edit()?.putInt(BLOOD_OXYGEN_INTERVAL, it)
                ?.commit()
        }

    }

    override fun getFirmwareVersion(): String {
        return mPrefs.getString(FIRMWARE_VERSION, "") ?: ""
    }

    override fun updateFirmwareVersion(firmwareVersion: String) {
        mPrefs.edit()?.putString(FIRMWARE_VERSION, firmwareVersion)?.commit()
    }


    override fun updateHeartRateInterval(interval: HeartRateInterval?) {
        mPrefs.edit()
            ?.putString(FEATURE_HEART_RATE_INTERVAL, gson.toJson(interval))
            ?.commit()
    }

    override fun getHeartRateInterval(): HeartRateInterval? {
        return gson.fromJson(
            mPrefs.getString(FEATURE_HEART_RATE_INTERVAL, null),
            HeartRateInterval::class.java
        )
    }

    override fun updateHandWashData(handWashing: HandWashing?) {
        mPrefs.edit()
            ?.putString(FEATURE_HAND_WASH, gson.toJson(handWashing))
            ?.commit()
    }

    override fun getHandWashData(): HandWashing? {
        return gson.fromJson(
            mPrefs.getString(FEATURE_HAND_WASH, null),
            HandWashing::class.java
        )
    }

    override fun saveWorldClockData(data: WorldClocksPushData?) {
        mPrefs.edit()
            ?.putString(WORLD_CLOCK_DATA, gson.toJson(data))
            ?.apply()
    }

    override fun getWorldClockData(): WorldClocksPushData? {
        return gson.fromJson(
            mPrefs.getString(WORLD_CLOCK_DATA, null),
            WorldClocksPushData::class.java
        )
    }

    override fun saveLogPathName(data: String) {
        mPrefs.edit()
            ?.putString(SAVE_LOGS_PATH_NAME, data)
            ?.apply()
    }

    override fun getLogPathName(): String? {
        return mPrefs.getString(SAVE_LOGS_PATH_NAME, null)
    }

    override fun getCustomReplies(): CustomReplyData? {
        return gson.fromJson(
            mPrefs.getString(SAVE_CUSTOM_REPLIES, null),
            CustomReplyData::class.java
        )
    }

    override fun setCustomReplies(customReplyData: CustomReplyData) {
        mPrefs.edit()
            ?.putString(SAVE_CUSTOM_REPLIES, gson.toJson(customReplyData))
            ?.apply()
    }

    override fun getUniqueIdForWatchFaces(): Int {
        return mPrefs.getInt(SAVE_UNIQUE_WATCH_FACE_ID, 1)
    }

    override fun setUniqueIdForWatchFaces() {
        var uniqueID = getUniqueIdForWatchFaces()
        uniqueID += 1
        mPrefs.edit()
            ?.putInt(SAVE_UNIQUE_WATCH_FACE_ID, uniqueID)
            ?.apply()

    }

    override fun clearWatchData() {
        mPrefs.edit().clear().commit()
    }

    override fun setAskForPermission(status: Boolean) {
        mPrefs.edit()
            ?.putBoolean(ASK_PERMISSION, status)
            ?.apply()
    }

    override fun getAskForPermission(): Boolean {
        return mPrefs.getBoolean(ASK_PERMISSION, false)
    }
}