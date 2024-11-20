package com.oreo.ui.chatGpt.audio

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.databinding.FragmentChatGptAudioBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "ChatGptAudioFragment"

@AndroidEntryPoint
class ChatGptAudioFragment :
    BaseFragment<FragmentChatGptAudioBinding>(FragmentChatGptAudioBinding::inflate) {

    val viewModel: ChatGptAudioViewModel by viewModels()
    private var isActive = true
    private var listOfCommands = ArrayList<String>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            uiController.onDisplayError("Ready for speech")

        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            uiController.onDisplayError("Speech ended")

        }

        override fun onError(error: Int) {
            uiController.onDisplayError("Error: $error")

        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            LOGS.d("$TAG onResults ${matches?.getOrNull(0)}")

        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial =
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            LOGS.d("$TAG onPartialResults ${partial?.getOrNull(0)}")
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun repeatFun(): Job {
        return scope.launch {
            while (isActive) {
                //do your network request here
                val command = listOfCommands.removeFirst()
                viewModel.speakText(command)
                if (listOfCommands.isEmpty()) {
                    isActive = false
                    repeatFun().cancel()
                }
                delay(4000)
            }
        }
    }

    override fun initListener() {

        binding.btnStartListen.setOnClickListener {
            viewModel.startSpeechRecognition(listener)
        }

        binding.btnStopListen.setOnClickListener {
            viewModel.stopSpeechRecognition()
        }


        binding.btnReadStream.setOnClickListener {
            isActive = true
            listOfCommands.add("Hello")
            listOfCommands.add("I'm your chat assistant")
            listOfCommands.add("How")
            listOfCommands.add("can")
            listOfCommands.add("I")
            listOfCommands.add("help")
            listOfCommands.add("you")
            listOfCommands.add("?")
            repeatFun().start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun subscribeObservers() {

    }

}