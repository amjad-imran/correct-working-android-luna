package com.oreo.ui.sleep2.sleepplanner

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Shader.TileMode
import android.text.TextPaint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.material.timepicker.MaterialTimePicker
import com.noisefit.data.base.ResourcesProvider
import com.oreo.data.model.AlarmDataModel
import com.noisefit.data.model.SAActiveDayDataModel
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.databinding.FragmentSetAlarmBinding
import com.noisefit.timepickerslider.TimeRangePicker
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.model.SleepPlannerData
import com.oreo.data.model.TimeDataModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalTime
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class SetAlarmViewModel @Inject constructor(
    private val userActivityRepository: OreoUserActivityRepository,
    private val resourcesProvider: ResourcesProvider,
) : BaseViewModel() {

    var lastSelectedPosition: Int?=null

    var selectedAlarmDays = ArrayList<String>()
    var alarmSound: String? = null
    var alarmTimeUpdate = MutableLiveData<Event<Pair<AlarmDataModel, Int>>>()

    var startEndTime = MutableLiveData<Pair<LocalTime, LocalTime>>()

    val sleepPlannerCard = MutableLiveData<SleepPlannerData?>()


    init {
        startEndTime.postValue(
            Pair(
                LocalTime.of(22, 0),
                LocalTime.of(6, 0)
            )
        )
    }

    fun getAlarmData(): ArrayList<SAActiveDayDataModel> {
        val listData = ArrayList<SAActiveDayDataModel>()
        listData.add(SAActiveDayDataModel("M", false, true, 0))
        listData.add(SAActiveDayDataModel("T", false, false, 1))
        listData.add(SAActiveDayDataModel("W", false, false, 2))
        listData.add(SAActiveDayDataModel("T", false, false, 3))
        listData.add(SAActiveDayDataModel("F", false, false, 4))
        listData.add(SAActiveDayDataModel("S", false, false, 5))
        listData.add(SAActiveDayDataModel("S", false, false, 6))
        return listData
    }

    fun setViewGradient(binding: FragmentSetAlarmBinding, hour: String): Shader {
        val paint: TextPaint = binding.lytAlarmTime.tvHour.paint
        val width = paint.measureText(hour)

        val textShader: Shader = LinearGradient(
            0f,
            0f,
            width,
            20f,
            intArrayOf(
                Color.parseColor("#ffc8d2"),
                Color.parseColor("#ff7c94")
            ),
            null,
            TileMode.CLAMP
        )
        return textShader

    }

    fun insert(newAlarm: AlarmDataModel, type: Int) {
        alarmTimeUpdate.postValue(Event(Pair(newAlarm, type)))
    }

    fun saveAlarm() {
        //write code for save alarm
    }

    fun updateTime(localTime: LocalTime, endTime: LocalTime) {
        startEndTime.postValue(
            Pair(
                localTime,
                endTime
            )
        )
    }

    fun getDurationMinutes(start: LocalTime, end: LocalTime): Long {
        return if (end.isAfter(start)) {
            Duration.between(start, end).toMinutes()
        } else {
            val dayEnd = LocalTime.of(23, 59)
            val dayStart = LocalTime.of(0, 0)
            Duration.between(start, dayEnd).toMinutes() + 1 + Duration.between(dayStart, end)
                .toMinutes()
        }
    }

    fun getSleepPlanerDetails() {
        viewModelScope.launch {

            userActivityRepository.getSleepPlannerDetails().collect { resource ->
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
                                        getSleepPlanerDetails()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            sleepPlannerCard.postValue(it)
                        }
                    }
                }
            }
        }
    }
}