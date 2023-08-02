package com.noisefit.receiver.workManager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.NonNull
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import com.google.common.util.concurrent.ListenableFuture
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit.data.local.AppStaticData
import com.noisefit.session.SessionManager
import com.noisefit.ui.SplashActivity
import com.noisefit_commans.data.enums.Actions
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.LOGS
import com.oreo.receiver.service.RingConnectionService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val TAG = "RescueServiceInBgWorker"

@HiltWorker
class RescueServiceInBgWorker
@AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    var sessionManager: SessionManager,
    var localDataStore: DataStoredInterface,
) : Worker(context, workerParams) {


    @NonNull
    override fun doWork(): Result {
        //call methods to perform background task
        LOGS.d(TAG, "RescueServiceInBgWorker inside rescue bg worker")
        try {

            connectionServiceOreo(Actions.START)

        } catch (e: Exception) {
            //  sessionManager.logCustomCrashlyticsEvents(TAG, "inside rescue worker failed", e)
            e.printStackTrace()

        }

        return Result.success()
    }


    override fun getForegroundInfoAsync(): ListenableFuture<ForegroundInfo> {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, SplashActivity::class.java)
        val contentIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        var title = AppStaticData.NOTIFICATION_TITLE
        if (BuildConfig.DEBUG) {
            title += " - Dev"

        }

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_noisefit_logo_short)
            .setContentTitle(title)
            .setContentIntent(contentIntent)
            .setLocalOnly(true)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        val future: SettableFuture<ForegroundInfo> = SettableFuture.create()
        future.set(ForegroundInfo(420, notification))
        return future
    }

    private fun connectionServiceOreo(action: Actions) {
        Intent(context, RingConnectionService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LOGS.i(TAG, "Starting the Ring service in >=26 Mode")
                ContextCompat.startForegroundService(context, it)
                return
            }
            LOGS.i(TAG, "Starting the Ringservice in < 26 Mode")
            context.startService(it)
        }
    }

    companion object {
        private const val TAG = "BackupWorker"
        private const val NOTIFICATION_CHANNEL_ID = "11"
        private const val NOTIFICATION_CHANNEL_NAME = "Work Service"
    }
}