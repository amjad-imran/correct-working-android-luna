package com.noisefit_commans.location

import android.Manifest
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.LOGS

object LocationUtils2 {

    fun startLocationService(postOnMain: Boolean = true) {
        val context = NoisefitApplication.context ?: return
        LOGS.d("LOCATION_lOG starting service")
        if (hasGpsPermission(context).not()) {
            return
        }
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (isGpsEnabled.not()) {
            LOGS.i("GPS not enabled")
            return
        }

        if (isMyServiceRunning(LocationService2::class.java, context).not()) {
            Intent(context, LocationService2::class.java).apply {
                action = if (postOnMain) LocationService2.ACTION_START else LocationService2.ACTION_START_2
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(this)
                } else {
                    context.startService(this)
                }
            }
            AppLogs.sendAppLogs("Start Location tracking")
        } else {
            AppLogs.sendAppLogs("Start Location tracking - already running")
        }
    }

    private fun hasGpsPermission(context: Application): Boolean {
        val hasFine = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return hasFine || hasCoarse
    }

    fun stopLocationService() {
        val context = NoisefitApplication.context ?: return
        LOGS.d("LOCATION_lOG stopping service")

        if (isMyServiceRunning(LocationService2::class.java, context)) {
            AppLogs.sendAppLogs("Stop Location tracking")
            Intent(context, LocationService2::class.java).apply {
                action = LocationService2.ACTION_STOP
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(this)
                } else {
                    context.startService(this)
                }
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