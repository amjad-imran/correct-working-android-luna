package com.noisefit.ui.dashboard.feature.qrPayment.bottomsheets

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.noisefit.R
import com.noisefit.databinding.BottomSheetUploadLinkBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.showShortToast
import dagger.hilt.android.AndroidEntryPoint

const val QR_UPLOAD_VIA_LINK = "QR_UPLOAD_VIA_LINK"

@AndroidEntryPoint
class BottomSheetAddLink :
    BaseBottomSheetWithTransparent<BottomSheetUploadLinkBinding>(BottomSheetUploadLinkBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,com.noisefit_commans.R.style.DialogStyle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //showSoftKeyboard(binding.etLink)

    }

    override fun initListener() {

        binding.btnBack.setOnClickListener {
            navigateUpSafe()
            setFragmentResult(
                QR_UPLOAD_VIA_LINK,
                bundleOf("isBackPressed" to true)
            )
        }
        binding.btnNext.setOnClickListener {
            val enteredText = binding.etLink.text.toString().trim()

            if (enteredText.isEmpty()) {
                context.showShortToast("Add Link to continue")
                return@setOnClickListener
            }

            navigateUpSafe()
            setFragmentResult(
                QR_UPLOAD_VIA_LINK,
                bundleOf("isBackPressed" to false, "url" to enteredText)
            )
        }
    }

    override fun subscribeObservers() {

    }
}