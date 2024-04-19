package com.oreo.ui.recordworkout

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.implementation.WeatherRepository
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
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

@HiltViewModel
class RecordWorkoutViewModel @Inject constructor(
    val sessionManager: SessionManager,
    val watchDataStore: WatchDataStore,
    val locationDataSource: LocationDataSource,
    val weatherRepository: WeatherRepository,
    val ringDataStore: RingDataStore
) : BaseViewModel() {


    val showWorkoutStoppedByRingDialog = MutableLiveData<Event<Boolean>>()
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
            String.format("%02d:%02d", minutes, seconds)
        } else {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
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
                                    getWeatherStatus(weather.current.weather?.firstOrNull())
                                locationDataSource.updateWeatherInfoForLatLong(
                                    lat,
                                    lng,
                                    weather.current.temp,
                                    weatherStatus
                                )
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
                            LOGS.i("weather data $weather")
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
    private fun getWeatherStatus(weather: WeatherItem?): Int? {
        val weatherId = weather?.id ?: return null

        return when (weatherId) {
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

    fun shouldCheckWeather(): Boolean {
        if (sportStartTime == 0L || workout == null) return false
        if (workout?.isGpsRequired == 1 && workout?.isTempSet == false) return true
        return false
    }
}