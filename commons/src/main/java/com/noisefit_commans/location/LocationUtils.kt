package com.noisefit_commans.location

import android.content.Intent
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.utils.LOGS

object LocationUtils {

    fun startLocationService() {
        val context = NoisefitApplication.context ?: return
        LOGS.d("LOCATION_lOG starting service")

        Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            context.startService(this)
        }
    }

    fun stopLocationService() {
        val context = NoisefitApplication.context ?: return
        LOGS.d("LOCATION_lOG stopping service")
        Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            context.startService(this)
        }
    }
}