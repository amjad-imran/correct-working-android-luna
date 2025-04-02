package com.oreo.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationGoals(
    val hydration: Int? = null,
    val hydration_required: Int? = null,
    val steps: Int? = null,
    val steps_required: Int? = null,
    //
    var isMetric: Boolean ?= false,
    var convertedHydrateGoal: Int ?= 0,
    var hydratePercent: Float ?= 0f,
    var notificationToggleModel: NotificationToggleModel ?= null,
    var glassImage: Int ?= 0,
    var key: String ?= "0"
    //
) : Parcelable
