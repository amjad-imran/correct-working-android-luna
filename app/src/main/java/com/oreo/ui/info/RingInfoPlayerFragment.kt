package com.oreo.ui.info

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRingCareBinding
import com.noisefit.luna.databinding.FragmentRingInfoPlayerBinding
import com.noisefit.luna.databinding.FragmentRingWelcomeBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RingInfoPlayerFragment :
    BaseFragment<FragmentRingInfoPlayerBinding>(FragmentRingInfoPlayerBinding::inflate) {
    val navArgs: RingInfoPlayerFragmentArgs by navArgs()
    var player: ExoPlayer? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPlayer()

    }

    private fun initPlayer() {

        binding.videoPlayer.setControllerVisibilityListener { visibility ->
            if (visibility == View.VISIBLE) {
                binding.groupControls.visible()
            } else {
                binding.groupControls.gone()
            }
        }

        player = ExoPlayer.Builder(binding.videoPlayer.context).build()
            .also { exoPlayer ->
                binding.videoPlayer.player = exoPlayer
                exoPlayer.playWhenReady = false
                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        //do some code
                        if (playbackState == ExoPlayer.STATE_ENDED) {
                            exoPlayer.seekTo(0)
                            exoPlayer.pause()
                            binding.groupControls.visible()
                            binding.ivPlay.visible()
                            binding.ivPlay.setImageResource(R.drawable.ic_play_info)
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
    }


    override fun initListener() {

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.ivPlay.setOnClickListener {
            if (player == null)
                return@setOnClickListener

            if (player?.isPlaying == true) {
                binding.groupControls.visible()
                binding.ivPlay.setImageResource(R.drawable.ic_play_info)
                player?.pause()

            } else {
                binding.groupControls.gone()
                binding.ivPlay.setImageResource(R.drawable.ic_pause_info)
                player?.play()
            }
        }
    }

    override fun subscribeObservers() {

    }


}