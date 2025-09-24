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
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
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
import com.noisefit_commans.ui.doOnNextLayoutOnce
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.FileLogsUtils
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageAppEventParams
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit_zhsdk.log.ZhBleLogUtils
import com.oreo.util.DateTimeUtil
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
                showPermDetailsDialog()
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
//                    lytDeviceConnected.root.visible()
                    lytDeviceConnectedNew.root.visible()
                    //btnUnpair.visible()
                    //btnReset.visible()
                    lytPairYourDeviceHeader.root.gone()
                    lytFeatures.visible()
                }

            } else {
                binding.apply {
//                    lytDeviceConnected.root.gone()
                    lytDeviceConnectedNew.root.gone()
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

//                    setHeaderUi(connectedState.noiseFitDevice)
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

        mViewModel.sessionManager.isCaseCurrentlyConnected.observe(this) {
            it?.getContent()?.let { res ->
                /*if (res && !mViewModel.localDataStore.getPortableChargerOnboarding()) {
                    navigate(
                        R.id.surgeCaseOnboardingFragment,
                        bundleOf("srcKey" to "oreo_my_device")
                    )
                }*/
                setStateConnected(mViewModel.ringDataStore.getRingDevice())
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

    private fun showPermDetailsDialog() {
        setFragmentResultListener(FIND_RING_LOCATION_PERM_REQUEST) { _, bundle ->
            val allow = bundle.getBoolean("allow")
            if (allow) {
                this@OreoMyDeviceFragment.showLocationPermissionDialog()
            }
        }
        navigate(R.id.bottomSheetLocationPermissionFindMyRing)
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
                UIComponentType.AreYouSureDialog(
                    getString(R.string.text_alert),
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

    /*enum class RingAndCaseState {
        ONLY_RING, RING_CHARGER_INACTIVE, RING_CHARGER_ACTIVE
    }

    private fun setHeaderUi(noiseFitDevice: ColorFitDevice?) {
        val state = RingAndCaseState.RING_CHARGER_ACTIVE

        val batteryPercent = mViewModel.watchDataStore.getBatteryPercentRing()

        val lastSync =
            mViewModel.sessionManager.getLastSyncTime()
                ?.let { DateTimeUtil.getRelativeTime(it, mViewModel.resProvider) }
        val lastSyncText = getString(
            R.string.text_synced_space,
            lastSync ?: getString(R.string.text_not_yet_syncyed)
        )

        val caseInfoData = CaseInfoData(
            isOpen = true,
            battLevel = 50,
            serialNumber = "49323484843",
        )

        val caseBatLevel = caseInfoData?.battLevel ?: 0

        when (state) {
            RingAndCaseState.ONLY_RING -> {
                binding.lytDeviceConnectedNew.apply {
                    textView197.text = "Luna Ring"
                    textView198.text = "Gen 1" // TODO add gen 1/2
                    val isSynced = true // TODO get sync data and other conditions
                    if (isSynced) {
                        textView199.text = lastSyncText
                        textView199.visible()
                    } else {
                        textView199.gone()
                    }

                    ivImgCenter.visible()
                    ivImgCenter.loadImage(this.root.context, noiseFitDevice?.ringInfo?.image)

                    lytOnlyRingProgress.apply {
                        this.ivImage.setImageResource(R.drawable.ic_ring_for_perc)
                        this.tvPercVal.text = "$batteryPercent%"
                        this.linearProgressIndicator.progress = batteryPercent
                        root.visible()
                    }

                    lytChargerOffContainer.gone()
                    lytBothActive.gone()

                    root.visible()
                }
            }

            RingAndCaseState.RING_CHARGER_INACTIVE -> {
                binding.lytDeviceConnectedNew.apply {
                    textView197.text = "Luna ring w/ Charging Case"
                    //
                    ivImgRight.visible()
                    ivImgRight.loadImage(this.root.context, noiseFitDevice?.ringInfo?.image)

                    ivImgLeft.visible()
                    ivImgLeft.setImageResource(R.drawable.image_ring_charge)
                    //
                    lytRingProgressChargerOff.apply {
                        this.ivImage.setImageResource(R.drawable.ic_ring_for_perc)
                        this.tvPercVal.text = "$batteryPercent%"
                        this.linearProgressIndicator.progress = batteryPercent
                    }
                    lytChargerOffContainer.visible()
                    lytOnlyRingProgress.root.gone()
                    lytBothActive.gone()

                    //
                    root.visible()
                }
            }

            RingAndCaseState.RING_CHARGER_ACTIVE -> {

                val padding = 4f.dpToPixel()
                binding.lytDeviceConnectedNew.apply {
                    textView197.text = "Luna ring w/ Charging Case"
                    //
                    lytRingProgressBoth.apply {

                        this.tvPercVal.gone()
                        this.linearProgressIndicator.gone()

                        this.root.post {
                            val width = lytBothActive.width
                            val params: ViewGroup.LayoutParams = this.root.layoutParams
                            params.width = ViewGroup.LayoutParams.WRAP_CONTENT//width/2 - padding.toInt()
                            this.root.layoutParams = params
                        }

                        this.ivImage.setImageResource(R.drawable.ic_ring_for_perc)
                        this.tvPercVal.text = "$batteryPercent%"
                        this.linearProgressIndicator.progress = batteryPercent
                        this.linearProgressIndicator.setIndicatorColor(
                            getIndicatorColor(
                                batteryPercent
                            )
                        )
                    }

                    lytRingCaseProgressBoth.apply {

                        this.root.post {
                            val width = lytBothActive.width
                            val params: ViewGroup.LayoutParams = this.root.layoutParams
                            params.width = width/2 - padding.toInt()
                            this.root.layoutParams = params
                        }

                        this.ivImage.setImageResource(R.drawable.ic_charger_for_perc)
                        this.tvPercVal.text = "$caseBatLevel%"
                        this.linearProgressIndicator.progress = caseBatLevel
                        this.linearProgressIndicator.setIndicatorColor(
                            getIndicatorColor(
                                caseBatLevel
                            )
                        )
                    }
                    lytBothActive.visible()
                    lytChargerOffContainer.gone()
                    lytOnlyRingProgress.root.gone()

                    root.visible()
                }
            }
        }
    }*/

    private fun getIndicatorColor(value: Int, isCharging: Boolean = false): Int {
        return when (value) {
            in 0..20 -> "#CC2929".toColorInt()
            in 21..40 -> if (isCharging) "#29CC74".toColorInt() else "#CC8029".toColorInt()
            else -> if (isCharging) "#29CC74".toColorInt() else "#FFFFFF".toColorInt()
        }
    }

    private fun setStateConnecting(noiseFitDevice: ColorFitDevice?) {
        /*if (mViewModel.sessionManager.bluetoothStateDash.value == false) {
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
                tvBattery.text = getString(R.string.text_trying_to_connect_dot)
                tvOtherInfo.gone()
            }
        }

        //binding.lytFeatures.gone()*/
        //---
        val context = binding.root.context
        val ringGen = getGeneration(mViewModel.ringDataStore.getRingDevice()?.ringInfo?.serialNoRaw)
        val rinCaseData = mViewModel.watchDataStore.getRingCaseData()
        val isChargerOpen = rinCaseData?.isOpen

        binding.lytDeviceConnectedNew.apply {
            //
            tvConnectedTitle.apply {
                text =
                    if (mViewModel.sessionManager.bluetoothStateDash.value == false)
                        getString(R.string.text_make_sure_your_bluetooth_is_on)
                    else
                        getString(R.string.text_trying_to_connect_dot)
                setTextColor("#CB5A5A".toColorInt())
            }

            genContainer.gone()
            tvSyncDesc.gone()
            //

            // Set Charing Progress Layout
            lytRingProgress.tvPercVal.gone()
            lytRingProgress.lPbContainer.gone()
            lytRingProgress.root.post {
                val params = lytRingProgress.root.layoutParams
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                lytRingProgress.root.layoutParams = params
            }
            lytRingProgress.ivImage.setImageResource(R.drawable.ic_ring_for_perc)

            if (rinCaseData != null) {
                lytRingCaseProgress.tvPercVal.gone()
                lytRingCaseProgress.lPbContainer.gone()
                lytRingCaseProgress.root.post {
                    val params = lytRingCaseProgress.root.layoutParams
                    params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    lytRingCaseProgress.root.layoutParams = params
                }
                lytRingCaseProgress.ivImage.setImageResource(R.drawable.ic_portable_charger_closed)
                lytRingCaseProgress.root.visible()
            } else {
                lytRingCaseProgress.root.gone()
            }
            //
            setCenterImage(noiseFitDevice)

            when (isChargerOpen) {
                null -> {
                    tvRingCaseName.text = "Luna ring"
                    tvGen.text = "$ringGen.0"
                    genContainer.visible()
                }

                true -> {
                    tvRingCaseName.text = "Luna ring w/ Charging Case"
                    genContainer.gone()
                }

                false -> {
                    tvRingCaseName.text = "Luna ring w/ Charging Case"
                    genContainer.gone()
                }

            }

        }
    }

    private fun getGeneration(serialNoRaw: String?): Int {
        if (serialNoRaw == null) return 1

        return try {
            serialNoRaw.substring(1, 2).toInt()
        } catch (exp: Exception) {
            exp.printStackTrace()
            1
        }
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
            tvBattery.text = getString(R.string.text_make_sure_your_bluetooth_is_on)
            tvOtherInfo.gone()
        }
    }

    private fun setStateConnected(noiseFitDevice: ColorFitDevice?) {
        val batteryPercent = mViewModel.watchDataStore.getBatteryPercentRing()

        val lastSync =
            mViewModel.sessionManager.getLastSyncTime()
                ?.let { DateTimeUtil.getRelativeTime(it, mViewModel.resProvider) }
        val lastSyncText = if (lastSync != null) {
            getString(
                R.string.text_last_synced_message,
                lastSync
            )
        } else {
            getString(R.string.text_not_yet_syncyed)
        }
        /*binding.lytDeviceConnected.apply {

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
                tvOtherInfo.text = getString(R.string.text_charging_bar)
            } else {
                tvOtherInfo.text = " | $lastSyncText"
            }


        }*/

        binding.apply {
            lytFeatures.visible()
        }

        // ----
        val context = binding.root.context
        val ringGen = getGeneration(mViewModel.ringDataStore.getRingDevice()?.ringInfo?.serialNoRaw)
        val caseInfoData = mViewModel.watchDataStore.getRingCaseData()

        binding.lytDeviceConnectedNew.apply {
            //
            tvConnectedTitle.apply {
                text = getString(R.string.text_connected_to)
                setTextColor("#83AAC6".toColorInt())
            }
            tvSyncDesc.text = lastSyncText
            tvSyncDesc.visible()
            //

            // Set Charging Progress Layout
            lytRingProgress.root.doOnLayout {
                val chargingContainerWidth =
                    resources.displayMetrics.widthPixels - (48 * resources.displayMetrics.density).toInt()
                val params = lytRingProgress.root.layoutParams
                params.width =
                    (chargingContainerWidth * 0.5).toInt() - (16 * resources.displayMetrics.density).toInt()
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                lytRingProgress.root.layoutParams = params

                lytRingProgress.apply {
                    ivImage.setImageResource(R.drawable.ic_ring_for_perc)
                    tvPercVal.text = "$batteryPercent%"
                    linearProgressIndicator.setIndicatorColor(
                        getIndicatorColor(
                            batteryPercent,
                            mViewModel.sessionManager.isRingCharging.value == true
                        )
                    )
                    linearProgressIndicator.progress = batteryPercent
                    tvPercVal.visible()
                    lPbContainer.visible()
                    ivLightening.setVisibilityByCondition(mViewModel.sessionManager.isRingCharging.value == true)
                    root.requestLayout()
                }
            }

            setCenterImage(noiseFitDevice)
            if (ringGen == 1 || caseInfoData == null) {
                tvRingCaseName.text = "Luna ring"
                tvGen.text = "$ringGen.0"
                genContainer.visible()

                lytRingCaseProgress.root.gone()

            } else {
                genContainer.gone()
                lytRingCaseProgress.root.visible()
                if (caseInfoData.battLevel == null/*mViewModel.sessionManager.isRingCharging.value != true*/) {
                    lytRingCaseProgress.root.doOnLayout {
                        val params = lytRingCaseProgress.root.layoutParams
                        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        lytRingCaseProgress.root.layoutParams = params
                        //
                        lytRingCaseProgress.tvPercVal.gone()
                        lytRingCaseProgress.lPbContainer.gone()
                        lytRingCaseProgress.ivImage.setImageResource(R.drawable.ic_portable_charger_closed)
                        lytRingCaseProgress.root.requestLayout()
                    }
                } else {
                    tvRingCaseName.text = "Luna ring w/ Charging Case"

                    //
                    val chargerBattery = caseInfoData.battLevel ?: 0
                    lytRingCaseProgress.root.doOnLayout {
                        val chargingContainerWidth =
                            resources.displayMetrics.widthPixels - (48 * resources.displayMetrics.density).toInt()
                        val params = lytRingCaseProgress.root.layoutParams
                        params.width =
                            (chargingContainerWidth * 0.5).toInt() - (16 * resources.displayMetrics.density).toInt()
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        lytRingCaseProgress.root.layoutParams = params
                        //
                        lytRingCaseProgress.apply {
                            ivImage.setImageResource(R.drawable.ic_charger_for_perc)
                            tvPercVal.text = "$chargerBattery%"
                            linearProgressIndicator.progress = chargerBattery
                            linearProgressIndicator.setIndicatorColor(
                                getIndicatorColor(
                                    chargerBattery,
                                    false
                                )
                            )
                            tvPercVal.visible()
                            lPbContainer.visible()
                            lytRingCaseProgress.root.requestLayout()
                        }
                    }
                }
            }
            //

        }
    }

    private fun setCenterImage(noiseFitDevice: ColorFitDevice?) {

        val context = binding.root.context
        val ringGen = getGeneration(mViewModel.ringDataStore.getRingDevice()?.ringInfo?.serialNoRaw)
        val caseInfoData = mViewModel.watchDataStore.getRingCaseData()

        val displayMetrics = resources.displayMetrics
        val deviceWidth = displayMetrics.widthPixels

        val ringImage = if (noiseFitDevice?.ringInfo?.image3.isNullOrEmpty()) {
            mViewModel.lunarBlackImagesUrl.first
        } else {
            noiseFitDevice.ringInfo?.image3
        }

        val chargerOpenImage = if (noiseFitDevice?.ringInfo?.chargerRingUrl.isNullOrEmpty()) {
            mViewModel.lunarBlackImagesUrl.second
        } else {
            noiseFitDevice.ringInfo?.chargerRingUrl
        }

        val isRingCharing = caseInfoData?.isRingCharging?:false//mViewModel.sessionManager.isRingCharging.value ?: false
        LOGS.d("sjkvbsvsvsj  state $isRingCharing")
        when {
            ringGen == 1 || caseInfoData == null/* || (!isRingCharing && caseInfoData.isOpen!=true)*/ -> {
                binding.lytDeviceConnectedNew.apply {
                    ivImgLeft.gone()
                    ivImgRight.gone()

                    ivImgCenter.post {
                        val params = ivImgCenter.layoutParams
                        params.width = deviceWidth / 2
                        params.height = deviceWidth / 2

                        if (params is ViewGroup.MarginLayoutParams) {
                            val topMarginInPx = (160 * resources.displayMetrics.density).toInt()
                            val bottomMarginInPx = (64 * resources.displayMetrics.density).toInt()
                            params.setMargins(0, topMarginInPx, 0, bottomMarginInPx)
                        }

                        ivImgCenter.layoutParams = params
                        //
                        ivImgCenter.loadImage(context, ringImage)
                        ivImgCenter.visible()
                    }
                }
            }


            !isRingCharing -> {
                binding.lytDeviceConnectedNew.apply {
                    ivImgCenter.gone()
                    ivImgLeft.apply {
                        /*ivImgLeft.post {
                            val params = ivImgLeft.layoutParams
                            params.width = (238 * resources.displayMetrics.density).toInt()
                            params.height = (192 * resources.displayMetrics.density).toInt()

                            if (params is ViewGroup.MarginLayoutParams) {
                                val topMarginInPx = (140 * resources.displayMetrics.density).toInt()
                                val bottomMarginInPx = (20 * resources.displayMetrics.density).toInt()
                                params.setMargins(0, topMarginInPx, 0, bottomMarginInPx)
                            }

                            ivImgLeft.layoutParams = params
                            //
                            ivImgLeft.loadImage(context, chargerOpenImage)
                            ivImgLeft.visible()
                        }*/
                        /*loadImage(context, chargerOpenImage)*/
                        setImageResource(R.drawable.ic_portable_charger_open_main)
                        visible()
                    }
                    ivImgRight.apply {
                        loadImage(context, ringImage)
                        visible()
                    }
                    ivImgCenter.gone()
                }
            }

            isRingCharing -> {
                binding.lytDeviceConnectedNew.apply {
                    ivImgLeft.gone()
                    ivImgRight.gone()
                    if (caseInfoData.isOpen == true) {
                        ivImgCenter.post {
                            val params = ivImgCenter.layoutParams
                            params.width = (deviceWidth * 0.9).toInt()
                            params.height = (400 * resources.displayMetrics.density).toInt()

                            if (params is ViewGroup.MarginLayoutParams) {
                                val topMarginInPx = (12 * resources.displayMetrics.density).toInt()
                                val bottomMarginInPx =
                                    (12 * resources.displayMetrics.density).toInt()
                                params.setMargins(0, topMarginInPx, 0, bottomMarginInPx)
                            }

                            ivImgCenter.layoutParams = params
                            //
                            ivImgCenter.loadImage(context, chargerOpenImage)
                            ivImgCenter.visible()
                        }
                    } else {
                        ivImgCenter.post {
                            val params = ivImgCenter.layoutParams
                            params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                            params.height = (190 * resources.displayMetrics.density).toInt()

                            if (params is ViewGroup.MarginLayoutParams) {
                                val topMarginInPx = (132 * resources.displayMetrics.density).toInt()
                                val bottomMarginInPx =
                                    (32 * resources.displayMetrics.density).toInt()
                                params.setMargins(0, topMarginInPx, 0, bottomMarginInPx)
                            }

                            ivImgCenter.layoutParams = params
                            //
                            ivImgCenter.setImageResource(R.drawable.ic_portable_charger_closed_main)
                            ivImgCenter.visible()
                        }
                    }
                }
            }

        }
    }

}
