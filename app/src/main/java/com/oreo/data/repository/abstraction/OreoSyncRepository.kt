package com.oreo.data.repository.abstraction

import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.model.DayTimeMovementBreakup
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.data.model.OreoBloodOxygenBreakup
import com.noisefit_commans.data.model.OreoBodyTemperatureBreakup
import com.noisefit_commans.data.model.OreoHeartRate
import com.noisefit_commans.data.model.OreoRespiratoryData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.data.model.OreoStepsData
import com.noisefit_commans.data.model.OreoStressDataBreakup
import com.noisefit_commans.data.model.RecordedWorkoutData
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.VersionCheckResponse
import com.noisefit_commans.models.StepDataGoogleFit
import com.noisefit_commans.response.SleepBreakup
import com.oreo.data.model.OreoUserSyncActivities
import com.oreo.data.model.OreoUserSyncRawData
import kotlinx.coroutines.flow.Flow


interface OreoSyncRepository {

    suspend fun saveStepsData(data: OreoStepsData): Flow<CacheResult<OreoStepsData?>>

    suspend fun getMovementData(date:String): Flow<CacheResult<String?>>

    suspend fun getRecordedWorkouts(): Flow<CacheResult<List<RecordedWorkoutData>?>>
    suspend fun saveRecordedWorkouts(data: List<RecordedWorkoutData>): Flow<CacheResult<Boolean?>>

    suspend fun saveAutoWorkoutData(data: List<OreoAutoSportData>): Flow<CacheResult<Boolean?>>

    suspend fun getAutoWorkoutData(): Flow<CacheResult<List<OreoAutoSportData>?>>

    suspend fun markWorkoutSyncedAll(): Flow<CacheResult<Boolean?>>
    suspend fun deleteAutoWorkoutData(id: Int): Flow<CacheResult<Boolean?>>
    suspend fun markWorkoutSynced(id: Int): Flow<CacheResult<Boolean?>>

    suspend fun saveSleepData(data: OreoSleepData): Flow<CacheResult<Boolean?>>
    suspend fun saveHealthScoreData(score: Int, date: String): Flow<CacheResult<Boolean?>>

    suspend fun getGoogleFitSleepUnSyncData(date: String):  List<OreoSleepData>?
    suspend fun updateGoogleFitSleepUnSyncData(sleepData:  List<OreoSleepData>)
    suspend fun saveStressData(
        data: OreoStressDataBreakup
    ): Flow<CacheResult<Boolean?>>

    suspend fun saveBodyTemperatureData(
        data: OreoBodyTemperatureBreakup
    ): Flow<CacheResult<Boolean?>>

    suspend fun saveHeartRateData(
        data: OreoHeartRate
    ): Flow<CacheResult<Boolean?>>

    suspend fun saveBloodOxygenData(
        data: OreoBloodOxygenBreakup
    ): Flow<CacheResult<Boolean?>>

    suspend fun saveDayTimeMovementData(
        data: DayTimeMovementBreakup
    ): Flow<CacheResult<Boolean?>>

    suspend fun saveRespiratoryData(
        data: OreoRespiratoryData
    ): Flow<CacheResult<Boolean?>>

    suspend fun markDataSynced(data: OreoUserSyncRawData)

    suspend fun deleteServerSyncData(data: OreoUserSyncRawData)

    suspend fun deleteSleepServerSyncData(data: OreoUserSyncRawData)

    suspend fun postDataToServer(data: OreoUserSyncActivities): Flow<Resource<BaseApiResponse<VersionCheckResponse>>>?

    suspend fun postSleepHistoryData(data: OreoUserSyncActivities): Flow<Resource<BaseApiResponse<VersionCheckResponse>>>?

    suspend fun getUnSyncUserActivities(): Pair<OreoUserSyncActivities, OreoUserSyncRawData>

    suspend fun updateGoogleFitUnSyncHeartRateStatus(heartRateList: List<OreoHeartRate>)

    suspend fun updateGoogleFitUnSyncSleepStatus(sleepData: OreoSleepData)

    suspend fun updateGoogleFitUnSyncStepsStatus(date: String, data: StepDataGoogleFit)

    suspend fun updateHashForLastSyncData(userSyncActivities: OreoUserSyncActivities)

    suspend fun updateSleepHashForLastSyncData(userSyncActivities: OreoUserSyncActivities)

    suspend fun checkHalfSyncData()

    suspend fun rescueTablesToCrash(): Flow<CacheResult<Unit?>>

    suspend fun getTodaySteps(): Flow<CacheResult<OreoStepsData?>>

    suspend fun getTodayHeartRate(): Flow<CacheResult<List<OreoHeartRate>?>>

    suspend fun getTodayStress(): Flow<CacheResult<List<OreoStressDataBreakup>?>>

    suspend fun getTodayBloodOxygen(): Flow<CacheResult<List<OreoBloodOxygenBreakup>?>>

    suspend fun getTodaySleep(): Flow<CacheResult<SleepBreakup?>>

    suspend fun getTodaySleepData(): Flow<CacheResult<OreoSleepData?>>

    suspend fun getTodayBodyTemp(): Flow<CacheResult<List<OreoBodyTemperatureBreakup>?>>

    suspend fun removeRecordedWorkouts(): Flow<CacheResult<Boolean?>>

    /*suspend fun saveAndGetGFitWorkout(data: List<GoogleFitWorkoutData>): Flow<CacheResult<List<GoogleFitWorkoutData>?>>

    suspend fun getGFitUnSyncWorkout(): Flow<CacheResult<List<GoogleFitWorkoutData>?>>

    suspend fun updateGFitSyncWorkout(syncData: List<GoogleFitWorkoutData>): Flow<CacheResult<Int?>>*/
}