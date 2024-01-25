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
    }

    override fun subscribeObservers() {

    }

    private fun handleMovementViews() {
        binding.lytHighMovement.tvHeader.text = getString(R.string.text_high_movement)
        binding.lytHighMovement.compareChart.setDrawData(
            movementViewModel.getCombinedMovementData(
                ArrayList(),
                false
            ) as ArrayList<Int>, 1, "#ffffff"
        )
        binding.lytMediumMovement.tvHeader.text = getString(R.string.text_medium_movement)
        binding.lytMediumMovement.compareChart.setDrawData(
            movementViewModel.getCombinedMovementData(
                ArrayList(),
                false
            ) as ArrayList<Int>, 1, "#8cd7f6"
        )
        binding.lytLowMovement.tvHeader.text = getString(R.string.text_low_movement)
        binding.lytLowMovement.compareChart.setDrawData(
            movementViewModel.getCombinedMovementData(
                ArrayList(),
                false
            ) as ArrayList<Int>, 1, "#307384"
        )
        binding.lytNoMovement.tvHeader.text = getString(R.string.text_no_movement)
        binding.lytNoMovement.compareChart.setDrawData(
            movementViewModel.getCombinedMovementData(
                ArrayList(),
                false
            ) as ArrayList<Int>, 1, "#757575"
        )
    }

}