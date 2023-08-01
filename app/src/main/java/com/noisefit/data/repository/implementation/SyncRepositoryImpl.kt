package com.noisefit.data.repository.implementation

import com.google.gson.Gson
import com.noisefit.luna.BuildConfig.BASE_URL
import com.noisefit.luna.BuildConfig.BASE_URL_NEW
import com.noisefit.data.dataConverter.OfflineDataMapper
import com.noisefit.data.dataConverter.OnlineDataMapper
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.implementation.*
import com.noisefit_commans.data.model.UserSyncActivities
import com.noisefit_commans.data.model.UserSyncRawData
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.VersionCheckResponse
import com.noisefit.data.repository.LastSyncItems
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.SyncRepository
import com.noisefit.data.repository.abstraction.UserActivityRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit.data.safeCacheCall
import com.noisefit_commans.utils.EncryptUtils
import com.noisefit.util.TestModeUtils
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.models.*
import com.noisefit_commans.response.SleepBreakup
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.db.implementation.OreoAutoSportDataImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class SyncRepositoryImpl(
    private val localDatSource: DataStoredInterface,
    private val remoteDataSource: NetworkService,
    private val stepsDataImpl: StepsDataImpl,
    private val stressDataImpl: StressDataImpl,
    private val heartRateDataImpl: HeartRateDataImpl,
    private val bloodOxygenDataImpl: BloodOxygenDataImpl,
    private val sleepDataImpl: SleepDataImpl,
    private val googleFitDataImpl: GoogleFitDataImpl,
    private val bodyTemperatureDataImpl: BodyTemperatureDataImpl,
    private val offlineDataMapper: OfflineDataMapper,
    private val gson: Gson,
    private val onlineDataMapper: OnlineDataMapper,
    private val encryptUtils: EncryptUtils,
    private val lastSyncProvider: LastSyncProvider,
    private val userActivityRepository: UserActivityRepository,
    private val testModeUtils: TestModeUtils,

    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SyncRepository {

    override suspend fun saveStepsData(data: StepsData): Flow<CacheResult<StepsData?>> {
        return safeCacheCall(Dispatchers.IO) {
            stepsDataImpl.syncInsertOrUpdate(data)
        }
    }

    override suspend fun rescueTablesToCrash(): Flow<CacheResult<Unit?>> {

        val stepArray = ArrayList<StepsData.StepDataBreakup>()
        for (i in 0..23) {
            stepArray.add(StepsData.StepDataBreakup(0, 0, 0, 0, i))
        }
        return safeCacheCall(Dispatchers.IO) {
            stepsDataImpl.updateNullStepArray(stepArray)
        }
    }

    override suspend fun saveSleepData(data: SleepData): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            sleepDataImpl.insertData(
                data
            )
        }
    }

    override suspend fun saveStressData(
        data: List<StressDataBreakup>
    ): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            stressDataImpl.insertData(
                data
            )
        }
    }

    override suspend fun saveHeartRateData(
        data: List<HeartRate>
    ): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            heartRateDataImpl.insertData(
                data
            )
        }
    }

    override suspend fun saveBloodOxygenData(
        data: List<BloodOxygenBreakup>
    ): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            bloodOxygenDataImpl.insertData(
                data
            )
        }
    }


    override suspend fun saveBodyTemperatureData(data: List<BodyTemperatureBreakup>): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            bodyTemperatureDataImpl.insertData(
                data
            )
        }
    }

    override suspend fun postDataToServer(data: UserSyncActivities): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<VersionCheckResponse>>> {
        return safeApiCallFlow(dispatcher) {


            if (!data.stepsDataList.isNullOrEmpty()) {
                lastSyncProvider.removeSyncTimeStamp(
                    listOf(
                        LastSyncItems.HEALTH_STEPS_DAY, LastSyncItems.HEALTH_STEPS_WEEK,
                        LastSyncItems.HEALTH_STEPS_MONTH, LastSyncItems.HEALTH_STEPS_YEAR,
                        LastSyncItems.HEALTH_STEPS_HIGHLIGHT
                    )
                )
                userActivityRepository.removeLocalChallengesData()
                userActivityRepository.removeLocalFriendsData()
                userActivityRepository.removeLocalStreakData()
            }
            if (!data.stressData.isNullOrEmpty()) {
                lastSyncProvider.removeSyncTimeStamp(
                    listOf(
                        LastSyncItems.HEALTH_STRESS_DAY, LastSyncItems.HEALTH_STRESS_WEEK,
                        LastSyncItems.HEALTH_STRESS_MONTH, LastSyncItems.HEALTH_STRESS_YEAR
                    )
                )
            }
            if (!data.boData.isNullOrEmpty()) {
                lastSyncProvider.removeSyncTimeStamp(
                    listOf(
                        LastSyncItems.HEALTH_BO_DAY, LastSyncItems.HEALTH_BO_WEEK,
                        LastSyncItems.HEALTH_BO_MONTH, LastSyncItems.HEALTH_BO_YEAR
                    )
                )
            }
            if (!data.hrHistoryData.isNullOrEmpty()) {
                lastSyncProvider.removeSyncTimeStamp(
                    listOf(
                        LastSyncItems.HEALTH_HR_DAY, LastSyncItems.HEALTH_HR_WEEK,
                        LastSyncItems.HEALTH_HR_MONTH, LastSyncItems.HEALTH_HR_YEAR
                    )
                )
            }

            if (!data.bodyTemperature.isNullOrEmpty()) {
                lastSyncProvider.removeSyncTimeStamp(
                    listOf(
                        LastSyncItems.HEALTH_BODY_TEMP_DAY, LastSyncItems.HEALTH_BODY_TEMP_WEEK,
                        LastSyncItems.HEALTH_BODY_TEMP_MONTH, LastSyncItems.HEALTH_BODY_TEMP_YEAR,
                    )
                )
            }




            onlineDataMapper.convertDataToPost(data)?.let {
                localDatSource.getConnectedDevice()?.let { device ->
                    it.deviceType = device.deviceType
                }
                val url = "$BASE_URL_NEW/multisync_v2/create"
                remoteDataSource.postCombinedHistoryData(url, it)
            }

        }

    }

    override suspend fun postSleepHistoryData(data: UserSyncActivities): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<VersionCheckResponse>>> {
        return safeApiCallFlow(dispatcher) {
            lastSyncProvider.removeSyncTimeStamp(
                listOf(
                    LastSyncItems.HEALTH_SLEEP_DAY,
                    LastSyncItems.HEALTH_SLEEP_WEEK,
                    LastSyncItems.HEALTH_SLEEP_MONTH,
                    LastSyncItems.HEALTH_SLEEP_YEAR,
                    LastSyncItems.HEALTH_SLEEP_HIGHLIGHT
                )
            )

            onlineDataMapper.getSleepDataToPost(data).let {
                localDatSource.getConnectedDevice()?.let { device ->
                    it.deviceType = device.deviceType
                }
                val url = "$BASE_URL/sleep_activities/v3/rewamp/create"
                remoteDataSource.postSleepHistoryData(url, it)
            }

        }

    }


    override suspend fun updateSleepHashForLastSyncData(userSyncActivities: UserSyncActivities) {
        userSyncActivities.sleepData?.let {
            localDatSource.setSleepLastSyncHash(
                encryptUtils.md5(
                    gson.toJson(
                        it
                    )
                )
            )
        }
    }

    override suspend fun updateHashForLastSyncData(userSyncActivities: UserSyncActivities) {

        userSyncActivities.stepsDataList?.let {
            localDatSource.setStepsLastSyncHash(
                encryptUtils.md5(
                    gson.toJson(
                        it
                    )
                )
            )
        }


        userSyncActivities.stressData?.let {
            localDatSource.setStressLastSyncHash(
                encryptUtils.md5(
                    gson.toJson(
                        it
                    )
                )
            )
        }
        userSyncActivities.boData?.let {
            localDatSource.setBloodOxygenLastSyncHash(
                encryptUtils.md5(
                    gson.toJson(
                        it
                    )
                )
            )
        }

        if (testModeUtils.saveHrHash()) {
            userSyncActivities.hrHistoryData?.let {
                localDatSource.setHeartLastSyncHash(
                    encryptUtils.md5(
                        gson.toJson(
                            it
                        )
                    )
                )
            }
        }


        userSyncActivities.bodyTemperature?.let {
            localDatSource.setBodyTempSyncHash(
                encryptUtils.md5(
                    gson.toJson(
                        it
                    )
                )
            )
        }
    }

    override suspend fun checkHalfSyncData() {
        stressDataImpl.checkHalfSyncData()
        heartRateDataImpl.checkHalfSyncData()
        bodyTemperatureDataImpl.checkHalfSyncData()
        bloodOxygenDataImpl.checkHalfSyncData()
    }

    override suspend fun getTodaySteps(): Flow<CacheResult<StepsData?>> {
        val todayDate = DateFormats.getTodaysDateString(7)
        return safeCacheCall(dispatcher) {
            stepsDataImpl.getTodayData(todayDate)
        }
    }

    override suspend fun getTodayHeartRate(): Flow<CacheResult<List<HeartRate>?>> {
        val todayDate = DateFormats.getTodaysDateString(7)
        return safeCacheCall(dispatcher) {
            heartRateDataImpl.getTodayData(todayDate)
        }
    }

    override suspend fun getTodayStress(): Flow<CacheResult<List<StressDataBreakup>?>> {
        val todayDate = DateFormats.getTodaysDateString(7)
        return safeCacheCall(dispatcher) {
            stressDataImpl.getTodayData(todayDate)
        }
    }

    override suspend fun getTodayBloodOxygen(): Flow<CacheResult<List<BloodOxygenBreakup>?>> {
        val todayDate = DateFormats.getTodaysDateString(7)
        return safeCacheCall(dispatcher) {
            bloodOxygenDataImpl.getTodayData(todayDate)
        }
    }

    override suspend fun getTodaySleep(): Flow<CacheResult<SleepBreakup?>> {
        val todayDate = DateFormats.getTodaysDateString(7)
        return safeCacheCall(dispatcher) {
            offlineDataMapper.convertSleepDataForDetailsView(
                offlineDataMapper.convertSleepData(
                    sleepDataImpl.getTodayData(todayDate)
                )
            )
        }
    }

    override suspend fun getTodaySleepData(): Flow<CacheResult<SleepData?>> {
        val todayDate = DateFormats.getTodaysDateString(7)
        return safeCacheCall(dispatcher) {
            offlineDataMapper.convertSleepData(
                sleepDataImpl.getTodayData(todayDate)
            )
        }
    }


    override suspend fun getTodayBodyTemp(): Flow<CacheResult<List<BodyTemperatureBreakup>?>> {
        val todayDate = DateFormats.getTodaysDateString(7)
        return safeCacheCall(dispatcher) {
            bodyTemperatureDataImpl.getTodayData(todayDate)
        }
    }

    override suspend fun deleteSleepServerSyncData(data: UserSyncRawData) {
        val todayTimeStampForSleep = DateFormats.convertTimeStampToPrevious12ofDay(
            DateFormats.subtractDate(
                DateFormats.getTimeStamp(),
                1
            )
        )
        data.sleepData?.let {
            sleepDataImpl.updateServerSyncData(it, todayTimeStampForSleep)
        }

    }

    override suspend fun deleteServerSyncData(data: UserSyncRawData) {
        val todayTimeStamp = DateFormats.convertTimeStampToStartOfDay(DateFormats.getTimeStamp())
        val todayTimeStampForSleep = DateFormats.convertTimeStampToPrevious12ofDay(
            DateFormats.subtractDate(
                DateFormats.getTimeStamp(),
                1
            )
        )
        LOGS.d("deleteServerSyncData $todayTimeStamp $todayTimeStampForSleep")

        data.stepsDataList?.let {
            stepsDataImpl.updateServerSyncData(it, todayTimeStamp)
        }


        data.hrHistoryData?.let {
            heartRateDataImpl.updateServerSyncData(it, todayTimeStamp)
        }

        data.stressData?.let {
            stressDataImpl.updateServerSyncData(it, todayTimeStamp)
        }

        data.bodyTemperature?.let {
            bodyTemperatureDataImpl.updateServerSyncData(it, todayTimeStamp)
        }

        data.boData?.let {
            bloodOxygenDataImpl.updateServerSyncData(it, todayTimeStamp)
        }

    }
    //1643049000009


    //TODO: Need to Think about a way to handle it more efficiently
    override suspend fun getUnSyncUserActivities(): Pair<UserSyncActivities, UserSyncRawData> {

        checkHalfSyncData()

        val nDayStartingTimeStamp = DateFormats.getNDayStartingTimeStamp()

        val userSyncRawData = UserSyncRawData()

        userSyncRawData.stepsDataList =
            stepsDataImpl.getUnSyncServerData(nDayStartingTimeStamp, false)
        userSyncRawData.hrHistoryData =
            heartRateDataImpl.getUnSyncServerData(nDayStartingTimeStamp, false)
        userSyncRawData.stressData =
            stressDataImpl.getUnSyncServerData(nDayStartingTimeStamp, false)
        userSyncRawData.boData =
            bloodOxygenDataImpl.getUnSyncServerData(nDayStartingTimeStamp, false)
        userSyncRawData.sleepData = sleepDataImpl.getUnSyncServerData(nDayStartingTimeStamp, false)
        userSyncRawData.bodyTemperature =
            bodyTemperatureDataImpl.getUnSyncServerData(nDayStartingTimeStamp, false)

//        LOGS.d("getUnSyncUserActivities ${Gson().toJson(userSyncRawData.hrHistoryData)}")
        var heartRateData = offlineDataMapper.convertUnSyncHeartRateDataListToObject(
            userSyncRawData.hrHistoryData
        )


        var stressData = offlineDataMapper.convertUnSyncStressDataListToObject(
            userSyncRawData.stressData
        )


        var bloodOxygenData = offlineDataMapper.convertUnSyncBloodOxygenDataListToObject(
            userSyncRawData.boData
        )

        // LOGS.d("SYNC_TIME ${Gson().toJson(bloodOxygenData)}")
        var sleepData = offlineDataMapper.convertUnSyncSleepDataListToObject(
            userSyncRawData.sleepData
        )

        var bodyTempData = offlineDataMapper.convertUnSyncBodyTempDataListToObject(
            userSyncRawData.bodyTemperature
        )

        if (encryptUtils.md5(gson.toJson(userSyncRawData.stepsDataList)) == localDatSource.getStepsLastSyncHash() || userSyncRawData.stepsDataList.isNullOrEmpty()) {
            userSyncRawData.stepsDataList = null
        }


        if (testModeUtils.saveHrHash()) {
            if (encryptUtils.md5(gson.toJson(heartRateData)) == localDatSource.getHeartLastSyncHash() || heartRateData.isNullOrEmpty()) {
                userSyncRawData.hrHistoryData = null
                heartRateData = null
            }
        }


        if (encryptUtils.md5(gson.toJson(stressData)) == localDatSource.getStressLastSyncHash() || stressData.isNullOrEmpty()) {
            userSyncRawData.stressData = null
            stressData = null
        }

        if (encryptUtils.md5(gson.toJson(bloodOxygenData)) == localDatSource.getBloodOxygenLastSyncHash() || bloodOxygenData.isNullOrEmpty()) {
            userSyncRawData.boData = null
            bloodOxygenData = null
        }

        if (encryptUtils.md5(gson.toJson(sleepData)) == localDatSource.getSleepLastSyncHash() || sleepData.isNullOrEmpty()) {
            userSyncRawData.sleepData = null
            sleepData = null
        }

        if (encryptUtils.md5(gson.toJson(bodyTempData)) == localDatSource.getBodyTempSyncHash() || bodyTempData.isNullOrEmpty()) {
            userSyncRawData.bodyTemperature = null
            bodyTempData = null
        }


        return Pair(
            UserSyncActivities(
                stepsDataList = userSyncRawData.stepsDataList,
                hrHistoryData = heartRateData,
                stressData = stressData,
                boData = bloodOxygenData,
                sleepData = sleepData,
                bodyTemperature = bodyTempData
            ), userSyncRawData
        )

    }

    override suspend fun getGoogleFitUnSyncData(date: String): SyncGoogleFitData {
        return googleFitDataImpl.getUnSyncedData(date)
    }


    override suspend fun updateGoogleFitUnSyncHeartRateStatus(heartRateList: List<HeartRate>) {
        googleFitDataImpl.updateSyncHeartRateStatus(heartRateList)
    }

    override suspend fun updateGoogleFitUnSyncStepsStatus(date: String, data: StepDataGoogleFit) {
        googleFitDataImpl.updateSyncStepsStatus(date, data)
    }

    override suspend fun updateGoogleFitUnSyncSleepStatus(sleepData: SleepData) {
        googleFitDataImpl.updateSyncSleepStatus(sleepData)
    }


}