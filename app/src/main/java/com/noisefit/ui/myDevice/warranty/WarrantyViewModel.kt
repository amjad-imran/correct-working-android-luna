package com.noisefit.ui.myDevice.warranty

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.warranty.MarketPlace
import com.noisefit_commans.data.model.warranty.WarrantyWatch
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WarrantyViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val localDataStore: DataStoredInterface
) : BaseViewModel() {

    private val _marketPlacesUpdated = MutableLiveData<List<MarketPlace>>()
    val marketPlaces: LiveData<List<MarketPlace>> = _marketPlacesUpdated

    private val _warrantyWatches = MutableLiveData<List<WarrantyWatch>>()
    val warrantyWatches: LiveData<List<WarrantyWatch>> = _warrantyWatches

    private val _warrantyRegistered = MutableLiveData<Event<Boolean>>()
    val warrantyRegistered: LiveData<Event<Boolean>> = _warrantyRegistered

    private val _isWarrantyAlreadyRegistered = MutableLiveData<Boolean>()
    val isWarrantyAlreadyRegistered: LiveData<Boolean> = _isWarrantyAlreadyRegistered

    fun getInitialData() {
        getMarketPlaces()
        getWarrantyWatchList()
    }

    fun getMarketPlaces() {
        viewModelScope.launch {
            deviceRepository.getMarketPlaces().collect { resource ->
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

    fun getWarrantyWatchList() {
        viewModelScope.launch {
            deviceRepository.getWarrantyWatchList().collect { resource ->
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
                                    getWarrantyWatchList()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.let {
                            it.data?.let { resposnse ->
                                resposnse.forEach { res ->
                                    if (!res.Wearables.isNullOrEmpty()) {
                                        _warrantyWatches.value = res.Wearables
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun addWarranty(channelId: Int, productId: Int, serialNo: String, orderNo: String) {
        val user = localDataStore.getUser()
        val connectedDevice = localDataStore.getConnectedDevice()
        val requestObject = JsonObject()
        requestObject.addProperty("name", user?.firstName)
        requestObject.addProperty("email", user?.email)
        requestObject.addProperty("mobile_no", user?.mobile)
        requestObject.addProperty("order_number", orderNo)//Order no input from user
        requestObject.addProperty("serial_number", serialNo)
        requestObject.addProperty("unique_id", productId)//product id
        requestObject.addProperty("platform", channelId)//channel id
        requestObject.addProperty("system_id", connectedDevice?.address?.replace(":", ""))


        viewModelScope.launch {
            deviceRepository.addWarranty(requestObject).collect { resource ->
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
                                    addWarranty(channelId, productId, serialNo, orderNo)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.let {
                            localDataStore.setWarrantyStatus(1)
                            _warrantyRegistered.value = Event(true)
                        }
                    }
                }
            }
        }
    }

    fun checkWarranty() {

          val warrantyStatus = localDataStore.getWarrantyStatus()

          if (warrantyStatus == 0) {
              _isWarrantyAlreadyRegistered.value = (false)
              return
          } else if (warrantyStatus == 1) {
              _isWarrantyAlreadyRegistered.value = (true)
              return
          }

        val user = localDataStore.getUser()
        val connectedDevice = localDataStore.getConnectedDevice()
        val requestObject = JsonObject()
        requestObject.addProperty("email", user?.email)
        requestObject.addProperty("system_id", connectedDevice?.address?.replace(":", ""))


        viewModelScope.launch {
            deviceRepository.checkWarranty(requestObject).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let {
                            if (it.data == true) {
                                localDataStore.setWarrantyStatus(1)
                                _isWarrantyAlreadyRegistered.postValue(true)
                            } else {
                                localDataStore.setWarrantyStatus(0)
                                _isWarrantyAlreadyRegistered.postValue(false)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }


    fun getMarketNameList(): List<String> {
        val list = ArrayList<String>()
        marketPlaces.value?.forEach {
            list.add(it.name)
        }
        return list
    }

    fun getDeviceNameList(): List<String> {
        val list = ArrayList<String>()
        warrantyWatches.value?.forEach {
            list.add(it.product_name)
        }
        return list
    }

    fun getMarketPlaceId(name: String): Int {
        marketPlaces.value?.forEach {
            if (it.name.equals(name, true)) {
                return it.id
            }
        }
        return -1
    }

    fun getDeviceId(name: String): Int {
        warrantyWatches.value?.forEach {
            if (it.product_name.equals(name, true)) {
                return it.product_id
            }
        }
        return -1
    }
}