package com.oreo.ui.chatGpt

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.here.oksse.OkSse
import com.here.oksse.ServerSentEvent
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.ChatGptOverview
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject


@HiltViewModel
class ChatGptViewModel
@Inject constructor(
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface,
    val oreoDeviceRepository: OreoDeviceRepository,
    val resourceProvider: ResourcesProvider
) : BaseViewModel() {

    private var userImage: String? = null
    private var userName: String? = null
    private val _chatGptOverview = MutableLiveData<ArrayList<ChatGptOverview>>()
    val chatGptOverview: LiveData<ArrayList<ChatGptOverview>>
        get() = _chatGptOverview

    private val _scrollToBottom = MutableLiveData<Event<Boolean>>()
    val scrollToBottom: LiveData<Event<Boolean>>
        get() = _scrollToBottom



    var assistantId: String? = null
    var threadId: String? = null

    val fetchInProgress = MutableLiveData<Boolean>()
    private val sourcePattern = "【\\d+:\\d+†[^]]+】"


    var lastApi: Pair<Int, String>? = null

    init {
        val user = localDataStore.getUser()
        userImage = user?.imageUrl
        userName = user?.firstName
    }


    fun addSentMessage(message: String) {
        val messages = _chatGptOverview.value ?: ArrayList()
        messages.add(ChatGptOverview.SentMessage(message, userImage))
        _chatGptOverview.value = (messages)
        //_chatGptOverview.postValue(messages)
    }

    fun addThinkingMessage() {
        val messages = _chatGptOverview.value ?: ArrayList()
        messages.removeAll {
            it is ChatGptOverview.ThinkingMessage || it is ChatGptOverview.RetryMessage
        }
        messages.add(ChatGptOverview.ThinkingMessage())
        _chatGptOverview.value = (messages)
        _scrollToBottom.postValue(Event(true))
    }

    //TODO optimize
    fun addReceivedMessage(message: String, streaming: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            val messages = _chatGptOverview.value ?: ArrayList()
            messages.removeAll {
                it is ChatGptOverview.ThinkingMessage || it is ChatGptOverview.RetryMessage
            }
            if (messages.lastOrNull() is ChatGptOverview.ReceivedMessage) {
                messages.removeLast()
            }
            messages.add(ChatGptOverview.ReceivedMessage(message))
            _chatGptOverview.value = (messages)
        }
    }

    fun addErrorState(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val messages = _chatGptOverview.value ?: ArrayList()
            messages.removeAll {
                it is ChatGptOverview.ThinkingMessage || it is ChatGptOverview.RetryMessage
            }
            messages.add(ChatGptOverview.RetryMessage(message))
            _chatGptOverview.postValue(messages)
        }
    }

    fun retryApi() {
        addThinkingMessage()
        lastApi?.let {
            askQuestionStream(it.second)
            //askQuestion(it.second)
        }
    }


    fun askQuestionStream(prompt: String) {
        fetchInProgress.value = true
        lastApi = Pair(1, prompt)

        val responseBuilder = StringBuilder()

        viewModelScope.launch(Dispatchers.IO) {

            val userToken = localDataStore.getUserToken()


            val request: Request =
                Request.Builder()
                    .url("${BuildConfig.BASE_URL_NEW}/ai-bridge/stream?message=$prompt").apply {
                        userToken?.let {
                            this.addHeader("access-token", "Bearer ${userToken.access_token}")
                            this.addHeader("wearable-type", "ring")
                        }
                    }.build()

            //LOGS.d("streammmmmm request -> ${Gson().toJson(request)}")
            val okSse = OkSse()
            val sse = okSse.newServerSentEvent(request, object : ServerSentEvent.Listener {
                override fun onOpen(sse: ServerSentEvent?, response: Response?) {
                    // When the channel is opened
                    //LOGS.d("streammmmmm onOpen()")
                }

                override fun onMessage(
                    sse: ServerSentEvent?,
                    id: String?,
                    event: String?,
                    message: String?
                ) {
                    // When a message is received
                    //LOGS.d("streammmmmm onMessage() $message")
                    var msg = message

                    if (msg != null) {
                        msg = cleanServerResponse(msg)
                        responseBuilder.append(msg)
                    }

                    addReceivedMessage(
                        responseBuilder.toString(), true
                    )
                }

                override fun onComment(sse: ServerSentEvent?, comment: String?) {
                    // When a comment is received
                    //LOGS.d("streammmmmm onComment() $comment")

                }

                override fun onRetryTime(sse: ServerSentEvent?, milliseconds: Long): Boolean {
                    //LOGS.d("streammmmmm onRetryTime() $sse")

                    return false; // True to use the new retry time received by SSE
                }

                override fun onRetryError(
                    sse: ServerSentEvent?,
                    throwable: Throwable?,
                    response: Response?
                ): Boolean {
                    //LOGS.d("streammmmmm onRetryError() $response")
                    fetchInProgress.postValue(false)
                    if (responseBuilder.toString().isEmpty()) {
                        addErrorState(
                            String.format(
                                resourceProvider.getString(R.string.text_ai_error_message),
                                userName ?: ""
                            )
                        )
                    }
                    return false; // True to retry, false otherwise
                }

                override fun onClosed(sse: ServerSentEvent?) {
                    //LOGS.d("streammmmmm onClosed()")
                    fetchInProgress.postValue(false)
                    sse?.close()
                }

                override fun onPreRetry(sse: ServerSentEvent?, originalRequest: Request): Request {
                    //LOGS.d("streammmmmm onPreRetry()")
                    return originalRequest
                }

            })


        }


    }

    private fun cleanServerResponse(msg: String): String {
        return msg.removeSuffix("\"").removePrefix("\"").replace("\\n", "\n")
            .replace(Regex(sourcePattern), "")
    }

    fun askQuestion(prompt: String) {
        fetchInProgress.value = true
        lastApi = Pair(1, prompt)

        val jsonObject = JsonObject()
        jsonObject.addProperty("message", prompt)
        if (assistantId != null && threadId != null) {
            jsonObject.addProperty("assistant_id", assistantId)
            jsonObject.addProperty("thread_id", threadId)
        }

        viewModelScope.launch {
            oreoDeviceRepository.askQuestionToChatGpt(jsonObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        fetchInProgress.value = false
                        addErrorState(
                            String.format(
                                resourceProvider.getString(R.string.text_ai_error_message),
                                userName ?: ""
                            )
                        )
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        fetchInProgress.value = false
                        addErrorState(
                            String.format(
                                resourceProvider.getString(R.string.text_ai_error_message),
                                userName ?: ""
                            )
                        )
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            if (it.assistant_id != null && it.thread_id != null && it.run_id != null) {
                                assistantId = it.assistant_id
                                threadId = it.thread_id
                                callAfterSomeTime(it.assistant_id, it.thread_id, it.run_id)
                            } else {
                                fetchInProgress.value = false
                                addErrorState(
                                    String.format(
                                        resourceProvider.getString(R.string.text_ai_error_message),
                                        userName ?: ""
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    private fun callAfterSomeTime(assistantId: String, threadId: String, runId: String) {
        Handler().postDelayed({
            pollAnswer(assistantId, threadId, runId)
        }, 2000)
    }

    fun pollAnswer(assistantId: String, threadId: String, runId: String) {

        val jsonObject = JsonObject()
        jsonObject.addProperty("assistant_id", assistantId)
        jsonObject.addProperty("thread_id", threadId)
        jsonObject.addProperty("run_id", runId)
        jsonObject.addProperty("cancel_run", false)

        viewModelScope.launch {
            oreoDeviceRepository.pollForAnswer(jsonObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        fetchInProgress.value = false
                        addErrorState(
                            String.format(
                                resourceProvider.getString(R.string.text_ai_error_message),
                                userName ?: ""
                            )
                        )
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        fetchInProgress.value = false
                        addErrorState(
                            String.format(
                                resourceProvider.getString(R.string.text_ai_error_message),
                                userName ?: ""
                            )
                        )
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            if (it.status.equals("completed", true) && it.reply != null) {
                                fetchInProgress.value = false
                                addReceivedMessage(it.reply, true)
                            } else {
                                callAfterSomeTime(assistantId, threadId, runId)
                            }
                        }
                    }
                }
            }
        }
    }

    fun sendInitMessage() {

        viewModelScope.launch(Dispatchers.IO) {
            val userName = localDataStore.getUser()?.firstName
            val initMessage =
                "Hello $userName, my name is Luna. I am an AI coach that can guide you with personalized nutritional advice, workout questions and to understand how to improve your health parameters tracked by the Luna ring. What do you need help with?"

            addReceivedMessage(initMessage, false)
        }

        return
        /* fetchInProgress.value = true
         addReceivedMessage("", true)

         val jsonObject = JsonObject()
         jsonObject.addProperty("message", "Hi")

         viewModelScope.launch {
             oreoDeviceRepository.askQuestionToChatGpt(jsonObject).collect { resource ->
                 when (resource) {
                     is Resource.GenericError -> {
                         sendMessage(resource.message)
                         fetchInProgress.value = false
                     }

                     is Resource.Loading -> {
                         //setLoading(resource.loading)
                     }

                     is Resource.NetworkError -> {
                         setApiErrors(resource.response.apply {
                             this.uiComponentType as UIComponentType.RetryApiDialog
                             (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                 object : BinaryActionCallback {
                                     override fun yes() {
                                         sendInitMessage()
                                     }

                                     override fun no() {

                                     }
                                 }
                         })

                     }

                     is Resource.Success -> {
                         resource.data?.data?.let {
                             if (it.assistant_id != null && it.thread_id != null && it.run_id != null) {
                                 assistantId = it.assistant_id
                                 threadId = it.thread_id
                                 callAfterSomeTime(it.assistant_id, it.thread_id, it.run_id)
                             } else {
                                 fetchInProgress.value = false
                                 sendMessage("Something went wrong")
                             }
                         }
                     }
                 }
             }
         }*/
    }
}