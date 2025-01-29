package com.oreo.ui.sleep2.sleepplanner

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Shader.TileMode
import android.text.TextPaint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.model.AlarmSoundDataModel
import com.noisefit.data.model.SAActiveDayDataModel
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSetAlarmBinding
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.AlarmTimingsData
import com.noisefit_commans.data.model.PlannerAlarmData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.model.AlarmDataModel
import com.noisefit_commans.data.model.SleepPlannerData
import com.noisefit_commans.utils.LOGS
import com.oreo.data.repository.AlarmRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import com.oreo.util.alarm.AlarmUtil.Companion.getAlarmToneByKey
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
    private val alarmRepository: AlarmRepository,
) : BaseViewModel() {

    var deleteMode = MutableLiveData(false)
    private var alarmsRawData: PlannerAlarmData? = null

    var editModeSelectedTime: Pair<LocalTime, LocalTime>? = null


    var lastSelectedPosition: Int? = null


    var alarmTimeUpdate = MutableLiveData<Event<Pair<AlarmDataModel, Int>>>()

    var startEndTime = MutableLiveData<Pair<LocalTime, LocalTime>>()

    val sleepPlannerCard = MutableLiveData<SleepPlannerData?>()

    var alarmUpdated = MutableLiveData<Event<Boolean>>()
    var selectedTone = MutableLiveData<AlarmSoundDataModel>(alarmTonesList().first())


    fun getAlarmData(alarms: PlannerAlarmData?): ArrayList<SAActiveDayDataModel> {
        val listData = ArrayList<SAActiveDayDataModel>()

        val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")

        listData.add(
            generateDayData(
                alarms?.mon,
                Calendar.MONDAY,
                timeFormat,
                editModeSelectedTime
            )
        )
        listData.add(
            generateDayData(
                alarms?.tue,
                Calendar.TUESDAY,
                timeFormat,
                editModeSelectedTime
            )
        )
        listData.add(
            generateDayData(
                alarms?.wed,
                Calendar.WEDNESDAY,
                timeFormat,
                editModeSelectedTime
            )
        )
        listData.add(
            generateDayData(
                alarms?.thu,
                Calendar.THURSDAY,
                timeFormat,
                editModeSelectedTime
            )
        )
        listData.add(
            generateDayData(
                alarms?.fri,
                Calendar.FRIDAY,
                timeFormat,
                editModeSelectedTime
            )
        )
        listData.add(
            generateDayData(
                alarms?.sat,
                Calendar.SATURDAY,
                timeFormat,
                editModeSelectedTime
            )
        )
        listData.add(
            generateDayData(
                alarms?.sun,
                Calendar.SUNDAY,
                timeFormat,
                editModeSelectedTime
            )
        )

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

    fun updateTime(localTime: LocalTime, endTime: LocalTime) {
        startEndTime.postValue(
            Pair(
                localTime,
                endTime
            )
        )
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
                            alarmsRawData = it?.alarms
                            sleepPlannerCard.postValue(it)
                        }
                    }
                }
            }
        }
    }

    fun updateAlarms(
        selectedAlarmDays: List<SAActiveDayDataModel>,
        unselectedItems: List<SAActiveDayDataModel>
    ) {
        viewModelScope.launch {

            if (startEndTime.value == null) return@launch

            val request = generateAlarmRequest(selectedAlarmDays, alarmsRawData, unselectedItems)

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
                                        updateAlarms(
                                            selectedAlarmDays,
                                            unselectedItems
                                        )
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
        selectedAlarmDays: List<SAActiveDayDataModel>,
        alarmsRawData: PlannerAlarmData?,
        unselectedItems: List<SAActiveDayDataModel>
    ): PlannerAlarmData {

        /*val bedTime = startEndTime.value!!.first.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val wakeTime = startEndTime.value!!.second.format(DateTimeFormatter.ofPattern("HH:mm:ss"))*/

        val bedTime = LocalTime.of(3, 0).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val wakeTime =
            LocalTime.now().plusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        val returnData = alarmsRawData?.copy() ?: PlannerAlarmData()

        val deleteMode = this.deleteMode.value == true
        val selectedToneKey = selectedTone.value?.key ?: 1

        selectedAlarmDays.forEach {
            when (it.dayKey) {
                Calendar.MONDAY -> {
                    returnData.mon = if (deleteMode) null else AlarmTimingsData(
                        bedTime,
                        wakeTime,
                        selectedToneKey
                    )
                }

                Calendar.TUESDAY -> {
                    returnData.tue = if (deleteMode) null else AlarmTimingsData(
                        bedTime,
                        wakeTime,
                        selectedToneKey
                    )
                }

                Calendar.WEDNESDAY -> {
                    returnData.wed = if (deleteMode) null else AlarmTimingsData(
                        bedTime,
                        wakeTime,
                        selectedToneKey
                    )
                }

                Calendar.THURSDAY -> {
                    returnData.thu = if (deleteMode) null else AlarmTimingsData(
                        bedTime,
                        wakeTime,
                        selectedToneKey
                    )
                }

                Calendar.FRIDAY -> {
                    returnData.fri = if (deleteMode) null else AlarmTimingsData(
                        bedTime,
                        wakeTime,
                        selectedToneKey
                    )
                }

                Calendar.SATURDAY -> {
                    returnData.sat = if (deleteMode) null else AlarmTimingsData(
                        bedTime,
                        wakeTime,
                        selectedToneKey
                    )
                }

                Calendar.SUNDAY -> {
                    returnData.sun = if (deleteMode) null else AlarmTimingsData(
                        bedTime,
                        wakeTime,
                        selectedToneKey
                    )
                }
            }
        }

        if (editModeSelectedTime != null) { //Edit mode

            unselectedItems.forEach {
                when (it.dayKey) {
                    Calendar.MONDAY -> {
                        returnData.mon = null
                    }

                    Calendar.TUESDAY -> {
                        returnData.tue = null
                    }

                    Calendar.WEDNESDAY -> {
                        returnData.wed = null
                    }

                    Calendar.THURSDAY -> {
                        returnData.thu = null
                    }

                    Calendar.FRIDAY -> {
                        returnData.fri = null
                    }

                    Calendar.SATURDAY -> {
                        returnData.sat = null
                    }

                    Calendar.SUNDAY -> {
                        returnData.sun = null
                    }
                }
            }
        }
        return returnData
    }

    fun setEditMode(bedTime: String?, wakeTime: String?) {
        if (bedTime != null && wakeTime != null) {
            editModeSelectedTime = Pair(
                LocalTime.parse(bedTime, DateTimeFormatter.ofPattern("HH:mm:ss")),
                LocalTime.parse(wakeTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
            )
            startEndTime.value = (editModeSelectedTime)
        } else {
            startEndTime.value = (
                    Pair(
                        LocalTime.of(22, 0),
                        LocalTime.of(6, 0)
                    )
                    )
        }
    }

    private fun generateDayData(
        alarmDay: AlarmTimingsData?,
        dayKey: Int,
        timeFormat: DateTimeFormatter,
        editModeSelectedTime: Pair<LocalTime, LocalTime>?
    ): SAActiveDayDataModel {

        if (editModeSelectedTime == null) {
            return SAActiveDayDataModel(false, alarmDay != null, dayKey)
        }

        return if (alarmDay == null) {
            SAActiveDayDataModel(false, false, dayKey)
        } else {
            val bed = LocalTime.parse(alarmDay.bed_time, timeFormat)
            val wake = LocalTime.parse(alarmDay.wake_time, timeFormat)
            if (bed == editModeSelectedTime.first && wake == editModeSelectedTime.second) {
                SAActiveDayDataModel(true, false, dayKey)
            } else {
                SAActiveDayDataModel(false, true, dayKey)
            }
        }
    }

    fun alarmTonesList(): List<AlarmSoundDataModel> {
        val dataList = ArrayList<AlarmSoundDataModel>()
        dataList.add(AlarmSoundDataModel(title = "Lofi", false, getAlarmToneByKey(1), 1))
        dataList.add(AlarmSoundDataModel(title = "Thailand", false, getAlarmToneByKey(2), 2))
        dataList.add(AlarmSoundDataModel(title = "Singapore", false, getAlarmToneByKey(3), 3))
        dataList.add(AlarmSoundDataModel(title = "Scotland", false, getAlarmToneByKey(4), 4))
        return dataList
    }

    fun setDefaultTone(editModeSelectedTime: Pair<LocalTime, LocalTime>) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val alarm = alarmsRawData?.getNonNullAlarms()?.find {
            it.second.bed_time.equals(editModeSelectedTime.first.format(formatter), true) &&
                    it.second.wake_time.equals(editModeSelectedTime.second.format(formatter), true)
        }
        selectedTone.postValue(alarmTonesList().find { it.key == (alarm?.second?.audio ?: 1) })
    }

    fun isDurationMin(durationMinutes: Long): Boolean {
        val min = (sleepPlannerCard.value?.planner?.min_duration ?: 0) / 60
        if (min == 0L) return false

        return durationMinutes<=min

    }
}