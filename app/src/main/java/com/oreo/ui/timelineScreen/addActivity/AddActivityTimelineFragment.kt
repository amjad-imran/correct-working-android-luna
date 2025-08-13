package com.oreo.ui.timelineScreen.addActivity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddActivityTimelineBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.visible
import com.oreo.ui.timelineScreen.addActivity.activities.ActivityListingFragment
import com.oreo.ui.timelineScreen.addActivity.activities.AddLightExposureFragment
import com.oreo.ui.timelineScreen.addActivity.activities.AddMealActivityTimelineFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddActivityTimelineFragment :
    BaseFragment<FragmentAddActivityTimelineBinding>(FragmentAddActivityTimelineBinding::inflate) {

    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.loadFragmentByType(AddActivityItemsEnum.ACTIVITIES_LISTING)
    }


    override fun initListener() {
        binding.ivClose.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        sharedViewModel.loadFragment.observe(this) {
            it.getContent()?.let {
                val fragment = when (it) {
                    AddActivityItemsEnum.ACTIVITIES_LISTING -> {
                        ActivityListingFragment.newInstance()
                    }

                    AddActivityItemsEnum.MEAL -> {
                        AddMealActivityTimelineFragment()
                    }

                    AddActivityItemsEnum.LIGHT_EXPOSURE -> {
                        AddLightExposureFragment()
                    }

                    AddActivityItemsEnum.WORKOUT -> {
                        AddMealActivityTimelineFragment()
                    }

                    AddActivityItemsEnum.CAFFEINE -> {
                        AddMealActivityTimelineFragment()
                    }

                    AddActivityItemsEnum.WATER -> {
                        AddMealActivityTimelineFragment()
                    }

                    AddActivityItemsEnum.CYCLE_LOG -> {
                        AddMealActivityTimelineFragment()
                    }

                    AddActivityItemsEnum.NAP -> {
                        AddMealActivityTimelineFragment()
                    }
                    AddActivityItemsEnum.SLEEP -> {
                        AddMealActivityTimelineFragment()
                    }
                }
                fragment?.let {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.childFragmentContainer, it)
                        .commit()
                }

            }
        }
        sharedViewModel.navigateUp.observe(this){
            it.getContent()?.let {
                navigateUpSafe()
            }
        }
    }


}