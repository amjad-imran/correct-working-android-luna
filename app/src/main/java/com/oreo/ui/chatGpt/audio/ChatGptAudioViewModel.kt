package com.oreo.ui.chatGpt.audio

import android.speech.RecognitionListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.util.SpeechRecognizerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatGptAudioViewModel @Inject constructor(
    private val speechRecognizerManager: SpeechRecognizerManager
) : BaseViewModel() {

    private val _speechResult = MutableLiveData<String>()
    val speechResult: LiveData<String> = _speechResult

    fun startSpeechRecognition(listener: RecognitionListener) {
        speechRecognizerManager.initializeSpeechRecognizer(listener)
        speechRecognizerManager.startListening()
    }

    fun stopSpeechRecognition() {
        speechRecognizerManager.stopListening()
    }

    fun speakText(text: String) {
        speechRecognizerManager.speakText(text)
    }

    fun setSpeechResult(result: String) {
        _speechResult.value = result
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizerManager.destroy()
    }
}
