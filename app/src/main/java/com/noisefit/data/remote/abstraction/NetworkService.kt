package com.noisefit.data.remote.abstraction

import com.google.gson.JsonObject
import com.noisefit.data.model.RingLocationData
import com.noisefit.data.remote.CityData
import com.noisefit.data.remote.StateData
import com.noisefit.data.remote.UserLocationUpdatedResponse
import com.noisefit.data.remote.request.LoginRequest
import com.noisefit_commans.data.model.Interest
import com.noisefit_commans.data.model.OWorkoutListModal
import com.noisefit_commans.data.model.OreoNapNetworkEntity
import com.noisefit_commans.data.model.OreoUserDataPost
import com.noisefit_commans.data.model.User
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.BaseApiResponseData
import com.noisefit_commans.data.response.BaseApiResponseImage
import com.noisefit_commans.data.response.DeviceFeatureResponse
import com.noisefit_commans.data.response.DeviceListResponse
import com.noisefit_commans.data.response.MessageResponse
import com.noisefit_commans.data.response.RegistrationResponse
import com.noisefit_commans.data.response.SendOtpResponse
import com.noisefit_commans.data.response.UpdateDeviceResponse
import com.noisefit_commans.data.response.UpdateResponse
import com.noisefit_commans.data.response.UserResponse
import com.noisefit_commans.data.response.VersionCheckResponse
import com.noisefit_commans.data.response.WatchTokenResponse
import com.oreo.data.model.AddWorkoutResponse
import com.oreo.data.model.ChatGptResponse
import com.oreo.data.model.FMHCycleHistoryDataModel
import com.oreo.data.model.FemaleHealthIconsModel
import com.oreo.data.model.HealthCalendar
import com.oreo.data.model.LearnModel
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OContributorResponseModal
import com.oreo.data.model.OHSModel
import com.oreo.data.model.OHSQuestionariesResponseModel
import com.oreo.data.model.OInternalPageResponseModal
import com.oreo.data.model.OSleepInternalTrendsDataModel
import com.oreo.data.model.OWorkoutDetailsResponseModel
import com.oreo.data.model.OreoNapDetailsDataModel
import com.oreo.data.model.PeriodCycleHistory
import com.oreo.data.model.RingCareResponse
import com.oreo.data.model.RingWelcome
import com.oreo.data.model.ServerUserHealthResponse
import com.oreo.data.model.StressResultData
import com.oreo.data.model.UpdateResponseV2
import com.oreo.data.model.ai.ChatHistoryItem
import com.oreo.data.model.ai.ChatMessagesResponse
import com.oreo.data.model.ai.ThreadIdResponse
import com.oreo.data.model.femaleh.FemaleCycleTrackInfoModel
import com.oreo.data.model.femaleh.FemaleHealthUserInfoModel
import com.oreo.data.model.femaleh.FemaleTempResponse
import com.oreo.data.model.femaleh.PeriodLengthListResponse
import com.oreo.data.model.sleep.SleepDataResponse
import com.oreo.data.model.sleep.SleepDay
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Url


interface NetworkService {

    //App APIs
    @POST
    suspend fun checkAppVersion(
        @Url url: String, @Body jsonObject: JsonObject
    ): BaseApiResponse<VersionCheckResponse>

    @POST
    suspend fun checkAppVersionV2(
        @Url url: String, @Body jsonObject: JsonObject
    ): BaseApiResponse<UpdateResponseV2>


    //Auth APIs
    @POST
    suspend fun loginUser(
        @Url url: String, @Body login: LoginRequest
    ): BaseApiResponse<RegistrationResponse>


    @POST
    suspend fun logoutUser(@Url url: String): BaseApiResponse<String?>

    @POST
    suspend fun deleteUser(@Url url: String): BaseApiResponse<String?>


    @POST
    suspend fun sendOtp(
        @Url url: String, @Body jsonObject: JsonObject
    ): BaseApiResponse<SendOtpResponse>

    @POST
    suspend fun verifyOtp(
        @Url url: String, @Body jsonObject: JsonObject
    ): BaseApiResponse<UserResponse>

    @POST("/users/v3/international/create")
    suspend fun createInternationalUser(@Body jsonObject: JsonObject): BaseApiResponse<RegistrationResponse>

