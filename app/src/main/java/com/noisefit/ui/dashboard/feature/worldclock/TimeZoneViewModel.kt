package com.noisefit.ui.dashboard.feature.worldclock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.WorldClockNetwork
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimeZoneViewModel @Inject constructor(
    val userRepository: UserRepository
) : BaseViewModel() {

    private val _clocks = MutableLiveData<List<WorldClockNetwork>>()

    private val _masterClocksList = ArrayList<WorldClockNetwork>()


    val clockList: LiveData<List<WorldClockNetwork>>
        get() = _clocks


    fun getCitiesList() {
        viewModelScope.launch {
            userRepository.getTimeZonesCities().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getCitiesList()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _masterClocksList.addAll(it.clocks)
                            _clocks.postValue(it.clocks)
                        }
                    }
                }
            }
        }
    }

    fun filterZoneList(searchText: String) {
        if(searchText.isEmpty()){
            _clocks.postValue(_masterClocksList)
            return
        }

        _clocks.postValue(_masterClocksList.filter {
            clock -> clock.city.contains(searchText,true)
        })
    }
}