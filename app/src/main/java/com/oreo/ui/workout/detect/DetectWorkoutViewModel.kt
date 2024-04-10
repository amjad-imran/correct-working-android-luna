package com.oreo.ui.workout.detect

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit_commans.common.maxWithoutInvalidMovementValues
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.getParseList
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import com.oreo.data.model.OAddWorkout
import com.oreo.data.repository.abstraction.OreoSyncRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class DetectWorkoutViewModel
@Inject
constructor(
    private val syncRepository: OreoSyncRepository,
    private val sessionManager: SessionManager,
    private val userActivityRepository: OreoUserActivityRepository,
    private val userHealthDataDataSource: OreoUserHealthDataDataSource
) : BaseViewModel() {

    var dayKey: String = ""
    var movementList: List<Int>? = null
    private val _oreoAutoSportData =
        MutableLiveData<Pair<ArrayList<String>, LinkedHashMap<String, ArrayList<OreoAutoSportData>>>>()
    val oreoAutoSportData: LiveData<Pair<ArrayList<String>, LinkedHashMap<String, ArrayList<OreoAutoSportData>>>> =
        _oreoAutoSportData

    private val _dayTimeMovementList = MutableLiveData<List<Int>>()
    val dayTimeMovementList: LiveData<List<Int>> = _dayTimeMovementList
    val workoutDeleted = MutableLiveData<Event<Pair<Int, Int>>>()


    fun getNotAcceptingData() {
        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.getAutoWorkoutData().collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {
                        val hm = LinkedHashMap<String, ArrayList<OreoAutoSportData>>()


                        resource.value?.forEach {
                            val date = DateFormats.convertTimestampToDate(
                                it.startTime,
                                DateFormats.monthDateWithoutYear2
                            )



                            if (hm.containsKey(date)) {
                                val programmeList = hm[date]!!
                                programmeList.add(it)
                                hm[date] = programmeList
                            } else {
                                val programmeList = ArrayList<OreoAutoSportData>()
                                programmeList.add(it)
                                hm[date] = programmeList

                            }
                        }

                        val dateList = ArrayList<String>()
                        hm.forEach {
                            dateList.add(it.key)
                        }


                        dateList.reverse()
                        _oreoAutoSportData.postValue(Pair(dateList, hm))
                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }

        }
    }

    fun getDayTimeMovement(date: String) {

        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.getMovementData(date).collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {
                        _dayTimeMovementList.postValue(resource.value.getParseList())
                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }

        }
    }


    fun deleteAutoSport(id: Int, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.deleteAutoWorkoutData(id).collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {
                        workoutDeleted.postValue(Event(Pair(id, position)))
                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }

        }
    }

    fun markWorkoutSynced(id: Int, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.markWorkoutSynced(id).collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {
                        workoutDeleted.postValue(Event(Pair(id, position)))
                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }

        }
    }

    private fun getIntensity(intensity: Int): String {
        return when (intensity) {
            0 -> {
                "Easy"
            }

            1 -> {
                "Moderate"
            }

            else -> {
                "Hard"
            }
        }
    }

    fun addWorkout(data: OreoAutoSportData, onAddSuccess: (id: String) -> Unit) {
        val addWorkout = OAddWorkout().apply {
            duration = TimeUnit.SECONDS.toMinutes(data.duration.toLong()).toInt()
            val endTime = DateFormats.addMinuteToTimeStamp(data.startTime, duration)
            calories = data.calories
            intensity = getIntensity(data.intensity ?: 0)
            steps = data.steps
            date = DateFormats.convertTimestampToDate(endTime, DateFormats.dateFormat3)

            tryCatch {
                val startTime =
                    DateFormats.convertTimestampToDate(data.startTime, DateFormats.timeFormat)
                if (startTime.isNotEmpty()) {
                    val startArray = startTime.split(":")
                    startHour = startArray[0].toInt()
                    startMinute = startArray[1].toInt()
                }
                val endTimeText =
                    DateFormats.convertTimestampToDate(endTime, DateFormats.timeFormat)
                if (endTimeText.isNotEmpty()) {
                    val endArray = endTimeText.split(":")
                    endHour = endArray[0].toInt()
                    endMinute = endArray[1].toInt()
                }
            }

            startTimeIn24H = DateFormats.formatTime(
                startHour,
                startMinute
            )
            endTimeIn24H = DateFormats.formatTime(
                endHour,
                endMinute
            )

        }


        viewModelScope.launch {
            val requestObject = JsonObject().apply {
                this.addProperty("duration", addWorkout.duration)
                this.addProperty("calories", addWorkout.calories)
                this.addProperty("activity_type", data.type)
                this.addProperty("type", "auto")
                this.addProperty("date", addWorkout.date)
                this.addProperty("start_time", addWorkout.startTimeIn24H)
                this.addProperty("steps", addWorkout.steps)
                this.addProperty("end_time", addWorkout.endTimeIn24H)
                this.addProperty("intensity", addWorkout.intensity)
            }
            userActivityRepository.addWorkout(
                requestObject
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
                                        addWorkout(data, onAddSuccess)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                            addWorkout.date?.let {
                                userHealthDataDataSource.clearDataByDates(listOf(it))
                            }
                            val sportObj = SportsModeResponse(
                                date = addWorkout.date,
                                distance = 0,
                                duration = addWorkout.duration.toLong() * 60,
                                calories = addWorkout.calories.toLong(),
                                heartRateCurrent = 0,
                                steps = addWorkout.steps,
                                type = data.type,
                                time = "${addWorkout.date} ${addWorkout.startTimeIn24H}"
                            )
                            sessionManager.saveSportsActivities(listOf(sportObj))

                            delay(100)
                            it.id?.let { it1 -> onAddSuccess.invoke(it1) }
                        }
                    }
                }
            }
        }
    }

    fun getCombinedMovementData(
        originalList: List<Int>?
    ): List<Int> {
        if (originalList.isNullOrEmpty()) {
            return MutableList(96) { 0 }
        }
        val combinedList = ArrayList<Int>()
        for (i in originalList.indices step 3) {
            val endIndex = i + 3
            if (endIndex <= originalList.size) {
                val max = originalList.subList(i, endIndex).maxWithoutInvalidMovementValues()
                combinedList.add(max)
            }
        }
        return combinedList
    }

    fun getWorkoutPointsList(oreoAutoSportData: List<OreoAutoSportData>): List<String?> {
        val list = MutableList<String?>(96) { null }

        var counter = 1

        oreoAutoSportData.reversed().forEach {

            val minutes = minutesSinceMidnight(it.startTime)
            val interval = (minutes / 5) / 3

            list[interval] = "$counter"
            counter++
        }

        return list

    }

    fun minutesSinceMidnight(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return hour * 60 + minute
    }

}