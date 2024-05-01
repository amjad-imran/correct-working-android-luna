package com.oreo.ui.home.summary.paginate

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSummaryDataTodayBinding
import com.noisefit.oreo.BottomNavOption
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.common.bottomSheet.DELETE_REQ_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.NAP_REQUEST_KEY
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.enums.DashInfoCard
import com.noisefit_commans.data.model.OreoNapData
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.loadImageWithCache
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageAppEventParams
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.AlertType
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.TapMeasureState
import com.oreo.data.model.TrendsData
import com.oreo.data.model.VideoInfoType
import com.oreo.data.model.health.ODashboardActivityScoreModel
import com.oreo.data.model.health.ODashboardReadinessScoreModel
import com.oreo.data.model.health.ODashboardSleepScoreModel
import com.oreo.ui.custom.CirclePagerIndicatorDecoration
import com.oreo.ui.custom.SnapHelperOneByOne
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
        LOGS.d(TAG, "Today onDestroyView called")
    }

    override fun onResume() {
        super.onResume()

        LOGS.d("SUMMART_TODAY on resume")
        LOGS.d(TAG, "Today onResume called")
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
                }

                is OSummaryHealthOverviewClickEnum.OnNapClicked -> {

                    navigate(R.id.napDetails, bundleOf("napId" to type.napId))
                }
            }
        }

    }


    override fun initListener() {
        binding.contentMain.lytHeartRate.bInfo.invisible()
        /*binding.contentMain.lytHeartRate.root.setOnClickListener {
            navigate(R.id.fragmentHeartRateDetails)
        }*/

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
            navigate(R.id.oreoHSQuestionFragment, Bundle().apply {
                putString("title", "Battery & Charging")
                putString("id", "6")
            })
        }

        binding.contentMain.lytChargeRing.root.setOnClickListener {
            navigate(R.id.ringBatteryChargeFragment)
            //viewModel.setRingBatteryInfoState()
        }

        binding.contentMain.lytPairDevice.btnPairDevice.setOnClickListener {
            startActivity(PairDeviceActivity.getStartIntent(requireContext(), true))
        }
        binding.contentMain.lytHeartRate.bInfo.setOnClickListener {
            viewModel.getContributorInfo("hr")
        }

        binding.contentMain.lytReadinessAvg.root.setOnClickListener {
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
        }

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
                    context?.let { ctx ->
                        val status = ApplicationUtils.startFeedbackSubmitWorker(ctx)
                    }
                }
            } else {
                binding.contentMain.lytConnectHelp.root.gone()
            }
        }

        viewModel.stateDashRingBattery.observe(viewLifecycleOwner) {
            if (it.first) {
                binding.contentMain.lytChargeRing.root.visible()
                binding.contentMain.lytChargeRing.imageView3.loadImage(
                    requireContext(),
                    it.second?.ringInfo?.image2
                )

                if (viewModel.checkBeforeTime()) {
                    binding.contentMain.lytChargeRing.textView84.text =
                        getString(R.string.text_before_9_pm_battery_charge_msg)
                } else {
                    binding.contentMain.lytChargeRing.textView84.text =
                        getString(R.string.text_after_9_pm_battery_charge_msg)
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