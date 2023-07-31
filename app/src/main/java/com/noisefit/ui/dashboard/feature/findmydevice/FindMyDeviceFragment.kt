package com.noisefit.ui.dashboard.feature.findmydevice

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.viewModels
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentFindMyDeviceBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.*
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.watch.SDKWatchType
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.SwitchSetting
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Deferred
import java.util.concurrent.TimeUnit
import javax.inject.Inject


const val REQUEST_COUNT = 5

@AndroidEntryPoint
class FindMyDeviceFragment :
    BaseFragment<FragmentFindMyDeviceBinding>(FragmentFindMyDeviceBinding::inflate) {

    private var fetchDatesTimer: Deferred<Unit>? = null

    private val viewModel: FindMyDeviceViewModel by viewModels()

    @Inject
    lateinit var sessionManager: SessionManager

    var vibrationRequestCount = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.sDKWatchType = viewModel.watchesSDK.getWatchType()

        binding.animateView.repeatCount = 0
        binding.animateView.setAnimation(R.raw.anim_finding_device_default)
        binding.animateView.playAnimation()

        sessionManager.connectedDevice.value?.let {
            binding.ivWatchFace.loadWatchImage(requireContext(), it.url,R.drawable.watch_default)
        }

    }

    override fun initListener() {
        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_find_my_device)
            tvDesc.text =
                getString(R.string.text_your_device_is_virbating)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }

        binding.bSearchStop.setOnClickListener {
            onStopSearchClicked()
        }
        binding.bSearch.setOnClickListener {
            sessionManager.logInsiderAppEvent(
                InsiderAppEvents.FIND_DEVICE_SEARCH_CLICK)
            binding.bSearch.gone()
            binding.bSearchStop.visible()

            viewModel.updateState(VibrationState.Vibrating)
            repeatRequest(true)
        }
    }


    private fun repeatRequest(isStarted: Boolean) {
        if (viewModel.sDKWatchType == SDKWatchType.SDK_NAV_PLUS ||
            viewModel.sDKWatchType == SDKWatchType.SDK_ZH ||
            viewModel.sDKWatchType == SDKWatchType.SDK_QUBE
        ) {

            if (isStarted && fetchDatesTimer == null) {
                fetchDatesTimer = scope
                    .launchPeriodicAsync(TimeUnit.SECONDS.toMillis(6)) {

                        if (vibrationRequestCount < REQUEST_COUNT) {
                            sendVibrationRequest(true)
                            vibrationRequestCount++
                        } else {
                            Handler(Looper.getMainLooper()).post {
                                onStopSearchClicked()
                            }
                        }
                    }
            } else {
                sendVibrationRequest(false)
                fetchDatesTimer?.cancel()
                fetchDatesTimer = null
            }


        } else {
            if (isStarted) {
                sendVibrationRequest(true)
            } else {
                sendVibrationRequest(false)
            }
        }

    }

    private fun onStopSearchClicked() {
        nullableBinding?.bSearch?.visible()
        nullableBinding?.bSearchStop?.gone()
        viewModel.updateState(VibrationState.Off)
        repeatRequest(false)
        vibrationRequestCount = 0
    }


    private fun sendVibrationRequest(status: Boolean) {
        viewModel.getSessionManager()
            .sendUpdateQueryAction(UpdateDeviceAction.FindDevice(SwitchSetting(status = status)))
    }

    private fun onScanStarted() {
        //binding.lScanning.viewRipple.startAnimation()

        binding.animateView.repeatCount = LottieDrawable.INFINITE
        binding.animateView.setAnimation(R.raw.anim_finding_device)
        binding.animateView.playAnimation()

    }

    override fun onDestroy() {
        super.onDestroy()
        fetchDatesTimer?.cancel()
        if (viewModel.vibrationState.value == VibrationState.Vibrating) {
            sendVibrationRequest(false)
        }
    }

    private fun onScanFinished() {
        //binding.lScanning.viewRipple.stopAnimation()

        binding.animateView.repeatCount = 1
        binding.animateView.setAnimation(R.raw.anim_finding_device_default)
        binding.animateView.playAnimation()

    }

    private fun updateButtonText() {
        binding.bSearch.text = getString(viewModel.getButtonText())
    }

    override fun subscribeObservers() {

        viewModel.vibrationState.observe(viewLifecycleOwner) {
            if (it != null) {
                handleUi()
            }
        }

    }

    private fun handleUi() {
        updateButtonText()
        LOGS.d("STATES : ${viewModel.vibrationState.value}")
        when (viewModel.vibrationState.value) {
            VibrationState.Vibrating -> {
                onScanStarted()
            }
            VibrationState.Off -> {
                onScanFinished()
            }
            VibrationState.None -> {
                onScanFinished()
            }
            else -> {
                throw NullPointerException("invalid vibration state")
            }
        }
    }
}
