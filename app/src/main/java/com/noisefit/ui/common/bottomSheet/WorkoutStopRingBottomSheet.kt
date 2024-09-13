package com.noisefit.ui.common.bottomSheet

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetAlertTextBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import dagger.hilt.android.AndroidEntryPoint

const val WORKOUT_STOP_KEY = "WORKOUT_STOP_KEY"

@AndroidEntryPoint
class WorkoutStopRingBottomSheet :
    BaseBottomSheetWithTransparent<BottomSheetAlertTextBinding>(
        BottomSheetAlertTextBinding::inflate
    ) {

    private val args: WorkoutStopRingBottomSheetArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = getString(R.string.text_end_workout)
        binding.tvDesc.text = args.message

        getString(R.string.text_ring_lo_battert_stop_message)

        binding.btnAllow.text = getString(R.string.text_save)
        binding.btnCancel.text = getString(R.string.text_delete)
        if (args.showSave.not()) {
            binding.btnAllow.gone()
        }
    }


    override fun initListener() {
        binding.btnAllow.setOnClickListener {
            setFragmentResult(
                WORKOUT_STOP_KEY,
                bundleOf("allow" to true)
            )
            navigateUpSafe()
        }
        binding.btnCancel.setOnClickListener {
            setFragmentResult(
                WORKOUT_STOP_KEY,
                bundleOf("delete" to true)
            )
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
                isCancelable = false
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }

}
