package com.oreo.data.model

import com.oreo.ui.sleep2.internal.SkinTempInternalLaunchState

data class OHealthMonTrendsDataModel(
    val trendType:SkinTempInternalLaunchState,
    val dayDate:String,
    val dspValue:String,
    val isShowOptimal:Boolean=false,
    val isShowHighlight:Boolean=true,
    val description:String

)