package com.oreo.util.moengage.push

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.moengage.pushbase.push.PushMessageListener
import com.noisefit_commans.utils.LOGS

class CustomPushMessageListener : PushMessageListener() {

    override fun onNotificationReceived(context: Context, payload: Bundle) {
        super.onNotificationReceived(context, payload)
        Log.d("moengage_test onNotificationReceived() Notification received", "$payload")
    }

    override fun onNotificationCleared(context: Context, payload: Bundle) {
        super.onNotificationCleared(context, payload)
        Log.d("moengage_test onNotificationCleared() Notification Cleared", "$payload")
    }


//    override fun onNotificationClick(activity: Activity, payload: Bundle) {
//        super.onNotificationClick(activity, payload)
//        Log.d(" onHandleRedirection() Notification clicked", "$payload")
//    }

    override fun handleCustomAction(context: Context, payload: String) {
        super.handleCustomAction(context, payload)
        LOGS.d("moengage_test handleCustomAction()", "Callback for custom action.")
    }
}