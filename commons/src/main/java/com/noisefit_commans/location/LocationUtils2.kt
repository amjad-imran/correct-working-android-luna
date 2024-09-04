package com.noisefit_commans.location

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.LOGS

object LocationUtils2 {

    fun startLocationService(postOnMain: Boolean = true) {
        val context = NoisefitApplication.context ?: return
        LOGS.d("LOCATION_lOG starting service")

        //todo check permissions
        if (isMyServiceRunning(LocationService2::class.java, context).not()) {
            Intent(context, LocationService2::class.java).apply {
                action =
                    if (postOnMain) LocationService2.ACTION_START else LocationService2.ACTION_START_2
                context.startService(this)
            }
            AppLogs.sendAppLogs("Start Location tracking")
        } else {
            AppLogs.sendAppLogs("Start Location tracking - already running")
        }
    }

    fun stopLocationService() {
        val context = NoisefitApplication.context ?: return
        LOGS.d("LOCATION_lOG stopping service")

        if (isMyServiceRunning(LocationService2::class.java, context)) {
            AppLogs.sendAppLogs("Stop Location tracking")
            Intent(context, LocationService2::class.java).apply {
                action = LocationService2.ACTION_STOP
                context.startService(this)
            }
        } else {
            AppLogs.sendAppLogs("Stop Location tracking - not running")
        }

    }

    fun isMyServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}