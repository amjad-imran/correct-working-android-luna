package com.oreo.ui.sleep.nap

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetNoDataONapScoreBinding
import com.noisefit.luna.databinding.BottomSheetONapScoreBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.loadImage
import com.oreo.data.model.ScoreImpact
import com.oreo.data.model.SlideUpNapScoreDataModel

class OBottomSheetNoDataNapScore :
    BaseBottomSheetWithTransparent<BottomSheetNoDataONapScoreBinding>(
        BottomSheetNoDataONapScoreBinding::inflate
    ) {
    private val args: OBottomSheetNapScoreArgs by navArgs()
    private var mNapScoreDataModel: SlideUpNapScoreDataModel? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            mNapScoreDataModel = args.napScoreData
        }
        updateUI()
    }

    private fun updateUI() {
        binding.tvTitle.text = mNapScoreDataModel?.title
        binding.tvDescription.text = mNapScoreDataModel?.description

        if (mNapScoreDataModel?.scoreImpact.equals(ScoreImpact.POSITIVE.name, true)) {
            binding.ivScoreStatus.setImageResource(R.drawable.ic_bs_nap_score_up)
        } else {
            binding.ivScoreStatus.setImageResource(R.drawable.ic_bs_nap_score_down)
        }

    }

    override fun initListener() {
        binding.btnKnowMore.setOnClickListener {
            navigateUpSafe()
            mNapScoreDataModel?.napId?.let {
                requireActivity().supportFragmentManager.setFragmentResult(
                    BOTTOM_NAP_RESULT,
                    bundleOf("napId" to it)
                )
            }
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
                isDraggable = false
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }
}