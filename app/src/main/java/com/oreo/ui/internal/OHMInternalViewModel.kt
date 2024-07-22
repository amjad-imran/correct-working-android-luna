package com.oreo.ui.internal

import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.ODropDownDataModel
import com.oreo.data.model.OHMDataModel
import com.oreo.ui.sleep2.internal.SkinTempInternalLaunchState
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

    fun getLaunchType(title: String): SkinTempInternalLaunchState {
        val launchMode:SkinTempInternalLaunchState = when {
            title.lowercase()=="Respiratory rate".lowercase() -> SkinTempInternalLaunchState.RESPIRATORY_RATE
            title.lowercase()=="Resting heart rate".lowercase() -> SkinTempInternalLaunchState.RESTING_HEART_RATE
            title.lowercase()=="Blood oxygen".lowercase() -> SkinTempInternalLaunchState.BLOOD_OXYGEN
            title.lowercase()=="HRV".lowercase() -> SkinTempInternalLaunchState.HRV
            else -> SkinTempInternalLaunchState.SKIN_TEMPERATURE
        }
        return launchMode

    }


}