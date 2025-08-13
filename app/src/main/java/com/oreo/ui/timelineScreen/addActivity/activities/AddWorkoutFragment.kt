package com.oreo.ui.timelineScreen.addActivity.activities

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.noisefit.data.local.AppStaticData
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddLightExposureBinding
import com.noisefit.luna.databinding.FragmentAddMealActivityTimelineBinding
import com.noisefit.luna.databinding.FragmentAddWorkoutBinding
import com.noisefit.luna.databinding.FragmentOAddWorkoutBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.data.model.OWorkoutListModal
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.ui.timelineScreen.addActivity.AddActivityItemsEnum
import com.oreo.ui.timelineScreen.addActivity.AddActivityTimelineSharedViewModel
import com.oreo.ui.workout.add.ADD_WORKOUT_REQUEST_KEY
import com.oreo.ui.workout.add.OAddWorkoutFragmentDirections
import com.oreo.ui.workout.add.OAddWorkoutViewModel
import com.oreo.ui.workout.add.SELECT_REQUEST_KEY
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AddWorkoutFragment :
    BaseFragment<FragmentAddWorkoutBinding>(FragmentAddWorkoutBinding::inflate) {

    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()

    private val viewModel: AddWorkoutViewModel by viewModels()
    //private val viewModel: OAddWorkoutViewModel by viewModels()
    private val mainViewModel: OreoMainViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userDayData =
            mainViewModel.userHealthData[DateFormats.getTodaysDateString(10)]

        setUi()
        viewModel.getWorkoutList(false)

    }

    private fun setUi() {

        setDefaultUIValue()

    }


    override fun initListener() {
        val navController =
            NavHostFragment.Companion.findNavController(this@AddWorkoutFragment)

        binding.lytSelected.setOnClickListener {
            sharedViewModel.loadFragmentByType(AddActivityItemsEnum.ACTIVITIES_LISTING)
        }
        binding.btnSave.setOnClickListener {
            viewModel.addWorkout()
        }

        setFragmentResultListener(SELECT_REQUEST_KEY) { _, bundle ->
            val workout = bundle.getParcelable<OWorkoutListModal>("workout")
            workout?.let {
                setWorkout(it)
            }
        }
        binding.lytCard.ivDropDown.setOnClickListener {
            if (viewModel.oWorkoutListModalResponse.value.isNullOrEmpty()) {
                viewModel.getWorkoutList(true)
            } else {
                goToSelectWorkout(viewModel.oWorkoutListModalResponse.value!!)
            }
        }

        binding.lytCard.lytTimePicker.setOnClickListener {
            /*parentFragment?.setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
                val hourOfDay = bundle.getInt("hour")
                val minute = bundle.getInt("minute")

                val time = LocalTime.of(hourOfDay, minute)
                if (time > LocalTime.now()) {
                    context.showShortToast("Time cannot be in future") //TODO message change
                    return@setFragmentResultListener
                }

                viewModel.lightTime.postValue(time)

            }


            navController.navigate(
                R.id.timeBottomSheet,
                bundleOf(
                    "hour" to viewModel.lightTime.value!!.hour,
                    "minute" to viewModel.lightTime.value!!.minute,
                    "hourOther" to 0,
                    "minuteOther" to 0,
                    "isStart" to 1,
                    "unitPosition" to 1,
                    "title" to getString(R.string.text_time)
                )
            )*/
        }

        binding.lytCard.lytDuration.setOnClickListener {
            /*parentFragment?.setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let { it1 ->
                    val minString = it1.split(" ").firstOrNull()
                    viewModel.duration.postValue(
                        minString?.toLongOrNull() ?: viewModel.defaultMinutes
                    )
                }

            }
            navController?.navigate(
                R.id.valueSelectorBottomSheet,
                bundleOf(
                    "selectedValue" to "${viewModel.duration.value} min",
                    "selectionList" to AppStaticData.getLightExposureDurationValues(),
                    "title" to getString(R.string.text_duration)
                )
            )*/
        }
    }

    override fun subscribeObservers() {
        viewModel.updateCalculatedData.observe(this) {
            it.getContent()?.let {
                updateCalculatedData()
            }
        }
        viewModel.oWorkoutListModalResponse.observe(this) {
            it?.let {
                goToSelectWorkout(it)
            }
        }

        viewModel.addWorkoutResponse.observe(this) {
            it?.let {

                viewModel.sessionManager.saveSportsActivities(listOf(it.first))

                mainViewModel.sessionManager.reloadTodayData.postValue(
                    Event(true)
                )
                mainViewModel.sessionManager.forceSyncData.postValue(Event(true))

                //mainViewModel.reloadTodaysData()

                setFragmentResult(
                    ADD_WORKOUT_REQUEST_KEY,
                    bundleOf(
                        "allow" to true,
                        "workId" to it.second,
                        "actName" to it.first.getFormattedActivityName()
                    )

                )
                context.showShortToast(getString(R.string.text_workout_added_successfully))

                uiController.logAppEvent(
                    MoEngageLunaAppEvents.past_workout_entry,
                    hashMapOf("source" to "activity",
                        "description" to "workout_start",
                        "workout_name" to "${it.first.activityType}")
                )
                navigateUpSafe()
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }


        /*viewModel.lightTime.observe(this) {
            binding.lytCard.tvTime.text =
                it.format(DateTimeFormatter.ofPattern("hh:mm:a")).uppercase()
        }
        viewModel.lightDuration.observe(this) {
            binding.lytCard.tvDuration.text =
                "$it min"//todo change to hour minute if greater than 60 min
        }
        viewModel.onAddSuccess.observe(this) {
            it.getContent()?.let {
                sharedViewModel.navigateUp()
            }
        }*/

    }

    private fun updateCalculatedData() {



        enableSaveBtn()

    }

    private fun enableSaveBtn() {


        if ((viewModel.duration.value!! > 0 &&
                    viewModel.selectedWorkout != null)
        ) {
            binding.btnSave.enable()

        } else {
            binding.btnSave.disable()
        }
    }

    private fun goToSelectWorkout(workoutList: List<OWorkoutListModal>) {
        navigate(
            OAddWorkoutFragmentDirections.actionNavigationAddWorkoutFragToSelectWorkoutFragment(
                workoutList.toTypedArray()
            )
        )
    }

    private fun setWorkout(workout: OWorkoutListModal) {
        viewModel.selectedWorkout = workout

        context?.let { ctx ->
            binding.lytCard.ivWorkoutImage.loadImage(ctx, workout.iconUrl)
        }

        binding.lytCard.tvWorkout.text = workout.getTranslatedActivityName()

        updateCalculatedData()
    }

    private fun setDefaultUIValue() {
        binding.lytCard.ivWorkoutImage.setImageResource(R.drawable.ic_o_workout_new)
        //binding.lytCaloriesBurn.tvDurationValue.text = "--"
        //binding.lytCaloriesBurn.tvDurationUnit.text = getString(R.string.text_min)
        //binding.lytCaloriesBurn.tvCalBurnValue.text = "--"
        //binding.lytCaloriesBurn.tvCalBurnUnit.text = getString(R.string.text_kcal)

        binding.lytCard.tvTime.text =getString(R.string.text_enter)

    }

}