package com.noisefit_commans.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class ColorFitNetworkDevice(
    @SerializedName("display_name") var bluetoothName: String? = null,
    @SerializedName("device_type") var deviceType: String? = null,
    @SerializedName("device_id") var deviceId: Int = 0,
    @SerializedName("id") var id: Int = 0,
    @SerializedName("bluetooth_name_pattern") val namePattern: String = "",
    @SerializedName("image_url") val url: String = "",
    @SerializedName("matching_type") val matchingType: String = ""
) : Parcelable
