package com.oreo.util.alarm;


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.noisefit.luna.R
import com.noisefit_commans.utils.LOGS
import com.oreo.util.alarm.AlarmUtil.Companion.getAlarmToneByKey

private const val ALARM_FOREGROUND_KEY = 1230

const val SLEEP_ALARM_CHANNEL = "SLEEP_ALARM_CHANNEL"
const val SLEEP_WIND_DOWN_CHANNEL = "SLEEP_WIND_DOWN_CHANNEL"

class AlarmService : Service() {

    private var vibrator: Vibrator? = null
    private var ringtone: Ringtone? = null


    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == "STOP_SERVICE") {
            stopService()
            return START_NOT_STICKY
        }
        val tone = intent?.getIntExtra("alarmTone", AlarmUtil.getAlarmToneByKey(1))

        ringtone = null
        ringtone = RingtoneManager.getRingtone(
            applicationContext,
            Uri.parse("android.resource://" + packageName + "/" + tone!!)
        )
        ringtone!!.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone?.isLooping = true
        }

        ringtone?.play()

        WakeLockManager.acquireServiceLock()
        showForegroundNotification(intent)
        val pattern = longArrayOf(0, 100, 1000)
        vibrator!!.vibrate(pattern, 0)

        return START_STICKY
    }


    private fun showForegroundNotification(intent: Intent?) {
        createNotificationChannel()
        val notificationIntent = Intent(this, AlarmActivity::class.java)
        notificationIntent.putExtra(
            "time",
            intent?.getStringExtra("time")
        )
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, SLEEP_ALARM_CHANNEL)
            .setContentTitle("Luna Ring")
            .setContentText("Alarm title here")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_luna_small)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(pendingIntent, true) // Ensures full-screen intent
            .setContentIntent(pendingIntent)
//            .addAction(R.drawable.ic_stop, "Dismiss", stopPendingIntent)
            .build()
        startForeground(ALARM_FOREGROUND_KEY, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SLEEP_ALARM_CHANNEL, "Alarm Service", NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel)
        }
    }

    private fun stopService(){
        WakeLockManager.releaseServiceLock()
        stopForeground(true)
        stopSelf()
        vibrator?.cancel()
        ringtone?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        LOGS.d("sadhjdsadjaskdsa service destoryed")
        stopService()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
