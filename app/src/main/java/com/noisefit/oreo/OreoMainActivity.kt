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
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ActivityOreoMainBinding
import com.noisefit.ui.APP_CONTINUE
import com.noisefit.ui.APP_EXIT
import com.noisefit.ui.APP_UPDATE
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit.ui.common.BaseActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.response.VersionCheckResponse
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.share.ShareUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OreoMainActivity : BaseActivity<ActivityOreoMainBinding>() {

    private val viewModel: OreoMainViewModel by viewModels()
    private var navController: NavController? = null

    private val btAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }
    private val REQUEST_ENABLE_BT = 133

    companion object {
        fun getStartIntent(
            context: Context, notificationType: String? = null,
            notificationIndex: String? = null,
            deeplink: String? = null
        ): Intent {
            return Intent(context, OreoMainActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navController = findNavController(com.noisefit.luna.R.id.o_nav_host_fragment)
        binding.navView.itemIconTintList = null
        binding.navView.setOnItemReselectedListener {
            return@setOnItemReselectedListener
        }

        binding.navView.setOnItemSelectedListener {
            val lastDestination = navController?.currentDestination

            when (it.itemId) {
                R.id.navigation_oreo_home -> {
                    if (lastDestination?.id != R.id.navigation_oreo_home) {
                        navController?.popBackStack(R.id.navigation_oreo_home, true)
                        navController?.navigate(R.id.navigation_oreo_home)
                    }
                }

                R.id.navigation_oreo_readiness -> {
                    if (lastDestination?.id != R.id.navigation_oreo_readiness) {
                        navController?.popBackStack(R.id.navigation_oreo_readiness, true)
                        navController?.navigate(R.id.navigation_oreo_readiness)
                    }
                }

                R.id.navigation_oreo_workouts -> {
                    if (lastDestination?.id != R.id.navigation_oreo_workouts) {
                        navController?.popBackStack(R.id.navigation_oreo_workouts, true)
                        navController?.navigate(R.id.navigation_oreo_workouts)
                    }
                }

                R.id.navigation_oreo_sleep -> {
                    if (lastDestination?.id != R.id.navigation_oreo_sleep) {
                        navController?.popBackStack(R.id.navigation_oreo_sleep, true)
                        navController?.navigate(R.id.navigation_oreo_sleep)
                    }
                }

                R.id.navigation_oreo_my_device -> {
                    if (lastDestination?.id != R.id.navigation_oreo_my_device) {
                        navController?.popBackStack(R.id.navigation_oreo_my_device, true)
                        navController?.navigate(R.id.navigation_oreo_my_device)
                    }
                }


            }
            true
        }

        viewModel.sessionManager.getPairedState()
        checkBluetooth()

    }

    private fun setBottomMenu(colorFitDevice: ColorFitDevice?) {
        val lastDestination = navController?.currentDestination


        //setSelected()
        lastDestination?.let {
            if (it.id == R.id.navigation_oreo_home) {
                binding.navView.selectedItemId = com.noisefit.luna.R.id.navigation_summary
            }
        }
    }

    override fun initListener() {

    }

    fun checkBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkBluetoothPermission(permissionGranted = {
                if (btAdapter != null && !btAdapter.isEnabled) {
                    enableBluetooth()
                }
            })
        } else {
            if (btAdapter == null) {
                return
            }
            if (!btAdapter.isEnabled) {
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
                ShareUtil.openPlayStore(this@OreoMainActivity, "com.noisefit")
            }
        }
        navController?.navigate(R.id.appUpdateBottomSheet, Bundle().apply {
            putParcelable("versonResponse", it)
        })
    }

    override fun observeSubscriber() {

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
                    BottomNavOption.HOME -> {}
                    BottomNavOption.EXPLORE -> {
                        binding.navView.selectedItemId = R.id.navigation_oreo_sleep
                    }

                    BottomNavOption.SHOP -> {
                        binding.navView.selectedItemId = R.id.navigation_oreo_readiness
                    }

                    BottomNavOption.MY_DEVICE -> {
                        binding.navView.selectedItemId = R.id.navigation_oreo_my_device
                    }

                    BottomNavOption.COMMUNITY -> {
                        binding.navView.selectedItemId = R.id.navigation_oreo_workouts
                    }
                }
            }
        }
    }

    private val navListener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.navigation_oreo_home,
                R.id.navigation_oreo_readiness,
                R.id.navigation_oreo_workouts,
                R.id.navigation_oreo_my_device,
                R.id.navigation_oreo_sleep
                -> {
                    binding.navView.visible()
                }

                else -> binding.navView.gone()
            }
        }

    override fun onResume() {
        super.onResume()
        navController?.addOnDestinationChangedListener(navListener)

        viewModel.ringDataStore.getRingDevice()?.let {
            if (viewModel.sessionManager.connectStateRing.value == null) {
                viewModel.sessionManager.setConnectStateRing(ConnectState.Connecting(it))
                ApplicationUtils.setRescueWorkManager(this)
            }
        }


    }

    override fun onPause() {
        navController?.removeOnDestinationChangedListener(navListener)
        super.onPause()
    }

    override fun onBackPressed() {
        navController?.let {
            when (it.currentDestination?.id) {
                R.id.navigation_oreo_home,
                R.id.navigation_oreo_readiness,
                R.id.navigation_oreo_workouts,
                R.id.navigation_oreo_sleep,
                R.id.navigation_oreo_my_device -> {

                    if (it.currentDestination?.id == R.id.navigation_oreo_home) {
                        finish()
                    } else {
                        binding.navView.selectedItemId = com.noisefit.luna.R.id.navigation_oreo_home
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

    override fun getViewBinding() = ActivityOreoMainBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding = binding.progressBar

    override fun logAppEvent(eventName: String, data: HashMap<String, Any?>) {

    }
}
enum class BottomNavOption {
    HOME, EXPLORE, SHOP, MY_DEVICE, COMMUNITY
}