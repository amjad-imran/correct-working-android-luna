package com.oreo.ui.internal

import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.ODropDownDataModel
import com.oreo.data.model.OHMDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OHMInternalViewModel @Inject constructor() : BaseViewModel() {

    fun getHealthMonitorData(): ArrayList<OHMDataModel> {
        val listData = ArrayList<OHMDataModel>()
        listData.add(
            OHMDataModel(
                R.drawable.ic_respiratory_rate,
                "Respiratory rate",
                value = "98.4",
                unit = "rpm",
                rangeValue = "near 11.9-13.8"
            )
        )
        listData.add(OHMDataModel(R.drawable.ic_resting_hr, "Resting heart rate"))
        listData.add(OHMDataModel(R.drawable.ic_blood_oxygen, "Blood oxygen"))
        listData.add(OHMDataModel(R.drawable.ic_hrv, "HRV"))
        listData.add(OHMDataModel(R.drawable.ic_skin_tempreature, "Skin temperature"))
        return listData
    }

    fun fetchDropDownData(): Triple<ArrayList<ODropDownDataModel>, ArrayList<ODropDownDataModel>, ArrayList<ODropDownDataModel>> {
        val sleepData = ArrayList<ODropDownDataModel>()
        sleepData.add(ODropDownDataModel(R.drawable.ic_respiratory_rate, "Efficiency"))
        sleepData.add(ODropDownDataModel(R.drawable.ic_resting_hr, "REM sleep"))
        sleepData.add(ODropDownDataModel(R.drawable.ic_blood_oxygen, "Deep sleep"))
        sleepData.add(ODropDownDataModel(R.drawable.ic_hrv, "Sleep duration"))
        sleepData.add(ODropDownDataModel(R.drawable.ic_skin_tempreature, "Latency"))
        sleepData.add(ODropDownDataModel(R.drawable.ic_skin_tempreature, "Restfullness"))

        val activityData = ArrayList<ODropDownDataModel>()
        activityData.add(ODropDownDataModel(R.drawable.ic_respiratory_rate, "Activity1"))
        activityData.add(ODropDownDataModel(R.drawable.ic_resting_hr, "Activity2"))
        activityData.add(ODropDownDataModel(R.drawable.ic_blood_oxygen, "Activity3"))
        activityData.add(ODropDownDataModel(R.drawable.ic_hrv, "Activity4"))
        activityData.add(ODropDownDataModel(R.drawable.ic_skin_tempreature, "Activity5"))
        activityData.add(ODropDownDataModel(R.drawable.ic_skin_tempreature, "Activity6"))

        val readinessData = ArrayList<ODropDownDataModel>()
        readinessData.add(ODropDownDataModel(R.drawable.ic_respiratory_rate, "Readiness1"))
        readinessData.add(ODropDownDataModel(R.drawable.ic_resting_hr, "Readiness2"))
        readinessData.add(ODropDownDataModel(R.drawable.ic_blood_oxygen, "Readiness3"))
        readinessData.add(ODropDownDataModel(R.drawable.ic_hrv, "Readiness3"))
        readinessData.add(ODropDownDataModel(R.drawable.ic_skin_tempreature, "Readiness4"))
        readinessData.add(ODropDownDataModel(R.drawable.ic_skin_tempreature, "Readiness5"))
        return Triple(sleepData, activityData, readinessData)
    }

}