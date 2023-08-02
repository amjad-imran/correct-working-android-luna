package com.oreo.data.repository.abstraction

import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.BaseApiResponseData
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OContributorResponseModal
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.OInternalPageResponseModal
import com.oreo.data.model.OWorkoutDetailsResponseModel
import com.oreo.data.model.OWorkoutListModal
import com.oreo.data.model.health.OreoActivityModel
import com.oreo.data.model.health.OreoDashboardResponseModel
import com.oreo.data.model.health.OreoReadinessModel
import com.oreo.data.model.health.OreoSleepModel
import com.oreo.receiver.workManager.HealthOverviewDataType
import com.oreo.ui.TestUserData
import kotlinx.coroutines.flow.Flow

interface OreoUserActivityRepository {

    suspend fun getDashboardData(): Flow<Resource<BaseApiResponse<OreoDashboardResponseModel>>>

    suspend fun getSleepHistory(date: String): Flow<Resource<BaseApiResponse<List<OreoSleepModel>>>>

    suspend fun getActivityHistory(date: String): Flow<Resource<BaseApiResponse<List<OreoActivityModel>>>>
    suspend fun getReadinessHistory(date: String): Flow<Resource<BaseApiResponse<List<OreoReadinessModel>>>>

    suspend fun getSummaryHRHealthOverview(): OHealthOverview?

    suspend fun getSummaryAutoWorkoutCount(): Int
    suspend fun getTestData(): List<TestUserData>

    suspend fun getTestDataListByType(data: TestUserData): List<Any>

    suspend fun getHealthOverview(
        healthOverviewDataType: HealthOverviewDataType,
        healthOverviewData: ArrayList<OHealthOverview>?
    ): Pair<ArrayList<OHealthOverview>?, Int?>

    suspend fun addWorkout(request: JsonObject): Flow<Resource<BaseApiResponseData<Any>>>
    suspend fun getWorkoutDetails(id: String): Flow<Resource<BaseApiResponse<OWorkoutDetailsResponseModel>>>

    suspend fun getWorkoutList(): Flow<Resource<BaseApiResponse<List<OWorkoutListModal>>>>
    suspend fun getRecentWorkoutList(): Flow<Resource<BaseApiResponse<List<OActivityListModal>>>>

    suspend fun getInternalPagesData(
        selectDate: String,
        filterType: String,
        contriType: String
    ): Flow<Resource<BaseApiResponse<OInternalPageResponseModal>>>

    suspend fun getActivityInternalPagesData(
        selectDate: String,
        filterType: String,
        contriType: String
    ): Flow<Resource<BaseApiResponse<OInternalPageResponseModal>>>

    suspend fun getReadinessInternalPagesData(
        selectDate: String,
        filterType: String,
        contriType: String
    ): Flow<Resource<BaseApiResponse<OInternalPageResponseModal>>>

    suspend fun getAllActivityList(
        page: Int,
        pageLimit: Int
    ): Flow<Resource<BaseApiResponse<List<OActivityListModal>>>>

    suspend fun getContributorDetailsInfo(contributorType: String): Flow<Resource<BaseApiResponse<OContributorResponseModal>>>
}