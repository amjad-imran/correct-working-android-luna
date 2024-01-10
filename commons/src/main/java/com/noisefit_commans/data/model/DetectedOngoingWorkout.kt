package com.noisefit_commans.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DetectedOngoingWorkout(
    val startTimeStamp: Long,
    val duration: Int,
    val sportStatus: Int
) : Parcelable
