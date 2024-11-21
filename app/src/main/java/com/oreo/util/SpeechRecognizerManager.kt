package com.oreo.util

import android.content.Context
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import com.noisefit_commans.utils.LOGS
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeechRecognizerManager
@Inject
constructor(
    private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTTSInitialized = false

    init {
        initializeTextToSpeech()
    }

    // Initialize SpeechRecognizer
    fun initializeSpeechRecognizer(listener: RecognitionListener) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(listener)
        }
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    // Initialize TextToSpeech
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            isTTSInitialized = status == TextToSpeech.SUCCESS
            if (isTTSInitialized) {
                textToSpeech?.language = Locale.getDefault()
            }
        }
    }

    fun speakText(text: String) {
        if (isTTSInitialized) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_ADD, null, "TTS_ID")
        } else {
            LOGS.e("SpeechRecognizerManager", "TextToSpeech not initialized")
        }
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}