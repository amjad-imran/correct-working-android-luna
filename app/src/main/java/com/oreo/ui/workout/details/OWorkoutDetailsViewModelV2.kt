package com.oreo.ui.workout.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import com.oreo.data.model.OWDActivityData
import com.oreo.data.model.OWDActivityHRZoneData
import com.oreo.data.model.OWorkoutDetailsResponseModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    var hrSelectedPosition = -1
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
        if (it.calories != null && it.calories > 0) {
            activityList.add(
                OWDActivityData(
                    context.getString(R.string.text_total_calories),
                    it.calories.toString(),
                    "kcal",
                )
            )
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

        if (it.recoveryTime != null && it.recoveryTime > 0) {
            activityList.add(
                OWDActivityData(
                    context.getString(R.string.text_recovery_time),
                    ApplicationUtils.getActivityDurationFormat2(it.recoveryTime),
                    "",
                )
            )
        }



        return activityList
    }

    fun getDistance(data: OWorkoutDetailsResponseModel): Triple<String, String, String> {
        if (data.distance != null && data.distance > 0L) {

            val distance = dataUnitConverter.formatDistance(
                data.distance.toInt(),
                Units.METRIC
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
        movementList: List<Int>?,
        startTime: String,
        endTime: String,
        duration: Long?
    ): List<String?> {
        if (movementList.isNullOrEmpty()) {
            return MutableList<String>(2, { "" })
                .apply {
                    this[0] = startTime.lowercase()
                    this[1] =
                        endTime.lowercase()
                }
                .toList()
        }

        val list = arrayOfNulls<String>(movementList.size)

        val startTimeFull =
            DateFormats.convertTimeIntoTime(
                startTime,
                DateFormats.timeFormat12,
                DateFormats.timeFormat
            ).split(":")


        val startHr = startTimeFull[0].toInt()
        val originalStartMin = startTimeFull[1].toInt()
        var startMin = originalStartMin
        startMin = (5 * (Math.floor(Math.abs(startMin.toDouble() / 5)))).toInt()


        val endTimeFull =
            DateFormats.convertTimeIntoTime(
                endTime,
                DateFormats.timeFormat12,
                DateFormats.timeFormat
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


        return list
            .apply {
                this[0] = startTime.lowercase()
                this[movementList.size - 1] =
                    endTime.lowercase()
            }
            .toList()
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
            val data = newList.average().ceilRound()
            list.add(data)
        }

        return list
    }



    fun generateHrZones(hrValue: List<Int>): List<OWDActivityHRZoneData> {
        val zones = mutableMapOf<String, IntRange>()


        val hrIntervalInSecond = 30L
        val age = getUserAge()
        val HRmax = (208 - 0.7 * age)
        // Zone 1 (50-60%)
        val zone1Min = (0.5 * HRmax).toInt()
        val zone1Max = (0.6 * HRmax).toInt()
        zones["Zone 1"] = zone1Min..zone1Max

        // Zone 2 (60-70%)
        val zone2Min = (0.6 * HRmax).toInt()
        val zone2Max = (0.7 * HRmax).toInt()
        zones["Zone 2"] = zone2Min..zone2Max

        // Zone 3 (70-80%)
        val zone3Min = (0.7 * HRmax).toInt()
        val zone3Max = (0.8 * HRmax).toInt()
        zones["Zone 3"] = zone3Min..zone3Max

        // Zone 4 (80-90%)
        val zone4Min = (0.8 * HRmax).toInt()
        val zone4Max = (0.9 * HRmax).toInt()
        zones["Zone 4"] = zone4Min..zone4Max

        // Zone 5 (90-100%)
        val zone5Min = (0.9 * HRmax).toInt()
        val zone5Max = HRmax.toInt()
        zones["Zone 5"] = zone5Min..zone5Max

        var zone1Frequency = 0L
        var zone2Frequency = 0L
        var zone3Frequency = 0L
        var zone4Frequency = 0L
        var zone5Frequency = 0L
        var zoneRestorativeFrequency = 0

        hrValue.forEach {
            when (it) {
                in zone1Min until zone1Max -> {
                    zone1Frequency += 1
                }

                in zone2Min until zone2Max -> {
                    zone2Frequency += 1
                }

                in zone3Min until zone3Max -> {
                    zone3Frequency += 1
                }

                in zone4Min until zone4Max -> {
                    zone4Frequency += 1
                }

                in zone5Min until zone5Max -> {
                    zone5Frequency += 1
                }

                else -> {
                    zoneRestorativeFrequency += 1
                }

            }
        }


        val duration =
            zone1Frequency + zone2Frequency + zone3Frequency + zone4Frequency + zone5Frequency + zoneRestorativeFrequency

//        zones.forEach { (zone, range) ->
//            println("$zone: $range bpm zonesss")
//        }
//        LOGS.d("zonesss ${Gson().toJson(hrValue)}")
        return arrayListOf<OWDActivityHRZoneData>().apply {
            add(
                OWDActivityHRZoneData(
                    isHighlighted = true,
                    title = "Restorative zone",
                    range = "<50%",
                    percentage = zoneRestorativeFrequency.toFloat()
                        .calculatePercentage(duration.toFloat()).toInt(),
                    duration = ApplicationUtils.getActivityDurationFormat2(zoneRestorativeFrequency * hrIntervalInSecond),
                    color = "#34f3ff",
                )
            )
            add(
                OWDActivityHRZoneData(
                    isHighlighted = true,
                    title = "Zone 1",
                    range = "(50-60%)",
                    percentage = zone1Frequency.toFloat().calculatePercentage(duration.toFloat())
                        .toInt(),
                    duration = ApplicationUtils.getActivityDurationFormat2(zone1Frequency * hrIntervalInSecond),
                    color = "#3485ff",
                )
            )
            add(
                OWDActivityHRZoneData(
                    isHighlighted = true,
                    title = "Zone 2",
                    range = "(60-70%)",
                    percentage = zone2Frequency.toFloat().calculatePercentage(duration.toFloat())
                        .toInt(),
                    duration = ApplicationUtils.getActivityDurationFormat2(zone2Frequency * hrIntervalInSecond),
                    color = "#34f3ff",
                )
            )
            add(
                OWDActivityHRZoneData(
                    isHighlighted = true,
                    title = "Zone 3",
                    range = "(70-80%)",
                    percentage = zone3Frequency.toFloat().calculatePercentage(duration.toFloat())
                        .toInt(),
                    duration = ApplicationUtils.getActivityDurationFormat2(zone3Frequency * hrIntervalInSecond),
                    color = "#48ff7b",
                )
            )
            add(
                OWDActivityHRZoneData(
                    isHighlighted = true,
                    title = "Zone 4",
                    range = "(80-90%)",
                    percentage = zone4Frequency.toFloat().calculatePercentage(duration.toFloat())
                        .toInt(),
                    duration = ApplicationUtils.getActivityDurationFormat2(zone4Frequency * hrIntervalInSecond),
                    color = "#ff8934",
                )
            )
            add(
                OWDActivityHRZoneData(
                    isHighlighted = true,
                    title = "Zone 5",
                    range = "(90-100%)",
                    percentage = zone5Frequency.toFloat().calculatePercentage(duration.toFloat())
                        .toInt(),
                    duration = ApplicationUtils.getActivityDurationFormat2(zone5Frequency * hrIntervalInSecond),
                    color = "#ff3434",
                )
            )

        }
    }


//    fun generateHrZones(hrValue: List<Int>): List<OWDActivityHRZoneData> {
//
//        val heartRateZones = calculateHeartRateZones(hrValue)
//
//        heartRateZones.forEach { (zone, range) ->
//            println("$zone: $range bpm lkjsdlkjadlkdsajlk")
//        }
//
//        val age = getUserAge()
//        val mhr = (208 - 0.7 * age)
//
//
//        return arrayListOf<OWDActivityHRZoneData>().apply {
//            add(
//                OWDActivityHRZoneData(
//                    title = "Restorative zone",
//                    range = "${getHrValue(mhr, 50)}%",
//                    percentage = 10,
//                    duration = "00:10",
//                    color = "#34f3ff",
//                )
//            )
//            add(
//                OWDActivityHRZoneData(
//                    title = "Zone 1",
//                    range = "<60%",
//                    percentage = 30,
//                    duration = "00:30",
//                    color = "#3485ff",
//                )
//            )
//            add(
//                OWDActivityHRZoneData(
//                    title = "Zone 2",
//                    range = "60-70%",
//                    percentage = 20,
//                    duration = "00:10",
//                    color = "#34f3ff",
//                )
//            )
//            add(
//                OWDActivityHRZoneData(
//                    title = "Zone 3",
//                    range = "60-70%",
//                    percentage = 10,
//                    duration = "00:10",
//                    color = "#34f3ff",
//                )
//            )
//            add(
//                OWDActivityHRZoneData(
//                    title = "Zone 4",
//                    range = "60-70%",
//                    percentage = 10,
//                    duration = "00:10",
//                    color = "#34f3ff",
//                )
//            )
//            add(
//                OWDActivityHRZoneData(
//                    title = "Zone 5",
//                    range = "60-70%",
//                    percentage = 10,
//                    duration = "00:10",
//                    color = "#34f3ff",
//                )
//            )
//
//        }
//    }

    fun getUserAge(): Int {
        return localDataStore.getUser()?.userInfo?.age ?: 30
    }
}