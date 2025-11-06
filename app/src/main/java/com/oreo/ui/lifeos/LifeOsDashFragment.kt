package com.oreo.ui.lifeos

import com.noisefit.luna.databinding.FragmentLifeOsDashBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LifeOsDashFragment :
    BaseFragment<FragmentLifeOsDashBinding>(FragmentLifeOsDashBinding::inflate) {


    override fun initListener() {
        binding.lytHeader.lytChatBox.root.setOnClickListener {

        }
    }

    override fun subscribeObservers() {

    }
}