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
import com.noisefit.luna.databinding.FragmentAddHabitsDiscardChangesBSBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent

class AddHabitsDiscardChangesBS :
    BaseBottomSheetWithTransparent<FragmentAddHabitsDiscardChangesBSBinding>(
        FragmentAddHabitsDiscardChangesBSBinding::inflate
    ) {

    companion object{
        const val DISCARD_CHANGES_ADD_HABITS_KEY = "DISCARD_CHANGES_ADD_HABITS_KEY"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUi()
    }

    private fun setUi() {

    }

    override fun initListener() {
        binding.btnDiscard.setOnClickListener {
            setFragmentResult(
                DISCARD_CHANGES_ADD_HABITS_KEY,
                bundleOf(
                    "isDiscardClicked" to true,
                )
            )
            navigateUpSafe()
        }

        binding.btnContinue.setOnClickListener {
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
                isCancelable = true
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }

}