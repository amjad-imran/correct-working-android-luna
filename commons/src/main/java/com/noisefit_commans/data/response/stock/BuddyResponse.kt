package com.noisefit_commans.data.response.stock

import com.google.gson.annotations.SerializedName

class BuddyResponse<T>(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("users")
    var data: T? = null,
    @SerializedName("error")
    val error: String? = null,
    @SerializedName("success")
    val success: Boolean
)