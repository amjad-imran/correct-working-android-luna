package com.noisefit.ui.friends.location.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.CityData
import com.noisefit.data.remote.StateData
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.FriendsRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SearchStateViewModel
@Inject
constructor(
    val friendsRepository: FriendsRepository,
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager
) : BaseViewModel() {


    private val _stateList = MutableLiveData<List<StateData>>()
    val stateList: LiveData<List<StateData>>
        get() = _stateList

    private val _cityList = MutableLiveData<List<CityData>>()
    val cityList: LiveData<List<CityData>>
        get() = _cityList

    var id: Int? = null
    var name: String? = null
    var type: SearchStateType = SearchStateType.State

    init {

    }


    fun updateUserLocationState(data: String?, id: Int?) {
        sessionManager.updateUserLocationState(data, id, type)
    }


    fun fetchStateList() {

        viewModelScope.launch {
            friendsRepository.getStateList().collect { resource ->
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
                                    fetchStateList()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _stateList.postValue(it)
                        }
                    }
                }
            }
        }

    }

    fun fetchCityList() {

        viewModelScope.launch {
            friendsRepository.getCityList(id!!).collect { resource ->
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
                                    fetchCityList()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _cityList.postValue(it)
                        }
                    }
                }
            }
        }

    }


}