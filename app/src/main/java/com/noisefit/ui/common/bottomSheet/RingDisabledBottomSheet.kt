package com.noisefit.ui.common.bottomSheet

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetAlertTextBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit_commans.utils.share.ShareUtil.SUPPORT_URL
import dagger.hilt.android.AndroidEntryPoint

const val RING_DISABLED_KEY = "RING_DISABLED_KEY"

@AndroidEntryPoint
class RingDisabledBottomSheet :
    BaseBottomSheetWithTransparent<BottomSheetAlertTextBinding>(
        BottomSheetAlertTextBinding::inflate
    ) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = "Ring disabled"
        binding.tvDesc.text =
            "Your ring has been disabled. To reactivate it, please contact our customer support team."

        binding.btnAllow.text = "Contact us"
        binding.btnCancel.text = "Cancel"

    }

    override fun initListener() {
        binding.btnAllow.setOnClickListener {

            context?.let {
                ShareUtil.openExternalUrl(it, SUPPORT_URL)
            }
        }
        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
            requireActivity().supportFragmentManager.setFragmentResult(
                RING_DISABLED_KEY,
                bundleOf("cancel" to true)
            )
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