    @Multipart
    @POST
    suspend fun periodicFeedbackFile(
        @Url url: String,
        @Part appLogs: MultipartBody.Part?,
        @Part ringLogs: MultipartBody.Part?,
        @Part firmwareLogs: MultipartBody.Part?,
    ): BaseApiResponseData<Any>

    @Multipart
    @POST
    suspend fun submitFeedbackFile(
        @Url url: String,
        @Part("platform") platform: RequestBody,
        @Part("mobile_device") mobileDevice: RequestBody,
        @Part("os_version") osVersion: RequestBody,
        @Part("app_version") appVersion: RequestBody,
        @Part("watch_name") watchName: RequestBody,
        @Part("watch_firmware_version") watchFirmwareVersion: RequestBody,
        @Part("rating") rating: RequestBody,
        @Part("problem_type") problem_type: RequestBody,
        @Part("suggestion") suggestion: RequestBody,
        @Part("date") date: RequestBody,
        @Part("user_id") userId: RequestBody,
        @Part logs: List<MultipartBody.Part>?
    ): BaseApiResponseData<String>

    @POST
    suspend fun submitFeedbackNew(
        @Url url: String, @Body jsonObject: JsonObject
    ): BaseApiResponseData<String>

    //User APIs
    @POST
    suspend fun setUserDevice(
        @Url url: String, @Body jsonObject: JsonObject
    ): BaseApiResponse<UpdateDeviceResponse>

    @POST
    suspend fun updatePushToken(
        @Url url: String, @Body jsonObject: JsonObject
    ): BaseApiResponse<MessageResponse>

    @POST
    suspend fun updateUserProfile(
        @Url url: String, @Body jsonObject: JsonObject
    ): BaseApiResponse<User>

    @GET
    suspend fun getUserProfile(@Url url: String): UserResponse

    @Multipart
    @POST
    suspend fun uploadUserImage(
        @Url url: String, @Part image: MultipartBody.Part?
    ): BaseApiResponseImage

    //Device APIs
    @GET
    suspend fun getDeviceList(
        @Url url: String, @Query("wearable_type") device: String
    ): BaseApiResponse<DeviceListResponse>

    @GET
    suspend fun getDeviceFeatures(
        @Url url: String, @Query("device_id") deviceId: Int, @Query("platform") platform: String
    ): BaseApiResponse<DeviceFeatureResponse>

    @POST
    suspend fun checkForUpdates(
        @Url url: String, @Body requestObject: JsonObject
    ): BaseApiResponseData<UpdateResponse>

    //HistoryData APIs ring
    @POST
    suspend fun postOreoCombinedHistoryData(
        @Url url: String, @Body requestObject: OreoUserDataPost,
        @Header("api-version") version: String,
    ): BaseApiResponse<VersionCheckResponse>

    @POST
    suspend fun postOreoSleepHistoryData(
        @Url url: String, @Body requestObject: OreoUserDataPost
    ): BaseApiResponse<VersionCheckResponse>


    @GET
    suspend fun getStateList(
        @Url string: String,
    ): BaseApiResponse<List<StateData>>

    @GET
    suspend fun getRingCareData(
        @Url string: String,
    ): BaseApiResponse<RingCareResponse>

    @GET
    suspend fun getRingWelcomeData(
        @Url string: String,
    ): BaseApiResponse<RingWelcome>

    @GET
    suspend fun getUserHealthData(
        @Url string: String,
        @Query("start_date") startDate: String?,
        @Query("end_date") endDate: String?
    ): BaseApiResponse<ServerUserHealthResponse>


    @GET
    suspend fun getUserHealthSleepData(
        @Url string: String,
        @Query("start_date") startDate: String?,
        @Query("end_date") endDate: String?,
        @Header("api-version") version: String,
    ): BaseApiResponse<SleepDataResponse>

    @GET
    suspend fun getCityList(
        @Url string: String,
    ): BaseApiResponse<List<CityData>>

    @GET
    suspend fun getRingLastLocation(
        @Url string: String,
    ): BaseApiResponse<RingLocationData>

    @POST
    suspend fun setRingLastLocation(
        @Url string: String,
        @Body jsonObject: JsonObject
    ): BaseApiResponse<Any>


    @POST
    suspend fun saveUserLocation(
        @Url string: String, @Body jsonObject: JsonObject
    ): BaseApiResponse<UserLocationUpdatedResponse>


