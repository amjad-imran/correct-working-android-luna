package com.noisefit_commans.data.model

import com.google.gson.annotations.SerializedName

data class SummaryProgressData(
    @SerializedName("stepsSelected") val stepsSelected: Boolean = true,
    @SerializedName("stepsMargin") val stepsMargin: Int = -12,
    @SerializedName("stepsPbSize") val stepsPbSize: Int = 150,
    @SerializedName("calorieSelected") val calorieSelected: Boolean = false,
    @SerializedName("calorieMargin") val calorieMargin: Int = 0,
    @SerializedName("caloriePbSize") val caloriePbSize: Int = 90,
    @SerializedName("distanceSelected") val distanceSelected: Boolean = false,
    @SerializedName("distanceMargin") val distanceMargin: Int = 0,
    @SerializedName("distancePbSize") val distancePbSize: Int = 90,
    )