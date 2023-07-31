package com.noisefit_commans.data.model.trophies

import com.google.gson.annotations.SerializedName

data class TrophyBadge(@SerializedName("date")
                       val date: String = "",
                       @SerializedName("badge")
                       val badge: Badge,
                       @SerializedName("difference_in_days")
                       val differenceInDays: Int = 0,
                       @SerializedName("activity_type")
                       val activityType: String = "",
                       @SerializedName("badge_id")
                       val badgeId: Int = 0,
                       @SerializedName("created_at")
                       val createdAt: String = "",
                       @SerializedName("badge_type")
                       val badgeType: String = "")