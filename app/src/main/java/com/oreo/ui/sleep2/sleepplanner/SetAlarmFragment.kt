package com.oreo.ui.sleep2.sleepplanner

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.data.model.SAActiveDayDataModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSetAlarmBinding
import com.noisefit.timepickerslider.TimeRangePicker
import com.noisefit.timepickerslider.TimeRangePicker.ClockFace
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.dpToPixel
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

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
        initTimePicker()
    }

    private fun initTimePicker() {

        binding.timePicker.apply {

            minDurationMinutes = 3 * 60
            maxDurationMinutes = 22 * 60

            thumbSize = 40f.dpToPixel().roundToInt()
            sliderWidth = 40f.dpToPixel().roundToInt()
            sliderColor = Color.TRANSPARENT
            thumbColor = Color.TRANSPARENT
            sliderRangeGradientStart = Color.parseColor("#7462A4")
            sliderRangeGradientMiddle = Color.parseColor("#845A64")
            sliderRangeGradientEnd = Color.parseColor("#1A1624")
            //thumbIconColor = Color.parseColor("#F79104")
            thumbSizeActiveGrow = 0f
            clockFace = ClockFace.APPLE
            hourFormat = TimeRangePicker.HourFormat.FORMAT_24
        }
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

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnSave.setOnClickListener {
            viewModel.selectedAlarmDays = mAdapter.getSelectedValue()
            viewModel.saveAlarm()
        }
        binding.lytAlarmSound.lytSoundView.tvSoundName.setOnClickListener {
            setFragmentResultListener(ALARM_SOUND) { _, bundle ->
                val data = bundle.getString("soundName")
                viewModel.alarmSound = data
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
        viewModel.startEndTime.observe(this) {
            val start = it.first
            val end = it.second

            binding.lytTopView.lytBedTime.tvTime.text = start.format(DateTimeFormatter.ofPattern("hh:mm"))
            binding.lytTopView.lytBedTime.tvTimeUnit.text = start.format(DateTimeFormatter.ofPattern("a"))
            binding.lytTopView.lytWakeupTime.tvTime.text = end.format(DateTimeFormatter.ofPattern("hh:mm"))
            binding.lytTopView.lytWakeupTime.tvTimeUnit.text = end.format(DateTimeFormatter.ofPattern("a"))

            val durationMinutes = viewModel.getDurationMinutes(it.first,it.second)
            val hours = durationMinutes / 60
            val minutes = durationMinutes % 60
            binding.lytAlarmTime.tvHour.text = hours.toString()
            binding.lytAlarmTime.tvMin.text = minutes.toString()

        }

        binding.timePicker.setOnTimeChangeListener(object : TimeRangePicker.OnTimeChangeListener {
            override fun onStartTimeChange(startTime: TimeRangePicker.Time) {
                viewModel.updateTime(startTime.localTime,binding.timePicker.endTime.localTime)
            }

            override fun onEndTimeChange(endTime: TimeRangePicker.Time) {
                viewModel.updateTime(binding.timePicker.startTime.localTime,endTime.localTime)
            }

            override fun onDurationChange(duration: TimeRangePicker.TimeDuration) {
                //updateDuration()
            }
        })

        binding.timePicker.setOnDragChangeListener(object : TimeRangePicker.OnDragChangeListener {
            override fun onDragStart(thumb: TimeRangePicker.Thumb): Boolean {
                if (thumb != TimeRangePicker.Thumb.BOTH) {
                    //animate(thumb, true)
                }
                /*Log.d(
                    "TimeRangePicker",
                    "Start time: " + binding.timePicker.startTime
                )
                Log.d(
                    "TimeRangePicker",
                    "End time: " + binding.timePicker.endTime
                )
                Log.d(
                    "TimeRangePicker",
                    "Total duration: " + binding.timePicker.duration
                )*/
                return true
            }

            override fun onDragStop(thumb: TimeRangePicker.Thumb) {
                if (thumb != TimeRangePicker.Thumb.BOTH) {
                    //animate(thumb, false)
                }

                /* Log.d(
                     "TimeRangePicker",
                     "Start time: " + binding.timePicker.startTime
                 )
                 Log.d(
                     "TimeRangePicker",
                     "End time: " + binding.timePicker.endTime
                 )
                 Log.d(
                     "TimeRangePicker",
                     "Total duration: " + binding.timePicker.duration
                 )*/
            }
        })
    }

}