package com.oreo.ui.femalehealth.onboarding

import androidx.fragment.app.activityViewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentFMHOnboardGoalBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.Event
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FMHOnboardGoalFragment :
    BaseFragment<FragmentFMHOnboardGoalBinding>(FragmentFMHOnboardGoalBinding::inflate) {
    private val mViewModel: FMHOnboardingViewModel by activityViewModels()
    override fun initListener() {
        initUiData()
        binding.lytTrackCycle.root.setOnClickListener {
            updateBackground(1)
            mViewModel.isGoalSelected.postValue(Event(true))
            mViewModel.goalTypeSelected = GoalType.TRACK_CYCLE.name

        }
        binding.lytConceive.root.setOnClickListener {
            updateBackground(2)
            mViewModel.isGoalSelected.postValue(Event(true))
            mViewModel.goalTypeSelected = GoalType.TRACK_CONCEIVE.name

        }
        binding.lytPregnancy.root.setOnClickListener {
            updateBackground(3)
            mViewModel.isGoalSelected.postValue(Event(true))
            mViewModel.goalTypeSelected = GoalType.TRACK_PREGNANCY.name
        }


    }

    private fun updateBackground(type: Int) {
        when (type) {
            1 -> {
                binding.lytTrackCycle.root.setBackgroundResource(R.drawable.back_modal_new_fmh_selected)
                binding.lytConceive.root.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
                binding.lytPregnancy.root.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
            }

            2 -> {
                binding.lytConceive.root.setBackgroundResource(R.drawable.back_modal_new_fmh_selected)
                binding.lytTrackCycle.root.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
                binding.lytPregnancy.root.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
            }

            else -> {
                binding.lytPregnancy.root.setBackgroundResource(R.drawable.back_modal_new_fmh_selected)
                binding.lytTrackCycle.root.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
                binding.lytConceive.root.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)

            }
        }
    }

    override fun subscribeObservers() {


    }

    private fun initUiData() {
        binding.lytTrackCycle.tvTitle.text = getString(R.string.text_tracking_my_cycle)
        binding.lytTrackCycle.tvDescription.text = getString(R.string.text_tracking_privacy_desc)
        binding.lytConceive.tvTitle.text = getString(R.string.text_trying_to_conceive)
        binding.lytConceive.tvDescription.text = getString(R.string.text_tracking_privacy_desc)
        binding.lytPregnancy.tvTitle.text = getString(R.string.text_tracking_my_pregnancy)
        binding.lytPregnancy.tvDescription.text = getString(R.string.text_tracking_privacy_desc)

    }

}