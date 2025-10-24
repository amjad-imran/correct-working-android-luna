package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName

data class BaseApiResponse<T>(
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    var data: T? = null,
    @SerializedName("error")
    val error:ErrorMessage? = null,
)

data class ErrorMessage(
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("statusCode")
    val statusCode: Int? = null,
    @SerializedName("timer")
    val timer: Int? = null,
    val otpExist: Boolean? = null,
)