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


        val watch = localDataStore.getConnectedDevice()?.deviceType

//        if (watch.isNullOrEmpty()) {
//            watch = localDataStore.getConnectedDevice()?.deviceType ?: return "$distanceInMeter"
//        }

        var finalDistance = ""
        when (watch?.lowercase()) {
            DeviceType.NOISE_EVOLVE_2.deviceType, DeviceType.COLORFIT_PULSE_2.deviceType,
            DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType, DeviceType.NOISE_EVOLVE_2_PLAY.deviceType -> {
                finalDistance =
                    DistanceUtil.getDistanceForEvolve(distanceInMeter, unit ?: Units.METRIC)
                        .toDouble().roundToNearestDecimal()
                LOGS.d("formatDistance $finalDistance")
            }
            DeviceType.COLORFIT_PULSE.deviceType,
            DeviceType.NOISE_ICON_BUZZ.deviceType,
            DeviceType.NOISE_ICON_PLUS.deviceType,
            DeviceType.ICON_MAX.deviceType,
            DeviceType.ICON_2.deviceType,
            DeviceType.NOISE_THRIVE.deviceType,
            DeviceType.COLORFIT_QUAD_CALL.deviceType,
            DeviceType.COLORFIT_VIVID_CALL.deviceType,
            DeviceType.ICON_3.deviceType,
            DeviceType.NOISE_BOUNCE.deviceType,
            DeviceType.NOISE_SPRINT.deviceType,
            DeviceType.COLORFIT_SPARK.deviceType,
            DeviceType.NOISEFIT_CANVAS.deviceType,
            DeviceType.NOISEFIT_TRIUMPH.deviceType,
            DeviceType.COLORFIT_THRILL.deviceType,
            DeviceType.FORCE.deviceType,
            DeviceType.COLORFIT_ICON_2_VISTA.deviceType,
            DeviceType.COLORFIT_PRO_2.deviceType,
            DeviceType.COLORFIT_CALIBER3_PLUS.deviceType,
            DeviceType.NOISEFIT_VENTURE.deviceType
            -> {
                finalDistance =
                    DistanceUtil.getDistanceForPulse(distanceInMeter, unit ?: Units.METRIC)
                        .toDouble().roundDownDecimal()

                LOGS.d("formatDistance $finalDistance")
            }
            DeviceType.COLORFIT_GRAND.deviceType, DeviceType.COLORFIT_CALIBER.deviceType, DeviceType.COLORFIT_CALIBER_2.deviceType -> {
                finalDistance =
                    DistanceUtil.getDistanceFromMeters(distanceInMeter, unit ?: Units.METRIC)
                        .upToNDecimal(1)

                LOGS.d(
                    "formatDistance $finalDistance" + DistanceUtil.getDistanceForPulse(
                        distanceInMeter,
                        unit ?: Units.METRIC
                    )
                )
            }
            DeviceType.COLORFIT_ULTRA_BUZZ.deviceType, DeviceType.COLORFIT_VISION_BUZZ.deviceType -> {
                finalDistance =
                    DistanceUtil.getDistanceFromMeters(distanceInMeter, unit ?: Units.METRIC)
                        .roundToNearestDecimalFloor(1).toString()

                LOGS.d("formatDistance $finalDistance")
            }
            DeviceType.COLORFIT_ULTRA_2_LITE.deviceType, DeviceType.COLORFIT_VISION_2.deviceType,
            DeviceType.COLORFIT_PRO_4.deviceType,
            DeviceType.COLORFIT_PULSE_3.deviceType,
            DeviceType.COLORFIT_PRIMUS.deviceType,
            DeviceType.NOISEFIT_TWIST.deviceType,
            DeviceType.NOISEFIT_CURVE.deviceType,
            DeviceType.NOISEFIT_ARC.deviceType,
            DeviceType.NOISEFIT_HALO.deviceType,
           DeviceType.NOISEFIT_ORIGIN.deviceType ,
            DeviceType.NOISEFIT_EVOLVE_4.deviceType,
            DeviceType.NOISEFIT_HALO_PLUS.deviceType,
            DeviceType.COLORFIT_PRO_4_GPS.deviceType,
            DeviceType.COLORFIT_PRO_4_ALPHA.deviceType,
            DeviceType.NOISEFIT_QUAD_CALL_MAX.deviceType,
            DeviceType.COLORFIT_CALIBER_2_BUZZ.deviceType,
            DeviceType.NOISEFIT_EVOLVE_3.deviceType,
            DeviceType.NOISEFIT_FUSE_PLUS.deviceType,
            DeviceType.NOISEFIT_ARC_PLUS.deviceType,
            DeviceType.NOISEFIT_FUSE.deviceType,
            DeviceType.NOISEFIT_VORTEX.deviceType,
            DeviceType.NOISEFIT_FORCE_PLUS.deviceType,
            DeviceType.PULSE_GO_BUZZ.deviceType, DeviceType.VISION_2_BUZZ.deviceType,
            DeviceType.COLORFIT_CALIBER_BUZZ.deviceType,
            DeviceType.COLORFIT_PULSE_2_MAX.deviceType,
            DeviceType.COLORFIT_LOOP.deviceType,
            DeviceType.COLORFIT_VICTOR.deviceType,
            DeviceType.COLORFIT_CALIBER_2.deviceType,
            DeviceType.COLORFIT_CALIBER_GO.deviceType,
            DeviceType.COLORFIT_ULTRA_2_BUZZ.deviceType,
            DeviceType.NOISEFIT_CREW.deviceType,
            DeviceType.NOISEFIT_CREW_PRO.deviceType,
            DeviceType.NOISEFIT_METTLE.deviceType,
            DeviceType.NOISEFIT_METALLIX.deviceType,
            DeviceType.NOISEFIT_TWIST_PRO.deviceType,
            DeviceType.COLORFIT_VISION_3.deviceType,
            DeviceType.ULTRA_3.deviceType,
            DeviceType.COLORFIT_ORE.deviceType,
            DeviceType.COLORFIT_PRO_5_47MM.deviceType,
            DeviceType.COLORFIT_PRO_5_44MM.deviceType,
            DeviceType.NOISEFIT_ACTIVE_2.deviceType,
            DeviceType.COLORFIT_CHROME.deviceType,
            DeviceType.NOISEFIT_ENDEAVOUR.deviceType -> {
                finalDistance =
                    DistanceUtil.getDistanceFromMeters(distanceInMeter, unit ?: Units.METRIC)
                        .roundToNearestDecimalFloor(2).toString()

                LOGS.d("formatDistance $finalDistance")
            }
            DeviceType.COLORFIT_MIGHTY.deviceType, DeviceType.NOISEFIT_NOVA.deviceType -> {
                finalDistance =
                    DistanceUtil.getDistanceFromMeters(distanceInMeter, unit ?: Units.METRIC)
                        .toFloat().roundToNearestDecimalUp().toString()

                LOGS.d("formatDistance $finalDistance")
            }
            else -> {
                finalDistance =
                    DistanceUtil.getDistanceFromMeters(distanceInMeter, unit ?: Units.METRIC)
                        .truncateDecimal(2)
            }
        }

        return finalDistance
    }

    fun formatActivityDistance(distanceInMeter: Int, unit: Units?): String {

        val watch = localDataStore.getConnectedDevice()?.deviceType
        var finalDistance = ""
        when (watch?.lowercase()) {
            DeviceType.NOISE_EVOLVE_2.deviceType, DeviceType.COLORFIT_PULSE_2.deviceType, DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType, DeviceType.NOISE_EVOLVE_2_PLAY.deviceType -> {
                finalDistance =
                    DistanceUtil.getDistanceForEvolve(distanceInMeter, unit ?: Units.METRIC)
                        .toDouble().roundToNearestDecimal()
                LOGS.d("formatDistance $finalDistance")
            }
            DeviceType.COLORFIT_PULSE.deviceType,
            DeviceType.NOISE_ICON_BUZZ.deviceType,
            DeviceType.NOISE_ICON_PLUS.deviceType,
            DeviceType.FORCE.deviceType,
            DeviceType.COLORFIT_QUAD_CALL.deviceType,
            DeviceType.ICON_MAX.deviceType,
            DeviceType.ICON_2.deviceType,
            DeviceType.NOISE_THRIVE.deviceType,
            DeviceType.COLORFIT_VIVID_CALL.deviceType,
            DeviceType.ICON_3.deviceType,
            DeviceType.NOISE_BOUNCE.deviceType,
            DeviceType.NOISE_SPRINT.deviceType,
            DeviceType.COLORFIT_SPARK.deviceType,
            DeviceType.NOISEFIT_CANVAS.deviceType,
            DeviceType.NOISEFIT_TRIUMPH.deviceType,
            DeviceType.COLORFIT_THRILL.deviceType,
            DeviceType.COLORFIT_ICON_2_VISTA.deviceType,
            DeviceType.COLORFIT_PRO_2.deviceType,
            DeviceType.COLORFIT_CALIBER3_PLUS.deviceType,
            DeviceType.NOISEFIT_VENTURE.deviceType-> {
                finalDistance =
                    DistanceUtil.getDistanceForPulse(distanceInMeter, unit ?: Units.METRIC)
                        .toDouble().roundDownDecimal()

                LOGS.d("formatDistance $finalDistance")
            }
            DeviceType.COLORFIT_GRAND.deviceType,
            DeviceType.XFIT2.deviceType,
            DeviceType.COLORFIT_CALIBER_2.deviceType,
            DeviceType.COLORFIT_CALIBER.deviceType -> {
                finalDistance =
                    DistanceUtil.getDistanceFromMeters(distanceInMeter, unit ?: Units.METRIC)
                        .roundToNearestDecimalFloor(1).toString()

                LOGS.d("formatDistance $finalDistance")
            }
            DeviceType.COLORFIT_ULTRA_BUZZ.deviceType, DeviceType.COLORFIT_VISION_BUZZ.deviceType -> {
                finalDistance =
                    DistanceUtil.getDistanceFromMeters(distanceInMeter, unit ?: Units.METRIC)
                        .roundToNearestDecimalFloor(2).toString()

                LOGS.d("formatDistance $finalDistance")
            }
            else -> {
                finalDistance =
                    DistanceUtil.getDistanceFromMeters(distanceInMeter, unit ?: Units.METRIC)
                        .upTo2Decimal().toString()
            }
        }

        return finalDistance
    }

    fun formatAvgStride(avgStride: Int?, unit: Units): String {
        if (avgStride == null) {
            return "$avgStride cm"
        }
        val watch = localDataStore.getConnectedDevice()?.deviceType ?: return "$avgStride"
        var finalStride = ""
        when (watch.lowercase()) {
            DeviceType.NOISE_EVOLVE_2.deviceType, DeviceType.NOISE_EVOLVE_2_PLAY.deviceType, DeviceType.COLORFIT_PULSE_2.deviceType,
            DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType -> {
                finalStride = DistanceUtil.convertCmsToInchEvolve(avgStride, unit)

                LOGS.d("finalStride $finalStride $avgStride")
            }
            else -> {
                finalStride = "$avgStride"
            }
        }
        return finalStride
    }

    fun formatCalories(value: Double): String {
        val watch = localDataStore.getConnectedDevice()?.deviceType ?: return ""
        //  LOGS.d("formatDistance $value")
        var calories = ""
        when (watch.lowercase()) {
            DeviceType.NOISE_EVOLVE_2.deviceType, DeviceType.NOISE_EVOLVE_2_PLAY.deviceType,
            DeviceType.COLORFIT_PULSE_2.deviceType, DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType -> {
                calories = value.roundToNearestDecimal()
            }
            else -> {
                calories = value.upToNDecimal(2)
            }
        }
        return calories
    }

    fun averageSpeed(distance: Long?, unit: Units?, duration: Long?): String {


        val watch = localDataStore.getConnectedDevice()?.deviceType ?: return ""

        if (distance == null) return ""
        if (duration == null) return ""

        when (watch.lowercase()) {
            DeviceType.NOISE_EVOLVE_2.deviceType, DeviceType.NOISE_EVOLVE_2_PLAY.deviceType, DeviceType.COLORFIT_PULSE_2.deviceType,
            DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType -> {
                return if (unit == Units.METRIC) {
                    ActivityConvertUtils.bigDecimalFormatUp((distance / 1000f).roundToNearestDecimalUp() / (duration / 3600f))
                } else {
                    ActivityConvertUtils.bigDecimalFormatUp((distance / 1000f).roundToNearestDecimalDown() / 1.61f / (duration / 3600f))
                }
            }
            DeviceType.COLORFIT_MIGHTY.deviceType, DeviceType.NOISEFIT_NOVA.deviceType -> {
                return if (unit == Units.METRIC) {
                    ActivityConvertUtils.bigDecimalFormatUp((distance * 3.6f / duration).roundToNearestDecimalUp())
                } else {
                    ActivityConvertUtils.bigDecimalFormatUp((distance * 3.6f * 0.62137f / duration).roundToNearestDecimalUp())
                }
            }
            else -> {
                return ActivityConvertUtils.averageSpeed(
                    unit ?: Units.METRIC,
                    distance,
                    duration
                )
            }
        }
    }

}