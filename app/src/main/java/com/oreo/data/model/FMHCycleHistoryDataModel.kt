package com.oreo.data.model

import com.google.gson.annotations.SerializedName

data class FMHCycleHistoryDataModel(
    @SerializedName("cycle_length")
    val cycleLength: Int? = null,
    @SerializedName("period_length")
    val periodLength: Int? = null,
    @SerializedName("period_date")
    val periodDate: String? = null,
    @SerializedName("fertile_window")
    val fertileWindow: String? = null,//"2024-05-14 / 2024-05-20"
    @SerializedName("ovulation_start_date")
    val ovulationStartDate: String? = null
)