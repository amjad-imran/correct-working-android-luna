package com.noisefit.ui.onboarding.setup.firmware

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentFirmwareUpdateSuccessBinding
import com.noisefit.ui.onboarding.setup.DeviceSetupSharedViewModel
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.loadImageWithCache

class FirmwareUpdateSuccessFragment :
    BaseFragment<FragmentFirmwareUpdateSuccessBinding>(FragmentFirmwareUpdateSuccessBinding::inflate) {

    private val viewModel: DeviceSetupSharedViewModel by activityViewModels()
    var isNavigated = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.updateProgress2.postValue(100)
        binding.animView.loadImageWithCache(binding.animView.context, viewModel.getRingImage())

        Handler(Looper.getMainLooper()).postDelayed({
            checkAndNavigate()
        }, 5000)
    }

    fun checkAndNavigate() {
        viewModel.sessionManager.connectStateRing.observe(viewLifecycleOwner) { connectedState ->
            when (connectedState) {
                is ConnectState.ConnectSuccess -> {
                    if (!isNavigated) {
                        navigate(FirmwareUpdateSuccessFragmentDirections.navigateToSetupSuccessFromFirmware())
                    }
                }

                else -> {}
            }
        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


}