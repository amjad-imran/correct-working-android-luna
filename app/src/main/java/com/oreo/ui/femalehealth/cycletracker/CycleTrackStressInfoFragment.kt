package com.oreo.ui.femalehealth.cycletracker

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCycleTrackStressInfoBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CycleTrackStressInfoFragment :
    BaseFragment<FragmentCycleTrackStressInfoBinding>(FragmentCycleTrackStressInfoBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUI()
    }

    private fun updateUI() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_cycle_tracker)

        binding.lytStartPoint.tvTitle.text = getString(R.string.text_stress_info_start_point)
        binding.lytStartPoint.tvDesc.text = getString(R.string.text_stress_info_start_point_desc)
        binding.lytFertileWindow.tvTitle.text = getString(R.string.text_stress_info_fertile_window)
        binding.lytFertileWindow.tvDesc.text =
            getString(R.string.text_stress_info_fertile_window_desc)
        binding.lytPeakOvulation.tvTitle.text = getString(R.string.text_stress_info_peak_ovulation)
        binding.lytPeakOvulation.tvDesc.text =
            getString(R.string.text_stress_info_peak_ovulation_desc)
        binding.lytCycleContinues.tvTitle.text = getString(R.string.text_stress_info_cycle_continue)
        binding.lytCycleContinues.tvDesc.text =
            getString(R.string.text_stress_info_cycle_continue_desc)
    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }


}