package com.noisefit.oreo

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ActivityOreoMainBinding
import com.noisefit.ui.APP_CONTINUE
import com.noisefit.ui.APP_EXIT
import com.noisefit.ui.APP_UPDATE
import com.noisefit.ui.common.BaseActivity
import com.noisefit.ui.onboarding.FirebaseUpdateViewModel
import com.noisefit.util.ApplicationUtils
import com.noisefit.util.notif.NotificationUtil
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.response.VersionCheckResponse
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.FirebaseLunaAppEvents
import com.noisefit_commans.utils.share.ShareUtil
import com.oreo.data.model.OWorkoutListModal
import com.oreo.ui.recordworkout.SELECT_RECORD_WORKOUT
import dagger.hilt.android.AndroidEntryPoint
import eightbitlab.com.blurview.RenderEffectBlur
import eightbitlab.com.blurview.RenderScriptBlur

@AndroidEntryPoint
class OreoMainActivity : BaseActivity<ActivityOreoMainBinding>() {

    private val viewModel: OreoMainViewModel by viewModels()
    private var navController: NavController? = null

    private val btAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }
    private val firebaseViewModel: FirebaseUpdateViewModel by viewModels()

    private val REQUEST_ENABLE_BT = 133

    companion object {
        fun getStartIntent(
            context: Context,
            notificationType: String? = null,
            notificationIndex: String? = null,
            deeplink: String? = null
        ): Intent {
            return Intent(context, OreoMainActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navController = findNavController(com.noisefit.luna.R.id.o_nav_host_fragment)
        setNavViewListeners()
        setBlur()
        setBlurAddCta()

        viewModel.sessionManager.getPairedState()
        checkBluetooth()
        firebaseViewModel.generateToken()
    }

    private fun setBlurAddCta() {
        val radius = 20f;
        val decorView = window.decorView;
        val rootView = binding.container
        val windowBackground = decorView.background

        val blurAlgo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            RenderEffectBlur()
        } else {
            RenderScriptBlur(this)
        }

        binding.blurViewSelector.setupWith(rootView, blurAlgo)
            .setFrameClearDrawable(windowBackground) // Optional
            .setBlurRadius(radius)
    }

    private fun setBlur() {
        val radius = 20f;
        val decorView = window.decorView;
        val rootView = binding.container
        val windowBackground = decorView.background

        val blurAlgo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            RenderEffectBlur()
        } else {
            RenderScriptBlur(this)
        }
        binding.blurView.setupWith(rootView, blurAlgo) // or RenderEffectBlur
            .setFrameClearDrawable(windowBackground) // Optional
            .setBlurRadius(radius)

    }

    private fun setNavViewListeners() {
        binding.navView.apply {
            lytHome.setOnClickListener {
                selectMenuItem(BottomNavOption.HOME)

            }
            lytActivity.setOnClickListener {
                selectMenuItem(BottomNavOption.ACTIVITY)

            }
            lytReadiness.setOnClickListener {
                selectMenuItem(BottomNavOption.READINESS)

            }
            lytSleep.setOnClickListener {
                selectMenuItem(BottomNavOption.SLEEP)

            }
            lytMyDevice.setOnClickListener {
                selectMenuItem(BottomNavOption.MY_DEVICE)

            }
        }
    }

    override fun initListener() {

        supportFragmentManager.setFragmentResultListener(SELECT_RECORD_WORKOUT, this) { _, bundle ->
            val workout = bundle.getParcelable<OWorkoutListModal>("workout")
            workout?.let {
                navController?.navigate(
                    R.id.recordWorkoutFragment,
                    bundleOf("workout" to it)
                )
            }
        }


        binding.layoutRetry.btnRetry.setOnClickListener {
            binding.layoutRetry.root.gone()
            viewModel.getUserHealthData(viewModel.mStartDate, viewModel.mEndDate)
        }

        binding.lytAddWorkoutSelector.tvAddWorkout.setOnClickListener {
            showAddWorkoutCta()
            binding.blurViewSelector.gone()
            if (viewModel.isDeviceConnected()) {
                navController?.navigate(R.id.addWorkoutFragment)
            } else {
                showShortToast("Please connect your ring to add a workout")
            }
        }
        binding.lytAddWorkoutSelector.ivWorkoutClose.setOnClickListener {
            showAddWorkoutCta()
            binding.blurViewSelector.gone()
        }

        binding.lytAddWorkoutSelector.tvRecordWorkout.setOnClickListener {
            showAddWorkoutCta()
            binding.blurViewSelector.gone()
            //TODO add ring connection related dialogs
            navController?.navigate(R.id.selectWorkoutFragment)
        }

        binding.btnAddWorkout.setOnClickListener {
            binding.btnAddWorkout.gone()
            binding.blurViewSelector.visible()
        }
    }

    fun showAddWorkoutCta() {
        binding.btnAddWorkout.visible()
    }

    fun checkBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkBluetoothPermission(permissionGranted = {
                if (btAdapter != null && !btAdapter.isEnabled) {
                    viewModel.sessionManager.setBluetoothState(false)
                    enableBluetooth()
                }
            })
        } else {
            if (btAdapter == null) {
                viewModel.sessionManager.setBluetoothState(false)
                return
            }
            if (!btAdapter.isEnabled) {
                viewModel.sessionManager.setBluetoothState(false)
                enableBluetooth()
                return
            }
        }
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
            viewModel.sessionManager.setBluetoothState(false)
            showShortToast(getString(R.string.bluetooth_turn_on_request))
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkBluetoothPermission(
        permissionGranted: () -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            permissionGranted.invoke()
        } else {
            bluetoothPermissionResultListener.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        }
    }

    private val bluetoothPermissionResultListener = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (it[Manifest.permission.BLUETOOTH_SCAN] == true && it[Manifest.permission.BLUETOOTH_CONNECT] == true) {
            if (btAdapter != null && !btAdapter.isEnabled) {
                enableBluetooth()
            }
        } else {
            onApiErrorReceived(
                ErrorResponse(
                    UIComponentType.AreYouSureDialog(getString(R.string.text_permission_required),
                        getString(R.string.text_permission_denial_bluetooth),
                        false,
                        getString(R.string.text_allow),
                        object : BinaryActionCallback {
                            override fun yes() {
                                ApplicationUtils.openAppSettings(this@OreoMainActivity)
                            }

                            override fun no() {}

                        })
                )
            )

        }
    }

    private fun checkIfShowUpdateDialog(versionCheckResponse: VersionCheckResponse): Boolean {

        if (versionCheckResponse.maintenanceMode == true) {
            return true
        }

        if (versionCheckResponse.upgradeType?.lowercase() == "force_upgrade") {
            return true
        } else if (versionCheckResponse.upgradeType?.lowercase() == "soft_upgrade") {
            val ignoredVersion = viewModel.localDataStore.getIgnoreVersion()
            if (ignoredVersion != versionCheckResponse.currentVersion) {
                return true
            }

        }
        return false
    }

    private fun showAppVersionBottomSheet(it: VersionCheckResponse) {
        supportFragmentManager.setFragmentResultListener(APP_EXIT, this) { key, bundle ->
            val isSelected = bundle.getBoolean("isSelected")
            if (isSelected) {
                finish()
                System.exit(0)
            }
        }
        supportFragmentManager.setFragmentResultListener(APP_CONTINUE, this) { key, bundle ->
            val isSelected = bundle.getBoolean("isSelected")
            if (isSelected) {
                viewModel.localDataStore.setIgnoreVersion(it.currentVersion ?: 0)
            }
        }
        supportFragmentManager.setFragmentResultListener(APP_UPDATE, this) { key, bundle ->
            val isSelected = bundle.getBoolean("isSelected")
            if (isSelected) {
                ShareUtil.openPlayStore(this@OreoMainActivity, "com.noisefit.luna")
            }
        }

        navController?.navigate(R.id.appUpdateBottomSheet, Bundle().apply {
            putParcelable("versonResponse", it)
        })
    }

    override fun observeSubscriber() {
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                if (viewModel.userHealthData.isEmpty()) {
                    binding.layoutRetry.root.visible()
                } else {
                    binding.layoutRetry.root.gone()
                    onApiErrorReceived(response)
                }
            }
        }

        viewModel.pushNotificationSleep.observe(this) {
            it.getContent()?.let {
                showLocalNotification(it.title, it.content, it.key)
            }
        }
        viewModel.pushNotificationReadiness.observe(this) {
            it.getContent()?.let {
                showLocalNotification(it.title, it.content, it.key)
            }
        }

        viewModel.checkBluetooth.observe(this) {
            it.getContent()?.let {
                checkBluetooth()
            }
        }


        viewModel.sessionManager.bluetoothState.observe(this) {
            if (it) {
                //uiController.onDisplayError("Bluetooth connected")
            } else {
                checkBluetooth()
                //uiController.onDisplayError("Bluetooth disconnected")
            }
        }


        viewModel.sessionManager.versionCheckData.observe(this) {

            if (checkIfShowUpdateDialog(it)) {
                showAppVersionBottomSheet(it)
            }

        }

        viewModel.bottomNavigation.observe(this) {
            it.getContent()?.let { navOpt ->
                when (navOpt) {
                    BottomNavOption.HOME -> {
                        selectMenuItem(BottomNavOption.HOME)
                    }

                    BottomNavOption.MY_DEVICE -> {
                        selectMenuItem(BottomNavOption.MY_DEVICE)
                    }

                    BottomNavOption.SLEEP -> {
                        selectMenuItem(BottomNavOption.SLEEP)
                    }

                    BottomNavOption.READINESS -> {
                        selectMenuItem(BottomNavOption.READINESS)
                    }

                    BottomNavOption.ACTIVITY -> {
                        selectMenuItem(BottomNavOption.ACTIVITY)
                    }
                }
            }
        }
    }

    private val navListener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.navigation_oreo_home, R.id.navigation_oreo_readiness, R.id.navigation_oreo_workouts, R.id.navigation_oreo_sleep -> {
                    binding.view27.visible()
                    binding.navView.root.visible()

                    binding.btnAddWorkout.visible()//todo add today condition
                }

                else -> {
                    binding.view27.gone()
                    binding.navView.root.gone()
                    binding.btnAddWorkout.gone()
                }
            }
        }


    private fun showLocalNotification(title: String, content: String, key: String) {
        NotificationUtil.pushNotification(
            NoiseFitApplicationMain.context!!, title, content, key, "1"
        )
    }

    override fun onResume() {
        super.onResume()
        navController?.addOnDestinationChangedListener(navListener)
        viewModel.ringDataStore.getRingDevice()?.let {
            if (viewModel.sessionManager.connectStateRing.value == null) {
                viewModel.sessionManager.setConnectStateRing(ConnectState.Connecting(it))
                ApplicationUtils.setRescueWorkManager(this)
            }

            if (viewModel.sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess) {
                viewModel.startDisconnectTimer()
            }
        }

        viewModel.shouldResetMasterDates()

    }

    override fun onPause() {
        navController?.removeOnDestinationChangedListener(navListener)
        super.onPause()
    }

    override fun onBackPressed() {
        navController?.let {
            when (it.currentDestination?.id) {
                R.id.navigation_oreo_home, R.id.navigation_oreo_readiness, R.id.navigation_oreo_workouts, R.id.navigation_oreo_sleep -> {

                    if (it.currentDestination?.id == R.id.navigation_oreo_home) {
                        finish()
                    } else {
                        selectMenuItem(BottomNavOption.HOME)
                        navController?.popBackStack(R.id.navigation_oreo_home, true)
                        navController?.navigate(R.id.navigation_oreo_home)
                    }
                }

                else -> {
                    super.onBackPressed()
                }
            }
        } ?: super.onBackPressed()
    }

    //TODO opimize
    private fun selectMenuItem(item: BottomNavOption) {
        when (item) {
            BottomNavOption.HOME -> {
                binding.navView.ivHome.setImageResource(R.drawable.ic_dash_summary_selected)
                binding.navView.ivSleep.setImageResource(R.drawable.ic_dash_oreo_sleep_default)
                binding.navView.ivReadiness.setImageResource(R.drawable.ic_dash_oreo_readiness_default)
                binding.navView.ivActivity.setImageResource(R.drawable.ic_dash_oreo_activity_default)
                binding.navView.ivMyDevice.setImageResource(R.drawable.ic_dash_device_default)


                binding.navView.apply {
                    ivGlowHome.visible()
                    ivGlowSleep.gone()
                    ivGlowReadiness.gone()
                    ivGlowActivity.gone()
                    ivGlowMyDevice.gone()
                }


                val lastDestination = navController?.currentDestination
                if (lastDestination?.id != R.id.navigation_oreo_home) {
                    navController?.popBackStack(R.id.navigation_oreo_home, true)
                    navController?.navigate(R.id.navigation_oreo_home)
                }

                viewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_FOOTER_HOME_CLICK)
            }

            BottomNavOption.SLEEP -> {
                binding.navView.ivHome.setImageResource(R.drawable.ic_dash_summary_default)
                binding.navView.ivSleep.setImageResource(R.drawable.ic_dash_oreo_sleep)
                binding.navView.ivReadiness.setImageResource(R.drawable.ic_dash_oreo_readiness_default)
                binding.navView.ivActivity.setImageResource(R.drawable.ic_dash_oreo_activity_default)
                binding.navView.ivMyDevice.setImageResource(R.drawable.ic_dash_device_default)

                binding.navView.apply {
                    ivGlowHome.gone()
                    ivGlowSleep.visible()
                    ivGlowReadiness.gone()
                    ivGlowActivity.gone()
                    ivGlowMyDevice.gone()
                }

                val lastDestination = navController?.currentDestination
                if (lastDestination?.id != R.id.navigation_oreo_sleep) {
                    navController?.popBackStack(R.id.navigation_oreo_sleep, true)
                    navController?.navigate(R.id.navigation_oreo_sleep)
                }
                viewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_FOOTER_SLEEP_CLICK)

            }

            BottomNavOption.READINESS -> {
                binding.navView.ivHome.setImageResource(R.drawable.ic_dash_summary_default)
                binding.navView.ivSleep.setImageResource(R.drawable.ic_dash_oreo_sleep_default)
                binding.navView.ivReadiness.setImageResource(R.drawable.ic_dash_oreo_readiness)
                binding.navView.ivActivity.setImageResource(R.drawable.ic_dash_oreo_activity_default)
                binding.navView.ivMyDevice.setImageResource(R.drawable.ic_dash_device_default)

                binding.navView.apply {
                    ivGlowHome.gone()
                    ivGlowSleep.gone()
                    ivGlowReadiness.visible()
                    ivGlowActivity.gone()
                    ivGlowMyDevice.gone()
                }
                val lastDestination = navController?.currentDestination
                if (lastDestination?.id != R.id.navigation_oreo_readiness) {
                    navController?.popBackStack(R.id.navigation_oreo_readiness, true)
                    navController?.navigate(R.id.navigation_oreo_readiness)
                }
                viewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_FOOTER_READINESS_CLICK)

            }

            BottomNavOption.ACTIVITY -> {
                binding.navView.ivHome.setImageResource(R.drawable.ic_dash_summary_default)
                binding.navView.ivSleep.setImageResource(R.drawable.ic_dash_oreo_sleep_default)
                binding.navView.ivReadiness.setImageResource(R.drawable.ic_dash_oreo_readiness_default)
                binding.navView.ivActivity.setImageResource(R.drawable.ic_dash_oreo_activity)
                binding.navView.ivMyDevice.setImageResource(R.drawable.ic_dash_device_default)
                binding.navView.apply {
                    ivGlowHome.gone()
                    ivGlowSleep.gone()
                    ivGlowReadiness.gone()
                    ivGlowActivity.visible()
                    ivGlowMyDevice.gone()
                }
                val lastDestination = navController?.currentDestination
                if (lastDestination?.id != R.id.navigation_oreo_workouts) {
                    navController?.popBackStack(R.id.navigation_oreo_workouts, true)
                    navController?.navigate(R.id.navigation_oreo_workouts)
                }
                viewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_FOOTER_ACTIVITY_CLICK)
            }

            else -> {}
        }


    }

    override fun getViewBinding() = ActivityOreoMainBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding = binding.progressBar

    override fun logAppEvent(eventName: String, data: HashMap<String, Any?>) {

    }
}

enum class BottomNavOption {
    HOME, SLEEP, READINESS, ACTIVITY, MY_DEVICE
}