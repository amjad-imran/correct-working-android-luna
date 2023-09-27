package com.oreo.data.model

import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.noisefit_commans.models.SleepData
import com.oreo.data.model.health.ODashboardActivityModel
import com.oreo.data.model.health.ODashboardReadinessModel
import com.oreo.data.model.health.ODashboardSleepModel


sealed class OHealthOverview {


    object InfoRingWelcome : OHealthOverview()
    object InfoRingCare : OHealthOverview()
    data class InfoVideo(val type: VideoInfoType, val title: String, val message: String) :
        OHealthOverview()


    class AutoSport(
        val count: Int
    ) : OHealthOverview()

    class Readiness(
        val data: ODashboardReadinessModel
    ) : OHealthOverview()

    class ReadinessMinimal(
        val data: ODashboardReadinessModel
    ) : OHealthOverview()

    class Sleep(
        val data: ODashboardSleepModel,
        val sleepArray: ArrayList<SleepData.SleepDataBreakup> = ArrayList(),
        val startTime: String,
        val endTime: String
    ) : OHealthOverview()

    class SleepMinimal(
        val data: ODashboardSleepModel,
        val sleepArray: ArrayList<SleepData.SleepDataBreakup> = ArrayList()
    ) : OHealthOverview()

    object SleepWaiting : OHealthOverview()
    class ActivityMinimal(
        val data: ODashboardActivityModel,
        val caloriesGoal: Int
    ) : OHealthOverview()

    class Activity(
        val data: ODashboardActivityModel,
        val caloriesGoal: Int
    ) : OHealthOverview()

    class HeartRate(
        var value: String,
        var lastTime: String,
        val candleValue: ArrayList<CandleEntry> = ArrayList(),
        val lineData: Pair<ArrayList<Entry>, ArrayList<Int>>,
        val xLabelList: ArrayList<String> = ArrayList(),
        val axisMinimum: Float,
        val average: Float,
        var measureState: TapMeasureState = TapMeasureState.DEFAULT
    ) : OHealthOverview()

    class SleepActivityScore(
        val sleepScore: Int? = null,
        val sleepTrend: Int? = null,
        val sleepValue: List<ChartModel>? = ArrayList(),
        val activityScore: Int? = null,
        val activityTrend: Int? = null,
        val activityValue: List<ChartModel>? = ArrayList()
    ) : OHealthOverview()


    class ReadinessScore(
        val score: Int? = null,
        val trend: Int? = null,
        val value: List<ChartModel>? = ArrayList()
    ) : OHealthOverview()


}

data class DashAlert(
    val message: String,
    val isCancellable: Boolean,
    var type: AlertType = AlertType.DEFAULT,
)

enum class AlertType {
    BLUETOOTH, OTA_UPDATE, DEFAULT
}

enum class VideoInfoType {
    SLEEP, READINESS, ACTIVITY
}

enum class TapMeasureState {
    NO_DEVICE, LAST_MEASURED, MEASURING, DEFAULT, ERROR
}