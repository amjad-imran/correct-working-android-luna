package com.oreo.ui.femalehealth.cycletracker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCycleTrackerSettingsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.ui.femalehealth.onboarding.GoalType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CycleTrackerSettingsFragment :
    BaseFragment<FragmentCycleTrackerSettingsBinding>(FragmentCycleTrackerSettingsBinding::inflate) {
    private val viewModel: CycleTrackerSettingViewModel by viewModels()
    private val args: CycleTrackerSettingsFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.cycleTrackerInfo = args.data

        updateUI()
    }

    private fun updateUI() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_cycle_tracker)
        binding.lytCycleLength.tvTitle.text = getString(R.string.text_cycle_length)
        binding.lytPeriodDuration.tvTitle.text = getString(R.string.text_period_duration)
        if (viewModel.cycleTrackerInfo != null) {
            goalSelectionUpdate(viewModel.cycleTrackerInfo?.goal)
        }

        if (viewModel.cycleTrackerInfo != null) {
            val cLength: String = if ((viewModel.cycleTrackerInfo?.cycleLength ?: 0) < 9) {
                "0${viewModel.cycleTrackerInfo?.cycleLength}"
            } else {
                viewModel.cycleTrackerInfo?.cycleLength.toString()
            }
            viewModel.lastSelectedCycleLength = cLength
            binding.lytCycleLength.tvDays.text = "$cLength days"
        } else {
            binding.lytCycleLength.tvDays.text = ""
        }
        if (viewModel.cycleTrackerInfo != null) {
            val cLength: String = if ((viewModel.cycleTrackerInfo?.periodLength ?: 0) < 9) {
                "0${viewModel.cycleTrackerInfo?.periodLength}"
            } else {
                viewModel.cycleTrackerInfo?.periodLength.toString()
            }
            viewModel.lastSelectedPeriodLength = cLength
            binding.lytPeriodDuration.tvDays.text = "$cLength days"
        } else {
            binding.lytPeriodDuration.tvDays.text = ""
        }

    }

    private fun goalSelectionUpdate(goal: String?) {
        if (goal != null) {
            when {
                goal.lowercase() == GoalType.TRACK_CYCLE.name.lowercase() -> {
                    binding.tvTrackMyPeriod.setBackgroundResource(R.drawable.back_modal_new_fmh_selected)
                    binding.tvGetPregnant.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
                    binding.tvAvoidPregnancy.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
                }

                goal.lowercase() == GoalType.TRACK_PREGNANCY.name.lowercase() -> {
                    binding.tvGetPregnant.setBackgroundResource(R.drawable.back_modal_new_fmh_selected)
                    binding.tvTrackMyPeriod.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
                    binding.tvAvoidPregnancy.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
                }

                goal.lowercase() == GoalType.TRY_CONCEIVE.name.lowercase() -> {
                    binding.tvAvoidPregnancy.setBackgroundResource(R.drawable.back_modal_new_fmh_selected)
                    binding.tvGetPregnant.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
                    binding.tvTrackMyPeriod.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
                }

                else -> {
                    binding.tvAvoidPregnancy.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
                    binding.tvGetPregnant.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
                    binding.tvTrackMyPeriod.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)

                }
            }
        } else {
            binding.tvAvoidPregnancy.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
            binding.tvGetPregnant.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
            binding.tvTrackMyPeriod.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)

        }

    }

    override fun initListener() {
        binding.tvTrackMyPeriod.setOnClickListener {
            goalSelectionUpdate(GoalType.TRACK_CYCLE.name)
            viewModel.lastSelectedGoal = GoalType.TRACK_CYCLE.name
            viewModel.isGoalUpdated = true
            viewModel.updateCycleTrackerInfo()

        }
        binding.tvGetPregnant.setOnClickListener {
            goalSelectionUpdate(GoalType.TRACK_PREGNANCY.name)
            viewModel.lastSelectedGoal = GoalType.TRACK_PREGNANCY.name
            viewModel.isGoalUpdated = true
            viewModel.updateCycleTrackerInfo()
        }
        binding.tvAvoidPregnancy.setOnClickListener {
            goalSelectionUpdate(GoalType.TRY_CONCEIVE.name)
            viewModel.lastSelectedGoal = GoalType.TRY_CONCEIVE.name
            viewModel.isGoalUpdated = true
            viewModel.updateCycleTrackerInfo()
        }
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.lytPeriodDuration.root.setOnClickListener {
            setFragmentResultListener(DURATION_LOG_SAVE) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                val isAllow = bundle.getBoolean("agree")
                viewModel.lastSelectedPeriodLength = selectedValue
                selectedValue?.let { value ->
                    binding.lytPeriodDuration.tvDays.text = "$value days"
                }
                if (isAllow) {
                    viewModel.isPeriodLengthUpdated = true
                    viewModel.updateCycleTrackerInfo()
                } else {
                    viewModel.isPeriodLengthUpdated = false
                }
            }
            navigate(
                R.id.cycleTrackerDurationLog,
                Bundle().apply {
                    this.putString("selectedValue", viewModel.lastSelectedPeriodLength ?: "")
                    this.putString("title", "Period duration")
                })

        }
        binding.lytCycleLength.root.setOnClickListener {
            setFragmentResultListener(DURATION_LOG_SAVE) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                val isAllow = bundle.getBoolean("agree")
                viewModel.lastSelectedCycleLength = selectedValue
                selectedValue?.let { value ->
                    binding.lytCycleLength.tvDays.text = "$value days"
                }
                if (isAllow) {
                    viewModel.isCycleLengthUpdated = true
                    viewModel.updateCycleTrackerInfo()
                } else
                    viewModel.isCycleLengthUpdated = false
            }
            navigate(
                R.id.cycleTrackerDurationLog,
                Bundle().apply {
                    this.putString("selectedValue", viewModel.lastSelectedCycleLength ?: "")
                    this.putString("title", "Cycle length")
                })
        }

    }

    override fun subscribeObservers() {
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }


}