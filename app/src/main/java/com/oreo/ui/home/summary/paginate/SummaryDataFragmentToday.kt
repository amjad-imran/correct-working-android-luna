package com.oreo.ui.home.summary.paginate

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
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
import com.noisefit_commans.data.model.OreoNapData
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.loadImageWithCache
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.AppConversionUtils
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageAppEventParams
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit_commans.utils.share.ShareUtil.SUPPORT_URL
import com.oreo.data.model.AlertType
import com.oreo.data.model.FemaleHealthCardState
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
import com.oreo.ui.custom.CirclePagerIndicatorDecoration
import com.oreo.ui.custom.SnapHelperOneByOne
import com.oreo.ui.femalehealth.cycletracker.log.CycleLogFragment
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
import com.oreo.ui.sleep2.help.LearnMoreFragment
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
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
        initSleepPlanerUi()

    }

    private fun initSleepPlanerUi() {

        binding.contentMain.lytSplanner.apply {
            lytBedTime.ivIcon.setImageResource(R.drawable.ic_bedtime_gray)

            lytBedTime.tvTitle.text = getString(R.string.text_bedtime)
            lytBedTime.tvTitle.setTextColor(
                ContextCompat.getColor(
                    binding.contentMain.lytSplanner.root.context,
                    R.color.white_64
                )
            )
            lytBedTime.tvTimeUnit.setTextColor(
                ContextCompat.getColor(
                    binding.contentMain.lytSplanner.root.context,
                    R.color.white
                )
            )
            lytBedTime.tvTime.text = "11:00"
            lytBedTime.tvTimeUnit.text = "pm"

            lytWakeupTime.ivIcon.setImageResource(R.drawable.ic_wakeup_gray)

            lytWakeupTime.tvTitle.text = getString(R.string.text_wakeup)
            lytWakeupTime.tvTitle.setTextColor(
                ContextCompat.getColor(
                    binding.contentMain.lytSplanner.root.context,
                    com.noisefit_commans.R.color.white_64
                )
            )
            lytWakeupTime.tvTimeUnit.setTextColor(
                ContextCompat.getColor(
                    binding.contentMain.lytSplanner.root.context,
                    R.color.white
                )
            )
            lytWakeupTime.tvTime.text = "7:30"
            lytWakeupTime.tvTimeUnit.text = "am"
        }
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


    }

    fun loadData() {
        LOGS.d(TAG, "Today Load data")
        viewModel.date?.let {
            mainViewModel.getDashBoardData(it)?.let { dash ->
                viewModel.registerDate = mainViewModel.registerDate
                viewModel.serverUserHealthData = dash.first
                viewModel.stressBeta = mainViewModel.stressBeta
                viewModel.enableAi = mainViewModel.enableAi
                viewModel.shouldShowStressCard = mainViewModel.shouldShowStressCard(it)
                setUi(dash.first, dash.second)
            }
        }
    }

    private fun setUi(data: ServerUserHealthData, trendsData: TrendsData?) {
        viewModel.initTodayData()
        viewModel.parseHealthData(data, trendsData)
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
                        mainViewModel.getChatHistoryToday()
                    } else {
                        navigate(R.id.chatSplashFragment)
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
                    })
                }
            }
        }

    }


    override fun initListener() {

        binding.contentMain.lytHeartRate.root.setOnClickListener {
            navigate(R.id.fragmentHeartRateDetails)
        }

        binding.contentMain.lytSplanner.ivMore.setOnClickListener {
            navigate(R.id.setAlarmFragment)
        }
        binding.contentMain.lytSplanner.lytBreathe.ivPlay.setOnClickListener {
            navigate(R.id.fragmentBreathExercise)
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
            viewModel.ringDataStore.setGoogleFitCrossed(true)
            viewModel.stateGoogleFitCard.postValue(false)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false


            val pairStatus: String

            LOGS.d("SyncDataWork: starting job")
            if (!viewModel.isDeviceConnected()) {
                pairStatus = "unpaired"
                return@setOnRefreshListener
            }

            if (viewModel.stateHeartRateCard.value?.measureState == TapMeasureState.MEASURING) {
                return@setOnRefreshListener
            }
            pairStatus = "paired"

            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_activity_sync_manual,
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
            navigate(R.id.troubleShootBottomSheetFragment)
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

    override fun subscribeObservers() {

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
                LOGS.d(TAG, "Today data reload")
                loadData()
            }
        }

        viewModel.sessionManager.manualMeasurementValue.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                if (it) {
                    viewModel.updateManualValue()
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
                }

                is ConnectState.Connecting -> {
                    viewModel.updateAlerts()
                    viewModel.stateDashRingBattery.postValue(Pair(false, null))
                }

                is ConnectState.ConnectSuccess -> {
                    viewModel.updateAlerts()
                    viewModel.handleBatteryAlert()
                }

                is ConnectState.UnPaired -> {
                    viewModel.updateAlerts()
                    viewModel.stateDashRingBattery.postValue(Pair(false, null))

                }

                else -> {}
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
            var outOfRangeCount = 0
            var isSignificant = false
            var trendName = ""
            if (!bloodOxy?.status.isNullOrEmpty()) {
                val state = viewModel.getHealthTrendState(
                    bloodOxy?.status
                )
                if (state == 2 || state == 1) {
                    outOfRangeCount++
                    trendName = "Blood oxygen"
                }
                if (state == 2) {
                    isSignificant = true
                }
                binding.contentMain.lytHealthMonitor.imvSpo2.setImageResource(
                    viewModel.getHealthTrendIcon(
                        bloodOxy?.status
                    )
                )
            } else {
                binding.contentMain.lytHealthMonitor.imvSpo2.setImageResource(R.drawable.ic_hm_check_default)
            }

            if (!hrv?.status.isNullOrEmpty()) {
                val state = viewModel.getHealthTrendState(
                    hrv?.status
                )
                if (state == 2 || state == 1) {
                    outOfRangeCount++
                    trendName = "HRV"
                }
                if (state == 2) {
                    isSignificant = true
                }
                binding.contentMain.lytHealthMonitor.imvHrv.setImageResource(
                    viewModel.getHealthTrendIcon(
                        hrv?.status
                    )
                )
            } else {
                binding.contentMain.lytHealthMonitor.imvHrv.setImageResource(R.drawable.ic_hm_check_default)
            }

            if (!rhr?.status.isNullOrEmpty()) {
                val state = viewModel.getHealthTrendState(
                    rhr?.status
                )
                if (state == 2 || state == 1) {
                    outOfRangeCount++
                    trendName = "Resting HR"
                }
                if (state == 2) {
                    isSignificant = true
                }
                binding.contentMain.lytHealthMonitor.imvRHR.setImageResource(
                    viewModel.getHealthTrendIcon(
                        rhr?.status
                    )
                )
            } else {
                binding.contentMain.lytHealthMonitor.imvRHR.setImageResource(R.drawable.ic_hm_check_default)
            }

            if (!skinTemp?.status.isNullOrEmpty()) {
                val state = viewModel.getHealthTrendState(
                    skinTemp?.status
                )
                if (state == 2 || state == 1) {
                    outOfRangeCount++
                    trendName = "Skin temperature"
                }
                if (state == 2) {
                    isSignificant = true
                }
                binding.contentMain.lytHealthMonitor.imvSkin.setImageResource(
                    viewModel.getHealthTrendIcon(
                        skinTemp?.status
                    )
                )
            } else {
                binding.contentMain.lytHealthMonitor.imvSkin.setImageResource(R.drawable.ic_hm_check_default)
            }

            if (!resp?.status.isNullOrEmpty()) {
                val state = viewModel.getHealthTrendState(
                    resp?.status
                )
                if (state == 2 || state == 1) {
                    outOfRangeCount++
                    trendName = "Respiratory rate"
                }
                if (state == 2) {
                    isSignificant = true
                }
                binding.contentMain.lytHealthMonitor.imvResp.setImageResource(
                    viewModel.getHealthTrendIcon(
                        resp?.status
                    )
                )
            } else {
                binding.contentMain.lytHealthMonitor.imvResp.setImageResource(R.drawable.ic_hm_check_default)
            }

            if (outOfRangeCount == 0) {
                binding.contentMain.lytHealthMonitor.tvNudge.visible()
                binding.contentMain.lytHealthMonitor.tvNudge.text =
                    "All readings are in your typical range"
            } else if (outOfRangeCount == 1) {
                val text = if (isSignificant) {
                    "significantly"
                } else {
                    "slightly"
                }
                binding.contentMain.lytHealthMonitor.tvNudge.visible()
                binding.contentMain.lytHealthMonitor.tvNudge.text =
                    "Your $trendName is $text elevated"
            } else {
                binding.contentMain.lytHealthMonitor.tvNudge.visible()
                binding.contentMain.lytHealthMonitor.tvNudge.text =
                    "$outOfRangeCount/5 metrics are out of range"
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
                tvNudge.text = "No data so far"
            }
        }

        binding.contentMain.lytHealthMonitor.root.setOnClickListener {
            navigate(R.id.healthMonitorInternal, Bundle().apply {
                this.putParcelable("healthTrend", data)
                this.putString("selectedDate", LocalDate.now().toString())
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
            this.tvPredictionDays.text = data.data.predictionDate
            this.tvOvlDaysCurrent.text = "Day ${data.data.currentCycleDay}"
            this.tvOvlDaysLeft.text = "of ${data.data.totalCycleDay}"
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
            this.tvCurrentDay.text = "Day ${data.data.days}"
            this.tvDaysLeft.text = "of ${data.data.totalCycleDay}"
            this.tvDesc.text = data.data.nudge
            this.tvValue.text = if (data.data.temperatureVariation == null) {
                tvUnit.text = ""
                "-"
            } else {

                val tempVariation = if (viewModel.sessionManager.isMetric()) {
                    tvUnit.text = "°C"
                    AppConversionUtils.fahrenheitToCelsius(32 + data.data.temperatureVariation)
                } else {
                    tvUnit.text = "°F"
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
            this.tvDays.text = data.data.predictionDate

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
                        putString("workoutName", data.getFormattedActivityName())
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

    private fun setHearRateCardUi(data: OHealthOverview.HeartRateDataModel) {
        val lytHeartRate = binding.contentMain.lytHeartRate
        lytHeartRate.root.visible()
        lytHeartRate.candleChart.enableInteractiveMode(false)
        lytHeartRate.candleChart.updateData(
            viewModel.hrDataConvertor.getHrCombinedData(
                viewModel.serverUserHealthData, data
            ), 3, data.minValues, data.maxValues
        )

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
                    text = "Measuring..."
                }
            }

            TapMeasureState.DEFAULT -> {
                lytHeartRate.lottieAnimView.invisible()
                lytHeartRate.imvHrMeasure.visible()

                lytHeartRate.groupValue.gone()
                lytHeartRate.tvEmptyConnect.visible()
                lytHeartRate.tvEmptyConnect.apply {
                    setTextColor(Color.parseColor("#88b0ff"))
                    text = "Tap to measure"
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
                    text = "Try again"
                }
                lytHeartRate.tvHeartUnit.text = "Unable to measure"

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

            viewModel.viewModelScope.launch(Dispatchers.IO) {
                context?.let {
                    val isWorkerRunning = ApplicationUtils.isOreoSyncDataWorkerRunning(it)
                    LOGS.w("imvHrMeasure isOreoSyncDataWorkerRunning $isWorkerRunning")
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
}