package com.oreo.ui.sleep2.sleepplanner

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.data.model.AlarmSoundDataModel
import com.noisefit.data.model.SAActiveDayDataModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSetAlarmBinding
import com.noisefit.timepickerslider.TimeRangePicker
import com.noisefit.timepickerslider.TimeRangePicker.ClockFace
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.dpToPixel
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.util.DateTimeUtil
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
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

                    uiController.logAppEvent(
                        MoEngageLunaAppEvents.sleep_planner_setup_alarm_info,
                        hashMapOf("source" to "sleep", "target" to "active_days")
                    )
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initTimePicker()
        viewModel.setEditMode(args.bedTime, args.wakeTime)

        initUi()

        viewModel.getSleepPlanerDetails()
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

    private fun initTimePicker() {

        binding.timePicker.apply {

            minDurationMinutes = 3 * 60
            maxDurationMinutes = 20 * 60

            thumbSize = 40f.dpToPixel().roundToInt()
            sliderWidth = 40f.dpToPixel().roundToInt()
            sliderColor = Color.TRANSPARENT
            thumbColor = Color.TRANSPARENT
            sliderRangeGradientStart = Color.parseColor("#7462A4")
            sliderRangeGradientMiddle = Color.parseColor("#845A64")
            sliderRangeGradientEnd = Color.parseColor("#845A64")
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

        binding.lytTopView.lytBedTime.ivIcon.setColorFilter(
            Color.parseColor("#C5A8ED"),
            android.graphics.PorterDuff.Mode.SRC_IN
        )


        binding.lytTopView.lytBedTime.tvTitle.text = getString(R.string.text_bedtime)
        binding.lytTopView.lytBedTime.tvTitle.setTextColor(Color.parseColor("#C5A8ED"))
        binding.lytTopView.lytWakeupTime.ivIcon.setImageResource(R.drawable.ic_wakeup_sleep)
        binding.lytTopView.lytWakeupTime.ivIcon.setColorFilter(
            Color.parseColor("#C5A8ED"),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        binding.lytTopView.lytWakeupTime.tvTitle.text = getString(R.string.text_wakeup)
        binding.lytTopView.lytWakeupTime.tvTitle.setTextColor(Color.parseColor("#C5A8ED"))

        if (viewModel.editModeSelectedTime != null) {
            viewModel.deleteMode.postValue(true)
        }
    }


    private fun setVolumeSeekbar() {
        val audioManager = requireActivity().getSystemService(AUDIO_SERVICE) as AudioManager
        binding.lytAlarmSound.lytSoundView.apply {
            volumeSeekBar.progress = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            volumeSeekBar.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            ivHighVol.setOnClickListener {

                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_ALARM,
                    AudioManager.ADJUST_RAISE,
                    0
                )
                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
                volumeSeekBar.progress = currentVolume
            }
            ivLowVol.setOnClickListener {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_ALARM,
                    AudioManager.ADJUST_LOWER,
                    0
                )
                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
                volumeSeekBar.progress = currentVolume
            }
            volumeSeekBar.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {


                    if (!fromUser) {
                        return
                    }

                    if (progress < 1) {
                        seekBar?.progress = 1
                    }


                    audioManager.setStreamVolume(
                        AudioManager.STREAM_ALARM,
                        progress,
                        0
                    );


                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }

            })
        }


    }

    private fun showAllowAlarmPermission(isScheduleAlarm: Boolean) {
        uiController.onApiErrorReceived(
            ErrorResponse(
                UIComponentType.AreYouSureDialog(
                    getString(R.string.text_permission_required),
                    getString(R.string.text_permission_denial_alarm),
                    false,
                    getString(R.string.text_allow),
                    object : BinaryActionCallback {
                        override fun yes() {
                            if (isScheduleAlarm) {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                intent.setData(Uri.parse("package:" + requireActivity().packageName))
                                requireActivity().startActivity(intent)
                            } else {
                                val intent =
                                    Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                                intent.setData(Uri.parse("package:" + requireActivity().packageName))
                                requireActivity().startActivity(intent)
                            }

                        }

                        override fun no() {

                        }
                    }
                )
            )
        )

    }

    private fun hasFullScreenIntentPermission(): Boolean {
        val notificationManager =
            requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            notificationManager.canUseFullScreenIntent()
        } else {
            true
        }
    }

    private fun hasExactAlarmPermission(): Boolean {
        val notificationManager =
            requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    override fun initListener() {
        setVolumeSeekbar()
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.btnSave.setOnClickListener {

            if (!hasExactAlarmPermission()) {
                showAllowAlarmPermission(true)
                return@setOnClickListener
            }

            if (!hasFullScreenIntentPermission()) {
                showAllowAlarmPermission(false)
                return@setOnClickListener
            }
            val selectedAlarms = mAdapter.getSelectedValue()
            if (selectedAlarms.isEmpty()) return@setOnClickListener

            uiController.logAppEvent(
                MoEngageLunaAppEvents.sleep_planner_setup_alarm_info,
                hashMapOf("source" to "sleep", "target" to "save_the_alarm")
            )

            viewModel.updateAlarms(selectedAlarms, mAdapter.getUnselectedItems())
        }

        binding.lytAlarmSound.lytSoundView.tvSoundName.setOnClickListener {
            setFragmentResultListener(ALARM_SOUND) { _, bundle ->
                val data = bundle.getParcelable("alarmTone") as? AlarmSoundDataModel
                if (data != null) {
                    viewModel.selectedTone.postValue(data)
                    viewModel.deleteMode.postValue(false)
                }
            }
            uiController.logAppEvent(
                MoEngageLunaAppEvents.sleep_planner_setup_alarm_info,
                hashMapOf("source" to "sleep", "target" to "alarm_sounds")
            )

            navigate(
                R.id.dialogAlarmSound,
                bundleOf(
                    "alarmToneList" to viewModel.alarmTonesList().toTypedArray(),
                    "selectedKey" to (viewModel.selectedTone.value?.key ?: 1)
                )
            )
        }

    }

    override fun subscribeObservers() {

        viewModel.selectedTone.observe(this) {
            binding.lytAlarmSound.lytSoundView.tvSoundName.text = it.title
        }
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

                viewModel.setDefaultTone(viewModel.editModeSelectedTime!!)
            } else {
                binding.timePicker.setPeriod(LocalTime.of(22, 0), LocalTime.of(6, 0))
            }
            //binding.timePicker.setSleepMinDuration(it?.planner?.min_duration ?: 0L)
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

            val durationMinutes = DateTimeUtil.getDurationMinutes(it.first, it.second)
            val hours = durationMinutes / 60
            val minutes = durationMinutes % 60
            binding.lytAlarmTime.tvHour.text = hours.toString()
            binding.lytAlarmTime.tvMin.text = String.format("%02d", minutes)

            val isDurationMin = viewModel.isDurationMin(durationMinutes)

            setTextGradient(isDurationMin, binding.lytAlarmTime.tvHour)
            setTextGradient(isDurationMin, binding.lytAlarmTime.tvMin)

            if (isDurationMin) {
                binding.ivTick.gone()
                binding.tvGoalDesc.text =
                    getString(R.string.text_this_schedule_does_not_meet_your_sleep_goal)
            } else {
                binding.ivTick.visible()
                binding.tvGoalDesc.text =
                    getString(R.string.text_this_schedule_meets_your_sleep_goal)
            }
        }

        binding.timePicker.setOnTimeChangeListener(object : TimeRangePicker.OnTimeChangeListener {
            override fun onStartTimeChange(startTime: TimeRangePicker.Time) {
                viewModel.updateTime(startTime.localTime, binding.timePicker.endTime.localTime)
                if (viewModel.editModeSelectedTime != null) {
                    viewModel.deleteMode.postValue(false)
                }
                setPickerGradient(startTime.localTime, binding.timePicker.endTime.localTime)

            }

            override fun onEndTimeChange(endTime: TimeRangePicker.Time) {
                viewModel.updateTime(binding.timePicker.startTime.localTime, endTime.localTime)
                if (viewModel.editModeSelectedTime != null) {
                    viewModel.deleteMode.postValue(false)
                }
                setPickerGradient(binding.timePicker.startTime.localTime, endTime.localTime)

            }

            override fun onDurationChange(duration: TimeRangePicker.TimeDuration) {}
        })
    }

    fun setPickerGradient(start: LocalTime, end: LocalTime) {
        val durationMinutes = DateTimeUtil.getDurationMinutes(start, end)

        if (viewModel.isDurationMin(durationMinutes)) {
            binding.timePicker.apply {
                sliderRangeGradientStart = Color.parseColor("#A46262")
                sliderRangeGradientMiddle = Color.parseColor("#BF5B6D")
                sliderRangeGradientEnd = Color.parseColor("#BF5B6D")
            }
        } else {
            binding.timePicker.apply {
                sliderRangeGradientStart = Color.parseColor("#7462A4")
                sliderRangeGradientMiddle = Color.parseColor("#845A64")
                sliderRangeGradientEnd = Color.parseColor("#845A64")
            }
        }
    }

    private fun setTextGradient(durationMin: Boolean, text: TextView) {
        if (durationMin) {
            text.setTextColor(Color.parseColor("#FFFFFF"))
            val textShader: Shader = LinearGradient(
                0f,
                text.paint.measureText(text.text.toString()),
                0f,
                0f,
                intArrayOf(
                    Color.parseColor("#FF7C94"),
                    Color.parseColor("#FFC8D2"),
                ),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            text.paint.shader = textShader
        } else {
            text.setTextColor(Color.parseColor("#FFFFFF"))
            val textShader: Shader = LinearGradient(
                0f,
                text.paint.measureText(text.text.toString()),
                0f,
                0f,
                intArrayOf(
                    Color.parseColor("#C5A8ED"),
                    Color.parseColor("#FFFFFF"),
                ),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            text.paint.shader = textShader
        }

    }

}