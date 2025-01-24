package com.oreo.ui.sleep2.sleepplanner

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Shader.TileMode
import android.text.TextPaint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.model.SAActiveDayDataModel
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.databinding.FragmentSetAlarmBinding
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.AlarmTimingsData
import com.noisefit_commans.data.model.PlannerAlarmData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.model.AlarmDataModel
import com.noisefit_commans.data.model.SleepPlannerData
import com.oreo.data.repository.AlarmRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import com.oreo.util.alarm.AlarmUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class SetAlarmViewModel @Inject constructor(
    private val userActivityRepository: OreoUserActivityRepository,
    private val resourcesProvider: ResourcesProvider,
    private val alarmRepository: AlarmRepository,
    private val alarmUtil: AlarmUtil,
) : BaseViewModel() {

    private var alarmsRawData: PlannerAlarmData? = null


    var lastSelectedPosition: Int? = null
    var selectedAlarmDays = ArrayList<SAActiveDayDataModel>()


    var alarmSound: String? = null
    var alarmTimeUpdate = MutableLiveData<Event<Pair<AlarmDataModel, Int>>>()

    var startEndTime = MutableLiveData<Pair<LocalTime, LocalTime>>()

    val sleepPlannerCard = MutableLiveData<SleepPlannerData?>()

    var alarmUpdated = MutableLiveData<Event<Boolean>>()


    init {
        alarmsRawData = alarmRepository.getAlarmsData()

        startEndTime.postValue(
            Pair(
                LocalTime.of(22, 0),
                LocalTime.of(6, 0)
            )
        )
    }

    fun getAlarmData(): ArrayList<SAActiveDayDataModel> {
        val listData = ArrayList<SAActiveDayDataModel>()
        listData.add(SAActiveDayDataModel("M", false, true, Calendar.MONDAY))
        listData.add(SAActiveDayDataModel("T", false, false, Calendar.TUESDAY))
        listData.add(SAActiveDayDataModel("W", false, false, Calendar.WEDNESDAY))
        listData.add(SAActiveDayDataModel("T", false, false, Calendar.THURSDAY))
        listData.add(SAActiveDayDataModel("F", false, false, Calendar.FRIDAY))
        listData.add(SAActiveDayDataModel("S", false, false, Calendar.SATURDAY))
        listData.add(SAActiveDayDataModel("S", false, false, Calendar.SUNDAY))
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

        //alarmUtil.scheduleWeeklyAlarm(Calendar.MONDAY, 11, 35)

        alarmRepository.saveAlarm(LocalTime.of(22, 0), LocalTime.of(6, 0), Calendar.MONDAY)
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

    fun updateAlarms() {
        viewModelScope.launch {

            if (startEndTime.value == null) return@launch

            val request = generateAlarmRequest(selectedAlarmDays, alarmsRawData)

            userActivityRepository.updateAlarms(request).collect { resource ->
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
                                        updateAlarms()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            alarmRepository.updateAlarms(request)
                            alarmUpdated.postValue(Event(true))
                        }
                    }
                }
            }
        }
    }

    private fun generateAlarmRequest(
        selectedAlarmDays: ArrayList<SAActiveDayDataModel>,
        alarmsRawData: PlannerAlarmData?
    ): PlannerAlarmData {

        /*val bedTime = startEndTime.value!!.first.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val wakeTime = startEndTime.value!!.second.format(DateTimeFormatter.ofPattern("HH:mm:ss"))*/

        val bedTime = LocalTime.of(3,0).format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        val wakeTime = LocalTime.now().plusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        val returnData = alarmsRawData?.copy() ?: PlannerAlarmData()

        selectedAlarmDays.forEach {
            when (it.dayKey) {
                Calendar.MONDAY -> {
                    returnData.mon = AlarmTimingsData(bedTime, wakeTime)
                }

                Calendar.TUESDAY -> {
                    returnData.tue = AlarmTimingsData(bedTime, wakeTime)
                }

                Calendar.WEDNESDAY -> {
                    returnData.wed = AlarmTimingsData(bedTime, wakeTime)
                }

                Calendar.THURSDAY -> {
                    returnData.thu = AlarmTimingsData(bedTime, wakeTime)
                }

                Calendar.FRIDAY -> {
                    returnData.fri = AlarmTimingsData(bedTime, wakeTime)
                }

                Calendar.SATURDAY -> {
                    returnData.sat = AlarmTimingsData(bedTime, wakeTime)
                }

                Calendar.SUNDAY -> {
                    returnData.sun = AlarmTimingsData(bedTime, wakeTime)
                }
            }
        }
        return returnData
    }
}