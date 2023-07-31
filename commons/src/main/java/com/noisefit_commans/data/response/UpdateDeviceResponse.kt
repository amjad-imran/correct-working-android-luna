package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.data.model.DeviceFeatures
import com.noisefit_commans.data.model.Token

data class UpdateDeviceResponse(
    @SerializedName("user_device") val userDevice: UserDevice,
    @SerializedName("tokens") val tokens: Token
)

data class UserDevice(

    //TODO add token
    @SerializedName("device_features") val deviceFeatures: DeviceFeatures?
)