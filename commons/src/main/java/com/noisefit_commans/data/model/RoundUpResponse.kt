package com.noisefit_commans.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RoundUpResponse(
    @SerializedName("name") val name: String,
    @SerializedName("steps") val steps: SummaryData?,
    @SerializedName("workout") val workout: SummaryData?,
    @SerializedName("calories") val calories: SummaryData?,
    @SerializedName("challenges") val challenges: SummaryData?,
    @SerializedName("sleep") val sleep: SummaryData?,
    @SerializedName("overall") val overall: SummaryData?,
    @SerializedName("roundUpEnd") val roundUpEnd: SummaryData?
) : Serializable

data class SummaryData(
    @SerializedName("title") val title: String,
    @SerializedName("totalMsg") val totalMsg: String,
    @SerializedName("totalValue") val totalValue: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("status") val status: String,
    @SerializedName("endingTitle") val endingTitle: String,
    @SerializedName("endingSubtitle") val endingSubtitle: String
):Serializable