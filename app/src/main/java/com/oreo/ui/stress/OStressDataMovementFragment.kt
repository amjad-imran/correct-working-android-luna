package com.oreo.ui.stress

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
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
        binding.compareChart.setDrawData(
            movementViewModel.getCombinedMovementData(
                ArrayList(),
                false
            ) as ArrayList<Int>, 1, "#8cd7f6"
        )
    }

}