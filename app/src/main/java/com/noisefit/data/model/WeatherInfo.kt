package com.noisefit.data.model

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.models.weather.Current
import com.noisefit_commans.models.weather.DailyItem
import com.noisefit_commans.models.weather.HourlyItem

data class WeatherInfo(
    @SerializedName("current")
    val current: Current,
    @SerializedName("timezone")
    val timezone: String = "",
    @SerializedName("timezone_offset")
    val timezoneOffset: Int = 0,
    @SerializedName("daily")
    val daily: List<DailyItem>?,
    @SerializedName("hourly")
    val hourly: List<HourlyItem>?,
    @SerializedName("lon")
    val lon: Double = 0.0,
    @SerializedName("lat")
    val lat: Double = 0.0
)