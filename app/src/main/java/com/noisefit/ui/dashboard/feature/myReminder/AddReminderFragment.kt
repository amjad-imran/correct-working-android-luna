package com.noisefit.ui.dashboard.feature.myReminder

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.R
import com.noisefit.data.local.AppStaticData
import com.noisefit.databinding.FragmentAddReminderBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.common.bottomSheet.DATE_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.REPEAT_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.ReminderList
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

const val REMINDER_REQUEST_KEY = "REMINDER_REQUEST_KEY"

@AndroidEntryPoint
class AddReminderFragment :
    BaseFragment<FragmentAddReminderBinding>(FragmentAddReminderBinding::inflate) {


    private val viewModel: AddReminderViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            viewModel.reminder = AddReminderFragmentArgs.fromBundle(it).reminder
            viewModel.myReminderList =
                ArrayList(AddReminderFragmentArgs.fromBundle(it).reminderList!!.toList())
        }
        initUi(viewModel.reminder)
    }

    private fun initUi(reminder: ReminderList.Reminder?) {

        if (reminder == null) {
            binding.lytToolbar.tvTitle.text = getString(R.string.text_add_reminder)


            val cal = Calendar.getInstance()
            viewModel.reminderHour = cal.get(Calendar.HOUR_OF_DAY)
            viewModel.reminderMinute = cal.get(Calendar.MINUTE)
            viewModel.reminderDay = cal.get(Calendar.DAY_OF_MONTH)
            viewModel.reminderMonth = cal.get(Calendar.MONTH) + 1
            viewModel.reminderYear = cal.get(Calendar.YEAR)

        } else {
            binding.lytToolbar.tvTitle.text = getString(R.string.text_edit_reminder)

            reminder.repeatDays?.forEachIndexed { index, i ->
                if (!i) {
                    viewModel.selectedWeekArray[index] = false
                }

            }

            with(reminder) {
                viewModel.reminderHour = hour
                viewModel.reminderMinute = minute
                viewModel.reminderDay = day
                viewModel.reminderMonth = month
                viewModel.reminderYear = year
            }
            binding.btnAllow.text = getString(R.string.text_update)

            binding.edNote.setText(reminder.label)

            viewModel.selectedRepeatValue = reminder.repeatMode ?: "Never"
        }
        setDate()
        setTime()
        repeatDays()
    }

    private fun repeatDays() {
        binding.tvRepeatData.text =
            ApplicationUtils.getRepeatReminderDays(viewModel.selectedWeekArray)
    }

    private fun setTime() {
        binding.tvReminderTime.text =
            DateFormats.formatTimeWithAmPm(viewModel.reminderHour, viewModel.reminderMinute)

    }

    private fun setDate() {
        binding.tvReminderDate.text =
            DateFormats.getReminderDate(
                viewModel.reminderMonth,
                viewModel.reminderDay,
                viewModel.reminderYear
            )
    }


    override fun initListener() {

        binding.tvRepeatData.setOnClickListener {

            setFragmentResultListener(REPEAT_REQUEST_KEY) { _, bundle ->
                val repeatList = bundle.getSerializable("repeat") as? List<Boolean>
                if (repeatList != null) {
                    repeatList.forEachIndexed { index, i ->
                        if (!i) {
                            viewModel.selectedWeekArray[index] = false
                        }

                    }
                    repeatDays()
                }


            }
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                LOGS.i("$position | $selectedValue")

                if (selectedValue != null) {
                    viewModel.selectedRepeatValue = selectedValue
                    binding.tvRepeatData.text = selectedValue
                }

            }

            if (viewModel.showRepeatLayout.value == RepeatLayoutType.LIST) {
                navigate(
                    AddReminderFragmentDirections.actionAddReminderFragmentToValueSelectorBottomSheet(
                        viewModel.selectedRepeatValue,
                        AppStaticData.getReminderRepeatValues(),
                        getString(R.string.text_repeat)
                    )
                )
            } else {
                navigate(
                    AddReminderFragmentDirections.actionAddReminderFragmentToRepeatBottomSheet(
                        viewModel.selectedWeekArray.toBooleanArray()
                    )
                )
            }


        }

        binding.tvReminderDate.setOnClickListener {
            setFragmentResultListener(DATE_REQUEST_KEY) { _, bundle ->
                val date = bundle.getInt("date")
                val month = bundle.getInt("month")
                val year = bundle.getInt("year")

                viewModel.reminderDay = date
                viewModel.reminderMonth = month + 1
                viewModel.reminderYear = year
                setDate()
            }
            navigate(
                AddReminderFragmentDirections.actionAddReminderFragmentToDateBottomSheet(
                    getString(R.string.text_date),
                    viewModel.reminderDay,
                    viewModel.reminderMonth,
                    viewModel.reminderYear,
                    false
                )
            )

        }

        binding.tvReminderTime.setOnClickListener {
            setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
                val hour = bundle.getInt("hour")
                val minute = bundle.getInt("minute")

                viewModel.reminderHour = hour
                viewModel.reminderMinute = minute

                setTime()
            }


            navigate(
                AddReminderFragmentDirections.actionAddReminderFragmentToTimeBottomSheet(
                    viewModel.reminderHour,
                    viewModel.reminderMinute,
                    1,
                    getString(R.string.text_time)
                )
            )

        }

        binding.lytToolbar.apply {

            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }

        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnAllow.setOnClickListener {
            val enteredText = binding.edNote.text.toString().trim()

            if (enteredText.isEmpty()) {
                context.showShortToast(getString(R.string.text_enter_reminder_label))
                return@setOnClickListener
            }

            if (viewModel.reminder == null && viewModel.checkReminderExist()) {
                context.showShortToast(getString(R.string.text_same_reminder_exist))
                return@setOnClickListener
            }

            viewModel.reminder = ReminderList.Reminder(
                label = enteredText,
                day = viewModel.reminderDay,
                month = viewModel.reminderMonth,
                year = viewModel.reminderYear,
                hour = viewModel.reminderHour,
                minute = viewModel.reminderMinute,
                repeatMode = viewModel.selectedRepeatValue,
                id = viewModel.reminder?.id ?: (0..1000).random(),
                repeatDays = viewModel.selectedWeekArray
            )


            viewModel.sessionManager.connectedDevice.value?.deviceType?.let {
                if (!it.equals(DeviceType.COLORFIT_NAV.deviceType, true) &&
                    !it.equals(DeviceType.COLORFIT_VISION.deviceType, true) &&
                    !it.equals(DeviceType.NOISEFIT_HYBRID.deviceType, true)
                ) {
                    sendData()
                } else {
                    binding.progressBar.root.visible()
                    viewModel.sessionManager.sendUpdateQueryAction(
                        UpdateDeviceAction.AddReminder(
                            viewModel.reminder!!
                        )
                    )
                }
            }


        }
    }

    private fun sendData() {
        setFragmentResult(
            REMINDER_REQUEST_KEY,
            bundleOf("reminder" to viewModel.reminder)
        )
        navigateUpSafe()
    }


    override fun subscribeObservers() {
        viewModel.showRepeatLayout.observe(viewLifecycleOwner) {
            if (it != null) {
                when (it) {
                    RepeatLayoutType.LIST -> {
                        binding.tvRepeatData.text = viewModel.selectedRepeatValue
                        binding.repeatContainer.visible()
                    }
                    RepeatLayoutType.WEEK -> {

                        binding.dateContainer.gone()
                        binding.repeatContainer.visible()
                    }
                    else -> {
                        binding.repeatContainer.gone()
                    }
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
        viewModel.sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) {
            it.getContent()?.let { callback ->
                if (callback is UpdateDeviceDataCallback.AddReminder) {
                    binding.progressBar.root.gone()
                    if (callback.success) {
                        context.showShortToast(getString(R.string.text_reminder_updated))
                        sendData()
                    } else {
                        context.showShortToast(getString(R.string.text_updated_failed))
                    }
                    //sessionManager.sendQueryAction(QueryAction.GetReminders)
                }
            }
        }
    }
}