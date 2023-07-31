package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName

data class BaseApiResponseActivity(
    @SerializedName("success")
    val success: Boolean? = null
)