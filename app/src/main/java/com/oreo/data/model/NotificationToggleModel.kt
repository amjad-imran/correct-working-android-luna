package com.oreo.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationToggleModel(
    var master_notification: Boolean = false,
    var hydrate_notification: Boolean = false,
    var steps_notification: Boolean = false,
    var sleep_notification: Boolean = false,
    var female_health: Boolean = false,
) : Parcelable