package com.oreo.ui.femalehealth.onboarding

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentFMHOnboardingBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class FMHOnboardingFragment :
    BaseFragment<FragmentFMHOnboardingBinding>(FragmentFMHOnboardingBinding::inflate) {
    private val mViewModel: FMHOnboardingViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)
        setViewpager()
    }

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPress()
            }
        }

    private fun setViewpager() {

        val fragAdapter =
            FMHFragmentAdapter(childFragmentManager, lifecycle, mViewModel.fragmentSize)
        binding.vpFmhOnboard.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 1
            adapter = fragAdapter
            setOnTouchListener(null)

        }

        binding.vpFmhOnboard.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)


            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 1 || position == 2) {
                    binding.bNotSure.visible()
                } else
                    binding.bNotSure.gone()
                if (position == mViewModel.fragmentSize - 1)
                    binding.bNext.text = getString(R.string.text_done)
                else
                    binding.bNext.text = getString(R.string.text_next)

                setProgress(position)


            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })

        binding.vpFmhOnboard.setCurrentItem(0, false)

    }

    private fun setProgress(position: Int) {
        val max = mViewModel.fragmentSize
        if (position == max - 1) binding.lytProgress.root.gone() else
            binding.lytProgress.root.visible()
        binding.lytProgress.apply {
            pgBr.progress = (((position + 1).toFloat() / max) * 100).roundToInt()
            tvCount.text = "0${position + 1}"
        }
    }

    override fun initListener() {
        binding.vLeft.setOnClickListener {
            onBackPress()
        }
        binding.vRight.setOnClickListener {
            onNextPress()
        }

        binding.backBtn.setOnClickListener {
            onBackPress()
        }
        binding.bNext.setOnClickListener {
            onNextPress()
        }
        binding.bNotSure.setOnClickListener {
            onNextPress()
        }


    }

    fun onBackPress() {
        val current = binding.vpFmhOnboard.currentItem
        if (current != 0) {
            binding.vpFmhOnboard.setCurrentItem(current - 1, true)
        } else {
            navigateUpSafe()
        }
    }

    private fun onNextPress() {
        val current = binding.vpFmhOnboard.currentItem
        if (current == (mViewModel.fragmentSize - 1)) {
            mViewModel.updateFemaleHealthData()

        } else {
            binding.vpFmhOnboard.setCurrentItem(current + 1, true)
        }
    }

    override fun subscribeObservers() {
        mViewModel.femaleHealthSubmitInfo.observe(this) { it1 ->
            it1?.getContent()?.let {
                navigate(R.id.fragmentCycleTracker)
            }

        }
        mViewModel.isGoalSelected.observe(this) { it1 ->
            it1?.getContent()?.let {
                binding.bNext.isEnabled = it
            }
        }
    }

}