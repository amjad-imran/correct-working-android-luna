package com.oreo.ui.home.summary.paginate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSummaryDataTodayBinding
import com.noisefit.oreo.BottomNavOption
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.common.bottomSheet.DELETE_REQ_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.NAP_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.RING_DISABLED_KEY
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.enums.DashInfoCard
import com.noisefit_commans.data.model.NotificationGoals
import com.noisefit_commans.data.model.OreoNapData
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.ManualMeasureType
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.loadImageWithCache
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.AppConversionUtils
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageAppEventParams
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.AlertType
import com.oreo.data.model.FemaleHealthCardState
import com.oreo.data.model.ImpactData
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.TapMeasureState
import com.oreo.data.model.TrendsData
import com.oreo.data.model.VideoInfoType
import com.oreo.data.model.health.ODashboardActivityScoreModel
import com.oreo.data.model.health.ODashboardReadinessScoreModel
import com.oreo.data.model.health.ODashboardSleepScoreModel
import com.oreo.data.model.sleep.HealthTrend
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.custom.CirclePagerIndicatorDecoration
import com.oreo.ui.custom.SnapHelperOneByOne
import com.oreo.ui.device.FIND_RING_LOCATION_PERM_REQUEST
import com.oreo.ui.home.summary.AlertClickListener
import com.oreo.ui.home.summary.HomeRecyclerViewHolder
import com.oreo.ui.home.summary.OSummaryHealthOverviewAdapter
import com.oreo.ui.home.summary.OSummaryHealthOverviewClickEnum
import com.oreo.ui.home.summary.OreoRWorkoutAdapter
import com.oreo.ui.home.summary.update.UpdateLaunchMode
import com.oreo.ui.sleep.nap.BOTTOM_NAP_RESULT
import com.oreo.ui.sleep.scoredetails.ClickViewType
import com.oreo.ui.sleep.scoredetails.SharedOSCDViewModel
import com.oreo.ui.sleep.scoredetails.ViewItemClickType
import com.oreo.util.DateTimeUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs


