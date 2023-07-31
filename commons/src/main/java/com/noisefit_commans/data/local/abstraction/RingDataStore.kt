package com.noisefit_commans.data.local.abstraction

import com.noisefit_commans.data.model.DeviceFeatures
import com.noisefit_commans.models.ColorFitDevice

interface RingDataStore {
    fun saveRingDevice(noiseFitDevice: ColorFitDevice): Boolean
    fun getRingDevice(): ColorFitDevice?

    fun clearConnectedDevice()

    fun getLastInfoFetchTime(): Long
    fun setLastInfoFetchTime(timeStamp: Long)

    fun getLastPeriodicDataSyncTime(): Long
    fun setLastPeriodicDataSyncTime(timeStamp: Long)

    fun updateDeviceToken(token: String?)
    fun getDeviceToken(): String?

    fun saveDeviceFeatures(deviceFeatures: DeviceFeatures)
    fun getDeviceFeatures(): DeviceFeatures?

    fun setLastSyncWithServer(timeStamp: Long)
    fun getLastSyncWithServer(): Long

    fun saveLastSyncTimeStamp(timeStamp: Long)
    fun getLastSyncTimeStamp(): Long?

}