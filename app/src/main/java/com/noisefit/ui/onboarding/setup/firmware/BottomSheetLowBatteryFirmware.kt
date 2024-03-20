package com.noisefit.ui.onboarding.setup.firmware

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetLowBatteryFirmwareBinding
import com.noisefit.luna.databinding.BottomSheetUpdateFailedBinding
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.loadImage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val LOW_BATTERY_FIRMWARE = "LOW_BATTERY_FIRMWARE"

@AndroidEntryPoint
class BottomSheetLowBatteryFirmware :
    BaseBottomSheetWithTransparent<BottomSheetLowBatteryFirmwareBinding>(
        BottomSheetLowBatteryFirmwareBinding::inflate
    ) {
    @Inject
    lateinit var watchDataStore: WatchDataStore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.oreoStatus.loadImage(
            requireContext(),
            R.drawable.ic_ring_default_silver_new
        )
        binding.batteryStatus.setIndicatorColor(resources.getColor(R.color.color_error))

        val batteryPercentage = watchDataStore.getBatteryPercentRing()
        binding.batteryStatus.progress = batteryPercentage
    }

    override fun initListener() {

        binding.btnTryAgain.setOnClickListener {
            setFragmentResult(
                LOW_BATTERY_FIRMWARE,
                bundleOf("tryAgain" to true)
            )
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dia ->
            val dialog = dia as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isHideable = true
                isDraggable = true
                isCancelable = false
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }
}
