package com.noisefit.data.remote.abstraction

import com.noisefit_commans.models.weather.WeatherInfo
import com.noisefit_commans.models.weather.WeatherInfoNew
import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherService {

    //App APIs
    @GET("/data/2.5/weather")
    suspend fun getWeatherData(
        @Query("lat") lat: Double,
        @Query("lon") long: Double,
        @Query("appid") apiKey: String,
        @Query("exclude") exclude: String,
        @Query("units") units: String
    ): WeatherInfoNew

}