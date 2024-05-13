package com.oreo.data.model

import com.google.gson.annotations.SerializedName

data class FemaleHealthUserInfoModel(
    @SerializedName("ota_log") val otaLog: Boolean? = false,
    val isPeriod: Boolean? = false,
    val isOvulation: Boolean? = false,
    val isFertileWindow: Boolean? = false,
    @SerializedName("period_date")
    val periodDate: String? = null,
    @SerializedName("ovulation_date")
    val ovulationDate: String? = null,
    @SerializedName("fertile_window")
    val fertileWindowList: ArrayList<String>? = null,
    @SerializedName("next_period_date")
    val nextPeriodDate: String? = null,
    val nudges: ArrayList<FMHNudges>? = null,
    @SerializedName("current_day")
    val currentDay: Int? = null,
    @SerializedName("period_length")
    val periodLength: Int? = null,
    @SerializedName("pregency_chances")
    val pregencyChances: String? = null

)

data class FMHNudges(val label: String? = null, val message: String? = null)