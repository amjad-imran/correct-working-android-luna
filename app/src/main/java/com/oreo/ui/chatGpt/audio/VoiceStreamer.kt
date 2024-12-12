package com.oreo.ui.chatGpt.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

//https://medium.com/@srinathsingh007/android-speech-to-text-using-google-cloud-api-58a89fa1bfaa
class VoiceStreamer{

    private var voiceRecorder: AudioRecord? = null
    private var voiceStreamListener: VoiceStreamListener? = null
    private var streamExecutorService: ExecutorService = Executors.newFixedThreadPool(1)
    private var isStreaming: Boolean = false

    fun registerOnVoiceListener(voiceStreamListener: VoiceStreamListener) {
        this.voiceStreamListener = voiceStreamListener
    }

    private val runnableAudioStream = Thread {
        try {
            val buffer = ByteArray(minBufferSize)
            if (voiceRecorder == null) {
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    minBufferSize * 10
                ).also {
                    voiceRecorder = it
                }
            }
            voiceRecorder?.apply {
                startRecording()
                while (isStreaming) {
                    minBufferSize = read(buffer, 0, buffer.size)
                    voiceStreamListener?.onVoiceDataAvailable(buffer)
                    //Log.i("MinBufferSize : ", "${buffer.size}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    val sampleRate: Int
        get() = if (voiceRecorder != null) {
            voiceRecorder!!.sampleRate
        } else 0


    fun stopVoiceStreaming() {
        isStreaming = false
        voiceRecorder?.release()
        voiceRecorder = null
        if (runnableAudioStream != null && runnableAudioStream.isAlive)
            streamExecutorService.shutdown()
    }

    fun startVoiceStreaming() {
        isStreaming = true
        streamExecutorService.submit(runnableAudioStream)
    }

    companion object {
        const val sampleRate = 44000
        private const val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO
        private const val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
        private var minBufferSize: Int = 2200
    }
}

interface VoiceStreamListener {
    fun onVoiceDataAvailable(buffer: ByteArray)
}