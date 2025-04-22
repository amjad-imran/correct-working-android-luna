package com.noisefit.data.remote.abstraction

import com.noisefit_commans.models.weather.WeatherInfo
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    //App APIs
    @GET("/v1/current.json")
    suspend fun getWeatherData(
        @Query("q") query: String,//lat,long
        @Query("days") days: Int,//1
        @Query("aqi") aqi: String,
        @Query("alerts") alerts: String,
        @Query("key") key: String,
    ): WeatherInfo

}