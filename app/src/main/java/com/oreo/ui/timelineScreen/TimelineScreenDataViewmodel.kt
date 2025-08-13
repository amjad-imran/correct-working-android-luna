package com.oreo.ui.timelineScreen

import androidx.core.graphics.toColorInt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.timeline.ItemTimelineResponseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TimelineScreenDataViewmodel @Inject constructor(
    private val userRepository: UserRepository,
): BaseViewModel() {

    var date: String? = null

    val activityListData = MutableLiveData<ArrayList<ItemTimelineResponseModel>>()

    companion object{
        val SLEEP_KEY = "sleep"
        val NAP_KEY = "nap"
        val WORKOUT_KEY = "workout"
        val WATER_CONSUMPTION_KEY = "hydration"
        val CAFFEINE_INTAKE_KEY = "caffeine"
        val MEAL_INTAKE_KEY_KEY = "meal"
        val LIGHT_EXPOSURE_KEY = "light-exposure"
        val PERIOD_STARTED_KEY = "period"
        val ACTIVITY_KEY = "activity"
    }

    fun getCurrDayActivities(date: String){
        viewModelScope.launch {
            userRepository.getCurrDayTimelineActivitiesData(date).collect{ resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {

                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            it.timeTracker?.let { dataList ->
                                dataList.map { obj ->
                                    obj.event?.let {
                                        getActivityTitleColorAndDesc(obj)
                                    }
                                }
                                activityListData.postValue(ArrayList(dataList))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getActivityTitleColorAndDesc(data: ItemTimelineResponseModel) {
        data.displayTime = convertTimeFormat(data.startTime)
        when(data.event){
            SLEEP_KEY -> {
                data.titleColor = "#A8A8ED".toColorInt()
                data.desc = getSleepDuration(data.startDate, data.startTime, data.endDate, data.endTime)
            }

            NAP_KEY -> {
                data.titleColor = "#A8A8ED".toColorInt()
                data.desc = getSleepDuration(data.startDate, data.startTime, data.endDate, data.endTime)
            }

            WORKOUT_KEY -> {
                data.titleColor = "#78C3F9".toColorInt()
                data.value?.let {
                    data.desc = it
                    data.unit?.let { data.desc += " $it" }
                }
            }

            WATER_CONSUMPTION_KEY -> {
                data.titleColor = "#8EF1C3".toColorInt()
                data.value?.let {
                    data.desc = it
                    data.unit?.let { data.desc += " $it" }
                }
            }

            CAFFEINE_INTAKE_KEY -> {
                data.titleColor = "#DCA58E".toColorInt()
                data.value?.let {
                    data.desc = it
                    data.unit?.let { data.desc += " $it" }
                }
            }

            MEAL_INTAKE_KEY_KEY -> {
                data.titleColor = "#FFE3B2".toColorInt()
            }

            LIGHT_EXPOSURE_KEY -> {
                data.titleColor = "#FFE1CF".toColorInt()
                data.value?.let {
                    data.desc = it
                    data.unit?.let { data.desc += " $it" }
                }
            }

            PERIOD_STARTED_KEY -> {
                data.titleColor = "#F18EBD".toColorInt()
            }

            ACTIVITY_KEY -> {
                data.titleColor = "#FFFFFF".toColorInt()
            }

            else -> {}
        }
    }

    private fun convertTimeFormat(time: String?): String {
        return try{
            val originalFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val timeObj = LocalTime.parse(time, originalFormatter)
            val newFormatter = DateTimeFormatter.ofPattern("HH:mm")

            timeObj.format(newFormatter)
        }catch (e: Exception){
            LOGS.e("TIMELINE_convertTimeFormat_EXCEPTION : $e")
            "-"
        }
    }

    fun getSleepDuration(
        startDate: String?,
        startTime: String?,
        endDate: String?,
        endTime: String?
    ): String {
        return try {
            // Define the format for time
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val timeFormatter12Hour = DateTimeFormatter.ofPattern("h:mm a")

            // Parse the start and end dates into LocalDate objects
            val startDateObj = LocalDateTime.parse("$startDate $startTime", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val endDateObj = LocalDateTime.parse("$endDate $endTime", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            // If the end time is before the start time, adjust the end time to the next day
            val adjustedEndDateObj = if (endDateObj.isBefore(startDateObj)) {
                endDateObj.plusDays(1)
            } else {
                endDateObj
            }

            // Calculate the duration between start and end times
            val duration = Duration.between(startDateObj, adjustedEndDateObj)
            val hours = duration.toHours()
            val minutes = duration.toMinutes() % 60

            // Format the start and end times into 12-hour AM/PM format
            val formattedStartTime = startDateObj.format(timeFormatter12Hour)
            val formattedEndTime = adjustedEndDateObj.format(timeFormatter12Hour)

            // Return the formatted result
            "$hours hr $minutes m; $formattedStartTime - $formattedEndTime"
        }catch (e: Exception){
            LOGS.e("TIMELINE_GET_SLEEP_DURATION_EXCEPTION : $e")
            "-"
        }
    }

}