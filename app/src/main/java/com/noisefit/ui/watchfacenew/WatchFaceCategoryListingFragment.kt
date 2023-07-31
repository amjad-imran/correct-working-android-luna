package com.noisefit.ui.watchfacenew

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentWatchFaceCategoryListingBinding
import com.noisefit.ui.challenge.challengeLeaderboard.MyBuddiesTabAdapter
import com.noisefit.ui.challenge.challengeLeaderboard.TabActions
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.watchface.WatchFaceCatListViewModel
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

@Deprecated("Use Watchface2CategoryFragment")
@AndroidEntryPoint
class WatchFaceCategoryListingFragment :
    BaseFragment<FragmentWatchFaceCategoryListingBinding>(FragmentWatchFaceCategoryListingBinding::inflate) {

    private lateinit var pagerAdapter: WatchFaceCategoryPagerAdapter
    private val viewModel: WatchFaceCatListViewModel by viewModels()


    private val tabsAdapter: MyBuddiesTabAdapter by lazy {
        MyBuddiesTabAdapter(arrayListOf("All", "MyFav"),
            object : TabActions {
                override fun onTabClicked(position: Int, text: String) {
                    binding.vpWatchFace.setCurrentItem(position, true)
                    if (position == 0) {

                        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACE_ALL_CLICK)
                        //viewModel.getWatchFaceCustomData()
                    } else if (position == 1) {
                        //viewModel.getFavouriteWatchFaces(false)
                        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACE_MYFAV_CLICK)
                    }
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.tvTitle.text = getString(R.string.text_watchface)
        setViewPager()
        setTabs()
        //viewModel.getWatchFaceCustomData()
        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACE_ALL_CLICK)
    }

    private fun setViewPager() {
        pagerAdapter = WatchFaceCategoryPagerAdapter(childFragmentManager, lifecycle)
        binding.vpWatchFace.isUserInputEnabled = false
        binding.vpWatchFace.adapter = pagerAdapter
    }

    private fun setTabs() {
        binding.rvTabs.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTabs.adapter = tabsAdapter
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }
}