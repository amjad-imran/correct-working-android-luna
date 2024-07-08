package com.oreo.ui.workout.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.common.ceilRound
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import com.oreo.data.model.OWorkoutDetailsResponseModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class OWorkoutDetailsViewModel @Inject constructor(
    val userActivityRepository: OreoUserActivityRepository,
    private val userHealthDataDataSource: OreoUserHealthDataDataSource
) : BaseViewModel() {

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
                DateFormats.timeFormat12(),
                DateFormats.timeFormat()
            ).split(":")


        val startHr = startTimeFull[0].toInt()
        val originalStartMin = startTimeFull[1].toInt()
        var startMin = originalStartMin
        startMin = (5 * (Math.floor(Math.abs(startMin.toDouble() / 5)))).toInt()


        val endTimeFull =
            DateFormats.convertTimeIntoTime(
                endTime,
                DateFormats.timeFormat12(),
                DateFormats.timeFormat()
            ).split(":")


        val endHr = endTimeFull[0].toInt()

        val originalEndMin = endTimeFull[1].toInt()
        var endMin = originalEndMin
        endMin = (5 * (ceil(Math.abs(endMin.toDouble() / 5)))).toInt()


        val calendar = Calendar.getInstance()
        calendar.time = DateFormats.timeFormat().parse(String.format(locale = Locale.US,"%02d:%02d", startHr, startMin))
        val endTimeCalendar = Calendar.getInstance()
        endTimeCalendar.time =
            DateFormats.timeFormat().parse(String.format(locale = Locale.US,"%02d:%02d", endHr, endMin))

        var index = 0
        while (calendar.before(endTimeCalendar)) {
            try {
                list[index] = DateFormats.timeFormat12_2().format(calendar.time)
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
            val data = if(newList.isEmpty()) 0 else newList.average().ceilRound()
            list.add(data)
        }

        return list
    }
}