package com.oreo.ui.chatGpt.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.RecognitionListener
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.abstraction.AudioApiService
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.LOGS
import com.oreo.util.SpeechRecognizerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okio.buffer
import okio.source
import java.io.InputStream
import javax.inject.Inject


@HiltViewModel
class AudioAiViewModel @Inject constructor(
    private val speechRecognizerManager: SpeechRecognizerManager,
    private val audioApiService: AudioApiService
) : BaseViewModel() {

    var isInitSuccess = false
    var audioTrack: AudioTrack? = null
    var inputStream: InputStream? = null
    private var job: Job? = null

    fun initSpeechRecognizer(listener: RecognitionListener) {
        speechRecognizerManager.initializeSpeechRecognizer(listener)
        startListening()
        isInitSuccess = true
    }

    fun startListening() {
        speechRecognizerManager.startListening()
    }

    fun stopSpeechRecognition() {
        speechRecognizerManager.stopListening()
    }

    fun speakText(text: String) {
        speechRecognizerManager.speakText(text)
    }


    fun getAudioResponse(text: String) {
        if(job?.isActive == true){
            job?.cancel()
        }

        job = viewModelScope.launch(Dispatchers.IO) {

            if (audioTrack != null) {
                if (audioTrack!!.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    audioTrack?.stop()
                    audioTrack?.release()
                }
            }
            inputStream?.close()

            try {

                val responseBody = audioApiService.getAudioStream("/ai-bridge/audio-chat", text)
                inputStream = responseBody.byteStream()

                val bufferSize = AudioTrack.getMinBufferSize(
                    24000,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(24000)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.play()
                val buffer = ByteArray(bufferSize * 2)
                var bytesRead: Int
                while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
                    if (bytesRead > 0) {
                        audioTrack?.write(buffer, 0, bytesRead)
                    } else {
                        Thread.sleep(100)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                audioTrack?.stop()
                audioTrack?.release()
                inputStream?.close()
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        if (audioTrack!!.playState == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack?.stop()
            audioTrack?.release()
        }
        speechRecognizerManager.destroy()
    }
}