package com.noisefit.data.repository.implementation

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.data.dataConverter.OfflineDataMapper
import com.noisefit.data.googleFit.GoogleFitDataObservers
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit.data.local.db.fromJson
import com.noisefit.data.model.GoalModel
import com.noisefit.data.remote.CityData
import com.noisefit.data.remote.StateData
import com.noisefit.data.remote.UserLocationUpdatedResponse
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit.data.safeCacheCall
import com.noisefit.luna.BuildConfig
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.Interest
import com.noisefit_commans.data.model.KeyValue
import com.noisefit_commans.data.model.RecentActivities
import com.noisefit_commans.data.model.User
import com.noisefit_commans.data.model.circadian.NudgeCircadianGraph
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.BaseApiResponseData
import com.noisefit_commans.data.response.MessageResponse
import com.noisefit_commans.data.response.UpdateDeviceResponse
import com.noisefit_commans.data.response.UserResponse
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.models.Units
import com.noisefit_commans.models.UserGoals
import com.noisefit_commans.models.UserInfo
import com.noisefit_commans.ui.checkDayDifferenceMoreOne
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.NotificationToggleModel
import com.oreo.data.model.RingLocationData
import com.noisefit_commans.data.model.customHomeScreen.CustomHomeScreenModel
import com.oreo.data.model.CaffeineFoodItem
import com.oreo.data.model.CaffeinePostApiModel
import com.oreo.data.model.circadian.CircadianQuizResponseModel
import com.oreo.data.model.circadian.CircadianResponseModel
import com.noisefit_commans.data.model.timeline.TimelineScreenResponse
import com.noisefit_commans.ui.checkTimeDifferenceMoreNMinutes
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.time.LocalDate

