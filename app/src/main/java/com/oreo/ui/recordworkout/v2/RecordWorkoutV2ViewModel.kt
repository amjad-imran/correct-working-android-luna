package com.oreo.ui.recordworkout.v2

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
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.data.model.OWorkoutListModal
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.weather.WeatherItem
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.random
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class RecordWorkoutV2ViewModel @Inject constructor(
    val sessionManager: SessionManager,
    val watchDataStore: WatchDataStore,
    val locationDataSource: LocationDataSource,
    val weatherRepository: WeatherRepository,
    val ringDataStore: RingDataStore,
    val geoCoder: Geocoder,
    private val localDataStore: DataStoredInterface,
    val resourcesProvider: ResourcesProvider
) : BaseViewModel() {


    val showWorkoutStoppedByRingDialog = MutableLiveData<Event<String>>()
    var markedDeleted: Boolean = false
    var workout: OWorkoutListModal? = null
    var sportStartTime = 0L
    var workoutDuration = 0L
    var timer: Timer? = null

    var displayTimer = MutableLiveData<String>()
    var countDownTimer = MutableLiveData<String?>()
    private val hrZones = ArrayList<HrZoneData>()


    init {
        setupZoneId()
    }

    private fun setupZoneId() {
        val age = getUserAge()
        val hrMax = (208 - 0.7 * age)
        // Zone 1 (50-60%)
        val zone1Min = (0.5 * hrMax).roundToInt()
        val zone1Max = (0.6 * hrMax).roundToInt()

        hrZones.add(HrZoneData(1, zone1Max, 1))

        // Zone 2 (60-70%)
        val zone2Max = (0.7 * hrMax).roundToInt()
        hrZones.add(HrZoneData(zone1Max, zone2Max, 2))

        // Zone 3 (70-80%)
        val zone3Max = (0.8 * hrMax).roundToInt()
        hrZones.add(HrZoneData(zone2Max, zone3Max, 3))

        // Zone 4 (80-90%)
        val zone4Max = (0.9 * hrMax).roundToInt()
        hrZones.add(HrZoneData(zone3Max, zone4Max, 4))

        // Zone 5 (90-100%)
        val zone5Max = hrMax.roundToInt()
        hrZones.add(HrZoneData(zone4Max, zone5Max, 5))
    }

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
                                    getWeatherStatus(weather.weather?.firstOrNull()?.id)
                                if (weather.main?.temp != null) {
                                    locationDataSource.updateWeatherInfoForLatLong(
                                        lat,
                                        lng,
                                        weather.main?.temp!!,
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

    fun startStartCountDown() {

        viewModelScope.launch(Dispatchers.IO) {
            var count = 3
            while (count > 0) {
                count -= 1
                countDownTimer.postValue((count + 1).toString())
                delay(1000)
            }
            countDownTimer.postValue("start")
        }
    }


    fun getHeartRateZone(currentBpm: Int?): Int? {

        //return (0..4).random()

        if (currentBpm == null || currentBpm == 0 || currentBpm == 255) return null
        val newZoneIndex = hrZones.indexOfFirst { currentBpm in it.minBpm..it.maxBpm }
            .takeIf { it != -1 } ?: -1

        if (newZoneIndex == -1) {
            return null
        }
        return newZoneIndex


    }

    fun getZoneText(zoneId: Int): String {
        return "Zone ${zoneId + 1}"
    }

    private fun getUserAge(): Int {
        val dob = localDataStore.getUser()?.userInfo?.dob ?: return 30
        return try {
            val dateOfBirth = LocalDate.parse(dob)
            val currentDate = LocalDate.now()
            Period.between(dateOfBirth, currentDate).years
        } catch (exp: Exception) {
            30
        }
    }

    fun getBgDrawableByZoneId(zoneId: Int?): Int {
        return when(zoneId){
            0 ->  R.drawable.back_workout_zone_1
            1 ->  R.drawable.back_workout_zone_2
            2 ->  R.drawable.back_workout_zone_3
            3 ->  R.drawable.back_workout_zone_4
            4 ->  R.drawable.back_workout_zone_5
            else ->  R.drawable.back_zone_transparent
        }
    }

    fun isWorkoutRunning(): Boolean {
        return currentWorkoutState == 1 || currentWorkoutState == 3

    }

    data class HrZoneData(val minBpm: Int, val maxBpm: Int, val zoneId: Int)

}