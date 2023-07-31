package com.noisefit.data.remote

import com.google.gson.JsonObject
import com.noisefit_commans.data.model.*
import com.noisefit_commans.data.model.trophies.Trophies
import com.noisefit_commans.data.model.trophies.TrophyBadge
import com.noisefit_commans.data.model.warranty.MarketPlace
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.request.LoginRequest
import com.noisefit.data.remote.request.RegistrationRequest
import com.noisefit.data.remote.request.UpdateAdditionalDetailRequest
import com.noisefit_commans.data.response.*
import com.noisefit_commans.data.response.history.*
import com.noisefit_commans.data.model.history.*
import com.noisefit_commans.models.SportsModeRequestList
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.models.WatchFace
import okhttp3.MultipartBody
import okhttp3.RequestBody


class FakeNetworkDataSourceImpl
constructor(
    private val challengeData: HashMap<Int, com.noisefit_commans.data.response.ChallengeModel>
) : NetworkService {
    override suspend fun checkAppVersion(jsonObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<VersionCheckResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun signup(signup: RegistrationRequest): com.noisefit_commans.data.response.BaseApiResponse<RegistrationResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun loginUser(login: LoginRequest): com.noisefit_commans.data.response.BaseApiResponse<RegistrationResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun logoutUser(): com.noisefit_commans.data.response.BaseApiResponse<String?> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteUser(): com.noisefit_commans.data.response.BaseApiResponse<String?> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserStats(): com.noisefit_commans.data.response.BaseApiResponse<UserStats?> {
        TODO("Not yet implemented")
    }

    override suspend fun updateMobile(jsonObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<MessageResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun generateOtp(jsonObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<MessageResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun changeMobileNumber(jsonObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<MessageResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun changeEmail(jsonObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<MessageResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun sendOtp(jsonObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<SendOtpResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun verifyOtp(jsonObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<UserResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun createInternationalUser(jsonObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<RegistrationResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun resetPassword(jsonObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<MessageResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun updatePassword(jsonObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<MessageResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun submitFeedback(
        platform: RequestBody,
        mobileDevice: RequestBody,
        osVersion: RequestBody,
        appVersion: RequestBody,
        watchName: RequestBody,
        watchFirmwareVersion: RequestBody,
        pairingType: RequestBody,
        problemDesc: RequestBody,
        date: RequestBody,
        userId: RequestBody,
        image0: List<MultipartBody.Part>?,
        logs: List<MultipartBody.Part>?
    ): com.noisefit_commans.data.response.BaseApiResponse<MessageResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun submitFeedbackNew(
        url: String,
        jsonObject: JsonObject
    ): com.noisefit_commans.data.response.BaseApiResponseData<String> {
        TODO("Not yet implemented")
    }

    override suspend fun setUserDevice(jsonObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<UpdateDeviceResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun updatePushToken(jsonObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<MessageResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun updateUserProfile(jsonObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<User> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserProfileInfo(): com.noisefit_commans.data.response.BaseApiResponse<UserInfoResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserProfile(): UserResponse {
        TODO("Not yet implemented")
    }

    override suspend fun saveAdditionalUserDetails(requestObject: UpdateAdditionalDetailRequest): com.noisefit_commans.data.response.BaseApiResponse<MessageResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun uploadUserImage(image: MultipartBody.Part?): com.noisefit_commans.data.response.BaseApiResponseImage {
        TODO("Not yet implemented")
    }

    override suspend fun uploadCrashLogs(
        platform: String,
        appVersion: String,
        date: String,
        file: MultipartBody.Part?
    ): com.noisefit_commans.data.response.BaseApiResponse<MessageResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getDeviceList(): com.noisefit_commans.data.response.BaseApiResponse<DeviceListResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getDeviceFeatures(
        deviceId: Int,
        platform: String
    ): com.noisefit_commans.data.response.BaseApiResponse<DeviceFeatureResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getWatchFaceCategory(url: String): com.noisefit_commans.data.response.BaseApiResponse<List<CatWiseWatchFacesItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun getWatchFacesByCategoryId(url: String): com.noisefit_commans.data.response.BaseApiResponse<List<WatchFace>> {
        TODO("Not yet implemented")
    }

    override suspend fun getTopDownloadedWatchFaces(url: String): com.noisefit_commans.data.response.BaseApiResponseData<List<WatchFace>> {
        TODO("Not yet implemented")
    }

    override suspend fun getNewlyAddedWatchFaces(url: String): com.noisefit_commans.data.response.BaseApiResponseData<List<WatchFace>> {
        TODO("Not yet implemented")
    }

    override suspend fun getWatchFaceById(url: String): com.noisefit_commans.data.response.BaseApiResponse<WatchFaceResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getWatchFaceCustomData(url: String): com.noisefit_commans.data.response.BaseApiResponse<WatchFaceCustomListResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun setRecentWatchFace(requestObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponseData<String> {
        TODO("Not yet implemented")
    }

    override suspend fun setWatchFaceDownload(
        url: String,
        requestObject: JsonObject
    ): com.noisefit_commans.data.response.BaseApiResponseData<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecentWatchFace(url: String): com.noisefit_commans.data.response.BaseApiResponse<List<WatchFace>?> {
        TODO("Not yet implemented")
    }

    override suspend fun getWatchFaceDownloadInfo(watchFaceId: Int): com.noisefit_commans.data.response.BaseApiResponseData<WatchFaceDownloadResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun markAsFavourite(
        url: String,
        requestObject: JsonObject
    ): com.noisefit_commans.data.response.BaseApiResponseData<Any> {
        TODO("Not yet implemented")
    }

    override suspend fun getFavouriteWatchFaces(url: String): com.noisefit_commans.data.response.BaseApiResponseData<List<WatchFace>> {
        TODO("Not yet implemented")
    }

    override suspend fun checkForUpdates(requestObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponseData<UpdateResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun checkForUpdatesRecent(requestObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponseData<UpdateResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getAgpsFileUrl(): com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.AgpsFileResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getCloudWatchFaces(url: String): com.noisefit_commans.data.response.BaseApiResponse<List<WatchFace>> {
        TODO("Not yet implemented")
    }

    override suspend fun postCombinedHistoryData(requestObject: UserDataPost): com.noisefit_commans.data.response.BaseApiResponse<VersionCheckResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun postSleepHistoryData(requestObject: UserDataPost): com.noisefit_commans.data.response.BaseApiResponse<VersionCheckResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getStepsHistory(
        url: String,
        historyType: String,
        selectedStartDate: String?,
        selectedEndDate: String?
    ): com.noisefit_commans.data.response.BaseApiResponse<StepsHistoryResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getBodyTempHistory(
        url: String,
        historyType: String,
        selectedStartDate: String?,
        selectedEndDate: String?
    ): com.noisefit_commans.data.response.BaseApiResponse<BodyTempHistoryResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getSleepHistory(
        historyType: String,
        selectedStartDate: String?,
        selectedEndDate: String?
    ): com.noisefit_commans.data.response.BaseApiResponse<SleepHistoryResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getBloodOxygenHistory(
        historyType: String,
        selectedStartDate: String?,
        selectedEndDate: String?
    ): com.noisefit_commans.data.response.BaseApiResponse<BoHistoryResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getStressHistory(
        historyType: String,
        selectedStartDate: String?,
        selectedEndDate: String?
    ): com.noisefit_commans.data.response.BaseApiResponse<StressHistoryResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getSleepHighlights(): com.noisefit_commans.data.response.BaseApiResponse<SleepHighlightResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getHrHistory(
        historyType: String,
        selectedStartDate: String?,
        selectedEndDate: String?
    ): com.noisefit_commans.data.response.BaseApiResponse<HrHistoryResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getSleepBlogs(): com.noisefit_commans.data.response.BaseApiResponse<List<SleepBlogCategories>> {
        TODO("Not yet implemented")
    }

    override suspend fun getHighlights(): com.noisefit_commans.data.response.BaseApiResponseData<GraphHighlightResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getStepsHighlights(url: String): com.noisefit_commans.data.response.BaseApiResponseData<GraphHighlightResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getOrderToken(requestObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getActivities(
        startDate: String,
        endDate: String
    ): com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.ActivityListResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getActivitiesPaging(
        page: Int,
        pageLimit: Int
    ): com.noisefit_commans.data.response.BaseApiResponse<List<SportsModeResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun getActivitiesDetails(itemId: Int): com.noisefit_commans.data.response.BaseApiResponse<SportsModeResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecentActivitiesDates(): com.noisefit_commans.data.response.BaseApiResponse<JsonObject> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecentActivities(): com.noisefit_commans.data.response.BaseApiResponse<RecentActivities> {
        TODO("Not yet implemented")
    }

    override suspend fun postActivities(requestObject: SportsModeRequestList): com.noisefit_commans.data.response.BaseApiResponseActivity {
        TODO("Not yet implemented")
    }

    override suspend fun getNoiseHealthContent(): NoiseHealthResponse {
        TODO("Not yet implemented")
    }

    override suspend fun setNoiseHealthContentView(requestObject: JsonObject) {
        TODO("Not yet implemented")
    }

    override suspend fun setNoiseHealthContentPlayTime(requestObject: JsonObject) {
        TODO("Not yet implemented")
    }

    override suspend fun getRecentPlayedContent(): com.noisefit_commans.data.response.BaseApiResponse<NoiseHealthCategory?> {
        TODO("Not yet implemented")
    }

    override suspend fun getPopularProduct(): com.noisefit_commans.data.response.BaseApiResponse<MessageResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun searchProduct(searchText: String): com.noisefit_commans.data.response.BaseApiResponse<MessageResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getTrophiesGoal(
        date: String,
        unitSystem: String
    ): com.noisefit_commans.data.response.BaseApiResponseData<Trophies> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecentTrophies(): com.noisefit_commans.data.response.BaseApiResponse<List<TrophyBadge>> {
        TODO("Not yet implemented")
    }

    override suspend fun getBuddiesTrophiesData(jsonObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponseData<Trophies> {
        TODO("Not yet implemented")
    }

    override suspend fun collectBadge(jsonObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<String> {
        TODO("Not yet implemented")
    }

    override suspend fun collectChallengeTrophy(jsonObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponseChallenge<Any> {
        TODO("Not yet implemented")
    }

    override suspend fun getTopBuddy(): com.noisefit_commans.data.response.BaseApiResponse<List<Buddy>> {
        TODO("Not yet implemented")
    }

    override suspend fun friendList(): com.noisefit_commans.data.response.BaseApiResponse<List<Buddy>> {
        TODO("Not yet implemented")
    }

    override suspend fun buddyAdd(id: String): com.noisefit_commans.data.response.BaseApiResponse<MessageResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun nudgeUser(requestObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<String> {
        TODO("Not yet implemented")
    }

    override suspend fun buddyInvite(id: String): com.noisefit_commans.data.response.BaseApiResponse<MessageResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun buddiesRequestAction(requestObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<String> {
        TODO("Not yet implemented")
    }

    override suspend fun buddyRequests(): com.noisefit_commans.data.response.BaseApiResponse<List<Buddy>> {
        TODO("Not yet implemented")
    }

    override suspend fun getNoiseFitContacts(requestObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<List<com.noisefit_commans.data.model.BuddiesUser>?> {
        TODO("Not yet implemented")
    }

    override suspend fun addNoiseFitContacts(requestObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<Any> {
        TODO("Not yet implemented")
    }

    override suspend fun checkWarranty(number: String): com.noisefit_commans.data.response.BaseApiResponse<WarrantyResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun checkWarranty(
        url: String,
        apiKey: String,
        jsonObject: JsonObject
    ): com.noisefit_commans.data.response.BaseApiResponse<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun getMarketPlaces(url: String): com.noisefit_commans.data.response.BaseApiResponseData<List<MarketPlace>> {
        TODO("Not yet implemented")
    }

    override suspend fun getMarketPlacesOld(): com.noisefit_commans.data.response.BaseApiResponseData<List<String>> {
        TODO("Not yet implemented")
    }

    override suspend fun addWarrantyOld(jsonObject: JsonObject): com.noisefit_commans.data.response.BaseApiResponse<String?> {
        TODO("Not yet implemented")
    }

    override suspend fun getWarrantyWatchList(
        url: String,
        apiKey: String
    ): com.noisefit_commans.data.response.BaseApiResponseData<List<WarrantyWatchesResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun addWarranty(
        url: String,
        apiKey: String,
        jsonObject: JsonObject
    ): com.noisefit_commans.data.response.BaseApiResponse<String?> {
        TODO("Not yet implemented")
    }

    override suspend fun getTimeZonesCities(): com.noisefit_commans.data.response.BaseApiResponseData<WorldClockResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getChallengeList(): com.noisefit_commans.data.response.BaseApiResponseChallenge<com.noisefit_commans.data.response.ChallengeListResponse> {
        TODO("Not yet implemented")

    }

    override suspend fun getChallengeDetail(challengeId: String): com.noisefit_commans.data.response.BaseApiResponseChallenge<com.noisefit_commans.data.response.ChallengeDetailResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getChallengeDetailByID(id: String): com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.ChallengeModel> {
        var challengeModel: com.noisefit_commans.data.response.ChallengeModel? = null
        for (key in challengeData.keys) {
            if (challengeData[key]!!.challenge_id == id.toInt()) {
                challengeModel = challengeData[key]!!
            }
        }

        return if (challengeModel == null) {
            com.noisefit_commans.data.response.BaseApiResponse(
                data = null,
                message = "",
                error = com.noisefit_commans.data.response.ErrorMessage("no data found")
            )
        } else {
            com.noisefit_commans.data.response.BaseApiResponse(
                data = challengeModel,
                message = "",
                error = null
            )
        }

    }

    override suspend fun joinChallengeById(url: String): com.noisefit_commans.data.response.BaseApiResponse<String> {
        TODO("Not yet implemented")
    }

    override suspend fun leaveChallengeById(
        url: String,
        requestObject: JsonObject
    ): com.noisefit_commans.data.response.BaseApiResponse<String> {
        TODO("Not yet implemented")
    }

    override suspend fun joinChallenge(requestObject: com.noisefit_commans.data.response.JoinChallengeRequest): com.noisefit_commans.data.response.BaseApiResponse<String> {
        TODO("Not yet implemented")
    }

    override suspend fun leaveChallenge(requestObject: com.noisefit_commans.data.response.LeaveChallengeRequest): com.noisefit_commans.data.response.BaseApiResponse<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getChallengeLeaderboard(url: String): com.noisefit_commans.data.response.BaseApiResponse<ArrayList<Leadership>> {
        TODO("Not yet implemented")
    }

    override suspend fun getChallengeBuddyLeaderboard(url: String): com.noisefit_commans.data.response.BaseApiResponseChallenge<ArrayList<Leadership>> {
        TODO("Not yet implemented")
    }


    override suspend fun addBuddy(requestObject: com.noisefit_commans.data.response.AddBuddyRequest): com.noisefit_commans.data.response.BaseApiResponse<Any> {
        TODO("Not yet implemented")
    }

    override suspend fun config(): com.noisefit_commans.data.response.BaseApiResponse<ConfigResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getDashboardBanner(): com.noisefit_commans.data.response.BaseApiResponse<List<DashboardBanner>> {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentChallenges(url: String): com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.ChallengeListingResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getCompletedChallenges(url: String): com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.ChallengeListingResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getHelpAndSupportList(url: String): com.noisefit_commans.data.response.BaseApiResponse<List<HelpAndSupportResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun getHelpAndSupportByQuestionId(url: String): com.noisefit_commans.data.response.BaseApiResponse<HelpAndSupportDetailResponse> {
        TODO("Not yet implemented")
    }

}