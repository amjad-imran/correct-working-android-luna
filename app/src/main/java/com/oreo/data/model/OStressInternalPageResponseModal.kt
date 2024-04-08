package com.oreo.data.model

import com.google.gson.annotations.SerializedName

data class StressResultData(
    val date: String,
    val data: SRData? = null,
    val year: String? = null,
    val message: String? = null
)

data class SRData(
    val stressed: TypicalData? = null,
    val calm: TypicalData? = null,
    val focused: TypicalData? = null
)

data class TypicalData(
    val duration: Int? = null,
    @SerializedName("typical_day") val typicalDay: Int? = null,
)

data class StressData(
    val focussed: StressShowData? = null,
    val stressed: StressShowData? = null,
    val calm: StressShowData? = null,
    @SerializedName("avg_duration")
    val avgDuration: Long? = null,
    @SerializedName("dsp_msg")
    val dspMsg: String? = null

)

data class StressShowData(
    val duration: Long? = null,
    val score: Int? = null
)