package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.models.WatchFace

data class WatchFaceResponseNew(@SerializedName("watch_faces")
                                val faces: List<WatchFace>)