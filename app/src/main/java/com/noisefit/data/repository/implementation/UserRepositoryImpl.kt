package com.noisefit.data.repository.implementation

import android.net.Uri
import com.google.gson.JsonObject
import com.noisefit.luna.BuildConfig
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.data.dataConverter.OfflineDataMapper
import com.noisefit.data.googleFit.GoogleFitDataObservers
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.CityData
import com.noisefit.data.remote.StateData
import com.noisefit.data.remote.UserLocationUpdatedResponse
import com.noisefit_commans.data.model.*
import com.noisefit_commans.data.model.trophies.Trophies
import com.noisefit_commans.data.model.trophies.TrophyBadge
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.request.UpdateAdditionalDetailRequest
import com.noisefit_commans.data.response.*
import com.noisefit.data.repository.LastSyncItems
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit.data.safeCacheCall
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.Interest
import com.noisefit_commans.ui.checkDayDifferenceMoreOne
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UserRepositoryImpl(
    private val localDatSource: DataStoredInterface,
    private val remoteDataSource: NetworkService,
    private val lastSyncProvider: LastSyncProvider,
    private val offlineDataMapper: OfflineDataMapper,
    private val googleFitDataObservers: GoogleFitDataObservers,
    private val dataUnitConverter: DataUnitConverter,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserRepository {

    override suspend fun getTrophiesData(
        date: String,
        unitSystem: String
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<Trophies>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getTrophiesGoal(date, unitSystem)
        }
    }

    override suspend fun collectBadge(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<String>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.collectBadge(request)
        }
    }

    override suspend fun collectChallengeTrophy(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseChallenge<Any>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.collectChallengeTrophy(request)
        }
    }

    override suspend fun getRecentTrophies(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<TrophyBadge>>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getRecentTrophies()
        }
    }

    override suspend fun saveUserLocation(request: JsonObject): Flow<Resource<BaseApiResponse<UserLocationUpdatedResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/users/location/save"
            remoteDataSource.saveUserLocation(url, request)
        }
    }

    override suspend fun getStateList(): Flow<Resource<BaseApiResponse<List<StateData>>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/master/location/state_list/1"
            remoteDataSource.getStateList(url)
        }
    }

    override suspend fun getCityList(stateId: Int): Flow<Resource<BaseApiResponse<List<CityData>>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/master/location/city_list/$stateId"
            remoteDataSource.getCityList(url)
        }
    }

    override suspend fun saveActivity(sportsModeResponse: List<SportsModeResponse>?) {
        val enableGoogleFit = localDatSource.isEnableGoogleFit()
        LOGS.i("SAVE Activity")
        AppLogs.sendAppLogs("Save to Google fit $sportsModeResponse")

        sportsModeResponse?.forEach { response ->
            if (enableGoogleFit) {
                val sportsMode = offlineDataMapper.convertSportDataToGoogleFit(response)
                sportsMode?.let {
                    googleFitDataObservers.insertActivityData(sportsMode)
                }

            }
        }
    }

    override suspend fun getBuddiesTrophiesData(requestObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<Trophies>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getBuddiesTrophiesData(requestObject)
        }
    }

    override suspend fun searchProduct(request: String): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.searchProduct(request)
        }
    }

    override suspend fun saveUserDevice(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<UpdateDeviceResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.setUserDevice(
                "${BuildConfig.BASE_URL_NEW}/user_detail/ring/devices",
                request
            )
        }
    }

    override suspend fun getUserProfileInfo(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<UserInfoResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getUserProfileInfo()
        }
    }

    override suspend fun getUserProfile(): Flow<Resource<UserResponse>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getUserProfile("${BuildConfig.BASE_URL_NEW}/auth_v2/auth/detail/user")
        }
    }

    override suspend fun saveAdditionalDetails(request: UpdateAdditionalDetailRequest): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.saveAdditionalUserDetails(request)
        }
    }

    override suspend fun updatePushToken(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.updatePushToken(request)
        }
    }

    override suspend fun updateUserProfile(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<User>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.updateUserProfile(request)
        }
    }

    override suspend fun updateUserProfile(request: User): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<User>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.updateUserProfile(request)
        }
    }

    override suspend fun getTimeZonesCities(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<WorldClockResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getTimeZonesCities()
        }
    }

    override suspend fun getOrderToken(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<String>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getOrderToken(request)
        }
    }

    override suspend fun getActivities(
        startDate: String,
        endDate: String
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.ActivityListResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getActivities(startDate, endDate)
        }
    }

    override suspend fun getRecentActivitiesDates(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<JsonObject>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getRecentActivitiesDates()
        }
    }

    private fun shouldCallBannerApi(
        serverTime: Long,
        localTime: Long
    ): Boolean {
        if (serverTime == 0L) return true
        if (localTime == 0L) return true

        return localTime < serverTime
    }

    fun getLocalDashboardBanners(removeData: Boolean): DashboardBannerData? {
        if (removeData) {
            localDatSource.setDashboardBanners(null)
            return null
        }
        return localDatSource.getDashboardBanners()
    }

    override suspend fun getActivitiesPaging(
        page: Int,
        pageLimit: Int
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<SportsModeResponse>>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getActivitiesPaging(page, pageLimit)
        }
    }

    override suspend fun getActivitiesDetails(itemId: Int): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<SportsModeResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getActivitiesDetails(itemId)
        }
    }

    private fun shouldCalWorkoutImagesApi(
        serverTime: Long,
        localTime: Long
    ): Boolean {
        if (serverTime == 0L) return true
        if (localTime == 0L) return true

        return localTime < serverTime
    }

    fun getLocalWorkoutShareData(removeData: Boolean): List<String>? {
        if (removeData) {
            localDatSource.setWorkoutImages(null)
            return null
        }
        return localDatSource.getWorkoutImages()
    }

    override suspend fun getRecentChallenges(): Flow<Resource<BaseApiResponse<List<ChallengeModel>>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/challenges/feeds/completed_challenges"
            remoteDataSource.getRecentChallenges(url)
        }
    }

    override suspend fun geRecentActivities(isForceRefresh: Boolean): Flow<Resource<BaseApiResponse<RecentActivities>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.geRecentActivities()
        }
    }


    fun getLocalRecentActivities(removeData: Boolean): RecentActivities? {
        if (removeData) {
            localDatSource.setRecentActivities(null)
            return null
        }
        return localDatSource.getRecentActivities()
    }

    fun setLocalRecentActivities(data: RecentActivities?) {
        localDatSource.setRecentActivities(data)
    }

    override suspend fun uploadUserImage(imageUri: Uri): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseImage>> {
        val file = File(imageUri.path)
        //val requestFile :RequestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val requestFile = MultipartBody.Part.createFormData(
            "image",
            file.name,
            file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        )
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.uploadUserImage(requestFile)
        }
    }

    override suspend fun uploadCrashLogFile(file: File): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>> {
        //val requestFile :RequestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val requestFile = MultipartBody.Part.createFormData(
            "report_file",
            file.name,
            file.asRequestBody("text/plain".toMediaTypeOrNull())
        )
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.uploadCrashLogs(
                "android",
                BuildConfig.VERSION_NAME,
                DateFormats.getTodaysDateString(9),
                requestFile
            )
        }
    }

    override suspend fun postActivities(request: SportsModeRequestList): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseActivity>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.postActivities(request)
        }
    }

    override suspend fun getNoiseHealthContent(): Flow<Resource<NoiseHealthResponse>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getNoiseHealthContent()
        }
    }

    override suspend fun setNoiseHealthContentView(request: JsonObject): Flow<Resource<Unit>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.setNoiseHealthContentView(request)
        }
    }

    override suspend fun setNoiseHealthContentPlayTime(request: JsonObject): Flow<Resource<Unit>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.setNoiseHealthContentPlayTime(request)
        }
    }

    override suspend fun getRecentPlayedContent(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<NoiseHealthCategory?>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getRecentPlayedContent()
        }
    }

    override fun getUnitSystem(): Units {
        return localDatSource.getUnit()
    }

    override fun getUserGoals(): UserGoals? {
        return localDatSource.getUser()?.userGoals
    }

    override fun getUserInfo(): UserInfo? {
        return localDatSource.getUser()?.userInfo
    }

    override fun getUser(): User? {
        return localDatSource.getUser()
    }


    override suspend fun getInterests(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<Interest>>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getInterests("${BuildConfig.BASE_URL_NEW}/users/interest/list")
        }
    }

    override suspend fun updateInterests(requestObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<Interest>>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.updateInterests(
                "${BuildConfig.BASE_URL_NEW}/users/interest/save",
                requestObject
            )
        }
    }

}