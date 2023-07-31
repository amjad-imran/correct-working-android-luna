package com.noisefit.data.repository.implementation

import com.google.gson.JsonObject
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.NetworkConstants
import com.noisefit.data.remote.NetworkErrors
import com.noisefit.data.remote.abstraction.ExternalFirmwareService
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.MessageResponse
import com.noisefit.data.repository.abstraction.ExternalFirmwareRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException

class ExternalFirmwareRepositoryImpl(
    private val firmwareService: ExternalFirmwareService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ExternalFirmwareRepository {


    override suspend fun getNavFirmware(productCode: String): Flow<Resource<MessageResponse?>> {
        return safeApiCallFlowFirmware(dispatcher){
            firmwareService.getNavFirmware(JsonObject())
        }
    }

    override suspend fun getAccessToken(): Flow<Resource<MessageResponse?>> {
        return safeApiCallFlowFirmware(dispatcher){
            firmwareService.getAccessToken(JsonObject())
        }
    }

}

suspend fun <T> safeApiCallFlowFirmware(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T?
): Flow<Resource<T>> {

    return flow {
        emit(Resource.Loading(true))
        try {
            withTimeout(NetworkConstants.CALL_TIMEOUT) {
                val response = apiCall.invoke()
                emit(Resource.Success(response))
                emit(Resource.Loading(false))
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            when (throwable) {
                is TimeoutCancellationException -> {
                    emit(networkError())
                }
                is IOException -> {
                    emit(networkError())
                }
                is HttpException -> {
                    emit(networkError())
                }
                else -> {
                    emit(networkError())
                }
            }
        }
    }.flowOn(dispatcher).catch {

    }
}

private fun networkError(): Resource.NetworkError {
    return Resource.NetworkError(
        ErrorResponse(
            UIComponentType.RetryApiDialog(NetworkErrors.NETWORK_ERROR_UNKNOWN)
        )
    )
}