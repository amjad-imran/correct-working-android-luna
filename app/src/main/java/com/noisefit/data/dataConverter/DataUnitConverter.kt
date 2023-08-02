package com.noisefit.data.dataConverter

import com.noisefit_commans.common.*
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.ActivityConvertUtils
import com.noisefit_commans.utils.DistanceUtil
import com.noisefit_commans.utils.LOGS
import javax.inject.Inject


class DataUnitConverter
@Inject
constructor(
    val localDataStore: DataStoredInterface
) {

    fun distanceUnit(unit: Units?): String {
        return if (unit == Units.IMPERIAL) {
            "mi"
        } else {
            "km"
        }
    }

    fun bodyTempUnit(unit: Units?): String {
        return if (unit == Units.IMPERIAL) {
            "°F"
        } else {
            "°C"
        }
    }

    fun formatBodyTemp(value: Float, unit: Units): String {
        if (value == 0f) {
            return "0"
        }
        if (unit == Units.METRIC) {
            return "${value.roundToNearestDecimalFloor(1)}"
        }

        return "${convertCelToFehTemp(value).roundToNearestDecimalFloor(1)}"
    }

    fun formatBodyTempInFloat(value: Float, unit: Units): Float {
        if (value == 0f) {
            return 0f
        }
        if (unit == Units.METRIC) {
            return value.roundToNearestDecimalFloor(1)
        }

        return convertCelToFehTemp(value).roundToNearestDecimalFloor(1)
    }


    private fun convertCelToFehTemp(value: Float): Float {
        return (value * 9) / 5 + 32
    }


    //  fun formatDistance(distanceInMeter: Int, unit: Units?): String {
//        var watch = sessionManager.connectedDevice.value?.deviceType
//        if (watch.isNullOrEmpty()) {
//            watch = localDataStore.getConnectedDevice()?.deviceType ?: return "$distanceInMeter"
//        }
//
//        when (watch.lowercase()) {
//            DeviceType.COLORFIT_CALIBER.deviceType,
//            DeviceType.COLORFIT_GRAND.deviceType -> {
//                return DistanceUtil.getDistanceFromMeters(distanceInMeter, unit ?: Units.METRIC)
//                    .upToNDecimal(1)
//            }
//        }

//        return DistanceUtil.getDistanceFromMeters(distanceInMeter, unit ?: Units.METRIC)
//            .truncateDecimal(2)
//    }

    fun formatDistance(distanceInMeter: Long, unit: Units?): String {
//        var watch = sessionManager.connectedDevice.value?.deviceType
//        if (watch.isNullOrEmpty()) {
//            watch = localDataStore.getConnectedDevice()?.deviceType ?: return "$distanceInMeter"
//        }
//
//        when (watch.lowercase()) {
//            DeviceType.COLORFIT_CALIBER.deviceType,
//            DeviceType.COLORFIT_GRAND.deviceType -> {
//                return DistanceUtil.getDistanceFromMeters(distanceInMeter, unit ?: Units.METRIC)
//                    .upToNDecimal(1)
//            }
//        }

        return formatDistance(distanceInMeter.toInt(), unit)
    }


    fun formatDistanceGoal(distanceInMeter: Int, unit: Units?): String {
        return DistanceUtil.getDistanceFromMeters(distanceInMeter, unit ?: Units.METRIC)
            .roundToNearestDecimal()
    }

    fun formatDistance(distanceInMeter: Int, unit: Units?): String {
        var finalDistance  =
            DistanceUtil.getDistanceFromMeters(distanceInMeter, unit ?: Units.METRIC)
                .roundToNearestDecimalFloor(2).toString()

        LOGS.d("formatDistance $finalDistance")

        return finalDistance
    }

    fun formatActivityDistance(distanceInMeter: Int, unit: Units?): String {

        var finalDistance  =
            DistanceUtil.getDistanceForPulse(distanceInMeter, unit ?: Units.METRIC)
                .toDouble().roundDownDecimal()

        LOGS.d("formatDistance $finalDistance")
        return finalDistance
    }

}