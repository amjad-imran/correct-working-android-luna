//package com.noisefit.watch
//
//import com.noisefit_commans.models.ColorFitDevice
//import com.noisefit_commans.models.DeviceType
//
//class Fikw {
//
//    fun getWatchType(connectedDevice: ColorFitDevice?): SDKWatchType {
//        val cDevice = connectedDevice
//
//
//
//        when (cDevice?.deviceType) {
//
//            DeviceType.NOISEFIT_EVOLVE.deviceType,
//            DeviceType.NOISEFIT_EVOLVE_SPORT.deviceType,
//            DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType,
//            DeviceType.NOISE_EVOLVE_2_PLAY.deviceType -> {
//                return SDKWatchType.SDK_EVOLVE
//            }
//
//            DeviceType.COLORFIT_BRIO_PRO.deviceType,
//            DeviceType.COLORFIT_BEAT.deviceType,
//            DeviceType.XFIT.deviceType,
//            DeviceType.XFIT2.deviceType,
//            DeviceType.COLORFIT_VISION_BUZZ.deviceType -> {
//                return SDKWatchType.SDK_NAV_PLUS
//            }
//
//            DeviceType.COLORFIT_VISION_2.deviceType,
//            DeviceType.NOISEFIT_TWIST.deviceType,
//            DeviceType.NOISEFIT_CURVE.deviceType,
//            DeviceType.NOISEFIT_HALO_PLUS.deviceType,
//            DeviceType.VISION_2_BUZZ.deviceType,
//            DeviceType.COLORFIT_LOOP.deviceType,
//            DeviceType.COLORFIT_VICTOR.deviceType,
//            DeviceType.NOISEFIT_EVOLVE_3.deviceType,
//            DeviceType.NOISEFIT_FUSE_PLUS.deviceType,
//            DeviceType.COLORFIT_CALIBER_BUZZ.deviceType,
//            DeviceType.NOISEFIT_METALLIX.deviceType,
//            DeviceType.NOISEFIT_QUAD_CALL_MAX.deviceType,
//            DeviceType.COLORFIT_VISION_3.deviceType,
//            -> {
//                return SDKWatchType.SDK_ZH
//            }
//
//
//            DeviceType.NOISEFIT_NOVA.deviceType -> {
//                return SDKWatchType.SDK_RYEEX
//            }
//
//
//            else -> {
//                throw IllegalArgumentException("Unsupported connected device found ${connectedDevice?.getDeviceLogInfo()}")
//            }
//        }
//    }
//
//}