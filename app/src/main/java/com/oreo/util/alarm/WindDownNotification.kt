package com.oreo.util.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit.data.local.db.fromJson
import com.noisefit.luna.R
import com.noisefit.oreo.OreoMainActivity
import com.noisefit.util.notif.NotificationEventsClass
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.NotificationToggleModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class WindDownNotification : BroadcastReceiver() {


    @Inject
    lateinit var localDataSource: DataStoredInterface

    override fun onReceive(context: Context?, intent: Intent?) {

        LOGS.d("dsfjhskdjfhkWInd down notification recieved")

        if (context == null) {
            return
        }

        val showSleepNotification = localDataSource.getShouldShowSleepNotification()

        if(showSleepNotification.not()){
            return
        }

        val intentMain = OreoMainActivity.getStartIntent(
            context,
            NotificationEventsClass.LOCAL_NOTIFICATION_BREATHING
        )
        intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intentMain,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        createNotificationChannel(context)
        val notification: Notification = NotificationCompat.Builder(
            context, SLEEP_WIND_DOWN_CHANNEL
        ).setContentTitle(intent?.getStringExtra("titleExtra") ?: "")
            .setContentText(intent?.getStringExtra("messageExtra") ?: "")
            .setSmallIcon(R.drawable.ic_luna_small)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1456, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SLEEP_WIND_DOWN_CHANNEL,
                "Sleep Wind down",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notification channel for Sleep wind down"
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}