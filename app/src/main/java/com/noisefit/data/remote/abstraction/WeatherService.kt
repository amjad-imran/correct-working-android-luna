package com.noisefit.data.remote.abstraction

import com.noisefit_commans.models.weather.WeatherInfo
import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherService {

    //App APIs
    @GET("/data/2.5/onecall?units=metric&exclude=minutely")
    suspend fun getWeatherData(
        @Query("lat") lat: Double,
        @Query("lon") long: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): WeatherInfo

}