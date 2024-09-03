package com.oreo.ui.findmyring

import com.noisefit.luna.databinding.BottomSheetMapTypeBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent

const val MAP_SELECTION_RESULT = "MAP_SELECTION_RESULT"

class BottomSheetMapType : BaseBottomSheetWithTransparent<BottomSheetMapTypeBinding>(
    BottomSheetMapTypeBinding::inflate
) {
    override fun initListener() {
        /* binding.btnCall.setOnClickListener {
             setFragmentResult(
                 MAP_SELECTION_RESULT,
                 bundleOf("isSatellite" to true)
             )
             navigateUpSafe()
         }
         binding.btnCancel.setOnClickListener {
             setFragmentResult(
                 MAP_SELECTION_RESULT,
                 bundleOf("isSatellite" to false)
             )
             navigateUpSafe()
         }*/

    }

    override fun subscribeObservers() {

    }
}