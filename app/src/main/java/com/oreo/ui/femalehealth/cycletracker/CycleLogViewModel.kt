package com.oreo.ui.femalehealth.cycletracker

import com.noisefit.luna.R
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
        flowListData.add(FlowLog(image = R.drawable.ic_fmh_light_flow, title = "None"))
        flowListData.add(FlowLog(image = R.drawable.ic_fmh_light_flow, title = "Light"))
        flowListData.add(FlowLog(image = R.drawable.ic_fmh_medium_flow, title = "Medium"))
        flowListData.add(FlowLog(image = R.drawable.ic_fmh_medium_flow, title = "Heavy"))

        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_bloating, title = "Bloating"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_cramps, title = "Cramps"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_bloating, title = "Backache"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_cramps, title = "Fatigue"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_bloating, title = "Heavy"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_cramps, title = "Headache"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_bloating, title = "Acne"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_cramps, title = "Craving"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_bloating, title = "Abdomen pain"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_cramps, title = "Tender breast"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_bloating, title = "Constipation"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_cramps, title = "Back pain"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_bloating, title = "Mood swing"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_cramps, title = "Vaginal itching"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_bloating, title = "Vaginal dryness"))
        symptomsListData.add(FlowLog(image = R.drawable.ic_fmh_cramps, title = "Nausea"))

        childData.flowData = flowListData
        childData.symptomsData = symptomsListData
        return childData
    }
}