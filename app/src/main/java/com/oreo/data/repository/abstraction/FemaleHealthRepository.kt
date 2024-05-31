package com.oreo.data.repository.abstraction

import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import com.oreo.data.model.FemaleHealthIconsModel
import com.oreo.data.model.PeriodCycleHistory
import com.oreo.data.model.femaleh.FemaleCycleTrackInfoModel
import com.oreo.data.model.femaleh.FemaleHealthUserInfoModel
import com.oreo.data.model.femaleh.PeriodLengthListResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Url

interface FemaleHealthRepository {

    suspend fun saveLogSymptom(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<Any>>>
    suspend fun submitFemaleHealthInfo(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<Any>>>
    suspend fun getFemaleHealthUserInfo(selectDate: String): Flow<Resource<BaseApiResponse<FemaleHealthUserInfoModel?>>>
    suspend fun logPeriod(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<Any>>>
    suspend fun getCycleLengthData(date: String): Flow<Resource<BaseApiResponse<PeriodLengthListResponse>>>
    suspend fun getPeriodDurationList(date: String): Flow<Resource<BaseApiResponse<PeriodLengthListResponse>>>

    suspend fun getPeriodCycleHistory(): Flow<Resource<BaseApiResponse<PeriodCycleHistory>>>
    suspend fun getCycleTrackerInfo(): Flow<Resource<BaseApiResponse<FemaleCycleTrackInfoModel?>?>>
    suspend fun updateCycleTrackerInfo(
        jsonObject: JsonObject,
        id: Long
    ): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun getFemaleHealthIcons():Flow<Resource<BaseApiResponse<FemaleHealthIconsModel>>>

}