package com.oreo.ui.chatGpt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.model.AiHeaderInsight1
import com.noisefit.data.model.AiMeals
import com.noisefit.data.model.AiWorkout
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.Token
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.ChatGptOverview
import com.oreo.data.model.ai.ChatMessage
import com.oreo.data.model.ai.TopQuestions
import com.oreo.data.repository.abstraction.ErrorServerCases
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import com.oreo.data.repository.abstraction.OreoSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class ChatGptViewModel
@Inject constructor(
    val sessionManager: SessionManager,
    val ringDataStore: RingDataStore,
    val localDataStore: DataStoredInterface,
    val oreoDeviceRepository: OreoDeviceRepository,
    private val syncRepository: OreoSyncRepository,
    val resourceProvider: ResourcesProvider,
) : BaseViewModel() {

    var cameraUri: Uri? = null

    val IMAGE_MAX_BYTES = 5 * 1024 * 1024 // 5 MB
    val DEFAULT_IMAGE_QUALITY = 80 // JPEG quality (0-100)

    private var userImage: String? = null
    private var userName: String? = null

    val removeSnackBar = MutableLiveData<Event<Boolean>>()

    private val _chatGptOverview = MutableLiveData<ArrayList<ChatGptOverview>>()
    val chatGptOverview: LiveData<ArrayList<ChatGptOverview>>
        get() = _chatGptOverview

    private val _scrollToBottom = MutableLiveData<Event<Boolean>>()
    val scrollToBottom: LiveData<Event<Boolean>>
        get() = _scrollToBottom

    val threadTitle = MutableLiveData<String>()
    val questions = MutableLiveData<List<TopQuestions>>(arrayListOf())


    val aiGeneratedPlanSaved = MutableLiveData<Event<Boolean>>()
    val showSavePlan = MutableLiveData<Event<AiPlanType>>()
    val videoState = MutableLiveData<Boolean>()

    var threadId: String? = null
    var defaultMessage: String? = null
    var userMessage: String? = null
    var meal: AiMeals? = null
    var workout: AiWorkout? = null
    var headerInsight1: AiHeaderInsight1? = null
    var planType: PlanType? = null
    var srcKey: String? = null

    val fetchInProgress = MutableLiveData<Boolean>()
    val showRetry = MutableLiveData<Boolean>()
    private val sourcePattern = "【\\d+:\\d+†[^]]+】"


    var lastApi: Pair<Int, String>? = null
    private var initMessage: String


    @Volatile
    var pendingAttachment: AttachmentData? = null
    var mediaTypeName: String ?= null
    val attachmentPreview = MutableLiveData<AttachmentData?>(null)

    val showSuggestedQuestions = MutableLiveData<Boolean>()

    fun setPendingAttachment(uri: Uri, mimeType: String, fileName: String, sizeBytes: Long) {
        val data = AttachmentData(uri, mimeType, fileName, sizeBytes)
        pendingAttachment = data
        attachmentPreview.value = (data)
    }

    fun clearPendingAttachment() {
        pendingAttachment = null
        attachmentPreview.postValue(null)
    }

    init {
        val user = localDataStore.getUser()
        userImage = user?.imageUrl
        userName = user?.firstName

        initMessage =
            "Hello $userName, my name is Luna. I am an AI coach that can guide you with personalized nutritional advice, workout questions and to understand how to improve your health parameters tracked by the Luna ring. What do you need help with?"
    }

    fun getAiTopQuestions(aiTopic: AITopics) {
        viewModelScope.launch {
            oreoDeviceRepository.getAiTopQuestions(aiTopic).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
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
                                        getAiTopQuestions(aiTopic)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            questions.value = it.questions ?: arrayListOf()
                            //showHistoryIcon.value = it.hasHistory
                        }
                    }
                }
            }
        }


    }


    fun addInitData() {
        if (workout != null || meal != null) {
            val messages = _chatGptOverview.value ?: ArrayList()
            if (workout != null) {
                messages.add(ChatGptOverview.HeaderWorkout(workout!!))
            }
            if (meal != null) {
                messages.add(ChatGptOverview.HeaderMeal(meal!!))
            }
            _chatGptOverview.value = (messages)
        } else {
            if(userMessage.isNullOrEmpty().not()){
                showSuggestedQuestions.postValue(false)
                generateInitMessage()
            }else {
                showSuggestedQuestions.postValue(true)
            }
        }
    }

    fun addSentMessage(message: String) {
        val messages = _chatGptOverview.value ?: ArrayList()
        messages.add(ChatGptOverview.SentMessage(message, userImage))
        _chatGptOverview.value = (messages)
    }

    fun addSentMessageWithPendingAttachment(message: String) {
        val messages = _chatGptOverview.value ?: ArrayList()
        val att = pendingAttachment
        messages.add(
            ChatGptOverview.SentMessage(
                message = message,
                userImage = userImage,
                attachmentSource = att?.uri?.toString(),
                attachmentMimeType = att?.mimeType,
                attachmentName = att?.fileName
            )
        )
        _chatGptOverview.value = messages
    }

    fun addInsight1HeaderMsg(message: AiHeaderInsight1) {
        val messages = _chatGptOverview.value ?: ArrayList()
        messages.add(
            ChatGptOverview.HeaderInsight1(
                message
            )
        )
        _chatGptOverview.value = messages
        clearPendingAttachment()
    }

    fun addThinkingMessage() {
        showRetry.postValue(false)
        val messages = _chatGptOverview.value ?: ArrayList()
        messages.removeAll {
            it is ChatGptOverview.ThinkingMessage || it is ChatGptOverview.RetryMessage
        }
        messages.add(ChatGptOverview.ThinkingMessage())
        _chatGptOverview.value = (messages)
        _scrollToBottom.postValue(Event(true))
    }

    fun addReceivedMessage(message: String, uuid: UUID = UUID.randomUUID(),isGenerating: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            showRetry.postValue(false)
            cachedAttachment = null
            val messages = _chatGptOverview.value ?: ArrayList()
            messages.removeAll {
                it is ChatGptOverview.ThinkingMessage || it is ChatGptOverview.RetryMessage
            }
            if (messages.lastOrNull() is ChatGptOverview.ReceivedMessage) {
                messages.removeAt(messages.lastIndex)
            }
            messages.add(ChatGptOverview.ReceivedMessage(message,isGenerating).apply {
                id = uuid
            })
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
            showRetry.postValue(true)
            _chatGptOverview.postValue(messages)
        }
    }

    fun removeThinkingState() {
        viewModelScope.launch(Dispatchers.IO) {
            val messages = _chatGptOverview.value ?: ArrayList()
            showRetry.postValue(false)
            messages.removeAll {
                it is ChatGptOverview.ThinkingMessage || it is ChatGptOverview.RetryMessage
            }
            _chatGptOverview.postValue(messages)
        }
    }

    fun retryApi() {
        lastApi?.let {
            askQuestionStream(it.second)
            addThinkingMessage()
        }
    }

    fun generateThreadId(sendInsightHeaderMsg: (() -> Unit) ?= null) {
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

                                if(sendInsightHeaderMsg!=null) sendInsightHeaderMsg()
                                else addInitData()
                                //generateInitMessage()
                            }
                        }
                    }
                }
            }
        }
    }

    fun generateInitMessage() {
        if (workout == null && meal == null) {
            if (userMessage.isNullOrEmpty().not()) {
                sendUserInitMessage(userMessage ?: "")
            } else {
                sendInitMessage()
            }
        }
    }

    private val sseClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    tryCatch {
                        //this.addInterceptor(HttpLoggingInterceptor())
                        //this.addInterceptor(CurlLoggerInterceptor("CURL"))
                    }
                }
            }
            .build()
    }

    @Volatile
    private var currentCall: Call? = null
    private var cachedAttachment: AttachmentData? = null

    fun askQuestionStream(prompt: String) {
        pendingAttachment?.let{ cachedAttachment = it }
        clearPendingAttachment()

        if (ApplicationUtils.isInternetConnected().not()){
            addErrorState(
                resourceProvider.getString(R.string.connection_issue)
            )
            return
        }
        fetchInProgress.value = true
        videoState.value = true
        lastApi = Pair(1, prompt)

        val responseBuilder = StringBuilder()
        val uuid = UUID.randomUUID()

        viewModelScope.launch(Dispatchers.IO) {
            val userToken = localDataStore.getUserToken()

            val baseUrl = when (planType) {
                PlanType.WORKOUT -> "${BuildConfig.BASE_URL_NEW}/luna/ai/v1/workout/stream"
                PlanType.DIET -> "${BuildConfig.BASE_URL_NEW}/luna/ai/v1/diet/stream"
                PlanType.NONE, null -> "${BuildConfig.BASE_URL_NEW}/luna/ai/v1/stream"
            }
            val urlWithParams = when (planType) {
                PlanType.WORKOUT -> "$baseUrl?message=$prompt"
                PlanType.DIET -> "$baseUrl?message=$prompt"
                PlanType.NONE, null ->
                    "$baseUrl?message=$prompt&thread_id=$threadId"
            }

            val ctx = resourceProvider.context
            val att = cachedAttachment
            val attachmentBody: RequestBody? = att?.let { a ->
                try {
                    ctx.contentResolver.openInputStream(a.uri)?.use { input ->
                        val bytes = input.readBytes()
                        RequestBody.create(a.mimeType.toMediaTypeOrNull(), bytes)
                    }
                } catch (e: Exception) {
                    null
                }
            }

            //
            val multipartBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)

            var hasParts = false

            headerInsight1?.insightData?.let { insightData ->
                val insightJson = Gson().toJson(insightData)

                val insightBody = insightJson.toRequestBody("application/json; charset=utf-8".toMediaType())
                multipartBuilder.addFormDataPart("insight_data", null, insightBody)

                hasParts = true
                headerInsight1?.insightData = null
            }

            if (attachmentBody != null && att != null) {
                multipartBuilder.addFormDataPart("file", att.fileName, attachmentBody)
                hasParts = true
            }

            val requestBody: RequestBody =
                if (hasParts) multipartBuilder.build()
                else ByteArray(0).toRequestBody(null)
            //

            val requestBuilder = Request.Builder()
                .url(urlWithParams)
                .post(requestBody)
                .addHeader("Accept", "text/event-stream")
                .addHeader("Cache-Control", "no-cache")

            userToken?.let { addHeaders(requestBuilder, it) }

            val request = requestBuilder.build()

            try {
                val call = sseClient.newCall(request)
                currentCall = call

                call.execute().use { response ->   // ✅ always closes body/response
                    if (!response.isSuccessful) {
                        val err = response.body?.string() // safe inside use{}
                        throw java.io.IOException("Unexpected HTTP ${response.code}. Body=$err")
                    }

                    val source = response.body?.source()
                        ?: throw java.io.IOException("Empty response body")

                    val eventBuffer = StringBuilder()

                    while (fetchInProgress.value == true && !source.exhausted()) {
                        val line = try {
                            source.readUtf8Line()
                        } catch (e: Exception) {
                            null
                        } ?: break

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
                addReceivedMessage(responseBuilder.toString(), uuid, false)
                sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.lifeos_reply_sent)

                fetchInProgress.postValue(false)
                videoState.postValue(false)
                checkForPlans(responseBuilder.toString())
            } catch (t: Throwable) {
                if(currentCall?.isCanceled() == true) return@launch
                if (fetchInProgress.value == true) {
                    fetchInProgress.postValue(false)
                    videoState.postValue(false)
                    if (responseBuilder.isEmpty()) {
                        addErrorState(
                            resourceProvider.getString(R.string.text_couldn_t_generate_a_response)
                        )
                        GlobalScope.launch(Dispatchers.IO) {
                            syncRepository.logErrorServer(
                                ErrorServerCases.LUNA_AI_ERROR.name,
                                "Error log : ${t.message}"
                            ).collect()
                        }
                    }
                }
            } finally {
                currentCall = null
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

    private fun checkForPlans(message: String) {
        val mealIdentifier = "🍎🥗🍲"
        val workoutIdentifier = "🚴‍♀️🏋️‍♂️🧘"

        if (message.contains(mealIdentifier)) {
            showSavePlan.postValue(Event(AiPlanType.MEAL))
            return
        }

        if (message.contains(workoutIdentifier)) {
            showSavePlan.postValue(Event(AiPlanType.WORKOUT))
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
            addReceivedMessage(message ?: "", UUID.randomUUID(),false)
        }
        return
    }

    private fun sendUserInitMessage(userMessage: String) {
        viewModelScope.launch(Dispatchers.Main) {
            //addSentMessage(userMessage)
            addSentMessageWithPendingAttachment(userMessage)
            addThinkingMessage()
            askQuestionStream(userMessage)
            //generateThreadTitle(userMessage)
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
            "Hi",
            "Hey",
            "Hey there",
            "Hi there",
            "Namaste",
            "Hola",
            "Hi Luna",
            "Hey Luna",
            "hiluna",
            "how are you",
            "howdie",
            "who are you",
            "dear",
            "hi dear",
            "hi sir",
            "hi mam",
            "sir",
            "mam",
            "hello"
        )
        return quesList.any { it.equals(ques, true) }
    }


    private fun generateMessages(messages: List<ChatMessage>?) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val tempMessage = ArrayList<ChatGptOverview>()
            tempMessage.add(ChatGptOverview.ReceivedMessage(initMessage,false))

            messages?.forEach {
                if (it.sender.equals("assistant", true)) {
                    tempMessage.add(ChatGptOverview.ReceivedMessage(it.message ?: "",false))
                } else if (it.sender.equals("user", true)) {
                    val metaUrl =
                        it.metadata?.takeIf { url -> url.isNotBlank() } ?: it.attachmentUrl
                    val (attSrc, attMime, attName) = if (!metaUrl.isNullOrBlank()) {
                        val parsed = parseAttachmentFromUrl(metaUrl)
                        Triple(parsed.first, parsed.second, parsed.third)
                    } else {
                        Triple(it.attachmentUrl, it.mimeType, it.documentName)
                    }
                    tempMessage.add(
                        ChatGptOverview.SentMessage(
                            message = it.message ?: "",
                            userImage = userImage,
                            attachmentSource = attSrc,
                            attachmentMimeType = attMime,
                            attachmentName = attName
                        )
                    )
                }
            }

            setLoading(false)
            _chatGptOverview.postValue(tempMessage)
            _scrollToBottom.postValue(Event(true))
        }
    }

    private fun parseAttachmentFromUrl(url: String): Triple<String?, String?, String?> {
        val clean = url.substringBefore('#').substringBefore('?')
        val rawName = clean.substringAfterLast('/')
        val decodedName = decodeUrlFileName(rawName)
        val name = decodedName.ifBlank { null }
        val ext = (name ?: rawName).substringAfterLast('.', missingDelimiterValue = "").lowercase()
        val mime = when (ext) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else -> if (ext.isNotBlank()) "application/octet-stream" else null
        }
        return Triple(url, mime, name)
    }

    private fun decodeUrlFileName(encoded: String): String {
        return try {
            java.net.URLDecoder.decode(encoded, Charsets.UTF_8.name())
        } catch (_: Exception) {
            encoded
        }
    }

    fun stopResponseGeneration() {
        cachedAttachment = null
        viewModelScope.launch {
            removeThinkingState()
            fetchInProgress.postValue(false)
            videoState.postValue(false)
            //serverSentEvent?.close()
            currentCall?.cancel()

            oreoDeviceRepository.stopResponseGeneration(threadId, planType ?: PlanType.NONE)
                .collect { resource ->
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
                                currentCall?.cancel()
                            }
                        }
                    }*/
                }
        }
    }

    fun savePlanData() {
        if (showSavePlan.value?.peekContent() == null) {
            return
        }

        viewModelScope.launch {
            if (showSavePlan.value?.peekContent() == AiPlanType.WORKOUT) {
                oreoDeviceRepository.saveWorkoutPlan().collect { resource ->
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
                                            savePlanData()
                                        }

                                        override fun no() {

                                        }
                                    }
                            })
                        }

                        is Resource.Success -> {
                            resource.data?.data?.let {
                                aiGeneratedPlanSaved.postValue(Event(true))
                            }
                        }
                    }
                }
            } else if (showSavePlan.value?.peekContent() == AiPlanType.MEAL) {
                oreoDeviceRepository.saveMealPlan()
                    .collect { resource ->
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
                                                savePlanData()
                                            }

                                            override fun no() {

                                            }
                                        }
                                })
                            }

                            is Resource.Success -> {
                                resource.data?.data?.let {
                                    aiGeneratedPlanSaved.postValue(Event(true))
                                }
                            }
                        }
                    }
            }
        }
    }

    fun removeSnackBar() {
        removeSnackBar.postValue(Event(true))
    }


    fun compressImage(
        context: Context,
        sourceUri: Uri,
        quality: Int = DEFAULT_IMAGE_QUALITY
    ): Uri? {
        return try {
            val resolver = context.contentResolver

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(sourceUri)
                ?.use { BitmapFactory.decodeStream(it, null, bounds) }

            val maxDimension = 1920
            var inSample = 1
            val w = bounds.outWidth
            val h = bounds.outHeight
            if (w > 0 && h > 0) {
                while ((w / inSample) > maxDimension || (h / inSample) > maxDimension) {
                    inSample *= 2
                }
            }

            val opts = BitmapFactory.Options().apply { inSampleSize = inSample.coerceAtLeast(1) }
            val originalBitmap = resolver.openInputStream(sourceUri)
                ?.use { BitmapFactory.decodeStream(it, null, opts) }
                ?: return null

            val orientedBitmap = try {
                val orientation = resolver.openInputStream(sourceUri)?.use { input ->
                    ExifInterface(input).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                } ?: ExifInterface.ORIENTATION_NORMAL

                val matrix: Matrix? = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> Matrix().apply { postRotate(90f) }
                    ExifInterface.ORIENTATION_ROTATE_180 -> Matrix().apply { postRotate(180f) }
                    ExifInterface.ORIENTATION_ROTATE_270 -> Matrix().apply { postRotate(270f) }
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> Matrix().apply {
                        preScale(
                            -1f,
                            1f
                        )
                    }

                    ExifInterface.ORIENTATION_FLIP_VERTICAL -> Matrix().apply { preScale(1f, -1f) }
                    ExifInterface.ORIENTATION_TRANSPOSE -> Matrix().apply {
                        postRotate(90f); preScale(
                        -1f,
                        1f
                    )
                    }

                    ExifInterface.ORIENTATION_TRANSVERSE -> Matrix().apply {
                        postRotate(270f); preScale(
                        -1f,
                        1f
                    )
                    }

                    else -> null
                }

                if (matrix != null) {
                    Bitmap.createBitmap(
                        originalBitmap,
                        0,
                        0,
                        originalBitmap.width,
                        originalBitmap.height,
                        matrix,
                        true
                    )
                } else originalBitmap
            } catch (_: Exception) {
                originalBitmap
            }

            val outFile =
                File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outFile).use { fos ->
                orientedBitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(0, 100), fos)
            }
            if (orientedBitmap !== originalBitmap) {
                originalBitmap.recycle()
            }
            orientedBitmap.recycle()

            FileProvider.getUriForFile(
                context,
                "com.noisefit.luna.fileprovider",
                outFile
            )
        } catch (_: Exception) {
            null
        }
    }

    fun postChatReview(
        text: String,
        reviewFlag: Int,
        messageId: UUID,
        negFeedbackText: String ?= null,
        reasons: String ?= null,
    ) {
        if (threadId == null) {
            return
        }
        val date = try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.timeZone = TimeZone.getDefault()
            sdf.format(Date())
        } catch (_: Exception) {
            ""
        }


        viewModelScope.launch {

            val request = JsonObject().apply {
                this.addProperty("thread_id",threadId)
                this.addProperty("text",text)
                this.addProperty("date",date)
                this.addProperty("review","$reviewFlag")
                if(reviewFlag==0){
                    this.addProperty(
                        "reason",
                        "feedback:- $negFeedbackText, reasons:- ${reasons ?: "[]"}"
                    )
                }
            }


            oreoDeviceRepository.markAiMessageState(request).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
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
                                        postChatReview(text, reviewFlag, messageId)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                        }
                    }
                }
            }
        }


    }


    fun getMimeType(context: Context, uri: Uri): String? =
        context.contentResolver.getType(uri)

    fun getDisplayName(context: Context, uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        }
    }

    fun getFileSize(context: Context, uri: Uri): Long {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (sizeIndex != -1 && cursor.moveToFirst()) cursor.getLong(sizeIndex) else -1L
        } ?: -1L
    }


    data class AttachmentData(
        val uri: Uri,
        val mimeType: String,
        val fileName: String,
        val sizeBytes: Long
    )
}

enum class AiPlanType {
    WORKOUT, MEAL
}
