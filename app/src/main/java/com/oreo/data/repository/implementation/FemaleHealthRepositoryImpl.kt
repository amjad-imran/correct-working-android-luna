package com.oreo.data.repository.implementation

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit.data.local.db.fromJson
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.safeApiCallFlow
import com.noisefit.data.safeCacheCall
import com.noisefit.luna.BuildConfig
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.KeyValue
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.ui.checkDayDifferenceMoreOne
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.FemaleHealthIconsModel
import com.oreo.data.model.LearnModel
import com.oreo.data.model.OHSModel
import com.oreo.data.model.PeriodCycleHistory
import com.oreo.data.model.femaleh.FemaleCycleTrackInfoModel
import com.oreo.data.model.femaleh.FemaleHealthUserInfoModel
import com.oreo.data.model.femaleh.FemaleTempResponse
import com.oreo.data.model.femaleh.PeriodLengthListResponse
import com.oreo.data.repository.abstraction.FemaleHealthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

class FemaleHealthRepositoryImpl(
    private val remoteDataSource: NetworkService,
    private val localDataStore: DataStoredInterface,
    private val keyValueDataSource: KeyValueDataSource,
    private val gson: Gson,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : FemaleHealthRepository {
    override suspend fun saveLogSymptom(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/log/symptom"
            remoteDataSource.saveLogSymptom(url, jsonObject)

        }
    }

    override suspend fun submitFemaleHealthInfo(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/user-health"
            remoteDataSource.submitFemaleHealthInfo(url, jsonObject)

        }
    }

    override suspend fun getFemaleHealthUserInfo(selectDate: String): Flow<Resource<BaseApiResponse<FemaleHealthUserInfoModel?>>> {
        if (LocalDate.parse(selectDate) == LocalDate.now()) {
            return flow {
                emit(Resource.Loading(true))
                val type = KeyValueDataType.FEMALE_HEALTH_CURRENT_DAY
                var resultData: FemaleHealthUserInfoModel? = null

                val cacheResult = safeCacheCall(Dispatchers.IO) {
                    val localData =
                        keyValueDataSource.getData("", type)
                            ?: return@safeCacheCall null

                    val lastCallTime = localData.getSafeLastSyncValue()

                    val shouldCallApi = lastCallTime.checkDayDifferenceMoreOne()
                    LOGS.d("FORCE_REFRESH should call api $shouldCallApi")


                    if (shouldCallApi) {
                        keyValueDataSource.removeDataByKey("", type)
                        return@safeCacheCall null
                    } else {

                        if (localData.value == null) {
                            return@safeCacheCall null
                        }

                        return@safeCacheCall localData.value?.let {
                            Gson().fromJson<FemaleHealthUserInfoModel>(
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
                    emit(Resource.Loading(false))
                    emit(
                        Resource.Success(
                            BaseApiResponse(
                                data = resultData,
                                message = "",
                            )
                        )
                    )
                    return@flow
                }


                val serverResult = safeApiCallFlow(dispatcher) {
                    val url = "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/user-health"
                    remoteDataSource.getFemaleHealthInfo(url, selectDate)
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
                                key = "",
                                value = gson.toJson(resultData),
                                type = type.name
                            )
                        )
                    }.collect { resource ->
                        when (resource) {
                            is CacheResult.Success -> {
                                emit(Resource.Loading(false))
                                emit(
                                    Resource.Success(
                                        BaseApiResponse(
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
                } else {
                    emit(Resource.Loading(false))
                    emit(
                        Resource.Success(
                            BaseApiResponse(
                                data = null,
                                message = "",
                            )
                        )
                    )
                }
            }
        } else {
            return safeApiCallFlow(dispatcher) {
                val url = "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/user-health"
                remoteDataSource.getFemaleHealthInfo(url, selectDate)
            }
        }
    }

    override suspend fun logPeriod(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {

            keyValueDataSource.removeDataByType(KeyValueDataType.FEMALE_CYCLE_HISTORY)
            keyValueDataSource.removeDataByType(KeyValueDataType.FEMALE_HEALTH_CURRENT_DAY)

            val url = "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/log/period"
            remoteDataSource.logPeriod(url, jsonObject)
        }
    }

    override suspend fun getCycleLengthData(date: String): Flow<Resource<BaseApiResponse<PeriodLengthListResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/user-health/cycle_length"
            remoteDataSource.getPeriodLengthList(url, date)
        }
    }

    override suspend fun getPeriodDurationList(date: String): Flow<Resource<BaseApiResponse<PeriodLengthListResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/user-health/period_length"
            remoteDataSource.getPeriodDurationList(url, date)
        }
    }

    override suspend fun getPeriodCycleHistory(): Flow<Resource<BaseApiResponse<PeriodCycleHistory>>> {
        return flow {
            emit(Resource.Loading(true))

            val type = KeyValueDataType.FEMALE_CYCLE_HISTORY
            var resultData: PeriodCycleHistory? = null


            val cacheResult = safeCacheCall(Dispatchers.IO) {
                val localData =
                    keyValueDataSource.getData("", type)
                        ?: return@safeCacheCall null

                val lastCallTime = localData.getSafeLastSyncValue()

                val shouldCallApi =
                    lastCallTime.checkDayDifferenceMoreOne()
                LOGS.d("FORCE_REFRESH should call api $shouldCallApi")


                if (shouldCallApi) {
                    keyValueDataSource.removeDataByKey("", type)
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<PeriodCycleHistory>(
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
                emit(Resource.Loading(false))
                emit(
                    Resource.Success(
                        BaseApiResponse(
                            data = resultData,
                            message = "",
                        )
                    )
                )
                return@flow
            }


            val serverResult = safeApiCallFlow(dispatcher) {
                val url =
                    "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/user-health/cycle_history?first_data=true"
                remoteDataSource.getPeriodCycleHistory(url)
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
                            key = "",
                            value = gson.toJson(resultData),
                            type = type.name
                        )
                    )
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(Resource.Loading(false))
                            emit(
                                Resource.Success(
                                    BaseApiResponse(
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

    override suspend fun getCycleTrackerInfo(): Flow<Resource<BaseApiResponse<FemaleCycleTrackInfoModel?>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/user-info"
            remoteDataSource.getCycleTrackerInfo(url)
        }
    }

    override suspend fun updateCycleTrackerInfo(
        jsonObject: JsonObject, id: Long
    ): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            keyValueDataSource.removeDataByType(KeyValueDataType.FEMALE_CYCLE_HISTORY)
            keyValueDataSource.removeDataByType(KeyValueDataType.FEMALE_HEALTH_CURRENT_DAY)

            val url = "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/user-info/$id"
            remoteDataSource.updateCycleTrackerInfo(url, jsonObject)
        }
    }

    override suspend fun getFemaleHealthIcons(): Flow<Resource<BaseApiResponse<FemaleHealthIconsModel>>> {
        return flow {
            emit(Resource.Loading(true))
            val type = KeyValueDataType.FEMALE_SYMPTOMS_ICON
            var resultData: FemaleHealthIconsModel? = null

            val cacheResult = safeCacheCall(Dispatchers.IO) {
                val localData =
                    keyValueDataSource.getData("", type)
                        ?: return@safeCacheCall null

                val lastCallTime = localData.getSafeLastSyncValue()

                val shouldCallApi =
                    lastCallTime.checkDayDifferenceMoreOne()
                LOGS.d("FORCE_REFRESH should call api $shouldCallApi")


                if (shouldCallApi) {
                    keyValueDataSource.removeDataByKey("", type)
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<FemaleHealthIconsModel>(
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
                emit(Resource.Loading(false))
                emit(
                    Resource.Success(
                        BaseApiResponse(
                            data = resultData,
                            message = "",
                        )
                    )
                )
                return@flow
            }


            val serverResult = safeApiCallFlow(dispatcher) {
                val url = "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/icon"
                remoteDataSource.getFemaleHealthIcons(url)
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
                            key = "",
                            value = gson.toJson(resultData),
                            type = type.name
                        )
                    )
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(Resource.Loading(false))
                            emit(
                                Resource.Success(
                                    BaseApiResponse(
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

    override suspend fun getFemaleHealthTempData(date: String): Flow<Resource<BaseApiResponse<FemaleTempResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/temp"
            remoteDataSource.getFemaleHealthTempData(url, date)
        }
    }

    override suspend fun setPeriodConfirm(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/user-health/confirm"
            remoteDataSource.setPeriodConfirm(url, jsonObject)
        }
    }

    /**
     * if Got period is already clicked for today's date, this function will return true else false
     */
    override fun getGotPeriodClickedStatus(): Boolean {
        return localDataStore.getGotPeriodClickedStatus()
    }

    override fun saveGotPeriodClicked() {
        localDataStore.saveGotPeriodClicked()
    }
}