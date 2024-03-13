package com.oreo.ui.home.summary.update

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.noisefit.data.model.OtaUpdateModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRingUpdateBinding
import com.noisefit.oreo.OreoMainActivity
import com.noisefit.ui.onboarding.setup.firmware.LOW_BATTERY_FIRMWARE
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
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MiscUtil
import com.noisefit_commans.utils.share.ShareUtil
import com.oreo.ui.helpsupport.questionaries.CALL_REQUEST_KEY
import com.oreo.ui.workout.add.SELECT_REQUEST_KEY
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

        /* var progress = 0
         val timer = object : CountDownTimer(60000L, 1000L) {
             override fun onTick(millisUntilFinished: Long) {
                 updateProgress(progress)
                 progress++
             }

             override fun onFinish() {

             }
         }

         timer.start()*/
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

            val battery = viewModel.sessionManager.batteryPercentRing.value ?: 0

            if (battery != 0) {
                if (battery <= viewModel.getMinBatteryPercent()
                ) {
                    showBatteryWarning()
                    return@tryCatch
                }
            }



            if (viewModel.otaData == null) return@tryCatch

            val url = viewModel.otaData!!.firmwareUrl


            if (url.isNullOrEmpty()) {
                //context.showShortToast("Update Failed")
                showFailedDialog()
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

    private fun showFailedDialog() {
        setFragmentResultListener(UPDATE_FAILED) { _, bundle ->
            val remindLater = bundle.getBoolean("remindLater")
            val tryAgain = bundle.getBoolean("tryAgain")
            if (remindLater) {
                viewModel.ringDataStore.saveOtaRemindDate()
                this@RingUpdateFragment.navigateUpSafe()
            }
            if (tryAgain) {
                startUpdate()
            }
        }
        navigate(R.id.bottomSheetUpdateFailed)
    }

    override fun initListener() {

    }


    override fun subscribeObservers() {

        viewModel.firmwareDownloadProgress.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                val percent = ((it.toFloat() / 100) * 20).toInt()
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
                updateProgress(20)
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

    private fun updateProgress(progress: Int) {
        binding.tvUpdatePercent.text = "$progress%"

        var calculatedProgress = progress
        if (progress == 100) {
            calculatedProgress = 99
        }
        binding.lottieAnimationView.setMinAndMaxProgress(
            calculatedProgress.toFloat() / 100,
            calculatedProgress.toFloat() / 100
        )
        binding.lottieAnimationView.playAnimation()
    }

    private fun updateFirmwareStatus(watchUpdateStatus: WatchUpdateStatus) {
        when (watchUpdateStatus.status) {
            UpdateStatus.STARTED, UpdateStatus.PROGRESS -> {
                val percentToAdd =
                    (((watchUpdateStatus.percentagePercentage ?: 0).toFloat() / 100) * 80).toInt()
                var percent = 20 + percentToAdd
                if (percent > 100) {
                    percent = 100
                }
                updateProgress(percent)
            }

            UpdateStatus.COMPLETED -> {
                viewModel.clearNewOtaUpdateData(onClearSuccess = {
                    viewModel.sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
                    viewModel.sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)
                    viewModel.sessionManager.customSuccessToast.postValue(Event("Ring firmware is up to date"))
                    navigateUpSafe()
                })


            }

            UpdateStatus.ERROR -> {
                //context.showShortToast("Failed")
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