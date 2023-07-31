package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName

data class WorldClockResponse(
    val clocks: List<WorldClockNetwork>
)

data class WorldClockNetwork(
    @SerializedName("id") val id: Int,
    @SerializedName("city") val city: String,
    @SerializedName("signed_value") val signedValue: Int
)