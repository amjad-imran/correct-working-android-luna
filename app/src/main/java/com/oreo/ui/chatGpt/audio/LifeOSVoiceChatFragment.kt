package com.oreo.ui.chatGpt.audio

import ChatAdapter
import VoiceChatMessage
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
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
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.VolumeObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import android.provider.Settings
@AndroidEntryPoint
class LifeOSVoiceChatFragment :
    BaseFragment<FragmentLifeOsVoiceChatBinding>(FragmentLifeOsVoiceChatBinding::inflate) {
    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var chatAdapter: ChatAdapter
    private val viewModel: LifeOSVoiceChatViewModel by viewModels()
    private var isRecognizerActive = false
    private val mp3Streamer: Mp3Streamer by lazy {
        Mp3Streamer(requireContext())
    }
    private var currentState = ActionState.LISTENING
    private var isRecognizerCommiting = AtomicBoolean(false)
    private var isMuted = false
    private var volumeObserver: VolumeObserver? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        viewModel.generateThreadId()
    }

    override fun onStop() {
        viewModel.disposeChatStream()
        releaseSpeechRecognizer()
        mp3Streamer.stop()
        volumeObserver?.let {
            requireContext().contentResolver.unregisterContentObserver(it)
        }
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        if(currentState != ActionState.ERROR)
            setActionState(ActionState.LISTENING)
        if(viewModel.chatMessages.value.isNullOrEmpty().not()){
            binding.tvStartTalking.gone()
        }
        volumeObserver = VolumeObserver(
            requireContext(),
            Handler(Looper.getMainLooper())
        ) { isLow ->
            if (isLow) {
                binding.tvTurnVolumeUp.visible()
            } else {
                binding.tvTurnVolumeUp.gone()
            }
        }.apply {
            requireContext().contentResolver.registerContentObserver(
                Settings.System.CONTENT_URI,
                true,
                this
            )
            notifyIfChanged()
        }
    }

    override fun onDestroyView() {
        viewModel.disposeChatStream()
        releaseSpeechRecognizer()
        super.onDestroyView()
    }

    override fun initListener() {
        binding.ivCross.setOnClickListener { navigateUpSafe() }
        binding.ivBtnAction.setOnClickListener {
            when (currentState) {
                ActionState.LISTENING -> {
                    releaseSpeechRecognizer()
                    setActionState(ActionState.MUTE)
                }

                ActionState.SPEAKING, ActionState.THINKING, ActionState.MUTE -> {
                    mp3Streamer.stop()
                    viewModel.disposeChatStream()
                    setActionState(ActionState.LISTENING)
                }

                ActionState.ERROR -> {
                    viewModel.lastPrompt
                        .takeIf { it.isNotBlank() }
                        ?.let{
                            setActionState(ActionState.THINKING)
                            viewModel.askQuestionStream(it)
                        }
                }
                else -> {}
            }
        }
        binding.ivPersonalization.setOnClickListener {
            viewModel.disposeChatStream()
            navigate(
                R.id.choosePersonaVoiceFragment,
                bundleOf(
                    "isFromVoiceChat" to true,
                )
            )
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
        viewModel.streamError.observe(viewLifecycleOwner) { errorText ->
            currentState = ActionState.ERROR
            setActionUIAndVisibility(
                isThinking = false,
                isListening = false,
                isMute = true,
                R.drawable.ic_voice_retry,
                errorText
            )
        }
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
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 13000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 13000)
        }
        isRecognizerActive = true
        speechRecognizer?.startListening(intent)
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
        isMuted = false
        when (state) {
            ActionState.LISTENING -> {
                binding.lvListening.apply {
                    clearValueCallback(KeyPath("**"), LottieProperty.COLOR)
                }
                setActionUIAndVisibility(
                    isThinking = false,
                    isListening = true,
                    isMute = false,
                    R.drawable.ic_voice_listening,
                    getString(R.string.text_listening)
                )
                setupSpeechRecognizer()
                checkMicrophonePermission()
            }

            ActionState.SPEAKING -> {
                binding.lvListening.apply {
                    addValueCallback(
                        KeyPath("**"),
                        LottieProperty.COLOR
                    ) { "#FFFFFF".toColorInt() }
                }
                setActionUIAndVisibility(
                    isThinking = false,
                    isListening = true,
                    isMute = false,
                    R.drawable.ic_voice_speak,
                    getString(R.string.text_speaking)
                )
            }

            ActionState.THINKING -> {
                setActionUIAndVisibility(
                    isThinking = true,
                    isListening = false,
                    isMute = false,
                    R.drawable.ic_voice_speak,
                    getString(R.string.text_thinking)
                )
            }

            ActionState.STARTING_UP -> {
                setActionUIAndVisibility(
                    isThinking = true,
                    isListening = false,
                    isMute = false,
                    R.drawable.ic_voice_speak,
                    getString(R.string.starting_up)
                )
            }

            ActionState.MUTE -> {
                isMuted = true
                setActionUIAndVisibility(
                    isThinking = false,
                    isListening = false,
                    isMute = true,
                    R.drawable.ic_voice_mute,
                    getString(R.string.text_unable_to_speak)
                )
            }

            ActionState.ERROR -> {}
        }
    }
    private fun setActionUIAndVisibility(
        isThinking: Boolean,
        isListening: Boolean,
        isMute: Boolean,
        drawable: Int,
        text: String
    ) {
        binding.lvThinking.gone()
        binding.lvListening.gone()
        binding.ivMute.gone()
        if (isThinking) {
            binding.lvThinking.visible()
        } else if (isListening) {
            binding.lvListening.visible()
        } else if (isMute) {
            binding.ivMute.visible()
        }
        binding.ivBtnAction.setImageDrawable(
            ContextCompat.getDrawable(requireContext(), drawable)
        )
        binding.tvActionText.text = text
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        val finalText = StringBuilder()
        var lastPartial = ""
        var messageId = UUID.randomUUID()
        val isFirst = AtomicBoolean(true)
        var commitJob: Job? = null
        val COMMIT_DELAY = 2500L

        fun resetListener(){
            finalText.clear()
            lastPartial = ""
            messageId = UUID.randomUUID()
            isFirst.set(true)
            commitJob = null
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {

            override fun onPartialResults(bundle: Bundle?) {
                if (!isRecognizerActive) return

                val text = bundle
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.takeIf { it.isNotEmpty() }
                    ?: return

                binding.tvStartTalking.gone()
                if (text.startsWith(lastPartial)) {
                    val delta = text.substring(lastPartial.length)
                    finalText.append(delta)
                }

                lastPartial = text
                if (isFirst.getAndSet(false)) {
                    viewModel.addMessage(
                        VoiceChatMessage(
                            id = messageId,
                            message = "",
                            isUser = true,
                            isStreaming = true
                        )
                    )
                }
                viewModel.addReceivedMessage(finalText.toString(), messageId)

                commitJob?.cancel()
                commitJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(COMMIT_DELAY)
                    if (!isMuted && !isRecognizerActive) return@launch
                    isRecognizerCommiting.set(true)
                    releaseSpeechRecognizer()
                    viewModel.askQuestionStream(finalText.toString())
                    setActionState(ActionState.THINKING)
                    resetListener()
                }
            }

            override fun onResults(results: Bundle?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                if(isRecognizerCommiting.getAndSet(false)) return
                startListening()
                resetListener()
            }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun releaseSpeechRecognizer() {
        isRecognizerActive = false
        try {
            speechRecognizer?.apply {
                setRecognitionListener(null)
                stopListening()
                cancel()
                destroy()
            }
            speechRecognizer = null
        } catch (e: Exception) {
            LOGS.e(e.toString())
        }
    }
}

enum class ActionState { LISTENING, SPEAKING, THINKING, STARTING_UP, MUTE, ERROR }