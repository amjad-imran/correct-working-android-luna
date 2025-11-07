package com.oreo.ui.lifeos

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.databinding.FragmentLifeOsInsightDetailsBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LifeOsInsightDetailsFragment :
    BaseFragment<FragmentLifeOsInsightDetailsBinding>(FragmentLifeOsInsightDetailsBinding::inflate) {

    val args: LifeOsInsightDetailsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}
