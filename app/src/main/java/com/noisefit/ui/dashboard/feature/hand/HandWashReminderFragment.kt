package com.noisefit.ui.dashboard.feature.hand

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.R
import com.noisefit.databinding.FragmentHandwashReminderBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.dashboard.feature.idle.FrequencyIn
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.HandWashing
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HandWashReminderFragment :
    BaseFragment<FragmentHandwashReminderBinding>(FragmentHandwashReminderBinding::inflate) {

    private val TAG = "HandWashReminderFragment"
    private val viewModel: HandWashReminderViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (viewModel.handwash.frequency == 0) {
            viewModel.setLoading(true)
            viewModel.sessionManager.sendQueryAction(QueryAction.GetHandWashData)
        } else {
            updateUi(viewModel.handwash)
        }

        initUi()

    }

    private fun initUi() {
        binding.lytReminderSettings.apply {
            tvRepeatData.gone()
            tvRepeatText.gone()
        }
    }

    override fun initListener() {

        binding.lytReminderSettings.tvEdit.setOnClickListener {
            setFragmentResultListener(EDIT_HAND_REQUEST_KEY) { _, bundle ->
                val handWashing = bundle.getParcelable("hand") as? HandWashing
                if (handWashing != null) {
                    LOGS.d("$TAG --> $handWashing")
                    updateUi(handWashing)
                }


            }
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.HAND_WASH_EDIT_CLICK)
            navigate(
                HandWashReminderFragmentDirections.actionHandWashReminderFragmentToEditHandWashFragment(
                    viewModel.handwash
                )
            )
        }

        binding.lytFeatureTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_hand_wash_dp)
            tvTitle.text = getString(R.string.text_hand_wash)
            tvTitleDisc.text = getString(R.string.text_not_receiving_alerts)
            tvTitleDisc.setTextColor(requireActivity().resources.getColor(com.noisefit_commans.R.color.text_accent_color))
            llSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (!buttonView.isPressed) return@setOnCheckedChangeListener
                if (isChecked) {
                    updateReminder(true)
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.HAND_WASH_CLICK,HashMap<String, Any>().apply {
                        this["is_enabled"]=true
                    })
                } else {
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.HAND_WASH_CLICK,HashMap<String, Any>().apply {
                        this["is_enabled"]=false
                    })
                    updateReminder(false)
                }
                editButtonState(isChecked)

            }
        }
        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_hand_wash)
            tvDesc.text =
                getString(R.string.text_allow_your_device_to_remind_you_to_wash_your_hands)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }


    }


    private fun updateReminder(status: Boolean) {
        viewModel.setLoading(true)
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetHandWashing(
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
            viewModel.handwash.startHour,
            viewModel.handwash.startMinute
        )
        val endTime = DateFormats.formatTimeWithAmPm(
            viewModel.handwash.endHour,
            viewModel.handwash.endMinute
        )

        binding.lytReminderSettings.tvStartTimeData.text = startTime
        binding.lytReminderSettings.tvEndTimeData.text = endTime
    }

    private fun updateFreq() {
        LOGS.d("$TAG ${viewModel.handwash.frequency}")
        binding.lytReminderSettings.tvFreqData.text =
            viewModel.getFrequencyValue(viewModel.handwash.frequency)
    }

    private fun updateDuration() {
        binding.lytReminderSettings.tvDurationData.text =
            viewModel.getDurationValue(viewModel.handwash.duration)
    }


    private fun updateUi(data: HandWashing) {
        viewModel.setHandWash(data)
        updateFreq()
        setSwitchState(data.startWash)
        setTimeBetween()
        updateDuration()
    }


    override fun subscribeObservers() {
        viewModel.showDurationLayout.observe(viewLifecycleOwner) {
            if (it == true) {
                binding.lytReminderSettings.apply {
                    tvDurationData.visible()
                    tvDurationText.visible()
                }
            } else {
                binding.lytReminderSettings.apply {
                    tvDurationData.gone()
                    tvDurationText.gone()
                }
            }
        }
        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.frequencyIn.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it == FrequencyIn.NONE) {
                    binding.lytReminderSettings.apply {
                        tvFreqData.gone()
                        tvFreqText.gone()
                    }
                }
            }
        }

        viewModel.sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            when (it) {
                is QueryCallback.GetHandWashing -> {
                    viewModel.setLoading(false)
                    LOGS.d("$TAG ${it.handWashing}")
                    updateUi(it.handWashing)
                }
                else -> {}
            }
        }

        viewModel.sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) {
            val event = it.getContent() ?: return@observe
            when (event) {
                is UpdateDeviceDataCallback.HandWashingUpdated -> {
                    viewModel.setLoading(false)
                    if (event.success) {
                        context.showShortToast(getString(R.string.text_hand_wash_reminder_updated))
                    }
                }
                else -> {}
            }
        }
    }

}