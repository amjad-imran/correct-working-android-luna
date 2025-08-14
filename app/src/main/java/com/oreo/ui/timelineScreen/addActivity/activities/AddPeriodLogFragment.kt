package com.oreo.ui.timelineScreen.addActivity.activities

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.noisefit.data.local.AppStaticData
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddCaffeineBinding
import com.noisefit.luna.databinding.FragmentAddLightExposureBinding
import com.noisefit.luna.databinding.FragmentAddMealActivityTimelineBinding
import com.noisefit.luna.databinding.FragmentAddPeriodBinding
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.ui.timelineScreen.addActivity.AddActivityItemsEnum
import com.oreo.ui.timelineScreen.addActivity.AddActivityTimelineSharedViewModel
import com.oreo.ui.workout.add.OAddWorkoutFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AddPeriodLogFragment :
    BaseFragment<FragmentAddPeriodBinding>(FragmentAddPeriodBinding::inflate) {

    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()
    private val viewModel: AddPeriodViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUi()
    }

    private fun setUi() {

    }


    override fun initListener() {
        val navController =
            NavHostFragment.Companion.findNavController(this@AddPeriodLogFragment)

        binding.lytSelected.setOnClickListener {
            sharedViewModel.loadFragmentByType(AddActivityItemsEnum.ACTIVITIES_LISTING)
        }
        binding.btnSave.setOnClickListener {

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

                viewModel.caffeineTime.postValue(time)

            }


            navController.navigate(
                R.id.timeBottomSheet,
                bundleOf(
                    "hour" to viewModel.caffeineTime.value!!.hour,
                    "minute" to viewModel.caffeineTime.value!!.minute,
                    "hourOther" to 0,
                    "minuteOther" to 0,
                    "isStart" to 1,
                    "unitPosition" to 1,
                    "title" to getString(R.string.text_time)
                )
            )*/
        }

       /* binding.lytCard.lytDuration.setOnClickListener {
            parentFragment?.setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let { it1 ->
                    val minString = it1.split(" ").firstOrNull()
                    viewModel.lightDuration.postValue(minString?.toLongOrNull()?:viewModel.defaultMinutes)
                }

            }
            navController?.navigate(
                R.id.valueSelectorBottomSheet,
                bundleOf(
                    "selectedValue" to "${viewModel.lightDuration.value} min",
                    "selectionList" to AppStaticData.getLightExposureDurationValues(),
                   "title" to getString(R.string.text_duration)
                )
            )
        }*/
    }

    override fun subscribeObservers() {


    }

}