package com.noisefit.ui.dashboard.feature.healthMonitor.medicine

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentMedicineReminderBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.dashboard.feature.idle.EDIT_IDLE_REQUEST_KEY
import com.noisefit.ui.dashboard.feature.idle.FrequencyIn
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.SedentaryData
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MedicineReminderFragment :
    BaseFragment<FragmentMedicineReminderBinding>(FragmentMedicineReminderBinding::inflate) {

    private val viewModel: MedicineViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (!viewModel.firstTimeData) {
            viewModel.setLoading(true)
            viewModel.sessionManager.sendQueryAction(QueryAction.GetMedicineReminderSettings)
        } else {
            updateUi(viewModel.sedentaryData)
        }

        initUi()
    }

    private fun initUi() {
        binding.lytReminderSettings.apply {
            tvDurationData.gone()
            tvDurationText.gone()
            tvRepeatText.gone()
            tvRepeatData.gone()
        }
    }

    override fun initListener() {
        binding.lytReminderSettings.tvEdit.setOnClickListener {
            setFragmentResultListener(EDIT_MEDICINE_REQUEST_KEY) { _, bundle ->
                val sedentaryData = bundle.getParcelable("medicine") as? SedentaryData
                if (sedentaryData != null) {
                    viewModel.sedentaryData = sedentaryData
                    updateUi(sedentaryData)
                }


            }
            navigate(
                MedicineReminderFragmentDirections.actionMedicineReminderFragmentToEditMealReminderFragment(
                    viewModel.sedentaryData
                )
            )
        }

        binding.lytFeatureTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_medicine)
            tvTitle.text = getString(R.string.text_medicine_reminder)
            tvTitleDisc.text = getString(R.string.text_not_receiving_alerts)
            tvTitleDisc.setTextColor(requireActivity().resources.getColor(com.noisefit_commans.R.color.text_accent_color))
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
            tvTitle.text = getString(R.string.text_medicine_reminder)
            tvDesc.text = getString(R.string.text_allow_your_device_to_take_medicine)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }


    }


    private fun updateReminder(status: Boolean) {
        viewModel.setLoading(true)
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetMedicineReminder(
                viewModel.generateDeviceReminderUpdateData(
                    status
                )
            )
        )
    }

    private fun editButtonState(isChecked: Boolean) {
        if (isChecked) {
            binding.lytReminderSettings.tvEdit.enable()
        } else {
            binding.lytReminderSettings.tvEdit.disable()
        }
    }

    private fun setSwitchState(isChecked: Boolean) {
        binding.lytFeatureTile.llSwitch.isChecked = isChecked
        editButtonState(isChecked)
    }

    private fun setTimeBetween() {
        val startTime = DateFormats.formatTimeWithAmPm(
            viewModel.sedentaryData.startHour,
            viewModel.sedentaryData.startMinute
        )
        val endTime = DateFormats.formatTimeWithAmPm(
            viewModel.sedentaryData.endHour,
            viewModel.sedentaryData.endMinute
        )

        binding.lytReminderSettings.tvStartTimeData.text = startTime
        binding.lytReminderSettings.tvEndTimeData.text = endTime
    }

    private fun updateFreq() {
        binding.lytReminderSettings.tvFreqData.text =
            viewModel.getFrequencyValue(viewModel.sedentaryData.interval)
    }

    private fun updateUi(data: SedentaryData) {
        viewModel.setSedentary(data)
        updateFreq()
        setSwitchState(data.status)
        setTimeBetween()

    }


    override fun subscribeObservers() {


        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.frequencyIn.observe(this) {
            if (it != null) {
                if (it == FrequencyIn.NONE) {
                    binding.lytReminderSettings.apply {
                        tvFreqData.gone()
                        tvFreqText.gone()
                    }
                }
            }
        }

        viewModel.sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.MedicineDataObtained -> {
                    viewModel.setLoading(false)
                    viewModel.firstTimeData = true
                    LOGS.d(it.sedentaryData.toString())
                    updateUi(it.sedentaryData)

                }
                else -> {}
            }
        }

        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            val event = it.getContent() ?: return@observe
            when (event) {
                is UpdateDeviceDataCallback.MedicineDataUpdated -> {
                    viewModel.setLoading(false)
                    if (event.success) {
                        context.showShortToast(getString(R.string.text_medicine_reminder_updated))
                    }
                }
                else -> {}
            }
        }
    }

}