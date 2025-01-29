package com.oreo.util.alarm;


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.noisefit.luna.R
import com.oreo.util.alarm.AlarmUtil.Companion.getAlarmToneByKey
import java.io.IOException


const val SLEEP_ALARM_CHANNEL = "SLEEP_ALARM_CHANNEL"
const val SLEEP_WIND_DOWN_CHANNEL = "SLEEP_WIND_DOWN_CHANNEL"

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var ringtone: Uri? = null


    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        ringtone = RingtoneManager.getActualDefaultRingtoneUri(
            this.baseContext, RingtoneManager.TYPE_ALARM
        )
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == "STOP_SERVICE") {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        WakeLockManager.acquireServiceLock()
//        try {
//            val track =
//                intent?.getIntExtra("alarmTone", getAlarmToneByKey(1)) ?: getAlarmToneByKey(1)
//            mediaPlayer = MediaPlayer.create(this, track)
//            mediaPlayer?.isLooping = true
//            mediaPlayer?.start()
//        } catch (ex: IOException) {
//            ex.printStackTrace()
//        }
//
//        val stopIntent = Intent(this, AlarmService::class.java).apply {
//            action = "STOP_SERVICE"
//        }
//        val stopPendingIntent = PendingIntent.getService(
//            this,
//            0,
//            stopIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
//        )


        showForegroundNotification()


//        val notification: Notification = NotificationCompat.Builder(
//            this, SLEEP_ALARM_CHANNEL
//        ).setContentTitle("Luna Ring").setContentText(alarmTitle)
//            .setSmallIcon(R.drawable.ic_luna_small).setSound(null)
//            .setCategory(NotificationCompat.CATEGORY_ALARM)
//            .setOngoing(true)
//            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            .addAction(R.drawable.ic_stop, "Dismiss", stopPendingIntent)
//            .build()

        val pattern = longArrayOf(0, 100, 1000)
        vibrator!!.vibrate(pattern, 0)

        return START_STICKY
    }


    private fun showForegroundNotification() {
        createNotificationChannel()
        val notificationIntent = Intent(this, AlarmActivity::class.java)
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
        startForeground(1, notification)
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

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        vibrator!!.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
