package com.noisefit_commans.data.response

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class RecentActivityResponse(@SerializedName("recent_activities") val activities: JsonObject?)