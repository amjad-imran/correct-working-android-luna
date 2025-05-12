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
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS

class UpdateAvailableFragment :
    BaseFragment<FragmentUpdateAvailableBinding>(FragmentUpdateAvailableBinding::inflate) {

    private val viewModel: DeviceSetupSharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vPlayer.repeatCount = 0
        binding.vPlayer.setAnimation(R.raw.anim_onboard_5)
        binding.vPlayer.playAnimation()

        binding.ivRingImage.loadImageWithCache(
            binding.ivRingImage.context,
            viewModel.getRingImage2()
        )

        viewModel.updateProgress1.postValue(100)
        viewModel.sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)

    }

    override fun initListener() {
        binding.tvRemindLater.setOnClickListener {
            navigate(UpdateAvailableFragmentDirections.navigateToDeviceSetupFromUpdateAvailableFrag())
        }


        binding.btnUpdateNow.setOnClickListener {
            onUpdateNowClicked()
        }
    }

    private fun onUpdateNowClicked(){
        viewModel.sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)

        val battery = viewModel.sessionManager.batteryPercentRing.value ?: 0
        if (battery != 0) {
            if (battery <= viewModel.getMinBatteryPercent()
            ) {
                showBatteryWarning()
                return
            }
        }
        val isCharging = viewModel.sessionManager.isRingCharging.value?:false

        if(isCharging.not() && battery!=100){
            showNotChargingDialog()
            return
        }


        navigate(UpdateAvailableFragmentDirections.navigateToUpdateFrag())
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

    private fun showNotChargingDialog() {
        navigate(R.id.bottomSheetNotCharingFirmware)
    }

    override fun subscribeObservers() {

        setFragmentResultListener(BATTERY_CHARGE_FIRMWARE) { _, bundle ->
            val start = bundle.getBoolean("start")
            if (start) {
                viewModel.startUpdate.postValue(Event(true))
            }
        }

        viewModel.startUpdate.observe(this){
            it.getContent()?.let {
                this@UpdateAvailableFragment.navigate(UpdateAvailableFragmentDirections.navigateToUpdateFrag())
            }
        }

    }


}