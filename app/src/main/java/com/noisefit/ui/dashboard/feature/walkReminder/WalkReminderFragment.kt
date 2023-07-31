package com.noisefit.ui.dashboard.feature.walkReminder

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.R
import com.noisefit.databinding.FragmentWalkReminderBinding
import com.noisefit.ui.common.*
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.WalkReminderData
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WalkReminderFragment :
    BaseFragment<FragmentWalkReminderBinding>(FragmentWalkReminderBinding::inflate) {
    private val viewModel: WalkReminderViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!viewModel.fetchData) {
            binding.progressBar.root.visible()
            viewModel.sessionManager.sendQueryAction(QueryAction.GetWalkReminderData)
            updateUi(viewModel.walkReminderData)
        } else {
            viewModel.setLoading(false)
            updateUi(viewModel.walkReminderData)
        }

        initUi()
    }

    private fun initUi() {
        binding.lytReminderSettings.apply {
            tvDurationData.gone()
            tvDurationText.gone()
            tvFreqText.text = getString(R.string.text_steps_per_hour)
        }
    }

    override fun initListener() {
        binding.lytReminderSettings.tvEdit.setOnClickListener {
            setFragmentResultListener(EDIT_WALK_REQUEST_KEY) { _, bundle ->
                val walkReminderData = bundle.getParcelable("walk") as? WalkReminderData
                if (walkReminderData != null) {
                    updateUi(walkReminderData)
                }


            }
            navigate(
                WalkReminderFragmentDirections.actionWalkReminderFragmentToEditWalkReminderFragment(
                    viewModel.walkReminderData
                )
            )
        }

        binding.lytFeatureTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_idle_alert)
            tvTitle.text = getString(R.string.text_walk_reminder)
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
            tvTitle.text = getString(R.string.text_walk_reminder)
            tvDesc.text = getString(R.string.text_allow_your_device_to_remind_you_to_get_up)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }

    }

    private fun updateReminder(status: Boolean) {
        binding.progressBar.root.visible()

        val intArray = IntArray(7)
        viewModel.selectedWeekArray.forEachIndexed { index, value ->
            if (value) {
                intArray[index] = 1
            } else {
                intArray[index] = 0
            }
        }

        viewModel.setWalkReminder(viewModel.generateDeviceReminderUpdateData(status, intArray))
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetWalkReminderPro3(
                viewModel.walkReminderData
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
            viewModel.walkReminderData.startHour,
            viewModel.walkReminderData.startMinute
        )
        val endTime = DateFormats.formatTimeWithAmPm(
            viewModel.walkReminderData.endHour,
            viewModel.walkReminderData.endMinute
        )

        binding.lytReminderSettings.tvStartTimeData.text = startTime
        binding.lytReminderSettings.tvEndTimeData.text = endTime
    }

    private fun updateFreq() {
        binding.lytReminderSettings.tvFreqData.text =
            viewModel.walkReminderData.goalSteps.toString()
    }

    private fun updateUi(data: WalkReminderData) {
        viewModel.setWalkReminder(data)
        updateFreq()
        setSwitchState(data.status)
        setTimeBetween()
        repeatDays()
    }

    private fun repeatDays() {
        viewModel.walkReminderData.weeks?.forEachIndexed { index, i ->
            viewModel.selectedWeekArray[index] = (i == 1)

        }
        binding.lytReminderSettings.tvRepeatData.text =
            ApplicationUtils.getRepeatReminderDays(viewModel.selectedWeekArray)

    }


    override fun subscribeObservers() {

//        viewModel.showRepeatLayout.observe(this) {
//            if (it == true) {
//                binding.lytReminderSettings.apply {
//                    tvRepeatData.visible()
//                    tvRepeatText.visible()
//                }
//            } else {
//                binding.lytReminderSettings.apply {
//                    tvRepeatData.gone()
//                    tvRepeatText.gone()
//                }
//            }
//        }

        viewModel.showEditLayout.observe(this) {
            if (it == true) {
                binding.lytReminderSettings.root.visible()

            } else {
                binding.lytToolbarWithDetails.tvDesc.text =
                    getString(R.string.text_walk_reminder_message_2)
                binding.lytReminderSettings.root.gone()
            }
        }

//        viewModel.showTimeLayout.observe(this) {
//            if (it == true) {
//                binding.lytReminderSettings.apply {
//                    tvStartTimeData.visible()
//                    tvStartTimeText.visible()
//                    tvEndTimeData.visible()
//                    tvEndTimeText.visible()
//                }
//            } else {
//                binding.lytReminderSettings.apply {
//                    tvStartTimeData.gone()
//                    tvStartTimeText.gone()
//                    tvEndTimeData.gone()
//                    tvEndTimeText.gone()
//                }
//            }
//        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.WalkReminderDataObtained -> {
                    binding.progressBar.root.gone()
                    viewModel.fetchData = true
                    updateUi(it.walkReminderData)
                }
                else -> {}
            }
        }

        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            it.getContent()?.let { content ->
                if (content is UpdateDeviceDataCallback.WalkReminderDataUpdated) {
                    binding.progressBar.root.gone()
                    if (content.success) {
                        viewModel.fetchData = true
                        context.showShortToast(getString(R.string.text_walk_reminder_update_msg))
                    }
                }
            }
        }
    }

}