package com.noisefit.ui.onboarding.setup.firmware

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentUpdateAvailableBinding
import com.noisefit.ui.onboarding.setup.DeviceSetupSharedViewModel
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.loadImageWithCache

class UpdateAvailableFragment :
    BaseFragment<FragmentUpdateAvailableBinding>(FragmentUpdateAvailableBinding::inflate) {

    private val viewModel: DeviceSetupSharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vPlayer.repeatCount = LottieDrawable.INFINITE
        binding.vPlayer.setAnimation(R.raw.anim_pulsating_glow_blue)
        binding.vPlayer.playAnimation()

        binding.ivRingImage.loadImageWithCache(
            binding.ivRingImage.context,
            viewModel.getRingImage2()
        )

        viewModel.updateProgress1.postValue(100)
    }

    override fun initListener() {
        binding.btnUpdateNow.setOnClickListener {

            val battery = viewModel.sessionManager.batteryPercentRing.value ?: 0
            if (battery != 0) {
                if (battery <= viewModel.getMinBatteryPercent()
                ) {
                    showBatteryWarning()
                    return@setOnClickListener
                }
            }


            navigate(UpdateAvailableFragmentDirections.navigateToUpdateFrag())
        }
    }

    private fun showBatteryWarning() {
        setFragmentResultListener(LOW_BATTERY_FIRMWARE) { _, bundle ->
            val tryAgain = bundle.getBoolean("tryAgain")
            if (tryAgain) {
                viewModel.sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
            }
        }
        navigate(R.id.bottomSheetLowBatteryFirmware)
    }

    override fun subscribeObservers() {

    }


}