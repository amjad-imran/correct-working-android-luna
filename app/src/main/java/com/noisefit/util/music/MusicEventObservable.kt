package com.noisefit.util.music

import android.content.ComponentName
import android.content.Context
import com.noisefit.util.music.listener.OnMediaSessionUpdateListener
import com.noisefit.util.music.listener.OnPlayingListener
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
import com.noisefit.util.music.listener.MediaControllerCallBackDelegate
import android.content.Intent
import android.os.Looper
import com.noisefit.util.music.mode.MusicMode
import android.media.session.PlaybackState
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.os.Handler
import android.provider.Settings
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.LOGS
import java.util.ArrayList
import kotlin.Exception

class MusicEventObservable(
    private val mNotificationListenerService: ComponentName,
    private val localDataStore: DataStoredInterface
) :
    OnMediaSessionUpdateListener {
    private var mPlayingListener: OnPlayingListener? = null
    private var mCurrentController: MediaController? = null
    private val mSessionChangedListener: OnActiveSessionsChangedListener
    private var mMediaSessionManager: MediaSessionManager? = null
    private val mCallBack: MediaControllerCallBackDelegate = MediaControllerCallBackDelegate(this)

    fun setPlayingListener(onPlayingListener: OnPlayingListener?) {
        mPlayingListener = onPlayingListener
    }

    private fun isOpenNotification(context: Context): Boolean {
        if (Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                .contains(context.packageName)
        ) {
            LOGS.d("locationResult", " Start Music Control isOpenNotification false")
            return true
        } else {
            if (localDataStore.getConnectedDevice() == null) return false
            if (!localDataStore.isNotificationAlertEnabled()) return false

            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        //return true;
        // return mNotificationListenerService != null &&
        //         NotificationSecureUtil.checkNotificationPermission(context, mNotificationListenerService);
        return true
    }

    fun setUp(context: Context) {
        //Log.d("locationResult", "Music Control");
        try {
            if (isOpenNotification(context)) {
                mMediaSessionManager =
                    context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
                mMediaSessionManager!!.addOnActiveSessionsChangedListener(
                    mSessionChangedListener,
                    mNotificationListenerService,
                    Handler(Looper.getMainLooper())
                )
                checkController()
            }
        } catch (ignored: Exception) {
            ignored.printStackTrace()
        }
    }

    fun release() {
        if (mMediaSessionManager != null) {
            mMediaSessionManager!!.removeOnActiveSessionsChangedListener(mSessionChangedListener)
            mMediaSessionManager = null
        }
    }

    private fun checkController() {
        val mediaController = pickController()
        if (mediaController != null && mediaController != mCurrentController) {
            if (mCurrentController != null) {
                mCurrentController!!.unregisterCallback(mCallBack)
            }
            mediaController.registerCallback(mCallBack, Handler(Looper.getMainLooper()))
            mCurrentController = mediaController
        }
        if (mediaController == null) {
            mCurrentController = null
            mPlayingListener!!.onPlayStateUpdate(MusicMode.createStopMusicMode())
        }
    }

    fun pickController(): MediaController? {
        var mediaController: MediaController? = null
        if (mMediaSessionManager != null) {
            try {
                val controllers =
                    mMediaSessionManager!!.getActiveSessions(mNotificationListenerService)
                if (controllers.size > 0) {
//                    mediaController = controllers.get(0);
                    mediaController = getRealMediaController(controllers)
                }
            } catch (ignored: Exception) {
            }
        }
        return mediaController
    }

    private fun getRealMediaController(controllers: List<MediaController>): MediaController? {
        var mediaController: MediaController? = null
        if (controllers.size == 1) {
            mediaController = controllers[0]
        } else if (controllers.size > 1) {
            for (i in controllers.indices) {
                // 过滤条件：通过metadata和playbackState
                if (null == controllers[i].metadata && null == controllers[i].playbackState) {
                    continue
                }
                mediaController = controllers[i]
                break
            }
        }
        return mediaController
    }

    fun pickController(packageName: String): List<MediaController> {
        val mediaControllerList: MutableList<MediaController> = ArrayList()
        if (mMediaSessionManager != null) {
            try {
                val controllers =
                    mMediaSessionManager!!.getActiveSessions(mNotificationListenerService)
                for (mediaController in controllers) {
                    if (packageName == mediaController.packageName) {
                        mediaControllerList.add(mediaController)
                    }
                }
            } catch (ignored: SecurityException) {
            }
        }
        return mediaControllerList
    }

    fun notifyMediaSessionUpdate() {
        checkController()
        if (mCurrentController != null) {
            var playState = PlaybackState.STATE_NONE
            val mediaMetadata = mCurrentController!!.metadata
            val state = mCurrentController!!.playbackState
            if (state != null) {
                playState = state.state
            }
            if (mediaMetadata != null) {
                val musicMode = getMusicMode(mediaMetadata, playState)
                mPlayingListener?.onPlayStateUpdate(musicMode)
            } else {
                mPlayingListener?.onPlayStateUpdate(MusicMode.createStopMusicMode())
            }
        }
    }

    private fun getMusicMode(mediaMetadata: MediaMetadata, playState: Int): MusicMode {
        try {
            val title = mediaMetadata.getString(MediaMetadata.METADATA_KEY_TITLE)
            val artist = mediaMetadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
            val album = mediaMetadata.getString(MediaMetadata.METADATA_KEY_ALBUM)
            val duration = mediaMetadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
            LOGS.d("locationResult", "Music Control $title")
            return MusicMode.createMusicMode(title, artist, album, duration, playState)
        } catch (exp: Exception) {
            // catch java.lang.RuntimeException: Could not read bitmap blob.
            return MusicMode.createMusicMode("", "", "", 0, playState)
        }
    }

    override fun onMediaSessionUpdate() {
        notifyMediaSessionUpdate()
    }

    init {
        mSessionChangedListener =
            OnActiveSessionsChangedListener { checkController() }
    }
}