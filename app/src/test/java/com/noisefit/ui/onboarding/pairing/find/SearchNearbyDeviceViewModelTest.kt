package com.noisefit.ui.onboarding.pairing.find

import com.google.common.truth.Truth.assertThat
import com.noisefit_commans.models.ColorFitNetworkDevice
import com.noisefit_commans.models.DeviceType
import org.junit.Test

class SearchNearbyDeviceViewModelTest {

    private val lunaBandDevice = ColorFitNetworkDevice(
        bluetoothName = LUNA_BAND_DISPLAY_NAME,
        deviceType = DeviceType.LUNA_BAND.deviceType,
        namePattern = LUNA_BAND_DISPLAY_NAME,
        matchingType = "exact_pattern"
    )

    @Test
    fun `legacy luna band exact pattern still matches existing scanned names`() {
        assertThat(lunaBandDevice.matchesScannedDeviceName("Luna Band_005E")).isTrue()
    }

    @Test
    fun `noise rep scanned names fall back to luna band device type`() {
        val result = resolveLunaBandNoiseRepAlias(
            devices = listOf(lunaBandDevice),
            scannedName = "Noise_REP_1A2B"
        )

        assertThat(result).isNotNull()
        assertThat(result!!.first).isEqualTo(DeviceType.LUNA_BAND)
        assertThat(result.second).isEqualTo(lunaBandDevice)
    }

    @Test
    fun `noise rep scanned names display as noise band in ui`() {
        assertThat(
            resolveScannedDeviceDisplayName(
                scannedName = "Noise_REP_1A2B",
                matchedDevice = lunaBandDevice
            )
        ).isEqualTo(NOISE_BAND_DISPLAY_NAME)
    }

    @Test
    fun `legacy luna band scanned names keep luna band ui name`() {
        assertThat(
            resolveScannedDeviceDisplayName(
                scannedName = "Luna Band_005E",
                matchedDevice = lunaBandDevice
            )
        ).isEqualTo(LUNA_BAND_DISPLAY_NAME)
    }
}
