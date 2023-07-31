package com.noisefit.ui.dashboard.feature.drink

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.R
import com.noisefit.databinding.FragmentDrinkReminderBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.dashboard.feature.idle.FrequencyIn
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.utils.InsiderAppEvents
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
class DrinkReminderFragment :
    BaseFragment<FragmentDrinkReminderBinding>(FragmentDrinkReminderBinding::inflate) {

    private val viewModel: DrinkReminderViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (viewModel.sedentaryData.interval == 0) {
            viewModel.setLoading(true)
            viewModel.sessionManager.sendQueryAction(QueryAction.GetDrinkWaterSettings)
        } else {
            updateUi(viewModel.sedentaryData)
        }

        initUi()
    }

    private fun initUi() {
        binding.lytReminderSettings.apply {
            tvDurationData.gone()
            tvDurationText.gone()
        }
    }

    override fun initListener() {
        binding.lytReminderSettings.tvEdit.setOnClickListener {
            setFragmentResultListener(EDIT_DRINK_REQUEST_KEY) { _, bundle ->
                val sedentaryData = bundle.getParcelable("drink") as? SedentaryData
                if (sedentaryData != null) {
                    LOGS.d("updateReminder qqweqw $sedentaryData")
                    viewModel.setSedentary(sedentaryData)
                    updateUi(sedentaryData)
                }


            }
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.DRINK_WATER_EDIT_CLICK)
            navigate(
                DrinkReminderFragmentDirections.actionDrinkReminderFragmentToEditDrinkFragment(
                    viewModel.sedentaryData
                )
            )
        }

        binding.lytFeatureTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_drink_water)
            tvTitle.text = getString(R.string.text_drink_water)
            tvTitleDisc.text = getString(R.string.text_not_receiving_alerts)
            tvTitleDisc.setTextColor(requireActivity().resources.getColor(com.noisefit_commans.R.color.text_accent_color))
            llSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (!buttonView.isPressed) return@setOnCheckedChangeListener
                if (isChecked) {
                    updateReminder(true)
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.DRINK_WATER_CLICK,HashMap<String, Any>().apply {
                        this["is_enabled"]=true
                    })
                } else {
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.DRINK_WATER_CLICK,HashMap<String, Any>().apply {
                        this["is_enabled"]=false
                    })
                    updateReminder(false)
                }
                editButtonState(isChecked)
            }
        }
        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_drink_water)
            tvDesc.text = getString(R.string.text_allow_your_device_to_remind_you_yo_drink_water)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }
    }

    private fun editButtonState(isChecked: Boolean) {
        if (isChecked) {
            binding.lytReminderSettings.tvEdit.enable()
        } else {
            binding.lytReminderSettings.tvEdit.disable()
        }
    }


    private fun updateReminder(status: Boolean) {
        viewModel.setLoading(true)
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetDrinkWaterReminder(
                viewModel.generateDeviceReminderUpdateData(
                    status
                )
            )
        )
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
        repeatDays()
    }

    private fun repeatDays() {
        binding.lytReminderSettings.tvRepeatData.text =
            ApplicationUtils.getRepeatReminderDays(viewModel.sedentaryData.repeatDays)
    }


    override fun subscribeObservers() {

        viewModel.showRepeatLayout.observe(this) {
            if (it == true) {
                binding.lytReminderSettings.apply {
                    tvRepeatData.visible()
                    tvRepeatText.visible()
                }
            } else {
                binding.lytReminderSettings.apply {
                    tvRepeatData.gone()
                    tvRepeatText.gone()
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
                is QueryCallback.DrinkWaterDataObtained -> {
                    viewModel.setLoading(false)
                    LOGS.d("updateReminder "+it.sedentaryData.toString())
                    updateUi(it.sedentaryData)
                }
                else -> {}
            }
        }

        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            val event = it.getContent() ?: return@observe
            when (event) {
                is UpdateDeviceDataCallback.DrinkWaterUpdated -> {
                    viewModel.setLoading(false)
                    if (event.success) {
                        context.showShortToast(getString(R.string.text_water_reminder_updated))
                    }
                }
                else -> {}
            }
        }
    }

}