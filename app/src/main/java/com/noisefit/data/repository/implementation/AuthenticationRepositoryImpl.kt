package com.noisefit.data.repository.implementation

//import com.clevertap.android.sdk.CleverTapAPI
import com.google.gson.JsonObject
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit_commans.data.model.User
import com.noisefit.luna.BuildConfig
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.request.LoginRequest
import com.noisefit.data.remote.request.RegistrationRequest
import com.noisefit_commans.data.response.*
import com.noisefit.data.repository.abstraction.AuthenticationRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.AppLogs
import com.oreo.data.db.OreoDataBase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class AuthenticationRepositoryImpl(
    private val remoteDataSource: NetworkService,
    private val localDataSource: DataStoredInterface,
//    private val cleverTapAPI: CleverTapAPI?,
    private val keyValueDataSource: KeyValueDataSource,
    private val database: OreoDataBase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : AuthenticationRepository {


    override suspend fun sendOtp(
        jsonObject: JsonObject
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<SendOtpResponse>>> {
        return safeApiCallFlow(Dispatchers.IO) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/auth_v2/send-otp"
            remoteDataSource.sendOtp(url,jsonObject)
        }
    }

    override suspend fun createInternationalUser(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<RegistrationResponse>>> {
        return safeApiCallFlow(Dispatchers.IO) {
            remoteDataSource.createInternationalUser(jsonObject)
        }
    }

    override suspend fun verifyOtp(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<UserResponse>>> {
        return safeApiCallFlow(Dispatchers.IO) {
            remoteDataSource.verifyOtp("${BuildConfig.BASE_URL_NEW}/auth_v2/verify-otp", jsonObject)
        }
    }


    override suspend fun saveUserInfo(user: User, forceReplaceLocal: Boolean) {
        localDataSource.saveUserInfo(user)



    }

    override suspend fun getUser(): User? {
        return localDataSource.getUser()
    }

    override suspend fun saveUserImage(imageUrl: String) {
        val userLocal = localDataSource.getUser()
        userLocal?.imageUrl = imageUrl
        userLocal?.let {
            localDataSource.saveUserInfo(it)
        }
    }

    override suspend fun logoutUserLocally(): Flow<Boolean> {
        return flow {
            AppLogs.sendAppLogs("User Logout")
            /*if (localDataSource.getConnectedDevice() != null) {
                *//*localDataSource.getUser()?.let { user ->
                    localDataSource.updateUserGoals(user.userGoals)
                    localDataSource.updateUserProfile(user.userInfo)
                }*//*
            }*/

            GlobalScope.launch(Dispatchers.IO) {
                removeOfflineUserData()
            }

            localDataSource.setVerifyMobileNumberStatus(false)
            localDataSource.setUserDataSynced(false)
            localDataSource.deleteUserInfo()
            localDataSource.deleteUserToken()
            localDataSource.deleteFcmToken()
            localDataSource.setUserLocationMapped(false)
            localDataSource.clearInterestStatus()
            localDataSource.clearQuizQuestionData()
            localDataSource.setWarrantyStatus(-1)
            localDataSource.setIsWatchFaceRewardEarned(false)
            localDataSource.setLastWinsCount(-1)
            emit(true)
        }
    }

    private suspend fun removeOfflineUserData() {
        arrayListOf(
            KeyValueDataType.DASHBOARD,
            KeyValueDataType.SLEEP,
            KeyValueDataType.ACTIVITY,
            KeyValueDataType.READINESS
        ).forEach {
            keyValueDataSource.removeDataByType(it)
            database.clearAllTables()
        }

    }

    override suspend fun resetPassword(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>> {
        return safeApiCallFlow(Dispatchers.IO) {
            remoteDataSource.resetPassword(jsonObject)
        }
    }

    override suspend fun updatePassword(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>> {
        return safeApiCallFlow(Dispatchers.IO) {
            remoteDataSource.updatePassword(jsonObject)
        }
    }


    override suspend fun signup(signup: RegistrationRequest): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<RegistrationResponse>>> {
        return safeApiCallFlow(Dispatchers.IO) {
            remoteDataSource.signup(signup)
        }
    }


    override suspend fun loginUser(login: LoginRequest): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<RegistrationResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.loginUser("${BuildConfig.BASE_URL_NEW}/auth_v2/login", login)
            //remoteDataSource.loginUserOld(login)
        }
    }

    override suspend fun logoutUser(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<String?>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.logoutUser()
        }
    }

    override suspend fun deleteUser(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<String?>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.deleteUser()
        }
    }


    override suspend fun generateOtp(
        jsonObject: JsonObject
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>> {
        return safeApiCallFlow(Dispatchers.IO) {
            remoteDataSource.generateOtp(jsonObject)
        }
    }

    override suspend fun changeMobileNumber(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>> {
        return safeApiCallFlow(Dispatchers.IO) {
            remoteDataSource.changeMobileNumber(jsonObject)
        }
    }

    override suspend fun changeEmail(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>> {
        return safeApiCallFlow(Dispatchers.IO) {
            remoteDataSource.changeEmail(jsonObject)
        }
    }


    companion object {
        val SIGN_UP_SUCCESS = "Signup success"
        val SIGN_UP_FAILED = "Signup failed"
        val USER_ACCOUNT_DOESNOT_EXIST = "user account does not exist"
        val MOBILE_NUMBER_REQUIRED = "mobile number required"


    }
}