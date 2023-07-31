package com.noisefit_commans.data.model

import com.google.gson.annotations.SerializedName
import com.noisefit_commans.response.SleepHeartRate

data class SleepOverlayData(
    @SerializedName("hr_max") var hrMax: Int = 0,
    @SerializedName("hr_min") var hrMin: Int = 0,
    @SerializedName("hr_breakup") var heartRateBreakup: List<SleepHrData>?,
    @SerializedName("stress_max") var stressMax: Int = 0,
    @SerializedName("stress_min") var stressMin: Int = 0,
    @SerializedName("stress_breakup") var stressBreakUp: List<SleepStressData>?,
)


data class SleepHrData(
    @SerializedName("time") var time: String? = null,
    @SerializedName("date") var date: String? = null,
    @SerializedName("avg_value") var avg_value: Int = 0,
)

data class SleepStressData(
    @SerializedName("time") var time: String? = null,
    @SerializedName("date") var date: String? = null,
    @SerializedName("value") var value: Int = 0,
)

data class SleepOfflineOverlayData(
    @SerializedName("hr_max") var hrMax: Int = 0,
    @SerializedName("hr_min") var hrMin: Int = 0,
    @SerializedName("hr_breakup") var heartRateBreakup: List<SleepHeartRate>?,
    @SerializedName("stress_max") var stressMax: Int = 0,
    @SerializedName("stress_min") var stressMin: Int = 0,
    @SerializedName("stress_breakup") var stressBreakUp: List<SleepHeartRate>?,
)


data class SleepHeartRateData(
    @SerializedName("time") var time: String? = null,
    @SerializedName("date") var date: String? = null,
    @SerializedName("avg_value") var avg_value: Int = 0,
)
