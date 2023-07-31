package com.noisefit.ui.myDevice.warrantyOld

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WarrantyViewModelOld @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val localDataStore: DataStoredInterface
) : BaseViewModel() {

    private val _marketPlacesUpdated = MutableLiveData<List<String>>()
    val marketPlaces: LiveData<List<String>> = _marketPlacesUpdated

    private val _warrantyRegistered = MutableLiveData<Event<Boolean>>()
    val warrantyRegistered: LiveData<Event<Boolean>> = _warrantyRegistered


    init {
        getMarketPlaces()
    }

    fun getMarketPlaces() {
        viewModelScope.launch {
            deviceRepository.getMarketPlacesOld().collect { resource ->
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
                                    getMarketPlaces()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.let {
                            it.data?.let { markets ->
                                _marketPlacesUpdated.value = markets
                            }
                        }
                    }
                }
            }
        }
    }

    fun addWarranty(marketPlace: String, serialNo: String) {
        val user = localDataStore.getUser()
        val connectedDevice = localDataStore.getConnectedDevice()
        val requestObject = JsonObject()
        requestObject.addProperty("name", user?.firstName)
        requestObject.addProperty("email", user?.email)
        requestObject.addProperty("mobile_no", user?.mobile)
        requestObject.addProperty("registration_time", DateFormats.getTodaysDateString(8))
        requestObject.addProperty("unique_id", connectedDevice?.deviceId)
        requestObject.addProperty("source", "android")
        requestObject.addProperty("platform", marketPlace)
        requestObject.addProperty("order_number", "122323")
        requestObject.addProperty("serial_number", serialNo)


        viewModelScope.launch {
            deviceRepository.addWarrantyOld(requestObject).collect { resource ->
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
                                    addWarranty(marketPlace, serialNo)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.let {
                            //localDataStore.setWarrantyStatus(true)
                            _warrantyRegistered.value = Event(true)
                        }
                    }
                }
            }
        }
    }
}