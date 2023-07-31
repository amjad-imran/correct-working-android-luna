package com.noisefit.ui.common

import com.noisefit_commans.utils.LOGS


fun Float.calculatePercentage(total: Float?): Float {
    if (total == null || total == 0f) {
        return 0f
    }
    val data = (this / total)
    return data * 100
}

fun Float.calculateInitFromPercentage(total: Float?): Float {
    if (total == null || total == 0f) {
        return 0f
    }
    val data = (this * total)
    return data / 100
}

//Percentage Increase=Final Value−Starting Value|Starting Value|×100
fun Float.increasePercentage(final: Float): Float {
    if (final == 0f || this == 0f) {
        return 0f
    }
    val data = (final - this) / this
    return data * 100
}

fun Float.challengeCompletePercentage(total: Float): Float {
    val percentage = this.increasePercentage(total)
    return 100 + percentage
}
