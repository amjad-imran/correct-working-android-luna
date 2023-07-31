package com.noisefit_commans.utils

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Vibrator
import com.noisefit_commans.NoisefitApplication


object PhoneRinger {

    private var currentVolume = 0
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null


    fun isRinging(): Boolean {
        try {
            if(mediaPlayer==null) return false
            return mediaPlayer?.isPlaying == true
        }catch (exp:Exception){
            return false
        }
    }

    fun enableRing(status: Boolean, isEndAvailable: Boolean) {
        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(NoisefitApplication.context, ringtoneUri)
            }

            if (vibrator == null) {
                vibrator =
                    NoisefitApplication.context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            val audioManager: AudioManager? =
                NoisefitApplication.context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            if (status) {


                currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
                val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

                audioManager?.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    maxVolume ?: 0,
                    0
                )


                mediaPlayer?.start()
                when (isEndAvailable) {
                    true -> {
                        mediaPlayer?.isLooping = true
                        vibrator?.vibrate(longArrayOf(0, 300, 700), 0)
                    }
                    else -> {
                        mediaPlayer?.isLooping = false
                        vibrator?.vibrate(longArrayOf(0, 300, 700), -1)
                    }
                }
            } else {
                audioManager?.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    currentVolume,
                    0
                )
                mediaPlayer?.stop()
                mediaPlayer = null
                vibrator?.cancel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun enableRing(status: Boolean) {
        enableRing(status, true)
    }
}