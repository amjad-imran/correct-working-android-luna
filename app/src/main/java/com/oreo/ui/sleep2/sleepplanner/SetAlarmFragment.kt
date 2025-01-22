package com.oreo.ui.sleep2.sleepplanner

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.data.local.AppStaticData
import com.noisefit.data.model.SAActiveDayDataModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSetAlarmBinding
import com.noisefit.timepickerslider.TimeRangePicker
import com.noisefit.timepickerslider.TimeRangePicker.ClockFace
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit.ui.profile.ProfileEditFragmentDirections
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.dpToPixel
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import dagger.hilt.android.AndroidEntryPoint
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@AndroidEntryPoint
class SetAlarmFragment : BaseFragment<FragmentSetAlarmBinding>(FragmentSetAlarmBinding::inflate) {
    private val viewModel: SetAlarmViewModel by viewModels()
    private val mAdapter: SAActiveDaysAdapter by lazy {
        SAActiveDaysAdapter(object : OnActiveDayItemClick {
            override fun onItemClick(data: SAActiveDayDataModel, position: Int) {
                viewModel.lastSelectedPosition = position
                if (data.isPreSelected) {
                    showAlreadyExistDialog()
                } else {
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
            }
        }
        navigate(R.id.bottomSheetChangeSchedule)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initTimePicker()

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


        /*binding.lytTopView.lytBedTime.tvTime.text = "00:00"
        binding.lytTopView.lytBedTime.tvTimeUnit.text = "am"

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
        )*/


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
        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.sleepPlannerCard.observe(this) {
            //updateUiData(it)

            mAdapter.setData(viewModel.getAlarmData())
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
            binding.lytAlarmTime.tvMin.text = minutes.toString()

        }

        binding.timePicker.setOnTimeChangeListener(object : TimeRangePicker.OnTimeChangeListener {
            override fun onStartTimeChange(startTime: TimeRangePicker.Time) {
                viewModel.updateTime(startTime.localTime, binding.timePicker.endTime.localTime)
            }

            override fun onEndTimeChange(endTime: TimeRangePicker.Time) {
                viewModel.updateTime(binding.timePicker.startTime.localTime, endTime.localTime)
            }

            override fun onDurationChange(duration: TimeRangePicker.TimeDuration) {}
        })
    }

}