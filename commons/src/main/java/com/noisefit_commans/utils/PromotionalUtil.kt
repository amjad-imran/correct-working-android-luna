package com.noisefit_commans.utils

import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType

object PromotionalUtil {


    fun showPromotionalBanner(colorFitDevice: ColorFitDevice?): Boolean {
        return false
//        return when (colorFitDevice?.deviceType) {
//            DeviceType.COLORFIT_PULSE.deviceType -> {
//                true
//            }
//            else -> {
//                false
//            }
//        }
    }
}