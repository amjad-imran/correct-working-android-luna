package com.oreo.ui.device

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentDeviceSettingsBinding
import com.noisefit.ui.myDevice.NEW_PAIR_REQUEST_KEY
import com.noisefit.ui.myDevice.REST_REQUEST_KEY
import com.noisefit.ui.myDevice.UNPAIR_REQUEST_KEY
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit_commans.ui.BaseFragment
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
                val restart = bundle.getBoolean("restart")
                val ringNotConnected = bundle.getBoolean("ring_not_connected")
                if (restart) {
                    Handler(Looper.getMainLooper()).post {
                        navigateUpSafe()
                    }
                }
                if (ringNotConnected) {
                    navigate(R.id.unpairDeviceNotConnectedFragment, Bundle().apply {
                        this.putString("title", "Soft reset failed")
                        this.putString(
                            "message",
                            "Your ring is not connected to the app. Please try again."
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
                        mViewModel.localDataStore.setGoogleFitStatus(false)
                        navigateUpSafe()
                    }
                }
                if (ringNotConnected) {
                    navigate(R.id.unpairDeviceNotConnectedFragment, Bundle().apply {
                        this.putString("title", "Ring reset failed")
                        this.putString(
                            "message",
                            "Your ring is not connected to the app. Please try again."
                        )
                    })
                }
            }
            mViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_MYDEVICES_UNPAIR_CLICK)

            navigate(R.id.unpairBottomDialogFragment)
        }
        binding.tvPairNew.setOnClickListener {
            setFragmentResultListener(NEW_PAIR_REQUEST_KEY) { _, bundle ->
                val unpairDevice = bundle.getBoolean("unpair")
                val ringNotConnected = bundle.getBoolean("ring_not_connected")
                if (unpairDevice) {
                    mViewModel.localDataStore.setGoogleFitStatus(false)
                    activity?.startActivity(
                        PairDeviceActivity.getStartIntent(
                            requireContext(),
                            true
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                }
                /*if (ringNotConnected) {
                    navigate(R.id.unpairDeviceNotConnectedFragment, Bundle().apply {
                        this.putString("title", "Ring Reset Failed")
                        this.putString(
                            "message",
                            "Ring not connected to Luna App. Please try again."
                        )
                    })
                }*/
            }
            mViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_MYDEVICES_UNPAIR_CLICK)

            navigate(R.id.unpairBottomDialogFragment, Bundle().apply {
                this.putBoolean("forceUnpair", true)
            })
        }

    }

    override fun subscribeObservers() {


    }


}