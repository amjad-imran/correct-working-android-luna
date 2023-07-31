package com.noisefit

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.findNavController
import com.airbnb.lottie.*
import com.airbnb.lottie.LottieDrawable.INFINITE
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.review.ReviewManagerFactory
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType

import com.noisefit_commans.data.model.BatteryNotificationType
import com.noisefit.databinding.ActivityMainBinding

import com.noisefit.databinding.DialogForceOtaBinding
import com.noisefit.ui.APP_CONTINUE
import com.noisefit.ui.APP_EXIT
import com.noisefit.ui.APP_UPDATE
import com.noisefit.receiver.workManager.WatchFaceTransferStates
import com.noisefit.ui.AppVersionDialogFragment
import com.noisefit.ui.SplashActivity
import com.noisefit.ui.common.*
import com.noisefit.ui.dashboard.summary.SummaryFragmentDirections
import com.noisefit.ui.myDevice.manage.CheckForUpdatesViewModel
import com.noisefit.ui.onboarding.FirebaseUpdateViewModel
import com.noisefit.ui.onboarding.OnBoardActivity
import com.noisefit.ui.onboarding.pairing.DeviceSetupViewModel
import com.noisefit.ui.settings.helpAndSupport.HelpAndSupportType
import com.noisefit.ui.trophies.TrophiesType
import com.noisefit.util.*
import com.noisefit.util.moveToServer.CHARGE_REMINDER
import com.noisefit.util.notif.NotificationEventsClass
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.ui.*
import com.noisefit_commans.data.enums.HealthOverViewHistoryType
import com.noisefit_commans.data.response.VersionCheckResponse
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import java.io.File
import java.io.FileOutputStream


