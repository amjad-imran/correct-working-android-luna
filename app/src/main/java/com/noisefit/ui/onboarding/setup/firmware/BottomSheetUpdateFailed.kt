package com.noisefit.ui.onboarding.setup.firmware

import android.app.Dialog
import android.os.Bundle
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.databinding.BottomSheetUpdateFailOnboardBinding
import com.noisefit.luna.databinding.BottomSheetUpdateFailedBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent

const val UPDATE_FAILED_ONBOARD = "UPDATE_FAILED_ONBOARD"

class BottomSheetUpdateFailed : BaseBottomSheetWithTransparent<BottomSheetUpdateFailOnboardBinding>(
    BottomSheetUpdateFailOnboardBinding::inflate
) {

    override fun initListener() {

        binding.btnTryAgain.setOnClickListener {
            setFragmentResult(
                UPDATE_FAILED_ONBOARD,
                bundleOf("tryAgain" to true)
            )
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dia ->
            val dialog = dia as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isHideable = true
                isDraggable = true
                isCancelable = false
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }
}
