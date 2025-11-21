package com.oreo.data.model.lifeos.dashModels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Graph(
    val date: String ?= null,
    val master_avg_hrv: Int ?= null
) : Parcelable