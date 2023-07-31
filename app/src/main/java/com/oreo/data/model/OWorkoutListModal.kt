package com.oreo.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import kotlinx.parcelize.Parcelize

@Parcelize
data class OWorkoutListModal(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("icon_url") val iconUrl: String? = null,
    @SerializedName("activity_type") val activityType: String? = null,
    @SerializedName("low") val lowIntensity: Float? = null,
    @SerializedName("medium") val mediumIntensity: Float? = null,
    @SerializedName("high") val highIntensity: Float? = null,
) : Parcelable{
    fun getFormattedActivityName(): String {
        val activityName =  activityType ?: return ""
        val actNameTemp = activityName.replace("_", " ")
        return actNameTemp.capitalizeWords()
    }
}


@Parcelize
data class OActivityListModal(
    @Transient var isHeader: Boolean = false,
    @SerializedName("date") var date: String? = null,
    @SerializedName("id") val id: String? = null,

    @SerializedName("duration") val duration: Long? = null,
    @SerializedName("calories") val calories: String? = null,
    @SerializedName("activity_type") val activityType: String? = null,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null,
    @SerializedName("intensity") val intensity: String? = null,
    @SerializedName("created_date") val createdDate: String? = null,
) : Parcelable{
    fun getFormattedActivityName(): String {
        val activityName =  activityType ?: return ""
        val actNameTemp = activityName.replace("_", " ")
        return actNameTemp.capitalizeWords()
    }
}