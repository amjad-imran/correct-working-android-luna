package com.oreo.ui.chatGpt.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.RecognitionListener
import android.util.Base64
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.grapesnberries.curllogger.CurlLoggerInterceptor
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.AppLogger
import com.noisefit.data.remote.abstraction.AudioApiService
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import com.oreo.util.SpeechRecognizerManager
import com.oreo.util.audiorecorder.RecorderState
import com.oreo.util.audiorecorder.WaveRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject


@HiltViewModel
class AudioAiViewModel @Inject constructor(
    private val audioApiService: AudioApiService,
    private val oreoDeviceRepository: OreoDeviceRepository,
    private val resourcesProvider: ResourcesProvider
) : BaseViewModel() {

    private val AMPLITUDE_MAX = 5000
    private val SILENCE_DURATION: Long = 2000

    var isRecording = false

    private var audioTrack: AudioTrack? = null
    private var inputStream: InputStream? = null
    private var job: Job? = null
    private var peakCount = 0
    private var lastSoundTime: Long = System.currentTimeMillis()
    private var lastFile: File? = null

    var videoPlayState = MutableLiveData<Boolean>()

    var waveRecorder: WaveRecorder? = null
    val audioAiState = MutableLiveData<AudioAiState>()

    var apiKey: String? = null
    val onCredentialsReceived = MutableLiveData<Event<Boolean>>()

    val stringBuilder = StringBuilder()
    val textReceived = MutableLiveData<Event<Boolean>>()

    /**
     * { "model": "gpt-4o-audio-preview", "modalities": ["text", "audio"], "audio": { "voice": "alloy", "format": "pcm16" }, "messages": [ { "role": "user", "content": [ { "type": "text", "text": "Answer this recording" }, {"type": "input_audio", "input_audio": { "data": "$base64String", "format": "wav"}}]}], "stream": true}
     */
    private fun getAudioResponseChatGpt(base64String: String) {

        if (job?.isActive == true) {
            job?.cancel()
        }

        job = viewModelScope.launch(Dispatchers.IO) {

            if (audioTrack != null) {
                if (audioTrack!!.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    audioTrack?.stop()
                    //audioTrack?.release()
                }
            }
            inputStream?.close()


            val client = OkHttpClient.Builder()
                .apply {
                    if (BuildConfig.DEBUG) {
                        tryCatch {
                            this.addInterceptor(CurlLoggerInterceptor("CURL"))
                        }
                    }
                }
                .build()
            val mediaType = "application/json".toMediaType()

            val inputText = "Please respond based on the content of the audio."

            val body =
                ("{ \"model\": \"gpt-4o-audio-preview\", \"modalities\": [\"text\", \"audio\"], \"audio\":" +
                        " { \"voice\": \"alloy\", \"format\": \"pcm16\" }, \"messages\": " +
                        "[ { \"role\": \"user\", \"content\": [ { \"type\": \"text\", \"text\": \"$inputText\" }," +
                        " {\"type\": \"input_audio\", \"input_audio\": { \"data\": \"$base64String\", \"format\": \"wav\"}}]}], \"stream\": true}").toRequestBody(
                    mediaType
                )

            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader(
                    "Authorization",
                    "Bearer $apiKey"
                )
                .build()

            try {
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {

                    inputStream = response.body?.byteStream()

                    processStreamingResponse(
                        inputStream,
                        ChatCompletionResponse::class.java
                    )
                } else {
                    sendMessage(resourcesProvider.getString(R.string.text_something_went_wrong))
                }

            } catch (exp: Exception) {
                exp.printStackTrace()
                AppLogs.sendAppLogs("Audio API exception ${exp.message}")
                sendMessage(resourcesProvider.getString(R.string.text_something_went_wrong))
            }
        }
    }

    private fun initializeAudioTrack() {
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

    }

    private fun processStreamingResponse(inputStream: InputStream?, responseType: Class<*>?) {
        val gson = Gson()
        try {
            stringBuilder.clear()

            videoPlayState.postValue(true)

            audioAiState.postValue(AudioAiState.AI_TALKING)

            if (audioTrack == null) {
                initializeAudioTrack()
            }

            audioTrack?.play()

            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String
                while ((reader.readLine().also { line = it }) != null) {
                    try {
                        if (line.isNotEmpty()) {
                            val subString = line.substring(line.indexOf("data:") + 5).trim()
                            val response =
                                gson.fromJson<ChatCompletionResponse>(subString, responseType)

                            val audioData = response.choices?.get(0)?.delta?.audio?.data
                            val transcript = response.choices?.get(0)?.delta?.audio?.transcript

                            if (transcript.isNullOrEmpty().not()) {
                                stringBuilder.append(transcript)
                                textReceived.postValue(Event(true))
                            }
                            //LOGS.d("sdflkhsdjkfhsdkjf $audioData")

                            if (audioData != null) {
                                val decodedAudio = Base64.decode(audioData, Base64.DEFAULT)
                                audioTrack?.write(decodedAudio, 0, decodedAudio.size)
                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            //audioTrack?.release()
            videoPlayState.postValue(false)

        } catch (exp: Exception) {
            exp.printStackTrace()
            //audioTrack?.release()
            videoPlayState.postValue(false)
        }
    }

    fun sendRecordingToServer() {
        if (lastFile != null) {
            LOGS.d("VOICE_RECORDER peakCount - >$peakCount")
            if (peakCount < 5) {
                //TODO check not working
                if (lastFile?.exists() == true) {
                    lastFile?.delete()
                }
                LOGS.d("VOICE_RECORDER File deleted $lastFile")
            } else {
                lastFile?.let {
                    val base64Wav = convertRawWavToBase64(it)

                    base64Wav?.let { base64 ->
                        getAudioResponseChatGpt(base64.replace("\n", ""))
                    }
                }
            }
        }
    }


    fun startNewRecording() {
        isRecording = true
        sendRecordingToServer()

        peakCount = 0

        val fileName = "recording_${System.currentTimeMillis()}.wav"
        lastFile = File(NoiseFitApplicationMain.context!!.filesDir, fileName)

        waveRecorder = WaveRecorder(filePath = lastFile!!.absolutePath).apply {
            //silenceDetection = true
            //noiseSuppressorActive = true
            startRecording()
            onStateChangeListener = {
                LOGS.d("VOICE_RECORDER  ${it.name}")
                when (it) {
                    RecorderState.RECORDING -> {}
                    RecorderState.STOP -> {}
                    RecorderState.PAUSE -> {}
                    RecorderState.SKIPPING_SILENCE -> {}
                }
            }
        }

        waveRecorder?.onAmplitudeListener = {
            LOGS.d("VOICE_RECORDER", "Amplitude : $it")
            val isSilent = isSilent(it)
            val currentTime = System.currentTimeMillis()

            if (isSilent) {
                if (currentTime - lastSoundTime > SILENCE_DURATION) {
                    lastSoundTime = currentTime

                    waveRecorder?.stopRecording(peakCount < 5)
                    startNewRecording()
                }
            } else {
                lastSoundTime = currentTime
            }

            if (it > AMPLITUDE_MAX) {
                peakCount += 1
            }
        }
    }

    private fun convertRawWavToBase64(file: File): String? {
        var base64String: String? = null
        try {

            val fileInputStream = FileInputStream(file)
            val buffer = ByteArray(1024)
            val outputStream = ByteArrayOutputStream()

            var bytesRead: Int
            while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.close()

            val byteArray = outputStream.toByteArray()

            base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return base64String
    }


    private fun isSilent(it: Int): Boolean {
        return it < AMPLITUDE_MAX
    }


    override fun onCleared() {
        super.onCleared()
        if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack?.stop()
            audioTrack?.release()
        }
    }

    fun getCredentials() {
        viewModelScope.launch {
            oreoDeviceRepository.getCredentials().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getCredentials()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            apiKey = it.OPENAI_API_KEY
                            if (apiKey.isNullOrEmpty().not()) {
                                onCredentialsReceived.postValue(Event(true))
                            }
                        }
                    }
                }
            }
        }
    }

    fun cleanup() {
        waveRecorder?.stopRecording(true)
        if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack?.stop()
        }
        audioTrack?.release()
        viewModelScope.launch(Dispatchers.IO) {
            inputStream?.close()
        }
    }
}

enum class AudioAiState {
    DEFAULT, LISTENING, GENERATING, AI_TALKING
}