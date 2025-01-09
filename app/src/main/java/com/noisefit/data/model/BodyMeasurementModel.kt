package com.noisefit.data.model

data class BodyMeasurementModel(
    val height: BodyMeasurementValue? = null,
    val weight: BodyMeasurementValue? = null,
    val bodyFat: BodyMeasurementValue? = null,
)

data class BodyMeasurementValue(
    val timeStamp: Long,
    val value: Float,
)