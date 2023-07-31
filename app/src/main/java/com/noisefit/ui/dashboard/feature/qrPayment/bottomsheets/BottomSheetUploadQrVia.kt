package com.noisefit.ui.dashboard.feature.qrPayment.bottomsheets

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.noisefit.R
import com.noisefit.databinding.BottomSheetUploadQrViaBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint

const val QR_UPLOAD_VIA = "QR_UPLOAD_VIA"

@AndroidEntryPoint
class BottomSheetUploadQrVia :
    BaseBottomSheetWithTransparent<BottomSheetUploadQrViaBinding>(BottomSheetUploadQrViaBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,com.noisefit_commans.R.style.DialogStyle)
    }

    override fun initListener() {

        binding.btnLink.setOnClickListener {
            navigateUpSafe()
            setFragmentResult(
                QR_UPLOAD_VIA,
                bundleOf("uploadMode" to UploadMode.LINK)
            )
        }
        binding.btnGallery.setOnClickListener {
            navigateUpSafe()
            setFragmentResult(
                QR_UPLOAD_VIA,
                bundleOf("uploadMode" to UploadMode.GALLERY)
            )
        }
    }

    override fun subscribeObservers() {

    }
}

enum class UploadMode {
    GALLERY, LINK
}