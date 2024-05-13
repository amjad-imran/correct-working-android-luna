package com.oreo.ui.chatGpt

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.ChatGptOverview
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChatGptViewModel
@Inject constructor(
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface,
    val oreoDeviceRepository: OreoDeviceRepository
) : BaseViewModel() {

    private val userImage = localDataStore.getUser()?.imageUrl
    private val _chatGptOverview = MutableLiveData<ArrayList<ChatGptOverview>>()
    val chatGptOverview: LiveData<ArrayList<ChatGptOverview>>
        get() = _chatGptOverview

    var assistantId: String? = null
    var threadId: String? = null


    fun addSentMessage(message: String) {
        val messages = _chatGptOverview.value ?: ArrayList()
        messages.add(ChatGptOverview.SentMessage(message, userImage))
        _chatGptOverview.value = (messages)
        //_chatGptOverview.postValue(messages)
    }

    fun addReceivedMessage(message: String, thinking: Boolean) {

        val messages = _chatGptOverview.value ?: ArrayList()
        if (thinking) {
            messages.add(ChatGptOverview.ReceivedMessage(thinking, message))
        } else {
            val lastOverViewType = messages.last()
            if (lastOverViewType is ChatGptOverview.ReceivedMessage) {
                messages.removeLast()
                messages.add(ChatGptOverview.ReceivedMessage(thinking, message))
            }

        }

        _chatGptOverview.postValue(messages)
    }


    fun askQuestion(prompt: String) {
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
                                        askQuestion(prompt)
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
                                sendMessage("Something went wrong")
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
                                        pollAnswer(assistantId, threadId, runId)
                                    }

                                    override fun no() {

                                    }
                                }
                        })

                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            if (it.status.equals("completed", true) && it.reply != null) {
                                    addReceivedMessage(it.reply, false)
                            } else {
                                callAfterSomeTime(assistantId, threadId, runId)
                            }
                        }
                    }
                }
            }
        }
    }
}