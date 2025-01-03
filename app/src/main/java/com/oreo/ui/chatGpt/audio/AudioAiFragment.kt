package com.oreo.ui.chatGpt.audio

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAudioAiBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
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

        if (viewModel.isCalibrated().not()) {
            navigate(AudioAiFragmentDirections.actionAudioAiFragmentToAudioAiCalibrationFragment(
                args.text
            ).apply {
                planType = args.planType
            })
            return
        }

        if (args.planType == PlanType.WORKOUT) {
            binding.tvAskLuna.visible()
            args.text?.let {
                binding.tvMessage.text = getString(R.string.text_how_to_perform_a_value, it)
            }
        } else if (args.planType == PlanType.DIET) {
            binding.tvAskLuna.visible()
            val questions = arrayListOf(
                getString(R.string.text_diet_1),
                getString(R.string.text_diet_2)
            )
            binding.tvMessage.text = questions.random()
        }

        if (args.planType != PlanType.NONE) {
            binding.ivCross.setImageResource(R.drawable.ic_toolbar_back_ai)
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)

        viewModel.getCredentials()
        setVideo()
    }


    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.cleanup()
                navigateUpSafe()
            }
        }

    private fun setVideo() {
        val fileName = ("android.resource://" + requireContext().packageName) + "/raw/video_chat_ai"
        val uri = Uri.parse(fileName)
        val videoView = binding.videoView
        videoView.setVideoURI(uri)
        videoView.stopPlayback()
        videoView.setOnPreparedListener { it.isLooping = true }
        videoView.start()
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

    override fun onResume() {
        super.onResume()
        setVideo()
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
        binding.tvMessageTest.setOnClickListener {
            viewModel.localDataStore.saveAudioMaxAmp(0)
            viewModel.cleanup()
            navigateUpSafe()
        }

        binding.ivCross.setOnClickListener {
            viewModel.cleanup()
            navigateUpSafe()
        }

        binding.ivMic.setOnClickListener {
            if (viewModel.isAiReplying()) {
                viewModel.interruptAi()
                viewModel.audioAiState.postValue(AudioAiState.AI_TALKING_STOP)
            } else {
                if (viewModel.isMicOn) {
                    viewModel.audioAiState.postValue(AudioAiState.DEFAULT)
                    viewModel.stopRecording(false)
                    viewModel.sendRecordingToServer()
                } else {
                    viewModel.startNewRecording()
                }
            }
        }

        binding.ivTextChat.setOnClickListener {
            navigate(
                AudioAiFragmentDirections.actionAudioAiFragmentToAiTopQuestionsFragment(
                    AITopics.GENERAL
                )
            )
        }
    }

    private fun micStateOff() {
        viewModel.isMicOn = false
        binding.ivMic.setBackgroundColor(android.graphics.Color.parseColor("#F76968"))
        binding.ivMic.setImageResource(R.drawable.ic_ai_mic_off)
    }

    private fun micStateOn() {
        viewModel.isMicOn = true
        binding.ivMic.setBackgroundColor(android.graphics.Color.parseColor("#26FFFFFF"))
        binding.ivMic.setImageResource(R.drawable.ic_ai_mic)
    }

    override fun subscribeObservers() {
       /* viewModel.maxAmplitudeDebug.observe(this) {
            binding.tvMessageTest.apply {
                visible()
                text = "Amplitude: $it\nAMPLITUDE_MAX - ${viewModel.AMPLITUDE_MAX}"
            }
        }*/

        viewModel.audioAiState.observe(this) {
            when (it) {
                AudioAiState.DEFAULT -> {
                    binding.tvMessage.text = ""
                    binding.tvAskLuna.gone()
                    micStateOff()
                }

                AudioAiState.LISTENING -> {
                    if (binding.tvAskLuna.isVisible.not()) {
                        binding.tvMessage.text = getString(R.string.text_speak_now)
                    }
                    micStateOn()
                }

                AudioAiState.GENERATING -> {
                    binding.tvMessage.text = ""
                    binding.tvAskLuna.gone()
                    micStateOff()
                    viewModel.stopRecording(true)
                }

                AudioAiState.AI_TALKING -> {
                    binding.tvMessage.text = ""
                    binding.tvAskLuna.gone()
                    micStateOff()
                    //binding.tvMessage.gone()
                }

                AudioAiState.AI_TALKING_STOP -> {
                    viewModel.startNewRecording()
                }
            }
        }


       /* viewModel.textReceived.observe(this) {

            it.getContent()?.let {
                binding.tvMessageTest.text = viewModel.stringBuilder.toString()
            }
        }*/

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
    }
}