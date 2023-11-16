package com.oreo.ui.info

import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.BottomSheetMyDeviceIntroBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent

const val CALL_GOT_IT = "CALL_GOT_IT"

class BottomSheetMyDeviceIntro : BaseBottomSheetWithTransparent<BottomSheetMyDeviceIntroBinding>(
    BottomSheetMyDeviceIntroBinding::inflate
) {

    override fun initListener() {
        binding.btnGotIt.setOnClickListener {
            setFragmentResult(CALL_GOT_IT, bundleOf("isSelected" to true))
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
    }
}