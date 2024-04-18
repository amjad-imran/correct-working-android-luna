package com.noisefit.data.repository.implementation

import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.models.weather.WeatherInfo
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun getWeatherData(
        lat: Double,
        long: Double, units: String
    ): Flow<Resource<WeatherInfo?>>
}