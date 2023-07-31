package com.noisefit.ui.onboarding.onboardProfile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOnBoardConnectDeviceBinding
import com.noisefit_commans.data.enums.Device
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardConnectDeviceFragment :
    BaseFragment<FragmentOnBoardConnectDeviceBinding>(FragmentOnBoardConnectDeviceBinding::inflate) {
    private val viewModel: ConnectDeviceViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.isDevicePaired() && viewModel.isRingPaired()) {
            activity?.finishAffinity()
        } else {
            when (viewModel.pairedDevice()) {
                Device.SMARTWATCH -> {
                    moveToNextScreen(Device.RING)
                }

                Device.RING -> moveToNextScreen(Device.SMARTWATCH)
                null -> {}
            }
        }
    }

    override fun initListener() {
        binding.tvSmartwatch.setOnClickListener {
            viewModel.setCurrentDevice(Device.SMARTWATCH)
            moveToNextScreen(Device.SMARTWATCH)
        }

        binding.tvOreo.setOnClickListener {
            viewModel.setCurrentDevice(Device.RING)
            moveToNextScreen(Device.RING)
        }

    }

    private fun moveToNextScreen(deviceType: Device) {
//        if (authViewModel.isDevicePaired()) {
//            if (authViewModel.isProfileSetupComplete()) {
//                startActivity(DeviceSetupActivity.getStartIntent(requireContext()))
//                activity?.finish()
//            } else {
//                startActivity(ProfileSetupActivity.getStartIntent(requireContext()))
//                activity?.finish()
//            }
//        } else {

        navigate(R.id.findDeviceListFragment, Bundle().apply {
            putSerializable("device", deviceType)
        })
    }

    override fun subscribeObservers() {

    }


}