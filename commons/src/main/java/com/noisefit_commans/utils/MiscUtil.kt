package com.noisefit_commans.utils

import com.noisefit_commans.models.SleepType

object MiscUtil {


    fun getSleepType(type: String): SleepType {
        return when (type.lowercase().trim()) {
            "light" -> {
                SleepType.LIGHT
            }
            "deep" -> {
                SleepType.DEEP
            }
            "awake" -> {
                SleepType.AWAKE
            }
            "rem" -> {
                SleepType.REM
            }
            else -> {
                return SleepType.SOBER
            }
        }
    }

    fun getFahrenheit(value: String): String {
        return value + "\u2109"
    }

    fun getCelsius(value: String): String {
        return value + "\u2103"
    }

    fun getFormattedTimeInHourMinute(timeInMinutes: Int): Pair<Int, Int> {
        val hour: Int = timeInMinutes.div(60)
        val min: Int = timeInMinutes.mod(60)
        return Pair(hour, min)

    }
    fun scorePercentCalculator(scoreValue: Float): Float {
        //after debug found .68 is max progress value for this anim file
        return .68.toFloat().times(scoreValue).div(100)

    }


}
