package com.noisefit.data.repository.implementation

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.noisefit.luna.BuildConfig
import com.noisefit.data.local.dataStored.abstraction.IOfflineApiResponseStore
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit.data.local.db.fromJson
import com.noisefit.data.local.db.implementation.*
import com.noisefit_commans.data.model.KeyValue
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.HelpAndSupportDetailResponse
import com.noisefit_commans.data.response.HelpAndSupportResponse
import com.noisefit_commans.data.response.VersionCheckResponse
import com.noisefit.data.repository.LastSyncItems
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.AppRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit.data.safeCacheCall
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class AppRepositoryImpl(
    private val remoteDataSource: NetworkService,
    private val stepsDataImpl: StepsDataImpl,
    private val stressDataImpl: StressDataImpl,
    private val heartRateDataImpl: HeartRateDataImpl,
    private val bloodOxygenDataImpl: BloodOxygenDataImpl,
    private val sleepDataImpl: SleepDataImpl,
    private val bodyTemperatureDataImpl: BodyTemperatureDataImpl,
    private val sportEventDataImpl: SportEventDataImpl,
    private val lastSyncProvider: LastSyncProvider,
    private val offlineApiStore: IOfflineApiResponseStore,
    private val keyValueDataSource: KeyValueDataSource,
    private val gson: Gson,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : AppRepository {

    override suspend fun checkAppVersion(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<VersionCheckResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.checkAppVersion(
                "${BuildConfig.BASE_URL_NEW}/master/app_version/check",
                request
            )
        }
    }

    override suspend fun deleteOldTableData(): Flow<CacheResult<Unit?>> {
        val timeStamp = DateFormats.lastClearDataTimeStamp()
        LOGS.d("deleteOldTableData $timeStamp")
        return safeCacheCall(dispatcher) { //1642962600747
            val id = stepsDataImpl.deleteOldData(timeStamp)
            stressDataImpl.deleteOldData(timeStamp)
            heartRateDataImpl.deleteOldData(timeStamp)
            bloodOxygenDataImpl.deleteOldData(timeStamp)
            sleepDataImpl.deleteOldData(timeStamp)
            bodyTemperatureDataImpl.deleteOldData(timeStamp)
            sportEventDataImpl.deleteExpireEvents()
            LOGS.d("deleteOldTableData $id")
        }


    }

    override suspend fun getHelpAndSupportList(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<HelpAndSupportResponse>>>> {
        return flow {

            val serverUpdateTimeStamp =
                lastSyncProvider.getSyncTimeStamp(LastSyncItems.H_AND_SUPPORT_SERVER_UPDATE)
            val localSyncTime =
                lastSyncProvider.getSyncTimeStamp(LastSyncItems.HELP_AND_SUPPORT_LIST)

            val shouldCallApi = shouldCallBannerApi(serverUpdateTimeStamp, localSyncTime)

            val resultData = ArrayList<HelpAndSupportResponse>()

            val cacheResult = safeCacheCall(Dispatchers.IO) {
                getLocalHelpAndSupportData(shouldCallApi)
            }
            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            resultData.clear()
                            resultData.addAll(it)
                        }
                    }
                    is CacheResult.GenericError -> {

                    }
                }
            }


            if (resultData.isNotEmpty()) {
                emit(Resource.Success(
                    com.noisefit_commans.data.response.BaseApiResponse(
                        data = resultData,
                        message = ""
                    )
                ))
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                val url =
                    "${BuildConfig.BASE_URL_NEW}/master/help_and_support/list"
                remoteDataSource.getHelpAndSupportList(url)
            }


            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(Resource.GenericError(resource.message, resource.errorCode))
                    }
                    is Resource.Loading -> {
                        emit(Resource.Loading(resource.loading))
                    }
                    is Resource.NetworkError -> {
                        emit(Resource.NetworkError(resource.response, resource.code))
                    }
                    is Resource.Success -> {

                        resource.data?.data?.let { response ->
                            withContext(Dispatchers.IO) {
                                keyValueDataSource.removeDataByType(KeyValueDataType.H_AND_S)
                            }
                            lastSyncProvider.setSyncTimeStamp(LastSyncItems.HELP_AND_SUPPORT_LIST)
                            resultData.clear()
                            resultData.addAll(response)
                        }
                    }
                }
            }

            if (resultData.isNotEmpty()) {
                safeCacheCall(Dispatchers.IO) {
                    offlineApiStore.setHelpAndSupportList(resultData)
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(Resource.Success(
                                com.noisefit_commans.data.response.BaseApiResponse(
                                    data = resultData,
                                    message = ""
                                )
                            ))
                        }
                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }
            }
        }
    }

    private fun shouldCallBannerApi(
        serverTime: Long,
        localTime: Long
    ): Boolean {
        if (serverTime == 0L) return true
        if (localTime == 0L) return true

        return localTime < serverTime
    }

    fun getLocalHelpAndSupportData(removeData: Boolean): List<HelpAndSupportResponse>? {
        if (removeData) {
            offlineApiStore.setHelpAndSupportList(null)
            return ArrayList()
        }
        return offlineApiStore.getHelpAndSupportList()
    }

    override suspend fun getHelpAndSupportByQuestionId(
        id: Int,
        manufacturer: String
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<HelpAndSupportDetailResponse>>> {
        return flow {
            var resultData: HelpAndSupportDetailResponse? = null

            val cacheResult = safeCacheCall(Dispatchers.IO) {

                val localData =
                    keyValueDataSource.getData(id.toString(), KeyValueDataType.H_AND_S)
                        ?: return@safeCacheCall null

                if (localData.value == null) {
                    return@safeCacheCall null
                }

                return@safeCacheCall localData.value?.let {
                    Gson().fromJson<HelpAndSupportDetailResponse>(
                        it
                    )
                }
            }
            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            resultData = it
                        }
                    }
                    is CacheResult.GenericError -> {

                    }
                }
            }


            if (resultData != null) {
                emit(
                    Resource.Success(
                        com.noisefit_commans.data.response.BaseApiResponse(
                            data = resultData,
                            message = "",
                        )
                    )
                )
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                val url =
//                    "${BuildConfig.BASE_URL_NEW}/master/help_and_support/ques_detail/$id?manufacturer=$manufacturer"
                    "${BuildConfig.BASE_URL_NEW}/master/help_and_support/v2/ques_detail/$id?manufacturer=$manufacturer"
                remoteDataSource.getHelpAndSupportByQuestionId(url)
            }


            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(Resource.GenericError(resource.message, resource.errorCode))
                    }
                    is Resource.Loading -> {
                        emit(Resource.Loading(resource.loading))
                    }
                    is Resource.NetworkError -> {
                        emit(Resource.NetworkError(resource.response, resource.code))
                    }
                    is Resource.Success -> {

                        resource.data?.data?.let { response ->
                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                safeCacheCall(Dispatchers.IO) {
                    keyValueDataSource.insertData(
                        KeyValue(
                            key = id.toString(),
                            value = gson.toJson(resultData),
                            type = KeyValueDataType.H_AND_S.name
                        )
                    )
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(
                                Resource.Success(
                                    com.noisefit_commans.data.response.BaseApiResponse(
                                        data = resultData,
                                        message = "",
                                    )
                                )
                            )
                        }
                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }
            }
        }
    }
}