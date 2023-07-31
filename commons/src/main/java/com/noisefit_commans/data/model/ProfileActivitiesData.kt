package com.noisefit_commans.data.model

import android.os.Parcelable
import com.github.mikephil.charting.data.BarEntry
import com.noisefit_commans.data.enums.HealthOverViewHistoryType
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileActivitiesData(
    val title: String,
    val total: String,
    val max: String,
    val avg: String,
    val type: HealthOverViewHistoryType,
    val betterThen: String? = null,
    val compareName: String,
    val compareDate: String,
    val graphData: Triple<ArrayList<BarEntry>, ArrayList<Int>, ArrayList<MarkerEntry>>?,
    val weeklyData: ArrayList<String>
) : Parcelable