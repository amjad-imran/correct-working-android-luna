package com.noisefit.util.notif

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.noisefit.luna.R
import com.noisefit.receiver.broadcastReceiver.StopFindMyPhoneBroadcast
import com.noisefit.ui.SplashActivity

import com.noisefit_commans.utils.LOGS
import java.util.*

object NotificationHelper {
    fun createHighNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_noise_push)
            val descriptionText = context.getString(R.string.channel_noise_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                NotificationEventsClass.NOISE_PUSH_CHANNEL_ID,
                name,
                importance
            ).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createMediumNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_noise_misc)
            val descriptionText = context.getString(R.string.channel_misc_noise_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                NotificationEventsClass.NOISE_MISC_CHANNEL_ID,
                name,
                importance
            ).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createDataSyncNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_noise_connection)
            val descriptionText = context.getString(R.string.channel_connection_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(
                NotificationEventsClass.NOISE_DATA_SYNC_CHANNEL_ID,
                name,
                importance
            ).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createConnectionNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_noise_data_sync)
            val descriptionText = context.getString(R.string.channel_data_sync_noise_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(
                NotificationEventsClass.NOISE_CONNECTION_CHANNEL_ID,
                name,
                importance
            ).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getSplashIntent(context: Context): PendingIntent {
        val intent = Intent(context, SplashActivity::class.java)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)

    }

    fun getAction(context: Context, notificationType: String): NotificationCompat.Action? {
        when (notificationType) {
            NotificationEventsClass.FIND_PHONE_NOTIFICATION_KEY -> {
                val snoozeIntent = Intent(context, StopFindMyPhoneBroadcast::class.java).apply {
                    action = StopFindMyPhoneBroadcast.ACTION_STOP
                }
                val stopPendingIntent: PendingIntent =
                    PendingIntent.getBroadcast(context, 0, snoozeIntent, PendingIntent.FLAG_MUTABLE)

                return NotificationCompat.Action(
                    0,
                    context.getString(R.string.stop),
                    stopPendingIntent
                )
            }
            else -> {
                return null
            }
        }
    }

    fun getPriority(channelID: String): Int {
        when (channelID) {
            NotificationEventsClass.NOISE_PUSH_CHANNEL_ID -> {
                return NotificationCompat.PRIORITY_MAX
            }
            NotificationEventsClass.NOISE_MISC_CHANNEL_ID,
            NotificationEventsClass.NOISE_DATA_SYNC_CHANNEL_ID,
            NotificationEventsClass.NOISE_CONNECTION_CHANNEL_ID -> {
                return NotificationCompat.PRIORITY_DEFAULT
            }

        }
        return NotificationCompat.PRIORITY_MAX
    }

    fun handleNotificationType(
        context: Context,
        notificationType: String,
        notificationIndex: String,
        deepLink: String? = null
    ): PendingIntent {

        LOGS.d("NOTIFICATION_TYPE $notificationType")

        val intent = when (notificationType) {
            NotificationEventsClass.FIND_PHONE_NOTIFICATION_KEY -> {
                Intent(context, StopFindMyPhoneBroadcast::class.java).apply {
                    action = StopFindMyPhoneBroadcast.ACTION_STOP
                }
            }
            else -> {
                Intent(context, SplashActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra(NotificationEventsClass.NOTIFICATION_BUNDLE_TYPE, notificationType)
                    putExtra(NotificationEventsClass.NOTIFICATION_BUNDLE_INDEX, notificationIndex)
                    putExtra(NotificationEventsClass.NOTIFICATION_BUNDLE_LINK, deepLink)
                }
            }
        }

        return PendingIntent.getActivity(
            context,
            Random().nextInt(),
            intent,
            PendingIntent.FLAG_MUTABLE
        )
    }


    fun getChannelInfo(notificationType: String): Pair<String, Int> {
        when (notificationType) {
            NotificationEventsClass.NOTIFICATION_TYPE_SLEEP_SCREEN,
            NotificationEventsClass.NOTIFICATION_TYPE_DISTANCE,
            NotificationEventsClass.NOTIFICATION_TYPE_STEPS,
            NotificationEventsClass.NOTIFICATION_TYPE_ACTIVITY_CHALLENGE,
            NotificationEventsClass.NOTIFICATION_TYPE_MANAGE,
            NotificationEventsClass.NOTIFICATION_TYPE_BUDDY,
            NotificationEventsClass.NOTIFICATION_TYPE_TROPHIES,
            NotificationEventsClass.NOTIFICATION_TYPE_HEART_RATE,
            NotificationEventsClass.NOTIFICATION_TYPE_STRESS,
            NotificationEventsClass.NOTIFICATION_TYPE_FEEDBACK_SCREEN -> {
                return Pair(NotificationEventsClass.NOISE_PUSH_CHANNEL_ID, (1..10000).random())
            }
            NotificationEventsClass.NOTIFICATION_TYPE_WATCH_FACES,
            NotificationEventsClass.NOTIFICATION_TYPE_SHOP,
            NotificationEventsClass.NOTIFICATION_TYPE_DEVICE_INFO,
            NotificationEventsClass.NOTIFICATION_TYPE_SETTINGS,
            NotificationEventsClass.NOTIFICATION_TYPE_ACTIVITY_HISTORY -> {
                return Pair(NotificationEventsClass.NOISE_MISC_CHANNEL_ID, (1..10000).random())
            }
            NotificationEventsClass.LOCAL_NOTIFICATION_KEY -> {
                return Pair(NotificationEventsClass.NOISE_PUSH_CHANNEL_ID, 23122)
            }
            NotificationEventsClass.APP_UPDATE_NOTIFICATION_KEY -> {
                return Pair(NotificationEventsClass.NOISE_MISC_CHANNEL_ID, 23478)
            }
            NotificationEventsClass.APP_RESCUE_NOTIFICATION_KEY -> {
                return Pair(NotificationEventsClass.NOISE_MISC_CHANNEL_ID, 23103)
            }
            NotificationEventsClass.NOTIFICATION_TYPE_WATCH_LOW_BATTERY -> {
                return Pair(NotificationEventsClass.NOISE_PUSH_CHANNEL_ID, 12342)
            }

            NotificationEventsClass.NOTIFICATION_TYPE_AGPS_FORCE_UPDATE -> {
                return Pair(
                    NotificationEventsClass.NOISE_PUSH_CHANNEL_ID,
                    NotificationEventsClass.AGPS_UPDATE_NOTIFICATION_ID
                )
            }
            NotificationEventsClass.FIND_PHONE_NOTIFICATION_KEY -> {
                return Pair(
                    NotificationEventsClass.NOISE_MISC_CHANNEL_ID,
                    NotificationEventsClass.FIND_PHONE_NOTIFICATION_ID
                )
            }
        }
        return Pair(NotificationEventsClass.NOISE_PUSH_CHANNEL_ID, (1..10000).random())
    }

}