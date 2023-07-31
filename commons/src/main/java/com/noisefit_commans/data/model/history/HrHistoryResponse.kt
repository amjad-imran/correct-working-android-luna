package com.noisefit_commans.data.model.history

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class HrHistoryResponse(
    val history_type : String? = null,
    val cumulative : HrCumulative? = null,
    val heart_rates : List<HrBreakup>? = null,
):Parcelable


@Parcelize
data class HrBreakup (
    @SerializedName("hour_of_the_day") val hour_of_the_day : Int?=0,
    @SerializedName("date") val date : String? = null,
    @SerializedName("avg") val avg : Int?=0,
    @SerializedName("max") val max : Int?=0,
    @SerializedName("hourly_break_up") val hourlyBreakUp : List<HrBreakup>?=null,
    @SerializedName("resting_hr") val restingHr : Int?=0,
    @SerializedName("min") val min : Int?=0):Parcelable

@Parcelize
data class HrCumulative (
    val avg : Int,
    val max : Int,
    val restingHr : Int,
    val min : Int
):Parcelable