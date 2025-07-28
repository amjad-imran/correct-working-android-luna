package com.oreo.ui.circadianAlignment.onboarding

import android.os.Bundle
import android.view.View
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
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

        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabIndicator, binding.viewPager) { _, _ -> }.attach()

    }

    override fun initListener() {
        binding.btnNext.setOnClickListener {
            val nextItem = binding.viewPager.currentItem + 1
            if (nextItem < fragments.size) {
                binding.viewPager.currentItem = nextItem
            } else {
                /*viewModel.setOnboardingCompleted()
                findNavController().navigate(R.id.action_onboarding_to_home)*/
            }
        }

        binding.tvSkip.setOnClickListener {
            /*viewModel.setOnboardingCompleted()
            navigate(R.id.action_onboarding_to_home)*/
        }

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

}