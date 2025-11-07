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
import android.view.ViewAnimationUtils
import android.animation.Animator
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.airbnb.lottie.LottieDrawable
import com.moengage.inapp.MoEInAppHelper
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ActivityOreoMainBinding
import com.noisefit.ui.APP_CONTINUE
import com.noisefit.ui.APP_EXIT
import com.noisefit.ui.APP_UPDATE
import com.noisefit.ui.AppLinks
import com.noisefit.ui.common.BaseActivity
import com.noisefit.ui.onboarding.FirebaseUpdateViewModel
import com.noisefit.ui.web.WebViewActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit.util.notif.NotificationEventsClass
import com.noisefit.util.notif.NotificationUtil
import com.noisefit_commans.constants.SyncEvents
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.AppTrackEvent
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
import com.oreo.data.model.CaffeineWindowData
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
import androidx.core.view.isVisible
import com.oreo.data.model.FabItems
import com.oreo.data.model.FabModel
import com.oreo.ui.circadianAlignment.CircadianAlignmentViewModel

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

        viewModel.localDataStore.saveAppTrackEvent(AppTrackEvent.APP_START, false)
        viewModel.updateAppTrackingEvent(AppTrackEvent.APP_START)

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

        viewModel.rescheduleAlarms()
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
                viewModel.sessionManager.logMoEngageAppEvent(
                    MoEngageLunaAppEvents.homepage_luna
                )
                selectMenuItem(BottomNavOption.LUNA_AI)
            }
        }
    }

    override fun initListener() {

        // FAB radial overlay dismiss
        binding.fabRevealOverlay.setOnClickListener {
            hideFabRadialMenu()
        }

        binding.blurViewSelector.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.activity_event_cancelled,
                HashMap<String, Any>().apply {
                    this["source"] = "homepage,activity"
                }
            )
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

        binding.lytAddWorkoutSelector.tvAddActivity.setOnClickListener {
            showTimeline()
        }
        binding.lytAddWorkoutSelector.ivAddActivity.setOnClickListener {
            showTimeline()
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
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.activity_event_cancelled,
                HashMap<String, Any>().apply {
                    this["source"] = "homepage,activity"
                }
            )
            animateFabDown()
        }

        binding.lytAddWorkoutSelector.tvRecordWorkout.setOnClickListener {
            showRecordWorkout()
        }
        binding.lytAddWorkoutSelector.ivRecordWorkout.setOnClickListener {
            showRecordWorkout()
        }

        binding.btnAddWorkout.setOnClickListener {
            if (binding.fabRevealOverlay.isVisible) {
                hideFabRadialMenu()
            } else {
                /*if (navController?.currentDestination?.id == R.id.navigation_oreo_workouts ||
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
                }*/
                showFabRadialMenu()
            }
            return@setOnClickListener

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

        intent?.let {
            Handler(Looper.getMainLooper()).postDelayed({

                handleIntent(it)
            }, 500)


        }
        //handleIntent(intent)
    }

    private fun showTimeline() {
        showAddWorkoutCta()
        binding.blurViewSelector.gone()
        if (viewModel.isDeviceConnected().not()) {
            showShortToast(getString(R.string.text_please_connect_your_ring))
            return
        }

        navController?.navigate(
            R.id.addActivityTimelineFragment,
            bundleOf("showTimeline" to true, "key" to null)
        )

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
                    navController?.navigate(
                        R.id.addActivityTimelineFragment,
                        bundleOf(
                            "showTimeline" to false,
                            "key" to CircadianAlignmentViewModel.sleep_key,
                            "srcKey" to "dash_fab"
                        )
                    )
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
        binding.lytAddWorkoutSelector.ivAddWorkoutManual.visible()
        binding.lytAddWorkoutSelector.tvAddWorkout.visible()
        animateItemsUp(binding.lytAddWorkoutSelector.ivAddWorkoutManual, 300f)
        animateItemsUp(binding.lytAddWorkoutSelector.tvAddWorkout, 300f)

        val lastDestination = navController?.currentDestination

        if (lastDestination?.id == R.id.navigation_oreo_home) {
            binding.lytAddWorkoutSelector.tvAddSleep.visible()
            binding.lytAddWorkoutSelector.ivRecordSleep.visible()
            animateItemsUp(binding.lytAddWorkoutSelector.tvAddSleep, 350f)
            animateItemsUp(binding.lytAddWorkoutSelector.ivRecordSleep, 350f)

            binding.lytAddWorkoutSelector.tvAddActivity.visible()
            binding.lytAddWorkoutSelector.ivAddActivity.visible()
            animateItemsUp(binding.lytAddWorkoutSelector.tvAddActivity, 400f)
            animateItemsUp(binding.lytAddWorkoutSelector.ivAddActivity, 400f)
        } else {
            binding.lytAddWorkoutSelector.tvAddSleep.gone()
            binding.lytAddWorkoutSelector.ivRecordSleep.gone()
            binding.lytAddWorkoutSelector.tvAddActivity.gone()
            binding.lytAddWorkoutSelector.ivAddActivity.gone()
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

        animateItemsDown(binding.lytAddWorkoutSelector.tvAddActivity)
        animateItemsDown(binding.lytAddWorkoutSelector.ivAddActivity)
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

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun populateFabRadialItems(cx: Int, cy: Int) {
        val overlay = binding.fabRevealOverlay
        overlay.removeAllViews()
        val radius = dpToPx(80)
        val items = getFabItems()

        items.reverse()


        val angles = listOf(
            Pair(90, dpToPx(56)),
            Pair(165, dpToPx(56)),
            Pair(200, dpToPx(70)),
            Pair(235, dpToPx(80)),
            Pair(270, dpToPx(105))
        )
        val angles2 = listOf(
            Pair(90, dpToPx(56)),
            Pair(165, dpToPx(56)),
            Pair(205, dpToPx(60)),
            Pair(270, dpToPx(65)),
        )
        val yBias = dpToPx(10)

        data class Entry(val view: View, val tx: Int, val ty: Int)

        val entries = ArrayList<Entry>(items.size)

        val is4Items = items.size == 4

        for ((index, item) in items.withIndex()) {
            val selectedAngle = if (is4Items) angles2 else angles
            val angle =
                Math.toRadians(selectedAngle[index].first.toDouble())
            val tx = (cx + selectedAngle[index].second * Math.cos(angle)).toInt()
            val ty = (cy + selectedAngle[index].second * Math.sin(angle)).toInt() + yBias

            val view = LayoutInflater.from(overlay.context).inflate(R.layout.layout_fab_item, null)
            view.findViewById<ImageView>(R.id.imageView).setImageResource(item.icon)
            view.findViewById<TextView>(R.id.tvTitle).apply {
                text = item.title
                setTextColor(item.color)
            }

            val lp = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            )
            overlay.addView(view, lp)
            entries.add(Entry(view, tx, ty))

            view.setOnClickListener {
                hideFabRadialMenu(false)
                onFabMenuItemCLicked(item)
            }
        }

        overlay.post {
            val sorted = entries.sortedBy { it.ty }
            sorted.forEachIndexed { rank, e ->
                e.view.x = cx - e.view.width.toFloat() / 2f
                e.view.y = cy - e.view.height / 2f
                e.view.alpha = 0f

                val baseDelay = rank * 40L
                val moveDuration = 250L

                val animX = ObjectAnimator.ofFloat(
                    e.view,
                    View.X,
                    e.view.x,
                    e.tx.toFloat() - e.view.width/* / 2f*/
                ).apply {
                    duration = moveDuration
                    startDelay = baseDelay
                }
                val animY = ObjectAnimator.ofFloat(
                    e.view,
                    View.Y,
                    e.view.y,
                    e.ty.toFloat() - e.view.height / 2f
                ).apply {
                    duration = moveDuration
                    startDelay = baseDelay
                }
                val alphaAnim = ObjectAnimator.ofFloat(e.view, View.ALPHA, 0f, 1f).apply {
                    duration = moveDuration / 2
                    startDelay = baseDelay + moveDuration / 2
                }

                animX.start(); animY.start(); alphaAnim.start()
            }
        }
    }

    private fun onFabMenuItemCLicked(item: FabModel) {

        when (item.type) {
            FabItems.RECORD_WORKOUT -> {
                showRecordWorkout()
            }

            FabItems.ADD_WORKOUT -> {
                showAddWorkout()
            }

            FabItems.ADD_SLEEP -> {
                showAddSleep()
            }

            FabItems.TRACK_PERIOD -> {
                onLogPeriodClicked()
            }

            FabItems.ADD_OTHER_ACTIVITY -> {
                showTimeline()
            }
        }
    }

    private fun getFabItems(): ArrayList<FabModel> {
        val lastDestination = navController?.currentDestination

        val items = ArrayList<FabModel>()

        /* if(lastDestination?.id==R.id.sleepDashFragment){
             items.add(
                 FabModel(
                     title = getString(R.string.text_add_sleep),
                     icon = R.drawable.ic_fab_add_sleep,
                     color = "#F2CEFF".toColorInt(),
                     type = FabItems.ADD_SLEEP
                 )
             )
             return items
         }*/

        items.add(
            FabModel(
                title = getString(R.string.text_record_workout),
                icon = R.drawable.ic_fab_record_workout,
                color = "#99D9FF".toColorInt(),
                type = FabItems.RECORD_WORKOUT
            )
        )
        items.add(
            FabModel(
                title = getString(R.string.text_add_workout),
                icon = R.drawable.ic_fab_add_workout,
                color = "#99D9FF".toColorInt(),
                type = FabItems.ADD_WORKOUT
            )
        )


        //if (lastDestination?.id == R.id.navigation_oreo_home) {
        items.add(
            FabModel(
                title = getString(R.string.text_add_sleep),
                icon = R.drawable.ic_fab_add_sleep,
                color = "#F2CEFF".toColorInt(),
                type = FabItems.ADD_SLEEP
            )
        )
        //}

        if (viewModel.shouldShowFemaleHealthCta()/* && lastDestination?.id == R.id.navigation_oreo_home*/) {
            items.add(
                FabModel(
                    title = getString(R.string.text_log_period),
                    icon = R.drawable.ic_fab_period,
                    color = "#FFBFBF".toColorInt(),
                    type = FabItems.TRACK_PERIOD
                )
            )
        }

        //if (lastDestination?.id == R.id.navigation_oreo_home) {
        items.add(
            FabModel(
                title = getString(R.string.text_add_other_activity),
                icon = R.drawable.ic_fab_add_other,
                color = "#A8E0CD".toColorInt(),
                type = FabItems.ADD_OTHER_ACTIVITY
            )
        )
        //}

        return items
    }

    private fun showFabRadialMenu() {
        if (binding.fabRevealOverlay.isVisible) return

        val overlay = binding.fabRevealOverlay
        overlay.visibility = View.VISIBLE

        try {
            ObjectAnimator.ofFloat(
                binding.ivWorkout,
                View.ROTATION,
                binding.ivWorkout.rotation,
                -45f
            ).apply {
                duration = 150
                start()
            }
        } catch (_: Exception) {
        }

        overlay.post {
            val fabLoc = IntArray(2)
            val overlayLoc = IntArray(2)
            binding.btnAddWorkout.getLocationOnScreen(fabLoc)
            overlay.getLocationOnScreen(overlayLoc)
            val cx = fabLoc[0] - overlayLoc[0] + binding.btnAddWorkout.width / 2
            val cy = fabLoc[1] - overlayLoc[1] + binding.btnAddWorkout.height / 2

            val finalRadius =
                kotlin.math.hypot(overlay.width.toDouble(), overlay.height.toDouble()).toFloat()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val anim = ViewAnimationUtils.createCircularReveal(overlay, cx, cy, 0f, finalRadius)
                anim.duration = 300
                overlay.visibility = View.VISIBLE
                anim.start()
            } else {
                overlay.alpha = 0f
                overlay.animate().alpha(1f).setDuration(200).start()
            }

            populateFabRadialItems(cx, cy)
        }
    }

    private fun hideFabRadialMenu(animate: Boolean = true) {
        if (binding.fabRevealOverlay.visibility != View.VISIBLE) return
        val overlay = binding.fabRevealOverlay
        try {
            ObjectAnimator.ofFloat(
                binding.ivWorkout, View.ROTATION,
                binding.ivWorkout.rotation, 0f
            )
                .apply {
                    duration = 150
                    start()
                }
        } catch (_: Exception) {
        }

        val fabLoc = IntArray(2)
        val overlayLoc = IntArray(2)
        binding.btnAddWorkout.getLocationOnScreen(fabLoc)
        overlay.getLocationOnScreen(overlayLoc)
        val cx = fabLoc[0] - overlayLoc[0] + binding.btnAddWorkout.width / 2
        val cy = fabLoc[1] - overlayLoc[1] + binding.btnAddWorkout.height / 2

        val childCount = overlay.childCount
        if (childCount > 0) {
            var completed = 0
            val children = (0 until childCount).map { overlay.getChildAt(it) }
                .sortedByDescending { it.y }
            children.forEachIndexed { rank, v ->
                val baseDelay = if (animate) (rank * 40L) else 0
                val moveDuration = if (animate) 200L else 0

                val animX = ObjectAnimator.ofFloat(
                    v,
                    View.X,
                    v.x,
                    cx - v.width / 2f
                ).apply {
                    duration = moveDuration
                    startDelay = baseDelay
                }
                val animY = ObjectAnimator.ofFloat(
                    v,
                    View.Y,
                    v.y,
                    cy - v.height / 2f
                ).apply {
                    duration = moveDuration
                    startDelay = baseDelay
                }
                val alphaAnim = ObjectAnimator.ofFloat(v, View.ALPHA, 1f, 0f).apply {
                    duration = moveDuration / 2
                    startDelay = baseDelay
                    addListener(object : android.animation.AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            completed++
                            if (completed == childCount) {
                                concealOverlayAfterItems(cx, cy, animate)
                            }
                        }
                    })
                }

                animX.start(); animY.start(); alphaAnim.start()
            }
        } else {
            concealOverlayAfterItems(cx, cy, animate)
        }
    }

    private fun concealOverlayAfterItems(cx: Int, cy: Int, animate: Boolean = true) {
        val overlay = binding.fabRevealOverlay
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && overlay.isShown) {
            val initialRadius =
                kotlin.math.hypot(overlay.width.toDouble(), overlay.height.toDouble()).toFloat()
            val anim = ViewAnimationUtils.createCircularReveal(overlay, cx, cy, initialRadius, 0f)
            anim.duration = if (animate) 220 else 0
            anim.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    overlay.visibility = View.GONE
                    overlay.removeAllViews()
                }
            })
            anim.start()
        } else {
            overlay.animate().alpha(0f).setDuration(if (animate) 150 else 0).withEndAction {
                overlay.visibility = View.GONE
                overlay.alpha = 1f
                overlay.removeAllViews()
            }.start()
        }
    }

    private fun showAddWorkoutCta() {
        viewModel.addWorkoutCtaVisibility.value = true
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

        viewModel.sessionManager.nfcSleepErr.observe(this) {
            it?.getContent()?.let {
                if (it) {
                    if (navController?.currentDestination?.id == R.id.ringExceptionDialogFragment) {
                        return@let
                    }

                    if (viewModel.checkExceptionCancelState()) {
                        navController?.navigate(R.id.ringExceptionDialogFragment)
                    }
                }
            }
        }

        viewModel.cannyFeedbackUrl.observe(this) {
            it.getContent()?.let {
                startActivity(
                    WebViewActivity.getStartIntent(
                        this,
                        getString(R.string.text_suggest_a_feature),
                        it
                    )
                )
            }
        }

        viewModel.sessionManager.moengageClicks.observe(this) {
            it.getContent()?.let {
                handleAppLinkNavigation(it)
            }

        }


        viewModel.lunaZoneReload.observe(this) {
            it.getContent()?.let {
                if (navController?.currentDestination?.id == R.id.navigation_lifeOsFragment) {
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
                if (viewModel.ringDataStore.getRingDevice() == null) {
                    showShortToast(getString(R.string.text_luna_ai_message))
                    return@observe
                }

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

                    if (response.uiComponentType is UIComponentType.RetryApiDialog) {
                        val code =
                            viewModel.getApiErrorCode((response.uiComponentType as UIComponentType.RetryApiDialog).message)

                        if (code != null) {
                            binding.layoutRetry.tvErrorCode.text =
                                getString(R.string.text_error_code_value, code)
                        } else {
                            binding.layoutRetry.tvErrorCode.text = ""
                        }
                    } else {
                        binding.layoutRetry.tvErrorCode.text = ""
                    }

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

                        viewModel.syncTextState.value = viewModel.getSyncingMessage(
                            this@OreoMainActivity,
                            syncDataStatus.progress,
                            syncDataStatus.total,
                            syncDataStatus
                        )

                        //viewModel.syncTextState.value = getString(R.string.text_syncing_dot)

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

                        viewModel.syncTextState.value = viewModel.getSyncingMessage(
                            this@OreoMainActivity,
                            syncDataStatus.progress,
                            syncDataStatus.total,
                            syncDataStatus
                        )

                        /*   binding.lytHeader.pbSync.max = syncDataStatus.total
                           binding.lytHeader.pbSync.progress = syncDataStatus.progress
                           binding.lytHeader.pbSync.visible()
                           binding.lytHeader.tvHeaderStatus.apply {
                               text = getString(R.string.text_syncing_dot)
                               visible()
                           }*/
                    }

                    is SyncEvents.Success -> {
                        //viewModel.syncProgressBarState.value = null
                        if (viewModel.syncTextState.value != null) {
                            viewModel.syncTextState.value = viewModel.getSyncingMessage(
                                this@OreoMainActivity,
                                syncDataStatus.progress,
                                syncDataStatus.total,
                                syncDataStatus
                            )
                        }


                        /* binding.lytHeader.pbSync.max = syncDataStatus.total
                         binding.lytHeader.pbSync.progress = syncDataStatus.progress
                         binding.lytHeader.tvHeaderStatus.gone()
                         binding.lytHeader.pbSync.gone()
                         resetSwipeLoadingAnim()*/
                    }

                    SyncEvents.ServerSyncFailed -> {
                        viewModel.syncTextState.value = null
                        viewModel.syncProgressBarState.value = null
                    }

                    SyncEvents.ServerSyncStarted -> {
                        viewModel.showSyncLoader = false
                        /*if(viewModel.showSyncLoader){
                            binding.progressBar.root.visible()
                        }*/
                        viewModel.syncTextState.value = viewModel.getSyncingMessage(
                            this@OreoMainActivity,
                            0,
                            0,
                            syncDataStatus
                        )
                    }

                    SyncEvents.ServerSyncSuccess -> {
                        viewModel.syncProgressBarState.value = null
                        if (viewModel.syncTextState.value != null) {
                            viewModel.syncTextState.value = viewModel.getSyncingMessage(
                                this@OreoMainActivity,
                                0,
                                0,
                                syncDataStatus
                            )

                            syncCompletedState()
//                            viewModel.getNudgeData()
                        }

                        viewModel.updateAppTrackingEvent(AppTrackEvent.SYNC)


                        //viewModel.syncTextState.value = null
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
                //binding.pbSync.gone()
                binding.lottieSync.cancelAnimation()
                binding.lottieSync.gone()
            } else {
                binding.lottieSync.visible()
                animateSyncLottie()

                /*val (progress, total) = it
                binding.pbSync.visible()
                binding.pbSync.max = total
                binding.pbSync.progress = progress*/
            }
        }
    }

    private fun syncCompletedState() {
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.syncTextState.value = null
        }, 1500)
    }

    private fun animateSyncLottie() {
        if (binding.lottieSync.isAnimating) {
            return
        }
        binding.lottieSync.setAnimation(R.raw.lottie_sync)
        binding.lottieSync.repeatCount = LottieDrawable.INFINITE
        binding.lottieSync.playAnimation()
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
                R.id.navigation_lifeOsFragment -> {
                    binding.view27.visible()
                    binding.navView.root.visible()

                    if (destination.id == R.id.navigation_oreo_home || destination.id == R.id.navigation_oreo_workouts
                        || destination.id == R.id.sleepDashFragment
                    ) {
                        viewModel.handleAddWorkoutVisibility()
                    } else {
                        viewModel.addWorkoutCtaVisibility.value = false
                        viewModel.isActivityWorkAdd = false
                    }

                    //binding.btnAddWorkout.visible()//todo add today condition
                }

                R.id.oActivityListFragment -> {
                    binding.view27.gone()
                    binding.navView.root.gone()

                    if (viewModel.isDevicePaired() != null) {
                        viewModel.addWorkoutCtaVisibility.value = true
                        viewModel.isActivityWorkAdd = true
                    }
                }

                else -> {
                    binding.view27.gone()
                    binding.navView.root.gone()
                    viewModel.addWorkoutCtaVisibility.value = false
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
        if (viewModel.sessionManager.syncCompleted.value?.peekContent() is SyncEvents.ServerSyncSuccess || viewModel.sessionManager.syncCompleted.value?.peekContent() is SyncEvents.Success) {
            viewModel.syncTextState.value = null
        }
        navController?.addOnDestinationChangedListener(navListener)
        viewModel.ringDataStore.getRingDevice()?.let {
            if (viewModel.sessionManager.connectStateRing.value == null) {
                viewModel.sessionManager.setConnectStateRing(ConnectState.Connecting(it))
                ApplicationUtils.setRescueWorkManager(this)
            }

            if (it.ringInfo?.image3 == null || it.ringInfo?.chargerRingUrl == null) {
                viewModel.getPortableChargerAndRingImage(it)
            }

            if (viewModel.sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess) {
                viewModel.startDisconnectTimer()
            }
            viewModel.syncRecordedWorkoutData()
        }

        viewModel.getNudgeData()


        viewModel.checkForForceUpdate()


        //viewModel.shouldResetMasterDates()


        viewModel.loginFreshChatUser()

        viewModel.incrementOpenCount()

        //For in app popup
        MoEInAppHelper.getInstance().showInApp(this)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.extras?.let { intentExtra ->

            val appLink = intent.getSerializableExtra(APP_LINK) as? AppLinks
            if (appLink != null) {


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
            } else if (intentExtra.containsKey(APP_LINK)) {
                val data = intent.getSerializableExtra(APP_LINK) as AppLinks
                handleAppLinkNavigation(data)
            } else {

            }

        }

    }

    private fun handleAppLinkNavigation(appLink: AppLinks) {
        val savedWorkout = viewModel.ringDataStore.getOngoingRecordWorkout()
        if (savedWorkout != null) return


        when (appLink) {
            AppLinks.REFERRAL -> {
                viewModel.getReferralInfo { data ->
                    if (this@OreoMainActivity.navController?.currentDestination?.id != R.id.referralFragment) {
                        this@OreoMainActivity.navController?.navigate(
                            R.id.referralFragment,
                            bundleOf("referralInfo" to data)
                        )
                    }

                }
            }

            AppLinks.NOTIFICATION_CONTROL -> {
                if (this@OreoMainActivity.navController?.currentDestination?.id != R.id.notificationSettingFragment) {
                    this@OreoMainActivity.navController?.navigate(R.id.notificationSettingFragment)
                }
            }

            AppLinks.PROFILE -> {
                if (this@OreoMainActivity.navController?.currentDestination?.id != R.id.profileFragmentOreo) {
                    this@OreoMainActivity.navController?.navigate(R.id.profileFragmentOreo)
                }
            }

            AppLinks.SLEEP_PLANNER -> {
                if (this@OreoMainActivity.navController?.currentDestination?.id != R.id.sleepPlannerFragment) {
                    this@OreoMainActivity.navController?.navigate(R.id.sleepPlannerFragment)
                }
            }

            AppLinks.DASHBOARD -> {
                viewModel.navigateTo(BottomNavOption.HOME)
            }
            AppLinks.LUNA_SETTINGS -> {
                navController?.navigate(R.id.settingsFragment)
            }

            AppLinks.LUNA_AI -> {

                if (viewModel.ringDataStore.getRingDevice() == null) {
                    showShortToast(getString(R.string.text_luna_ai_message))
                    return
                }

                navController?.navigate(R.id.aiTopQuestionsFragment,
                    bundleOf("aiTopic" to AITopics.GENERAL))

                /*val (frag, bundle) = ChatGptFragment.getStartData(
                    null,
                    null,
                    null,
                    null,
                    AITopics.GENERAL
                )
                navController?.navigate(frag, bundle)*/

                //viewModel.navigateTo(BottomNavOption.LUNA_AI)
            }

            AppLinks.FEATURE_REQUEST -> {
                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    val cannyState = viewModel.ringDataStore.getCannyState()
                    if (cannyState) {
                        viewModel.getCannyFeedbackUrl()
                    }
                }
            }

            AppLinks.FEMALE_HEALTH -> {
                val user = viewModel.localDataStore.getUser()
                if (user?.userInfo?.gender.equals("female", true).not()) {
                    showShortToast(getString(R.string.text_feature_not_enabled_for_this_user))
                } else {
                    viewModel.getCycleHistoryData() {
                        navController?.navigate(R.id.fragmentCycleTracker)
                    }
                }
            }

            AppLinks.CAFFEINE_WINDOW -> {
                viewModel.getCaffeineWindowData { caffeineGraphData ->
                    if (caffeineGraphData.status == true) {
                        val caffeineValues = ArrayList<Int>()
                        caffeineGraphData.caffeine_window.forEach {
                            caffeineValues.add(it.dose)
                        }
                        val mainData = CaffeineWindowData(
                            wakeUpTime = caffeineGraphData.wakeUpTime,
                            bedTime = caffeineGraphData.bedTime,
                            caffeineStartTime = caffeineGraphData.caffeineStartTime,
                            caffeineEndTime = caffeineGraphData.caffeineEndTime,
                            caffeineValues = caffeineValues,
                        )

                        val (message, maxQuantity) = viewModel.getMaxQuantityAndMessage(mainData)

                        mainData.apply {
                            this.message = message
                            this.maxQuantity = maxQuantity
                        }


                        navController?.navigate(
                            R.id.caffeineWindowScreenFragment,
                            Bundle().apply {
                                this.putParcelable("caffeineGraphData", mainData)
                            }
                        )
                    }
                }
                //navController?.navigate(R.id.caffeineWindowScreenFragment)
            }

            AppLinks.ADD_CAFFEINE_INTAKE -> {
                navController?.navigate(
                    R.id.addActivityTimelineFragment,
                    bundleOf(
                        "showTimeline" to false,
                        "key" to CircadianAlignmentViewModel.caffeine_window_key,
                        "srcKey" to "appLink",
                    )
                )
            }
            AppLinks.ADD_LIGHT_EXPOSURE -> {
                navController?.navigate(
                    R.id.addActivityTimelineFragment,
                    bundleOf(
                        "showTimeline" to false,
                        "key" to CircadianAlignmentViewModel.light_exposure_key,
                        "srcKey" to "appLink",
                    )
                )
            }

            AppLinks.ADD_MEAL -> {
                navController?.navigate(
                    R.id.addActivityTimelineFragment,
                    bundleOf(
                        "showTimeline" to false,
                        "key" to CircadianAlignmentViewModel.meal_window_key,
                        "srcKey" to "appLink",
                    )
                )
            }
            AppLinks.ADD_RECOVERY -> {
                navController?.navigate(
                    R.id.addActivityTimelineFragment,
                    bundleOf(
                        "showTimeline" to false,
                        "key" to "recovery",
                        "srcKey" to "appLink",
                    )
                )
            }
            AppLinks.ADD_SUPPLEMENTS -> {
                navController?.navigate(
                    R.id.addActivityTimelineFragment,
                    bundleOf(
                        "showTimeline" to false,
                        "key" to "supplements",
                        "srcKey" to "appLink",
                    )
                )
            }
            AppLinks.ADD_ALCOHOL -> {
                navController?.navigate(
                    R.id.addActivityTimelineFragment,
                    bundleOf(
                        "showTimeline" to false,
                        "key" to "alcohol",
                        "srcKey" to "appLink",
                    )
                )
            }


            AppLinks.ADD_WORKOUT -> {
                navController?.navigate(
                    R.id.addActivityTimelineFragment,
                    bundleOf(
                        "showTimeline" to false,
                        "key" to "workout",
                        "srcKey" to "appLink",
                    )
                )
            }

            AppLinks.LOG_PERIOD_SYMPTOMS -> {
                val user = viewModel.localDataStore.getUser()
                if (user?.userInfo?.gender.equals("female", true).not()) {
                    showShortToast(getString(R.string.text_feature_not_enabled_for_this_user))
                } else {
                    viewModel.getCycleHistoryData() {
                        navController?.navigate(
                            R.id.addActivityTimelineFragment,
                            bundleOf(
                                "showTimeline" to false,
                                "key" to "symptom",
                                "srcKey" to "appLink",
                            )
                        )
                    }
                }


            }

            AppLinks.ADD_SLEEP -> {
                navController?.navigate(
                    R.id.addActivityTimelineFragment,
                    bundleOf(
                        "showTimeline" to false,
                        "key" to CircadianAlignmentViewModel.sleep_key,
                        "srcKey" to "appLink",
                    )
                )
            }

            AppLinks.SLEEP_DASH -> {
                viewModel.navigateTo(BottomNavOption.SLEEP)
            }

            AppLinks.READINESS_DASH -> {
                viewModel.navigateTo(BottomNavOption.READINESS)
            }

            AppLinks.ACTIVITY_DASH -> {
                viewModel.navigateTo(BottomNavOption.ACTIVITY)
            }

            AppLinks.CIRCADIAN_PAGE -> {
                val isOnBoard = viewModel.localDataStore.getCircadianGraphData()?.onboarding ?: false
                if(isOnBoard){
                    navController?.navigate(R.id.circadianAlignmentFragment)
                }else {
                    navController?.navigate(R.id.circadianSplashScreenFragment)
                }
            }

            AppLinks.STRESS_DETAIL -> {
                if (viewModel.localDataStore.getStressWalkthroughShownStatus()) {
                    navController?.navigate(R.id.fragmentOStressDetails)
                } else {
                    navController?.navigate(R.id.stressSplashFragment)
                }
            }

            AppLinks.HR_DETAIL -> {
                navController?.navigate(R.id.fragmentHeartRateDetails)
            }

            AppLinks.TIMELINE_LIST -> {
                navController?.navigate(
                    R.id.addActivityTimelineFragment,
                    bundleOf(
                        "showTimeline" to false,
                        "key" to "",
                        "srcKey" to "appLink",
                    )
                )
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

            if (binding.fabRevealOverlay.isVisible) {
                hideFabRadialMenu(false)
                return
            }


            when (it.currentDestination?.id) {
                R.id.navigation_oreo_home, R.id.navigation_oreo_readiness,
                R.id.navigation_oreo_workouts, R.id.sleepDashFragment, R.id.navigation_lifeOsFragment -> {

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
                binding.navView.ivLunaAi.setImageResource(R.drawable.ic_dash_luna_zone_default)



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
                binding.navView.ivLunaAi.setImageResource(R.drawable.ic_dash_luna_zone_default)


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
                binding.navView.ivLunaAi.setImageResource(R.drawable.ic_dash_luna_zone_default)


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
                binding.navView.ivLunaAi.setImageResource(R.drawable.ic_dash_luna_zone_default)

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
                binding.navView.ivLunaAi.setImageResource(R.drawable.ic_dash_luna_zone_selected)

                binding.navView.apply {
                    ivGlowHome.gone()
                    ivGlowSleep.gone()
                    ivGlowReadiness.gone()
                    ivGlowActivity.gone()
                    ivGlowLunaAi.visible()
                }
                val lastDestination = navController?.currentDestination
                if (lastDestination?.id != R.id.navigation_lifeOsFragment) {
                    viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.home_footer_lunaai)
                    navController?.popBackStack(R.id.navigation_lifeOsFragment, true)
                    navController?.navigate(R.id.navigation_lifeOsFragment)
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
