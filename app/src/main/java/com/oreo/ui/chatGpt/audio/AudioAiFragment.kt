package com.oreo.ui.chatGpt.audio

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAudioAiBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.ChatGptFragment
import com.oreo.ui.chatGpt.PlanType
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AudioAiFragment : BaseFragment<FragmentAudioAiBinding>(FragmentAudioAiBinding::inflate) {

    val viewModel: AudioAiViewModel by viewModels()
    val args: AudioAiFragmentArgs by navArgs()

    companion object {
        fun getStartData(
            planType: PlanType,
            text: String? = null,
        ): Pair<Int, Bundle?> {
            return Pair(R.id.audioAiFragment, Bundle().apply {
                putSerializable("planType", planType)
                putString("text", text)
            })
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //binding.tvMessage.text = getString(R.string.text_setting_up)

        if (args.planType == PlanType.WORKOUT) {
            binding.tvAskLuna.visible()
            args.text?.let {
                binding.tvMessage.text = "How to perform a ${it}?"
            }
        }

        viewModel.getCredentials()

        setVideo()
    }


    private fun setVideo() {
        val fileName = ("android.resource://" + requireContext().packageName) + "/raw/video_chat_ai"
        val uri = Uri.parse(fileName)
        val videoView = binding.videoView
        videoView.setVideoURI(uri)
        videoView.stopPlayback()
        videoView.setOnPreparedListener { it.isLooping = true }
    }


    override fun onDestroyView() {
        viewModel.cleanup()
        super.onDestroyView()
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
            viewModel.cleanup()
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
        viewModel.audioAiState.observe(this) {
            when (it) {
                AudioAiState.DEFAULT -> {}
                AudioAiState.LISTENING -> {}
                AudioAiState.GENERATING -> {}
                AudioAiState.AI_TALKING -> {
                    binding.tvAskLuna.gone()
                    binding.tvMessage.gone()
                }
            }
        }


        viewModel.textReceived.observe(this) {

            it.getContent()?.let {
                binding.tvMessageTest.text = viewModel.stringBuilder.toString()
            }
        }

        viewModel.onCredentialsReceived.observe(this) {
            it.getContent()?.let {
                //binding.tvMessage.text = ""
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