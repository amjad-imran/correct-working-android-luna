package com.noisefit.ui.roundup

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import androidx.core.os.HandlerCompat
import androidx.fragment.app.*
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRoundUpLandingBinding
import com.noisefit.ui.common.*
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit_commans.utils.ImageUtil
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@AndroidEntryPoint
class RoundUpLandingFragment :
    BaseFragment<FragmentRoundUpLandingBinding>(FragmentRoundUpLandingBinding::inflate) {
    private var pagerAdapter: PagerAdapter? = null
    private val viewModel: RoundUpViewModel by viewModels()
    private var mediaPlayer: MediaPlayer? = null
    private var isPlay: Boolean = true
    private var audioManager: AudioManager? = null
    private var currentScrollState = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getYearlySummaryData()
    }

    private fun playMusic() {
        try {
            audioManager = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager?.adjustVolume(AudioManager.ADJUST_UNMUTE, 0)
            mediaPlayer = MediaPlayer.create(context, R.raw.summary_background_music)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    override fun onPause() {
        super.onPause()
        tryCatch {
            mediaPlayer?.pause()
        }
    }

    override fun onResume() {
        super.onResume()

        tryCatch {
            if (mediaPlayer?.isPlaying != true) {
                mediaPlayer?.start()
            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initListener() {

        binding.vLeft.setOnClickListener {
            binding.spb.previous()

        }
        binding.vRight.setOnClickListener {
            binding.spb.next()
        }

        binding.spb.viewPager?.setOnTouchListener(null)

        binding.lytToolbar.shareBtn.setOnClickListener {

            if (currentScrollState != SCROLL_STATE_IDLE) return@setOnClickListener

            viewModel.mLastClickTime?.let {
                if (SystemClock.elapsedRealtime() - it < 1000) {
                    return@setOnClickListener
                }
            }
            viewModel.mLastClickTime = SystemClock.elapsedRealtime()
            shareImageSocial()
        }

        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.lytToolbar.muteBtn.setOnClickListener {
            isPlay = if (isPlay) {
                audioManager?.adjustVolume(AudioManager.ADJUST_MUTE, 0)
                binding.lytToolbar.muteBtn.setImageResource(R.drawable.ic_sound_off)
                false
            } else {
                audioManager?.adjustVolume(AudioManager.ADJUST_UNMUTE, 0)
                binding.lytToolbar.muteBtn.setImageResource(R.drawable.ic_sound_on)
                true
            }
        }
    }

    override fun onDestroyView() {
        binding.spb.reset()
        try {
            if (audioManager != null)
                audioManager?.adjustVolume(AudioManager.ADJUST_UNMUTE, 0)
            if (mediaPlayer != null) {
                mediaPlayer?.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        super.onDestroyView()
    }


    override fun subscribeObservers() {
        viewModel.yearlySummaryData.observe(this) { data ->
            if (data != null) {
                playMusic()
                val dataCount =
                    if (viewModel.hasUserData(viewModel.yearlySummaryData.value)) 8 else 2
                pagerAdapter = fragmentManager?.let {
                    PagerAdapter(it, data).apply {
                        this.dataCount = dataCount
                    }
                }
                binding.vpSlider.adapter = pagerAdapter
                binding.spb.viewPager = binding.vpSlider
                binding.spb.segmentCount = dataCount
                binding.spb.start()
                binding.spb.timePerSegmentMs = 10000
                binding.spb.viewPager?.addOnPageChangeListener(object :
                    ViewPager.OnPageChangeListener {

                    override fun onPageScrollStateChanged(state: Int) {
                        currentScrollState = state
                    }

                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {


                    }

                    override fun onPageSelected(position: Int) {


                        if (position == 0) {
                            binding.spb.timePerSegmentMs = 10000
                        } else binding.spb.timePerSegmentMs = 5000

                        val lastPosition =
                            if (viewModel.hasUserData(viewModel.yearlySummaryData.value)) 7 else 1

                        if (position == lastPosition) {
                            binding.vRight.invisible()
                        } else {
                            binding.vRight.visible()
                        }

                        pagerAdapter?.notifyDataSetChanged()


                    }

                })

                pagerAdapter?.notifyDataSetChanged()

            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
    }


    fun shareImageSocial() {

        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        val mainThreadHandler: Handler = HandlerCompat.createAsync(Looper.getMainLooper())
        val view = nullableBinding?.root ?: return
        executorService.execute {
            mainThreadHandler.post {
                uiController.displayProgressBar(true, "")
                nullableBinding?.spb?.invisible()
                nullableBinding?.lytToolbar?.root?.invisible()
            }
            val shareBitmap = ImageUtil.getBitmapFromView(view)
            shareBitmap?.let {
                val shareUri = ImageUtil.getTempImageUri(it, requireContext())
                mainThreadHandler.post {
                    nullableBinding?.spb?.visible()
                    nullableBinding?.lytToolbar?.root?.visible()
                    ShareUtil.shareOthers(requireContext(), shareUri)
                    uiController.displayProgressBar(false, "")
                }
            }
            mainThreadHandler.post {
                nullableBinding?.spb?.visible()
                nullableBinding?.lytToolbar?.root?.visible()
                uiController.displayProgressBar(false, "")
            }
        }
    }


}