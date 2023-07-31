package com.noisefit.receiver.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.noisefit.NoiseFitApplicationMain

import com.noisefit.util.notif.NotificationEventsClass
import com.noisefit.util.notif.NotificationUtil
import com.noisefit_commans.utils.LOGS

class AlarmReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context?, p1: Intent?) {
        p1?.let {
            LOGS.d("SleepNotificationUtils ,onReceive")
            handleAlarmData(context, it)
        }
    }

    private fun handleAlarmData(context: Context?, intent: Intent) {

        context?.let {

            val title = intent.getStringExtra("title")
            val description = intent.getStringExtra("content")
            LOGS.d("SleepNotificationUtils ,onReceive $title")
            if (!title.isNullOrEmpty() && !description.isNullOrEmpty()) {
                showLocalNotification(title, description)
            }

        }

    }

    private fun showLocalNotification(title: String, content: String) {
        LOGS.d("SleepNotificationUtils , broad: $title")
        NotificationUtil.pushNotification(
            NoiseFitApplicationMain.context!!,
            title,
            content,
            NotificationEventsClass.LOCAL_SLEEP_NOTIFICATION_KEY,
            "1"
        )
    }
}

