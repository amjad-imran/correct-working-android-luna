package com.noisefit.data.remote.abstraction

import com.google.gson.JsonObject
import com.noisefit.data.model.*
import com.noisefit.data.model.timeline.FriendTimeline
import com.noisefit.data.remote.CityData
import com.noisefit.data.remote.StateData
import com.noisefit.data.remote.UserLocationUpdatedResponse
import com.noisefit.data.remote.request.LoginRequest
import com.noisefit.data.remote.request.RegistrationRequest
import com.noisefit.data.remote.request.UpdateAdditionalDetailRequest
import com.noisefit.data.remote.response.CatWiseWatchFacesItem
import com.noisefit.data.remote.response.WatchFaceCustomListResponse
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


    //Auth APIs

    @POST("/users/auth/sign_up")
    suspend fun signup(@Body signup: RegistrationRequest): BaseApiResponse<RegistrationResponse>

    @POST
    suspend fun loginUser(
        @Url url: String,
        @Body login: LoginRequest
    ): BaseApiResponse<RegistrationResponse>

    @POST("/users/v3/login")
    suspend fun loginUserOld(@Body login: LoginRequest): BaseApiResponse<RegistrationResponse>

    @POST("/users/v3/auth/logout")
    suspend fun logoutUser(): BaseApiResponse<String?>

    @POST("/users/v3/disable")
    suspend fun deleteUser(): BaseApiResponse<String?>


    @POST("/users/auth/update_mobile")
    suspend fun updateMobile(@Body jsonObject: JsonObject): BaseApiResponse<MessageResponse>

    @POST("/users/auth/forgot_password")
    suspend fun generateOtp(@Body jsonObject: JsonObject): BaseApiResponse<MessageResponse>

    @POST("/users/auth/update_mobile")
    suspend fun changeMobileNumber(@Body jsonObject: JsonObject): BaseApiResponse<MessageResponse>

    @POST("/users/auth/update_email")
    suspend fun changeEmail(@Body jsonObject: JsonObject): BaseApiResponse<MessageResponse>


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


    @POST("/users/auth/reset_password")
    suspend fun resetPassword(@Body jsonObject: JsonObject): BaseApiResponse<MessageResponse>


    @POST("users/update/password")
    suspend fun updatePassword(@Body jsonObject: JsonObject): BaseApiResponse<MessageResponse>


    @Multipart
    @POST("/users/v3/feedback/create")
    suspend fun submitFeedback(
        @Header("device-id") deviceId: Int?,
        @Part("platform") platform: RequestBody,
        @Part("mobile_device") mobileDevice: RequestBody,
        @Part("os_version") osVersion: RequestBody,//yyyy-mm-dd,
        @Part("app_version") appVersion: RequestBody,//yyyy-mm-dd,
        @Part("watch_name") watchName: RequestBody,
        @Part("watch_firmware_version") watchFirmwareVersion: RequestBody,
        @Part("problem_type") pairingType: RequestBody,
        @Part("problem_desc") problemDesc: RequestBody,
        @Part("date") date: RequestBody,
        @Part("user_id") userId: RequestBody,
        @Part image0: List<MultipartBody.Part>?,
        @Part logs: List<MultipartBody.Part>?
    ): BaseApiResponse<MessageResponse>

    @Multipart
    @POST
    suspend fun periodicFeedbackFile(
        @Url url: String,
        @Part appLogs: MultipartBody.Part?,
        @Part ringLogs: MultipartBody.Part?,
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

    @POST("/users/v3/update/push-token")
    suspend fun updatePushToken(@Body jsonObject: JsonObject): BaseApiResponse<MessageResponse>

    @POST("/users/v3/profile/update")
    suspend fun updateUserProfile(@Body jsonObject: JsonObject): BaseApiResponse<User>

    @POST("/users/v3/profile/update")
    suspend fun updateUserProfile(@Body user: User): BaseApiResponse<User>

    @GET("/users/additional_details")
    suspend fun getUserProfileInfo(): BaseApiResponse<UserInfoResponse>

    @GET
    suspend fun getUserProfile(@Url url: String): UserResponse

    @POST("/users/additional_details")
    suspend fun saveAdditionalUserDetails(@Body requestObject: UpdateAdditionalDetailRequest): BaseApiResponse<MessageResponse>

    @Multipart
    @POST("/users/v2/image")
    suspend fun uploadUserImage(
        @Part image: MultipartBody.Part?
    ): BaseApiResponseImage


    @Multipart
    @POST("/users/upload_crash_report")
    suspend fun uploadCrashLogs(
        @Part("platform") platform: String,
        @Part("version") appVersion: String,
        @Part("date") date: String,//yyyy-mm-dd
        @Part file: MultipartBody.Part?
    ): BaseApiResponse<MessageResponse>

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

    @GET
    suspend fun getWatchFaceCategory(
        @Url url: String
    ): BaseApiResponse<List<CatWiseWatchFacesItem>>

    @GET
    suspend fun getWatchFacesByCategoryId(
        @Url url: String
    ): BaseApiResponse<List<WatchFace>>

    @GET
    suspend fun getTopDownloadedWatchFaces(
        @Url url: String
    ): BaseApiResponseData<List<WatchFace>>

    @GET
    suspend fun getNewlyAddedWatchFaces(
        @Url url: String
    ): BaseApiResponseData<List<WatchFace>>

    @GET
    suspend fun getWatchFaceById(
        @Url url: String,
    ): BaseApiResponse<WatchFaceResponse>

    @GET
    suspend fun getWatchFaceCustomData(
        @Url url: String
    ): BaseApiResponse<WatchFaceCustomListResponse>


    @POST("/watch_faces/v3/current")
    suspend fun setRecentWatchFace(
        @Body requestObject: JsonObject
    ): BaseApiResponseData<String>

    @POST
    suspend fun setWatchFaceDownload(
        @Url url: String, @Body requestObject: JsonObject
    ): BaseApiResponseData<String>

    @GET
    suspend fun getRecentWatchFace(
        @Url url: String
    ): BaseApiResponse<List<Watchface2>?>


    @GET("/cloud_watch_faces/v3/dial_plate/info")
    suspend fun getWatchFaceDownloadInfo(
        @Query("id") watchFaceId: Int
    ): BaseApiResponseData<WatchFaceDownloadResponse>

    @POST
    suspend fun markAsFavourite(
        @Url url: String, @Body requestObject: JsonObject
    ): BaseApiResponseData<Any>


    @GET
    suspend fun getFavouriteWatchFaces(
        @Url url: String
    ): BaseApiResponseData<List<WatchFace>>

    @POST
    suspend fun checkForUpdates(
        @Url url: String,
        @Body requestObject: JsonObject
    ): BaseApiResponseData<UpdateResponse>

    @POST("/firmware-versions/v3/recent")
    suspend fun checkForUpdatesRecent(
        @Body requestObject: JsonObject
    ): BaseApiResponseData<UpdateResponse>

    @GET("/devices/v3/file/link")
    suspend fun getAgpsFileUrl(
    ): BaseApiResponse<AgpsFileResponse>

    @GET
    suspend fun getVendorAgpsFileUrl(
        @Url url: String
    ): BaseApiResponse<AgpsFileResponse>


    @GET
    suspend fun getCloudWatchFaces(
        @Url url: String
    ): BaseApiResponse<List<WatchFace>>


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
    suspend fun getStepsHistory(
        @Url url: String,
        @Query("history_type") historyType: String,
        @Query("start_date") selectedStartDate: String?,
        @Query("end_date") selectedEndDate: String?
    ): BaseApiResponse<StepsHistoryResponse>

    @GET
    suspend fun getBodyTempHistory(
        @Url url: String,
        @Query("history_type") historyType: String,
        @Query("start_date") selectedStartDate: String?,
        @Query("end_date") selectedEndDate: String?
    ): BaseApiResponse<BodyTempHistoryResponse>


    @GET("/sleep_activities/v3/history")
    suspend fun getSleepHistory(
        @Query("history_type") historyType: String,
        @Query("start_date") selectedStartDate: String?,
        @Query("end_date") selectedEndDate: String?
    ): BaseApiResponse<SleepHistoryResponse>


    @GET("/blood_oxygen/v3/history")
    suspend fun getBloodOxygenHistory(
        @Query("history_type") historyType: String,
        @Query("start_date") selectedStartDate: String?,
        @Query("end_date") selectedEndDate: String?
    ): BaseApiResponse<BoHistoryResponse>

    @GET("/stress/v3/history")
    suspend fun getStressHistory(
        @Query("history_type") historyType: String,
        @Query("start_date") selectedStartDate: String?,
        @Query("end_date") selectedEndDate: String?
    ): BaseApiResponse<StressHistoryResponse>

    @GET("/sleep_activities/v3/history/highlight")
    suspend fun getSleepHighlights(): BaseApiResponse<SleepHighlightResponse>


    @GET("heart_rates/v3/history")
    suspend fun getHrHistory(
        @Query("history_type") historyType: String,
        @Query("start_date") selectedStartDate: String?,
        @Query("end_date") selectedEndDate: String?
    ): BaseApiResponse<HrHistoryResponse>


    @GET("/sleep_categories/v3/list")
    suspend fun getSleepBlogs(): BaseApiResponse<List<SleepBlogCategories>>

    @GET("/highlights")
    suspend fun getHighlights(): BaseApiResponseData<GraphHighlightResponse>


    @GET
    suspend fun getStepsHighlights(
        @Url url: String
    ): BaseApiResponseData<GraphHighlightResponse>

    //App Version Check API

    //Shop APIs
    @POST("/shopify/v3/token")
    suspend fun getOrderToken(@Body requestObject: JsonObject): BaseApiResponse<String>

    //Sports Mode APIs
    @GET("/activities/list")
    suspend fun getActivities(
        @Query("start_date") startDate: String, @Query("end_date") endDate: String
    ): BaseApiResponse<ActivityListResponse>

    @GET("/activities/v3/collection")
    suspend fun getActivitiesPaging(
        @Query("page") page: Int, @Query("limit") pageLimit: Int
    ): BaseApiResponse<List<SportsModeResponse>>

    @GET("/activities/v3/{itemId}/detail")
    suspend fun getActivitiesDetails(
        @Path("itemId") itemId: Int
    ): BaseApiResponse<SportsModeResponse>


    @GET
    suspend fun getWorkoutShareImages(
        @Url url: String,
    ): BaseApiResponse<List<String>>


    @GET("/activities/v3/recent")
    suspend fun getRecentActivitiesDates(): BaseApiResponse<JsonObject>

    @GET("/activities/v3/list")
    suspend fun getRecentActivities(): BaseApiResponse<RecentActivities>

    @POST("/activities/v3/create")
    suspend fun postActivities(
        @Body requestObject: SportsModeRequestList
    ): BaseApiResponseActivity

    //Noise Health APIs

    @GET("/content")
    suspend fun getNoiseHealthContent(): NoiseHealthResponse

    //{"sub_category_id":51}
    @PUT("/content/view")
    suspend fun setNoiseHealthContentView(@Body requestObject: JsonObject): Unit

    //{"sub_category_id":51,"playtime_in_seconds":503.526}
    @PUT("content/playtime")
    suspend fun setNoiseHealthContentPlayTime(@Body requestObject: JsonObject): Unit

    @GET("content/recent")
    suspend fun getRecentPlayedContent(): BaseApiResponse<NoiseHealthCategory?>

    //Shop APIs

    @GET("/mobile/list/product/popular")
    suspend fun getPopularProduct(): BaseApiResponse<MessageResponse>

    @GET("/mobile/search/{searchText}")
    suspend fun searchProduct(
        @Path("searchText") searchText: String
    ): BaseApiResponse<MessageResponse>

    //Trophies
    @GET("/trophies/v3/goals")
    suspend fun getTrophiesGoal(
        @Query("date") date: String, @Query("unit_system") unitSystem: String
    ): BaseApiResponseData<Trophies>

    @GET("/trophies/v3/recent")
    suspend fun getRecentTrophies(
    ): BaseApiResponse<List<TrophyBadge>>

    @POST("/buddies/profile")
    suspend fun getBuddiesTrophiesData(
        @Body jsonObject: JsonObject
    ): BaseApiResponseData<Trophies>

    //{"id":4825}
    @POST("/user_badges/v3/set_collect")
    suspend fun collectBadge(
        @Body jsonObject: JsonObject
    ): BaseApiResponse<String>

    @POST("/activity_challenge/v3/collect_trophy")
    suspend fun collectChallengeTrophy(
        @Body jsonObject: JsonObject
    ): BaseApiResponseChallenge<Any>


    //friend
    @GET
    suspend fun getFriendList(
        @Url string: String,
        @Query("startDate") selectedStartDate: String?,
        @Query("endDate") selectedEndDate: String?
    ): BaseApiResponse<FriendsData>

    @GET
    suspend fun getCompetitionList(
        @Url string: String,
    ): BaseApiResponse<CompetitionListResponse>


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
    suspend fun getCityList(
        @Url string: String,
    ): BaseApiResponse<List<CityData>>


    @POST
    suspend fun saveUserLocation(
        @Url string: String, @Body jsonObject: JsonObject
    ): BaseApiResponse<UserLocationUpdatedResponse>


    //Challenge APIs

    //Warranty Register APIs
    @GET("/warranty/check/{number}")
    suspend fun checkWarranty(
        @Path("number") number: String
    ): BaseApiResponse<WarrantyResponse>

    @POST
    suspend fun checkWatchTokenExist(
        @Url url: String,
        @Body jsonObject: JsonObject
    ): BaseApiResponse<WatchTokenResponse>

    //  @GET("/warranty/check/{number}")
    //    suspend fun checkWarranty(
    //        @Path("number") number: String
    @POST
    suspend fun removeWatchTokenFromServer(
        @Url url: String,
        @Body jsonObject: JsonObject
    ): BaseApiResponseData<Any>

    @POST
    suspend fun getMarketPlaces(
        @Url url: String
    ): BaseApiResponseData<List<MarketPlace>>

    @GET("/warranty/v3")
    suspend fun getMarketPlacesOld(): BaseApiResponseData<List<String>>

    @POST("/warranty/v3/add")
    suspend fun addWarrantyOld(
        @Body jsonObject: JsonObject
    ): BaseApiResponse<String?>


    @POST
    suspend fun getWarrantyWatchList(
        @Url url: String, @Header("api-key") apiKey: String
    ): BaseApiResponseData<List<WarrantyWatchesResponse>>

    @POST
    suspend fun addWarranty(
        @Url url: String, @Header("api-key") apiKey: String, @Body jsonObject: JsonObject
    ): BaseApiResponse<String?>

    @POST
    suspend fun checkWarranty(
        @Url url: String, @Header("api-key") apiKey: String, @Body jsonObject: JsonObject
    ): BaseApiResponse<Boolean>


    //Explore Collection APIs

    //Sleep Blog APIs

    // Dashboard content APIs

    //World Clock APIs
    @GET("/users/v3/world_clock/0")
    suspend fun getTimeZonesCities(): BaseApiResponseData<WorldClockResponse>

    @GET("/activity_challenge/v3/list")
    suspend fun getChallengeList(): BaseApiResponseChallenge<ChallengeListResponse>

    @GET("/activity_challenge/v3/{challengeId}")
    suspend fun getChallengeDetail(@Path("challengeId") challengeId: String): BaseApiResponseChallenge<ChallengeDetailResponse>

    @GET
    suspend fun getChallengeDetailByID(
        @Url url: String
    ): BaseApiResponse<ChallengeModel>

    @GET
    suspend fun joinChallengeById(
        @Url url: String
    ): BaseApiResponse<String>

    @POST
    suspend fun leaveChallengeById(
        @Url url: String, @Body requestObject: JsonObject
    ): BaseApiResponse<String>

    @Deprecated("use joinChallengeById()")
    @POST("/activity_challenge/v3/join")
    suspend fun joinChallenge(
        @Body requestObject: JoinChallengeRequest
    ): BaseApiResponse<String>

    @Deprecated("use leaveChallengeById()")
    @POST("/activity_challenge/v3/leave")
    suspend fun leaveChallenge(
        @Body requestObject: LeaveChallengeRequest
    ): BaseApiResponse<String>

    @GET
    suspend fun getChallengeLeaderboard(@Url url: String): BaseApiResponse<ArrayList<Leadership>>


    @GET
    suspend fun getChallengeBuddyLeaderboard(@Url url: String): BaseApiResponseChallenge<ArrayList<Leadership>>


    @GET
    suspend fun config(@Url url: String): BaseApiResponse<ConfigResponse>

    @GET
    suspend fun getDashboardBanner(
        @Url url: String
    ): BaseApiResponse<DashboardBannerData>


    @GET
    suspend fun getCurrentChallenges(
        @Url url: String
    ): BaseApiResponse<ChallengeListingResponse>

    @GET
    suspend fun getCompletedChallenges(
        @Url url: String
    ): BaseApiResponse<ChallengeListingResponse>

    @GET
    suspend fun getHelpAndSupportList(
        @Url url: String
    ): BaseApiResponse<List<HelpAndSupportResponse>>

    @GET
    suspend fun getHelpAndSupportByQuestionId(
        @Url url: String
    ): BaseApiResponse<HelpAndSupportDetailResponse>


    @GET
    suspend fun getInterests(
        @Url url: String
    ): BaseApiResponse<List<Interest>>

    @POST
    suspend fun updateInterests(
        @Url url: String, @Body requestObject: JsonObject
    ): BaseApiResponse<List<Interest>>

    @GET
    suspend fun getDashboardRewardsData(
        @Url url: String
    ): BaseApiResponse<DashboardRewardsResponse>

    @POST
    suspend fun earnRewardsPoints(
        @Url url: String, @Body requestObject: JsonObject
    ): BaseApiResponse<Any>

    @GET
    suspend fun getRewardsProfile(
        @Url url: String
    ): BaseApiResponse<Any>

    @POST
    suspend fun collectRewardsPoints(
        @Url url: String, @Body requestObject: JsonObject
    ): BaseApiResponse<CollectCoinResponse>

    @GET
    suspend fun availCoupon(
        @Url url: String
    ): BaseApiResponse<com.noisefit_commans.data.model.AvailCouponData>

    @GET
    suspend fun getVoucherList(
        @Url url: String
    ): BaseApiResponse<VoucherListData>


    @GET
    suspend fun getTimeLine(
        @Url url: String
    ): BaseApiResponse<FriendTimeline>


    @GET
    suspend fun getDashboardFeed(
        @Url url: String
    ): BaseApiResponse<FeedResponse>


    @GET
    suspend fun getRecentChallenges(
        @Url url: String
    ): BaseApiResponse<List<ChallengeModel>>

    @GET("/activities/v3/list/feeds")
    suspend fun geRecentActivities(
    ): BaseApiResponse<RecentActivities>


    /**
     * ---------------------------------------------------------------------------------
     *                                Oreo Services Start
     * ---------------------------------------------------------------------------------
     */

    @GET
    suspend fun getSleepHistory(
        @Url url: String, @Query("date") selectDate: String,
    ): BaseApiResponse<List<OreoSleepModel>>

    @GET
    suspend fun deleteWorkout(
        @Url url: String
    ): BaseApiResponse<Any>


    @GET
    suspend fun getActivityHistory(
        @Url url: String, @Query("date") selectDate: String,
    ): BaseApiResponse<List<OreoActivityModel>>

    @GET
    suspend fun getReadinessHistory(
        @Url url: String, @Query("date") selectDate: String,
    ): BaseApiResponse<List<OreoReadinessModel>>

    @GET
    suspend fun getDashboardData(
        @Url url: String
    ): BaseApiResponse<OreoDashboardResponseModel>


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
    /**
     * ---------------------------------------------------------------------------------
     *                                Oreo Services End
     * ---------------------------------------------------------------------------------
     */
}