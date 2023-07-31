package com.noisefit.data.model

import com.noisefit_commans.data.response.LiveMatch


data class WinListData(
    val response: List<LiveMatch>? = null,
    val user_points: Int = 0
)