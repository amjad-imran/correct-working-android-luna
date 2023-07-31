package com.noisefit_commans.data.model.history

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


data class BodyTempHistoryResponse(
    @SerializedName("history_type") val history_type: String? = null,
    @SerializedName("temp") val history: ArrayList<BodyTempHistory>? = null,
    @SerializedName("cumulative") val cumulative: DodyTempCumulative? = null
)

@Parcelize
data class BodyTempHistory(
    @SerializedName("history_type") val history_type: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("time") val time: String? = null,
    @SerializedName("month") val month: Int? = null,
    @SerializedName("count") val count: Float? = null,
    @SerializedName("value") val value: Float? = null,
    @SerializedName("max_count") val maxCount: Float? = null,
    @SerializedName("min_count") val minCount: Float? = null,
    @SerializedName("no_of_records") val noOfRecords: Int? = null,
    @SerializedName("hourly_break_up") var hourly_breakup: List<BodyTempHistory>? = null
) : Parcelable

data class DodyTempCumulative(
    @SerializedName("count") val count: Float? = null,
    @SerializedName("max") val max: Float? = null,
    @SerializedName("min") val min: Float? = null
)