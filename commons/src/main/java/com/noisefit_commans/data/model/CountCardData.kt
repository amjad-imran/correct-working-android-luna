package com.noisefit_commans.data.model

import android.os.Parcelable
import com.github.mikephil.charting.data.PieEntry
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class CountCardData(
    @SerializedName("count") var count: String = "_",
    @SerializedName("type") val type: String = "",
    @SerializedName("countSubText") var countSubText: String = "",
    @SerializedName("imageSourceId") val imageSourceId: Int = 0,
    @SerializedName("cardSourceId") val cardSourceId: Int = 0,
    @SerializedName("imageBgSourceId") val imageBgSourceId: Int = 0,
    @SerializedName("leftImageSourceId") val leftImageSourceId: Int = 0,
    @SerializedName("rightImageSourceId") val rightImageSourceId: Int = 0,
    @SerializedName("restingHr") var restingHr: Int = 0,
    @SerializedName("leftValue") var leftValue: String = "_",
    @SerializedName("rightValue") var rightValue: String = "_",
)

data class StressPieData(
    @SerializedName("high") var high: String = "0",
    @SerializedName("medium") var medium: String = "0",
    @SerializedName("normal") var normal: String = "0",
    @SerializedName("relax") var relax: String = "0",
    @SerializedName("pieDataList") var pieDataList: ArrayList<PieEntry>? = null
)

data class SleepPieData(
    @SerializedName("deep") var deep: String = "0",
    @SerializedName("light") var light: String = "0",
    @SerializedName("awake") var awake: String = "0",
    @SerializedName("rem") var rem: String = "0",
    var availableSleepTypes: String = "deep;light;awake:rem",
    @SerializedName("pieDataList") var pieDataList: ArrayList<PieEntry>? = null,
    var typesData: SleepTypesData? = null
)

data class SleepTypesData(
    var deep: String = "_",
    var light: String = "_",
    var awake: String = "_",
    var rem: String = "_",
    var remEnabled: Boolean = false
)

@Parcelize
data class HighlightsData(
    var cardBgSourceId: Int = 0,
    var progressBgSourceId: Int = 0,
    var betterThan: String = "",
    var monthTitle: String = "",
    var currentMonth: String = "",
    var currentMonthText: String = "",
    var currentMonthSubText: String = "",
    var prevMonth: String = "",
    var prevMonthText: String = "",
    var prevMonthSubText: String = "",
    var dayTitle: String = "",
    var currentDay: String = "",
    var currentDayText: String = "",
    var currentDaySubText: String = "",
    var prevDay: String = "",
    var prevDayText: String = "",
    var prevDaySubText: String = "",
    var weekTitle: String = "",
    var weekText: String = "",
    var weekSubText: String = "",
) : Parcelable

data class MarkerEntry(
    var value: String = "",
    var date: String? = "",
    var type: String? = ""
    )