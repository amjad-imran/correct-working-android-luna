package com.noisefit.ui.onboarding.setup.firmware

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentFirmwareCheckBinding
import com.noisefit.ui.onboarding.setup.DeviceSetupSharedViewModel
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FirmwareCheckFragment :
    BaseFragment<FragmentFirmwareCheckBinding>(FragmentFirmwareCheckBinding::inflate) {

    private val viewModel: DeviceSetupSharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.sessionManager.postFirmwareDetailsOnSetup = true
        viewModel.sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)

        viewModel.updateProgress1.postValue(100)

    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

        viewModel.sessionManager.checkForVersionUpdateSetup.observe(viewLifecycleOwner) {
            it.getContent()?.let { pair ->
                viewModel.checkOtaVersionServer(pair)
            }
        }

        viewModel.navigateToDeviceUpToDate.observe(this) {
            it.getContent()?.let {
                navigate(FirmwareCheckFragmentDirections.navigateToDeviceUpToDate())
            }
        }
        viewModel.navigateToUpdateAvailable.observe(this) {
            it.getContent()?.let {
                navigate(FirmwareCheckFragmentDirections.navigateToUpdateAvailable())
            }
        }

    }

}