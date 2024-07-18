package com.oreo.ui.sleep2

import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.ODropDownDataModel
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ODropDownViewModel @Inject constructor(
    private val resourcesProvider: ResourcesProvider
) : BaseViewModel() {
    lateinit var selectedLaunchMode: SleepInternalLaunchState
    fun fetchDropDownData(): Triple<ArrayList<ODropDownDataModel>, ArrayList<ODropDownDataModel>, ArrayList<ODropDownDataModel>> {
        val sleepData = ArrayList<ODropDownDataModel>()
        sleepData.add(ODropDownDataModel(R.drawable.ic_sleep_efficieny, "Efficiency"))
        sleepData.add(ODropDownDataModel(R.drawable.ic_sleep_rem_sp, "REM sleep"))
        sleepData.add(ODropDownDataModel(R.drawable.ic_sleep_deep_sp, "Deep sleep"))
        sleepData.add(ODropDownDataModel(R.drawable.ic_clock_off_sleep, "Sleep duration"))
        sleepData.add(ODropDownDataModel(R.drawable.ic_sleep_latency, "Latency"))
        sleepData.add(ODropDownDataModel(R.drawable.ic_sleep_restfulness, "Restfulness"))

        val activityData = ArrayList<ODropDownDataModel>()
        activityData.add(ODropDownDataModel(R.drawable.ic_sleep_efficieny, "Efficiency"))
        activityData.add(ODropDownDataModel(R.drawable.ic_sleep_rem_sp, "REM sleep"))
        activityData.add(ODropDownDataModel(R.drawable.ic_sleep_deep_sp, "Deep sleep"))
        activityData.add(ODropDownDataModel(R.drawable.ic_clock_off_sleep, "Sleep duration"))
        activityData.add(ODropDownDataModel(R.drawable.ic_sleep_latency, "Latency"))
        activityData.add(ODropDownDataModel(R.drawable.ic_sleep_restfulness, "Restfulness"))

        val readinessData = ArrayList<ODropDownDataModel>()
        readinessData.add(ODropDownDataModel(R.drawable.ic_sleep_efficieny, "Efficiency"))
        readinessData.add(ODropDownDataModel(R.drawable.ic_sleep_rem_sp, "REM sleep"))
        readinessData.add(ODropDownDataModel(R.drawable.ic_sleep_deep_sp, "Deep sleep"))
        readinessData.add(ODropDownDataModel(R.drawable.ic_clock_off_sleep, "Sleep duration"))
        readinessData.add(ODropDownDataModel(R.drawable.ic_sleep_latency, "Latency"))
        readinessData.add(ODropDownDataModel(R.drawable.ic_sleep_restfulness, "Restfulness"))

        return Triple(sleepData, activityData, readinessData)
    }

    fun getTitle(): Pair<String, Int> {
        return when (selectedLaunchMode) {
            SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
                Pair(
                    resourcesProvider.getString(R.string.text_restorative_sleep),
                    R.drawable.ic_sleep_restroactive_sp
                )
            }

            SleepInternalLaunchState.SLEEP_TIME -> Pair(
                resourcesProvider.getString(R.string.text_sleep_time),
                R.drawable.ic_sleep_time
            )

            SleepInternalLaunchState.HOUR_VS_NEED -> Pair(
                resourcesProvider.getString(R.string.text_hour_vs_need),
                R.drawable.ic_sleep_snooz
            )

            SleepInternalLaunchState.SLEEP_PERFORMANCE -> Pair(
                resourcesProvider.getString(R.string.text_sleep_performance),
                R.drawable.ic_sleep_performance
            )

            SleepInternalLaunchState.EFFICIENCY -> Pair(
                resourcesProvider.getString(R.string.text_efficiency),
                R.drawable.ic_sleep_efficieny
            )

            SleepInternalLaunchState.REM_SLEEP -> Pair(
                resourcesProvider.getString(R.string.text_rem_sleep),
                R.drawable.ic_sleep_rem_sp
            )

            SleepInternalLaunchState.DEEP_SLEEP -> Pair(
                resourcesProvider.getString(R.string.text_deep_sleep),
                R.drawable.ic_sleep_deep_sp
            )

            SleepInternalLaunchState.LATENCY -> Pair(
                resourcesProvider.getString(R.string.text_latency),
                R.drawable.ic_sleep_latency
            )

            SleepInternalLaunchState.RESTFULNESS -> Pair(
                resourcesProvider.getString(R.string.text_restfulness),
                R.drawable.ic_sleep_restfulness
            )

            SleepInternalLaunchState.SLEEP_DURATION -> Pair(
                resourcesProvider.getString(R.string.text_sleep_duration),
                R.drawable.ic_clock_off_sleep
            )


        }
    }


}