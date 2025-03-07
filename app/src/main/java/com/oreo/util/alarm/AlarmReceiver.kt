package com.oreo.util.alarm;

import android.app.Service.START_NOT_STICKY
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.LOGS
import com.oreo.data.repository.AlarmRepository
import com.oreo.util.alarm.AlarmUtil.Companion.getAlarmToneByKey
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmRepository: AlarmRepository


    override fun onReceive(context: Context, intent: Intent?) {

        if (Intent.ACTION_BOOT_COMPLETED == intent?.action || Intent.ACTION_REBOOT == intent?.action) {
            alarmRepository.rescheduleAlarms()
        } else {

            if(NotificationManagerCompat.from(context).areNotificationsEnabled().not()){
                return
            }

            val millis = intent?.getLongExtra("millis", 0L) ?: 0L

            if (millis != 0L) {
                val current = System.currentTimeMillis()
                if (current > (millis + 60 * 1000)) {
                    return
                }
            }

            val pendingIntent = Intent(context, AlarmService::class.java)
            pendingIntent.putExtra(
                "alarmTone",
                intent?.getIntExtra("alarmTone", getAlarmToneByKey(1))
            )
            pendingIntent.putExtra(
                "time",
                intent?.getStringExtra("time")
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(pendingIntent)
            } else {
                context.startService(pendingIntent)
            }

            LOGS.i("Alarm ringing")

        }
    }
}
