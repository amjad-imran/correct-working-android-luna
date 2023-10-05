package com.oreo.ui.info

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SeekBar
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRingInfoPlayerBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import java.util.TimerTask


@AndroidEntryPoint
class RingInfoPlayerFragment :
    BaseFragment<FragmentRingInfoPlayerBinding>(FragmentRingInfoPlayerBinding::inflate) {
    val navArgs: RingInfoPlayerFragmentArgs by navArgs()
    var player: ExoPlayer? = null
    var timer: Timer? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPlayer()

    }

    private fun initPlayer() {

        binding.videoPlayer.setOnClickListener {
            if (binding.groupControls.visibility == View.VISIBLE) {
                binding.groupControls.gone()
                binding.progressBarVideoWithoutThumb.visible()
            } else {
                binding.groupControls.visible()
                binding.progressBarVideoWithoutThumb.gone()
            }
        }




        player = ExoPlayer.Builder(binding.videoPlayer.context).build()
            .also { exoPlayer ->
                binding.videoPlayer.player = exoPlayer
                exoPlayer.playWhenReady = true
                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        //do some code
                        if (playbackState == ExoPlayer.STATE_ENDED) {
                            exoPlayer.seekTo(0)
                            exoPlayer.pause()
                            binding.groupControls.visible()
                            binding.progressBarVideoWithoutThumb.gone()
                            binding.pbLoading.gone()
                            binding.ivPlay.visible()
                            binding.ivPlay.setImageResource(R.drawable.ic_play_info)
                        } else if (playbackState == ExoPlayer.STATE_BUFFERING) {
                            binding.groupControls.gone()
                            binding.pbLoading.visible()
                        } else if (playbackState == ExoPlayer.STATE_READY) {

                            binding.progressBarVideo.max = (exoPlayer.duration / 1000).toInt()
                            binding.progressBarVideoWithoutThumb.max =
                                (exoPlayer.duration / 1000).toInt()
                            val time = ApplicationUtils.getFormattedVideoDurationFromSeconds(
                                (exoPlayer.duration / 1000).toInt()
                            )
                            binding.tvVideoDuration.text = time

                            binding.ivPlay.setImageResource(R.drawable.ic_pause_info)
                            binding.groupControls.gone()
                            binding.progressBarVideoWithoutThumb.visible()
                            binding.pbLoading.gone()
                        }
                    }
                })
            }
        val mediaItem: MediaItem =
            MediaItem.fromUri(
                Uri.parse(
                    navArgs.videoUrl
                )
            )
        player?.setMediaItem(mediaItem)
        player?.prepare()

        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    val currentPos = player?.currentPosition ?: 0
                    if (currentPos == 0L) {
                        nullableBinding?.progressBarVideo?.progress = 0
                        nullableBinding?.progressBarVideoWithoutThumb?.progress = 0
                    } else {
                        nullableBinding?.progressBarVideo?.progress = (currentPos / 1000).toInt()
                        nullableBinding?.progressBarVideoWithoutThumb?.progress = (currentPos / 1000).toInt()
                    }
                }
            }
        }, 0, 1000)
    }


    override fun initListener() {

        binding.progressBarVideo.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (player != null && seekBar != null) {
                    player?.seekTo(seekBar.progress * 1000L)
                }
            }

        })

        binding.ivBack10.setOnClickListener {
            player?.let {
                val newPos = it.currentPosition - 10000
                if (newPos > 0) {
                    it.seekTo(newPos)
                } else {
                    it.seekTo(0)
                }
            }
        }
        binding.ivForward10.setOnClickListener {
            player?.let {
                val newPos = it.currentPosition + 10000
                if (newPos < it.duration) {
                    it.seekTo(newPos)
                }
            }
        }

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.ivPlay.setOnClickListener {
            if (player == null)
                return@setOnClickListener

            if (player?.isPlaying == true) {
                binding.groupControls.visible()
                binding.progressBarVideoWithoutThumb.gone()
                binding.ivPlay.setImageResource(R.drawable.ic_play_info)
                player?.pause()

            } else {
                binding.groupControls.gone()
                binding.progressBarVideoWithoutThumb.visible()
                binding.ivPlay.setImageResource(R.drawable.ic_pause_info)
                player?.play()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= 24) {
            releasePlayer()
        }
        timer?.cancel()
    }

    private fun releasePlayer() {
        player?.run {
            release()
        }
        player = null
    }

    override fun onPause() {
        super.onPause()
        binding.ivPlay.setImageResource(R.drawable.ic_play_info)
        player?.pause()
    }

    override fun subscribeObservers() {

    }


}