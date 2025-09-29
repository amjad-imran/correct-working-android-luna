package com.noisefit.data.repository.abstraction

import android.net.Uri
import com.google.gson.JsonObject
import com.noisefit.data.model.GoalModel
import com.noisefit.data.model.timeline.MealAiResponse
import com.noisefit.data.model.timeline.SupplementsListResponse
import com.oreo.data.model.RingLocationData
import com.noisefit.data.remote.CityData
import com.noisefit.data.remote.StateData
import com.noisefit.data.remote.UserLocationUpdatedResponse
import com.noisefit_commans.data.model.*
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.*
import com.noisefit_commans.data.model.Interest
import com.noisefit_commans.data.model.circadian.NudgeCircadianGraph
import com.noisefit_commans.models.*
import com.oreo.data.model.NotificationToggleModel
import com.noisefit_commans.data.model.customHomeScreen.CustomHomeScreenModel
import com.oreo.data.model.CaffeineFoodItem
import com.oreo.data.model.CaffeinePostApiModel
import com.oreo.data.model.circadian.CircadianQuizResponseModel
import com.oreo.data.model.circadian.CircadianResponseModel
import com.noisefit_commans.data.model.timeline.TimelineScreenResponse
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

    suspend fun updateCaffeineItemsList(req: CaffeinePostApiModel): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun getCannyFeedbackUrl(): Flow<Resource<BaseApiResponse<String>>>

    suspend fun sendAppTrackingEvent(request: JsonObject): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun updateWorkoutStatus(request: JsonObject): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun submitLogCircadianData(request: JsonObject): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun getCircadianData(): Flow<Resource<BaseApiResponse<CircadianResponseModel>>>
    suspend fun getNudgeCircadianData(reqObj: JsonObject): Flow<Resource<BaseApiResponse<NudgeCircadianGraph>>>

    suspend fun getCircadianQuizData(): Flow<Resource<BaseApiResponse<List<CircadianQuizResponseModel>>>>

    suspend fun submitCircadianQuizData(req: JsonObject, isAllSkipAttempted: Boolean): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun getCurrDayTimelineActivitiesData(date: String): Flow<Resource<BaseApiResponse<TimelineScreenResponse>>>

    suspend fun submitLogMealTimelineData(req: JsonObject): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun submitLogCaffeineTimelineData(req: JsonObject): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun submitLogLightExposureTimelineData(req: JsonObject): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun submitLogSupplementsTimelineData(req: JsonObject): Flow<Resource<BaseApiResponse<Any>>>
    suspend fun submitLogAlcoholTimelineData(req: JsonObject): Flow<Resource<BaseApiResponse<Any>>>

    suspend fun getAddSupplementsListData(): Flow<Resource<BaseApiResponse<SupplementsListResponse>>>
    suspend fun getTimelineOptionIdData(option: String): Flow<Resource<BaseApiResponse<SupplementsListResponse>>>
    suspend fun getNutritionFromText(req: JsonObject): Flow<Resource<BaseApiResponse<MealAiResponse>>>

}