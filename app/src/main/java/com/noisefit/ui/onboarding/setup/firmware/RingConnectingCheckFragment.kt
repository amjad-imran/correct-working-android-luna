package com.noisefit.ui.onboarding.setup.firmware

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRingConnectingCheckBinding
import com.noisefit.ui.onboarding.setup.DeviceSetupSharedViewModel
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.loadImageWithCache

class RingConnectingCheckFragment :
    BaseFragment<FragmentRingConnectingCheckBinding>(FragmentRingConnectingCheckBinding::inflate) {

    private val viewModel: DeviceSetupSharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.vPlayer.repeatCount = LottieDrawable.INFINITE
        binding.vPlayer.setAnimation(R.raw.anim_onboard_6)
        binding.vPlayer.playAnimation()

        viewModel.updateProgress1.postValue(50)

        binding.ivRingImage.loadImageWithCache(
            binding.ivRingImage.context,
            viewModel.getRingImage2()
        )
    }


    override fun initListener() {
        binding.tvRemindLater.setOnClickListener {
            navigate(RingConnectingCheckFragmentDirections.navigateToDeviceSetupFromConnectionCheck())
        }
    }


    override fun subscribeObservers() {
        viewModel.sessionManager.connectStateRing.observe(this) { connectedState ->
            when (connectedState) {
                is ConnectState.ConnectSuccess -> {
                    if (!viewModel.connectionChecked) {
                        viewModel.connectionChecked = true
                        if (viewModel.fullSetup) {
                            navigate(RingConnectingCheckFragmentDirections.navigateToFirmwareCheck())
                        } else {
                            navigate(RingConnectingCheckFragmentDirections.navigateToDeviceSetupFromConnectionCheck())
                        }
                    }

                }

                else -> {}
            }
        }
    }


}