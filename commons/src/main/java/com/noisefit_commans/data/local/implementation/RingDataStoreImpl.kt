package com.noisefit_commans.data.local.implementation

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.DeviceFeatures
import com.noisefit_commans.data.model.OWorkoutListModal
import com.noisefit_commans.data.model.customHomeScreen.CustomHomeScreenModel
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.GoogleFitDataLastSync
import com.noisefit_commans.models.ManualMeasurement
import com.noisefit_commans.utils.DateFormats
import java.time.LocalDate
import javax.inject.Inject

private const val RING_DEVICE_INFO = "RING_DEVICE_INFO"
private const val LAST_INFO_FETCH_TIME = "LAST_INFO_FETCH_TIME"
private const val PERIODIC_DATA_TIMESTAMP = "PERIODIC_DATA_TIMESTAMP"
private const val DEVICE_TOKEN = "device_token"
private const val DEVICE_FEATURES = "device_features"
private const val LAST_SYNC_WITH_SERVER = "LAST_SYNC_WITH_SERVER"
private const val LAST_SYNC = "LAST_SYNC"
private const val LAST_SYNC_LOGS = "LAST_SYNC_LOGS"

private const val REGISTER_DAY_KEY = "REGISTER_DAY_KEY"
private const val SLEEP_WALKAROUND_KEY = "SLEEP_WALKAROUND_KEY"
private const val READINESS_WALKAROUND_KEY = "READINESS_WALKAROUND_KEY"
private const val ACTIVITY_WALKAROUND_KEY = "ACTIVITY_WALKAROUND_KEY"
private const val MANUAL_MEASUREMENT_KEY = "MANUAL_MEASUREMENT_KEY"
private const val MANUAL_MEASUREMENT_KEY_STRESS = "MANUAL_MEASUREMENT_KEY_STRESS"
private const val DEVICE_INTRO = "DEVICE_INTRO"
private const val RECORD_DELETE_LIST = "RECORD_DELETE_LIST"

private const val RECORD_WORKOUT_TIMESTAMP = "RECORD_WORKOUT_TIMESTAMP"
private const val RECORD_WORKOUT_MODEL = "RECORD_WORKOUT_MODEL"
private const val TEMP_BASE_LINE = "TEMP_BASE_LINE"

private const val OTA_VERSION_NEW = "OTA_VERSION_NEW"
private const val OTA_VERSION_NEW_TIMESTAMP = "OTA_VERSION_NEW_TIMESTAMP"
private const val OTA_VERSION_REMIND = "OTA_VERSION_REMIND"
private const val OTA_VERSION_CURRENT = "OTA_VERSION_CURRENT"
private const val FIRST_STRESS_DAY = "FIRST_STRESS_DAY"
private const val STRESS_BETA_STATE = "STRESS_BETA_STATE"
private const val ENABLE_AI_STATE_2 = "ENABLE_AI_STATE_2"
private const val SLEEP_ALERT_REMOVE = "SLEEP_ALERT_REMOVE"
private const val RING_PAIR_DATE = "RING_PAIR_DATE"
private const val LAST_SYNC_STEPS = "LAST_SYNC_STEPS"

private const val UPDATE_USER_DEVICE_STATUS = "UPDATE_USER_DEVICE_STATUS"

//
private const val CUSTOMIZE_HOME_SCREEN = "CUSTOMIZE_HOME_SCREEN"
private const val CANNY_STATE = "CANNY_STATE"
//

private inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

