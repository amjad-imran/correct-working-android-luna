package com.oreo.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.noisefit_commans.models.LocationDataNetwork
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import com.oreo.data.model.health.Nudges
import kotlinx.parcelize.Parcelize

data class OWorkoutDetailsResponseModel(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("device_id") val deviceId: String,
    val duration: Long? = null,
    @SerializedName("duration_seconds") val durationSeconds: Long? = null,
    val calories: Int? = null,
    val hrArray: List<Int>? = null,
    val steps: Int? = null,
    @SerializedName("activity_type") val activityType: String? = null,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null,
    val intensity: String? = null,
    @SerializedName("created_date") val createdDate: String? = null,
    val type: String? = null,
    val date: String? = null,
    @SerializedName("hr_avg") val hrAvg: Int? = null,
    @SerializedName("hr_low") val hrLow: Int? = null,
    @SerializedName("hr_max") val hrMax: Int? = null,
    @SerializedName("distance") val distance: Long? = null,
    @SerializedName("cadence") val cadence: Long? = null,
    @SerializedName("recovery_time") val recoveryTime: Long? = null,
    @SerializedName("icon_url") val iconUrl: String? = null,
    @SerializedName("gps_distance") val gpsDistance: Long? = null,
    val nudges: List<Nudges>? = null,
    @SerializedName("daytime_movement") val movement: List<Int>? = null,

    @SerializedName("data_type") val dataType: String? = null,//distance/calories, default -> calories
    @SerializedName("data_priority") val dataPriority: String? = null,//ring/app. default-> ring
    @SerializedName("start_location") val startLocation: String? = null,//ring/app. default-> ring

    val location: List<LocationDataNetwork>? = null,
    val weather: Weather? = null
) {
    fun getFormattedActivityName(): String {
        val activityName = activityType ?: return ""
        val actNameTemp = activityName.replace("_", " ")
        return actNameTemp.capitalizeWords()
    }
}

data class Weather(val temp: Int? = null, val status: Int? = null)