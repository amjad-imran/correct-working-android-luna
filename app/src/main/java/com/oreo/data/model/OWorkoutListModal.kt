package com.oreo.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import kotlinx.parcelize.Parcelize


@Parcelize
data class OActivityListModal(
    @Transient var isHeader: Boolean = false,
    @SerializedName("date") var date: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("duration") val duration: Long? = null,
    @SerializedName("calories") val calories: String? = null,
    @SerializedName("activity_type") val activityType: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null,
    @SerializedName("intensity") val intensity: String? = null,
    @SerializedName("created_date") val createdDate: String? = null,
    @SerializedName("icon_url") val iconUrl: String? = null
) : Parcelable {
    fun getFormattedActivityName(): String {
        val activityName = activityType ?: return ""
        val actNameTemp = activityName.replace("_", " ")
        return actNameTemp.capitalizeWords()
    }

    fun getDisplayVersionType(): Int {
        return if (type.equals(
                WorkoutTypes.USERWORKOUT.name,
                true
            ) || type.equals(WorkoutTypes.MANUAL.name, true)
            || type.equals(WorkoutTypes.AUTO.name, true)
            || type.equals(WorkoutTypes.AUTOMANUAL.name, true)
        ) {
            2
        } else {
            1
        }
    }
}

enum class WorkoutTypes {
    GOOGLE, APPLE, USERWORKOUT, MANUAL, AUTO, AUTOMANUAL
}