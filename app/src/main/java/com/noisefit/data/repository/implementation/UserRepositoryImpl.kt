package com.noisefit.data.repository.implementation

import android.net.Uri
import com.google.gson.JsonObject
import com.noisefit.luna.BuildConfig
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.data.dataConverter.OfflineDataMapper
import com.noisefit.data.googleFit.GoogleFitDataObservers
import com.noisefit.data.model.GoalModel
import com.oreo.data.model.RingLocationData
import com.noisefit.data.remote.CityData
import com.noisefit.data.remote.StateData
import com.noisefit.data.remote.UserLocationUpdatedResponse
import com.noisefit_commans.data.model.*
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.*
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.Interest
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.LOGS
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

}