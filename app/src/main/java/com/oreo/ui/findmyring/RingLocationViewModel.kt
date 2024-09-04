package com.oreo.ui.findmyring

import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.model.RingLocationData
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RingLocationViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val watchDataStore: WatchDataStore,
    val geoCoder: Geocoder,
    private val ringDataStore: RingDataStore
) : BaseViewModel() {

    val ringLocationData = MutableLiveData<RingLocationData?>()


    fun getRingLastLocation() {
        viewModelScope.launch {
            val mac = ringDataStore.getRingDevice()?.address
            if (mac.isNullOrEmpty()) return@launch
            userRepository.getRingLastLocation(mac).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object :
                                    BinaryActionCallback {
                                    override fun yes() {
                                        getRingLastLocation()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                            ringLocationData.postValue(it)

                        }
                    }
                }
            }
        }


    }

    fun getUserName(): String {
        val name = localDataStore.getUser()?.firstName
        return if (name.isNullOrEmpty()) "" else "$name's "
    }

    fun getRingImage(): String? {
        val connectedDevice = ringDataStore.getRingDevice()
        return connectedDevice?.ringInfo?.image
    }


    fun getAddress(lat: Double?, long: Double?, onAddressFetched: (String?) -> Unit) {
        if (lat == null || long == null) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geoCoder.getFromLocation(
                lat, long, 1
            ) { addresses ->
                val address = addresses.getOrNull(0)

                //LOGS.d("sdfjshdkfjh $address")
                viewModelScope.launch(Dispatchers.Main) {
                    onAddressFetched(address?.getAddressLine(0))
                }
            }
        } else {
            val addresses = geoCoder.getFromLocation(lat, long, 1)
            val address = addresses?.getOrNull(0)
            viewModelScope.launch(Dispatchers.Main) {
                onAddressFetched(address?.getAddressLine(0))
            }
//            onAddressFetched(address?.locality)
        }
    }

    fun updateRingLocation(location: Pair<Double, Double>) {
        viewModelScope.launch {
            val mac = ringDataStore.getRingDevice()?.address
            val batteryPercent = watchDataStore.getBatteryPercentRing()

            ringLocationData.postValue(
                RingLocationData(
                    location.first,
                    location.second,
                    batteryPercent,
                    DateFormats.getCurrentDate(DateFormats.dateTimeFormat5())
                )
            )


            val request = JsonObject().apply {
                this.addProperty("latitude", location.first)
                this.addProperty("longitude", location.second)
                this.addProperty("battery_percentage", batteryPercent)
                this.addProperty("mac_address", mac)
            }

            userRepository.setRingLastLocation(
                request
            ).collect { resource ->
                when (resource) {

                    is Resource.Success -> {
                        resource.data?.data?.let {

                        }
                    }

                    else -> {}
                }
            }
        }
    }

}