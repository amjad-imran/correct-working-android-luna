package com.noisefit_commans.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class VersionCheckResponse(
    @SerializedName("upgrade_type")
    val upgradeType: String? = null,
    @SerializedName("maintenance_mode")
    val maintenanceMode: Boolean? = null,
    @SerializedName("current_version_name")
    val currentVersionName: String? = null,
    @SerializedName("current_version")
    val currentVersion: Int? = null,
    @SerializedName("reset_interval")
    val resetInterval: Int? = 24,
    val calendarYears: Int? = 2,
    val bannerTime: String? = null,
    val summaryUpdate: String? = null,
    val workoutImageUpdate: String? = null,
    val helpUpdate: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("testMode")
    val testMode: String? = null,
    @SerializedName("otaResponse")
    val otaResponse: UpdateResponse? = null
) : Parcelable