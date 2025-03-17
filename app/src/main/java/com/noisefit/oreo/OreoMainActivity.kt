package com.noisefit.oreo

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ActivityOreoMainBinding
import com.noisefit.ui.APP_CONTINUE
import com.noisefit.ui.APP_EXIT
import com.noisefit.ui.APP_UPDATE
import com.noisefit.ui.AppLinks
import com.noisefit.ui.common.BaseActivity
import com.noisefit.ui.onboarding.FirebaseUpdateViewModel
import com.noisefit.util.ApplicationUtils
import com.noisefit.util.notif.NotificationEventsClass
import com.noisefit.util.notif.NotificationUtil
import com.noisefit_commans.constants.SyncEvents
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.OWorkoutListModal
import com.noisefit_commans.data.response.VersionCheckResponse
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.noisefit_commans.utils.share.ShareUtil
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.ChatGptFragment
import com.oreo.ui.femalehealth.cycletracker.log.CycleLogFragment
import com.oreo.ui.recordworkout.SELECT_RECORD_WORKOUT
import dagger.hilt.android.AndroidEntryPoint
import eightbitlab.com.blurview.RenderEffectBlur
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class OreoMainActivity : BaseActivity<ActivityOreoMainBinding>() {

    private val viewModel: OreoMainViewModel by viewModels()
    private var navController: NavController? = null
    private val TAG = "oreoMainActivity"

    private val btAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }
    private val firebaseViewModel: FirebaseUpdateViewModel by viewModels()

    private val REQUEST_ENABLE_BT = 133

    companion object {
        val NOTIFICATION_TYPE = "NOTIFICATION_TYPE"
        val APP_LINK = "APP_LINK"
        fun getStartIntent(
            context: Context,
            notificationType: String? = null,
            notificationIndex: String? = null,
            appLink: AppLinks? = null
        ): Intent {

            return Intent(context, OreoMainActivity::class.java).apply {
                this.putExtra(NOTIFICATION_TYPE, notificationType)
                this.putExtra(APP_LINK, appLink)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LOGS.d(TAG, "App killed on destroy")
        /* if (viewModel.sessionManager.connectStateRing.value != null)
             NotificationUtil.sendForcePushNotification(
                 this,
                 getString(R.string.text_open_luna_ring_app),
                 getString(R.string.text_keep_the_luna_ring_app_running_so_your_data_can_stay_upto_date)
             )*/
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(viewModel.isBottomNavGifPlaying.not()){
            binding.navView.ivLunaAi.loadImage(this, R.drawable.anim_luna_ai_nav)
            viewModel.isBottomNavGifPlaying = true
        }

        navController = findNavController(R.id.o_nav_host_fragment)
        setNavViewListeners()
        setBlur()
        setBlurAddCta()
        viewModel.sessionManager.getPairedState()
        checkBluetooth()
        firebaseViewModel.generateToken()

        intent?.let {
            Handler(Looper.getMainLooper()).postDelayed({

                handleIntent(it)
            }, 500)


        }

        /*
                Handler(Looper.getMainLooper()).postDelayed({
                    navController?.navigate(R.id.bottomSheetForceUpdate)
                }, 3000)*/

        viewModel.rescheduleAlarms()
        setLunaIcon()
    }

    private fun setLunaIcon() {


        /*  binding.navView.ivLunaAi.post {
              val viewWidth = 36f * resources.displayMetrics.density
              val viewHeight = 36f * resources.displayMetrics.density

              // Get the animation's intrinsic width and height
              val animationWidth = binding.navView.ivLunaAi.composition?.bounds?.width()?.toFloat() ?: 0f
              val animationHeight = binding.navView.ivLunaAi.composition?.bounds?.height()?.toFloat() ?: 0f

              // Calculate the scale factors
              val scaleX = if (animationWidth > 0) viewWidth / animationWidth else 1f
              val scaleY = if (animationHeight > 0) viewHeight / animationHeight else 1f

              // Apply the scale
              binding.navView.ivLunaAi.scaleX = scaleX
              binding.navView.ivLunaAi.scaleY = scaleY

          }*/

        /*binding.navView.ivLunaAi.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Remove the listener to avoid multiple calls
                binding.navView.ivLunaAi.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val viewWidth = 36f * resources.displayMetrics.density
                val viewHeight = 36f * resources.displayMetrics.density

                // Get the animation's intrinsic width and height
                val animationWidth = binding.navView.ivLunaAi.composition?.bounds?.width()?.toFloat() ?: 0f
                val animationHeight = binding.navView.ivLunaAi.composition?.bounds?.height()?.toFloat() ?: 0f

                // Calculate the scale factors
                val scaleX = if (animationWidth > 0) viewWidth / animationWidth else 1f
                val scaleY = if (animationHeight > 0) viewHeight / animationHeight else 1f

                // Apply the scale
                binding.navView.ivLunaAi.scaleX = scaleX
                binding.navView.ivLunaAi.scaleY = scaleY
            }
        })*/
    }

    private fun setBlurAddCta(radius: Float = 5f) {
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

            lytLunaAi.setOnClickListener {
                selectMenuItem(BottomNavOption.LUNA_AI)
            }
        }
    }

    override fun initListener() {

        binding.blurViewSelector.setOnClickListener {
            animateFabDown()
            //binding.blurViewSelector.gone()
        }

        supportFragmentManager.setFragmentResultListener(SELECT_RECORD_WORKOUT, this) { _, bundle ->
            val workout = bundle.getParcelable<OWorkoutListModal>("workout")
            workout?.let {
                navController?.navigate(
                    R.id.recordWorkoutFragmentV2,
                    bundleOf("workout" to it)
                )
            }
        }


        binding.layoutRetry.btnRetry.setOnClickListener {
            binding.layoutRetry.root.gone()
            viewModel.getUserHealthData(viewModel.mStartDate, viewModel.mEndDate)
        }

        binding.lytAddWorkoutSelector.tvLogPeriod.setOnClickListener {
            onLogPeriodClicked()
        }
        binding.lytAddWorkoutSelector.ivLogPeriod.setOnClickListener {
            onLogPeriodClicked()
        }

        binding.lytAddWorkoutSelector.tvAddSleep.setOnClickListener {
            showAddSleep()
        }
        binding.lytAddWorkoutSelector.ivRecordSleep.setOnClickListener {
            showAddSleep()
        }


        binding.lytAddWorkoutSelector.tvAddWorkout.setOnClickListener {
            showAddWorkout()
        }
        binding.lytAddWorkoutSelector.ivAddWorkoutManual.setOnClickListener {
            showAddWorkout()
        }

        binding.lytAddWorkoutSelector.ivWorkoutClose.setOnClickListener {
            animateFabDown()
        }

        binding.lytAddWorkoutSelector.tvRecordWorkout.setOnClickListener {
            showRecordWorkout()
        }
        binding.lytAddWorkoutSelector.ivRecordWorkout.setOnClickListener {
            showRecordWorkout()
        }

        binding.btnAddWorkout.setOnClickListener {
            setBlurAddCta()
            if (viewModel.isActivityWorkAdd)
                viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_activity_add_workout_click)
            else
                viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_homepage_add_workout_click)
            viewModel.addWorkoutCtaVisibility.postValue(false)
            viewModel.isActivityWorkAdd = false
            animateFabUp()


            if (navController?.currentDestination?.id == R.id.navigation_oreo_workouts ||
                navController?.currentDestination?.id == R.id.oActivityListFragment
            ) {
                logAppEvent(
                    MoEngageLunaAppEvents.workout_add_button_clicked,
                    hashMapOf("source" to "activity")
                )
            } else {
                logAppEvent(
                    MoEngageLunaAppEvents.workout_add_button_clicked,
                    hashMapOf("source" to "homepage")
                )
            }

            //binding.blurViewSelector.visible()
        }

        //TODO comment after use
        /*  binding.btnAddWorkout.setOnLongClickListener {
              if (BuildConfig.DEBUG) {
                  startActivity(DeviceSetupActivityV2.getStartIntent(this,fullSetup = true),)
              }
              true
          }*/
    }


    private fun onLogPeriodClicked() {
        binding.blurViewSelector.gone()
        val (frag, bundle) = CycleLogFragment.getStartData(viewModel.selectedDate)
        navController?.navigate(frag, bundle)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun showAddSleep() {
        showAddWorkoutCta()
        binding.blurViewSelector.gone()
        if (viewModel.isDeviceConnected().not()) {
            showShortToast(getString(R.string.text_please_connect_your_ring_to_add_sleep))
            return
        }

        viewModel.viewModelScope.launch(Dispatchers.IO) {
            this@OreoMainActivity.let {
                val isWorkerRunning = ApplicationUtils.isOreoSyncDataWorkerRunning(it)
                if (isWorkerRunning) {
                    withContext(Dispatchers.Main) {
                        showShortToast(getString(R.string.text_please_wait_for_sync_to_complete_before_adding_sleep))
                    }
                    return@launch
                }
                withContext(Dispatchers.Main) {
                    navController?.navigate(R.id.fragmentAddSleep)
                }
            }
        }
    }

    private fun showAddWorkout() {
        if (navController?.currentDestination?.id == R.id.navigation_oreo_workouts ||
            navController?.currentDestination?.id == R.id.oActivityListFragment
        ) {
            logAppEvent(
                MoEngageLunaAppEvents.past_workout_started,
                hashMapOf("source" to "activity")
            )
        } else {
            logAppEvent(
                MoEngageLunaAppEvents.past_workout_started,
                hashMapOf("source" to "homepage")
            )
        }

        showAddWorkoutCta()
        binding.blurViewSelector.gone()
        if (viewModel.isDeviceConnected().not()) {
            showShortToast(getString(R.string.text_please_connect_your_ring_to_add_a_workout))
            return
        }

        viewModel.viewModelScope.launch(Dispatchers.IO) {
            this@OreoMainActivity.let {
                val isWorkerRunning = ApplicationUtils.isOreoSyncDataWorkerRunning(it)
                if (isWorkerRunning) {
                    withContext(Dispatchers.Main) {
                        showShortToast(getString(R.string.text_please_wait_for_sync_to_complete_before_starting_your_activity))
                    }
                    return@launch
                }
                withContext(Dispatchers.Main) {
                    navController?.navigate(R.id.addWorkoutFragment)
                }
            }
        }
    }

    private fun showRecordWorkout() {
        if (navController?.currentDestination?.id == R.id.navigation_oreo_workouts ||
            navController?.currentDestination?.id == R.id.oActivityListFragment
        ) {
            logAppEvent(
                MoEngageLunaAppEvents.live_workout_started,
                hashMapOf("source" to "activity")
            )
        } else {
            logAppEvent(
                MoEngageLunaAppEvents.live_workout_started,
                hashMapOf("source" to "homepage")
            )
        }

        showAddWorkoutCta()
        binding.blurViewSelector.gone()

        viewModel.viewModelScope.launch(Dispatchers.IO) {
            this@OreoMainActivity.let {
                val isWorkerRunning = ApplicationUtils.isOreoSyncDataWorkerRunning(it)
                if (isWorkerRunning) {
                    withContext(Dispatchers.Main) {
                        showShortToast(getString(R.string.text_please_wait_for_sync_to_complete_before_starting_your_activity))
                    }
                    return@launch
                }
                withContext(Dispatchers.Main) {
                    navController?.navigate(R.id.selectWorkoutFragment)
                }
            }
        }
    }

    private fun animateFabUp() {
        binding.blurViewSelector.visible()

        animateItemsUp(binding.lytAddWorkoutSelector.ivRecordWorkout, 200f)
        animateItemsUp(binding.lytAddWorkoutSelector.tvRecordWorkout, 200f)
        animateItemsUp(binding.lytAddWorkoutSelector.ivAddWorkoutManual, 300f)
        animateItemsUp(binding.lytAddWorkoutSelector.tvAddWorkout, 300f)
        /*animateItemsUp(binding.lytAddWorkoutSelector.tvAddSleep, 350f)
        animateItemsUp(binding.lytAddWorkoutSelector.ivRecordSleep, 350f)*/

        val lastDestination = navController?.currentDestination

        if (lastDestination?.id == R.id.navigation_oreo_home) {
            binding.lytAddWorkoutSelector.tvAddSleep.visible()
            binding.lytAddWorkoutSelector.ivRecordSleep.visible()
            animateItemsUp(binding.lytAddWorkoutSelector.tvAddSleep, 350f)
            animateItemsUp(binding.lytAddWorkoutSelector.ivRecordSleep, 350f)
        } else {
            binding.lytAddWorkoutSelector.tvAddSleep.gone()
            binding.lytAddWorkoutSelector.ivRecordSleep.gone()
        }

        if (viewModel.shouldShowFemaleHealthCta() && lastDestination?.id == R.id.navigation_oreo_home) {
            binding.lytAddWorkoutSelector.ivLogPeriod.visible()
            binding.lytAddWorkoutSelector.tvLogPeriod.visible()
            animateItemsUp(binding.lytAddWorkoutSelector.ivLogPeriod, 400f)
            animateItemsUp(binding.lytAddWorkoutSelector.tvLogPeriod, 400f)
        } else {
            binding.lytAddWorkoutSelector.ivLogPeriod.gone()
            binding.lytAddWorkoutSelector.tvLogPeriod.gone()
        }


        val rotate =
            ObjectAnimator.ofFloat(
                binding.lytAddWorkoutSelector.ivWorkoutClose,
                View.ROTATION,
                0f,
                -45f
            )
                .apply {
                    this.duration = viewModel.FAB_ANIM_TIME
                }

        val alphaAdd =
            ObjectAnimator.ofFloat(
                binding.lytAddWorkoutSelector.ivAddWorkoutBack,
                View.ALPHA,
                1f,
                0f
            )
                .apply {
                    this.duration = viewModel.FAB_ANIM_TIME
                }

        val alphaBlurLayer =
            ObjectAnimator.ofFloat(
                binding.blurViewSelector,
                View.ALPHA,
                0f,
                1f
            )
                .apply {
                    this.duration = viewModel.FAB_ANIM_TIME
                }

        val scaleDownX =
            ObjectAnimator.ofFloat(binding.lytAddWorkoutSelector.ivAddWorkoutBack, View.SCALE_X, 0f)
        val scaleDownY =
            ObjectAnimator.ofFloat(binding.lytAddWorkoutSelector.ivAddWorkoutBack, View.SCALE_Y, 0f)
        scaleDownX.setDuration(viewModel.FAB_ANIM_TIME)
        scaleDownY.setDuration(viewModel.FAB_ANIM_TIME)


        val animatorSet = AnimatorSet()
        animatorSet.playTogether(rotate, scaleDownX, scaleDownY, alphaAdd, alphaBlurLayer)
        animatorSet.start()


    }

    private fun animateFabDown() {
        //binding.blurViewSelector.gone()

        animateItemsDown(binding.lytAddWorkoutSelector.ivRecordWorkout)
        animateItemsDown(binding.lytAddWorkoutSelector.ivAddWorkoutManual)
        animateItemsDown(binding.lytAddWorkoutSelector.tvAddWorkout)
        animateItemsDown(binding.lytAddWorkoutSelector.tvRecordWorkout)
        animateItemsDown(binding.lytAddWorkoutSelector.tvAddSleep)
        animateItemsDown(binding.lytAddWorkoutSelector.ivRecordSleep)
        animateItemsDown(binding.lytAddWorkoutSelector.tvLogPeriod)
        animateItemsDown(binding.lytAddWorkoutSelector.ivLogPeriod)

        val alpha =
            ObjectAnimator.ofFloat(
                binding.lytAddWorkoutSelector.ivWorkoutClose,
                View.ROTATION,
                -45f,
                0f
            )
                .apply {
                    this.duration = viewModel.FAB_ANIM_TIME
                }

        val alphaAdd =
            ObjectAnimator.ofFloat(
                binding.lytAddWorkoutSelector.ivAddWorkoutBack,
                View.ALPHA,
                0f,
                1f
            )
                .apply {
                    this.duration = viewModel.FAB_ANIM_TIME
                }

        val alphaBlurLayer =
            ObjectAnimator.ofFloat(
                binding.blurViewSelector,
                View.ALPHA,
                1f,
                0.3f
            )
                .apply {
                    this.duration = viewModel.FAB_ANIM_TIME
                }

        val scaleDownX =
            ObjectAnimator.ofFloat(binding.lytAddWorkoutSelector.ivAddWorkoutBack, View.SCALE_X, 1f)
        val scaleDownY =
            ObjectAnimator.ofFloat(binding.lytAddWorkoutSelector.ivAddWorkoutBack, View.SCALE_Y, 1f)
        scaleDownX.setDuration(viewModel.FAB_ANIM_TIME)
        scaleDownY.setDuration(viewModel.FAB_ANIM_TIME)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(alpha, scaleDownX, scaleDownY, alphaAdd, alphaBlurLayer)
        animatorSet.start()

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                viewModel.addWorkoutCtaVisibility.value = (true)
                binding.blurViewSelector.gone()
            } catch (exp: Exception) {
            }
        }, viewModel.FAB_ANIM_TIME)

    }

    private fun animateItemsUp(view: View, value: Float) {

        val translateUp = ObjectAnimator.ofFloat(
            view,
            View.TRANSLATION_Y,
            value,
            0f
        ).apply {
            this.duration = viewModel.FAB_ANIM_TIME
        }
        val alpha =
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
                .apply {
                    this.duration = viewModel.FAB_ANIM_TIME
                }

        val animatorSet = AnimatorSet()
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.playTogether(translateUp, alpha)
        animatorSet.start()
    }

    private fun animateItemsDown(view: View) {
        val translateDown = ObjectAnimator.ofFloat(
            view,
            View.TRANSLATION_Y,
            0f,
            binding.lytAddWorkoutSelector.ivWorkoutClose.y - view.y
        ).apply {
            this.duration = viewModel.FAB_ANIM_TIME
        }

        val alpha =
            ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
                .apply {
                    this.duration = viewModel.FAB_ANIM_TIME
                }

        val animatorSet = AnimatorSet()
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.playTogether(translateDown, alpha)
        animatorSet.start()
    }

    private fun showAddWorkoutCta() {
        viewModel.addWorkoutCtaVisibility.postValue(true)
        //binding.btnAddWorkout.visible()
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
                    UIComponentType.AreYouSureDialog(
                        getString(R.string.text_permission_required),
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
        }/* else if (versionCheckResponse.upgradeType?.lowercase() == "soft_upgrade") {
            val ignoredVersion = viewModel.localDataStore.getIgnoreVersion()
            if (ignoredVersion != versionCheckResponse.currentVersion) {
                return true
            }

        }*/
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
        viewModel.lunaZoneReload.observe(this) {
            it.getContent()?.let {
                if (navController?.currentDestination?.id == R.id.navigation_lunaZoneFragment) {
                    viewModel.lunaZoneReloadConfirm.postValue(Event(true))
                }
            }
        }
        viewModel.sleepDashTodayReload.observe(this) {
            it.getContent()?.let {
                if (navController?.currentDestination?.id == R.id.sleepDashFragment) {
                    navController?.popBackStack(R.id.sleepDashFragment, true)
                    navController?.navigate(R.id.sleepDashFragment)
                }
            }
        }

        viewModel.sessionManager.forceUpdateApp.observe(this) {
            it.getContent()?.let { isRequired ->
                if (isRequired) {
                    if (navController?.currentDestination?.id != R.id.bottomSheetForceUpdate) {
                        navController?.navigate(R.id.bottomSheetForceUpdate)
                    }
                }
            }
        }

        viewModel.showChatUi.observe(this) {
            it.getContent()?.let { threadId ->
                val (frag, bundle) = ChatGptFragment.getStartData(
                    threadId,
                    null,
                    null,
                    null,
                    AITopics.GENERAL
                )
                navController?.navigate(frag, bundle)
            }
        }

        viewModel.sessionManager.customSuccessToast.observe(this) {
            it.getContent()?.let {
                showCustomSuccessToast(it)
            }
        }

        viewModel.sessionManager.reloadTodayData.observe(this) {
            it.getContent()?.let {
                viewModel.reloadTodaysData()
            }
        }

        viewModel.sessionManager.ongoingWorkoutDetected.observe(this) {
            it.getContent()?.let { pair ->
                if (navController?.currentDestination?.id != R.id.recordWorkoutFragmentV2) {
                    navController?.navigate(
                        R.id.recordWorkoutFragmentV2, bundleOf(
                            "workout" to pair.second,
                            "onGoingWorkout" to pair.first,
                        )
                    )
                }
            }
        }

        viewModel.addWorkoutCtaVisibility.observe(this) {
            if (it) {
                binding.btnAddWorkout.visible()
            } else {
                binding.btnAddWorkout.gone()
            }
        }

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

                    BottomNavOption.SLEEP -> {
                        selectMenuItem(BottomNavOption.SLEEP)
                    }

                    BottomNavOption.READINESS -> {
                        selectMenuItem(BottomNavOption.READINESS)
                    }

                    BottomNavOption.ACTIVITY -> {
                        selectMenuItem(BottomNavOption.ACTIVITY)
                    }

                    BottomNavOption.LUNA_AI -> {
                        selectMenuItem(BottomNavOption.LUNA_AI)
                    }
                }
            }
        }

        viewModel.sessionManager.syncCompleted.observe(this) {
            it?.getContent()?.let { syncDataStatus ->
                when (syncDataStatus) {
                    SyncEvents.Failed -> {
                        viewModel.syncProgressBarState.value = null
                        viewModel.syncTextState.value = null
                    }

                    is SyncEvents.InProgress -> {
                        LOGS.d("Progress_____________ ${syncDataStatus.progress}")
                        viewModel.syncProgressBarState.value =
                            Pair(syncDataStatus.progress, syncDataStatus.total)
                        viewModel.syncTextState.value = getString(R.string.text_syncing_dot)

                        /*binding.lytHeader.pbSync.max = syncDataStatus.total
                        binding.lytHeader.pbSync.progress = syncDataStatus.progress
                        binding.lytHeader.pbSync.visible()
                        binding.lytHeader.tvHeaderStatus.apply {
                            text = getString(R.string.text_syncing_dot)
                            visible()
                        }*/
                    }

                    is SyncEvents.Started -> {
                        viewModel.syncProgressBarState.value =
                            Pair(syncDataStatus.progress, syncDataStatus.total)
                        viewModel.syncTextState.value = getString(R.string.text_syncing_dot)


                        /*   binding.lytHeader.pbSync.max = syncDataStatus.total
                           binding.lytHeader.pbSync.progress = syncDataStatus.progress
                           binding.lytHeader.pbSync.visible()
                           binding.lytHeader.tvHeaderStatus.apply {
                               text = getString(R.string.text_syncing_dot)
                               visible()
                           }*/
                    }

                    is SyncEvents.Success -> {
                        viewModel.syncProgressBarState.value = null
                        viewModel.syncTextState.value = null

                        /* binding.lytHeader.pbSync.max = syncDataStatus.total
                         binding.lytHeader.pbSync.progress = syncDataStatus.progress
                         binding.lytHeader.tvHeaderStatus.gone()
                         binding.lytHeader.pbSync.gone()
                         resetSwipeLoadingAnim()*/
                    }

                    SyncEvents.ServerSyncStarted -> {
                        binding.progressBar.root.visible()
                    }

                    SyncEvents.ServerSyncSuccess -> {
                        viewModel.syncProgressBarState.value = null
                        viewModel.syncTextState.value = null
                        binding.progressBar.root.gone()

                        viewModel.reloadTodaysData()
                        viewModel.sleepDashTodayReload.value = Event(true)

                        //sendLogs()
                    }
                }
            }
        }

        viewModel.sessionManager.connectStateRing.observe(this) { connectedState ->
            when (connectedState) {
                is ConnectState.ConnectFailed -> {
                    viewModel.syncProgressBarState.value = null
                    viewModel.syncTextState.value = null
                }

                is ConnectState.Connecting -> {
                    viewModel.syncProgressBarState.value = null
                    viewModel.syncTextState.value = null
                }

                is ConnectState.ConnectSuccess -> {

                }

                is ConnectState.UnPaired -> {
                    viewModel.syncProgressBarState.value = null
                    viewModel.syncTextState.value = null
                }

                else -> {}
            }
        }

        viewModel.syncProgressBarState.observe(this) {
            if (it == null) {
                binding.pbSync.gone()
            } else {
                val (progress, total) = it
                binding.pbSync.visible()
                binding.pbSync.max = total
                binding.pbSync.progress = progress
            }
        }
    }

    private fun showCustomSuccessToast(message: String) {
        binding.lytToastSuccess.tvText.text = message
        binding.lytToastSuccess.root.visible()
        Handler(Looper.getMainLooper()).postDelayed({
            binding.lytToastSuccess.root.gone()
        }, 3000)

    }

    private val navListener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->

            when (destination.id) {
                R.id.navigation_oreo_home, R.id.navigation_oreo_readiness,
                R.id.navigation_oreo_workouts, R.id.sleepDashFragment,
                R.id.navigation_lunaZoneFragment -> {
                    binding.view27.visible()
                    binding.navView.root.visible()

                    if (destination.id == R.id.navigation_oreo_home || destination.id == R.id.navigation_oreo_workouts) {
                        viewModel.handleAddWorkoutVisibility()
                    } else {
                        viewModel.addWorkoutCtaVisibility.postValue(false)
                        viewModel.isActivityWorkAdd = false
                    }

                    //binding.btnAddWorkout.visible()//todo add today condition
                }

                R.id.oActivityListFragment -> {
                    binding.view27.gone()
                    binding.navView.root.gone()

                    if (viewModel.isDevicePaired() != null) {
                        viewModel.addWorkoutCtaVisibility.postValue(true)
                        viewModel.isActivityWorkAdd = true
                    }
                }

                else -> {
                    binding.view27.gone()
                    binding.navView.root.gone()
                    viewModel.addWorkoutCtaVisibility.postValue(false)
                    viewModel.isActivityWorkAdd = false

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
            viewModel.syncRecordedWorkoutData()
        }


        viewModel.checkForForceUpdate()


        //viewModel.shouldResetMasterDates()


        viewModel.loginFreshChatUser()

        viewModel.incrementOpenCount()
    }


    private fun handleIntent(intent: Intent?) {
        intent?.extras?.let { intentExtra ->

            val appLink  = intent.getSerializableExtra(APP_LINK) as? AppLinks
            if(appLink!=null){


                handleAppLinkNavigation(appLink)
                return
            }


            if (intentExtra.containsKey(NOTIFICATION_TYPE)) {
                LOGS.d("NEW_NOTIFICATION_TYPE  ${intentExtra.getString(NOTIFICATION_TYPE)}")
                handleNotificationType(
                    intentExtra.getString(OreoMainActivity.NOTIFICATION_TYPE) ?: "",
                    "0",
                    ""
                )
                intent.putExtra(NOTIFICATION_TYPE, "")
            }else if(intentExtra.containsKey(APP_LINK)){
                val data  = intent.getSerializableExtra(APP_LINK) as AppLinks
                handleAppLinkNavigation(data)
            } else {

            }

        }

    }

    private fun handleAppLinkNavigation(appLink: AppLinks) {
        when(appLink){
            AppLinks.REFERRAL -> {
                viewModel.getReferralInfo{ data->
                    this@OreoMainActivity.navController?.navigate(R.id.referralFragment, bundleOf("referralInfo" to data))
                }
            }
        }
    }

    private fun handleNotificationType(
        notificationType: String,
        notificationIndex: String,
        deeplink: String?
    ) {
        if (notificationType.equals(NotificationEventsClass.LOCAL_NOTIFICATION_WORKOUT_KEY, true)) {
            navController?.navigate(R.id.detectWorkoutListFragment)
        } else if (notificationType.equals(
                NotificationEventsClass.LOCAL_NOTIFICATION_BREATHING,
                true
            )
        ) {
            navController?.navigate(R.id.fragmentBreathExercise)
        }
    }

    override fun onPause() {
        navController?.removeOnDestinationChangedListener(navListener)
        super.onPause()
    }

    override fun onBackPressed() {
        navController?.let {
            when (it.currentDestination?.id) {
                R.id.navigation_oreo_home, R.id.navigation_oreo_readiness,
                R.id.navigation_oreo_workouts, R.id.sleepDashFragment, R.id.navigation_lunaZoneFragment -> {

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
                //binding.navView.ivLunaAi.setImageResource(R.drawable.ic_dash_luna_zone_default)

                if(viewModel.isBottomNavGifPlaying.not()){
                    binding.navView.ivLunaAi.loadImage(this, R.drawable.anim_luna_ai_nav)
                    viewModel.isBottomNavGifPlaying = true
                }

                binding.navView.apply {
                    ivGlowHome.visible()
                    ivGlowSleep.gone()
                    ivGlowReadiness.gone()
                    ivGlowActivity.gone()
                    ivGlowLunaAi.gone()
                }


                val lastDestination = navController?.currentDestination
                if (lastDestination?.id != R.id.navigation_oreo_home) {
                    navController?.popBackStack(R.id.navigation_oreo_home, true)
                    navController?.navigate(R.id.navigation_oreo_home)
                }

                viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_footer_home_click)
            }

            BottomNavOption.SLEEP -> {
                binding.navView.ivHome.setImageResource(R.drawable.ic_dash_summary_default)
                binding.navView.ivSleep.setImageResource(R.drawable.ic_dash_oreo_sleep)
                binding.navView.ivReadiness.setImageResource(R.drawable.ic_dash_oreo_readiness_default)
                binding.navView.ivActivity.setImageResource(R.drawable.ic_dash_oreo_activity_default)
                //binding.navView.ivLunaAi.setImageResource(R.drawable.ic_dash_luna_zone_default)

                if(viewModel.isBottomNavGifPlaying.not()){
                    binding.navView.ivLunaAi.loadImage(this, R.drawable.anim_luna_ai_nav)
                    viewModel.isBottomNavGifPlaying = true
                }
                binding.navView.apply {
                    ivGlowHome.gone()
                    ivGlowSleep.visible()
                    ivGlowReadiness.gone()
                    ivGlowActivity.gone()
                    ivGlowLunaAi.gone()
                }

                val lastDestination = navController?.currentDestination
                if (lastDestination?.id != R.id.sleepDashFragment) {
                    navController?.popBackStack(R.id.sleepDashFragment, true)
                    navController?.navigate(R.id.sleepDashFragment)
                }
                viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_footer_sleep_click)

            }

            BottomNavOption.READINESS -> {
                binding.navView.ivHome.setImageResource(R.drawable.ic_dash_summary_default)
                binding.navView.ivSleep.setImageResource(R.drawable.ic_dash_oreo_sleep_default)
                binding.navView.ivReadiness.setImageResource(R.drawable.ic_dash_oreo_readiness)
                binding.navView.ivActivity.setImageResource(R.drawable.ic_dash_oreo_activity_default)
                //binding.navView.ivLunaAi.setImageResource(R.drawable.ic_dash_luna_zone_default)

                if(viewModel.isBottomNavGifPlaying.not()){
                    binding.navView.ivLunaAi.loadImage(this, R.drawable.anim_luna_ai_nav)
                    viewModel.isBottomNavGifPlaying = true
                }
                binding.navView.apply {
                    ivGlowHome.gone()
                    ivGlowSleep.gone()
                    ivGlowReadiness.visible()
                    ivGlowActivity.gone()
                    ivGlowLunaAi.gone()
                }
                val lastDestination = navController?.currentDestination
                if (lastDestination?.id != R.id.navigation_oreo_readiness) {
                    navController?.popBackStack(R.id.navigation_oreo_readiness, true)
                    navController?.navigate(R.id.navigation_oreo_readiness)
                }
                viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_footer_readiness_click)

            }

            BottomNavOption.ACTIVITY -> {
                binding.navView.ivHome.setImageResource(R.drawable.ic_dash_summary_default)
                binding.navView.ivSleep.setImageResource(R.drawable.ic_dash_oreo_sleep_default)
                binding.navView.ivReadiness.setImageResource(R.drawable.ic_dash_oreo_readiness_default)
                binding.navView.ivActivity.setImageResource(R.drawable.ic_dash_oreo_activity)
                if(viewModel.isBottomNavGifPlaying.not()){
                    binding.navView.ivLunaAi.loadImage(this, R.drawable.anim_luna_ai_nav)
                    viewModel.isBottomNavGifPlaying = true
                }
                //binding.navView.ivLunaAi.setImageResource(R.drawable.ic_dash_luna_zone_default)

                binding.navView.apply {
                    ivGlowHome.gone()
                    ivGlowSleep.gone()
                    ivGlowReadiness.gone()
                    ivGlowActivity.visible()
                    ivGlowLunaAi.gone()
                }
                val lastDestination = navController?.currentDestination
                if (lastDestination?.id != R.id.navigation_oreo_workouts) {
                    navController?.popBackStack(R.id.navigation_oreo_workouts, true)
                    navController?.navigate(R.id.navigation_oreo_workouts)
                }
                viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_footer_activity_click)
            }

            BottomNavOption.LUNA_AI -> {
                binding.navView.ivHome.setImageResource(R.drawable.ic_dash_summary_default)
                binding.navView.ivSleep.setImageResource(R.drawable.ic_dash_oreo_sleep_default)
                binding.navView.ivReadiness.setImageResource(R.drawable.ic_dash_oreo_readiness_default)
                binding.navView.ivActivity.setImageResource(R.drawable.ic_dash_oreo_activity_default)

                viewModel.isBottomNavGifPlaying = false
                binding.navView.ivLunaAi.loadImage(this, R.drawable.ic_dash_luna_zone_selected)

                //binding.navView.ivLunaAi.setImageResource(R.drawable.ic_dash_luna_zone_selected)

                binding.navView.apply {
                    ivGlowHome.gone()
                    ivGlowSleep.gone()
                    ivGlowReadiness.gone()
                    ivGlowActivity.gone()
                    ivGlowLunaAi.gone()
                }
                val lastDestination = navController?.currentDestination
                if (lastDestination?.id != R.id.navigation_lunaZoneFragment) {
                    viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.home_footer_lunaai)
                    navController?.popBackStack(R.id.navigation_lunaZoneFragment, true)
                    navController?.navigate(R.id.navigation_lunaZoneFragment)
                }
            }
        }
    }

    override fun getViewBinding() = ActivityOreoMainBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding = binding.progressBar

    override fun logAppEvent(eventName: String, data: HashMap<String, Any>?) {
        viewModel.sessionManager.logAppEvents(eventName, data)
    }

}

enum class BottomNavOption {
    HOME, SLEEP, READINESS, ACTIVITY, LUNA_AI
}