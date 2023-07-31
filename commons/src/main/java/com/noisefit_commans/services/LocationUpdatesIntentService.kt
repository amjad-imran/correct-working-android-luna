package com.noisefit_commans.services

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationResult.extractResult
import com.noisefit_commans.models.LocationDataModel
import com.noisefit_commans.services.LocationUpdatesBroadcastReceiver.Companion.LAT_LONG
import com.noisefit_commans.services.LocationUpdatesBroadcastReceiver.Companion.LOCATION_BROADCAST_RECEIVER
import com.noisefit_commans.utils.DistanceUtil

/**
 * Handles incoming location updates and displays a notification with the location data.
 *
 * For apps targeting API level 25 ("Nougat") or lower, location updates may be requested
 * using {@link android.app.PendingIntent#getService(Context, int, Intent, int)} or
 * {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)}. For apps targeting
 * API level O, only {@code getBroadcast} should be used.
 *
 *  Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 *  less frequently than the interval specified in the
 *  {@link com.google.android.gms.location.LocationRequest} when the app is no longer in the
 *  foreground.
 */

class LocationUpdatesIntentService : IntentService(TAG) {

    companion object {
        const val ACTION_PROCESS_UPDATES =
            "com.noisefit_commans.services.action.PROCESS_UPDATES"
        private val TAG = LocationUpdatesIntentService::class.java.simpleName
    }

    override fun onHandleIntent(intent: Intent?) {
        intent?.let {
            try {
                val action = intent.action
                if (ACTION_PROCESS_UPDATES == action) {
                    val result = extractResult(intent)
                    val locations = result.locations ?: return
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
                    setConnectBroadcast(applicationContext, mLocationDataModels)
                }
            } catch (exp: NullPointerException) {
                exp.printStackTrace()
            }
        }

    }

    private fun setConnectBroadcast(
        context: Context,
        mLocationDataModels: ArrayList<LocationDataModel>
    ) {
        val intent = Intent(LOCATION_BROADCAST_RECEIVER)
        intent.putParcelableArrayListExtra(
            LAT_LONG,
            mLocationDataModels
        )
        intent.component = null
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}
