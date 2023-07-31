package com.noisefit.ui.onboarding.pairing.qr

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.huawei.hms.hmsscankit.RemoteView
import com.huawei.hms.ml.scan.HmsScan
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.repository.abstraction.IBluetoothScan
import com.noisefit.luna.databinding.DialogUnsupportedDeviceBinding
import com.noisefit.luna.databinding.FragmentQrScanBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.onboarding.pairing.find.SearchNearbyDeviceViewModel
import com.noisefit.util.ApplicationUtils
import com.noisefit.util.DeviceUtil
import com.noisefit_commans.utils.ScreenUtils
import com.noisefit_commans.utils.bleUtils.CRPScanRecordInfo
import com.noisefit_commans.utils.bleUtils.CRPScanRecordParser
import com.noisefit_commans.utils.bleUtils.DeviceEntity
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val SELECT_DEVICE_REQUEST_CODE = 1231
private const val TAG = "QrScanFragment"

@AndroidEntryPoint
class QrScanFragment : BaseFragment<FragmentQrScanBinding>(FragmentQrScanBinding::inflate),
    IBluetoothScan {

    private val flashList = intArrayOf(
        R.drawable.baseline_flashlight_on_white_24dp,
        R.drawable.baseline_flashlight_off_white_24dp
    )
    private var mScreenWidth = 0
    private var mScreenHeight = 0
    private val SCAN_FRAME_SIZE = 240
    private val REQUEST_ENABLE_BT = 146

    private var savedInstanceState: Bundle? = null
    private var remoteView: RemoteView? = null

    private val handler = Handler(Looper.getMainLooper())
    private var scannedMac: String? = null
    private val viewModel: SearchNearbyDeviceViewModel by activityViewModels()

    /*  private val deviceManager: CompanionDeviceManager by lazy {
          requireActivity().getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
      }*/
    @Inject
    lateinit var screenUtils: ScreenUtils

    @Inject
    lateinit var deviceUtil: DeviceUtil


    private val btAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private var bluetoothLeScanner: BluetoothLeScanner? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.savedInstanceState = savedInstanceState

        viewModel.deviceFound = false
        viewModel.fetchDeviceList()

    }


    override fun initListener() {
        binding.btnBack.setOnClickListener {
            navigateUpSafe()
        }

        binding.imvFlash.setOnClickListener {
            if (remoteView?.lightStatus == true) {
                remoteView?.switchLight()
                binding.imvFlash.setImageResource(flashList[1])
            } else {
                remoteView?.switchLight()
                binding.imvFlash.setImageResource(flashList[0])
            }
        }
    }

    override fun subscribeObservers() {
        viewModel.getWatchToken().observe(this) {
            it?.getContent()?.let { watchToken ->
                viewModel.tempColorFitDevice?.let { it1 -> moveToPairingScreen(it1, watchToken) }
            }
        }

        viewModel.getDevices().observe(this) {
            checkCameraPermission {
                if (remoteView == null) {
                    initScanner()
                }
            }
        }

    }

    private fun initScanner() {

        //1. Obtain the screen density to calculate the viewfinder's rectangle.
        val dm = resources.displayMetrics
        val density = dm.density

        //2. Obtain the screen size.
        mScreenWidth = resources.displayMetrics.widthPixels
        mScreenHeight = resources.displayMetrics.heightPixels

        val scanFrameSize = (SCAN_FRAME_SIZE * density).toInt()

        //3. Calculate the viewfinder's rectangle, which in the middle of the layout.
        //Set the scanning area. (Optional. Rect can be null. If no settings are specified, it will be located in the middle of the layout.)
        val rect = Rect()
        rect.left = mScreenWidth / 2 - scanFrameSize / 2
        rect.right = mScreenWidth / 2 + scanFrameSize / 2
        rect.top = mScreenHeight / 2 - scanFrameSize / 2
        rect.bottom = mScreenHeight / 2 + scanFrameSize / 2


        //Initialize the RemoteView instance, and set callback for the scanning result.
        remoteView = RemoteView.Builder().setContext(requireActivity())
            .setFormat(HmsScan.ALL_SCAN_TYPE).build()
        // When the light is dim,w this API is called back to display the flashlight switch.
        remoteView?.setOnLightVisibleCallback { visible ->
            if (visible) {
                binding.imvFlash.visible()
            }
        }

        // Subscribe to the scanning result callback event.
        remoteView?.setOnResultCallback { result -> //Check the result.
            if (result != null && result.isNotEmpty() && result[0] != null && !TextUtils.isEmpty(
                    result[0].getOriginalValue()
                )
            ) {
            //    LOGS.d("SCAN_RESULT ${Gson().toJson(result[0].getOriginalValue())}")
                onQrScanned(result[0].getOriginalValue())
            }
        }

        // Load the customized view to the activity.
        remoteView?.onCreate(savedInstanceState)
        val params = FrameLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        binding.qrScannerContainer.addView(remoteView, params)

    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        remoteView?.onCreate(outState)
//    }
    /**
     * @param scannedResult ->Ultra -> https://noise-images.s3.ap-south-1.amazonaws.com/qr_code/download_noisefit.html?mac=D6:14:25:15:22:06&dtmodel=20012
     *
     * Zh-> https://noise-images.s3.ap-south-1.amazonaws.com/qr_code/download_noisefit.html?mac=D7:E9:ED:C4:3B:6E&dtmodel=20056
     *
     * Nav - > https://d.fashioncomm.com/download/dl.html?g=Noise?Info=ColorFitNAV#33709|4c5987ccd702|FC42AP20091901033709|A0.4R1.4T1.0H0.6B0.6|L42A+_GoNoise
     *
     *Active-> http://www.youduoyun.com/q/1000006.html?m=C2:A6:CD:81:52:DE
     *
     * Fusion ->https://d.fashioncomm.com/download/dl.html?g=Noise?Info=Fusion#18129|80eaca706c40|200313118129|N0.4A0.4R0.5T3.3H0.5B0.1|W007GA_GoNoise
     */
    private fun onQrScanned(scannedResult1: String) {
//        val scan =
//            "https://noise-images.s3.ap-south-1.amazonaws.com/qr_code/dafit.html?mac&sn&type MAC:28:B7:7F:1E:36:00 SN:210925080000000526 TYPE:OY08B"

        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_device_scan_by_qr)
        val scannedResult =
            if (scannedResult1.contains("typeMAC") || scannedResult1.contains("type MAC")) {
                scannedResult1
            } else {
                scannedResult1.replace(" ", "")
            }

        LOGS.d(TAG, "onQrScanned() called with: scannedResult = $scannedResult $scannedResult1")

        //Ultra
        if (Uri.parse(scannedResult).getQueryParameter("mac") != null) {
            scannedMac = Uri.parse(scannedResult).getQueryParameter("mac")
        } else if (Uri.parse(scannedResult).getQueryParameter("MAC") != null) {
            scannedMac = Uri.parse(scannedResult).getQueryParameter("MAC")
        }


        //  LOGS.d(TAG, "onQrScanned() called with: scannedResult = $scannedMac")

//        scannedMac = try {
//            Uri.parse(scannedResult).getQueryParameter("MAC")
//        } catch (exp: Exception) {
//            null
//        }
        LOGS.d(TAG, "onQrScanned() called with: scannedResult = $scannedMac")
        LOGS.d("$TAG scan result $scannedMac")
        //NoiseFit Active
        if (scannedMac.isNullOrEmpty()) {
            scannedMac = try {
                Uri.parse(scannedResult).getQueryParameter("m")
            } catch (exp: Exception) {
                null
            }

        }

        // NAV
        if (scannedMac.isNullOrEmpty()) {
            scannedMac = try {
                val uri = Uri.parse(scannedResult.replace("#", "?"))
                val info = uri.getQueryParameter("g")
                info?.split("|")?.get(1)
            } catch (exp: Exception) {
                null
            }
        }

        //vision
        if (scannedMac.isNullOrEmpty()) {
            try {
                val queryList = scannedResult.split("|")
                LOGS.d("Scan result ${Gson().toJson(queryList)}")
                if (queryList.size > 1) {
                    scannedMac = queryList[1]
                }
            } catch (exp: Exception) {
                scannedMac = null
            }


        }

        //vision
        if (scannedMac.isNullOrEmpty()) {
            val queryList = scannedResult.split("|")

            if (queryList.size > 1) {
                scannedMac = queryList[1]
            }

        }

        //evolve2
//        if (scannedMac.isNullOrEmpty()) {
//            val queryList = scannedResult.split(" ")
//            if (queryList.size > 2) {
//                scannedMac = queryList[1].replace("MAC", "").replace(":", "")
//            }
//
//        }


        //for pro4
        // https://noise-images.s3.ap-south-1.amazonaws.com/qr_code/download_noisefit.html?radio=d925181e146732750101023400002625181e1467&random=461620
        if (scannedMac.isNullOrEmpty()) {
            if (scannedResult.contains("radio") && scannedResult.contains("random")) {
                scannedMac = try {
                    viewModel.deviceScanQrCodeBean(scannedResult)
                } catch (exp: Exception) {
                    null
                }
            }
        }

        //for evolve 2
        if (scannedMac.isNullOrEmpty()) {
            val scannedResultNew = scannedResult.replace("typeMAC", "type MAC")
            val queryList = scannedResultNew.split(" ")
            if (queryList.size > 1) {
                scannedMac = queryList[1].replace("MAC:", "")
            }
        }


        if (!scannedMac.isNullOrEmpty()) {
            if (!ApplicationUtils.isInternetConnected()) {
                remoteView?.resumeContinuouslyScan()
                uiController.onDisplayError(getString(R.string.text_no_internet_connection))
                return
            }
            viewModel.isBLEScanning = false
            checkPermissionAndScan()
        } else {

            if (scannedResult.equals(
                    "https://noise-images.s3.ap-south-1.amazonaws.com/qr_code/smart-time-pro/smart_time_pro.html",
                    true
                )
            ) {
                showOpenPlayStoreDialog(ShareUtil.PACKAGE_ACE, "", R.drawable.ic_noisefit_ace)
            } else if (scannedResult.equals(
                    "https://app.help-document.com/noisefit/download/index.html",
                    true
                )
            ) {
                showOpenPlayStoreDialog(ShareUtil.PACKAGE_APEX, "", R.drawable.ic_noisefit_apex)
            } else if (scannedResult.contains(
                    "https://app.uteasy.com/noisefitprime/download/index.html",
                    true
                )
            ) {
                showOpenPlayStoreDialog(ShareUtil.PACKAGE_PRIME, "", R.drawable.ic_noisefit_prime)
            } else if (scannedResult.contains(
                    "https://noise-images.s3.ap-south-1.amazonaws.com/qr_code/download_noisefit.html",
                    true
                ) || (scannedResult.contains(
                    "https://noise-images.s3.ap-south-1.amazonaws.com/qr_code/noisefit_track/noisefit_track_app.html",
                    true
                ) || (scannedResult.contains(
                    "https://noise-images.s3.ap-south-1.amazonaws.com/qr_code/noisefit_tracknoisefit_track_app.html",
                    true
                )))
            ) {
                context.showShortToast("QR pairing is not supported. Please connect with Search and Pair")
            } else {
                context.showShortToast("Invalid QR Code")
                Handler(Looper.getMainLooper()).postDelayed({
                    remoteView?.resumeContinuouslyScan()
                }, 2000)
            }
//
        }

        remoteView?.pauseContinuouslyScan()

    }

    private val cameraResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            if (remoteView == null) {
                initScanner()
            }
        } else {
            uiController.onApiErrorReceived(
                ErrorResponse(
                    UIComponentType.AreYouSureDialog(
                        getString(R.string.text_permission_required),
                        getString(R.string.text_camera_permission_qr),
                        false,
                        getString(R.string.text_allow),
                        object : BinaryActionCallback {
                            override fun yes() {
                                activity?.let { act ->
                                    ApplicationUtils.openAppSettings(act)
                                }
                            }

                            override fun no() {

                            }

                        }
                    )
                )
            )

        }
    }

    fun checkCameraPermission(callback: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            callback.invoke()
        } else {
            cameraResult.launch(Manifest.permission.CAMERA)
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

                            }

                        }
                    )
                )
            )

        }
    }

    private fun startScan() {
        if (!btAdapter.isEnabled) {
            enableBluetooth()
            return
        }
        viewModel.forcedQRScanning = true
        startDiscovery()
    }

    /**
     * Should be called after all
     * required bluetooth permissions are granted
     */
    fun enableBluetooth() {
        try {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } catch (exp: Exception) {
            context.showShortToast(getString(R.string.bluetooth_turn_on_request))
        }
    }

    private fun stopLeScanning() {
        LOGS.d("$TAG stopLeScanning")
        viewModel.isBLEScanning = false
        onScanFinished()
        if (btAdapter != null && btAdapter.isEnabled) {
            bluetoothLeScanner?.stopScan(leScanCallback)
        }

        if (viewModel.forcedQRScanning) {
            startDiscovery()
            LOGS.d("$TAG forced scanning")
            viewModel.forcedQRScanning = false
        }
    }

    private fun scanLeDevice() {
        if (!viewModel.isBLEScanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                stopLeScanning()
            }, viewModel.qrScanDeviceTime)
            onScanStarted()
            LOGS.d("$TAG  startLeScanning")
            if (bluetoothLeScanner == null) {
                bluetoothLeScanner = btAdapter.bluetoothLeScanner
            }

            if (!btAdapter.isEnabled) {
                stopLeScanning()
                return
            }

            viewModel.isBLEScanning = true
            bluetoothLeScanner?.startScan(leScanCallback)
        } else {
            LOGS.d("$TAG  stopLeScanning")

            stopLeScanning()
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
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
//                LOGS.d("BLEDEVICE ${result.device?.name} ${result.device?.address}  ${result.device?.bondState} ${result.device?.type} ${result.device?.uuids} ${deviceEntity.mDeviceRadioBroadcastBean}")
            }
        }
    }


    /* @RequiresApi(Build.VERSION_CODES.O)
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

     }*/

    override fun onStart() {
        super.onStart()
        remoteView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        remoteView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        remoteView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        remoteView?.onStop()
    }

    private fun startDiscovery() {
        scanLeDevice()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            remoteView?.onDestroy()
            if (btAdapter != null && btAdapter.isEnabled) {
                bluetoothLeScanner?.stopScan(leScanCallback)
            }
            handler.removeCallbacksAndMessages(null)
            if (btAdapter.isDiscovering) {
                btAdapter.cancelDiscovery()
            }
        } catch (exp: Exception) {
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                startDiscovery()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                context?.showShortToast("Bluetooth required")
                navigateUpSafe()
            }
        }
    }

    /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
         when (requestCode) {
             REQUEST_ENABLE_BT ->{
                 if (resultCode == Activity.RESULT_OK) {
                     startDiscovery()
                 } else if (resultCode == Activity.RESULT_CANCELED) {
                     context?.showShortToast("Bluetooth required")
                     navigateUpSafe()
                 }
             }
             SELECT_DEVICE_REQUEST_CODE -> when (resultCode) {
                 Activity.RESULT_OK -> {

                     // The user chose to pair the app with a Bluetooth device.
 //                    val deviceToPair: BluetoothDevice? =
 //                        data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
 //                    val colorFitDevice: ColorFitDevice? = data?.extras?.getParcelable("colorfit")

                     navigate(
                         QrScanFragmentDirections.actionQrScanFragmentToPairingFragment(
                             viewModel.selectedColorFitDevice!!
                         )
                     )
                 }

                 else -> {
                     navigateUpSafe()
                 }
             }

             else -> super.onActivityResult(requestCode, resultCode, data)
         }

     }*/

    override fun onScanStarted() {
        try {
            binding.progressBar.root.visible()
        } catch (exp: Exception) {
        }
    }

    override fun onScanFinished() {
        if (viewModel.deviceFound) return

        if (viewModel.forcedQRScanning) return

        uiController.onApiErrorReceived(
            ErrorResponse(
                UIComponentType.RetryApiDialog("Connection failed. Retry?").apply {
                    this.callback = object : BinaryActionCallback {
                        override fun yes() {

                            if (isAdded) {
                                checkPermissionAndScan()
                            }
                        }

                        override fun no() {
                            navigateUpSafe()
                        }
                    }
                }
            )
        )
    }

    override fun onDeviceFound(deviceEntity: DeviceEntity) {
        if (deviceEntity.address.equals(scannedMac, true) ||
            deviceEntity.address.replace(":", "").equals(scannedMac, true)
        ) {
            try {
                viewModel.forcedQRScanning = false
                viewModel.deviceFound = true
                binding.progressBar.root.gone()
                stopLeScanning()

                val result = viewModel.getDeviceType(deviceEntity) ?: return

                result.first?.let {

                    var mcuPlatform: CRPScanRecordInfo.McuPlatform? = null
                    if (deviceEntity.mcuPlatform != null) {
                        mcuPlatform = deviceEntity.mcuPlatform

                    }
                    val colorFitDevice = ColorFitDevice(
                        bluetoothName = result.second.bluetoothName,
                        address = deviceEntity.address,
                        rssi = deviceEntity.rssi,
                        deviceType = it.deviceType,
                        url = result.second.url,
                        deviceId = result.second.id,
                        isSupportHeadset = deviceEntity.mDeviceRadioBroadcastBean?.isSupportHeadset
                            ?: false,
                        headsetMac = deviceEntity.mDeviceRadioBroadcastBean?.headsetMac ?: "",
                        isBind = deviceEntity.mDeviceRadioBroadcastBean?.isBind ?: false,
                        mcuPlatform = mcuPlatform?.name


                    )


                    LOGS.d("device bind status ${colorFitDevice.isBind}")

                    val (isUnSupported, playStorePackage, appIcon) = deviceUtil.isUnsupportedDevice(
                        colorFitDevice
                    )

                    viewModel.sessionManager.logInsiderAppEvent(
                        InsiderAppEvents.PairingEvents.wn_pair_device_by_qr,
                        HashMap<String, Any>().apply {
                            this["name"] = colorFitDevice.bluetoothName ?: ""
                            this["supported"] = !isUnSupported
                        })
                    if (!isUnSupported) {
                        /*if (colorFitDevice.isBind) {
                            val msg = "Watch "+colorFitDevice.bluetoothName+" ("+ colorFitDevice.address+")"+" already connected with some other devices"
                            uiController.onApiErrorReceived(
                                ErrorResponse(
                                    UIComponentType.InfoWatchConnectedAlertDialog(
                                        "Connected", msg, getString(R.string.text_close)
                                    )
                                )
                            )
                        } else {
                            navigate(
                                QrScanFragmentDirections.actionQrScanFragmentToPairingFragment(
                                    colorFitDevice
                                )
                            )
                        }*/

                        /*viewModel.selectedColorFitDevice = colorFitDevice
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            createAssociation(colorFitDevice)
                        } else {
                            navigate(
                                QrScanFragmentDirections.actionQrScanFragmentToPairingFragment(
                                    colorFitDevice
                                )
                            )
                        }*/

                        navigate(
                            QrScanFragmentDirections.actionQrScanFragmentToPairingFragment(
                                colorFitDevice
                            )
                        )

                        viewModel.tempColorFitDevice = colorFitDevice
                        colorFitDevice.address?.let { viewModel.checkWatchTokenExist(it) }
                    } else {
                        playStorePackage?.let {
                            showOpenPlayStoreDialog(it, colorFitDevice.bluetoothName ?: "", appIcon)
                        }
                    }
                }
            } catch (exp: Exception) {
            }
        }
    }

    private fun moveToPairingScreen(colorFitDevice: ColorFitDevice, watchToken: String) {
        colorFitDevice.watchToken = watchToken
        navigate(
            QrScanFragmentDirections.actionQrScanFragmentToPairingFragment(
                colorFitDevice
            )
        )
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
            val userNameToDisplay = if (userName.isNullOrEmpty()) "User" else userName

            tvDesc.text = if (deviceName.isEmpty()) {
                "Hi $userNameToDisplay, your smartwatch is not compatible with this app. Please download $appName app to proceed with pairing."
            } else {
                "Hi $userNameToDisplay, your smartwatch $deviceName is not compatible with this app. Please download $appName app to proceed with pairing."
            }


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
}