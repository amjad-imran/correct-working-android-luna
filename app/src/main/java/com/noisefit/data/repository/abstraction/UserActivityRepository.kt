package com.noisefit.data.repository.abstraction

import com.google.gson.JsonObject
import com.noisefit_commans.data.model.UserDataPost
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.*
import com.noisefit_commans.data.model.history.*
import com.noisefit_commans.response.SleepHistoryResponse
import kotlinx.coroutines.flow.Flow
import java.util.*

interface UserActivityRepository {

    suspend fun postCombinedHistoryData(request: UserDataPost): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<VersionCheckResponse>>>

    suspend fun getStepsHistory(
        historyType: String,
        selectedStartDate: String?,
        selectedEndDate: String?
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<StepsHistoryResponse>>>

    suspend fun getStressHistory(
        historyType: String,
        selectedStartDate: String?,
        selectedEndDate: String?
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<StressHistoryResponse>>>

    suspend fun getBloodOxygenHistory(
        historyType: String,
        selectedStartDate: String?,
        selectedEndDate: String?
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<BoHistoryResponse>>>

    suspend fun getHrHistory(
        historyType: String,
        selectedStartDate: String?,
        selectedEndDate: String?,
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<HrHistoryResponse>>>

    suspend fun getSleepHistory(
        historyType: String,
        selectedStartDate: String?,
        selectedEndDate: String?
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<SleepHistoryResponse>>>

    suspend fun getSleepHighlights(
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<SleepHighlightResponse>>>


    suspend fun getBodyTempHistory(
        historyType: String,
        selectedStartDate: String?,
        selectedEndDate: String?
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<BodyTempHistoryResponse>>>


    suspend fun getSleepBlogs(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<SleepBlogCategories>>>>

    suspend fun getHighlights(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<GraphHighlightResponse>>>

    suspend fun getStepsHighlights(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<GraphHighlightResponse>>>


    suspend fun getChallengeLeaderboard(
        forceRefresh: Boolean,
        challengeId: String
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<ArrayList<Leadership>>>>


    suspend fun getChallengeBuddyLeaderboard(
        forceRefresh: Boolean,
        challengeId: String
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseChallenge<ArrayList<Leadership>>>>



    suspend fun getCurrentChallenges(forceRefresh: Boolean): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.ChallengeListingResponse>>>

    suspend fun getCompletedChallenges(forceRefresh: Boolean): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.ChallengeListingResponse>>>

    suspend fun getChallengeDetailByID(
        forceRefresh: Boolean,
        challengeId: Int
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.ChallengeModel>>>

    suspend fun joinChallengeById(
        challengeId: Int
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<String>>>

    suspend fun leaveChallengeById(
        requestObject: JsonObject
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<String>>>



    suspend fun removeLocalChallengesData()


    suspend fun removeLocalFriendsData()

    suspend fun removeLocalRewardsData()

    suspend fun removeLocalStreakData()

    suspend fun markChallengeBuddyRequest(mobile: String?, ids: String, onFinished: () -> Unit)





}