package com.oreo.data.model

import com.oreo.data.model.health.OreoSleepModel

sealed class OSleepDetails {
    class Header(
        val suffix: MutableList<ChartModel> = ArrayList(),
        val prefix: MutableList<ChartModel> = ArrayList(),
        val list: MutableList<ChartModel> = ArrayList()
    ) : OSleepDetails()

    class Bottom(
        val dayData: OreoSleepModel
    ) : OSleepDetails()
}