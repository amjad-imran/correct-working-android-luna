package com.oreo.ui.chatGpt.audio

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAudioAiBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.LOGS
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.ChatGptFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AudioAiFragment : BaseFragment<FragmentAudioAiBinding>(FragmentAudioAiBinding::inflate) {

    val viewModel: AudioAiViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvMessage.text = getString(R.string.text_setting_up)

        checkMicrophonePermission {
            viewModel.initSpeechRecognizer(listener)
        }

        //viewModel.getAudioResponse("Who is )
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
            viewModel.initSpeechRecognizer(listener)
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
            if (viewModel.isInitSuccess) {
                viewModel.startListening()
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

    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            nullableBinding?.tvMessage?.text = ""

            LOGS.d("RecognitionListener", "onReadyForSpeech() $params")
            viewModel.startListening()
        }

        override fun onBeginningOfSpeech() {
            LOGS.d("RecognitionListener", "onBeginningOfSpeech()")
        }

        override fun onRmsChanged(rmsdB: Float) {
            //LOGS.d("RecognitionListener", "onRmsChanged - $rmsdB")
            //binding.lytAudio.viewAudioVisualizer.updateRms(rmsdB)
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            LOGS.d("RecognitionListener", "onBufferReceived() - $buffer")
        }

        override fun onEndOfSpeech() {
            LOGS.d("RecognitionListener", "onEndOfSpeech()")
            startListeningWithDelay()

        }

        override fun onError(error: Int) {
            LOGS.d("RecognitionListener", "onError() - $error")
            if(error== SpeechRecognizer.ERROR_NO_MATCH){
                startListeningWithDelay()
            }

        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.getOrNull(0) ?: ""
            //binding.lytAudio.testUserText.text = text
            LOGS.d("RecognitionListener", "onResults ${matches?.getOrNull(0)}")
            viewModel.getAudioResponse(text)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial =
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            LOGS.d("RecognitionListener", " onPartialResults ${partial?.getOrNull(0)}")
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            LOGS.d("RecognitionListener", " onEvent $eventType")
        }
    }

    fun startListeningWithDelay(){
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.startListening()
        }, 500)
    }

}