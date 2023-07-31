package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName

data class BaseApiResponseData<T>(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("data")
    var data: T? = null,
    @SerializedName("error")
    val error: String? = null
)