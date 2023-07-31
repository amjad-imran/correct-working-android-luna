package com.noisefit_commans.utils

import java.util.*
import kotlin.math.roundToInt

object StringUtils {
    val DefaultStartHour = 8
    val DefaultStopHour = 18

    fun String?.isValidEmail():Boolean{
        if(this.isNullOrEmpty()) return false
        return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    fun String?.convertDoubleStringToInt():Int{
        if(this.isNullOrEmpty()) return 0
        return try {
            this.toDoubleOrNull()?.roundToInt()?:0
        }catch (exp : Exception){
            0
        }
    }

    fun String.capitalizeWords(): String = split(" ").joinToString(" ") {
        it.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(
                Locale.getDefault()
            ) else char.toString()
        }
    }

}