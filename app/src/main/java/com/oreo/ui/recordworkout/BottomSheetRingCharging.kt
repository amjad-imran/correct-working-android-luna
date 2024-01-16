package com.oreo.ui.recordworkout

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetRingChargingBinding
import com.noisefit.luna.databinding.BottomSheetRingConnectingBinding
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BottomSheetRingCharging :
    BaseBottomSheetWithTransparent<BottomSheetRingChargingBinding>(
        BottomSheetRingChargingBinding::inflate
    ) {

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
        binding.oreoStatus.setImageResource(R.drawable.ic_ring_charging)
        binding.textView90.text = getString(R.string.text_ring_charging)
        binding.textRingConnecteMessage.text = getString(R.string.text_charging_message)
        binding.btnAllow.isEnabled = true
        binding.btnAllow.text = getString(R.string.text_try_again)
    }
}
