package com.oreo.ui.chatGpt

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
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
    val oreoDeviceRepository: OreoDeviceRepository
) : BaseViewModel() {

    private val _chatGptOverview = MutableLiveData<ArrayList<ChatGptOverview>>()
    val chatGptOverview: LiveData<ArrayList<ChatGptOverview>>
        get() = _chatGptOverview


    fun addSentMessage(message: String) {
        val messages = _chatGptOverview.value ?: ArrayList()
        messages.add(ChatGptOverview.SentMessage(message))
        _chatGptOverview.value = (messages)
        _chatGptOverview.postValue(messages)
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

        /*
         Timer("DelayConnection", false)
                            .schedule(500) {
                                isInitSDK = true
                                baseInitializeCallbacks?.serviceConnected()
                            }
         */
        val jsonObject = JsonObject()
        jsonObject.addProperty("message", prompt)
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
                            it.reply?.let { it1 -> addReceivedMessage(it1, false) }
                        }
                    }
                }
            }
        }
    }
}