package com.oreo.data.model.lifeos.dashModels

import android.os.Parcelable
import com.noisefit_commans.models.SleepData
import com.oreo.data.model.TrendsValues
import com.oreo.data.model.health.SleepMovementBreakup
import kotlinx.parcelize.Parcelize

@Parcelize
data class InsightItemResponseModel(
    val description: String? = null,
    val graph: InsightGraph? = null,
    val graph_type: String? = null,
    val related_suggested_questions: List<String>? = null,
    val suggestions: String? = null,
    val title: String? = null,
    val insightIcon: String? = null,
) : Parcelable


@Parcelize
data class InsightGraph(
    val breakup_int: List<Int>? = null,
    val breakup_sleep: List<SleepData.SleepDataBreakup>? = null,
    val sleep_movement: List<SleepMovementBreakup>? = null,
    val trends_breakup: List<TrendsValues>?=null
) : Parcelable