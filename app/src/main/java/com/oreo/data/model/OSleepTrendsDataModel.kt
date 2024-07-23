package com.oreo.data.model

import com.oreo.ui.sleep2.internal.SleepInternalLaunchState

data class OSleepTrendsDataModel(
    var trendType:SleepInternalLaunchState=SleepInternalLaunchState.REM_SLEEP,
    var dayDate:String?=null,
    var dspValue:Int?=null,
    var dspValue2:Int?=null,
    val isShowOptimal:Boolean=false,
    val isShowHighlight:Boolean=true,
    val description:String?=null

)