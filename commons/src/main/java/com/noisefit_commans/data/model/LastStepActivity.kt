package com.noisefit_commans.data.model


import com.google.gson.annotations.SerializedName

data class LastStepActivity(
    @SerializedName("steps")
    val steps: Int
)