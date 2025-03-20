package com.noisefit_commans.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class NotificationGoals(
    val hydration: Int? = null,
    val hydration_required: Int? = null,
    val steps: Int? = null,
    val steps_required: Int? = null
) : Parcelable
