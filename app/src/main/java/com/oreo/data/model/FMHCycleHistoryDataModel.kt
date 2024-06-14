package com.oreo.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

data class PeriodCycleHistory(
    @SerializedName("cycle_history")
    val cycleHistory: List<FMHCycleHistoryDataModel>? = null,
    @SerializedName("user_default")
    val userDefault: CycleDetailsDefault? = null,
    val avg: CycleAverage? = null
)

data class CycleAverage(
    @SerializedName("cycle_length_avg")
    val cycleLengthAvg: Int? = null,
    @SerializedName("period_length_avg")
    val periodLengthAvg: Int? = null
)

data class CycleDetailsDefault(
    @SerializedName("calendar_start")
    val calendarStart: String? = null,
    @SerializedName("first_period_date")
    val firstPeriodDate: String? = null,
    @SerializedName("cycle_length")
    val cycleLength: Int? = null,
    @SerializedName("period_length")
    val periodLength: Int? = null,
)

@Parcelize
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
    val ovulationStartDate: String? = null,
    @SerializedName("next_period_date")
    val nextPeriodDate: String? = null
) : Parcelable {

    fun getCycleStart(): LocalDate {
        return LocalDate.parse(periodDate)
    }

    fun getCycleEnd(): LocalDate {
        return getCycleStart().plusDays(cycleLength?.toLong() ?: 0L)
    }
}