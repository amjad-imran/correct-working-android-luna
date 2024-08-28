package com.oreo.ui.sleep2.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.OAddSleep
import com.oreo.data.repository.abstraction.OreoSyncRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OAddSleepViewModel
@Inject
constructor(
    private val userActivityRepository: OreoUserActivityRepository,
    private val resourcesProvider: ResourcesProvider,
    private val oreoStepsDataImpl: OreoSyncRepository,
) :
    BaseViewModel() {

    var isStartTimeSelected = false
    var isEndTimeSelected = false

    private var totalDuration = 0L
    var startTimeSleep = OAddSleep()
    var endTimeSleep = OAddSleep()


    private val _addSleepResponse =
        MutableLiveData<Event<Boolean>>()//todo return type will change once finalized
    val addSleepResponse = _addSleepResponse

    fun getSleepDuration(): Long {
        if (!isStartTimeSelected || !isEndTimeSelected) {
            return 0
        }

        val startTime = if (startTimeSleep.day.equals("Today", true)) {
            "${LocalDate.now()} ${String.format(locale = Locale.US, "%02d:%02d", startTimeSleep.hour.toInt(),
                startTimeSleep.minute.toInt())}"
        } else {
            "${
                LocalDate.now().minusDays(1)
            } ${String.format(locale = Locale.US, "%02d:%02d", startTimeSleep.hour.toInt(), startTimeSleep.minute.toInt())}"
        }

        val endTime = if (endTimeSleep.day.equals("Today", true)) {
            "${LocalDate.now()} ${String.format(locale = Locale.US, "%02d:%02d", endTimeSleep.hour.toInt(), 
                endTimeSleep.minute.toInt())}"
        } else {
            "${
                LocalDate.now().minusDays(1)
            } ${String.format(locale = Locale.US, "%02d:%02d", endTimeSleep.hour.toInt(),
                endTimeSleep.minute.toInt())}"
        }

        totalDuration = Duration.between(
            LocalDateTime.parse(
                startTime,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            ), LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        ).toSeconds()
        return totalDuration
    }

    fun callApiToAddSleep() {
        viewModelScope.launch {

            oreoStepsDataImpl.getTodaySteps().collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        val activeCalories = resource.value?.activeCalories ?: 0

                        val jsonObject = JsonObject()


                        val startTime = if (startTimeSleep.day.equals("Today", true)) {
                            "${LocalDate.now()} ${String.format(locale = Locale.US, "%02d:%02d", startTimeSleep.hour.toInt(),
                                startTimeSleep.minute.toInt())}"
                        } else {
                            "${
                                LocalDate.now().minusDays(1)
                            } ${String.format(locale = Locale.US, "%02d:%02d", startTimeSleep.hour.toInt(),
                                startTimeSleep.minute.toInt())}"
                        }

                        val endTime = if (endTimeSleep.day.equals("Today", true)) {
                            "${LocalDate.now()} ${String.format(locale = Locale.US, "%02d:%02d", endTimeSleep.hour.toInt(),
                                endTimeSleep.minute.toInt())}"
                        } else {
                            "${
                                LocalDate.now().minusDays(1)
                            } ${String.format(locale = Locale.US, "%02d:%02d", endTimeSleep.hour.toInt(),
                                endTimeSleep.minute.toInt())}"
                        }
                        val date = DateFormats.getCurrentDate(DateFormats.dateFormat3())
                        jsonObject.addProperty(
                            "date",
                            date
                        )
                        jsonObject.addProperty("end_time", endTime)
                        jsonObject.addProperty("start_time", startTime)
                        jsonObject.addProperty("total_duration", totalDuration)
                        jsonObject.addProperty("active_calories", activeCalories)

                        val jsonFinalObject = JsonObject()
                        jsonFinalObject.add("day_break_up", jsonObject)


                        val jsonArray = JsonArray()
                        jsonArray.add(jsonFinalObject)


                        LOGS.d("fdgkdfjgkdfg ${Gson().toJson(jsonArray)}")
                        return@collect
                        userActivityRepository.addManualSleep(
                            jsonArray,date
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
                                                    callApiToAddSleep()
                                                }

                                                override fun no() {

                                                }
                                            }
                                    })
                                }

                                is Resource.Success -> {
                                    resource.data?.data?.let {
                                        _addSleepResponse.postValue(Event(true))
                                    }
                                }
                            }
                        }

                    }

                    is CacheResult.GenericError -> {
                        sendMessage("Something went wrong")
                    }
                }
            }


        }

    }

    fun isStartDateToday(): Boolean {
        return startTimeSleep.day.equals("Today", true)
    }


}