package com.oreo.data.model

import com.oreo.ui.custom.ItemTemp
import com.oreo.ui.custom.Section
import com.oreo.ui.femalehealth.cycletracker.CyclePhase
import com.oreo.ui.femalehealth.cycletracker.DayState
import com.oreo.ui.femalehealth.cycletracker.PeriodPos

open class ChartModel {
    var value = 0
    var valueFloat = 0.0f
    var index: String? = null
    var date: String? = null
    var isDistanceGraph: Boolean = false
    var isMetric: Boolean = true
    var formattedDate: String? = null
}

data class ChartModelStress(
    val calm: Int = 0,
    val focussed: Int = 0,
    val stressed: Int = 0,
    val index: String? = null,
    val date: String? = null,
    val isDistanceGraph: Boolean = false,
    val formattedDate: String? = null
)

data class PeriodChartModel(
    var value: Int = 0,
    var date: String? = null,
    var month: String? = null,
    var day: String? = null,
    var isNormal: Boolean = true
)

data class PeriodTempChartModel(
    var value: Float? = null,
    var phase: CyclePhase = CyclePhase.FOLLECULAR,
    var date: String? = null,
    var month: String? = null,
    var day: String? = null
)