package com.oreo.ui.home.summary.update

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.noisefit.data.model.OtaUpdateModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRingUpdateBinding
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.WatchUpdateStatus
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.loadImageWithCache
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint
import eightbitlab.com.blurview.RenderEffectBlur
import eightbitlab.com.blurview.RenderScriptBlur


@AndroidEntryPoint
class RingUpdateFragment :
    BaseFragment<FragmentRingUpdateBinding>(FragmentRingUpdateBinding::inflate) {

    private val viewModel: RingUpdateViewModel by viewModels()
    private val args: RingUpdateFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.otaData = args.otaData

        setBlur()
        setUiOtaUpdate(args.otaData)

        val device = viewModel.getConnectedDevice()
        device?.let {
            binding.ivRingImage.loadImage(binding.ivRingImage.context, it.ringInfo?.image)
        }

        startUpdate()

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)
    }
    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        }

    private fun setBlur() {
        val radius = 30f
        val decorView = binding.blurView
        val rootView = binding.lytMain
        val windowBackground = decorView.background

        val blurAlgo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            RenderEffectBlur()
        } else {
            RenderScriptBlur(requireContext())
        }
        binding.blurView.setupWith(rootView, blurAlgo) // or RenderEffectBlur
            .setFrameClearDrawable(windowBackground) // Optional
            .setBlurRadius(radius)

    }

    private fun setUiOtaUpdate(otaUpdateModel: OtaUpdateModel) {
        binding.lytBack.apply {
            toolbar.tvTitle.text = getString(R.string.text_update_my_ring)
            tvHeader.text = otaUpdateModel.description?.header
            tvMessage.text = otaUpdateModel.description?.longDescription
            ivBack.loadImageWithCache(ivBack.context, otaUpdateModel.imageUrl)
            btnUpdateNow.gone()
            tvRemindLater.gone()
        }
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


            if (viewModel.otaData == null) return@tryCatch

            val url = viewModel.otaData!!.firmwareUrl


            if (url.isNullOrEmpty()) {
                context.showShortToast("Update Failed")
                return@tryCatch
            }
            val fileName = url.split("/").last()

            updateProgress(0)
            binding.tvUpdating.text = getString(R.string.text_downloading_firmware)

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
                binding.tvUpdating.text = getString(R.string.text_updating_firmware)
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
                var percent = 50 + (watchUpdateStatus.percentagePercentage ?: 0) / 2
                if (percent > 100) {
                    percent = 100
                }
                updateProgress(percent)
            }

            UpdateStatus.COMPLETED -> {
                viewModel.sessionManager.showCustomToast("Ring firmware is up to date")
                viewModel.clearNewOtaUpdateData()
                viewModel.sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
                viewModel.sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)
                viewModel.deleteTempFile()
                navigateUpSafe()
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