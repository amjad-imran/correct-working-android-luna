package com.oreo.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class OSleepInternalTrendsDataModel(
    var data: List<TrendsValues>? = null,
    var nudge: String? = null
) : Parcelable

@Parcelize
data class TrendsValues(
    var date: String? = null,
    var value1: Int? = null,
    var value2: Int? = null,
) : Parcelable


@Parcelize
data class TrendsGraphData(
    var data: List<TrendsValues>? = null,
) : Parcelable