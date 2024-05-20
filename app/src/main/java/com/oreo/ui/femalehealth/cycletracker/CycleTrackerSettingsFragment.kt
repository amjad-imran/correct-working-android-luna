package com.oreo.ui.femalehealth.cycletracker

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCycleTrackerSettingsBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CycleTrackerSettingsFragment :
    BaseFragment<FragmentCycleTrackerSettingsBinding>(FragmentCycleTrackerSettingsBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUI()
    }

    private fun updateUI() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_cycle_tracker)
        binding.lytCycleLength.tvTitle.text = getString(R.string.text_cycle_length)
        binding.lytPeriodDuration.tvTitle.text = getString(R.string.text_period_duration)

    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }


}