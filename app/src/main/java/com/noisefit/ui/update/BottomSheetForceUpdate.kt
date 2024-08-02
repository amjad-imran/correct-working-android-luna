package com.noisefit.ui.update

import android.app.Dialog
import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.databinding.BottomSheetForceUpdateBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.utils.share.ShareUtil


class BottomSheetForceUpdate :
    BaseBottomSheetWithTransparent<BottomSheetForceUpdateBinding>(BottomSheetForceUpdateBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, com.noisefit_commans.R.style.DialogStyle)
    }

    override fun initListener() {
        binding.btnDone.setOnClickListener {
            ShareUtil.openPlayStore(requireContext(), "com.noisefit.luna")
        }

    }

    override fun subscribeObservers() {

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
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