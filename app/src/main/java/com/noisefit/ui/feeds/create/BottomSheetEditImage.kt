package com.noisefit.ui.feeds.create

import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.databinding.BottomSheetEditImageBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent

/**
 * Returns 0 if remove is selected
 * Returns 1 if replace is selected
 *
 * key -> selectedValue
 */
class BottomSheetEditImage : BaseBottomSheetWithTransparent<BottomSheetEditImageBinding>(
    BottomSheetEditImageBinding::inflate
) {

    private val args :BottomSheetEditImageArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vBackImage.setImageURI(null)
        binding.vBackImage.setImageURI(args.imageUri.toUri())
    }


    companion object {
        const val IMAGE_EDIT_RESULT = "IMAGE_EDIT_RESULT"
    }

    override fun initListener() {
        binding.bRemove.setOnClickListener {
            setFragmentResult(
                IMAGE_EDIT_RESULT,
                bundleOf("selectedValue" to 0)
            )
            navigateUpSafe()
        }
        binding.bReplace.setOnClickListener {
            setFragmentResult(
                IMAGE_EDIT_RESULT,
                bundleOf("selectedValue" to 1)
            )
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }
}