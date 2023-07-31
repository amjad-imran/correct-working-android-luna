package com.noisefit.ui.settings.helpAndSupport

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.video.VideoSize
import com.noisefit.R
import com.noisefit.databinding.FragmentHelpAndSupportVideoPlayerBinding
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEYS
import com.noisefit_commans.data.response.VideoData
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HelpAndSupportVideoPlayerFragment :
    BaseFragment<FragmentHelpAndSupportVideoPlayerBinding>(FragmentHelpAndSupportVideoPlayerBinding::inflate) {
    private var mExoplayer: ExoPlayer? = null
    private val viewModel: HSVPViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val listData =
                HelpAndSupportVideoPlayerFragmentArgs.fromBundle(it).videoListData?.toList()
                    ?.let { it1 -> ArrayList(it1) }
            if (listData != null) {
                viewModel.helpAndSupportVideoData = listData
                handleVideoContent(listData)
            }

        }


    }

    private fun initPlayer(videoUrl: String) {
        binding.lytSvPlayer.root.visible()
        mExoplayer =
            ExoPlayer.Builder(binding.lytSvPlayer.videoPlayer.context)
                .setSeekBackIncrementMs(10000)
                .setSeekForwardIncrementMs(10000).build()
                .also { exoPlayer ->
                    binding.lytSvPlayer.videoPlayer.player = exoPlayer
                    exoPlayer.playWhenReady = true
                    exoPlayer.addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == ExoPlayer.STATE_BUFFERING) {
                                nullableBinding?.lytSvPlayer?.progressBar?.visible()
                            } else if (playbackState == ExoPlayer.STATE_READY) {
                                nullableBinding?.lytSvPlayer?.progressBar?.gone()
                            }
                        }

                        override fun onVideoSizeChanged(videoSize: VideoSize) {
                        }
                    })
                }

        binding.lytSvPlayer.videoPlayer.keepScreenOn = true
        val mediaItem: MediaItem =
            MediaItem.fromUri(Uri.parse(videoUrl))
        mExoplayer?.setMediaItem(mediaItem)
        mExoplayer?.prepare()
//        setVideoThumbnail()
    }

    private fun handleVideoContent(videoData: List<VideoData>?) {
        if (videoData.isNullOrEmpty()) {
            binding.lytSvPlayer.root.gone()
        } else {
            binding.lytSvPlayer.root.visible()
            if (videoData.size == 1) {
                binding.lytToolbar.ivSetting.gone()
                viewModel.videoThumbnailUrl =
                    videoData[0].thumbnail.toString()
                videoData[0].url?.let {
                    initPlayer(
                        it
                    )
                }

            } else {
                binding.lytToolbar.ivSetting.visible()
                viewModel.setLanguageValues(viewModel.parseLanguageData(videoData))
                viewModel.videoThumbnailUrl =
                    videoData[viewModel.defaultVideoPos].thumbnail.toString()
                videoData[viewModel.defaultVideoPos].url?.let {
                    initPlayer(
                        it
                    )
                }

            }
        }

    }


    override fun initListener() {

        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytToolbar.ivSetting.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEYS) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                val selectedPos = bundle.getInt("selectedPosition")

                if (selectedPos != null) {
                    viewModel.defaultVideoPos = selectedPos
                }
                selectedValue?.let { it1 ->
                    viewModel.setLanguage(it1)
                    updateVideoContent()
                }


            }
            navigate(
                HelpAndSupportVideoPlayerFragmentDirections.actionHsVideoPlayerFragmentToHsLanguageValueSelectorBottomSheet(
                    viewModel.getSelectedLanguageValue(),
                    viewModel.getLanguageValues(),
                    getString(R.string.text_video_guide_langugae)
                )
            )
        }

    }

    private fun updateVideoContent() {
        releasePlayer()
        viewModel.isFirstTimeLoad = true
        viewModel.videoThumbnailUrl =
            viewModel.helpAndSupportVideoData.get(viewModel.defaultVideoPos).thumbnail.toString()
        viewModel.helpAndSupportVideoData.get(viewModel.defaultVideoPos).url?.let {
            initPlayer(
                it
            )
        }

    }


    override fun subscribeObservers() {

    }

    override fun onPause() {
        super.onPause()
        if (mExoplayer != null) {
            mExoplayer?.pause()
        }
//        setVideoThumbnail()
    }

    override fun onResume() {
        super.onResume()
        if (mExoplayer != null) {
            mExoplayer?.play()
        }
        playAndHideThumbnail()
    }

    private fun playAndHideThumbnail() {
        mExoplayer?.playWhenReady = true
        binding.lytSvPlayer.imagePlay.invisible()
        binding.lytSvPlayer.viewThumbnailBackground.invisible()
        binding.lytSvPlayer.imageThumbnail.invisible()

    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        mExoplayer?.run {
            playWhenReady = this.playWhenReady
            release()
        }
        mExoplayer = null
    }


//    private fun setVideoThumbnail() {
//        var bitmap: Bitmap? = null
//        if (viewModel.isFirstTimeLoad) {
//            viewModel.isFirstTimeLoad = false
//            Glide.with(binding.lytSvPlayer.imageThumbnail.context)
//                .asBitmap().load(viewModel.videoThumbnailUrl)
//                .into(object : CustomTarget<Bitmap>() {
//                    override fun onLoadCleared(placeholder: Drawable?) {}
//                    override fun onResourceReady(
//                        resource: Bitmap,
//                        transition: Transition<in Bitmap>?
//                    ) {
//                        bitmap = resource
//                        nullableBinding?.lytSvPlayer?.apply {
//                            imageThumbnail.setImageBitmap(bitmap)
//                            viewThumbnailBackground.visible()
//                            imageThumbnail.visible()
//                            imagePlay.visible()
//                        }
//                    }
//                })
//        } else {
//            val textureView =
//                binding.lytSvPlayer.videoPlayer.videoSurfaceView as TextureView
//            bitmap = textureView.bitmap
//            binding.lytSvPlayer.imageThumbnail.setImageBitmap(bitmap)
//            binding.lytSvPlayer.viewThumbnailBackground.visible()
//            binding.lytSvPlayer.imageThumbnail.visible()
//            binding.lytSvPlayer.imagePlay.visible()
//        }
//    }

}