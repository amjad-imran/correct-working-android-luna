package com.noisefit_commans.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import com.noisefit_commans.NoisefitApplication
import java.lang.reflect.Method
import javax.inject.Inject


class CallHandler
@Inject
constructor(){

    var isPhoneMuted = false

    private fun endCall() {
        if (NoisefitApplication.context != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (NoisefitApplication.context!!.checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                        return
                    }
                    val telephoneManager =
                        NoisefitApplication.context!!.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                    telephoneManager.endCall()
                } else {
                    val telephoneManager =
                        NoisefitApplication.context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    var c = Class.forName(telephoneManager.javaClass.name)
                    var m: Method = c.getDeclaredMethod("getITelephony")
                    m.isAccessible = true
                    val telephonyService: Any =
                        m.invoke(telephoneManager) // Get the internal ITelephony object
                    c = Class.forName(telephonyService.javaClass.name) // Get its class
                    m = c.getDeclaredMethod("endCall") // Get the "endCall()" method
                    m.isAccessible = true // Make it accessible
                    m.invoke(telephonyService) // invoke endCall()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun acceptCall() {
        if (NoisefitApplication.context == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (NoisefitApplication.context!!
                        .checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) !== PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                val tm = NoisefitApplication.context!!
                    .getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                tm.acceptRingingCall()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkRingerIsOn(): Boolean {
        if (NoisefitApplication.context == null) {
            return false
        }
        val am = NoisefitApplication.context!!
            .getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return am.ringerMode == AudioManager.RINGER_MODE_NORMAL
    }

    fun silenceRinger(isMute: Boolean) {
        if (NoisefitApplication.context == null) {
            return
        }
        val audioManager = NoisefitApplication.context!!
            .getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val adJustMute = if (isMute) {
            AudioManager.ADJUST_MUTE
        } else {
            AudioManager.ADJUST_UNMUTE
        }
        audioManager.adjustStreamVolume(AudioManager.STREAM_RING, adJustMute, 0)
    }

    fun updateCallStatus(status: Boolean) {
        if (status) {
            acceptCall()
        } else {
            endCall()
        }
    }
}