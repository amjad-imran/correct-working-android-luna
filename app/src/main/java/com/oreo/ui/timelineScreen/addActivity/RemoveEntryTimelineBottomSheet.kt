package com.oreo.ui.timelineScreen.addActivity

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentModifyingEntriesBottomSheetBinding
import com.noisefit.luna.databinding.FragmentModifyingEntriesBottomSheetBinding.inflate
import com.noisefit.luna.databinding.FragmentRemoveEntryTimelineBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.oreo.ui.timelineScreen.habits.ModifyingEntriesBottomSheetArgs
import kotlin.getValue

class RemoveEntryTimelineBottomSheet :
    BaseBottomSheetWithTransparent<FragmentRemoveEntryTimelineBottomSheetBinding>(
        FragmentRemoveEntryTimelineBottomSheetBinding::inflate
    ) {

    companion object{
        const val REMOVE_ENTRY_BOTTOM_SHEET_KEY = "REMOVE_ENTRY_BOTTOM_SHEET_KEY"
    }

    private val args: RemoveEntryTimelineBottomSheetArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.tvTitle.text = args.title
        binding.tvDesc.text = args.description
    }


    override fun initListener() {
        binding.btnDelete.setOnClickListener {
            setFragmentResult(
                REMOVE_ENTRY_BOTTOM_SHEET_KEY,
                bundleOf(
                    "deleteClicked" to true,
                )
            )
            navigateUpSafe()
        }

        binding.btnCancel.setOnClickListener {
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