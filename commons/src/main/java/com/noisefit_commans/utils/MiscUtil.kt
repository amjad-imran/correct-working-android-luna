package com.noisefit_commans.utils

import android.text.TextUtils
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

    fun addUnderscore(itemName: String): String {
        return itemName.lowercase().replace(" ", "_")
    }
    fun getDeviceName(): String {

        val manufacturer: String = BuildUtils.getDeviceManufacturer()
        val model: String = BuildUtils.getDeviceModel()
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else capitalize(manufacturer) + " " + model
    }

    private fun capitalize(str: String): String {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true
        val phrase = StringBuilder()
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(c.uppercaseChar())
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase.append(c)
        }
        return phrase.toString()
    }


}
