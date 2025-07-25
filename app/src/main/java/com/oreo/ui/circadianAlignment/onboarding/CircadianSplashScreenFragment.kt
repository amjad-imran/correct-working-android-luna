package com.oreo.ui.circadianAlignment.onboarding

import android.os.Bundle
import android.view.View
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.noisefit.luna.databinding.FragmentCircadianSplashScreenBinding
import com.noisefit_commans.ui.BaseFragment

class CircadianSplashScreenFragment :
    BaseFragment<FragmentCircadianSplashScreenBinding>(FragmentCircadianSplashScreenBinding::inflate) {

    private val fragments = listOf(
        OnboardingCarcadianFragment1(),
        OnboardingCarcadianFragment2(),
        OnboardingCarcadianFragment3(),
        OnboardingCarcadianFragment4(),
        OnboardingCarcadianFragment5(),
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}