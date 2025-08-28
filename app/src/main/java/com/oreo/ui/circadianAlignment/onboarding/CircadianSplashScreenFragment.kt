package com.oreo.ui.circadianAlignment.onboarding

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCircadianSplashScreenBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CircadianSplashScreenFragment :
    BaseFragment<FragmentCircadianSplashScreenBinding>(FragmentCircadianSplashScreenBinding::inflate) {

    private val fragments = listOf(
        OnboardingCarcadianFragment0(),
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

        binding.viewPager.isUserInputEnabled = false

        val tvSkip = binding.tvSkip
        tvSkip.paintFlags = tvSkip.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        setUi(binding.viewPager.currentItem)
    }

    override fun initListener() {
        binding.btnNext.setOnClickListener {
            val nextItem = binding.viewPager.currentItem + 1
            if (nextItem < fragments.size) {
                binding.viewPager.setCurrentItem(nextItem, true)
            } else {
                navigate(CircadianSplashScreenFragmentDirections.actionCircadianSplashScreenFragmentToQuizCircadianFragment())
            }
            setUi(binding.viewPager.currentItem)
        }

        binding.tvSkip.setOnClickListener {
            navigate(CircadianSplashScreenFragmentDirections.actionCircadianSplashScreenFragmentToQuizCircadianFragment())
        }

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

    private fun setUi(pos: Int){
        when(pos){
            0 -> {
                binding.btnNext.text = getString(R.string.text_learn_more_2)
            }
            1 -> {
                binding.btnNext.text = getString(R.string.text_learn_more_2)
            }
            2 -> {
                binding.btnNext.text = getString(R.string.text_next)
            }
            3 -> {
                binding.btnNext.text = getString(R.string.text_next)
            }
            4 -> {
                binding.btnNext.text = getString(R.string.text_next)
                binding.toolbar.tvTitle.text = getString(R.string.text_corrective_activities)
            }
            5 -> {
                binding.btnNext.text = getString(R.string.text_let_s_identify_your_chronotype)
            }
            else -> {}
        }
    }

}