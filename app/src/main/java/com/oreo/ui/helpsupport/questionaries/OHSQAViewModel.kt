package com.oreo.ui.helpsupport.questionaries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.OHSQuestionariesResponseModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OHSQAViewModel @Inject constructor(
    val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {
    private val _hsqAnswerData = MutableLiveData<List<OHSQuestionariesResponseModel>>()
    val hsqAnswerData: LiveData<List<OHSQuestionariesResponseModel>> = _hsqAnswerData

    fun getHSQAnswer(id: String) {
        viewModelScope.launch {
            userActivityRepository.getHSQAnswer(
                id
            ).collect { resource ->
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
                                        getHSQAnswer(id)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _hsqAnswerData.postValue(it)
                        }
                    }
                }
            }
        }
    }
}