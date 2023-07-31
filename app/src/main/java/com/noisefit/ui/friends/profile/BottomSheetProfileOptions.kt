package com.noisefit.ui.friends.profile

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.R
import com.noisefit.databinding.BottomSheetProfileOptionsBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

const val EDIT_PROFILE = "EDIT_PROFILE"
const val COMPETE_NOW = "COMPETE_NOW"
const val REMOVE_FRIEND = "REMOVE_FRIEND"

@AndroidEntryPoint
class BottomSheetProfileOptions :
    BaseBottomSheetWithTransparent<BottomSheetProfileOptionsBinding>(
        BottomSheetProfileOptionsBinding::inflate
    ) {

    private var isMyProfile: Boolean = false
    private var canCompute: Boolean = false

    private val args: BottomSheetProfileOptionsArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, com.noisefit_commans.R.style.DialogStyle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isMyProfile = args.isMyProfile
        canCompute=args.canCompute
        showHideOptions()

    }

    private fun showHideOptions() {
        if (isMyProfile) {
            binding.tvEditMyProfile.visible()
            binding.tvGuidelines.visible()
            binding.tvComputeNow.gone()
            binding.tvRemoveFriend.gone()
        } else {
            binding.tvEditMyProfile.gone()
            binding.tvGuidelines.gone()
            if (canCompute)
            binding.tvComputeNow.visible()
            else
                binding.tvComputeNow.gone()
            binding.tvRemoveFriend.visible()
        }
    }

    override fun initListener() {

        binding.tvEditMyProfile.setOnClickListener {
            requireActivity().supportFragmentManager.setFragmentResult(
                EDIT_PROFILE,
                bundleOf("allow" to true)
            )
            navigateUpSafe()
        }
        binding.tvGuidelines.setOnClickListener {
            requireActivity().supportFragmentManager.setFragmentResult(
                EDIT_PROFILE,
                bundleOf("guidelines" to true)
            )
            navigateUpSafe()
        }
        binding.tvComputeNow.setOnClickListener {
            requireActivity().supportFragmentManager.setFragmentResult(
                COMPETE_NOW,
                bundleOf("allow" to true)
            )
            navigateUpSafe()
        }
        binding.tvRemoveFriend.setOnClickListener {
            requireActivity().supportFragmentManager.setFragmentResult(
                REMOVE_FRIEND,
                bundleOf("allow" to true)
            )
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