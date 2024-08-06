package com.oreo.ui.internal

import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.OHMDataModel
import com.oreo.data.model.sleep.HealthTrend
import com.oreo.ui.sleep2.SleepContributor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class OHMInternalViewModel @Inject constructor() : BaseViewModel() {

    var healthTrend: HealthTrend? = null
    fun getHealthMonitorData(): ArrayList<OHMDataModel> {
        val listData = ArrayList<OHMDataModel>()
        val resp = healthTrend?.resp?.value?.let {
            it.roundToInt().toString()
        }
        val rhr = healthTrend?.rhr?.value?.let {
            it.roundToInt().toString()
        }

        val hrv = healthTrend?.hrv?.value?.let {
            it.roundToInt().toString()
        }
        val skinTemp = healthTrend?.skinTemp?.value?.let {
            it.toString()
        }
        val bloodOxy = healthTrend?.bloodOxy?.value?.let {
            it.roundToInt().toString()
        }
        listData.add(
            OHMDataModel(
                SleepContributor.RESPIRATORY_RATE,
                value = resp,
                valueTime = null,
                unit = "/min",
                text = healthTrend?.resp?.text,
                status = healthTrend?.resp?.status
            )
        )
        listData.add(
            OHMDataModel(
                SleepContributor.RESTING_HEART_RATE,
                value = rhr,
                valueTime = null,
                unit = "bmp",
                text = healthTrend?.rhr?.text,
                status = healthTrend?.rhr?.status
            )
        )
        listData.add(
            OHMDataModel(
                SleepContributor.HRV,
                value = hrv,
                valueTime = null,
                unit = "ms",
                text = healthTrend?.hrv?.text,
                status = healthTrend?.hrv?.status
            )
        )
        listData.add(
            OHMDataModel(
                SleepContributor.SKIN_TEMPERATURE,
                value = skinTemp,
                valueTime = null,
                unit = "F",
                text = healthTrend?.skinTemp?.text,
                status = healthTrend?.skinTemp?.status
            )
        )
        listData.add(
            OHMDataModel(
                SleepContributor.BLOOD_OXYGEN,
                value = bloodOxy,
                valueTime = null,
                unit = "%",
                text = healthTrend?.bloodOxy?.text,
                status = healthTrend?.bloodOxy?.status
            )
        )


        return listData
    }


}