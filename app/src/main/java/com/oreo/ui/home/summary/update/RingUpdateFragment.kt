package com.oreo.ui.home.summary.update

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRingUpdateBinding
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.WatchUpdateStatus
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RingUpdateFragment :
    BaseFragment<FragmentRingUpdateBinding>(FragmentRingUpdateBinding::inflate) {

    private val viewModel: RingUpdateViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        startUpdate()
    }

    private fun startUpdate() {
        tryCatch {

            val isConnected = viewModel.checkIfConnected()
            if (!isConnected) {
                context.showShortToast("Ring not connected")
                navigateUpSafe()
                return@tryCatch
            }

            if ((viewModel.sessionManager.batteryPercentRing.value
                    ?: 0) <= viewModel.getMinBatteryPercent()
            ) {
                showBatteryWarning()
                return@tryCatch
            }


            val updateInfo =
                viewModel.sessionManager.forceOtaResponseRing
                    ?: return@tryCatch//TODO manage view repository

            val url = updateInfo.url


            if (url.isNullOrEmpty()) {
                context.showShortToast("Update Failed")
                return@tryCatch
            }
            val fileName = url.split("/").last()

            //TODO handle Downloading state
            /*showProgressDialog(
                getString(R.string.text_downloading_firmware),
                getString(R.string.text_downloading_firmware_wait),
                "Downloading..."
            )
            progressBottomSheet?.setProgress(0)*/

            viewModel.downloadFirmware(
                url,
                requireContext().externalCacheDir!!,
                fileName
            )
        }


    }

    override fun initListener() {

    }


    override fun subscribeObservers() {

        viewModel.updateFirmware.observe(viewLifecycleOwner) {
            it.getContent()?.let { file ->
                val fileUri = Uri.fromFile(file).toString()
                viewModel.localFilePath = fileUri
                viewModel.sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.UpdateFirmware(
                        fileUri
                    )
                )

                binding.tvUpdatePercent.text = "0%"
            }
        }

        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let {
                context.showShortToast(it)
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
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

    fun updateProgress(progress: Int) {
        binding.tvUpdatePercent.text = "$progress%"
    }

    private fun updateFirmwareStatus(watchUpdateStatus: WatchUpdateStatus) {
        when (watchUpdateStatus.status) {
            UpdateStatus.STARTED, UpdateStatus.PROGRESS -> {
                updateProgress(watchUpdateStatus.percentagePercentage ?: 0)
            }

            UpdateStatus.COMPLETED -> {
                context.showShortToast("Firmware Updated")


                /*context?.let { viewModel.saveWatchUpdateLogs(it) }
                viewModel.sessionManager.forceOtaFlowRunning = false
                viewModel.sessionManager.forceOtaResponseRing = null
                viewModel.resetPostOnDash()
                progressBottomSheet?.dismiss()
                viewModel.watchDataStore.setLastUpdatedTimeStamp(DateFormats.getTimeStamp())


                viewModel.sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
                viewModel.sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)

                viewModel.deleteTempFile()
                viewModel.setUpdateAvailable(false)
                mShouldFetchInfo = true
                viewModel.mShouldFetchInfo = true*/


            }

            UpdateStatus.ERROR -> {
                context.showShortToast("Failed")
                viewModel.deleteTempFile()
            }

            UpdateStatus.RETRY -> {
                context.showShortToast("Retrying")
            }

            null -> {}
            else -> {}
        }
    }


    private fun showBatteryWarning() {
        val alertMessage =
            getString(R.string.text_battery_low_ring, viewModel.getMinBatteryPercent())
        uiController.onApiErrorReceived(
            ErrorResponse(
                UIComponentType.InfoAlertDialog(
                    getString(
                        R.string.text_ring_battery_low
                    ), alertMessage, getString(R.string.text_got_it)
                )
            )
        )

    }

}