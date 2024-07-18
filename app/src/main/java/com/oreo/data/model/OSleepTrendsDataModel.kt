package com.oreo.data.model

import com.oreo.ui.sleep2.internal.SleepInternalLaunchState

data class OSleepTrendsDataModel(
    val trendType:SleepInternalLaunchState,
    val dayDate:String,
    val dspValue:String,
    val isShowOptimal:Boolean=false,
    val isShowHighlight:Boolean=true,
    val description:String

)