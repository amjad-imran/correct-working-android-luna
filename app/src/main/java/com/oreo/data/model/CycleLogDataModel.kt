package com.oreo.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class CycleLogDataModel(
    var flowData: List<FlowLog>? = null,
    var symptomsData: List<FlowLog>? = null
) : Parcelable

@Parcelize
data class FlowLog(
    val image: Int? = null,
    val title: String? = null,
    val isChecked: Boolean = false
) : Parcelable
