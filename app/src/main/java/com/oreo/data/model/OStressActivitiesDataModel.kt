package com.oreo.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OStressActivitiesDataModel(
    val type: String? = null,
    val workoutData: OActivityListModal? = null,
    val startTime: String? = null,
    val endTime: String? = null
) : Parcelable