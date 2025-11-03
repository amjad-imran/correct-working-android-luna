package com.oreo.ui.timelineScreen.addActivity.activities

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddMealActivityTimelineBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.ui.timelineScreen.addActivity.ActivitySelectorDialog
import com.oreo.ui.timelineScreen.addActivity.AddActivityItemsEnum
import com.oreo.ui.timelineScreen.addActivity.AddActivityTimelineSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AddMealActivityTimelineFragment :
    BaseFragment<FragmentAddMealActivityTimelineBinding>(FragmentAddMealActivityTimelineBinding::inflate) {

    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()
    private val viewModel: AddMealViewModel by viewModels()

    private val mainViewModel: OreoMainViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUi()
    }

    private fun setUi() {

    }


    override fun initListener() {
        binding.lytSelected.setOnClickListener {

          /*  val location = IntArray(2)
            it.getLocationOnScreen(location)
            val dialog = ActivitySelectorDialog.newInstance(location[1])
            dialog.show(parentFragmentManager, "ProfileDialog")*/


            sharedViewModel.loadFragmentByType(AddActivityItemsEnum.ACTIVITIES_LISTING)
        }

        binding.btnSave.setOnClickListener {
            viewModel.mealTime.value?.let { time ->
                //
                sharedViewModel.sourceKey?.let { sourceKey ->
                    sharedViewModel.sessionManager.logMoEngageAppEvent(
                        MoEngageLunaAppEvents.insight_logged,
                        HashMap<String, Any>().apply {
                            this["source"] = sourceKey
                            if(sourceKey.equals("circadian")){
                                this["target"] = "meal"
                            }else {
                                this["log_category"] = "meal"
                            }
                        }
                    )
                }
                //
                viewModel.logMeal(time)
            }
        }

        binding.lytAddMeal.lytTimePicker.setOnClickListener {
            parentFragment?.setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
                val hourOfDay = bundle.getInt("hour")
                val minute = bundle.getInt("minute")

                val time = LocalTime.of(hourOfDay, minute)
                if (time > LocalTime.now()) {
                    context.showShortToast("Time cannot be in future") //TODO message change
                    return@setFragmentResultListener
                }

                viewModel.mealTime.postValue(time)

            }

            val navController =
                NavHostFragment.Companion.findNavController(this@AddMealActivityTimelineFragment)

            navController.navigate(
                R.id.timeBottomSheet,
                bundleOf(
                    "hour" to viewModel.mealTime.value!!.hour,
                    "minute" to viewModel.mealTime.value!!.minute,
                    "hourOther" to 0,
                    "minuteOther" to 0,
                    "isStart" to 1,
                    "unitPosition" to 1,
                    "title" to getString(R.string.text_time)
                )
            )
        }

        binding.lytSelected.setOnClickListener {
            sharedViewModel.showDropdownDialog(binding.lytSelected, AddActivityItemsEnum.MEAL)
        }
    }

    override fun subscribeObservers() {
        viewModel.mealTime.observe(this) {
            binding.lytAddMeal.tvTime.text =
                it.format(DateTimeFormatter.ofPattern("h:mm a")).uppercase()
        }
        viewModel.onAddSuccess.observe(this){
            it.getContent()?.let {
                mainViewModel.sessionManager.reloadOnResume = true
                sharedViewModel.navigateUp()
            }
        }

        //
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }

}