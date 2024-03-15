package com.noisefit.ui.onboarding.setup.firmware

import android.animation.Animator
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentFirmwareCheckBinding
import com.noisefit.ui.onboarding.setup.DeviceSetupSharedViewModel
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.loadImageWithCache
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FirmwareCheckFragment :
    BaseFragment<FragmentFirmwareCheckBinding>(FragmentFirmwareCheckBinding::inflate) {

    private val viewModel: DeviceSetupSharedViewModel by activityViewModels()
    var currentCheckState = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /* binding.vPlayer.repeatCount = LottieDrawable.INFINITE
         binding.vPlayer.setAnimation(R.raw.anim_pairing)
         binding.vPlayer.playAnimation()*/
        binding.ivRingImage.loadImageWithCache(
            binding.ivRingImage.context,
            viewModel.getRingImage2()
        )

        viewModel.sessionManager.postFirmwareDetailsOnSetup = true
        viewModel.sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)

        viewModel.updateProgress1.postValue(50)
        startFirmwareCheckLottie(onAnimRepeat = {
            if (currentCheckState == 1) {
                binding.vPlayer.cancelAnimation()
                navigate(FirmwareCheckFragmentDirections.navigateToDeviceUpToDate())
            } else if (currentCheckState == 2) {
                binding.vPlayer.cancelAnimation()
                navigate(FirmwareCheckFragmentDirections.navigateToUpdateAvailable())
            }
        })


    }

    private fun startFirmwareCheckLottie(onAnimRepeat: () -> Unit) {
        binding.vPlayer.repeatCount = LottieDrawable.INFINITE
        binding.vPlayer.setAnimation(R.raw.anim_bottom_fill_blue)
        binding.vPlayer.playAnimation()
        binding.vPlayer.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {

            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {
                onAnimRepeat.invoke()
            }

        })
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
                currentCheckState = 1
            }
        }
        viewModel.navigateToUpdateAvailable.observe(this) {
            it.getContent()?.let {
                currentCheckState = 2
            }
        }

    }

}