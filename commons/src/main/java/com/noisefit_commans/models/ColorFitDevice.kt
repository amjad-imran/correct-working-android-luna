package com.noisefit_commans.models

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class ColorFitDevice(
    @SerializedName("bluetooth_name") var bluetoothName: String? = null,
    @SerializedName("address") var address: String? = null,
    @SerializedName("rssi") var rssi: Int = 0,
    @SerializedName("type") var deviceType: String? = null,
    @SerializedName("device_id") var deviceId: Int = 0,
    @SerializedName("id") var id: Int = 0,
    @SerializedName("is") var mIs: Int = 0,
    @SerializedName("len") var len: Int = 0,
    @SerializedName("is_in_duff") var isInDuffMode: Boolean = false,
    @SerializedName("bluetooth_name_pattern") val namePattern: String = "",
    @SerializedName("matching_type") val matchingType: String = "",
    @SerializedName("isSupportHeadset") var isSupportHeadset: Boolean = false,
    @SerializedName("isBind") var isBind: Boolean = false,
    @SerializedName("headsetMac") var headsetMac: String = "",
    @SerializedName("url") val url: String = "",
    @SerializedName("mcuPlatform") val mcuPlatform: String? = null,
    @SerializedName("userId") var userId: String = "",
    @SerializedName("watchToken") var watchToken: String = "",
    @SerializedName("ringInfo") var ringInfo: RingInfo? = null
) : Parcelable {

    fun getDeviceDetails(): String {
        return Gson().toJson(this)
    }

    fun getDeviceLogInfo(): String {
        return "$bluetoothName | $deviceType | $deviceId"
    }
}

@Parcelize
data class WatchFirmwareDetails(
    val version: Int = 0,
    val firmwareId: Int = 0
) : Parcelable

@Parcelize
data class RingInfo(
    @SerializedName("size")
    val size: Int? = null,
    @SerializedName("color")
    val color: String? = null,
    @SerializedName("monthOfProduction")
    val monthOfProduction: Int? = null,
    @SerializedName("yearOfProduction")
    val yearOfProduction: Int? = null,
    @SerializedName("image")
    val image: String? = null,
    @SerializedName("versionNumber")
    val versionNumber: Int? = null,
    @SerializedName("serialNoRaw")
    val serialNoRaw: String? = null,

) : Parcelable