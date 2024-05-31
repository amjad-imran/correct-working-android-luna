package com.oreo.ui.femalehealth.cycletracker.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.FMHCycleHistoryDataModel
import com.oreo.data.repository.abstraction.FemaleHealthRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CycleTrackerHistoryViewModel @Inject constructor(
    private val femaleHealthRepository: FemaleHealthRepository
) : BaseViewModel() {

    val cycleHistoryData = MutableLiveData<List<FMHCycleHistoryDataModel>>()


    fun getCycleHistoryData() {
        viewModelScope.launch {
            femaleHealthRepository.getPeriodCycleHistory().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object :
                                BinaryActionCallback {
                                override fun yes() {
                                    getCycleHistoryData()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            cycleHistoryData.postValue(it.cycleHistory?:ArrayList())
                        }
                    }
                }
            }
        }
    }
}