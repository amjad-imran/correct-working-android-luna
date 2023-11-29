package com.oreo.ui.home.summary

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSummaryOBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.receiver.service.FeedbackSubmitService
import com.noisefit.util.ApplicationUtils
import com.noisefit.util.notif.NotificationUtil
import com.noisefit_commans.constants.SyncEvents
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.FirebaseLunaAppEvents
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.TapMeasureState
import com.oreo.receiver.workManager.HealthOverviewDataType
import com.oreo.ui.home.summary.paginate.SummaryPagerAdapter
import com.oreo.ui.info.CALL_GOT_IT
import com.oreo.ui.sleep.scoredetails.ClickViewType
import com.oreo.ui.sleep.scoredetails.SharedOSCDViewModel
import com.oreo.ui.sleep.scoredetails.ViewItemClickType
import com.oreo.ui.workout.add.ADD_WORKOUT_REQUEST_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class OSummaryFragment : BaseFragment<FragmentSummaryOBinding>(FragmentSummaryOBinding::inflate) {

    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val viewModel: OSummaryViewModel by viewModels()

    private var pagerAdapter: SummaryPagerAdapter? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViewPager()

    }

    private fun setViewPager() {
        activity?.let {
            pagerAdapter = SummaryPagerAdapter(it)
            binding.viewPagerSummary.adapter = pagerAdapter
            binding.viewPagerSummary.offscreenPageLimit = 1
        }

        /* TabLayoutMediator(binding.tabLayout, binding.viewPagerSummary) { tab, position ->
             tab.text = pagerAdapter.getDate(position)
         }.attach()*/

        binding.viewPagerSummary.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                mainViewModel.selectedDate = pagerAdapter?.getDate(position)
                setTabDates(position)

                if (mainViewModel.shouldLoadMoreData()) {
                    LOGS.w("Loading more data")
                }
            }
        })
    }

    private fun setTabDates(position: Int) {
        var currentDayText = ""
        val centerDate = pagerAdapter?.getDate(position)
        if (centerDate.equals(DateFormats.getCurrentDate(DateFormats.dateFormat3))) {
            currentDayText = "Today, "
        }
        binding.tabLayout.tvSelectedDate.text = "$currentDayText${
            DateFormats.formatDate(
                centerDate,
                DateFormats.dateFormat3,
                DateFormats.dateFormat7
            )
        }"
        val leftDate = pagerAdapter?.getDate(position - 1)
        if (leftDate == null) {
            binding.tabLayout.tvDateLeft.gone()
        } else {
            binding.tabLayout.tvDateLeft.visible()
            binding.tabLayout.tvDateLeft.text = DateFormats.formatDate(
                leftDate,
                DateFormats.dateFormat3,
                DateFormats.dateFormat7
            )
        }
        val rightDate = pagerAdapter?.getDate(position + 1)
        if (rightDate == null) {
            binding.tabLayout.tvDateRight.gone()
        } else {
            var rightTodayText = ""
            if (rightDate.equals(DateFormats.getCurrentDate(DateFormats.dateFormat3))) {
                rightTodayText = "Today, "
            }
            binding.tabLayout.tvDateRight.visible()
            binding.tabLayout.tvDateRight.text = "$rightTodayText${
                DateFormats.formatDate(
                    rightDate,
                    DateFormats.dateFormat3,
                    DateFormats.dateFormat7
                )
            }"
        }
    }

    override fun initListener() {
        binding.lytHeader.oreoStatus.setOnClickListener {
            viewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_DEVICE_CAPSULE_CLICK)
            navigate(R.id.oreo_my_device)
        }

        binding.lytHeader.lottieAnimView.setOnClickListener {
            viewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_DEVICE_CAPSULE_CLICK)
            navigate(R.id.oreo_my_device)
        }



        binding.lytHeader.batteryStatus.setOnClickListener {
            viewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_DEVICE_CAPSULE_CLICK)
            navigate(R.id.oreo_my_device)
        }


        binding.lytHeader.profileView1.setOnClickListener {
            viewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_HAMBURGER_CLICK)
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

        binding.lytHeader.profileView1.setOnLongClickListener {
            if (BuildConfig.DEBUG) {
                navigate(R.id.logsDisplayFragment)
            }
            return@setOnLongClickListener true
        }


        //handle device intro
        if (!mainViewModel.ringDataStore.isShowDeviceIntro()) {
            setFragmentResultListener(CALL_GOT_IT) { _, bundle ->
                val isSelected = bundle.getBoolean("isSelected")
                if (isSelected) {
                    mainViewModel.ringDataStore.setShowDeviceIntro(true)
                }
            }
            navigate(R.id.myDeviceIntroBottomSheet)
        }

    }

    fun resetSwipeLoadingAnim() {
        /* binding.layoutRefresh.textSyncingData.gone()
         binding.swipeToRefresh.refreshComplete()*/
    }


    private fun shouldSync() {
        val lastSyncTime = viewModel.sessionManager.getLastSyncTime() ?: 0L
        LOGS.d("shouldSync $lastSyncTime -- ${DateFormats.getTimeStamp()}")

        val shouldSync = viewModel.sessionManager.forceSyncData.value?.getContent() ?: false

        if (shouldSync || kotlin.math.abs(DateFormats.getTimeStamp() - lastSyncTime) > 2 * 60 * 60 * 1000L) {
            binding.lytHeader.tvHeaderStatus.apply {
                text = context.getString(R.string.text_syncing_dot)
                visible()
            }
            syncData()
        }
    }


    override fun subscribeObservers() {
        mainViewModel.dashboard.observe(viewLifecycleOwner) {
            LOGS.w("Setting_data size ${it.size}")
            pagerAdapter?.setDataSet(it)

            val pos = pagerAdapter?.getPositionForDate(mainViewModel.selectedDate) ?: (it.size - 1)

            binding.viewPagerSummary.setCurrentItem(pos, false)
            setTabDates(pos)

        }






        viewModel.sessionManager.showSyncOfflineData.observe(viewLifecycleOwner) {
            it.getContent()?.let { event ->
                if (event == HealthOverviewDataType.AUTO_WORKOUT) {
                    //viewModel.getDashboardDataFromServer(false)
                }
            }

        }



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
                binding.lytHeader.oreoStatus.loadImage(
                    requireContext(),
                    R.drawable.ic_ring_not_connected
                )
            }
        }

        viewModel.sessionManager.syncCompleted.observe(this) {
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

                        mainViewModel.onSyncSuccess()
                        //sendLogs()
                    }
                }
            }
        }

        viewModel.sessionManager.isRingCharging.observe(this) {
            if (viewModel.sessionManager.connectStateRing.value is ConnectState.ConnectSuccess) {
                setStateConnected((viewModel.sessionManager.connectStateRing.value as ConnectState.ConnectSuccess).noiseFitDevice)
            }

        }

        viewModel.sessionManager.connectStateRing.observe(this) { connectedState ->
            when (connectedState) {
                is ConnectState.ConnectFailed -> {
                    setConnectingState(true)
                    viewModel.updateAlerts()
                }

                is ConnectState.Connecting -> {
                    setConnectingState(true)
                    viewModel.updateAlerts()

                }

                is ConnectState.ConnectSuccess -> {
                    setConnectingState(false)
                    setStateConnected(connectedState.noiseFitDevice)
                    viewModel.checkBatteryPercentage()
                    viewModel.updateAlerts()
                    shouldSync()
                    mainViewModel.onRingConnected()
                }

                is ConnectState.UnPaired -> {
                    viewModel.handleUnPairState()
                    viewModel.updateDeviceConnectedStatus()
                    viewModel.updateAlerts()
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
        mainViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.sessionManager.manualMeasurementValue.observe(this) {
            it.getContent()?.let {
                if (it) {
                    viewModel.updateManualValue()
                }

            }
        }


    }

    private fun sendLogs() {
        val shouldSendLogs = viewModel.shouldSendLogs()
        if (shouldSendLogs) {
            context?.let {
                FeedbackSubmitService.startService(
                    it
                )
            }
        }
    }


    private fun stateBluetoothOff() {
        binding.lytHeader.batteryStatus.gone()
        binding.lytHeader.lottieAnimView.gone()
        binding.lytHeader.oreoStatus.visible()
        binding.lytHeader.tvHeaderStatus.gone()

        binding.lytHeader.oreoStatus.loadImage(
            requireContext(),
            R.drawable.ic_ring_bluetooth_off
        )
        binding.lytHeader.oreoStatus.setBackgroundResource(R.drawable.back_modal_new_round)

        if (viewModel.stateHeartRateCard.value?.measureState == TapMeasureState.MEASURING) {
            viewModel.stateHeartRateCard.postValue(viewModel.stateHeartRateCard.value.apply {
                this?.measureState = TapMeasureState.ERROR
            })
        }
    }

    private fun setConnectingState(connecting: Boolean) {
        binding.lytHeader.batteryStatus.isIndeterminate = connecting

        if (viewModel.sessionManager.bluetoothStateDash.value == false) {
            stateBluetoothOff()
        } else {

            binding.lytHeader.batteryStatus.invisible()
            binding.lytHeader.lottieAnimView.visible()
            binding.lytHeader.oreoStatus.visible()
            binding.lytHeader.oreoStatus.loadImage(
                requireContext(),
                R.drawable.ic_ring_default_silver_new
            )
        }

    }

    private fun setStateConnected(noiseFitDevice: ColorFitDevice) {
        binding.lytHeader.batteryStatus.visible()
        binding.lytHeader.ivExclamation.gone()
        binding.lytHeader.lottieAnimView.gone()
        binding.lytHeader.oreoStatus.visible()


        val batteryPercentage = viewModel.watchDataStore.getBatteryPercentRing()
        binding.lytHeader.batteryStatus.progress = batteryPercentage
        if (batteryPercentage <= 20) {
            binding.lytHeader.oreoStatus.loadImage(
                requireContext(),
                R.drawable.ic_ring_low_battery
            )
            binding.lytHeader.batteryStatus.setIndicatorColor(resources.getColor(R.color.color_error))
            viewModel.handleBatteryAlert(noiseFitDevice)
        } else {
            binding.lytHeader.oreoStatus.setBackgroundResource(R.drawable.back_modal_new_round)
            binding.lytHeader.batteryStatus.setIndicatorColor(resources.getColor(R.color.white))
        }

        if (viewModel.sessionManager.isRingCharging.value == true) {
            binding.lytHeader.oreoStatus.loadImage(
                requireContext(),
                R.drawable.ic_ring_charging
            )
        } else {
            binding.lytHeader.oreoStatus.loadImage(
                requireContext(),
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

//        viewModel.getRecentWorkoutList()
    }


    private fun logFirebaseAppEvent(eventName: String, params: HashMap<String, Any>) {
        viewModel.sessionManager.logFirebaseEvent(eventName, params)
    }

}