package com.oreo.ui.stress

import android.os.Bundle
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOStressDataMovementBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible


class OStressDataMovementFragment :
    BaseFragment<FragmentOStressDataMovementBinding>(FragmentOStressDataMovementBinding::inflate) {
    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val movementViewModel: OStressDataMovementViewModel by viewModels()
    private val ARGS_DATE = "ARGS_DATE"

    companion object {

        @JvmStatic
        fun newInstance(date: String) = OStressDataMovementFragment().apply {
            arguments = Bundle().apply {
                putString(ARGS_DATE, date)
            }
        }
    }

    override fun initListener() {
        binding.ivOpen.setOnClickListener {
            handleMovementViews(true)
        }
        binding.ivClose.setOnClickListener {
            handleMovementViews(false)
        }
        handleMovementViews()
        handleBannerView(60)
        handleStressProgressView()
    }

    private fun handleStressProgressView() {
        binding.lytStressHeader.lytStressProgress.viewCalm.layoutParams =
            binding.lytStressHeader.lytStressProgress.viewCalm.layoutParams.apply {
                (this as LinearLayout.LayoutParams).weight =
                    calculateWeightPercent(60)
            }

        binding.lytStressHeader.lytStressProgress.viewFocused.layoutParams =
            binding.lytStressHeader.lytStressProgress.viewFocused.layoutParams.apply {
                (this as LinearLayout.LayoutParams).weight =
                    calculateWeightPercent(20)
            }
        binding.lytStressHeader.lytStressProgress.viewStressed.layoutParams =
            binding.lytStressHeader.lytStressProgress.viewStressed.layoutParams.apply {
                (this as LinearLayout.LayoutParams).weight =
                    calculateWeightPercent(20)
            }

    }

    private fun calculateWeightPercent(progress: Int): Float {
        return (progress.toFloat() / 100).times(100)
    }

    private fun handleBannerView(stressValue: Int) {
        when (stressValue) {
            in 1..35 -> {
                binding.lytStressBanner.rootView.setBackgroundResource(R.drawable.ic_st_calm_cue_bg)
            }

            in 31..70 -> {
                binding.lytStressBanner.rootView.setBackgroundResource(R.drawable.ic_st_focus_cue_bg)
            }

            else -> {
                binding.lytStressBanner.rootView.setBackgroundResource(R.drawable.ic_st_stress_cue_bg)
            }
        }

    }

    override fun subscribeObservers() {

    }

    private fun handleMovementViews(isOpen: Boolean = false) {
        if (isOpen) {
            binding.viewOpen.gone()
            binding.ivOpen.gone()
            binding.tvCompareHeader.visible()
            binding.lytHighMovement.root.visible()
            binding.lytMediumMovement.root.visible()
            binding.lytLowMovement.root.visible()
            binding.lytNoMovement.root.visible()
            binding.viewClose.visible()
            binding.ivClose.visible()
        } else {
            binding.viewOpen.visible()
            binding.ivOpen.visible()
            binding.tvCompareHeader.gone()
            binding.lytHighMovement.root.gone()
            binding.lytMediumMovement.root.gone()
            binding.lytLowMovement.root.gone()
            binding.lytNoMovement.root.gone()
            binding.viewClose.gone()
            binding.ivClose.gone()
        }

        binding.lytHighMovement.tvHeader.text = getString(R.string.text_high_movement)
        binding.lytHighMovement.compareChart.setDrawData(
            movementViewModel.getCombinedMovementData(
                ArrayList(),
                false
            ) as ArrayList<Int>, 1, requireContext().getColor(R.color.white)
        )
        binding.lytMediumMovement.tvHeader.text = getString(R.string.text_medium_movement)
        binding.lytMediumMovement.compareChart.setDrawData(
            movementViewModel.getCombinedMovementData(
                ArrayList(),
                false
            ) as ArrayList<Int>, 1, requireContext().getColor(R.color.medium_movement_color)
        )
        binding.lytLowMovement.tvHeader.text = getString(R.string.text_low_movement)
        binding.lytLowMovement.compareChart.setDrawData(
            movementViewModel.getCombinedMovementData(
                ArrayList(),
                false
            ) as ArrayList<Int>, 1, requireContext().getColor(R.color.low_movement_color)
        )
        binding.lytNoMovement.tvHeader.text = getString(R.string.text_no_movement)
        binding.lytNoMovement.compareChart.setDrawData(
            movementViewModel.getCombinedMovementData(
                ArrayList(),
                false
            ) as ArrayList<Int>, 1, requireContext().getColor(R.color.no_movement_color)
        )
    }

}