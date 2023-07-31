package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName

data class NplDashResponse(
    @SerializedName("live_match") val live_match: LiveMatch,
    @SerializedName("won_match") val won_match: List<LiveMatch>,
    @SerializedName("upcoming_match") val upcomingMatch: List<LiveMatch>
)
