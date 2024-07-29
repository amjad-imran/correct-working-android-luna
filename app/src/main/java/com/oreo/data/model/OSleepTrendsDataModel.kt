package com.oreo.data.model

import android.os.Parcelable
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState
import kotlinx.parcelize.Parcelize

@Parcelize
data class OSleepTrendsDataModel(
    var trendType:SleepInternalLaunchState=SleepInternalLaunchState.REM_SLEEP,
    var dayDate:String?=null,
    var dspValue:Int?=null,
    var dspValue2:Int?=null,
    val isShowOptimal:Boolean=false,
    val isShowHighlight:Boolean=true,
    val description:String?=null

):Parcelable