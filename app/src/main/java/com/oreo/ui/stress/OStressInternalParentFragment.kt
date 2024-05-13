package com.oreo.ui.stress

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayout
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOStressInternalParentBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OStressInternalParentFragment :
    BaseFragment<FragmentOStressInternalParentBinding>(FragmentOStressInternalParentBinding::inflate) {
    private val args: OStressInternalParentFragmentArgs by navArgs()

    @Inject
    lateinit var sessionManager: SessionManager


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lytToolbar.view1.visible()
        setViewPager()
    }

    private fun setViewPager() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Day"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Week"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Month"))
        if (args.cameFrom == "active") {
            binding.lytToolbar.tvTitle.text = getString(R.string.text_overall_stress)
            sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_overall_stress_page_visit)

        } else {
            binding.lytToolbar.tvTitle.text = getString(R.string.text_non_active_stress)
            sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_non_active_stress_page_visit)
        }

        loadFragment(
            OStressInternalDetailsFragment.newInstance(
                "Day",
                args.date,
                args.cameFrom
            )
        )
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        loadFragment(
                            OStressInternalDetailsFragment.newInstance(
                                "Day",
                                args.date,
                                args.cameFrom
                            )
                        )
                    }

                    1 -> {
                        loadFragment(
                            OStressInternalDetailsFragment.newInstance(
                                "Week",
                                args.date,
                                args.cameFrom
                            )
                        )
                    }

                    else -> {
                        loadFragment(
                            OStressInternalDetailsFragment.newInstance(
                                "Month",
                                args.date,
                                args.cameFrom
                            )
                        )
                    }
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })


    }

    private fun loadFragment(fragment: Fragment) {
        val fm: FragmentManager = parentFragmentManager
        val fragmentTransaction: FragmentTransaction = fm.beginTransaction()
        fragmentTransaction.replace(R.id.flFragment, fragment)
        fragmentTransaction.commit()

    }

    override fun initListener() {
        binding.lytToolbar.view1.invisible()
        binding.lytToolbar.ivAddFriend.invisible()
        binding.lytToolbar.view1.loadImage(requireActivity(), R.drawable.ic_info_oreo)
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.lytToolbar.view1.setOnClickListener {
            navigate(R.id.stressUnderstandingFragment)
        }

    }

    override fun subscribeObservers() {

    }

}