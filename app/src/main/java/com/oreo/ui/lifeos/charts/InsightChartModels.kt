package com.oreo.ui.lifeos.charts

import com.noisefit_commans.data.model.CountCardData
import com.noisefit_commans.models.SleepData
import com.oreo.data.dataConverter.GraphsKey
import com.oreo.data.model.DayTimeDataModel
import com.oreo.data.model.lifeos.dashModels.InsightItemResponseModel
import com.oreo.ui.custom.HRCombineModel
import com.oreo.ui.custom.StressCombineModel
import com.oreo.ui.custom.sleep.internal.SleepSingleGradientChartType
import java.time.LocalDate

/** UI model the adapter will consume. */
data class InsightCardUiModel(
    val id: Long,
    val title: String?,
    val timeText: String?,
    val chartKey: GraphsKey?,
    val payload: Any?,
    val styleRes: Int? = null,
    val raw: InsightItemResponseModel? = null
)

/**
 * Typed payload wrappers to avoid casting in renderers.
 */
data class HrChartPayload(
    val model: HRCombineModel?,
    val yAxisCount: Int = 3,
    val minYAxis: Int = 0,
    val maxYAxis: Int = 200
)

data class StressChartPayload(
    val model: StressCombineModel?
)

data class DayTimeChartPayload(
    val model: DayTimeDataModel?
)

// Health monitor single-line gradient chart payload (daily/day/week/month)
data class TimeSeriesPayload(
    val values: List<Float?>,
    val dates: List<LocalDate>,
    val unitLabel: String,
    val chartType: SleepSingleGradientChartType = SleepSingleGradientChartType.DEFAULT
)
