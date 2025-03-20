package com.oreo.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationToggleModel(
    val master_notification: Boolean,
    val hydrate_notification: Boolean,
    val steps_notification: Boolean,
    val sleep_notification: Boolean,
) : Parcelable