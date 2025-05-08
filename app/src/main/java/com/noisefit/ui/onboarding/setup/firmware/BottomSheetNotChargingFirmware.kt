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
import com.noisefit.luna.databinding.BottomSheetNotChargingFirmwareBinding
import com.noisefit.luna.databinding.BottomSheetUpdateFailedBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val BATTERY_CHARGE_FIRMWARE = "BATTERY_CHARGE_FIRMWARE"

@AndroidEntryPoint
class BottomSheetNotChargingFirmware :
    BaseBottomSheetWithTransparent<BottomSheetNotChargingFirmwareBinding>(
        BottomSheetNotChargingFirmwareBinding::inflate
    ) {
    @Inject
    lateinit var sessionManager: SessionManager


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)

    }

    override fun initListener() {
        binding.btnOnCharger.setOnClickListener {
            setFragmentResult(
                BATTERY_CHARGE_FIRMWARE,
                bundleOf("start" to true)
            )
            navigateUpSafe()
        }
        binding.ivClose.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        binding.btnOnCharger.invisible()
        sessionManager.isRingCharging.observe(this) {
            if (it) {
                binding.btnOnCharger.visible()
            } else {
                binding.btnOnCharger.invisible()
            }
        }

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
