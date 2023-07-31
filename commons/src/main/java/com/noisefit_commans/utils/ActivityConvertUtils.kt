package com.noisefit_commans.utils

import com.noisefit_commans.models.Units
import java.math.BigDecimal
import java.util.*

object ActivityConvertUtils {

    fun avgPace(unit: Units, distance: Long?, duration: Long?): String {
        if (distance == null) return ""
        if (duration == null) return ""
        val avgPaceString: String
        var avgPace = 0.0f
        if (distance != 0L) {
            avgPace = duration / (distance / 1000.0f)
        }

        if (unit == Units.IMPERIAL) {
            avgPace = duration / (distance / 1610.0f)
        }
        avgPaceString = java.lang.String.format(
            Locale.ENGLISH, "%1$02d'%2$02d", (avgPace / 60).toInt(), (avgPace % 60).toInt()
        )
        return avgPaceString
    }

    fun avgPacePulse2(unit: Units, distance: Long?, duration: Long?): String {
        if (distance == null) return ""
        if (duration == null) return ""
        val avgPaceString: String
        var avgPace = 0.0f
        if (distance != 0L) {
            avgPace = duration / (distance / 1000.0f)
        }

        if (unit == Units.IMPERIAL) {
            avgPace = duration / (distance / 1610.0f)
        }
        avgPaceString = java.lang.String.format(
//            Locale.ENGLISH, "%1$02d'%2$02d", (avgPace / 60).roundToInt(), (avgPace % 60).roundToInt()
            Locale.ENGLISH, "%1$02d'%2$02d", (avgPace / 60).toInt(), (avgPace % 60).toInt()
        )
        return avgPaceString
    }

    fun  avgPaceUltra(unit: Units, distance: Long?, duration: Long?): String {
        if (distance == null) return ""
        if (duration == null) return ""
        val avgPaceString: String
        var avgPace = 0.0f
        if (distance != 0L) {
            avgPace = duration / (distance / 1000.0f)
        }
        val minute = (avgPace / 60).toInt()
        val second = (avgPace % 60).toInt()
        if (isShow00Pace(minute, second)) {
            avgPaceString = java.lang.String.format(Locale.ENGLISH, "%1$02d'%2$02d\"", 0, 0)
        } else {
            if (unit == Units.IMPERIAL) {
                avgPace = duration / (distance / 1000.0f / 1.61f)
            }
            avgPaceString = java.lang.String.format(
                Locale.ENGLISH, "%1$02d'%2$02d\"",
                (avgPace / 60).toInt(), (avgPace % 60).toInt()
            )
        }
        return avgPaceString
    }

    fun isShow00Pace(minute: Int, second: Int): Boolean {
        val totalSecond = minute * 60 + second
        return totalSecond > 50 * 60 + 58 || totalSecond <= 0
    }

    fun averageSpeed(unit: Units, distance: Long?, duration: Long?): String {
        if (distance == null) return ""
        if (duration == null) return ""
        return if (unit == Units.METRIC) {
            bigDecimalFormat(distance / 1000f / (duration / 3600f))
        } else {
            bigDecimalFormat(distance / 1.61f / 1000.0f / (duration / 3600.0f))
        }
    }

    fun bigDecimalFormat(number: Float): String {
        if(number.isNaN()){
            return "0"
        }
        LOGS.d("bigDecimalFormat ${number}")
        return BigDecimal(number.toString()).setScale(2, BigDecimal.ROUND_DOWN).toString()
    }

    fun bigDecimalFormatUp(number: Float): String {
        if(number.isNaN()){
            return "0"
        }
        return BigDecimal(number.toString()).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString()
    }

}