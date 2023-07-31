package com.noisefit.ui.dashboard.summary

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.bold
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.hookedonplay.decoviewlib.events.DecoEvent
import com.noisefit.BottomNavOption
import com.noisefit.luna.BuildConfig
import com.noisefit.MainViewModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSummaryBinding
import com.noisefit.receiver.workManager.HealthOverviewDataType
import com.noisefit.ui.common.*
import com.noisefit.ui.common.bottomSheet.ALERT_REQUEST_KEY
import com.noisefit.ui.feeds.create.CREATE_POST_KEY
import com.noisefit.ui.feeds.create.PostContent
import com.noisefit.ui.npl.NPL_TERMS_KEY
import com.noisefit.ui.npl.summary.IplDashboardAdapter
import com.noisefit.ui.onboarding.pairing.DeviceSetupActivity
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit.ui.settings.feedbacknew.LATER
import com.noisefit.ui.settings.feedbacknew.RATE_NOW
import com.noisefit.ui.settings.helpAndSupport.HelpAndSupportType
import com.noisefit.ui.web.WebViewActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit.util.ApplicationUtils.seriesItemWithInset
import com.noisefit.util.ApplicationUtils.seriesItemWithoutInset
import com.noisefit.watch.SDKWatchType
import com.noisefit_commans.constants.EventConstants
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.TrinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.enums.HealthOverViewHistoryType
import com.noisefit_commans.data.model.*
import com.noisefit_commans.data.response.LiveMatch
import com.noisefit_commans.data.response.NplLeague
import com.noisefit_commans.data.response.PrizeInfo
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.DateFormats.checkTimeDifferenceMoreThanNDays
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.share.ShareUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.dkzwm.widget.srl.RefreshingListenerAdapter


const val RING_ANIMATION = 1000L

@AndroidEntryPoint
class SummaryFragment : BaseFragment<FragmentSummaryBinding>(FragmentSummaryBinding::inflate) {

    //    private lateinit var iplDashboardAdapter: IplDashboardAdapter
    private var bluetoothAlertDialog: AlertDialog? = null
    private val viewModel: SummaryViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()


    private val healthOverviewAdapter by lazy {
        SummaryHealthOverviewAdapter()
    }

    private val imageSliderAdapter: DashboardBannerSliderAdapter by lazy {
        DashboardBannerSliderAdapter(object : DashboardBannerAction {
            override fun onBannerClicked(banner: DashboardBanner, position: Int) {
                openBannerUrl(banner, position)
            }

            override fun imageLoadedSuccessfully() {
                tryCatch {
                    nullableBinding?.lytImageSlider?.vpImageSlider?.post {
                        nullableBinding?.lytImageSlider?.vpImageSlider?.requestLayout()
                        nullableBinding?.lytImageSlider?.vpImageSlider?.requestTransform()
                    }
                }

            }
        })
    }

    private val recentWorkoutsAdapter by lazy {
        RecentWorkoutsAdapter()
    }

    private val alertAdapter by lazy {
        AlertAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LOGS.d("summary_init")
        resetProgressValue()
        setRing()
        viewModel.sessionManager.addUserAttributeToInsider(false, HashMap<String, Any>().apply {
            this["home_page_visit"] = ""
        })
        viewModel.initData()
        initUi()
        setAdapter()
        viewModel.getInitialOfflineData()
        setImageSlider()
        viewModel.getAlerts()
        mainViewModel.getShopBanners()

    }

    private fun resetProgressValue() {
        viewModel.summary.apply {
            caloriesProgressCompleted = -1f
            distanceProgressCompleted = -1f
            stepsProgressCompleted = -1f
        }
    }

