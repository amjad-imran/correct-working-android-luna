package com.oreo.ui.heartrate

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.databinding.FragmentLearnMoreDetailsBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LearnMoreDetailsFragment :
    BaseFragment<FragmentLearnMoreDetailsBinding>(FragmentLearnMoreDetailsBinding::inflate) {
    private val args: LearnMoreDetailsFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initListener() {
        binding.lytToolbar.tvTitle.text = args.data.label
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

}