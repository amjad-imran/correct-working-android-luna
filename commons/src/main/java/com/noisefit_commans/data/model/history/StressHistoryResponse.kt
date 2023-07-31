package com.noisefit_commans.data.model.history

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class StressHistoryResponse(
    @SerializedName("history_type") val history_type: String? = null,
    @SerializedName("stress") val history: ArrayList<StressHistory>? = null,
    @SerializedName("cumulative") val cumulative: StressCumulative? = null,

    )

@Parcelize
data class StressHistory(
    @SerializedName("history_type") val history_type: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("time") val time: String? = null,
    @SerializedName("month") val month: Int? = null,
    @SerializedName("count") val count: Int? = null,
    @SerializedName("value") val value: Int? = null,
    @SerializedName("max_count") val maxCount: Int? = null,
    @SerializedName("min_count") val minCount: Int? = null,
    @SerializedName("no_of_records") val noOfRecords: Int? = null,
    @SerializedName("hourly_break_up") var hourly_breakup: List<StressHistory>? = null
) : Parcelable


data class StressCumulative(
    @SerializedName("count") val count: Int? = null,
    @SerializedName("max") val max: Int? = null,
    @SerializedName("min") val min: Int? = null
)