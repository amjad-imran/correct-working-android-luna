package com.oreo.ui.chatGpt.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.ai.ChatHistoryItem
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatHistoryViewModel @Inject constructor(
    val oreoDeviceRepository: OreoDeviceRepository
) : BaseViewModel() {

    private val _chatHistory = MutableLiveData<List<ChatHistoryItem>>()
    val chatHistory: LiveData<List<ChatHistoryItem>> = _chatHistory

    fun getChatHistory() {
        viewModelScope.launch {
            oreoDeviceRepository.getChatHistory().collect { resource ->
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
                                        getChatHistory()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            generateData(it)
                        }
                    }
                }
            }
        }
    }

    private fun generateData(data: List<ChatHistoryItem>) {

        val result = ArrayList<ChatHistoryItem>()
        val datesSet = HashSet<String>()

        data.forEach {
            it.date ?: return@forEach

            val date = DateFormats.formatDateTime(
                it.date, DateFormats.dateFormat3(),
                DateFormats.dateFormat6()
            )

            if (date.isEmpty()) return@forEach

            if (!datesSet.contains(date)) {
                datesSet.add(date)
                result.add(ChatHistoryItem(isHeader = true, date = date))
            }

            result.add(it.apply {
                isHeader = false
            })
        }
        _chatHistory.postValue(result)
    }

    fun deleteChatHistory(threadId: String) {
        val oldData = _chatHistory.value as? ArrayList<ChatHistoryItem>
        val index = oldData?.indexOfFirst {
            it.threadId.equals(threadId)
        }

        if (index != null && index != -1) {
            oldData.removeAt(index)
        }

        if (oldData != null) {
            _chatHistory.postValue(oldData)
        }
    }
}