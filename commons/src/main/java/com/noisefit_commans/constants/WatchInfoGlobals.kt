package com.noisefit_commans.constants

object WatchInfoGlobals {

    /**
     * Ring info
     */
    var firmwareVersionRing: String? = null
    var serialNumberRing: String? = null
    var firmwareVersionNumberRing: Int = 0
    var firmwareDeviceIdRing: Int = 0

    val GEN_2_DEVICE_ID = 34001

    var firmwareVersion: String? = null
    var serialNumber: String? = null
    var firmwareVersionNumber: Int = 0
    var firmwareDeviceId: Int = 0
    var firmwareFullRequired: Boolean = false
    var firmwareDeviceAddress: String = ""
    var firmwareBuildNumber: Int = 0

    var hideBleCallingDialogForThisSession = false
    var isWatchDataUpdating = false


    var EVOLVE_2_FIRMWARE_CONST_VERSION = 2108
    var PULSE_2_FIRMWARE_CONST_VERSION = 2109
    var EVOLVE_PLAY_FIRMWARE_CONST_VERSION = 2110


    fun resetData() {
        firmwareVersion = null
        firmwareVersionNumber = 0
        firmwareDeviceId = 0
        serialNumber = null
        firmwareDeviceAddress = ""
        firmwareBuildNumber = 0
        isWatchDataUpdating = false
        firmwareFullRequired = false

        firmwareVersionRing = null
        serialNumberRing = null
        firmwareVersionNumberRing = 0
        firmwareDeviceIdRing = 0


    }
}