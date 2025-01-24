package com.oreo.data.model

data class AlarmDisplayModel(
    val bedTime: String,
    val wakeTime: String,
    val selectedDays: List<Int>
)