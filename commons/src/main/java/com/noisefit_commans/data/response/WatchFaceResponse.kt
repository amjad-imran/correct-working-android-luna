package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.models.WatchFace

data class WatchFaceResponse(val face: WatchFace,
                             @SerializedName("similarWatchFaces")
                                    val similarWatchFaces: List<WatchFace>)