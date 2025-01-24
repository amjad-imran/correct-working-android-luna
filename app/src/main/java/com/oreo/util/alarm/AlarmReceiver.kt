package com.oreo.util.alarm;

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {

        val intentService = Intent(context, AlarmService::class.java)
        val bundle = Bundle()
        //bundle.putSerializable(context.getString(R.string.arg_alarm_obj), alarm1)
        //intentService.putExtra(context.getString(R.string.bundle_alarm_obj), bundle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentService)
        } else {
            context.startService(intentService)
        }

    }
}
