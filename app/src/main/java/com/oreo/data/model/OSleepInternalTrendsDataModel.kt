package com.oreo.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class OSleepInternalTrendsDataModel(
    @SerializedName("values")
    var values: List<TrendsValues>? = null
):Parcelable
@Parcelize
data class TrendsValues(
    @SerializedName("date")
    var date: String? = null,
    @SerializedName("value")
    var value: Int? = null,
    @SerializedName("value2")
    var value2: Int? = null,
    @SerializedName("nudge")
    var nudge: String? = null
):Parcelable