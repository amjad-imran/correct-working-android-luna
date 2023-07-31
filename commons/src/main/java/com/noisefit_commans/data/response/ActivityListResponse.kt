package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.models.SportsModeResponse

data class ActivityListResponse(
    val activities: List<SportsModeResponse>,
    @SerializedName("first_activity_on") val firstActivityOn: String?,
    @SerializedName("active_dates") val activeDates: List<String>
)