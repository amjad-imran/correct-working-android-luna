package com.noisefit.ui.profile

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.databinding.BottomSheetDeleteAccountBinding
import com.noisefit_commans.ui.BaseBottomSheet
import dagger.hilt.android.AndroidEntryPoint

const val DELETE_KEY = "DELETE_KEY"

@AndroidEntryPoint
class DeleteAccountBottomSheet :
    BaseBottomSheet<BottomSheetDeleteAccountBinding>(BottomSheetDeleteAccountBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()

    }

    private fun initListener() {
        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnSave.setOnClickListener {
            setFragmentResult(
                DELETE_KEY,
                bundleOf("isSelected" to true)
            )
            navigateUpSafe()
        }

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
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog

    }
}