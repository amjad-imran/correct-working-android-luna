package com.noisefit_commans.data.model

import com.noisefit_commans.data.enums.SleepExtraType


class SleepExtraData(
    val title: String,
    val description: String,
    var sleepExtraType: SleepExtraType,
    var isSelected: Boolean = false
)