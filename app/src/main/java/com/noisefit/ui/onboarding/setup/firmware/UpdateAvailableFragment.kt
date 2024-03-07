package com.noisefit.ui.onboarding.setup.firmware

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentUpdateAvailableBinding
import com.noisefit.ui.onboarding.setup.DeviceSetupSharedViewModel
import com.noisefit_commans.ui.BaseFragment

class UpdateAvailableFragment :
    BaseFragment<FragmentUpdateAvailableBinding>(FragmentUpdateAvailableBinding::inflate) {

    private val viewModel: DeviceSetupSharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.updateProgress2.postValue(20)
    }

    override fun initListener() {
        binding.btnUpdateNow.setOnClickListener {
            navigate(UpdateAvailableFragmentDirections.navigateToUpdateFrag())
        }
    }

    override fun subscribeObservers() {

    }




}