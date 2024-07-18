package com.oreo.ui.sleep2.internal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.data.model.OSleepTrendsDataModel
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
        MutableLiveData<Pair<String, Int>>()
    val titleUpdate: LiveData<Pair<String, Int>> = _titleUpdate


    private fun getTitle(): Pair<String, Int> {
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
        when (trendsName?.lowercase()?.replace(" ", "_")) {
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

    fun trendsDummyData(): OSleepTrendsDataModel? {
        return OSleepTrendsDataModel(
            trendType = selectedLaunchMode,
            dayDate = "Monday  30 May, 2024",
            dspValue = "40",
            isShowOptimal = false,
            isShowHighlight = true,
            description = "Your resting HR seems to be higher than previous day. Allow yourself sufficient time for recovery by taking it slow."
        )

    }

    fun getPostFixAbr(trendType: SleepInternalLaunchState): String {
        var abr = ""
        if (trendType == SleepInternalLaunchState.SLEEP_PERFORMANCE) {
            abr = "% - average"
        } else if (trendType == SleepInternalLaunchState.RESTFULNESS)
            abr = "times - average"

        return abr

    }

    fun getHighlightBackType(type: Int): Pair<Int, Int> {
        /*
        * 0-up
        * 1-warning
        * 2-red alert
        * */
        val background: Int
        val textColor: Int
        when (type) {
            0 -> {
                background = R.drawable.back_hm_range
                textColor = R.color.edit_text_color
            }

            1 -> {
                background = R.drawable.back_hm_warning_range
                textColor = R.color.color_trends_warning
            }

            else -> {
                background = R.drawable.back_hm_down_range
                textColor = R.color.oreo_contributor_warning
            }
        }
        return Pair(background, textColor)
    }

    fun returnTrendsArrow(type: SleepInternalLaunchState): Int {
        val icon: Int = when (type) {
            SleepInternalLaunchState.SLEEP_PERFORMANCE, SleepInternalLaunchState.HOUR_VS_NEED, SleepInternalLaunchState.RESTORATIVE_SLEEP -> R.drawable.ic_trend_up
            SleepInternalLaunchState.EFFICIENCY, SleepInternalLaunchState.RESTFULNESS, SleepInternalLaunchState.LATENCY, SleepInternalLaunchState.SLEEP_DURATION -> R.drawable.ic_hm_tick
            else -> 0
        }
        return icon

    }


}

enum class InternalSelectedPeriod {
    DAY, WEEK, MONTH
}