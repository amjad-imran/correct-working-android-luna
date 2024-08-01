package com.oreo.ui.sleep2.internal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.data.model.OHealthMonTrendsDataModel
import com.oreo.ui.sleep2.SleepContributor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SkinTempInternalDetailsViewModel @Inject constructor(
    private val resourcesProvider: ResourcesProvider
) : BaseViewModel() {

    lateinit var selectedLaunchMode: SleepInternalLaunchState
    var isDeviationSelected:Boolean=true

    private val _selectedPeriod =
        MutableLiveData(InternalSelectedPeriod.DAY)
    val selectedPeriod: LiveData<InternalSelectedPeriod> = _selectedPeriod

    fun setSelectedPeriod(selectedPeriod: InternalSelectedPeriod) {
        _selectedPeriod.postValue(selectedPeriod)
    }

    private val _titleUpdate =
        MutableLiveData<Pair<String, Int>>()
    val titleUpdate: LiveData<Pair<String, Int>> = _titleUpdate


    private fun getTitle(): Pair<String, Int> {
        return when (selectedLaunchMode) {
            SleepInternalLaunchState.RESPIRATORY_RATE -> {
                Pair(
                    resourcesProvider.getString(R.string.text_respiratory_rate),
                    R.drawable.ic_respiratory_rate
                )
            }

            SleepInternalLaunchState.RESTING_HEART_RATE -> Pair(
                resourcesProvider.getString(R.string.text_resting_heart_rate),
                R.drawable.ic_resting_hr
            )

            SleepInternalLaunchState.HRV -> Pair(
                resourcesProvider.getString(R.string.text_hrv),
                R.drawable.ic_hrv
            )

            SleepInternalLaunchState.SKIN_TEMPERATURE -> Pair(
                resourcesProvider.getString(R.string.text_skin_temperature),
                R.drawable.ic_skin_tempreature
            )

            SleepInternalLaunchState.BLOOD_OXYGEN -> Pair(
                resourcesProvider.getString(R.string.text_blood_oxygen),
                R.drawable.ic_blood_oxygen
            )

            else -> {
                Pair(
                    resourcesProvider.getString(R.string.text_skin_temperature),
                    R.drawable.ic_skin_tempreature
                )
            }
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

//    fun updateTrendsName(trendsName: String?) {
//        when (trendsName?.lowercase()?.replace(" ", "_")) {
//            SleepInternalLaunchState.RESPIRATORY_RATE.name.lowercase() -> selectedLaunchMode =
//                SleepContributor.RESPIRATORY_RATE
//
//            SleepContributor.RESTING_HEART_RATE.name.lowercase() -> selectedLaunchMode =
//                SleepContributor.RESTING_HEART_RATE
//
//            SleepContributor.HRV.name.lowercase() -> selectedLaunchMode =
//                SleepContributor.HRV
//
//            SleepContributor.SKIN_TEMPERATURE.name.lowercase() -> selectedLaunchMode =
//                SleepContributor.SKIN_TEMPERATURE
//
//            SleepContributor.BLOOD_OXYGEN.name.lowercase() -> selectedLaunchMode =
//                SleepContributor.BLOOD_OXYGEN
//
//        }
//    }

    fun getPostFixAbr(trendType: SleepInternalLaunchState): String {
        val abr: String = when (trendType) {
            SleepInternalLaunchState.SKIN_TEMPERATURE -> "°F - average"
            SleepInternalLaunchState.HRV -> "ms - average"
            SleepInternalLaunchState.RESPIRATORY_RATE -> "rpm - average"
            SleepInternalLaunchState.RESTING_HEART_RATE -> "bpm - average"
            SleepInternalLaunchState.BLOOD_OXYGEN -> "% - average"
            SleepInternalLaunchState.SLEEP_DURATION -> ""
            SleepInternalLaunchState.REM_SLEEP -> ""
            SleepInternalLaunchState.DEEP_SLEEP -> ""
            SleepInternalLaunchState.EFFICIENCY -> ""
            SleepInternalLaunchState.LATENCY -> ""
            SleepInternalLaunchState.RESTFULNESS -> ""
            SleepInternalLaunchState.TIMING -> ""
            else->{
                ""
            }
        }
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
                background = R.drawable.back_hm_optimal
                textColor = R.color.white
            }

            1 -> {
                background = R.drawable.back_hm_fair
                textColor = R.color.color_trends_warning
            }

            else -> {
                background = R.drawable.back_hm_warning
                textColor = R.color.oreo_contributor_warning
            }
        }
        return Pair(background, textColor)
    }

    fun returnTrendsArrow(type: SleepInternalLaunchState): Int {
        val icon: Int = when (type) {
            SleepInternalLaunchState.RESPIRATORY_RATE,
            SleepInternalLaunchState.HRV,
            SleepInternalLaunchState.RESTING_HEART_RATE,
            SleepInternalLaunchState.BLOOD_OXYGEN,
            SleepInternalLaunchState.SKIN_TEMPERATURE
            -> R.drawable.ic_hm_tick
            else -> 0
        }
        return icon

    }

    fun hmDummyData(): OHealthMonTrendsDataModel {
        return OHealthMonTrendsDataModel(
            trendType = selectedLaunchMode,
            dayDate = "Monday  30 May, 2024",
            dspValue = "40",
            isShowOptimal = false,
            isShowHighlight = true,
            description = "Your resting HR seems to be higher than previous day. Allow yourself sufficient time for recovery by taking it slow."
        )
    }


}

