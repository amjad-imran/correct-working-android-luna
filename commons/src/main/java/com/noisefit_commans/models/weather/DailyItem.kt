package com.noisefit_commans.models.weather

import com.google.gson.annotations.SerializedName

data class DailyItem(
    @SerializedName("temp")
    val temp: Temp,
    @SerializedName("pressure")
    val pressure: Double = 0.0,
    @SerializedName("weather")
    val weather: List<WeatherItem>?,
    @SerializedName("humidity")
    val humidity: Double = 0.0,
    @SerializedName("dt")
    val dt: Long = 0,
)