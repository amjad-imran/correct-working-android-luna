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
    var avg: TrendAverage? = null
) : Parcelable

@Parcelize
data class TrendsValues(
    var date: String? = null,
    var value1: Float? = null,
    var value2: Float? = null,

    //For sleep timing
    var master_start_time: String? = null,
    var master_end_time: String? = null,

    //For daily
    var start_time: String? = null,
    var end_time: String? = null,

    //For daily
    val avg: Float? = null,
    val breakup: List<Float>? = null
) : Parcelable

@Parcelize
data class TrendAverage(
    val avg: Float? = null,
    val avg_need: Float? = null,
    val avg_hour: Float? = null,
    val nudge: String? = null,
    val percent: Int? = null,
    val percent_hour: Int? = null,
    val percent_need: Int? = null,
    val status: String? = null
) : Parcelable


@Parcelize
data class TrendsGraphData(
    var data: List<TrendsValues>? = null,
    var contributorType: SleepInternalLaunchState? = null,
    var selectedPeriod: InternalSelectedPeriod = InternalSelectedPeriod.DAY
) : Parcelable