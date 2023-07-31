package com.noisefit.ui.content.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.model.VideosList
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.ContentRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WContentDetailsViewModel @Inject constructor(private val contentRepository: ContentRepository) :
    BaseViewModel() {
    var id: Int? = null
    var title: String? = null
    var videoUrl: String? = null
    var videoId: Int? = null

    private val _contentDetails = MutableLiveData<VideosList>()
    val contentDetails: LiveData<VideosList>
        get() = _contentDetails

    fun getContentDetails() {
        viewModelScope.launch {
            id?.let {
                contentRepository.getVideoDetails(it).collect { resource ->
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
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        getContentDetails()
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data.let {
                                _contentDetails.postValue(it)
                            }

                        }
                    }
                }
            }
        }
    }
}