package com.noisefit_commans.data.model.history

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class StepsHistoryResponse(
    val total: Total? = null,
    val avg: Average? = null,
    val max: Max? = null,
    val history_type: String? = null,
    var userName: String? = null,
    val step_activities: ArrayList<StepsHistoryData>? = null
)

data class ActivityMessage(
    val steps: String? = null,
    val distance: String? = null,
    val calories: String? = null,
)

data class Max(
    val maxSteps: Double? = 0.0,
    val maxDistance: Double? = 0.0,
    val maxCalories: Double? = 0.0,
    val maxStepsDate: String? = null,
    val maxDistanceDate: String? = null,
    val maxCaloriesDate: String? = null
)

data class Total(
    val steps: Double? = 0.0,
    val distance: Double? = 0.0,
    val calories: Double? = 0.0
)

data class Average(
    val steps: Double? = 0.0,
    val distance: Double? = 0.0,
    val calories: Double? = 0.0
)

@Parcelize
data class StepsHistoryData(
    var month: Int? = null,
    var date: String? = null,
    var steps: Long? = null,
    var distance: Long? = null,
    var calories: Long? = null,
    var active_time: Int? = null,
    var hour_of_the_day: Int? = null,
    var hourly_breakup: List<StepsHistoryData>? = null
) : Parcelable
