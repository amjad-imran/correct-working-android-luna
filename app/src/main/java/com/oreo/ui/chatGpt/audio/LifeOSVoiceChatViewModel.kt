package com.oreo.ui.chatGpt.audio

import VoiceChatMessage
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.BuildConfig
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.Token
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.LOGS
import com.oreo.ui.chatGpt.ChatGptViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class LifeOSVoiceChatViewModel @Inject constructor(
    val resourceProvider: ResourcesProvider,
    val localDataStore: DataStoredInterface
    ) : BaseViewModel() {

    val fetchInProgress = MutableLiveData<Boolean>()
    private val _chatMessages = MutableLiveData<MutableList<VoiceChatMessage>>(mutableListOf())
    val chatMessages: LiveData<MutableList<VoiceChatMessage>> = _chatMessages
    private val sourcePattern = "【\\d+:\\d+†[^]]+】"

    private val sseClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()
    }
    fun addUserMessage(text: String) {
        addMessage(
            VoiceChatMessage(
                id = UUID.randomUUID(),
                message = text,
                isUser = true
            )
        )
        askQuestionStream(text)
    }

    private fun addMessage(message: VoiceChatMessage) {
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
//        val attachment = pendingAttachment
//        clearPendingAttachment()

        if (ApplicationUtils.isInternetConnected().not()){
//            addErrorState(
//                resourceProvider.getString(R.string.connection_issue)
//            )
            return
        }
        fetchInProgress.value = true
//        videoState.value = true
//        lastApi = Pair(1, prompt)

        val responseBuilder = StringBuilder()
        val uuid = UUID.randomUUID()

        addMessage(
            VoiceChatMessage(
                id = uuid,
                message = "",
                isUser = false,
                isStreaming = true
            )
        )

        viewModelScope.launch(Dispatchers.IO) {
            val userToken = localDataStore.getUserToken()

            val baseUrl = "${BuildConfig.BASE_URL_NEW}/luna/ai/v1/stream"

            val urlWithParams = "$baseUrl?message=$prompt&thread_id=$123456asdf"


            val ctx = resourceProvider.context
            val att:  ChatGptViewModel.AttachmentData? = null
            val attachmentBody: RequestBody? = null

            val requestBody: RequestBody = if (attachmentBody != null && att != null) {
                MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", att.fileName, attachmentBody)
                    .build()
            } else {
                ByteArray(0).toRequestBody(null, 0, 0)
            }

            val requestBuilder = Request.Builder()
                .url(urlWithParams)
                .post(requestBody)
                .addHeader("Accept", "text/event-stream")
                .addHeader("Cache-Control", "no-cache")

            userToken?.let { addHeaders(requestBuilder, it) }

            val request = requestBuilder.build()

            try {
                val call = sseClient.newCall(request)
//                currentCall = call
                val response = call.execute()
                if (!response.isSuccessful) throw java.io.IOException("Unexpected code ${response.code}")

                response.body?.source()?.let { source ->
                    val eventBuffer = StringBuilder()
                    while (fetchInProgress.value == true) {
                        val line = try {
                            source.readUtf8Line()
                        } catch (e: Exception) {
                            null
                        }
                        if (line == null) break
                        if (line.startsWith("data:")) {
                            eventBuffer.append(line.removePrefix("data:").trimStart())
                            LOGS.d("SSE response $line")
                        } else if (line.isBlank()) {
                            if (eventBuffer.isNotEmpty()) {
                                val cleaned = cleanServerResponse(eventBuffer.toString())
                                responseBuilder.append(cleaned)
                                addReceivedMessage(responseBuilder.toString(), uuid,true)
                                eventBuffer.setLength(0)
                            }
                        }
                    }
                }
                addReceivedMessage(responseBuilder.toString(), uuid,false)

                fetchInProgress.postValue(false)
//                videoState.postValue(false)
//                checkForPlans(responseBuilder.toString())
            } catch (t: Throwable) {
                if (fetchInProgress.value == true) {
                    fetchInProgress.postValue(false)
//                    videoState.postValue(false)
                    if (responseBuilder.isEmpty()) {
//                        addErrorState(
//                            resourceProvider.getString(R.string.text_couldn_t_generate_a_response)
//                        )
//                        GlobalScope.launch(Dispatchers.IO) {
//                            syncRepository.logErrorServer(
//                                ErrorServerCases.LUNA_AI_ERROR.name,
//                                "Error log : ${t.message}"
//                            ).collect()
//                        }
                    }
                }
            } finally {
//                currentCall = null
            }
        }
    }

    fun addReceivedMessage(message: String, uuid: UUID = UUID.randomUUID(),isGenerating: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
//            showRetry.postValue(false)
            updateMessage(uuid, message)
        }
    }

    private fun cleanServerResponse(msg: String): String {
        return msg.removeSuffix("\"").removePrefix("\"").replace("\\n", "\n")
            .replace(Regex(sourcePattern), "")
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
