package com.noisefit.ui.common.bottomSheet


import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import com.noisefit.luna.R
import com.noisefit.luna.databinding.SleepTimeBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.WheelAdapter
import com.noisefit_commans.utils.WheelItem
import com.oreo.data.model.OAddSleep
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

private const val YESTERDAY_HOUR = 20
const val SLEEP_TIME_REQUEST_KEY = "SLEEP_TIME_REQUEST_KEY"

@AndroidEntryPoint
class SleepTimeBottomSheet : BaseBottomSheetWithTransparent<SleepTimeBottomSheetBinding>(
    SleepTimeBottomSheetBinding::inflate
) {

    private var isStartDateToday = false
    private var showYesterdayToast = false
    private var showTodayToast = false
    private var addSleep = OAddSleep()

    private val dayList = ArrayList<WheelItem<String>>()

    private val wheelAdapter: WheelAdapter<String> by lazy {
        WheelAdapter()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val args = SleepTimeBottomSheetArgs.fromBundle(it)
            addSleep = args.addSleep.copy()
            isStartDateToday = args.isStartDateToday

            LOGS.d("sdjfhksj $isStartDateToday $addSleep")

            dayData()
            initUi()
        }

    }

    private fun initUi() {
        binding.tvTitle.text = addSleep.title
        binding.lytTimePicker.timePicker.currentHour = addSleep.hour.toInt()
        binding.lytTimePicker.timePicker.currentMinute = addSleep.minute.toInt()
        val calendar = Calendar.getInstance()
        /*val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)*/
        binding.lytTimePicker.timePicker.setOnTimeChangedListener { _, hour, minute ->
            checkCondition(
                hour,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                minute
            )
        }

        binding.lytTimePicker.timePicker.descendantFocusability = DatePicker.FOCUS_BLOCK_DESCENDANTS
        setWheelPicker()

    }

    private fun checkCondition(hour: Int, currentHour: Int, currentMinute: Int, minute: Int) {

        if (addSleep.day.equals("Today", true)) {

            if (hour > currentHour) {
                binding.lytTimePicker.timePicker.currentHour = currentHour
                binding.lytTimePicker.timePicker.currentMinute = currentMinute
                if (!showTodayToast) {
                    showTodayToast = true
                    context.showShortToast(getString(R.string.text_please_select_a_time_before_current_time))
                }

            }
            else if(hour == currentHour && minute > currentMinute){
                binding.lytTimePicker.timePicker.currentMinute = currentMinute
                if (!showTodayToast) {
                    showTodayToast = true
                    context.showShortToast(getString(R.string.text_please_select_a_time_before_current_time))
                }
            }
            else {
                if (hour != currentHour && minute != 0) {
                    showTodayToast = false
                }

                addSleep.hour = handleMinutes(hour)
                addSleep.minute = handleMinutes(minute)
            }
        } else {
            if (hour >= YESTERDAY_HOUR) {
                if (hour != YESTERDAY_HOUR && minute != 0) {
                    showYesterdayToast = false
                }

                addSleep.hour = handleMinutes(hour)
                addSleep.minute = handleMinutes(minute)
            } else {

                binding.lytTimePicker.timePicker.currentHour = YESTERDAY_HOUR
                binding.lytTimePicker.timePicker.currentMinute = 0
                addSleep.day = "Yesterday"
                wheelAdapter.selectedItemPosition = getUpdatedIndex()

                if (!showYesterdayToast) {
                    showYesterdayToast = true
                    context.showShortToast(getString(R.string.text_please_select_a_time_after_8_pm))
                }

            }

        }
    }

    private fun handleMinutes(value: Int): String {
        if (value < 10) {
            return "0$value"
        }
        return value.toString()
    }

    private fun dayData() {
        if (dayList.isNotEmpty()) {
            dayList.clear()
        }

        if (!isStartDateToday) {
            dayList.add(WheelItem("Yesterday"))
        }
        dayList.add(WheelItem("Today"))

        if (addSleep.day.isEmpty()) {
            addSleep.day = if(isStartDateToday){
                dayList[0].data
            }else{
                dayList[1].data
            }
        }
    }

    private fun setWheelPicker() {
        binding.lytTimePicker.wheelPicker.visibleItemCount = 2
        wheelAdapter.data = dayList
        wheelAdapter.setOnItemSelectedListener { item ->
            if (!viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                return@setOnItemSelectedListener
            }

            addSleep.day = item.split(" ")[0]

            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)
            val hour = binding.lytTimePicker.timePicker.currentHour
            val minute = binding.lytTimePicker.timePicker.currentMinute
            checkCondition(hour, currentHour, currentMinute, minute)

        }
        wheelAdapter.bind(binding.lytTimePicker.wheelPicker)
        wheelAdapter.selectedItemPosition = getUpdatedIndex()
    }

    private fun getUpdatedIndex(): Int {
        dayList.forEachIndexed { index, wheelItem ->
            if (wheelItem.data.equals(addSleep.day, true)) {
                return index
            }
        }
        return 0
    }


    override fun initListener() {
        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnAllow.setOnClickListener {

            setFragmentResult(
                SLEEP_TIME_REQUEST_KEY,
                bundleOf(
                    "sleepTime" to addSleep
                )
            )
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

    override fun onDestroyView() {
        // timePicker listener -> no-op to avoid holding the Fragment
        binding.lytTimePicker.timePicker.setOnTimeChangedListener { _, _, _ -> }

        // wheel listener -> no-op (in case WheelPicker posts runnables internally)
        wheelAdapter.setOnItemSelectedListener { _ -> }

        super.onDestroyView()
    }

}
