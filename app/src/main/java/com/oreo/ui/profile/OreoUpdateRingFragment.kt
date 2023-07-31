package com.oreo.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOreoUpdateRingBinding
import com.noisefit.ui.myDevice.manage.CheckForUpdatesViewModel
import com.noisefit.ui.myDevice.manage.WatchUpdateBottomDialogFragment
import com.noisefit.watch.SDKWatchType
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.WatchUpdateStatus
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OreoUpdateRingFragment :
    BaseFragment<FragmentOreoUpdateRingBinding>(FragmentOreoUpdateRingBinding::inflate) {

    private val viewModel: CheckForUpdatesViewModel by activityViewModels()
    private var minimumBatteryLevel = 30
    private var mShouldFetchInfo = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        minimumBatteryLevel = viewModel.watchesSDK.getMinimumBatteryLevel()

        viewModel.sessionManager.forceOtaResponseRing?.let {
            binding.toolbar.tvTitle.text = "Version ${it.version}"
            binding.tvUpdateMessage.text = it.descriptionEnglish
        }


    }

    override fun initListener() {

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.btnDownload.setOnClickListener {
            if (viewModel.sessionManager.connectStateRing.value is ConnectState.ConnectSuccess
                || viewModel.sessionManager.connectStateRing.value is ConnectState.DfuMode
            ) {
                downloadAndStartUpdate()
            } else {
                context.showShortToast("Ring not connected")
            }
        }

    }

    private fun downloadAndStartUpdate() {
        try {
            if ((viewModel.sessionManager.batteryPercentRing.value ?: 0) <= minimumBatteryLevel) {
                showBatteryWarning()
                return
            }

            val updateInfo =
                viewModel.sessionManager.forceOtaResponseRing ?: return

            val url = updateInfo.url


            if (url.isEmpty()) {
                context.showShortToast("Update Failed")
                return
            }
            val fileName = url.split("/").last()
            showProgressDialog(
                getString(R.string.text_downloading_firmware),
                getString(R.string.text_downloading_firmware_wait),
                "Downloading..."
            )
            progressBottomSheet?.setProgress(0)

            viewModel.downloadFirmware(
                url,
                requireContext().externalCacheDir!!,
                fileName
            )
        } catch (e: Exception) {
        }
    }

    private var progressBottomSheet: WatchUpdateBottomDialogFragment? = null

    private fun showProgressDialog(title: String, message: String, typeText: String) {
        progressBottomSheet = WatchUpdateBottomDialogFragment.getInstance(title, message, typeText)

        progressBottomSheet?.isCancelable = false
        progressBottomSheet?.show(
            childFragmentManager,
            "DownloadBottomSheet"
        )
    }

    private fun showBatteryWarning() {
        val alertMessage = getString(R.string.text_battery_low_ring, minimumBatteryLevel)
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


    override fun subscribeObservers() {

        viewModel.updateAvailable.observe(this) {
            it.getContent()?.let {
                navigateUpSafe()
            }
        }

        viewModel.sessionManager.connectStateRing.observe(this) {
            when (it) {
                is ConnectState.ConnectSuccess -> {
                    if (mShouldFetchInfo) {
                        viewModel.sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
                        viewModel.sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)
                        mShouldFetchInfo = false
                    }
                }

                else -> {}
            }
        }

        viewModel.updateFirmware.observe(this) {
            it.getContent()?.let { file ->
                val fileUri = Uri.fromFile(file).toString()
                viewModel.localFilePath = fileUri
                viewModel.sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.UpdateFirmware(
                        fileUri
                    )
                )

                progressBottomSheet?.dismiss()
                showProgressDialog(
                    getString(R.string.text_updating_ring_fw),
                    getString(R.string.text_updating_firmware_message_oreo),
                    "Updating..."
                )
                progressBottomSheet?.setProgress(0)

            }
        }

        viewModel.sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) { event ->
            event.getContent()?.let {
                if (it is UpdateDeviceDataCallback.FirmwareUpgradeProgress) {
                    updateFirmwareStatus(it.watchUpdateStatus)
                }
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

    }

    private fun updateFirmwareStatus(watchUpdateStatus: WatchUpdateStatus) {
        when (watchUpdateStatus.status) {
            UpdateStatus.STARTED, UpdateStatus.PROGRESS -> {
                progressBottomSheet?.setProgress(watchUpdateStatus.percentagePercentage ?: 0)
            }

            UpdateStatus.COMPLETED -> {
                context?.let { viewModel.saveWatchUpdateLogs(it) }
                viewModel.sessionManager.forceOtaFlowRunning = false
                viewModel.sessionManager.forceOtaResponseRing = null
                viewModel.resetPostOnDash()
                progressBottomSheet?.dismiss()
                context.showShortToast("Firmware Updated")
                viewModel.watchDataStore.setLastUpdatedTimeStamp(DateFormats.getTimeStamp())


                viewModel.sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
                viewModel.sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)

                viewModel.deleteTempFile()
                viewModel.setUpdateAvailable(false)
                mShouldFetchInfo = true


            }

            UpdateStatus.ERROR -> {
                progressBottomSheet?.dismiss()
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


}