package com.noisefit.ui.onboarding.setup.firmware

import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentRingConnectingCheckBinding
import com.noisefit.ui.onboarding.setup.DeviceSetupSharedViewModel
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.LOGS

class RingConnectingCheckFragment :
    BaseFragment<FragmentRingConnectingCheckBinding>(FragmentRingConnectingCheckBinding::inflate) {

    private val viewModel: DeviceSetupSharedViewModel by activityViewModels()


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