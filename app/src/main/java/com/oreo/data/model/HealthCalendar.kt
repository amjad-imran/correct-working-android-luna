package com.oreo.data.model

data class HealthCalendar(
    val date: String,
    val sleep_status: String? = null,
    val readiness_status: String? = null,
    val activity_status: String? = null
)