package com.oreo.data.model.femaleh

import com.google.gson.annotations.SerializedName
import com.oreo.data.model.health.Nudges

data class FemaleHealthUserInfoModel(
    @SerializedName("ota_log") val otaLog: Boolean = false,
    val isPeriod: Boolean = false,
    val isOvulation: Boolean = false,
    val isFertileWindow: Boolean = false,
    @SerializedName("period_date")
    val periodDate: String? = null,
    @SerializedName("ovulation_date")
    val ovulationDate: String? = null,
    @SerializedName("fertile_window")
    val fertileWindowList: ArrayList<String>? = null,
    @SerializedName("next_period_date")
    val nextPeriodDate: String? = null,
    val nudges: ArrayList<Nudges>? = null,
    @SerializedName("current_day")
    val currentDay: Int? = null,
    @SerializedName("cycle_length")
    val cycleLength: Int? = null,
    @SerializedName("period_length")
    val periodLength: Int? = null,
    @SerializedName("pregnancy_chances")
    val pregnancyChances: String? = null,
    @SerializedName("is_not_sure")
    val isNotSure: Boolean? = null,
    @SerializedName("is_track_pregnancy")
    val isTrackPregnancy: Boolean? = null

)
