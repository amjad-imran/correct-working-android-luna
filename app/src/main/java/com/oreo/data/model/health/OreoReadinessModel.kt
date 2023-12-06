package com.oreo.data.model.health

import com.google.gson.annotations.SerializedName

data class OreoReadinessModel(
    val date: String,//YYYY-MM-DD,
    val start_time: String,//YYYY-MM-DD,
    val end_time: String,//YYYY-MM-DD,
    @SerializedName("readiness_score")
    val readinessScore: CommonDataModel? = null,
    val restingHr: UnitDataModel? = null,
    val totalSleep: CommonDataModel? = null,
    @SerializedName("int_nudges")
    val nudges: List<Nudges>?,
    @SerializedName("dash_nudges")
    val dashNudges: List<Nudges>?,
    val sleepBalance: CommonDataModel? = null,
    val recoveryIndex: CommonDataModel? = null,
    @SerializedName("activity_score")
    val activityScore: CommonDataModel? = null,
    val activityBalance: CommonDataModel? = null,
    val restingHrBalance: CommonDataModel? = null,
    val hrReserve: CommonDataModel? = null,
    val hrBreakUp: UnitDataModelArray? = null,
    val hrvBalance: CommonDataModel? = null,
    val hrv: UnitDataModel? = null,
    val hrvBreakUp: UnitDataModelArray? = null,
    val respiration: UnitDataModel? = null,
    val temperature: UnitDataModelFloat? = null,
    val temperatureBreakUp: UnitDataModelArrayFloat? = null


)

data class UnitDataModel(val value: Int)
data class UnitDataModelArray(
    val value: List<Int>?,
    val avg: Int? = null,
    val low: Int? = null,
    val max: Int? = null
)
data class UnitDataModelArrayFloat(
    val value: List<Float>?,
    val avg: Float? = null,
    val max: Float? = null
)

data class UnitDataModelFloat(val value: Float)
