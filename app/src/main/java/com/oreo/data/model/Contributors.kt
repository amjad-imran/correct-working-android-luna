package com.oreo.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contributors(
    val title: String,
    val leftText: String,
    val leftTextColor: Int,
    val barColor: Int,
    var barPercent: Int,
    var hasData: Boolean = true,
    val backgroundRes: Int,
    var description: String = "",
    var contriType: Contributor
) : Parcelable

enum class Contributor {
    SLEEP_SCORE, ACTIVITY_SCORE,
    RECOVERY_INDEX, SLEEP_REGULARITY,
    SLEEP_BALANCE, AVERAGE_HR,
    ACTIVITY_BALANCE, HRV_BALANCE,
    SKIN_TEMP, SLEEP_DURATION,

    STAY_ACTIVE, MOVE_EVERY_HOUR,
    CALORIE_GOAL, TRAINING_FREQUENCY,
    TRAINING_VOLUME
}