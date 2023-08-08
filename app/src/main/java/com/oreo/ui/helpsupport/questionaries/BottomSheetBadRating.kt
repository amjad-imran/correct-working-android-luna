package com.oreo.ui.helpsupport.questionaries

import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.BottomSheetBadRatingBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent

const val CALL_REQUEST_KEY = "CALL_REQUEST_KEY"

class BottomSheetBadRating : BaseBottomSheetWithTransparent<BottomSheetBadRatingBinding>(
    BottomSheetBadRatingBinding::inflate
) {
    override fun initListener() {
        binding.btnCall.setOnClickListener {
            setFragmentResult(
                CALL_REQUEST_KEY,
                bundleOf("call" to true)
            )
            navigateUpSafe()
        }
        binding.btnCancel.setOnClickListener {
            setFragmentResult(
                CALL_REQUEST_KEY,
                bundleOf("call" to false)
            )
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }
}