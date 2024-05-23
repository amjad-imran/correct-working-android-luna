package com.oreo.data.repository.abstraction

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.model.OWorkoutListModal
import com.noisefit_commans.data.model.OreoNapData
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.BaseApiResponseData
import com.oreo.data.model.AddWorkoutResponse
import com.oreo.data.model.FMHCycleHistoryDataModel
import com.oreo.data.model.LearnModel
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OContributorResponseModal
import com.oreo.data.model.OHSModel
import com.oreo.data.model.OHSQuestionariesResponseModel
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.OInternalPageResponseModal
import com.oreo.data.model.OWorkoutDetailsResponseModel
import com.oreo.data.model.OreoNapDetailsDataModel
import com.oreo.data.model.RingCareResponse
import com.oreo.data.model.RingWelcome
import com.oreo.data.model.ServerUserHealthResponse
import com.oreo.data.model.StressResultData
import com.oreo.data.model.TestUserData
import com.oreo.data.model.femaleh.FemaleCycleTrackInfoModel
import com.oreo.data.model.femaleh.FemaleHealthUserInfoModel
import com.oreo.data.model.femalehealth.PeriodLength
import com.oreo.receiver.workManager.HealthOverviewDataType
import kotlinx.coroutines.flow.Flow

interface OreoUserActivityRepository {

    suspend fun getRingCareData(): Flow<Resource<BaseApiResponse<RingCareResponse>>>
    suspend fun getWelcomeRingData(): Flow<Resource<BaseApiResponse<RingWelcome>>>

    //    suspend fun getDashboardData(forceRefresh: Boolean): Flow<Resource<BaseApiResponse<OreoDashboardResponseModel>>>
    suspend fun getLearnData(): Flow<Resource<BaseApiResponse<List<LearnModel>>>>

    //    suspend fun getSleepHistory(date: String): Flow<Resource<BaseApiResponse<List<OreoSleepModel>>>>
    suspend fun getUserHealthData(
        startDate: String? = null,
        endDate: String? = null
    ): Flow<Resource<BaseApiResponse<ServerUserHealthResponse>>>

//    suspend fun getActivityHistory(date: String): Flow<Resource<BaseApiResponse<List<OreoActivityModel>>>>
//    suspend fun getReadinessHistory(date: String): Flow<Resource<BaseApiResponse<List<OreoReadinessModel>>>>

    suspend fun getSummaryHRHealthOverview(): OHealthOverview.HeartRateDataModel?

    suspend fun getSummaryAutoWorkoutCount(): Int
    suspend fun getTestData(): List<TestUserData>

    suspend fun getTestDataListByType(data: TestUserData): List<Any>

    suspend fun getHealthOverview(
        healthOverviewDataType: HealthOverviewDataType,
        healthOverviewData: ArrayList<OHealthOverview>?
    ): Pair<ArrayList<OHealthOverview>?, Int?>

    suspend fun addWorkout(request: JsonObject): Flow<Resource<BaseApiResponseData<OActivityListModal>>>
    suspend fun addGFitWorkout(request: JsonArray): Flow<Resource<BaseApiResponseData<Any>>>
    suspend fun syncGoogleFitUserData(request: JsonObject): Flow<Resource<BaseApiResponseData<Any>>>
    suspend fun getWorkoutDetails(id: String): Flow<Resource<BaseApiResponse<OWorkoutDetailsResponseModel>>>
    suspend fun getWorkoutDetailsV2(id: String): Flow<Resource<BaseApiResponse<OWorkoutDetailsResponseModel>>>

    suspend fun getWorkoutListRecord(): Flow<Resource<BaseApiResponse<List<OWorkoutListModal>>>>
    suspend fun getWorkoutList(): Flow<Resource<BaseApiResponse<List<OWorkoutListModal>>>>
    suspend fun getRecentWorkoutList(isToday: Boolean): Flow<Resource<BaseApiResponse<List<OActivityListModal>>>>

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

    suspend fun addRecordedWorkout(
        request: JsonObject
    ): Flow<Resource<BaseApiResponse<List<AddWorkoutResponse>>>>

    suspend fun deleteWorkoutFromServer(
        id: String
    ): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun getContributorDetailsInfo(contributorType: String): Flow<Resource<BaseApiResponse<OContributorResponseModal>>>
    suspend fun getHSCategories(): Flow<Resource<BaseApiResponse<List<OHSModel>>>>
    suspend fun getHSQAnswer(quesId: String): Flow<Resource<BaseApiResponse<List<OHSQuestionariesResponseModel>>>>

    suspend fun clearAllHealthData()

    suspend fun getUserNapData(
        napId: String
    ): Flow<Resource<BaseApiResponse<OreoNapDetailsDataModel>>>

    suspend fun addNapServer(
        nap: OreoNapData
    ): Flow<Resource<BaseApiResponse<List<OreoNapDetailsDataModel>>>>

    suspend fun getNapsToConfirm(): List<OreoNapData>?

    suspend fun removeNap(id: Int): Boolean
    suspend fun getStressInternalPagesData(
        selectDate: String,
        dayType: String,
        filterType: String
    ): Flow<Resource<BaseApiResponse<List<StressResultData>>>>

    //female health
    suspend fun submitFemaleHealthInfo(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<Any>>>
    suspend fun getFemaleHealthUserInfo(selectDate: String): Flow<Resource<BaseApiResponse<FemaleHealthUserInfoModel?>>>
    suspend fun logPeriod(jsonObject: JsonObject): Flow<Resource<BaseApiResponse<Any>>>
    suspend fun getPeriodLengthList(date: String): Flow<Resource<BaseApiResponse<List<PeriodLength>>>>
    suspend fun getPeriodDurationList(date: String): Flow<Resource<BaseApiResponse<List<PeriodLength>>>>

    suspend fun getPeriodCycleHistory(): Flow<Resource<BaseApiResponse<List<FMHCycleHistoryDataModel>>>>
    suspend fun getCycleTrackerInfo(): Flow<Resource<BaseApiResponse<FemaleCycleTrackInfoModel?>?>>
    suspend fun updateCycleTrackerInfo(
        jsonObject: JsonObject,
        id: Long
    ): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun getCycleStreakInfo(): Flow<Resource<BaseApiResponse<List<FMHCycleHistoryDataModel>>>>

}