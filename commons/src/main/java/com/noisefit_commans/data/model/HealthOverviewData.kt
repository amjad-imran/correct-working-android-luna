package com.noisefit_commans.data.model

import com.google.gson.annotations.SerializedName

data class HealthOverviewData(
    @SerializedName("healthOverview") var healthOverviewList: ArrayList<HealthOverview>? = null,
    @SerializedName("activeMinute") var activeMinute: Int? = 0,
    @SerializedName("activeMinuteGoal") var activeMinuteGoal: Int? = 0,
    @SerializedName("activeMinuteGoalProgress") var activeMinuteGoalProgress: Float? = null,
    @SerializedName("distance") var distance: String? = "0",
    @SerializedName("distanceGoal") var distanceGoal: String? = "0",
    @SerializedName("distanceGoalProgress") var distanceGoalProgress: Float? = null,
    @SerializedName("calories") var calories: Int? = 0,
    @SerializedName("caloriesGoal") var caloriesGoal: Int? = 0,
    @SerializedName("caloriesGoalProgress") var caloriesGoalProgress: Float? = null,
    @SerializedName("steps") var steps: Int? = 0,
    @SerializedName("stepsGoal") var stepsGoal: Int? = 0,
    @SerializedName("stepsGoalProgress") var stepsGoalProgress: Float? = null,
    @SerializedName("stand") var stand: Int? = 0,
    @SerializedName("standGoal") var standGoal: Int? = 0,
    @SerializedName("standGoalProgress") var standGoalProgress: Float? = null,
)