    override fun initListener() {
        binding.lytDeviceConnected.root.setOnClickListener {
            if (viewModel.sessionManager.connectState.value !is ConnectState.ConnectSuccess) {
                navigate(R.id.helpAndSupportListFragment, Bundle().apply {
                    putString("type", HelpAndSupportType.PAIRING_AND_CONNECTIVITY.name)
                    putParcelable("helpSupportItem", null)
                })
            }
        }

        binding.lytDashHeader.ivProfileImage.setOnLongClickListener {
            if (BuildConfig.DEBUG) {
                navigate(R.id.logsDisplayFragment)
            }
            return@setOnLongClickListener true
        }

        binding.lytDataSummary.ivShare.setOnClickListener {
            setFragmentResultListener(CREATE_POST_KEY) { _, bundle ->
                val updated = bundle.getBoolean("updated")
                if (updated) {
                    mainViewModel.navigateTo(BottomNavOption.COMMUNITY)
                }
            }
            navigate(SummaryFragmentDirections.actionSummaryFragmentToCreatePostFragment().apply {
                this.shareContent = PostContent.RINGS
            })
        }

        binding.lytSummaryWorkout.root.setOnClickListener {
            navigate(R.id.dashboardWorkoutFragment)
        }

        binding.lytRewardsAwait.root.setOnClickListener {
            logInsiderAppEvent(InsiderAppEvents.HOMESCREEN_REWARD_AWAITS_YOU_CLICK)
            navigate(R.id.coinFragment)
        }

        binding.lytStepStreaks.root.setOnClickListener {
            if (viewModel.streaks.value?.msg.isNullOrEmpty()) {
                logInsiderAppEvent(InsiderAppEvents.HOMESCREEN_NORMAL_STEP_STREAK_CLICK)
            } else logInsiderAppEvent(InsiderAppEvents.HOMESCREEN_ALERT_STEP_STREAK_CLICK)
            navigate(R.id.stepStreakFragment)
        }
        binding.lytDashHeader.vStreakBack.setOnClickListener {
            navigate(R.id.stepStreakFragment)
        }

        binding.layoutRefresh.animationView.setAnimation(R.raw.loading_swipe_anim)



        binding.swipeToRefresh.setOnRefreshListener(object : RefreshingListenerAdapter() {
            override fun onRefreshing() {
                super.onRefreshing()

                LOGS.d("SyncDataWork: starting job")
                if (!viewModel.isDeviceConnected()) {
                    binding.swipeToRefresh.refreshComplete()
                    return
                }

                binding.layoutRefresh.textSyncingData.visible()
                viewModel.sessionManager.forceSyncDataWithServer = true
                syncData()
            }
        })

//        binding.lytSummaryHealth.btnEditHealth.setOnClickListener {
//            logInsiderAppEvent(InsiderAppEvents.HOMESCREEN_HEALTHOVERVIEW_EDITHEALTHOVERVIEW_CLICK)
//            navigate(R.id.editDashboardFragment)
//        }
//        binding.lytSummaryHealth.btnViewAll.setOnClickListener {
//            logInsiderAppEvent(InsiderAppEvents.HOMESCREEN_HEALTHOVERVIEW_VIEWALL_CLICK)
//            navigate(R.id.healthOverviewFragment)
//        }
//        binding.lytSummaryHealth.root.setOnClickListener {
//            logInsiderAppEvent(InsiderAppEvents.HOMESCREEN_HEALTHOVERVIEW_VIEWALL_CLICK)
//            navigate(R.id.healthOverviewFragment)
//        }
        healthOverviewAdapter.itemClickListener = { view, item, position ->
            when (item) {
                is HealthOverview.BloodOxygen -> {
                    logInsiderAppEvent(InsiderAppEvents.HOMESCREEN_HEALTHOVERVIEW_BLOODOXYGEN_CLICK)
                    navigate(R.id.bloodOxygenDetailsFragment)
                }

                is HealthOverview.BodyTemp -> {
                    logInsiderAppEvent(InsiderAppEvents.HOMESCREEN_HEALTHOVERVIEW_BODYTEMPERATURE_CLICK)
                    navigate(R.id.bodyTemperatureDetailsFragment)
                }

                is HealthOverview.Distance -> {
                    logInsiderAppEvent(InsiderAppEvents.HOMESCREEN_HEALTHOVERVIEW_DISTANCE_CLICK)
                    navigate(
                        SummaryFragmentDirections.actionSummaryFragmentToStepsDetailsFragment(
                            HealthOverViewHistoryType.Distance.name
                        )
                    )
                }

                is HealthOverview.HeartRate -> {
                    logInsiderAppEvent(InsiderAppEvents.HOMESCREEN_HEALTHOVERVIEW_HEARTRATE_CLICK)
                    navigate(R.id.heartRateDetailsFragment)
                }

                is HealthOverview.Sleep -> {
                    logInsiderAppEvent(InsiderAppEvents.HOMESCREEN_HEALTHOVERVIEW_SLEEP_CLICK)
                    navigate(R.id.sleepDetailsFragment)
                }

                is HealthOverview.Steps -> {
                    logInsiderAppEvent(InsiderAppEvents.HOMESCREEN_HEALTHOVERVIEW_STEPSCOUNT_CLICK)
                    navigate(
                        SummaryFragmentDirections.actionSummaryFragmentToStepsDetailsFragment(
                            HealthOverViewHistoryType.Steps.name
                        )
                    )
                }

                is HealthOverview.Calories -> {
                    logInsiderAppEvent(InsiderAppEvents.HOMESCREEN_HEALTHOVERVIEW_CALORIES_CLICK)
                    navigate(
                        SummaryFragmentDirections.actionSummaryFragmentToStepsDetailsFragment(
                            HealthOverViewHistoryType.Calories.name
                        )
                    )
                }

                is HealthOverview.Stress -> {
                    logInsiderAppEvent(InsiderAppEvents.HOMESCREEN_HEALTHOVERVIEW_STRESS_CLICK)
                    navigate(R.id.stressDetailsFragment)
                }
            }

        }

        binding.lytPairYourDeviceHeader.btnPairDevice.setOnClickListener {

            checkLocationPermission(permissionGranted = {
                checkBluetooth()
            })

        }

        binding.lytSummaryRecentWorkouts.btnViewAll.setOnClickListener {
            logInsiderAppEvent(InsiderAppEvents.HOMESCREEN_RECENTWORKOUTS_VIEWALL_CLICK)
            navigate(R.id.navigation_activity)
        }
        binding.lytDashHeader.ivProfileImage.setOnClickListener {
            logInsiderAppEvent(InsiderAppEvents.HAMBURGER_MENU_CLICK)
            //navigate(R.id.diyWatchFaceFragment)
            navigate(R.id.myProfileFragment)
//            navigate(R.id.oreoSleepDetailsFragment)
        }

        binding.lytDashHeader.vCoinsBack.setOnClickListener {
            logInsiderAppEvent(InsiderAppEvents.HOMESCREEN_COIN_ICON_CLICK)
            navigate(R.id.coinFragment)
        }
        binding.lytNplCorrectAnswer.root.setOnClickListener {
            showNplDashboard()
        }
        binding.lytNplTargetAchieve.root.setOnClickListener {
            showNplDashboard()
        }
    }


    private fun checkBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkBluetoothPermission {
                openPairDevice()
            }
        } else {
            openPairDevice()
        }
    }

    private val TAG = SummaryFragment::class.java.simpleName

    override fun onResume() {
        super.onResume()
        viewModel.handleBackGroundPermissionAlert()
        viewModel.getRecentWorkouts()
        viewModel.getRewardsData()
        if (viewModel.localDataStore.getConnectedDevice() == null) {
            binding.lytDeviceConnected.root.gone()
            binding.lytPairYourDeviceHeader.root.visible()
        } else {
            binding.lytPairYourDeviceHeader.root.gone()
        }
        /*
         * show custom pop up
          * */
        val shouldShowReview = mainViewModel.getShouldShowReview()
        LOGS.d(TAG, "Should Show Review $shouldShowReview")
        if (shouldShowReview) {
            setFragmentResultListener(RATE_NOW) { key, bundle ->
                val isSelected = bundle.getBoolean("isSelected")
                if (isSelected) {
                    ShareUtil.openPlayStore(requireContext(), "com.noisefit")
                    viewModel.localDataStore.setShowReviewPopUp(true)
                    navigateUpSafe()
                }
            }
            setFragmentResultListener(LATER) { key, bundle ->
                val isSelected = bundle.getBoolean("isSelected")
                if (isSelected) {
                    if (viewModel.localDataStore.getLaterNowLastReviewShownTimeStamp()
                            .checkTimeDifferenceMoreThanNDays(5)
                    ) {
                        viewModel.localDataStore.setTenDayLaterNowLasTimeStamp(System.currentTimeMillis())
                    } else {
                        viewModel.localDataStore.setLaterNowLastReviewShownTimeStamp(System.currentTimeMillis())
                    }
                    viewModel.localDataStore.setShowReviewPopUp(false)
                    navigateUpSafe()
                }
            }
            if (viewModel.localDataStore.isShowReviewPopUp() && (viewModel.localDataStore.getLaterNowLastReviewShownTimeStamp()
                    .checkTimeDifferenceMoreThanNDays(5) || viewModel.localDataStore.getTenDayLaterNowLastTimeStamp()
                    .checkTimeDifferenceMoreThanNDays(10))
            )
                navigate(R.id.rateNowBottomSheet, Bundle().apply {
                    putString("cameFrom", "summary")
                })
        }
    }

