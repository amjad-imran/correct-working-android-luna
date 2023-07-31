package com.noisefit_commans.interfaces.data


import com.noisefit_commans.models.*

interface UserActivityDataCallbacks {
    fun onStepsDataObtained(stepsData: StepsData)
    fun onSleepDataObtained(sleepData: SleepData)
    fun onHeartRateObtained(heartRate: HeartRate)
    fun onBloodOxygenObtained(bloodOxygen: BloodOxygen)
    fun onBloodPressureObtained(bloodPressureData: BloodPressureData)
    fun onStressDataObtained(stressData: StressData)
    fun onUserDataSyncUpdated(syncDataStatus: SyncDataStatus)
    fun onSportsModeStatusChange(syncDataStatus: SyncDataStatus)
    fun onSportsModeDataObtained(sportsModeResponse: SportsModeList)
    fun onHeartHistoryObtained(heartRateHistory: HeartRateHistory)
    fun onSportsModeDataObtainedGPS(sportsModeResponse: SportsModeListGPS)
}