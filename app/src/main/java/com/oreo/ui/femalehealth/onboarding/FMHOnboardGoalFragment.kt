package com.oreo.ui.femalehealth.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentFMHOnboardGoalBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.Event
import com.oreo.ui.femalehealth.onboarding.GoalType.TRACK_CYCLE
import com.oreo.ui.femalehealth.onboarding.GoalType.TRACK_PREGNANCY
import com.oreo.ui.femalehealth.onboarding.GoalType.TRY_CONCEIVE
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FMHOnboardGoalFragment :
    BaseFragment<FragmentFMHOnboardGoalBinding>(FragmentFMHOnboardGoalBinding::inflate) {
    private val mViewModel: FMHOnboardingViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUiData()
    }

    override fun onResume() {
        super.onResume()
        binding.lytBottomControls.bNext.isEnabled = mViewModel.goalTypeSelected != null
    }

    override fun initListener() {
        binding.lytTrackCycle.root.setOnClickListener {
            updateBackground(TRACK_CYCLE)
            binding.lytBottomControls.bNext.isEnabled = true
            mViewModel.goalTypeSelected = TRACK_CYCLE
        }
        binding.lytConceive.root.setOnClickListener {
            updateBackground(TRY_CONCEIVE)
            binding.lytBottomControls.bNext.isEnabled = true
            mViewModel.goalTypeSelected = TRY_CONCEIVE
        }
        binding.lytPregnancy.root.setOnClickListener {
            updateBackground(TRACK_PREGNANCY)
            binding.lytBottomControls.bNext.isEnabled = true
            mViewModel.goalTypeSelected = TRACK_PREGNANCY
        }

        binding.lytBottomControls.bNext.setOnClickListener {
            mViewModel.onNextPress.postValue(Event(true))
        }
    }

    private fun updateBackground(type: GoalType) {
        when (type) {
            TRACK_CYCLE -> {
                binding.lytTrackCycle.root.setBackgroundResource(R.drawable.back_modal_new_fmh_selected)
                binding.lytConceive.root.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
                binding.lytPregnancy.root.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
            }

            TRY_CONCEIVE -> {
                binding.lytConceive.root.setBackgroundResource(R.drawable.back_modal_new_fmh_selected)
                binding.lytTrackCycle.root.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
                binding.lytPregnancy.root.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
            }

            TRACK_PREGNANCY -> {
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

        mViewModel.goalTypeSelected?.let {
            updateBackground(it)
        }
    }
}