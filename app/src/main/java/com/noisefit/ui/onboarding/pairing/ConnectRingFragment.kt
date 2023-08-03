package com.noisefit.ui.onboarding.pairing

import com.noisefit.luna.databinding.FragmentConnectRingBinding
import com.noisefit_commans.ui.BaseFragment

class ConnectRingFragment :
    BaseFragment<FragmentConnectRingBinding>(FragmentConnectRingBinding::inflate) {


    override fun initListener() {
        binding.btnSearchNow.setOnClickListener {
            navigate(ConnectRingFragmentDirections.actionConnectRingFragmentToFindDeviceListFragment())
        }
    }

    override fun subscribeObservers() {

    }


}