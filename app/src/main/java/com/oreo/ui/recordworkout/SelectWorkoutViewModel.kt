package com.oreo.ui.recordworkout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.model.OWorkoutListModal
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectWorkoutViewModel @Inject
constructor(
    private val userActivityRepository: OreoUserActivityRepository,
    private val sessionManager: SessionManager,
    private val watchDataStore: WatchDataStore
) : BaseViewModel() {


    private val _oWorkoutListModalResponse = MutableLiveData<List<OWorkoutListModal>>()
    val oWorkoutListModalResponse: LiveData<List<OWorkoutListModal>> = _oWorkoutListModalResponse


    fun isDeviceConnected(): Boolean {
        return sessionManager.connectStateRing.value is ConnectState.ConnectSuccess
    }

    fun getWorkoutList() {
        viewModelScope.launch {

            userActivityRepository.getWorkoutListRecord().collect { resource ->
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
                                        getWorkoutList()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _oWorkoutListModalResponse.postValue(it)

                        }
                    }
                }
            }
        }


    }

    fun isBatteryLow(): Boolean {
        val batteryPercentage = watchDataStore.getBatteryPercentRing()
        return batteryPercentage<=5
    }


}