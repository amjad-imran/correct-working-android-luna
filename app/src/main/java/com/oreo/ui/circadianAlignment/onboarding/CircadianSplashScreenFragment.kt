package com.oreo.ui.circadianAlignment.onboarding

import android.os.Bundle
import android.view.View
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCircadianSplashScreenBinding
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CircadianSplashScreenFragment :
    BaseFragment<FragmentCircadianSplashScreenBinding>(FragmentCircadianSplashScreenBinding::inflate) {

    private val fragments = listOf(
        OnboardingCarcadianFragment1(),
        OnboardingCarcadianFragment2(),
        OnboardingCarcadianFragment3(),
        OnboardingCarcadianFragment4(),
        OnboardingCarcadianFragment5(),
    )

    @Inject
    lateinit var localDataStore: DataStoredInterface

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabIndicator, binding.viewPager) { _, _ -> }.attach()

        binding.viewPager.isUserInputEnabled = false

    }

    override fun initListener() {
        binding.btnNext.setOnClickListener {
            val nextItem = binding.viewPager.currentItem + 1
            if (nextItem < fragments.size) {
                binding.viewPager.setCurrentItem(nextItem, true)
            } else {
                localDataStore.setCircadianOnboardShown()
                navigate(R.id.quizCircadianFragment)
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