package com.oreo.ui.sleep2.sleepplanner

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.data.model.SAActiveDayDataModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSetAlarmBinding
import com.noisefit.timepickerslider.TimeRangePicker
import com.noisefit.timepickerslider.TimeRangePicker.ClockFace
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.dpToPixel
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@AndroidEntryPoint
class SetAlarmFragment : BaseFragment<FragmentSetAlarmBinding>(FragmentSetAlarmBinding::inflate) {
    private val viewModel: SetAlarmViewModel by viewModels()

    val args: SetAlarmFragmentArgs by navArgs()

    private val mAdapter: SAActiveDaysAdapter by lazy {
        SAActiveDaysAdapter(object : OnActiveDayItemClick {
            override fun onItemClick(data: SAActiveDayDataModel, position: Int) {
                viewModel.lastSelectedPosition = position
                if (data.isPreSelected) {
                    showAlreadyExistDialog()
                } else {
                    viewModel.deleteMode.postValue(false)
                    mAdapter.updateItem(position)
                }
            }
        })
    }

    private fun showAlreadyExistDialog() {

        setFragmentResultListener(CHANGE_SCHEDULE) { _, bundle ->
            val change = bundle.getBoolean("change")
            if (change && viewModel.lastSelectedPosition != null) {
                mAdapter.updateItem(viewModel.lastSelectedPosition!!)
                viewModel.lastSelectedPosition = null
                viewModel.deleteMode.postValue(false)
            }
        }
        navigate(R.id.bottomSheetChangeSchedule)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initTimePicker()
        viewModel.setEditMode(args.bedTime, args.wakeTime)

        initUi()

        viewModel.getSleepPlanerDetails()
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

        binding.lytTopView.lytBedTime.ivIcon.setImageResource(R.drawable.ic_bedtime_sleep)
        binding.lytTopView.lytBedTime.tvTitle.text = getString(R.string.text_bedtime)
        binding.lytTopView.lytWakeupTime.ivIcon.setImageResource(R.drawable.ic_wakeup_sleep)
        binding.lytTopView.lytWakeupTime.tvTitle.text = getString(R.string.text_wakeup)

        if (viewModel.editModeSelectedTime != null) {
            viewModel.deleteMode.postValue(true)
        }
    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.btnSave.setOnClickListener {
            val selectedAlarms = mAdapter.getSelectedValue()
            if (selectedAlarms.isEmpty()) return@setOnClickListener
            viewModel.updateAlarms(selectedAlarms, mAdapter.getUnselectedItems())
        }

        binding.lytAlarmSound.lytSoundView.tvSoundName.setOnClickListener {
            setFragmentResultListener(ALARM_SOUND) { _, bundle ->
                val data = bundle.getString("soundName")
                viewModel.alarmSound = data
                binding.lytAlarmSound.lytSoundView.tvSoundName.text = data
            }
            navigate(R.id.dialogAlarmSound)
        }

    }

    override fun subscribeObservers() {
        viewModel.deleteMode.observe(this) {
            if (it) {
                binding.btnSave.text = getString(R.string.text_delete_alarm)
                binding.btnSave.setTextColor(Color.parseColor("#FF7C94"))
            } else {
                binding.btnSave.text = getString(R.string.text_save_the_alarm)
                binding.btnSave.setTextColor(Color.parseColor("#FFFFFF"))
            }
        }
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.alarmUpdated.observe(this) {
            it.getContent()?.let {
                navigateUpSafe()
            }
        }

        viewModel.sleepPlannerCard.observe(this) {
            mAdapter.setData(viewModel.getAlarmData(it?.alarms))

            if (viewModel.editModeSelectedTime != null) {
                val start = viewModel.editModeSelectedTime!!.first
                val end = viewModel.editModeSelectedTime!!.second

                binding.timePicker.setPeriod(start, end)
            }
            binding.lytMain.visible()
        }


        viewModel.startEndTime.observe(this) {
            val start = it.first
            val end = it.second

            binding.lytTopView.lytBedTime.tvTime.text =
                start.format(DateTimeFormatter.ofPattern("hh:mm"))
            binding.lytTopView.lytBedTime.tvTimeUnit.text =
                start.format(DateTimeFormatter.ofPattern("a"))
            binding.lytTopView.lytWakeupTime.tvTime.text =
                end.format(DateTimeFormatter.ofPattern("hh:mm"))
            binding.lytTopView.lytWakeupTime.tvTimeUnit.text =
                end.format(DateTimeFormatter.ofPattern("a"))

            val durationMinutes = viewModel.getDurationMinutes(it.first, it.second)
            val hours = durationMinutes / 60
            val minutes = durationMinutes % 60
            binding.lytAlarmTime.tvHour.text = hours.toString()
            binding.lytAlarmTime.tvMin.text = String.format("%02d", minutes)
        }

        binding.timePicker.setOnTimeChangeListener(object : TimeRangePicker.OnTimeChangeListener {
            override fun onStartTimeChange(startTime: TimeRangePicker.Time) {
                viewModel.updateTime(startTime.localTime, binding.timePicker.endTime.localTime)
                if (viewModel.editModeSelectedTime != null) {
                    viewModel.deleteMode.postValue(false)
                }
            }

            override fun onEndTimeChange(endTime: TimeRangePicker.Time) {
                viewModel.updateTime(binding.timePicker.startTime.localTime, endTime.localTime)
                if (viewModel.editModeSelectedTime != null) {
                    viewModel.deleteMode.postValue(false)
                }
            }

            override fun onDurationChange(duration: TimeRangePicker.TimeDuration) {}
        })
    }

}