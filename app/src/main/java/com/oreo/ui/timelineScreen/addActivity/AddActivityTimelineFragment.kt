package com.oreo.ui.timelineScreen.addActivity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddActivityTimelineBinding
import com.noisefit_commans.data.model.timeline.ItemTimelineResponseModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.utils.Event
import com.oreo.ui.circadianAlignment.CircadianAlignmentViewModel
import com.oreo.ui.timelineScreen.addActivity.activities.ActivityListingFragment
import com.oreo.ui.timelineScreen.addActivity.activities.AddAlcoholFragment
import com.oreo.ui.timelineScreen.addActivity.activities.AddCaffeineFragment
import com.oreo.ui.timelineScreen.addActivity.activities.AddLightExposureFragment
import com.oreo.ui.timelineScreen.addActivity.activities.AddPeriodLogFragment
import com.oreo.ui.timelineScreen.addActivity.activities.AddRecoveryFragment
import com.oreo.ui.timelineScreen.addActivity.activities.AddSleepFragment
import com.oreo.ui.timelineScreen.addActivity.activities.AddSupplementsFragment
import com.oreo.ui.timelineScreen.addActivity.activities.AddWaterFragment
import com.oreo.ui.timelineScreen.addActivity.activities.AddWorkoutFragment
import com.oreo.ui.timelineScreen.meal.MealAiFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddActivityTimelineFragment :
    BaseFragment<FragmentAddActivityTimelineBinding>(FragmentAddActivityTimelineBinding::inflate) {

    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()
    val args: AddActivityTimelineFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val key = args.key
        args.srcKey?.let { sharedViewModel.sourceKey = it }
        sharedViewModel.showTimeline = try {
            args.showTimeline
        } catch (exp: Exception) {
            false
        }

        val editData: ItemTimelineResponseModel? = args.editData

        when (key) {
            CircadianAlignmentViewModel.light_exposure_key ->
                sharedViewModel.loadFragmentByType(AddActivityItemsEnum.LIGHT_EXPOSURE, editData)

            CircadianAlignmentViewModel.meal_window_key ->
                sharedViewModel.loadFragmentByType(AddActivityItemsEnum.MEAL, editData)

            CircadianAlignmentViewModel.caffeine_window_key ->
                sharedViewModel.loadFragmentByType(AddActivityItemsEnum.CAFFEINE, editData)

            CircadianAlignmentViewModel.sleep_key ->
                sharedViewModel.loadFragmentByType(AddActivityItemsEnum.SLEEP, editData)

            "supplements" ->
                sharedViewModel.loadFragmentByType(AddActivityItemsEnum.SUPPLEMENTS, editData)

            "recovery" ->
                sharedViewModel.loadFragmentByType(AddActivityItemsEnum.RECOVERY, editData)

            "alcohol" ->
                sharedViewModel.loadFragmentByType(AddActivityItemsEnum.ALCOHOL, editData)

            else -> sharedViewModel.loadFragmentByType(AddActivityItemsEnum.ACTIVITIES_LISTING)
        }
    }


    override fun initListener() {
        binding.ivClose.setOnClickListener {
            navigateUpSafe()
        }

        binding.ivDelete.setOnClickListener {
            if (args.editData?.id == null){
                return@setOnClickListener
            }
            sharedViewModel.deleteBtnClickedEvent.postValue(Event(true))
        }
    }

    override fun subscribeObservers() {
        sharedViewModel.loadFragment.observe(this) {
            it.getContent()?.let {
                val fragment = when (it.first) {
                    AddActivityItemsEnum.ACTIVITIES_LISTING -> {
                        ActivityListingFragment.newInstance().apply {
                            it.second?.let { editData ->
                                this.arguments = Bundle().apply {
                                    putParcelable("editData", editData)
                                }
                            }
                        }
                    }

                    AddActivityItemsEnum.MEAL -> {
                        MealAiFragment()
                    /*AddMealActivityTimelineFragment()*/
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
                        AddSleepFragment().apply {
                            it.second?.let { editData ->
                                this.arguments = Bundle().apply {
                                    putParcelable("editData", editData)
                                }
                            }
                        }
                    }

                    AddActivityItemsEnum.SLEEP -> {
                        AddSleepFragment().apply {
                            it.second?.let { editData ->
                                this.arguments = Bundle().apply {
                                    putParcelable("editData", editData)
                                }
                            }
                        }
                    }

                    AddActivityItemsEnum.SUPPLEMENTS -> {
                        AddSupplementsFragment().apply {
                            it.second?.let { editData ->
                                this.arguments = Bundle().apply {
                                    putParcelable("editData", editData)
                                }
                            }
                        }
                    }
                    AddActivityItemsEnum.ALCOHOL -> {
                        AddAlcoholFragment().apply {
                            it.second?.let { editData ->
                                this.arguments = Bundle().apply {
                                    putParcelable("editData", editData)
                                }
                            }
                        }
                    }
                    AddActivityItemsEnum.RECOVERY -> {
                        AddRecoveryFragment().apply {
                            it.second?.let { editData ->
                                this.arguments = Bundle().apply {
                                    putParcelable("editData", editData)
                                }
                            }
                        }
                    }
                }
                fragment?.let {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.childFragmentContainer, it)
                        .commit()
                }

                binding.ivDelete.setVisibilityByCondition(args.editData?.canBeEditedOrDeleted == 2 || args.editData?.canBeEditedOrDeleted == 3)
            }
        }
        sharedViewModel.navigateUp.observe(this) {
            it.getContent()?.let {
                if (sharedViewModel.showTimeline) {
                    navigateUpSafe()
                    navigate(R.id.timelineScreenFragment)
                } else {
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