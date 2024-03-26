package com.oreo.ui.stress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.OStressInternalPageResponseModal
import com.oreo.data.model.StressData
import com.oreo.data.model.StressShowData
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OSIDViewModel @Inject constructor(
    val userActivityRepository: OreoUserActivityRepository,
    val sessionManager: SessionManager
) : BaseViewModel() {


    var dayType: String? = null
    var selectedDate: String? = null

    private val _internalDetailsData = MutableLiveData<OStressInternalPageResponseModal>()
    val internalDetailsData: LiveData<OStressInternalPageResponseModal>
        get() = _internalDetailsData


    fun getInternalDetailsData() {
        /*viewModelScope.launch {
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
        }*/
        val dummyData = OStressInternalPageResponseModal(
            resultData = null, stressData = StressData(
                focussed = StressShowData(duration = 368, 5),
                calm = StressShowData(duration = 468, 5),
                stressed = StressShowData(duration = 125, 14),
                avgDuration = 425,
                dspMsg = "You have spent an average of"
            )
        )
        _internalDetailsData.postValue(dummyData)
    }

}