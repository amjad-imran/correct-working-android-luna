package com.oreo.ui.sleep2.sleepplanner

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Shader.TileMode
import android.text.TextPaint
import com.noisefit.data.model.SAActiveDayDataModel
import com.noisefit.luna.databinding.FragmentSetAlarmBinding
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SetAlarmViewModel @Inject constructor() : BaseViewModel() {
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
}