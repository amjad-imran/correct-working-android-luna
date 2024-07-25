package com.noisefit.ui.common.bottomSheet

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.SleepTimeBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.utils.WheelAdapter
import com.noisefit_commans.utils.WheelItem
import dagger.hilt.android.AndroidEntryPoint

const val SLEEP_TIME_REQUEST_KEY = "SLEEP_TIME_REQUEST_KEY"

@AndroidEntryPoint
class SleepTimeBottomSheet : BaseBottomSheetWithTransparent<SleepTimeBottomSheetBinding>(
    SleepTimeBottomSheetBinding::inflate
) {

    private var mHour: Int = 0
    private var title: String = ""
    private var mMinute: Int = 0
    private var mUnitPosition = 0

    private var mHourOther: Int = 0
    private var mMinuteOther: Int = 0
    private var isStart: Int = 0

    private var lastSelectedPos: Int = 0
    private var dayName: String = ""
    private val dayList = ArrayList<WheelItem<String>>()

    private val wheelAdapter: WheelAdapter<String> by lazy {
        WheelAdapter()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val args = SleepTimeBottomSheetArgs.fromBundle(it)
            mMinute = args.minute
            mHour = args.hour
            title = args.title
            mHourOther = args.hourOther
            mMinuteOther = args.minuteOther
            isStart = args.isStart
            mUnitPosition = args.unitPosition
            lastSelectedPos=args.dayPos


            /* viewModel.userDayData =
                 mainViewModel.userHealthData[DateFormats.getTodaysDateString(10)]*/

            initUi()
        }


    }

    private fun initUi() {
        binding.tvTitle.text = title
        binding.lytTimePicker.timePicker.currentHour = mHour
        binding.lytTimePicker.timePicker.currentMinute = mMinute
        binding.lytTimePicker.timePicker.setOnTimeChangedListener { _, hour, minute ->
            this.mHour = hour
            this.mMinute = minute
        }

        binding.lytTimePicker.timePicker.descendantFocusability = DatePicker.FOCUS_BLOCK_DESCENDANTS
        setWheelPicker()

    }

    private fun dayData(): ArrayList<WheelItem<String>> {
        if (dayList.size > 0) {
            dayList.clear()
        }

        dayList.add(WheelItem("Yesterday"))
        dayList.add(WheelItem("Today"))
        return dayList
    }

    private fun setWheelPicker() {
        binding.lytTimePicker.wheelPicker.visibleItemCount = 2
        wheelAdapter.data = dayData()
        wheelAdapter.setOnItemSelectedListener { item ->
             dayName = item.split(" ")[0]
            updateSelectedIndex(wheelAdapter.currentItemPosition)
            lastSelectedPos = getUpdatedIndex()

        }
        wheelAdapter.bind(binding.lytTimePicker.wheelPicker)
        wheelAdapter.selectedItemPosition = getUpdatedIndex()
    }

    private fun updateSelectedIndex(pos: Int) {
        lastSelectedPos = pos
    }

    private fun getUpdatedIndex(): Int {
        dayList.forEachIndexed { index, wheelItem ->
            if (wheelItem == dayList[lastSelectedPos]) {
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
                    "hour" to mHour,
                    "minute" to mMinute,
                    "unit" to mUnitPosition,
                    "day" to dayName,
                    "dayPos" to lastSelectedPos
                )
            )
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

}