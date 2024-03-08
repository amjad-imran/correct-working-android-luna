package com.noisefit.ui.onboarding.setup.firmware

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentRingConnectingCheckBinding
import com.noisefit.ui.onboarding.setup.DeviceSetupSharedViewModel
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.loadImageWithCache
import com.noisefit_commans.utils.LOGS

class RingConnectingCheckFragment :
    BaseFragment<FragmentRingConnectingCheckBinding>(FragmentRingConnectingCheckBinding::inflate) {

    private val viewModel: DeviceSetupSharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.animView.loadImageWithCache(binding.animView.context,viewModel.getRingImage())
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {
        viewModel.sessionManager.connectStateRing.observe(this) { connectedState ->
            when (connectedState) {
                is ConnectState.ConnectSuccess -> {
                    if (!viewModel.connectionChecked) {
                        viewModel.connectionChecked = true

                        navigate(RingConnectingCheckFragmentDirections.navigateToFirmwareCheck())
                    }

                }

                else -> {}
            }
        }
    }


}