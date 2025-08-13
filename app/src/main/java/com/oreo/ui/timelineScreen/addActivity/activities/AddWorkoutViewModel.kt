package com.oreo.ui.timelineScreen.addActivity.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.OWorkoutListModal
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AddWorkoutViewModel @Inject constructor(
    private val userActivityRepository: OreoUserActivityRepository,
    val resourcesProvider: ResourcesProvider,
    private val userHealthDataDataSource: OreoUserHealthDataDataSource,
    val sessionManager: SessionManager,

    ) : BaseViewModel() {

    val defaultMinutes = 10L


    val onAddSuccess = MutableLiveData<Event<Boolean>>()

    private val _addWorkoutResponse = MutableLiveData<Pair<SportsModeResponse, String>>()

    private val _oWorkoutListModalResponse = MutableLiveData<List<OWorkoutListModal>>()
    val oWorkoutListModalResponse: LiveData<List<OWorkoutListModal>> = _oWorkoutListModalResponse
    val addWorkoutResponse = _addWorkoutResponse

    var selectedWorkout: OWorkoutListModal? = null
    var userDayData: ServerUserHealthData? = null

    var updateCalculatedData = MutableLiveData<Event<Boolean>>()

    var workoutDate = MutableLiveData<LocalDate>(LocalDate.now())
    var startTime = MutableLiveData<LocalTime>(LocalTime.now().minusMinutes(defaultMinutes))
    var duration = MutableLiveData<Long>(defaultMinutes)//in minutes
    var intensity = MutableLiveData<Int>(0)
    var calories = MutableLiveData<Int>(0)


    fun getWorkoutList(postValue: Boolean) {
        viewModelScope.launch {

            userActivityRepository.getWorkoutList().collect { resource ->
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
                                        getWorkoutList(postValue)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            val walkingWorkout =
                                it.find { it.activityType.equals("walking", true) }

                            selectedWorkout = walkingWorkout
                            updateCalculatedData.postValue(Event(true))

                        }
                    }
                }
            }
        }


    }

    fun addWorkout() {

        val type = selectedWorkout?.activityType



        val startHour = this.startTime.value!!.format(DateTimeFormatter.ofPattern("HH:mm"))
        val endHour = this.startTime.value!!.plusMinutes(duration.value!!)!!.format(DateTimeFormatter.ofPattern("HH:mm"))

        val existMessage = if (workoutDate.value?.equals(LocalDate.now()) == true)
            checkIfAnyEventExists(startHour, endHour) else null

        if (existMessage.isNullOrEmpty().not()) {
            sendMessage(existMessage)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val requestObject = JsonObject().apply {
                this.addProperty("duration", duration.value!!)
                this.addProperty("calories", calories.value)
                this.addProperty("activity_type", type)

                /*val isAuto = autoSport.value != null

                if (isAuto) {
                    if (isDataSame()) {
                        this.addProperty("type", "auto")
                    } else {
                        this.addProperty("type", "automanual")
                        this.addProperty("extra_calories", addWorkout.extraCalories)
                    }

                    this.addProperty("date", addWorkout.date)
                } else {*/
                    this.addProperty("type", "manual")
                    this.addProperty("date", workoutDate.value!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                /*}*/

                this.addProperty("start_time", startHour)
                //this.addProperty("steps", addWorkout.steps)
                this.addProperty("end_time", endHour)
                this.addProperty("intensity", intensity.value)
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
                                        addWorkout()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {


                            val date = /*if (isAuto) {*/
                                workoutDate.value?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: DateFormats.getTodaysDateString(10)
                            /*} else {
                                DateFormats.getTodaysDateString(10)
                            }*/

                            userHealthDataDataSource.clearDataByDates(listOf(date))
                            delay(100)

                            val sportObj = SportsModeResponse(
                                date = date,
                                distance = 0,
                                duration = duration.value!! * 60,
                                calories = calories.value!!.toLong(),
                                heartRateCurrent = 0,
                                steps = 0,
                                type = type,
                                time = "$date ${startHour}"
                            )
                            if (it.id != null) _addWorkoutResponse.postValue(Pair(sportObj, it.id))
                        }
                    }
                }
            }
        }
    }

    fun checkIfAnyEventExists(startTime: String, endTime: String): String? {

        LOGS.d(
            "checkIfAndEventExists() called with: startTime = $startTime, endTime = $endTime"
        )

        if (userDayData == null) return null

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

        val workoutStartTime = LocalDateTime.parse("${userDayData!!.date} $startTime:00", formatter)
        val workoutEndTime = LocalDateTime.parse("${userDayData!!.date} $endTime:00", formatter)


        var hasOverlappingWorkout = false
        var workoutName = "Workout"
        userDayData?.activity?.workout?.forEach {
            LOGS.d("checkIfAndEventExists   Workouts->  ${it.startTime}  ${it.endTime}")

            val wStartTime = LocalDateTime.parse("${it.date} ${it.startTime}", formatter)
            val wEndTime = LocalDateTime.parse("${it.date} ${it.endTime}", formatter)

            if (workoutStartTime in wStartTime..wEndTime || workoutEndTime in wStartTime..wEndTime) {
                hasOverlappingWorkout = true
                workoutName = it.getTranslatedActivityName()
                return@forEach
            }

            if (wStartTime in workoutStartTime..workoutEndTime || wEndTime in workoutStartTime..workoutEndTime) {
                workoutName = it.getTranslatedActivityName()
                hasOverlappingWorkout = true
                return@forEach
            }
        }
        if (hasOverlappingWorkout) {
            return resourcesProvider.getString(
                R.string.text_valuein_this_time_frame_already_exists,
                workoutName
            )
        }

        var hasOverlappingNap = false
        userDayData?.sleep?.naps?.forEach {
            LOGS.d("checkIfAndEventExists   Naps->  ${it.startTime}  ${it.endTime}")

            val wStartTime = LocalDateTime.parse(it.startTime, formatter)
            val wEndTime = LocalDateTime.parse(it.endTime, formatter)

            if (workoutStartTime in wStartTime..wEndTime || workoutEndTime in wStartTime..wEndTime) {
                hasOverlappingNap = true
                return@forEach
            }

            if (wStartTime in workoutStartTime..workoutEndTime || wEndTime in workoutStartTime..workoutEndTime) {
                hasOverlappingNap = true
                return@forEach
            }
        }
        if (hasOverlappingNap) {
            return resourcesProvider.getString(R.string.text_nap_in_this_time_frame_already_exists)
        }

        var hasOverlappingSleep = false
        userDayData?.sleep?.let {

            val sleepStart = it.hourly_breakup?.firstOrNull()?.start_time
            val sleepEnd = it.hourly_breakup?.lastOrNull()?.end_time

            if (sleepStart != null && sleepEnd != null) {
                LOGS.d("checkIfAndEventExists   Sleep->  ${sleepStart}  ${sleepEnd}")
                val wStartTime = LocalDateTime.parse(sleepStart, formatter)
                val wEndTime = LocalDateTime.parse(sleepEnd, formatter)

                if (workoutStartTime in wStartTime..wEndTime || workoutEndTime in wStartTime..wEndTime) {
                    hasOverlappingSleep = true
                }

                if (wStartTime in workoutStartTime..workoutEndTime || wEndTime in workoutStartTime..workoutEndTime) {
                    hasOverlappingSleep = true
                }
            }
        }

        if (hasOverlappingSleep) {
            return resourcesProvider.getString(R.string.text_sleep_in_this_time_frame_already_exists)
        }
        return null
    }


}