//    private fun setIplViewPager(data: Pair<PrizeInfo?, LiveMatch?>) {
//
//
//        val listOfIplFragment = ArrayList<String>()
//
//        if (data.second != null) {
//            listOfIplFragment.add(IplDashBoardEnum.LIVE_SCORE.name)
//        }
//        if (data.first != null) {
//            val prizeInfo = data.first!!
//            val winsRequired = (prizeInfo.rewardWins ?: 0) - (prizeInfo.userWins ?: 0)
//
//            if (winsRequired > (prizeInfo.matchesLeft ?: 0)) {
//                listOfIplFragment.add(IplDashBoardEnum.LOST_STATE.name)
//            } else {
//                listOfIplFragment.add(IplDashBoardEnum.PREDICTION.name)
//            }
//        }
//        if (listOfIplFragment.isEmpty()) {
//            binding.iplTabLayout.gone()
//            binding.iplViewPager.gone()
//            return
//        }
//        binding.iplViewPager.visible()
//        if (listOfIplFragment.size > 1) {
//            binding.iplTabLayout.visible()
//        } else {
//            binding.iplTabLayout.gone()
//        }
//        iplDashboardAdapter =
//            IplDashboardAdapter(childFragmentManager, lifecycle, data, listOfIplFragment)
//        binding.iplViewPager.apply {
//            clipToPadding = false
//            clipChildren = false
//            offscreenPageLimit = 3
//            setPageTransformer(CompositePageTransformer().apply {
//                addTransformer(MarginPageTransformer(40))
//            })
//            adapter = iplDashboardAdapter
//        }
//
//        TabLayoutMediator(
//            binding.iplTabLayout, binding.iplViewPager
//        ) { _, _ -> }.attach()
//
//
//        if (listOfIplFragment.size > 1) {
//            viewModel.startNplTimer()
//        }
//
//    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkBluetoothPermission(
        permissionGranted: () -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.BLUETOOTH_CONNECT
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
            openPairDevice()
        } else {
            uiController.onApiErrorReceived(
                ErrorResponse(
                    UIComponentType.AreYouSureDialog(getString(
                        R.string.text_permission_required
                    ),
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

                        })
                )
            )

        }
    }

    fun openPairDevice() {
        startActivity(PairDeviceActivity.getStartIntent(requireContext(), true))
        //activity?.finish()
    }

    private fun checkLocationPermission(
        permissionGranted: () -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
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
            checkBluetooth()
        } else {
            uiController.onApiErrorReceived(ErrorResponse(UIComponentType.AreYouSureDialog(getString(
                R.string.text_permission_required
            ),
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
                    }
                }

            )))
        }
    }


    private fun logInsiderAppEvent(eventName: String) {
        viewModel.sessionManager.logInsiderAppEvent(eventName)
    }

    private fun shouldSync() {
        val lastSyncTime = viewModel.sessionManager.getLastSyncTime() ?: 0L
        if (kotlin.math.abs(DateFormats.getTimeStamp() - lastSyncTime) > 300000L) {
            syncData()
        }
    }

    private fun setImageSlider() {
        binding.lytImageSlider.vpImageSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(imageTransformer)
            adapter = imageSliderAdapter
        }
        TabLayoutMediator(
            binding.lytImageSlider.tabLayout, binding.lytImageSlider.vpImageSlider
        ) { _, _ -> }.attach()

        binding.lytImageSlider.vpImageSlider.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val maxCount = imageSliderAdapter.itemCount
                if (maxCount == 0) {
                    binding.lytImageSlider.tvCurrentTabCount.gone()
                } else {
                    binding.lytImageSlider.tvCurrentTabCount.visible()
                    binding.lytImageSlider.tvCurrentTabCount.text = "${position + 1}/$maxCount"
                }
            }

        })


    }

    private fun updatePagerHeightForChild(view: View, pager: ViewPager2) {
        view.post {
            val wMeasureSpec =
                View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
            val hMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            view.measure(wMeasureSpec, hMeasureSpec)
            pager.layoutParams = (pager.layoutParams).also { lp -> lp.height = view.measuredHeight }
            pager.invalidate()
        }
    }

    private val imageTransformer = CompositePageTransformer().apply {
        addTransformer(MarginPageTransformer(40))
        addTransformer { page, position ->
            nullableBinding?.lytImageSlider?.vpImageSlider?.let {
                updatePagerHeightForChild(page, it)
            }
        }
    }


    private fun showBleCallingDialog() {

        if (bluetoothAlertDialog != null) {
            return
        }

        if (viewModel.watchesSDK.getWatchType() == SDKWatchType.SDK_RYEEX) {
            return
        }
        //check for non calling watches
        if (viewModel.summary.bleCallingStatus?.first == false) {
            return
        }
        //check for if users already choose do not show again
        if (viewModel.summary.bleCallingStatus?.third == true) {
            return
        }
        //check for if users already choose cancel
        if (WatchInfoGlobals.hideBleCallingDialogForThisSession) {
            return
        }
        val desc = "${
            getString(
                R.string.text_to_use_the_bluetooth_calling_feature,
                viewModel.summary.bleCallingStatus?.second ?: ""
            )
        }\n${getString(R.string.text_note_this_will_reduce_the_watch_s_battery_life)}"

