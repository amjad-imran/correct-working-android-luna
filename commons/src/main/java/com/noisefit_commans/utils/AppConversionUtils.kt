package com.noisefit_commans.utils


import com.noisefit_commans.common.upTo1Decimal
import com.noisefit_commans.models.HeightUnitSystem
import java.text.SimpleDateFormat
import java.util.*

object AppConversionUtils {

    fun getCentigradeBody(tempValue: Int): Float {
        var bleBodyTemp = 0f
        val value1 = 300 + tempValue
        bleBodyTemp = value1.toFloat() / 10
        return bleBodyTemp.upTo1Decimal()
    }

    fun getOreoCentigradeBody(tempValue: Int): Float {
        var bleBodyTemp = 0f
        bleBodyTemp = tempValue.toFloat() / 100
        return bleBodyTemp.upTo1Decimal()
    }

    fun celsiusToFahrenheit(celsius: Float): Float {
        if (celsius == 0.0f) return 0.0f //No conversion for 0 as ring returns 0 as default value
        return (celsius * 9 / 5) + 32
    }

    fun fahrenheitToCelsius(fahrenheit: Float): Float {
        return ((fahrenheit - 32) * 5) / 9
    }

    /**
     * 体表温度算法（华氏度）Body surface temperature algorithm (Fahrenheit)
     *
     * @param tempValue 温度参数 Temperature parameter
     * @return
     */
    fun getFahrenheitBody(tempValue: Int): Float {
        var bleBodyTemp = 0f
        val value1 = 300 + tempValue
        val value2 = value1 * 9 / 5 + 320
        bleBodyTemp = value2.toFloat() / 10
        return bleBodyTemp
    }

    fun getDefaultHeightValue(heightUnitSystem: HeightUnitSystem): Int {
        return if (HeightUnitSystem.METRIC == heightUnitSystem) 170 else 67
    }

    fun getDefaultWeightValue(heightUnitSystem: HeightUnitSystem): Int {
        return if (HeightUnitSystem.METRIC == heightUnitSystem) 59 else 129
    }

    fun getAgeFromDOB(dob: String): Int {
        var age: Int = 0
        try {
            val birth = Calendar.getInstance()
            birth.time = DateFormats.dateFormat3().parse(dob)!!

            val today = Calendar.getInstance()

            val presentYear = today.get(Calendar.YEAR)
            val birthYear = birth.get(Calendar.YEAR)

            age = presentYear.minus(birthYear)

            if (presentYear < birthYear) {
                age--
            }
        } catch (e: Exception) {

        }

        return age
    }

    fun round(value: Double): String {
        return String.format(locale = Locale.US, "%.2f", value)
    }
}