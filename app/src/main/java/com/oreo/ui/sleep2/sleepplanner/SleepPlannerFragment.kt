package com.oreo.ui.sleep2.sleepplanner

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSleepPlannerBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SleepPlannerFragment :
    BaseFragment<FragmentSleepPlannerBinding>(FragmentSleepPlannerBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    private fun setupUi() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_sleep_planner)
        binding.lytToolbar.view1.visible()
        binding.lytToolbar.view1.setBackgroundResource(R.drawable.ic_info_oreo)

        binding.lytSetupGoal.tvTitle.text = getString(R.string.text_setup_your_goal)
        binding.lytSetupAlarm.tvTitle.text = getString(R.string.text_setup_alarm)

        binding.lytLegend1.tvTitle.text = getString(R.string.text_extra_sleep_need)
        binding.lytLegend1.ivColorBox.setBackgroundColor(android.graphics.Color.parseColor("#a477ff"))
        binding.lytLegend2.tvTitle.text = getString(R.string.text_avg_sleep_duration)
        binding.lytLegend2.ivColorBox.setBackgroundColor(android.graphics.Color.parseColor("#c5a8ed"))
    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.lytToolbar.view1.setOnClickListener {
            //
        }
        binding.lytSetupGoal.ivMore.setOnClickListener {
            //
        }
        binding.lytSetupAlarm.ivMore.setOnClickListener {
            navigate(R.id.setAlarmFragment)
        }


    }

    override fun subscribeObservers() {

    }

}