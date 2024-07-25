package com.oreo.data.model

data class OAddSleep(
    var duration: Int = 0,
    var startHour: Int = 0,
    var startMinute: Int = 0,
    var endHour: Int = 23,
    var endMinute: Int = 59
)

