package com.noisefit.data.repository.abstraction

import com.google.gson.JsonObject
import com.noisefit_commans.data.model.*
import com.noisefit.data.remote.CityData
import com.noisefit.data.remote.StateData
import com.noisefit.data.remote.UserLocationUpdatedResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.*
import kotlinx.coroutines.flow.Flow

interface FriendsRepository {
    suspend fun getFriendList(
        forceRefresh: Boolean,
        startDate: String,
        endDate: String,
        duration: String
    ): Flow<Resource<BaseApiResponse<FriendsData>>>


    suspend fun getFriendProfile(
        forceRefresh: Boolean,
        request: JsonObject,
        profileId: Long
    ): Flow<Resource<BaseApiResponse<FriendProfile>>>

    suspend fun getPendingRequestCount(
    ): Flow<Resource<BaseApiResponse<RequestCountResponse>>>

    suspend fun getCommonFriends(request: JsonObject): Flow<Resource<BaseApiResponse<List<BuddiesUserNew>>>>


    suspend fun getFriendChallenges(userId: Long): Flow<Resource<BaseApiResponse<ChallengeFriendListingResponse>>>

    suspend fun getFriendBadges(request: JsonObject): Flow<Resource<BaseApiResponse<List<FriendBadge>>>>

    suspend fun getCompetitionList(forceRefresh: Boolean): Flow<Resource<BaseApiResponse<CompetitionListResponse>>>

    suspend fun getCompeteFriendsList(): Flow<Resource<BaseApiResponse<CompeteFriendResponse>>>

    suspend fun getNoiseFitContactsList(request: JsonObject): Flow<Resource<BaseApiResponse<List<BuddiesUserNew>>>>

    suspend fun getInterestList(request: JsonObject): Flow<Resource<BaseApiResponse<List<BuddiesUserNew>>>>

    suspend fun getPastWinnerList(request: JsonObject): Flow<Resource<BaseApiResponse<List<BuddiesUserNew>>>>

    suspend fun getNearBy(request: JsonObject): Flow<Resource<BaseApiResponse<List<BuddiesUserNew>>>>

    suspend fun getReceivedRequest(): Flow<Resource<BaseApiResponse<ReceivedRequestResponse>>>

    suspend fun getSentRequest(): Flow<Resource<BaseApiResponse<ReceivedRequestResponse>>>

    suspend fun getAllCompetitionRequests(): Flow<Resource<BaseApiResponse<List<Requests>>>>

    suspend fun setCompetitionRequestStatus(request: JsonObject): Flow<Resource<BaseApiResponse<String>>>

    suspend fun setFriendRequestStatus(request: JsonObject): Flow<Resource<BaseApiResponse<String>>>

    suspend fun saveUserLocation(request: JsonObject): Flow<Resource<BaseApiResponse<UserLocationUpdatedResponse>>>

    suspend fun postEmojiToUser(
        request: JsonObject,
        resetCache: Boolean
    ): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun getStateList(): Flow<Resource<BaseApiResponse<List<StateData>>>>

    suspend fun getUserFriendEmoji(): Flow<Resource<BaseApiResponse<UserFriendReactions>>>
    suspend fun getYearlySummary(): Flow<Resource<BaseApiResponse<RoundUpResponse>>>

    suspend fun getCityList(stateId: Int): Flow<Resource<BaseApiResponse<List<CityData>>>>

    suspend fun updateEmojiLocally(emoji: String?, profileId: Long)
}