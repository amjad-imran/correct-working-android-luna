package com.noisefit.ui.dashboard.feature.walkReminder

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.R
import com.noisefit.data.local.AppStaticData
import com.noisefit.databinding.FragmentEditWalkReminderBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.common.bottomSheet.REPEAT_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.WalkReminderData
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

const val EDIT_WALK_REQUEST_KEY = "EDIT_WALK_REQUEST_KEY"

@AndroidEntryPoint
class EditWalkReminderFragment :
    BaseFragment<FragmentEditWalkReminderBinding>(FragmentEditWalkReminderBinding::inflate) {


    private val viewModel: WalkReminderViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            viewModel.setWalkReminder(EditWalkReminderFragmentArgs.fromBundle(it).walkReminder)
            viewModel.walkReminderData.weeks?.forEachIndexed { index, i ->
                viewModel.selectedWeekArray[index] = (i == 1)

            }
        }
        initUi()
        updateUi(viewModel.walkReminderData)
    }

    private fun initUi() {
        binding.lytEditReminder.apply {
            tvDurationData.gone()
            tvDurationText.gone()
            tvFreqText.text = getString(R.string.text_steps_per_hour)
        }
    }


    private fun updateUi(data: WalkReminderData) {
        viewModel.setWalkReminder(data)
        updateFreq()
        setStartTimeBetween()
        setEndTimeBetween()
        updateRepeatDays()
    }

    private fun updateFreq() {
        binding.lytEditReminder.tvFreqData.text =
            viewModel.walkReminderData.goalSteps.toString()
    }

    private fun updateRepeatDays() {

        binding.lytEditReminder.tvRepeatData.text =
            ApplicationUtils.getRepeatReminderDays( viewModel.selectedWeekArray)
    }

    private fun setStartTimeBetween() {
        val startTime = DateFormats.formatTimeWithAmPm(
            viewModel.walkReminderData.startHour,
            viewModel.walkReminderData.startMinute
        )
        binding.lytEditReminder.tvStartTimeData.text = startTime
    }

    private fun setEndTimeBetween() {
        val endTime = DateFormats.formatTimeWithAmPm(
            viewModel.walkReminderData.endHour,
            viewModel.walkReminderData.endMinute
        )
        binding.lytEditReminder.tvEndTimeData.text = endTime
    }

    private fun updateReminder() {

        val intArray = IntArray(7)
        viewModel.selectedWeekArray.forEachIndexed { index, value ->
            if (value) {
                intArray[index] = 1
            } else {
                intArray[index] = 0
            }
        }
        viewModel.setLoading(true)
        viewModel.setWalkReminder(viewModel.generateDeviceReminderUpdateData(true, intArray))
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetWalkReminderPro3(
                viewModel.walkReminderData
            )
        )

    }

    override fun initListener() {
        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_walk_reminder_settings)
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
                        viewModel.walkReminderData.startHour,
                        viewModel.walkReminderData.startMinute
                    ) <= 0
                ) {
                    context.showShortToast(getString(R.string.text_end_time_greater))
                } else if (hourOfDay == viewModel.walkReminderData.startHour && minute == viewModel.walkReminderData.startMinute) {
                    context.showShortToast(getString(R.string.text_start_end_time_should_be_different))
                } else {
                    viewModel.walkReminderData.endHour = hourOfDay
                    viewModel.walkReminderData.endMinute = minute
                    setEndTimeBetween()
                }
            }


            navigate(
                EditWalkReminderFragmentDirections.actionEditWalkReminderFragmentToTimeBottomSheet(
                    viewModel.walkReminderData.endHour,
                    viewModel.walkReminderData.endMinute,
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
                        viewModel.walkReminderData.endHour,
                        viewModel.walkReminderData.endMinute
                    ) >= 0
                ) {
                    context.showShortToast(getString(R.string.text_start_time_less))
                } else if (hourOfDay == viewModel.walkReminderData.endHour && minute == viewModel.walkReminderData.endMinute) {
                    context.showShortToast(getString(R.string.text_start_end_time_should_be_different))
                } else {
                    viewModel.walkReminderData.startHour = hourOfDay
                    viewModel.walkReminderData.startMinute = minute
                    setStartTimeBetween()
                }


            }


            navigate(
                EditWalkReminderFragmentDirections.actionEditWalkReminderFragmentToTimeBottomSheet(
                    viewModel.walkReminderData.startHour,
                    viewModel.walkReminderData.startMinute,
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
                viewModel.walkReminderData.goalSteps = try {
                    selectedValue!!.split(" ")[0].toInt()
                } catch (exp: Exception) {
                    exp.printStackTrace()
                    1
                }
                updateFreq()

            }
            navigate(
                EditWalkReminderFragmentDirections.actionEditWalkReminderFragmentToValueSelectorBottomSheet(
                    viewModel.walkReminderData.goalSteps.toString(),
                    AppStaticData.getWalkStepsPerHour(),
                    getString(R.string.text_steps_per_hour)
                )
            )

        }

        binding.lytEditReminder.tvRepeatData.setOnClickListener {
            setFragmentResultListener(REPEAT_REQUEST_KEY) { _, bundle ->
                val repeatList = bundle.getSerializable("repeat") as? List<Boolean>
                if (repeatList != null) {
                    viewModel.selectedWeekArray = ArrayList(repeatList)
                    updateRepeatDays()
                }


            }
            navigate(
                EditWalkReminderFragmentDirections.actionEditWalkReminderFragmentToRepeatBottomSheet(
                    viewModel.selectedWeekArray.toBooleanArray()
                )
            )

        }
    }

    override fun subscribeObservers() {
//        viewModel.showRepeatLayout.observe(this) {
//            if (it == true) {
//                binding.lytEditReminder.apply {
//                    tvRepeatData.visible()
//                    tvRepeatText.visible()
//                }
//            } else {
//                binding.lytEditReminder.apply {
//                    tvRepeatData.gone()
//                    tvRepeatText.gone()
//                }
//            }
//        }

//        viewModel.showEditLayout.observe(this) {
//            if (it == true) {
//                binding.lytReminderSettings.root.visible()
//
//            } else {
//                binding.lytReminderSettings.root.gone()
//            }
//        }

//        viewModel.showTimeLayout.observe(this) {
//            if (it == true) {
//                binding.lytEditReminder.apply {
//                    tvStartTimeData.visible()
//                    tvStartTimeText.visible()
//                    tvEndTimeData.visible()
//                    tvEndTimeText.visible()
//                }
//            } else {
//                binding.lytEditReminder.apply {
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

                    it.walkReminderData.let { data ->
                        LOGS.d(data)
                        updateUi(data)

                    }
                }
                else -> {}
            }
        }

        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            it.getContent()?.let { content ->
                if (content is UpdateDeviceDataCallback.WalkReminderDataUpdated) {
                    binding.progressBar.root.gone()
                    if (content.success) {
                        setFragmentResult(
                            EDIT_WALK_REQUEST_KEY,
                            bundleOf("walk" to viewModel.walkReminderData)
                        )
                        navigateUpSafe()
                        context.showShortToast(getString(R.string.text_walk_reminder_update_msg))
                    }
                }
            }
        }
    }


}