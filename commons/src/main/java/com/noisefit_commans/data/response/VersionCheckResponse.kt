package com.noisefit_commans.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class VersionCheckResponse(
    val dates: List<String>? = null,
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
    @SerializedName("logs_interval")
    val logsSyncInterval: Int? = 2,
    val helpUpdate: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("cache_ver")
    val cacheVersion: Int? = 1,
    @SerializedName("otaResponse")
    val otaResponse: UpdateResponse? = null
) : Parcelable