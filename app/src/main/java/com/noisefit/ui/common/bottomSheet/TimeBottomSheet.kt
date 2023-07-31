package com.noisefit.ui.common.bottomSheet

import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.FragmentTimeBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint

const val TIME_REQUEST_KEY = "TIME_REQUEST_KEY"

@AndroidEntryPoint
class TimeBottomSheet : BaseBottomSheetWithTransparent<FragmentTimeBottomSheetBinding>(
    FragmentTimeBottomSheetBinding::inflate
) {


    private var mHour: Int = 0
    private var title: String = ""
    private var mMinute: Int = 0
    private var mUnitPosition = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val args = TimeBottomSheetArgs.fromBundle(it)
            mMinute = args.minute
            mHour = args.hour
            title = args.title
            mUnitPosition = args.unitPosition
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
            setFragmentResult(
                TIME_REQUEST_KEY,
                bundleOf("hour" to mHour, "minute" to mMinute, "unit" to mUnitPosition)
            )
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

}