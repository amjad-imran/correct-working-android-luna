package com.oreo.ui.timelineScreen.addActivity.activities

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddSleepBinding
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
import com.oreo.ui.sleep2.add.OAddSleepFragmentDirections
import com.oreo.ui.sleep2.add.OAddSleepViewModel
import com.oreo.ui.timelineScreen.addActivity.AddActivityItemsEnum
import com.oreo.ui.timelineScreen.addActivity.AddActivityTimelineSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import kotlin.getValue

@AndroidEntryPoint
class AddSleepFragment :
    BaseFragment<FragmentAddSleepBinding>(FragmentAddSleepBinding::inflate) {

    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()
    private val viewModel: OAddSleepViewModel by viewModels()
    private val mainViewModel: OreoMainViewModel by activityViewModels()


    override fun initListener() {
        binding.lytSelected.setOnClickListener {
            sharedViewModel.loadFragmentByType(AddActivityItemsEnum.ACTIVITIES_LISTING)
        }
        binding.btnSave.setOnClickListener {
            if (mainViewModel.isDeviceConnected().not()) {
                context.showShortToast(getString(R.string.text_please_connect_your_ring_to_add_sleep))
                return@setOnClickListener
            }

            if (viewModel.startTimeSleep.day.isEmpty()) {
                uiController.onDisplayError(getString(R.string.text_please_select_start_time))
                return@setOnClickListener
            }
            if (viewModel.endTimeSleep.day.isEmpty()) {
                uiController.onDisplayError(getString(R.string.text_please_select_end_time))
                return@setOnClickListener
            }
            /*if (viewModel.getSleepDuration() < (3 * 60 * 60)) {
                uiController.onDisplayError("Sleep duration should be minimum of 3 hours")
                return@setOnClickListener
            }*/


            viewModel.callApiToAddSleep()
        }


        binding.lytCard.lytStartTime.setOnClickListener {

            parentFragment?.setFragmentResultListener(SLEEP_TIME_REQUEST_KEY) { _, bundle ->
                val addSleep = bundle.getParcelable<OAddSleep>("sleepTime")

                addSleep?.let {
                    val oldData = viewModel.startTimeSleep.copy()
                    viewModel.startTimeSleep = addSleep

                    if (addSleep.hour.toInt() == 0) {
                        viewModel.startTimeSleep = addSleep.apply {
                            this.day = "Today"
                        }
                    }

                    if (!viewModel.isEndTimeSelected) {
                        setStartTimeBetween()
                    } else if (viewModel.startTimeSleep.day.equals("Today", true) &&
                        viewModel.endTimeSleep.day.equals("Today", true)
                    ) {

                        val startTime = LocalTime.of(
                            viewModel.startTimeSleep.hour.toInt(),
                            viewModel.startTimeSleep.minute.toInt()
                        )
                        val endTime = LocalTime.of(
                            viewModel.endTimeSleep.hour.toInt(),
                            viewModel.endTimeSleep.minute.toInt()
                        )

                        if (startTime < endTime) {
                            setStartTimeBetween()
                        } else {
                            uiController.onDisplayError(getString(R.string.text_start_time_should_be_less_than_end_time))
                            viewModel.startTimeSleep = oldData
                        }

                    } else if (viewModel.startTimeSleep.day.equals("Yesterday", true) &&
                        viewModel.endTimeSleep.day.equals("Yesterday", true)
                    ) {
                        val startTime = LocalTime.of(
                            viewModel.startTimeSleep.hour.toInt(),
                            viewModel.startTimeSleep.minute.toInt()
                        )
                        val endTime = LocalTime.of(
                            viewModel.endTimeSleep.hour.toInt(),
                            viewModel.endTimeSleep.minute.toInt()
                        )

                        if (startTime < endTime) {
                            setStartTimeBetween()
                        } else {
                            uiController.onDisplayError(getString(R.string.text_start_time_should_be_less_than_end_time))
                            viewModel.startTimeSleep = oldData
                        }

                    } else if (viewModel.startTimeSleep.day.equals("Yesterday", true) &&
                        viewModel.endTimeSleep.day.equals("Today", true)
                    ) {


                        setStartTimeBetween()
                    } else if (viewModel.startTimeSleep.day.equals("Today", true) &&
                        viewModel.endTimeSleep.day.equals("Yesterday", true)
                    ) {

                        uiController.onDisplayError(getString(R.string.text_please_check_end_time))
                        viewModel.startTimeSleep = oldData
                    }

                    updateCalculatedData()
                }

            }
            viewModel.startTimeSleep.title = getString(R.string.text_start_time)


            val navController =
                NavHostFragment.Companion.findNavController(this@AddSleepFragment)

            navController.navigate(R.id.sleepTimeBottomSheet, bundleOf(
                "addSleep" to viewModel.startTimeSleep.copy(),
                "isStartDateToday" to false))
        }
        binding.lytCard.lytEndTime.setOnClickListener {
            if (binding.lytCard.tvStartTime.text == getString(R.string.text_enter)) {
                context.showShortToast(getString(R.string.text_select_start_time_first))
                return@setOnClickListener
            }
            parentFragment?.setFragmentResultListener(SLEEP_TIME_REQUEST_KEY) { _, bundle ->
                val addSleep = bundle.getParcelable<OAddSleep>("sleepTime")

                addSleep?.let {
                    val oldData = viewModel.endTimeSleep.copy()
                    viewModel.endTimeSleep = addSleep

                    if (viewModel.startTimeSleep.day.equals("Today", true) &&
                        viewModel.endTimeSleep.day.equals("Today", true)
                    ) {
                        val startTime = LocalTime.of(
                            viewModel.startTimeSleep.hour.toInt(),
                            viewModel.startTimeSleep.minute.toInt()
                        )
                        val endTime = LocalTime.of(
                            viewModel.endTimeSleep.hour.toInt(),
                            viewModel.endTimeSleep.minute.toInt()
                        )
                        if (startTime < endTime) {
                            setEndTimeBetween()
                        } else {
                            viewModel.endTimeSleep = oldData
                            uiController.onDisplayError(getString(R.string.text_end_time_must_be_later_than_the_start_time))
                        }

                    } else if (viewModel.startTimeSleep.day.equals("Yesterday", true) &&
                        viewModel.endTimeSleep.day.equals("Yesterday", true)
                    ) {
                        val startTime = LocalTime.of(
                            viewModel.startTimeSleep.hour.toInt(),
                            viewModel.startTimeSleep.minute.toInt()
                        )
                        val endTime = LocalTime.of(
                            viewModel.endTimeSleep.hour.toInt(),
                            viewModel.endTimeSleep.minute.toInt()
                        )
                        if (startTime < endTime) {
                            setEndTimeBetween()
                        } else {
                            viewModel.endTimeSleep = oldData
                            uiController.onDisplayError(getString(R.string.text_end_time_must_be_later_than_the_start_time))
                        }

                    } else if (viewModel.startTimeSleep.day.equals("Yesterday", true) &&
                        viewModel.endTimeSleep.day.equals("Today", true)
                    ) {
                        setEndTimeBetween()
                    } else if (viewModel.startTimeSleep.day.equals("Today", true) &&
                        viewModel.endTimeSleep.day.equals("Yesterday", true)
                    ) {
                        viewModel.endTimeSleep = oldData
                        uiController.onDisplayError(getString(R.string.text_please_check_end_time))
                    }
                }


                updateCalculatedData()
            }

            viewModel.endTimeSleep.title = getString(R.string.text_end_time)


            val navController =
                NavHostFragment.Companion.findNavController(this@AddSleepFragment)

            navController.navigate(R.id.sleepTimeBottomSheet, bundleOf(
                "addSleep" to viewModel.endTimeSleep.copy(),
                "isStartDateToday" to viewModel.isStartDateToday()))

        }
//        binding.tvDeleteSleep.setOnClickListener {
//            //wrote code to delete sleep
//        }

        binding.lytSelected.setOnClickListener {
            sharedViewModel.showDropdownDialog(binding.lytSelected, AddActivityItemsEnum.SLEEP)
        }
    }

    private fun updateCalculatedData() {
        val duration = viewModel.getSleepDuration()
        LOGS.d("updateCalculatedData $duration")
        val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(duration.toInt())

        val text = StringBuilder()
        text.append(if (hour > 0) {
            hour.toString()
        } else {
            "--"
        })
        text.append("hr")
        text.append(" ")

        text.append(if (minute >= 0) {
            minute.toString()
        } else {
            "--"
        })
        text.append("min")

        binding.lytCard.tvSleepDuration.text = text.toString()
    }

    private fun setEndTimeBetween() {
        val endTime = DateFormats.formatTimeWithAmPm(
            viewModel.endTimeSleep.hour.toInt(),
            viewModel.endTimeSleep.minute.toInt()
        )
        viewModel.isEndTimeSelected = true
        binding.lytCard.tvEndTime.setTextColor(resources.getColor(R.color.white))
        binding.lytCard.tvEndTime.text = "${viewModel.endTimeSleep.day}, $endTime"
        if (viewModel.isStartTimeSelected && viewModel.isEndTimeSelected) {
            binding.btnSave.enable()
        }
    }

    private fun setStartTimeBetween() {
        val startTime = DateFormats.formatTimeWithAmPm(
            viewModel.startTimeSleep.hour.toInt(),
            viewModel.startTimeSleep.minute.toInt()
        )
        binding.lytCard.tvStartTime.setTextColor(resources.getColor(R.color.white))
        binding.lytCard.tvStartTime.text =
            "${viewModel.startTimeSleep.day}, $startTime"
        viewModel.isStartTimeSelected = true
    }

    override fun subscribeObservers() {
        viewModel.addSleepResponse.observe(this) { it1 ->
            it1?.getContent().let {
                if (it == true) {
                    mainViewModel.sessionManager.reloadOnResume = true
                    sharedViewModel.navigateUp()
                    mainViewModel.sleepDashTodayReload.value = Event(true)
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
                uiController.displayProgressBar(true,"")
            } else {
                uiController.displayProgressBar(false,"")
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
    }

    private fun initUI() {

        binding.btnSave.disable()

        binding.lytCard.tvSleepDuration.text = "--hr --min"

        binding.lytCard.tvStartTime.text = getString(R.string.text_enter)
        binding.lytCard.tvEndTime.text = getString(R.string.text_enter)

//        if (args.launchMode == OAddSleepLaunchState.ADD) {
//            binding.tvDeleteSleep.gone()
//        } else {
//            binding.tvDeleteSleep.visible()
//        }
    }
}
