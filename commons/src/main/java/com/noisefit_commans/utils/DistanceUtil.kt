package com.noisefit_commans.utils

import android.location.Location
import com.noisefit_commans.common.roundToNearestDecimal
import com.noisefit_commans.models.LocationDataModel
import com.noisefit_commans.models.Units
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToInt


object DistanceUtil {

    const val kDefaultMinimumAcceptableAccuracy = 40.0f
    const val KM_TO_METER = 1000f
    const val MI_TO_METER = 1609.34f

    const val METER_TO_MILE = 0.000621371f
    const val METER_TO_KM = 0.001

    const val KM_TO_MI = 0.621f
    const val MI_TO_KM = 1.609f
    const val ME_TO_KM = 0.001f

    const val CM_TO_INCH = 0.393701f
    const val INCH_TO_CM = 2.54f

    const val LBS_TO_KG = 0.453592f
    const val KG_TO_LBS = 2.20462f


    fun getDistanceInMetres(value: Double?, unit: Units): Int {
        if (value == null) return 0
        return if (unit == Units.METRIC) {
            (value * KM_TO_METER).roundToInt()
        } else {
            (value * MI_TO_METER).roundToInt()
        }
    }

    fun getDistanceBetweenTwoLatLng(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        unit: Units
    ): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        val distanceInMeters = results[0]
        if (unit == Units.IMPERIAL) {
            return metersToMiles(
                distanceInMeters.toDouble()
            )
        } else if (unit == Units.METRIC) {
            return metersToKm(
                distanceInMeters.toDouble()
            )
        }
        return 0.0
    }

    fun getDistanceFromMetres(value: Int, unit: Units): String {
        val convertedValue = if (unit == Units.METRIC) {
            (value / KM_TO_METER)
        } else {
            (value / MI_TO_METER)
        }

        val df = DecimalFormat(".#")
        return df.format(convertedValue)
    }

    fun getDistanceFromMeters(value: Int, unit: Units): Double {
        return if (unit == Units.METRIC) {
            metersToKm(value.toDouble())
        } else {
            metersToMiles(value.toDouble())
        }

    }

    fun getDistanceFromMeters(value: Long, unit: Units): Double {
        return if (unit == Units.METRIC) {
            metersToKm(value.toDouble())
        } else {
            metersToMiles(value.toDouble())
        }

    }

    fun getDistanceForEvolve(value: Int, unit: Units): Float {
        return if (unit == Units.METRIC) {
            value / 1000f
        } else {
            value / 1610f
        }

    }

    fun getDistanceForPulse(value: Int, unit: Units): Float {
        return if (unit == Units.METRIC) {
            value / 1000f
        } else {
            value / 1610f
        }

    }

