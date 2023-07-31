package com.noisefit_commans.handler

import android.content.Context
import android.media.AudioManager
import android.os.SystemClock
import android.view.KeyEvent
import com.noisefit_commans.NoisefitApplication
import android.content.Intent
import com.noisefit_commans.utils.MusicUtil


object MusicPlayerControlsHandler {

    fun onEvent(event: String) {
        when (event) {
            MusicControlActionsEvents.PLAY -> updateMusicState(KeyEvent.KEYCODE_MEDIA_PLAY)
            MusicControlActionsEvents.PAUSE -> updateMusicState(KeyEvent.KEYCODE_MEDIA_PAUSE)
            MusicControlActionsEvents.PREVIOUS -> updateMusicState(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            MusicControlActionsEvents.NEXT -> updateMusicState(KeyEvent.KEYCODE_MEDIA_NEXT)
            MusicControlActionsEvents.STOP -> updateMusicState(KeyEvent.KEYCODE_MEDIA_STOP)
            else -> {
            }
        }
    }

    private fun updateMusicState(keyEvent: Int) {
        val eventTime = SystemClock.uptimeMillis() - 1
        val downEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyEvent, 0)
        getAudioManager().dispatchMediaKeyEvent(downEvent)

        val upTime = eventTime + 1
        val upEvent = KeyEvent(upTime, upTime, KeyEvent.ACTION_UP, keyEvent, 0)
        getAudioManager().dispatchMediaKeyEvent(upEvent)

        if (MusicUtil.isHuaweiDevice()) {
            var command: String? = null
            if (keyEvent == KeyEvent.KEYCODE_MEDIA_NEXT) {
                command = "next"
            } else if (keyEvent == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                command = "previous"
            }

            command?.let {
                try {
                    val intent = Intent("com.android.music.musicservicecommand")
                    intent.putExtra("command", it)
                    NoisefitApplication.context?.sendBroadcast(intent)
                } catch (exp: Exception){
                    //Package not found
                }
            }
        }
    }

    private fun getAudioManager(): AudioManager {
        return NoisefitApplication.context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
}

class MusicControlActionsEvents {
    companion object {
        const val PLAY = "play"
        const val PAUSE = "pause"
        const val PREVIOUS = "previous"
        const val NEXT = "next"
        const val STOP = "stop"
    }
}