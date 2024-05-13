package com.oreo.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.Nullable
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.R
import com.noisefit.oreo.OreoMainActivity
import com.noisefit.session.SessionManager
import com.noisefit.util.notif.NotificationUtil
import com.noisefit_commans.utils.LOGS


class MyActivityLifecycleCallbacks(val sessionManager: SessionManager) :
    Application.ActivityLifecycleCallbacks {
    private var activityCount = 0
    private var activityCount2 = 0
    val TAG = "MyActivityLifecycleCallbacks"
    override fun onActivityCreated(activity: Activity, @Nullable savedInstanceState: Bundle?) {
        // Activity created
        LOGS.d(TAG, "onActivityCreated $activity")
        activityCount2++
    }

    override fun onActivityStarted(activity: Activity) {
        if (activityCount == 0) {
            // App went to foreground
            // Handle foreground state
            LOGS.d(TAG, "onActivityStarted activityCount 0 state")
        }
        activityCount++
    }

    override fun onActivityResumed(activity: Activity) {
        // Activity resumed
        LOGS.d(TAG, "onActivityResumed $activity")
        if (activity is OreoMainActivity) {
            sessionManager.onAppInForeground()
        }

    }

    override fun onActivityPaused(activity: Activity) {
        // Activity paused
        LOGS.d(TAG, "onActivityPaused $activity")

    }

    override fun onActivityStopped(activity: Activity) {
        activityCount--
        if (activityCount == 0) {
            // App went to background
            // Handle background state
            LOGS.d(TAG, "onActivityStopped $activity")
        }

        if (activity is OreoMainActivity) {
            sessionManager.onAppInBackground()

        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        // Activity destroyed
        LOGS.d(TAG, "onActivityDestroyed $activity")
        activityCount2--
        if (activityCount2 == 0) {
            if (sessionManager.connectStateRing.value != null) {
                NotificationUtil.sendForcePushNotification(
                    NoiseFitApplicationMain.context!!,
                    NoiseFitApplicationMain.context!!.getString(R.string.text_open_luna_ring_app),
                    NoiseFitApplicationMain.context!!.getString(R.string.text_keep_the_luna_ring_app_running_so_your_data_can_stay_upto_date)
                )
            }
        }

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // Activity state saved
        LOGS.d(TAG, "onActivitySaveInstanceState $activity")
    }
}

