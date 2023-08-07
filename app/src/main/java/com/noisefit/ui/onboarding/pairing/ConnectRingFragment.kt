package com.noisefit.ui.onboarding.pairing

import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentConnectRingBinding
import com.noisefit.ui.web.WebViewActivity
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.AppConstants

class ConnectRingFragment :
    BaseFragment<FragmentConnectRingBinding>(FragmentConnectRingBinding::inflate) {


    override fun initListener() {
        binding.btnSearchNow.setOnClickListener {
            navigate(ConnectRingFragmentDirections.actionConnectRingFragmentToFindDeviceListFragment())
        }
        binding.tvNoLunaRing.setOnClickListener {
            startActivity(
                WebViewActivity.getStartIntent(
                    requireActivity(),
                    getString(R.string.text_shop),
                    AppConstants.NO_LUNA_RING
                )
            )
        }
    }

    override fun subscribeObservers() {

    }


}