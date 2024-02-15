package com.oreo.ui.recordworkout

import androidx.core.view.setPadding
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.utils.Utils
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetRingConnectingBinding
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.ScreenUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class BottomSheetRingConnecting :
    BaseBottomSheetWithTransparent<BottomSheetRingConnectingBinding>(
        BottomSheetRingConnectingBinding::inflate
    ) {

    val viewModel: RecordWorkoutViewModel by viewModels()

    @Inject
    lateinit var screenUtils: ScreenUtils

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

                }

                is ConnectState.Connecting -> {
                    if (viewModel.sessionManager.bluetoothStateDash.value == false) {
                        stateBluetoothOff()
                    } else {
                        stateConnecting()
                    }
                }

                is ConnectState.ConnectSuccess -> {

                    binding.oreoStatus.gone()
                    binding.lottieAnimView.gone()
                    binding.textView90.gone()
                    binding.textRingConnecteMessage.gone()

                    binding.ivConnectSuccess.visible()
                    binding.textConnectSuccess.visible()

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
        binding.oreoStatus.visible()
        binding.lottieAnimView.gone()
        binding.textView90.visible()
        binding.textRingConnecteMessage.visible()

        binding.ivConnectSuccess.gone()
        binding.textConnectSuccess.gone()


        binding.btnAllow.isEnabled = true
        binding.oreoStatus.setImageResource(R.drawable.ic_ring_bluetooth_off_2)
        binding.oreoStatus.setPadding(screenUtils.dpToPx(0,requireContext()).toInt())
        binding.oreoStatus.background = null
        binding.textView90.text = getString(R.string.text_bluetooth_turn_on)
        binding.textRingConnecteMessage.text = getString(R.string.text_bluetooth_on_message)
        binding.lottieAnimView.gone()
    }

    private fun stateConnecting() {

        binding.oreoStatus.visible()
        binding.lottieAnimView.visible()
        binding.textView90.visible()
        binding.textRingConnecteMessage.visible()

        binding.ivConnectSuccess.gone()
        binding.textConnectSuccess.gone()

        binding.btnAllow.isEnabled = false
        binding.oreoStatus.setImageResource(R.drawable.ic_ring_default_silver)
        binding.oreoStatus.setPadding(screenUtils.dpToPx(14,requireContext()).toInt())
        binding.textView90.text = getString(R.string.text_trying_to_connect_your_ring)
        binding.textRingConnecteMessage.text = getString(R.string.text_ring_not_in_range)
    }

}
