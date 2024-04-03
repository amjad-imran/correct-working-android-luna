package com.noisefit.util.notif

import android.app.Notification
import android.app.Notification.DEFAULT_SOUND
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit.data.local.AppStaticData
import com.noisefit.ui.SplashActivity
import com.noisefit.util.notif.NotificationEventsClass.APP_UPDATE_NOTIFICATION_KEY
import com.noisefit.util.notif.NotificationEventsClass.FIND_PHONE_NOTIFICATION_KEY
import com.noisefit.util.notif.NotificationEventsClass.LOCAL_NOTIFICATION_KEY
import com.noisefit.util.notif.NotificationEventsClass.LOCAL_NOTIFICATION_WORKOUT_KEY
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.StringUtils.capitalizeWords


object NotificationUtil {
    const val NOTIFICATION_ID_MAIN = 1337
    const val NOTIFICATION_ID_MAIN_OREO = 1338

    fun pushNotification(
        context: Context,
        title: String,
        content: String,
        notificationType: String,
        notificationIndex: String,
        deepLink: String? = null
    ) {
        NotificationHelper.createMediumNotificationChannel(context)
        NotificationHelper.createHighNotificationChannel(context)

        val channelInfo = NotificationHelper.getChannelInfo(notificationType)
        /*val contentIntent = NotificationHelper.handleNotificationType(
            context,
            notificationType,
            notificationIndex,
            deepLink
        )*/
        val intent = Intent(context, SplashActivity::class.java)
        val contentIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)

        val priority = NotificationHelper.getPriority(channelInfo.first)

        val builder = NotificationCompat.Builder(context, channelInfo.first)
            .setSmallIcon(R.drawable.icon_transparent)
            .setContentTitle(title)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.icon_transparent
                )
            )
            .setDefaults(DEFAULT_SOUND)
            .setContentText(content)
            .setContentIntent(contentIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(content)
            )
            .setAutoCancel(true)
            .setPriority(priority)
        with(NotificationManagerCompat.from(context)) {
            notify(channelInfo.second, builder.build())
        }

    }


    fun showLocalNotification(context: Context, title: String, content: String) {
        pushNotification(context, title, content, LOCAL_NOTIFICATION_KEY, "1")
    }

    fun showWorkoutLocalNotification(context: Context, data: OreoAutoSportData) {
        val workoutName = data.type?.lowercase()?.capitalizeWords() ?: "Workout"
        val startTime = DateFormats.convertTimestampToDate(data.startTime, DateFormats.timeFormat12)
        val endTime = DateFormats.convertTimestampToDate(
            data.startTime + data.duration * 1000,
            DateFormats.timeFormat12
        )

        pushNotification(
            context,
            "$workoutName detected",
            "$workoutName was detected from $startTime to $endTime and is ready to review",
            LOCAL_NOTIFICATION_WORKOUT_KEY,
            "1"
        )
    }


    fun showAppUpdateNotification(
        context: Context,
        title: String,
        content: String
    ) {
        pushNotification(context, title, content, APP_UPDATE_NOTIFICATION_KEY, "1")
    }


    fun getNotification(
        context: Context?,
        title: String? = null,
        content: String? = null
    ): Notification? {


        var notification: Notification? = null
        if (context != null) {
            var nTitle = context.getString(R.string.app_name)
            title?.let {
                nTitle = title
            }

            var nDesc = context.getString(R.string.text_running_in_bg)
            content?.let {
                nDesc = content
            }
            NotificationHelper.createConnectionNotificationChannel(context)
            notification = NotificationCompat.Builder(
                context,
                NotificationEventsClass.NOISE_CONNECTION_CHANNEL_ID
            )
                .setSmallIcon(R.drawable.icon_transparent)
                .setContentTitle(nTitle)
                .setContentIntent(NotificationHelper.getSplashIntent(context))
                .setContentText(nDesc)
                .setPriority(NotificationHelper.getPriority(NotificationEventsClass.NOISE_CONNECTION_CHANNEL_ID))
                .build()

        }
        return notification
    }


    fun changeNotificationContent(
        context: Context,
        time: String? = null
    ): Notification {


        val lastSync = if (time.isNullOrEmpty()) {
            context.getString(R.string.text_not_yet_syncyed)
        } else {
            "last sync at $time"
        }

        //Please make sure update this title bar in notification block list
        var title = AppStaticData.NOTIFICATION_TITLE
        if (BuildConfig.DEBUG) {
            title += " - Dev"
        }

        return getNotification(context, title, lastSync)!!
    }


    fun showFindPhoneNotification(
        context: Context,
    ) {
        val title = AppStaticData.FINDING_YOUR_PHONE
        pushNotification(context, title, "", FIND_PHONE_NOTIFICATION_KEY, "1")
    }

    fun removeNotification(
        context: Context,
        id: Int
    ) {

        val notificationManager = context.getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.cancel(id)
    }


    fun notificationActionGranted(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        val packageName = context.packageName
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
        return enabledPackages.contains(packageName)
    }

    fun createDataSyncNotification(context: Context, title: String): Notification {
        NotificationHelper.createDataSyncNotificationChannel(context)

        return NotificationCompat.Builder(
            context,
            NotificationEventsClass.NOISE_DATA_SYNC_CHANNEL_ID
        )
            .setContentTitle(title)
            .setSmallIcon(R.drawable.icon_transparent)
            .build()
    }


    fun postAGPSNotification(context: Context) {
        pushNotification(
            context,
            "AGPS Update",
            "Your watch AGPS has expired, please click here to update your AGPS.",
            NotificationEventsClass.NOTIFICATION_TYPE_AGPS_FORCE_UPDATE,
            "1"
        )
    }

}