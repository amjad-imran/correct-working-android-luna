package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName

data class BaseApiResponseChallenge<T>(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: T? = null,
    @SerializedName("message") var message: String? = null,
)