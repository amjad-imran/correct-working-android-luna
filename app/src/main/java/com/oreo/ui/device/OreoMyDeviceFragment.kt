package com.oreo.ui.device

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
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
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.FileLogsUtils
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageAppEventParams
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit_zhsdk.log.ZhBleLogUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OreoMyDeviceFragment :
    BaseFragment<FragmentOreoMyDeviceBinding>(FragmentOreoMyDeviceBinding::inflate),
    OnCompleteListener<LocationSettingsResponse> {
    private val mViewModel: OMyDeviceViewModel by viewModels()
    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private var isActivelyRequestFwLog = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.features = mViewModel.ringDataStore.getDeviceFeatures()
        //binding.features = DeviceFeatures(shareLogs = 1)

        binding.lifecycleOwner = this
    }

    override fun initListener() {

        binding.rowAboutDevice.setUpdateAvailable(mViewModel.ringDataStore.isNewOtaAvailable())

        binding.rowFindMyRing.setOnClickListener {

            if (hasGpsPermission().not()) {
                showLocationPermissionDialog()
            } else {
                if (!isGpsTurnedOn()) {
                    return@setOnClickListener
                }
                navigate(R.id.ringLocationFragment)
            }

        }

        binding.backBtn.setOnClickListener { navigateUpSafe() }
        binding.rowSettings.setOnClickListener {
            mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_mydevices_settings_click)
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
            mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_mydevices_logs_click)
        }

        binding.rowShareRingLogs.setOnClickListener {
            //TODO There is a problem with missing data in this log
            /*if (mViewModel.watchLogFile?.exists() == true) {
                context?.let { ctx ->
                    ShareUtil.shareFile(ctx, FileLogsUtils.getFileUri(ctx))
                }
            } else {
                context.showShortToast("No logs")
            }*/
            mViewModel.viewModelScope.launch(Dispatchers.IO) {
                val data = ZhBleLogUtils.getUriByBleAllLog()
                if (data?.first != null) {
                    context?.let { ctx ->
                        ShareUtil.shareZipFile(ctx, data.first)
                    }
                } else {
                    context.showShortToast("No logs")
                }
            }
            mViewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.luna_mydevices_share_logs_click,
                HashMap<String, Any>().apply {
                    this[MoEngageAppEventParams.star_rating] = "luna"
                })
        }
        binding.rowShareFirmwareLogs.setOnClickListener {
            if (mViewModel.sessionManager.connectStateRing.value is ConnectState.ConnectSuccess) {
                isActivelyRequestFwLog = true
                mViewModel.setLoading(true)
                mViewModel.sessionManager.sendQueryAction(QueryAction.GetFirmwareLogs)
            } else {
                if (mViewModel.firmwareLogFile?.exists() == true) {
                    context?.let { ctx ->
                        ShareUtil.shareFile(
                            ctx,
                            FileLogsUtils.geFirmwareLogsUri(mViewModel.firmwareLogFile!!.path, ctx)
                        )
                    }
                } else {
                    context.showShortToast("No logs")
                }
            }
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
            mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_mydevices_about_click)
            navigate(R.id.OAboutDeviceFragment)
        }
        /*binding.rowGoogleFit.setOnClickListener {
            navigate(R.id.googleFitFragmentOreo)
        }*/

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
            mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_mydevices_unpair_click)

            navigate(R.id.unpairBottomDialogFragment)

        }
    }

    override fun subscribeObservers() {

        mViewModel.sessionManager.isRingCharging.observe(this) {
            if (mViewModel.sessionManager.connectStateRing.value is ConnectState.ConnectSuccess) {
                setStateConnected((mViewModel.sessionManager.connectStateRing.value as ConnectState.ConnectSuccess).noiseFitDevice)
            }
        }

        mViewModel.sessionManager.firmwareLogsStatus.observe(this) { state ->
            if (!isActivelyRequestFwLog) return@observe
            when (state) {
                0/*START*/ -> mViewModel.setLoading(true)

                1/*UPLOADING*/ -> mViewModel.setLoading(true)

                2/*END*/ -> {
                    mViewModel.viewModelScope.launch {
                        delay(1000)
                        mViewModel.setLoading(false)
                        if (mViewModel.firmwareLogFile?.exists() == true) {
                            context?.let { ctx ->
                                ShareUtil.shareFile(
                                    ctx,
                                    FileLogsUtils.geFirmwareLogsUri(
                                        mViewModel.firmwareLogFile!!.path,
                                        ctx
                                    )
                                )
                            }
                        } else {
                            context.showShortToast("No logs")
                        }
                        isActivelyRequestFwLog = false
                    }
                }
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

    fun isGpsTurnedOn(): Boolean {
        if (!ApplicationUtils.isLocationProviderEnabled(requireContext())) {
            val locationRequest: LocationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 10000
            locationRequest.fastestInterval = 5000
            val builder: LocationSettingsRequest.Builder =
                LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)
            val task: Task<LocationSettingsResponse> =
                LocationServices.getSettingsClient(requireActivity())
                    .checkLocationSettings(builder.build())
            task.addOnCompleteListener(this)
            return false
        } else {
            return true
        }
    }

    override fun onComplete(task: Task<LocationSettingsResponse>) {
        try {
            task.getResult(ApiException::class.java)
            //startScan()
        } catch (exception: ApiException) {
            when (exception.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {

                    try {
                        exception.status.resolution?.intentSender?.let {
                            startIntentSenderForResult(
                                it,
                                44,
                                null,
                                0,
                                0,
                                0,
                                null
                            )
                        }
                    } catch (exp: Exception) {
                        //CASE : For handling Fragment not attached to Activity
                    }

                } catch (sendEx: IntentSender.SendIntentException) {
                    LOGS.d("Failed to show dialog")
                } catch (classCast: ClassCastException) {
                }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

                }
            }
        }
    }
    private fun hasGpsPermission(): Boolean {
        val permissionAccessFineLocationApproved =
            (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
                    == PackageManager.PERMISSION_GRANTED)

        val backgroundLocationPermissionApproved =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            } else {
                true
            }

        return permissionAccessFineLocationApproved && backgroundLocationPermissionApproved
    }

    private fun showLocationPermissionDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }

    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        var openSettings = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when {
                permissions.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    false
                ) && permissions.getOrDefault(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    false
                ) -> {
                    LOGS.d("LOCATION_PERM LOCATION GRANTED")
                }

                else -> {
                    openSettings = true
                }
            }
        } else {
            when {
                permissions.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    false
                ) -> {
                    LOGS.d("LOCATION_PERM LOCATION GRANTED")
                }

                else -> {
                    openSettings = true
                }
            }
        }
        if (openSettings) {
            tryCatch {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
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