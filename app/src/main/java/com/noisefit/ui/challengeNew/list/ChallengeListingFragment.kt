package com.noisefit.ui.challengeNew.list

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieDrawable
import com.noisefit.R
import com.noisefit.databinding.FragmentChallengeListingBinding
import com.noisefit_commans.data.local.abstraction.DataStoredInterface

import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.getDeeplinkPathArg
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChallengeListingFragment :
    BaseFragment<FragmentChallengeListingBinding>(FragmentChallengeListingBinding::inflate) {
    private lateinit var pagerAdapter: ChallengeListPagerAdapter

    @Inject
    lateinit var localDataStoredInterface: DataStoredInterface
    var currentItem: Int = 0


    private val tabSharedViewModel: MyChallengeTabSharedViewModel by activityViewModels()
    private val tabsAdapter: MyChallengesTabAdapter by lazy {
        MyChallengesTabAdapter(
            object : TestTabActions {
                override fun onTabClicked(position: Int, text: String) {
                    tabSharedViewModel.isTabClicked = true
                    currentItem = position
                    binding.vpWatchFace.setCurrentItem(position, true)
                    if (position == 0)
                        tabSharedViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CHALLENGES_NEW_CLICK)
                    else if (position == 1)
                        tabSharedViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CHALLENGES_JOINED_CLICK)
                    else
                        tabSharedViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CHALLENGES_COMPLETED_CLICK)

                }
            })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!localDataStoredInterface.isChallengeHelperScreenClick()) {
            binding.lytPullToRefresh.visible()
            binding.lottieAnimViewPullToRefresh.repeatCount = LottieDrawable.INFINITE
            binding.lottieAnimViewPullToRefresh.setAnimation(R.raw.anim_pull_to_refresh)
            binding.lottieAnimViewPullToRefresh.playAnimation()
        }
        arguments?.let {

            it.getDeeplinkPathArg()?.let { param ->
                currentItem = if (param.equals("completedchallenges", true)) {
                    2
                } else {
                    0
                }
            }
        }
        setViewPager()
        setTabs()
    }

    private fun setViewPager() {
        pagerAdapter = ChallengeListPagerAdapter(childFragmentManager, lifecycle)
        binding.vpWatchFace.isUserInputEnabled = false
        binding.vpWatchFace.adapter = pagerAdapter
        tabsAdapter.setDataSet(arrayListOf("New", "Joined", "Completed"))

        Handler(Looper.getMainLooper()).postDelayed({
            if (view != null) {
                binding.vpWatchFace.setCurrentItem(currentItem, true)
            }
        }, 500)
    }

    private fun setTabs() {
        binding.rvTabs.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTabs.adapter = tabsAdapter
        tabsAdapter.selectedPosition = currentItem
    }


    override fun initListener() {

        binding.lytPullToRefresh.setOnClickListener {
            localDataStoredInterface.setChallengeHelperScreenClick(true)
            binding.lytPullToRefresh.gone()
            binding.lottieAnimViewPullToRefresh.pauseAnimation()
        }
    }

    override fun subscribeObservers() {
        tabSharedViewModel.tabListData.observe(this) {
            if(it.isNotEmpty()){
                tabsAdapter.setDataSet(it)
            }
        }
        tabSharedViewModel.updatedAtText.observe(this) {
            if (it.isNullOrEmpty()) {
                binding.tvUpdatedLast.gone()
            } else {
                binding.tvUpdatedLast.visible()
            }
            binding.tvUpdatedLast.text = it
        }

        tabSharedViewModel.moveToNewChallenge.observe(this) {
            it.getContent()?.let {
                tabsAdapter.selectedPosition = 0
                tabsAdapter.notifyDataSetChanged()
                binding.vpWatchFace.setCurrentItem(0, true)
            }
        }

        tabSharedViewModel.moveToJoinChallenge.observe(this) {
            it.getContent()?.let {
                tabsAdapter.selectedPosition = 1
                tabsAdapter.notifyDataSetChanged()
                currentItem = 1
                //setViewPager()
//                binding.vpWatchFace.setCurrentItem(1, true)
            }
        }


    }


    override fun onResume() {
        super.onResume()
        tabSharedViewModel.isTabClicked = false
    }
}