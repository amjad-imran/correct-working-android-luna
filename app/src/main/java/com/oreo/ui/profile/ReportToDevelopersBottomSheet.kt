package com.oreo.ui.profile

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.BottomSheetBadRatingBinding
import com.noisefit.luna.databinding.FragmentReportToDevelopersBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.utils.LOGS

const val REPORT_TO_DEVS_KEY = "REPORT_TO_DEVS_KEY"

class ReportToDevelopersBottomSheet :
    BaseBottomSheetWithTransparent<FragmentReportToDevelopersBottomSheetBinding>(FragmentReportToDevelopersBottomSheetBinding::inflate) {

    override fun initListener() {
        binding.btnCancel.setOnClickListener {

        }

        binding.btnSubmit.setOnClickListener {
            dismiss()
            setFragmentResult(
                REPORT_TO_DEVS_KEY,
                Bundle().apply {
                    putString("title" , binding.titleEtv.text.toString())
                    putString("desc" , binding.descEtv.text.toString())
                }
            )
        }
    }

    override fun subscribeObservers() {

    }


}