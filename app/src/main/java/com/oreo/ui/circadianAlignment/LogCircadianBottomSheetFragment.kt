package com.oreo.ui.circadianAlignment

import android.app.Dialog
import android.os.Bundle
import android.view.View
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
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LogEvents


const val LOG_CIRCADIAN_BOTTOM_SHEET_KEY = "LOG_CIRCADIAN_BOTTOM_SHEET_KEY"

class LogCircadianBottomSheetFragment  :
    BaseBottomSheetWithTransparent<FragmentLogCircadianBottomSheetBinding>(
        FragmentLogCircadianBottomSheetBinding::inflate
    ) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            setUi(it.getString("key"), it.getString("textKey"))
        }
    }

    private fun setUi(key: String?, text: String?) {
        LOGS.d("iovhvnibv : key: $key, text: $text")
        when(key){
            CircadianAlignmentViewModel.light_exposure_key -> {
                binding.ivIconWithText.setImageResource(R.drawable.ic_sun_rise)
                binding.tvTextWithIcon.text = text?: "-"
            }

            CircadianAlignmentViewModel.meal_window_key -> {
                binding.ivOnlyIcon.setImageResource(R.drawable.ic_meal_corrective_activities)
            }

            CircadianAlignmentViewModel.caffeine_window_key -> {
                binding.ivOnlyIcon.setImageResource(R.drawable.ic_caffeine_corrective_activities)
            }

            else -> {}
        }
    }

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
                }
            )
        }

        binding.btnNo.setOnClickListener {
            dismiss()
        }

        binding.tvClose.setOnClickListener {
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