package com.oreo.ui.findmyring

import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.model.RingLocationData
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RingLocationViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
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

                            ringLocationData.postValue(it.firstOrNull())

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
                onAddressFetched(address?.getAddressLine(0))
            }
        } else {
            val addresses = geoCoder.getFromLocation(lat, long, 1)
            val address = addresses?.getOrNull(0)
            onAddressFetched(address?.getAddressLine(0))
//            onAddressFetched(address?.locality)
        }
    }
}