package com.noisefit.ui.myDevice

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.text.bold
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.noisefit.BottomNavOption
import com.noisefit.MainViewModel
import com.noisefit.luna.R
import com.noisefit.data.remote.response.Watchface2
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.luna.databinding.FragmentMyDeviceBinding
import com.noisefit.oreo.OreoMainActivity
import com.noisefit.ui.SplashActivity
import com.noisefit.ui.common.*
import com.noisefit.ui.dashboard.feature.wristsense.WristMode
import com.noisefit.ui.feeds.create.CREATE_POST_KEY
import com.noisefit.ui.myDevice.camera.CameraShutterActivity
import com.noisefit.ui.myDevice.manage.CheckForUpdatesViewModel
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit.ui.settings.helpAndSupport.HelpAndSupportType
import com.noisefit.ui.watchface.WatchFaceProgressBottomDialog
import com.noisefit.ui.watchface.adapter.RandomWatchFaceAdapter
import com.noisefit.ui.watchface.adapter.WatchFaceActions
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.InsiderAppEvents

import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.enums.Device
import com.noisefit_commans.databinding.LayoutCustomAlertBinding
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.*
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.ui.profile.CHOOSE_DEVICE_KEY
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MyDeviceFragment : BaseFragment<FragmentMyDeviceBinding>(FragmentMyDeviceBinding::inflate) {

    private lateinit var dashboardWfAdapter: DashboardWfAdapter
    private val viewModel: MyDeviceViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    /*private val deviceManager: CompanionDeviceManager by lazy {
        requireActivity().getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
    }*/
    private val updateViewModel: CheckForUpdatesViewModel by activityViewModels()
    private var minimumBatteryLevel = 20


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.features = viewModel.localDataStore.getDeviceFeatures()
        binding.lifecycleOwner = this
        handleOfflineSwitch()

        if (viewModel.watchesSDK.hasCategoryWatchFace()) {
            mainViewModel.getRecentWatchFaces()
        }

        if (viewModel.localDataStore.getConnectedDevice() != null) {
            if (viewModel.sessionManager.connectState.value !is ConnectState.ConnectSuccess) {
                mainViewModel.checkBluetooth.postValue(Event(true))
                if (viewModel.watchesSDK.getWatchType() == SDKWatchType.SDK_CF_PRO || viewModel.watchesSDK.getDevicesForLocation()) {
                    mainViewModel.checkLocation.postValue(Event(true))
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        /*if (viewModel.localDataStore.getConnectedDevice() != null) {
            if (viewModel.sessionManager.connectState.value is ConnectState.ConnectSuccess) {
                if (viewModel.shouldShowAgpsDialog()) {
                    showAgpsUpdateDialog()
                }
            }
        }*/

    }

    var agpsUpdateDialog: AlertDialog? = null

    private fun showAgpsUpdateDialog() {
        val builder =
            MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_rounded)
        val layoutCustomAlertBinding: LayoutCustomAlertBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            com.noisefit_commans.R.layout.layout_custom_alert, null, false
        )
        layoutCustomAlertBinding.apply {
            tvTitle.text = getString(
                R.string.text_agps_update
            )
            tvDesc.text = "Your watch AGPS has expired, please click update for map positioning."
            btnAllow.text = getString(R.string.text_update)
            btnCancel.visibility = View.GONE
            btnAllow.setOnClickListener {
                onAgpsUpdateClicked()
                agpsUpdateDialog?.dismiss()

            }
        }
        builder.setView(layoutCustomAlertBinding.root)
        builder.setCancelable(false)

        agpsUpdateDialog = builder.create()
        agpsUpdateDialog?.show()

    }

    override fun onPause() {
        super.onPause()
        if (agpsUpdateDialog?.isShowing == true) {
            agpsUpdateDialog?.dismiss()
        }
    }

    private fun showForceUnPairDialog() {
        val messageBuilder =
            StringBuilder("Manually reset the device to connect again. Press unpair to continue")
        val callingWatchMessage = viewModel.getBleCallingStatusMessage()
        if (!callingWatchMessage.isNullOrEmpty()) {
            messageBuilder.append("\n\n")
            messageBuilder.append(callingWatchMessage)
        }
        uiController.onApiErrorReceived(
            ErrorResponse(
                UIComponentType.AreYouSureDialog(getString(R.string.text_alert),
                    messageBuilder.toString(),
                    false,
                    getString(R.string.text_unpair),
                    object : BinaryActionCallback {
                        override fun yes() {

                            viewModel.sessionManager.forceDisconnect.value = (Event(true))
                            viewModel.sessionManager.setConnectState(ConnectState.UnPaired())

                        }

                        override fun no() {

                        }
                    })
            )
        )
    }

    /*private fun disAssociate() {
        viewModel.localDataStore.getConnectedDevice()?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.address?.let { it1 -> deviceManager.disassociate(it1) }
            }
        }
    }*/

    private fun showUnPairDialog() {
        //disAssociate()
        viewModel.localDataStore.getConnectedDevice()?.let {
            viewModel.connectionHandler.getConnectionActions(it)?.disconnect(it)

        }
        if (isAdded) {
            binding.progressBar.root.visible()
        }
    }

    private fun showDeviceSelector() {
        setFragmentResultListener(CHOOSE_DEVICE_KEY) { _, bundle ->
            val addDevice = bundle.getBoolean("addDevice")
            if (addDevice) {
                viewModel.setLoading(true)
                viewModel.nextAction = MyDeviceAction.ADD_DEVICE
                viewModel.sessionManager.hibernateCurrentDevice {
                    if (it) {
                        onHibernateSuccess()
                    } else {
                        context.showShortToast(getString(R.string.text_something_went_wrong))
                    }
                }
                return@setFragmentResultListener
            }

            val selectedDevice = bundle.getSerializable("selectedDevice") as Device

            if (selectedDevice == Device.RING) {
                viewModel.setLoading(true)
                viewModel.nextAction = MyDeviceAction.SWITCH_TO_RING
                viewModel.sessionManager.hibernateCurrentDevice {
                    if (it) {
                        onHibernateSuccess()
                    } else {
                        context.showShortToast(getString(R.string.text_something_went_wrong))
                    }
                }
            }
        }
        navigate(R.id.bottomSheetChooseDevice)
    }

    override fun initListener() {

        binding.toolbar.setOnClickListener {
            showDeviceSelector()
        }
        binding.ivToolbarDeviceArrow.setOnClickListener {
            showDeviceSelector()
        }


        binding.lytPairYourDeviceHeader.btnPairDevice.setOnClickListener {
            startActivity(PairDeviceActivity.getStartIntent(requireContext(), true))
            //activity?.finish()
        }

        val s =
            SpannableStringBuilder().append(getString(R.string.text_don_t_have_a_noisefit_device_yet_check_out_our_latest_collection_by_clicking_on_the))
                .append(" ").bold { append("'") }
                .bold { append(getString(R.string.text_get_noise)) }.bold { append("'") }
                .append(" ").append(getString(R.string.text_in_the_navbar))
        updateToolbarTitle(getString(R.string.text_no_device_paired))
        binding.ivToolbarDeviceArrow.gone()
        binding.lytPairYourDeviceHeader.tvMsg.text = s
        binding.btnUnpair.setOnClickListener {
            setFragmentResultListener(UNPAIR_REQUEST_KEY) { _, bundle ->
                val unpairDevice = bundle.getBoolean("unpair")
                val forceUnpair = bundle.getBoolean("force_unpair")
                if (unpairDevice) {
                    viewModel.setTempColorFitDevice()
                    logInsiderEvent(InsiderAppEvents.MYDEVICE_UNPAIRDEVICE_CLICK)
                    showUnPairDialog()
                }
                if (forceUnpair) {
                    viewModel.setTempColorFitDevice()
                    logInsiderEvent(InsiderAppEvents.MYDEVICE_UNPAIRDEVICE_CLICK)
                    showForceUnPairDialog()
                }
            }

            navigate(MyDeviceFragmentDirections.actionNavigationDeviceFragToUnpairBottomDialogFragment())
        }

        binding.lytDeviceConnected.root.setOnClickListener {
            if (viewModel.sessionManager.connectState.value is ConnectState.ConnectSuccess) {
                logInsiderEvent(InsiderAppEvents.MYDEVICE_DEVICEMANAGEMENT_CLICK)

                navigate(R.id.deviceManagementFragment)

            } else {
                navigate(R.id.helpAndSupportListFragment, Bundle().apply {
                    putString("type", HelpAndSupportType.PAIRING_AND_CONNECTIVITY.name)
                    putParcelable("helpSupportItem", null)
                })

            }
        }

        binding.llMyContacts.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.MYDEVICE_MYCONTACTS_CLICK)
            navigate(R.id.contactListFragment)
        }

        binding.llWeather.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.MYDEVICE_WEATHERSETTINGS_CLICK)
            navigate(R.id.weatherFragment)
        }

        binding.llGoogleFit.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.MYDEVICE_GOOGLEFIT_CLICK)
            navigate(R.id.googleFitFragment)
        }

        binding.llCallAlert.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.MYDEVICE_CALLALERT_CLICK)
            navigate(R.id.callAlertFragment)
        }

        binding.llNotifications.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.MYDEVICE_NOTIFICATION_CLICK)
            navigate(R.id.notificationFragment)
        }

        binding.llAppListSort.setOnClickListener {
//            logInsiderEvent(InsiderAppEvents.MYDEVICE_NOTIFICATION_CLICK)
            navigate(R.id.appListSelectionFragment)
        }

        binding.llSos.setOnClickListener {
            navigate(R.id.SOSFragment)
        }

        binding.llFindMyDevice.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.MYDEVCIE_FINDMYDEVICE_CLICK)
            navigate(R.id.findMyDeviceFragment)
        }

        binding.llQrPayment.setOnClickListener {
            //logInsiderEvent(InsiderAppEvents.MYDEVCIE_FINDMYDEVICE_CLICK)
            navigate(R.id.qrCodeListingFragment)
        }
        binding.llStock.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.MYDEVICE_STOCKS_CLICK)
            navigate(R.id.stocksFragment)
        }
        binding.llWorldClock.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.MYDEVICE_WORLDCLOCK_CLICK)
            navigate(R.id.worldClockFragment)
        }
        binding.llQuickReply.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.MYDEVICE_QUICKREPLY_CLICK)
            navigate(R.id.quickReplyFragment)
        }

        binding.llWidgetSort.setOnClickListener {
            navigate(R.id.widgetSelectionFragment)
        }



        binding.llSportSelection.setOnClickListener {
            if (viewModel.isZhWatch()) {
                navigate(R.id.zhSportSelectionFragment)
            } else {
                navigate(R.id.sportSelectionFragment)
            }

        }

        binding.llFindMyPhone.setOnClickListener {
            navigate(R.id.findPhoneFragment)
        }

        binding.llHealthMonitor.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.MYDEVICE_HEALTHMONITOR_CLICK)
            navigate(R.id.healthMonitorFragment)
        }

        binding.llMyAlarms.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.MYDEVICE_MYALARMS_CLICK)
            navigate(R.id.myAlarmFragment)
        }
        binding.llMyMusic.setOnClickListener {
            navigate(R.id.musicControlFragment)
        }

        binding.llDeviceSettings.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.MYDEVICE_DEVICESETTING_CLICK)
            navigate(R.id.settingDeviceFragment)
        }
        binding.rowWristSense.setOnClickListener {

            var wristMode = WristMode.NONE
            if (viewModel.hasWristSenseWithTiming()) {
                wristMode = WristMode.SHOW_TIME
            }
            navigate(
                MyDeviceFragmentDirections.actionNavigationDeviceToWristSenseFragment(
                    wristMode
                )
            )
            logInsiderEvent(InsiderAppEvents.MYDEVICE_WRIST_SENSE)
        }

        binding.rowWatchFace.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.MYDEVICE_WATCHFACES_CLICK)
            if (viewModel.watchesSDK.hasCategoryWatchFace()) {
                //navigate(R.id.navigation_watchface)
                navigate(R.id.watchface2CategoryFragment)
            } else {
                navigate(R.id.watchFaceListingFragment)
            }

        }
        binding.llAgps.setOnClickListener {
            onAgpsUpdateClicked()
        }

        binding.llCameraSwitch.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.MYDEVICE_CAMERA_SWITCH)
            navigate(R.id.cameraSwitchFragment)
        }

        binding.llCameraShutter.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.MYDEVICE_CAMERASHUTTER_CLICK)
            startActivity(CameraShutterActivity.getStartIntent(requireContext()))
        }

        binding.llActivityRecognise.setOnClickListener {
            navigate(R.id.sportsRecognitionFragment)
        }
    }

    fun onAgpsUpdateClicked() {
        updateViewModel.agpsFileLocation = requireContext().externalCacheDir!!
        if (!viewModel.sessionManager.isDeviceConnected()) {
            context.showShortToast(getString(R.string.text_device_not_connected))
            return
        }
        if ((viewModel.sessionManager.batterPercent.value ?: 0) <= minimumBatteryLevel) {
            showBatteryWarning()
            viewModel.sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
            return
        }

        binding.progressBar.root.visible()
        when (viewModel.sessionManager.connectedDevice.value?.deviceType) {
            DeviceType.COLORFIT_NAV_PLUS.deviceType -> {
                updateViewModel.getAgpsFileUrl()
            }

            DeviceType.COLORFIT_PRO_4_GPS.deviceType -> {
                viewModel.sessionManager.sendQueryAction(QueryAction.GetAgpsState)
            }

            else -> {
                //For Hybrid
                updateViewModel.downloadAgpsFile1(
                    "http://offline-live1.services.u-blox.com/GetOfflineData.ashx?token=jtl0zz2MSK6-jgNz6k5yig;gnss=gps,glo;alm=gps,glo;period=2;resolution=1",
                    requireContext().externalCacheDir!!,
                    "mgaoffline.ubx"
                )
            }
        }

        logInsiderEvent(InsiderAppEvents.MYDEVICE_UPLOAD_AGPS)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setRecyclerView(listOFWf: List<List<Watchface2>>) {
        dashboardWfAdapter =
            DashboardWfAdapter(
                childFragmentManager,
                lifecycle,
                listOFWf,
                object : DashboardWfListener {
                    override fun onWatchfaceClicked(watchface2: Watchface2) {
                        logInsiderEvent(InsiderAppEvents.MYDEVICE_WATCHFACEITEM_CLICK)
                        viewModel.stopWatchFaceScroll()
                        navigate(R.id.watchface2CategoryFragment, Bundle().apply {
                            this.putParcelable("watchFace", watchface2)
                        })
                    }

                    override fun onStopScroll() {
                        viewModel.stopWatchFaceScroll()
                    }

                })
        binding.rvSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(40))
            })
            adapter = dashboardWfAdapter
        }

    }

    private fun showBatteryWarning() {
        val alertMessage = getString(R.string.text_battery_agps, minimumBatteryLevel)
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


    private fun logInsiderEvent(eventName: String) {
        viewModel.sessionManager.logInsiderAppEvent(
            eventName
        )
    }

    override fun subscribeObservers() {

        viewModel.startOreoFlow.observe(this) {
            it.getContent()?.let {
                viewModel.localDataStore.savePairDeviceType(Device.RING)
                //startRingService()
                activity?.let { act ->
                    startActivity(SplashActivity.getStartIntent(act))
                    act.finish()
                }
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.showNextWatchFace.observe(this) {

            tryCatch {
                val current = binding.rvSlider.currentItem

                val imageSize = mainViewModel.randomWatchFaces.value?.size ?: 0

                if (imageSize == 0) return@tryCatch

                if ((current + 1) == imageSize) {
                    binding.rvSlider.setCurrentItem(0, true)
                } else {
                    binding.rvSlider.setCurrentItem(current + 1, true)
                }
            }
        }

        updateViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        //watch paired ui handle
        viewModel.deviceConnected.observe(this) { connected ->
            if (connected) {
                binding.apply {
                    //lytFeatures.visible()
                    lytDeviceConnected.root.visible()
                    btnUnpair.visible()
                    lytPairYourDeviceHeader.root.gone()
                }

            } else {
                binding.apply {
                    //lytFeatures.gone()
                    lytDeviceConnected.root.gone()
                    btnUnpair.gone()
                    lytPairYourDeviceHeader.root.visible()
                    updateToolbarTitle(getString(R.string.text_no_device_paired))
                    binding.ivToolbarDeviceArrow.gone()
                }
            }
        }

        //handle connecting state
        viewModel.sessionManager.connectState.observe(this) { connectedState ->
            when (connectedState) {
                is ConnectState.ConnectFailed -> {
                    setStateConnecting(connectedState.noiseFitDevice)
                }

                is ConnectState.Connecting -> {
                    setStateConnecting(connectedState.noiseFitDevice)
                }

                is ConnectState.ConnectSuccess -> {
                    setStateConnected(connectedState.noiseFitDevice)
                    viewModel.sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
                }

                is ConnectState.UnPaired -> {
                    viewModel.removeWatchTokenFromServer()
                    binding.lytFeatures.gone()
                    binding.progressBar.root.gone()
                    viewModel.updateDeviceConnectedStatus()

                    val hasRingDevice = viewModel.ringDataStore.getRingDevice()
                    if (hasRingDevice != null) {
                        viewModel.startOreoFlow.postValue(Event(true))
                    }

                }

                is ConnectState.DisconnectSuccess -> {
                    /*binding.progressBar.root.gone()
                    navigateUpSafe()*/
                }

                is ConnectState.Hibernate -> {
                    binding.progressBar.root.gone()
                    onHibernateSuccess()
                }

                else -> {}
            }
        }
        updateViewModel.agpsFile1.observe(this) {
            it.getContent()?.let {
                if (viewModel.sessionManager.connectedDevice.value?.deviceType.equals(
                        DeviceType.COLORFIT_NAV_PLUS.deviceType, true
                    ) || viewModel.sessionManager.connectedDevice.value?.deviceType.equals(
                        DeviceType.COLORFIT_PRO_4_GPS.deviceType, true
                    )
                ) {
                    val file1 = updateViewModel.agpsFile1.value?.peekContent()

                    if (file1 != null) {
                        val uri1 = Uri.fromFile(file1)
                        val uri2 = Uri.fromFile(file1)
                        viewModel.sessionManager.sendUpdateQueryAction(
                            UpdateDeviceAction.UpdateAPGSData(
                                uri1, uri2
                            )
                        )

                        showProgressDialog(
                            getString(R.string.text_updating_your_agps),
                            getString(R.string.text_updating_agps),
                            "Updating…",
                            "",
                            ""
                        )
                        progressBottomSheet?.setProgress(0)
                    } else {
                        LOGS.e("AGPS Update Failed")
                    }
                    binding.progressBar.root.gone()
                } else {
                    //For Hybrid
                    updateViewModel.downloadAgpsFile2(
                        "http://offline-live1.services.u-blox.com/GetOfflineData.ashx?token=jtl0zz2MSK6-jgNz6k5yig;gnss=gps,glo;period=1;resolution=1",
                        requireContext().externalCacheDir!!,
                        "mgaoffline1.ubx"
                    )
                }
            }

        }
        updateViewModel.agpsFile2.observe(this) {
            it.getContent()?.let {

                binding.progressBar.root.gone()
                val file1 = updateViewModel.agpsFile1.value?.peekContent()
                val file2 = updateViewModel.agpsFile2.value?.peekContent()

                if (file1 != null && file2 != null) {
                    val uri1 = Uri.fromFile(file1)
                    val uri2 = Uri.fromFile(file2)
                    viewModel.sessionManager.sendUpdateQueryAction(
                        UpdateDeviceAction.UpdateAPGSData(
                            uri1, uri2
                        )
                    )
                    showProgressDialog(
                        getString(R.string.text_updating_your_agps),
                        getString(R.string.text_updating_agps),
                        "Updating…",
                        null,
                        null
                    )
                    progressBottomSheet?.setProgress(0)
                } else {
                    LOGS.e("AGPS Update Failed")
                }
            }
        }

        viewModel.sessionManager.agpsStatus.observe(this) { event ->

            event.getContent()?.let { status ->
                LOGS.d("AGPS_UPDATE ${status}")
                if (status) {
                    LOGS.d("AGPS_UPDATE REQUIRED")
                    binding.progressBar.root.visible()
                    updateViewModel.getAgpsFileUrl()
                } else {
                    binding.progressBar.root.gone()
                    context.showShortToast("AGPS is Up-to-date")
                }
            }

        }

        viewModel.sessionManager.updateDeviceCallback.observe(this) { event ->
            event.getContent()?.let {

                if (it is UpdateDeviceDataCallback.AGPSUpdateProgress) {
                    updateAgpsStatus(it)
                }
            }
        }

        mainViewModel.randomWatchFaces.observe(this) {
            if (it.isEmpty()) {
                binding.rvSlider.gone()
            } else {
                binding.rvSlider.visible()
                setRecyclerView(it)
                viewModel.startWatchFaceScroll()
            }

        }
    }

    fun onHibernateSuccess() {
        when (viewModel.nextAction) {
            MyDeviceAction.ADD_DEVICE -> {
                activity?.let {
                    startActivity(PairDeviceActivity.getStartIntent(it))
                    it.finish()
                }
            }

            MyDeviceAction.SWITCH_TO_RING -> {
                viewModel.ringDataStore.getRingDevice()?.let {
                    viewModel.updateUserDevice(it, true)
                }

            }

            else -> {}
        }
    }

    private fun startRingService() {
        context?.let {
            ApplicationUtils.setRescueWorkManager(it)
        }
    }

    private var progressBottomSheet: WatchFaceProgressBottomDialog? = null

    private fun showProgressDialog(
        title: String, message: String, typeText: String, watchfaceImageUrl: String?,
        watchFaceName: String?
    ) {
        progressBottomSheet = WatchFaceProgressBottomDialog.getInstance(
            title, message, typeText, watchfaceImageUrl,
            watchFaceName
        )
        progressBottomSheet?.isCancelable = false
        progressBottomSheet?.show(
            childFragmentManager,
            "DownloadBottomSheet"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressBottomSheet?.dismissAllowingStateLoss()
    }

    private fun updateAgpsStatus(agpsStatus: UpdateDeviceDataCallback.AGPSUpdateProgress) {
        when (agpsStatus.status) {
            UpdateStatus.STARTED -> {
                progressBottomSheet?.setProgress(agpsStatus.progress ?: 0)
            }

            UpdateStatus.PROGRESS -> {
                progressBottomSheet?.setProgress(agpsStatus.progress ?: 0)
            }

            UpdateStatus.COMPLETED -> {

                progressBottomSheet?.dismiss()
                AppLogs.sendAppLogs("APGS transfer Success")
                /*viewModel.localDataStore.setAGPSStatusState(
                    AGPSStatusState(
                        state = AGPSStatusEnum.UPDATED,
                        lastUpdated = System.currentTimeMillis()
                    )
                )*/
                context.showShortToast(getString(R.string.text_agps_updated))
                //NotificationUtil.clearAgpsNotification(requireContext())
            }

            UpdateStatus.ERROR -> {

                progressBottomSheet?.dismiss()

                viewModel.localDataStore.getConnectedDevice() ?: return


                uiController.onApiErrorReceived(
                    ErrorResponse(
                        UIComponentType.RetryApiDialog(
                            getString(R.string.text_agps_fail)
                        ).apply {
                            this.callback = object : BinaryActionCallback {
                                override fun yes() {
                                    if (isAdded) {
                                        onAgpsUpdateClicked()
                                    }
                                }

                                override fun no() {}
                            }
                        })
                )
            }

            else -> {}
        }
    }


    private fun updateToolbarTitle(title: String?) {
        binding.toolbar.text = title ?: ""
    }

    private fun setStateConnected(noiseFitDevice: ColorFitDevice) {
        val batteryPercent = "${viewModel.watchDataStore.getBatteryPercent()}% Battery level"

        val lastSync =
            viewModel.sessionManager.getLastSyncTime()?.let { DateFormats.getRelativeTime(it) }
        val lastSyncText = "Synced : ${lastSync ?: getString(R.string.text_not_yet_syncyed)}"
        binding.lytDeviceConnected.apply {
            this.layoutDevice.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
            tvStatus.text = getString(R.string.text_connected)
            tvStatus.setTextColor(
                resources.getColor(
                    R.color.white
                )
            )
            progressBarConnecting.gone()
            ivSettingsArrow.visible()
            imgWatch.loadWatchImage(
                requireContext(), noiseFitDevice.url,
                R.drawable.watch_default
            )
            tvBatteryPercentage.text = batteryPercent
            updateToolbarTitle(noiseFitDevice.bluetoothName)
            binding.ivToolbarDeviceArrow.visible()


            tvLastSync.text = lastSyncText

        }

        binding.apply {
            lytFeatures.visible()
        }
    }

    private fun setStateConnecting(noiseFitDevice: ColorFitDevice?) {

        binding.lytDeviceConnected.apply {
            this.layoutDevice.setBackgroundResource(R.drawable.back_modal_new_red)
            tvStatus.text = getString(R.string.text_trying_to_connect)
            tvStatus.setTextColor(
                resources.getColor(
                    R.color.color_error
                )
            )
            progressBarConnecting.visible()
            ivSettingsArrow.gone()
            imgWatch.loadImage(
                requireContext(), noiseFitDevice?.url
            )
            updateToolbarTitle(noiseFitDevice?.bluetoothName)
            binding.ivToolbarDeviceArrow.visible()
            tvLastSync.text = ""
            tvBatteryPercentage.text = ""

        }

        binding.lytFeatures.gone()
    }

    private fun handleOfflineSwitch() {
        viewModel.sessionManager.connectedDevice.value?.deviceType?.let {
            if (it.equals(
                    DeviceType.NOISE_EVOLVE_2.deviceType,
                    true
                ) || it.equals(
                    DeviceType.NOISEFIT_EVOLVE.deviceType,
                    true
                ) || it.equals(
                    DeviceType.COLORFIT_PULSE_2.deviceType,
                    true
                ) || it.equals(DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType, true) || it.equals(
                    DeviceType.NOISE_EVOLVE_2_PLAY.deviceType,
                    true
                )
            ) {
                binding.llCameraSwitch.visible()
            }
        }
    }

}