class UserRepositoryImpl(
    private val localDatSource: DataStoredInterface,
    private val remoteDataSource: NetworkService,
    private val lastSyncProvider: LastSyncProvider,
    private val offlineDataMapper: OfflineDataMapper,
    private val googleFitDataObservers: GoogleFitDataObservers,
    private val dataUnitConverter: DataUnitConverter,
    private val keyValueDataSource: KeyValueDataSource,
    private val userHealthDataDataSource: OreoUserHealthDataDataSource,
    private val gson: Gson,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserRepository {

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

    override suspend fun getRingLastLocation(mac: String): Flow<Resource<BaseApiResponse<RingLocationData>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/user_detail/ring/get/last-disconnect/location/$mac"
            remoteDataSource.getRingLastLocation(url)
        }
    }

    override suspend fun setRingLastLocation(request: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/user_detail/ring/last-disconnect/location"
            remoteDataSource.setRingLastLocation(url, request)
        }
    }

    override suspend fun saveActivity(sportsModeResponse: List<SportsModeResponse>?) {
        val enableGoogleFit = localDatSource.isEnableGoogleFit()
        val syncWorkout = localDatSource.getStatusGoogleFitKey("workout")
        LOGS.i("SAVE Activity")
        AppLogs.sendAppLogs("Save to Google fit $sportsModeResponse")

        sportsModeResponse?.forEach { response ->
            if (enableGoogleFit && syncWorkout) {
                val sportsMode = offlineDataMapper.convertSportDataToGoogleFit(response)
                sportsMode?.let {
                    googleFitDataObservers.insertActivityData(sportsMode)
                }

            }
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

    override suspend fun getUserProfile(): Flow<Resource<UserResponse>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getUserProfile("${BuildConfig.BASE_URL_NEW}/auth_v2/auth/detail/user")
        }
    }

    override suspend fun updatePushToken(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/user_detail/update/push-token"
            remoteDataSource.updatePushToken(url, request)
        }
    }

    override suspend fun updateUserProfile(request: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<User>>> {
        return safeApiCallFlow(dispatcher) {
            keyValueDataSource.removeDataByType(KeyValueDataType.NOTIFICATION_GOAL_DATA)
            localDatSource.saveAppBodyMeasurementsTimeStamp()
            val url = "${BuildConfig.BASE_URL_NEW}/user_detail/profile/update"
            remoteDataSource.updateUserProfile(url, request)
        }
    }

    override suspend fun getUserGoalsList(): Flow<Resource<BaseApiResponseData<List<GoalModel>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/user_detail/ring/intent/list"
            remoteDataSource.getUserGoalsList(url)
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
            val url = "${BuildConfig.BASE_URL_NEW}/user_detail/upload/profile-image"
            remoteDataSource.uploadUserImage(url, requestFile)
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

    override suspend fun saveAppLanguage(): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.saveAppLanguage(
                "${BuildConfig.BASE_URL_NEW}/user_detail/language/change"
            )
        }
    }

    override suspend fun getNotificationToggle(): Flow<Resource<BaseApiResponse<NotificationToggleModel>>> {

        return flow {
            emit(Resource.Loading(true))

            val type = KeyValueDataType.NOTIFICATION_GOAL_TOGGLE
            var resultData: NotificationToggleModel? = null


            val cacheResult = safeCacheCall(Dispatchers.IO) {
                val localData =
                    keyValueDataSource.getData("", type)
                        ?: return@safeCacheCall null

                val lastCallTime = localData.getSafeLastSyncValue()

                val shouldCallApi =
                    lastCallTime.checkDayDifferenceMoreOne()
                LOGS.d("FORCE_REFRESH should call api $shouldCallApi")


                if (shouldCallApi) {
                    keyValueDataSource.removeDataByKey("", type)
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<NotificationToggleModel>(
                            it
                        )
                    }
                }
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
                emit(Resource.Loading(false))
                emit(
                    Resource.Success(
                        BaseApiResponse(
                            data = resultData,
                            message = "",
                        )
                    )
                )
                return@flow
            }


            val serverResult = safeApiCallFlow(dispatcher) {
                val date = LocalDate.now()
                val url = "${BuildConfig.OREO_BASE_URL}/activity/v2/notification/toggle?date=$date"
                remoteDataSource.getNotificationToggle(url)
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
                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                safeCacheCall(Dispatchers.IO) {
                    keyValueDataSource.insertData(
                        KeyValue(
                            key = "",
                            value = gson.toJson(resultData),
                            type = type.name
                        )
                    )
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(Resource.Loading(false))
                            emit(
                                Resource.Success(
                                    BaseApiResponse(
                                        data = resultData,
                                        message = "",
                                    )
                                )
                            )
                        }

                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }
            }
        }
    }

    override suspend fun updateNotificationToggle(requestObject: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            keyValueDataSource.removeDataByType(KeyValueDataType.NOTIFICATION_GOAL_TOGGLE)
            val date = LocalDate.now()
            remoteDataSource.updateNotificationToggle(
                "${BuildConfig.OREO_BASE_URL}/activity/v2/notification/toggle?date=$date",
                requestObject
            )
        }
    }

    override suspend fun submitCustomHomeScreenPriority(request: CustomHomeScreenModel): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.submitCustomHomeScreenItemsPriority(
                "${BuildConfig.OREO_BASE_URL}/protean/v3/custom-screen",
                request
            )
        }
    }

    override suspend fun getCaffeineWindowItemsList(): Flow<Resource<BaseApiResponse<List<CaffeineFoodItem>>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getCaffeineWindowItemsList(
                "${BuildConfig.OREO_BASE_URL}/sleep/v3/caffeine_items",
            )
        }
    }

    override suspend fun updateCaffeineItemsList(req: CaffeinePostApiModel): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.updateCaffeineItemsList(
                "${BuildConfig.OREO_BASE_URL}/sleep/v3/favorite_caffeine_item",
                req
            )
        }
    }

    override suspend fun getCannyFeedbackUrl(): Flow<Resource<BaseApiResponse<String>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getCannyFeedbackUrl(
                "${BuildConfig.OREO_BASE_URL}/protean/v3/canny",
            )
        }
    }

    override suspend fun sendAppTrackingEvent(request: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.sendAppTrackingEvent(
                "${BuildConfig.OREO_BASE_URL}/protean/v3/time-log",
                request
            )
        }
    }

    override suspend fun updateWorkoutStatus(request: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.updateWorkoutStatus(
                "${BuildConfig.OREO_BASE_URL}/protean/v3/workout-init",
                request
            )
        }
    }

    override suspend fun submitLogCircadianData(request: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.submitLogCircadianData(
                "${BuildConfig.OREO_BASE_URL}/sleep/v3/circadian/logs",
                request
            )
        }
    }

    override suspend fun getCircadianData(): Flow<Resource<BaseApiResponse<CircadianResponseModel>>> {


        return flow {
            emit(Resource.Loading(true))

            val type = KeyValueDataType.CIRCADIAN_DATA
            var resultData: CircadianResponseModel? = null


            val cacheResult = safeCacheCall(Dispatchers.IO) {
                val localData =
                    keyValueDataSource.getData("", type)
                        ?: return@safeCacheCall null

                val lastCallTime = localData.getSafeLastSyncValue()

                val shouldCallApi =
                    lastCallTime.checkTimeDifferenceMoreNMinutes(10)
                LOGS.d("FORCE_REFRESH should call api $shouldCallApi")


                if (shouldCallApi) {
                    keyValueDataSource.removeDataByKey("", type)
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<CircadianResponseModel>(
                            it
                        )
                    }
                }
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
                emit(Resource.Loading(false))
                emit(
                    Resource.Success(
                        BaseApiResponse(
                            data = resultData,
                            message = "",
                        )
                    )
                )
                return@flow
            }


            val serverResult = safeApiCallFlow(dispatcher) {
                remoteDataSource.getCircadianData(
                    "${BuildConfig.OREO_BASE_URL}/sleep/v3/circadian/logs",
                )
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
                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                safeCacheCall(Dispatchers.IO) {
                    keyValueDataSource.insertData(
                        KeyValue(
                            key = "",
                            value = gson.toJson(resultData),
                            type = type.name
                        )
                    )
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(Resource.Loading(false))
                            emit(
                                Resource.Success(
                                    BaseApiResponse(
                                        data = resultData,
                                        message = "",
                                    )
                                )
                            )
                        }

                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }
            }
        }
    }

    override suspend fun getNudgeCircadianData(reqObj: JsonObject): Flow<Resource<BaseApiResponse<NudgeCircadianGraph>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getNudgeCircadianData(
                "${BuildConfig.OREO_BASE_URL}/ai/v2/generate/nudge/new",
                reqObj
            )
        }
    }

    override suspend fun getCircadianQuizData(): Flow<Resource<BaseApiResponse<List<CircadianQuizResponseModel>>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getCircadianQuizData(
                "${BuildConfig.OREO_BASE_URL}/sleep/v3/circadian/chrono/ques",
            )
        }
    }

    override suspend fun submitCircadianQuizData(req: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.submitCircadianQuizData(
                "${BuildConfig.OREO_BASE_URL}/sleep/v3/circadian/onboarding",
                req
            )
        }
    }

    override suspend fun getCurrDayTimelineActivitiesData(date: String): Flow<Resource<BaseApiResponse<TimelineScreenResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getCurrDayTimelineActivitiesData(
                "${BuildConfig.OREO_BASE_URL}/protean/v3/time-tracker",
                date
            )
        }
    }

    override suspend fun submitLogMealTimelineData(req: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            userHealthDataDataSource.clearDataByDates(listOf(DateFormats.getTodaysDateString(10)))
            keyValueDataSource.removeDataByKey("", KeyValueDataType.CIRCADIAN_DATA)
            remoteDataSource.submitLogMealTimelineData(
                "${BuildConfig.OREO_BASE_URL}/protean/v3/track-meal",
                req
            )
        }
    }

    override suspend fun submitLogCaffeineTimelineData(req: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            userHealthDataDataSource.clearDataByDates(listOf(DateFormats.getTodaysDateString(10)))
            keyValueDataSource.removeDataByKey("", KeyValueDataType.CIRCADIAN_DATA)
            remoteDataSource.submitLogCaffeineTimelineData(
                "${BuildConfig.OREO_BASE_URL}/protean/v3/track-caffeine",
                req
            )
        }
    }

    override suspend fun submitLogLightExposureTimelineData(req: JsonObject): Flow<Resource<BaseApiResponse<Any>>> {
        return safeApiCallFlow(dispatcher) {
            userHealthDataDataSource.clearDataByDates(listOf(DateFormats.getTodaysDateString(10)))
            keyValueDataSource.removeDataByKey("", KeyValueDataType.CIRCADIAN_DATA)
            remoteDataSource.submitLogLightExposureTimelineData(
                "${BuildConfig.OREO_BASE_URL}/protean/v3/track-light",
                req
            )
        }
    }

}