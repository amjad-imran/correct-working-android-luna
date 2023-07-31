package com.noisefit_commans.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import com.google.android.gms.location.LocationResult
import com.google.gson.Gson
import com.noisefit_commans.models.LocationDataModel
import com.noisefit_commans.utils.DistanceUtil
import com.noisefit_commans.utils.LOGS

/**
 *
 * Receiver for handling location updates.
 *
 * For apps targeting API level O
 * {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)} should be used when
 * requesting location updates. Due to limits on background services,
 * {@link android.app.PendingIntent#getService(Context, int, Intent, int)} should not be used.
 *
 *  Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 *  less frequently than the interval specified in the
 *  {@link com.google.android.gms.location.LocationRequest} when the app is no longer in the
 *  foreground.
 */

class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {


    companion object {
        const val ACTION_PROCESS_UPDATES =
            "com.noisefit_commans.services.ACTION_PROCESS_UPDATES"
        const val LOCATION_BROADCAST_RECEIVER = "LOCATION_BROADCAST_RECEIVER"
        const val LAT_LONG = "LAT_LONG"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (ACTION_PROCESS_UPDATES == action) {
            val result = LocationResult.extractResult(intent) ?: return
            val locations = result.locations
            val mLocationDataModels = java.util.ArrayList<LocationDataModel>()
            for (mLocation in locations) {

                if (mLocation.accuracy <= DistanceUtil.kDefaultMinimumAcceptableAccuracy) {
                    val mLocationDataModel = LocationDataModel(
                        mLocation.latitude,
                        mLocation.longitude,
                        mLocation.speed,
                        mLocation.accuracy,
                        mLocation.hasSpeed(),
                        true,
                        mLocation.altitude,
                        mLocation.bearing,
                        mLocation.time
                    )

                    mLocationDataModels.add(mLocationDataModel)
                }
            }

            setConnectBroadcast(context, mLocationDataModels)
        }
    }

    private fun setConnectBroadcast(
        context: Context,
        mLocationDataModels: ArrayList<LocationDataModel>
    ) {
        val intent = Intent(LOCATION_BROADCAST_RECEIVER)
        intent.putParcelableArrayListExtra(LAT_LONG, mLocationDataModels)
        intent.component = null
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

}