//    fun convertAvgStrideCmToIn(value: Int): Float {
//        return value / 2.54f
//
//    }


    fun convertKmValueToMi(value: Int?): String {
        if (value == null) return "0"
        val df = DecimalFormat(".#",DecimalFormatSymbols(Locale.US))
        return df.format(value * KM_TO_MI)
    }

    fun convertCmsToInch(value: Int?): String {
        if (value == null) return "0"
        val df = DecimalFormat(".#",DecimalFormatSymbols(Locale.US))
        return df.format(value * CM_TO_INCH)
    }


    fun convertCmsToInchEvolve(value: Int?, unit: Units): String {
        if (value == null) return "0"

        if (unit == Units.METRIC) {
            return "$value"
        }
        return "${(value * 0.39).roundToNearestDecimal()}"
    }

    fun convertKgToLbs(value: Int?): String {
        if (value == null) return "0"
        val df = DecimalFormat(".#",DecimalFormatSymbols(Locale.US))
        return df.format(value * KG_TO_LBS)
    }

    fun convertCmToFeetAndInches(cm: Double): Pair<Int, Double> {
        val totalInches = cm / 2.54
        val feet = (totalInches / 12).toInt()
        val inches = totalInches % 12
        return Pair(feet, inches)
    }


    fun convertMiToKm(value: Int?): String {
        if (value == null) return "0"
        val df = DecimalFormat(".#",DecimalFormatSymbols(Locale.US))
        return df.format(value * MI_TO_KM)
    }

    fun convertMeterToKm(value: Int?): String {
        if (value == null) return "0"
        val df = DecimalFormat("#.#",DecimalFormatSymbols(Locale.US))
        return df.format(value * ME_TO_KM)
    }

    fun convertMeterToMiles(value: Int): String {
        val df = DecimalFormat("#.#",DecimalFormatSymbols(Locale.US))
        return df.format(metersToMiles(value.toDouble()))
    }


    fun convertInchToCms(value: Int?): String {
        if (value == null) return "0"
        val df = DecimalFormat(".#",DecimalFormatSymbols(Locale.US))
        return df.format(value * INCH_TO_CM)
    }

    fun convertLbsToKg(value: Int?): String {
        if (value == null) return "0"
        val df = DecimalFormat(".#",DecimalFormatSymbols(Locale.US))
        return df.format(value * LBS_TO_KG)
    }

    fun getCalculatedDistanceWithPause(locationArrayList: List<LocationDataModel?>): Double {
        var distance = 0.0
        var tempDistance = 0.0
        var mLastLongitudePoint = 0.0
        var mLastLatitudePoint = 0.0
        for (location in locationArrayList) {
            if (location!!.accuracy >= kDefaultMinimumAcceptableAccuracy) {

                continue
            }
            if (!location.isRunning) {
                tempDistance += distance
                distance = 0.0
                mLastLatitudePoint = 0.0
                mLastLongitudePoint = 0.0
                LOGS.d("getCalculatedDistanceWithPause ${location.isRunning}")
                continue
            }
            if (mLastLongitudePoint == 0.0 && mLastLatitudePoint == 0.0) {
                mLastLongitudePoint = location.longitude
                mLastLatitudePoint = location.latitude
                LOGS.d("getCalculatedDistanceWithPause first loop")
            } else {
                val nextLongitudePoint = location.longitude
                val nextLatitudePoint = location.latitude
                distance += distance(
                    mLastLatitudePoint, mLastLongitudePoint, nextLatitudePoint,
                    nextLongitudePoint
                )
                LOGS.d("getCalculatedDistanceWithPause $distance")
                mLastLongitudePoint = nextLongitudePoint
                mLastLatitudePoint = nextLatitudePoint
            }
        }
        if (tempDistance > 0) {
            distance += tempDistance
        }
        return distance
    }

    fun getCalculatedDistance(locationArrayList: List<LocationDataModel>): Int {
        var distance = 0
        var tempDistance = 0
        var mLastLongitudePoint = 0.0
        var mLastLatitudePoint = 0.0
        for (location in locationArrayList) {
            if (location.accuracy >= kDefaultMinimumAcceptableAccuracy) {
                continue
            }
            if (!location.isRunning) {
                tempDistance += distance
                distance = 0
                mLastLatitudePoint = 0.0
                mLastLongitudePoint = 0.0
                continue
            }
            if (mLastLongitudePoint == 0.0 && mLastLatitudePoint == 0.0) {
                mLastLongitudePoint = location.longitude
                mLastLatitudePoint = location.latitude
                println("first loop")
            } else {
                val nextLongitudePoint = location.longitude
                val nextLatitudePoint = location.latitude
                distance += distance(
                    mLastLatitudePoint, mLastLongitudePoint, nextLatitudePoint,
                    nextLongitudePoint
                )
                mLastLongitudePoint = nextLongitudePoint
                mLastLatitudePoint = nextLatitudePoint
            }
        }
        if (tempDistance > 0) {
            distance += tempDistance
        }
        return distance
    }

    private fun getDistance(distance: Double): Double {
        val decimalFormat = DecimalFormat("#.##", DecimalFormatSymbols(Locale.US))
        return decimalFormat.format(distance).toFloat().toDouble()
    }


    private fun distance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Int {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        val distanceInMeters = results[0]
        return distanceInMeters.roundToInt()
//        if (unit == Units.IMPERIAL) {
//            return metersToMiles(distanceInMeters.toDouble())
//        }
//        return if (unit == Units.METRIC) {
//            metersToKm(distanceInMeters.toDouble())
//        } else 0.0
    }

    private fun milesToKm(miles: Double): Double {
        return 1.609339952468872 * miles
    }

    private fun kmToMiles(km: Double): Double {
        return 0.6213709712028503 * km
    }

    private fun metersToKm(meters: Double): Double {
        return meters / 1000.0
    }

    private fun metersToMiles(meters: Double): Double {
        return 6.21371204033494E-4 * meters
    }

    private fun getDistanceInMeter(unit: String, distance: Double): Long {
        return if (unit == "mi") {
            milesToMeters(distance)
        } else kmToMeters(distance)
    }

    fun milesToMeters(miles: Double): Long {
        return (miles * 1609.3399658203125).toLong()
    }

    private fun kmToMeters(km: Double): Long {
        return (km * 1000.0).toLong()
    }

    fun centimeterToMeter(value: Float): Float {
        return (value / 100)
    }

    fun meterToCentimeter(value: Float): Float {
        return (value * 100)
    }
}