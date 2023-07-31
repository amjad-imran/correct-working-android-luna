package com.noisefit.ui.content.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.model.ContentListData
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
class DashboardWorkoutViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : BaseViewModel() {
    private val _contentList = MutableLiveData<ContentListData>()
    val contentList: LiveData<ContentListData>
        get() = _contentList

    fun getContentList() {
        viewModelScope.launch {
            contentRepository.getContentList().collect { resource ->
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
                                    getContentList()
                                }

                                override fun no() {

                                }
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _contentList.postValue(it)
                        }
                    }
                }
            }
        }
    }

    fun getSubCatVidList(id: Int, fetchSuccess: (List<VideosList>) -> Unit) {
        viewModelScope.launch {
            contentRepository.getSubCategoryVideoList(id = id).collect { resource ->
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
                                    getSubCatVidList(id, fetchSuccess)
                                }

                                override fun no() {

                                }
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            fetchSuccess(it)
                        }
                    }
                }
            }
        }
    }
}