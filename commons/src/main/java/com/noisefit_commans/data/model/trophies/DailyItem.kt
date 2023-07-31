package com.noisefit_commans.data.model.trophies

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DailyItem(
    @SerializedName("is_steps_notify")
    val isStepsNotify: Int = 0,
    @SerializedName("image")
    val image: String = "",
    @SerializedName("is_steps_achieved")
    var isStepsAchieved: Int = 0,
    @SerializedName("is_steps_collect")
    var isStepsCollect: Int = 0,
    @SerializedName("title_for_km")
    val titleForKm: String = "",
    @SerializedName("title_for_mile")
    val titleForMile: String = "",
    @SerializedName("distance")
    val distance: Int = 0,
    @SerializedName("is_distance_notify")
    val isDistanceNotify: Int = 0,
    @SerializedName("is_distance_achieved")
    val isDistanceAchieved: Int = 0,
    @SerializedName("is_distance_collect")
    var isDistanceCollect: Int = 0,
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("badge_type")
    val badgeType: String = "",
    @SerializedName("steps")
    val steps: Int = 0,
    @SerializedName("steps_user_badge_id")
    val stepsUserBadgeId: Int = 0,
    @SerializedName("distance_user_badge_id")
    val distanceUserBadgeId: Int = 0
) : Parcelable {

    fun getDistanceKm(): Int {
        return distance / 1000
    }
}