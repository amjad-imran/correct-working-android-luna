package com.noisefit.ui.dashboard.feature.weather

import android.location.Address
import android.location.Geocoder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.WeatherRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.utils.Event
import com.noisefit_commans.converter.WeatherConverter
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel
@Inject
constructor(
    private val weatherRepository: WeatherRepository,
    private val localDataStore: DataStoredInterface,
    private val geoCoder: Geocoder,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    var lat: Double? = null
    var long: Double? = null


    private val _weatherData = MutableLiveData<Pair<List<WeatherData>, List<WeatherDataHourly>>>()
    private val _location = MutableLiveData<Event<Location>>()
    private val _temperatureUnit = MutableLiveData<Units>()

    private var _weatherEnabled = MutableLiveData<Event<Boolean>>()

    var showUnitSelection = false

    val location: LiveData<Event<Location>>
        get() = _location

    val temperatureUnit: LiveData<Units>
        get() = _temperatureUnit

    val weatherData: LiveData<Pair<List<WeatherData>, List<WeatherDataHourly>>>
        get() = _weatherData

    val weatherEnabled: LiveData<Event<Boolean>>
        get() = _weatherEnabled


    fun setUnitSelectionVisibility() {
        val connectedDevice = getConnectedDevice()

        if (connectedDevice == null) {
            showUnitSelection = false
            return
        }

        showUnitSelection =
            !(connectedDevice.deviceType!!.equals(DeviceType.NOISEFIT_ACTIVE.deviceType, true) ||
                    connectedDevice.deviceType!!.equals(
                        DeviceType.NOISEFIT_AGILE.deviceType,
                        true
                    ) ||
                    connectedDevice.deviceType!!.equals(
                        DeviceType.COLORFIT_PRO_3.deviceType,
                        true
                    ) ||
                    connectedDevice.deviceType!!.equals(DeviceType.COLORFIT_NAV.deviceType, true) ||
                    connectedDevice.deviceType!!.equals(
                        DeviceType.NOISEFIT_HYBRID.deviceType,
                        true
                    ))


    }

    fun setWeatherEnabled(isEnabled: Boolean) {
        localDataStore.updateWeatherSettings(isEnabled)
        if (_weatherEnabled.value?.peekContent() != isEnabled) {
            _weatherEnabled.value = Event(isEnabled)
        }
    }

    fun setUnit(unit: Units) {
        localDataStore.saveTemperatureUnit(unit)
        _temperatureUnit.value = unit
    }

    fun getTemperatureUnit(): String {
        return when (_temperatureUnit.value) {
            Units.METRIC -> {
                return "Celsius"
            }
            Units.IMPERIAL -> {
                return "Fahrenheit"
            }
            else -> ""
        }
    }

    fun updateLocation(address: Address?, locationString: String) {
        if (lat == null || long == null) return
        localDataStore.updateLocations(Location(lat!!, long!!, address, locationString))
    }

    init {
        _temperatureUnit.value = (localDataStore.getTemperatureUnit())
        _weatherEnabled.value = (Event(localDataStore.getWeatherSwitch()))

        if (localDataStore.getWeatherSwitch() == true) {
            _location.value = Event(localDataStore.getLocation())
        }
    }

    fun getWeatherData() {
        setLoading(true)

        if (lat == null || long == null) {
            setLoading(false)
            return
        }


        viewModelScope.launch {
            weatherRepository.getWeatherData(
                lat!!,
                long!!,
                temperatureUnit.value!!.name.lowercase()
            )
                .collect { resource ->
                    when (resource) {
                        is Resource.NetworkError -> {
                            setLoading(false)
                            setApiErrors(resource.response.apply {
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        getWeatherData()
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            setLoading(false)
                            resource.data?.let {
                                _weatherData.postValue(
                                    WeatherConverter.parseWeatherData(
                                        it,
                                        temperatureUnit.value!!.name.lowercase(),
                                        location.value?.peekContent()?.address
                                    )
                                )
                            }
                        }
                        else -> {}
                    }
                }
        }
    }

    fun getAddress() {

        try {
            setLoading(true)

            if (lat == null || long == null) {
                setLoading(false)
                return
            }

            val addresses = geoCoder.getFromLocation(lat!!, long!!, 1)
            var address: Address? = null
            var locationString: String = ""
            if (!addresses.isNullOrEmpty()) {
                address = addresses[0]
                locationString = ApplicationUtils.getLocationString(address)
                //getWeatherData()
            }
            _location.value = Event(Location(lat!!, long!!, address, locationString))

            //getWeatherData()
        } catch (e: Exception) {
            e.printStackTrace()
            _location.value = Event(Location(lat!!, long!!, null, ""))
        }

    }


    fun getLocalDataStore(): DataStoredInterface {
        return localDataStore
    }

    fun getSessionManager(): SessionManager {
        return sessionManager
    }

    fun getConnectedDevice(): ColorFitDevice? {
        return localDataStore.getConnectedDevice()
    }


}