package com.noisefit.ui.onboarding.setup.firmware

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentFirmwareUpdateSuccessBinding
import com.noisefit.ui.onboarding.setup.DeviceSetupSharedViewModel
import com.noisefit_commans.ui.BaseFragment

class FirmwareUpdateSuccessFragment :
    BaseFragment<FragmentFirmwareUpdateSuccessBinding>(FragmentFirmwareUpdateSuccessBinding::inflate) {

    private val viewModel: DeviceSetupSharedViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.updateProgress2.postValue(100)

        Handler(Looper.getMainLooper()).postDelayed({
            navigate(FirmwareUpdateSuccessFragmentDirections.navigateToSetupSuccessFromFirmware())
        }, 5000)
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


}