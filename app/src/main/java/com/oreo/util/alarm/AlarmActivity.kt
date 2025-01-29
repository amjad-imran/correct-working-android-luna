package com.oreo.util.alarm

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ActivityAlarmBinding

class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding
    private var mediaPlayer: MediaPlayer? = null

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

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.track_1_lofi)
        mediaPlayer?.setAudioStreamType(AudioManager.STREAM_ALARM);
        mediaPlayer?.isLooping = true // Repeat audio until dismissed
        mediaPlayer?.start()

        // Set up the dismiss button
        binding.tvDismiss.setOnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            //wakeLock.release()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        WakeLockManager.releaseServiceLock()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }


}