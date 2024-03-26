package com.oreo.ui.stress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.OInternalPageResponseModal
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OSIDViewModel @Inject constructor(
    val userActivityRepository: OreoUserActivityRepository,
    val sessionManager: SessionManager
) : BaseViewModel() {


    var dayType: String? = null
    var selectedDate: String? = null

    private val _internalDetailsData = MutableLiveData<OInternalPageResponseModal>()
    val internalDetailsData: LiveData<OInternalPageResponseModal>
        get() = _internalDetailsData


    fun getInternalDetailsData() {
        viewModelScope.launch {
            userActivityRepository.getStressInternalPagesData(
                selectedDate!!, dayType.toString().lowercase()
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
                                        getInternalDetailsData()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _internalDetailsData.postValue(it)
                        }
                    }
                }
            }
        }
    }

}