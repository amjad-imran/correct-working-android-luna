package com.oreo.ui.chatGpt.audio

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAudioAiCalibrationBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AudioAiCalibrationFragment :
    BaseFragment<FragmentAudioAiCalibrationBinding>(FragmentAudioAiCalibrationBinding::inflate) {

    private val viewModel: AudioCalibrationViewModel by viewModels()
    val args: AudioAiCalibrationFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setVideo()
    }

    private fun setVideo() {
       /* val fileName = ("android.resource://" + requireContext().packageName) + "/raw/video_chat_ai"
        val uri = Uri.parse(fileName)
        val videoView = binding.videoView
        videoView.setVideoURI(uri)
        videoView.stopPlayback()
        videoView.setOnPreparedListener { it.isLooping = true }
        videoView.start()*/
    }

    override fun initListener() {
        binding.ivAllSet.setOnClickListener {
            navigate(AudioAiCalibrationFragmentDirections.actionAudioAiCalibrationFragmentToAudioAiFragment(
                args.text
            ).apply {
                planType = args.planType
            })
        }

        binding.ivCross.setOnClickListener {
            navigateUpSafe()
        }

        binding.ivStart.setOnClickListener {
            if (viewModel.isRecording) return@setOnClickListener

            checkMicrophonePermission {
                showStep3(viewModel.maxAmpList.size, true)
                viewModel.startNewRecording()
            }
        }
    }

    private fun showStep3(pos: Int, listening: Boolean) {
        binding.tvHeader.text = when (pos) {
            1 -> getString(R.string.text_ai_header_2)
            2 -> getString(R.string.text_ai_header_3)
            else -> getString(R.string.text_speak)
        }
        binding.tvMessage.text = getString(R.string.text_hello_luna)
        binding.ivStart.imageAlpha = if (listening) 128 else 255
        binding.tvListening.apply {
            visible()
            text = if (listening) {
                getString(R.string.text_listening_dot)
            } else {
                getString(R.string.text_tap_to_speak)
            }
        }
    }

    override fun subscribeObservers() {

        viewModel.uiStates.observe(this) {
            it.getContent()?.let {
                when (it) {
                    AudioCalibUiStates.STEP_1 -> {
                        binding.tvHeader.text = getString(R.string.text_personalize_luna)
                        binding.tvMessage.text =
                            getString(R.string.text_help_luna_understand_your_voice)
                        binding.ivStart.gone()
                    }

                    AudioCalibUiStates.STEP_2 -> {
                        binding.tvHeader.text = getString(R.string.text_personalize_luna)
                        binding.tvMessage.text = getString(R.string.text_press_the_button_below)
                        binding.ivStart.visible()
                    }
                }
            }
        }

        viewModel.videoPlayState.observe(this) {
            if (it) {
                if (binding.videoView.isPlaying.not()) {
                    binding.videoView.start()
                }
            } else {
                binding.videoView.pause()
            }
        }

        viewModel.currentTimer.observe(this) {
            it.getContent()?.let { percent ->
                if (percent == 100) {
                    binding.progressBarRecording.gone()
                    val dataSize = viewModel.maxAmpList.size
                    viewModel.completionState.postValue(dataSize)
                    if (dataSize < 3) {
                        showStep3(dataSize, false)
                    } else {
                        viewModel.sameMaxAmp()
                        stateAllSet()
                    }
                } else {
                    binding.progressBarRecording.visible()
                    binding.progressBarRecording.progress = percent
                }

            }
        }

        viewModel.completionState.observe(this) {
            if (it == null) {
                binding.lytBottomChecks.root.gone()
                return@observe
            }
            showProgressState(it)
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

    }

    /**
     * 1,2,3
     */
    private fun showProgressState(position: Int) {
        binding.lytBottomChecks.root.visible()
        when (position) {
            1 -> {
                binding.lytBottomChecks.apply {
                    bg1.invisible()
                    bg2.visible()
                    bg3.visible()

                    ivCheck1.visible()
                    ivCheck2.gone()
                    ivCheck3.gone()
                }
            }

            2 -> {
                binding.lytBottomChecks.apply {
                    bg1.invisible()
                    bg2.invisible()
                    bg3.visible()

                    ivCheck1.visible()
                    ivCheck2.visible()
                    ivCheck3.gone()
                }
            }

            3 -> {
                binding.lytBottomChecks.apply {
                    bg1.invisible()
                    bg2.invisible()
                    bg3.invisible()

                    ivCheck1.visible()
                    ivCheck2.visible()
                    ivCheck3.visible()
                }
            }
        }
    }

    private fun stateAllSet() {
        viewModel.completionState.postValue(null)
        binding.tvHeader.text = ""
        binding.tvMessage.text = getString(R.string.text_you_re_all_set)
        binding.ivStart.invisible()
        binding.tvListening.gone()
        binding.progressBarRecording.gone()
        Handler(Looper.getMainLooper()).postDelayed({
            binding.tvMessage.text = getString(R.string.text_you_re_all_set)
            binding.ivAllSet.visible()
        }, 800)
    }

    private fun checkMicrophonePermission(callback: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            callback.invoke()
        } else {
            micPermissionResult.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private val micPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            showStep3(viewModel.maxAmpList.size, true)
            viewModel.startNewRecording()
        } else {
            context.showShortToast("Permission Required")
            navigateUpSafe()
        }
    }

    override fun onResume() {
        super.onResume()
        setVideo()
    }

}