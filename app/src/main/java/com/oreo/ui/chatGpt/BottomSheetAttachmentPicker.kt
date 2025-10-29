package com.oreo.ui.chatGpt

import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.BottomSheetAttachmentPickerBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent


const val ATTACHMENT_KEY = "ATTACHMENT_KEY"

class BottomSheetAttachmentPicker :
    BaseBottomSheetWithTransparent<BottomSheetAttachmentPickerBinding>(
        BottomSheetAttachmentPickerBinding::inflate
    ) {
    override fun initListener() {

        binding.ivCamera.setOnClickListener {
            setFragmentResult(
                ATTACHMENT_KEY,
                bundleOf("type" to "camera")
            )
            navigateUpSafe()
        }
        binding.ivPhotos.setOnClickListener {
            setFragmentResult(
                ATTACHMENT_KEY,
                bundleOf("type" to "photo")
            )
            navigateUpSafe()
        }
        binding.ivFiles.setOnClickListener {
            setFragmentResult(
                ATTACHMENT_KEY,
                bundleOf("type" to "file")
            )
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }
}