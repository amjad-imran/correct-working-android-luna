package com.noisefit.util.music.listener

import com.noisefit.util.music.mode.MusicMode

interface OnPlayingListener {
    fun onPlayStateUpdate(musicMode: MusicMode?)
}