package com.noisefit.receiver.broadcastReceiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.noisefit.session.SessionManager
import com.noisefit_commans.utils.AppLogs
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SportsReqReceiver : BroadcastReceiver() {

    @Inject
    lateinit var sessionManager: SessionManager
    override fun onReceive(context: Context?, intent: Intent?) {

        AppLogs.sendAppLogs("In SportsReqReceiver")
//        sessionManager.checkSport.value = Event(true)

        context?.let {
            startReceiver(it)
        }
    }

    private fun startReceiver(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SportsReqReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                Calendar.getInstance().timeInMillis + 60 * 1000,
                pendingIntent
            )
        }
    }
}