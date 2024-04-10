package com.noisefit_commans.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.noisefit_commans.NoisefitApplication
import javax.inject.Inject

const val LOW_VIBRATION = 200L
const val HAPTIC_VIBRATION = 60L

class VibrationUtils
@Inject
constructor() {

    private var vibe: Vibrator? = null

    init {
        vibe =
            NoisefitApplication.context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun vibrate(value: Long) {
        if (vibe == null) {
            return
        }

        if (!vibe!!.hasVibrator()) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibe?.vibrate(VibrationEffect.createOneShot(value, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibe?.vibrate(value)
        }
    }

    fun cancelVibrate() {
        if (vibe == null) {
            return
        }
        vibe?.cancel()
    }
}