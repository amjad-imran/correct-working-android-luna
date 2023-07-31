package com.noisefit_commans.data.model.trophies

import com.google.gson.annotations.SerializedName

data class StreaksItem(@SerializedName("no_of_days")
                       val noOfDays: Int = 0,
                       @SerializedName("id")
                       val id: Int = 0,
                       @SerializedName("title")
                       val title: String = "",
                       @SerializedName("is_collected")
                       var isCollected: Boolean)