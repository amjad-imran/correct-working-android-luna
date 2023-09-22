package com.noisefit.data.repository.implementation

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.noisefit.luna.BuildConfig
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.VersionCheckResponse
import com.noisefit.data.repository.abstraction.AppRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit.data.safeCacheCall
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.db.implementation.OreoBloodOxygenDataImpl
import com.oreo.data.db.implementation.OreoBodyTemperatureDataImpl
import com.oreo.data.db.implementation.OreoDayTimeMovementDataImpl
import com.oreo.data.db.implementation.OreoHeartRateDataImpl
import com.oreo.data.db.implementation.OreoRespiratoryDataImpl
import com.oreo.data.db.implementation.OreoSleepDataImpl
import com.oreo.data.db.implementation.OreoStepsDataImpl
import com.oreo.data.db.implementation.OreoStressDataImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

const val DELETE_DB_DAYS = 5
class AppRepositoryImpl(
    private val remoteDataSource: NetworkService,
    private val heartRateDataSource: OreoHeartRateDataImpl,
    private val stepsDataSource: OreoStepsDataImpl,
    private val stressDataSource: OreoStressDataImpl,
    private val bloodOxygenDataSource: OreoBloodOxygenDataImpl,
    private val tempDataSource: OreoBodyTemperatureDataImpl,
    private val respDataSource: OreoRespiratoryDataImpl,
    private val sleepDataSource: OreoSleepDataImpl,
    private val dayTimeMovementDataSource: OreoDayTimeMovementDataImpl,
    private val gson: Gson,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : AppRepository {

    override suspend fun checkAppVersion(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<VersionCheckResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.checkAppVersion(
                "${BuildConfig.BASE_URL_NEW}/core/ring/app_version",
                request
            )
        }
    }

    override suspend fun deleteOldTableData(): Flow<CacheResult<Unit?>> {
        val timeStamp = DateFormats.lastClearDataTimeStamp()
        LOGS.d("deleteOldTableData $timeStamp")
        return safeCacheCall(dispatcher) { //1642962600747

            heartRateDataSource.deleteOldData(DELETE_DB_DAYS)
            stepsDataSource.deleteOldData(DELETE_DB_DAYS)
            stressDataSource.deleteOldData(DELETE_DB_DAYS)
            bloodOxygenDataSource.deleteOldData(DELETE_DB_DAYS)
            tempDataSource.deleteOldData(DELETE_DB_DAYS)
            respDataSource.deleteOldData(DELETE_DB_DAYS)
            sleepDataSource.deleteOldData(DELETE_DB_DAYS)
            dayTimeMovementDataSource.deleteOldData(DELETE_DB_DAYS)


            LOGS.w("DELETING_OLD_TABLE")

            /*val id = stepsDataImpl.deleteOldData(timeStamp)
            stressDataImpl.deleteOldData(timeStamp)
            heartRateDataImpl.deleteOldData(timeStamp)
            bloodOxygenDataImpl.deleteOldData(timeStamp)
            sleepDataImpl.deleteOldData(timeStamp)
            bodyTemperatureDataImpl.deleteOldData(timeStamp)
            sportEventDataImpl.deleteExpireEvents()
            LOGS.d("deleteOldTableData $id")*/

        }


    }

}