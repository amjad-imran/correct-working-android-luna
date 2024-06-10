package com.oreo.ui.femalehealth.cycletracker

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.databinding.BottomSheetComingSoonBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent

const val NOTIFY_LOG = "NOTIFY_LOG"

class BottomSheetComingSoon : BaseBottomSheetWithTransparent<BottomSheetComingSoonBinding>(
    BottomSheetComingSoonBinding::inflate
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }


    override fun initListener() {
        binding.btnNotifyMe.setOnClickListener {
            setFragmentResult(
                NOTIFY_LOG,
                bundleOf("agree" to true)
            )
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