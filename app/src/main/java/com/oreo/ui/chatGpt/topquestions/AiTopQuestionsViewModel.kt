package com.oreo.ui.chatGpt.topquestions

import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModelCompose
import com.oreo.data.model.ai.TopQuestions
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import com.oreo.ui.chatGpt.AITopics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiTopQuestionsViewModel @Inject constructor(
    val oreoDeviceRepository: OreoDeviceRepository,
    private val localDataStore: DataStoredInterface
) : BaseViewModelCompose() {

    val questions = MutableStateFlow<List<TopQuestions>>(arrayListOf())
    val showHistoryIcon = MutableStateFlow<Boolean>(false)
    val userName = MutableStateFlow<String>("")

    init {
        viewModelScope.launch(Dispatchers.IO) {
            userName.value = localDataStore.getUser()?.firstName ?: ""
        }
    }

    fun getAiTopQuestions(aiTopic: AITopics) {
        viewModelScope.launch {
            oreoDeviceRepository.getAiTopQuestions(aiTopic).collect { resource ->
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
                            showHistoryIcon.value = it.hasHistory
                        }
                    }
                }
            }
        }


    }

}