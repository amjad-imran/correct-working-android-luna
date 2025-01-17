package com.noisefit_commans.data.local.abstraction

import com.noisefit_commans.data.model.DeviceFeatures
import com.noisefit_commans.data.model.OWorkoutListModal
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.GoogleFitData
import com.noisefit_commans.models.GoogleFitDataLastSync
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

    fun setManualMeasurementValueStress(data: ManualMeasurement)
    fun getManualMeasurementValueStress(): ManualMeasurement?

    fun saveAutoLogsTimeStamp()
    fun getAutoLogsTimeStamp(): Long
    fun isShowDeviceIntro(): Boolean
    fun setShowDeviceIntro(boolean: Boolean)


    fun addToRecordDeleteList(sportStartTime: Long)
    fun removeRecordDeleteList()
    fun getRecordDeleteList(): HashSet<Long>

    fun saveOngoingRecordWorkout(pair: Pair<Long, OWorkoutListModal>)
    fun getOngoingRecordWorkout(): Pair<Long, OWorkoutListModal>?
    fun deleteOngoingRecordWorkout()

    fun saveNewOtaVersion(newOtaData: String?, currentVersion: Int)
    fun getNewOtaVersion(): Triple<String, Int, Long>?
    fun saveOtaVersionCheckTimeStamp()
    fun getOtaVersionCheckTimeStamp(): Long
    fun cleaNewOtaVersion()
    fun saveOtaRemindDate()
    fun getOtaRemindDate(): String?

    fun isUpdateUserDeviceDone(): Boolean
    fun setUpdateUserDeviceStatus(status: Boolean)

    fun isNewOtaAvailable(): Boolean

    fun getFirstStressDay(): String?
    fun setFirstStressDay(firstStress: String?)

    fun getStressBetaState(): Boolean?
    fun setStressBetaState(state: Boolean?)

    fun getEnableAiState(): Boolean
    fun setEnableAiState(state: Boolean)

    fun removeSleepAlert(date: String)
    fun sleepAlertCrossedForDate(): String?

    fun saveRingPairedDate()
    fun getRingPairedDate(): String?

    fun getLastSyncedStepsData(): GoogleFitDataLastSync?
    fun setLastSyncedStepsData(data: GoogleFitDataLastSync)

}