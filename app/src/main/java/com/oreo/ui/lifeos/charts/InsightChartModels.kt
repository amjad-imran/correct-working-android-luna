package com.oreo.ui.lifeos.charts

import android.os.Parcelable
import com.noisefit_commans.data.model.CountCardData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.models.SleepData
import com.oreo.data.dataConverter.GraphsKey
import com.oreo.data.model.DayTimeDataModel
import com.oreo.data.model.lifeos.dashModels.InsightItemResponseModel
import com.oreo.ui.custom.HRCombineModel
import com.oreo.ui.custom.StressCombineModel
import com.oreo.ui.custom.sleep.internal.GraphDataModel
import com.oreo.ui.custom.sleep.internal.SleepSingleGradientChartType
import com.oreo.ui.sleep2.internal.InternalSelectedPeriod
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/** UI model the adapter will consume. */
@Parcelize
data class InsightCardUiModel(
    val id: Long,
    val title: String?,
    val timeText: String?,
    val chartKey: GraphsKey?,
    val payload: PayloadData?,
    val styleRes: Int? = null,
    val raw: InsightItemResponseModel? = null,
) : Parcelable

@Parcelize
data class PayloadData(
    val hrData: HrChartPayload? = null,
    val stressData: StressChartPayload? = null,
    val dayTimeData: DayTimeChartPayload? = null,
    val sleepBreakup: List<SleepData.SleepDataBreakup>? = null,
    val sleepMovement: Pair<List<OreoSleepData.OreoSleepMovementDataBreakup>,
            CountCardData>? = null,
    val trendData: TrendGraphData? = null,
) : Parcelable

@Parcelize

data class TrendGraphData(
    val list: List<GraphDataModel>? = null,
    val yAxisRange: List<Pair<Int, String>>? = null,
    val xAxisRange: List<LocalDate>?=null,
    val avgValue: Pair<Float, String>? = null,
    val selectedPosition: Int? = null,
    val contributorType: SleepInternalLaunchState? = null,
    val optimalRange: Pair<Float, Float>? = null,
    val nonNullDataCount: Int? = null,
    val chartType: SleepSingleGradientChartType?=null,
    val showOverlay: Boolean?=null,
    val selectedPeriod: InternalSelectedPeriod?=null,
    val maxDeviation: Int?=null,
    val maxValue: Int?=null,
) : Parcelable

/**
 * Typed payload wrappers to avoid casting in renderers.
 */
@Parcelize
data class HrChartPayload(
    val model: HRCombineModel?,
    val yAxisCount: Int = 3,
    val minYAxis: Int = 0,
    val maxYAxis: Int = 200
) : Parcelable

@Parcelize
data class StressChartPayload(
    val model: StressCombineModel?
) : Parcelable

@Parcelize
data class DayTimeChartPayload(
    val model: DayTimeDataModel?
) : Parcelable

// Health monitor single-line gradient chart payload (daily/day/week/month)
@Parcelize
data class TimeSeriesPayload(
    val values: List<Float?>,
    val dates: List<LocalDate>,
    val unitLabel: String,
    val chartType: SleepSingleGradientChartType = SleepSingleGradientChartType.DEFAULT
) : Parcelable
