package com.noisefit.data.local.db.abstraction

import com.noisefit_commans.models.*

interface GoogleFitDataSource {
    suspend fun insertOrUpdate(data: GoogleFitData, date: String): GoogleFitData?
    suspend fun insertOrDelete(data: GoogleFitData)
    suspend fun getTodayData(date: String): GoogleFitData?
    suspend fun getUnSyncedData(date: String): SyncGoogleFitData
    suspend fun updateSyncHeartRateStatus(heartRateList: List<HeartRate>)
    suspend fun updateSyncStepsStatus(date: String,data: StepDataGoogleFit)
    suspend fun updateSyncSleepStatus(sleepData:SleepData)
}