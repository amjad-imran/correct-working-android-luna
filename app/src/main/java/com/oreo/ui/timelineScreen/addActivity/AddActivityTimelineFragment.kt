package com.oreo.ui.timelineScreen.addActivity

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.moengage.core.internal.utils.showToast
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddActivityTimelineBinding
import com.noisefit_commans.data.model.timeline.ItemTimelineResponseModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
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

        val lunaOption = args.lunaOption

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

            "nap" ->
                sharedViewModel.loadFragmentByType(AddActivityItemsEnum.SLEEP, editData)

            "supplements" ->
                sharedViewModel.loadFragmentByType(AddActivityItemsEnum.SUPPLEMENTS, editData, lunaOption)

            "recovery" ->
                sharedViewModel.loadFragmentByType(AddActivityItemsEnum.RECOVERY, editData, lunaOption)

            "alcohol" ->
                sharedViewModel.loadFragmentByType(AddActivityItemsEnum.ALCOHOL, editData)

            "symptom" ->
                sharedViewModel.loadFragmentByType(AddActivityItemsEnum.CYCLE_LOG, editData)

            "workout" ->
                sharedViewModel.loadFragmentByType(AddActivityItemsEnum.WORKOUT, editData)

            else -> sharedViewModel.loadFragmentByType(AddActivityItemsEnum.ACTIVITIES_LISTING)
        }
    }


    override fun initListener() {
        binding.ivClose.setOnClickListener {
            navigateUpSafe()
        }

        binding.ivDelete.setOnClickListener {
            if (args.editData?.id == null){
                showToast(requireContext(),
                    getString(R.string.text_something_went_wrong_please_try_again))
                return@setOnClickListener
            }
            sharedViewModel.deleteBtnClickedEvent.postValue(Event(true))
            sharedViewModel.deleteBtnClickedEvent.value = Event(null)
        }
    }

    override fun subscribeObservers() {
        sharedViewModel.loadFragment.observe(this) {
            it.getContent()?.let {
                var titleTxt = getString(R.string.text_add_activity)
                val fragment = when (it.first) {
                    AddActivityItemsEnum.ACTIVITIES_LISTING -> {
                        titleTxt = getString(R.string.text_add_activity)
                        ActivityListingFragment.newInstance().apply {
                            it.second?.let { editData ->
                                this.arguments = Bundle().apply {
                                    putParcelable("editData", editData)
                                }
                            }
                        }
                    }

                    AddActivityItemsEnum.MEAL -> {
                        null
                        //MealAiFragment()
                    /*AddMealActivityTimelineFragment()*/
                    }

                    AddActivityItemsEnum.LIGHT_EXPOSURE -> {
                        titleTxt = getString(R.string.text_add_light_exposure)
                        AddLightExposureFragment().apply {
                            it.second?.let { editData ->
                                this.arguments = Bundle().apply {
                                    putParcelable("editData", editData)
                                }
                            }
                        }
                    }

                    AddActivityItemsEnum.WORKOUT -> {
                        titleTxt = getString(R.string.text_add_workout)
                        AddWorkoutFragment().apply {
                            it.second?.let { editData ->
                                this.arguments = Bundle().apply {
                                    putParcelable("editData", editData)
                                }
                            }
                        }
                    }

                    AddActivityItemsEnum.CAFFEINE -> {
                        titleTxt = getString(R.string.text_add_caffeine_intake)
                        AddCaffeineFragment().apply {
                            it.second?.let { editData ->
                                this.arguments = Bundle().apply {
                                    putParcelable("editData", editData)
                                }
                            }
                        }
                    }

                    AddActivityItemsEnum.WATER -> {
                        titleTxt = getString(R.string.text_add_water)
                        AddWaterFragment()
                    }

                    AddActivityItemsEnum.CYCLE_LOG -> {
                        titleTxt = "Add Period Symptoms"
                        AddPeriodLogFragment().apply {
                            it.second?.let { editData ->
                                this.arguments = Bundle().apply {
                                    putParcelable("editData", editData)
                                }
                            }
                        }
                    }

                    AddActivityItemsEnum.NAP -> {
//                        titleTxt = getString(R.string.text_add_water)
                        AddSleepFragment().apply {
                            it.second?.let { editData ->
                                this.arguments = Bundle().apply {
                                    putParcelable("editData", editData)
                                }
                            }
                        }
                    }

                    AddActivityItemsEnum.SLEEP -> {
                        titleTxt = getString(R.string.text_add_sleep2)
                        AddSleepFragment().apply {
                            val bundle = Bundle()
                            if("timeline".equals(args.srcKey)){
                                bundle.putString("srcKey", args.srcKey)
                            }
                            it.second?.let { editData ->
                                bundle.putParcelable("editData", editData)
                            }

                            this.arguments = bundle
                        }
                    }

                    AddActivityItemsEnum.SUPPLEMENTS -> {
                        titleTxt = getString(R.string.text_add_supplements)
                        AddSupplementsFragment().apply {
                            it.second?.let { editData ->
                                this.arguments = Bundle().apply {
                                    putParcelable("editData", editData)
                                }
                            }
                            it.third?.let { lunaOpt ->
                                this.arguments = Bundle().apply {
                                    putString("lunaOption", lunaOpt)
                                }
                            }
                        }
                    }
                    AddActivityItemsEnum.ALCOHOL -> {
                        titleTxt = getString(R.string.text_add_alcohol_intake)
                        AddAlcoholFragment().apply {
                            it.second?.let { editData ->
                                this.arguments = Bundle().apply {
                                    putParcelable("editData", editData)
                                }
                            }
                        }
                    }
                    AddActivityItemsEnum.RECOVERY -> {
                        titleTxt = getString(R.string.text_add_recovery)
                        AddRecoveryFragment().apply {
                            it.second?.let { editData ->
                                this.arguments = Bundle().apply {
                                    putParcelable("editData", editData)
                                }
                            }
                            it.third?.let { lunaOpt ->
                                this.arguments = Bundle().apply {
                                    putString("lunaOption", lunaOpt)
                                }
                            }
                        }
                    }
                }

                binding.textView191.text = titleTxt
                fragment?.let {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.childFragmentContainer, it)
                        .commit()
                }

                if(
                    args.editData?.canBeEditedOrDeleted == 2 ||
                    args.editData?.canBeEditedOrDeleted == 3
                ){
                    binding.ivDelete.visible()
                }else{
                    binding.ivDelete.invisible()
                }

                if(it.first== AddActivityItemsEnum.MEAL){
                    navigateUpSafe()
                    navigate(R.id.mealAiFragment, bundleOf("mealData" to null))
                }

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