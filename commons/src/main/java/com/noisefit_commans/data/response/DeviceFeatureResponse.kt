package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.data.model.DeviceFeatures

data class DeviceFeatureResponse(
    @SerializedName("device_features") val deviceFeatures: DeviceFeatures
)

data class WatchTokenResponse(
    @SerializedName("ring_token")
    val ringToken: String? = null
)
