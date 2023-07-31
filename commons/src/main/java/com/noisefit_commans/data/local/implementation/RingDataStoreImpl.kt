package com.noisefit_commans.data.local.implementation

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.DeviceFeatures
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import javax.inject.Inject

private const val RING_DEVICE_INFO = "RING_DEVICE_INFO"
private const val LAST_INFO_FETCH_TIME = "LAST_INFO_FETCH_TIME"
private const val PERIODIC_DATA_TIMESTAMP = "PERIODIC_DATA_TIMESTAMP"
private const val DEVICE_TOKEN = "device_token"
private const val DEVICE_FEATURES = "device_features"
private const val LAST_SYNC_WITH_SERVER = "LAST_SYNC_WITH_SERVER"
private const val LAST_SYNC = "LAST_SYNC"


private inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

class RingDataStoreImpl
@Inject constructor(
    private val gson: Gson,
    private val mPrefs: SharedPreferences
) : RingDataStore {
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

    override fun saveLastSyncTimeStamp(timeStamp: Long) {
        mPrefs.edit()?.putLong(LAST_SYNC, timeStamp)?.commit()
    }


}