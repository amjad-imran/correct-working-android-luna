package com.oreo.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ODayTimeActivitiesDataModel(
    val type: String? = null,
    val id: String? = null,
    val workoutData: OActivityListModal? = null,
    val startTime: String? = null,
    val endTime: String? = null
) : Parcelable