package com.oreo.ui.stress

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOStressDataMovementBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.ui.BaseFragment


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
        handleMovementViews()
        handleBannerView(60)
    }

    private fun handleBannerView(stressValue: Int) {
        when (stressValue) {
            in 1..30 -> {
                binding.lytStressBanner.rootView.setBackgroundResource(R.drawable.ic_st_calm_cue_bg)
            }
            in 31..60 -> {
                binding.lytStressBanner.rootView.setBackgroundResource(R.drawable.ic_st_focus_cue_bg)
            }
            else -> {
                binding.lytStressBanner.rootView.setBackgroundResource(R.drawable.ic_st_stress_cue_bg)
            }
        }

    }

    override fun subscribeObservers() {

    }

    private fun handleMovementViews() {
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