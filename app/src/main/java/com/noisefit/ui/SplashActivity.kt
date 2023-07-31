package com.noisefit.ui

//import com.clevertap.android.sdk.CleverTapAPI
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.work.WorkManager
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.BuildConfig
import com.noisefit.MainActivity
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.luna.databinding.ActivitySplashBinding
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit.oreo.OreoMainActivity
import com.noisefit.ui.common.BaseActivity
import com.noisefit_commans.ui.playAnimation
import com.noisefit.ui.onboarding.OnBoardActivity
import com.noisefit.ui.onboarding.PrivacyBottomDialogFragment
import com.noisefit.util.ApplicationUtils
import com.noisefit.ui.onboarding.onboardProfile.ProfileSetupActivity
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit.util.UniqueDiyWatchFaceSyncWorkName
import com.noisefit.util.UniqueWatchFaceSyncWorkName
import com.noisefit.util.notif.NotificationEventsClass.NOTIFICATION_BUNDLE_INDEX
import com.noisefit.util.notif.NotificationEventsClass.NOTIFICATION_BUNDLE_LINK
import com.noisefit.util.notif.NotificationEventsClass.NOTIFICATION_BUNDLE_TYPE
import com.noisefit.util.notif.NotificationEventsClass.NOTIFICATION_INDEX_EXTRA
import com.noisefit.util.notif.NotificationEventsClass.NOTIFICATION_LINK
import com.noisefit.util.notif.NotificationEventsClass.NOTIFICATION_TYPE_EXTRA
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    private val viewModel: SplashViewModel by viewModels()

    companion object {
        fun getStartIntent(context: Context): Intent {
            return Intent(context, SplashActivity::class.java)
        }
    }

    @SuppressLint("LogNotTimber")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.lottieBackAnim.playAnimation(
            LottieDrawable.INFINITE,
            R.raw.anim_splash_screen
        )

        ApplicationUtils.startNotificationListenerService(
            viewModel.localDataStore,
            applicationContext
        )

        binding.tvAppVersion.text =
            getString(R.string.app_version, ApplicationUtils.getAppVersion())
        viewModel.setIgnoreVersion(viewModel.localDataStore.getIgnoreVersion())
        handleBackgroundNotifications(intent)

        if (viewModel.connectedDevice != null) {
            checkPermissionAndStartService()
        } else {
            startOnBoardFlow()
        }
        viewModel.checkAppVersion()
        viewModel.initFirstOpen()

    }


    private fun startOnBoardFlow() {
        if (BuildConfig.DEBUG) {
            viewModel.checkOnBoardingFlow()
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    viewModel.checkOnBoardingFlow()
                } catch (exp: Exception) {
                    //Context null handling
                    exp.printStackTrace()
                }
            }, 3000)
        }

        dirtyWorkers()
    }

    private fun checkPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkBluetoothPermission {

                val alarmManager: AlarmManager =
                    getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) {
                    showAllowAlarmPermission()
                    return@checkBluetoothPermission
                }
                ApplicationUtils.setRescueWorkManager(this)
                startOnBoardFlow()
            }
        } else {
            ApplicationUtils.setRescueWorkManager(this)
            startOnBoardFlow()
        }
    }

    private fun dirtyWorkers() {
        WorkManager.getInstance(this)
            .cancelUniqueWork(UniqueWatchFaceSyncWorkName)
        WorkManager.getInstance(this)
            .cancelUniqueWork(UniqueDiyWatchFaceSyncWorkName)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkBluetoothPermission(
        permissionGranted: () -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
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
            ApplicationUtils.setRescueWorkManager(this)
            startOnBoardFlow()
        } else {
            onApiErrorReceived(
                ErrorResponse(
                    UIComponentType.AreYouSureDialog(
                        getString(R.string.text_permission_required),
                        getString(R.string.text_permission_denial_near_by_device_bluetooth),
                        false,
                        getString(R.string.text_allow),
                        object : BinaryActionCallback {
                            override fun yes() {
                                ApplicationUtils.openAppSettings(this@SplashActivity)
                                finish()
                            }

                            override fun no() {
                                finish()
                            }
                        }
                    )
                )
            )

        }
    }

    private fun showAllowAlarmPermission() {
        onApiErrorReceived(
            ErrorResponse(
                UIComponentType.AreYouSureDialog(
                    getString(R.string.text_permission_required),
                    getString(R.string.text_permission_denial_alarm),
                    false,
                    getString(R.string.text_allow),
                    object : BinaryActionCallback {
                        override fun yes() {
                            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))

                            finish()
                        }

                        override fun no() {
                            finish()
                        }
                    }
                )
            )
        )

    }

    private fun handleBackgroundNotifications(intent: Intent?) {


        intent?.extras?.run {
            if (get(NOTIFICATION_TYPE_EXTRA) == null && get(NOTIFICATION_INDEX_EXTRA) == null) {
                handleNotifications(intent)
                return
            }
            viewModel.notificationType = get(NOTIFICATION_TYPE_EXTRA) as String?
            viewModel.notificationIndex = get(NOTIFICATION_INDEX_EXTRA) as String?
            viewModel.deeplink = get(NOTIFICATION_LINK) as String?
            intent.data = null
            intent.putExtra(NOTIFICATION_BUNDLE_TYPE, "")
        }


    }

    private fun goToDashboard() {
        startActivity(
            MainActivity.getStartIntent(
                this,
                viewModel.notificationType,
                viewModel.notificationIndex,
                viewModel.deeplink
            )
        )
        finish()
    }

    private fun goToOreoDashboard() {
        startActivity(
            OreoMainActivity.getStartIntent(
                this,
                viewModel.notificationType,
                viewModel.notificationIndex,
                viewModel.deeplink
            )
        )
        finish()
    }


    private fun goToOnBoardScreens() {
        startActivity(OnBoardActivity.getStartIntent(this))
        finish()
    }

    private fun showPrivacyBottomSheet() {
        val bottomSheet =
            PrivacyBottomDialogFragment()
        bottomSheet.isCancelable = false
        bottomSheet.setPrivacyBottomInteractionListener(object :
            PrivacyBottomDialogFragment.PrivacyBottomInteractionListener {
            override fun onPrivacyStatus(accepted: Boolean) {
                if (accepted) {
                    viewModel.updatePrivacyPolicyStatus(true)
                    viewModel.checkOnBoardingFlow()
                } else {
                    finish()
                }
            }

        })
        bottomSheet.show(supportFragmentManager, "dialog")
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        handleNotifications(intent)
    }

    private fun handleNotifications(intent1: Intent?) {

        intent1?.let {

            viewModel.notificationType = it.getStringExtra(NOTIFICATION_BUNDLE_TYPE)
            viewModel.notificationIndex = it.getStringExtra(NOTIFICATION_BUNDLE_INDEX)
            viewModel.deeplink = it.getStringExtra(NOTIFICATION_BUNDLE_LINK)
            intent1.data = null
            intent1.putExtra(NOTIFICATION_BUNDLE_TYPE, "")
            LOGS.d("NEW_NOTIFICATION_TYPE ${viewModel.notificationType} ${viewModel.notificationIndex} ${viewModel.deeplink}")
//            openWebPage(it)
        }
    }

    override fun logAppEvent(eventName: String, data: HashMap<String, Any?>) {

    }

    override fun initListener() {

    }


    override fun observeSubscriber() {
        viewModel.userOnBoardingFlow.observe(this) { userOnBoardingFlow ->
            if (userOnBoardingFlow != null) {
//                startActivity(OreoMainActivity.getStartIntent(this))
                when (userOnBoardingFlow) {
                    UserOnBoardingFlow.ACCEPT_PRIVACY_POLICY -> {
                        showPrivacyBottomSheet()
                    }

                    UserOnBoardingFlow.ASK_FOR_LOGIN -> {
                        goToOnBoardScreens()
                    }

                    UserOnBoardingFlow.SHOW_DASHBOARD -> {
                        goToDashboard()
                    }

                    UserOnBoardingFlow.SETUP_PROFILE -> {
                        startActivity(ProfileSetupActivity.getStartIntent(this))
                        finish()
                    }

                    UserOnBoardingFlow.PAIR_DEVICE->{
                        startActivity(PairDeviceActivity.getStartIntent(this))
                        finish()
                    }

                    UserOnBoardingFlow.SHOW_OREO_DASHBOARD -> {
                        goToOreoDashboard()
                    }
                }
            }
        }
    }

    override fun getViewBinding() = ActivitySplashBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding? = null
}