    //Challenge APIs

    @POST
    suspend fun checkWatchTokenExist(
        @Url url: String, @Body jsonObject: JsonObject
    ): BaseApiResponse<WatchTokenResponse>

    @POST
    suspend fun removeWatchTokenFromServer(
        @Url url: String, @Body jsonObject: JsonObject
    ): BaseApiResponseData<Any>

    @GET
    suspend fun getInterests(
        @Url url: String
    ): BaseApiResponse<List<Interest>>

    @POST
    suspend fun updateInterests(
        @Url url: String, @Body requestObject: JsonObject
    ): BaseApiResponse<List<Interest>>


    /**
     * ---------------------------------------------------------------------------------
     *                                Oreo Services Start
     * ---------------------------------------------------------------------------------
     */


    @GET
    suspend fun deleteWorkout(
        @Url url: String
    ): BaseApiResponse<Any>


    @GET
    suspend fun getWorkoutList(
        @Url url: String
    ): BaseApiResponse<List<OWorkoutListModal>>

    @GET
    suspend fun getRecentWorkoutList(
        @Url url: String, @Query("today") today: Boolean
    ): BaseApiResponse<List<OActivityListModal>>

    @GET
    suspend fun getAllActivityList(
        @Url url: String
    ): BaseApiResponse<List<OActivityListModal>>


    @GET
    suspend fun getInternalPagesData(
        @Url url: String,
        @Query("date") selectDate: String,
        @Query("filter_type") filterType: String,
        @Query("contri_type") contriType: String,
    ): BaseApiResponse<OInternalPageResponseModal>

    @GET
    suspend fun getActivityInternalPagesData(
        @Url url: String,
        @Query("date") selectDate: String,
        @Query("filter_type") filterType: String,
        @Query("contri_type") contriType: String,
    ): BaseApiResponse<OInternalPageResponseModal>

    @GET
    suspend fun getLearnData(
        @Url url: String
    ): BaseApiResponse<List<LearnModel>>

    @POST
    suspend fun addRecordedWorkout(
        @Url url: String, @Body requestObject: JsonObject
    ): BaseApiResponse<List<AddWorkoutResponse>>

    @GET
    suspend fun getReadinessInternalPagesData(
        @Url url: String,
        @Query("date") selectDate: String,
        @Query("filter_type") filterType: String,
        @Query("contri_type") contriType: String,
    ): BaseApiResponse<OInternalPageResponseModal>


    @POST
    suspend fun addWorkout(
        @Url url: String, @Body jsonObject: JsonObject
    ): BaseApiResponseData<OActivityListModal>

    @POST
    suspend fun syncGoogleFitUserData(
        @Url url: String, @Body jsonObject: JsonObject
    ): BaseApiResponseData<Any>

    @POST
    suspend fun addGFitWorkout(
        @Url url: String, @Body jsonObject: JsonObject
    ): BaseApiResponseData<Any>

    @GET
    suspend fun getContributorsDetails(
        @Url url: String, @Query("type") contributorType: String
    ): BaseApiResponse<OContributorResponseModal>

    @POST
    suspend fun savePairingErrorLogs(
        @Url url: String, @Body requestObject: JsonObject
    ): BaseApiResponse<Any>

    @GET
    suspend fun getWorkoutDetails(
        @Url url: String
    ): BaseApiResponse<OWorkoutDetailsResponseModel>

    @GET
    suspend fun getHSCategories(
        @Url url: String
    ): BaseApiResponse<List<OHSModel>>

    @GET
    suspend fun getHSQAnswer(
        @Url url: String
    ): BaseApiResponse<List<OHSQuestionariesResponseModel>>

    @GET
    suspend fun getUserNapDetailsData(
        @Url url: String
    ): BaseApiResponse<OreoNapDetailsDataModel>

    @POST
    suspend fun addNapServer(
        @Url url: String, @Body napRequest: OreoNapNetworkEntity
    ): BaseApiResponse<List<OreoNapDetailsDataModel>>


    @GET
    suspend fun getStressInternalPageData(
        @Url url: String,
        @Query("date") selectDate: String,
        @Query("type") dayType: String,
        @Query("filter_type") filterType: String
    ): BaseApiResponse<List<StressResultData>>

