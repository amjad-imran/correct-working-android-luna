package com.oreo.util.alarm

import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ActivityAlarmBinding

class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding
    private var mediaPlayer: MediaPlayer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }*/

        /*val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "Luna:WakeLock"
        )
        wakeLock.acquire(2 * 60 * 1000L) // Acquire for up to 10 minutes*/

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.track_1_lofi)
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
        mediaPlayer?.release()
    }


}