package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.models.ColorFitNetworkDevice

data class DeviceListResponse(
    val devices: List<ColorFitNetworkDevice>,
    @SerializedName("ring_info")
    val ringInfo: List<RingInfoResponse>?
)

data class RingInfoResponse(
    val color: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("image_url_2")
    val imageUrl2: String? = null,
    @SerializedName("image_url_3")
    val imageUrl3: String? = null,
    @SerializedName("charger_url")
    val chargerRingUrl: String? = null,
    val mapping: List<RingInfoMappingResponse> = ArrayList()
)

data class RingInfoMappingResponse(
    val code: Int? = null,
    val size: Int? = null
)
