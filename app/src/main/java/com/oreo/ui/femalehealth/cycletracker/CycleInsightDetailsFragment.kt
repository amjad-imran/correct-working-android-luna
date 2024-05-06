package com.oreo.ui.femalehealth.cycletracker

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.databinding.FragmentCycleInsightDetailsBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CycleInsightDetailsFragment :
    BaseFragment<FragmentCycleInsightDetailsBinding>(FragmentCycleInsightDetailsBinding::inflate) {
    private val args: CycleInsightDetailsFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun initListener() {
        binding.lytToolbar.tvTitle.text = args.pageTitle
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
    }

}