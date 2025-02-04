package com.oreo.data.model

import com.noisefit_commans.data.model.SleepPlannerData
import com.noisefit_commans.models.SleepData
import com.oreo.data.model.health.InfoTextData
import com.oreo.data.model.health.InfoVideoData
import com.oreo.data.model.health.Nap
import com.oreo.data.model.health.ODashboardActivityModel
import com.oreo.data.model.health.ODashboardReadinessModel
import com.oreo.data.model.health.ODashboardSleepModel
import com.oreo.data.model.sleep.HealthTrend
import com.oreo.ui.custom.StressCombineModel


sealed class OHealthOverview {


    data class InfoRingWelcome(val data: InfoTextData) : OHealthOverview()
    data class InfoRingCare(val data: InfoTextData) : OHealthOverview()
    data class NapDashCard(val naps: List<Nap>, val date: String) : OHealthOverview()
    data class InfoVideo(val type: VideoInfoType, val data: InfoVideoData) :
        OHealthOverview()

    data class StressGraph(
        val data: StressCombineModel,
        val value: Int,
        val timeStamp: Long,
        val valueStatus: String,
        val isToday: Boolean,
        val isBeta: Boolean = false
    ) :
        OHealthOverview()

    class SleepPlannerCard(
        val data: SleepPlannerData
    ) : OHealthOverview()


    class LunaAiCard(
    ) : OHealthOverview()

    class HealthMonitorCard(
        val data: HealthTrend
    ) : OHealthOverview()

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

    //    class HeartRate(
//        var value: String,
//        var lastTime: String,
//        val candleValue: ArrayList<CandleEntry> = ArrayList(),
//        val lineData: Pair<ArrayList<Entry>, ArrayList<Int>>,
//        val xLabelList: ArrayList<String> = ArrayList(),
//        val axisMinimum: Float,
//        val average: Float,
//        var measureState: TapMeasureState = TapMeasureState.DEFAULT
//    ) : OHealthOverview()
    class HeartRateDataModel(
        val listData: List<HRModel>? = null,
        val average: Float,
        var lastTime: String? = "",
        var value: String? = "",
        var maxValues: Int,
        var minValues: Int,
        var measureState: TapMeasureState = TapMeasureState.DEFAULT
    ) : OHealthOverview()

    class StressDashDataModel(
        var data: StressCombineModel? = null,
        val listData: List<Int>? = null,
        var lastTime: String? = "",
        var value: Int? = null,
        var measureState: TapMeasureState = TapMeasureState.DEFAULT
    )


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

    class CycleTrackerCardSmall(
        val data: PeriodCard1
    ) : OHealthOverview()

    class CycleTrackerCardBig(
        val data: PeriodCard2
    ) : OHealthOverview()

    class GotYourPeriod(
        val title: String? = null,
        val currentDay: Int
    ) : OHealthOverview()

    class CardTrackFemaleHealth(
        val state: FemaleHealthCardState
    ) : OHealthOverview()

}

enum class FemaleHealthCardState {
    TRACK, LOG
}


data class PeriodCard1(
    val title: String,
    val days: Int,
    val nudge: String,
    val currentCycleDay: Int,
    val totalCycleDay: Int,
    val bottomText: String,
    val predictionDate: String,
    val background: Int
)

data class PeriodCard2(
    val title: String,
    val days: Int,
    val subTitle: String,
    val nudge: String,
    val currentCycleDay: Int,
    val totalCycleDay: Int,
    val temperatureVariation: Float? = null,
    val predictionString: String,
    val predictionDate: String,
    val background: Int
)

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
    NO_DEVICE, LAST_MEASURED, MEASURING, DEFAULT, ERROR, HIDE
}