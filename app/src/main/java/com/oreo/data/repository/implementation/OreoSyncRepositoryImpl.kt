package com.oreo.data.repository.implementation

import com.google.gson.Gson
import com.noisefit.data.dataConverter.OfflineDataMapper
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.safeApiCallFlow
import com.noisefit.data.safeCacheCall
import com.noisefit.luna.BuildConfig
import com.noisefit.util.TestModeUtils
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.DayTimeMovementBreakup
import com.noisefit_commans.data.model.GoogleFitWorkoutData
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.data.model.OreoBloodOxygenBreakup
import com.noisefit_commans.data.model.OreoBodyStressData
import com.noisefit_commans.data.model.OreoBodyTemperatureBreakup
import com.noisefit_commans.data.model.OreoHeartRate
import com.noisefit_commans.data.model.OreoNapData
import com.noisefit_commans.data.model.OreoRespiratoryData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.data.model.OreoStepsData
import com.noisefit_commans.data.model.OreoStressDataBreakup
import com.noisefit_commans.data.model.RecordedWorkoutData
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.VersionCheckResponse
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.models.StepDataGoogleFit

import com.noisefit_commans.response.SleepBreakup
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.EncryptUtils
import com.oreo.data.dataConverter.OreoOnlineDataMapper
import com.oreo.data.db.abstaction.OreoNapDataSource
import com.oreo.data.db.implementation.OreoAutoSportDataImpl
import com.oreo.data.db.implementation.OreoBloodOxygenDataImpl
import com.oreo.data.db.implementation.OreoBodyStressDataImpl
import com.oreo.data.db.implementation.OreoBodyTemperatureDataImpl
import com.oreo.data.db.implementation.OreoDayTimeMovementDataImpl
import com.oreo.data.db.implementation.OreoGFitWorkoutDataImpl
import com.oreo.data.db.implementation.OreoHeartRateDataImpl
import com.oreo.data.db.implementation.OreoNapDataImpl
import com.oreo.data.db.implementation.OreoRecordedWorkoutDataImpl
import com.oreo.data.db.implementation.OreoRespiratoryDataImpl
import com.oreo.data.db.implementation.OreoSleepDataImpl
import com.oreo.data.db.implementation.OreoStepsDataImpl
import com.oreo.data.db.implementation.OreoStressDataImpl
import com.oreo.data.model.OreoUserSyncActivities
import com.oreo.data.model.OreoUserSyncRawData
import com.oreo.data.repository.abstraction.OreoSyncRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class OreoSyncRepositoryImpl(
    private val localDatSource: DataStoredInterface,
    private val remoteDataSource: NetworkService,
    private val stepsDataImpl: OreoStepsDataImpl,
    private val stressDataImpl: OreoStressDataImpl,
    private val heartRateDataImpl: OreoHeartRateDataImpl,
    private val bloodOxygenDataImpl: OreoBloodOxygenDataImpl,
    private val dayTimeMovementImpl: OreoDayTimeMovementDataImpl,
    private val respiratoryDataImpl: OreoRespiratoryDataImpl,
    private val bodyStressDataImpl: OreoBodyStressDataImpl,
    private val sleepDataImpl: OreoSleepDataImpl,
    private val napDataSource: OreoNapDataImpl,
    private val bodyTemperatureDataImpl: OreoBodyTemperatureDataImpl,
    private val offlineDataMapper: OfflineDataMapper,
    private val gson: Gson,
    private val onlineDataMapper: OreoOnlineDataMapper,
    private val encryptUtils: EncryptUtils,
    private val lastSyncProvider: LastSyncProvider,
    private val testModeUtils: TestModeUtils,
    private val oreoAutoSportDataImpl: OreoAutoSportDataImpl,
    private val oreoRecordedWorkoutDataImpl: OreoRecordedWorkoutDataImpl,
    private val oreoGFitWorkoutDataImpl: OreoGFitWorkoutDataImpl,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : OreoSyncRepository {

    override suspend fun saveStepsData(data: OreoStepsData): Flow<CacheResult<OreoStepsData?>> {
        return safeCacheCall(Dispatchers.IO) {
            stepsDataImpl.syncInsertOrUpdate(data)
        }
    }

    override suspend fun getMovementData(date: String): Flow<CacheResult<String?>> {
        return safeCacheCall(Dispatchers.IO) {
            dayTimeMovementImpl.getTodayDayTimeMovement(date)
        }
    }

    override suspend fun getAutoWorkoutData(): Flow<CacheResult<List<OreoAutoSportData>?>> {
        return safeCacheCall(Dispatchers.IO) {
            oreoAutoSportDataImpl.getAllNotAcceptingData()
        }
    }

    override suspend fun markWorkoutSyncedAll(): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            oreoAutoSportDataImpl.markWorkoutSyncedAll()
        }
    }

    override suspend fun deleteAutoWorkoutData(id: Int): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            oreoAutoSportDataImpl.deleteAutoSport(id)
        }
    }

    override suspend fun markWorkoutSynced(id: Int): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            oreoAutoSportDataImpl.markWorkoutSynced(id)
        }
    }

    override suspend fun saveRecordedWorkouts(data: List<RecordedWorkoutData>): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            oreoRecordedWorkoutDataImpl.insertData(data)
            true
        }
    }

    override suspend fun getRecordedWorkouts(): Flow<CacheResult<List<RecordedWorkoutData>?>> {
        return safeCacheCall(Dispatchers.IO) {
            oreoRecordedWorkoutDataImpl.getAllWorkouts()
        }
    }

    override suspend fun removeRecordedWorkouts(): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            oreoRecordedWorkoutDataImpl.deleteAllAutoSport()
            true
        }
    }

    override suspend fun saveAutoWorkoutData(data: List<OreoAutoSportData>): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            data.forEach {
                val workout = oreoAutoSportDataImpl.getWorkoutByTime(it.startTime)
                if (workout == null) {
                    oreoAutoSportDataImpl.insertData(arrayListOf(it))
                }
            }
            true
        }
    }

    override suspend fun rescueTablesToCrash(): Flow<CacheResult<Unit?>> {

        val stepArray = ArrayList<OreoStepsData.OreoStepDataBreakup>()
        for (i in 0..23) {
            stepArray.add(OreoStepsData.OreoStepDataBreakup(0, 0, 0, 0, i))
        }
        return safeCacheCall(Dispatchers.IO) {
            stepsDataImpl.updateNullStepArray(stepArray)
        }
    }

    override suspend fun saveSleepData(data: OreoSleepData): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            sleepDataImpl.insertData(
                data
            )
        }
    }

    override suspend fun saveNapData(napList: List<OreoNapData>): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            napDataSource.insertData(
                napList
            )
        }
    }

    override suspend fun saveHealthScoreData(
        score: Int,
        date: String
    ): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            sleepDataImpl.setHealthScore(
                score, date
            )
            return@safeCacheCall true
        }
    }

    override suspend fun getGoogleFitSleepUnSyncData(date: String): List<OreoSleepData>? {
        return sleepDataImpl.getUnSyncGoogleFitData()
    }


    override suspend fun saveStressData(
        data: OreoStressDataBreakup
    ): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            stressDataImpl.insertData(
                data
            )
        }
    }

    override suspend fun saveHeartRateData(
        data: OreoHeartRate
    ): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            heartRateDataImpl.insertData(
                data
            )
        }
    }

    override suspend fun saveBloodOxygenData(
        data: OreoBloodOxygenBreakup
    ): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            bloodOxygenDataImpl.insertData(
                data
            )
        }
    }

    override suspend fun saveDayTimeMovementData(data: DayTimeMovementBreakup): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            dayTimeMovementImpl.insertData(
                data
            )
        }
    }

    override suspend fun saveRespiratoryData(data: OreoRespiratoryData): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            respiratoryDataImpl.insertData(
                data
            )
        }
    }

    override suspend fun saveBodyStressData(data: OreoBodyStressData): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            bodyStressDataImpl.insertData(
                data
            )
        }
    }


    override suspend fun saveBodyTemperatureData(data: OreoBodyTemperatureBreakup): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            bodyTemperatureDataImpl.insertData(
                data
            )
        }
    }

    override suspend fun postDataToServer(data: OreoUserSyncActivities): Flow<Resource<BaseApiResponse<VersionCheckResponse>>>? {
        return safeApiCallFlow(dispatcher) {

            onlineDataMapper.convertDataToPost(data)?.let {
                val url = "${BuildConfig.OREO_BASE_URL}/protean/v1/sync"
                AppLogs.sendAppLogs("POST Multisync DATA SERVER $url -> ${Gson().toJson(it)}")

                remoteDataSource.postOreoCombinedHistoryData(url, it)
            }

        }

    }

    override suspend fun postSleepHistoryData(data: OreoUserSyncActivities): Flow<Resource<BaseApiResponse<VersionCheckResponse>>>? {
        return safeApiCallFlow(dispatcher) {

            onlineDataMapper.getOreoSleepDataToPost(data).let {
                val url = "${BuildConfig.OREO_BASE_URL}/sleep/v1/sync"
                AppLogs.sendAppLogs("POST Sleep DATA SERVER $url -> ${Gson().toJson(it)}")
                remoteDataSource.postOreoSleepHistoryData(url, it)
            }

        }

    }


    override suspend fun updateSleepHashForLastSyncData(userSyncActivities: OreoUserSyncActivities) {
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

    override suspend fun updateHashForLastSyncData(userSyncActivities: OreoUserSyncActivities) {

        /* userSyncActivities.stepsDataList?.let {
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
         }*/
    }

    override suspend fun checkHalfSyncData() {
        /*stressDataImpl.checkHalfSyncData()
        heartRateDataImpl.checkHalfSyncData()
        bodyTemperatureDataImpl.checkHalfSyncData()
        bloodOxygenDataImpl.checkHalfSyncData()*/
    }

    override suspend fun getTodaySteps(): Flow<CacheResult<OreoStepsData?>> {
        val todayDate = DateFormats.getTodaysDateString(7)
        return safeCacheCall(dispatcher) {
            stepsDataImpl.getTodayData(todayDate)
        }
    }

    override suspend fun getTodayHeartRate(): Flow<CacheResult<List<OreoHeartRate>?>> {
        val todayDate = DateFormats.getTodaysDateString(7)
        return safeCacheCall(dispatcher) {
            null //TODO implement
            //heartRateDataImpl.getTodayData(todayDate)
        }
    }

    override suspend fun getTodayStress(): Flow<CacheResult<List<OreoStressDataBreakup>?>> {
        val todayDate = DateFormats.getTodaysDateString(7)
        return safeCacheCall(dispatcher) {
            null //TODO implement
            //stressDataImpl.getTodayData(todayDate)
        }
    }

    override suspend fun getTodayBloodOxygen(): Flow<CacheResult<List<OreoBloodOxygenBreakup>?>> {
        val todayDate = DateFormats.getTodaysDateString(7)
        return safeCacheCall(dispatcher) {
            null //TODO implement
            //bloodOxygenDataImpl.getTodayData(todayDate)
        }
    }

    override suspend fun getTodaySleep(): Flow<CacheResult<SleepBreakup?>> {
        val todayDate = DateFormats.getTodaysDateString(7)
        return safeCacheCall(dispatcher) {
            null
            /*offlineDataMapper.convertSleepDataForDetailsView(
                    offlineDataMapper.convertSleepData(
                        sleepDataImpl.getTodayData(todayDate)
                    )
                )*/
        }
    }

    override suspend fun getTodaySleepData(): Flow<CacheResult<OreoSleepData?>> {
        val todayDate = DateFormats.getTodaysDateString(7)
        return safeCacheCall(dispatcher) {
            offlineDataMapper.convertSleepData(
                sleepDataImpl.getTodayData(todayDate)
            )
        }
    }


    override suspend fun getTodayBodyTemp(): Flow<CacheResult<List<OreoBodyTemperatureBreakup>?>> {
        //   val todayDate = DateFormats.getTodaysDateString(7)
        return safeCacheCall(dispatcher) {
            null //TODO implement
            //bodyTemperatureDataImpl.getTodayData(todayDate)
        }
    }

    override suspend fun saveAndGetGFitWorkout(data: List<GoogleFitWorkoutData>): Flow<CacheResult<List<GoogleFitWorkoutData>?>> {
        return safeCacheCall(dispatcher) {
            oreoGFitWorkoutDataImpl.saveWorkout(data)
        }
    }


    override suspend fun getGFitUnSyncWorkout(): Flow<CacheResult<List<GoogleFitWorkoutData>?>> {
        return safeCacheCall(dispatcher) {
            oreoGFitWorkoutDataImpl.getUnSyncWorkout()
        }
    }

    override suspend fun updateGFitSyncWorkout(syncData: List<GoogleFitWorkoutData>): Flow<CacheResult<Int?>> {
        return safeCacheCall(dispatcher) {
            oreoGFitWorkoutDataImpl.updateServerSyncData(syncData)
        }
    }

    override suspend fun deleteSleepServerSyncData(data: OreoUserSyncRawData) {
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

    override suspend fun markDataSynced(data: OreoUserSyncRawData) {
        val todayTimeStamp = DateFormats.convertTimeStampToStartOfDay(DateFormats.getTimeStamp())
        /* val todayTimeStampForSleep = DateFormats.convertTimeStampToPrevious12ofDay(
             DateFormats.subtractDate(
                 DateFormats.getTimeStamp(),
                 1
             )
         )
         LOGS.d("deleteServerSyncData $todayTimeStamp $todayTimeStampForSleep")*/

        data.stepsDataList?.let {
            stepsDataImpl.updateServerSyncData(it)
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
        data.respiratory?.let {
            respiratoryDataImpl.updateServerSyncData(it, todayTimeStamp)
        }

        data.boData?.let {
            bloodOxygenDataImpl.updateServerSyncData(it, todayTimeStamp)
        }
    }

    override suspend fun deleteServerSyncData(data: OreoUserSyncRawData) {
        val todayTimeStamp = DateFormats.convertTimeStampToStartOfDay(DateFormats.getTimeStamp())
        /* val todayTimeStampForSleep = DateFormats.convertTimeStampToPrevious12ofDay(
             DateFormats.subtractDate(
                 DateFormats.getTimeStamp(),
                 1
             )
         )
         LOGS.d("deleteServerSyncData $todayTimeStamp $todayTimeStampForSleep")*/

        data.stepsDataList?.let {
            stepsDataImpl.updateServerSyncData(it)
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
        data.respiratory?.let {
            respiratoryDataImpl.updateServerSyncData(it, todayTimeStamp)
        }

        data.boData?.let {
            bloodOxygenDataImpl.updateServerSyncData(it, todayTimeStamp)
        }
    }
    //1643049000009


    //TODO: Need to Think about a way to handle it more efficiently
    override suspend fun getUnSyncUserActivities(): Pair<OreoUserSyncActivities, OreoUserSyncRawData> {

        checkHalfSyncData()

        val nDayStartingTimeStamp = DateFormats.getNDayStartingTimeStamp()

        val userSyncRawData = OreoUserSyncRawData()

        userSyncRawData.stepsDataList =
            stepsDataImpl.getUnSyncServerData(nDayStartingTimeStamp, false)
        userSyncRawData.hrHistoryData =
            heartRateDataImpl.getUnSyncServerData(nDayStartingTimeStamp, false)
        userSyncRawData.stressData =
            stressDataImpl.getUnSyncServerData(nDayStartingTimeStamp, false)

        userSyncRawData.dayTimeMovement =
            dayTimeMovementImpl.getUnSyncServerData(nDayStartingTimeStamp, false)

        userSyncRawData.respiratory =
            respiratoryDataImpl.getUnSyncServerData(nDayStartingTimeStamp, false)
        userSyncRawData.boData =
            bloodOxygenDataImpl.getUnSyncServerData(nDayStartingTimeStamp, false)

        userSyncRawData.bodyStressData =
            bodyStressDataImpl.getUnSyncServerData(nDayStartingTimeStamp, false)

        userSyncRawData.sleepData = sleepDataImpl.getUnSyncServerData(nDayStartingTimeStamp, false)
        userSyncRawData.bodyTemperature =
            bodyTemperatureDataImpl.getUnSyncServerData(nDayStartingTimeStamp, false)

        val filteredHrList = ArrayList<OreoHeartRate>()
        userSyncRawData.hrHistoryData?.forEach {
            val breakUp = Gson().fromJson<List<Int>>(it.breakUp ?: "")
            if (breakUp.sum() > 0) {
                filteredHrList.add(it)
            }
        }

        val heartRateData = if (filteredHrList.isEmpty()) {
            null
        } else {
            filteredHrList
        }


        val sleepData = offlineDataMapper.convertUnSyncSleepDataListToObjectOreo(
            userSyncRawData.sleepData
        )


        val filteredTempList = ArrayList<OreoBodyTemperatureBreakup>()
        userSyncRawData.bodyTemperature?.forEach {
            val breakUp = Gson().fromJson<List<Float>>(it.breakUp ?: "")
            if (breakUp.sum() > 0) {
                filteredTempList.add(it)
            }
        }
        val bodyTempData = if (filteredTempList.isEmpty()) {
            null
        } else {
            filteredTempList
        }


        val filteredBoList = ArrayList<OreoBloodOxygenBreakup>()
        userSyncRawData.boData?.forEach {
            val breakUp = Gson().fromJson<List<Int>>(it.breakUp ?: "")
            if (breakUp.sum() > 0) {
                filteredBoList.add(it)
            }
        }
        val bloodOxygenData = if (filteredBoList.isEmpty()) {
            null
        } else {
            filteredBoList
        }

        val filteredBodyStressList = ArrayList<OreoBodyStressData>()
        userSyncRawData.bodyStressData?.forEach {
            val breakUp = Gson().fromJson<List<Int>>(it.breakUp ?: "")
            if (breakUp.sum() > 0) {
                filteredBodyStressList.add(it)
            }
        }
        val bodyStressData = if (filteredBodyStressList.isEmpty()) {
            null
        } else {
            filteredBodyStressList
        }

        val filteredStressList = ArrayList<OreoStressDataBreakup>()
        userSyncRawData.stressData?.forEach {
            val breakUp = Gson().fromJson<List<Int>>(it.breakUp ?: "")
            if (breakUp.sum() > 0) {
                filteredStressList.add(it)
            }
        }
        val stressData = if (filteredStressList.isEmpty()) {
            null
        } else {
            filteredStressList
        }


        val respiratoryDataList = ArrayList<OreoRespiratoryData>()
        userSyncRawData.respiratory?.forEach {
            val breakUp = Gson().fromJson<List<Int>>(it.breakUp ?: "")
            if (breakUp.sum() > 0) {
                respiratoryDataList.add(it)
            }
        }
        val respiratoryData = if (respiratoryDataList.isEmpty()) {
            null
        } else {
            respiratoryDataList
        }


        val dayTimeDataList = ArrayList<DayTimeMovementBreakup>()
        userSyncRawData.dayTimeMovement?.forEach {
            val breakUp = Gson().fromJson<List<Int>>(it.breakUp ?: "")
            if (breakUp.sum() > 0) {
                dayTimeDataList.add(it)
            }
        }
        val dayTimeData = if (dayTimeDataList.isEmpty()) {
            null
        } else {
            dayTimeDataList
        }

        return Pair(
            OreoUserSyncActivities(
                stepsDataList = userSyncRawData.stepsDataList,
                hrHistoryData = heartRateData,
                stressData = stressData,
                boData = bloodOxygenData,
                bodyStressData = bodyStressData,
                sleepData = sleepData,
                respiratory = respiratoryData,
                bodyTemperature = bodyTempData,
                dayTimeMovement = dayTimeData
            ), userSyncRawData
        )

    }


    override suspend fun updateGoogleFitUnSyncHeartRateStatus(heartRateList: List<OreoHeartRate>) {
        //TODO implement
        //googleFitDataImpl.updateSyncHeartRateStatus(heartRateList)
    }

    override suspend fun updateGoogleFitUnSyncStepsStatus(date: String, data: StepDataGoogleFit) {
        //TODO implement

        //        googleFitDataImpl.updateSyncStepsStatus(date, data)
    }

    override suspend fun updateGoogleFitUnSyncSleepStatus(sleepData: OreoSleepData) {
        sleepDataImpl.updateUnSyncGoogleFitData(sleepData)
    }


}