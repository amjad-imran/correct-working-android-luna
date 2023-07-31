package com.noisefit_commans.data.model.trophies

import com.google.gson.annotations.SerializedName

data class Trophies(@SerializedName("distance")
                    val distance: Distance,
                    @SerializedName("steps")
                    val steps: Steps)