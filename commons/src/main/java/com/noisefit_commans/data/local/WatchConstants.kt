//package com.noisefit_commans.data.local
//
//import com.noisefit_commans.models.ColorFitDevice
//import com.noisefit_commans.models.DeviceType
//
//object WatchConstants {
//
//    //368, 448
//    //368, 448
//    fun getWatchScreenWidth(connectedDevice: ColorFitDevice?): Float {
//        if (connectedDevice == null) return 0f
//        return when (connectedDevice.deviceType) {
//            DeviceType.NOISE_ULTRA.deviceType, DeviceType.COLORFIT_ULTRA_BUZZ.deviceType, DeviceType.COLORFIT_VISION_BUZZ.deviceType -> 320f
//            DeviceType.COLORFIT_BRIO.deviceType, DeviceType.XFIT.deviceType, DeviceType.COLORFIT_BRIO_PRO.deviceType -> 360f
//            DeviceType.COLORFIT_PULSE.deviceType, DeviceType.COLORFIT_BEAT.deviceType, DeviceType.COLORFIT_CALIBER.deviceType,
//            DeviceType.COLORFIT_GRAND.deviceType, DeviceType.XFIT2.deviceType -> 240f
//            DeviceType.COLORFIT_NAV_PLUS.deviceType,
//            DeviceType.COLORFIT_PRIMUS.deviceType -> 320f
//            DeviceType.COLORFIT_ULTRA_2_LITE.deviceType, DeviceType.COLORFIT_ULTRA_2.deviceType, DeviceType.COLORFIT_VISION.deviceType, DeviceType.COLORFIT_VISION_2.deviceType, DeviceType.COLORFIT_ULTRA_2_BUZZ.deviceType, DeviceType.VISION_2_BUZZ.deviceType -> 368f
//            DeviceType.NOISE_EVOLVE_2.deviceType, DeviceType.NOISE_EVOLVE_2_PLAY.deviceType -> 390f
//            DeviceType.COLORFIT_ULTRA_2.deviceType, DeviceType.COLORFIT_VISION.deviceType -> 368f
//            DeviceType.NOISE_ICON_BUZZ.deviceType,
//            DeviceType.NOISE_ICON_PLUS.deviceType,
//            DeviceType.FORCE.deviceType,
//            DeviceType.COLORFIT_QUAD_CALL.deviceType,
//            DeviceType.ICON_2.deviceType,
//            DeviceType.NOISE_THRIVE.deviceType,
//            DeviceType.COLORFIT_VIVID_CALL.deviceType,
//            DeviceType.ICON_3.deviceType,
//            DeviceType.NOISE_BOUNCE.deviceType,
//            DeviceType.NOISE_SPRINT.deviceType,
//            DeviceType.COLORFIT_SPARK.deviceType,
//            DeviceType.NOISEFIT_CANVAS.deviceType,
//            DeviceType.COLORFIT_PULSE_2.deviceType,
//            DeviceType.COLORFIT_ICON_2_VISTA.deviceType,
//            DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType -> 240f
//            DeviceType.NOISEFIT_ENDURE.deviceType -> 240f
//            DeviceType.NOISE_QUBE.deviceType, DeviceType.NOISEFIT_ARC.deviceType, DeviceType.NOISE_QUBE_O2.deviceType, DeviceType.NOISEFIT_CURVE.deviceType, DeviceType.NOISEFIT_TWIST.deviceType, DeviceType.NOISEFIT_CREW.deviceType, DeviceType.NOISEFIT_METTLE.deviceType,
//            DeviceType.NOISEFIT_CREW_PRO.deviceType, DeviceType.NOISEFIT_METALLIX.deviceType,
//            DeviceType.NOISEFIT_TWIST_PRO.deviceType, DeviceType.QUBE_2.deviceType -> 240f
//            DeviceType.COLORFIT_PRO_4.deviceType -> 356f
//            DeviceType.COLORFIT_PRO_4_GPS.deviceType -> 240f
//            DeviceType.COLORFIT_PRO_4_ALPHA.deviceType, DeviceType.NOISEFIT_QUAD_CALL_MAX.deviceType -> 368f
//            DeviceType.COLORFIT_CALIBER_2.deviceType -> 240f
//            DeviceType.PULSE_GO_BUZZ.deviceType -> 240f
//            DeviceType.COLORFIT_CALIBER_GO.deviceType,
//            DeviceType.COLORFIT_CALIBER_2_BUZZ.deviceType,
//            DeviceType.COLORFIT_CALIBER_BUZZ.deviceType,
//            DeviceType.COLORFIT_PULSE_2_MAX.deviceType,
//            DeviceType.COLORFIT_LOOP.deviceType,
//            DeviceType.COLORFIT_PULSE_3.deviceType,
//            DeviceType.NOISEFIT_FUSE.deviceType,
//            DeviceType.COLORFIT_VICTOR.deviceType -> 240f
//
//            DeviceType.ULTRA_3.deviceType, DeviceType.COLORFIT_VISION_3.deviceType -> 410f
//            DeviceType.NOISEFIT_EVOLVE_3.deviceType,
//            DeviceType.NOISEFIT_VORTEX.deviceType,
//            DeviceType.NOISEFIT_FORCE_PLUS.deviceType,
//            DeviceType.NOISEFIT_HALO.deviceType,
//            DeviceType.NOISEFIT_HALO_PLUS.deviceType,
//            DeviceType.NOISEFIT_FUSE_PLUS.deviceType,
//            DeviceType.NOISEFIT_ARC_PLUS.deviceType-> 466f
//            DeviceType.COLORFIT_MIGHTY.deviceType -> 240f
//            DeviceType.NOISEFIT_NOVA.deviceType -> 466f
//            else -> 0f
//        }
//    }
//
//    fun getWatchScreenHeight(connectedDevice: ColorFitDevice?): Float {
//        if (connectedDevice == null) return 0f
//
//        return when (connectedDevice.deviceType) {
//            DeviceType.NOISE_ULTRA.deviceType, DeviceType.COLORFIT_ULTRA_BUZZ.deviceType, DeviceType.COLORFIT_VISION_BUZZ.deviceType -> 385f
//            DeviceType.COLORFIT_BRIO.deviceType, DeviceType.XFIT.deviceType, DeviceType.COLORFIT_BRIO_PRO.deviceType -> 400f
//            DeviceType.COLORFIT_PULSE.deviceType, DeviceType.COLORFIT_BEAT.deviceType, DeviceType.NOISEFIT_FUSE.deviceType -> 240f
//            DeviceType.COLORFIT_CALIBER.deviceType, DeviceType.COLORFIT_GRAND.deviceType,
//            DeviceType.COLORFIT_PULSE_3.deviceType,
//            DeviceType.XFIT2.deviceType -> 280f
//            DeviceType.COLORFIT_PRIMUS.deviceType -> 380f
//            DeviceType.COLORFIT_NAV_PLUS.deviceType -> 320f
//            DeviceType.COLORFIT_ULTRA_2_LITE.deviceType, DeviceType.COLORFIT_ULTRA_2.deviceType, DeviceType.COLORFIT_VISION.deviceType, DeviceType.COLORFIT_VISION_2.deviceType -> 448f
//            DeviceType.NOISE_EVOLVE_2.deviceType, DeviceType.NOISE_EVOLVE_2_PLAY.deviceType -> 390f
//            DeviceType.COLORFIT_ULTRA_2.deviceType, DeviceType.COLORFIT_VISION.deviceType, DeviceType.COLORFIT_ULTRA_2_BUZZ.deviceType, DeviceType.VISION_2_BUZZ.deviceType -> 448f
//            DeviceType.NOISE_ICON_BUZZ.deviceType,
//            DeviceType.NOISE_ICON_PLUS.deviceType,
//            DeviceType.FORCE.deviceType,
//            DeviceType.ICON_2.deviceType,
//            DeviceType.NOISE_THRIVE.deviceType,
//            DeviceType.COLORFIT_QUAD_CALL.deviceType,
//            DeviceType.COLORFIT_VIVID_CALL.deviceType,
//            DeviceType.ICON_3.deviceType,
//            DeviceType.NOISE_BOUNCE.deviceType,
//            DeviceType.NOISE_SPRINT.deviceType,
//            DeviceType.COLORFIT_SPARK.deviceType,
//            DeviceType.NOISEFIT_CANVAS.deviceType,
//            DeviceType.COLORFIT_ICON_2_VISTA.deviceType,
//            DeviceType.COLORFIT_PULSE_2.deviceType,
//            DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType -> 280f
//            DeviceType.NOISEFIT_ENDURE.deviceType, DeviceType.NOISEFIT_ARC.deviceType, DeviceType.NOISEFIT_CURVE.deviceType, DeviceType.NOISEFIT_TWIST.deviceType, DeviceType.NOISEFIT_METTLE.deviceType, DeviceType.NOISEFIT_CREW.deviceType, DeviceType.NOISEFIT_CREW_PRO.deviceType, DeviceType.NOISEFIT_METALLIX.deviceType, DeviceType.NOISEFIT_TWIST_PRO.deviceType -> 240f
//            DeviceType.NOISE_QUBE.deviceType, DeviceType.NOISE_QUBE_O2.deviceType, DeviceType.QUBE_2.deviceType -> 240f
//            DeviceType.COLORFIT_PRO_4.deviceType -> 400f
//            DeviceType.COLORFIT_PRO_4_GPS.deviceType -> 280f
//            DeviceType.COLORFIT_PRO_4_ALPHA.deviceType, DeviceType.NOISEFIT_QUAD_CALL_MAX.deviceType -> 448f
//            DeviceType.COLORFIT_CALIBER_2.deviceType -> 280f
//            DeviceType.PULSE_GO_BUZZ.deviceType -> 280f
//            DeviceType.COLORFIT_CALIBER_GO.deviceType,
//            DeviceType.COLORFIT_CALIBER_2_BUZZ.deviceType,
//            DeviceType.COLORFIT_CALIBER_BUZZ.deviceType,
//            DeviceType.COLORFIT_PULSE_2_MAX.deviceType,
//            DeviceType.COLORFIT_LOOP.deviceType,
//            DeviceType.COLORFIT_VICTOR.deviceType -> 280f
//            DeviceType.ULTRA_3.deviceType,DeviceType.COLORFIT_VISION_3.deviceType -> 502f
//            DeviceType.NOISEFIT_EVOLVE_3.deviceType,
//            DeviceType.NOISEFIT_VORTEX.deviceType,
//            DeviceType.NOISEFIT_FORCE_PLUS.deviceType,
//            DeviceType.NOISEFIT_HALO.deviceType,
//            DeviceType.NOISEFIT_HALO_PLUS.deviceType,
//            DeviceType.NOISEFIT_FUSE_PLUS.deviceType,
//            DeviceType.NOISEFIT_ARC_PLUS.deviceType,-> 466f
//            DeviceType.COLORFIT_MIGHTY.deviceType -> 286f
//            DeviceType.NOISEFIT_NOVA.deviceType -> 466f
//            else -> 0f
//        }
//    }
//}