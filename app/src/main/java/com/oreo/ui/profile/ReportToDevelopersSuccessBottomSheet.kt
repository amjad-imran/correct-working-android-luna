package com.oreo.ui.profile

import com.noisefit.luna.databinding.FragmentReportToDevelopersSuccessBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent

class ReportToDevelopersSuccessBottomSheet :
    BaseBottomSheetWithTransparent<FragmentReportToDevelopersSuccessBottomSheetBinding>(
        FragmentReportToDevelopersSuccessBottomSheetBinding::inflate)
{
    override fun initListener() {
        binding.btnDone.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

}