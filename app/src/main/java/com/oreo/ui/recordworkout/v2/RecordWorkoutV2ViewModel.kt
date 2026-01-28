package com.oreo.ui.recordworkout.v2

import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
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
import android.os.SystemClock
import java.io.IOException
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
    val userRepository: UserRepository,
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
    // Monotonic duration tracking
    private var baseDurationSec: Long = 0L
    private var resumeRealtimeMs: Long = 0L

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

    private fun computedDurationSec(): Long {
        return if (resumeRealtimeMs > 0L) {
            val delta = (SystemClock.elapsedRealtime() - resumeRealtimeMs) / 1000
            baseDurationSec + delta
        } else {
            baseDurationSec
        }
    }

    fun updateTimer() {
        workoutDuration = computedDurationSec()
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
        resumeRealtimeMs = SystemClock.elapsedRealtime()
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    updateTimer()
                }
            }, 0, 1000)
        }
    }

    fun pauseTimer() {
        baseDurationSec = computedDurationSec()
        resumeRealtimeMs = 0L
        timer?.cancel()
    }

    fun resumeTimer() {
        starTimer()
    }

    fun stopTimer() {
        timer?.cancel()
    }

    fun initFromOngoing(durationSec: Long, sportStatus: Int) {
        baseDurationSec = durationSec
        workoutDuration = baseDurationSec
        resumeRealtimeMs = 0L
        updateTimer()
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
        if (id == null) return 800
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geoCoder.getFromLocation(lat, long, 1) { addresses ->
                        val address = addresses.getOrNull(0)
                        viewModelScope.launch(Dispatchers.IO) {
                            updateCity(address?.locality, lat, long)
                        }
                    }
                } else {
                    val addresses = geoCoder.getFromLocation(lat, long, 1)
                    val address = addresses?.getOrNull(0)
                    updateCity(address?.locality, lat, long)
                }
            } catch (e: IOException) {
                updateCity(null, lat, long)
            } catch (e: IllegalArgumentException) {
                updateCity(null, lat, long)
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

        countDownTimer.postValue("start")

        /*viewModelScope.launch(Dispatchers.IO) {
            var count = 3
            while (count > 0) {
                count -= 1
                countDownTimer.postValue((count + 1).toString())
                delay(1000)
            }
            countDownTimer.postValue("start")
        }*/
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
        return when (zoneId) {
            0 -> R.drawable.back_workout_zone_1
            1 -> R.drawable.back_workout_zone_2
            2 -> R.drawable.back_workout_zone_3
            3 -> R.drawable.back_workout_zone_4
            4 -> R.drawable.back_workout_zone_5
            else -> R.drawable.back_zone_transparent
        }
    }

    fun isWorkoutRunning(): Boolean {
        return currentWorkoutState == 1 || currentWorkoutState == 3

    }

    fun sendWorkoutEvent(status: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val request = JsonObject().apply {
                this.addProperty("status", status)
            }
            userRepository.updateWorkoutStatus(request)
                .collect { resource ->

                }
        }

    }

    data class HrZoneData(val minBpm: Int, val maxBpm: Int, val zoneId: Int)

}