class RingDataStoreImpl
@Inject constructor(
    private val gson: Gson,
    private val mPrefs: SharedPreferences
) : RingDataStore {

    override fun setCannyState(enableCanny: Boolean) {
        mPrefs.edit().putBoolean(CANNY_STATE, enableCanny).commit()

    }

    override fun getCannyState(): Boolean {
        return mPrefs.getBoolean(CANNY_STATE, false)
    }

    override fun getLastSyncedStepsData(): GoogleFitDataLastSync? {
        val lastSyncData = Gson().fromJson<GoogleFitDataLastSync>(
            mPrefs.getString(LAST_SYNC_STEPS, "") ?: ""
        )
        return lastSyncData
    }

    override fun setLastSyncedStepsData(data: GoogleFitDataLastSync) {
        mPrefs.edit().putString(LAST_SYNC_STEPS, gson.toJson(data)).commit()

    }

    //
    override fun getCustomHomeScreenData(): CustomHomeScreenModel? {
        val data = mPrefs.getString(CUSTOMIZE_HOME_SCREEN, null)
        return if(data.isNullOrEmpty()){
            null
        }else{
            gson.fromJson(data, CustomHomeScreenModel::class.java)
        }
    }

    override fun setCustomHomeScreenData(data: CustomHomeScreenModel) {
        mPrefs.edit()?.putString(CUSTOMIZE_HOME_SCREEN, gson.toJson(data))?.apply()
    }
    //

    override fun saveRingPairedDate() {
        mPrefs.edit()?.putString(RING_PAIR_DATE, LocalDate.now().toString())?.commit()
    }

    override fun getRingPairedDate(): String? {
        return mPrefs.getString(RING_PAIR_DATE, null)
    }

    override fun removeSleepAlert(date: String) {
        mPrefs.edit()?.putString(SLEEP_ALERT_REMOVE, date)?.commit()
    }

    override fun sleepAlertCrossedForDate(): String? {
        return mPrefs.getString(SLEEP_ALERT_REMOVE, null)
    }

    override fun getEnableAiState(): Boolean {
        return mPrefs.getBoolean(ENABLE_AI_STATE_2, false)

    }

    override fun setEnableAiState(state: Boolean) {
        mPrefs.edit()?.putBoolean(ENABLE_AI_STATE_2, state)?.commit()

    }

    override fun getFirstStressDay(): String? {
        return mPrefs.getString(FIRST_STRESS_DAY, null)
    }

    override fun setFirstStressDay(firstStress: String?) {
        mPrefs.edit()?.putString(FIRST_STRESS_DAY, firstStress)?.commit()
    }

    override fun getStressBetaState(): Boolean? {
        return mPrefs.getBoolean(STRESS_BETA_STATE, false)
    }

    override fun setStressBetaState(state: Boolean?) {
        mPrefs.edit()?.putBoolean(STRESS_BETA_STATE, state ?: false)?.commit()
    }

    override fun isUpdateUserDeviceDone(): Boolean {
        return mPrefs.getBoolean(UPDATE_USER_DEVICE_STATUS, false)
    }

    override fun setUpdateUserDeviceStatus(status: Boolean) {
        mPrefs.edit()?.putBoolean(UPDATE_USER_DEVICE_STATUS, status)?.commit()
    }

    override fun saveNewOtaVersion(newOtaData: String?, currentVersion: Int) {
        mPrefs.edit()?.putString(OTA_VERSION_NEW, newOtaData)?.commit()
        mPrefs.edit()?.putInt(OTA_VERSION_CURRENT, currentVersion)?.commit()
        saveOtaVersionCheckTimeStamp()
    }

    override fun getNewOtaVersion(): Triple<String, Int, Long>? {
        val gson = mPrefs.getString(OTA_VERSION_NEW, null)
        return if (gson == null) {
            null
        } else {
            val timestamp = mPrefs.getLong(OTA_VERSION_NEW_TIMESTAMP, 0L)

            Triple(
                gson,
                mPrefs.getInt(OTA_VERSION_CURRENT, -1),
                timestamp
            )
        }
    }

    override fun saveOtaVersionCheckTimeStamp() {
        mPrefs.edit()?.putLong(OTA_VERSION_NEW_TIMESTAMP, System.currentTimeMillis())?.commit()
    }

    override fun getOtaVersionCheckTimeStamp(): Long {
        return mPrefs.getLong(OTA_VERSION_NEW_TIMESTAMP, 0)
    }

    override fun cleaNewOtaVersion() {
        mPrefs.edit()?.remove(OTA_VERSION_NEW)?.commit()
        mPrefs.edit()?.remove(OTA_VERSION_CURRENT)?.commit()
        mPrefs.edit()?.remove(OTA_VERSION_REMIND)?.commit()
        mPrefs.edit()?.remove(OTA_VERSION_NEW_TIMESTAMP)?.commit()
    }

    override fun saveOtaRemindDate() {
        mPrefs.edit()?.putString(OTA_VERSION_REMIND, DateFormats.getCurrentDate())?.commit()
    }

    override fun getOtaRemindDate(): String? {
        return mPrefs.getString(OTA_VERSION_REMIND, null)
    }

    override fun saveOngoingRecordWorkout(pair: Pair<Long, OWorkoutListModal>) {
        mPrefs.edit().putLong(RECORD_WORKOUT_TIMESTAMP, pair.first).commit()
        mPrefs.edit().putString(RECORD_WORKOUT_MODEL, gson.toJson(pair.second)).commit()
    }

    override fun getOngoingRecordWorkout(): Pair<Long, OWorkoutListModal>? {

        val model = Gson().fromJson<OWorkoutListModal>(
            mPrefs.getString(RECORD_WORKOUT_MODEL, "") ?: ""
        )
        val timeStamp = mPrefs.getLong(RECORD_WORKOUT_TIMESTAMP, 0L)

        if (model == null || timeStamp == 0L) {
            return null
        }
        return Pair(timeStamp, model)
    }

    override fun deleteOngoingRecordWorkout() {
        mPrefs.edit().remove(RECORD_WORKOUT_TIMESTAMP).commit()
        mPrefs.edit().remove(RECORD_WORKOUT_MODEL).commit()

    }


    override fun addToRecordDeleteList(sportStartTime: Long) {
        var prevList = Gson().fromJson<HashSet<Long>>(
            mPrefs.getString(RECORD_DELETE_LIST, "") ?: ""
        )
        if (prevList == null) {
            prevList = HashSet()
        }
        prevList.add(sportStartTime)

        mPrefs.edit().putString(RECORD_DELETE_LIST, Gson().toJson(prevList)).commit()
    }

    override fun removeRecordDeleteList() {
        mPrefs.edit().remove(RECORD_DELETE_LIST).commit()
    }

    override fun getRecordDeleteList(): HashSet<Long> {
        val prevList = Gson().fromJson<HashSet<Long>>(
            mPrefs.getString(RECORD_DELETE_LIST, "") ?: ""
        )
        return prevList ?: HashSet()
    }


    override fun getTempBaseLine(): Float {
        return mPrefs.getFloat(TEMP_BASE_LINE, 98.6f)
    }

    override fun setTempBaseLine(temp: Float) {
        mPrefs.edit()?.putFloat(TEMP_BASE_LINE, temp)?.apply()

    }

    override fun saveRingDevice(noiseFitDevice: ColorFitDevice): Boolean {
        return mPrefs.edit()?.putString(RING_DEVICE_INFO, gson.toJson(noiseFitDevice))?.commit()
            ?: false
    }

    override fun getRingDevice(): ColorFitDevice? {
        return gson.fromJson(
            mPrefs.getString(RING_DEVICE_INFO, null), ColorFitDevice::class.java
        )
    }

    override fun clearConnectedDevice() {
        mPrefs.edit().remove(RING_DEVICE_INFO).commit()
        mPrefs.edit().remove(RING_PAIR_DATE).commit()
        mPrefs.edit().remove(UPDATE_USER_DEVICE_STATUS).commit()

    }

    override fun getLastInfoFetchTime(): Long {
        return mPrefs.getLong(LAST_INFO_FETCH_TIME, 0)
    }

    override fun setLastInfoFetchTime(timeStamp: Long) {
        mPrefs.edit()?.putLong(LAST_INFO_FETCH_TIME, timeStamp)?.apply()
    }

    override fun getLastPeriodicDataSyncTime(): Long {
        return mPrefs.getLong(PERIODIC_DATA_TIMESTAMP, DateFormats.getTimeStamp())
    }

    override fun setLastPeriodicDataSyncTime(timeStamp: Long) {
        mPrefs.edit()?.putLong(PERIODIC_DATA_TIMESTAMP, timeStamp)?.apply()
    }

    override fun updateDeviceToken(token: String?) {
        mPrefs.edit()?.putString(DEVICE_TOKEN, token)?.apply()
    }

    override fun getDeviceToken(): String? {
        return mPrefs.getString(DEVICE_TOKEN, null)
    }

    override fun saveDeviceFeatures(deviceFeatures: DeviceFeatures) {
        mPrefs.edit()?.putString(DEVICE_FEATURES, gson.toJson(deviceFeatures))?.commit()
    }

    override fun getDeviceFeatures(): DeviceFeatures? {
        return gson.fromJson(mPrefs.getString(DEVICE_FEATURES, null), DeviceFeatures::class.java)
    }

    override fun setLastSyncWithServer(timeStamp: Long) {
        mPrefs.edit()?.putLong(LAST_SYNC_WITH_SERVER, timeStamp)?.apply()
    }

    override fun getLastSyncWithServer(): Long {
        return mPrefs.getLong(LAST_SYNC_WITH_SERVER, 0)
    }

    override fun getLastSyncTimeStamp(): Long {
        return mPrefs.getLong(LAST_SYNC, -1)
    }

    override fun setRegisterDay(day: Int) {
        mPrefs.edit()?.putInt(REGISTER_DAY_KEY, day)?.apply()
    }

    override fun getRegisterDay(): Int {
        return mPrefs.getInt(REGISTER_DAY_KEY, -1)
    }

    override fun setSleepWalkAroundShown(status: Boolean) {
        mPrefs.edit()?.putBoolean(SLEEP_WALKAROUND_KEY, status)?.apply()
    }

    override fun isSleepWalkAroundShown(): Boolean {
        return mPrefs.getBoolean(SLEEP_WALKAROUND_KEY, false)
    }

    override fun setReadinessWalkAroundShown(status: Boolean) {
        mPrefs.edit()?.putBoolean(READINESS_WALKAROUND_KEY, status)?.apply()
    }

    override fun isReadinessWalkAroundShown(): Boolean {
        return mPrefs.getBoolean(READINESS_WALKAROUND_KEY, false)
    }

    override fun setActivityWalkAroundShown(status: Boolean) {
        mPrefs.edit()?.putBoolean(ACTIVITY_WALKAROUND_KEY, status)?.apply()
    }

    override fun isActivityWalkAroundShown(): Boolean {
        return mPrefs.getBoolean(ACTIVITY_WALKAROUND_KEY, false)
    }

    override fun setManualMeasurementValue(data: ManualMeasurement) {
        mPrefs.edit()?.putString(MANUAL_MEASUREMENT_KEY, gson.toJson(data))?.commit()
    }

    override fun getManualMeasurementValue(): ManualMeasurement? {
        return gson.fromJson(
            mPrefs.getString(MANUAL_MEASUREMENT_KEY, null),
            ManualMeasurement::class.java
        )
    }

    override fun setManualMeasurementValueStress(data: ManualMeasurement) {
        mPrefs.edit()?.putString(MANUAL_MEASUREMENT_KEY_STRESS, gson.toJson(data))?.commit()
    }

    override fun getManualMeasurementValueStress(): ManualMeasurement? {
        return gson.fromJson(
            mPrefs.getString(MANUAL_MEASUREMENT_KEY_STRESS, null),
            ManualMeasurement::class.java
        )
    }

    override fun saveLastSyncTimeStamp(timeStamp: Long) {
        mPrefs.edit()?.putLong(LAST_SYNC, timeStamp)?.commit()
    }

    override fun saveAutoLogsTimeStamp() {
        mPrefs.edit()?.putLong(LAST_SYNC_LOGS, DateFormats.getTimeStamp())?.apply()
    }

    override fun getAutoLogsTimeStamp(): Long {
        return mPrefs.getLong(LAST_SYNC_LOGS, 0L)
    }

    override fun isShowDeviceIntro(): Boolean {
        return mPrefs.getBoolean(DEVICE_INTRO, false)
    }

    override fun setShowDeviceIntro(boolean: Boolean) {
        mPrefs.edit()?.putBoolean(DEVICE_INTRO, boolean)?.apply()
    }

    override fun isNewOtaAvailable(): Boolean {
        return getNewOtaVersion()?.first != null
    }
}