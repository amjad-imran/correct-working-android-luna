package com.noisefit.ui.friends.request

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.databinding.BottomSheetCompetitionRuleAcceptBinding
import com.noisefit_commans.ui.BaseBottomSheet
import dagger.hilt.android.AndroidEntryPoint

const val COMPETITION_ACCEPT_KEY = "COMPETITION_ACCEPT_KEY"

@AndroidEntryPoint
class CompetitionRuleAcceptBottomSheet :
    BaseBottomSheet<BottomSheetCompetitionRuleAcceptBinding>(BottomSheetCompetitionRuleAcceptBinding::inflate) {


    private val args: CompetitionRuleAcceptBottomSheetArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvNameLabel.text = args.request.title
        binding.tvMessage.text = args.request.description

        initListener()

    }

    private fun initListener() {
        binding.btnCancel.setOnClickListener {
            requireActivity().supportFragmentManager.setFragmentResult(
                COMPETITION_ACCEPT_KEY,
                bundleOf("accept" to false)
            )
            navigateUpSafe()
        }
        binding.btnSave.setOnClickListener {

            requireActivity().supportFragmentManager.setFragmentResult(
                COMPETITION_ACCEPT_KEY,
                bundleOf("accept" to true)
            )

            navigateUpSafe()
        }
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
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog

    }
}