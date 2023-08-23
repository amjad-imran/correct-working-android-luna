package com.oreo.ui.helpsupport.questionaries

import android.content.Intent
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.BottomSheetBadRatingBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.makeLinks
import com.noisefit_commans.utils.share.ShareUtil

const val CALL_REQUEST_KEY = "CALL_REQUEST_KEY"

class BottomSheetBadRating : BaseBottomSheetWithTransparent<BottomSheetBadRatingBinding>(
    BottomSheetBadRatingBinding::inflate
) {
    override fun initListener() {
        binding.tvDesc.makeLinks(
            false,
            Pair("luna.support@nexxbase.com", View.OnClickListener {
                context?.let {
                    ShareUtil.composeEmail(it,"luna.support@nexxbase.com","")
                }
            })
        )
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