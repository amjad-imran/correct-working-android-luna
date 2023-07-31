package com.noisefit_commans.models

import com.google.gson.annotations.SerializedName

data class CFP2WatchFaces(@SerializedName("data") val data: List<WatchFace>) {
    class WatchFace(
        @SerializedName("id") val id: Int,
        @SerializedName("imageName") val imageName: String,
        @SerializedName("publishStatus") val publishStatus: Boolean,
        @SerializedName("image") val image: String,
        @SerializedName("dialPlateNames") val dialPlateNames: List<DialPlateName>
    ) {
        class DialPlateName(
            @SerializedName("languageCode") val languageCode: Int,
            @SerializedName("name") val name: String,
            @SerializedName("summary") val summary: String,
            @SerializedName("createdAt") val createdAt: String
        )
    }
}