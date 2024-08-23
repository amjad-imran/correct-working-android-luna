package com.oreo.data.model

import com.google.gson.annotations.SerializedName
import com.oreo.data.model.AppUpdateModel
import com.oreo.data.model.OtaUpdateModel

data class UpdateResponseV2(
    @SerializedName("app_version")
    val appVersion: AppUpdateModel?,
    @SerializedName("firmware_version")
    val firmwareVersion: OtaUpdateModel?,
    @SerializedName("is_blacklisted_ring")
    val isBlacklistedRing: Boolean?,
)