@AndroidEntryPoint
class SummaryDataFragmentToday :
    BaseFragment<FragmentSummaryDataTodayBinding>(FragmentSummaryDataTodayBinding::inflate) {

    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val viewModel: SummaryDataViewModelToday by viewModels()
    private val mSharedViewModel: SharedOSCDViewModel by activityViewModels()

    private val TAG = "SummaryDataFragment"
    val circleObj = CirclePagerIndicatorDecoration()


    companion object {

        private val ARGS_DATE = "ARGS_DATE"

        @JvmStatic
        fun newInstance(date: String): Fragment {
            return SummaryDataFragmentToday().apply {
                arguments = Bundle().apply {
                    putString(ARGS_DATE, date)
                }
            }
        }
    }


    private val healthOverviewAdapter by lazy {
        OSummaryHealthOverviewAdapter()
    }
    private val viewedCardsAdapter by lazy {
        OSummaryHealthOverviewAdapter()
    }


    private val napsAdapter: NapsConfirmAdapter by lazy {
        NapsConfirmAdapter(object : NapConfirmAction {
            override fun onNapConfirmClicked(nap: OreoNapData) {
                viewModel.confirmNap(nap)
            }

            override fun onNapRemoveClicked(nap: OreoNapData) {
                showRemoveNapBottomSheet(nap)
            }

            /*override fun onMoveDetails(nap: ONapDataModel) {
                navigate(R.id.napDetails, bundleOf("napId" to nap.id))
            }*/
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setNapsPager()

        setAdapter()

        val date = arguments?.getString(ARGS_DATE)
        viewModel.date = date

        viewModel.getPeriodData()


    }

    private fun setNapsPager() {
        SnapHelperOneByOne().attachToRecyclerView(binding.contentMain.lytConfirmNap.vpNaps)
        with(binding.contentMain.lytConfirmNap.vpNaps) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = napsAdapter
            //addItemDecoration(CirclePagerIndicatorDecoration())
            clipToPadding = false
            val padding =
                viewModel.screenUtils.dpToPx(12, binding.contentMain.lytConfirmNap.vpNaps.context)
                    .toInt()
            setPadding(padding, 0, padding, 0)

        }

        binding.contentMain.lytConfirmNap.vpNaps.addOnItemTouchListener(object :
            RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(view: RecyclerView, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> binding.contentMain.lytConfirmNap.vpNaps.parent
                        .requestDisallowInterceptTouchEvent(true)
                }
                return false
            }

            override fun onTouchEvent(view: RecyclerView, event: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()

        loadData()

        viewModel.checkForNewAppVersion()
        viewModel.checkForNewOtaVersion()

        handleFindMyRingCard()
        viewModel.handleGoogleFitCard()

        viewModel.getSleepPlanerDetails()

        viewModel.getNotificationToggle()
    }


    fun loadData() {
        viewModel.date?.let {
            mainViewModel.getDashBoardData(it)?.let { dash ->
                viewModel.registerDate = mainViewModel.registerDate
                viewModel.serverUserHealthData = dash.first
                viewModel.stressBeta = mainViewModel.stressBeta
                viewModel.enableAi = mainViewModel.enableAi
                viewModel.shouldShowStressCard = mainViewModel.shouldShowStressCard(it)
                setUi(dash.first, dash.second, dash.third)
            }
        }
    }

    private fun setUi(
        data: ServerUserHealthData,
        trendsData: TrendsData?,
        impactData: ImpactData?
    ) {
        viewModel.initTodayData()

        //
        if(viewModel.lunaManagedSwitchState){
            viewModel.getUserManagedHealthData(data, trendsData, impactData)
        }else {
            viewModel.parseHealthData(data, trendsData, impactData)
        }
        //
    }


    private fun setAdapter() {
        binding.contentMain.rvHealthData.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = healthOverviewAdapter
        }

        binding.contentMain.rvViewedCards.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = viewedCardsAdapter
        }

        viewedCardsAdapter.itemClickListener = { type ->
            when (type) {
                is OSummaryHealthOverviewClickEnum.TextRingCareClicked -> {
                    navigate(R.id.ringCareFragment, Bundle().apply {
                        this.putString("title", type.title)
                    })
                }

                is OSummaryHealthOverviewClickEnum.VideoInfoClicked -> {
                    navigate(R.id.ringInfoPlayerFragment, Bundle().apply {
                        this.putString("videoUrl", type.videoUrl)
                    })
                }

                else -> {}
            }

        }

        healthOverviewAdapter.itemClickListener = { type ->
            when (type) {

                is OSummaryHealthOverviewClickEnum.WorkoutAlertWhatisThis -> {

                    navigate(R.id.aboutAutoWorkoutBottomSheet)
                }

                is OSummaryHealthOverviewClickEnum.WorkoutAlertIdentify -> {
                    navigate(R.id.detectWorkoutListFragment)
                }

                is OSummaryHealthOverviewClickEnum.AutoSportsDelete -> {
                    requireActivity().supportFragmentManager.setFragmentResultListener(
                        DELETE_REQ_REQUEST_KEY,
                        viewLifecycleOwner
                    ) { _, bundle ->
                        val allow = bundle.getBoolean("allow")
                        if (allow) {
                            viewModel.markWorkoutSyncedAll()
                            viewModel.removeAutoWorkoutCard()
                            viewModel.handleGoogleFitCard()
                        }
                    }
                    navigate(R.id.deleteAllWorkoutBottomSheet, Bundle().apply {
                        this.putString("title", getString(R.string.text_dismiss_activity_title))
                        this.putString(
                            "description", getString(R.string.text_dismiss_activity_desc)
                        )
                        this.putString("acceptText", "")
                        this.putString("declineText", "")
                    })
                }


                OSummaryHealthOverviewClickEnum.ActivityDetailsWorkoutClick -> {
                    mainViewModel.navigateTo(BottomNavOption.ACTIVITY)
                    mainViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_homepage_activity_click)
                }

                OSummaryHealthOverviewClickEnum.ReadinessDetailsWorkoutClick -> {
                    mainViewModel.navigateTo(BottomNavOption.READINESS)
                    mainViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_homepage_readiness_click)
                }

                OSummaryHealthOverviewClickEnum.SleepDetailsWorkoutClick -> {
                    mainViewModel.navigateTo(BottomNavOption.SLEEP)
                    mainViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_homepage_sleep_click)
                }

                is OSummaryHealthOverviewClickEnum.VideoInfoClicked -> {
                    navigate(R.id.ringInfoPlayerFragment, Bundle().apply {
                        this.putString("videoUrl", type.videoUrl)
                    })
                    viewModel.localDataStore.setDashCardClickState(
                        when (type.type) {
                            VideoInfoType.SLEEP -> DashInfoCard.SLEEP
                            VideoInfoType.READINESS -> DashInfoCard.READINESS
                            VideoInfoType.ACTIVITY -> DashInfoCard.ACTIVITY
                        }, true
                    )
                }

                is OSummaryHealthOverviewClickEnum.TextRingCareClicked -> {
                    viewModel.localDataStore.setDashCardClickState(
                        DashInfoCard.CARE, true
                    )
                    navigate(R.id.ringCareFragment, Bundle().apply {
                        this.putString("title", type.title)
                    })

                }

                OSummaryHealthOverviewClickEnum.TextWelcomeRingClicked -> {
                    viewModel.localDataStore.setDashCardClickState(DashInfoCard.WELCOME, true)
                    navigate(R.id.ringWelcomeFragment)
                }

                OSummaryHealthOverviewClickEnum.StressGraphClicked -> {
                    if (viewModel.getStressWalkthroughShownStatus()) {
                        navigate(R.id.fragmentOStressDetails)
                    } else {
                        navigate(R.id.stressSplashFragment)
                    }
                    mainViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_homepage_stress_click)
                }

                is OSummaryHealthOverviewClickEnum.OnNapClicked -> {

                    navigate(R.id.napDetails, bundleOf("napId" to type.napId))
                }

                is OSummaryHealthOverviewClickEnum.OnAiCardClicked -> {

                    if (viewModel.isChatSplashShown()) {
                        navigate(
                            R.id.aiTopQuestionsFragment,
                            bundleOf("aiTopic" to AITopics.GENERAL)
                        )
                        //mainViewModel.getChatHistoryToday()
                    } else {
                        navigate(R.id.aiChatOnboardFragment)
                        //navigate(R.id.chatSplashFragment)
                    }
                }

                is OSummaryHealthOverviewClickEnum.TrackYourFemaleHealth -> {
                    navigate(R.id.femaleHealthSplashFragment)
                }

                is OSummaryHealthOverviewClickEnum.TrackYourFemaleHealthRemindLater -> {
                    viewModel.localDataStore.setFMHRemindLater()
                    healthOverviewAdapter.removeCycleGetStartedCard()
                }

                OSummaryHealthOverviewClickEnum.FemaleHealthHome -> {
                    navigate(R.id.fragmentCycleTracker)
                }

                is OSummaryHealthOverviewClickEnum.GotPeriodClicked -> {
                    //viewModel.onGotPeriodClicked(type.status)
                }

                is OSummaryHealthOverviewClickEnum.OnHealthMonitorCardClicked -> {
                    navigate(R.id.healthMonitorInternal, Bundle().apply {
                        this.putParcelable("healthTrend", type.data)
                        this.putString("selectedDate", LocalDate.now().toString())
                        this.putString("source", "homepage")
                    })
                }

                OSummaryHealthOverviewClickEnum.OnSleepPlannerAlarmClicked -> {
                    uiController.logAppEvent(
                        MoEngageLunaAppEvents.sleep_planner_setup_alarm,
                        hashMapOf("source" to "sleep")
                    )
                    navigate(
                        R.id.setAlarmFragment,
                        bundle = bundleOf("bed_time" to null, "wake_time" to null)
                    )
                }

                OSummaryHealthOverviewClickEnum.OnSleepPlannerBreathingClicked -> {

                    navigate(
                        R.id.fragmentBreathExercise
                    )
                }

                OSummaryHealthOverviewClickEnum.OnSleepPlannerCardClicked -> {
                    uiController.logAppEvent(
                        MoEngageLunaAppEvents.sleep_planner
                    )
                    navigate(R.id.sleepPlannerFragment)
                }
            }
        }

    }


    override fun initListener() {

        binding.contentMain.lytNotificationCard.ivNotificationSteps.setOnClickListener {

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.notification_toggled,
                HashMap<String, Any>().apply {
                    this["config"] = "turned_on/turned_off"
                    this["goal"] = "steps"
                }
            )

            if (viewModel.notificationToggleModel != null) {
                viewModel.notificationToggleModel!!.steps_notification =
                    viewModel.notificationToggleModel?.steps_notification!!.not()
                viewModel.updateNotificationToggle(NotificationGoal.STEPS)
            }
        }

        binding.contentMain.lytNotificationCard.ivNotificationHydrate.setOnClickListener {

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.notification_toggled,
                HashMap<String, Any>().apply {
                    this["config"] = "turned_on/turned_off"
                    this["goal"] = "hydrate"
                }
            )

            if (viewModel.notificationToggleModel != null) {
                viewModel.notificationToggleModel!!.hydrate_notification =
                    viewModel.notificationToggleModel?.hydrate_notification!!.not()
                viewModel.updateNotificationToggle(NotificationGoal.HYDRATE)
            }
        }

        binding.contentMain.lytNotificationCard.ivHydrateMinus.setOnClickListener {

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.notification_toggled,
                HashMap<String, Any>().apply {
                    this["goal"] = "hydrate"
                    this["action"] = "subtracted"
                }
            )

            viewModel.decreaseHydration()
        }

        binding.contentMain.lytNotificationCard.ivHydratePlus.setOnClickListener {

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.notification_toggled,
                HashMap<String, Any>().apply {
                    this["goal"] = "hydrate"
                    this["action"] = "added"
                }
            )

            viewModel.increaseHydration()
        }

        binding.contentMain.lytNotificationCard.tvEdit.setOnClickListener {

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.goalsSetting_clicked
            )

            navigate(R.id.editNotificationGoalFragment)
        }

        binding.contentMain.lytFindMyRingAlert.ivCross.setOnClickListener {
            viewModel.hideFindMyRingPermCard()
        }
        binding.contentMain.lytFindMyRingAlert.tvTurnOn.setOnClickListener {
            showPermDetailsDialog()
        }

        binding.contentMain.lytHeartRate.root.setOnClickListener {
            navigate(R.id.fragmentHeartRateDetails)
        }

        binding.contentMain.lytStressGraph.root.setOnClickListener {
            if (viewModel.getStressWalkthroughShownStatus()) {
                navigate(R.id.fragmentOStressDetails)
            } else {
                navigate(R.id.stressSplashFragment)
            }
            mainViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_homepage_stress_click)
        }

        binding.contentMain.lytAppUpdate.root.setOnClickListener {
            navigate(
                R.id.appUpdateDetailFragment,
                bundleOf("launchMode" to UpdateLaunchMode.APP)
            )
        }

        binding.contentMain.lytOtaUpdate.root.setOnClickListener {
            val isConnected = viewModel.isDeviceConnected()
            if (isConnected.not()) {
                context.showShortToast("Ring not connected")
                return@setOnClickListener
            }
            navigate(R.id.appUpdateDetailFragment, bundleOf("launchMode" to UpdateLaunchMode.OTA))
        }

        binding.contentMain.lytGoogleFit.tvGoogleFitTurnOn.setOnClickListener {
            navigate(R.id.googleFitFragmentOreo)
        }

        binding.contentMain.lytGoogleFit.ivCross.setOnClickListener {
            viewModel.localDataStore.setGoogleFitCrossed(true)
            viewModel.stateGoogleFitCard.postValue(false)
        }

        binding.contentMain.lytGoogleFitDataAvailable.tvGoogleFitTurnOn.setOnClickListener {
            navigate(R.id.googleFitDataFragment)
        }

        binding.contentMain.lytGoogleFitDataAvailable.ivCross.setOnClickListener {
            viewModel.localDataStore.setGoogleFitManageCrossed()
            viewModel.stateGoogleFitCardDataSyncAvailable.postValue(false)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false

            val pairStatus: String

            if (!viewModel.isDeviceConnected()) {
                pairStatus = "unpaired"
                return@setOnRefreshListener
            }

            if (viewModel.stateHeartRateCard.value?.measureState == TapMeasureState.MEASURING) {
                return@setOnRefreshListener
            }
            pairStatus = "paired"

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.luna_activity_sync_manual,
                HashMap<String, Any>().apply {
                    this[MoEngageAppEventParams.operating_system] = "Android"
                    this[MoEngageAppEventParams.device_pairing_status] = pairStatus
                })


            syncData()

        }

        binding.contentMain.lytConnectHelp.btnCancel.setOnClickListener {
            mainViewModel.onRingConnected()
        }

        binding.contentMain.lytConnectHelp.tvDesc.setOnClickListener {
            navigate(R.id.troubleShootBottomSheetFragment, bundleOf("showLastLocation" to true))
        }

        binding.contentMain.lytChargeRing.root.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_homepage_low_battery_click)
            navigate(R.id.ringBatteryChargeFragment)
            //viewModel.setRingBatteryInfoState()
        }

        binding.contentMain.lytPairDevice.btnPairDevice.setOnClickListener {
            startActivity(PairDeviceActivity.getStartIntent(requireContext(), true))
        }
        /*binding.contentMain.lytHeartRate.bInfo.setOnClickListener {
            viewModel.getContributorInfo("hr")
        }*/

        /*binding.contentMain.lytReadinessAvg.root.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_homepage_readiness_score_click)
            viewModel.getContributorInfo("readiness")
        }

        binding.contentMain.lytSleepAvg.constraintLayout2.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_homepage_sleep_score_click)
            viewModel.getContributorInfo("sleep")

        }

        binding.contentMain.lytSleepAvg.constraintLayout.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_homepage_activity_score_click)
            viewModel.getContributorInfo("activity")
        }*/

    }

    private fun syncData() {
        viewModel.sessionManager.forceSyncDataWithServer = true
        scope.launch {
            val status = ApplicationUtils.startOreoSyncScheduler(requireContext())
            withContext(Dispatchers.Main) {
                if (status) {
                    //   uiController.onDisplayError("Syncing")
                } else {
                    //uiController.onDisplayError("Job is already running please wait")
                }
            }

        }
    }


    private fun notificationTextFade(textView1: TextView, textView2: TextView) {
        textView1.visibility = View.VISIBLE
        textView2.visibility = View.INVISIBLE

        val fadeIn: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_goal)
        val fadeOut: Animation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out_goal)

        textView1.startAnimation(fadeOut)
        textView1.visibility = View.INVISIBLE
        textView2.visibility = View.VISIBLE
        textView2.startAnimation(fadeIn)

        Handler(Looper.getMainLooper()).postDelayed({
            textView2.startAnimation(fadeOut)
            textView2.visibility = View.INVISIBLE
            textView1.visibility = View.VISIBLE
            textView1.startAnimation(fadeIn)
        }, 1500)

    }


    override fun subscribeObservers() {


        viewModel.notificationUpdatedState.observe(this) {
            it.getContent()?.let {
                when (it.first) {
                    NotificationGoal.HYDRATE -> {
                        if (it.second) {
                            binding.contentMain.lytNotificationCard.textHydrateReminderMessage.text =
                                getString(
                                    R.string.text_reminder_active
                                )
                        } else {
                            binding.contentMain.lytNotificationCard.textHydrateReminderMessage.text =
                                getString(
                                    R.string.text_reminder_silent
                                )
                        }
                        notificationTextFade(
                            binding.contentMain.lytNotificationCard.textView153,
                            binding.contentMain.lytNotificationCard.textHydrateReminderMessage
                        )
                    }

                    NotificationGoal.STEPS -> {
                        if (it.second) {
                            binding.contentMain.lytNotificationCard.textStepsReminderMessage.text =
                                getString(
                                    R.string.text_reminder_active
                                )
                        } else {
                            binding.contentMain.lytNotificationCard.textStepsReminderMessage.text =
                                getString(
                                    R.string.text_reminder_silent
                                )
                        }
                        notificationTextFade(
                            binding.contentMain.lytNotificationCard.textView89,
                            binding.contentMain.lytNotificationCard.textStepsReminderMessage
                        )
                    }
                }
            }
        }

        viewModel.notificationGoalsCardData.observe(this) {
            if (it == null) {
                binding.contentMain.lytNotificationCard.root.gone()
            } else {
                binding.contentMain.lytNotificationCard.root.visible()
                setNotificationGoalsCardData(it)
            }
        }

        viewModel.sessionManager.googleFitSyncCompleted.observe(this) {
            it.getContent()?.let {
                viewModel.handleGoogleFitCard()
            }
        }

        viewModel.findMyRingCard.observe(this) {
            if (it == true) {
                binding.contentMain.lytFindMyRingAlert.root.visible()
            } else {
                binding.contentMain.lytFindMyRingAlert.root.gone()
            }
        }

        viewModel.showBlackListDialog.observe(this) {
            it.getContent()?.let {
                showBlackListDialog()
            }
        }

        viewModel.sleepAlert.observe(this) {
            if (it == null) {
                binding.contentMain.lytSleepAlert.root.gone()
            } else {
                binding.contentMain.lytSleepAlert.root.visible()
                setSleepAlertUi(it)
            }
        }

        viewModel.healthMonitorCardData.observe(this) { data ->
            if (data == null) {
                binding.contentMain.lytHealthMonitor.root.gone()
            } else {
                binding.contentMain.lytHealthMonitor.root.visible()
                setHealthMonitorCardData(data)
            }


        }

        viewModel.gotYourPeriodData.observe(this) { data ->
            if (data == null) {
                binding.contentMain.lytFemaleHealthGotPeriod.root.gone()
            } else {
                binding.contentMain.lytFemaleHealthGotPeriod.apply {
                    root.visible()
                    this.tvPredictedDay.text = data.title ?: ""

                    this.bYes.setOnClickListener {
                        viewModel.onGotPeriodClicked(true, data.currentDay)
                    }
                    this.bNo.setOnClickListener {
                        viewModel.onGotPeriodClicked(false, data.currentDay)
                    }
                }
            }

        }

        viewModel.trackFemaleHealthCardData.observe(this) {
            if (it == null) {
                binding.contentMain.lytFemaleHealthGetStarted.root.gone()
            } else {
                setFemaleGetStartedUI(it)
                viewModel.sessionManager.canLogPeriod = false
            }
        }

        viewModel.cycleTrackerCardBigData.observe(this) {
            if (it == null) {
                binding.contentMain.lytFemaleHealthCardBig.root.gone()
            } else {
                setBigCardUi(it)
                viewModel.sessionManager.canLogPeriod = true
            }
        }
        viewModel.cycleTrackerCardSmallData.observe(this) {
            if (it == null) {
                binding.contentMain.lytFemaleHealthCardSmall.root.gone()
            } else {
                setSmallCardUi(it)
                viewModel.sessionManager.canLogPeriod = true
            }
        }

        /*viewModel.removePeriodQuestionWidget.observe(this) {
            it.getContent()?.let {
                healthOverviewAdapter.removeGotPeriodCard()
            }
        }*/
        viewModel.femaleHealthDataLoaded.observe(this) {
            it.getContent()?.let {
                loadData()
            }
        }
        viewModel.sleepPlannerDataLoaded.observe(this) {
            it.getContent()?.let {
                loadData()
            }
        }

        viewModel.sessionManager.isRingCharging.observe(this) {
            viewModel.handleBatteryAlert()
        }

        viewModel.sessionManager.checkForVersionUpdate.observe(viewLifecycleOwner) {
            it.getContent()?.let { pair ->
                viewModel.checkOtaVersionServer(pair)
            }
        }

        viewModel.appUpdateInfo.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.contentMain.lytAppUpdate.root.gone()
            } else {
                binding.contentMain.lytAppUpdate.apply {
                    this.tvTitle.text = it.description?.header
                    this.tvMessage.text = it.description?.shortDescription
                    this.imvBack.loadImageWithCache(this.imvBack.context, it.imageUrl)
                    root.visible()
                }
            }

        }
        viewModel.otaUpdateInfo.observe(viewLifecycleOwner) {

            if (it == null) {
                binding.contentMain.lytOtaUpdate.root.gone()
            } else {
                binding.contentMain.lytOtaUpdate.apply {
                    this.tvTitle.text = it.description?.header
                    this.tvMessage.text = it.description?.shortDescription
                    this.imvBack.loadImageWithCache(this.imvBack.context, it.imageUrl)
                    root.visible()
                }

            }
        }

        requireActivity().supportFragmentManager.setFragmentResultListener(
            BOTTOM_NAP_RESULT,
            this
        ) { key, bundle ->
            val napId = bundle.getString("napId")
            if (!napId.isNullOrEmpty()) {
                navigate(R.id.napDetails, bundleOf("napId" to napId))
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.onNapAddSuccess.observe(viewLifecycleOwner) {
            it.getContent()?.let { nap ->
                mainViewModel.reloadTodaysData()

                if ((nap.sleepScore ?: 0) != 0 /*&& (nap.readinessScore ?: 0) != 0*/) {
                    navigate(
                        R.id.bottomSheetNapScore, bundleOf(
                            "napScoreData" to viewModel.getNapSlideUpObj(nap)
                        )
                    )
                    return@observe
                }

                /* val hour = getHoursBasedOnDateTime(nap.startTime)
                 if (hour.toInt() >= 19) {*/
                navigate(
                    R.id.bottomSheetNoDataNapScore,
                    bundleOf("napScoreData" to viewModel.getNapSlideUpObj(nap))
                )
                //}
            }
        }

        viewModel.napsList.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.contentMain.lytConfirmNap.root.gone()
            } else {
                binding.contentMain.lytConfirmNap.root.visible()
            }
            napsAdapter.setDataSet(it, viewModel.date)
            binding.contentMain.lytConfirmNap.vpNaps.removeItemDecoration(circleObj)
            if (it.size > 1) {
                binding.contentMain.lytConfirmNap.vpNaps.addItemDecoration(circleObj)
            }
        }

        mainViewModel.dashTodayReload.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                loadData()
            }
        }

        viewModel.sessionManager.manualMeasurementValue.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                if (it) {
                    viewModel.updateManualValue(ManualMeasureType.HEART_RATE)
                }
            }
        }
        viewModel.sessionManager.manualMeasurementValueStress.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                if (it) {
                    viewModel.updateManualValue(ManualMeasureType.STRESS)
                }
            }
        }

        viewModel.sessionManager.bluetoothStateDash.observe(viewLifecycleOwner) {
            viewModel.updateAlerts()
            //viewModel.updateBluetoothStateInList(it)
        }

        viewModel.stateWorkouts.observe(viewLifecycleOwner) {
            setWorkoutUI(it)
        }

        viewModel.hrInfo.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                navigate(R.id.bottomSheetDataMetrics, Bundle().apply {
                    this.putString("infoData", it)
                })
            }
        }

        viewModel.activityScoreInfo.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                mSharedViewModel.selectedTab = 0
                mSharedViewModel.itemType = ClickViewType.ACTIVITY.name
                mSharedViewModel.itemClickType = ViewItemClickType.ACTIVITY_SCORE
                navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                    putString("viewType", "activity")
                    putString("infoData", it)
                    putString("date", DateFormats.getCurrentDateOreoFormat())
                })
            }
        }

        viewModel.readinessScoreInfo.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                mSharedViewModel.selectedTab = 0
                mSharedViewModel.itemType = ClickViewType.READINESS.name
                mSharedViewModel.itemClickType = ViewItemClickType.READINESS_SCORE
                navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                    putString("viewType", "readiness")
                    putString("infoData", it)
                    putString("date", DateFormats.getCurrentDateOreoFormat())
                })
            }
        }
        viewModel.sleepScoreInfo.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                mSharedViewModel.selectedTab = 0
                mSharedViewModel.itemType = ClickViewType.SLEEP.name
                mSharedViewModel.itemClickType = ViewItemClickType.SLEEP_SCORE
                navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                    putString("viewType", "sleep")
                    putString("infoData", it)
                    putString("date", DateFormats.getCurrentDateOreoFormat())
                })
            }
        }

        /*viewModel.stateHeaderCard.observe(viewLifecycleOwner) {
            binding.contentMain.lytHeader.apply {
                this.tvDate.text =
                    it.second
                this.tvGreeting.text = it.first
                this.root.visible()
            }
        }*/

        viewModel.healthOverviewData.observe(viewLifecycleOwner) {
            healthOverviewAdapter.items = it
            healthOverviewAdapter.refreshPosition = null
        }

        viewModel.viewedCardsData.observe(viewLifecycleOwner) {
            it?.let {
                viewedCardsAdapter.refreshPosition = null
                viewedCardsAdapter.items = it
            }
        }

        viewModel.stateGoogleFitCard.observe(viewLifecycleOwner) {
            if (it) {
                binding.contentMain.lytGoogleFit.root.visible()
            } else {
                binding.contentMain.lytGoogleFit.root.gone()
            }
        }

        viewModel.stateGoogleFitCardDataSyncAvailable.observe(viewLifecycleOwner) {
            if (it) {
                binding.contentMain.lytGoogleFitDataAvailable.apply {
                    textView91.text = getString(R.string.text_data_available_for_sync)
                    textView92.text = getString(R.string.text_data_sync_available)
                    tvGoogleFitTurnOn.text = getString(R.string.text_manage_data)
                    this.root.visible()
                }
            } else {
                binding.contentMain.lytGoogleFitDataAvailable.root.gone()
            }
        }

        viewModel.stateReadinessAvgCard.observe(viewLifecycleOwner) {

            if (it == null) {
                binding.contentMain.lytReadinessAvg.root.gone()
                return@observe
            }

            binding.contentMain.lytReadinessAvg.root.visible()


            updateReadinessAvgUi(it)

        }

        viewModel.stateSleepAvgCard.observe(viewLifecycleOwner) {

            if (it == null) {
                binding.contentMain.lytSleepAvg.root.gone()
                return@observe
            }

            if (it.first != null || it.second != null) {
                binding.contentMain.lytSleepAvg.root.visible()

                updateSleepAvgUi(it)

            } else {
                binding.contentMain.lytSleepAvg.root.gone()
            }
        }

        viewModel.stateHeartRateCard.observe(viewLifecycleOwner) {
            if (it != null) {
                setHearRateCardUi(it)
            }
        }

        viewModel.stateStressCard.observe(viewLifecycleOwner) {
            if (it != null) {
                setStressCardUi(it)
            }
        }

        viewModel.statePairDeviceCard.observe(viewLifecycleOwner) {
            binding.contentMain.lytPairDevice.apply {
                if (it) {
                    this.root.visible()
                    this.root.setOnClickListener {
                        startActivity(PairDeviceActivity.getStartIntent(requireContext(), true))
                    }
                    viewModel.stateDashRingBattery.postValue(Pair(false, null))
                } else {
                    this.root.gone()
                }
            }
        }

        mainViewModel.stateConnectHelp.observe(viewLifecycleOwner) {
            if (it) {
                binding.contentMain.lytConnectHelp.root.visible()
                val logsSync = mainViewModel.shouldSyncAutoLogs()
                if (logsSync) {
                    if (mainViewModel.sessionManager.connectStateRing.value is ConnectState.ConnectSuccess) {
                        mainViewModel.sessionManager.sendQueryAction(QueryAction.GetFirmwareLogs)
                    } else {
                        context?.let { ctx ->
                            val status = ApplicationUtils.startFeedbackSubmitWorker(ctx)
                        }
                    }
                }
            } else {
                binding.contentMain.lytConnectHelp.root.gone()
            }
        }

        /* mainViewModel.sessionManager.firmwareLogsStatus.observe(this) { state ->
             when (state) {
                 2*//*END*//* -> {
                    *//* mainViewModel.viewModelScope.launch {
                         delay(1000)
                         context?.let { ctx ->
                             val status = ApplicationUtils.startFeedbackSubmitWorker(ctx)
                         }
                     }*//*
                }
            }
        }*/

        viewModel.stateDashRingBattery.observe(viewLifecycleOwner) {
            if (it.first) {
                binding.contentMain.lytChargeRing.root.visible()
                binding.contentMain.lytChargeRing.imageView3.loadImage(
                    requireContext(),
                    it.second?.ringInfo?.image2
                )

                if (viewModel.checkBeforeTime()) {
                    binding.contentMain.lytChargeRing.textView84.text =
                        getString(R.string.text_after_9_pm_battery_charge_msg)
                } else {
                    binding.contentMain.lytChargeRing.textView84.text =
                        getString(R.string.text_before_9_pm_battery_charge_msg)
                }
            } else {
                binding.contentMain.lytChargeRing.root.gone()
            }
        }


        viewModel.stateDashAlerts.observe(viewLifecycleOwner) {

            if (it.isNullOrEmpty()) {
                binding.contentMain.lytAlerts.root.gone()
                return@observe
            }
            if (it.size == 1) {
                binding.contentMain.lytAlerts.tabLayout.invisible()
            } else
                binding.contentMain.lytAlerts.tabLayout.visible()
            binding.contentMain.lytAlerts.apply {
                binding.contentMain.lytAlerts.root.visible()
                val winsAdapter = HomeRecyclerViewHolder.AlertsAdapter(object : AlertClickListener {
                    override fun onAlertClicked(alertType: AlertType) {
                        handleAlertClick(alertType)
                    }
                })
                vpAlertSlider.apply {
                    adapter = winsAdapter
                }
                winsAdapter.setDataSet(it)

                TabLayoutMediator(
                    tabLayout,
                    vpAlertSlider
                ) { _, _ -> }.attach()
            }
        }

        viewModel.sessionManager.connectStateRing.observe(viewLifecycleOwner) { connectedState ->
            when (connectedState) {
                is ConnectState.ConnectFailed -> {
                    viewModel.updateAlerts()
                    viewModel.stateDashRingBattery.postValue(Pair(false, null))
                    viewModel.handleGoogleFitCard()
                }

                is ConnectState.Connecting -> {
                    viewModel.updateAlerts()
                    viewModel.stateDashRingBattery.postValue(Pair(false, null))
                    viewModel.handleGoogleFitCard()
                }

                is ConnectState.ConnectSuccess -> {
                    viewModel.updateAlerts()
                    viewModel.handleBatteryAlert()
                    viewModel.handleGoogleFitCard()
                }

                is ConnectState.UnPaired -> {
                    viewModel.updateAlerts()
                    viewModel.stateDashRingBattery.postValue(Pair(false, null))
                    viewModel.handleGoogleFitCard()
                }

                else -> {}
            }

        }

    }

    private fun setNotificationGoalsCardData(notificationGoal: NotificationGoals) {

        binding.contentMain.lytNotificationCard.apply {
            root.visible()
            val stepsGoal = notificationGoal.steps_required ?: 5000

            tvSteps.text =
                if (notificationGoal.steps == null) "0" else notificationGoal.steps.toString()
            tvStepsGoal.text = "/$stepsGoal"

            val userSteps = notificationGoal.steps ?: 0
            val percent = (userSteps.toFloat() / stepsGoal.toFloat()) * 100

            progressSteps.progress = percent

            if (percent >= 100) {
                textStepsGoalAchieved.visible()
            } else {
                textStepsGoalAchieved.gone()
            }

            val hydrateGoal = notificationGoal.hydration_required ?: 3000
            val hydrate = notificationGoal.hydration ?: 0


            val hydrationText = StringBuilder()
            var hydratePercent = 0f

            if (viewModel.sessionManager.isMetric()) {
                hydrationText.append((hydrate.toFloat() / 1000))
                hydrationText.append("/")
                hydrationText.append((hydrateGoal.toFloat() / 1000))
                hydrationText.append("L")


                hydratePercent = (hydrate.toFloat() / hydrateGoal.toFloat()) * 100

            } else {

                val convertedHydrate = hydrate.toFloat() * 0.033814
                hydrationText.append(String.format("%.1f", convertedHydrate))
                hydrationText.append("/")

                //val convertedHydrateGoal = hydrateGoal.toFloat() * 0.033814
                val convertedHydrateGoal =
                    viewModel.convertMlToOuncesRounded(hydrateGoal.toDouble())

                hydrationText.append("$convertedHydrateGoal")
                hydrationText.append("oz")

                hydratePercent = (convertedHydrate.toFloat() / convertedHydrateGoal.toFloat()) * 100

            }
            progressHydrate.progress = hydratePercent

            if (hydratePercent >= 100) {
                textHydrateGoalAchieved.visible()
            } else {
                textHydrateGoalAchieved.gone()
            }

            tvHydration.text = hydrationText

            ivGlassImage.setImageResource(viewModel.getGlassImage(hydratePercent.toInt()))

            if (viewModel.notificationToggleModel?.hydrate_notification == true &&
                viewModel.notificationToggleModel?.master_notification == true
            ) {
                ivNotificationHydrate.setImageResource(R.drawable.ic_hydrate_notify_on)
            } else {
                ivNotificationHydrate.setImageResource(R.drawable.ic_hydrate_notify_off)
            }

            if (viewModel.notificationToggleModel?.steps_notification == true &&
                viewModel.notificationToggleModel?.steps_notification == true
            ) {
                ivNotificationSteps.setImageResource(R.drawable.ic_steps_notify_on)
            } else {
                ivNotificationSteps.setImageResource(R.drawable.ic_steps_notify_off)
            }
        }
    }

    private fun showBlackListDialog() {

        requireActivity().supportFragmentManager.setFragmentResultListener(
            RING_DISABLED_KEY,
            viewLifecycleOwner
        ) { key, bundle ->
            val cancel = bundle.getBoolean("cancel")
            if (cancel) {

            }
        }
        navigate(R.id.ringDisabledBottomSheet)
    }

    private fun setSleepAlertUi(data: SleepAlert) {
        binding.contentMain.lytSleepAlert.tvTitle.text = data.title
        binding.contentMain.lytSleepAlert.tvMessage.text = data.message

        if (data.addSleep) {
            binding.contentMain.lytSleepAlert.btnDone.visible()
            binding.contentMain.lytSleepAlert.btnDone.setOnClickListener {
                navigate(R.id.fragmentAddSleep)
            }
        } else {
            binding.contentMain.lytSleepAlert.btnDone.gone()
        }
        binding.contentMain.lytSleepAlert.ivClose.setOnClickListener {
            viewModel.removeSleepAlert()
        }
    }

    private fun setHealthMonitorCardData(data: HealthTrend?) {
        val hasHealthData = viewModel.hasHealthData(data)
        data?.apply {
            binding.contentMain.lytHealthMonitor.tvNudge.visible()
            binding.contentMain.lytHealthMonitor.tvNudge.text = nudge

            if (!bloodOxy?.status.isNullOrEmpty()) {
                binding.contentMain.lytHealthMonitor.imvSpo2.setImageResource(
                    viewModel.getHealthTrendIcon(
                        bloodOxy?.status
                    )
                )
            } else {
                binding.contentMain.lytHealthMonitor.imvSpo2.setImageResource(R.drawable.ic_hm_check_default)
            }

            if (!hrv?.status.isNullOrEmpty()) {
                binding.contentMain.lytHealthMonitor.imvHrv.setImageResource(
                    viewModel.getHealthTrendIcon(
                        hrv?.status
                    )
                )
            } else {
                binding.contentMain.lytHealthMonitor.imvHrv.setImageResource(R.drawable.ic_hm_check_default)
            }

            if (!rhr?.status.isNullOrEmpty()) {
                binding.contentMain.lytHealthMonitor.imvRHR.setImageResource(
                    viewModel.getHealthTrendIcon(
                        rhr?.status
                    )
                )
            } else {
                binding.contentMain.lytHealthMonitor.imvRHR.setImageResource(R.drawable.ic_hm_check_default)
            }

            if (!skinTemp?.status.isNullOrEmpty()) {
                binding.contentMain.lytHealthMonitor.imvSkin.setImageResource(
                    viewModel.getHealthTrendIcon(
                        skinTemp?.status
                    )
                )
            } else {
                binding.contentMain.lytHealthMonitor.imvSkin.setImageResource(R.drawable.ic_hm_check_default)
            }

            if (!resp?.status.isNullOrEmpty()) {
                binding.contentMain.lytHealthMonitor.imvResp.setImageResource(
                    viewModel.getHealthTrendIcon(
                        resp?.status
                    )
                )
            } else {
                binding.contentMain.lytHealthMonitor.imvResp.setImageResource(R.drawable.ic_hm_check_default)
            }
        }

        if (hasHealthData.not()) {
            binding.contentMain.lytHealthMonitor.apply {
                imvResp.setImageResource(R.drawable.ic_hm_check_default)
                imvRHR.setImageResource(R.drawable.ic_hm_check_default)
                imvSpo2.setImageResource(R.drawable.ic_hm_check_default)
                imvHrv.setImageResource(R.drawable.ic_hm_check_default)
                imvSkin.setImageResource(R.drawable.ic_hm_check_default)
                tvNudge.visible()
                tvNudge.text = getString(R.string.text_no_data_so_far)
            }
        }

        binding.contentMain.lytHealthMonitor.root.setOnClickListener {
            navigate(R.id.healthMonitorInternal, Bundle().apply {
                this.putParcelable("healthTrend", data)
                this.putString("selectedDate", LocalDate.now().toString())
                this.putString("source", "homepage")
            })
        }

    }

    private fun setFemaleGetStartedUI(data: OHealthOverview.CardTrackFemaleHealth) {
        binding.contentMain.lytFemaleHealthGetStarted.apply {
            root.visible()
            when (data.state) {
                FemaleHealthCardState.TRACK -> {
                    this.btnGetStarted.text =
                        this.btnGetStarted.context.getString(R.string.text_get_started)
                    this.textView92.text =
                        this.textView92.context.getString(R.string.text_track_your_cycle_desc)

                }

                FemaleHealthCardState.LOG -> {
                    this.btnGetStarted.text =
                        this.btnGetStarted.context.getString(R.string.text_log_period)
                    this.textView92.text =
                        this.textView92.context.getString(R.string.text_log_text)
                }
            }

            this.root.setOnClickListener {
                navigate(R.id.femaleHealthSplashFragment)
            }
            this.btnGetStarted.setOnClickListener {
                navigate(R.id.femaleHealthSplashFragment)
            }
            this.tvRemindMeLater.setOnClickListener {
                viewModel.localDataStore.setFMHRemindLater()
                viewModel.trackFemaleHealthCardData.postValue(null)
                //healthOverviewAdapter.removeCycleGetStartedCard()
            }
            this.ivCross.setOnClickListener {
                viewModel.localDataStore.setFMHRemindLater()
                viewModel.trackFemaleHealthCardData.postValue(null)
                //healthOverviewAdapter.removeCycleGetStartedCard()
            }
        }
    }

    private fun setSmallCardUi(data: OHealthOverview.CycleTrackerCardSmall) {
        binding.contentMain.lytFemaleHealthCardSmall.apply {
            this.root.visible()
            this.textView3.text = data.data.title
            this.tvOvlInDays.text = data.data.days.toString()
            this.textView1.text = data.data.bottomText
            this.tvPredictionDays.text = if (data.data.predictionDate.isNullOrEmpty().not()) {
                LocalDate.parse(data.data.predictionDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .format(
                        DateTimeFormatter.ofPattern(
                            "dd MMM",
                            Locale(NoiseFitApplicationMain.appLanguage.languageCode)
                        )
                    )
            } else {
                ""
            }

            this.tvOvlDaysCurrent.text =
                getString(R.string.text_day_value, data.data.currentCycleDay)
            this.tvOvlDaysLeft.text = getString(R.string.text_of_value, data.data.totalCycleDay)
            this.imv.setBackgroundResource(data.data.background)

            this.tvDesc.text = data.data.nudge

            this.root.setOnClickListener {
                navigate(R.id.fragmentCycleTracker)
            }
        }

    }

    private fun setBigCardUi(data: OHealthOverview.CycleTrackerCardBig) {
        binding.contentMain.lytFemaleHealthCardBig.apply {
            root.visible()
            this.textView3.text = data.data.title
            this.tvOvlInDays.text = data.data.subTitle
            this.tvCurrentDay.text = getString(R.string.text_day_value, data.data.days)
            this.tvDaysLeft.text = getString(R.string.text_of_value, data.data.totalCycleDay)
            this.tvDesc.text = data.data.nudge
            this.tvValue.text = if (data.data.temperatureVariation == null) {
                tvUnit.text = ""
                "-"
            } else {

                val tempVariation = if (viewModel.sessionManager.isMetric()) {
                    tvUnit.text = getString(R.string.text_degree_c)
                    AppConversionUtils.fahrenheitToCelsius(32 + data.data.temperatureVariation)
                } else {
                    tvUnit.text = getString(R.string.text_degree_f)
                    data.data.temperatureVariation
                }

                if (data.data.temperatureVariation > 0f) {
                    "+${String.format(locale = Locale.US, "%.1f", tempVariation)}"
                } else {
                    "-${String.format(locale = Locale.US, "%.1f", abs(tempVariation))}"
                }
            }
            this.imv.setBackgroundResource(data.data.background)

            this.tvPeriodicPeriod.text = data.data.predictionString

            this.tvDays.text = if (data.data.predictionDate.isNullOrEmpty().not()) {
                LocalDate.parse(data.data.predictionDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .format(
                        DateTimeFormatter.ofPattern(
                            "dd MMM",
                            Locale(NoiseFitApplicationMain.appLanguage.languageCode)
                        )
                    )
            } else {
                ""
            }

            this.root.setOnClickListener {
                navigate(R.id.fragmentCycleTracker)
            }
        }
    }

    private fun handleAlertClick(alertType: AlertType) {
        when (alertType) {
            AlertType.BLUETOOTH -> {
                mainViewModel.checkBluetooth.postValue(Event(true))
            }

            AlertType.DEFAULT -> {}
            AlertType.OTA_UPDATE -> {
                navigate(R.id.oreoUpdateRingFragment)
            }
        }
    }

    private fun updateReadinessAvgUi(data: ODashboardReadinessScoreModel) {
        val lytReadinessAvg = binding.contentMain.lytReadinessAvg
        if (data.readinessScore != null && data.readinessScore >= 0) {
            lytReadinessAvg.tvSleepScore.text = data.readinessScore.toString()
            lytReadinessAvg.tvDaysAvg.visible()
            lytReadinessAvg.tvSleepScore.visible()
            lytReadinessAvg.lineChart.visible()
            val trendValue = "${kotlin.math.abs(data.trend ?: 0)}%"
            if (data.trend != null && data.trend > 0) {
                lytReadinessAvg.sleepTrendValue.text = trendValue
                lytReadinessAvg.sleepTrendValue.setTextColor(Color.parseColor("#29cc74"))
                lytReadinessAvg.sleepTrendImv.loadImage(
                    lytReadinessAvg.sleepTrendImv.context, R.drawable.ic_trend_up
                )
                lytReadinessAvg.sleepTrendImv.visible()
                lytReadinessAvg.sleepTrendValue.visible()
                lytReadinessAvg.tvSleepFromLast.visible()
            } else if (data.trend != null && data.trend < 0) {
                lytReadinessAvg.sleepTrendValue.text = trendValue
                lytReadinessAvg.sleepTrendImv.loadImage(
                    lytReadinessAvg.sleepTrendImv.context, R.drawable.ic_trend_down
                )
                lytReadinessAvg.sleepTrendValue.setTextColor(Color.parseColor("#ff5b79"))
                lytReadinessAvg.sleepTrendImv.visible()
                lytReadinessAvg.sleepTrendValue.visible()
                lytReadinessAvg.tvSleepFromLast.visible()
            } else {
                lytReadinessAvg.sleepTrendImv.invisible()
                lytReadinessAvg.sleepTrendValue.invisible()
                lytReadinessAvg.tvSleepFromLast.invisible()
            }



            lytReadinessAvg.lineChart.updateDataWithMaxMin(
                viewModel.convertIntToChartModel(data.value), ArrayList(), ArrayList(), 20, true
            )
        } else {
            lytReadinessAvg.lineChart.updateDataWithMaxMin(
                viewModel.convertIntToChartModel(arrayListOf(0, 0, 0, 0, 0, 0, 0)),
                ArrayList(),
                ArrayList(),
                20,
                true
            )

            lytReadinessAvg.tvSleepScore.text = "--"
            lytReadinessAvg.tvDaysAvg.visible()
            lytReadinessAvg.lineChart.visible()
            lytReadinessAvg.sleepTrendImv.invisible()
            lytReadinessAvg.sleepTrendValue.invisible()
            lytReadinessAvg.tvSleepFromLast.invisible()
        }
    }

    private fun updateSleepAvgUi(data: Pair<ODashboardSleepScoreModel?, ODashboardActivityScoreModel?>) {
        val lytSleepAvg = binding.contentMain.lytSleepAvg

        val sleep = data.first!!
        val activity = data.second!!

        if (sleep.sleepScore != null && sleep.sleepScore >= 0) {
            lytSleepAvg.tvSleepScore.visible()
            lytSleepAvg.tvSleepScore.text = sleep.sleepScore.toString()
            lytSleepAvg.tvDaysAvg.visible()
            lytSleepAvg.sleepLineChart.visible()
            lytSleepAvg.sleepLine.root.visible()
            val trendValue = "${kotlin.math.abs(sleep.trend ?: 0)}%"
            if (sleep.trend != null && sleep.trend > 0) {
                lytSleepAvg.sleepTrendValue.text = trendValue
                lytSleepAvg.sleepTrendValue.setTextColor(Color.parseColor("#29cc74"))
                lytSleepAvg.sleepTrendImv.loadImage(
                    lytSleepAvg.sleepTrendImv.context, R.drawable.ic_trend_up
                )
                lytSleepAvg.sleepTrendImv.visible()
                lytSleepAvg.sleepTrendValue.visible()
                lytSleepAvg.tvSleepFromLast.visible()
            } else if (sleep.trend != null && sleep.trend < 0) {
                lytSleepAvg.sleepTrendValue.text = trendValue
                lytSleepAvg.sleepTrendImv.loadImage(
                    lytSleepAvg.sleepTrendImv.context, R.drawable.ic_trend_down
                )
                lytSleepAvg.sleepTrendValue.setTextColor(Color.parseColor("#ff5b79"))
                lytSleepAvg.sleepTrendImv.visible()
                lytSleepAvg.sleepTrendValue.visible()
                lytSleepAvg.tvSleepFromLast.visible()
            } else {
                lytSleepAvg.sleepTrendImv.invisible()
                lytSleepAvg.sleepTrendValue.invisible()
                lytSleepAvg.tvSleepFromLast.invisible()
            }


            lytSleepAvg.sleepLineChart.updateDataWithMaxMin(
                viewModel.convertIntToChartModel(sleep.value), ArrayList(), ArrayList(), 20, true
            )
        } else {
            lytSleepAvg.sleepLineChart.updateDataWithMaxMin(
                viewModel.convertIntToChartModel(arrayListOf(0, 0, 0, 0, 0, 0, 0)),
                ArrayList(),
                ArrayList(),
                20,
                true
            )

            lytSleepAvg.tvSleepScore.text = "--"
            lytSleepAvg.tvDaysAvg.visible()
            lytSleepAvg.sleepTrendImv.invisible()
            lytSleepAvg.sleepTrendValue.invisible()
            lytSleepAvg.tvSleepFromLast.invisible()
            lytSleepAvg.sleepLine.root.invisible()
        }

        if (activity.activityScore != null && activity.activityScore >= 0) {

            lytSleepAvg.tvActivityScore.text = activity.activityScore.toString()
            lytSleepAvg.activityLineChart.updateDataWithMaxMin(
                viewModel.convertIntToChartModel(activity.value), ArrayList(), ArrayList(), 20, true
            )
            lytSleepAvg.tvDaysAvg1.visible()
            lytSleepAvg.activityLineChart.visible()
            lytSleepAvg.activityLine.root.visible()
            lytSleepAvg.activityTrendImv.visible()
            lytSleepAvg.activityTrendValue.visible()
            lytSleepAvg.tvActivityFrom.visible()

            val trendValue = "${kotlin.math.abs(activity.trend ?: 0)}%"
            if (activity.trend != null && activity.trend > 0) {
                lytSleepAvg.activityTrendValue.text = trendValue
                lytSleepAvg.activityTrendValue.setTextColor(Color.parseColor("#29cc74"))
                lytSleepAvg.activityTrendImv.loadImage(
                    lytSleepAvg.sleepTrendImv.context, R.drawable.ic_trend_up
                )
                lytSleepAvg.activityTrendImv.visible()
                lytSleepAvg.activityTrendValue.visible()
                lytSleepAvg.tvActivityFrom.visible()
            } else if (activity.trend != null && activity.trend < 0) {
                lytSleepAvg.activityTrendValue.text = trendValue
                lytSleepAvg.activityTrendImv.loadImage(
                    lytSleepAvg.sleepTrendImv.context, R.drawable.ic_trend_down
                )
                lytSleepAvg.activityTrendValue.setTextColor(Color.parseColor("#ff5b79"))
                lytSleepAvg.activityTrendImv.visible()
                lytSleepAvg.activityTrendValue.visible()
                lytSleepAvg.tvActivityFrom.visible()
            } else {
                lytSleepAvg.activityTrendImv.invisible()
                lytSleepAvg.activityTrendValue.invisible()
                lytSleepAvg.tvActivityFrom.invisible()
            }
            //                binding.activityLineChart.updateDataWithMax(data.activityValue, ArrayList(), ArrayList())
        } else {
            lytSleepAvg.tvActivityScore.text = "--"
            lytSleepAvg.activityLineChart.updateDataWithMaxMin(
                viewModel.convertIntToChartModel(arrayListOf(0, 0, 0, 0, 0, 0, 0)),
                ArrayList(),
                ArrayList(),
                20,
                true
            )

            lytSleepAvg.tvDaysAvg1.visible()
            //lytSleepAvg.activityLineChart.gone()
            lytSleepAvg.activityLine.root.invisible()
            lytSleepAvg.activityTrendImv.invisible()
            lytSleepAvg.activityTrendValue.invisible()
            lytSleepAvg.tvActivityFrom.invisible()
        }


        binding.root.setOnClickListener {
            //   itemClickListener?.invoke(it, data, position)
        }
    }

    private fun setWorkoutUI(workouts: List<OActivityListModal>?) {
        val lytWorkouts = binding.contentMain.lytWorkouts
        lytWorkouts.root.visible()

        if (workouts.isNullOrEmpty()) {
            lytWorkouts.tvEmptyMsg.visible()
            lytWorkouts.tvEmptyMsg.text = getString(R.string.text_tap_plus_workout)

        } else {
            lytWorkouts.tvEmptyMsg.gone()
        }
        lytWorkouts.rvWorkouts.layoutManager = LinearLayoutManager(
            lytWorkouts.rvWorkouts.context, LinearLayoutManager.VERTICAL, false
        )
        val adapter1 = OreoRWorkoutAdapter(object : OreoRWorkoutAdapter.OnItemClickListener {
            override fun onItemClick(data: OActivityListModal, position: Int) {
                if (data.getDisplayVersionType() == 2) {
                    navigate(R.id.oWorkoutDetailsFragmentV2, Bundle().apply {
                        putString("workoutId", data.id ?: "")
                        putInt("position", position)
                    })
                } else {
                    navigate(R.id.oWorkoutDetailsFragment, Bundle().apply {
                        putString("workoutName", data.getTranslatedActivityName())
                        putString("workoutId", data.id ?: "")
                        putInt("position", position)
                    })
                }
            }
        })


        //        if (viewModel.ringDataStore.getRingDevice() != null) {
        //            lytWorkouts.viewAddWorkout.visible()
        //            lytWorkouts.root.visible()
        //        } else {
        //            lytWorkouts.viewAddWorkout.gone()
        //            if (workouts.isNullOrEmpty()) {
        //                lytWorkouts.root.gone()
        //            } else {
        //                lytWorkouts.root.visible()
        //            }
        //        }


        lytWorkouts.rvWorkouts.apply {
            adapter = adapter1
        }
        adapter1.setData(workouts ?: ArrayList())
        lytWorkouts.viewAddWorkout.setOnClickListener {
            if (viewModel.isDeviceConnected()) {
                navigate(R.id.addWorkoutFragment)
                viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_homepage_add_workout_click)
            } else {
                requireContext().showShortToast(getString(R.string.text_please_connect_your_ring_to_add_a_workout))
            }
        }

        lytWorkouts.root.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_homepage_workouts_entry_click)
            navigate(R.id.oActivityListFragment)
        }

    }

    private fun setStressCardUi(data: OHealthOverview.StressDashDataModel) {
        val lytStress = binding.contentMain.lytStressGraph
        lytStress.root.visible()
        lytStress.graphStress.updateData(data.data)


        /*lytStress.lottieAnimView.gone()
        lytStress.imvHrMeasure.gone()
        lytStress.tvLastMeasure.gone()
        lytStress.tvHeartValue.gone()
        lytStress.tvHeartUnit.gone()
        lytStress.tvEmptyConnect.gone()

        return

        when (data.measureState) {
            TapMeasureState.NO_DEVICE -> {
                lytStress.lottieAnimView.invisible()
                lytStress.imvHrMeasure.visible()

                lytStress.groupValue.gone()
                lytStress.tvEmptyConnect.visible()
                lytStress.tvEmptyConnect.text =
                    lytStress.tvEmptyConnect.context.getString(R.string.text_connect_your_device_to_measure)

            }

            TapMeasureState.LAST_MEASURED -> {
                lytStress.lottieAnimView.invisible()
                lytStress.imvHrMeasure.visible()

                lytStress.groupValue.visible()
                lytStress.tvEmptyConnect.gone()

                lytStress.tvHeartValue.text = if (data.value != null) "${data.value}" else ""
                lytStress.tvHeartUnit.text = viewModel.getStressStatus(data.value)

                lytStress.tvLastMeasure.apply {
                    setTextColor(Color.parseColor("#a3ffffff"))
                    text = data.lastTime
                }
            }

            TapMeasureState.MEASURING -> {
                lytStress.lottieAnimView.visible()
                lytStress.imvHrMeasure.invisible()

                lytStress.groupValue.gone()
                lytStress.tvEmptyConnect.visible()

                lytStress.tvEmptyConnect.apply {
                    setTextColor(resources.getColor(R.color.white))
                    text = getString(R.string.text_measuring_dots)
                }
            }

            TapMeasureState.DEFAULT -> {
                lytStress.lottieAnimView.invisible()
                lytStress.imvHrMeasure.visible()

                lytStress.groupValue.gone()
                lytStress.tvEmptyConnect.visible()
                lytStress.tvEmptyConnect.apply {
                    setTextColor(Color.parseColor("#88b0ff"))
                    text = getString(R.string.text_tap_to_measure)
                }
            }

            TapMeasureState.ERROR -> {
                lytStress.lottieAnimView.invisible()
                lytStress.imvHrMeasure.visible()

                lytStress.groupValue.visible()
                lytStress.tvEmptyConnect.gone()
                lytStress.tvHeartValue.gone()

                lytStress.tvLastMeasure.apply {
                    setTextColor(Color.parseColor("#88b0ff"))
                    text = getString(R.string.text_try_again)
                }
                lytStress.tvHeartUnit.text = getString(R.string.text_unable_to_measure)

            }

            TapMeasureState.HIDE -> {
                lytStress.lottieAnimView.invisible()
                lytStress.imvHrMeasure.invisible()

                lytStress.groupValue.invisible()
                lytStress.tvEmptyConnect.gone()
                lytStress.tvHeartValue.gone()
            }
        }
        lytStress.imvHrMeasure.setOnClickListener {

            if (WatchInfoGlobals.firmwareDeviceIdRing != WatchInfoGlobals.GEN_2_DEVICE_ID) {
                context.showShortToast(getString(R.string.text_tap_to_measure_is_only))
                *//*viewModel.stateStressCard.postValue(viewModel.stateStressCard.value?.apply {
                    this.measureState = TapMeasureState.ERROR
                })*//*
                return@setOnClickListener
            }

            if (data.measureState == TapMeasureState.MEASURING || data.measureState == TapMeasureState.NO_DEVICE) {
                return@setOnClickListener
            }

            if (viewModel.sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess) {
                return@setOnClickListener
            }

            if (viewModel.stateHeartRateCard.value?.measureState == TapMeasureState.MEASURING) {
                return@setOnClickListener
            }


            viewModel.viewModelScope.launch(Dispatchers.IO) {
                context?.let {
                    val isWorkerRunning = ApplicationUtils.isOreoSyncDataWorkerRunning(it)
                    if (isWorkerRunning) {
                        viewModel.stateStressCard.postValue(viewModel.stateStressCard.value?.apply {
                            this.measureState = TapMeasureState.ERROR
                        })
                        return@launch
                    }
                    viewModel.measureStress(true)
                }
            }

            return@setOnClickListener
        }*/


        /*binding.graphStress.updateData(data.data)
        binding.tvBeta.setVisibilityByCondition(data.isBeta)
        binding.ivBackBeta.setVisibilityByCondition(data.isBeta)*/


        val (lastMeasuredValue, lastMeasuredIndex) = viewModel.getLastMeasuredValue(data.listData)


        if (lastMeasuredValue == 0) {
            lytStress.tvStressValue.gone()
            lytStress.tvStressStatus.gone()
            lytStress.tvLastUpdate.gone()
            lytStress.lytTrend.root.gone()
        } else {
            lytStress.tvStressValue.visible()
            lytStress.tvStressStatus.visible()
            lytStress.tvLastUpdate.visible()

            lytStress.tvStressValue.text = "$lastMeasuredValue"
            val (displayValue, displayColor) = viewModel.getStressStatus(lastMeasuredValue)
            lytStress.tvStressStatus.text = displayValue
            lytStress.tvStressStatus.setTextColor(displayColor)


            val lastUpdatedTimestamp =
                DateTimeUtil.getTodayMidnightTimestamp() + (lastMeasuredIndex + 1) * 15 * 60 * 1000


            val currentTimeStamp = DateFormats.getTimeStamp()
            val timeDiff = currentTimeStamp - lastUpdatedTimestamp
            if (timeDiff <= (15 * 60 * 1000)) {

                val trendPercent = viewModel.getStressTrend(data.listData, lastMeasuredIndex)

                if (trendPercent != null && trendPercent != 0) {
                    if (trendPercent > 0) {
                        lytStress.lytTrend.apply {
                            ivTrend.setImageResource(R.drawable.ic_trend_dash_red)
                            backLayer.setBackgroundColor(Color.parseColor("#4DFF4365"))
                            tvPercent.text = "$trendPercent%"
                            tvPercent.setTextColor(Color.parseColor("#FF426F"))
                            root.visible()
                        }

                    } else {
                        lytStress.lytTrend.apply {
                            ivTrend.setImageResource(R.drawable.ic_trend_dash_green)
                            backLayer.setBackgroundColor(Color.parseColor("#6629CC74"))
                            tvPercent.text = "${abs(trendPercent)}%"
                            tvPercent.setTextColor(Color.parseColor("#00FF66"))
                            root.visible()
                        }
                    }
                } else {
                    lytStress.lytTrend.root.gone()
                }
            } else {
                lytStress.lytTrend.root.gone()
            }


            if (lastUpdatedTimestamp == 0L) {
                lytStress.tvLastUpdate.text = ""
            } else {
                lytStress.tvLastUpdate.text =
                    lytStress.tvLastUpdate.context.getString(
                        R.string.text_updated_value,
                        DateTimeUtil.getRelativeTime(
                            lastUpdatedTimestamp,
                            viewModel.resourceProvider
                        ).lowercase()
                    )
            }
        }
    }

    private fun setHearRateCardUi(data: OHealthOverview.HeartRateDataModel) {
        val lytHeartRate = binding.contentMain.lytHeartRate
        lytHeartRate.root.visible()
        lytHeartRate.candleChart.enableInteractiveMode(false)
        lytHeartRate.candleChart.updateData(
            viewModel.hrDataConvertor.getHrCombinedData(
                viewModel.serverUserHealthData, data
            ), 3, data.minValues, data.maxValues
        )

        val (lastMeasuredValue, lastMeasuredIndex) = viewModel.getLastMeasuredValue(data.rawData)

        if (lastMeasuredValue == 0) {
            lytHeartRate.lytTrend.root.gone()
        } else {
            val lastUpdatedTimestamp =
                DateTimeUtil.getTodayMidnightTimestamp() + (lastMeasuredIndex + 1) * 5 * 60 * 1000


            val currentTimeStamp = DateFormats.getTimeStamp()
            val timeDiff = currentTimeStamp - lastUpdatedTimestamp
            if (timeDiff <= (5 * 60 * 1000)) {

                val trendPercent = viewModel.getHrTrend(data.rawData, lastMeasuredIndex)

                if (trendPercent != null && trendPercent != 0) {
                    if (trendPercent > 0) {
                        lytHeartRate.lytTrend.apply {
                            ivTrend.setImageResource(R.drawable.ic_trend_dash_red)
                            backLayer.setBackgroundColor(Color.parseColor("#4DFF4365"))
                            tvPercent.text = "$trendPercent%"
                            tvPercent.setTextColor(Color.parseColor("#FF426F"))
                            root.visible()
                        }
                    } else {
                        lytHeartRate.lytTrend.apply {
                            ivTrend.setImageResource(R.drawable.ic_trend_dash_green)
                            backLayer.setBackgroundColor(Color.parseColor("#6629CC74"))
                            tvPercent.text = "${abs(trendPercent)}%"
                            tvPercent.setTextColor(Color.parseColor("#00FF66"))
                            root.visible()
                        }
                    }
                } else {
                    lytHeartRate.lytTrend.root.gone()
                }
            } else {
                lytHeartRate.lytTrend.root.gone()
            }
        }



        when (data.measureState) {
            TapMeasureState.NO_DEVICE -> {
                lytHeartRate.lottieAnimView.invisible()
                lytHeartRate.imvHrMeasure.visible()

                lytHeartRate.groupValue.gone()
                lytHeartRate.tvEmptyConnect.visible()
                lytHeartRate.tvEmptyConnect.text =
                    lytHeartRate.tvEmptyConnect.context.getString(R.string.text_connect_your_device_to_measure)

            }

            TapMeasureState.LAST_MEASURED -> {
                lytHeartRate.lottieAnimView.invisible()
                lytHeartRate.imvHrMeasure.visible()

                lytHeartRate.groupValue.visible()
                lytHeartRate.tvEmptyConnect.gone()

                lytHeartRate.tvHeartValue.text = data.value
                lytHeartRate.tvHeartUnit.text = getString(R.string.text_bpm_small)

                lytHeartRate.tvLastMeasure.apply {
                    setTextColor(Color.parseColor("#a3ffffff"))
                    text = data.lastTime
                }

            }

            TapMeasureState.MEASURING -> {
                lytHeartRate.lottieAnimView.visible()
                lytHeartRate.imvHrMeasure.invisible()

                lytHeartRate.groupValue.gone()
                lytHeartRate.tvEmptyConnect.visible()

                lytHeartRate.tvEmptyConnect.apply {
                    setTextColor(resources.getColor(R.color.white))
                    text = getString(R.string.text_measuring_dots)
                }
            }

            TapMeasureState.DEFAULT -> {
                lytHeartRate.lottieAnimView.invisible()
                lytHeartRate.imvHrMeasure.visible()

                lytHeartRate.groupValue.gone()
                lytHeartRate.tvEmptyConnect.visible()
                lytHeartRate.tvEmptyConnect.apply {
                    setTextColor(Color.parseColor("#88b0ff"))
                    text = getString(R.string.text_tap_to_measure)
                }
            }

            TapMeasureState.ERROR -> {
                lytHeartRate.lottieAnimView.invisible()
                lytHeartRate.imvHrMeasure.visible()

                lytHeartRate.groupValue.visible()
                lytHeartRate.tvEmptyConnect.gone()
                lytHeartRate.tvHeartValue.gone()

                lytHeartRate.tvLastMeasure.apply {
                    setTextColor(Color.parseColor("#88b0ff"))
                    text = getString(R.string.text_try_again)
                }
                lytHeartRate.tvHeartUnit.text = getString(R.string.text_unable_to_measure)

            }

            TapMeasureState.HIDE -> {
                lytHeartRate.lottieAnimView.invisible()
                lytHeartRate.imvHrMeasure.invisible()

                lytHeartRate.groupValue.invisible()
                lytHeartRate.tvEmptyConnect.gone()
                lytHeartRate.tvHeartValue.gone()
            }
        }

        lytHeartRate.imvHrMeasure.setOnClickListener {

            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_homepage_hr_refresh_click)
            if (data.measureState == TapMeasureState.MEASURING || data.measureState == TapMeasureState.NO_DEVICE) {
                return@setOnClickListener
            }

            if (viewModel.sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess) {
                return@setOnClickListener
            }

            if (viewModel.stateStressCard.value?.measureState == TapMeasureState.MEASURING) {
                return@setOnClickListener
            }

            viewModel.viewModelScope.launch(Dispatchers.IO) {
                context?.let {
                    val isWorkerRunning = ApplicationUtils.isOreoSyncDataWorkerRunning(it)
                    if (isWorkerRunning) {
                        viewModel.stateHeartRateCard.postValue(viewModel.stateHeartRateCard.value?.apply {
                            this.measureState = TapMeasureState.ERROR
                        })
                        return@launch
                    }
                    viewModel.measureHr(true)
                }
            }

            return@setOnClickListener
        }
    }

    private fun showRemoveNapBottomSheet(nap: OreoNapData) {
        requireActivity().supportFragmentManager.setFragmentResultListener(
            NAP_REQUEST_KEY,
            this
        ) { _, bundle ->
            val updated = bundle.getBoolean("remove")
            if (updated) {
                viewModel.removeNapById(nap)
            }
        }

        navigate(R.id.removeNapBottomSheet)
    }

    private fun logFirebaseAppEvent(eventName: String, params: HashMap<String, Any>) {
        viewModel.sessionManager.logFirebaseEvent(eventName, params)
    }


    private fun handleFindMyRingCard() {
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            val isFindMyRingCrossed = viewModel.localDataStore.isFindMyRingLocationCardHidden()
            if (hasGpsPermission().not() && isFindMyRingCrossed.not()) {
                viewModel.findMyRingCard.postValue(true)
            } else {
                viewModel.findMyRingCard.postValue(false)
            }
        }
    }


    private fun hasGpsPermission(): Boolean {
        val permissionAccessFineLocationApproved =
            (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
                    == PackageManager.PERMISSION_GRANTED)

        val backgroundLocationPermissionApproved =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            } else {
                true
            }

        return permissionAccessFineLocationApproved && backgroundLocationPermissionApproved
    }

    private fun showLocationPermissionDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }

    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        var openSettings = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when {
                permissions.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    false
                ) && permissions.getOrDefault(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    false
                ) -> {
                    LOGS.d("LOCATION_PERM LOCATION GRANTED")
                }

                else -> {
                    openSettings = true
                }
            }
        } else {
            when {
                permissions.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    false
                ) -> {
                    LOGS.d("LOCATION_PERM LOCATION GRANTED")
                }

                else -> {
                    openSettings = true
                }
            }
        }
        if (openSettings) {
            tryCatch {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
    }

    private fun showPermDetailsDialog() {
        requireActivity().supportFragmentManager.setFragmentResultListener(
            FIND_RING_LOCATION_PERM_REQUEST,
            this
        ) { _, bundle ->
            val allow = bundle.getBoolean("allow")
            if (allow) {
                showLocationPermissionDialog()
            }
        }
        navigate(R.id.bottomSheetLocationPermissionFindMyRing, bundleOf("postOnActivity" to true))
    }
}