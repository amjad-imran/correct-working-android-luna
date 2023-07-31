package com.noisefit.ui.reward.walkthrough

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.R
import com.noisefit.databinding.BottomSheetStreakInfoBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent


class BottomSheetStreakInfo :
    BaseBottomSheetWithTransparent<BottomSheetStreakInfoBinding>(BottomSheetStreakInfoBinding::inflate) {

    private val args: BottomSheetStreakInfoArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,com.noisefit_commans.R.style.DialogStyle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvCurrentMultiplier.text = args.currentMultipler
        binding.tvNextMultiplier.text = args.nextMultipler

    }

    override fun initListener() {

        binding.btnNext.setOnClickListener {
            navigateUpSafe()
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
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }
}