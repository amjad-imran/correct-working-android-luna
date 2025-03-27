package com.oreo.ui.home.summary

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSummaryOBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.constants.SyncEvents
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageAppEventParams
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.ChartModel
import com.oreo.data.model.FMHCycleHistoryDataModel
import com.oreo.ui.custom.ScrollListener
import com.oreo.ui.femalehealth.cycletracker.streak.CycleDetailsFragment
import com.oreo.ui.home.summary.paginate.SummaryPagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class OSummaryFragment : BaseFragment<FragmentSummaryOBinding>(FragmentSummaryOBinding::inflate),
    ScrollListener {

    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val viewModel: OSummaryViewModel by viewModels()

    private var pagerAdapter: SummaryPagerAdapter? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.sessionManager.logMoEngageAppEvent(
            MoEngageLunaAppEvents.luna_homepage_visit,
            HashMap<String, Any>().apply {
                this[MoEngageAppEventParams.operating_system] = "Android"
                this[MoEngageAppEventParams.device_pairing_status] = viewModel.isDeviceConnected()
            })
        setViewPager()

    }

    private fun setViewPager() {

        pagerAdapter = SummaryPagerAdapter(this)
        binding.viewPagerSummary.adapter = pagerAdapter
        binding.viewPagerSummary.offscreenPageLimit = 1

        /*TabLayoutMediator(binding.tabLayout, binding.viewPagerSummary) { tab, position ->
            tab.text = pagerAdapter!!.getDate(position)
        }.attach()*/

        binding.viewPagerSummary.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                mainViewModel.selectedDate = pagerAdapter?.getDate(position)
                mainViewModel.handleAddWorkoutVisibility()

                if (!binding.tabLayout.isInteracting) {
                    setTopBar()
                }
                //setTabDates(mainViewModel.selectedDate)

                if (mainViewModel.shouldLoadMoreData()) {
                    LOGS.w("Loading more data")
                }
            }
        })
    }

    override fun initListener() {

        binding.tabLayout.setOnChartScrollChangedListener(this)


        binding.lytHeader.oreoStatus.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_device_capsule_click)
            navigate(R.id.oreo_my_device)
        }

        binding.lytHeader.lottieAnimView.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_device_capsule_click)
            navigate(R.id.oreo_my_device)
        }



        binding.lytHeader.batteryStatus.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_device_capsule_click)
            navigate(R.id.oreo_my_device)
        }


        binding.lytHeader.profileView1.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.user_ham_clicked,
                HashMap<String, Any>().apply {
                    this["property"] = "just_clicked"
                })
            navigate(R.id.OMyProfileFragment)
        }

        /* setFragmentResultListener(ADD_WORKOUT_REQUEST_KEY) { _, bundle ->
             val allow = bundle.getBoolean("allow")

             if (allow) {
                 viewModel.getRecentWorkoutList()

             }
         }*/
        /* binding.layoutRefresh.animationView.setAnimation(R.raw.loading_swipe_anim)
         binding.swipeToRefresh.setOnRefreshListener(object : RefreshingListenerAdapter() {
             override fun onRefreshing() {
                 super.onRefreshing()
                 val pairStatus: String

                 LOGS.d("SyncDataWork: starting job")
                 if (!viewModel.isDeviceConnected()) {
                     binding.swipeToRefresh.refreshComplete()
                     pairStatus = "unpaired"
                     return
                 }

                 if (viewModel.stateHeartRateCard.value?.measureState == TapMeasureState.MEASURING) {
                     binding.swipeToRefresh.refreshComplete()
                     return
                 }
                 pairStatus = "paired"
 //                viewModel.sessionManager.logFirebaseEvent(
 //                    FirebaseLunaAppEvents.LUNA_ACTIVITY_SYNC_MANUAL,
 //                    HashMap<String, Any>().apply {
 //                        this["operating_system"] = "Android"
 //                        this["device_pairing_status"] = pairStatus
 //                    })

                 logFirebaseAppEvent(FirebaseLunaAppEvents.LUNA_ACTIVITY_SYNC_MANUAL,
                     HashMap<String, Any>().apply {
                         this["operating_system"] = "Android"
                         this["device_pairing_status"] = pairStatus
                     })
                 binding.layoutRefresh.textSyncingData.visible()
                 binding.swipeToRefresh.refreshComplete()

                 syncData()
             }
         })*/


        /* //handle device intro
         if (!mainViewModel.ringDataStore.isShowDeviceIntro()) {
             setFragmentResultListener(CALL_GOT_IT) { _, bundle ->
                 val isSelected = bundle.getBoolean("isSelected")
                 if (isSelected) {
                     mainViewModel.ringDataStore.setShowDeviceIntro(true)
                 }
             }
             navigate(R.id.myDeviceIntroBottomSheet)
         }*/

    }

    fun resetSwipeLoadingAnim() {
        /* binding.layoutRefresh.textSyncingData.gone()
         binding.swipeToRefresh.refreshComplete()*/
    }


    private fun shouldSync() {
        val lastSyncTime = viewModel.sessionManager.getLastSyncTime() ?: 0L
        LOGS.d("shouldSync $lastSyncTime -- ${DateFormats.getTimeStamp()}")

        val shouldSync = viewModel.sessionManager.forceSyncData.value?.getContent() ?: false

        if (shouldSync || kotlin.math.abs(DateFormats.getTimeStamp() - lastSyncTime) > 5 * 60 * 1000L) {
            if (viewModel.sessionManager.bluetoothStateDash.value != false) {
                mainViewModel.syncTextState.value = getString(R.string.text_syncing_recent_data)
            }
            syncData()
        }
    }

    fun setTopBar() {

        val it = mainViewModel.dashboard.value
        if (it.isNullOrEmpty()) return

        val topGraphData = viewModel.getPrefixAndSuffixList(it)

        var moveToPos = -1

        if (mainViewModel.selectedDate != null) {
            val index = it?.indexOfFirst { data ->
                data.equals(mainViewModel.selectedDate, true)
            }
            if (index != null) {
                moveToPos = 15 + (it.size - index - 1)
            }
        }

        binding.tabLayout.updateDataWithMax(
            topGraphData.first,
            topGraphData.third,
            topGraphData.second,
            moveToPos
        )
    }


    override fun subscribeObservers() {
        val allSetText = getString(R.string.text_all_set)
        mainViewModel.syncTextState.observe(this) {
            if (it.isNullOrEmpty()) {
                binding.lytHeader.tvHeaderStatus.gone()
                binding.lytHeader.tvHeaderStatusNonShimmer.gone()
            } else {
                if (it.equals(allSetText, true)) {
                    binding.lytHeader.tvHeaderStatusNonShimmer.visible()
                    binding.lytHeader.tvHeaderStatus.gone()
                    binding.lytHeader.tvHeaderStatusNonShimmer.text = it
                } else {
                    binding.lytHeader.tvHeaderStatusNonShimmer.gone()
                    binding.lytHeader.tvHeaderStatus.visible()
                    binding.lytHeader.tvHeaderStatus.text = it
                }
            }
        }

        mainViewModel.dashboard.observe(viewLifecycleOwner) {
            setTopBar()
            pagerAdapter?.setDataSet(it)

            val pos = pagerAdapter?.getPositionForDate(mainViewModel.selectedDate) ?: (it.size - 1)

            binding.viewPagerSummary.setCurrentItem(pos, false)
            binding.tabLayout.visible()
            //setTabDates(pos)

        }


        /* viewModel.sessionManager.showSyncOfflineData.observe(viewLifecycleOwner) {
             it.getContent()?.let { event ->
                 if (event == HealthOverviewDataType.AUTO_WORKOUT) {
                     //viewModel.getDashboardDataFromServer(false)
                 }
             }

         }*/



        viewModel.sessionManager.forceSyncData.observe(this) {
            if (viewModel.sessionManager.connectStateRing.value is ConnectState.ConnectSuccess) {
                it.getContent()?.let {
                    syncData()
                }
            }

        }


        viewModel.deviceConnected.observe(this) { connected ->
            if (!connected) {
                binding.lytHeader.ivExclamation.gone()
                binding.lytHeader.batteryStatus.invisible()
                binding.lytHeader.lottieAnimView.gone()
                binding.lytHeader.oreoStatus.visible()
                binding.lytHeader.oreoStatus.setImageResource(
                    R.drawable.ic_ring_not_connected
                )
            }
        }

        /*viewModel.sessionManager.syncCompleted.observe(this) {
            it?.getContent()?.let { syncDataStatus ->
                when (syncDataStatus) {
                    SyncEvents.Failed -> {
                        binding.lytHeader.tvHeaderStatus.gone()
                        binding.lytHeader.pbSync.gone()
                        resetSwipeLoadingAnim()
                    }

                    is SyncEvents.InProgress -> {
                        LOGS.d("Progress_____________ ${syncDataStatus.progress}")
                        binding.lytHeader.pbSync.max = syncDataStatus.total
                        binding.lytHeader.pbSync.progress = syncDataStatus.progress
                        binding.lytHeader.pbSync.visible()
                        binding.lytHeader.tvHeaderStatus.apply {
                            text = getString(R.string.text_syncing_dot)
                            visible()
                        }
                    }

                    is SyncEvents.Started -> {
                        binding.lytHeader.pbSync.max = syncDataStatus.total
                        binding.lytHeader.pbSync.progress = syncDataStatus.progress
                        binding.lytHeader.pbSync.visible()
                        binding.lytHeader.tvHeaderStatus.apply {
                            text = getString(R.string.text_syncing_dot)
                            visible()
                        }
                    }

                    is SyncEvents.Success -> {
                        binding.lytHeader.pbSync.max = syncDataStatus.total
                        binding.lytHeader.pbSync.progress = syncDataStatus.progress
                        binding.lytHeader.tvHeaderStatus.gone()
                        binding.lytHeader.pbSync.gone()
                        resetSwipeLoadingAnim()
                    }

                    SyncEvents.ServerSyncStarted -> {
                        binding.progressBar.root.visible()
                    }

                    SyncEvents.ServerSyncSuccess -> {
                        binding.progressBar.root.gone()

                        mainViewModel.reloadTodaysData()
                        //sendLogs()
                    }
                }
            }
        }*/

        viewModel.sessionManager.isRingCharging.observe(this) {
            if (viewModel.sessionManager.connectStateRing.value is ConnectState.ConnectSuccess) {
                setStateConnected((viewModel.sessionManager.connectStateRing.value as ConnectState.ConnectSuccess).noiseFitDevice)
            }

        }

        viewModel.sessionManager.connectStateRing.observe(this) { connectedState ->
            when (connectedState) {
                is ConnectState.ConnectFailed -> {
                    setConnectingState(true)
                    //binding.lytHeader.pbSync.gone()
                    //binding.lytHeader.tvHeaderStatus.gone()

                }

                is ConnectState.Connecting -> {
                    setConnectingState(true)
                    //binding.lytHeader.pbSync.gone()
                    //binding.lytHeader.tvHeaderStatus.gone()

                }

                is ConnectState.ConnectSuccess -> {
                    setConnectingState(false)
                    setStateConnected(connectedState.noiseFitDevice)
                    viewModel.checkBatteryPercentage()
                    shouldSync()
                    mainViewModel.onRingConnected()

                    //condition to be called once only
                    mainViewModel.checkOnGoingWorkout()

                }

                is ConnectState.UnPaired -> {
                    binding.lytHeader.ivRingUpdate.gone()
                    viewModel.handleUnPairState()
                    viewModel.updateDeviceConnectedStatus()
                    //binding.lytHeader.pbSync.gone()
                }

                else -> {}
            }

        }


        /* viewModel.sessionManager.showSyncOfflineData.observe(this) {
             it?.getContent()?.let { userActivity ->
                 if (userActivity == HealthOverviewDataType.SERVER_SYNC_SUCCESS) {
                     viewModel.getDashboardDataFromServer(true)
                 }

             }
         }*/
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }


    }


    private fun stateBluetoothOff() {
        binding.lytHeader.batteryStatus.gone()
        binding.lytHeader.lottieAnimView.gone()
        binding.lytHeader.oreoStatus.visible()
        mainViewModel.syncTextState.value = null
        //binding.lytHeader.tvHeaderStatus.gone()

        binding.lytHeader.oreoStatus.setImageResource(
            R.drawable.ic_ring_bluetooth_off
        )
        binding.lytHeader.oreoStatus.setBackgroundResource(R.drawable.back_modal_new_round)

    }

    private fun setConnectingState(connecting: Boolean) {
        binding.lytHeader.batteryStatus.isIndeterminate = connecting

        if (viewModel.sessionManager.bluetoothStateDash.value == false) {
            ApplicationUtils.stopOreSyncScheduler(requireContext())
            stateBluetoothOff()
            //binding.lytHeader.pbSync.gone()
        } else {

            binding.lytHeader.batteryStatus.invisible()
            binding.lytHeader.lottieAnimView.visible()
            binding.lytHeader.oreoStatus.visible()
            binding.lytHeader.oreoStatus.setImageResource(
                R.drawable.ic_ring_default_silver_new
            )
        }

        nullableBinding?.lytHeader?.ivRingUpdate?.visibility =
            if (mainViewModel.ringDataStore.isNewOtaAvailable()) {
                View.VISIBLE
            } else {
                View.GONE
            }


    }

    private fun setStateConnected(noiseFitDevice: ColorFitDevice) {
        LOGS.d("SummaryFragment setStateConnected()")
        binding.lytHeader.batteryStatus.visible()
        binding.lytHeader.ivExclamation.gone()
        binding.lytHeader.lottieAnimView.gone()
        binding.lytHeader.oreoStatus.visible()


        val batteryPercentage = viewModel.watchDataStore.getBatteryPercentRing()
        binding.lytHeader.batteryStatus.progress = batteryPercentage
        if (batteryPercentage <= 20) {
            binding.lytHeader.oreoStatus.setImageResource(
                R.drawable.ic_ring_low_battery
            )
            binding.lytHeader.batteryStatus.setIndicatorColor(resources.getColor(R.color.color_error))
            viewModel.handleBatteryAlert(noiseFitDevice)
        } else {
            binding.lytHeader.oreoStatus.setBackgroundResource(R.drawable.back_modal_new_round)
            binding.lytHeader.batteryStatus.setIndicatorColor(resources.getColor(R.color.white))
        }
        if (viewModel.sessionManager.isRingCharging.value == true) {
            binding.lytHeader.oreoStatus.setImageResource(
                R.drawable.ic_ring_charging
            )
        } else {
            binding.lytHeader.oreoStatus.setImageResource(
                R.drawable.ic_ring_default_silver_new
            )
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

    override fun onResume() {
        super.onResume()

        if (viewModel.isDeviceConnected()) {
            shouldSync()
        }
        mainViewModel.shouldResetMasterDates()
        nullableBinding?.lytHeader?.ivAppUpdate?.visibility =
            if (mainViewModel.localDataStore.isNewAppVersionAvailable()) {
                View.VISIBLE
            } else {
                View.GONE
            }

//        viewModel.getRecentWorkoutList()
    }


    private fun logFirebaseAppEvent(eventName: String, params: HashMap<String, Any>) {
        viewModel.sessionManager.logFirebaseEvent(eventName, params)
    }

    override fun onPositionSelected(position: Int, chartModel: ChartModel?) {
        LOGS.w("moveToPosition onPositionSelected ${chartModel?.date}")
        if (mainViewModel.selectedDate == chartModel?.date!!) {
            return
        }
        mainViewModel.selectedDate = chartModel.date!!
        mainViewModel.handleAddWorkoutVisibility()

        val returnDate = mainViewModel.updateSelectedDate(mainViewModel.selectedDate)
        if (returnDate != null) {
            mainViewModel.selectedDate = returnDate
        }

        val pos = pagerAdapter?.getPositionForDate(mainViewModel.selectedDate)
        if (pos != null && pos != -1) {
            binding.viewPagerSummary.setCurrentItem(pos, false)
        }

        if (mainViewModel.shouldLoadMoreData()) {
            LOGS.w("Loading more data")
        }
    }

    override fun onScrolling(position: Int, chartModel: ChartModel?) {

    }

}