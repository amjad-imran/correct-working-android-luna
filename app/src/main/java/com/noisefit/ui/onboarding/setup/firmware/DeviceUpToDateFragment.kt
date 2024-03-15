package com.noisefit.ui.onboarding.setup.firmware

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.activityViewModels
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentDeviceUpToDateBinding
import com.noisefit.ui.onboarding.setup.DeviceSetupSharedViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.loadImageWithCache


class DeviceUpToDateFragment :
    BaseFragment<FragmentDeviceUpToDateBinding>(FragmentDeviceUpToDateBinding::inflate) {

    private val viewModel: DeviceSetupSharedViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.updateProgress1.postValue(100)
        viewModel.updateProgress2.postValue(100)
        binding.vPlayer.repeatCount = 0
        binding.vPlayer.setAnimation(R.raw.anim_onboard_5)
        binding.vPlayer.playAnimation()
        binding.ivRingImage.loadImageWithCache(binding.ivRingImage.context,viewModel.getRingImage2())



        Handler(Looper.getMainLooper()).postDelayed({
            navigate(DeviceUpToDateFragmentDirections.navigateToSetupDevice())
        }, 3000)
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


}