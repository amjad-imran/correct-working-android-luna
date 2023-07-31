package com.noisefit.ui.dashboard.summary

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.noisefit.R
import com.noisefit.databinding.FragmentShowcaseVideoBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ShowcaseVideoFragment :
    BaseFragment<FragmentShowcaseVideoBinding>(FragmentShowcaseVideoBinding::inflate) {
    private var currentPosition: Int = 0
    private var isPlay: Boolean = true
    private var temp: AudioManager? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnBoardVideo()
        temp = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        temp?.adjustVolume(AudioManager.ADJUST_UNMUTE, 0)
    }


    override fun onPause() {
        super.onPause()
        currentPosition = binding.videoOnboard.currentPosition
        binding.videoOnboard.pause()
    }

    override fun onResume() {
        super.onResume()
        binding.videoOnboard.seekTo(currentPosition)
        binding.videoOnboard.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (temp != null)
                temp?.adjustVolume(AudioManager.ADJUST_UNMUTE, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setOnBoardVideo() {
        binding.videoOnboard.apply {
//            setVideoURI(
//                Uri.parse(
//                    "android.resource://" + requireContext().packageName + "/" +
//                            R.raw.video_round_up
//                )
//            )
//            setOnCompletionListener {
//                it.seekTo(8000)
//                it.start()
//            }
            start()
        }
    }

    override fun initListener() {
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.ivSound.setOnClickListener {
            isPlay = if (isPlay) {
                temp?.adjustVolume(AudioManager.ADJUST_MUTE, 0)
                binding.ivSound.setImageResource(R.drawable.ic_sound_off)
                false
            } else {
                temp?.adjustVolume(AudioManager.ADJUST_UNMUTE, 0)
                binding.ivSound.setImageResource(R.drawable.ic_sound_on)
                true
            }
        }
    }

    override fun subscribeObservers() {

    }


}