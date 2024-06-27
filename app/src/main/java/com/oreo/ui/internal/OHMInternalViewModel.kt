package com.oreo.ui.internal

import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.OHMDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OHMInternalViewModel @Inject constructor() : BaseViewModel() {

    fun getHealthMonitorData(): ArrayList<OHMDataModel> {
        val listData = ArrayList<OHMDataModel>()
        listData.add(OHMDataModel(R.drawable.ic_respiratory_rate, "Respiratory rate", value = "98.4", unit = "rpm", rangeValue = "near 11.9-13.8"))
        listData.add(OHMDataModel(R.drawable.ic_resting_hr, "Resting heart rate"))
        listData.add(OHMDataModel(R.drawable.ic_blood_oxygen, "Blood oxygen"))
        listData.add(OHMDataModel(R.drawable.ic_hrv, "HRV"))
        listData.add(OHMDataModel(R.drawable.ic_skin_tempreature, "Skin temperature"))
        return listData
    }
}