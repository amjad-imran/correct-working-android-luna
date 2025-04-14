package com.noisefit.data.repository.abstraction

import android.net.Uri
import com.google.gson.JsonObject
import com.noisefit.data.model.GoalModel
import com.oreo.data.model.RingLocationData
import com.noisefit.data.remote.CityData
import com.noisefit.data.remote.StateData
import com.noisefit.data.remote.UserLocationUpdatedResponse
import com.noisefit_commans.data.model.*
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.*
import com.noisefit_commans.data.model.Interest
import com.noisefit_commans.models.*
import com.oreo.data.model.NotificationToggleModel
import com.noisefit_commans.data.model.customHomeScreen.CustomHomeScreenModel
import com.oreo.ui.caffeineWindowScreen.CaffeineFoodItem
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun saveActivity(sportsModeResponse: List<SportsModeResponse>?)

    suspend fun saveUserDevice(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<UpdateDeviceResponse>>>

    suspend fun getUserProfile(): Flow<Resource<UserResponse>>

    suspend fun updatePushToken(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>>

    suspend fun updateUserProfile(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<User>>>

    suspend fun getUserGoalsList(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<GoalModel>>>>

    suspend fun uploadUserImage(imageUri: Uri): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseImage>>

    fun getUnitSystem(): Units

    fun getUserGoals(): UserGoals?

    fun getUserInfo(): UserInfo?

    fun getUser(): User?


    suspend fun getInterests(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<Interest>>>>

    suspend fun updateInterests(requestObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<Interest>>>>

    suspend fun saveUserLocation(request: JsonObject): Flow<Resource<BaseApiResponse<UserLocationUpdatedResponse>>>
    suspend fun getStateList(): Flow<Resource<BaseApiResponse<List<StateData>>>>
    suspend fun getCityList(stateId: Int): Flow<Resource<BaseApiResponse<List<CityData>>>>

    suspend fun getRingLastLocation(mac:String): Flow<Resource<BaseApiResponse<RingLocationData>>>
    suspend fun setRingLastLocation(request: JsonObject): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun saveAppLanguage(): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun getNotificationToggle(): Flow<Resource<BaseApiResponse<NotificationToggleModel>>>

    suspend fun updateNotificationToggle(requestObject: JsonObject): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun submitCustomHomeScreenPriority(request: CustomHomeScreenModel): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun getCaffeineWindowItemsList(): Flow<Resource<BaseApiResponse<List<CaffeineFoodItem>>>>

    suspend fun getCannyFeedbackUrl(): Flow<Resource<BaseApiResponse<String>>>

    suspend fun sendAppTrackingEvent(request: JsonObject): Flow<Resource<BaseApiResponse<Any>>>

}