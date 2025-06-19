package com.oreo.ui.profile

import android.app.Dialog
import android.os.Bundle
import android.widget.FrameLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentReportToDevelopersBottomSheetBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.BindingEvents
import com.noisefit_commans.utils.LogEvents

const val REPORT_TO_DEVS_KEY = "REPORT_TO_DEVS_KEY"

class ReportToDevelopersBottomSheet :
    BaseBottomSheetWithTransparent<FragmentReportToDevelopersBottomSheetBinding>(
        FragmentReportToDevelopersBottomSheetBinding::inflate
    ) {

    override fun initListener() {

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSubmit.setOnClickListener {

            if (!ApplicationUtils.isInternetConnected()) {
                AppLogs.sendAppLogs(LogEvents.Binding, BindingEvents.NetworkIssue)
                context.showShortToast(getString(R.string.text_no_internet_connection))
                return@setOnClickListener
            }

            dismiss()
            setFragmentResult(
                REPORT_TO_DEVS_KEY,
                Bundle().apply {
                    putString("title", binding.titleInputLayout.text.toString())
                    putString("desc", binding.descInputLayout.text.toString())
                }
            )
        }
    }

    override fun subscribeObservers() {

        binding.titleInputLayout.addTextChangedListener {
            val content = it?.toString()?.trim()
            binding.btnSubmit.isEnabled = content.isNullOrEmpty().not()
        }
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