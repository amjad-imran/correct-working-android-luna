package com.noisefit_commans.data.model.matches.score

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MatchesScore(
    @SerializedName("success")
    @Expose
    val success: Int?=null,
    @SerializedName("result")
    @Expose
    val result: List<MatchesResultScore>? = null)