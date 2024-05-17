package com.oreo.ui.chatGpt

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.ChatGptOverview
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    var assistantId: String? = null
    var threadId: String? = null

    val fetchInProgress = MutableLiveData<Boolean>()


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
    }

    fun addReceivedMessage(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val messages = _chatGptOverview.value ?: ArrayList()
            messages.removeAll {
                it is ChatGptOverview.ThinkingMessage || it is ChatGptOverview.RetryMessage
            }
            messages.add(ChatGptOverview.ReceivedMessage(message))
            _chatGptOverview.postValue(messages)
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
            askQuestion(it.second)
        }
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
                                addReceivedMessage(it.reply)
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
            val initMessage ="Hello  $userName, my name is Luna. I am an AI that can help you understand your bio markers and improve your scores. What should I start giving you deeper insights on?"
            addReceivedMessage(initMessage)
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