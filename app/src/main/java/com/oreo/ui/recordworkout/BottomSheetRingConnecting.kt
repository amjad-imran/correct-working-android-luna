package com.oreo.ui.recordworkout

import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetRingConnectingBinding
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BottomSheetRingConnecting :
    BaseBottomSheetWithTransparent<BottomSheetRingConnectingBinding>(
        BottomSheetRingConnectingBinding::inflate
    ) {

    val viewModel: RecordWorkoutViewModel by viewModels()

    override fun initListener() {
        binding.btnAllow.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        viewModel.sessionManager.connectStateRing.observe(this) { connectedState ->
            when (connectedState) {
                is ConnectState.ConnectFailed -> {
                    if (viewModel.sessionManager.bluetoothStateDash.value == false) {
                        stateBluetoothOff()
                    } else {
                        stateConnecting()
                    }

                    binding.btnAllow.isEnabled = false
                }

                is ConnectState.Connecting -> {
                    if (viewModel.sessionManager.bluetoothStateDash.value == false) {
                        stateBluetoothOff()
                    } else {
                        stateConnecting()
                    }
                    binding.btnAllow.isEnabled = false
                }

                is ConnectState.ConnectSuccess -> {
                    binding.groupConnecting.gone()
                    binding.groupConnected.visible()
                    binding.btnAllow.isEnabled = true
                }

                is ConnectState.UnPaired -> {
                    navigateUpSafe()
                }

                else -> {}
            }
        }
    }

    private fun stateBluetoothOff() {
        binding.groupConnecting.visible()
        binding.groupConnected.gone()
        binding.oreoStatus.setImageResource(R.drawable.ic_ring_bluetooth_off)
        binding.textView90.text = getString(R.string.text_bluetooth_turn_on)
        binding.textRingConnecteMessage.text = getString(R.string.text_bluetooth_on_message)
        binding.lottieAnimView.gone()
    }

    private fun stateConnecting() {
        binding.groupConnecting.visible()
        binding.groupConnected.gone()
        binding.lottieAnimView.visible()
        binding.oreoStatus.setImageResource(R.drawable.ic_ring_default_silver_new)
        binding.textView90.text = getString(R.string.text_trying_to_connect_your_ring)
        binding.textRingConnecteMessage.text = getString(R.string.text_ring_not_in_range)
    }

}
