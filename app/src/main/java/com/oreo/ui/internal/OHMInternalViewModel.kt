package com.oreo.ui.internal

import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.AppConversionUtils
import com.oreo.data.model.OHMDataModel
import com.oreo.data.model.sleep.HealthTrend
import com.oreo.ui.sleep2.SleepContributor
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class OHMInternalViewModel @Inject constructor(
    val sessionManager: SessionManager,
    val resourcesProvider: ResourcesProvider
) : BaseViewModel() {

    var healthTrend: HealthTrend? = null
    var selectedDate: String? = null
    var source: String? = null

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

        /*val skinTemp = healthTrend?.skinTemp?.value?.let {
            it.toString()
        }*/

        val skinTemp = if (healthTrend?.skinTemp?.value != null) {
            if (sessionManager.isMetric()) {
                val convertedVal = AppConversionUtils.fahrenheitToCelsius(
                    32f + (healthTrend?.skinTemp?.value?.toFloat() ?: 0.0f)
                )

                if (convertedVal > 0) {
                    String.format(
                        locale = Locale.US,
                        "+%.1f",
                        convertedVal,
                    )
                } else {
                    String.format(
                        locale = Locale.US,
                        "%.1f",
                        convertedVal,
                    )
                }
            } else {
                val value = healthTrend?.skinTemp?.value?.toFloat() ?: 0.0f
                if (value > 0) {
                    String.format(
                        locale = Locale.US,
                        "+%.1f",
                        value,
                    )
                } else {
                    String.format(
                        locale = Locale.US,
                        "%.1f",
                        value,
                    )
                }
            }
        } else {
            null
        }


        val bloodOxy = healthTrend?.bloodOxy?.value?.let {
            it.roundToInt().toString()
        }
        listData.add(
            OHMDataModel(
                SleepContributor.RESPIRATORY_RATE,
                getSleepContributorDisplayName(SleepContributor.RESPIRATORY_RATE),
                value = resp,
                valueTime = null,
                unit = "rpm",
                text = healthTrend?.resp?.text,
                status = healthTrend?.resp?.status
            )
        )
        listData.add(
            OHMDataModel(
                SleepContributor.RESTING_HEART_RATE,
                getSleepContributorDisplayName(SleepContributor.RESTING_HEART_RATE),
                value = rhr,
                valueTime = null,
                unit = "bpm",
                text = healthTrend?.rhr?.text,
                status = healthTrend?.rhr?.status
            )
        )
        listData.add(
            OHMDataModel(
                SleepContributor.HRV,
                getSleepContributorDisplayName(SleepContributor.HRV),
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
                getSleepContributorDisplayName(SleepContributor.SKIN_TEMPERATURE),
                value = skinTemp,
                valueTime = null,
                unit = if (sessionManager.isMetric()) "°C" else "°F",
                text = if (sessionManager.isMetric()) {
                    healthTrend?.skinTemp?.textC
                } else {
                    healthTrend?.skinTemp?.text
                },
                status = healthTrend?.skinTemp?.status
            )
        )
        listData.add(
            OHMDataModel(
                SleepContributor.BLOOD_OXYGEN,
                getSleepContributorDisplayName(SleepContributor.BLOOD_OXYGEN),
                value = bloodOxy,
                valueTime = null,
                unit = "%",
                text = healthTrend?.bloodOxy?.text,
                status = healthTrend?.bloodOxy?.status
            )
        )


        return listData
    }

    fun getSleepContributorDisplayName(contributor: SleepContributor): String {
        return when (contributor) {
            SleepContributor.SLEEP_DURATION -> resourcesProvider.getString(R.string.text_sleep_duration)
            SleepContributor.REM_SLEEP -> resourcesProvider.getString(R.string.text_rem_sleep)
            SleepContributor.DEEP_SLEEP -> resourcesProvider.getString(R.string.text_deep_sleep)
            SleepContributor.EFFICIENCY -> resourcesProvider.getString(R.string.text_efficiency)
            SleepContributor.LATENCY -> resourcesProvider.getString(R.string.text_latency)
            SleepContributor.RESTFULNESS -> resourcesProvider.getString(R.string.text_restfulness)
            SleepContributor.TIMING -> resourcesProvider.getString(R.string.text_circadian_mid_point)
            SleepContributor.RESPIRATORY_RATE -> resourcesProvider.getString(R.string.text_respiratory_rate)
            SleepContributor.RESTING_HEART_RATE -> resourcesProvider.getString(R.string.text_resting_heart_rate)
            SleepContributor.HRV -> resourcesProvider.getString(R.string.text_hrv)
            SleepContributor.SKIN_TEMPERATURE -> resourcesProvider.getString(R.string.text_skin_temperature)
            SleepContributor.BLOOD_OXYGEN -> resourcesProvider.getString(R.string.text_blood_oxygen)
        }
    }

}