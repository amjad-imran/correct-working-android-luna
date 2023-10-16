package com.noisefit.ui.common.bottomSheet

import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.FragmentDateBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

const val DATE_REQUEST_KEY = "DATE_REQUEST_KEY"

@AndroidEntryPoint
class DateBottomSheet : BaseBottomSheetWithTransparent<FragmentDateBottomSheetBinding>(
    FragmentDateBottomSheetBinding::inflate
) {

    private var mDate: Int = 0
    private var mMonth: Int = 0
    private var mYear: Int = 0
    private var mTitle: String? = null
    private var dobDialog = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val args = DateBottomSheetArgs.fromBundle(it)
            mDate = args.date
            mMonth = args.month - 1 //month Adjustment
            mYear = args.year
            mTitle = args.title
            dobDialog = args.dobDialog

        }
        binding.tvTitle.text = mTitle
        initUi()
    }

    private fun initUi() {
        binding.datePicker.init(
            mYear, mMonth, mDate
        ) { _, year, monthOfYear, dayOfMonth ->
            mDate = dayOfMonth
            mMonth = monthOfYear
            mYear = year

        }
        val c = Calendar.getInstance()
        if (dobDialog) {

            c.add(Calendar.YEAR, -10)
            binding.datePicker.maxDate = c.timeInMillis
            c.add(Calendar.YEAR,-89)
            binding.datePicker.minDate =c.timeInMillis
        } else {
            c.add(Calendar.YEAR, 10)
            binding.datePicker.maxDate = c.timeInMillis
            binding.datePicker.minDate = System.currentTimeMillis()
        }
        binding.datePicker.descendantFocusability = DatePicker.FOCUS_BLOCK_DESCENDANTS

    }

    override fun initListener() {


        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnAllow.setOnClickListener {
            setFragmentResult(
                DATE_REQUEST_KEY,
                bundleOf("date" to mDate, "month" to mMonth, "year" to mYear)
            )
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

}