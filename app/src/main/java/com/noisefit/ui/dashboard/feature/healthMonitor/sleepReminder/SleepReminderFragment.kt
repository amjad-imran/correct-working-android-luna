package com.noisefit.ui.dashboard.feature.healthMonitor.sleepReminder

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.R
import com.noisefit.databinding.FragmentSleepReminderBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SleepReminderFragment :
    BaseFragment<FragmentSleepReminderBinding>(FragmentSleepReminderBinding::inflate) {

    private val viewModel: SleepReminderViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setLoading(true)
        viewModel.sessionManager.sendQueryAction(QueryAction.GetSleepReminder)

        binding.lytReminderSettings.apply {
            tvAlertValueText.text = getString(R.string.text_start_time)
            editButtonState(false)
        }

    }

    override fun initListener() {

        binding.lytReminderSettings.tvAlertValueData.setOnClickListener {
            setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
                val hourOfDay = bundle.getInt("hour")
                val minute = bundle.getInt("minute")

                viewModel.sleepReminder.hour = hourOfDay
                viewModel.sleepReminder.minute = minute
                updateAlertValue()
                updateReminder(true)

            }

            navigate(
                SleepReminderFragmentDirections.actionSleepReminderFragmentToTimeBottomSheet(
                    viewModel.sleepReminder.hour,
                    viewModel.sleepReminder.minute,
                    1,
                    getString(R.string.text_start_time)
                )
            )
        }

        binding.lytFeatureTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_sleep)
            tvTitle.text = getString(R.string.text_sleep_reminder)
            tvTitleDisc.text = getString(R.string.text_not_receiving_alerts)
            tvTitleDisc.setTextColor(requireActivity().resources.getColor(R.color.text_accent_color))
            llSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (!buttonView.isPressed) return@setOnCheckedChangeListener
                if (isChecked) {
                    updateReminder(true)

                } else {

                    updateReminder(false)
                }
                editButtonState(isChecked)

            }
        }
        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_sleep_reminder)
            tvDesc.gone()
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }

    }

    private fun editButtonState(checked: Boolean) {
        binding.lytReminderSettings.tvAlertValueData.isEnabled = checked
    }

    private fun updateReminder(status: Boolean) {
        viewModel.sleepReminder.status = status
        viewModel.setLoading(true)
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.UpdateSleepReminder(
                viewModel.sleepReminder
            )
        )
    }

    override fun subscribeObservers() {

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            it.getContent()?.let { callBack ->
                when (callBack) {

                    is UpdateDeviceDataCallback.SleepReminderUpdated -> {
                        binding.progressBar.root.gone()
                        if (callBack.success) {
                            context.showShortToast(getString(R.string.text_sleep_reminder_success))

                        } else {
                            context.showShortToast(getString(R.string.text_sleep_reminder_failed))
                        }
                    }

                    else -> {}
                }
            }
        }

        viewModel.sessionManager.deviceQueryCallback.observe(this) {
            when (it) {

                is QueryCallback.SleepReminderObtained -> {
                    binding.progressBar.root.gone()
                    viewModel.sleepReminder = it.sleepReminder
                    editButtonState(viewModel.sleepReminder.status)
                    binding.lytFeatureTile.llSwitch.isChecked = viewModel.sleepReminder.status
                    updateAlertValue()
                }


                else -> {}
            }
        }
    }

    private fun updateAlertValue() {

        val startTime = DateFormats.formatTimeWithAmPm(
            viewModel.sleepReminder.hour,
            viewModel.sleepReminder.minute
        )
        binding.lytReminderSettings.tvAlertValueData.text = startTime
    }
}