package com.noisefit.receiver.broadcastReceiver

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import com.noisefit.session.SessionManager
import com.noisefit_commans.interfaces.QueryAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext


class AudioSettingReceiver(
    val context: Context,
    val handler: Handler?,
    val sessionManager: SessionManager?
) :
    ContentObserver(handler) {

    private val audioManager: AudioManager? =
        (context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager)

    override fun deliverSelfNotifications(): Boolean {
        return false
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private val backgroundDispatcher = newFixedThreadPoolContext(1, "Audio")


    override fun onChange(selfChange: Boolean) {
        try {
            scope.launch(backgroundDispatcher) {

                val currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

                if (currentVolume != null && maxVolume != null) {
                    sessionManager?.sendQueryAction(
                        QueryAction.UpdateVolume(
                            currentVolume,
                            maxVolume
                        )
                    )
                }
            }
        } catch (exp: Exception) {
            exp.printStackTrace()
            //Exceptions thrown by getStreamVolume
        }
    }


}
