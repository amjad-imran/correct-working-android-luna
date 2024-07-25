package com.oreo.ui.sleep2.internal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.data.model.OHealthMonTrendsDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SkinTempInternalDetailsViewModel @Inject constructor(
    private val resourcesProvider: ResourcesProvider
) : BaseViewModel() {

    lateinit var selectedLaunchMode: SkinTempInternalLaunchState
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
            SkinTempInternalLaunchState.RESPIRATORY_RATE -> {
                Pair(
                    resourcesProvider.getString(R.string.text_respiratory_rate),
                    R.drawable.ic_respiratory_rate
                )
            }

            SkinTempInternalLaunchState.RESTING_HEART_RATE -> Pair(
                resourcesProvider.getString(R.string.text_resting_heart_rate),
                R.drawable.ic_resting_hr
            )

            SkinTempInternalLaunchState.HRV -> Pair(
                resourcesProvider.getString(R.string.text_hrv),
                R.drawable.ic_hrv
            )

            SkinTempInternalLaunchState.SKIN_TEMPERATURE -> Pair(
                resourcesProvider.getString(R.string.text_skin_temperature),
                R.drawable.ic_skin_tempreature
            )

            SkinTempInternalLaunchState.BLOOD_OXYGEN -> Pair(
                resourcesProvider.getString(R.string.text_blood_oxygen),
                R.drawable.ic_blood_oxygen
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
            SkinTempInternalLaunchState.RESPIRATORY_RATE.name.lowercase() -> selectedLaunchMode =
                SkinTempInternalLaunchState.RESPIRATORY_RATE

            SkinTempInternalLaunchState.RESTING_HEART_RATE.name.lowercase() -> selectedLaunchMode =
                SkinTempInternalLaunchState.RESTING_HEART_RATE

            SkinTempInternalLaunchState.HRV.name.lowercase() -> selectedLaunchMode =
                SkinTempInternalLaunchState.HRV

            SkinTempInternalLaunchState.SKIN_TEMPERATURE.name.lowercase() -> selectedLaunchMode =
                SkinTempInternalLaunchState.SKIN_TEMPERATURE

            SkinTempInternalLaunchState.BLOOD_OXYGEN.name.lowercase() -> selectedLaunchMode =
                SkinTempInternalLaunchState.BLOOD_OXYGEN

        }
    }

    fun getPostFixAbr(trendType: SkinTempInternalLaunchState): String {
        val abr: String = when (trendType) {
            SkinTempInternalLaunchState.SKIN_TEMPERATURE -> "°F - average"
            SkinTempInternalLaunchState.HRV -> "ms - average"
            SkinTempInternalLaunchState.RESPIRATORY_RATE -> "rpm - average"
            SkinTempInternalLaunchState.RESTING_HEART_RATE -> "bpm - average"
            SkinTempInternalLaunchState.BLOOD_OXYGEN -> "% - average"
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

    fun returnTrendsArrow(type: SkinTempInternalLaunchState): Int {
        val icon: Int = when (type) {
            SkinTempInternalLaunchState.RESPIRATORY_RATE,
            SkinTempInternalLaunchState.HRV,
            SkinTempInternalLaunchState.RESTING_HEART_RATE,
            SkinTempInternalLaunchState.BLOOD_OXYGEN,
                SkinTempInternalLaunchState.SKIN_TEMPERATURE
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

