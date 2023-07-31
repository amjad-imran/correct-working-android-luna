package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName
import com.noisefit.data.remote.response.CatWiseWatchFacesItem
import com.noisefit_commans.models.WatchFace

data class WatchFaceCategoryResponse(val category : CatWiseWatchFacesItem,
                                     @SerializedName("faces")
                                    val faces: List<WatchFace>)