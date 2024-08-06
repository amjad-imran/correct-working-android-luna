package com.oreo.data.model

import android.os.Parcelable
import androidx.lifecycle.LiveData
import com.google.gson.annotations.SerializedName
import com.oreo.ui.sleep2.internal.InternalSelectedPeriod
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState
import kotlinx.parcelize.Parcelize

@Parcelize
data class OSleepInternalTrendsDataModel(
    var data: List<TrendsValues>? = null,
    var nudge: String? = null,
    @SerializedName("day_avg")
    var dayAvg: TrendAverage? = null,
    @SerializedName("week_avg")
    var weekAvg: TrendAverage? = null,
    @SerializedName("month_avg")
    var monthAvg: TrendAverage? = null,
) : Parcelable

@Parcelize
data class TrendsValues(
    var date: String? = null,
    var value1: Float? = null,
    var value2: Float? = null,
) : Parcelable

@Parcelize
data class TrendAverage(
    val avg: Double? = null,
    val avg_need: Double? = null,
    val avg_hour: Double? = null,
    val nudge: String? = null,
    val percent: Int? = null,
    val status: String? = null
) : Parcelable


@Parcelize
data class TrendsGraphData(
    var data: List<TrendsValues>? = null,
    var contributorType: SleepInternalLaunchState? = null,
    var selectedPeriod: InternalSelectedPeriod = InternalSelectedPeriod.DAY
) : Parcelable