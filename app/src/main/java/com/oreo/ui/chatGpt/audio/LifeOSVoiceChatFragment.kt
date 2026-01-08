package com.oreo.ui.chatGpt.audio

import ChatAdapter
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.databinding.FragmentLifeOsVoiceChatBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LifeOSVoiceChatFragment :
    BaseFragment<FragmentLifeOsVoiceChatBinding>(FragmentLifeOsVoiceChatBinding::inflate) {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var chatAdapter: ChatAdapter

    private val viewModel: LifeOSVoiceChatViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        setupSpeechRecognizer()
    }

    override fun initListener() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
        }
        binding.ivBtnAction.setOnClickListener {
            speechRecognizer.startListening(intent)
        }
    }

    override fun subscribeObservers() {
        viewModel.chatMessages.observe(viewLifecycleOwner) {
            chatAdapter.submitMessages(it)
            binding.chatRecycler.scrollToPosition(it.size - 1)
        }
    }

    private fun setupRecycler() {
        chatAdapter = ChatAdapter(mutableListOf())
        binding.chatRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
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
            override fun onError(p0: Int) {}
            override fun onPartialResults(p0: Bundle?) {}
            override fun onEvent(p0: Int, p1: Bundle?) {}
        })
    }
}
