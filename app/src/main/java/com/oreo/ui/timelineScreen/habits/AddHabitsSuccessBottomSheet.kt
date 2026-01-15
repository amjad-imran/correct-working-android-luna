package com.oreo.ui.timelineScreen.habits

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddHabitsSuccessBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent

const val ADD_HABITS_SUCCESS_BS_KEY = "ADD_HABITS_SUCCESS_BS_KEY"

class AddHabitsSuccessBottomSheet :
    BaseBottomSheetWithTransparent<FragmentAddHabitsSuccessBottomSheetBinding>(
        FragmentAddHabitsSuccessBottomSheetBinding::inflate
    ) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUi()
    }

    private fun setUi() {
        binding.tvTitle.text = arguments?.getString("successTitle")
        binding.btnOkay.text = arguments?.getString("buttonText")
    }

    override fun initListener() {
        binding.btnOkay.setOnClickListener {
            setFragmentResult(
                ADD_HABITS_SUCCESS_BS_KEY,
                bundleOf(
                    "okayClicked" to true,
                )
            )
            navigateUpSafe()
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
                isCancelable = false
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }

}