package com.noisefit.ui.dashboard.feature.healthMonitor.meal

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentEditMealReminderBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit.ui.dashboard.feature.idle.FrequencyIn
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

const val EDIT_MEAL_REQUEST_KEY = "EDIT_MEAL_REQUEST_KEY"

@AndroidEntryPoint
class EditMealReminderFragment :
    BaseFragment<FragmentEditMealReminderBinding>(FragmentEditMealReminderBinding::inflate) {

    private val viewModel: MealReminderViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            viewModel.setSedentary(EditMealReminderFragmentArgs.fromBundle(it).reminder)
        }

        initUi()
        updateUi()
    }

    private fun initUi() {
        binding.lytEditReminder.apply {
            tvDurationData.gone()
            tvDurationText.gone()
            tvRepeatText.gone()
            tvRepeatData.gone()
        }
    }

    private fun updateUi() {
        updateFreq()
        setStartTimeBetween()
        setEndTimeBetween()

    }

    private fun updateFreq() {
        binding.lytEditReminder.tvFreqData.text =
            viewModel.getFrequencyValue(viewModel.sedentaryData.interval)
    }


    private fun setStartTimeBetween() {
        val startTime = DateFormats.formatTimeWithAmPm(
            viewModel.sedentaryData.startHour,
            viewModel.sedentaryData.startMinute
        )
        binding.lytEditReminder.tvStartTimeData.text = startTime
    }

    private fun setEndTimeBetween() {
        val endTime = DateFormats.formatTimeWithAmPm(
            viewModel.sedentaryData.endHour,
            viewModel.sedentaryData.endMinute
        )
        binding.lytEditReminder.tvEndTimeData.text = endTime
    }

    private fun updateReminder() {
        viewModel.sedentaryData = viewModel.generateDeviceReminderUpdateData(true)

        viewModel.setLoading(true)
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetMealReminder(
                viewModel.sedentaryData
            )
        )
    }

    override fun initListener() {

        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_meal_reminder_settings)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }
        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }

        binding.btnAllow.setOnClickListener {
            updateReminder()
        }

        binding.lytEditReminder.tvEndTimeData.setOnClickListener {
            setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
                val hourOfDay = bundle.getInt("hour")
                val minute = bundle.getInt("minute")

                if (DateFormats.compareTime(
                        hourOfDay,
                        minute,
                        viewModel.sedentaryData.startHour,
                        viewModel.sedentaryData.startMinute
                    ) <= 0
                ) {
                    context.showShortToast(getString(R.string.text_end_time_greater))
                } else if (hourOfDay == viewModel.sedentaryData.startHour && minute == viewModel.sedentaryData.startMinute) {
                    context.showShortToast(getString(R.string.text_start_end_time_should_be_different))
                } else if (!DateFormats.checkDifferenceInBtwInterval(
                        viewModel.sedentaryData.startHour,
                        viewModel.sedentaryData.startMinute,
                        hourOfDay,
                        minute,
                        viewModel.getIntervalInMinutes()
                    )
                ) {
                    context.showShortToast(getString(R.string.text_start_and_end_time_should_be_greater_than_the_selected_frequency))
                } else {
                    viewModel.sedentaryData.endHour = hourOfDay
                    viewModel.sedentaryData.endMinute = minute
                    setEndTimeBetween()
                }
            }


            navigate(
                EditMealReminderFragmentDirections.actionEditWalkReminderFragmentToTimeBottomSheet(
                    viewModel.sedentaryData.endHour,
                    viewModel.sedentaryData.endMinute,
                    1,
                    getString(R.string.text_end_time)
                )
            )

        }

        binding.lytEditReminder.tvStartTimeData.setOnClickListener {
            setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
                val hourOfDay = bundle.getInt("hour")
                val minute = bundle.getInt("minute")

                if (DateFormats.compareTime(
                        hourOfDay,
                        minute,
                        viewModel.sedentaryData.endHour,
                        viewModel.sedentaryData.endMinute
                    ) >= 0
                ) {
                    context.showShortToast(getString(R.string.text_start_time_less))
                } else if (hourOfDay == viewModel.sedentaryData.endHour && minute == viewModel.sedentaryData.endMinute) {
                    context.showShortToast(getString(R.string.text_start_end_time_should_be_different))
                } else if (!DateFormats.checkDifferenceInBtwInterval(
                        hourOfDay,
                        minute,
                        viewModel.sedentaryData.endHour,
                        viewModel.sedentaryData.endMinute,
                        viewModel.getIntervalInMinutes()
                    )
                ) {
                    context.showShortToast(getString(R.string.text_start_and_end_time_should_be_greater_than_the_selected_frequency))
                } else {
                    viewModel.sedentaryData.startHour = hourOfDay
                    viewModel.sedentaryData.startMinute = minute
                    setStartTimeBetween()
                }


            }


            navigate(
                EditMealReminderFragmentDirections.actionEditWalkReminderFragmentToTimeBottomSheet(
                    viewModel.sedentaryData.startHour,
                    viewModel.sedentaryData.startMinute,
                    1,
                    getString(R.string.text_start_time)
                )
            )
        }

        binding.lytEditReminder.tvFreqData.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                LOGS.i("$position | $selectedValue")
                viewModel.sedentaryData.interval = try {
                    selectedValue!!.split(" ")[0].toInt()
                } catch (exp: Exception) {
                    exp.printStackTrace()
                    1
                }
                updateFreq()

            }
            navigate(
                EditMealReminderFragmentDirections.actionEditWalkReminderFragmentToValueSelectorBottomSheet(
                    viewModel.getFrequencyValue(viewModel.sedentaryData.interval),
                    viewModel.frequencyList.value!!,
                    getString(R.string.text_freqency)
                )
            )

        }


    }

    override fun subscribeObservers() {

        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            val event = it.getContent() ?: return@observe
            when (event) {
                is UpdateDeviceDataCallback.MealDataUpdated -> {
                    viewModel.setLoading(false)
                    if (event.success) {
                        context.showShortToast(getString(R.string.text_meal_reminder_updated))
                        setFragmentResult(
                            EDIT_MEAL_REQUEST_KEY,
                            bundleOf("meal" to viewModel.sedentaryData)
                        )
                        navigateUpSafe()
                    }
                }
                else -> {}
            }
        }
        viewModel.frequencyIn.observe(this) {
            if (it != null) {
                if (it == FrequencyIn.NONE) {
                    binding.lytEditReminder.apply {
                        tvFreqData.gone()
                        tvFreqText.gone()
                    }
                }
            }
        }
    }

}