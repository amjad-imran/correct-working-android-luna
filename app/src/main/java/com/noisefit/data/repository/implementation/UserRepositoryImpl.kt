package com.noisefit.data.repository.implementation

import android.net.Uri
import com.google.gson.JsonObject
import com.noisefit.BuildConfig
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.data.dataConverter.OfflineDataMapper
import com.noisefit.data.googleFit.GoogleFitDataObservers
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.database.ActivityDao
import com.noisefit.data.local.db.implementation.*
import com.noisefit_commans.data.model.*
import com.noisefit_commans.data.model.trophies.Trophies
import com.noisefit_commans.data.model.trophies.TrophyBadge
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.request.UpdateAdditionalDetailRequest
import com.noisefit_commans.data.response.*
import com.noisefit.data.repository.LastSyncItems
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.UserActivityRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit.data.safeCacheCall
import com.noisefit.receiver.workManager.HealthOverviewDataType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.Interest
import com.noisefit_commans.ui.checkDayDifferenceMoreOne
import com.noisefit_commans.models.*
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
    private val activityDao: ActivityDao,
    private val stepsDataImpl: StepsDataImpl,
    private val stressDataImpl: StressDataImpl,
    private val lastSyncProvider: LastSyncProvider,
    private val heartRateDataImpl: HeartRateDataImpl,
    private val bloodOxygenDataImpl: BloodOxygenDataImpl,
    private val sleepDataImpl: SleepDataImpl,
    private val bodyTemperatureDataImpl: BodyTemperatureDataImpl,
    private val offlineDataMapper: OfflineDataMapper,
    private val googleFitDataObservers: GoogleFitDataObservers,
    private val dataUnitConverter: DataUnitConverter,
    private val userActivityRepository: UserActivityRepository,
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
            userActivityRepository.removeLocalStreakData()
            remoteDataSource.setUserDevice(
                "${BuildConfig.BASE_URL_NEW}/master/user/v3/devices",
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
            userActivityRepository.removeLocalStreakData()
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

    override suspend fun getDashboardBanner(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<DashboardBannerData>>> {
        return flow {
            val serverUpdateTimeStamp =
                lastSyncProvider.getSyncTimeStamp(LastSyncItems.DASHBOARD_BANNER_SERVER_UPDATE_1)
            val localDashboardSyncTime =
                lastSyncProvider.getSyncTimeStamp(LastSyncItems.DASHBOARD_BANNER_1)

            val shouldCallApi = shouldCallBannerApi(serverUpdateTimeStamp, localDashboardSyncTime)

            var resultData: DashboardBannerData? = null

            val cacheResult = safeCacheCall(Dispatchers.IO) {
                getLocalDashboardBanners(shouldCallApi)
            }
            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            resultData=it
                        }
                    }
                    is CacheResult.GenericError -> {

                    }
                }
            }


            if (resultData!=null) {
                emit(Resource.Success(
                    com.noisefit_commans.data.response.BaseApiResponse(
                        data = resultData,
                        message = ""
                    )
                ))
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                val url=" ${BuildConfig.BASE_URL_NEW}/master/dashboard_banner_content"
                remoteDataSource.getDashboardBanner(url)
            }


            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(Resource.GenericError(resource.message, resource.errorCode))
                    }
                    is Resource.Loading -> {
                        emit(Resource.Loading(resource.loading))
                    }
                    is Resource.NetworkError -> {
                        emit(Resource.NetworkError(resource.response, resource.code))
                    }
                    is Resource.Success -> {

                        resource.data?.data?.let { response ->
                            lastSyncProvider.setSyncTimeStamp(LastSyncItems.DASHBOARD_BANNER_1)
                            resultData=response
                        }
                    }
                }
            }

            if (resultData!=null) {
                safeCacheCall(Dispatchers.IO) {
                    localDatSource.setDashboardBanners(resultData)
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(Resource.Success(
                                com.noisefit_commans.data.response.BaseApiResponse(
                                    data = resultData,
                                    message = ""
                                )
                            ))
                        }
                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }
            }
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

    override suspend fun getWorkoutShareImages(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<String>>>> {
        return flow {

            val serverUpdateTimeStamp =
                lastSyncProvider.getSyncTimeStamp(LastSyncItems.WORKOUT_IMAGES_SERVER_TIMESTAMP)
            val localSyncTime =
                lastSyncProvider.getSyncTimeStamp(LastSyncItems.WORKOUT_IMAGES_LOCAL_TIMESTAMP)

            val shouldCallApi = shouldCalWorkoutImagesApi(serverUpdateTimeStamp, localSyncTime)

            val resultData = ArrayList<String>()

            val cacheResult = safeCacheCall(Dispatchers.IO) {
                getLocalWorkoutShareData(shouldCallApi)
            }
            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            resultData.clear()
                            resultData.addAll(it)
                        }
                    }
                    is CacheResult.GenericError -> {

                    }
                }
            }


            if (resultData.isNotEmpty()) {
                emit(Resource.Success(
                    com.noisefit_commans.data.response.BaseApiResponse(
                        data = resultData,
                        message = ""
                    )
                ))
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                val url =
                    "${BuildConfig.BASE_URL_NEW}/master/workout_images"
                remoteDataSource.getWorkoutShareImages(url)
            }


            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(Resource.GenericError(resource.message, resource.errorCode))
                    }
                    is Resource.Loading -> {
                        emit(Resource.Loading(resource.loading))
                    }
                    is Resource.NetworkError -> {
                        emit(Resource.NetworkError(resource.response, resource.code))
                    }
                    is Resource.Success -> {

                        resource.data?.data?.let { response ->
                            lastSyncProvider.setSyncTimeStamp(LastSyncItems.WORKOUT_IMAGES_LOCAL_TIMESTAMP)
                            resultData.clear()
                            resultData.addAll(response)
                        }
                    }
                }
            }

            if (resultData.isNotEmpty()) {
                safeCacheCall(Dispatchers.IO) {
                    localDatSource.setWorkoutImages(resultData)
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(Resource.Success(
                                com.noisefit_commans.data.response.BaseApiResponse(
                                    data = resultData,
                                    message = ""
                                )
                            ))
                        }
                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }
            }
        }
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

    override suspend fun getSummaryRecentActivities(isForceRefresh: Boolean): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<RecentActivities>>> {

        return flow {
            val shouldCallApi =
                lastSyncProvider.getSyncTimeStamp(LastSyncItems.RECENT_ACTIVITIES)
                    .checkDayDifferenceMoreOne() || isForceRefresh

            var resultData: RecentActivities? = null
            val cacheResult = safeCacheCall(Dispatchers.IO) {
                getLocalRecentActivities(shouldCallApi)
            }
            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            resultData = it
                        }
                    }
                    is CacheResult.GenericError -> {

                    }
                }
            }


            if (resultData != null) {
                emit(Resource.Success(
                    com.noisefit_commans.data.response.BaseApiResponse(
                        data = resultData,
                        message = ""
                    )
                ))
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                remoteDataSource.getRecentActivities()
            }


            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(Resource.GenericError(resource.message, resource.errorCode))
                    }
                    is Resource.Loading -> {
                        emit(Resource.Loading(resource.loading))
                    }
                    is Resource.NetworkError -> {
                        emit(Resource.NetworkError(resource.response, resource.code))
                    }
                    is Resource.Success -> {

                        resource.data?.data?.let { response ->
                            lastSyncProvider.setSyncTimeStamp(LastSyncItems.RECENT_ACTIVITIES)
                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                safeCacheCall(Dispatchers.IO) {
                    setLocalRecentActivities(resultData)
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(Resource.Success(
                                com.noisefit_commans.data.response.BaseApiResponse(
                                    data = resultData,
                                    message = ""
                                )
                            ))
                        }
                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }
            }
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

    override suspend fun getOfflineActivities(): List<SportsModeResponse> {
        return activityDao.getActivities()
    }

    override suspend fun saveActivity(sportsModeResponse: List<SportsModeResponse>?) {
        val enableGoogleFit = localDatSource.isEnableGoogleFit()
        LOGS.d("SAVE Activity")
        sportsModeResponse?.forEach { response ->
            if (enableGoogleFit) {
                val sportsMode = offlineDataMapper.convertSportDataToGoogleFit(response)
                sportsMode?.let {
                    googleFitDataObservers.insertActivityData(sportsMode)
                }

            }



            try {
                GlobalScope.launch(dispatcher) {
                    activityDao.insertActivity(sportsModeResponse = response)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun getUnSyncedActivities(): List<SportsModeResponse> {
        return activityDao.getUnSyncedActivities()
    }

    override suspend fun setActivitiesSynced() {
        return activityDao.setActivitiesSynced()
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

    override suspend fun getTodayStepsData(): StepsData? {
        val todayDate = DateFormats.getTodaysDateString(7)
        return stepsDataImpl.getTodayData(todayDate)
    }

    override suspend fun getSummaryHealthOverview(
        deviceFeatures: DeviceFeatures
    ): HealthOverviewData {
        try {
            val todayDate = DateFormats.getTodaysDateString(7)
            val user = localDatSource.getUser()

            val healthOverviewList = ArrayList<HealthOverview>()
            val stepsData = stepsDataImpl.getTodayData(todayDate)


            val healthOverviewData = offlineDataMapper.convertHealthOverviewData(
                stepsData,
                user?.userGoals,
                dataUnitConverter
            )

            if (deviceFeatures.heartRate == 1) {
                healthOverviewList.add(
                    offlineDataMapper.convertHeartRateOverviewData(
                        heartRateDataImpl.getTodayData(todayDate)
                    )
                )
            }


            if (deviceFeatures.sleepData == 1) {
                healthOverviewList.add(
                    offlineDataMapper.convertSleepOverviewData(
                        sleepDataImpl.getTodayData(todayDate)
                    )
                )
            }


            if (deviceFeatures.bloodOxygen == 1) {
                healthOverviewList.add(
                    offlineDataMapper.convertBloodOxygenOverviewData(
                        bloodOxygenDataImpl.getTodayData(
                            todayDate
                        )
                    )
                )
            }


            if (deviceFeatures.stressCount == 1) {
                healthOverviewList.add(
                    offlineDataMapper.convertStressOverviewData(
                        stressDataImpl.getTodayData(
                            todayDate
                        )
                    )
                )
            }

            if (deviceFeatures.bodyTemperature == 1) {
                healthOverviewList.add(
                    offlineDataMapper.convertBodyTempOverviewData(
                        bodyTemperatureDataImpl.getTodayData(
                            todayDate
                        ),
                        dataUnitConverter,
                        localDatSource.getBodyTempUnit()
                    )
                )
            }

            if (deviceFeatures.stepsData == 1) {
                healthOverviewList.add(
                    offlineDataMapper.convertStepsOverviewData(
                        stepsData,
                        user?.userGoals
                    )
                )
            }

            if (deviceFeatures.stepsData == 1) {
                healthOverviewList.add(
                    offlineDataMapper.convertDistanceOverviewData(
                        stepsData,
                        user?.userGoals,
                        dataUnitConverter
                    )
                )
            }

            if (deviceFeatures.calorieData == 1) {
                healthOverviewList.add(
                    offlineDataMapper.convertCaloriesOverviewData(
                        stepsData
                    )
                )
            }


            healthOverviewData.healthOverviewList = healthOverviewList
            return healthOverviewData
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return HealthOverviewData()
    }

    //TODO: Need to Think  a way to handle it more efficiently
    override suspend fun getAllHealthOverview(deviceFeatures: DeviceFeatures): HealthOverviewData {
        val todayDate = DateFormats.getTodaysDateString(7)
        val user = localDatSource.getUser()

        val healthOverviewList = ArrayList<HealthOverview>()
        val stepsData = stepsDataImpl.getTodayData(todayDate)

        val healthOverviewData = offlineDataMapper.convertHealthOverviewData(
            stepsData,
            user?.userGoals,
            dataUnitConverter
        )

        if (deviceFeatures.stepsData == 1) {
            healthOverviewList.add(
                offlineDataMapper.convertStepsOverviewData(
                    stepsData,
                    user?.userGoals
                )
            )
        }
        if (deviceFeatures.stepsData == 1) {
            healthOverviewList.add(
                offlineDataMapper.convertDistanceOverviewData(
                    stepsData,
                    user?.userGoals,
                    dataUnitConverter
                )
            )
        }

        if (deviceFeatures.calorieData == 1) {
            healthOverviewList.add(
                offlineDataMapper.convertCaloriesOverviewData(
                    stepsData
                )
            )
        }

        if (deviceFeatures.heartRate == 1) {
            healthOverviewList.add(
                offlineDataMapper.convertHeartRateOverviewData(
                    heartRateDataImpl.getTodayData(todayDate)
                )
            )
        }


        if (deviceFeatures.sleepData == 1) {
            healthOverviewList.add(
                offlineDataMapper.convertSleepOverviewData(
                    sleepDataImpl.getTodayData(todayDate)
                )
            )
        }


        if (deviceFeatures.bloodOxygen == 1) {
            healthOverviewList.add(
                offlineDataMapper.convertBloodOxygenOverviewData(
                    bloodOxygenDataImpl.getTodayData(
                        todayDate
                    )
                )
            )
        }


        if (deviceFeatures.stressCount == 1) {
            healthOverviewList.add(
                offlineDataMapper.convertStressOverviewData(
                    stressDataImpl.getTodayData(
                        todayDate
                    )
                )
            )
        }


        if (deviceFeatures.bodyTemperature == 1) {
            healthOverviewList.add(
                offlineDataMapper.convertBodyTempOverviewData(
                    bodyTemperatureDataImpl.getTodayData(
                        todayDate
                    ),
                    dataUnitConverter,
                    localDatSource.getBodyTempUnit()
                )
            )
        }



        healthOverviewData.healthOverviewList = healthOverviewList
        return healthOverviewData


    }

    override suspend fun getHealthOverview(
        healthOverviewDataType: HealthOverviewDataType,
        healthOverviewData: HealthOverviewData?
    ): Pair<HealthOverviewData, Int?> {
        var hOverviewData = healthOverviewData
        if (hOverviewData == null) {
            hOverviewData = HealthOverviewData()
        }

        var index: Int? = null
        if (hOverviewData.healthOverviewList == null) {
            hOverviewData.healthOverviewList = ArrayList()
        }
        val todayDate = DateFormats.getTodaysDateString(7)
        val user = localDatSource.getUser()

        //val healthOverviewList = ArrayList<HealthOverview>()

        when (healthOverviewDataType) {
            HealthOverviewDataType.ALL, HealthOverviewDataType.ACTIVITY -> {

            }

            HealthOverviewDataType.STEPS -> {

                val stepsData = stepsDataImpl.getTodayData(todayDate)

                val hrOverview = offlineDataMapper.convertHealthOverviewData(
                    stepsData,
                    user?.userGoals,
                    dataUnitConverter
                )
                hOverviewData.apply {
                    stand = hrOverview.stand
                    standGoal = hrOverview.standGoal
                    calories = hrOverview.calories
                    caloriesGoal = hrOverview.caloriesGoal
                    distance = hrOverview.distance
                    distanceGoal = hrOverview.distanceGoal
                    activeMinute = hrOverview.activeMinute
                    activeMinuteGoal = hrOverview.activeMinuteGoal
                    stepsGoal = hrOverview.stepsGoal
                    steps = hrOverview.steps
                    stepsGoalProgress = hrOverview.stepsGoalProgress
                    caloriesGoalProgress = hrOverview.caloriesGoalProgress
                    distanceGoalProgress = hrOverview.distanceGoalProgress

                }

                index = hOverviewData.healthOverviewList?.indexOfFirst {
                    it is HealthOverview.Steps
                }

                val distanceIndex = hOverviewData.healthOverviewList?.indexOfFirst {
                    it is HealthOverview.Distance
                }

                val caloriesIndex = hOverviewData.healthOverviewList?.indexOfFirst {
                    it is HealthOverview.Calories
                }
                if (index != -1) {
                    hOverviewData.healthOverviewList!![index!!] =
                        offlineDataMapper.convertStepsOverviewData(
                            stepsData,
                            user?.userGoals
                        )
                }

                if (distanceIndex != -1) {
                    hOverviewData.healthOverviewList!![distanceIndex!!] =
                        offlineDataMapper.convertDistanceOverviewData(
                            stepsData,
                            user?.userGoals,
                            dataUnitConverter
                        )
                }

                if (caloriesIndex != -1) {
                    hOverviewData.healthOverviewList!![caloriesIndex!!] =
                        offlineDataMapper.convertCaloriesOverviewData(
                            stepsData
                        )
                }

            }
            HealthOverviewDataType.STRESS -> {
                index = hOverviewData.healthOverviewList?.indexOfFirst {
                    it is HealthOverview.Stress
                }
                if (index != -1) {
                    hOverviewData.healthOverviewList!![index!!] =
                        offlineDataMapper.convertStressOverviewData(
                            stressDataImpl.getTodayData(
                                todayDate
                            )
                        )
                }
            }
            HealthOverviewDataType.SLEEP -> {

                index = hOverviewData.healthOverviewList?.indexOfFirst {
                    it is HealthOverview.Sleep
                }
                if (index != -1) {
                    hOverviewData.healthOverviewList!![index!!] =
                        offlineDataMapper.convertSleepOverviewData(
                            sleepDataImpl.getTodayData(todayDate)
                        )
                }


            }
            HealthOverviewDataType.HEART -> {

                index = hOverviewData.healthOverviewList?.indexOfFirst {
                    it is HealthOverview.HeartRate
                }
                if (index != -1) {
                    hOverviewData.healthOverviewList!![index!!] =
                        offlineDataMapper.convertHeartRateOverviewData(
                            heartRateDataImpl.getTodayData(
                                todayDate
                            )
                        )
                }


            }
            HealthOverviewDataType.BLOOD -> {

                index = hOverviewData.healthOverviewList?.indexOfFirst {
                    it is HealthOverview.BloodOxygen
                }
                if (index != -1) {
                    hOverviewData.healthOverviewList!![index!!] =
                        offlineDataMapper.convertBloodOxygenOverviewData(
                            bloodOxygenDataImpl.getTodayData(
                                todayDate
                            )
                        )
                }


            }
            HealthOverviewDataType.TEMPERATURE -> {

                index = hOverviewData.healthOverviewList?.indexOfFirst {
                    it is HealthOverview.BodyTemp
                }
                if (index != -1) {
                    hOverviewData.healthOverviewList!![index!!] =
                        offlineDataMapper.convertBodyTempOverviewData(
                            bodyTemperatureDataImpl.getTodayData(
                                todayDate
                            ),
                            dataUnitConverter,
                            localDatSource.getBodyTempUnit()
                        )
                }

            }

            HealthOverviewDataType.SERVER_SYNC_SUCCESS -> {}
        }

        return Pair(hOverviewData, index)

    }

    override suspend fun saveBloodOxygenData(
        data: List<BloodOxygenBreakup>
    ): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(Dispatchers.IO) {
            bloodOxygenDataImpl.insertData(
                data
            )
        }
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