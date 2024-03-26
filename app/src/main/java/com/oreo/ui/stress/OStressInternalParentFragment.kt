package com.oreo.ui.stress

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOStressInternalParentBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OStressInternalParentFragment :
    BaseFragment<FragmentOStressInternalParentBinding>(FragmentOStressInternalParentBinding::inflate) {
    private val args: OStressInternalParentFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewPager()
    }

    private fun setViewPager() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Day"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Week"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Month"))
        binding.lytToolbar.tvTitle.text = getString(R.string.text_overall_stress)

        loadFragment(
            OStressInternalDetailsFragment.newInstance(
                "Day",
                args.date
            )
        )


    }

    private fun loadFragment(fragment: Fragment) {
        val fm: FragmentManager = parentFragmentManager
        val fragmentTransaction: FragmentTransaction = fm.beginTransaction()
        fragmentTransaction.replace(R.id.flFragment, fragment)
        fragmentTransaction.commit()

    }

    override fun initListener() {
        binding.lytToolbar.view1.visible()
        binding.lytToolbar.ivAddFriend.invisible()
        binding.lytToolbar.view1.loadImage(requireActivity(), R.drawable.ic_info_oreo)
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.lytToolbar.view1.setOnClickListener {
            //todo will open info screen
        }

    }

    override fun subscribeObservers() {

    }

}