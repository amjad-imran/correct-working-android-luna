package com.noisefit.watch


import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.DeviceFeatures
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import javax.inject.Inject

class WatchesSDK
@Inject
constructor(
    private var localDataStore: DataStoredInterface
) {

    fun getWatchType(connectedDevice: ColorFitDevice?): SDKWatchType {
        var cDevice = connectedDevice

        if (cDevice == null) {
            cDevice = localDataStore.getConnectedDevice()
        }

        when (cDevice?.deviceType) {
            DeviceType.COLORFIT_2.deviceType,
            DeviceType.COLORFIT_PRO_2.deviceType,
            DeviceType.COLORFIT_PRO_3.deviceType,
            DeviceType.COLORFIT_PRO_2_OXY.deviceType,
            DeviceType.NOISEFIT_ACTIVE.deviceType,
            DeviceType.NOISEFIT_ACTIVE_OTA.deviceType,
            DeviceType.NOISEFIT_AGILE_DFU.deviceType,
            DeviceType.NOISEFIT_AGILE.deviceType,
            DeviceType.NOISEFIT_AGILE_OTA.deviceType -> {
                return SDKWatchType.SDK_CF_PRO

            }

            DeviceType.COLORFIT_PRO.deviceType,
            DeviceType.COLORFIT_PRO_Y23.deviceType,
            DeviceType.NOISEFIT_ENDURE.deviceType,
            DeviceType.NOISE_QUBE.deviceType,
            DeviceType.NOISE_QUBE_O2.deviceType,
            DeviceType.ICON_2.deviceType,
            DeviceType.NOISE_THRIVE.deviceType,
            DeviceType.COLORFIT_QUAD_CALL.deviceType,
            DeviceType.ICON_3.deviceType,
            DeviceType.NOISE_BOUNCE.deviceType,
            DeviceType.NOISE_SPRINT.deviceType,
            DeviceType.COLORFIT_SPARK.deviceType,
            DeviceType.NOISEFIT_CANVAS.deviceType,
            DeviceType.COLORFIT_ICON_2_VISTA.deviceType,
            DeviceType.FORCE.deviceType,
            DeviceType.COLORFIT_VIVID_CALL.deviceType,
            DeviceType.NOISE_ICON_PLUS.deviceType,
            DeviceType.NOISE_ICON_BUZZ.deviceType,
            DeviceType.QUBE_2.deviceType,
            DeviceType.COLORFIT_THRILL.deviceType,
            DeviceType.NOISEFIT_TRIUMPH.deviceType,
            DeviceType.COLORFIT_CALIBER3_PLUS.deviceType,
            DeviceType.COLORFIT_MACRO.deviceType,
            DeviceType.NOISEFIT_VENTURE.deviceType -> {
                return SDKWatchType.SDK_QUBE
            }

            DeviceType.NOISEFIT_EVOLVE.deviceType,
            DeviceType.NOISEFIT_EVOLVE_SPORT.deviceType,
            DeviceType.NOISE_EVOLVE_2.deviceType,
            DeviceType.COLORFIT_PULSE_2.deviceType,
            DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType,
            DeviceType.NOISE_EVOLVE_2_PLAY.deviceType -> {
                return SDKWatchType.SDK_EVOLVE
            }

            DeviceType.NOISEFIT_HYBRID.deviceType,
            DeviceType.COLORFIT_NAV.deviceType,
            DeviceType.COLORFIT_VISION.deviceType -> {
                return SDKWatchType.SDK_HYBRID
            }

            DeviceType.COLORFIT_NAV_PLUS.deviceType,
            DeviceType.NOISE_ULTRA.deviceType,
            DeviceType.COLORFIT_PULSE.deviceType,
            DeviceType.COLORFIT_BRIO.deviceType,
            DeviceType.COLORFIT_BRIO_PRO.deviceType,
            DeviceType.COLORFIT_ULTRA_2.deviceType,
            DeviceType.COLORFIT_CALIBER.deviceType,
            DeviceType.COLORFIT_BEAT.deviceType,
            DeviceType.XFIT.deviceType,
            DeviceType.COLORFIT_GRAND.deviceType,
            DeviceType.XFIT2.deviceType,
            DeviceType.COLORFIT_CALIBER_2.deviceType,
            DeviceType.COLORFIT_ULTRA_BUZZ.deviceType,
            DeviceType.COLORFIT_ULTRA_2_RP_EDITION.deviceType,
            DeviceType.COLORFIT_VISION_BUZZ.deviceType -> {
                return SDKWatchType.SDK_NAV_PLUS
            }

            DeviceType.COLORFIT_ULTRA_2_LITE.deviceType,
            DeviceType.COLORFIT_VISION_2.deviceType,
            DeviceType.NOISEFIT_TWIST.deviceType,
            DeviceType.NOISEFIT_CURVE.deviceType,
            DeviceType.NOISEFIT_ARC.deviceType,
            DeviceType.NOISEFIT_HALO.deviceType,
            DeviceType.NOISEFIT_ORIGIN.deviceType,
            DeviceType.NOISEFIT_EVOLVE_4.deviceType,
            DeviceType.NOISEFIT_HALO_PLUS.deviceType,
            DeviceType.COLORFIT_PRO_4.deviceType,
            DeviceType.COLORFIT_PRO_4_GPS.deviceType,
            DeviceType.COLORFIT_PRO_4_ALPHA.deviceType,
            DeviceType.VISION_2_BUZZ.deviceType,
            DeviceType.COLORFIT_PULSE_2_MAX.deviceType,
            DeviceType.COLORFIT_LOOP.deviceType,
            DeviceType.COLORFIT_VICTOR.deviceType,
            DeviceType.NOISEFIT_VORTEX.deviceType,
            DeviceType.NOISEFIT_EVOLVE_3.deviceType,
            DeviceType.NOISEFIT_FUSE_PLUS.deviceType,
            DeviceType.NOISEFIT_ARC_PLUS.deviceType,
            DeviceType.NOISEFIT_FUSE.deviceType,
            DeviceType.PULSE_GO_BUZZ.deviceType,
            DeviceType.COLORFIT_CALIBER_GO.deviceType,
            DeviceType.COLORFIT_CALIBER_2_BUZZ.deviceType,
            DeviceType.COLORFIT_CALIBER_BUZZ.deviceType,
            DeviceType.COLORFIT_ULTRA_2_BUZZ.deviceType,
            DeviceType.NOISEFIT_CREW.deviceType,
            DeviceType.NOISEFIT_CREW_PRO.deviceType,
            DeviceType.NOISEFIT_METTLE.deviceType,
            DeviceType.NOISEFIT_TWIST_PRO.deviceType,
            DeviceType.NOISEFIT_METALLIX.deviceType,
            DeviceType.COLORFIT_PULSE_3.deviceType,
            DeviceType.COLORFIT_PRIMUS.deviceType,
            DeviceType.NOISEFIT_QUAD_CALL_MAX.deviceType,
            DeviceType.NOISEFIT_FORCE_PLUS.deviceType,
            DeviceType.NOISEFIT_LUNA.deviceType,
            DeviceType.COLORFIT_VISION_3.deviceType,
            DeviceType.ULTRA_3.deviceType,
            DeviceType.COLORFIT_ORE.deviceType,
            DeviceType.COLORFIT_PRO_5_47MM.deviceType,
            DeviceType.COLORFIT_PRO_5_44MM.deviceType,
            DeviceType.NOISEFIT_ACTIVE_2.deviceType,
            DeviceType.COLORFIT_CHROME.deviceType,
            DeviceType.NOISEFIT_ENDEAVOUR.deviceType -> {
                return SDKWatchType.SDK_ZH
            }

            DeviceType.COLORFIT_MIGHTY.deviceType,
            DeviceType.NOISEFIT_NOVA.deviceType -> {
                return SDKWatchType.SDK_RYEEX
            }


            else -> {
                throw IllegalArgumentException("Unsupported connected device found ${connectedDevice?.getDeviceLogInfo()}")
            }
        }
    }


    fun getWatchType(): SDKWatchType? {
        val connectedDevice = localDataStore.getConnectedDevice() ?: return null

        return getWatchType(connectedDevice)
    }

    fun getDevice(): ColorFitDevice? {
        return localDataStore.getConnectedDevice()
    }

    fun getDevicesForLocation(): Boolean {
        val device = localDataStore.getConnectedDevice()
        when (device?.deviceType) {

            DeviceType.COLORFIT_CALIBER.deviceType,
            DeviceType.COLORFIT_GRAND.deviceType,
            DeviceType.XFIT2.deviceType -> {
                return true
            }
        }
        return false
    }

    fun isCaloriesSupported(): Boolean {
        val watchesSDK = getWatchType()
        if (watchesSDK == SDKWatchType.SDK_NAV_PLUS || watchesSDK == SDKWatchType.SDK_QUBE) {
            return false
        }
        return true
    }

    /**
     * Returns minimum allowed battery level
     * for OTA/ Watchface/AGPS
     */
    fun getMinimumBatteryLevel(): Int {

        val watchType = getWatchType()
        return if (watchType == SDKWatchType.SDK_NAV_PLUS ||
            watchType == SDKWatchType.SDK_ZH ||
            watchType == SDKWatchType.SDK_RYEEX
        ) {
            20
        } else {
            30
        }
    }

    /**
     * Return 0-> square
     * 1-> Circle
     */
    fun getWatchForm(): WatchForm {
        val deviceFeatures = localDataStore.getDeviceFeatures() ?: return WatchForm.SQUARE
        return getWatchForm(deviceFeatures)
    }

    fun watchHasAGPS(): Boolean {
        val connectedDevice = localDataStore.getConnectedDevice() ?: return false
        return watchHasAGPS(connectedDevice)
    }

    private fun watchHasAGPS(connectedDevice: ColorFitDevice): Boolean {
        return when (connectedDevice.deviceType) {
            DeviceType.COLORFIT_PRO_4_GPS.deviceType -> {
                true
            }

            else -> {
                false
            }
        }
    }

    fun getMaxContactsToAdd(connectedDevice: ColorFitDevice?): Int {
        if (connectedDevice == null) return 10


        return when (getWatchType(connectedDevice)) {
            SDKWatchType.SDK_NAV_PLUS,
            SDKWatchType.SDK_ZH -> {
                10
            }

            SDKWatchType.SDK_QUBE -> {
                8
            }

            else -> {
                10
            }
        }
    }

    fun getMaxSOSContactsToAdd(connectedDevice: ColorFitDevice?): Int {
        if (connectedDevice == null) return 5


        return when (getWatchType(connectedDevice)) {
            SDKWatchType.SDK_NAV_PLUS,
            SDKWatchType.SDK_ZH -> {
                1
            }

            SDKWatchType.SDK_QUBE -> {
                8
            }

            else -> {
                10
            }
        }
    }

    fun getWatchWidthHeight(): Pair<Int, Int> {
        val deviceFeatures = localDataStore.getDeviceFeatures() ?: return Pair(0, 0)
        return Pair(deviceFeatures.deviceWidth ?: 0, deviceFeatures.deviceHeight ?: 0)
    }

    fun getWatchFaceGif(): String {
        val deviceFeatures = localDataStore.getDeviceFeatures()

        if (deviceFeatures == null || deviceFeatures.cwGif.isEmpty()) {
            return ""
        }

        return deviceFeatures.cwGif
    }

    private fun getWatchForm(deviceFeatures: DeviceFeatures?): WatchForm {
        val screenType = deviceFeatures?.screenType
        return when (screenType?.lowercase()) {
            "circular" -> {
                WatchForm.CIRCLE
            }

            "arc" -> {
                WatchForm.ARC
            }

            else -> {
                WatchForm.SQUARE
            }
        }
    }


    fun hasCategoryWatchFace(): Boolean {
        val deviceFeatures = localDataStore.getDeviceFeatures() ?: return false

        if (deviceFeatures.hasWfCategory == 1) {
            return true
        }
        return false
    }


}

enum class SDKWatchType {
    SDK_ZH,
    SDK_NAV_PLUS,
    SDK_HYBRID,
    SDK_QUBE,
    SDK_EVOLVE,
    SDK_CF_PRO,
    SDK_RYEEX
}

enum class WatchForm(val type: String) {
    SQUARE("square"), CIRCLE("circular"), ARC("arc")
}