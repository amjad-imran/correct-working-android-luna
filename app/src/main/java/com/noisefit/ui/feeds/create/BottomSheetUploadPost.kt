package com.noisefit.ui.feeds.create

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.noisefit.luna.databinding.BottomSheetUploadPostBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetUploadPost : BaseBottomSheetWithTransparent<BottomSheetUploadPostBinding>(
    BottomSheetUploadPostBinding::inflate
) {

    companion object {
        fun getInstance(
            image: Bitmap?
        ): BottomSheetUploadPost {
            val data = Bundle()
            data.putParcelable("image", image)
            return BottomSheetUploadPost().apply {
                arguments = data
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bitmap = arguments?.getParcelable("image") as? Bitmap

        if (bitmap == null) {
            binding.ivImage.gone()
        } else {
            binding.ivImage.setImageBitmap(bitmap)
            binding.ivImage.visibility
        }

    }

    fun setProgress(progress: Int) {
        try {
            binding.pbSteps.progress = progress
        } catch (exp: Exception) {
        }
    }

    override fun initListener() {

    }


    override fun subscribeObservers() {

    }
}