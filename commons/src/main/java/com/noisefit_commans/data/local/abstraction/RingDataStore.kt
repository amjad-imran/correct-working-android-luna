package com.noisefit_commans.data.local.abstraction

import com.noisefit_commans.data.model.DeviceFeatures
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.ManualMeasurement

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

    fun setRegisterDay(day: Int)
    fun getRegisterDay(): Int?

    fun setTempBaseLine(temp: Float)
    fun getTempBaseLine(): Float?

    fun setSleepWalkAroundShown(status: Boolean)
    fun isSleepWalkAroundShown(): Boolean

    fun setReadinessWalkAroundShown(status: Boolean)
    fun isReadinessWalkAroundShown(): Boolean

    fun setActivityWalkAroundShown(status: Boolean)
    fun isActivityWalkAroundShown(): Boolean

    fun setManualMeasurementValue(data: ManualMeasurement)
    fun getManualMeasurementValue(): ManualMeasurement?

    fun saveAutoLogsTimeStamp()
    fun getAutoLogsTimeStamp(): Long
    fun isShowDeviceIntro():Boolean
    fun setShowDeviceIntro(boolean: Boolean)
}