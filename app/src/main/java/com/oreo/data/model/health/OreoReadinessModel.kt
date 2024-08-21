package com.oreo.data.model.health

import com.google.gson.annotations.SerializedName

data class OreoReadinessModel(
    val date: String,//YYYY-MM-DD,
    val start_time: String,//YYYY-MM-DD,
    val end_time: String,//YYYY-MM-DD,

    @SerializedName("total_score_impact") val totalScoreImpact: Int? = null,


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
    @SerializedName("sleep_score")
    val sleepScore: CommonDataModel? = null,
    @SerializedName("sleep_regularity")
    val sleepRegularity: CommonDataModel? = null,
    @SerializedName("body_temp")
    val bodyTemp: CommonDataModel? = null,

    val tempBalance: CommonDataModel? = null,

    @SerializedName("contri_ver")
    val contriVersion: Int? = null,//1-> show 7 contributors, 2-> show 9 contributors

    val activityBalance: CommonDataModel? = null,
    val restingHrBalance: CommonDataModel? = null,
    val hrReserve: CommonDataModel? = null,
    val hrBreakUp: UnitDataModelArray? = null,
    val hrvBalance: CommonDataModel? = null,
    val hrv: UnitDataModel? = null,
    val hrvBreakUp: UnitDataModelArray? = null,
    val respiration: UnitDataModel? = null,
    val temperature: UnitDataModelFloat? = null,
    val avg_temp: UnitDataModelTemp? = null,
    val base_temp: Float? = null,
    val temperatureBreakUp: UnitDataModelArrayFloat? = null,
    @SerializedName("readiness_nap_score_impact")
    val readinessNapScoreImpact: Int? = null,
    @SerializedName("no_of_naps")
    val noOfNaps: Int? = null
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
data class UnitDataModelTemp(val value: Float? = null, val deviation: Float? = null)
