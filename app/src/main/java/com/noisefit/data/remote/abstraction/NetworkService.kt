package com.noisefit.data.remote.abstraction

import com.google.gson.JsonObject
import com.noisefit.data.model.*
import com.noisefit.data.model.timeline.FriendTimeline
import com.noisefit.data.remote.CityData
import com.noisefit.data.remote.StateData
import com.noisefit.data.remote.UserLocationUpdatedResponse
import com.noisefit.data.remote.request.LoginRequest
import com.noisefit.data.remote.request.UpdateAdditionalDetailRequest
import com.noisefit.data.remote.response.Watchface2
import com.noisefit_commans.data.model.*
import com.noisefit_commans.data.model.history.*
import com.noisefit_commans.data.model.trophies.Trophies
import com.noisefit_commans.data.model.trophies.TrophyBadge
import com.noisefit_commans.data.model.warranty.MarketPlace
import com.noisefit_commans.data.response.*
import com.noisefit_commans.models.SportsModeRequestList
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.response.SleepHistoryResponse
import com.oreo.data.model.*
import com.oreo.data.model.health.OreoActivityModel
import com.oreo.data.model.health.OreoDashboardResponseModel
import com.oreo.data.model.health.OreoReadinessModel
import com.oreo.data.model.health.OreoSleepModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*


interface NetworkService {

    //App APIs
    @POST
    suspend fun checkAppVersion(
        @Url url: String,
        @Body jsonObject: JsonObject
    ): BaseApiResponse<VersionCheckResponse>

    @POST
    suspend fun checkAppVersionV2(
        @Url url: String,
        @Body jsonObject: JsonObject
    ): BaseApiResponse<UpdateResponseV2>


    //Auth APIs
    @POST
    suspend fun loginUser(
        @Url url: String,
        @Body login: LoginRequest
    ): BaseApiResponse<RegistrationResponse>


    @POST
    suspend fun logoutUser(@Url url: String): BaseApiResponse<String?>

    @POST
    suspend fun deleteUser(@Url url: String): BaseApiResponse<String?>


    @POST
    suspend fun sendOtp(
        @Url url: String,
        @Body jsonObject: JsonObject
    ): BaseApiResponse<SendOtpResponse>

    @POST
    suspend fun verifyOtp(
        @Url url: String,
        @Body jsonObject: JsonObject
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
        @Url url: String,
        @Body jsonObject: JsonObject
    ): BaseApiResponse<UpdateDeviceResponse>

    @POST
    suspend fun updatePushToken(
        @Url url: String,
        @Body jsonObject: JsonObject
    ): BaseApiResponse<MessageResponse>

    @POST
    suspend fun updateUserProfile(
        @Url url: String,
        @Body jsonObject: JsonObject
    ): BaseApiResponse<User>

    @GET
    suspend fun getUserProfile(@Url url: String): UserResponse

    @Multipart
    @POST
    suspend fun uploadUserImage(
        @Url url: String,
        @Part image: MultipartBody.Part?
    ): BaseApiResponseImage

    //Device APIs
    @GET
    suspend fun getDeviceList(
        @Url url: String,
        @Query("wearable_type") device: String
    ): BaseApiResponse<DeviceListResponse>

    @GET
    suspend fun getDeviceFeatures(
        @Url url: String,
        @Query("device_id") deviceId: Int,
        @Query("platform") platform: String
    ): BaseApiResponse<DeviceFeatureResponse>

    @POST
    suspend fun checkForUpdates(
        @Url url: String,
        @Body requestObject: JsonObject
    ): BaseApiResponseData<UpdateResponse>

    //HistoryData APIs ring
    @POST
    suspend fun postOreoCombinedHistoryData(
        @Url url: String,
        @Body requestObject: OreoUserDataPost
    ): BaseApiResponse<VersionCheckResponse>

    @POST
    suspend fun postOreoSleepHistoryData(
        @Url url: String,
        @Body requestObject: OreoUserDataPost
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
    suspend fun getCityList(
        @Url string: String,
    ): BaseApiResponse<List<CityData>>


    @POST
    suspend fun saveUserLocation(
        @Url string: String, @Body jsonObject: JsonObject
    ): BaseApiResponse<UserLocationUpdatedResponse>


    //Challenge APIs

    @POST
    suspend fun checkWatchTokenExist(
        @Url url: String,
        @Body jsonObject: JsonObject
    ): BaseApiResponse<WatchTokenResponse>

    @POST
    suspend fun removeWatchTokenFromServer(
        @Url url: String,
        @Body jsonObject: JsonObject
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
        @Url url: String,
        @Query("today") today: Boolean
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
        @Url url: String,
        @Body requestObject: JsonObject
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
        @Url url: String,
        @Query("type") contributorType: String
    ): BaseApiResponse<OContributorResponseModal>

    @POST
    suspend fun savePairingErrorLogs(
        @Url url: String,
        @Body requestObject: JsonObject
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
        @Url url: String,
        @Body napRequest: OreoNapNetworkEntity
    ): BaseApiResponse<List<OreoNapDetailsDataModel>>


    @GET
    suspend fun getStressInternalPageData(
        @Url url: String,
        @Query("date") selectDate: String,
        @Query("type") dayType: String,
        @Query("filter_type") filterType: String
    ): BaseApiResponse<List<StressResultData>>

    /*
    * Female health service start
    * */
    @POST
    suspend fun submitFemaleHealthInfo(
        @Url url: String, @Body requestObject: JsonObject
    ): BaseApiResponse<Any>

    @GET
    suspend fun getFemaleHealthInfo(
        @Url url: String,
        @Query("date") selectDate: String
    ): BaseApiResponse<FemaleHealthUserInfoModel?>

    @POST
    suspend fun logPeriod(
        @Url url: String,
        @Body requestObject: JsonObject
    ): BaseApiResponse<Any>

    /**
     * ---------------------------------------------------------------------------------
     *                                Oreo Services End
     * ---------------------------------------------------------------------------------
     */
}