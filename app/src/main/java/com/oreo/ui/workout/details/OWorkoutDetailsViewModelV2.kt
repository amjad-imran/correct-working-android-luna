package com.oreo.ui.workout.details

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit.ui.common.calculatePercentage
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.ceilRound
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.Units
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import com.oreo.data.model.OWDActivityData
import com.oreo.data.model.OWDActivityHRZoneData
import com.oreo.data.model.OWorkoutDetailsResponseModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.time.LocalDate
import java.time.Period
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class OWorkoutDetailsViewModelV2 @Inject constructor(
    val dataUnitConverter: DataUnitConverter,
    val userActivityRepository: OreoUserActivityRepository,
    val localDataStore: DataStoredInterface,
    private val userHealthDataDataSource: OreoUserHealthDataDataSource
) : BaseViewModel() {


    var avgValue: String = ""
    var workoutDetailsExpanded = false
    var position: Int = -1
    private val _workoutDeletedResponse = MutableLiveData<Event<Boolean>>()
    val workoutDeletedResponse: LiveData<Event<Boolean>> = _workoutDeletedResponse
    private val _workoutDetailsResponse = MutableLiveData<OWorkoutDetailsResponseModel>()
    val workoutDetailsResponse: LiveData<OWorkoutDetailsResponseModel> = _workoutDetailsResponse
    fun getWorkoutDetails(workoutId: String) {
        viewModelScope.launch {
            userActivityRepository.getWorkoutDetails(
                workoutId
            ).collect { resource ->
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
                                        getWorkoutDetails(workoutId)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _workoutDetailsResponse.postValue(it)
                        }
                    }
                }
            }
        }

    }

    fun deleteWorkout(workoutId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userActivityRepository.deleteWorkoutFromServer(
                workoutId
            ).collect { resource ->
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
                                        deleteWorkout(workoutId)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            sendMessage("Workout Deleted")
                            userHealthDataDataSource.clearDataByDates(
                                listOf(
                                    DateFormats.getTodaysDateString(
                                        10
                                    )
                                )
                            )
                            delay(100)

                            _workoutDeletedResponse.postValue(Event(true))
                        }
                    }
                }
            }
        }

    }

    fun prepareDataForActivity(it: OWorkoutDetailsResponseModel): ArrayList<OWDActivityData> {
        val activityList = ArrayList<OWDActivityData>()
        val context = NoiseFitApplicationMain.context!!

        if (showDistance(it) && it.dataType != null) {
            if (it.calories != null && it.calories > 0) {
                activityList.add(
                    OWDActivityData(
                        context.getString(R.string.text_total_calories),
                        it.calories.toString(),
                        "kcal",
                    )
                )
            }
        }



        if (it.cadence != null && it.cadence > 0) {
            activityList.add(
                OWDActivityData(
                    context.getString(R.string.text_cadence),
                    it.cadence.toString(),
                    "spm",
                )
            )
        }

        if (it.hrMax != null && it.hrMax > 0) {
            activityList.add(
                OWDActivityData(
                    context.getString(R.string.text_max_hr),
                    it.hrMax.toString(),
                    "bpm",
                )
            )
        }

        if (it.hrLow != null && it.hrLow > 0) {
            activityList.add(
                OWDActivityData(
                    context.getString(R.string.text_min_hr),
                    it.hrLow.toString(),
                    "bpm",
                )
            )
        }


        if (it.steps != null && it.steps > 0) {
            activityList.add(
                OWDActivityData(
                    "Steps",
                    it.steps.toString(),
                    "",
                )
            )
        }

        if (it.recoveryTime != null && it.recoveryTime > 59) {
            val recoveryTimeMin = it.recoveryTime/60
            activityList.add(
                OWDActivityData(
                    context.getString(R.string.text_recovery_time),
                    ApplicationUtils.getActivityDurationFormat2(recoveryTimeMin),
                    "",
                )
            )
        }



        return activityList
    }


    private fun showDistance(data: OWorkoutDetailsResponseModel): Boolean {
        return if (data.dataType.equals("distance", true)) {
            if (data.dataPriority.equals("app", true)) {
                (data.gpsDistance ?: 0) > 0
            } else {
                data.distance != null && data.distance > 0L
            }
        } else {
            false
        }
    }

    fun getDistance(data: OWorkoutDetailsResponseModel): Triple<String, String, String> {
        if (showDistance(data)) {
            val distanceToUse =
                if (data.dataPriority.equals("app")) data.gpsDistance ?: 0 else data.distance
            val distance = dataUnitConverter.formatDistance(
                distanceToUse?.toInt() ?: 0, Units.METRIC
            )
            return Triple(distance, "km", "Total Distance")

        } else if (data.calories != null && data.calories > 0L) {
            return Triple(data.calories.toString(), "Kcal", "Total Calories")
        }

        return Triple("", "", "")
    }

    fun getDummyBreakUpDataForTimeDisplay(): ArrayList<Int> {
        val dummyList = ArrayList<Int>()
        for (i in 0..287) {
            dummyList.add(0)
        }
        return dummyList

    }

    fun getXAxisList(
        movementList: List<Int>?, startTime: String, endTime: String, duration: Long?
    ): List<String?> {
        if (movementList.isNullOrEmpty()) {
            return MutableList<String>(2, { "" }).apply {
                this[0] = startTime.lowercase()
                this[1] = endTime.lowercase()
            }.toList()
        }

        val list = arrayOfNulls<String>(movementList.size)

        val startTimeFull = DateFormats.convertTimeIntoTime(
            startTime, DateFormats.timeFormat12, DateFormats.timeFormat
        ).split(":")


        val startHr = startTimeFull[0].toInt()
        val originalStartMin = startTimeFull[1].toInt()
        var startMin = originalStartMin
        startMin = (5 * (Math.floor(Math.abs(startMin.toDouble() / 5)))).toInt()


        val endTimeFull = DateFormats.convertTimeIntoTime(
            endTime, DateFormats.timeFormat12, DateFormats.timeFormat
        ).split(":")


        val endHr = endTimeFull[0].toInt()

        val originalEndMin = endTimeFull[1].toInt()
        var endMin = originalEndMin
        endMin = (5 * (ceil(Math.abs(endMin.toDouble() / 5)))).toInt()


        val calendar = Calendar.getInstance()
        calendar.time = DateFormats.timeFormat.parse(String.format("%02d:%02d", startHr, startMin))
        val endTimeCalendar = Calendar.getInstance()
        endTimeCalendar.time =
            DateFormats.timeFormat.parse(String.format("%02d:%02d", endHr, endMin))

        var index = 0
        while (calendar.before(endTimeCalendar)) {
            try {
                list[index] = DateFormats.timeFormat12_2.format(calendar.time)
                calendar.add(Calendar.MINUTE, 5)
                index++
            } catch (exp: Exception) {
                exp.printStackTrace()
                calendar.add(Calendar.MINUTE, 5)
                index++
            }
        }


        return list.apply {
            this[0] = startTime.lowercase()
            this[movementList.size - 1] = endTime.lowercase()
        }.toList()
    }

    fun getCombinedMovement(movement: List<Int>): List<Int> {
        val list = ArrayList<Int>()

        val chunkSize = when (movement.size) {
            in 0..60 -> 2
            in 61..180 -> 4
            in 181..320 -> 10
            in 321..Int.MAX_VALUE -> 120
            else -> 1
        }

        val chunked = movement.chunked(chunkSize)
        chunked.forEach {
            val newList = it.map {
                if (it == 255) {
                    0
                } else it
            }
            val data = if(newList.isEmpty()) 0 else newList.average().ceilRound()
            list.add(data)
        }

        return list
    }


    private var zone1Indexes = ArrayList<Int>()
    private var zone2Indexes = ArrayList<Int>()
    private var zone3Indexes = ArrayList<Int>()
    private var zone4Indexes = ArrayList<Int>()
    private var zone5Indexes = ArrayList<Int>()
    private var zoneRestorativeIndexes = ArrayList<Int>()

    fun generateHrZones(hrValue: List<Int>): List<OWDActivityHRZoneData> {
        val zones = mutableMapOf<String, IntRange>()


        val hrIntervalInSecond = 30L
        val age = getUserAge()
        LOGS.d("MY_AGE $age")
        val HRmax = (208 - 0.7 * age)
        // Zone 1 (50-60%)
        val zone1Min = (0.5 * HRmax).toInt()
        val zone1Max = (0.6 * HRmax).toInt()
        zones["Zone 1"] = zone1Min..zone1Max

        // Zone 2 (60-70%)
        val zone2Max = (0.7 * HRmax).toInt()
        zones["Zone 2"] = zone1Max..zone2Max

        // Zone 3 (70-80%)
        val zone3Max = (0.8 * HRmax).toInt()
        zones["Zone 3"] = zone2Max..zone3Max

        // Zone 4 (80-90%)
        val zone4Max = (0.9 * HRmax).toInt()
        zones["Zone 4"] = zone3Max..zone4Max

        // Zone 5 (90-100%)
        val zone5Max = HRmax.toInt()
        zones["Zone 5"] = zone4Max..zone5Max


        hrValue.forEachIndexed { index, value ->
            when (value) {
                in zone1Min until zone1Max -> {
                    zone1Indexes.add(index)
                }

                in zone1Max until zone2Max -> {
                    zone2Indexes.add(index)
                }

                in zone2Max until zone3Max -> {
                    zone3Indexes.add(index)
                }

                in zone3Max until zone4Max -> {
                    zone4Indexes.add(index)
                }

                in zone4Max until zone5Max -> {
                    zone5Indexes.add(index)
                }

                else -> {
                    if (value != 0 && value != 255 && value < zone5Max) {
                        zoneRestorativeIndexes.add(index)
                    }
                }

            }
        }

        LOGS.d("khgkhgkgkk ${Gson().toJson(zones)}")


        val duration =
            zone1Indexes.size + zone2Indexes.size + zone3Indexes.size + zone4Indexes.size + zone5Indexes.size + zoneRestorativeIndexes.size

//        zones.forEach { (zone, range) ->
//            println("$zone: $range bpm zonesss")
//        }
//        LOGS.d("zonesss ${Gson().toJson(hrValue)}")
        return arrayListOf<OWDActivityHRZoneData>().apply {
            add(
                OWDActivityHRZoneData(

                    title = "Restorative zone",
                    range = "(<50%)",
                    zone = 0,
                    percentage = zoneRestorativeIndexes.size.toFloat()
                        .calculatePercentage(duration.toFloat()).toInt(),
                    duration = ApplicationUtils.getFormattedRecordedWorkoutFromSeconds(
                        zoneRestorativeIndexes.size * hrIntervalInSecond
                    ),
                    color = "#ACABAB",
                    dataSize = hrValue.size,
                    selectedIndexes = zoneRestorativeIndexes
                )
            )
            add(
                OWDActivityHRZoneData(

                    title = "Zone 1",
                    zone = 1,
                    range = "(50-60%)",
                    percentage = zone1Indexes.size.toFloat().calculatePercentage(duration.toFloat())
                        .toInt(),
                    duration = ApplicationUtils.getFormattedRecordedWorkoutFromSeconds(zone1Indexes.size * hrIntervalInSecond),
                    color = "#3485ff",
                    dataSize = hrValue.size,
                    selectedIndexes = zone1Indexes
                )
            )
            add(
                OWDActivityHRZoneData(

                    title = "Zone 2",
                    range = "(60-70%)",
                    zone = 2,
                    percentage = zone2Indexes.size.toFloat().calculatePercentage(duration.toFloat())
                        .toInt(),
                    duration = ApplicationUtils.getFormattedRecordedWorkoutFromSeconds(zone2Indexes.size * hrIntervalInSecond),
                    color = "#34f3ff",
                    dataSize = hrValue.size,
                    selectedIndexes = zone2Indexes
                )
            )
            add(
                OWDActivityHRZoneData(

                    title = "Zone 3",
                    range = "(70-80%)",
                    zone = 3,
                    percentage = zone3Indexes.size.toFloat().calculatePercentage(duration.toFloat())
                        .toInt(),
                    duration = ApplicationUtils.getFormattedRecordedWorkoutFromSeconds(zone3Indexes.size * hrIntervalInSecond),
                    color = "#48ff7b",
                    dataSize = hrValue.size,
                    selectedIndexes = zone3Indexes
                )
            )
            add(
                OWDActivityHRZoneData(
                    title = "Zone 4",
                    range = "(80-90%)",
                    zone = 4,
                    percentage = zone4Indexes.size.toFloat().calculatePercentage(duration.toFloat())
                        .toInt(),
                    duration = ApplicationUtils.getFormattedRecordedWorkoutFromSeconds(zone4Indexes.size * hrIntervalInSecond),
                    color = "#ff8934",
                    dataSize = hrValue.size,
                    selectedIndexes = zone4Indexes
                )
            )
            add(
                OWDActivityHRZoneData(
                    title = "Zone 5",
                    range = "(90-100%)",
                    zone = 5,
                    percentage = zone5Indexes.size.toFloat().calculatePercentage(duration.toFloat())
                        .toInt(),
                    duration = ApplicationUtils.getFormattedRecordedWorkoutFromSeconds(zone5Indexes.size * hrIntervalInSecond),
                    color = "#ff3434",
                    dataSize = hrValue.size,
                    selectedIndexes = zone5Indexes
                )
            )
        }
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


    fun getIndexList(zone: Int): Pair<List<Int>, Int> {
        return when (zone) {
            0 -> Pair(zoneRestorativeIndexes, Color.parseColor("#ACABAB"))//pending from design
            1 -> Pair(zone1Indexes, Color.parseColor("#3485ff"))
            2 -> Pair(zone2Indexes, Color.parseColor("#34f3ff"))
            3 -> Pair(zone3Indexes, Color.parseColor("#48ff7b"))
            4 -> Pair(zone4Indexes, Color.parseColor("#ff8934"))
            5 -> Pair(zone5Indexes, Color.parseColor("#ff3434"))
            else -> Pair(ArrayList(), Color.parseColor("#000000"))
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
    fun getWeatherImage(status: Int?, startTime: String?): Int {
        val isDay = try {
            val start = LocalDateTime.parse(startTime, DateTimeFormat.forPattern("HH:mm:ss"))
            val hourOFDay = start.hourOfDay
            hourOFDay in 6..19
        } catch (exp: Exception) {
            true
        }

        return when (status) {
            0 -> if (isDay) R.drawable.weather_clear else R.drawable.weather_clear_night
            2 -> R.drawable.weather_thunder
            3 -> R.drawable.weather_drizzle
            5 -> R.drawable.weather_rainy
            6 -> R.drawable.weather_snow
            7 -> R.drawable.weather_haze
            8 -> if (isDay) R.drawable.weather_cloudy else R.drawable.weather_cloudy_night
            else -> R.drawable.weather_haze
        }
    }
}