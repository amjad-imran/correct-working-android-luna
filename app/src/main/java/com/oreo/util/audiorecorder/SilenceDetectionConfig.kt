package com.oreo.util.audiorecorder

data class SilenceDetectionConfig(
    var minAmplitudeThreshold: Int,
    var bufferDurationInMillis: Long = 2000,
    var preSilenceDurationInMillis: Long = 2000,
)