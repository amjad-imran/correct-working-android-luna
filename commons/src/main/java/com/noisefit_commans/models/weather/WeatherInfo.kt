package com.noisefit_commans.models.weather

import com.google.gson.annotations.SerializedName


data class WeatherInfo(
    val current: CurrentInfo? = null
)

data class CurrentInfo(
    @SerializedName("temp_c") val tempC: Double? = null,
    val condition: WeatherCondition? = null,
)

data class WeatherCondition(
    val text: String? = null,
    val code: Int? = null,
)