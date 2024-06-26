package com.oreo.ui.sleep2.sleepplanner

import com.noisefit.data.model.SAActiveDayDataModel
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
}