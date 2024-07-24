package com.noisefit.ui.common.bottomSheet

import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.SleepTimeBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
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

           /* viewModel.userDayData =
                mainViewModel.userHealthData[DateFormats.getTodaysDateString(10)]*/

            initUi()
        }


    }

    private fun initUi() {
        binding.tvTitle.text = title
        binding.timePicker.currentHour = mHour
        binding.timePicker.currentMinute = mMinute
        binding.timePicker.setOnTimeChangedListener { _, hour, minute ->
            this.mHour = hour
            this.mMinute = minute
        }

        binding.timePicker.descendantFocusability = DatePicker.FOCUS_BLOCK_DESCENDANTS

    }


    override fun initListener() {
        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnAllow.setOnClickListener {

           /* val existMessage =
                if (isStart == 1) {
                    viewModel.checkIfAnyEventExists(
                        String.format("%02d:%02d", mHour, mMinute),
                        String.format("%02d:%02d", mHourOther, mMinuteOther)
                    )
                } else {
                    viewModel.checkIfAnyEventExists(
                        String.format(
                            "%02d:%02d",
                            mHourOther,
                            mMinuteOther
                        ), String.format("%02d:%02d", mHour, mMinute)
                    )
                }

            if (existMessage.isNullOrEmpty().not()) {
                binding.lytMessage.visible()
                binding.tvMessage.text = existMessage
                return@setOnClickListener
            } else {
                binding.lytMessage.gone()
            }*/

            setFragmentResult(
                SLEEP_TIME_REQUEST_KEY,
                bundleOf("hour" to mHour, "minute" to mMinute, "unit" to mUnitPosition)
            )
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

}