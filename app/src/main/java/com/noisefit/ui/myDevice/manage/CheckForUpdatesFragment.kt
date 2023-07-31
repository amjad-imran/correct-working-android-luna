package com.noisefit.ui.myDevice.manage

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCheckForUpdatesBinding
import com.noisefit.ui.common.*
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.*
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CheckForUpdatesFragment :
    BaseFragment<FragmentCheckForUpdatesBinding>(FragmentCheckForUpdatesBinding::inflate) {


    private val viewModel: CheckForUpdatesViewModel by activityViewModels()

    private val args: CheckForUpdatesFragmentArgs by navArgs()
    private var minimumBatteryLevel = 30
    private var mShouldFetchInfo = false

    private val TAG = "CheckForUpdatesFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lytToolbar.tvTitle.text = getString(R.string.text_check_for_updates)
        minimumBatteryLevel = viewModel.watchesSDK.getMinimumBatteryLevel()
        viewModel.sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
        viewModel.sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)
        viewModel.localDataStore.getConnectedDevice()?.let {
            setWatchUi(it)
        } ?: run {
            context.showShortToast(getString(R.string.text_no_device_paired))
            navigateUpSafe()
            return
        }

        if (args.startUpdate) {

            viewModel.setUpdateAvailable(true)

            val isForce = viewModel.sessionManager.forceOtaResponse?.forceUpdate ?: false
            viewModel.forceUpdate = isForce
            if (isForce) {
                binding.progressBar.root.visible()
                getDeviceBatteryInfo()
            } else {
                viewModel.sessionManager.forceOtaResponse?.let { res ->
                    val currentVersion = res.version
                    val ignoredVersion = viewModel.watchDataStore.getWatchIgnoreVersion()
                    LOGS.d("currentVersion $currentVersion ignoredVersion $ignoredVersion")
                    if (currentVersion != ignoredVersion) {
                        binding.progressBar.root.visible()
                        getDeviceBatteryInfo()
                    }
                }
            }
        } else {
            checkForUpdates()
        }

        viewModel.updateAvailable.value?.let {
            val isAvailable = it.peekContent()
            if (isAvailable == true) {
                setUpdateAvailableUi()
            } else {
                setNoUpdateAvailableUi()
            }
        }
    }

    private fun getDeviceBatteryInfo() {
        if (viewModel.sessionManager.batterPercent.value == 0) {

            val observer = Observer<Int> {

                if (it >= minimumBatteryLevel) {
                    binding.progressBar.root.gone()

                    viewModel.sessionManager.forceOtaFlowRunning = true
                    binding.bDownloadAndInstall.performClick()

                    viewModel.sessionManager.batterPercent.removeObservers(viewLifecycleOwner)
                } else if (it in 1 until minimumBatteryLevel) {
                    binding.progressBar.root.gone()
                    showBatteryWarning()
                    viewModel.sessionManager.batterPercent.removeObservers(viewLifecycleOwner)
                }
            }
            viewModel.sessionManager.batterPercent.observe(
                viewLifecycleOwner,
                observer
            )
        } else {
            binding.progressBar.root.gone()
            if (viewModel.sessionManager.batterPercent.value!! < minimumBatteryLevel) {
                showBatteryWarning()
            } else {
                viewModel.sessionManager.forceOtaFlowRunning = true
                binding.bDownloadAndInstall.performClick()
            }
        }
    }

    override fun initListener() {
        binding.bDownloadAndInstall.setOnClickListener {
            if (viewModel.sessionManager.connectState.value is ConnectState.ConnectSuccess || viewModel.sessionManager.connectState.value is ConnectState.DfuMode) {
                if (viewModel.watchesSDK.getWatchType() == SDKWatchType.SDK_QUBE) {
                    viewModel.sessionManager.sendUpdateQueryAction(
                        UpdateDeviceAction.UpdateFirmware(
                            ""
                        )
                    )
                    showProgressDialog(
                        getString(R.string.text_updating_firmware),
                        getString(R.string.text_updating_firmware_message),
                        "Updating..."
                    )
                    progressBottomSheet?.setProgress(0)
                } else {
                    if (viewModel.localDataStore.getConnectedDevice()?.deviceType == DeviceType.COLORFIT_VISION.deviceType) {
                        startVisionDownloadAndUpdate()
                    } else {
                        downloadAndStartUpdate()
                    }
                }
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.MY_DEVICE_WATCH_UPDATE_INSTALL_CLICK)
            } else {
                context.showShortToast("Watch not connected")
            }
        }

        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

        viewModel.sessionManager.connectState.observe(this) {
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


        viewModel.sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            when (it) {
                is QueryCallback.FirmwareVersionObtained -> {
                    binding.tvFirmwareVersion.text =
                        "Firmware version: ${it.deviceFirmware.version}"
                }
                is QueryCallback.FirmwareUpgradeAvailable -> {
                    viewModel.setLoading(false)
                    if (it.deviceFirmware.status.equals("update_available")) {
                        viewModel.setUpdateAvailable(true)
                    } else {
                        viewModel.setUpdateAvailable(false)
                    }

                }
                else -> {}
            }
        }

        viewModel.updateAvailable.observe(viewLifecycleOwner) {
            it.getContent()?.let { isAvailable ->
                if (isAvailable) {
                    setUpdateAvailableUi()
                } else {
                    setNoUpdateAvailableUi()
                }
            }

        }

        viewModel.updateInfo.observe(viewLifecycleOwner) {
            it.getContent()?.let { res ->
                viewModel.sessionManager.forceOtaResponse = res
            }
        }

        viewModel.firmwareDownloadProgress.observe(this) {
            it.getContent()?.let { progress ->
                if (progress == 100) {
                    progressBottomSheet?.dismiss()
                } else {
                    if (progressBottomSheet == null) {
                        showProgressDialog(
                            getString(R.string.text_downloading_firmware),
                            getString(R.string.text_downloading_firmware_wait),
                            "Downloading..."
                        )
                    }
                    progressBottomSheet?.setProgress(progress)
                }
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

                showProgressDialog(
                    getString(R.string.text_updating_firmware),
                    getString(R.string.text_updating_firmware_message),
                    "Updating..."
                )
                progressBottomSheet?.setProgress(0)

            }
        }

        viewModel.visionUpdateFirmware.observe(viewLifecycleOwner) {
            it.getContent()?.let { file ->
                viewModel.sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.VisionUpdateFirmware(
                        file
                    )
                )
                showProgressDialog(
                    getString(R.string.text_updating_firmware),
                    getString(R.string.text_updating_firmware_message),
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

    private var progressBottomSheet: WatchUpdateBottomDialogFragment? = null

    private fun showProgressDialog(title: String, message: String, typeText: String) {
        progressBottomSheet = WatchUpdateBottomDialogFragment.getInstance(title, message, typeText)

        progressBottomSheet?.isCancelable = false
        progressBottomSheet?.show(
            childFragmentManager,
            "DownloadBottomSheet"
        )
    }

    private fun setWatchUi(noiseFitDevice: ColorFitDevice) {
        binding.imgWatch.loadImage(
            requireContext(),
            noiseFitDevice.url
        )
        binding.tvWatchName.text = noiseFitDevice.bluetoothName

        viewModel.watchDataStore.getFirmwareVersion().let {
            if (it.isNotEmpty()) {
                binding.tvFirmwareVersion.text = "Firmware version: $it"
            }
        }
    }


    private fun setUpdateAvailableUi() {
        binding.bDownloadAndInstall.visible()
        binding.textWhatsNew.visible()
        binding.textWhatsNew.text = getString(R.string.text_what_s_new_in_the_update)
        binding.tvUpdateWhatsNew.visible()
        binding.tvUpdateResult.text = getString(R.string.text_new_update_is_here)

        viewModel.sessionManager.forceOtaResponse?.let {
            val updateData = it.descriptionEnglish
            if (!updateData.isNullOrEmpty()) {
                binding.tvUpdateWhatsNew.text = updateData
            } else {
                binding.tvUpdateWhatsNew.text =
                    getString(R.string.text_firmware_update_message)
            }
        }
    }

    private fun setNoUpdateAvailableUi() {
        binding.bDownloadAndInstall.gone()
        binding.textWhatsNew.visible()
        binding.textWhatsNew.text = getString(R.string.text_last_update_log)
        binding.tvUpdateWhatsNew.visible()
        binding.tvUpdateWhatsNew.text = viewModel.getLastUpdateLog(context)
        binding.tvUpdateResult.text = getString(R.string.text_watch_is_up_to_date)
    }

    private fun checkForUpdates() {
        if (viewModel.sessionManager.connectState.value is ConnectState.ConnectSuccess || viewModel.sessionManager.connectState.value is ConnectState.DfuMode) {
            if (viewModel.watchesSDK.getWatchType() == SDKWatchType.SDK_QUBE) {
                viewModel.setLoading(true)
                viewModel.sessionManager.sendQueryAction(QueryAction.QueryFirmwareUpgrade())
            } else {
                if (viewModel.localDataStore.getConnectedDevice()?.deviceType == DeviceType.COLORFIT_VISION.deviceType) {
                    viewModel.checkForUpdatesRecent()
                } else {
                    viewModel.checkForUpdates(false)
                }
            }
        } else {
            context.showShortToast("Watch not connected")
        }
    }

    fun downloadAndStartUpdate() {
        try {
            if ((viewModel.sessionManager.batterPercent.value ?: 0) <= minimumBatteryLevel) {
                showBatteryWarning()
                return
            }

            val updateInfo =
                viewModel.sessionManager.forceOtaResponse ?: return


            val url = when (viewModel.watchesSDK.getWatchType()) {
                SDKWatchType.SDK_EVOLVE -> {
                    if (viewModel.sessionManager.needDfuUpdate.value?.peekContent() == true) {
                        updateInfo.touchUrl
                    } else {
                        updateInfo.url
                    }
                }

                SDKWatchType.SDK_RYEEX -> {
                    if (WatchInfoGlobals.firmwareFullRequired) {
                        updateInfo.touchUrl
                    } else {
                        updateInfo.url
                    }
                }
                else -> {
                    updateInfo.url
                }
            }


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

    fun startVisionDownloadAndUpdate() {
        try {
            val updateInfo =
                viewModel.sessionManager.forceOtaResponse ?: return

            val visionOtaFiles = ArrayList<VisionOtaFiles>()
            if (!updateInfo.hrUrl.isNullOrEmpty()) {
                val fileName = updateInfo.hrUrl!!.split("/").last()
                visionOtaFiles.add(
                    VisionOtaFiles(
                        updateInfo.hrUrl!!, requireContext().externalCacheDir!!, fileName,
                        VisionOtaFileTypes.Heart
                    )
                )
            }
            if (!updateInfo.touchUrl.isNullOrEmpty()) {
                val fileName = "touchpanel" + updateInfo.touchUrl.split("/").last()
                visionOtaFiles.add(
                    VisionOtaFiles(
                        updateInfo.touchUrl, requireContext().externalCacheDir!!, fileName,
                        VisionOtaFileTypes.Touch
                    )
                )
            }
            if (!updateInfo.imageUrl.isNullOrEmpty()) {
                val fileName = updateInfo.imageUrl!!.split("/").last()
                visionOtaFiles.add(
                    VisionOtaFiles(
                        updateInfo.imageUrl!!, requireContext().externalCacheDir!!, fileName,
                        VisionOtaFileTypes.Image
                    )
                )
            }
            if (!updateInfo.url.isNullOrEmpty()) {
                val fileName = "apollo" + updateInfo.url.split("/").last()
                visionOtaFiles.add(
                    VisionOtaFiles(
                        updateInfo.url, requireContext().externalCacheDir!!, fileName,
                        VisionOtaFileTypes.Firmware
                    )
                )
            }

            showProgressDialog(
                getString(R.string.text_downloading_firmware),
                getString(R.string.text_downloading_firmware_wait),
                "Downloading..."
            )
            progressBottomSheet?.setProgress(0)

            viewModel.downloadVisionFirmware(
                visionOtaFiles
            )
        } catch (e: Exception) {
        }
    }

    private fun showBatteryWarning() {
        val alertMessage = getString(R.string.text_battery_low_watchface, minimumBatteryLevel)
        uiController.onApiErrorReceived(
            ErrorResponse(
                UIComponentType.InfoAlertDialog(
                    getString(
                        R.string.text_watch_battery_low
                    ), alertMessage, getString(R.string.text_got_it)
                )
            )
        )

    }


    private fun updateFirmwareStatus(watchUpdateStatus: WatchUpdateStatus) {
        when (watchUpdateStatus.status) {
            UpdateStatus.STARTED, UpdateStatus.PROGRESS -> {
                progressBottomSheet?.setProgress(watchUpdateStatus.percentagePercentage ?: 0)
            }
            UpdateStatus.COMPLETED -> {
                context?.let { viewModel.saveWatchUpdateLogs(it) }
                viewModel.sessionManager.forceOtaFlowRunning = false
                viewModel.sessionManager.forceOtaResponse = null
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