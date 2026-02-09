package com.noisefit_commans.utils

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler

class VolumeObserver(
    private val context: Context,
    handler: Handler,
    private val thresholdPercent: Int = 30,
    private val onVolumeStateChanged: (Boolean) -> Unit
) : ContentObserver(handler) {

    private var lastState: Boolean? = null

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        notifyIfChanged()
    }

    fun notifyIfChanged() {
        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        val percent = (current * 100) / max
        val isLow = percent <= thresholdPercent

        if (lastState != isLow) {
            lastState = isLow
            onVolumeStateChanged(isLow)
        }
    }
}