    @GET
    suspend fun getSleepTrendsInternalPageData(
        @Url url: String,
        @Query("startDate") selectDate: String,
        @Query("endDate") dayType: String,
        @Query("data_type") dataType: String
    ): BaseApiResponse<OSleepInternalTrendsDataModel>

    @GET
    suspend fun getDailyTrendsData(
        @Url url: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): BaseApiResponse<OSleepInternalTrendsDataModel>


    /**
     * Chat GPT APIs
     */
    @POST
    suspend fun askQuestionToChatGpt(
        @Url url: String,
        @Body requestObject: JsonObject
    ): BaseApiResponse<ChatGptResponse>

    @POST
    suspend fun pollForAnswer(
        @Url url: String,
        @Body requestObject: JsonObject
    ): BaseApiResponse<ChatGptResponse>

    @GET
    suspend fun getChatHistory(
        @Url url: String
    ): BaseApiResponse<List<ChatHistoryItem>?>

    @GET
    suspend fun getChatHistoryByDate(
        @Url url: String,
        @Query("date") date: String?
    ): BaseApiResponse<List<ChatHistoryItem>?>

    @DELETE
    suspend fun deleteChatHistory(
        @Url url: String,
        @Query("thread_id") threadId: String
    ): BaseApiResponse<Any?>

    @GET
    suspend fun generateThreadId(
        @Url url: String
    ): BaseApiResponse<ThreadIdResponse?>

    @GET
    suspend fun loadMessagesByThreadId(
        @Url url: String
    ): BaseApiResponse<ChatMessagesResponse>

    @POST
    suspend fun addSleep(
        @Url url: String, @Body jsonObject: JsonObject
    ): BaseApiResponseData<Any>

    @GET
    suspend fun getCalendarData(
        @Url url: String,
        @Query("start_date") startDate: String?,
        @Query("end_date") endDate: String?,
    ): BaseApiResponse<List<HealthCalendar>>

    /**
     * ===================================
     */


    /*
    * Female health service start
    * */
    @POST
    suspend fun submitFemaleHealthInfo(
        @Url url: String, @Body requestObject: JsonObject
    ): BaseApiResponse<Any>

    @POST
    suspend fun saveLogSymptom(
        @Url url: String, @Body requestObject: JsonObject
    ): BaseApiResponse<Any>

    @GET
    suspend fun getFemaleHealthInfo(
        @Url url: String, @Query("date") selectDate: String
    ): BaseApiResponse<FemaleHealthUserInfoModel?>

    @POST
    suspend fun logPeriod(
        @Url url: String, @Body jsonObject: JsonObject
    ): BaseApiResponse<Any>

    @GET
    suspend fun getPeriodLengthList(
        @Url url: String, @Query("date") selectDate: String
    ): BaseApiResponse<PeriodLengthListResponse>

    @GET
    suspend fun getPeriodDurationList(
        @Url url: String, @Query("date") selectDate: String
    ): BaseApiResponse<PeriodLengthListResponse>

    @GET
    suspend fun getPeriodCycleHistory(
        @Url url: String,
    ): BaseApiResponse<PeriodCycleHistory>

    //todo response model will update, once receive actual api from backend
    @GET
    suspend fun getCycleStreakInfo(
        @Url url: String,
    ): BaseApiResponse<List<FMHCycleHistoryDataModel>>

    @GET
    suspend fun getFemaleHealthIcons(
        @Url url: String
    ): BaseApiResponse<FemaleHealthIconsModel>

    @GET
    suspend fun getFemaleHealthTempData(
        @Url url: String, @Query("date") date: String
    ): BaseApiResponse<FemaleTempResponse>

    @POST
    suspend fun setPeriodConfirm(
        @Url url: String, @Body jsonObject: JsonObject
    ): BaseApiResponse<Any>

    @GET
    suspend fun getCycleTrackerInfo(@Url url: String): BaseApiResponse<FemaleCycleTrackInfoModel?>?

    @PUT
    suspend fun updateCycleTrackerInfo(
        @Url url: String, @Body jsonObject: JsonObject
    ): BaseApiResponse<Any>

    @POST
    suspend fun updateCycleTrackerToggle(
        @Url url: String, @Body jsonObject: JsonObject
    ): BaseApiResponse<Any>


    /**
     * ---------------------------------------------------------------------------------
     *                                Oreo Services End
     * ---------------------------------------------------------------------------------
     */
}