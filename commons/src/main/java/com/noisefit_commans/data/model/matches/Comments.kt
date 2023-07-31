package com.noisefit_commans.data.model.matches

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Comments(
    @SerializedName("Live")
    @Expose
    val live: List<Live>? = null
)

data class Live(
    @SerializedName("innings")
    @Expose
    val innings: String? = null,
    @SerializedName("balls")
    @Expose
    val balls: String? = null,
    @SerializedName("overs")
    @Expose
    val overs: String? = null,
    @SerializedName("ended")
    @Expose
    val ended: String? = null,
    @SerializedName("runs")
    @Expose
    val runs: String? = null,
    @SerializedName("post")
    @Expose
    val post: String? = null,
)