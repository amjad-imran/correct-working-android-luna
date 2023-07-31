package com.noisefit.ui.friends.request

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRequestBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RequestFragment : BaseFragment<FragmentRequestBinding>(FragmentRequestBinding::inflate) {
    private lateinit var pagerAdapter: RequestPagerAdapter
    private val sharedViewModel: RequestTabSharedViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewPager()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0)
                    sharedViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.REQUESTRECEIVED_CLICK)
                else
                    sharedViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.REQUESTSENT_CLICK)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

    }

    private fun setViewPager() {
        pagerAdapter = RequestPagerAdapter(childFragmentManager, lifecycle)
        binding.vpRequest.isUserInputEnabled = false
        binding.vpRequest.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.vpRequest) { tab, position ->
            tab.text = arrayListOf("Requests received", "Requests sent")[position]
        }.attach()

    }



    override fun initListener() {

        binding.view1.setOnClickListener {
            navigate(R.id.addFriendsFragment)
        }
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

}