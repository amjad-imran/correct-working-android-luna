package com.oreo.ui.sleep2.sleepplanner

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.noisefit.data.model.AlarmDataModel
import com.noisefit.data.model.SAActiveDayDataModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSetAlarmBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class SetAlarmFragment : BaseFragment<FragmentSetAlarmBinding>(FragmentSetAlarmBinding::inflate) {
    private val viewModel: SetAlarmViewModel by viewModels()
    private val mAdapter: SAActiveDaysAdapter by lazy {
        SAActiveDaysAdapter(object : OnActiveDayItemClick {
            override fun onItemClick(data: SAActiveDayDataModel, position: Int) {
                mAdapter.updateItem(data, position)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    private fun initUi() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_set_alarm)
        with(binding.lytActiveDays.rvDays) {
            adapter = mAdapter
        }
        mAdapter.setData(viewModel.getAlarmData())
        binding.lytTopView.lytBedTime.ivIcon.setImageResource(R.drawable.ic_bedtime_sleep)
        binding.lytTopView.lytBedTime.tvTitle.text = getString(R.string.text_bedtime)
        binding.lytTopView.lytBedTime.tvTime.text = "00:00"
        binding.lytTopView.lytBedTime.tvTimeUnit.text = "am"

        binding.lytTopView.lytWakeupTime.ivIcon.setImageResource(R.drawable.ic_wakeup_sleep)
        binding.lytTopView.lytWakeupTime.tvTitle.text = getString(R.string.text_wakeup)
        binding.lytTopView.lytWakeupTime.tvTime.text = "07:00"
        binding.lytTopView.lytWakeupTime.tvTimeUnit.text = "am"
        binding.lytAlarmTime.tvHour.text = "7"
        binding.lytAlarmTime.tvMin.text = "00"
        binding.lytAlarmTime.tvHour.paint.setShader(
            viewModel.setViewGradient(
                binding,
                4.toString()
            )
        )
        binding.lytAlarmTime.tvMin.paint.setShader(
            viewModel.setViewGradient(
                binding,
                30.toString()
            )
        )


    }

    /*
    * type
    * 0-bedtime
    * 1-wakeup time
    * */
    private fun showTimePicker(alarm: AlarmDataModel? = null, type: Int) {
        viewModel.picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Alarm Time").build()

        viewModel.picker.show(childFragmentManager, "AlarmManager")

        var hour: Int
        var state: String
        viewModel.picker.addOnPositiveButtonClickListener {
            if (viewModel.picker.hour > 12) {
                hour = viewModel.picker.hour - 12
                state = "PM"
            } else {
                hour = viewModel.picker.hour
                state = "AM"

            }

            viewModel.calendar = Calendar.getInstance()
            viewModel.calendar[Calendar.HOUR_OF_DAY] = viewModel.picker.hour
            viewModel.calendar[Calendar.MINUTE] = viewModel.picker.minute
            viewModel.calendar[Calendar.SECOND] = 0
            viewModel.calendar[Calendar.MILLISECOND] = 0

            if (alarm != null) {
                alarm.hour = hour
                alarm.minute = viewModel.picker.minute
                alarm.state = state
                alarm.timeInMillis = viewModel.calendar.timeInMillis
//                viewModel.update(alarm)
            } else {
                val newAlarm = AlarmDataModel(
                    hour = hour,
                    minute = viewModel.picker.minute,
                    state = state,
                    timeInMillis = viewModel.calendar.timeInMillis
                )
                viewModel.insert(newAlarm, type)
            }
        }
    }

    override fun initListener() {
        binding.lytTopView.lytBedTime.root.setOnClickListener {
            showTimePicker(type = 0)
        }
        binding.lytTopView.lytWakeupTime.root.setOnClickListener {
            showTimePicker(type = 1)
        }
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnSave.setOnClickListener {
            //
        }
        binding.lytAlarmSound.lytSoundView.tvSoundName.setOnClickListener {
            setFragmentResultListener(ALARM_SOUND) { _, bundle ->
                val data = bundle.getString("soundName")
                binding.lytAlarmSound.lytSoundView.tvSoundName.text = data
            }
            navigate(R.id.dialogAlarmSound)
        }
        binding.lytAlarmSound.switchMain.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                binding.lytAlarmSound.lytSoundView.root.visible()
            } else {
                binding.lytAlarmSound.lytSoundView.root.gone()
            }
        }

    }

    override fun subscribeObservers() {
        viewModel.alarmTimeUpdate.observe(this) {
            it.getContent()?.let { it1 ->
                val data = it1.first
                val hour: String = if (data.hour < 9)
                    "0${data.hour}"
                else
                    data.hour.toString()
                val minute: String = if (data.minute < 9)
                    "0${data.minute}"
                else
                    data.minute.toString()

                if (it1.second == 0) {
                    binding.lytTopView.lytBedTime.tvTime.text = "$hour:$minute"
                    binding.lytTopView.lytBedTime.tvTimeUnit.text = data.state.lowercase()
                } else {
                    binding.lytTopView.lytWakeupTime.tvTime.text = "$hour:$minute"
                    binding.lytTopView.lytWakeupTime.tvTimeUnit.text = data.state.lowercase()
                }
            }
        }
    }

}