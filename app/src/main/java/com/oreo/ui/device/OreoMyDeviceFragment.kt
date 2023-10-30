package com.oreo.ui.device

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOreoMyDeviceBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.SplashActivity
import com.noisefit.ui.myDevice.REST_REQUEST_KEY
import com.noisefit.ui.myDevice.UNPAIR_REQUEST_KEY
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
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
import com.noisefit_commans.utils.FirebaseLunaAppEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.share.ShareUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OreoMyDeviceFragment :
    BaseFragment<FragmentOreoMyDeviceBinding>(FragmentOreoMyDeviceBinding::inflate) {
    private val mViewModel: OMyDeviceViewModel by viewModels()
    private val mainViewModel: OreoMainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.features = mViewModel.ringDataStore.getDeviceFeatures()
        //binding.features = DeviceFeatures(shareLogs = 1)

        binding.lifecycleOwner = this
    }

    override fun initListener() {
        binding.rowSettings.setOnClickListener {
            navigate(R.id.deviceSettingsFragment)
        }

        binding.rowAppLogs.setOnClickListener {
            if (mViewModel.appLogFile?.exists() == true) {
                context?.let { ctx ->
                    ShareUtil.shareFile(ctx, AppLogs.getFileUri(ctx))
                }
            } else {
                context.showShortToast("No logs")
            }
            mViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_MYDEVICES_SHARE + "_APP_LOGS_CLICK")
        }

        binding.rowShareRingLogs.setOnClickListener {
            if (mViewModel.watchLogFile?.exists() == true) {
                context?.let { ctx ->
                    ShareUtil.shareFile(ctx, FileLogsUtils.getFileUri(ctx))
                }
            } else {
                context.showShortToast("No logs")
            }
            mViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_MYDEVICES_SHARE + "_RING_LOGS_CLICK")
        }

        binding.btnSoftReset.setOnClickListener {
            setFragmentResultListener(REST_REQUEST_KEY) { _, bundle ->
                val reset = bundle.getBoolean("reset")
                val ringNotConnected = bundle.getBoolean("ring_not_connected")
                if (reset) {
                    mViewModel.sessionManager.sendQueryAction(QueryAction.RestartDevice)
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


        binding.lytPairYourDeviceHeader.btnPairDevice.setOnClickListener {
            startActivity(PairDeviceActivity.getStartIntent(requireContext(), true))
            //activity?.finish()
        }

        binding.rowAboutDevice.setOnClickListener {
            mViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_MYDEVICES_ABOUT_CLICK)
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
                val ringNotConnected = bundle.getBoolean("ring_not_connected")
                if (unpairDevice) {
                    showUnPairDialog()
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
                    //btnUnpair.visible()
                    //btnReset.visible()
                    lytPairYourDeviceHeader.root.gone()
                    lytFeatures.visible()
                }

            } else {
                binding.apply {
                    lytDeviceConnected.root.gone()
                    //btnUnpair.gone()
                    //btnReset.gone()

                    lytPairYourDeviceHeader.root.visible()
                    lytFeatures.gone()
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
                    getBatteryInfo()
                    mainViewModel.onRingConnected()

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

    private fun getBatteryInfo() {
        GlobalScope.launch(Dispatchers.IO) {
            context?.let {
                val isWorkerRunning = ApplicationUtils.isOreoSyncDataWorkerRunning(it)
                LOGS.w("getBatteryInfo isOreoSyncDataWorkerRunning $isWorkerRunning")
                if (isWorkerRunning) {
                    return@launch
                }

                mViewModel.sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
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
                    R.drawable.ic_ring_default_new
                )
                tvRingName.text = noiseFitDevice?.bluetoothName
                tvBattery.setTextColor(resources.getColor(R.color.oreo_contributor_warning))
                tvBattery.text = "Trying to connect..."
                tvOtherInfo.gone()
            }
        }

        //binding.lytFeatures.gone()
    }

    private fun setStateBtOff(noiseFitDevice: ColorFitDevice?) {
        binding.lytDeviceConnected.apply {
            batteryStatus.gone()
            ivRingImage.loadImage(
                requireContext(),
                R.drawable.ic_ring_bluetooth_off_40,
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

            ivRingImage.loadImage(
                requireContext(),
                R.drawable.ic_ring_default_new
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