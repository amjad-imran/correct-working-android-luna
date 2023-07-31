package com.noisefit.util.music.mode

import android.media.session.PlaybackState

class MusicMode private constructor(//名称
    val title: String?, //作家
    val artist: String?, //专辑
    val album: String?, //时长
    val duration: Long, val playState: Int
) {
    val isPlaying: Boolean
        get() = playState == PlaybackState.STATE_PLAYING
    val isPlayedStop: Boolean
        get() = PlaybackState.STATE_STOPPED == playState

    fun isSameMode(musicMode: MusicMode?): Boolean {
        if (musicMode != null) {
            val sameStatus = musicMode.playState == playState
            val sameTitle = equal(musicMode.title, title)
            val sameAlbum = equal(musicMode.album, album)
            val sameArtist = equal(musicMode.artist, artist)
            return sameStatus && sameTitle && sameAlbum && sameArtist
        }
        return false
    }

    private fun equal(from: String?, to: String?): Boolean {
        return from == null && to == null || from != null && from == to
    }

    companion object {
        fun createMusicMode(
            title: String?,
            artist: String?,
            album: String?,
            duration: Long,
            playState: Int
        ): MusicMode {
            return MusicMode(title, artist, album, duration, playState)
        }

        fun createStopMusicMode(): MusicMode {
            return MusicMode("", "", "", 0, PlaybackState.STATE_STOPPED)
        }
    }
}