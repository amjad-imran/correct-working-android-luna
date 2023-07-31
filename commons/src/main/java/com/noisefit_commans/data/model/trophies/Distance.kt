package com.noisefit_commans.data.model.trophies

import com.google.gson.annotations.SerializedName

data class Distance(@SerializedName("streaks")
                    val streaks: List<StreaksItem>?,
                    @SerializedName("goal")
                    val goal: Int = 0,
                    @SerializedName("better_then")
                    val betterThen: Int = 0,
                    @SerializedName("daily")
                    val daily: List<DailyItem>?,
                    @SerializedName("lifetime")
                    val lifetime: List<DailyItem>?,
                    @SerializedName("achieved")
                    val achieved: Int = 0)