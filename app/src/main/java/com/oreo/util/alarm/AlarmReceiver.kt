package com.oreo.util.alarm;

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
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
            val pendingIntent = Intent(context, AlarmService::class.java)
            pendingIntent.putExtra(
                "alarmTone",
                intent?.getIntExtra("alarmTone", getAlarmToneByKey(1))
            )
            //val bundle = Bundle()
            //bundle.putSerializable(context.getString(R.string.arg_alarm_obj), alarm1)
            //intentService.putExtra(context.getString(R.string.bundle_alarm_obj), bundle)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(pendingIntent)
            } else {
                context.startService(pendingIntent)
            }

            LOGS.i("Alarm ringing")

            /* val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
                 flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
             }
             context.startActivity(fullScreenIntent)*/
        }
    }
}
