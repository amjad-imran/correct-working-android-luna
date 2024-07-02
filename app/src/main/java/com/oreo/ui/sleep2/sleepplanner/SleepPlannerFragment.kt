package com.oreo.ui.sleep2.sleepplanner

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSleepPlannerBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SleepPlannerFragment :
    BaseFragment<FragmentSleepPlannerBinding>(FragmentSleepPlannerBinding::inflate) {
    private val viewModel: SleepPlannerViewModel by viewModels()
    private val bedTimeAdapter: SADaysAdapter by lazy {
        SADaysAdapter()
    }
    private val wakeupAdapter: SADaysAdapter by lazy {
        SADaysAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setRecycler()
    }

    private fun setRecycler() {
        with(binding.lytWakeupTime.rvDays) {
            adapter = wakeupAdapter
        }
        wakeupAdapter.setData(viewModel.getAlarmDays())

        with(binding.lytBedTime.rvDays) {
            adapter = bedTimeAdapter
        }
        bedTimeAdapter.setData(viewModel.getAlarmDays())
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

        //type-0-set alarm, 1-multiple alarm
        updateSetAlarmView(0)
    }

    private fun updateSetAlarmView(type: Int) {
        if (type == 1) {
            binding.lytSetupAlarm.root.gone()
            binding.lytBedTime.root.visible()
            binding.lytWakeupTime.root.visible()
        } else {
            binding.lytSetupAlarm.root.visible()
            binding.lytBedTime.root.gone()
            binding.lytWakeupTime.root.gone()
        }
        binding.lytBedTime.lytAlarmTime.apply {
            lytBedTime.ivIcon.setImageResource(R.drawable.ic_bedtime_gray)

            lytBedTime.tvTitle.text = getString(R.string.text_bedtime)
            lytBedTime.tvTitle.setTextColor(
                ContextCompat.getColor(
                    binding.view1.context,
                    R.color.white_55
                )
            )
            lytBedTime.tvTime.text = "11:00"
            lytBedTime.tvTimeUnit.text = "pm"

            lytWakeupTime.ivIcon.setImageResource(R.drawable.ic_wakeup_gray)

            lytWakeupTime.tvTitle.text = getString(R.string.text_wakeup)
            lytWakeupTime.tvTitle.setTextColor(
                ContextCompat.getColor(
                    binding.view1.context,
                    R.color.white_55
                )
            )
            lytWakeupTime.tvTime.setTextColor(
                ContextCompat.getColor(
                    binding.view1.context,
                    R.color.white_60
                )
            )
            lytWakeupTime.tvTime.text = "7:30"
            lytWakeupTime.tvTimeUnit.text = "am"
        }
        binding.lytWakeupTime.lytAlarmTime.apply {
            lytBedTime.ivIcon.setImageResource(R.drawable.ic_bedtime_gray)


            lytBedTime.tvTitle.text =
                getString(R.string.text_bedtime)
            lytBedTime.tvTitle.setTextColor(
                ContextCompat.getColor(
                    binding.view1.context,
                    R.color.white_55
                )
            )
            lytBedTime.tvTime.text = "11:00"
            lytBedTime.tvTimeUnit.text = "pm"

            lytWakeupTime.ivIcon.setImageResource(R.drawable.ic_wakeup_gray)

            lytWakeupTime.tvTitle.text =
                getString(R.string.text_wakeup)
            lytWakeupTime.tvTitle.setTextColor(
                ContextCompat.getColor(
                    binding.view1.context,
                    R.color.white_55
                )
            )
            lytWakeupTime.tvTime.setTextColor(
                ContextCompat.getColor(
                    binding.view1.context,
                    R.color.white_60
                )
            )
            lytWakeupTime.tvTime.text = "7:30"
            lytWakeupTime.tvTimeUnit.text = "am"
        }


    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.lytToolbar.view1.setOnClickListener {
            //
        }
        binding.lytSetupGoal.ivMore.setOnClickListener {
            navigate(R.id.dialogSaGoal)
        }
        binding.lytSetupAlarm.ivMore.setOnClickListener {
            navigate(R.id.setAlarmFragment)
        }
        binding.lytBreathExercise.root.setOnClickListener {
            navigate(R.id.fragmentBreathExercise)
        }


    }

    override fun subscribeObservers() {

    }

}