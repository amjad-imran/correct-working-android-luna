package com.oreo.ui.circadianAlignment

import android.app.Dialog
import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLogCircadianBottomSheetBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.BindingEvents
import com.noisefit_commans.utils.LogEvents


const val LOG_CIRCADIAN_BOTTOM_SHEET_KEY = "LOG_CIRCADIAN_BOTTOM_SHEET_KEY"

class LogCircadianBottomSheetFragment  :
    BaseBottomSheetWithTransparent<FragmentLogCircadianBottomSheetBinding>(
        FragmentLogCircadianBottomSheetBinding::inflate
    ) {
    override fun initListener() {
        binding.btnYes.setOnClickListener {
            if (!ApplicationUtils.isInternetConnected()) {
                AppLogs.sendAppLogs(LogEvents.Binding, BindingEvents.NetworkIssue)
                context.showShortToast(getString(R.string.text_no_internet_connection))
                return@setOnClickListener
            }

            dismiss()
            setFragmentResult(
                LOG_CIRCADIAN_BOTTOM_SHEET_KEY,
                Bundle().apply {
                    putString("key", "")
                }
            )
        }

        binding.btnNo.setOnClickListener {
            dismiss()
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
                isCancelable = true
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }

}