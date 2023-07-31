package com.noisefit_commans.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BatteryNotification(
    var batteryNotificationData: List<com.noisefit_commans.data.model.BatteryNotificationData>? = null,
    var lastBatteryPercentage: Int = 100
) : Parcelable

@Parcelize
data class BatteryNotificationData(
    val type: com.noisefit_commans.data.model.BatteryNotificationType,
    var hasTriggered: Boolean
) : Parcelable


@Parcelize
data class BatteryNotificationLastStateData(
    val batteryPercentage: Int,
    var lastTimeStamp: Long,
    val type: String
) : Parcelable

enum class BatteryNotificationType {
    FULL,
    CRITICAL,
    CRITICAL_REMINDER
}