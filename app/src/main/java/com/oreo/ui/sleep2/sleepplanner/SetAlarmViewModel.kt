package com.oreo.ui.sleep2.sleepplanner

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Shader.TileMode
import android.text.TextPaint
import androidx.lifecycle.MutableLiveData
import com.google.android.material.timepicker.MaterialTimePicker
import com.noisefit.data.model.AlarmDataModel
import com.noisefit.data.model.SAActiveDayDataModel
import com.noisefit.luna.databinding.FragmentSetAlarmBinding
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class SetAlarmViewModel @Inject constructor() : BaseViewModel() {

    lateinit var picker: MaterialTimePicker
    lateinit var calendar: Calendar
    var selectedAlarmDays = ArrayList<String>()
    var alarmSound:String?= null
    var alarmTimeUpdate = MutableLiveData<Event<Pair<AlarmDataModel, Int>>>()
    fun getAlarmData(): ArrayList<SAActiveDayDataModel> {
        val listData = ArrayList<SAActiveDayDataModel>()
        listData.add(SAActiveDayDataModel("S", false))
        listData.add(SAActiveDayDataModel("M", false))
        listData.add(SAActiveDayDataModel("T", true))
        listData.add(SAActiveDayDataModel("W", false))
        listData.add(SAActiveDayDataModel("T", true))
        listData.add(SAActiveDayDataModel("F", false))
        listData.add(SAActiveDayDataModel("S", false))
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
}