package com.noisefit.data

import com.google.gson.Gson
import com.noisefit.data.local.db.CacheConstants.CACHE_TIMEOUT
import com.noisefit.data.local.db.CacheErrors.CACHE_ERROR_TIMEOUT
import com.noisefit.data.local.db.CacheErrors.CACHE_ERROR_UNKNOWN
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.NetworkConstants.CALL_TIMEOUT
import com.noisefit.data.remote.NetworkErrors.NETWORK_ERROR
import com.noisefit.data.remote.NetworkErrors.NETWORK_ERROR_205
import com.noisefit.data.remote.NetworkErrors.NETWORK_ERROR_TIMEOUT
import com.noisefit.data.remote.NetworkErrors.NETWORK_ERROR_UNKNOWN
import com.noisefit.data.remote.NetworkErrors.WRONG_CLIENT_TIME_ERROR
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.response.ErrorResponse
import com.noisefit_commans.utils.LOGS
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException
import java.net.ProtocolException


suspend fun <T> safeApiCallFlow(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T?
): Flow<Resource<T>> {

    return flow {
        emit(Resource.Loading(true))

        try {
            // throws TimeoutCancellationException
            withTimeout(CALL_TIMEOUT) {
                emit(Resource.Success(apiCall.invoke()))
                emit(Resource.Loading(false))
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            emit(Resource.Loading(false))
            when (throwable) {
                is TimeoutCancellationException -> {
                    val code = 408 // timeout error code
                    emit(networkError(NETWORK_ERROR_TIMEOUT, code))
                }
                is ProtocolException -> {
                    emit(networkError(NETWORK_ERROR_205, 205))
                }
                is IOException -> {
                    val message = throwable.message
                    if (message == WRONG_CLIENT_TIME_ERROR) {
                        emit(networkError(message, null))
                    } else {
                        emit(networkError(NETWORK_ERROR, null))
                    }

                }
                is HttpException -> {
                    val code = throwable.code()
                    if(code==502){
                        emit(networkError(NETWORK_ERROR, null))
                    }else{
                        val errorResponse = convertErrorBody(throwable)
                        emit(genericError(errorResponse, code))
                    }
                }

                else -> {
                    emit(networkError(NETWORK_ERROR_UNKNOWN, null))
                }
            }
        }

    }.flowOn(dispatcher).catch {

    }

}

//Flow<Resource<T>>
suspend fun <T> safeCacheCall(
    dispatcher: CoroutineDispatcher,
    cacheCall: suspend () -> T?
): Flow<CacheResult<T?>> {
    return flow {
        try {
            // throws TimeoutCancellationException
            withTimeout(CACHE_TIMEOUT) {
                emit(CacheResult.Success(cacheCall.invoke()))
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            when (throwable) {

                is TimeoutCancellationException -> {
                    emit(CacheResult.GenericError(CACHE_ERROR_TIMEOUT))

                }
                else -> {
                    emit(CacheResult.GenericError(CACHE_ERROR_UNKNOWN))
                }
            }
        }
    }.flowOn(dispatcher).catch {

    }
}


private fun genericError(message: ErrorResponse?, statusCode: Int?): Resource.GenericError {
    message?.let {
        LOGS.d(it)
    }
    return Resource.GenericError(message?.errors?.message, statusCode)
}

private fun networkError(message: String?, statusCode: Int?): Resource.NetworkError {


    //return Resource.NetworkError(message, statusCode)
    return Resource.NetworkError(
        com.noisefit_commans.data.ErrorResponse(
            UIComponentType.RetryApiDialog(message)
        ), statusCode
    )
}

//private fun wrongClientTimeError(message: String?, statusCode: Int?): Resource.WrongClientTimeError {
//
//
//    //return Resource.NetworkError(message, statusCode)
//    return Resource.WrongClientTimeError(
//        com.noisefit_commans.data.ErrorResponse(
//            UIComponentType.WrongTimeDialog(message)
//        ), statusCode
//    )
//}

private fun convertErrorBody(throwable: HttpException): ErrorResponse? {
    return try {
        return Gson().fromJson(
            throwable.response()!!.errorBody()!!.string(),
            ErrorResponse::class.java
        )
    } catch (exception: Exception) {
        ErrorResponse(errors = com.noisefit_commans.data.response.ErrorMessage(message = "Something went wrong"))
    }
}


