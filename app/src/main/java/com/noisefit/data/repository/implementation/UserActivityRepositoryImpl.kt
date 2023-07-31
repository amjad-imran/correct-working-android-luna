package com.noisefit.data.repository.implementation

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.noisefit.BuildConfig
import com.noisefit.data.local.dataStored.abstraction.IOfflineApiResponseStore
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit_commans.data.model.KeyValue
import com.noisefit_commans.data.model.UserDataPost
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.*
import com.noisefit.data.repository.LastSyncItems
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.UserActivityRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit.data.safeCacheCall
import com.noisefit_commans.data.model.history.*
import com.noisefit_commans.response.SleepHistoryResponse
import com.noisefit_commans.ui.checkDayDifferenceMoreNMinutes
import com.noisefit_commans.ui.checkDayDifferenceMoreOne
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

private inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

class UserActivityRepositoryImpl(
    private val offlineApiStore: IOfflineApiResponseStore,
    private val remoteDataSource: NetworkService,
    private val lastSyncProvider: LastSyncProvider,
    private val keyValueDataSource: KeyValueDataSource,
    private val gson: Gson,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : UserActivityRepository {

    /*
    bypass offline mode
     */
    val mOnlineMode = false

    override suspend fun postCombinedHistoryData(request: UserDataPost): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<VersionCheckResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url ="${BuildConfig.BASE_URL_NEW}/multisync_v2/create"
            remoteDataSource.postCombinedHistoryData(url,request)
        }
    }

    override suspend fun getStepsHistory(
        historyType: String,
        selectedStartDate: String?,
        selectedEndDate: String?
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<StepsHistoryResponse>>> {

        return flow {


            var resultData: StepsHistoryResponse? = null
            val isLatestData = selectedStartDate.isNullOrEmpty() && selectedEndDate.isNullOrEmpty()
            if (isLatestData) {
                val shouldCallApi = when (historyType) {
                    "daily" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_STEPS_DAY)
                        .checkDayDifferenceMoreOne()
                    "weekly" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_STEPS_WEEK)
                        .checkDayDifferenceMoreOne()
                    "monthly" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_STEPS_MONTH)
                        .checkDayDifferenceMoreOne()
                    "yearly" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_STEPS_YEAR)
                        .checkDayDifferenceMoreOne()
                    else -> true
                }


                val cacheResult = safeCacheCall(Dispatchers.IO) {
                    getLocalStepsHistoryData(historyType, shouldCallApi)
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
                    emit(Resource.Success(
                        com.noisefit_commans.data.response.BaseApiResponse(
                            data = resultData,
                            message = ""
                        )
                    ))
                    return@flow
                }
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                val url =
                    "${BuildConfig.STEPS_URL}/step_activities/v3/users/history/data"
                remoteDataSource.getStepsHistory(
                    url,
                    historyType,
                    selectedStartDate,
                    selectedEndDate
                )
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
                            when (historyType) {
                                "daily" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_STEPS_DAY)
                                "weekly" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_STEPS_WEEK)
                                "monthly" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_STEPS_MONTH)
                                "yearly" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_STEPS_YEAR)
                            }

                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                if (isLatestData) {
                    safeCacheCall(Dispatchers.IO) {
                        offlineApiStore.setGraphLocalStepsData(resultData, historyType)
                    }.collect { resource ->
                        when (resource) {
                            is CacheResult.Success -> {
                                emit(
                                    Resource.Success(
                                        com.noisefit_commans.data.response.BaseApiResponse(
                                            data = resultData,
                                            message = ""
                                        )
                                    )
                                )
                            }
                            is CacheResult.GenericError -> {
                                emit(Resource.GenericError(message = "Something went wrong", 0))
                            }
                        }
                    }
                } else {
                    emit(Resource.Success(
                        com.noisefit_commans.data.response.BaseApiResponse(
                            data = resultData,
                            message = ""
                        )
                    ))
                }
            }
        }
    }

    private fun getLocalStepsHistoryData(
        historyType: String,
        removeData: Boolean
    ): StepsHistoryResponse? {
        if (removeData) {
            offlineApiStore.setGraphLocalStepsData(null, historyType)
            return null
        }

        return when (historyType) {
            "daily" -> offlineApiStore.getGraphLocalStepsData("daily")
            "weekly" -> offlineApiStore.getGraphLocalStepsData("weekly")
            "monthly" -> offlineApiStore.getGraphLocalStepsData("monthly")
            "yearly" -> offlineApiStore.getGraphLocalStepsData("yearly")
            else -> null
        }
    }

    override suspend fun getStressHistory(
        historyType: String,
        selectedStartDate: String?,
        selectedEndDate: String?
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<StressHistoryResponse>>> {
        return flow {

            var resultData: StressHistoryResponse? = null
            val isLatestData = selectedStartDate.isNullOrEmpty() && selectedEndDate.isNullOrEmpty()
            if (isLatestData) {
                val shouldCallApi = when (historyType) {
                    "daily" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_STRESS_DAY)
                        .checkDayDifferenceMoreOne()
                    "weekly" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_STRESS_WEEK)
                        .checkDayDifferenceMoreOne()
                    "monthly" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_STRESS_MONTH)
                        .checkDayDifferenceMoreOne()
                    "yearly" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_STRESS_YEAR)
                        .checkDayDifferenceMoreOne()
                    else -> true
                }


                val cacheResult = safeCacheCall(Dispatchers.IO) {
                    getLocalStressHistoryData(historyType, shouldCallApi)
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
                    emit(Resource.Success(
                        com.noisefit_commans.data.response.BaseApiResponse(
                            data = resultData,
                            message = ""
                        )
                    ))
                    return@flow
                }
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                remoteDataSource.getStressHistory(
                    historyType,
                    selectedStartDate,
                    selectedEndDate
                )
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
                            when (historyType) {
                                "daily" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_STRESS_DAY)
                                "weekly" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_STRESS_WEEK)
                                "monthly" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_STRESS_MONTH)
                                "yearly" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_STRESS_YEAR)
                            }

                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                if (isLatestData) {
                    safeCacheCall(Dispatchers.IO) {
                        offlineApiStore.setGraphLocalStressData(resultData, historyType)
                    }.collect { resource ->
                        when (resource) {
                            is CacheResult.Success -> {
                                emit(
                                    Resource.Success(
                                        com.noisefit_commans.data.response.BaseApiResponse(
                                            data = resultData,
                                            message = ""
                                        )
                                    )
                                )
                            }
                            is CacheResult.GenericError -> {
                                emit(Resource.GenericError(message = "Something went wrong", 0))
                            }
                        }
                    }
                } else {
                    emit(Resource.Success(
                        com.noisefit_commans.data.response.BaseApiResponse(
                            data = resultData,
                            message = ""
                        )
                    ))
                }
            }
        }
    }

    private fun getLocalStressHistoryData(
        historyType: String,
        removeData: Boolean
    ): StressHistoryResponse? {
        if (removeData) {
            offlineApiStore.setGraphLocalStressData(null, historyType)
            return null
        }

        return when (historyType) {
            "daily" -> offlineApiStore.getGraphLocalStressData("daily")
            "weekly" -> offlineApiStore.getGraphLocalStressData("weekly")
            "monthly" -> offlineApiStore.getGraphLocalStressData("monthly")
            "yearly" -> offlineApiStore.getGraphLocalStressData("yearly")
            else -> null
        }
    }

    override suspend fun getBloodOxygenHistory(
        historyType: String,
        selectedStartDate: String?,
        selectedEndDate: String?
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<BoHistoryResponse>>> {
        return flow {

            var resultData: BoHistoryResponse? = null
            val isLatestData = selectedStartDate.isNullOrEmpty() && selectedEndDate.isNullOrEmpty()
            if (isLatestData) {
                val shouldCallApi = when (historyType) {
                    "daily" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_BO_DAY)
                        .checkDayDifferenceMoreOne()
                    "weekly" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_BO_WEEK)
                        .checkDayDifferenceMoreOne()
                    "monthly" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_BO_MONTH)
                        .checkDayDifferenceMoreOne()
                    "yearly" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_BO_YEAR)
                        .checkDayDifferenceMoreOne()
                    else -> true
                }


                val cacheResult = safeCacheCall(Dispatchers.IO) {
                    getLocalBOHistoryData(historyType, shouldCallApi)
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
                    emit(Resource.Success(
                        com.noisefit_commans.data.response.BaseApiResponse(
                            data = resultData,
                            message = ""
                        )
                    ))
                    return@flow
                }
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                remoteDataSource.getBloodOxygenHistory(
                    historyType,
                    selectedStartDate,
                    selectedEndDate
                )
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
                            when (historyType) {
                                "daily" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_BO_DAY)
                                "weekly" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_BO_WEEK)
                                "monthly" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_BO_MONTH)
                                "yearly" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_BO_YEAR)
                            }

                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                if (isLatestData) {
                    safeCacheCall(Dispatchers.IO) {
                        offlineApiStore.setGraphLocalBOData(resultData, historyType)
                    }.collect { resource ->
                        when (resource) {
                            is CacheResult.Success -> {
                                emit(
                                    Resource.Success(
                                        com.noisefit_commans.data.response.BaseApiResponse(
                                            data = resultData,
                                            message = ""
                                        )
                                    )
                                )
                            }
                            is CacheResult.GenericError -> {
                                emit(Resource.GenericError(message = "Something went wrong", 0))
                            }
                        }
                    }
                } else {
                    emit(Resource.Success(
                        com.noisefit_commans.data.response.BaseApiResponse(
                            data = resultData,
                            message = ""
                        )
                    ))
                }
            }
        }
    }

    private fun getLocalBOHistoryData(
        historyType: String,
        removeData: Boolean
    ): BoHistoryResponse? {
        if (removeData) {
            offlineApiStore.setGraphLocalBOData(null, historyType)
            return null
        }

        return when (historyType) {
            "daily" -> offlineApiStore.getGraphLocalBOData("daily")
            "weekly" -> offlineApiStore.getGraphLocalBOData("weekly")
            "monthly" -> offlineApiStore.getGraphLocalBOData("monthly")
            "yearly" -> offlineApiStore.getGraphLocalBOData("yearly")
            else -> null
        }
    }

    override suspend fun getHrHistory(
        historyType: String,
        selectedStartDate: String?,
        selectedEndDate: String?,
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<HrHistoryResponse>>> {
        return flow {

            var resultData: HrHistoryResponse? = null
            val isLatestData = selectedStartDate.isNullOrEmpty() && selectedEndDate.isNullOrEmpty()
            if (isLatestData) {
                val shouldCallApi = when (historyType) {
                    "daily" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_HR_DAY)
                        .checkDayDifferenceMoreOne()
                    "weekly" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_HR_WEEK)
                        .checkDayDifferenceMoreOne()
                    "monthly" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_HR_MONTH)
                        .checkDayDifferenceMoreOne()
                    "yearly" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_HR_YEAR)
                        .checkDayDifferenceMoreOne()
                    else -> true
                }


                val cacheResult = safeCacheCall(Dispatchers.IO) {
                    getLocalHRHistoryData(historyType, shouldCallApi)
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
                    emit(Resource.Success(
                        com.noisefit_commans.data.response.BaseApiResponse(
                            data = resultData,
                            message = ""
                        )
                    ))
                    return@flow
                }
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                remoteDataSource.getHrHistory(
                    historyType,
                    selectedStartDate,
                    selectedEndDate
                )
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
                            when (historyType) {
                                "daily" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_HR_DAY)
                                "weekly" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_HR_WEEK)
                                "monthly" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_HR_MONTH)
                                "yearly" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_HR_YEAR)
                            }

                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                if (isLatestData) {
                    safeCacheCall(Dispatchers.IO) {
                        offlineApiStore.setGraphLocalHRData(resultData, historyType)
                    }.collect { resource ->
                        when (resource) {
                            is CacheResult.Success -> {
                                emit(
                                    Resource.Success(
                                        com.noisefit_commans.data.response.BaseApiResponse(
                                            data = resultData,
                                            message = ""
                                        )
                                    )
                                )
                            }
                            is CacheResult.GenericError -> {
                                emit(Resource.GenericError(message = "Something went wrong", 0))
                            }
                        }
                    }
                } else {
                    emit(Resource.Success(
                        com.noisefit_commans.data.response.BaseApiResponse(
                            data = resultData,
                            message = ""
                        )
                    ))
                }
            }
        }
    }

    private fun getLocalHRHistoryData(
        historyType: String,
        removeData: Boolean
    ): HrHistoryResponse? {
        if (removeData) {
            offlineApiStore.setGraphLocalHRData(null, historyType)
            return null
        }

        return when (historyType) {
            "daily" -> offlineApiStore.getGraphLocalHRData("daily")
            "weekly" -> offlineApiStore.getGraphLocalHRData("weekly")
            "monthly" -> offlineApiStore.getGraphLocalHRData("monthly")
            "yearly" -> offlineApiStore.getGraphLocalHRData("yearly")
            else -> null
        }
    }

    override suspend fun getSleepHistory(
        historyType: String,
        selectedStartDate: String?,
        selectedEndDate: String?
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<SleepHistoryResponse>>> {
        return flow {

            var resultData: SleepHistoryResponse? = null
            val isLatestData = selectedStartDate.isNullOrEmpty() && selectedEndDate.isNullOrEmpty()
            if (isLatestData) {
                val shouldCallApi = when (historyType) {
                    "daily" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_SLEEP_DAY)
                        .checkDayDifferenceMoreOne()
                    "weekly" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_SLEEP_WEEK)
                        .checkDayDifferenceMoreOne()
                    "monthly" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_SLEEP_MONTH)
                        .checkDayDifferenceMoreOne()
                    "yearly" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_SLEEP_YEAR)
                        .checkDayDifferenceMoreOne()
                    else -> true
                }


                val cacheResult = safeCacheCall(Dispatchers.IO) {
                    getLocalSleepHistoryData(historyType, shouldCallApi)
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
                    emit(Resource.Success(
                        com.noisefit_commans.data.response.BaseApiResponse(
                            data = resultData,
                            message = ""
                        )
                    ))
                    return@flow
                }
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                remoteDataSource.getSleepHistory(
                    historyType,
                    selectedStartDate,
                    selectedEndDate
                )
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
                            when (historyType) {
                                "daily" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_SLEEP_DAY)
                                "weekly" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_SLEEP_WEEK)
                                "monthly" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_SLEEP_MONTH)
                                "yearly" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_SLEEP_YEAR)
                            }

                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                if (isLatestData) {
                    safeCacheCall(Dispatchers.IO) {
                        offlineApiStore.setGraphLocalSleepData(resultData, historyType)
                    }.collect { resource ->
                        when (resource) {
                            is CacheResult.Success -> {
                                emit(
                                    Resource.Success(
                                        com.noisefit_commans.data.response.BaseApiResponse(
                                            data = resultData,
                                            message = ""
                                        )
                                    )
                                )
                            }
                            is CacheResult.GenericError -> {
                                emit(Resource.GenericError(message = "Something went wrong", 0))
                            }
                        }
                    }
                } else {
                    emit(Resource.Success(
                        com.noisefit_commans.data.response.BaseApiResponse(
                            data = resultData,
                            message = ""
                        )
                    ))
                }
            }
        }
    }

    private fun getLocalSleepHistoryData(
        historyType: String,
        removeData: Boolean
    ): SleepHistoryResponse? {
        if (removeData) {
            offlineApiStore.setGraphLocalSleepData(null, historyType)
            return null
        }

        return when (historyType) {
            "daily" -> offlineApiStore.getGraphLocalSleepData("daily")
            "weekly" -> offlineApiStore.getGraphLocalSleepData("weekly")
            "monthly" -> offlineApiStore.getGraphLocalSleepData("monthly")
            "yearly" -> offlineApiStore.getGraphLocalSleepData("yearly")
            else -> null
        }
    }

    override suspend fun getSleepHighlights(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<SleepHighlightResponse>>> {

        return flow {
            var resultData: SleepHighlightResponse? = null
            val shouldCallApi =
                lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_SLEEP_HIGHLIGHT)
                    .checkDayDifferenceMoreOne()


            val cacheResult = safeCacheCall(Dispatchers.IO) {
                if (shouldCallApi) {
                    offlineApiStore.setGraphLocalSleepHighlightData(null)
                    return@safeCacheCall null
                } else {
                    offlineApiStore.getGraphLocalSleepHighlightData()
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
                emit(Resource.Success(
                    com.noisefit_commans.data.response.BaseApiResponse(
                        data = resultData,
                        message = ""
                    )
                ))
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                remoteDataSource.getSleepHighlights()
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

                            lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_SLEEP_HIGHLIGHT)
                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                safeCacheCall(Dispatchers.IO) {
                    offlineApiStore.setGraphLocalSleepHighlightData(resultData)
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(
                                Resource.Success(
                                    com.noisefit_commans.data.response.BaseApiResponse(
                                        data = resultData,
                                        message = ""
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


    override suspend fun getBodyTempHistory(
        historyType: String,
        selectedStartDate: String?,
        selectedEndDate: String?
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<BodyTempHistoryResponse>>> {

        return flow {

            var resultData: BodyTempHistoryResponse? = null
            val isLatestData = selectedStartDate.isNullOrEmpty() && selectedEndDate.isNullOrEmpty()
            if (isLatestData) {
                val shouldCallApi = when (historyType) {
                    "daily" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_BODY_TEMP_DAY)
                        .checkDayDifferenceMoreOne()
                    "weekly" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_BODY_TEMP_WEEK)
                        .checkDayDifferenceMoreOne()
                    "monthly" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_BODY_TEMP_MONTH)
                        .checkDayDifferenceMoreOne()
                    "yearly" -> lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_BODY_TEMP_YEAR)
                        .checkDayDifferenceMoreOne()
                    else -> true
                }


                val cacheResult = safeCacheCall(Dispatchers.IO) {
                    getLocalBTempHistoryData(historyType, shouldCallApi)
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
                    emit(Resource.Success(
                        com.noisefit_commans.data.response.BaseApiResponse(
                            data = resultData,
                            message = ""
                        )
                    ))
                    return@flow
                }
            }

            val url = "${BuildConfig.TEMPERATURE_URL}/temps/v3/history"
            val serverResult = safeApiCallFlow(dispatcher) {
                remoteDataSource.getBodyTempHistory(
                    url,
                    historyType,
                    selectedStartDate,
                    selectedEndDate
                )
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
                            when (historyType) {
                                "daily" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_BODY_TEMP_DAY)
                                "weekly" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_BODY_TEMP_WEEK)
                                "monthly" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_BODY_TEMP_MONTH)
                                "yearly" -> lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_BODY_TEMP_YEAR)
                            }

                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                if (isLatestData) {
                    safeCacheCall(Dispatchers.IO) {
                        offlineApiStore.setGraphLocalBodyTempData(resultData, historyType)
                    }.collect { resource ->
                        when (resource) {
                            is CacheResult.Success -> {
                                emit(
                                    Resource.Success(
                                        com.noisefit_commans.data.response.BaseApiResponse(
                                            data = resultData,
                                            message = ""
                                        )
                                    )
                                )
                            }
                            is CacheResult.GenericError -> {
                                emit(Resource.GenericError(message = "Something went wrong", 0))
                            }
                        }
                    }
                } else {
                    emit(Resource.Success(
                        com.noisefit_commans.data.response.BaseApiResponse(
                            data = resultData,
                            message = ""
                        )
                    ))
                }
            }
        }
    }

    private fun getLocalBTempHistoryData(
        historyType: String,
        removeData: Boolean
    ): BodyTempHistoryResponse? {
        if (removeData) {
            offlineApiStore.setGraphLocalBodyTempData(null, historyType)
            return null
        }

        return when (historyType) {
            "daily" -> offlineApiStore.getGraphLocalBodyTempData("daily")
            "weekly" -> offlineApiStore.getGraphLocalBodyTempData("weekly")
            "monthly" -> offlineApiStore.getGraphLocalBodyTempData("monthly")
            "yearly" -> offlineApiStore.getGraphLocalBodyTempData("yearly")
            else -> null
        }
    }


    override suspend fun getSleepBlogs(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<SleepBlogCategories>>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getSleepBlogs()
        }
    }

    override suspend fun getHighlights(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<GraphHighlightResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getHighlights()
        }
    }

    override suspend fun getStepsHighlights(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<GraphHighlightResponse>>> {

        return flow {
            var resultData: GraphHighlightResponse? = null
            val shouldCallApi =
                lastSyncProvider.getSyncTimeStamp(LastSyncItems.HEALTH_STEPS_HIGHLIGHT)
                    .checkDayDifferenceMoreOne()


            val cacheResult = safeCacheCall(Dispatchers.IO) {
                if (shouldCallApi) {
                    offlineApiStore.setGraphLocalStepsHighlightData(null)
                    return@safeCacheCall null
                } else {
                    offlineApiStore.getGraphLocalStepsHighlightData()
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
                emit(Resource.Success(com.noisefit_commans.data.response.BaseApiResponseData(data = resultData)))
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                val url = "${BuildConfig.STEPS_URL}/step_activities/v3/users/highlights/data"
                remoteDataSource.getStepsHighlights(url)
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

                            lastSyncProvider.setSyncTimeStamp(LastSyncItems.HEALTH_STEPS_HIGHLIGHT)
                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                safeCacheCall(Dispatchers.IO) {
                    offlineApiStore.setGraphLocalStepsHighlightData(resultData)
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(
                                Resource.Success(
                                    com.noisefit_commans.data.response.BaseApiResponseData(data = resultData)
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

    override suspend fun getChallengeLeaderboard(
        forceRefresh: Boolean,
        challengeId: String
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<ArrayList<Leadership>>>> {

        return flow {
            val resultData = ArrayList<Leadership>()


            val cacheResult = safeCacheCall(Dispatchers.IO) {

                val localData =
                    keyValueDataSource.getData(
                        challengeId,
                        KeyValueDataType.CHALLENGE_LEADERBOARD_2
                    )
                        ?: return@safeCacheCall null

                val shouldCallApi =
                    localData.getSafeLastSyncValue()
                        .checkDayDifferenceMoreOne() || forceRefresh || mOnlineMode
                if (shouldCallApi) {
                    keyValueDataSource.removeDataByKey(
                        challengeId,
                        KeyValueDataType.CHALLENGE_LEADERBOARD_2
                    )
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<List<Leadership>>(
                            it
                        )
                    }
                }
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
                    "${BuildConfig.BASE_URL_NEW}/challenges/leaderboard/$challengeId"
                remoteDataSource.getChallengeLeaderboard(url)
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
                            resultData.clear()
                            resultData.addAll(response)
                        }
                    }
                }
            }

            if (resultData.isNotEmpty()) {
                safeCacheCall(Dispatchers.IO) {
                    keyValueDataSource.insertData(
                        KeyValue(
                            key = challengeId,
                            value = gson.toJson(resultData),
                            type = KeyValueDataType.CHALLENGE_LEADERBOARD_2.name
                        )
                    )
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(
                                Resource.Success(
                                    com.noisefit_commans.data.response.BaseApiResponse(
                                        data = resultData,
                                        message = ""
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

    override suspend fun getChallengeBuddyLeaderboard(
        forceRefresh: Boolean,
        challengeId: String
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseChallenge<ArrayList<Leadership>>>> {
        return flow {
            val resultData = ArrayList<Leadership>()


            val cacheResult = safeCacheCall(Dispatchers.IO) {

                val localData =
                    keyValueDataSource.getData(challengeId, KeyValueDataType.CHALLENGE_BUDDIES_2)
                        ?: return@safeCacheCall null

                val shouldCallApi =
                    localData.getSafeLastSyncValue().checkDayDifferenceMoreOne() || forceRefresh
                if (shouldCallApi) {
                    keyValueDataSource.removeDataByKey(
                        challengeId,
                        KeyValueDataType.CHALLENGE_BUDDIES_2
                    )
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<List<Leadership>>(
                            it
                        )
                    }
                }
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
                emit(
                    Resource.Success(
                        com.noisefit_commans.data.response.BaseApiResponseChallenge(
                            data = resultData,
                            message = "",
                            success = true
                        )
                    )
                )
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                val url =
                    "${BuildConfig.BASE_URL_NEW}/challenges/buddies/leaderboard/$challengeId"
                remoteDataSource.getChallengeBuddyLeaderboard(url)
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
                            resultData.clear()
                            resultData.addAll(response)
                        }
                    }
                }
            }

            if (resultData.isNotEmpty()) {
                safeCacheCall(Dispatchers.IO) {
                    keyValueDataSource.insertData(
                        KeyValue(
                            key = challengeId,
                            value = gson.toJson(resultData),
                            type = KeyValueDataType.CHALLENGE_BUDDIES_2.name
                        )
                    )
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(
                                Resource.Success(
                                    com.noisefit_commans.data.response.BaseApiResponseChallenge(
                                        data = resultData,
                                        message = "",
                                        success = true
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

    override suspend fun removeLocalChallengesData() {
        lastSyncProvider.removeSyncTimeStamp(LastSyncItems.CHALLENGE_LIST_CURRENT)
        lastSyncProvider.removeSyncTimeStamp(LastSyncItems.CHALLENGE_LIST_COMPLETED)
        keyValueDataSource.removeDataByType(KeyValueDataType.CHALLENGE_2)
        keyValueDataSource.removeDataByType(KeyValueDataType.CHALLENGE_BUDDIES_2)
        keyValueDataSource.removeDataByType(KeyValueDataType.CHALLENGE_LEADERBOARD_2)
    }

    override suspend fun removeLocalFriendsData() {
        keyValueDataSource.removeDataByType(KeyValueDataType.FRIENDS_ACTIVITY)
        keyValueDataSource.removeDataByType(KeyValueDataType.FRIENDS_COMPETITION)
        keyValueDataSource.removeDataByType(KeyValueDataType.FRIENDS_PROFILE)
    }

    override suspend fun removeLocalRewardsData() {
        keyValueDataSource.removeDataByType(KeyValueDataType.USER_COUPON)
        keyValueDataSource.removeDataByType(KeyValueDataType.USER_COUPON_LIST)
    }

    override suspend fun removeLocalStreakData() {
        keyValueDataSource.removeDataByType(KeyValueDataType.DASH_STREAK_DATA)
        keyValueDataSource.removeDataByType(KeyValueDataType.COINS_PROFILE_DATA)
        keyValueDataSource.removeDataByType(KeyValueDataType.ALL_TASK_LIST)
    }


    override suspend fun markChallengeBuddyRequest(
        mobile: String?,
        id: String,
        onFinished: () -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val localData = keyValueDataSource.getData(id, KeyValueDataType.CHALLENGE_LEADERBOARD_2)
            localData?.value?.let { value ->
                val buddiesObj = value.let {
                    Gson().fromJson<List<Leadership>>(
                        it
                    )
                }
                buddiesObj.forEach {
                    if (it.mobile.equals(mobile)) {
                        it.status = "1"
                    }
                }
                keyValueDataSource.removeDataByKey(
                    id,
                    KeyValueDataType.CHALLENGE_LEADERBOARD_2
                )//TODO optz replace with update query
                keyValueDataSource.insertData(
                    KeyValue(
                        key = id,
                        value = gson.toJson(buddiesObj),
                        type = KeyValueDataType.CHALLENGE_LEADERBOARD_2.name
                    )
                )
            }
            onFinished()
        }
    }


    override suspend fun getCurrentChallenges(forceRefresh: Boolean): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.ChallengeListingResponse>>> {

        return flow {

            var resultData: com.noisefit_commans.data.response.ChallengeListingResponse? = null
            val shouldCallApi =
                lastSyncProvider.getSyncTimeStamp(LastSyncItems.CHALLENGE_LIST_CURRENT)
                    .checkDayDifferenceMoreOne() || forceRefresh || mOnlineMode

            val cacheResult = safeCacheCall(Dispatchers.IO) {
                if (shouldCallApi) {
                    offlineApiStore.setCurrentChallenges(null)
                    return@safeCacheCall null
                } else {
                    offlineApiStore.getCurrentChallenges()
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
                    "${BuildConfig.BASE_URL_NEW}/challenges/current_list"
                remoteDataSource.getCurrentChallenges(
                    url
                )
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

                            lastSyncProvider.setSyncTimeStamp(LastSyncItems.CHALLENGE_LIST_CURRENT)
                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                safeCacheCall(Dispatchers.IO) {
                    offlineApiStore.setCurrentChallenges(resultData)
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

    override suspend fun getCompletedChallenges(forceRefresh: Boolean): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.ChallengeListingResponse>>> {
        return flow {

            var resultData: com.noisefit_commans.data.response.ChallengeListingResponse? = null
            val shouldCallApi =
                lastSyncProvider.getSyncTimeStamp(LastSyncItems.CHALLENGE_LIST_COMPLETED)
                    .checkDayDifferenceMoreOne() || forceRefresh || mOnlineMode


            val cacheResult = safeCacheCall(Dispatchers.IO) {
                if (shouldCallApi) {
                    offlineApiStore.setCompletedChallenges(null)
                    return@safeCacheCall null
                } else {
                    offlineApiStore.getCompletedChallenges()
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
                    "${BuildConfig.BASE_URL_NEW}/challenges/completed"
                remoteDataSource.getCompletedChallenges(
                    url
                )
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

                            lastSyncProvider.setSyncTimeStamp(LastSyncItems.CHALLENGE_LIST_COMPLETED)
                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                safeCacheCall(Dispatchers.IO) {
                    offlineApiStore.setCompletedChallenges(resultData)
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

    override suspend fun getChallengeDetailByID(
        forceRefresh: Boolean,
        challengeId: Int
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.ChallengeModel>>> {
        return flow {
            var resultData: com.noisefit_commans.data.response.ChallengeModel? = null

            val cacheResult = safeCacheCall(Dispatchers.IO) {

                val localData =
                    keyValueDataSource.getData(challengeId.toString(), KeyValueDataType.CHALLENGE_2)
                        ?: return@safeCacheCall null

                val lastCallTime = localData.getSafeLastSyncValue()

                val shouldCallApi =
                    lastCallTime.checkDayDifferenceMoreOne() || forceRefresh || lastCallTime.checkDayDifferenceMoreNMinutes(
                        60
                    ) || mOnlineMode

                if (shouldCallApi) {
                    keyValueDataSource.removeDataByKey(
                        challengeId.toString(),
                        KeyValueDataType.CHALLENGE_2
                    )
                    keyValueDataSource.removeDataByKey(
                        challengeId.toString(),
                        KeyValueDataType.CHALLENGE_LEADERBOARD_2
                    )
                    keyValueDataSource.removeDataByKey(
                        challengeId.toString(),
                        KeyValueDataType.CHALLENGE_BUDDIES_2
                    )
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<com.noisefit_commans.data.response.ChallengeModel>(
                            it
                        )
                    }
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
                    "${BuildConfig.BASE_URL_NEW}/challenges/detail/$challengeId"
                remoteDataSource.getChallengeDetailByID(url)
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


                            val challengeList = offlineApiStore.getCurrentChallenges()

                            challengeList?.new?.forEach { challenge ->
                                if (challenge.challenge_id == challengeId) {
                                    challenge.participants =
                                        response.participants ?: challenge.participants
                                }
                            }

                            challengeList?.joined?.forEach { challenge ->
                                if (challenge.challenge_id == challengeId) {
                                    challenge.participants =
                                        response.participants ?: challenge.participants

                                    if (response.isDisqualified()) {
                                        challenge.user_rank = null
                                    }
                                }
                            }
                            offlineApiStore.setCurrentChallenges(challengeList)
                        }
                    }
                }
            }

            if (resultData != null) {
                safeCacheCall(Dispatchers.IO) {
                    keyValueDataSource.insertData(
                        KeyValue(
                            key = challengeId.toString(),
                            value = gson.toJson(resultData),
                            type = KeyValueDataType.CHALLENGE_2.name
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

    override suspend fun joinChallengeById(
        challengeId: Int
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<String>>> {
        val url =
            "${BuildConfig.BASE_URL_NEW}/challenges/users/join/$challengeId"
        return safeApiCallFlow(dispatcher) {
            removeLocalStreakData()
            lastSyncProvider.removeSyncTimeStamp(LastSyncItems.CHALLENGE_LIST_CURRENT)
            remoteDataSource.joinChallengeById(url)
        }
    }

    override suspend fun leaveChallengeById(
        requestObject: JsonObject
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<String>>> {
        val url =
            "${BuildConfig.BASE_URL_NEW}/challenges/users/leave"
        return safeApiCallFlow(dispatcher) {
            lastSyncProvider.removeSyncTimeStamp(LastSyncItems.CHALLENGE_LIST_CURRENT)
            remoteDataSource.leaveChallengeById(url, requestObject)
        }
    }
}