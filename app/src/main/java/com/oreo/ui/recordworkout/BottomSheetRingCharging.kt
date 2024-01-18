package com.oreo.ui.recordworkout

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetRingChargingBinding
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class BottomSheetRingCharging :
    BaseBottomSheetWithTransparent<BottomSheetRingChargingBinding>(
        BottomSheetRingChargingBinding::inflate
    ) {

    @Inject
    lateinit var watchDataStore: WatchDataStore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stateCharging()
    }

    override fun initListener() {
        binding.btnAllow.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

    private fun stateCharging() {
        binding.batteryStatus.setIndicatorColor(resources.getColor(R.color.white))
        val batteryPercentage = watchDataStore.getBatteryPercentRing()
        binding.batteryStatus.progress = batteryPercentage
        binding.oreoStatus.setImageResource(R.drawable.ic_ring_charging)
        binding.textView90.text = getString(R.string.text_ring_charging)
        binding.textRingConnecteMessage.text = getString(R.string.text_charging_message)
        binding.btnAllow.isEnabled = true
        binding.btnAllow.text = getString(R.string.text_try_again)
    }
}
