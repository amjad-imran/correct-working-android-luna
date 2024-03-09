package com.noisefit.ui.onboarding.setup.firmware

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentFirmwareUpdateBinding
import com.noisefit.ui.onboarding.setup.DeviceSetupSharedViewModel
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.WatchUpdateStatus
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch


class FirmwareUpdateFragment :
    BaseFragment<FragmentFirmwareUpdateBinding>(FragmentFirmwareUpdateBinding::inflate) {

    private val viewModel: DeviceSetupSharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val device = viewModel.getConnectedDevice()
        device?.let {
            binding.ivRingImage.loadImage(binding.ivRingImage.context, it.ringInfo?.image)
        }
        startUpdate()

    }

    override fun initListener() {

    }

    override fun subscribeObservers() {
        viewModel.firmwareDownloadProgress.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                val percent = it / 2
                updateProgress(percent)
            }
        }

        viewModel.updateFirmware.observe(viewLifecycleOwner) {
            it.getContent()?.let { file ->
                val fileUri = Uri.fromFile(file).toString()
                viewModel.localFilePath = fileUri
                viewModel.sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.UpdateFirmware(
                        fileUri
                    )
                )
                updateProgress(50)
                binding.tvTitle.text = getString(R.string.text_updating_ring)
                binding.tvUpdating.text = getString(R.string.text_updating_ring)
            }
        }

        viewModel.sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) { event ->
            event.getContent()?.let {
                if (it is UpdateDeviceDataCallback.FirmwareUpgradeProgress) {
                    updateFirmwareStatus(it.watchUpdateStatus)
                }
            }
        }

    }

    private fun startUpdate() {
        tryCatch {

            val isConnected = viewModel.checkIfConnected()
            if (!isConnected) {
                updateProgress(0)
                showFailedDialog()
                return@tryCatch
            }

            if ((viewModel.sessionManager.batteryPercentRing.value
                    ?: 0) <= viewModel.getMinBatteryPercent()
            ) {
                updateProgress(0)
                showBatteryWarning()
                return@tryCatch
            }


            if (viewModel.updateOtaData == null) return@tryCatch

            val url = viewModel.updateOtaData!!.firmwareUrl


            if (url.isNullOrEmpty()) {
                showFailedDialog()
                return@tryCatch
            }
            val fileName = url.split("/").last()

            updateProgress(0)
            binding.tvTitle.text = getString(R.string.text_downloading_firmware)
            binding.tvUpdating.text = getString(R.string.text_downloading_firmware)

            viewModel.downloadFirmware(
                url,
                requireContext().externalCacheDir!!,
                fileName
            )
        }


    }

    fun updateProgress(progress: Int) {

        binding.tvUpdatePercent.text = "$progress%"

        viewModel.updateProgress2.postValue(
            if (progress > 20) {
                progress
            } else {
                20
            }
        )

        binding.lottieAnimationView.setMinAndMaxProgress(
            progress.toFloat() / 100,
            progress.toFloat() / 100
        )
        binding.lottieAnimationView.playAnimation()
    }

    private fun updateFirmwareStatus(watchUpdateStatus: WatchUpdateStatus) {
        when (watchUpdateStatus.status) {
            UpdateStatus.STARTED, UpdateStatus.PROGRESS -> {
                var percent = 50 + (watchUpdateStatus.percentagePercentage ?: 0) / 2
                if (percent > 100) {
                    percent = 100
                }
                updateProgress(percent)
            }

            UpdateStatus.COMPLETED -> {
                viewModel.clearNewOtaUpdateData()
                viewModel.sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
                viewModel.sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)
                viewModel.deleteTempFile()



                try {
                    Handler(Looper.getMainLooper()).postDelayed({
                        navigate(FirmwareUpdateFragmentDirections.navigateToFirmwareUpdateSuccess())
                    }, 2000)

                } catch (ignored: Exception) {
                }

            }

            UpdateStatus.ERROR -> {
                showFailedDialog()
                viewModel.deleteTempFile()
            }

            UpdateStatus.RETRY -> {
                context.showShortToast("Retrying")
            }

            null -> {}
            else -> {}
        }
    }

    private fun showFailedDialog() {
        setFragmentResultListener(UPDATE_FAILED_ONBOARD) { _, bundle ->
            val tryAgain = bundle.getBoolean("tryAgain")
            if (tryAgain) {
                startUpdate()
            }
        }
        navigate(R.id.bottomSheetUpdateFailedOnboard)
    }

    private fun showBatteryWarning() {
        setFragmentResultListener(LOW_BATTERY_FIRMWARE) { _, bundle ->
            val tryAgain = bundle.getBoolean("tryAgain")
            if (tryAgain) {
                startUpdate()
            }
        }
        navigate(R.id.bottomSheetLowBatteryFirmware)
    }


}