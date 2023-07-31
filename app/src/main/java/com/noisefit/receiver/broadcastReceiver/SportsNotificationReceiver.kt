package com.noisefit.receiver.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.noisefit.receiver.workManager.MatchReminderWork
import com.noisefit.util.UniqueMatchReminderWorkName
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.LOGS

class SportsNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION = "SPORTS_NOTIFICATION_ACTION"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ACTION) {
            LOGS.d("SportsNotificationReceiver", "Received Alarm")
            AppLogs.sendAppLogs("Sports: SportsNotificationReceiver onReceive ")

            context?.let { ctx ->
                val uniqueId = UniqueMatchReminderWorkName
                val mWorkManager = WorkManager.getInstance(ctx)
                mWorkManager.cancelAllWorkByTag(uniqueId)

                WorkManager.getInstance(ctx).cancelUniqueWork(uniqueId)

                val work =
                    OneTimeWorkRequest.Builder(MatchReminderWork::class.java)
                        .addTag(uniqueId)
                        .build()

                WorkManager.getInstance(ctx).enqueueUniqueWork(
                    uniqueId,
                    ExistingWorkPolicy.REPLACE,
                    work
                )
            }

        }

    }

}