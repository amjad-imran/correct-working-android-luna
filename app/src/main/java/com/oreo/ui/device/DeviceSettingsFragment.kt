package com.oreo.ui.device

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentDeviceSettingsBinding
import com.noisefit.ui.myDevice.REST_REQUEST_KEY
import com.noisefit.ui.myDevice.UNPAIR_REQUEST_KEY
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.FirebaseLunaAppEvents
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DeviceSettingsFragment :
    BaseFragment<FragmentDeviceSettingsBinding>(FragmentDeviceSettingsBinding::inflate) {

    private val mViewModel: OMyDeviceViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.toolbar.tvTitle.text = "Device settings"
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }


        binding.tvRestartDevice.setOnClickListener {
            setFragmentResultListener(REST_REQUEST_KEY) { _, bundle ->
                val reset = bundle.getBoolean("reset")
                val ringNotConnected = bundle.getBoolean("ring_not_connected")
                if (reset) {
                    mViewModel.sessionManager.sendQueryAction(QueryAction.RestartDevice)
                    Handler(Looper.getMainLooper()).post {
                        navigateUpSafe()
                    }
                }
                if (ringNotConnected) {
                    navigate(R.id.unpairDeviceNotConnectedFragment, Bundle().apply {
                        this.putString("title", "Soft reset failed")
                        this.putString(
                            "message",
                            "Ring not connected to Luna App. Please try again later."
                        )
                    })
                }
            }
            navigate(R.id.restartBottomDialogFragment)
        }
        binding.tvFactoryReset.setOnClickListener {
            setFragmentResultListener(UNPAIR_REQUEST_KEY) { _, bundle ->
                val unpairDevice = bundle.getBoolean("unpair")
                val ringNotConnected = bundle.getBoolean("ring_not_connected")
                if (unpairDevice) {
                    Handler(Looper.getMainLooper()).post {
                        navigateUpSafe()
                    }
                }
                if (ringNotConnected) {
                    navigate(R.id.unpairDeviceNotConnectedFragment, Bundle().apply {
                        this.putString("title", "Ring Unpair Failed")
                        this.putString(
                            "message",
                            "Ring not connected to Luna App. Please try again."
                        )
                    })
                }
            }
            mViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_MYDEVICES_UNPAIR_CLICK)

            navigate(R.id.unpairBottomDialogFragment)
        }
        binding.tvPairNew.setOnClickListener {
            setFragmentResultListener(UNPAIR_REQUEST_KEY) { _, bundle ->
                val unpairDevice = bundle.getBoolean("unpair")
                val ringNotConnected = bundle.getBoolean("ring_not_connected")
                if (unpairDevice) {
                    Handler(Looper.getMainLooper()).post {
                        navigateUpSafe()
                    }
                }
                if (ringNotConnected) {
                    navigate(R.id.unpairDeviceNotConnectedFragment, Bundle().apply {
                        this.putString("title", "Ring Unpair Failed")
                        this.putString(
                            "message",
                            "Ring not connected to Luna App. Please try again."
                        )
                    })
                }
            }
            mViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_MYDEVICES_UNPAIR_CLICK)

            navigate(R.id.unpairBottomDialogFragment,Bundle().apply {
                this.putBoolean("forceUnpair",true)
            })
        }

    }

    override fun subscribeObservers() {


    }


}