package com.oreo.ui.timelineScreen.habits

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.databinding.FragmentModifyingEntriesBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent

class ModifyingEntriesBottomSheet :
    BaseBottomSheetWithTransparent<FragmentModifyingEntriesBottomSheetBinding>(
        FragmentModifyingEntriesBottomSheetBinding::inflate
    ) {

    private val args: ModifyingEntriesBottomSheetArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.tvTitle.text = args.title
        binding.tvDesc.text = args.description
    }


    override fun initListener() {
        binding.btnGotIt.setOnClickListener {
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
                isCancelable = true
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }

}