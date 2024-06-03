package com.oreo.data.repository.implementation

import com.google.gson.JsonObject
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.safeApiCallFlow
import com.noisefit.luna.BuildConfig
import com.noisefit_commans.data.response.BaseApiResponse
import com.oreo.data.model.FemaleHealthIconsModel
import com.oreo.data.model.PeriodCycleHistory
import com.oreo.data.model.femaleh.FemaleCycleTrackInfoModel
import com.oreo.data.model.femaleh.FemaleHealthUserInfoModel
import com.oreo.data.model.femaleh.FemaleTempResponse
import com.oreo.data.model.femaleh.PeriodLengthListResponse
import com.oreo.data.repository.abstraction.FemaleHealthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class FemaleHealthRepositoryImpl(
    private val remoteDataSource: NetworkService,
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
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/user-health"
            remoteDataSource.getFemaleHealthInfo(url, selectDate)
        }
    }

    override suspend fun logPeriod(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
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
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/user-health/cycle_history?first_data=true"//TODO handle on offline
            remoteDataSource.getPeriodCycleHistory(url)
        }
    }

    override suspend fun getCycleTrackerInfo(): Flow<Resource<BaseApiResponse<FemaleCycleTrackInfoModel?>?>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/user-info"
            remoteDataSource.getCycleTrackerInfo(url)
        }
    }

    override suspend fun updateCycleTrackerInfo(
        jsonObject: JsonObject,
        id: Long
    ): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/user-info/$id"
            remoteDataSource.updateCycleTrackerInfo(url, jsonObject)
        }
    }

    override suspend fun getFemaleHealthIcons(): Flow<Resource<BaseApiResponse<FemaleHealthIconsModel>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/icon"
            remoteDataSource.getFemaleHealthIcons(url)
        }
    }

    override suspend fun getFemaleHealthTempData(date: String): Flow<Resource<BaseApiResponse<FemaleTempResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.OREO_BASE_URL}/wellbeing/v1/temp"
            remoteDataSource.getFemaleHealthTempData(url,date)
        }
    }

}