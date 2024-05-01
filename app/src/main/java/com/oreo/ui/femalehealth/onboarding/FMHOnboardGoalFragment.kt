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
            mViewModel.isGoalSelected.postValue(Event(true))
        }
        binding.lytConceive.root.setOnClickListener {
            mViewModel.isGoalSelected.postValue(Event(true))
        }
        binding.lytPregnancy.root.setOnClickListener {
            mViewModel.isGoalSelected.postValue(Event(true))
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