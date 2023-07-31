package com.noisefit.data.model

import com.google.gson.annotations.SerializedName

data class SubmitAnswerDataModel(
    val points: Int,
    val message: String,
    @SerializedName("elapsed_time")
    val elapsedTime: Long,
)