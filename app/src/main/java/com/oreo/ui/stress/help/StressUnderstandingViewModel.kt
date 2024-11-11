package com.oreo.ui.stress.help

import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.StressUnderstandingOverview
import com.oreo.data.model.StressUnderstandingSubList
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class StressUnderstandingViewModel
@Inject
constructor(val sessionManager: SessionManager,
    private val resourcesProvider: ResourcesProvider) : BaseViewModel() {

    fun getData(): ArrayList<StressUnderstandingOverview> {
        val dataList = ArrayList<StressUnderstandingOverview>()

        dataList.add(
            StressUnderstandingOverview.ImageWithText(
                resourcesProvider.getString(R.string.text_what_is_stress),
                R.drawable.image_what_is_stress,
                resourcesProvider.getString(R.string.text_stress_content_1)
            )
        )

        dataList.add(
            StressUnderstandingOverview.ImageWithText(
                resourcesProvider.getString(R.string.text_what_are_the_different_stress_zones),
                R.drawable.image_stress_zone,
                resourcesProvider.getString(R.string.text_stress_content_2) +
                        resourcesProvider.getString(R.string.text_stress_content_3) +
                        resourcesProvider.getString(R.string.text_stress_content_4)
            )
        )

        dataList.add(
            StressUnderstandingOverview.ImageWithText(
                resourcesProvider.getString(R.string.text_making_sense_of_stress_tracking),
                R.drawable.image_stress_tracking,
                resourcesProvider.getString(R.string.text_stress_content_5) +
                        resourcesProvider.getString(R.string.text_stress_content_6)

            )
        )

        dataList.add(
            StressUnderstandingOverview.LottieWithText(
                resourcesProvider.getString(R.string.text_correlating_movement_stress),
                R.raw.anim_correlating_movement,
                resourcesProvider.getString(R.string.text_stress_content_7)
            )
        )

        dataList.add(
            StressUnderstandingOverview.LottieWithText(
                resourcesProvider.getString(R.string.text_mapping_out_non_activity_stress),
                R.raw.anim_mapping_non_activity,
                resourcesProvider.getString(R.string.text_stress_content_8)
            )
        )

        dataList.add(
            StressUnderstandingOverview.TextWithAdapter(
                resourcesProvider.getString(R.string.text_managing_acute_stress_short_term),
                resourcesProvider.getString(R.string.text_stress_content_9),
                getShortTermListText(),
                true
            )
        )

        dataList.add(
            StressUnderstandingOverview.TextWithAdapter(
                resourcesProvider.getString(R.string.text_managing_chronic_stress_long_term),
                resourcesProvider.getString(R.string.text_stress_content_10),
                getLongTermListText(),
                false
            )
        )

        return dataList
    }


    private fun getShortTermListText(): List<String> {
        val dataList = ArrayList<String>()
        dataList.add(resourcesProvider.getString(R.string.text_cultivating_new_lifestyle_habits))
        dataList.add(resourcesProvider.getString(R.string.text_practicing_breathwork))
        dataList.add(resourcesProvider.getString(R.string.text_meditating))
        dataList.add(resourcesProvider.getString(R.string.text_exercising))
        dataList.add(resourcesProvider.getString(R.string.text_connecting_with_loved_ones))
        dataList.add(resourcesProvider.getString(R.string.text_spending_time_in_nature))
        dataList.add(resourcesProvider.getString(R.string.text_ensuring_high_quality_sleep))
        dataList.add(resourcesProvider.getString(R.string.text_exploring_new_hobbies))
        return dataList
    }


    private fun getLongTermListText(): List<String> {
        val dataList = ArrayList<String>()
        dataList.add(resourcesProvider.getString(R.string.text_engaging_in_regular_exercise))
        dataList.add(resourcesProvider.getString(R.string.text_incorporating_cold_exposure_such_as_cold_showers_or_ice_baths))
        dataList.add(resourcesProvider.getString(R.string.text_deliberate_heat_exposure_like_saunas))
        dataList.add(resourcesProvider.getString(R.string.text_experimenting_with_intermittent))
        return dataList
    }
}