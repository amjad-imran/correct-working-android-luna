package com.noisefit.ui.dashboard.feature.female

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.R
import com.noisefit.data.local.AppStaticData
import com.noisefit.databinding.FragmentFemaleHealthBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.common.bottomSheet.DATE_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit.ui.dashboard.feature.myReminder.AddReminderFragmentDirections
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FemaleHealthFragment :
    BaseFragment<FragmentFemaleHealthBinding>(FragmentFemaleHealthBinding::inflate) {

    private val viewModel: FemaleHealthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lytFeatureTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_female_health)
            tvTitle.text = getString(R.string.text_cycle_tracking)
            tvTitleDisc.gone()
        }
        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_cycle_tracking)
            tvDesc.text = getString(R.string.text_cycle_tracking_about)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }

        if (viewModel.menstrualData == null) {
            viewModel.initMsData(null)
        }
        viewModel.setLoading(true)
        viewModel.sessionManager.sendQueryAction(QueryAction.GetMenstrualSettings)

    }

    private fun setSwitchState(isChecked: Boolean) {
        binding.lytFeatureTile.llSwitch.isChecked = isChecked
        editButtonState(isChecked)
    }

    private fun editButtonState(isChecked: Boolean) {
        if (isChecked) {
            binding.lytFemaleHealthTop.apply {
                tvMenNotification.enable()
                tvMenNotificationData.enable()
                tvOvulationData.enable()
                tvOvulationText.enable()
                tvReminderData.enable()
                tvReminderText.enable()
            }
            binding.lytFemaleHealthBottom.apply {
                tvLastMenData.enable()
                tvMenCycleData.enable()
                tvCycleLengthData.enable()
                tvCycleLengthText.enable()
                tvLastMenText.enable()
                tvMenCycleText.enable()
            }
        } else {
            binding.lytFemaleHealthBottom.apply {
                tvLastMenData.disable()
                tvMenCycleData.disable()
                tvCycleLengthData.disable()
                tvCycleLengthText.disable()
                tvLastMenText.disable()
                tvMenCycleText.disable()
            }
            binding.lytFemaleHealthTop.apply {
                tvMenNotification.disable()
                tvMenNotificationData.disable()
                tvOvulationData.disable()
                tvOvulationText.disable()
                tvReminderData.disable()
                tvReminderText.disable()
            }
        }
    }

    private fun updateUi() {
        updateOvulationDataData()
        updateMenNotificationData()
        setStartTimeBetween()
        setDate()
        updateMenCycleData()
        updateCycleLengthData()
    }

    private fun setDate() {
        binding.lytFemaleHealthBottom.tvLastMenData.text =
            DateFormats.getReminderDate(
                viewModel.lastMsMonth!!,
                viewModel.lastMsDay!!,
                viewModel.lastMsyear!!
            )
    }

    private fun updateMenNotificationData() {
        val formattedData = "${viewModel.msReminderAdvance} days before"
        binding.lytFemaleHealthTop.tvMenNotificationData.text = formattedData
    }

    private fun updateOvulationDataData() {
        val formattedData = "${viewModel.ovulationReminderAdvance} days before"
        binding.lytFemaleHealthTop.tvOvulationData.text = formattedData
    }

    private fun updateMenCycleData() {
        val formattedData = "${viewModel.menstrualData.menstrualLength} days"
        binding.lytFemaleHealthBottom.tvMenCycleData.text = formattedData
    }

    private fun setStartTimeBetween() {
        val startTime = DateFormats.formatTimeWithAmPm(
            viewModel.reminderTimeHour,
            viewModel.reminderTimeMinute
        )
        binding.lytFemaleHealthTop.tvReminderData.text = startTime
    }

    private fun updateCycleLengthData() {
        val formattedData = "${viewModel.menstrualData.menstrualCycleLength} days"
        binding.lytFemaleHealthBottom.tvCycleLengthData.text = formattedData
    }

    override fun initListener() {

        binding.lytFemaleHealthBottom.tvLastMenData.setOnClickListener {
            setFragmentResultListener(DATE_REQUEST_KEY) { _, bundle ->
                val date = bundle.getInt("date")
                val month = bundle.getInt("month")
                val year = bundle.getInt("year")

                viewModel.lastMsDay = date
                viewModel.lastMsMonth = month + 1
                viewModel.lastMsyear = year
                setDate()

                LOGS.d("SATEAREFDAFD ${viewModel.lastMsDay} ${viewModel.lastMsMonth} ${viewModel.lastMsyear}")
                viewModel.sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.UpdateMenstrualData(
                        viewModel.menstrualData.apply {
                            lastMenstrualDate = "${viewModel.lastMsDay}/${viewModel.lastMsMonth}/${viewModel.lastMsyear}"
                        }
                    )
                )


            }
            navigate(
                FemaleHealthFragmentDirections.actionFemaleHealthFragmentToDateBottomSheet(
                    getString(R.string.text_last_menstruation),
                    viewModel.lastMsDay!!,
                    viewModel.lastMsMonth!!,
                    viewModel.lastMsyear!!,
                    false
                )
            )

        }
        binding.lytFemaleHealthBottom.tvCycleLengthData.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { key, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let {
                    viewModel.menstrualData.menstrualCycleLength = it.split(" ")[0].toInt()
                    updateCycleLengthData()
                    viewModel.setLoading(true)
                    viewModel.sessionManager.sendUpdateQueryAction(
                        UpdateDeviceAction.UpdateMenstrualData(
                            viewModel.menstrualData
                        )
                    )
                }

            }
            navigate(
                FemaleHealthFragmentDirections.actionFemaleHealthFragmentToValueSelectorBottomSheet(
                    "${viewModel.menstrualData.menstrualCycleLength} Days",
                    AppStaticData.getPeriodLengthValues(),
                    getString(R.string.text_cycle_length)
                )
            )
        }
        binding.lytFemaleHealthBottom.tvMenCycleData.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { key, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let {
                    viewModel.menstrualData.menstrualLength = it.split(" ")[0].toInt()
                    updateMenCycleData()
                    viewModel.setLoading(true)
                    viewModel.sessionManager.sendUpdateQueryAction(
                        UpdateDeviceAction.UpdateMenstrualData(
                            viewModel.menstrualData
                        )
                    )
                }
            }
            navigate(
                FemaleHealthFragmentDirections.actionFemaleHealthFragmentToValueSelectorBottomSheet(
                    "${viewModel.menstrualData.menstrualLength} Days",
                    AppStaticData.getMenstrualLengthValues(),
                    getString(R.string.text_menstruation_cycle)
                )
            )

        }

        binding.lytFemaleHealthTop.tvReminderData.setOnClickListener {
            setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
                val hourOfDay = bundle.getInt("hour")
                val minute = bundle.getInt("minute")

                viewModel.reminderTimeHour = hourOfDay
                viewModel.reminderTimeMinute = minute
                setStartTimeBetween()

                viewModel.sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.UpdateMenstrualData(
                        viewModel.menstrualData.apply {
                            this.menstrualReminder?.reminderTime =
                                DateFormats.formatTime(hourOfDay, minute)

                        }
                    )
                )

            }


            navigate(
                FemaleHealthFragmentDirections.actionFemaleHealthFragmentToTimeBottomSheet(
                    viewModel.reminderTimeHour,
                    viewModel.reminderTimeMinute,
                    1,
                    getString(R.string.text_start_time)
                )
            )

        }

        binding.lytFemaleHealthTop.tvMenNotificationData.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let {
                    viewModel.msReminderAdvance = it.split(" ")[0].toInt()
                    updateMenNotificationData()
                    LOGS.i("$position | $selectedValue")
                    viewModel.setLoading(true)
                    viewModel.sessionManager.sendUpdateQueryAction(
                        UpdateDeviceAction.UpdateMenstrualData(
                            viewModel.menstrualData.apply {
                                this.menstrualReminder?.remindStartDayBefore =
                                    viewModel.msReminderAdvance
                            }
                        )
                    )
                }


            }
            navigate(
                FemaleHealthFragmentDirections.actionFemaleHealthFragmentToValueSelectorBottomSheet(
                    "${viewModel.msReminderAdvance} Days",
                    AppStaticData.getMsReminderValues(),
                    getString(R.string.text_mensuration_notification)
                )
            )

        }

        binding.lytFemaleHealthTop.tvOvulationData.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { key, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let {
                    viewModel.ovulationReminderAdvance = it.split(" ")[0].toInt()
                    updateOvulationDataData()
                    LOGS.i("$position | $selectedValue")
                    viewModel.setLoading(true)

                    viewModel.sessionManager.sendUpdateQueryAction(
                        UpdateDeviceAction.UpdateMenstrualData(
                            viewModel.menstrualData.apply {
                                this.menstrualReminder?.remindOvulationDayBefore =
                                    viewModel.ovulationReminderAdvance
                            }
                        )
                    )
                }


            }
            navigate(
                FemaleHealthFragmentDirections.actionFemaleHealthFragmentToValueSelectorBottomSheet(
                    "${viewModel.ovulationReminderAdvance} Days",
                    AppStaticData.getMsReminderValues(),
                    getString(R.string.text_ovulation_reminder)
                )
            )

        }
        binding.lytFeatureTile.llSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            viewModel.sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.UpdateMenstrualData(
                    viewModel.menstrualData.apply {
                        status = isChecked
                    }
                )
            )
            editButtonState(isChecked)
            viewModel.setLoading(true)

        }

    }


    override fun subscribeObservers() {
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            when (it) {
                is QueryCallback.MenstrualSettingsObtained -> {
                    LOGS.d(it.menstrualData.toString())
                    setSwitchState(it.menstrualData.status)
                    viewModel.initMsData(it.menstrualData)
                    updateUi()
                    viewModel.setLoading(false)
                }
                else -> {}
            }
        }
        viewModel.sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) {
            it.getContent()?.let { value ->
                when (value) {
                    is UpdateDeviceDataCallback.MenstrualDataUpdated -> {
                        binding.progressBar.root.gone()
                        if (value.success) {
                            context.showShortToast(getString(R.string.text_setting_updated))
                        }
                    }
                    else -> {}
                }
            }
        }
    }

}