package com.oreo.ui.femalehealth.cycletracker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
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
        binding.lytPeriodDuration.root.setOnClickListener {
            setFragmentResultListener(DURATION_LOG_SAVE) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                val isAllow = bundle.getBoolean("agree")
                selectedValue?.let { value ->
                    binding.lytPeriodDuration.tvDays.text = "$value days"
                }
                if (isAllow) {
                    //call api for update
                }
            }
            navigate(
                R.id.cycleTrackerDurationLog,
                Bundle().apply {
                    this.putString("selectedValue", "08")
                    this.putString("title", "Period duration")
                })

        }
        binding.lytCycleLength.root.setOnClickListener {
            setFragmentResultListener(DURATION_LOG_SAVE) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                val isAllow = bundle.getBoolean("agree")
                selectedValue?.let { value ->
                    binding.lytCycleLength.tvDays.text = "$value days"
                }
                if (isAllow) {
                    //call api for update
                }
            }
            navigate(
                R.id.cycleTrackerDurationLog,
                Bundle().apply {
                    this.putString("selectedValue", "10")
                    this.putString("title", "Cycle length")
                })

        }

    }

    override fun subscribeObservers() {

    }


}