package com.oreo.ui.chatGpt.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AudioCalibrationViewModel @Inject constructor() : BaseViewModel() {
    var completionState = MutableLiveData<Int?>()
    var isRecording = false

    var userAttemptsCount = 0
    private var speechRecognizer: SpeechRecognizer? = null
    val speechText = MutableLiveData<Event<String>?>()
    private var isListeningSessionActive = false

    fun initSpeechRecognizer(context: Context) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onResults(results: Bundle) {
                if (!isListeningSessionActive) return
                isListeningSessionActive = false
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { speechText.postValue(Event(it)) }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Ignore partial results for short phrases
            }

            override fun onError(error: Int) {
                if (!isListeningSessionActive) return
                isListeningSessionActive = false
                speechText.postValue(null)
            }
        })
    }

    fun startListening(locale: Locale = Locale.getDefault()) {
        isRecording = true
        isListeningSessionActive = true
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
        }

        // optional small delay to improve recognition
        Handler(Looper.getMainLooper()).postDelayed({
            speechRecognizer?.startListening(intent)
        }, 300)
    }

    fun stopListening() {
        isRecording = false
        speechRecognizer?.stopListening()
    }

    private fun similarity(a: String, b: String): Float {
        val longer = if (a.length > b.length) a else b
        val shorter = if (a.length > b.length) b else a

        if (longer.isEmpty()) return 1f

        val editDistance = levenshteinDistance(longer, shorter)
        return (longer.length - editDistance).toFloat() / longer.length
    }
    private fun levenshteinDistance(lhs: String, rhs: String): Int {
        val dp = Array(lhs.length + 1) { IntArray(rhs.length + 1) }

        for (i in 0..lhs.length) dp[i][0] = i
        for (j in 0..rhs.length) dp[0][j] = j

        for (i in 1..lhs.length) {
            for (j in 1..rhs.length) {
                val cost = if (lhs[i - 1] == rhs[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[lhs.length][rhs.length]
    }

    fun isHeyLunaSpoken(input: String): Boolean {
        val target = "hey luna"

        val normalizedInput = input.lowercase().replace(Regex("[^a-z\\s]"), "").trim()
        val normalizedTarget = target.lowercase()

        if (normalizedInput.contains("hey") && normalizedInput.contains("luna")) return true

        val words = normalizedInput.split(" ")
        val targetLength = normalizedTarget.split(" ").size

        for (i in 0..words.size - targetLength) {
            val window = words.subList(i, i + targetLength).joinToString(" ")
            val similarityScore = similarity(window, normalizedTarget)
            if (similarityScore >= 0.6f) return true
        }

        val fullSimilarity = similarity(normalizedInput, normalizedTarget)
        if (fullSimilarity >= 0.6f) return true

        return false
    }


}