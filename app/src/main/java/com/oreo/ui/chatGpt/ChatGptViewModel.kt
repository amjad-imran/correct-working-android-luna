package com.oreo.ui.chatGpt

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.here.oksse.OkSse
import com.here.oksse.ServerSentEvent
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.ChatGptOverview
import com.oreo.data.model.ai.ChatMessage
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    val threadTitle = MutableLiveData<String>()

    var threadId: String? = null
    var defaultMessage: String? = null
    var userMessage: String? = null

    val fetchInProgress = MutableLiveData<Boolean>()
    private val sourcePattern = "【\\d+:\\d+†[^]]+】"


    var lastApi: Pair<Int, String>? = null
    private var initMessage: String

    init {
        val user = localDataStore.getUser()
        userImage = user?.imageUrl
        userName = user?.firstName

        initMessage =
            "Hello $userName, my name is Luna. I am an AI coach that can guide you with personalized nutritional advice, workout questions and to understand how to improve your health parameters tracked by the Luna ring. What do you need help with?"
    }


    fun addSentMessage(message: String) {
        val messages = _chatGptOverview.value ?: ArrayList()
        messages.add(ChatGptOverview.SentMessage(message, userImage))
        _chatGptOverview.value = (messages)
        //_chatGptOverview.postValue(messages)

        generateThreadTitle(message)

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

    fun removeThinkingState() {
        viewModelScope.launch(Dispatchers.IO) {
            val messages = _chatGptOverview.value ?: ArrayList()
            messages.removeAll {
                it is ChatGptOverview.ThinkingMessage || it is ChatGptOverview.RetryMessage
            }
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
                            it.threadId?.let { id ->
                                threadId = id
                                if (userMessage.isNullOrEmpty().not()) {
                                    sendUserInitMessage(userMessage ?: "")
                                } else {
                                    sendInitMessage()
                                }
                            }
                        }
                    }
                }
            }
        }


    }


    var serverSentEvent: ServerSentEvent? = null
    val okSse = OkSse()

    fun askQuestionStream(prompt: String) {
        fetchInProgress.value = true
        lastApi = Pair(1, prompt)

        val responseBuilder = StringBuilder()

        viewModelScope.launch(Dispatchers.IO) {

            val userToken = localDataStore.getUserToken()

            val request: Request =
                Request.Builder()
                    .url("${BuildConfig.BASE_URL_NEW}/ai-bridge/stream?message=$prompt&thread_id=$threadId")
                    .apply {
                        userToken?.let {
                            this.addHeader("access-token", "Bearer ${userToken.access_token}")
                            this.addHeader("wearable-type", "ring")
                        }
                    }.build()

            serverSentEvent = okSse.newServerSentEvent(request, object : ServerSentEvent.Listener {
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
                    if (fetchInProgress.value == false) return false

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

    private fun sendInitMessage() {
        viewModelScope.launch(Dispatchers.IO) {
            val message = if (defaultMessage.isNullOrEmpty().not()) {
                defaultMessage
            } else {
                initMessage
            }
            addReceivedMessage(message ?: "", false)
        }
        return
    }

    private fun sendUserInitMessage(userMessage: String) {
        viewModelScope.launch(Dispatchers.Main) {
            addSentMessage(userMessage)
            addThinkingMessage()
            askQuestionStream(userMessage)
            generateThreadTitle(userMessage)
        }
        return
    }

    fun loadMessagesByThreadId(threadId: String) {
        viewModelScope.launch {
            oreoDeviceRepository.loadMessagesByThreadId(threadId).collect { resource ->
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
                                        loadMessagesByThreadId(threadId)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            generateMessages(it.chatHistory)
                        }
                    }
                }
            }
        }
    }

    private fun generateThreadTitle(ques: String) {

        if (threadTitle.value.isNullOrEmpty().not()) return

        if (ignoreQues(ques)) return
        if (threadId == null) return

        viewModelScope.launch {
            oreoDeviceRepository.generateThreadTitle(ques, threadId!!).collect { resource ->
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
                                        generateThreadTitle(ques)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            threadTitle.postValue(cleanServerResponse(it))
                        }
                    }
                }
            }
        }
    }

    private fun ignoreQues(ques: String): Boolean {
        val quesList = arrayListOf(
            "Hi", "Hey", "Hey there", "Hi there",
            "Namaste", "Hola", "Hi Luna", "Hey Luna", "hiluna", "how are you", "howdie", "who are you",
            "dear", "hi dear", "hi sir", "hi mam", "sir", "mam", "hello"
        )
        return quesList.any { it.equals(ques, true) }
    }


    private fun generateMessages(messages: List<ChatMessage>?) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val tempMessage = ArrayList<ChatGptOverview>()
            tempMessage.add(ChatGptOverview.ReceivedMessage(initMessage))

            messages?.forEach {
                if (it.sender.equals("assistant", true)) {
                    tempMessage.add(ChatGptOverview.ReceivedMessage(it.message ?: ""))
                } else if (it.sender.equals("user", true)) {
                    tempMessage.add(ChatGptOverview.SentMessage(it.message ?: "", userImage))
                }
            }

            setLoading(false)
            _chatGptOverview.postValue(tempMessage)
            _scrollToBottom.postValue(Event(true))
        }
    }

    fun stopResponseGeneration() {
        if (threadId == null) return

        viewModelScope.launch {
            removeThinkingState()
            fetchInProgress.postValue(false)
            serverSentEvent?.close()

            oreoDeviceRepository.stopResponseGeneration(threadId!!).collect { resource ->
                /*when (resource) {
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
                                        stopResponseGeneration()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.let {
                            removeThinkingState()
                            fetchInProgress.postValue(false)
                            serverSentEvent?.close()
                        }
                    }
                }*/
            }
        }
    }
}