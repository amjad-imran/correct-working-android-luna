package com.noisefit.ui.profile

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.BottomSheetImagePickerBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent

/**
 * Returns 0 if Gallery is selected
 * Returns 1 if Camera is selected
 *
 * key -> selectedValue
 */
class BottomSheetImagePicker : BaseBottomSheetWithTransparent<BottomSheetImagePickerBinding>(
    BottomSheetImagePickerBinding::inflate
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



    }


    companion object {
        const val IMAGE_PICKER_RESULT = "image_picker_result"
    }

    override fun initListener() {
        binding.ivGalleryBack.setOnClickListener {
            setFragmentResult(
                IMAGE_PICKER_RESULT,
                bundleOf("selectedValue" to 0)
            )
            navigateUpSafe()
        }
        binding.ivCameraBack.setOnClickListener {
            setFragmentResult(
                IMAGE_PICKER_RESULT,
                bundleOf("selectedValue" to 1)
            )
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }
}