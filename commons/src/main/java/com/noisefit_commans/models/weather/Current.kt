package com.noisefit_commans.models.weather

import com.google.gson.annotations.SerializedName

data class Current(
    @SerializedName("temp")
    val temp: Double = 0.0,
    @SerializedName("pressure")
    val pressure: Double = 0.0,
    @SerializedName("weather")
    val weather: List<WeatherItem>?,
    @SerializedName("humidity")
    val humidity: Double = 0.0,
    @SerializedName("wind_speed")
    val windSpeed: Double = 0.0,
    @SerializedName("dt")
    val dt: Long = 0,
    @SerializedName("sunrise")
    val sunrise: Long = 0,
    @SerializedName("sunset")
    val sunset: Long = 0,
    @SerializedName("uvi")
    val uvi: Double = 0.0
)