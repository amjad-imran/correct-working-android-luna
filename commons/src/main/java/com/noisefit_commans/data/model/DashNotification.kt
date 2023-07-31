package com.noisefit_commans.data.model

data class DashNotification(
    val icon: Int,
    val message: String,
    val type: DashNotificationType,
    val hideClose: Boolean = false
)

enum class DashNotificationType {
    NOTIFICATION, BACKGROUND_ALERT, DEVICE_SETUP, BATTERY_OPTIMIZATION, CONNECTIVITY, SUPPORT_QUERIES, BACKGROUND_PERMISSION
}
