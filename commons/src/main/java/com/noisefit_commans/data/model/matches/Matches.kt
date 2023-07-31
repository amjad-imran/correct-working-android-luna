package com.noisefit_commans.data.model.matches

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Matches(
    @SerializedName("success")
    @Expose
    val success: Int? = null,
    @SerializedName("result")
    @Expose
    val result: List<MatchResult>? = null,
    var lastSync: Long = 0
)