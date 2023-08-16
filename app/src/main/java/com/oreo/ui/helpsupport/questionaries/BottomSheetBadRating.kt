package com.oreo.ui.helpsupport.questionaries

import android.content.Intent
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.BottomSheetBadRatingBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.makeLinks

const val CALL_REQUEST_KEY = "CALL_REQUEST_KEY"

class BottomSheetBadRating : BaseBottomSheetWithTransparent<BottomSheetBadRatingBinding>(
    BottomSheetBadRatingBinding::inflate
) {
    override fun initListener() {
        binding.tvDesc.makeLinks(
            true,
            Pair("luna.support@nexxbase.com", View.OnClickListener {
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>("luna.support@nexxbase.com"))
                intent.putExtra(Intent.EXTRA_SUBJECT, "")
                intent.putExtra(Intent.EXTRA_TEXT, "")
                intent.type = "message/rfc822"
                startActivity(Intent.createChooser(intent, "Send email"))
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