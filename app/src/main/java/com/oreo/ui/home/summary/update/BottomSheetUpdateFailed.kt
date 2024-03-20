package com.oreo.ui.home.summary.update

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.databinding.BottomSheetBadRatingBinding
import com.noisefit.luna.databinding.BottomSheetUpdateFailedBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.makeLinks
import com.noisefit_commans.utils.share.ShareUtil

const val UPDATE_FAILED = "UPDATE_FAILED"

class BottomSheetUpdateFailed : BaseBottomSheetWithTransparent<BottomSheetUpdateFailedBinding>(
    BottomSheetUpdateFailedBinding::inflate
) {
    override fun initListener() {

        binding.btnRemindLater.setOnClickListener {
            setFragmentResult(
                UPDATE_FAILED,
                bundleOf("remindLater" to true)
            )
            navigateUpSafe()
        }

        binding.btnTryAgain.setOnClickListener {
            setFragmentResult(
                UPDATE_FAILED,
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
