package com.oreo.ui.femalehealth.onboarding

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentFMHOnboardingAllDoneBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FMHOnboardingAllDoneFragment :
    BaseFragment<FragmentFMHOnboardingAllDoneBinding>(FragmentFMHOnboardingAllDoneBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bNext.text = getString(R.string.text_done)

    }

    override fun initListener() {
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.bNext.setOnClickListener {
            navigate(FMHOnboardingAllDoneFragmentDirections.actionFmhOnboardingAllDoneFragmentToFragmentCycleTracker())
        }

    }

    override fun subscribeObservers() {


    }


}