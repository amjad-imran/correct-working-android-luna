package com.noisefit_commans.data.model


sealed class HealthOverview {

    class Steps(
        val value: String,
        val completed: Float,
        val goal: Int,
        val timeAgo: String,
    ) : HealthOverview()

    class Calories(
        val value: String,
        val timeAgo: String,
        val unit:String
    ) : HealthOverview()

    class Stress(
        val value: String,
        val type: String,
        val timeAgo: String,
        val avg: String,
//        val values: ArrayList<BarEntry>,
//        val colors: ArrayList<Int>
    ) : HealthOverview()

    class BodyTemp(
        val value: String,
        val timeAgo: String,
        val avg: String,
//        val values: ArrayList<BarEntry>,
//        val colors: ArrayList<Int>
    ) : HealthOverview()

    class Distance(
        val value: String,
        val timeAgo: String,
        val distanceUnit: String,
//        val values: ArrayList<BarEntry>
    ) : HealthOverview()

    class Sleep(
        val duration: Int,
        val date: String,
        val sleepScore: String,
//        val sleepArray: ArrayList<SleepData.SleepDataBreakup>?,
//        val countCardData: CountCardData?
    ) : HealthOverview()

    class BloodOxygen(
        val value: String,
        val avg: String,
        val timeAgo: String,
    ) : HealthOverview()

    class HeartRate(
        val value: String,
        val timeAgo: String,
        val avg: String,
//        val values: ArrayList<Entry>,
    ) : HealthOverview()


}