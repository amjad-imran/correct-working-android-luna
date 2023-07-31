package com.oreo.ui.home.summary

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.BottomNavOption
import com.noisefit.BuildConfig
import com.noisefit.R
import com.noisefit.databinding.FragmentSummaryOBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.receiver.workManager.HealthOverviewDataType
import com.noisefit.ui.common.bottomSheet.ALERT_REQUEST_KEY
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.constants.EventConstants
import com.noisefit_commans.data.enums.Device
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.dkzwm.widget.srl.RefreshingListenerAdapter


@AndroidEntryPoint
class OSummaryFragment : BaseFragment<FragmentSummaryOBinding>(FragmentSummaryOBinding::inflate) {

    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val viewModel: OSummaryViewModel by viewModels()
    private val healthOverviewAdapter by lazy {
        OSummaryHealthOverviewAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        viewModel.initData()
    }

    override fun initListener() {
        binding.lytHeader.profileView1.setOnClickListener {
            navigate(R.id.OMyProfileFragment)
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

                syncData()
            }
        })

        binding.lytHeader.profileView1.setOnLongClickListener {
            if (BuildConfig.DEBUG) {
                navigate(R.id.logsDisplayFragment)
            }
            return@setOnLongClickListener true
        }

    }

    fun resetSwipeLoadingAnim() {
        binding.layoutRefresh.textSyncingData.gone()
        binding.swipeToRefresh.refreshComplete()
    }

    private fun setAdapter() {
        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = healthOverviewAdapter
        }

        healthOverviewAdapter.itemClickListener = { type ->
            when (type) {
                is OSummaryHealthOverviewClickEnum.MeasureHRClick -> {
                    viewModel.measureHr(true)
                }

                is OSummaryHealthOverviewClickEnum.AddWorkoutClick -> {
                    navigate(R.id.addWorkoutFragment)
                }

                is OSummaryHealthOverviewClickEnum.ItemWorkoutClick -> {
                    //will perform action later
                    navigate(R.id.oWorkoutDetailsFragment, Bundle().apply {
                        putString("workoutName", type.workOutName)
                        putString("workoutId", type.id)
                    })
                }

                is OSummaryHealthOverviewClickEnum.WorkoutAlertWhatisThis -> {
                    setFragmentResultListener(ALERT_REQUEST_KEY) { _, bundle ->
                        val allow = bundle.getBoolean("allow")
                        if (allow) {

                        }
                    }
                    navigate(
                        OSummaryFragmentDirections.actionHomeToAlertTextBottomSheet(
                            getString(R.string.text_automatic_activity_detection),
                            getString(R.string.text_automatic_activity_detection_desc),
                            "", ""
                        )
                    )
                }

                is OSummaryHealthOverviewClickEnum.WorkoutAlertIdentify -> {
                    navigate(R.id.detectWorkoutListFragment)
                }

                is OSummaryHealthOverviewClickEnum.WorkoutAlertDismiss -> {
                    setFragmentResultListener(ALERT_REQUEST_KEY) { _, bundle ->
                        val allow = bundle.getBoolean("allow")
                        if (allow) {

                        }
                    }
                    navigate(
                        OSummaryFragmentDirections.actionHomeToAlertTextBottomSheet(
                            getString(R.string.text_dismiss_activity_title),
                            getString(R.string.text_dismiss_activity_desc),
                            "", ""
                        )
                    )
                }

                is OSummaryHealthOverviewClickEnum.ViewAllWorkoutClick -> {
                    navigate(R.id.oActivityListFragment)
                }

                is OSummaryHealthOverviewClickEnum.PairDeviceClicked -> {
                    startActivity(PairDeviceActivity.getStartIntent(requireContext(), true))
                }

                OSummaryHealthOverviewClickEnum.ActivityDetailsWorkoutClick -> {
                    mainViewModel.navigateTo(BottomNavOption.COMMUNITY)
                }

                OSummaryHealthOverviewClickEnum.ReadinessDetailsWorkoutClick -> {
                    mainViewModel.navigateTo(BottomNavOption.SHOP)
                }

                OSummaryHealthOverviewClickEnum.SleepDetailsWorkoutClick -> {
                    mainViewModel.navigateTo(BottomNavOption.EXPLORE)
                }
            }
        }

    }

    private fun shouldSync() {
        val lastSyncTime = viewModel.sessionManager.getLastSyncTime(Device.RING) ?: 0L
        LOGS.d("shouldSync $lastSyncTime -- ${DateFormats.getTimeStamp()}")
        if (kotlin.math.abs(DateFormats.getTimeStamp() - lastSyncTime) > 300000L) {
            syncData()
        }
    }

    override fun subscribeObservers() {

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

        viewModel.sessionManager.connectStateRing.observe(this) { connectedState ->
            when (connectedState) {
                is ConnectState.ConnectFailed -> {

                }

                is ConnectState.Connecting -> {
                    setConnectingState(true)
                }

                is ConnectState.ConnectSuccess -> {
                    setConnectingState(false)
                    shouldSync()
                    setStateConnected(connectedState.noiseFitDevice)
                    viewModel.checkBatteryPercentage()
                }

                is ConnectState.UnPaired -> {
                    viewModel.handleUnPairState()
                }

                is ConnectState.Hibernate -> {

                }

                else -> {}
            }

        }

        viewModel.summary.healthOverviewData.observe(this) {
            it?.let {
                healthOverviewAdapter.refreshPosition = viewModel.summary.refreshPosition
                healthOverviewAdapter.items = it
                healthOverviewAdapter.refreshPosition = null
            }
        }

        viewModel.sessionManager.showSyncOfflineData.observe(this) {
            it?.getContent()?.let { userActivity ->
                if (userActivity == HealthOverviewDataType.SERVER_SYNC_SUCCESS) {
                    viewModel.getDashboardDataFromServer(true, false)
                }

            }
        }
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
        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            it.getContent()?.let { callback ->
                if (callback is UpdateDeviceDataCallback.ManualMeasurementObtained) {
                    viewModel.updateManualValue(callback.manualMeasurement)
                }
            }
        }


    }

    private fun setConnectingState(connecting: Boolean) {
        binding.lytHeader.batteryStatus.isIndeterminate = connecting
    }

    private fun setStateConnected(noiseFitDevice: ColorFitDevice) {
        val batteryPercentage = viewModel.watchDataStore.getBatteryPercentRing()
        binding.lytHeader.batteryStatus.progress = batteryPercentage
        if (batteryPercentage < 20) {
            binding.lytHeader.oreoStatus.setBackgroundResource(R.drawable.back_modal_red_circle)
            binding.lytHeader.batteryStatus.setIndicatorColor(resources.getColor(R.color.color_error))

        } else {
            binding.lytHeader.oreoStatus.setBackgroundResource(R.drawable.back_modal_new_round)
            binding.lytHeader.batteryStatus.setIndicatorColor(resources.getColor(R.color.white))
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
//        viewModel.getRecentWorkoutList()
    }


}