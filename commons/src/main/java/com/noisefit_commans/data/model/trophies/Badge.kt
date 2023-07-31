package com.noisefit_commans.data.model.trophies

import com.google.gson.annotations.SerializedName

data class Badge(@SerializedName("image")
                 val image: String = "",
                 @SerializedName("code")
                 val code: String = "",
                 @SerializedName("distance")
                 val distance: Int = 0,
                 @SerializedName("id")
                 val id: Int = 0,
                 @SerializedName("title_for_km")
                 val titleForKm: String = "",
                 @SerializedName("title_for_mile")
                 val titleForMile: String = "",
                 @SerializedName("badge_type")
                 val badgeType: String = "",
                 @SerializedName("display_name")
                 val displayName: String = "",
                 @SerializedName("steps")
                 val steps: Int = 0){
    fun getDistanceKm(): Int {
        return distance / 1000
    }
}