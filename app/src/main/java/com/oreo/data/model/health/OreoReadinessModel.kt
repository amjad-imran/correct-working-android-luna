package com.oreo.data.model.health

import com.google.gson.annotations.SerializedName

data class OreoReadinessModel(
    val date: String,//YYYY-MM-DD,
    @SerializedName("readiness_score")
    val readinessScore: CommonDataModel? = null,
    val restingHr: UnitDataModel? = null,
    val totalSleep: CommonDataModel? = null,
    val nudges: List<Nudges>?,
    val sleepBalance: CommonDataModel? = null,
    val recoveryIndex: CommonDataModel? = null,
    @SerializedName("activity_score")
    val activityScore: CommonDataModel? = null,
    val activityBalance: CommonDataModel? = null,
    val restingHrBalance: CommonDataModel? = null,
    val hrBreakUp: List<Int>? = null,
    val hrvBalance: CommonDataModel? = null,
    val hrv: UnitDataModel? = null,
    val hrvBreakUp: List<Int>? = null,
    val respiration: UnitDataModel? = null,
    val temperature: UnitDataModelFloat? = null,
    val temperatureBreakUp: List<Float>? = null


)

data class UnitDataModel(val value: Int)
data class UnitDataModelFloat(val value: Float)
