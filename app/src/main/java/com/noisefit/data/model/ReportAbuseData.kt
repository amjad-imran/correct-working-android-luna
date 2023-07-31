package com.noisefit.data.model

import com.google.gson.annotations.SerializedName

data class ReportAbuseData(
    @SerializedName("title")
    var title: String,
    @SerializedName("id")
    var id: Int
)