package com.oreo.ui.sleep.scoredetails

import com.google.gson.annotations.SerializedName

class OSCDResponse(val data: ScoreDetailsData)
class ScoreDetailsData(val day: DayBasedData, val week:DayBasedData, val month:DayBasedData) {

}

class DayBasedData(
    @SerializedName("day_time")
    val dayTime: Long,
    @SerializedName("score")
    val score: Int,
    @SerializedName("unit")
    val unit: String,
    @SerializedName("score_data")
    val scoreData: ScoreData
)

class ScoreData(
    val title: String, val subtitle: String,
    @SerializedName("today_progress")
    val todayProgress: Int,
    @SerializedName("yesterday_progress")
    val yesterdayProgress: Int,
    @SerializedName("all_time_avg")
    val allTimeAvg: Int

)