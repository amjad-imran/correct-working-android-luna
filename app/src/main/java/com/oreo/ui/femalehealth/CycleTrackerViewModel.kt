package com.oreo.ui.femalehealth

import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.FMHCycleHistoryDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CycleTrackerViewModel @Inject constructor() : BaseViewModel() {
    fun getCycleHistoryData(): ArrayList<FMHCycleHistoryDataModel> {
        val listData = ArrayList<FMHCycleHistoryDataModel>()
        for (i in 1..3) {
            listData.add(FMHCycleHistoryDataModel(cycleLength = i, startDate = "March $i"))
        }
        return listData
    }
}