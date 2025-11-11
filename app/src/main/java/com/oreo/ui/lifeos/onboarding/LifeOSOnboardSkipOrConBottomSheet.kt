package com.oreo.ui.lifeos.onboarding

import android.app.Dialog
import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOSOnboardSkipOrConBottomSheetBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.BindingEvents
import com.noisefit_commans.utils.LogEvents

const val LIFEOS_ONBOARD_BS_KEY = "LIFEOS_ONBOARD_BS_KEY"

class LifeOSOnboardSkipOrConBottomSheet :
    BaseBottomSheetWithTransparent<FragmentLifeOSOnboardSkipOrConBottomSheetBinding>(
        FragmentLifeOSOnboardSkipOrConBottomSheetBinding::inflate
    ) {

    override fun initListener() {

        binding.btnSaveAndExit.setOnClickListener {
            if (!ApplicationUtils.isInternetConnected()) {
                AppLogs.sendAppLogs(LogEvents.Binding, BindingEvents.NetworkIssue)
                context.showShortToast(getString(R.string.text_no_internet_connection))
                return@setOnClickListener
            }

            dismiss()
            setFragmentResult(
                LIFEOS_ONBOARD_BS_KEY,
                Bundle().apply {
                    putBoolean("saveAndExit", true)
                }
            )
        }

        binding.btnContinue.setOnClickListener {

            if (!ApplicationUtils.isInternetConnected()) {
                AppLogs.sendAppLogs(LogEvents.Binding, BindingEvents.NetworkIssue)
                context.showShortToast(getString(R.string.text_no_internet_connection))
                return@setOnClickListener
            }

            dismiss()
            setFragmentResult(
                LIFEOS_ONBOARD_BS_KEY,
                Bundle().apply {
                    putBoolean("saveAndExit", false)
                }
            )
        }
    }

    override fun subscribeObservers() {

    }

    override fun getTheme(): Int {
        return R.style.MyCustomDialogStyleWithBlurEffect
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