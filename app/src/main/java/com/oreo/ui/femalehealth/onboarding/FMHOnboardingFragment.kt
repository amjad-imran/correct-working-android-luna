package com.oreo.ui.femalehealth.onboarding

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.viewpager2.widget.ViewPager2
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentFMHOnboardingBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.ui.femalehealth.cycletracker.NOTIFY_LOG
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class FMHOnboardingFragment :
    BaseFragment<FragmentFMHOnboardingBinding>(FragmentFMHOnboardingBinding::inflate) {
    private val mViewModel: FMHOnboardingViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)
        mViewModel.resetData()
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
                /*  if (position == 1 || position == 2) {
                      binding.bNotSure.visible()
                  } else
                      binding.bNotSure.invisible()*/

                /* if (position == mViewModel.fragmentSize - 1)
                     binding.bNext.text = getString(R.string.text_done)
                 else {
                     if (position == 1 || position == 2) {
                         binding.bNext.text = getString(R.string.text_confirm)
                     } else
                         binding.bNext.text = getString(R.string.text_next)
                 }*/

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

        binding.backBtn.setOnClickListener {
            onBackPress()
        }
    }

    fun onBackPress() {
        val current = binding.vpFmhOnboard.currentItem
        if (current != 0) {
            binding.vpFmhOnboard.setCurrentItem(current - 1, true)
        } else {
            navigateUpSafe()
            //activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)
        }
    }

    private fun showComingSoonBottomSheet() {
        setFragmentResultListener(NOTIFY_LOG) { _, bundle ->
            val isNotifyMe = bundle.getBoolean("agree")
            if (isNotifyMe) {
                mViewModel.localDataStore.setStressWalkthroughShown(true)
                navigate(R.id.navigation_oreo_home)
            }
        }
        navigate(
            R.id.dialogComingSoon
        )
    }

    private fun onNextPress() {
        val current = binding.vpFmhOnboard.currentItem
        when (current) {
            0 -> {
                if (mViewModel.goalTypeSelected == GoalType.TRACK_PREGNANCY) {
                    mViewModel.updateFemaleHealthData(4)
                    showComingSoonBottomSheet()
                    return
                }
            }

            3 -> {
                if (mViewModel.selectedPStartDate == null) {
                    return
                }
            }

            4 -> {
                if (mViewModel.selectedDiagnoseListData.isEmpty()) {
                    return
                }
            }

            5 -> {
                if (mViewModel.selectedHormoneListData.isEmpty()) {
                    return
                }
                mViewModel.updateFemaleHealthData()
                return
            }
        }

        binding.vpFmhOnboard.setCurrentItem(current + 1, true)
    }

    override fun subscribeObservers() {
        mViewModel.onNextPress.observe(this) {
            it.getContent()?.let {
                onNextPress()
            }
        }
        mViewModel.onNotSurePress.observe(this) {
            it.getContent()?.let {
                val current = binding.vpFmhOnboard.currentItem

                when (current) {
                    1 -> {
                        mViewModel.updateFemaleHealthData(3)
                        return@observe
                    }

                    2 -> {
                        mViewModel.updateFemaleHealthData(3)
                        return@observe
                    }
                }
            }
        }

        mViewModel.moveBack.observe(this) {
            it.getContent()?.let {
                navigateUpSafe()
            }
        }
        mViewModel.femaleHealthSubmitInfo.observe(this) { it1 ->
            it1?.getContent()?.let {
                navigate(FMHOnboardingFragmentDirections.actionFemaleHealthOnboardingFragmentToFmhOnboardingAllDoneFragment())
            }
        }

        mViewModel.femaleHealthSkip.observe(this) { it1 ->
            it1?.getContent()?.let {
                navigate(FMHOnboardingFragmentDirections.actionFemaleHealthOnboardingFragmentToFragmentCycleTracker())
            }
        }

        mViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        mViewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        mViewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }

}