@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(), LocationListener,
    OnCompleteListener<LocationSettingsResponse> {


    private val REQUEST_ENABLE_BT = 146

    private val viewModel: MainViewModel by viewModels()
    private val checkForUpdatesVM: CheckForUpdatesViewModel by viewModels()
    private val deviceSetupViewModel: DeviceSetupViewModel by viewModels()
    private val firebaseViewModel: FirebaseUpdateViewModel by viewModels()

    private val TAG = MainActivity::class.java.simpleName

    private var navController: NavController? = null
    private var minimumBatteryLevel = 30
    private var locationManager: LocationManager? = null

    private val btAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }


    companion object {
        private const val NotificationType = "NotificationType"
        private const val NotificationIndex = "NotificationIndex"
        private const val NotificationDeeplink = "NotificationDeeplink"
        const val OPEN_PROFILE = "OPEN_PROFILE"
        private const val REQUEST_CHECK_SETTINGS = 41

        fun getStartIntent(
            context: Context,
            notificationType: String?,
            notificationIndex: String?,
            deeplink: String?
        ): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(NotificationType, notificationType)
            intent.putExtra(NotificationIndex, notificationIndex)
            intent.putExtra(NotificationDeeplink, deeplink)
            return intent
        }

        fun getStartIntent(context: Context, openProfile: Boolean = false): Intent {
            return Intent(context, MainActivity::class.java).apply {
                this.putExtra(OPEN_PROFILE, openProfile)
            }

        }
    }

    private fun disconnectBadge(show: Boolean, isConnected: Boolean) {
        val badge = binding.navView.getOrCreateBadge(R.id.navigation_device)
        badge.isVisible = show
        if (isConnected) {
            badge.backgroundColor = getColor(R.color.badge_color_connected)
        } else {
            badge.backgroundColor = getColor(R.color.badge_color)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.lottieBackAnim.playAnimation(
            INFINITE, R.raw.anim_challenge_back
        )

        if (viewModel.localDataStore.getUser() == null) {
            startActivity(OnBoardActivity.getStartIntent(this, true))
            return
        }



        if (viewModel.localDataStore.getConnectedDevice() != null) {
            if (!hasRequiredBluetoothPermission()) {
                startActivity(SplashActivity.getStartIntent(this).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
        }

        minimumBatteryLevel = viewModel.watchesSDK.getMinimumBatteryLevel()


        binding.navView.itemIconTintList = null

        navController = findNavController(R.id.nav_host_fragment)

        //setupActionBarWithNavController(navController, appBarConfiguration)
        //navView.setupWithNavController(navController!!)

        binding.navView.setOnItemReselectedListener {
            return@setOnItemReselectedListener
        }

//        animateFriendsIcon()
        binding.navView.setOnItemSelectedListener {
            val lastDestination = navController?.currentDestination

            when (it.itemId) {
                R.id.navigation_summary -> {
                    if (lastDestination?.id != R.id.summaryFragment) {
                        logInsiderAppEvent(InsiderAppEvents.FOOTER_SUMMARY_CLICK)
                        navController?.popBackStack(R.id.summaryFragment, true)
                        navController?.navigate(R.id.summaryFragment)
                    }
                }

                R.id.navigation_challenge -> {
                    if (lastDestination?.id != R.id.navigation_challenge) {
                        logInsiderAppEvent(InsiderAppEvents.FOOTER_CHALLENGES_CLICK)
                        navController?.popBackStack(R.id.navigation_challenge, true)
                        navController?.navigate(R.id.navigation_challenge)
                    }
                }

                R.id.navigation_friends -> {
                    it.setIcon(R.drawable.ic_friends_navigation)
                    if (lastDestination?.id != R.id.navigation_friends) {
                        logInsiderAppEvent(InsiderAppEvents.FOOTER_COMMUNITY_CLICK)
                        it.setIcon(R.drawable.ic_dash_friends_selector)
                        navController?.popBackStack(R.id.navigation_friends, true)
                        navController?.navigate(R.id.navigation_friends)
                    }
                }

                R.id.navigation_shop -> {
                    if (lastDestination?.id != R.id.navigation_shop) {
                        logInsiderAppEvent(InsiderAppEvents.FOOTER_SHOP_CLICK)
                        navController?.popBackStack(R.id.navigation_shop, true)
                        navController?.navigate(R.id.navigation_shop)
                    }
                }

                R.id.navigation_device -> {
                    if (lastDestination?.id != R.id.navigation_device) {
                        logInsiderAppEvent(InsiderAppEvents.FOOTER_MYDEVICES_CLICK)
                        navController?.popBackStack(R.id.navigation_device, true)
                        navController?.navigate(R.id.navigation_device)
                    }
                }

            }
            true
        }

        firebaseViewModel.generateToken()
        //viewModel.updateUserAdditionalDetails()
        viewModel.sessionManager.getPairedState()

        checkBluetooth()

        if (!BuildConfig.DEBUG) {
            if (viewModel.hasCrashLog()) {
                writeToFile(viewModel.localDataStore.getCrashLog(), this@MainActivity)
            }
        }


        if (!viewModel.localDataStore.isAutoStartEnabled()) {
            if (viewModel.autoStartUtil.isSupportedAndroid(this)) {
                viewModel.autoStartUtil.showAlert(
                    this
                ) { dialog, which ->
                    viewModel.autoStartUtil.startAutostartSettings(this)
                    viewModel.localDataStore.setAutoStart(true)
                }
            }
        }
        intent?.let {
            Handler(Looper.getMainLooper()).postDelayed({
                handleIntent(it)
                if (intent.getBooleanExtra(OPEN_PROFILE, false)) {
                    navController?.navigate(R.id.myProfileFragment)
                }
            }, 500)


        }

        /**
         * In app review
         */
        val shouldShowReview = viewModel.getShouldShowReview()
        LOGS.d(TAG, "Should Show Review $shouldShowReview")
        if (shouldShowReview) {
            startAppReviewFlow()
        }



        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            try { // please replace it with sdk check
                viewModel.sessionManager.connectedDevice.value?.let {
                    if (it.deviceType == DeviceType.NOISE_EVOLVE_2.deviceType ||
                        it.deviceType == DeviceType.COLORFIT_PULSE_2.deviceType ||
                        it.deviceType == DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType ||
                        it.deviceType == DeviceType.COLORFIT_PRO_4.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_TWIST.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_CURVE.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_ARC.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_HALO_PLUS.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_HALO.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_ORIGIN.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_EVOLVE_4.deviceType ||
                        it.deviceType == DeviceType.COLORFIT_PRO_4_GPS.deviceType ||
                        it.deviceType == DeviceType.COLORFIT_PRO_4_ALPHA.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_QUAD_CALL_MAX.deviceType ||
                        it.deviceType == DeviceType.COLORFIT_CALIBER_2_BUZZ.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_EVOLVE_3.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_FUSE_PLUS.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_ARC_PLUS.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_FUSE.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_VORTEX.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_FORCE_PLUS.deviceType ||
                        it.deviceType == DeviceType.PULSE_GO_BUZZ.deviceType ||
                        it.deviceType == DeviceType.COLORFIT_CALIBER_BUZZ.deviceType ||
                        it.deviceType == DeviceType.VISION_2_BUZZ.deviceType ||
                        it.deviceType == DeviceType.COLORFIT_PULSE_2_MAX.deviceType ||
                        it.deviceType == DeviceType.COLORFIT_LOOP.deviceType ||
                        it.deviceType == DeviceType.COLORFIT_VICTOR.deviceType ||
                        it.deviceType == DeviceType.COLORFIT_CALIBER_2.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_CREW.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_CREW_PRO.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_METTLE.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_TWIST_PRO.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_METALLIX.deviceType ||
                        it.deviceType == DeviceType.COLORFIT_PULSE_3.deviceType ||
                        it.deviceType == DeviceType.COLORFIT_PRIMUS.deviceType ||
                        it.deviceType == DeviceType.ULTRA_3.deviceType ||
                        it.deviceType == DeviceType.COLORFIT_VISION_3.deviceType ||
                        it.deviceType == DeviceType.COLORFIT_ORE.deviceType ||
                        it.deviceType == DeviceType.COLORFIT_PRO_5_47MM.deviceType ||
                        it.deviceType == DeviceType.COLORFIT_PRO_5_44MM.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_ACTIVE_2.deviceType ||
                        it.deviceType == DeviceType.COLORFIT_CHROME.deviceType ||
                        it.deviceType == DeviceType.NOISEFIT_ENDEAVOUR.deviceType
                    ) {
                        if (!viewModel.watchDataStore.isInitialOtaChecked()) {
                            checkForUpdatesVM.checkForUpdates(true)
                        } else {
                            viewModel.sessionManager.needDfuUpdate.observe(this) {
                                it.getContent()?.let { inDfu ->
                                    if (inDfu) {
                                        if (viewModel.sessionManager.forceOtaResponse == null) {
                                            checkForUpdatesVM.checkForUpdates(true)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (exp: Exception) {
            }
        }, 500)
        viewModel.checkConfigData()
        viewModel.checkUserLocationStatus()
        viewModel.checkUserInterestStatus()
    }


    private fun animateFriendsIcon() {
        val drawable = LottieDrawable()
        LottieCompositionFactory.fromRawRes(this, R.raw.anim_menu_friends).addListener { comp ->
            drawable.composition = comp
        }
        val menuItem = binding.navView.menu.findItem(R.id.navigation_friends)
        menuItem.icon = drawable
        val icon = menuItem.icon as? LottieDrawable
        icon?.apply {
            icon.repeatCount = INFINITE
            playAnimation()
        }
    }


    /* private fun generateHash() {
         val singleHash = BCrypt.hashpw("arun131!", BCrypt.gensalt())
         val multiHash = BCrypt.hashpw("arun131!", BCrypt.gensalt(12))

         println("Password : $singleHash")
         println("Password : $multiHash")

         val testPass = "\$2a\$12\$n0fIT60Qf1vSimEtFh2QsuaQFSSbFd81bU7fBHyfWYSBSycXSa0Bu"


         if (BCrypt.checkpw("arun131!", testPass))
             println("Password : It matches");
         else
             println("Password : It does not match");

     }*/


    private fun logInsiderAppEvent(eventName: String) {
        viewModel.sessionManager.logInsiderAppEvent(eventName)
    }

    private fun startAppReviewFlow() {
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                }
            }
        }
    }


    //
//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
////        handleNotifications(intent)
//    }

    //    private fun handleNotifications(intent: Intent?) {
//        intent?.getStringExtra(NOTIFICATION_BUNDLE_TYPE)?.let {
//            LOGS.d("NEW_NOTIFICATION_TYPE $it")
//        }
//    }

    //TODO:Handle it through deeplink

    private fun handleNotificationType(
        notificationType: String, notificationIndex: String, deeplink: String?
    ) {

        logEvent(notificationType)
        LOGS.d("handleNotificationType $notificationType $deeplink")

        if (!deeplink.isNullOrEmpty()) {
            try {
                val request = NavDeepLinkRequest.Builder.fromUri(deeplink.toUri()).build()
                navController?.navigate(request)
                return
            } catch (exp: Exception) {
                LOGS.d("Invalid deeplink")
                exp.printStackTrace()
            }
        }
        when (notificationType.lowercase()) {
            NotificationEventsClass.NOTIFICATION_TYPE_AGPS_FORCE_UPDATE -> {
                viewModel.navigateTo(BottomNavOption.MY_DEVICE)
            }

            "step_goal_achieved" -> {

            }

            NotificationEventsClass.NOTIFICATION_TYPE_NPL_DASH -> {
                viewModel.playNplAnim = true
                navController?.navigate(R.id.nplDashboardFragment)
            }

            NotificationEventsClass.NOTIFICATION_TYPE_TROPHIES -> {
                LOGS.d("handleNotificationType inside trophies $notificationType")
                var index = 0
                if (notificationIndex.isNotEmpty()) {
                    index = notificationIndex.toInt()
                }

                val default = if (index == 1) {
                    TrophiesType.DISTANCE.name
                } else {
                    TrophiesType.STEPS.name
                }
                navController?.popBackStack(R.id.trophiesFragment, true)
                navController?.navigate(R.id.trophiesFragment, Bundle().apply {
                    putString("selectedDefault", default)
                })
            }

            NotificationEventsClass.NOTIFICATION_TYPE_ACTIVITY_HISTORY -> {
                navController?.popBackStack(R.id.navigation_activity, true)
                navController?.navigate(R.id.navigation_activity)
            }

            NotificationEventsClass.NOTIFICATION_TYPE_SETTINGS -> {
                navController?.popBackStack(R.id.settingFragment, true)
                navController?.navigate(R.id.settingFragment)
            }


            NotificationEventsClass.NOTIFICATION_TYPE_USER_ACCEPT_REQUEST -> {
                var index = -1
                if (notificationIndex.isNotEmpty()) {
                    index = notificationIndex.toInt()
                }

                navController?.popBackStack(R.id.friendProfileFragment, true)
                navController?.navigate(R.id.friendProfileFragment, Bundle().apply {
                    putInt("friendId", index)
                })
            }

            NotificationEventsClass.POST_DETAIL_KEY -> {
                if (notificationIndex.isNotEmpty()) {
                    navController?.popBackStack(R.id.postDetailsFragment, true)
                    navController?.navigate(R.id.postDetailsFragment, Bundle().apply {
                        putLong("id", notificationIndex.toLong())
                    })
                }
            }

            NotificationEventsClass.NOTIFICATION_TYPE_USER_PROFILE_REQUEST -> {

                navController?.popBackStack(R.id.friendProfileFragment, true)
                navController?.navigate(R.id.friendProfileFragment, Bundle().apply {
                    putInt("friendId", -1)
                })
            }

            NotificationEventsClass.NOTIFICATION_TYPE_FRIEND_REQUEST_RECEIVED -> {
                navController?.popBackStack(R.id.requestFragment, true)
                navController?.navigate(R.id.requestFragment)
            }

            NotificationEventsClass.NOTIFICATION_TYPE_COMPETE_REQUEST_RECEIVED -> {
                navController?.popBackStack(R.id.requestFragment, true)
                navController?.navigate(R.id.requestFragment)
            }

            NotificationEventsClass.NOTIFICATION_TYPE_USER_ACCEPT_COMPETE_REQUEST -> {
                navController?.popBackStack(R.id.navigation_friends, true)
                navController?.navigate(R.id.navigation_friends, Bundle().apply {
                    putInt("friendsactivity", 1)
                })
            }
            //TODO - for next release
//            NotificationEventsClass.NOTIFICATION_TYPE_CHALLENGE_DETAIL -> {
//                 val deeplinkUri =
//                     "https://noisefit.page.link/ChallengeDetails?id=$notificationIndex"
//                 navController?.navigate(deeplinkUri)
//             }


            NotificationEventsClass.NOTIFICATION_TYPE_DEVICE_INFO -> {
                navController?.popBackStack(R.id.navigation_device, true)
                navController?.navigate(R.id.navigation_device)
            }

            NotificationEventsClass.NOTIFICATION_TYPE_WATCH_LOW_BATTERY -> {
                var index = 10
                if (notificationIndex.isNotEmpty()) {
                    index = notificationIndex.toInt()
                }
                LOGS.d("LOGS_FIREBASE_EVENT $index")
                if (index <= 1) {
                    navController?.popBackStack(R.id.helpAndSupportListFragment, true)
                    navController?.navigate(R.id.helpAndSupportListFragment, Bundle().apply {
                        putString("type", HelpAndSupportType.BATTERY_AND_CHARGING.name)
                        putParcelable("helpSupportItem", null)
                    })
                } else {
                    navController?.popBackStack(R.id.summaryFragment, true)
                    navController?.navigate(R.id.summaryFragment)
                }

                logFirebaseEvent(index)

            }

            NotificationEventsClass.NOTIFICATION_TYPE_SHOP -> {
                navController?.popBackStack(R.id.navigation_shop, true)
                navController?.navigate(R.id.navigation_shop)
            }
//            NotificationEventsClass.NOTIFICATION_TYPE_BUDDY -> {
//                navController?.popBackStack(R.id.requestFragment, true)
//                navController?.navigate(R.id.requestFragment, Bundle())
//            }
//            NotificationEventsClass.NOTIFICATION_TYPE_MANAGE -> {
//                navController?.popBackStack(R.id.myBuddiesFragment, true)
//                navController?.navigate(R.id.myBuddiesFragment, Bundle().apply {
//                    putString("currentItem", "1")
//                })
//            }
            NotificationEventsClass.NOTIFICATION_TYPE_ACTIVITY_CHALLENGE -> {
                viewModel.navigateTo(BottomNavOption.EXPLORE)
            }

            NotificationEventsClass.NOTIFICATION_TYPE_WATCH_FACES -> {
                navController?.popBackStack(R.id.handleDeepLinkWatchfaceFragment, true)
                navController?.navigate(R.id.handleDeepLinkWatchfaceFragment)
            }

            NotificationEventsClass.NOTIFICATION_TYPE_STEPS -> {
                navController?.popBackStack(R.id.stepsDetailsFragment, true)
                navController?.navigate(R.id.stepsDetailsFragment, Bundle().apply {
                    putString("type", HealthOverViewHistoryType.Steps.name)
                })
            }

            NotificationEventsClass.LOCAL_SLEEP_NOTIFICATION_KEY -> {
                navController?.popBackStack(R.id.sleepDetailsFragment, true)
                navController?.navigate(R.id.sleepDetailsFragment)
            }

            NotificationEventsClass.NOTIFICATION_TYPE_DISTANCE -> {
                navController?.popBackStack(R.id.stepsDetailsFragment, true)
                navController?.navigate(R.id.stepsDetailsFragment, Bundle().apply {
                    putString("type", HealthOverViewHistoryType.Distance.name)
                })
            }

            NotificationEventsClass.NOTIFICATION_TYPE_SLEEP_SCREEN -> {
                navController?.popBackStack(R.id.sleepDetailsFragment, true)
                navController?.navigate(R.id.sleepDetailsFragment)
            }

            NotificationEventsClass.NOTIFICATION_TYPE_HEART_RATE -> {
                navController?.popBackStack(R.id.heartRateDetailsFragment, true)
                navController?.navigate(R.id.heartRateDetailsFragment)
            }

            NotificationEventsClass.NOTIFICATION_TYPE_STRESS -> {
                navController?.popBackStack(R.id.stressDetailsFragment, true)
                navController?.navigate(R.id.stressDetailsFragment)
            }

            NotificationEventsClass.NOTIFICATION_TYPE_FEEDBACK_SCREEN -> {
                navController?.popBackStack(R.id.navigation_activity_feedback, true)
                navController?.navigate(R.id.navigation_activity_feedback)
            }
        }
    }


    private fun logEvent(type: String) {
        if (type.isNotEmpty()) {
            viewModel.sessionManager.logInsiderAppEvent(type)
        }

    }

    private fun logFirebaseEvent(type: Int) {
        var batteryNotificationType = ""
        when (type) {
            0 -> {
                batteryNotificationType =
                    com.noisefit_commans.data.model.BatteryNotificationType.CRITICAL_REMINDER.name
            }

            1 -> {
                batteryNotificationType =
                    com.noisefit_commans.data.model.BatteryNotificationType.CRITICAL.name
            }

            2 -> {
                batteryNotificationType =
                    com.noisefit_commans.data.model.BatteryNotificationType.FULL.name
            }

            3 -> {
                batteryNotificationType = CHARGE_REMINDER
            }
        }
//        viewModel.sessionManager.logFirebaseEvent(
//            AppEvents.LOW_BATTERY_NOTIFICATION_CLICKED,
//            HashMap<String, Any>().apply {
//                this["type"] = batteryNotificationType.lowercase()
//            }
//        )
        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.LOW_BATTERY_NOTIFICATION_CLICKED,
            HashMap<String, Any>().apply {
                this["type"] = batteryNotificationType.lowercase()
            })


    }

    private fun handleIntent(intent: Intent?) {
        intent?.extras?.let { intentExtra ->
            if (intentExtra.containsKey(NotificationType)) {
                LOGS.d("NEW_NOTIFICATION_TYPE  ${intentExtra.getString(NotificationType)}")
                handleNotificationType(
                    intentExtra.getString(NotificationType) ?: "",
                    intentExtra.getString(NotificationIndex) ?: "0",
                    intentExtra.getString(NotificationDeeplink)
                )
                intent.putExtra(NotificationType, "")
            }

        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
//        viewModel.updateUserAdditionalDetails()
        LOGS.d(TAG, "onNewIntent")


        if (viewModel.localDataStore.getConnectedDevice() != null) {
            if (!hasRequiredBluetoothPermission()) {
                startActivity(SplashActivity.getStartIntent(this).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
        }

        handleIntent(intent)

        navController?.handleDeepLink(intent)
    }

    fun hasRequiredBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun setBottomMenu(colorFitDevice: ColorFitDevice?) {
        val lastDestination = navController?.currentDestination
//        val menu = binding.navView.menu
//        menu.let {
//            menu.clear()
//            menu.add(
//                Menu.NONE,
//                R.id.navigation_summary,
//                Menu.NONE,
//                getString(R.string.text_summary)
//            ).setIcon(R.drawable.ic_dash_summary_selector)
//
//            if (!AppUtil.isOutSideIndia()) {
//                menu.add(Menu.NONE, R.id.navigation_shop, Menu.NONE, getString(R.string.text_shop))
//                    .setIcon(R.drawable.ic_dash_shop_selector)
//            }
//            colorFitDevice?.let {
//                if (!it.deviceType.equals(DeviceType.NOISEFIT_HYBRID.deviceType, true)) {
//                    menu.add(
//                        Menu.NONE,
//                        R.id.navigation_activity,
//                        Menu.NONE,
//                        getString(R.string.text_activity)
//                    ).setIcon(R.drawable.ic_dash_activity_selector)
//                }
//                menu.add(
//                    Menu.NONE,
//                    R.id.navigation_watchface,
//                    Menu.NONE,
//                    getString(R.string.text_watchfaces)
//                ).setIcon(R.drawable.ic_dash_watchface_selector)
//            }
//
//            menu.add(
//                Menu.NONE,
//                R.id.navigation_settings,
//                Menu.NONE,
//                getString(R.string.text_settings)
//            ).setIcon(R.drawable.ic_dash_settings_selector)
//        }

        //setSelected()
        lastDestination?.let {
            if (it.id == R.id.summaryFragment) {
                binding.navView.selectedItemId = R.id.navigation_summary
            } else if (it.id == R.id.navigation_challenge) {
                binding.navView.selectedItemId = R.id.navigation_challenge
            } else if (it.id == R.id.navigation_friends) {
                binding.navView.selectedItemId = R.id.navigation_friends
            } else if (it.id == R.id.navigation_shop) {
                binding.navView.selectedItemId = R.id.navigation_shop
            } else if (it.id == R.id.navigation_device) {
                binding.navView.selectedItemId = R.id.navigation_device
            }
        }
    }

    fun setSelectShop() {
        navController?.currentDestination?.let {
//            binding.navView.selectedItemId = R.id.navigation_settings
        }
    }


    private val navListener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.summaryFragment,
                R.id.navigation_shop,
                R.id.navigation_friends,
                R.id.navigation_challenge,
                R.id.navigation_device,
                -> {
                    binding.navView.visible()
                }

                else -> binding.navView.gone()
            }
        }

    override fun onResume() {
        super.onResume()
        navController?.addOnDestinationChangedListener(navListener)


        viewModel.localDataStore.getConnectedDevice()?.let {
            deviceSetupViewModel.updateUserDevice(it,false)
            if (viewModel.sessionManager.connectState.value == null) {
                viewModel.sessionManager.setConnectState(ConnectState.Connecting(it))
                ApplicationUtils.setRescueWorkManager(this)
                ApplicationUtils.startNotificationListenerService(
                    viewModel.localDataStore,
                    applicationContext
                )
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
                R.id.summaryFragment, R.id.navigation_challenge, R.id.navigation_friends, R.id.navigation_device, R.id.navigation_shop -> {

                    if (it.currentDestination?.id == R.id.summaryFragment) {
                        finish()
                    } else {
                        binding.navView.selectedItemId = R.id.navigation_summary
                        navController?.popBackStack(R.id.summaryFragment, true)
                        navController?.navigate(R.id.summaryFragment)
                    }
                }

                else -> {
                    super.onBackPressed()
                }
            }
        } ?: super.onBackPressed()
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
                                ApplicationUtils.openAppSettings(this@MainActivity)
                            }

                            override fun no() {}

                        })
                )
            )

        }
    }

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding = binding.progressBar

    override fun initListener() {}


    override fun observeSubscriber() {


        viewModel.showInterestSelector.observe(this) {
            it.getContent()?.let { show ->
                if (show) {
                    navController?.navigate(R.id.bottomSheetInterestSelector)
                }
            }
        }

        viewModel.fetchLocation.observe(this) {
            it.getContent()?.let {
                initLocationManager()
                checkLocationPermission(permissionGranted = {
                    fetchLocation()
                })
            }
        }


        viewModel.bottomNavigation.observe(this) {
            it.getContent()?.let { navOpt ->
                when (navOpt) {
                    BottomNavOption.HOME -> {}
                    BottomNavOption.EXPLORE -> {
                        binding.navView.selectedItemId = R.id.navigation_challenge
                    }

                    BottomNavOption.SHOP -> {
                        binding.navView.selectedItemId = R.id.navigation_shop
                    }

                    BottomNavOption.MY_DEVICE -> {
                        binding.navView.selectedItemId = R.id.navigation_device
                    }

                    BottomNavOption.COMMUNITY -> {
                        binding.navView.selectedItemId = R.id.navigation_friends
                    }
                }
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

        viewModel.checkBluetooth.observe(this) {
            it.getContent()?.let { checkBluetooth ->
                if (checkBluetooth) {
                    checkBluetooth()
                }
            }
        }
        viewModel.checkLocation.observe(this) {
            it.getContent()?.let { event ->

                if (!hasPermissions(
                        arrayListOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                ) {
                    LOGS.d(TAG, "Location Permission missing")
                    showShortToast(getString(R.string.text_permission_missing_location))
                    return@let
                }

                if (!ApplicationUtils.isLocationProviderEnabled(this)) {
                    val locationRequest: LocationRequest = LocationRequest.create()
                    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    locationRequest.interval = 10000
                    locationRequest.fastestInterval = 5000
                    val builder: LocationSettingsRequest.Builder =
                        LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                    builder.setAlwaysShow(true)
                    val task: Task<LocationSettingsResponse> =
                        LocationServices.getSettingsClient(this)
                            .checkLocationSettings(builder.build())
                    task.addOnCompleteListener {
                        try {
                            task.getResult(ApiException::class.java)
                        } catch (exception: ApiException) {
                            when (exception.statusCode) {
                                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                                    try {
                                        startIntentSenderForResult(
                                            exception.status.resolution?.intentSender,
                                            22,
                                            null,
                                            0,
                                            0,
                                            0,
                                            null
                                        )
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
                }
            }
        }

        viewModel.sessionManager.connectedDevice.observe(this) {
            if (viewModel.lastConnectedDevice == null) {
                setBottomMenu(it)
                viewModel.lastConnectedDevice = it
            } else {
                if (viewModel.lastConnectedDevice != it) {
                    setBottomMenu(it)
                    viewModel.lastConnectedDevice = it
                }
                handleOtaUpdate()
            }
        }

        viewModel.logFileUploadStatus.observe(this) {
            it.getContent()?.let {
                viewModel.localDataStore.saveCrashLog("")
                viewModel.deleteLogFile(this)
            }
        }

        checkForUpdatesVM.updateInfoDash.observe(this) {
            it.getContent()?.let { res ->
                /*if (res.forceUpdate) {*/
                viewModel.sessionManager.forceOtaFlowRunning = false
                viewModel.sessionManager.forceOtaResponse = res
                handleOtaUpdate()/*}*/
            }
        }

        viewModel.sessionManager.versionCheckData.observe(this) {

            if (checkIfShowUpdateDialog(it)) {
                showAppVersionBottomSheet(it)
            }

        }

        viewModel.sessionManager.connectState.observe(this) { connectedState ->
            when (connectedState) {
                is ConnectState.ConnectFailed -> {
                    disconnectBadge(show = true, isConnected = false)
                }

                is ConnectState.Connecting -> {
                    disconnectBadge(show = true, isConnected = false)
                }

                is ConnectState.ConnectSuccess -> {
                    disconnectBadge(show = true, isConnected = true)
                }

                is ConnectState.UnPaired -> {
                    disconnectBadge(show = false, isConnected = false)
                }

                is ConnectState.DisconnectSuccess -> {
                    disconnectBadge(show = false, isConnected = false)
                }

                else -> {
                    disconnectBadge(show = false, isConnected = false)
                }
            }
        }
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
                ShareUtil.openPlayStore(this@MainActivity, "com.noisefit")
            }
        }
        navController?.navigate(R.id.appUpdateBottomSheet, Bundle().apply {
            putParcelable("versonResponse", it)
        })
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

    private fun showAppVersionDialog(versionCheckResponse: VersionCheckResponse) {
        val dialogFragment = AppVersionDialogFragment.newInstance(versionCheckResponse)
        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        dialogFragment.show(ft, "dialog")
        dialogFragment.setOnInteractionListener(object :
            AppVersionDialogFragment.OnInteractionListener {
            override fun onAppContinue(version: Int) {
                viewModel.localDataStore.setIgnoreVersion(version)
                dialogFragment.dismiss()
            }

            override fun onAppExit() {
                finish()
                System.exit(0)

            }

            override fun onAppUpdate() {
                ShareUtil.openPlayStore(this@MainActivity, "com.noisefit")
            }
        })
    }

    private fun handleOtaUpdate() {
        if (viewModel.sessionManager.forceOtaResponse == null) return

        if (navController?.currentDestination?.id == R.id.checkForUpdatesFragment) {
            return
        }

        if (viewModel.sessionManager.forceOtaResponse!!.forceUpdate) {
            if (!viewModel.sessionManager.forceOtaFlowRunning) {

                if (!viewModel.isOtaUpdateShownToUser) {
                    viewModel.isOtaUpdateShownToUser = true
                    binding.progressBar.root.visible()
                    getDeviceBatteryInfo()
                }
            }
        } else {
            viewModel.sessionManager.forceOtaResponse?.let { res ->
                val currentVersion = res.version
                val ignoredVersion = viewModel.watchDataStore.getWatchIgnoreVersion()
                LOGS.d("currentVersion $currentVersion ignoredVersion $ignoredVersion")
                if (currentVersion != ignoredVersion) {

                    if (!viewModel.isOtaUpdateShownToUser) {
                        viewModel.isOtaUpdateShownToUser = true

                        showForceOtaDialog()/* navController?.navigate(R.id.checkForUpdatesFragment, Bundle().apply {
                             putBoolean("startUpdate", true)
                         })*/
                    }

                }
            }
        }
    }

    private fun getDeviceBatteryInfo() {
        if (viewModel.sessionManager.batterPercent.value == 0) {

            val observer = androidx.lifecycle.Observer<Int> {

                if (it >= minimumBatteryLevel) {
                    binding.progressBar.root.gone()
                    showForceOtaDialog()
                    viewModel.sessionManager.batterPercent.removeObservers(this)
                } else if (it in 1 until minimumBatteryLevel) {
                    binding.progressBar.root.gone()
                    showBatteryWarning()
                    viewModel.sessionManager.batterPercent.removeObservers(this)
                }
            }
            viewModel.sessionManager.batterPercent.observe(
                this, observer
            )
        } else {
            binding.progressBar.root.gone()
            if (viewModel.sessionManager.batterPercent.value!! < minimumBatteryLevel) {
                showBatteryWarning()
            } else {
                showForceOtaDialog()
            }
        }
    }

    private fun showBatteryWarning() {
        val alertMessage = getString(R.string.text_battery_low_watchface, minimumBatteryLevel)
        onApiErrorReceived(
            ErrorResponse(
                UIComponentType.InfoAlertDialog(
                    getString(
                        R.string.text_watch_battery_low
                    ), alertMessage, getString(R.string.text_got_it)
                )
            )
        )
    }

    private fun showForceOtaDialog() {

        val builder = MaterialAlertDialogBuilder(this)
        val view = DataBindingUtil.inflate<DialogForceOtaBinding>(
            LayoutInflater.from(this), R.layout.dialog_force_ota, null, false
        )
        builder.setView(view.root)
        builder.setCancelable(false)

        val isForce = viewModel.sessionManager.forceOtaResponse?.forceUpdate ?: false
        view.tvWhatsNew.text = viewModel.sessionManager.forceOtaResponse?.descriptionEnglish ?: ""
        if (!isForce) {
            view.btnDoNotShowAgain.visible()
        }

        viewModel.localDataStore.getConnectedDevice()?.let {
            view.ivWatchImage.loadWatchImage(view.root.context, it.url, R.drawable.watch_default)
        }

        val otaUpdateDialog = builder.create()
        otaUpdateDialog.show()
        view.bStartUpdate.setOnClickListener {
            try {
                if (viewModel.sessionManager.connectState.value is ConnectState.ConnectSuccess || viewModel.sessionManager.connectState.value is ConnectState.DfuMode) {
                    otaUpdateDialog.dismiss()
                    navController?.navigate(R.id.checkForUpdatesFragment, Bundle().apply {
                        putBoolean("startUpdate", true)
                    })
                } else {
                    showShortToast("Watch not connected")
                }
            } catch (exp: NullPointerException) {
                exp.printStackTrace()
            }
        }

        view.btnDoNotShowAgain.setOnClickListener {
            try {
                if (viewModel.sessionManager.forceOtaResponse != null) {
                    val versionCode = viewModel.sessionManager.forceOtaResponse?.version ?: 0
                    viewModel.watchDataStore.setWatchIgnoreVersion(versionCode)
                }
                viewModel.sessionManager.forceOtaFlowRunning = true
                viewModel.sessionManager.forceOtaResponse = null
                checkForUpdatesVM.forceUpdate = false
                otaUpdateDialog.dismiss()
            } catch (exp: NullPointerException) {
                exp.printStackTrace()
            }
        }
    }


    override fun logAppEvent(eventName: String, data: HashMap<String, Any?>) {
//        viewModel.sessionManager.logAppEvent(eventName, data)
    }

//    private fun showCrashShareDialog() {
//        val builder = MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
//            .apply {
//                setTitle("Alert!")
//                setMessage("Share Crash log with developers?")
//                setPositiveButton(
//                    "Yes"
//                ) { dialog, which ->
//                    //ShareUtil.shareCrashLog(this@MainActivity, viewModel.localDataStore.getCrashLog())
//                }
//                setNegativeButton(
//                    "No"
//                ) { dialog, which ->
//                    viewModel.localDataStore.saveCrashLog("")
//                    viewModel.deleteLogFile(this@MainActivity)
//                }
//            }
//        val alertDialog = builder.create()
//        alertDialog.show()
//    }

    //TODO move to background thread
    private fun writeToFile(data: String?, context: Context) {
        try {
            val path: File? = context.externalCacheDir
            val fileName = "CrashLogs_${System.currentTimeMillis()}.txt"
            viewModel.logFileName = fileName

            val file = File(path, fileName)
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()

            val outputStream = FileOutputStream(file)
            outputStream.write(data?.toByteArray())
            outputStream.close()

            viewModel.uploadLogFile(file)

        } catch (e: Exception) {
            LOGS.d("Exception", "File write failed: " + e.toString())
        }
    }

    private var provider: String? = null
    private fun initLocationManager() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        val criteria = Criteria()
        provider = locationManager?.getBestProvider(criteria, false)
    }

    private fun checkLocationPermission(
        permissionGranted: () -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
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
            fetchLocation()
        } else {
            onApiErrorReceived(ErrorResponse(UIComponentType.AreYouSureDialog(getString(R.string.text_permission_required),
                getString(R.string.text_permission_denial_location_weather),
                false,
                getString(R.string.text_allow),
                object : BinaryActionCallback {
                    override fun yes() {
                        ApplicationUtils.openAppSettings(this@MainActivity)
                    }

                    override fun no() {

                    }

                }

            )))

        }
    }

    @SuppressLint("MissingPermission")
    fun fetchLocation() {

        if (viewModel.lat != null && viewModel.long != null) {
            viewModel.getAddress()
            return
        }

        if (!ApplicationUtils.isLocationProviderEnabled(this)) {
            val locationRequest: LocationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 10000
            locationRequest.fastestInterval = 5000
            val builder: LocationSettingsRequest.Builder =
                LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)
            val task: Task<LocationSettingsResponse> =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
            task.addOnCompleteListener(this)
            return
        }

        viewModel.setLoading(true)
        try {
            if (locationManager!!.allProviders.contains(LocationManager.GPS_PROVIDER)) {
                locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
            }
        } catch (exp: Exception) {
            //Handle getAllProviders exception
        }

        try {
            if (locationManager!!.allProviders.contains(LocationManager.NETWORK_PROVIDER)) {
                locationManager!!.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0f, this
                )
            }
        } catch (exp: Exception) {
            //Handle getAllProviders exception
        }

    }


    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onLocationChanged(location: Location) {
        viewModel.lat = location.latitude
        viewModel.long = location.longitude
        LOGS.d(TAG, " ${viewModel.lat} ${viewModel.long}")
        locationManager?.removeUpdates(this)
        viewModel.getAddress()
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}


    override fun onComplete(task: Task<LocationSettingsResponse>) {
        try {
            task.getResult(ApiException::class.java)
            fetchLocation()
        } catch (exception: ApiException) {
            when (exception.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {

                    startIntentSenderForResult(
                        exception.status.resolution?.intentSender,
                        REQUEST_CHECK_SETTINGS,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    LOGS.d("Failed to show dialog")
                } catch (classCast: ClassCastException) {
                }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

                }
            }
        }
    }

}