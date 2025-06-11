package com.oreo.ui.profile

import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.FragmentReportToDevelopersBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent

const val REPORT_TO_DEVS_KEY = "REPORT_TO_DEVS_KEY"

class ReportToDevelopersBottomSheet :
    BaseBottomSheetWithTransparent<FragmentReportToDevelopersBottomSheetBinding>(
        FragmentReportToDevelopersBottomSheetBinding::inflate
    ) {

    override fun initListener() {

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSubmit.setOnClickListener {
            dismiss()
            setFragmentResult(
                REPORT_TO_DEVS_KEY,
                Bundle().apply {
                    putString("title", binding.titleInputLayout.text.toString())
                    putString("desc", binding.descInputLayout.text.toString())
                }
            )
        }
    }

    override fun subscribeObservers() {

        binding.titleInputLayout.addTextChangedListener {
            binding.btnSubmit.isEnabled = it.isNullOrEmpty().not()
        }
    }


}