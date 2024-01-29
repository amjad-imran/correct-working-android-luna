package com.oreo.data.model.health

import com.google.gson.annotations.SerializedName
import com.oreo.data.model.OActivityListModal

data class OreoActivityModel(
    val date: String,//YYYY-MM-DD,
    val activityScore: ActivityScore? = null,
    val dash_nudges: List<Nudges>? = null,
    val activeCalories: Int? = null,
    val totalCalories: Int? = null,
    val steps: Int? = null,
    val distance: Int? = null,
    @SerializedName("int_nudges")
    val nudges: List<Nudges>?,
    val activityContributors: ActivityContributor? = null,
    val daytimeMovement: DayTimeMovement? = null,
    val workout: List<OActivityListModal>? = null,


)

data class ActivityScore(val value: Int? = null, val level: String? = null,val status:String?=null)
data class ActivityContributor(
    val stayActive: CommonDataModel,
    val moveEveryHour: CommonDataModel,
    val calorieGoal: CommonDataModel,
    val trainingFrequency: CommonDataModel,
    val trainingVolume: CommonDataModel
)

data class DayTimeMovement(
    val endTime: String? = null,
    val startTime: String? = null,
    val movement: List<Int>? = null
)
