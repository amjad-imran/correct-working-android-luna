package com.noisefit.ui.onboarding.pairing.pair

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentPairingBinding
import com.noisefit.receiver.service.FeedbackSubmitService
import com.noisefit.receiver.service.ProblemType
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.onboarding.onboardProfile.ProfileSetupActivity
import com.noisefit.ui.onboarding.pairing.DeviceSetupActivity
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.watch.ApplicationHandler
import com.noisefit.watch.ConnectionHandler
import com.noisefit_commans.common.copyToClipBoard
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.SingleActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.enums.Actions
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.interfaces.base.BaseInitializeCallbacks
import com.noisefit_commans.interfaces.connection.BindState
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.connection.ConnectionCallbacks
import com.noisefit_commans.interfaces.connection.WatchBindState
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceFirmware
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.*
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.ConnectEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LogEvents
import com.oreo.receiver.service.RingConnectionService
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val TRY_AGAIN_TIME = 1000L * 60
const val AUTO_RECONNECT_TIME = 1000L * 30

@AndroidEntryPoint
class PairingFragment : BaseFragment<FragmentPairingBinding>(FragmentPairingBinding::inflate) {


    @Inject
    lateinit var applicationHandler: ApplicationHandler

    @Inject
    lateinit var connectionHandler: ConnectionHandler

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var vibrationUtils: VibrationUtils

    private val viewModel: PairDeviceViewModel by viewModels()
    private val args: PairingFragmentArgs by navArgs()
    private val TAG = "PairDeviceBottomSheet"

