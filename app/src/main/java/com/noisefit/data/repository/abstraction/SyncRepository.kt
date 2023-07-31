package com.noisefit.data.repository.abstraction

import com.noisefit.data.local.db.CacheResult
import com.noisefit_commans.data.model.UserSyncActivities
import com.noisefit_commans.data.model.UserSyncRawData
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.VersionCheckResponse
import com.noisefit_commans.models.*
import com.noisefit_commans.response.SleepBreakup
import kotlinx.coroutines.flow.Flow

interface SyncRepository {

    suspend fun saveStepsData(data: StepsData): Flow<CacheResult<StepsData?>>

    suspend fun saveSleepData(data: SleepData): Flow<CacheResult<Boolean?>>

    suspend fun saveStressData(
        data: List<StressDataBreakup>
    ): Flow<CacheResult<Boolean?>>

    suspend fun saveBodyTemperatureData(
        data: List<BodyTemperatureBreakup>
    ): Flow<CacheResult<Boolean?>>

    suspend fun saveHeartRateData(
        data: List<HeartRate>
    ): Flow<CacheResult<Boolean?>>

    suspend fun saveBloodOxygenData(
        data: List<BloodOxygenBreakup>
    ): Flow<CacheResult<Boolean?>>

    suspend fun deleteServerSyncData(data: UserSyncRawData)

    suspend fun deleteSleepServerSyncData(data: UserSyncRawData)

    suspend fun postDataToServer(data: UserSyncActivities): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<VersionCheckResponse>>>?

    suspend fun postSleepHistoryData(data: UserSyncActivities): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<VersionCheckResponse>>>?

    suspend fun getUnSyncUserActivities(): Pair<UserSyncActivities, UserSyncRawData>

    suspend fun getGoogleFitUnSyncData(date: String): SyncGoogleFitData?

    suspend fun updateGoogleFitUnSyncHeartRateStatus(heartRateList: List<HeartRate>)

    suspend fun updateGoogleFitUnSyncSleepStatus(sleepData: SleepData)

    suspend fun updateGoogleFitUnSyncStepsStatus(date: String, data: StepDataGoogleFit)

    suspend fun updateHashForLastSyncData(userSyncActivities: UserSyncActivities)

    suspend fun updateSleepHashForLastSyncData(userSyncActivities: UserSyncActivities)

    suspend fun checkHalfSyncData()

    suspend fun rescueTablesToCrash(): Flow<CacheResult<Unit?>>

    suspend fun getTodaySteps(): Flow<CacheResult<StepsData?>>

    suspend fun getTodayHeartRate(): Flow<CacheResult<List<HeartRate>?>>

    suspend fun getTodayStress(): Flow<CacheResult<List<StressDataBreakup>?>>

    suspend fun getTodayBloodOxygen(): Flow<CacheResult<List<BloodOxygenBreakup>?>>

    suspend fun getTodaySleep(): Flow<CacheResult<SleepBreakup?>>

    suspend fun getTodaySleepData(): Flow<CacheResult<SleepData?>>

    suspend fun getTodayBodyTemp(): Flow<CacheResult<List<BodyTemperatureBreakup>?>>
}