package com.noisefit.data.repository.abstraction


import com.google.gson.JsonObject
import com.noisefit_commans.data.model.User
import com.noisefit_commans.data.model.UserStats
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.request.LoginRequest
import com.noisefit.data.remote.request.RegistrationRequest
import com.noisefit_commans.data.response.*
import kotlinx.coroutines.flow.Flow

interface AuthenticationRepository {


    suspend fun signup(signup: RegistrationRequest): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<RegistrationResponse>>>

    suspend fun loginUser(login: LoginRequest): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<RegistrationResponse>>>

    suspend fun logoutUser(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<String?>>>

    suspend fun deleteUser(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<String?>>>



    suspend fun generateOtp(
        jsonObject: JsonObject
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>>

    suspend fun changeMobileNumber(
        jsonObject: JsonObject
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>>

    suspend fun changeEmail(
        jsonObject: JsonObject
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>>

    suspend fun sendOtp(
        jsonObject: JsonObject
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<SendOtpResponse>>>

    suspend fun createInternationalUser(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<RegistrationResponse>>>

    suspend fun verifyOtp(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<UserResponse>>>

    suspend fun resetPassword(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>>

    suspend fun updatePassword(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>>

    /**
     * @param forceReplaceLocal ->Forcefully replace the locally stored goals and info
     */
    suspend fun saveUserInfo(user: User, forceReplaceLocal: Boolean = false)

    suspend fun saveUserImage(imageUrl:String)

    suspend fun getUser(): User?

    suspend fun logoutUserLocally(): Flow<Boolean>
}