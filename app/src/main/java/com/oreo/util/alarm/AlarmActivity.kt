package com.oreo.util.alarm


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.noisefit.luna.databinding.ActivityAlarmBinding
import com.noisefit_commans.utils.LOGS


class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding
//    private var mediaPlayer: MediaPlayer? = null

    //    private var ringtone: Ringtone? = null
    private fun turnScreenOn() {
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        // Deprecated flags are required on some devices, even with API>=27
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnScreenOn()
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        mediaPlayer = MediaPlayer()
//        try {
//            mediaPlayer!!.setDataSource(
//                this,
//                Uri.parse("android.resource://"+ packageName +"/"+R.raw.track_1_lofi)
//            )
//            mediaPlayer!!.setAudioAttributes(
//                AudioAttributes.Builder()
//                    .setUsage(AudioAttributes.USAGE_ALARM)
//                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                    .build()
//            )
//            mediaPlayer!!.prepare()
//            mediaPlayer?.isLooping = true // Repeat audio until dismissed
//            mediaPlayer?.start()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }


        // Initialize MediaPlayer
//        mediaPlayer = MediaPlayer.create(this, R.raw.track_1_lofi)
//        mediaPlayer?.setAudioStreamType(AudioManager.STREAM_ALARM);
//        mediaPlayer?.isLooping = true // Repeat audio until dismissed
//        mediaPlayer?.start()

        // Set up the dismiss button
        binding.tvDismiss.setOnClickListener {
            LOGS.d("sadhjdsadjaskdsa dismiss")
            stopService()
//            mediaPlayer?.stop()
//            mediaPlayer?.release()
//            mediaPlayer = null
//            ringtone?.stop()
            //wakeLock.release()
            finish()
        }
    }

    private fun stopService() {
        val myService = Intent(
            this,
            AlarmService::class.java
        )
        stopService(myService)
    }

    override fun onDestroy() {
        super.onDestroy()
        LOGS.d("sadhjdsadjaskdsa onDestroy")
        stopService()

//        try {
//            mediaPlayer?.stop()
//        } catch (e: Exception) {
//
//        }

//        ringtone?.stop()
//        ringtone = null
    }


}