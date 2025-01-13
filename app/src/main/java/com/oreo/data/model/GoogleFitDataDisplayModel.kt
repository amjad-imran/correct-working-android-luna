package com.oreo.data.model

data class GoogleFitDataDisplayModel(
    var isSelected: Boolean = false,
    val type: GoogleFitDataType,
    val startTime: Long,
    val endTime: Long,
    val duration: Long,//in seconds
    val rawData: String? = null
)

enum class GoogleFitDataType(val type: Int) {
    SLEEP(0), NAP(1), WORKOUT(2), BODY_MEASUREMENTS(3)
}