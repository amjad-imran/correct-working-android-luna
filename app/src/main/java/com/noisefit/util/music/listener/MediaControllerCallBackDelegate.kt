package com.noisefit.util.music.listener


import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class MediaControllerCallBackDelegate(private val mSessionUpdateListener: OnMediaSessionUpdateListener) :
    MediaController.Callback() {
    override fun onMetadataChanged(metadata: MediaMetadata?) {
        super.onMetadataChanged(metadata)
        mSessionUpdateListener.onMediaSessionUpdate()
    }

    override fun onSessionDestroyed() {
        super.onSessionDestroyed()
        mSessionUpdateListener.onMediaSessionUpdate()
    }

    override fun onPlaybackStateChanged(state: PlaybackState?) {
        super.onPlaybackStateChanged(state)
        mSessionUpdateListener.onMediaSessionUpdate()
    }

    override fun onSessionEvent(event: String, extras: Bundle?) {
        super.onSessionEvent(event, extras)
        mSessionUpdateListener.onMediaSessionUpdate()
    }

    override fun onAudioInfoChanged(info: MediaController.PlaybackInfo) {
        super.onAudioInfoChanged(info)
        mSessionUpdateListener.onMediaSessionUpdate()
    }

    override fun onExtrasChanged(extras: Bundle?) {
        super.onExtrasChanged(extras)
        mSessionUpdateListener.onMediaSessionUpdate()
    }

    override fun onQueueChanged(queue: List<MediaSession.QueueItem>?) {
        super.onQueueChanged(queue)
        mSessionUpdateListener.onMediaSessionUpdate()
    }

    override fun onQueueTitleChanged(title: CharSequence?) {
        super.onQueueTitleChanged(title)
        mSessionUpdateListener.onMediaSessionUpdate()
    }
}