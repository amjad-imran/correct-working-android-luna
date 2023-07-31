package com.noisefit.data.model.diy

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class DiyCustomWatchFacesData(
    @SerializedName("background")
    @Expose
    val diyWatchFaceBackground: List<DiyWatchFaceBackground>,
    @SerializedName("placement")
    @Expose
    val diyWatchFacePlacement: List<DiyWatchFacePlacement>,
    @SerializedName("colour")
    @Expose
    val colour: List<String>? = null

)