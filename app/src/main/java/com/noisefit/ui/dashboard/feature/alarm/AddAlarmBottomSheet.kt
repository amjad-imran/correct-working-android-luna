package com.noisefit.ui.dashboard.feature.alarm

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddAlarmBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.models.AlarmAction
import com.noisefit_commans.models.AlarmsList
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

const val ALARM_KEY = "alarm_key"

@AndroidEntryPoint
class AddAlarmBottomSheet : BaseBottomSheetWithTransparent<FragmentAddAlarmBinding>(
    FragmentAddAlarmBinding::inflate
) {

    private val viewModel: AddAlarmViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {

            viewModel.editAlarm = AddAlarmBottomSheetArgs.fromBundle(it).alarm
            viewModel.editAlarm?.let { alarm ->
                initUi(alarm)
                viewModel.isAlarmUpdated = true
            }
        }
        initTimePicker()
    }

    private fun initTimePicker() {
        binding.timePicker.currentHour = viewModel.mHour
        binding.timePicker.currentMinute = viewModel.mMinute
        binding.timePicker.setOnTimeChangedListener { _, hour, minute ->

            this.viewModel.mHour = hour
            this.viewModel.mMinute = minute
            LOGS.d("ADDED_ALARM Time ${this.viewModel.mHour} ${this.viewModel.mMinute}")
        }

    }

    private fun initUi(alarm: AlarmsList.Alarm) {

        viewModel.mMinute = alarm.minute
        viewModel.mHour = alarm.hour
        viewModel.selectedWeekArray = alarm.repeatDays as ArrayList<Boolean>
        setWeekArray()


    }

    private val weekViewArray: List<TextView> by lazy {
        arrayListOf(
            binding.layoutWeek.bMon,
            binding.layoutWeek.bTues,
            binding.layoutWeek.bWed,
            binding.layoutWeek.bThurs,
            binding.layoutWeek.bFri,
            binding.layoutWeek.bSat,
            binding.layoutWeek.bSun,
        )
    }

    private fun setWeekArray() {
        viewModel.selectedWeekArray.forEachIndexed { index, selected ->
            try {
                if (index == 0) return@forEachIndexed

                if (selected) {
                    weekViewArray[index - 1].setBackgroundResource(R.drawable.back_week_selected)
                } else {
                    weekViewArray[index - 1].setBackgroundResource(R.drawable.back_week_un_selected)
                }
            } catch (exp: Exception) {
                exp.printStackTrace()
            }
        }
    }


    private val onWeekSelectionListener = View.OnClickListener { view ->
        val clickedView = view as TextView
        val tag = clickedView.tag.toString().toIntOrNull() ?: return@OnClickListener

        val lastState = viewModel.selectedWeekArray[tag + 1]
        if (!lastState) {
            clickedView.setBackgroundResource(R.drawable.back_week_selected)
            viewModel.selectedWeekArray[tag + 1] = true
        } else {
            clickedView.setBackgroundResource(R.drawable.back_week_un_selected)
            viewModel.selectedWeekArray[tag + 1] = false
        }
    }


    override fun initListener() {
        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }


        binding.layoutWeek.bMon.setOnClickListener(onWeekSelectionListener)
        binding.layoutWeek.bTues.setOnClickListener(onWeekSelectionListener)
        binding.layoutWeek.bWed.setOnClickListener(onWeekSelectionListener)
        binding.layoutWeek.bThurs.setOnClickListener(onWeekSelectionListener)
        binding.layoutWeek.bFri.setOnClickListener(onWeekSelectionListener)
        binding.layoutWeek.bSat.setOnClickListener(onWeekSelectionListener)
        binding.layoutWeek.bSun.setOnClickListener(onWeekSelectionListener)

        binding.btnAllow.setOnClickListener {

            var alarmType = AlarmAction.ALARM_ADD
            if (viewModel.isAlarmUpdated) {
                alarmType = AlarmAction.ALARM_CHANGE
            }

//            val hour =
//                DateFormats.get24HourFrom12(selectedHour, selectedAm) ?: return@setOnClickListener

            val alarm = AlarmsList.Alarm(
                id = viewModel.editAlarm?.id ?: ApplicationUtils.generateRandomId(),
                alarmType = "custom",
                repeatMode = null,
                repeatDays = viewModel.selectedWeekArray.apply {
                    this[0] = true
                },
                hour = viewModel.mHour,
                minute = viewModel.mMinute,
                alarmAction = alarmType
            )
            setFragmentResult(
                ALARM_KEY,
                bundleOf("alarm" to alarm)
            )
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }
}