package com.oreo.ui.custom.female

import com.oreo.data.model.PeriodTempChartModel

interface ScrollListenerPeriodTemp {
    fun onPositionSelected(position: Int, chartModel: PeriodTempChartModel?)

    fun onScrolling(position: Int, chartModel: PeriodTempChartModel?)
}