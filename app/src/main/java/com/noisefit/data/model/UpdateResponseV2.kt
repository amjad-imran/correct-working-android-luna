package com.noisefit.data.model

import com.google.gson.annotations.SerializedName

data class UpdateResponseV2(
    @SerializedName("app_version")
    val appVersion: AppUpdateModel?,
    @SerializedName("firmware_version")
    val firmwareVersion: OtaUpdateModel?,
)
