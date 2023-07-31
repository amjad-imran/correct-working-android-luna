package com.noisefit.ui.settings.feedbacknew

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.databinding.BottomSheetRateNowBinding
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val RATE_NOW = "RATE_NOW"
const val LATER = "LATER"

@AndroidEntryPoint
class RateNowBottomSheet :
    BaseBottomSheet<BottomSheetRateNowBinding>(BottomSheetRateNowBinding::inflate) {

    @Inject
    lateinit var localDataStore: DataStoredInterface
    private var cameFrom: String = ""


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameFrom = arguments?.let {
            RateNowBottomSheetArgs.fromBundle(it).cameFrom
        }.toString()
        val msg: String = if (localDataStore.getUser()?.firstName.isNullOrEmpty())
            "Hi Noisemaker, Your feedback is very important for us. please take a moment to rate us."
        else
            "Hi ${localDataStore.getUser()?.firstName}, Your feedback is very important for us. please take a moment to rate us."
        binding.tvMessage.text = msg
        initListener()

    }

    private fun initListener() {
        binding.btnCancel.setOnClickListener {
            if (cameFrom == "feedback") {
                setFragmentResult(
                    LATER,
                    bundleOf("isSelected" to true)
                )
            } else {
                requireActivity().supportFragmentManager.setFragmentResult(
                    LATER,
                    bundleOf("isSelected" to true)
                )
            }
            navigateUpSafe()
        }
        binding.btnSave.setOnClickListener {
            if (cameFrom == "feedback") {
                setFragmentResult(
                    RATE_NOW,
                    bundleOf("isSelected" to true)
                )
            } else {
                requireActivity().supportFragmentManager.setFragmentResult(
                    RATE_NOW,
                    bundleOf("isSelected" to true)
                )
            }
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