    private var stopPairHandler: Handler? = null
    private var tryAgainHandler: Handler? = null
    private var autoConnectHandler: Handler? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)

        initUi(args.colorFitDevice)
        startPairing()
        initListener()
        startTryAgainTimer()
        startAutoReconnectTimer()


    }

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.pairState.value == PairState.FAILED) {
                    onDisconnect()
                    navigateUpSafe()
                }
            }
        }

    override fun initListener() {
        binding.btnPairingIssue.setOnClickListener {


        }
        binding.btnStopPairing.setOnClickListener {
            sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_try_again,
                HashMap<String, Any>().apply {
                    this["state"] = viewModel.pairState.value ?: ""
                })
            when (viewModel.pairState.value) {
                PairState.PAIRING -> {

                    onDisconnect()
                }

                PairState.FAILED -> {
                    //Try Again
                    if (viewModel.mIsDevicePaired) {
                        viewModel.getDeviceFeatures()
                    } else {
                        viewModel.pairingSuccessEvent = false
                        viewModel.pairingFailedEvent = false
                        sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_pairing_retry,
                            HashMap<String, Any>().apply {
                                this["name"] = args.colorFitDevice.bluetoothName.toString()
                                this["mac"] = args.colorFitDevice.address.toString()
                                this["tryAgain"] = true
                            })
                        LOGS.d("PAIRING_FRAG_CALLED FAILED CASE")

                        connect(args.colorFitDevice)
                        startTryAgainTimer()
                    }
                    viewModel.pairState.postValue(PairState.PAIRING)
                }

                PairState.PAIRED -> {
                    //Continue
                    handlePairState()
                }

                else -> {}
            }
        }
        binding.btnStart.setOnClickListener {
            handlePairState()
        }
    }

    private fun handlePairState() {
        if (viewModel.isProfileSetupComplete()) {
            startActivity(DeviceSetupActivity.getStartIntent(requireContext()))
            activity?.finish()
        } else {
            startActivity(ProfileSetupActivity.getStartIntent(requireContext()))
            activity?.finish()
        }
    }

    private fun startDeviceService() {

        try {
            viewModel.pairState.postValue(PairState.PAIRED)
            vibrationUtils.vibrate(LOW_VIBRATION)

            if (!viewModel.isMyServiceRunning(
                    RingConnectionService::class.java,
                    requireContext()
                )
            ) {
                startRingConnectionService(Actions.INIT_DEFAULT)
            } else {
                LOGS.d("Ring connection service already running")
            }
        } catch (ignored: Exception) {
            ignored.printStackTrace()
        }
    }

    override fun subscribeObservers() {
        viewModel.continueDeviceSetup.observe(this) {
            it.getContent()?.let {
                navigateUpSafe()
                startDeviceService()
            }
        }
        viewModel.deviceSetupSuccess.observe(this) {
            it.getContent()?.let {
                startDeviceService()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        /*viewModel.getApiErrors().observe(this) {
            it.getContent()?.let {
                setUI(PairState.FAILED)
            }
        }*/
        viewModel.updateInfo.observe(this) {
            it.getContent()?.let { res ->

                try {

                    val url =
                        if (viewModel.tempColorFitDevice?.deviceType == DeviceType.NOISE_EVOLVE_2.deviceType
                            || viewModel.tempColorFitDevice?.deviceType == DeviceType.NOISE_EVOLVE_2_PLAY.deviceType
                            || viewModel.tempColorFitDevice?.deviceType == DeviceType.COLORFIT_PULSE_2.deviceType
                            || viewModel.tempColorFitDevice?.deviceType == DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType
                        ) {
                            res.touchUrl
                        } else {
                            res.url
                        }
                    LOGS.d("Loading File $url")
                    val fileName = url.split("/").last()
                    viewModel.downloadFirmware(
                        url,
                        requireContext().externalCacheDir!!,
                        fileName
                    )
                } catch (e: Exception) {
                }
            }
        }
        viewModel.updateFirmware.observe(this) {
            it.getContent()?.let { file ->
                val fileUri = Uri.fromFile(file).toString()
                viewModel.localFilePath = fileUri

                connectionHandler.getConnectionActions(args.colorFitDevice)?.apply {
                    this.startDfuUpdate(fileUri)
                }
            }
        }

        viewModel.pairState.observe(this) {
            setUI(it)

            /*if (it == PairState.FAILED) {
                onDisconnect(false)
            }*/
        }

    }

    private fun startTryAgainTimer() {
        tryAgainHandler = Handler(Looper.getMainLooper())
        tryAgainHandler?.postDelayed({
            try {
                LOGS.d(TAG, "Try again timer")
                if (viewModel.pairState.value != PairState.PAIRED) {
                    LOGS.d(TAG, "Try again timer:Failed")
                    viewModel.pairState.postValue(PairState.FAILED)
                    AppLogs.sendAppLogs(LogEvents.Connect, ConnectEvents.TimeOut)
                }
            } catch (exp: NullPointerException) {
                //CASE : Binding destroyed
            }

        }, TRY_AGAIN_TIME)
    }

    private fun startAutoReconnectTimer() {
        autoConnectHandler = Handler(Looper.getMainLooper())
        autoConnectHandler?.postDelayed({
            try {
                val colorFitDevice = args.colorFitDevice
                LOGS.d("PAIRING_FRAG_CALLED autoreconnect")

                connect(colorFitDevice)
            } catch (exp: Exception) {
                //CASE : Binding destroyed
            }

        }, AUTO_RECONNECT_TIME)
    }

    private fun startPairing() {
        LOGS.d("PAIRING_FRAG_CALLED startPairing()")
        viewModel.pairingTimeTaken = System.currentTimeMillis()
        viewModel.pairingSuccessEvent = false
        viewModel.pairingFailedEvent = false
        sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_pairing_start,
            HashMap<String, Any>().apply {
                this["name"] = args.colorFitDevice.bluetoothName.toString()
                this["mac"] = args.colorFitDevice.address.toString()
                this["tryAgain"] = false
            })

        AppLogs.sendAppLogs("Pairing device ${args.colorFitDevice.bluetoothName} | ${args.colorFitDevice.address}")


        sessionManager.clearSessionManager()
        val colorFitDevice = args.colorFitDevice
        applicationHandler.initSdks(colorFitDevice)?.apply {
            callbackListener(object : BaseInitializeCallbacks {
                override fun serviceConnected() {
                    AppLogs.sendAppLogs("serviceConnected")
                    LOGS.d("PAIRING_FRAG_CALLED service connected")
                    connect(colorFitDevice)
                    removeCallback()
                }

                override fun serviceDisconnected() {
                    AppLogs.sendAppLogs("serviceDisconnected")
                    removeCallback()
                }

            })
        }

    }

    private fun connect(colorFitDevice: ColorFitDevice) {

        LOGS.d("connect_inside call")
        connectionHandler.getConnectionActions(colorFitDevice)?.apply {
            LOGS.d("PAIRING_FRAG_CALLED")
            if (colorFitDevice.watchToken.isEmpty()) {
                connect(colorFitDevice)
            } else {
                Handler(Looper.getMainLooper()).post {
                    context.showShortToast("Reconnecting..")
                }
                reconnect(colorFitDevice, true)
            }

            onConnectedQRBinding()
            callbackListener(object : ConnectionCallbacks {


                override fun onConnect(
                    connectState: ConnectState
                ) {
                    when (connectState) {
                        is ConnectState.ConnectFailed -> {
                            LOGS.d(
                                TAG,
                                "onConnectFailed() called with: noiseFitDevice = ${connectState.noiseFitDevice}"
                            )
                            if (viewModel.isInDfuMode) {
                                return
                            }
                            Handler(Looper.getMainLooper()).post {
                                try {
                                    tryAgainHandler?.removeCallbacksAndMessages(null)
                                    autoConnectHandler?.removeCallbacksAndMessages(null)
                                    viewModel.pairState.postValue(PairState.FAILED)
                                } catch (exp: Exception) {
                                }
                                //onDisconnect()
                            }

                            AppLogs.sendAppLogs("Pairing ConnectState.ConnectFailed ${args.colorFitDevice.bluetoothName} | ${args.colorFitDevice.address}")



                            viewModel.shouldSendPairingFailLogs {
                                if (it) {
                                    context?.let { context ->
                                        val comment = "Failed ${colorFitDevice.bluetoothName}"
                                        FeedbackSubmitService.startService(
                                            context,
                                            ProblemType.PAIRING.name,
                                            comment,
                                            colorFitDevice.deviceId
                                        )
                                    }
                                }
                            }

                        }

                        is ConnectState.DfuMode -> {
                            viewModel.isInDfuMode = true
                            if (connectState.needForceOTA) {
                                Handler(Looper.getMainLooper()).post {
                                    try {
                                        /*  binding.searchCloseBtn.invisible()
                                          binding.layoutMain.gone()
                                          binding.layoutRepairWatch.visible()*/
                                        tryAgainHandler?.removeCallbacksAndMessages(null)
                                        autoConnectHandler?.removeCallbacksAndMessages(null)
                                        viewModel.checkForUpdates(
                                            colorFitDevice,
                                            connectState.version
                                        )
                                    } catch (exp: Exception) {
                                    }
                                }
                            }
                        }

                        is ConnectState.ReconnectStatus -> {
                            var text =
                                "Ring is already connected with another account"


                            var showResetDialog = false
                            when (connectState.watchBindState) {
                                WatchBindState.WatchIsUnbind -> {
                                    viewModel.removeWatchTokenFromServer(colorFitDevice.address)
                                    colorFitDevice.watchToken = ""
                                    Handler(Looper.getMainLooper()).post {
                                        context.showShortToast("Connecting..")
                                    }
                                    connect(colorFitDevice)
                                }

                                WatchBindState.InvalidToken -> {
                                    LOGS.d("verifyUserIdCallBack invalid token 1221")
                                    text += ""
                                    viewModel.removeWatchTokenFromServer(colorFitDevice.address)
                                    colorFitDevice.watchToken = ""
                                    showResetDialog = true
                                }

                                WatchBindState.AlreadyPaired -> {
                                    showResetDialog = true
                                }
                            }

                            if (showResetDialog) {
                                val infoAlert = UIComponentType.InfoAlertDialog(
                                    "Already connected?",
                                    text,

                                    getString(R.string.text_ok)
                                )
                                infoAlert.callback = object : SingleActionCallback {
                                    override fun onClicked() {
                                        navigateUpSafe()
                                    }

                                }

                                uiController.onApiErrorReceived(
                                    ErrorResponse(
                                        infoAlert
                                    )
                                )


                            }


                        }

                        else -> {}
                    }
                }

                override fun onBluetoothConnect(isBluetoothConnected: Boolean) {
                    LOGS.d(TAG, "onBluetoothConnect $isBluetoothConnected")
                    localDataStore.setBluetoothDialogShown(isBluetoothConnected)
                }

                override fun onInitCompleted(noiseFitDevice: ColorFitDevice?) {
                    LOGS.d(TAG, "onInitCompleted() called with: noiseFitDevice = $noiseFitDevice")
                }

                override fun onBind(noiseFitDevice: ColorFitDevice?, bindState: BindState) {

                }

                override fun onDeviceReady(noiseFitDevice: ColorFitDevice?) {
                    LOGS.d(TAG, "onDeviceReady() called with: noiseFitDevice = $noiseFitDevice")
                    AppLogs.sendAppLogs("Paired New Device : ${noiseFitDevice?.deviceType},  ${noiseFitDevice?.address}, ${noiseFitDevice?.deviceId} ")
                    autoConnectHandler?.removeCallbacksAndMessages(null)
                    noiseFitDevice?.let {

                        viewModel.mIsDevicePaired = true
                        viewModel.colorFitDevice = noiseFitDevice
                        viewModel.getDeviceFeatures()
                    }
                }

                override fun onFirmwareUpgradeProgress(firmware: DeviceFirmware) {
                    LOGS.d(TAG, "onFirmwareUpgradeProgress() called with: firmware = $firmware")
                    AppLogs.sendAppLogs("$TAG :onFirmwareUpgradeProgress() called with: firmware = $firmware")
                    try {
                        if (firmware.status.equals("success", true)) {
                            context.showShortToast(getString(R.string.text_ota_updated_successfully))
                            connectionHandler.getConnectionActions(args.colorFitDevice)?.apply {
                                this.disconnect(args.colorFitDevice)
                            }
                            findNavController().navigateUp()
                        } else if (firmware.status.equals("error", true)) {
                            context.showShortToast(getString(R.string.text_ota_update_failed))
                            findNavController().navigateUp()
                        }
                    } catch (exp: Exception) {

                    }
                }
            })
        }


    }


    private fun startRingConnectionService(action: Actions) {
        if (action == Actions.STOP) return
        Intent(requireContext(), RingConnectionService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LOGS.i(TAG, "Starting the Ring service in >=26 Mode")
                ContextCompat.startForegroundService(requireContext(), it)
                return
            }
            LOGS.i(TAG, "Starting the Ring service in < 26 Mode")
            activity?.startService(it)
            AppLogs.sendAppLogs("Ring Connection service started")
        }
    }

    private fun onDisconnect(moveBack: Boolean = true) {
        connectionHandler.getConnectionActions()?.disconnect(args.colorFitDevice)
        applicationHandler.unInitSdks(args.colorFitDevice)
        // connectionHandler.getConnectionActions()?.disconnect()
        connectionHandler.getConnectionActions()?.removeCallbacks()

        sessionManager.setConnectedDeviceRing(null)
        sessionManager.setConnectStateRing(ConnectState.UnPaired())

        AppLogs.sendAppLogs("All connection get disconnected")
        try {
            if (moveBack) {
                navigateUpSafe()
            }
        } catch (exp: Exception) {
        }
    }


    private fun initUi(colorFitDevice: ColorFitDevice) {
        activity?.let {


            binding.ivWatchImage.loadWatchImage(//TODO change for ring
                it,
                colorFitDevice.ringInfo?.image2 ?: "",
                R.drawable.ic_ring_default_silver
            )

        }

        val colorInfo = if (colorFitDevice.ringInfo != null) {
            " (${colorFitDevice.ringInfo?.color}, Size ${colorFitDevice.ringInfo?.size})"
        } else {
            null
        }

        if (colorInfo != null) {
            binding.tvWatchNameInfo.visible()
            binding.tvWatchNameInfo.text = colorInfo
        }



        binding.tvWatchMac.text = "MAC ${colorFitDevice.address}"
        binding.tvWatchName.text = colorFitDevice.bluetoothName

        viewModel.pairState.postValue(PairState.PAIRING)

        binding.btnCopyMac.setOnClickListener {
            colorFitDevice.address?.copyToClipBoard()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopPairHandler?.removeCallbacksAndMessages(null)
        tryAgainHandler?.removeCallbacksAndMessages(null)
        autoConnectHandler?.removeCallbacksAndMessages(null)
    }

    private fun setUI(pairState: PairState) {
        //binding.layoutWatch.circularProgressBar.setState(pairState)
        when (pairState) {
            PairState.PAIRING -> {
                binding.layoutWatch.repeatCount = LottieDrawable.INFINITE
                binding.layoutWatch.setAnimation(R.raw.anim_pairing)
                binding.layoutWatch.playAnimation()

//                binding.layoutWatchLayer2.gone()
                //binding.layoutWatch.circularProgressBar.startAnimation()
                binding.textView.text = getString(R.string.text_pairing)
                binding.tvPairStatus.text = getString(R.string.text_keep_your_watch_near_your_phone)
                binding.btnStopPairing.text = getString(R.string.text_stop_pairing)
            }

            PairState.FAILED -> {
                binding.layoutWatch.repeatCount = LottieDrawable.INFINITE
                binding.layoutWatch.setAnimation(R.raw.anim_pair_failed)
                binding.layoutWatch.playAnimation()

//                binding.layoutWatchLayer2.visible()
//                binding.layoutWatchLayer2.repeatCount = LottieDrawable.INFINITE
//                binding.layoutWatchLayer2.setAnimation(R.raw.anim_pair_failed)
//                binding.layoutWatchLayer2.playAnimation()

                binding.textView.text = getString(R.string.text_pairing_unsuccess)
                binding.tvPairStatus.text = getString(
                    R.string.text_watch_pair_fail,
                    args.colorFitDevice.bluetoothName
                )
                binding.btnStopPairing.text = getString(R.string.text_try_again)
                logPairingFailed()
            }

            PairState.PAIRED -> {
                binding.layoutWatch.repeatCount = 1
                binding.layoutWatch.setAnimation(R.raw.anim_pair_success)

//                binding.layoutWatchLayer2.visible()
//                binding.layoutWatchLayer2.repeatCount = LottieDrawable.INFINITE
//                binding.layoutWatchLayer2.setAnimation(R.raw.anim_pair_success)
//                binding.layoutWatchLayer2.playAnimation()


                binding.btnPairingIssue.gone()
                binding.btnStopPairing.gone()
                binding.btnStart.visible()
                binding.textView.text = getString(R.string.text_pairing_success)
                binding.tvPairStatus.text = getString(
                    R.string.text_watch_pair_success,
                    args.colorFitDevice.bluetoothName
                )
                binding.btnStopPairing.text = getString(R.string.text_get_started)
                logPairingSuccess()
            }
        }
    }

    private fun logPairingSuccess() {
        val deviceData = args.colorFitDevice

        if (!viewModel.pairingSuccessEvent) {
            viewModel.pairingSuccessEvent = true
            val timeTaken = System.currentTimeMillis() - viewModel.pairingTimeTaken
            sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_pairing_complete,
                HashMap<String, Any>().apply {
                    this["name"] = deviceData.bluetoothName.toString()
                    this["mac"] = deviceData.address.toString()
                    this["time"] = TimeUnit.MILLISECONDS.toSeconds(timeTaken)
                    this["status"] = "complete"
                })
        }


        val arr = arrayOf(deviceData.bluetoothName)
        sessionManager.addUserAttributeToInsider(false, HashMap<String, Any>().apply {
            this["pair_device_watchname"] = deviceData.bluetoothName.toString()
            this["pair_device_mac_address"] = deviceData.address.toString()
            this["pair_device_firmware_number"] = ""
            this["paired_devices_list"] = arr
        })


    }

    private fun logPairingFailed() {
        val deviceData = args.colorFitDevice
        if (!viewModel.pairingFailedEvent) {
            viewModel.pairingFailedEvent = true
            sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_pairing_failed,
                HashMap<String, Any>().apply {
                    this["name"] = deviceData.bluetoothName.toString()
                    this["mac"] = deviceData.address.toString()
                    this["status"] = "failed"
                })
        }



        sessionManager.addUserAttributeToInsider(false, HashMap<String, Any>().apply {
            this["pair_device_watchname"] = deviceData.bluetoothName.toString()
            this["pair_device_mac_address"] = deviceData.address.toString()
            this["pair_device_firmware_number"] = deviceData.deviceId.toString()
        })

    }

}

enum class PairState {
    PAIRING, FAILED, PAIRED
}