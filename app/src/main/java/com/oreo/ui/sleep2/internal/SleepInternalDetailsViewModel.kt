package com.oreo.ui.sleep2.internal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.LearnMoreDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SleepInternalDetailsViewModel @Inject constructor(
    private val resourcesProvider: ResourcesProvider
) : BaseViewModel() {

    lateinit var selectedLaunchMode: SleepInternalLaunchState

    private val _selectedPeriod =
        MutableLiveData<InternalSelectedPeriod>(InternalSelectedPeriod.DAY)
    val selectedPeriod: LiveData<InternalSelectedPeriod> = _selectedPeriod

    fun setSelectedPeriod(selectedPeriod: InternalSelectedPeriod) {
        _selectedPeriod.postValue(selectedPeriod)
    }

    private val _titleUpdate =
        MutableLiveData<String>()
    val titleUpdate: LiveData<String> = _titleUpdate


    private fun getTitle(): String {
        return when (selectedLaunchMode) {
            SleepInternalLaunchState.RESTORATIVE_SLEEP -> resourcesProvider.getString(R.string.text_restorative_sleep)
            SleepInternalLaunchState.SLEEP_TIME -> resourcesProvider.getString(R.string.text_sleep_time)
            SleepInternalLaunchState.HOUR_VS_NEED -> resourcesProvider.getString(R.string.text_hour_vs_need)
            SleepInternalLaunchState.SLEEP_PERFORMANCE -> resourcesProvider.getString(R.string.text_sleep_performance)
            SleepInternalLaunchState.EFFICIENCY -> resourcesProvider.getString(R.string.text_efficiency)
            SleepInternalLaunchState.REM_SLEEP -> resourcesProvider.getString(R.string.text_rem_sleep)
            SleepInternalLaunchState.DEEP_SLEEP -> resourcesProvider.getString(R.string.text_deep_sleep)
            SleepInternalLaunchState.LATENCY -> resourcesProvider.getString(R.string.text_latency)
            SleepInternalLaunchState.RESTFULNESS -> resourcesProvider.getString(R.string.text_restfulness)
            SleepInternalLaunchState.SLEEP_DURATION -> resourcesProvider.getString(R.string.text_sleep_duration)

        }
    }

    fun updateTitle() {
        _titleUpdate.postValue(getTitle())
    }

    fun getLearnMoreData(): ArrayList<LearnMoreDataModel> {
        val dataList = ArrayList<LearnMoreDataModel>()
        dataList.add(
            LearnMoreDataModel(
                title = "General heart rate terms",
                msg = "2 min read",
                img = R.drawable.img_hr_article_1,
                type = 1
            )
        )
        dataList.add(
            LearnMoreDataModel(
                title = "Normal heart rate for my age",
                msg = "2 min read",
                img = R.drawable.img_hr_article_2,
                type = 2
            )
        )
        dataList.add(
            LearnMoreDataModel(
                title = "What are heart rate zones ?",
                msg = "2 min read",
                img = R.drawable.img_hr_article_3,
                type = 3
            )
        )
        dataList.add(
            LearnMoreDataModel(
                title = "Heart rate during sleep",
                msg = "2 min read",
                img = R.drawable.img_hr_article_4,
                type = 4
            )
        )
        return dataList
    }

    fun updateTrendsName(trendsName: String?) {
        when (trendsName?.lowercase()?.replace(" ","_")) {
            SleepInternalLaunchState.RESTORATIVE_SLEEP.name.lowercase() -> selectedLaunchMode =
                SleepInternalLaunchState.RESTORATIVE_SLEEP

            SleepInternalLaunchState.SLEEP_PERFORMANCE.name.lowercase() -> selectedLaunchMode =
                SleepInternalLaunchState.SLEEP_PERFORMANCE

            SleepInternalLaunchState.DEEP_SLEEP.name.lowercase() -> selectedLaunchMode =
                SleepInternalLaunchState.DEEP_SLEEP

            SleepInternalLaunchState.EFFICIENCY.name.lowercase() -> selectedLaunchMode =
                SleepInternalLaunchState.EFFICIENCY

            SleepInternalLaunchState.LATENCY.name.lowercase() -> selectedLaunchMode =
                SleepInternalLaunchState.LATENCY

            SleepInternalLaunchState.RESTFULNESS.name.lowercase() -> selectedLaunchMode =
                SleepInternalLaunchState.RESTFULNESS

            SleepInternalLaunchState.REM_SLEEP.name.lowercase() ->
                selectedLaunchMode = SleepInternalLaunchState.REM_SLEEP

            SleepInternalLaunchState.SLEEP_TIME.name.lowercase() ->
                selectedLaunchMode = SleepInternalLaunchState.SLEEP_TIME
            SleepInternalLaunchState.SLEEP_DURATION.name.lowercase() ->
                selectedLaunchMode = SleepInternalLaunchState.SLEEP_DURATION

        }
    }


}

enum class InternalSelectedPeriod {
    DAY, WEEK, MONTH
}