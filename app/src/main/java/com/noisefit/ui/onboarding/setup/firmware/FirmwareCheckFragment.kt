package com.noisefit.ui.onboarding.setup.firmware

import android.animation.Animator
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentFirmwareCheckBinding
import com.noisefit.ui.common.bottomSheet.RING_DISABLED_KEY
import com.noisefit.ui.onboarding.setup.DeviceSetupSharedViewModel
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.loadImageWithCache
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.Event
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FirmwareCheckFragment :
    BaseFragment<FragmentFirmwareCheckBinding>(FragmentFirmwareCheckBinding::inflate) {

    private val viewModel: DeviceSetupSharedViewModel by activityViewModels()
    var currentCheckState = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        binding.vPlayer.setAnimation(R.raw.anim_onboard_6)
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
        binding.tvRemindLater.setOnClickListener {
            navigate(FirmwareCheckFragmentDirections.navigateToDeviceSetupFromFirmwareCheck())

        }
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
        viewModel.unpairDevice.observe(this) {
            it.getContent()?.let {
                viewModel.sessionManager.forceDisconnect.value = (Event(true))
                viewModel.sessionManager.setConnectStateRing(ConnectState.UnPaired())

                showBlackListDialog()
            }
        }
        viewModel.navigateToUpdateAvailable.observe(this) {
            it.getContent()?.let {
                currentCheckState = 2
            }
        }

    }

    private fun showBlackListDialog() {
        requireActivity().supportFragmentManager.setFragmentResultListener(
            RING_DISABLED_KEY,
            viewLifecycleOwner
        ) { key, bundle ->
            val cancel = bundle.getBoolean("cancel")
            if (cancel) {
                activity?.finish()
            }
        }

        navigate(R.id.ringDisabledBottomSheet2)
    }

}
