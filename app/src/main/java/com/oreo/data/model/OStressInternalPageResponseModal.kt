package com.oreo.data.model

import com.google.gson.annotations.SerializedName

data class OStressInternalPageResponseModal(
    val resultData: List<StressResultData>? = null,
    val stressData: StressData? = null,
)

data class StressResultData(val date: String, val data: SRData, val year: String? = null)
data class SRData(
    val stressed: Int = 0,
    val calm: Int = 0,
    val focussed: Int = 0
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