//        val otherText = if (viewModel.watchesSDK.getWatchType() == SDKWatchType.SDK_QUBE) {
//            getString(R.string.text_media_play_stop)
//        } else {
//            null
//        }


        bluetoothAlertDialog = showBleCallingDialog(getString(
            R.string.text_bluetooth_calling
        ), desc, "", true, getString(R.string.text_proceed), object : TrinaryActionCallback {
            override fun yes() {
                WatchInfoGlobals.hideBleCallingDialogForThisSession = true
                startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                viewModel.updateBluetoothDialogState(true)
            }

            override fun maybe() {
                WatchInfoGlobals.hideBleCallingDialogForThisSession = true
                viewModel.updateBluetoothDialogState(true)
            }

            override fun no() {
                WatchInfoGlobals.hideBleCallingDialogForThisSession = true
            }
        })

        bluetoothAlertDialog?.setOnDismissListener {
            WatchInfoGlobals.hideBleCallingDialogForThisSession = true
            viewModel.updateBluetoothDialogState(true)
        }
    }


    override fun onStop() {
        super.onStop()
        bluetoothAlertDialog?.dismiss()
        bluetoothAlertDialog = null
    }

    @SuppressLint("SetTextI18n")
    override fun subscribeObservers() {

        viewModel.rewardsPoints.observe(this) {
            binding.lytDashHeader.tvCoins.text = it.numberFormatter()

        }

        viewModel.sessionManager.needToUpdateStreakData.observe(this) {
            it.getContent()?.let { value ->
                if (value.second && System.currentTimeMillis() <= (value.first + 50000)) {
                    viewModel.getRewardsData()
                }
            }
        }


//        viewModel.iplData.observe(this) {
//            it?.let {
//                setIplViewPager(it)
//
//            }
//        }

        viewModel.nplLeagueData.observe(this) {
            setNplLeagueData(it)
        }




        viewModel.streaks.observe(this) { streak ->
            if (streak == null) {
                binding.lytDashHeader.tvStreakCount.text = "0"
                binding.lytStepStreaks.root.gone()
                binding.lytDashHeader.imageStreak.setImageResource(R.drawable.ic_streak_dash_inactive)
            } else {
                binding.lytDashHeader.tvStreakCount.text = "${streak.curr_streak_length ?: 0}"
                binding.lytStepStreaks.root.visible()
                binding.lytDashHeader.imageStreak.setImageResource(R.drawable.ic_streak_dash)

                binding.lytStepStreaks.layoutCurrentStreak.apply {
                    if (streak.curr_streak_length == streak.current_target_days) {
                        tvCurrentDay.text = "Milestone Day"
                        tvCurrentStreakDays.gone()
                    } else {
                        tvCurrentDay.text = "${streak.curr_streak_length ?: 0}"
                        tvCurrentStreakDays.text = "of ${streak.current_target_days ?: 0} days"
                        tvCurrentStreakDays.visible()

                    }
                    tvCurrentMultiplier.text = "${streak.current_multiplier ?: 0}x"
                    tvLevelCodeFirst.text = "${streak.current_target_days ?: 0}"
                    tvLevelCode.text = "${streak.next_target_days ?: 0}"
                    tvCoinMultiplier1.text = "${streak.next_multiplier ?: 0}x"
                    tvCoinMultiplier2.text = "${streak.last_multiplier ?: 0}x"
                    pbSteps.progress = streak.currentStreakProgress()

                    if (streak.msg.isNullOrEmpty()) {
                        tvMessage.gone()
                        vDividerRed.gone()
                        binding.lytStepStreaks.rootView.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
                        binding.lytDashHeader.vStreakBack.setBackgroundResource(R.drawable.back_modal_new_20)
                        context?.let {
                            pbSteps.setIndicatorColor(
                                ContextCompat.getColor(
                                    it, R.color.accent_color_purple
                                )
                            )
                        }
                    } else {
                        vDividerRed.visible()
                        tvMessage.visible()
                        tvMessage.text = streak.msg
                        binding.lytStepStreaks.rootView.setBackgroundResource(R.drawable.back_modal_new_red)
                        binding.lytDashHeader.vStreakBack.setBackgroundResource(R.drawable.back_modal_new_red_20)
                        context?.let {
                            pbSteps.setIndicatorColor(
                                ContextCompat.getColor(
                                    it, R.color.color_error
                                )
                            )
                        }
                    }


                }

            }
        }

        viewModel.rewardAwaited.observe(this) {
            if (it) {
                binding.lytRewardsAwait.root.visible()
            } else {
                binding.lytRewardsAwait.root.gone()
            }
        }

        /*viewModel.sessionManager.forceRefreshRecentWorkout.observe(this) {
            it.getContent()?.let {
                viewModel.getRecentWorkouts()
            }
        }*/

        viewModel.dashboardAlerts.observe(this) {
            if (it.isNullOrEmpty()) {
                binding.lytDashAlerts.root.gone()
            } else {
                binding.lytDashAlerts.root.visible()
                setAlerts(it)
            }

        }

        viewModel.summary.healthOverviewData.observe(this) { healthOverviewData ->
            if (healthOverviewData != null) {
                setOverviewData(healthOverviewData)
            }
        }

        viewModel.summary.showPromotionalBanner.observe(this) { event ->
            event.peekContent()?.let { status ->
                if (status) {
                    viewModel.summary.showPromotionalBanner.value = Event(false)
                    navigate(SummaryFragmentDirections.actionSummaryFragmentFragToPromotionalBottomSheet())
                }
            }

        }

        mainViewModel.displayBanners.observe(this) { shopBannerModel ->
            //binding.lytImageSlider.pgbr.gone()
            imageSliderAdapter.setDataSet(shopBannerModel)

            with(nullableBinding) {
                if (shopBannerModel.isEmpty()) {
                    //this?.lytImageSlider?.tvMessage?.visible()
                } else {
                    //this?.lytImageSlider?.tvMessage?.gone()
                    this?.lytImageSlider?.vpImageSlider?.setCurrentItem(
                        (shopBannerModel.size - 1), false
                    )

                    this?.lytImageSlider?.vpImageSlider?.post {
                        nullableBinding?.lytImageSlider?.vpImageSlider?.requestLayout()
                        nullableBinding?.lytImageSlider?.vpImageSlider?.requestTransform()
                    }
                    viewModel.startBannerTimer()
                }
            }


        }
        mainViewModel.contentData.observe(this) {
            binding.lytSummaryWorkout.root.visible()
            binding.lytSummaryWorkout.tvTitle.text = it.title
            binding.lytSummaryWorkout.tvSubTitle.text = it.subtitle
            binding.lytSummaryWorkout.ivBanner.loadImage(
                requireContext(), it.image_url
            )
        }
//        viewModel.showNextIplData.observe(this) {
//            it.getContent()?.let {
//                tryCatch {
//                    val currentIplPage = binding.iplViewPager.currentItem
//                    if (binding.iplTabLayout.visibility == View.VISIBLE) {
//                        if (currentIplPage == 0) {
//                            binding.iplViewPager.setCurrentItem(1, true)
//                        } else {
//                            binding.iplViewPager.setCurrentItem(0, true)
//                        }
//                    }
//
//                }
//            }
//        }


        viewModel.showNextImage.observe(this) {
            it.getContent()?.let {
                val current = binding.lytImageSlider.vpImageSlider.currentItem
                val imageSize = mainViewModel.displayBanners.value?.size ?: 0
                if (imageSize == 0) return@let

                if ((current + 1) == imageSize) {
                    try {
                        binding.lytImageSlider.vpImageSlider.setCurrentItem(0, true)
                    } catch (exp: IllegalStateException) {
                    }
                } else {
                    try {
                        binding.lytImageSlider.vpImageSlider.setCurrentItem(current + 1, true)
                    } catch (exp: IllegalStateException) {
                    }
                }
            }
        }

        viewModel.summary.recentActivities.observe(this) { recentActivities ->
            if (recentActivities != null) {
                setRecentActivities(recentActivities)
            }
        }

        viewModel.sessionManager.connectState.observe(this) { connectedState ->
            LOGS.d("CONNECT_STATE", "$connectedState")
            when (connectedState) {
                is ConnectState.ConnectFailed -> {
                    setStateConnecting(connectedState.noiseFitDevice)
                }

                is ConnectState.ConnectSuccess -> {
                    binding.lytDeviceConnected.root.gone()
                    shouldSync()
                    showBleCallingDialog()
                }

                is ConnectState.Connecting -> {
                    setStateConnecting(connectedState.noiseFitDevice)
                }

                is ConnectState.DisconnectFailed -> {
                    uiController.onDisplayError("DisconnectFailed")
                }

                is ConnectState.UnPaired, is ConnectState.DisconnectSuccess -> {
                    binding.lytDeviceConnected.root.gone()

                }

                else -> {}
            }
        }

        viewModel.sessionManager.showSyncOfflineData.observe(this) {
            it?.getContent()?.let { userActivity ->
                when (userActivity) {
                    HealthOverviewDataType.ACTIVITY -> {
                        viewModel.getRecentWorkouts()
                    }

                    else -> {
                        viewModel.getUserActivities(userActivity)
                    }
                }

            }
        }
        viewModel.sessionManager.syncCompleted.observe(this) {
            it?.getContent()?.let { syncDataStatus ->
                when (syncDataStatus.status) {
                    EventConstants.UPDATE_STATUS_SUCCESS -> {
                        resetSwipeLoadingAnim()

                    }

                    EventConstants.UPDATE_STATUS_FAILED -> {
                        resetSwipeLoadingAnim()
                    }

                    EventConstants.UPDATE_STATUS_STARTED -> {
                    }
                }
            }
        }


    }

    private fun setNplLeagueData(nplData: NplLeague?) {
        if (nplData != null) {
            binding.lytNplCorrectAnswer.root.visible()
            binding.lytNplCorrectAnswer.tvTitle.text = nplData.title ?: ""
            binding.lytNplCorrectAnswer.tvCorrectAnswer.text = nplData.correctQues.toString()
            val correctAns = "/${nplData.totalQues} correct answers"
            binding.lytNplCorrectAnswer.tvCorrectAnswerCount.text = correctAns
            binding.lytNplCorrectAnswer.tvQLeftValue.text = nplData.quesLeft.toString()
            binding.lytNplCorrectAnswer.tvBRValue.text = nplData.reward.toString()

            if (nplData.message.isNullOrEmpty()) {
                binding.lytNplCorrectAnswer.tvMsg.gone()
                binding.lytNplCorrectAnswer.divider1.root.gone()
            } else {
                binding.lytNplCorrectAnswer.tvMsg.visible()
                binding.lytNplCorrectAnswer.divider1.root.visible()
                binding.lytNplCorrectAnswer.tvMsg.text =
                    HtmlCompat.fromHtml(nplData.message ?: "", 0)
            }
            binding.lytNplCorrectAnswer.pbSteps.progress =
                ApplicationUtils.calculateProgressPercentage(nplData)
            binding.lytNplCorrectAnswer.pbSteps.setIndicatorColor1(R.color.accent_color_purple)

            if (nplData.correctQues >= nplData.totalQues) {
                binding.lytNplTargetAchieve.tvTitle.text = nplData.title
                binding.lytNplTargetAchieve.root.visible()
                binding.lytNplCorrectAnswer.root.gone()
            } else {
                binding.lytNplTargetAchieve.root.gone()
                binding.lytNplCorrectAnswer.root.visible()
            }

            if (nplData.sponsorImage.isNullOrEmpty()) {
                binding.lytNplCorrectAnswer.tvPowered.gone()
                binding.lytNplCorrectAnswer.ivPowered.gone()
                binding.lytNplCorrectAnswer.divider2.root.gone()
            } else {
                binding.lytNplCorrectAnswer.tvPowered.visible()
                binding.lytNplCorrectAnswer.ivPowered.visible()
                binding.lytNplCorrectAnswer.divider2.root.visible()
                binding.lytNplCorrectAnswer.ivPowered.loadImageWithCache(
                    binding.lytNplCorrectAnswer.ivPowered.context,
                    nplData.sponsorImage
                )
            }
        } else {
            binding.lytNplCorrectAnswer.root.gone()
            binding.lytNplTargetAchieve.root.gone()
        }
    }


    private fun setStateConnecting(noiseFitDevice: ColorFitDevice?) {
        binding.lytDeviceConnected.apply {
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
            tvLastSync.text = ""
            tvBatteryPercentage.text = noiseFitDevice?.bluetoothName ?: ""
            root.visible()
        }
    }

    fun resetSwipeLoadingAnim() {
        binding.layoutRefresh.textSyncingData.gone()
        binding.swipeToRefresh.refreshComplete()
    }

    private fun setAlerts(alerts: List<DashNotification>) {
        binding.lytDashAlerts.apply {
            if (alerts.size > 1) {
                this.imageView13.visible()
                this.textView35.visible()
                this.ivToggle.visible()

                if (viewModel.isAlertExpanded) {
                    this.ivToggle.setImageResource(R.drawable.ic_alert_collapse)
                    binding.lytDashAlerts.rvAlerts.visible()
                } else {
                    this.ivToggle.setImageResource(R.drawable.ic_alert_expand)
                    binding.lytDashAlerts.rvAlerts.gone()
                }

            } else {
                binding.lytDashAlerts.rvAlerts.visible()
            }


            this.ivToggle.setImageResource(R.drawable.ic_alert_expand)
            this.ivToggle.setOnClickListener {
                toggleAlerts()
            }
            this.textView35.setOnClickListener {
                logInsiderAppEvent(InsiderAppEvents.HOMEPAGE_ALERT_COMPLETEWATCHSETUP_CLICK)
                toggleAlerts()
            }
        }


        binding.lytDashAlerts.rvAlerts.layoutManager = LinearLayoutManager(requireContext())
        binding.lytDashAlerts.rvAlerts.adapter = alertAdapter
        alertAdapter.setDataSet(alerts)
        alertAdapter.setListeners(object : AlertActions {
            override fun onCloseClicked(data: DashNotification, position: Int) {
                viewModel.removeDashboardAlert(position)
                //alertAdapter.removeItem(position)
                when (data.type) {
                    DashNotificationType.NOTIFICATION -> viewModel.localDataStore.setNotificationMessageClearedStatus(
                        true
                    )

                    DashNotificationType.BACKGROUND_ALERT -> viewModel.localDataStore.showEnableBgPermissionDialog(
                        true
                    )

                    DashNotificationType.BATTERY_OPTIMIZATION -> viewModel.localDataStore.setBatteryOptimisationStatus(
                        true
                    )

                    DashNotificationType.DEVICE_SETUP -> {}
                    DashNotificationType.CONNECTIVITY -> {
                        viewModel.localDataStore.setAlertConnectivityStatus(
                            true
                        )
                    }

                    DashNotificationType.BACKGROUND_PERMISSION -> {
                        showBackgroundPermissionDialog(true, false)
                    }

                    DashNotificationType.SUPPORT_QUERIES -> {
                        viewModel.localDataStore.setAlertSupportStatus(
                            true
                        )
                    }
                }

                updateAlertUi()
            }

            override fun onAlertClicked(data: DashNotification, position: Int) {
                when (data.type) {
                    DashNotificationType.NOTIFICATION -> {
                        logInsiderAppEvent(InsiderAppEvents.HOMEPAGE_ALERT_WATCH_NOTIFICATION_CLICK)
                        navigate(R.id.notificationFragment)
                    }

                    DashNotificationType.BACKGROUND_ALERT -> {
                        //navigate(R.id.backgroundPermissionFragment)
                    }

                    DashNotificationType.BATTERY_OPTIMIZATION -> {
                        batteryOptimisationDialog()
                    }

                    DashNotificationType.DEVICE_SETUP -> {
                        if (!viewModel.isDeviceConnected()) {
                            context.showShortToast(getString(R.string.text_connecting_to_device))
                            return
                        }
                        startActivity(DeviceSetupActivity.getStartIntent(requireContext()))
                        activity?.finish()
                    }

                    DashNotificationType.CONNECTIVITY -> {
                        logInsiderAppEvent(InsiderAppEvents.HOMEPAGE_ALERT_PAIRING_CONNECTIVITY_CLICK)
                        navigate(
                            SummaryFragmentDirections.actionSummaryFragmentToHelpAndSupportListFragment(
                                null, HelpAndSupportType.PAIRING_AND_CONNECTIVITY.name
                            )
                        )
                    }

                    DashNotificationType.SUPPORT_QUERIES -> {
                        logInsiderAppEvent(InsiderAppEvents.HOMEPAGE_ALERT_HELP_SUPPORT_CLICK)
                        navigate(SummaryFragmentDirections.actionSummaryFragmentToHelpAndSupportFragment()
                            .apply {
                                this.highlightTopic = HelpAndSupportType.PAIRING_AND_CONNECTIVITY
                            })
                        //navigate(R.id.helpAndSupportFragment)
                    }

                    DashNotificationType.BACKGROUND_PERMISSION -> {
                        showBackgroundPermissionDialog(true, false)
                    }
                }
            }
        })
    }


    private fun showBackgroundPermissionDialog(
        askPermission: Boolean,
        redirectToSettings: Boolean
    ) {
        var title = "Noisefit requires background location permission for"
        var description = getString(R.string.text_background_permission_message)
        var continueButtonText = getString(R.string.text_continue)
        if (!askPermission) {
            title = getString(
                R.string.text_noisefit_needs_location_access
            )
            description = getString(
                R.string.text_to_track_your_real_time_run_noisefit_needs_location
            )
            continueButtonText = getString(R.string.text_yes)
        }
        uiController.onApiErrorReceived(ErrorResponse(
            UIComponentType.AreYouSureDialog(
                title,
                description,
                false,
                continueButtonText,
                object : BinaryActionCallback {
                    override fun yes() {
                        if (!askPermission) {
                            if (redirectToSettings) {
                                ShareUtil.openAppPermissionSettings(context)
                            } else {
                                requestPermissions(
                                    Array(1) { Manifest.permission.ACCESS_BACKGROUND_LOCATION },
                                    409
                                )
                            }


                        } else {
                            askBackgroundPermission()
                        }

                    }

                    override fun no() {

                    }
                }

            )))
    }


    private fun askBackgroundPermission() {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissionResultListener.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
            return
        }


        val permissionAccessCoarseLocationApproved = (ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
        if (permissionAccessCoarseLocationApproved) {
            val backgroundLocationPermissionApproved = (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
            if (backgroundLocationPermissionApproved) {
                // App can access location both in the foreground and in the background.
                // Start your service that doesn't have a foreground service type
                // defined.
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    showBackgroundPermissionDialog(false, false)
                } else {
                    requestPermissions(
                        Array(1) { Manifest.permission.ACCESS_BACKGROUND_LOCATION }, 409
                    )
                }
            }


//            }
        } else {
            permissionResultListener.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {


        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            if (requestCode == 409 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                showBackgroundPermissionDialog(false, true)

            }
        }

    }

    private fun batteryOptimisationDialog() {

        setFragmentResultListener(ALERT_REQUEST_KEY) { _, bundle ->
            val allow = bundle.getBoolean("allow")
            if (allow) {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(intent)
            }
        }

        navigate(
            SummaryFragmentDirections.actionNavigationActivityToAlertTextBottomSheet(
                getString(R.string.text_watch_got_disconnected_automatically),
                getString(R.string.text_battery_optimisation_desc),
                "", ""
            )
        )
    }

    fun toggleAlerts() {
        if (!viewModel.isAlertExpanded) {
            binding.lytDashAlerts.ivToggle.setImageResource(R.drawable.ic_alert_collapse)
            binding.lytDashAlerts.rvAlerts.visible()
            viewModel.isAlertExpanded = true
        } else {
            binding.lytDashAlerts.ivToggle.setImageResource(R.drawable.ic_alert_expand)
            binding.lytDashAlerts.rvAlerts.gone()
            viewModel.isAlertExpanded = false
        }
    }


    fun updateAlertUi() {
        val itemCount = alertAdapter.itemCount
        if (itemCount < 1) {
            binding.lytDashAlerts.root.gone()
        } else if (itemCount == 1) {
            binding.lytDashAlerts.apply {
                this.imageView13.gone()
                this.textView35.gone()
                this.ivToggle.gone()
            }
            alertAdapter.notifyItemChanged(0)
        }
    }


    private fun setRecentActivities(recentActivities: RecentActivities) {
        binding.lytSummaryRecentWorkouts.apply {


        }

        recentActivities.activities?.let { recentWorkoutsAdapter.setDataSet(it) }
    }


    private fun setRing() {
        binding.lytDataSummary.apply {

            //Ring 1
            dynamicArcView.addSeries(
                seriesItemWithoutInset(
                    requireActivity(), 100f, 100f, R.color.steps_arc_bg, 24f
                )
            )


            //Ring 2
            dynamicArcView.addSeries(
                seriesItemWithInset(
                    requireActivity(), 100f, 100f, R.color.distance_arc_bg, 40f, 24f
                )
            )

            //Ring 3
            dynamicArcView.addSeries(
                seriesItemWithInset(
                    requireActivity(), 100f, 100f, R.color.calories_arc_bg, 80f, 24f
                )
            )
        }

    }

    private fun setOverviewData(healthOverviewData: HealthOverviewData) {

        //  setRingBg()
        binding.lytDataSummary.apply {


            tvCalorieCurrent.text = healthOverviewData.calories.toString()
            val caloriesGoal = "/${healthOverviewData.caloriesGoal.toString()}"
            tvCalorieGoal.text = getString(R.string.text_kcal_with_unit, caloriesGoal)


            tvDistanceCurrent.text = healthOverviewData.distance.toString()
            val distanceGoal = "/${healthOverviewData.distanceGoal.toString()}"
            tvDistanceGoal.text = distanceGoal

            tvStepCurrent.text = healthOverviewData.steps.toString()
            val stepsGoal = "/${healthOverviewData.stepsGoal.toString()}"
            tvStepGoal.text = getString(R.string.text_steps_with_unit, stepsGoal)


            val distanceValue = healthOverviewData.distanceGoalProgress ?: 0f

            if (viewModel.summary.distanceProgressCompleted != distanceValue) {
                viewModel.summary.distanceProgressCompleted = distanceValue
                LOGS.d("update_distance_value $distanceValue")


                val distanceIndex: Int = dynamicArcView.addSeries(
                    seriesItemWithInset(
                        requireActivity(), 0f, 100f, R.color.distance_arc, 40f, 24f
                    )
                )

                dynamicArcView.addEvent(
                    DecoEvent.Builder(distanceValue).setIndex(distanceIndex)
                        .setDuration(RING_ANIMATION).build()
                )
            }


            val caloriesValue = healthOverviewData.caloriesGoalProgress ?: 0f

            if (viewModel.summary.caloriesProgressCompleted != caloriesValue) {
                viewModel.summary.caloriesProgressCompleted = caloriesValue

                val caloriesIndex: Int = dynamicArcView.addSeries(
                    seriesItemWithInset(
                        requireActivity(), 0f, 100f, R.color.calories_arc, 80f, 24f
                    )
                )
                dynamicArcView.addEvent(
                    DecoEvent.Builder(caloriesValue).setIndex(caloriesIndex)
                        .setDuration(RING_ANIMATION).build()
                )
            }


            val stepsValue = healthOverviewData.stepsGoalProgress ?: 0f
            if (viewModel.summary.stepsProgressCompleted != stepsValue) {
                viewModel.summary.stepsProgressCompleted = stepsValue


                val stepIndex: Int = dynamicArcView.addSeries(
                    seriesItemWithoutInset(
                        requireActivity(), 0f, 100f, R.color.steps_arc, 24f
                    )
                )
                dynamicArcView.addEvent(
                    DecoEvent.Builder(stepsValue).setIndex(stepIndex).setDuration(RING_ANIMATION)
                        .build()
                )
            }


            healthOverviewData.healthOverviewList?.let {
                healthOverviewAdapter.items = it
                healthOverviewAdapter.refreshPosition = viewModel.summary.refreshPosition
                healthOverviewAdapter.lastPosition = it.size - 1
                healthOverviewAdapter.devicePaired = viewModel.summary.connectedDevice != null

            }
        }
    }


    private fun syncData() {
        scope.launch {
            viewModel.sessionManager.forceSyncDataWithServer = true
//            resetProgressValue()
            val status = ApplicationUtils.startSyncScheduler(requireContext())
            withContext(Dispatchers.Main) {
                if (status) {
                    //   uiController.onDisplayError("Syncing")
                } else {
                    //uiController.onDisplayError("Job is already running please wait")
                }
            }

        }
    }


    private fun initUi() {
        val connectedDevice = viewModel.getDeviceConnected()
        val isDeviceConnected = connectedDevice != null
        if (isDeviceConnected) {
            // binding.lytSummaryHealth.btnEditHealth.visible()
            binding.lytPairYourDeviceHeader.root.gone()
        } else {
            binding.lytPairYourDeviceHeader.root.visible()
            // binding.lytSummaryHealth.btnEditHealth.gone()
        }

        val userInfo = viewModel.getUserHeaderTitle(requireContext(), isDeviceConnected)

        binding.lytDashHeader.tvUserName.text = userInfo.first
        binding.lytDashHeader.tvDateTime.text = DateFormats.getTodaysDateString(14)
        Glide.with(binding.lytDashHeader.ivProfileImage.context).load(userInfo.third)
            .placeholder(R.drawable.ic_default_profile_image)
            .error(R.drawable.ic_default_profile_image).into(binding.lytDashHeader.ivProfileImage)



        binding.lytPairYourDeviceHeader.tvMsg.text =
            SpannableStringBuilder().append(getString(R.string.text_don_t_have_a_noisefit_device_yet_check_out_our_latest_collection_by_clicking_on_the))
                .append(" ").bold { append("'") }
                .bold { append(getString(R.string.text_get_noise)) }.bold { append("'") }
                .append(" ").append(getString(R.string.text_in_the_navbar))

    }


    private fun setAdapter() {

        binding.lytSummaryHealth.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = healthOverviewAdapter
        }

        binding.lytSummaryRecentWorkouts.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentWorkoutsAdapter
        }

        recentWorkoutsAdapter.setOnRecentWorkoutsInteractionListener(object :
            RecentWorkoutsAdapter.RecentWorkoutsInteractionListener {
            override fun onWorkoutSelected(sportsModeResponse: SportsModeResponse) {
                if (sportsModeResponse.activityType?.lowercase() == "outdoor_cycling__") {
                    navigate(
                        SummaryFragmentDirections.actionNavigationActivityToActivityCyclingFragment(
                            sportsModeResponse
                        )
                    )
                } else {
                    navigate(
                        SummaryFragmentDirections.actionNavigationActivityToActivityDetailsFragment(
                            sportsModeResponse
                        )
                    )
                }

            }

        })


    }


    private fun logBannerInsiderAppEvent(type: Int, url: String, position: Int) {
        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.HOMESCREEN_BANNER_POS + position)
        viewModel.sessionManager.logInsiderAppEvent(
            InsiderAppEvents.HOMESCREEN_BANNER_CLICK,
            HashMap<String, Any>().apply {
                this["position"] = type
                this["url"] = url
            })
    }

    /**
     * 1-> Open watchface
     * 2-> Open activity
     * 3-> Open Challenges
     * 4-> Open shop
     * 5-> Open Step Detail
     * 6-> Open Heart Rate Detail
     * 7-> Open Sleep Detail
     * 8-> Open Blood Oxygen Detail
     * 9-> Open Stress Detail
     * 10-> Round up
     * 11-> Help & Support
     * else -> if it has data_url then open web view
     */
    fun openBannerUrl(it: DashboardBanner, position: Int) {
        logBannerInsiderAppEvent(it.type, it.data_url ?: "", position)
        activity?.let { act ->
            when (it.type) {
                1 -> {
                    openWatchFace()
                }

                2 -> {
                    navigate(R.id.navigation_activity)
                }

                3 -> {
                    mainViewModel.navigateTo(BottomNavOption.EXPLORE)
                }

                4 -> {
                    mainViewModel.navigateTo(BottomNavOption.SHOP)
                }

                5 -> {
                    navigate(
                        SummaryFragmentDirections.actionSummaryFragmentToStepsDetailsFragment(
                            HealthOverViewHistoryType.Steps.name
                        )
                    )
                }

                6 -> {
                    navigate(R.id.heartRateDetailsFragment)
                }

                7 -> {
                    navigate(R.id.sleepDetailsFragment)
                }

                8 -> {
                    navigate(R.id.bloodOxygenDetailsFragment)
                }

                9 -> {
                    navigate(R.id.stressDetailsFragment)
                }

                10 -> {
                    navigate(R.id.roundUpLandingFragment)
                }

                11 -> {
                    navigate(SummaryFragmentDirections.actionSummaryFragmentToHelpAndSupportFragment())
                }

                13 -> {
                    showNplDashboard()
                }

                14 -> {
                    navigate(R.id.coinFragment)
                }

                else -> {
                    if (!it.data_url.isNullOrEmpty()) {
                        startActivity(
                            WebViewActivity.getStartIntent(
                                act,
                                WebViewActivity.DEFAULT_TITLE,
                                it.data_url + "?utm_source=Abanner" + "&utm_medium=APP_Homepage_Banner_$position" + "&utm_campaign="
                            )
                        )
                    }
                }
            }
        }

    }

    private fun showNplDashboard() {

        val nplPrivacyAccepted = viewModel.localDataStore.getNplPrivacyPolicyStatus()
        if (nplPrivacyAccepted) {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_HOMEPAGE_NPL_CLICK)
            mainViewModel.playNplAnim = true
            navigate(R.id.nplDashboardFragment)
        } else showPrivacyBottomSheet()
    }

    private fun showPrivacyBottomSheet() {
        requireActivity().supportFragmentManager.setFragmentResultListener(
            NPL_TERMS_KEY, this
        ) { _, bundle ->
            val agree = bundle.getBoolean("agree")
            if (agree) {
                viewModel.localDataStore.setNplPrivacyPolicyStatus(true)
                mainViewModel.playNplAnim = true
                navigate(R.id.nplDashboardFragment)
            }
        }
        navigate(SummaryFragmentDirections.actionSummaryFragmentToNplPrivacyBottomDialogFragment())
    }

    fun openWatchFace() {
        if (!viewModel.isDeviceConnected()) {
            context.showShortToast(getString(R.string.text_no_device_connected))
            return
        }

        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.HOMEPAGE_WATCHFACE_BANNER_CLICK)
        if (viewModel.watchesSDK.hasCategoryWatchFace()) {
            findNavController().popBackStack(R.id.watchFaceCategoryListingFragment, true)
            navigate(R.id.watchface2CategoryFragment)
        } else {
            findNavController().popBackStack(R.id.watchFaceListingFragment, true)
            navigate(R.id.watchFaceListingFragment)
        }
    }


}