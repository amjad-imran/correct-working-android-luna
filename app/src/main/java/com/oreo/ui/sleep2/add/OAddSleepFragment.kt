package com.oreo.ui.sleep2.add

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOAddSleepBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.common.bottomSheet.SLEEP_TIME_REQUEST_KEY
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.OAddSleep
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OAddSleepFragment :
    BaseFragment<FragmentOAddSleepBinding>(FragmentOAddSleepBinding::inflate) {
    private val viewModel: OAddSleepViewModel by viewModels()
    private val mainViewModel: OreoMainViewModel by activityViewModels()


    override fun initListener() {
        binding.lytToolbar.tvSave.setOnClickListener {
            if (viewModel.startTimeSleep.day.isEmpty()) {
                uiController.onDisplayError("Please select start time")
                return@setOnClickListener
            }
            if (viewModel.endTimeSleep.day.isEmpty()) {
                uiController.onDisplayError("Please select end time")
                return@setOnClickListener
            }
            if(viewModel.getSleepDuration()< (3 * 60 * 60)){
                uiController.onDisplayError("Sleep duration should be minimum of 3 hours")
                return@setOnClickListener
            }


            viewModel.callApiToAddSleep()
        }
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.lytAddTime.lytStartTime.root.setOnClickListener {

            setFragmentResultListener(SLEEP_TIME_REQUEST_KEY) { _, bundle ->
                val addSleep = bundle.getParcelable<OAddSleep>("sleepTime")


                addSleep?.let {
                    viewModel.startTimeSleep = addSleep

                    if (viewModel.endTimeSleep.day.isEmpty()) {
                        setStartTimeBetween()
                    } else if (viewModel.startTimeSleep.day.equals("Today", true) &&
                        viewModel.endTimeSleep.day.equals("Today", true)
                    ) {

                        if (viewModel.startTimeSleep.hour.toInt() < viewModel.endTimeSleep.hour.toInt()) {
                            setStartTimeBetween()
                        } else {
                            uiController.onDisplayError("Start time should be less than end time")
                        }

                    } else if (viewModel.startTimeSleep.day.equals("Yesterday", true) &&
                        viewModel.endTimeSleep.day.equals("Yesterday", true)
                    ) {

                        if (viewModel.startTimeSleep.hour.toInt() < viewModel.endTimeSleep.hour.toInt()) {
                            setStartTimeBetween()
                        } else {
                            uiController.onDisplayError("Start time should be less than end time")
                        }

                    } else if (viewModel.startTimeSleep.day.equals("Yesterday", true) &&
                        viewModel.endTimeSleep.day.equals("Today", true)
                    ) {


                        setStartTimeBetween()
                    } else if (viewModel.startTimeSleep.day.equals("Today", true) &&
                        viewModel.endTimeSleep.day.equals("Yesterday", true)
                    ) {

                        uiController.onDisplayError("Please check end time")
                    }

                    updateCalculatedData()
                }

            }
            viewModel.startTimeSleep.title = getString(R.string.text_start_time)
            navigate(
                OAddSleepFragmentDirections.actionAddSleepFragmentToSleepTimeBottomSheet(
                    viewModel.startTimeSleep,
                    false
                )
            )
        }
        binding.lytAddTime.lytEndTime.root.setOnClickListener {
            if (binding.lytAddTime.lytStartTime.tvTimeValue.text == getString(R.string.text_enter)) {
                context.showShortToast(getString(R.string.text_select_start_time_first))
                return@setOnClickListener
            }
            setFragmentResultListener(SLEEP_TIME_REQUEST_KEY) { _, bundle ->
                val addSleep = bundle.getParcelable<OAddSleep>("sleepTime")

                addSleep?.let {
                    viewModel.endTimeSleep = addSleep


                    if (viewModel.startTimeSleep.day.equals("Today", true) &&
                        viewModel.endTimeSleep.day.equals("Today", true)
                    ) {
                        if (viewModel.startTimeSleep.hour.toInt() < viewModel.endTimeSleep.hour.toInt()) {
                            setEndTimeBetween()
                        } else {
                            uiController.onDisplayError(getString(R.string.text_end_time_greater_then_current_time))
                        }

                    } else if (viewModel.startTimeSleep.day.equals("Yesterday", true) &&
                        viewModel.endTimeSleep.day.equals("Yesterday", true)
                    ) {
                        if (viewModel.startTimeSleep.hour.toInt() < viewModel.endTimeSleep.hour.toInt()) {
                            setEndTimeBetween()
                        } else {
                            uiController.onDisplayError(getString(R.string.text_end_time_greater_then_current_time))
                        }

                    } else if (viewModel.startTimeSleep.day.equals("Yesterday", true) &&
                        viewModel.endTimeSleep.day.equals("Today", true)
                    ) {
                        setEndTimeBetween()
                    } else if (viewModel.startTimeSleep.day.equals("Today", true) &&
                        viewModel.endTimeSleep.day.equals("Yesterday", true)
                    ) {
                        uiController.onDisplayError("Please check end time")
                    }

                }


                updateCalculatedData()
            }

            viewModel.endTimeSleep.title = getString(R.string.text_end_time)
            navigate(
                OAddSleepFragmentDirections.actionAddSleepFragmentToSleepTimeBottomSheet(
                    viewModel.endTimeSleep,
                    viewModel.isStartDateToday()
                )
            )
        }
//        binding.tvDeleteSleep.setOnClickListener {
//            //wrote code to delete sleep
//        }
    }

    private fun updateCalculatedData() {
        val duration = viewModel.getSleepDuration()
        LOGS.d("updateCalculatedData $duration")
        val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(duration.toInt())
        if (hour > 0) {
            binding.lytDuration.tvHour.text = hour.toString()
        } else {
            binding.lytDuration.tvHour.text = "--"
        }
        if (minute >= 0) {

            binding.lytDuration.tvMinute.text = minute.toString()
        } else {
            binding.lytDuration.tvMinute.text = "--"

        }
    }

    private fun setEndTimeBetween() {
        val endTime = DateFormats.formatTimeWithAmPm(
            viewModel.endTimeSleep.hour.toInt(),
            viewModel.endTimeSleep.minute.toInt()
        )
        viewModel.isEndTimeSelected = true
        binding.lytAddTime.lytEndTime.tvTimeValue.setTextColor(resources.getColor(R.color.white))
        binding.lytAddTime.lytEndTime.tvTimeValue.text = "${viewModel.endTimeSleep.day}, $endTime"
        if (viewModel.isStartTimeSelected && viewModel.isEndTimeSelected) {
            binding.lytToolbar.tvSave.enable()
        }
    }

    private fun setStartTimeBetween() {
        val startTime = DateFormats.formatTimeWithAmPm(
            viewModel.startTimeSleep.hour.toInt(),
            viewModel.startTimeSleep.minute.toInt()
        )
        binding.lytAddTime.lytStartTime.tvTimeValue.setTextColor(resources.getColor(R.color.white))
        binding.lytAddTime.lytStartTime.tvTimeValue.text =
            "${viewModel.startTimeSleep.day}, $startTime"
        viewModel.isStartTimeSelected = true
    }

    override fun subscribeObservers() {
        viewModel.addSleepResponse.observe(this) { it1 ->
            it1?.getContent().let {
                if (it == true) {
                    mainViewModel.dashTodayReload.value = Event(true)
                    navigateUpSafe()
                }
            }
        }
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(this) {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
    }

    private fun initUI() {

        binding.lytToolbar.tvTitle.text = getString(R.string.text_add_sleep)
        binding.lytToolbar.tvSave.text = getString(R.string.text_save)
        binding.lytToolbar.tvSave.disable()
        binding.lytToolbar.tvSave.setTextColor(
            ContextCompat.getColor(
                binding.tvDurationDesc.context,
                R.color.text_color_luna
            )
        )
        binding.lytDuration.tvHour.text = "--"
        binding.lytDuration.tvHour.setTextColor(
            ContextCompat.getColor(
                binding.tvDurationDesc.context,
                R.color.white
            )
        )
        binding.lytDuration.tvHourUnit.setTextColor(
            ContextCompat.getColor(
                binding.tvDurationDesc.context,
                R.color.white
            )
        )
        binding.lytDuration.tvMinute.text = "--"
        binding.lytDuration.tvMinute.setTextColor(
            ContextCompat.getColor(
                binding.tvDurationDesc.context,
                R.color.white
            )
        )
        binding.lytDuration.tvMinuteUnit.setTextColor(
            ContextCompat.getColor(
                binding.tvDurationDesc.context,
                R.color.white
            )
        )

        binding.lytAddTime.lytStartTime.tvTime.text = getString(R.string.text_start_time)
        binding.lytAddTime.lytEndTime.tvTime.text = getString(R.string.text_end_time)

        binding.lytAddTime.lytStartTime.tvTimeValue.text = getString(R.string.text_enter)
        binding.lytAddTime.lytStartTime.tvTimeValue.setTextColor(
            ContextCompat.getColor(
                binding.tvDurationDesc.context,
                R.color.white_48
            )
        )
        binding.lytAddTime.lytEndTime.tvTimeValue.text = getString(R.string.text_enter)
        binding.lytAddTime.lytEndTime.tvTimeValue.setTextColor(
            ContextCompat.getColor(
                binding.tvDurationDesc.context,
                R.color.white_48
            )
        )

//        if (args.launchMode == OAddSleepLaunchState.ADD) {
//            binding.tvDeleteSleep.gone()
//        } else {
//            binding.tvDeleteSleep.visible()
//        }
    }


}

enum class OAddSleepLaunchState {
    ADD, EDIT
}