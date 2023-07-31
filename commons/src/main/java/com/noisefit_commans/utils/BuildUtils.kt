package com.noisefit_commans.utils

import android.os.Build

object BuildUtils {

    fun getDeviceManufacturer(): String {
        return Build.MANUFACTURER
    }

    fun getDeviceModel(): String {
        return Build.MODEL
    }

    fun getDeviceOSVersion(): String {
        return Build.VERSION.RELEASE
    }
}