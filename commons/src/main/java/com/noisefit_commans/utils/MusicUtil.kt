package com.noisefit_commans.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.constants.CommonGlobals
import java.lang.reflect.Method

private const val SONG_NOT_PLAYING = ""
private const val SONG_PLAYING = "Connected"

object MusicUtil {

    fun isMusicActive(): Boolean {
        val audioManager =
            NoisefitApplication.context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioManager.isMusicActive) {
            return true
        }
        return false

    }

    fun getSongName(title: String?): String {
        if (title != null) {
            return title
        }

        if (!CommonGlobals.songName.isNullOrEmpty()) {
            return CommonGlobals.songName!!
        }

        if (isMusicActive()) {
            return SONG_PLAYING
        }

        return SONG_NOT_PLAYING
    }

    @SuppressLint("PrivateApi")
    fun isHuaweiDevice(): Boolean {
        try {
            val propertyClass = Class.forName("android.os.SystemProperties")
            val method: Method = propertyClass.getMethod("get", String::class.java)
            val versionEmui = method.invoke(propertyClass, "ro.build.version.emui") as String
            if (versionEmui.startsWith("EmotionUI_")) {
                return true
            }
            return false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun isNotificationServiceRunning(context: Context): Boolean {
        val contentResolver: ContentResolver = context.contentResolver
        val enabledNotificationListeners =
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(
            "com.noisefit.receiver.service.NotificationAlertService"
        )
    }
}