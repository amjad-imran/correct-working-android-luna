package com.oreo.ui.circadianAlignment.logBottomSheets

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.databinding.FragmentAddLogCircadianBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.invisible
import com.oreo.ui.timelineScreen.addActivity.ActivitiesListAdapter

class AddLogCircadianBottomSheetFragment :
    BaseBottomSheetWithTransparent<FragmentAddLogCircadianBottomSheetBinding>(FragmentAddLogCircadianBottomSheetBinding::inflate) {

    private var isDropdownVisible = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSave.isEnabled = false
        setDropdown()
    }

    override fun initListener() {
        binding.lytSelected.setOnClickListener {
            isDropdownVisible = !isDropdownVisible
            binding.rvDropdown.visibility = if(isDropdownVisible) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun subscribeObservers() {

    }

    private fun setDropdown() {
        val activities = listOf(
            "Meal intake",
            "Light exposure",
            "Caffeine intake",
            "Exercise duration",
            "Water consumption",
            "Period started",
            "Nap detected",
        )
        /*binding.rvDropdown.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = ActivitiesListAdapter(activities) { selected ->
                binding.tvSelected.text = selected
                binding.rvDropdown.invisible()
                isDropdownVisible = false
                binding.btnSave.isEnabled = true
            }
        }*/
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