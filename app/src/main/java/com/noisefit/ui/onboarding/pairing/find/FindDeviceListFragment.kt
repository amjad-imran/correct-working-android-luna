package com.noisefit.ui.onboarding.pairing.find

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieDrawable
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.noisefit.data.repository.abstraction.IBluetoothScan
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit.luna.databinding.DialogUnsupportedDeviceBinding
import com.noisefit.luna.databinding.FragmentFindDeviceListBinding
import com.noisefit.oreo.OreoMainActivity
import com.noisefit.ui.onboarding.onboardProfile.ProfileSetupActivity
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit.ui.onboarding.pairing.pair.NearbyDevicesAdapter
import com.noisefit.ui.onboarding.pairing.pair.NearbyDevicesClickListener
import com.noisefit.util.ApplicationUtils
import com.noisefit.util.DeviceUtil
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.*
import com.noisefit_commans.utils.bleUtils.CRPScanRecordParser
import com.noisefit_commans.utils.bleUtils.DeviceEntity
import com.noisefit_commans.utils.share.ShareUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

//private const val SELECT_DEVICE_REQUEST_CODE = 1230


@AndroidEntryPoint
class FindDeviceListFragment :
    BaseFragment<FragmentFindDeviceListBinding>(FragmentFindDeviceListBinding::inflate),
    IBluetoothScan, OnCompleteListener<LocationSettingsResponse> {
    private val REQUEST_ENABLE_BT = 146

    private lateinit var devicesAdapter: NearbyDevicesAdapter

    /*  private val deviceManager: CompanionDeviceManager by lazy {
          requireActivity().getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
      }*/
    private val handler = Handler(Looper.getMainLooper())
    private val viewModel: SearchNearbyDeviceViewModel by activityViewModels()
    private var currentPosition: Int = 0


    private val btAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }
    private var bluetoothLeScanner: BluetoothLeScanner? = null


    @Inject
    lateinit var deviceUtil: DeviceUtil

    private var scanCount = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_device_list)
        binding.lScanning.repeatCount = 0
        binding.lScanning.setAnimation(R.raw.anim_device_default)
        binding.lScanning.playAnimation()

        setVideo()


        viewModel.clearScannedDeviceList()
        setRecycler()
        viewModel.fetchDeviceList()

        /*
        id=7
        title=Ring Bluetooth Scanning Issues
        depend on categories api last item, once changed, value changes required here
         */
        binding.tvTroubleShoot.setOnClickListener {
            navigate(R.id.oreoHSQuestionFragment, Bundle().apply {
                putString("title", "Get Started")
                putString("id", "1")
            })
        }

        if (PairDeviceActivity.showBack) {
            binding.backBtn.visible()
        } else {
            binding.backBtn.gone()
        }
    }

    private fun setVideo() {
        binding.videoOnboard.apply {
            setVideoURI(
                Uri.parse(
                    "android.resource://" + requireContext().packageName + "/" +
                            R.raw.video_find_ring
                )
            )
            setOnPreparedListener { mp -> mp.isLooping = true }
            start()
        }
    }

    private fun checkPermissionAndScan() {
        checkLocationPermission(permissionGranted = {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkBluetoothPermission {
                    startScan()
                }
            } else {
                startScan()
            }

        })
    }


    /*@RequiresApi(Build.VERSION_CODES.O)
    private fun createAssociation(colorFitDevice: ColorFitDevice) {
        val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
            .setAddress(colorFitDevice.address)
            .build()

        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
            .addDeviceFilter(deviceFilter)
            .setSingleDevice(true)
            .build()

        deviceManager.associate(
            pairingRequest,
            object : CompanionDeviceManager.Callback() {

                override fun onDeviceFound(chooserLauncher: IntentSender) {
                    val bundle = Bundle()
                    bundle.putParcelable("colorfit", colorFitDevice)
                    startIntentSenderForResult(
                        chooserLauncher,
                        SELECT_DEVICE_REQUEST_CODE,
                        null,
                        0,
                        0,
                        0,
                        bundle
                    )

                }

                override fun onFailure(error: CharSequence?) {
                    context.showShortToast(getString(R.string.text_something_went_wrong))
                }
            }, null
        )


//        deviceManager.associate(
//            pairingRequest,
//            executor,
//            object : CompanionDeviceManager.Callback() {
//                // Called when a device is found. Launch the IntentSender so the user
//                // can select the device they want to pair with.
//                override fun onAssociationPending(intentSender: IntentSender) {
//                    Log.d("TAG", "pending")
//                    intentSender?.let {
//                        startIntentSenderForResult(it, SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0)
//                    }
//
//                }
//
//                override fun onAssociationCreated(associationInfo: AssociationInfo) {
//                    // AssociationInfo object is created and get association id and the
//                    // macAddress.
//
//                    val associationId: Int = associationInfo.id
//                    val macAddress: MacAddress? = associationInfo.deviceMacAddress
//                    Log.d("TAG", "associate $associationId $macAddress")
//                }
//
//                override fun onFailure(errorMessage: CharSequence?) {
//                    // Handle the failure.
//                    Log.d("TAG", "failure $errorMessage")
//                }
//            })

    }*/


    private fun checkLocationPermission(
        permissionGranted: () -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            permissionGranted.invoke()
        } else {
            permissionResultListener.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkBluetoothPermission(
        permissionGranted: () -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            permissionGranted.invoke()
        } else {
            bluetoothPermissionResultListener.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        }
    }

    private val bluetoothPermissionResultListener = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (it[Manifest.permission.BLUETOOTH_SCAN] == true && it[Manifest.permission.BLUETOOTH_CONNECT] == true) {
            startScan()
        } else {
            uiController.onApiErrorReceived(
                ErrorResponse(
                    UIComponentType.AreYouSureDialog(
                        getString(R.string.text_permission_required),
                        getString(R.string.text_permission_denial_bluetooth),
                        false,
                        getString(R.string.text_allow),
                        object : BinaryActionCallback {
                            override fun yes() {
                                activity?.let { act ->
                                    ApplicationUtils.openAppSettings(act)
                                }
                            }

                            override fun no() {
                                navigateUpSafe()
                            }

                        }
                    )
                )
            )

        }
    }


    private val permissionResultListener = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (it[Manifest.permission.ACCESS_COARSE_LOCATION] == true && it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            checkPermissionAndScan()
        } else {
            uiController.onApiErrorReceived(
                ErrorResponse(
                    UIComponentType.AreYouSureDialog(
                        getString(R.string.text_permission_required),
                        getString(R.string.text_permission_denial_location),
                        false,
                        getString(R.string.text_allow),
                        object : BinaryActionCallback {
                            override fun yes() {
                                activity?.let { act ->
                                    ApplicationUtils.openAppSettings(act)
                                }
                            }

                            override fun no() {
                                context.showShortToast(getString(R.string.text_permission_denial_location_message))
                                pairLater()
                            }
                        }

                    )
                )
            )
        }
    }


    override fun initListener() {

        binding.ivRefresh.setOnClickListener {
            viewModel.fetchDeviceList()
        }

        binding.backBtn.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_device_back)
            activity?.finish()
        }
        binding.lScanning.setOnClickListener {

            when (viewModel.findDeviceState) {
                FindDeviceState.DEFAULT, FindDeviceState.FINISHED -> {
                    viewModel.fetchDeviceList()
                }

                else -> {}
            }

        }
        /*if (BuildConfig.DEBUG) {
            binding.bPairLater.visible()
        } else
            binding.bPairLater.gone()

        binding.bPairLater.setOnClickListener {
            pairLater()
        }*/

        /*binding.layoutSearchAgain.root.setOnClickListener {
            checkPermissionAndScan()
        }*/


    }

    fun showLocationTurnOnDialogCamera() {
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
    }

    private fun pairLater() {
        viewModel.setPairLaterClicked(true)
        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_later)
        if (viewModel.isProfileSetupComplete()) {
            context?.let {
                ApplicationUtils.setRescueWorkManager(it)
            }

            startActivity(OreoMainActivity.getStartIntent(requireContext()))
            activity?.finish()


        } else {
            startActivity(ProfileSetupActivity.getStartIntent(requireContext()))
            activity?.finish()
        }
    }

    private fun startRingService() {
        context?.let {
            ApplicationUtils.setRescueWorkManager(it)
        }
    }

    override fun subscribeObservers() {
        viewModel.getMessages().observe(this) {
            it?.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getWatchToken().observe(this) {
            it?.getContent()?.let { watchToken ->
                viewModel.tempColorFitDevice?.let { it1 -> moveToPairingScreen(it1, watchToken) }
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) binding.progressBar.root.visible() else binding.progressBar.root.gone()
        }

        viewModel.getStartBluetoothScan().observe(this) {
            it.getContent()?.let { startScan ->
                if (startScan) checkPermissionAndScan()
            }
        }
        viewModel.getScannedDevices().observe(this) {
            if (it.isNullOrEmpty()) return@observe

            binding.tvDevicesFound.text =
                "${it.size} ${resources.getQuantityText(R.plurals.text_device_found, it.size)}"
            devicesAdapter.setDataSet(it)
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }

    private fun setRecycler() {
        devicesAdapter = NearbyDevicesAdapter(object : NearbyDevicesClickListener {
            override fun onDeviceClicked(colorFitDevice: ColorFitDevice) {
                if (!ApplicationUtils.isInternetConnected()) {
                    AppLogs.sendAppLogs(LogEvents.Binding, BindingEvents.NetworkIssue)
                    uiController.onDisplayError(getString(R.string.text_no_internet_connection))
                    return
                }


                if (btAdapter?.isDiscovering == true) {
                    btAdapter?.cancelDiscovery()
                }

                stopLeScanning()

                viewModel.sessionManager.addUserAttributeToInsider(false,
                    HashMap<String, Any>().apply
                    {
                        this["pair_device_method_used"] = "list of nearby devices"
                    })
                /*if (colorFitDevice.isBind) {
                    //show alert
                    val msg = "Watch "+colorFitDevice.bluetoothName+" ("+ colorFitDevice.address+")"+" already connected with some other devices"
                    uiController.onApiErrorReceived(
                        ErrorResponse(
                            UIComponentType.InfoWatchConnectedAlertDialog(
                                "Connected", msg, getString(R.string.text_close)
                            )
                        )
                    )
                } else {
                    startPairing(colorFitDevice)
                }*/

                startPairing(colorFitDevice)


                /*viewModel.selectedColorFitDevice = colorFitDevice
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createAssociation(colorFitDevice)
                } else {
                    startPairing(colorFitDevice)
                }*/
            }
        })

        with(binding.rvDevices) {
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = false
            adapter = devicesAdapter
        }
    }

    private fun startPairing(colorFitDevice: ColorFitDevice) {


        colorFitDevice.userId = viewModel.userId()
        viewModel.tempColorFitDevice = colorFitDevice
        colorFitDevice.address?.let { viewModel.checkWatchTokenExist(it) }

       /* if (findNavController().currentDestination?.id == R.id.findDeviceListFragment) {
            navigate(
                FindDeviceListFragmentDirections.actionFindDeviceListFragmentToPairingFragment(
                    colorFitDevice
                )
            )
        }*/

    }

    private fun moveToPairingScreen(colorFitDevice: ColorFitDevice, watchToken: String) {
        if (findNavController().currentDestination?.id == R.id.findDeviceListFragment) {
            colorFitDevice.watchToken = watchToken
            navigate(
                FindDeviceListFragmentDirections.actionFindDeviceListFragmentToPairingFragment(
                    colorFitDevice
                )
            )
        }
    }

    private fun showOpenPlayStoreDialog(packageName: String, deviceName: String, appIcon: Int) {
        val appName = when {
            packageName.equals(ShareUtil.PACKAGE_EVOLVE, true) -> {
                "NoiseFit Peak"
            }

            packageName.equals(ShareUtil.PACKAGE_TRACK, true) -> {
                "NoiseFit Track"
            }

            packageName.equals(ShareUtil.PACKAGE_ASSIST, true) -> {
                "NoiseFit Assist"
            }

            packageName.equals(ShareUtil.PACKAGE_SYNC, true) -> {
                "NoiseFit Sync"
            }

            packageName.equals(ShareUtil.PACKAGE_PRIME, true) -> {
                "NoiseFit Prime"
            }

            packageName.equals(ShareUtil.PACKAGE_ACE, true) -> {
                "NoiseFit ACE"
            }

            packageName.equals(ShareUtil.PACKAGE_APEX, true) -> {
                "NoiseFit Apex"
            }

            else -> {
                ""
            }
        }

        var alert: androidx.appcompat.app.AlertDialog? = null
        val builder =
            MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_rounded)
        val dialogView: DialogUnsupportedDeviceBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_unsupported_device, null, false
        )
        dialogView.apply {

            ivAppImage.setImageResource(appIcon)

            tvAppName.text = appName

            val userName = viewModel.localDataStore.getUser()?.firstName
            tvDesc.text =
                "Hi ${if (userName.isNullOrEmpty()) "User" else userName}, your smartwatch $deviceName is not compatible with this app. Please download $appName app to proceed with pairing."

            btnAllow.setOnClickListener {
                alert?.dismiss()
                if (isAdded) {
                    ShareUtil.openPlayStore(requireContext(), packageName)
                }
            }
            btnCancel.setOnClickListener {
                alert?.dismiss()
            }
        }
        builder.setView(dialogView.root)
        builder.setCancelable(false)
        alert = builder.create()
        alert.show()
    }

    private fun startScan() {
        if (activity == null) return
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
            return
        }

        if (btAdapter == null) {
            AppLogs.sendAppLogs(LogEvents.Connect, ConnectEvents.BluetoothEnableFailed)
            //TODO Case: Device does not have bluetooth
            return
        }
        if (!btAdapter.isEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkBluetoothPermission(permissionGranted = {
                    enableBluetooth()
                })
            } else {
                enableBluetooth()
            }
            return
        }
        startDiscovery()

    }

    /**
     * Should be called after all
     * required bluetooth permissions are granted
     */
    private fun enableBluetooth() {
        try {
            AppLogs.sendAppLogs(LogEvents.Connect, ConnectEvents.BluetoothEnableFailed)
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            registerForResult.launch(enableBtIntent)
        } catch (e: Exception) {
            context.showShortToast(getString(R.string.bluetooth_turn_on_request))
        }
    }

    private val registerForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            AppLogs.sendAppLogs("Bluetooth ON")
            startDiscovery()
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            AppLogs.sendAppLogs("Bluetooth Off")
            AppLogs.sendAppLogs(LogEvents.Connect, ConnectEvents.BluetoothEnableFailed)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            /*if (resultCode == Activity.RESULT_OK) {
                AppLogs.sendAppLogs("Bluetooth ON")
                startDiscovery()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                AppLogs.sendAppLogs("Bluetooth Off")
                AppLogs.sendAppLogs(LogEvents.Connect, ConnectEvents.BluetoothEnableFailed)
                //TODO Show Dialog Bluetooth Required
            }*/
        } else if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK || ApplicationUtils.isLocationProviderEnabled(
                    requireContext()
                )
            ) {
                startScan()
            } else {
                context.showShortToast("Location Permission required")
                navigateUpSafe()
            }
        }
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK || AppUtil.isLocationProviderEnabled(
                        requireContext()
                    )
                ) {
                    startScan()
                } else {
                    context.showShortToast("Location Permission required")
                    navigateUpSafe()
                }
            }

            SELECT_DEVICE_REQUEST_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {

                    // The user chose to pair the app with a Bluetooth device.
                    val deviceToPair: BluetoothDevice? =
                        data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
                    val colorFitDevice: ColorFitDevice? = data?.extras?.getParcelable("colorfit")
                    startPairing(viewModel.selectedColorFitDevice!!)
                }

                else -> {
                    navigateUpSafe()
                }
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }

    }*/

    private fun startDiscoveryAfterPermission() {
        startLeDiscovery()
    }

    private fun startDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkBluetoothPermission(permissionGranted = {
                viewModel.isBLEScanning = false
                startDiscoveryAfterPermission()
            })
        } else {
            viewModel.isBLEScanning = false
            startDiscoveryAfterPermission()
        }
    }

    private fun stopLeScanning() {
        try {
            LOGS.d("stopLeScanning")
            viewModel.isBLEScanning = false
            onScanFinished()
            if (btAdapter != null && btAdapter.isEnabled) {
                bluetoothLeScanner?.stopScan(leScanCallback)
            }

            viewModel.sessionManager.logInsiderAppEvent(
                InsiderAppEvents.PairingEvents.wn_pair_scanning_stop,
                HashMap<String, Any>().apply {
                    this["count"] = devicesAdapter.itemCount
                })
        } catch (e: Exception) {

        }
    }

    private fun scanLeDevice() {
        if (!viewModel.isBLEScanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                stopLeScanning()
            }, viewModel.scanDeviceTime)
            onScanStarted()
            LOGS.d("startLeScanning")
            viewModel.isBLEScanning = true
            if (bluetoothLeScanner == null) {
                bluetoothLeScanner = btAdapter.bluetoothLeScanner
            }


            bluetoothLeScanner?.startScan(leScanCallback)
        } else {
            LOGS.d("stopLeScanning")
            stopLeScanning()
        }
    }


    private val leScanCallback: ScanCallback = object : ScanCallback() {
        //        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
//            super.onBatchScanResults(results)
////            LOGS.d("watch_list_size ${results?.size}")
//        }
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            if (viewModel.isBLEScanning && result.device != null && !result.device.name.isNullOrEmpty() && !result.device.address.isNullOrEmpty()) {
                val deviceEntity = DeviceEntity()
                deviceEntity.address = result.device?.address
                deviceEntity.name = result.device?.name
                deviceEntity.rssi = result.rssi
                deviceEntity.mDeviceRadioBroadcastBean =
                    DeviceEntity.getScanRecordModel(result.scanRecord)
                CRPScanRecordParser.parseScanRecord(result.scanRecord?.bytes)?.let {
                    deviceEntity.mcuPlatform = it.platform
                }

                onDeviceFound(deviceEntity)
                LOGS.d(
                    "BLEDEVICE_____  ${result.device?.name} ${result.device?.address}" +
                            "  ${result.device?.bondState} ${result.device?.type} ${result.device?.uuids} " +
                            "${result.device?.alias} ${deviceEntity.mDeviceRadioBroadcastBean}"
                )
            }
        }
    }

    private fun startLeDiscovery() {
        scanLeDevice()
    }


    override fun onScanStarted() {
        viewModel.findDeviceState = FindDeviceState.SEARCHING
        binding.ivRefresh.gone()

        if (nullableBinding == null) return

        if (scanCount == 0) {
            binding.lScanning.repeatCount = LottieDrawable.INFINITE
            binding.lScanning.setAnimation(R.raw.anim_device_search)
            binding.lScanning.playAnimation()
        } else {
            binding.lScanning.repeatCount = 0
            binding.lScanning.setAnimation(R.raw.anim_device_refresh_to_search)
            binding.lScanning.playAnimation()

            Handler(Looper.getMainLooper()).postDelayed({
                if (nullableBinding != null) {
                    binding.lScanning.repeatCount = LottieDrawable.INFINITE
                    binding.lScanning.setAnimation(R.raw.anim_device_search)
                    binding.lScanning.playAnimation()
                }
            }, 500)
        }
        scanCount++
    }

    override fun onScanFinished() {
        try {
            binding.ivRefresh.visible()



            viewModel.findDeviceState = FindDeviceState.FINISHED
            binding.lScanning.repeatCount = 0
            binding.lScanning.setAnimation(R.raw.anim_device_search_to_refresh)
            binding.lScanning.playAnimation()

            if (viewModel.getScannedDevices().value.isNullOrEmpty()) {
                binding.tvDevicesFound.text = getString(R.string.text_no_device_found)
            }
            //retryAgainText()
        } catch (ignored: Exception) {
        }

    }

    override fun onDeviceFound(deviceEntity: DeviceEntity) {
        viewModel.onDeviceFound(deviceEntity)
    }

    override fun onPause() {
        super.onPause()
        currentPosition = binding.videoOnboard.currentPosition
        binding.videoOnboard.pause()
    }

    override fun onResume() {
        super.onResume()
        binding.videoOnboard.seekTo(currentPosition)
        binding.videoOnboard.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            if (btAdapter != null && btAdapter.isEnabled) {
                bluetoothLeScanner?.stopScan(leScanCallback)
            }
            handler.removeCallbacksAndMessages(null)
            if (btAdapter.isDiscovering) {
                btAdapter.cancelDiscovery()
            }
        } catch (e: Exception) {
        }
    }

    override fun onComplete(task: Task<LocationSettingsResponse?>) {
        try {
            task.getResult(ApiException::class.java)
            startScan()
        } catch (exception: ApiException) {
            when (exception.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {

                    try {
                        startIntentSenderForResult(
                            exception.status.resolution?.intentSender,
                            REQUEST_CHECK_SETTINGS,
                            null,
                            0,
                            0,
                            0,
                            null
                        )
                    } catch (exp: Exception) {
                        //CASE : For handling Fragment not attached to Activity
                    }


                    //TODO migrate
//                    val resolvable = exception as ResolvableApiException
//                    resolvable.startResolutionForResult(
//                        requireActivity(),
//                        REQUEST_CHECK_SETTINGS
//                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    LOGS.d("Failed to show dialog")
                } catch (classCast: ClassCastException) {
                }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

                }
            }
        }
    }

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 42

    }

}