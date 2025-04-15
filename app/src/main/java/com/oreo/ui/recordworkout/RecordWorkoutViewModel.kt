package com.oreo.ui.recordworkout

import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.implementation.WeatherRepository
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.db.abstraction.LocationDataSource
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.data.model.OWorkoutListModal
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.weather.WeatherItem
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

@HiltViewModel
class RecordWorkoutViewModel @Inject constructor(
    val sessionManager: SessionManager,
    val watchDataStore: WatchDataStore,
    val locationDataSource: LocationDataSource,
    val weatherRepository: WeatherRepository,
    val ringDataStore: RingDataStore,
    val geoCoder: Geocoder,
    val resourcesProvider: ResourcesProvider
) : BaseViewModel() {


    val showWorkoutStoppedByRingDialog = MutableLiveData<Event<String>>()
    var markedDeleted: Boolean = false
    var workout: OWorkoutListModal? = null
    var sportStartTime = 0L
    var workoutDuration = 0L
    var timer: Timer? = null

    var displayTimer = MutableLiveData<String>()

    /**
     * 0->Default
     * 1->Started
     * 2->Pause
     * 3->Resume
     * 4->Stop
     */
    var currentWorkoutState = 0


    fun getCurrentTimeStamp(): Long {
        return System.currentTimeMillis() / 1000
    }

    fun updateTimer() {
        val hours = workoutDuration / 3600
        val minutes = (workoutDuration % 3600) / 60
        val seconds = workoutDuration % 60

        val timeString = if (hours == 0L) {
            String.format(locale = Locale.US, "%02d:%02d", minutes, seconds)
        } else {
            String.format(locale = Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        }
        displayTimer.postValue(timeString)
    }

    fun starTimer() {
        timer?.cancel()
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    workoutDuration += 1
                    updateTimer()
                }
            }, 0, 1000)
        }
    }

    fun pauseTimer() {
        timer?.cancel()
    }

    fun resumeTimer() {
        starTimer()
    }

    fun stopTimer() {
        timer?.cancel()
    }


    fun isDeviceConnected(): Boolean {
        if (isDevicePaired() == null) {
            return false
        }

        if (sessionManager.connectStateRing.value is ConnectState.ConnectSuccess) {
            return true
        }
        return false
    }

    fun isDevicePaired(): ColorFitDevice? {
        return ringDataStore.getRingDevice()
    }

    fun markForDelete(sportStartTime: Long) {
        ringDataStore.addToRecordDeleteList(sportStartTime * 1000L)
    }

    fun saveOngoingRecordWorkout() {
        if (sportStartTime != 0L && workout != null) {
            ringDataStore.saveOngoingRecordWorkout(Pair(sportStartTime, workout!!))
        }
    }

    fun deleteOngoingRecordWorkout() {
        ringDataStore.deleteOngoingRecordWorkout()
    }

    private suspend fun getWeatherData(lat: Double, lng: Double) {

    }

    fun requireGps(): Boolean {
        return workout?.isGpsRequired == 1
    }

    fun getWeatherDetails(lat: Double, lng: Double) {
        if (workout == null || sportStartTime == 0L) return
        if (lat == 0.0 || lng == 0.0) return

        viewModelScope.launch(Dispatchers.IO) {
            val unit = "metric"
            weatherRepository.getWeatherData(
                lat,
                lng,
                unit
            ).collect { resource ->
                when (resource) {
                    is Resource.NetworkError -> {
                    }

                    is Resource.Success -> {
                        resource.data?.let { weather ->
                            if (sportStartTime != 0L && workout != null) {



                                val weatherStatus =
                                    getWeatherStatus(weatherIdConverter(weather.current?.condition?.code))
                                if (weather.current?.tempC != null) {
                                    locationDataSource.updateWeatherInfoForLatLong(
                                        lat,
                                        lng,
                                        weather.current?.tempC!!,
                                        weatherStatus
                                    )
                                }

                                val newModel = workout!!.apply {
                                    this.isTempSet = true
                                }
                                ringDataStore.saveOngoingRecordWorkout(
                                    Pair(
                                        sportStartTime,
                                        newModel
                                    )
                                )
                                workout = newModel

                            }
                            //LOGS.i("weather data $weather")
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    /**
     * 0-> Clear
     * 2->Thunderstorm
     * 3->Drizzle
     * 5->Rain
     * 6->Snow
     * 7->Atmosphere
     * 8->Clouds
     */
    private fun getWeatherStatus(id: Int?): Int? {
        id ?: return null

        return when (id) {
            800 -> 0
            in 200..299 -> 2
            in 300..399 -> 3
            in 500..599 -> 5
            in 600..699 -> 6
            in 700..799 -> 7
            in 801..899 -> 8
            else -> null
        }
    }

    private fun weatherIdConverter(id: Int?): Int {
        if(id==null) return 800
        var updatedWId: Int = 200
        when (id) {
            1000 -> {
                updatedWId = 800
            }

            1003 -> {
                updatedWId = 801
            }

            1006 -> {
                updatedWId = 802
            }

            1009 -> {
                updatedWId = 804
            }

            1030 -> {
                updatedWId = 701
            }

            1063 -> {
                updatedWId = 500
            }

            1066 -> {
                updatedWId = 600
            }

            1069 -> {
                updatedWId = 611
            }

            1072 -> {
                updatedWId = 511
            }

            1087 -> {
                updatedWId = 200
            }

            1114 -> {
                updatedWId = 621
            }

            1135 -> {
                updatedWId = 741
            }

            1147 -> {
                updatedWId = 741
            }

            1150 -> {
                updatedWId = 300
            }

            1153 -> {
                updatedWId = 301
            }

            1168 -> {
                updatedWId = 511
            }

            1180 -> {
                updatedWId = 500
            }

            1183 -> {
                updatedWId = 500
            }

            1192 -> {
                updatedWId = 502
            }

            1195 -> {
                updatedWId = 502
            }

            1210 -> {
                updatedWId = 600
            }

            1213 -> {
                updatedWId = 600
            }

            1225 -> {
                updatedWId = 602
            }

            1240 -> {
                updatedWId = 520
            }

            1243 -> {
                updatedWId = 522
            }

            1255 -> {
                updatedWId = 620
            }

            1258 -> {
                updatedWId = 622
            }

            1273 -> {
                updatedWId = 200
            }

            1276 -> {
                updatedWId = 201
            }

            1279 -> {
                updatedWId = 202
            }

            1282 -> {
                updatedWId = 212
            }

            1171 -> {
                updatedWId = 312
            }

            1186 -> {
                updatedWId = 501
            }

            1189 -> {
                updatedWId = 501
            }

            1198 -> {
                updatedWId = 511
            }

            1201 -> {
                updatedWId = 511
            }

            1204 -> {
                updatedWId = 611
            }

            1207 -> {
                updatedWId = 613
            }

            1216 -> {
                updatedWId = 601
            }

            1219 -> {
                updatedWId = 601
            }

            1222 -> {
                updatedWId = 602
            }

            1237 -> {
                updatedWId = 616
            }

            1246 -> {
                updatedWId = 531
            }

            1249 -> {
                updatedWId = 612
            }

            1252 -> {
                updatedWId = 613
            }

            1261 -> {
                updatedWId = 620
            }

            1264 -> {
                updatedWId = 622
            }

            1117 -> {
                updatedWId = 771
            }

            else -> 721
        }
        return updatedWId
    }


    fun shouldCheckWeather(): Boolean {
        if (sportStartTime == 0L || workout == null) return false
        if (workout?.isGpsRequired == 1 && workout?.isTempSet == false) return true
        return false
    }

    fun shouldCheckCity(): Boolean {
        if (sportStartTime == 0L || workout == null) return false
        if (workout?.isGpsRequired == 1 && workout?.isCitySet == false) return true
        return false
    }

    fun getAddress(lat: Double, long: Double) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geoCoder.getFromLocation(
                lat, long, 1
            ) { addresses ->
                val address = addresses.getOrNull(0)
                viewModelScope.launch(Dispatchers.IO) {
                    updateCity(address?.locality, lat, long)
                }
            }
        } else {
            val addresses = geoCoder.getFromLocation(lat, long, 1)
            val address = addresses?.getOrNull(0)
            viewModelScope.launch(Dispatchers.IO) {
                updateCity(address?.locality, lat, long)
            }
        }
    }

    private suspend fun updateCity(locality: String?, lat: Double, long: Double) {

        if (sportStartTime != 0L && workout != null) {

            locationDataSource.updateCityForLatLong(
                lat,
                long,
                locality ?: ""
            )
            val newModel = workout!!.apply {
                this.isCitySet = true
            }
            ringDataStore.saveOngoingRecordWorkout(
                Pair(
                    sportStartTime,
                    newModel
                )
            )
            workout = newModel
        }

    }

    fun getStoppedByRingMessage(error: String, showSave: Boolean): String {
        return if (error.equals("charging", true)) {
            if (showSave) {
                "Your workout has ended because you have kept your ring on charging. Make sure you wear your ring on your finger while doing workout. Do you want to save your current progress?"
            } else {
                "Your workout has ended because you have kept your ring on charging. Make sure you wear your ring on your finger while doing workout. Since your workout last less than 1 minute, it won't be saved."
            }
        } else {
            resourcesProvider.getString(R.string.text_ring_lo_battert_stop_message)
        }
    }
}