package com.oreo.ui.timelineScreen.addActivity.activities

import android.os.Bundle
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentActivityListingBinding
import com.noisefit.ui.myDevice.DashboardWfFragment
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.timeline.addActivityTimelineModels.AddActivityListTimelineModel
import com.oreo.ui.timelineScreen.addActivity.ActivitiesListAdapter
import com.oreo.ui.timelineScreen.addActivity.AddActivityItemsEnum
import com.oreo.ui.timelineScreen.addActivity.AddActivityTimelineSharedViewModel
import kotlin.getValue


class ActivityListingFragment :
    BaseFragment<FragmentActivityListingBinding>(FragmentActivityListingBinding::inflate) {

    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()


    companion object {
        @JvmStatic
        fun newInstance() =
            ActivityListingFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    private val activitiesListAdapter by lazy {
        ActivitiesListAdapter() {
            sharedViewModel.loadFragmentByType(it.type)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
    }


    private fun setRecycler() {
        binding.rvActivities.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = activitiesListAdapter
        }
        activitiesListAdapter.updateDataSet(getAllActivityListMap())
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

    private fun getAllActivityListMap() = arrayListOf(
        AddActivityListTimelineModel(
            name = getString(R.string.text_meal_intake),
            type = AddActivityItemsEnum.MEAL,
            titleColor = "#FFE3B2".toColorInt()
        ), AddActivityListTimelineModel(
            name = getString(R.string.text_light_exposure),
            type = AddActivityItemsEnum.LIGHT_EXPOSURE,
            titleColor = "#FFE1CF".toColorInt()
        ),
        AddActivityListTimelineModel(
            name = getString(R.string.text_caffeine_intake),
            type = AddActivityItemsEnum.CAFFEINE,
            titleColor = "#DCA58E".toColorInt()
        ),
        AddActivityListTimelineModel(
            name = getString(R.string.text_workout),
            type = AddActivityItemsEnum.WORKOUT,
            titleColor = "#78C3F9".toColorInt()
        ),
        AddActivityListTimelineModel(
            name = getString(R.string.text_water_consumption),
            type = AddActivityItemsEnum.WATER,
            titleColor = "#8EF1C3".toColorInt()
        ),
        AddActivityListTimelineModel(
            name = getString(R.string.text_period_started),
            type = AddActivityItemsEnum.CYCLE_LOG,
            titleColor = "#F18EBD".toColorInt()
        ),
        AddActivityListTimelineModel(
            name = getString(R.string.text_nap),
            type = AddActivityItemsEnum.NAP,
            titleColor = "#A8A8ED".toColorInt()
        ),
        AddActivityListTimelineModel(
            name = getString(R.string.text_sleep),
            type = AddActivityItemsEnum.SLEEP,
            titleColor = "#C5A8ED".toColorInt()
        )
    )


}