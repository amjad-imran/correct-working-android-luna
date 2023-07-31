package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName

data class RequestCountResponse(
    @SerializedName("requests_count") val requestsCount: Int? = 0
)
