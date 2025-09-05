package com.oreo.ui.timelineScreen.addActivity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddActivityTimelineBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.ui.circadianAlignment.CircadianAlignmentViewModel
import com.oreo.ui.timelineScreen.addActivity.activities.ActivityListingFragment
import com.oreo.ui.timelineScreen.addActivity.activities.AddCaffeineFragment
import com.oreo.ui.timelineScreen.addActivity.activities.AddLightExposureFragment
import com.oreo.ui.timelineScreen.addActivity.activities.AddMealActivityTimelineFragment
import com.oreo.ui.timelineScreen.addActivity.activities.AddPeriodLogFragment
import com.oreo.ui.timelineScreen.addActivity.activities.AddSleepFragment
import com.oreo.ui.timelineScreen.addActivity.activities.AddWaterFragment
import com.oreo.ui.timelineScreen.addActivity.activities.AddWorkoutFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddActivityTimelineFragment :
    BaseFragment<FragmentAddActivityTimelineBinding>(FragmentAddActivityTimelineBinding::inflate) {

    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()
    val args: AddActivityTimelineFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val key = arguments?.getString("key")
        arguments?.getString("srcKey")?.let { sharedViewModel.sourceKey= it }
        when(key){
            CircadianAlignmentViewModel.light_exposure_key ->
                sharedViewModel.loadFragmentByType(AddActivityItemsEnum.LIGHT_EXPOSURE)

            CircadianAlignmentViewModel.meal_window_key ->
                sharedViewModel.loadFragmentByType(AddActivityItemsEnum.MEAL)

            CircadianAlignmentViewModel.caffeine_window_key ->
                sharedViewModel.loadFragmentByType(AddActivityItemsEnum.CAFFEINE)

            CircadianAlignmentViewModel.sleep_key ->
                sharedViewModel.loadFragmentByType(AddActivityItemsEnum.SLEEP)

            else -> sharedViewModel.loadFragmentByType(AddActivityItemsEnum.ACTIVITIES_LISTING)
        }
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
                        AddWorkoutFragment()
                    }

                    AddActivityItemsEnum.CAFFEINE -> {
                        AddCaffeineFragment()
                    }

                    AddActivityItemsEnum.WATER -> {
                        AddWaterFragment()
                    }

                    AddActivityItemsEnum.CYCLE_LOG -> {
                        AddPeriodLogFragment()
                    }

                    AddActivityItemsEnum.NAP -> {
                        AddSleepFragment()
                    }
                    AddActivityItemsEnum.SLEEP -> {
                        AddSleepFragment()
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
                if(args.showTimeline){
                    navigateUpSafe()
                    navigate(R.id.timelineScreenFragment)
                }else{
                    navigateUpSafe()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharedViewModel.sourceKey = null
    }

}