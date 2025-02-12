package com.oreo.util.alarm


import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.noisefit.luna.databinding.ActivityAlarmBinding
import com.noisefit_commans.common.setTextGradient
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
        val alarmTime = intent?.getStringExtra("time")

        binding.tvAlarmTime.text = alarmTime
        setTextColor()
        // Set up the dismiss button
        binding.tvDismiss.setOnClickListener {
            stopService()
            finish()
        }
    }

    private fun setTextColor() {
        binding.tvAlarmTime.apply {
            setTextColor(Color.parseColor("#DC7D38"))
            val textShader: Shader = LinearGradient(
                0f,
                this.paint.measureText(this.text.toString()),
                0f,
                0f,
                intArrayOf(
                    Color.parseColor("#DC7D38"),
                    Color.parseColor("#DC7D38"),
                    Color.parseColor("#EDCDA8"),
                ),
                floatArrayOf(0f,0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            this.paint.shader = textShader
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
        stopService()

    }


}