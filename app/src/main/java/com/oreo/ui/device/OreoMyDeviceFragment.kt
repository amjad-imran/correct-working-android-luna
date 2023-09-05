package com.oreo.ui.device

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOreoMyDeviceBinding
import com.noisefit.ui.SplashActivity
import com.noisefit.ui.myDevice.UNPAIR_REQUEST_KEY
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.DeviceFeatures
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.loadWatchImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.FileLogsUtils
import com.noisefit_commans.utils.share.ShareUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OreoMyDeviceFragment :
    BaseFragment<FragmentOreoMyDeviceBinding>(FragmentOreoMyDeviceBinding::inflate) {
    private val mViewModel: OMyDeviceViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.features = mViewModel.ringDataStore.getDeviceFeatures()
        //binding.features = DeviceFeatures(shareLogs = 1)

        binding.lifecycleOwner = this
    }

    override fun initListener() {

        binding.rowAppLogs.setOnClickListener {
            if (mViewModel.appLogFile?.exists() == true) {
                context?.let { ctx ->
                    ShareUtil.shareFile(ctx, AppLogs.getFileUri(ctx))
                }
            } else {
                context.showShortToast("No logs")
            }
        }

        binding.rowShareRingLogs.setOnClickListener {
            if (mViewModel.watchLogFile?.exists() == true) {
                context?.let { ctx ->
                    ShareUtil.shareFile(ctx, FileLogsUtils.getFileUri(ctx))
                }
            } else {
                context.showShortToast("No logs")
            }
        }

        binding.btnReset.setOnClickListener {
            mViewModel.sessionManager.sendQueryAction(QueryAction.ResetTrigger)
        }


        binding.lytPairYourDeviceHeader.btnPairDevice.setOnClickListener {
            startActivity(PairDeviceActivity.getStartIntent(requireContext(), true))
            //activity?.finish()
        }

        binding.rowAboutDevice.setOnClickListener {
            navigate(R.id.OAboutDeviceFragment)
        }
        binding.rowGoogleFit.setOnClickListener {
            navigate(R.id.googleFitFragmentOreo)
        }

        binding.rowWarrantyRegistration.setOnClickListener {
            //navigate(R.id.warrantyFragmentOreo)
        }
        binding.btnUnpair.setOnClickListener {
            setFragmentResultListener(UNPAIR_REQUEST_KEY) { _, bundle ->
                val unpairDevice = bundle.getBoolean("unpair")
                val forceUnpair = bundle.getBoolean("force_unpair")
                if (unpairDevice) {
                    showUnPairDialog()
                }
                if (forceUnpair) {
                    showForceUnPairDialog()
                }
            }

            navigate(R.id.unpairBottomDialogFragment)

        }
    }

    override fun subscribeObservers() {

        mViewModel.sessionManager.isRingCharging.observe(this) {
            if (mViewModel.sessionManager.connectStateRing.value is ConnectState.ConnectSuccess) {
                setStateConnected((mViewModel.sessionManager.connectStateRing.value as ConnectState.ConnectSuccess).noiseFitDevice)
            }

        }


        mViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        mViewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        mViewModel.startWatchFlow.observe(this) {
            it.getContent()?.let {
                activity?.let { act ->
                    startActivity(SplashActivity.getStartIntent(act))
                    act.finish()
                }
            }
        }

        mViewModel.deviceConnected.observe(this) { connected ->
            if (connected) {
                binding.apply {
                    lytDeviceConnected.root.visible()
                    btnUnpair.visible()
                    //btnReset.visible()
                    lytPairYourDeviceHeader.root.gone()
                }

            } else {
                binding.apply {
                    lytDeviceConnected.root.gone()
                    btnUnpair.gone()
                    //btnReset.gone()

                    lytPairYourDeviceHeader.root.visible()
                }
            }
        }

        mViewModel.sessionManager.connectStateRing.observe(this) { connectedState ->
            when (connectedState) {
                is ConnectState.ConnectFailed -> {
                    setStateConnecting(connectedState.noiseFitDevice)
                }

                is ConnectState.Connecting -> {
                    setStateConnecting(connectedState.noiseFitDevice)
                }

                is ConnectState.ConnectSuccess -> {
                    setStateConnected(connectedState.noiseFitDevice)
                    mViewModel.sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
                }

                is ConnectState.UnPaired -> {
                    binding.lytFeatures.gone()
                    binding.progressBar.root.gone()
                    mViewModel.updateDeviceConnectedStatus()


                    /* val hasWatchDevice = mViewModel.localDataStore.getConnectedDevice()
                     if (hasWatchDevice != null) {
                         mViewModel.startWatchFlow.postValue(Event(true))
                     }*/
                }

                else -> {}
            }

        }
    }

    private fun startWatchService() {
        context?.let {
            ApplicationUtils.setRescueWorkManager(it)
        }
    }

    private fun showForceUnPairDialog() {
        val messageBuilder =
            StringBuilder("Manually reset the device to connect again. Press unpair to continue")

        uiController.onApiErrorReceived(
            ErrorResponse(
                UIComponentType.AreYouSureDialog(getString(R.string.text_alert),
                    messageBuilder.toString(),
                    false,
                    getString(R.string.text_unpair),
                    object : BinaryActionCallback {
                        override fun yes() {

                            mViewModel.sessionManager.forceDisconnect.value = (Event(true))
                            mViewModel.sessionManager.setConnectStateRing(ConnectState.UnPaired())

                        }

                        override fun no() {

                        }
                    })
            )
        )
    }

    private fun showUnPairDialog() {

        if (mViewModel.ringDataStore.getRingDevice() != null) {
            mViewModel.ringDataStore.getRingDevice()?.let {
                mViewModel.connectionHandler.getConnectionActions(it)?.disconnect(it)
            }

            if (isAdded) {
                binding.progressBar.root.visible()
            }
        }
    }


    private fun setStateConnecting(noiseFitDevice: ColorFitDevice?) {
        if (mViewModel.sessionManager.bluetoothStateDash.value == false) {
            setStateBtOff(noiseFitDevice)
        } else {
            binding.lytDeviceConnected.apply {
                batteryStatus.gone()

                ivRingImage.loadImage(
                    requireContext(),
                    R.drawable.ic_ring_default_silver
                )
                tvRingName.text = noiseFitDevice?.bluetoothName
                tvBattery.setTextColor(resources.getColor(R.color.oreo_contributor_warning))
                tvBattery.text = "Trying to connect..."
                tvOtherInfo.gone()
            }
        }

        binding.lytFeatures.gone()
    }

    private fun setStateBtOff(noiseFitDevice: ColorFitDevice?) {
        binding.lytDeviceConnected.apply {
            batteryStatus.gone()
            ivRingImage.loadImage(
                requireContext(),
                R.drawable.ic_luna_state_bt_off,
            )
            tvBattery.setTextColor(resources.getColor(R.color.oreo_contributor_warning))
            tvRingName.text = noiseFitDevice?.bluetoothName
            tvBattery.text = "Make sure your bluetooth is on..."
            tvOtherInfo.gone()
        }
    }

    private fun setStateConnected(noiseFitDevice: ColorFitDevice) {
        val batteryPercent = mViewModel.watchDataStore.getBatteryPercentRing()

        val lastSync =
            mViewModel.sessionManager.getLastSyncTime()?.let { DateFormats.getRelativeTime(it) }
        val lastSyncText = "Synced : ${lastSync ?: getString(R.string.text_not_yet_syncyed)}"
        binding.lytDeviceConnected.apply {

            ivRingImage.loadWatchImage(
                requireContext(),
                noiseFitDevice.ringInfo?.image ?: "",
                R.drawable.ic_ring_default_silver
            )
            tvRingName.text = noiseFitDevice.bluetoothName

            batteryStatus.visible()
            batteryStatus.progress = batteryPercent

            if (batteryPercent <= 20) {
                batteryStatus.setIndicatorColor(resources.getColor(R.color.oreo_contributor_warning))
                tvBattery.setTextColor(resources.getColor(R.color.oreo_contributor_warning))
            } else {
                batteryStatus.setIndicatorColor(resources.getColor(R.color.white))
                tvBattery.setTextColor(resources.getColor(R.color.white_64))
            }

            tvBattery.text = "$batteryPercent%"

            tvOtherInfo.visible()

            if (mViewModel.sessionManager.isRingCharging.value == true) {
                tvOtherInfo.text = " | Charging"
            } else {
                tvOtherInfo.text = " | $lastSyncText"
            }


        }

        binding.apply {
            lytFeatures.visible()
        }
    }

}