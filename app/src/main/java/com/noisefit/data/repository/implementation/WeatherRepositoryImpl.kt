package com.noisefit.data.repository.implementation

import com.noisefit.data.remote.abstraction.WeatherService
import com.noisefit_commans.models.weather.WeatherInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WeatherRepositoryImpl(
    private val weatherDataSource: WeatherService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : WeatherRepository {
    override suspend fun getWeatherData(
        lat: Double,
        long: Double,
        units: String,
    ): Flow<WeatherInfo?> {
        return flow {
            weatherDataSource.getWeatherData(
                lat,
                long,
                "5985eee1b043d8f48edee1f4d83e076b",
                "minutely,hourly,daily",
                units
            )
        }
    }
}
