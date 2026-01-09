package com.oreo.ui.chatGpt.audio

import ChatAdapter
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsVoiceChatBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LifeOSVoiceChatFragment :
    BaseFragment<FragmentLifeOsVoiceChatBinding>(FragmentLifeOsVoiceChatBinding::inflate) {
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var chatAdapter: ChatAdapter
    private val viewModel: LifeOSVoiceChatViewModel by viewModels()
    private val mp3Streamer: Mp3Streamer by lazy {
        Mp3Streamer(requireContext())
    }
    private var currentState = ActionState.LISTENING

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        setupSpeechRecognizer()
        viewModel.generateThreadId()
    }

    override fun onResume() {
        super.onResume()
        if (currentState == ActionState.LISTENING) {
            setActionState(ActionState.LISTENING)
        }
    }

    override fun initListener() {
        binding.ivBtnAction.setOnClickListener {
            when (currentState) {
                ActionState.LISTENING -> {
                    speechRecognizer.stopListening()
                }

                ActionState.SPEAKING, ActionState.THINKING -> {
                    mp3Streamer.stop()
                    setActionState(ActionState.LISTENING)
                }

                else -> {}
            }
        }
    }

    private fun checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startListening()
        } else {
            micPermissionResult.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private val micPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            startListening()
        } else {
            context.showShortToast("Permission Required")
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        viewModel.chatMessages.observe(viewLifecycleOwner) {
            chatAdapter.submitMessages(it)
            binding.chatRecycler.scrollToPosition(it.size - 1)
        }
        viewModel.audioStream.observe(viewLifecycleOwner) { audioStream ->
            audioStream?.let {
                lifecycleScope.launch {
                    mp3Streamer.addChunk(it)
                }
                setActionState(ActionState.SPEAKING)
            }
        }
        mp3Streamer.isSpeaking.observe(viewLifecycleOwner) {
            if (it == false) {
                setActionState(ActionState.LISTENING)
            }
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                30000L
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
        }
        speechRecognizer.startListening(intent)
    }

    private fun setupRecycler() {
        chatAdapter = ChatAdapter(mutableListOf())
        binding.chatRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }
    }

    private fun setActionState(state: ActionState) {
        currentState = state
        when (state) {
            ActionState.LISTENING -> {
                binding.lvThinking.gone()
                binding.lvListening.apply {
                    this.visible()
                    clearValueCallback(KeyPath("**"), LottieProperty.COLOR)
                }
                binding.tvActionText.text = getString(R.string.text_listening)

                binding.ivBtnAction.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_voice_listening)
                )
                checkMicrophonePermission()
            }

            ActionState.SPEAKING -> {
                binding.lvThinking.gone()
                binding.lvListening.apply {
                    this.visible()
                    addValueCallback(
                        KeyPath("**"),
                        LottieProperty.COLOR
                    ) { "#FFFFFF".toColorInt() }
                }
                binding.tvActionText.text = getString(R.string.text_speaking)
                binding.ivBtnAction.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_voice_speak)
                )
            }

            ActionState.THINKING -> {
                binding.lvListening.gone()
                binding.lvThinking.visible()
                binding.tvActionText.text = getString(R.string.text_thinking)
            }

            ActionState.STARTING_UP -> {
                binding.lvListening.gone()
                binding.lvThinking.visible()
                binding.tvActionText.text = getString(R.string.starting_up)
            }
        }
    }
    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?: return

                viewModel.addUserMessage(text)
            }

            override fun onReadyForSpeech(p0: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(p0: Int) {
                startListening()
            }
            override fun onPartialResults(p0: Bundle?) {}
            override fun onEvent(p0: Int, p1: Bundle?) {}
        })
    }
}

enum class ActionState { LISTENING, SPEAKING, THINKING, STARTING_UP }