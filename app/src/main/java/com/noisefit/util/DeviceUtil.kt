package com.noisefit.util

import com.noisefit.luna.R
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import javax.inject.Inject

class DeviceUtil
@Inject
constructor() {

    /**
     * Returns if the device is supported
     * if not then the play store package to open
     */
    fun isUnsupportedDevice(
        colorFitDevice: ColorFitDevice
    ): Triple<Boolean, String?, Int> {
        return when (colorFitDevice.deviceType) {
            DeviceType.COLORFIT_PRO.deviceType, DeviceType.COLORFIT_PRO_Y23.deviceType,
            DeviceType.CORE_2_BUZZ.deviceType, DeviceType.AGILE_2_BUZZ.deviceType -> {
                Triple(true, ShareUtil.PACKAGE_TRACK, R.drawable.icon_noisefit_track)
            }
            DeviceType.NOISEFIT_EVOLVE.deviceType, DeviceType.NOISEFIT_EVOLVE_SPORT.deviceType -> {
                Triple(true, ShareUtil.PACKAGE_EVOLVE, R.drawable.ic_noisefit_peak)
            }
            DeviceType.NOISEFIT_BUZZ.deviceType -> {
                Triple(true, ShareUtil.PACKAGE_ACE, R.drawable.ic_noisefit_ace)
            }
            DeviceType.COLORFIT_PRO_3_ALPHA.deviceType, DeviceType.COLORFIT_PRO_4_MAX.deviceType -> {
                Triple(true, ShareUtil.PACKAGE_ASSIST, R.drawable.ic_noisefit_assist)
            }
            DeviceType.PULSE_BUZZ.deviceType -> {
                Triple(true, ShareUtil.PACKAGE_PRIME, R.drawable.ic_noisefit_prime)
            }
            DeviceType.CORE_OXY.deviceType, DeviceType.NOISEFIT_CORE.deviceType -> {
                Triple(true, ShareUtil.PACKAGE_APEX, R.drawable.ic_noisefit_apex)
            }
            DeviceType.NOISE_EXCEL.deviceType, DeviceType.CORE_2.deviceType -> {
                Triple(true, ShareUtil.PACKAGE_SYNC, R.drawable.ic_noisefit_sync)
            }
            else -> {
                return Triple(false, null, 0)
            }
        }
    }
}