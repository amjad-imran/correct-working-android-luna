package com.oreo.ui.workout.add

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
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.OAddWorkout
import com.oreo.data.model.OWorkoutListModal
import com.oreo.data.repository.abstraction.OreoSyncRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class OAddWorkoutViewModel
@Inject
constructor(
    private val userActivityRepository: OreoUserActivityRepository,
    private val localDatSource: DataStoredInterface,
    private val syncRepository: OreoSyncRepository,
    val sessionManager: SessionManager
) : BaseViewModel() {

    val minimumWorkoutTime = 20
    val maxWorkoutTime = 180

    var autoSport = MutableLiveData<Boolean>()
    private val _addWorkoutResponse = MutableLiveData<Boolean>()
    val addWorkoutResponse = _addWorkoutResponse
    private val _oWorkoutListModalResponse = MutableLiveData<List<OWorkoutListModal>>()
    val oWorkoutListModalResponse: LiveData<List<OWorkoutListModal>> = _oWorkoutListModalResponse
    val updateDefaultWorkout = MutableLiveData<Event<OWorkoutListModal>>()


    var addWorkout = OAddWorkout()
    var workoutListModal: OWorkoutListModal? = null
    var activityType: String? = null
    var autoWorkoutId: Int? = null
    var movementList: List<Int>? = null
    var preFilledOreoAutoSportData: OreoAutoSportData? = null


    var isStartTimeSelected = false
    var isEndTimeSelected = false


    fun convertAutoSport(data: OreoAutoSportData?) {
        if (data == null) {
            return
        }

        preFilledOreoAutoSportData = data
        autoWorkoutId = data.id
        addWorkout.duration = TimeUnit.SECONDS.toMinutes(data.duration.toLong()).toInt()
        val endTime = DateFormats.addMinuteToTimeStamp(data.startTime, addWorkout.duration)
        addWorkout.calories = data.calories
        addWorkout.intensity = getIntensity(data.intensity ?: 0)
        addWorkout.steps = data.steps
        addWorkout.date = DateFormats.convertTimestampToDate(endTime, DateFormats.dateFormat3)
        activityType = "Walking"/*data.type*/

        tryCatch {
            val startTime =
                DateFormats.convertTimestampToDate(data.startTime, DateFormats.timeFormat)
            if (startTime.isNotEmpty()) {
                val startArray = startTime.split(":")
                addWorkout.startHour = startArray[0].toInt()
                addWorkout.startMinute = startArray[1].toInt()
            }
            val endTimeText = DateFormats.convertTimestampToDate(endTime, DateFormats.timeFormat)
            if (endTimeText.isNotEmpty()) {
                val endArray = endTimeText.split(":")
                addWorkout.endHour = endArray[0].toInt()
                addWorkout.endMinute = endArray[1].toInt()
            }
        }
        autoSport.postValue(true)
    }

    fun isDataSame(): Boolean {
        if (preFilledOreoAutoSportData == null) return false

        try {

            val isDurationEqual =
                addWorkout.duration == TimeUnit.SECONDS.toMinutes(preFilledOreoAutoSportData!!.duration.toLong())
                    .toInt()
            val isCaloriesEqual = addWorkout.calories == preFilledOreoAutoSportData!!.calories
            val isIntensityEqual =
                addWorkout.intensity == getIntensity(preFilledOreoAutoSportData!!.intensity ?: 0)

            var compareStartHour: Int = 0
            var compareStartMinute: Int = 0
            val startTime = DateFormats.convertTimestampToDate(
                preFilledOreoAutoSportData!!.startTime,
                DateFormats.timeFormat
            )
            if (startTime.isNotEmpty()) {
                val startArray = startTime.split(":")
                compareStartHour = startArray[0].toInt()
                compareStartMinute = startArray[1].toInt()
            }
            val endTime = DateFormats.addMinuteToTimeStamp(
                preFilledOreoAutoSportData!!.startTime,
                addWorkout.duration
            )
            var compareEndHour: Int = 0
            var compareEndMinute: Int = 0
            val endTimeText = DateFormats.convertTimestampToDate(endTime, DateFormats.timeFormat)
            if (endTimeText.isNotEmpty()) {
                val endArray = endTimeText.split(":")
                compareEndHour = endArray[0].toInt()
                compareEndMinute = endArray[1].toInt()
            }

            val isStartTimeEqual =
                compareStartHour == addWorkout.startHour && compareStartMinute == addWorkout.startMinute
            val isEndTimeEqual =
                compareEndHour == addWorkout.endHour && compareEndMinute == addWorkout.endMinute

            return isDurationEqual && isCaloriesEqual && isIntensityEqual && isStartTimeEqual && isEndTimeEqual
        } catch (exp: Exception) {
            exp.printStackTrace()
            return false
        }
    }

    fun addWorkout() {

        val type = if (workoutListModal?.activityType?.isNotEmpty() == true) {
            workoutListModal?.activityType
        } else {
            activityType
        }
        viewModelScope.launch {
            val requestObject = JsonObject().apply {
                this.addProperty("duration", addWorkout.duration)
                this.addProperty("calories", addWorkout.calories)
                this.addProperty("activity_type", type)

                val isAuto = autoSport.value != null

                if (isAuto) {
                    this.addProperty("type", if (isDataSame()) "auto" else "automanual")
                    this.addProperty("date", addWorkout.date)
                } else {
                    this.addProperty("type", "manual")
                }

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
                                        addWorkout()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            autoWorkoutId?.let {
                                deleteAutoSport(it)
                            }
                            _addWorkoutResponse.postValue(true)
                        }
                    }
                }
            }
        }
    }

    private fun deleteAutoSport(id: Int) {
        GlobalScope.launch {
            syncRepository.markWorkoutSynced(id).collect { resource ->
                when (resource) {
                    is CacheResult.GenericError -> {

                    }

                    is CacheResult.Success -> {

                    }
                }
            }
        }
    }

    fun getWorkoutDuration(): Int {

        if (!isStartTimeSelected || !isEndTimeSelected) {
            return 0
        }

        val diffInHours = addWorkout.endHour - addWorkout.startHour

        return (diffInHours * 60) + (addWorkout.endMinute - addWorkout.startMinute)
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

    fun getCaloriesBurnt(): Float {

        LOGS.d("getCaloriesBurnt ${addWorkout.duration} ${addWorkout.intensity} ${workoutListModal}")
        if (addWorkout.duration == 0) {
            return 0f
        }

        if (addWorkout.intensity.isEmpty()) {
            return 0f
        }

        if (workoutListModal == null) {
            return 0f
        }

        val weight = localDatSource.getUser()?.userInfo?.weight ?: 1

        when (addWorkout.intensity.lowercase()) {
            "easy" -> {
                return addWorkout.duration * (workoutListModal?.lowIntensity ?: 0f) * weight
            }

            "moderate" -> {
                return addWorkout.duration * (workoutListModal?.mediumIntensity ?: 0f) * weight
            }

            "hard" -> {
                return addWorkout.duration * (workoutListModal?.highIntensity ?: 0f) * weight
            }
        }
        return 0f
    }

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
                            if (postValue) {
                                _oWorkoutListModalResponse.postValue(it)
                            } else {
                                val walkingWorkout =
                                    it.find { it.activityType.equals("walking", true) }
                                if (autoSport.value == null) {

                                    walkingWorkout?.let { walk ->
                                        updateDefaultWorkout.postValue(Event(walk))
                                    }
                                } else {
                                    workoutListModal = walkingWorkout
                                }

                            }
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

    fun getHighlightedPoints(): HashSet<Int> {
        try {
            val startMinutes = addWorkout.startHour * 60 + addWorkout.startMinute
            val endMinutes = addWorkout.endHour * 60 + addWorkout.endMinute
            var start = startMinutes / 15
            val end = endMinutes / 15

            LOGS.d("getHighlightedPoints $start $end")
            return if (start == end) {
                hashSetOf(start)
            } else {
                val result = HashSet<Int>()
                while (start != end) {
                    result.add(start)
                    start++
                }
                result
            }

        } catch (exp: Exception) {
            return HashSet()
        }
    }
}