package com.noisefit.data.repository.abstraction

import android.net.Uri
import com.google.gson.JsonObject
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.CityData
import com.noisefit.data.remote.StateData
import com.noisefit.data.remote.UserLocationUpdatedResponse
import com.noisefit_commans.data.model.*
import com.noisefit_commans.data.model.trophies.Trophies
import com.noisefit_commans.data.model.trophies.TrophyBadge
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.request.UpdateAdditionalDetailRequest
import com.noisefit_commans.data.response.*
import com.noisefit_commans.data.model.Interest
import com.noisefit_commans.models.*
import kotlinx.coroutines.flow.Flow
import java.io.File

interface UserRepository {

    suspend fun getTrophiesData(
        date: String,
        unitSystem: String
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<Trophies>>>

    suspend fun getBuddiesTrophiesData(requestObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<Trophies>>>

    suspend fun getRecentTrophies(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<TrophyBadge>>>>

    suspend fun collectBadge(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<String>>>

    suspend fun collectChallengeTrophy(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseChallenge<Any>>>

    suspend fun searchProduct(request: String): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>>

    suspend fun saveUserDevice(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<UpdateDeviceResponse>>>

    suspend fun saveAdditionalDetails(request: UpdateAdditionalDetailRequest): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>>

    suspend fun getUserProfileInfo(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<UserInfoResponse>>>

    suspend fun getUserProfile(): Flow<Resource<UserResponse>>

    suspend fun updatePushToken(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>>

    suspend fun updateUserProfile(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<User>>>

    suspend fun updateUserProfile(request: User): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<User>>>

    suspend fun getTimeZonesCities(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<WorldClockResponse>>>

    suspend fun getOrderToken(reqest: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<String>>>

    suspend fun getActivities(
        startDate: String,
        endDate: String
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.ActivityListResponse>>>


    suspend fun getActivitiesPaging(
        page: Int,
        pageLimit: Int
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<SportsModeResponse>>>>

    suspend fun getWorkoutShareImages(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<String>>>>

    suspend fun getActivitiesDetails(itemId: Int): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<SportsModeResponse>>>

    suspend fun getRecentActivitiesDates(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<JsonObject>>>
    suspend fun getDashboardBanner(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<DashboardBannerData>>>

    suspend fun postActivities(request: SportsModeRequestList): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseActivity>>

    suspend fun getNoiseHealthContent(): Flow<Resource<NoiseHealthResponse>>

    suspend fun setNoiseHealthContentView(request: JsonObject): Flow<Resource<Unit>>

    suspend fun setNoiseHealthContentPlayTime(request: JsonObject): Flow<Resource<Unit>>

    suspend fun getRecentPlayedContent(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<NoiseHealthCategory?>>>



    suspend fun getSummaryRecentActivities(isForceRefresh: Boolean): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<RecentActivities>>>

    suspend fun uploadUserImage(imageUri: Uri): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseImage>>
    suspend fun geRecentActivities(isForceRefresh: Boolean): Flow<Resource<BaseApiResponse<RecentActivities>>>

    suspend fun getRecentChallenges(): Flow<Resource<BaseApiResponse<List<ChallengeModel>>>>

    suspend fun uploadCrashLogFile(file: File): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>>




    fun getUnitSystem(): Units

    fun getUserGoals(): UserGoals?

    fun getUserInfo(): UserInfo?

    fun getUser(): User?


    suspend fun getInterests(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<Interest>>>>

    suspend fun updateInterests(requestObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<Interest>>>>

    suspend fun saveUserLocation(request: JsonObject): Flow<Resource<BaseApiResponse<UserLocationUpdatedResponse>>>
    suspend fun getStateList(): Flow<Resource<BaseApiResponse<List<StateData>>>>
    suspend fun getCityList(stateId: Int): Flow<Resource<BaseApiResponse<List<CityData>>>>
}