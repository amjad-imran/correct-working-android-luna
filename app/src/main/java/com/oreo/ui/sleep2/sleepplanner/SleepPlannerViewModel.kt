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
class SleepPlannerViewModel @Inject constructor() : BaseViewModel() {
    fun getAlarmDays(): ArrayList<String> {
        return arrayListOf("Mon","Tue","Wed","Thur","Fri","Sat","Sun")
    }

    }