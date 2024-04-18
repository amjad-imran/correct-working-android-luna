package com.noisefit_commans.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import kotlinx.parcelize.Parcelize

@Parcelize
data class OWorkoutListModal(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("ring_id") val ringId: Int? = null,
    @SerializedName("icon_url") val iconUrl: String? = null,
    @SerializedName("activity_type") val activityType: String? = null,
    @SerializedName("low") val lowIntensity: Float? = null,
    @SerializedName("medium") val mediumIntensity: Float? = null,
    @SerializedName("high") val highIntensity: Float? = null,
    @SerializedName("gps") val isGpsRequired: Int = 0,
    var isTempSet: Boolean = false
) : Parcelable {
    fun getFormattedActivityName(): String {
        val activityName = activityType ?: return ""
        val actNameTemp = activityName.replace("_", " ")
        return actNameTemp.capitalizeWords()
    }
}