package com.oreo.ui.internal

import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.OHMDataModel
import com.oreo.ui.sleep2.SleepContributor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OHMInternalViewModel @Inject constructor() : BaseViewModel() {

    fun getHealthMonitorData(): ArrayList<OHMDataModel> {
        val listData = ArrayList<OHMDataModel>()
        listData.add(
            OHMDataModel(
                SleepContributor.RESPIRATORY_RATE,
                value = "98.4",
                valueTime = 1,
                unit = "rpm",
                status = "near 11.9-13.8"
            )
        )
        listData.add(OHMDataModel(SleepContributor.RESTING_HEART_RATE))
        listData.add(OHMDataModel(SleepContributor.HRV))
        listData.add(OHMDataModel(SleepContributor.SKIN_TEMPERATURE))
        listData.add(OHMDataModel(SleepContributor.BLOOD_OXYGEN))
        return listData
    }


}