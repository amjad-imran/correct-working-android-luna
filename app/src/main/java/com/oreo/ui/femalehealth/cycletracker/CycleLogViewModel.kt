package com.oreo.ui.femalehealth.cycletracker

import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.CycleLogDataModel
import com.oreo.data.model.FlowLog
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CycleLogViewModel @Inject constructor() : BaseViewModel() {
    fun getCycleLogData(): CycleLogDataModel {
        val childData = CycleLogDataModel()
        val flowListData = ArrayList<FlowLog>()
        val symptomsListData = ArrayList<FlowLog>()
        flowListData.add(FlowLog(image = null, title = "None"))
        flowListData.add(FlowLog(image = null, title = "Light"))
        flowListData.add(FlowLog(image = null, title = "Typical"))
        flowListData.add(FlowLog(image = null, title = "Heavy"))

        symptomsListData.add(FlowLog(image = null, title = "None"))
        symptomsListData.add(FlowLog(image = null, title = "Light"))
        symptomsListData.add(FlowLog(image = null, title = "Typical"))
        symptomsListData.add(FlowLog(image = null, title = "Heavy"))

        childData.flowData = flowListData
        childData.symptomsData = symptomsListData
        return childData
    }
}