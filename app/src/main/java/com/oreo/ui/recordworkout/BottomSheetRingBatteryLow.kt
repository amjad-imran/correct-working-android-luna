package com.oreo.ui.recordworkout

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetBatteryLowBinding
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.loadImage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class BottomSheetRingBatteryLow :
    BaseBottomSheetWithTransparent<BottomSheetBatteryLowBinding>(
        BottomSheetBatteryLowBinding::inflate
    ) {

    @Inject
    lateinit var watchDataStore: WatchDataStore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.oreoStatus.loadImage(
            requireContext(),
            R.drawable.ic_ring_low_battery
        )
        binding.batteryStatus.setIndicatorColor(resources.getColor(R.color.color_error))

        val batteryPercentage = watchDataStore.getBatteryPercentRing()
        binding.batteryStatus.progress = batteryPercentage

    }

    override fun initListener() {
        binding.btnAllow.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

}
