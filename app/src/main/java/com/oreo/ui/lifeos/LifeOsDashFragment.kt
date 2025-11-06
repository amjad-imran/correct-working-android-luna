package com.oreo.ui.lifeos

import com.noisefit.luna.databinding.FragmentLifeOsDashBinding
import com.noisefit_commans.ui.BaseFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.noisefit.luna.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LifeOsDashFragment :
    BaseFragment<FragmentLifeOsDashBinding>(FragmentLifeOsDashBinding::inflate) {


    override fun initListener() {
        binding.lytHeader.lytChatBox.root.setOnClickListener {
            val sharedView = binding.lytHeader.lytChatBox.root
            val transitionName = sharedView.transitionName
            val extras = FragmentNavigatorExtras(sharedView to transitionName)
            findNavController().navigate(
                R.id.action_navigation_lifeOsFragment_to_lifeOsChatFragment,
                null,
                null,
                extras
            )
        }
    }

    override fun subscribeObservers() {

    }
}
