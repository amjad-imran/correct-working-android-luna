package com.oreo.ui.chatGpt.audio

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAudioAiBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.ChatGptFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AudioAiFragment : BaseFragment<FragmentAudioAiBinding>(FragmentAudioAiBinding::inflate) {

    val viewModel: AudioAiViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvMessage.text = getString(R.string.text_setting_up)
        viewModel.getCredentials()

        setVideo()
    }


    private fun setVideo() {
        val fileName = ("android.resource://" + requireContext().packageName) + "/raw/video_chat_ai"
        val uri = Uri.parse(fileName)
        val videoView = binding.videoView
        videoView.setVideoURI(uri)
        videoView.stopPlayback()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.waveRecorder?.stopRecording(true)
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
            binding.ivMic.visible()
            viewModel.startNewRecording()
        } else {
            context.showShortToast("Permission Required")
            navigateUpSafe()
        }
    }


    override fun initListener() {
        binding.ivCross.setOnClickListener {
            navigateUpSafe()
        }

        binding.ivMic.setOnClickListener {
            if (viewModel.isRecording) {
                binding.ivMic.setBackgroundColor(android.graphics.Color.parseColor("#F76968"))
                binding.ivMic.setImageResource(R.drawable.ic_ai_mic_off)
                viewModel.waveRecorder?.stopRecording(false)
                viewModel.sendRecordingToServer()

                viewModel.isRecording = false
            } else {
                binding.ivMic.setBackgroundColor(android.graphics.Color.parseColor("#26FFFFFF"))
                binding.ivMic.setImageResource(R.drawable.ic_ai_mic)
                viewModel.waveRecorder?.startRecording()
                viewModel.isRecording = true
            }
        }

        binding.ivTextChat.setOnClickListener {
            val (frag, bundle) = ChatGptFragment.getStartData(
                null,
                null,
                null,
                null,
                AITopics.GENERAL
            )
            navigate(frag, bundle)
        }
    }

    override fun subscribeObservers() {
        viewModel.onCredentialsReceived.observe(this) {
            it.getContent()?.let {
                binding.tvMessage.text = ""
                checkMicrophonePermission {
                    binding.ivMic.visible()
                    viewModel.startNewRecording()
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

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getLoading().observe(this) {
            /* if (it) {
                 binding.progressBar.root.visible()
             } else {
                 binding.progressBar.root.gone()
             }*/
        }

        /*  viewModel.audioAiState.observe(this) {
              when (it) {
                  AudioAiState.DEFAULT -> binding.tvMessage.text = "Default"
                  AudioAiState.LISTENING -> binding.tvMessage.text = "Listening"
                  AudioAiState.GENERATING -> binding.tvMessage.text = "Generating"
                  AudioAiState.TALKING -> binding.tvMessage.text = "Talking"
                  else -> binding.tvMessage.text = ""
              }
          }*/
    }
}