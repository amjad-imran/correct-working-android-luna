package com.oreo.ui.chatGpt.audio

import VoiceChatMessage
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.BuildConfig
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.Token
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSource
import okio.IOException
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class LifeOSVoiceChatViewModel @Inject constructor(
    val resourceProvider: ResourcesProvider,
    val localDataStore: DataStoredInterface,
    val oreoDeviceRepository: OreoDeviceRepository,
    val ringDataStore: RingDataStore,
    ) : BaseViewModel() {
    val fetchInProgress = MutableLiveData<Boolean>()
    private val _chatMessages = MutableLiveData<MutableList<VoiceChatMessage>>(mutableListOf())
    val chatMessages: LiveData<MutableList<VoiceChatMessage>> = _chatMessages
    val audioStream: MutableLiveData<String?> = MutableLiveData()
    val streamError: MutableLiveData<String> = MutableLiveData()
    private val sourcePattern = "【\\d+:\\d+†[^]]+】"
    private var threadId: String? = null
    private var currentChatJob: Job? = null
    private var currentSseCall: Call? = null
    var lastPrompt = ""

    private val sseClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()
    }

    fun addMessage(message: VoiceChatMessage) {
        val list = _chatMessages.value ?: mutableListOf()
        list.add(message)
        _chatMessages.postValue(list)
    }

    private fun updateMessage(id: UUID, text: String) {
        val list = _chatMessages.value ?: return
        val index = list.indexOfFirst { it.id == id }
        if (index != -1) {
            list[index].message = text
            _chatMessages.postValue(list)
        }
    }
    fun askQuestionStream(prompt: String) {
        lastPrompt = prompt
        if (!ApplicationUtils.isInternetConnected()) {
            streamError.postValue("Connection lost. \n" +
                    "Check your internet and try again.")
            return
        }

        setLoading(true)
        fetchInProgress.value = true

        val messageId = UUID.randomUUID()
        val responseBuilder = StringBuilder()

        currentChatJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = buildSseRequest(prompt)
                val sseClient = sseClient.newCall(request)
                currentSseCall = sseClient
                val response = sseClient.execute()

                if (!response.isSuccessful) {
                    streamError.postValue("Server error, please try again.")
                }

                response.body.source().let { source ->
                    val isFirst = AtomicBoolean(true)
                    lastPrompt = ""
                    parseSseStream(
                        source = source,
                        onText = { text ->
                            if(isFirst.getAndSet(false)){
                                addMessage(
                                    VoiceChatMessage(
                                        id = messageId,
                                        message = "",
                                        isUser = false,
                                        isStreaming = true
                                    )
                                )
                            }
                            responseBuilder.append(text)
                            addReceivedMessage(
                                responseBuilder.toString(),
                                messageId
                            )
                        },
                        onAudio = { audioBytes ->
                            audioStream.postValue(audioBytes)
                        }
                    )
                }

                addReceivedMessage(
                    responseBuilder.toString(),
                    messageId
                )

            } catch (e: Exception) {
                if(currentSseCall?.isCanceled()?.not() == true)
                    streamError.postValue("Server error, please try again.")
            } finally {
                fetchInProgress.postValue(false)
                setLoading(false)
            }
        }
    }

    private fun buildSseRequest(prompt: String): Request {
        val userToken = localDataStore.getUserToken()
        val baseUrl = "${BuildConfig.BASE_URL_NEW}/luna/ai/v1/stream"
        val persona = ringDataStore.getUserSelectedPersona()
        val url = "$baseUrl?message=$prompt&thread_id=$threadId&response_type=audio&persona=$persona"

        val requestBuilder = Request.Builder()
            .url(url)
            .post(ByteArray(0).toRequestBody())
            .addHeader("Accept", "text/event-stream")
            .addHeader("Cache-Control", "no-cache")

        userToken?.let { addHeaders(requestBuilder, it) }

        return requestBuilder.build()
    }

    fun disposeChatStream() {
        audioStream.postValue(null)
        currentChatJob?.cancel()
        currentSseCall?.cancel()
    }

    fun generateThreadId() {
        viewModelScope.launch {
            oreoDeviceRepository.generateThreadId().collect { resource ->
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
                                        generateThreadId()
                                    }
                                    override fun no() {
                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            it.threadId?.let { id -> threadId = id }
                        }
                    }
                }
            }
        }
    }
    fun addReceivedMessage(message: String, uuid: UUID = UUID.randomUUID()) {
        setLoading(false)
        viewModelScope.launch(Dispatchers.Main) {
            updateMessage(uuid, message)
        }
    }
    private fun cleanServerResponse(msg: String): String {
        return msg.removeSuffix("\"").removePrefix("\"").replace("\\n", "\n")
            .replace(Regex(sourcePattern), "")
    }

    private fun parseSseStream(
        source: BufferedSource,
        onText: (String) -> Unit,
        onAudio: (String) -> Unit
    ) {
        val eventBuffer = StringBuilder()
        var currentEvent: String? = null

        while (fetchInProgress.value == true) {
            val line = source.readUtf8Line() ?: break

            when {
                line.startsWith("event:") -> {
                    currentEvent = line.removePrefix("event:").trim()
                }

                line.startsWith("data:") -> {
                    val data = line.removePrefix("data:").trim()

                    if (currentEvent == "audio") {
                        onAudio(data)
                    } else {
                        eventBuffer.append(data)
                    }
                }

                line.isBlank() -> {
                    if (eventBuffer.isNotEmpty()) {
                        val cleaned = cleanServerResponse(eventBuffer.toString())
                        onText(cleaned)
                        eventBuffer.setLength(0)
                    }
                    currentEvent = null
                }
            }
        }
    }

    private fun addHeaders(builder: Request.Builder, token: Token) {
        builder.addHeader("access-token", "Bearer ${token.access_token}")
        builder.addHeader("wearable-type", "ring")
        val timeZone = localDataStore.getLastKnownTimezone() ?: TimeZone.getDefault().id
        builder.addHeader("timezone", timeZone)
        val offset = localDataStore.getLastKnownOffset() ?: TimeUnit.MILLISECONDS.toMinutes(
            Calendar.getInstance().get(Calendar.ZONE_OFFSET).toLong()
        ).toString()
        builder.addHeader("offset", offset)
    }
}
