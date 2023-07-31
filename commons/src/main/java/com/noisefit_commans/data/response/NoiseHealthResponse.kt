package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.data.model.NoiseHealthCategory

data class NoiseHealthResponse(@SerializedName("categories")
                               val categories: List<NoiseHealthCategory>,
                               @SerializedName("recent")
                               val recent: NoiseHealthCategory?)