package com.noisefit.data.repository.implementation

import com.noisefit.data.remote.NetworkConstants
import com.noisefit.data.remote.NetworkErrors
import com.noisefit.data.remote.abstraction.WeatherService
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.models.weather.WeatherInfo
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

class WeatherRepositoryImpl(
    private val weatherDataSource: WeatherService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : WeatherRepository {
    override suspend fun getWeatherData(
        lat: Double,
        long: Double,
        units: String,
    ): Flow<Resource<WeatherInfo?>> {
        return safeApiCallFlowWeather(dispatcher) {
            weatherDataSource.getWeatherData(
                "$lat,$long",
                1,
                "no",
                "no",
                "13fe5f535ff8446f89f71919240310"
            )
        }
    }
}

suspend fun <T> safeApiCallFlowWeather(
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
    }.flowOn(dispatcher).catch { }
}

private fun networkError(): Resource.NetworkError {
    return Resource.NetworkError(
        com.noisefit_commans.data.ErrorResponse(
            UIComponentType.RetryApiDialog(NetworkErrors.NETWORK_ERROR_UNKNOWN)
        ), 400
    )
}
