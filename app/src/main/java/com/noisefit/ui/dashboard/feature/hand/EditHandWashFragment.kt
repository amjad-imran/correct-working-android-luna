package com.noisefit.ui.dashboard.feature.hand

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import com.noisefit.R
import com.noisefit.data.local.AppStaticData
import com.noisefit.databinding.FragmentEditHandWashBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.dashboard.feature.idle.FrequencyIn
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

const val EDIT_HAND_REQUEST_KEY = "EDIT_HAND_REQUEST_KEY"

@AndroidEntryPoint
class EditHandWashFragment :
    BaseFragment<FragmentEditHandWashBinding>(FragmentEditHandWashBinding::inflate) {
    private val TAG = "EditHandWashFragment"
    private val viewModel: HandWashReminderViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            viewModel.setHandWash(EditHandWashFragmentArgs.fromBundle(it).handwash)
        }

        initUi()
        updateUi()
    }

    private fun initUi() {
        binding.lytEditReminder.apply {
            tvRepeatData.gone()
            tvRepeatText.gone()
        }
    }

    private fun updateUi() {
        updateFreq()
        setStartTimeBetween()
        setEndTimeBetween()
        updateDuration()
    }

    private fun updateFreq() {
        binding.lytEditReminder.tvFreqData.text =
            viewModel.getFrequencyValue(viewModel.handwash.frequency)
    }


    private fun setStartTimeBetween() {
        val startTime = DateFormats.formatTimeWithAmPm(
            viewModel.handwash.startHour,
            viewModel.handwash.startMinute
        )
        binding.lytEditReminder.tvStartTimeData.text = startTime
    }

    private fun setEndTimeBetween() {
        val endTime = DateFormats.formatTimeWithAmPm(
            viewModel.handwash.endHour,
            viewModel.handwash.endMinute
        )
        binding.lytEditReminder.tvEndTimeData.text = endTime
    }

    private fun updateDuration() {
        binding.lytEditReminder.tvDurationData.text =
            viewModel.getDurationValue(viewModel.handwash.duration)
    }
    private fun updateReminder() {
        viewModel.handwash = viewModel.generateDeviceReminderUpdateData(true)

        viewModel.setLoading(true)
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetHandWashing(
                viewModel.handwash
            )
        )
    }

    override fun initListener() {

        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_hand_wash_settings)
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
                        viewModel.handwash.startHour,
                        viewModel.handwash.startMinute
                    ) <= 0
                ) {
                    context.showShortToast(getString(R.string.text_end_time_greater))
                } else if (hourOfDay == viewModel.handwash.startHour && minute == viewModel.handwash.startMinute) {
                    context.showShortToast(getString(R.string.text_start_end_time_should_be_different))
                } else if (!DateFormats.checkDifferenceInBtwInterval(
                        viewModel.handwash.startHour,
                        viewModel.handwash.startMinute,
                        hourOfDay,
                        minute,
                        viewModel.getIntervalInMinutes()
                    )
                ) {
                    context.showShortToast(getString(R.string.text_start_and_end_time_should_be_greater_than_the_selected_frequency))
                } else {
                    viewModel.handwash.endHour = hourOfDay
                    viewModel.handwash.endMinute = minute
                    setEndTimeBetween()
                }
            }


            navigate(
                EditHandWashFragmentDirections.actionEditHandWashFragmentToTimeBottomSheet(
                    viewModel.handwash.endHour,
                    viewModel.handwash.endMinute,
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
                        viewModel.handwash.endHour,
                        viewModel.handwash.endMinute
                    ) >= 0
                ) {
                    context.showShortToast(getString(R.string.text_start_time_less))
                } else if (hourOfDay == viewModel.handwash.endHour && minute == viewModel.handwash.endMinute) {
                    context.showShortToast(getString(R.string.text_start_end_time_should_be_different))
                } else if (!DateFormats.checkDifferenceInBtwInterval(
                        hourOfDay,
                        minute,
                        viewModel.handwash.endHour,
                        viewModel.handwash.endMinute,
                        viewModel.getIntervalInMinutes()
                    )
                ) {
                    context.showShortToast(getString(R.string.text_start_and_end_time_should_be_greater_than_the_selected_frequency))
                } else {
                    viewModel.handwash.startHour = hourOfDay
                    viewModel.handwash.startMinute = minute
                    setStartTimeBetween()
                }


            }


            navigate(
                EditHandWashFragmentDirections.actionEditHandWashFragmentToTimeBottomSheet(
                    viewModel.handwash.startHour,
                    viewModel.handwash.startMinute,
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
                viewModel.handwash.frequency = try {
                    selectedValue!!.split(" ")[0].toInt()
                } catch (exp: Exception) {
                    exp.printStackTrace()
                    1
                }
                updateFreq()

            }
            navigate(
                EditHandWashFragmentDirections.actionEditHandWashFragmentToValueSelectorBottomSheet(
                    viewModel.getFrequencyValue(viewModel.handwash.frequency),
                    viewModel.frequencyList.value!!,
                    getString(R.string.text_freqency)
                )
            )

        }

        binding.lytEditReminder.tvDurationData.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                LOGS.i("$position | $selectedValue")
                viewModel.handwash.duration = try {
                    selectedValue!!.split(" ")[0].toInt()
                } catch (exp: Exception) {
                    exp.printStackTrace()
                    5
                }
                updateDuration()

            }
            navigate(
                EditHandWashFragmentDirections.actionEditHandWashFragmentToValueSelectorBottomSheet(
                    viewModel.getDurationValue(viewModel.handwash.duration),
                    AppStaticData.getHandWashDurationValues(),
                    getString(R.string.text_duration)
                )
            )

        }


    }

    override fun subscribeObservers() {
        viewModel.showDurationLayout.observe(viewLifecycleOwner) {
            if (it == true) {
                binding.lytEditReminder.apply {
                    tvDurationData.visible()
                    tvDurationText.visible()
                }
            } else {
                binding.lytEditReminder.apply {
                    tvDurationText.gone()
                    tvDurationData.gone()
                }
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) {
            val event = it.getContent() ?: return@observe
            when (event) {
                is UpdateDeviceDataCallback.HandWashingUpdated -> {
                    viewModel.setLoading(false)
                    if (event.success) {

                        context.showShortToast(getString(R.string.text_hand_wash_reminder_updated))
                        setFragmentResult(
                            EDIT_HAND_REQUEST_KEY,
                            bundleOf("hand" to viewModel.handwash)
                        )
                        navigateUpSafe()
                    }
                }
                else -> {}
            }
        }
        viewModel.frequencyIn.observe(viewLifecycleOwner) {
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