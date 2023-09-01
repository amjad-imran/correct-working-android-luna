package com.oreo.ui.home.summary

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.github.mikephil.charting.data.CombinedData
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSummaryOBinding
import com.noisefit.oreo.BottomNavOption
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.common.bottomSheet.ALERT_REQUEST_KEY
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit.util.ApplicationUtils
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
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.AlertType
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.TapMeasureState
import com.oreo.data.model.health.ODashboardActivityScoreModel
import com.oreo.data.model.health.ODashboardReadinessScoreModel
import com.oreo.data.model.health.ODashboardSleepScoreModel
import com.oreo.receiver.workManager.HealthOverviewDataType
import com.oreo.ui.workout.add.ADD_WORKOUT_REQUEST_KEY
import com.oreo.util.graph.OCombineChartUtils
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

    }

    override fun initListener() {

        binding.contentMain.lytPairDevice.btnPairDevice.setOnClickListener {
            startActivity(PairDeviceActivity.getStartIntent(requireContext(), true))
        }

        binding.lytHeader.batteryStatus.setOnClickListener {
            mainViewModel.navigateTo(BottomNavOption.MY_DEVICE)
        }


        binding.lytHeader.profileView1.setOnClickListener {
            navigate(R.id.OMyProfileFragment)
        }

        setFragmentResultListener(ADD_WORKOUT_REQUEST_KEY) { _, bundle ->
            val allow = bundle.getBoolean("allow")

            if (allow) {
                viewModel.getRecentWorkoutList()

            }
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

                if (viewModel.stateHeartRateCard.value?.measureState == TapMeasureState.MEASURING) {
                    binding.swipeToRefresh.refreshComplete()
                    return
                }

                binding.layoutRefresh.textSyncingData.visible()
                binding.swipeToRefresh.refreshComplete()

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
        binding.contentMain.rvHealthData.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = healthOverviewAdapter
        }

        healthOverviewAdapter.itemClickListener = { type ->
            when (type) {

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

                is OSummaryHealthOverviewClickEnum.AutoSportsDelete -> {
                    setFragmentResultListener(ALERT_REQUEST_KEY) { _, bundle ->
                        val allow = bundle.getBoolean("allow")
                        if (allow) {
                            viewModel.deleteAllAutoWorkout()
                            viewModel.removeAutoWorkoutCard()
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


                OSummaryHealthOverviewClickEnum.ActivityDetailsWorkoutClick -> {
                    mainViewModel.navigateTo(BottomNavOption.ACTIVITY)
                }

                OSummaryHealthOverviewClickEnum.ReadinessDetailsWorkoutClick -> {
                    mainViewModel.navigateTo(BottomNavOption.READINESS)
                }

                OSummaryHealthOverviewClickEnum.SleepDetailsWorkoutClick -> {
                    mainViewModel.navigateTo(BottomNavOption.SLEEP)
                }
            }
        }

    }

    private fun shouldSync() {
        val lastSyncTime = viewModel.sessionManager.getLastSyncTime() ?: 0L
        LOGS.d("shouldSync $lastSyncTime -- ${DateFormats.getTimeStamp()}")
        if (kotlin.math.abs(DateFormats.getTimeStamp() - lastSyncTime) > 300000L) {
            binding.lytHeader.tvHeaderStatus.apply {
                text = context.getString(R.string.text_updating_dot)
                visible()
            }
            syncData()
        }
    }

    override fun subscribeObservers() {

        viewModel.stateHeaderCard.observe(this) {
            binding.contentMain.lytHeader.apply {
                this.tvDate.text =
                    it.second
                this.tvGreeting.text = it.first
            }
        }

        viewModel.stateHeartRateCard.observe(this) {
            if (it != null) {
                setHearRateCardUi(it)
            }
        }
        viewModel.statePairDeviceCard.observe(this) {
            binding.contentMain.lytPairDevice.apply {
                if (it) {
                    this.root.visible()
                    this.root.setOnClickListener {
                        startActivity(PairDeviceActivity.getStartIntent(requireContext(), true))
                    }

                } else {
                    this.root.gone()
                }
            }
        }

        viewModel.stateDashAlerts.observe(this) {

            if (it.isNullOrEmpty()) {
                binding.contentMain.lytAlerts.root.gone()
                return@observe
            }
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

        viewModel.stateWorkouts.observe(this) {
            setWorkoutUI(it)

        }

        viewModel.stateSleepAvgCard.observe(this) {

            if (it == null) {
                binding.contentMain.lytSleepAvg.root.gone()
                return@observe
            }

            if (it.first != null && it.second != null) {
                binding.contentMain.lytSleepAvg.root.visible()

                updateSleepAvgUi(it)

            } else {
                binding.contentMain.lytSleepAvg.root.gone()
            }
        }
        viewModel.stateReadinessAvgCard.observe(this) {

            if (it == null) {
                binding.contentMain.lytReadinessAvg.root.gone()
                return@observe
            }

            binding.contentMain.lytReadinessAvg.root.visible()

            updateReadinessAvgUi(it)

        }



        viewModel.sessionManager.bluetoothStateDash.observe(this) {
            viewModel.updateBluetoothStateInList(it)
        }


        viewModel.deviceConnected.observe(this) { connected ->
            if (!connected) {
                binding.lytHeader.ivExclamation.visible()
                binding.lytHeader.batteryStatus.invisible()
                binding.lytHeader.lottieAnimView.gone()
                binding.lytHeader.oreoStatus.visible()
                binding.lytHeader.oreoStatus.loadImage(
                    requireContext(),
                    R.drawable.ic_ring_default_silver
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
                            text = getString(R.string.text_updating_dot)
                            visible()
                        }
                    }

                    is SyncEvents.Started -> {
                        binding.lytHeader.pbSync.max = syncDataStatus.total
                        binding.lytHeader.pbSync.progress = syncDataStatus.progress
                        binding.lytHeader.pbSync.visible()
                        binding.lytHeader.tvHeaderStatus.apply {
                            text = getString(R.string.text_updating_dot)
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
                }
            }
        }

        viewModel.sessionManager.connectStateRing.observe(this) { connectedState ->
            when (connectedState) {
                is ConnectState.ConnectFailed -> {
                    setConnectingState(true)
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
                    viewModel.updateDeviceConnectedStatus()
                }

                else -> {}
            }

        }

        viewModel.summary.healthOverviewData.observe(this) {
            it?.let {

                binding.contentMain.root.visible()

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
        viewModel.sessionManager.manualMeasurementValue.observe(this) {
            it.getContent()?.let {
                if (it) {
                    viewModel.updateManualValue()
                }

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
            lytReadinessAvg.tvAvgThisWeek.gone()
            lytReadinessAvg.tvDaysAvg.visible()
            lytReadinessAvg.tvSleepScore.visible()
            lytReadinessAvg.tvEmpty.gone()
            lytReadinessAvg.lineChart.visible()
            val trendValue = "${kotlin.math.abs(data.trend ?: 0)}%"
            if (data.trend != null && data.trend > 0) {
                lytReadinessAvg.sleepTrendValue.text = trendValue
                lytReadinessAvg.sleepTrendValue.setTextColor(Color.parseColor("#29cc74"))
                lytReadinessAvg.sleepTrendImv.loadImage(
                    lytReadinessAvg.sleepTrendImv.context,
                    R.drawable.ic_trend_up
                )
                lytReadinessAvg.sleepTrendImv.visible()
                lytReadinessAvg.sleepTrendValue.visible()
                lytReadinessAvg.tvSleepFromLast.visible()
            } else if (data.trend != null && data.trend < 0) {
                lytReadinessAvg.sleepTrendValue.text = trendValue
                lytReadinessAvg.sleepTrendImv.loadImage(
                    lytReadinessAvg.sleepTrendImv.context,
                    R.drawable.ic_trend_down
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
                viewModel.convertIntToChartModel(data.value),
                ArrayList(),
                ArrayList(),
                20,
                true
            )
        } else {

            lytReadinessAvg.tvAvgThisWeek.visible()
            lytReadinessAvg.tvSleepScore.text = "--"
            lytReadinessAvg.tvSleepScore.gone()
            lytReadinessAvg.tvEmpty.visible()
            lytReadinessAvg.tvDaysAvg.gone()
            lytReadinessAvg.lineChart.gone()
            lytReadinessAvg.sleepTrendImv.gone()
            lytReadinessAvg.sleepTrendValue.gone()
            lytReadinessAvg.tvSleepFromLast.gone()
        }
    }

    private fun updateSleepAvgUi(data: Pair<ODashboardSleepScoreModel?, ODashboardActivityScoreModel?>) {
        val lytSleepAvg = binding.contentMain.lytSleepAvg

        val sleep = data.first!!
        val activity = data.second!!

        if (sleep.sleepScore != null && sleep.sleepScore >= 0) {
            lytSleepAvg.tvSleepScore.text = sleep.sleepScore.toString()
            lytSleepAvg.tvAvgThisWeek.gone()
            lytSleepAvg.tvDaysAvg.visible()
            lytSleepAvg.sleepLineChart.visible()
            lytSleepAvg.sleepLine.root.visible()
            val trendValue = "${kotlin.math.abs(sleep.trend ?: 0)}%"
            if (sleep.trend != null && sleep.trend > 0) {
                lytSleepAvg.sleepTrendValue.text = trendValue
                lytSleepAvg.sleepTrendValue.setTextColor(Color.parseColor("#29cc74"))
                lytSleepAvg.sleepTrendImv.loadImage(
                    lytSleepAvg.sleepTrendImv.context,
                    R.drawable.ic_trend_up
                )
                lytSleepAvg.sleepTrendImv.visible()
                lytSleepAvg.sleepTrendValue.visible()
                lytSleepAvg.tvSleepFromLast.visible()
            } else if (sleep.trend != null && sleep.trend < 0) {
                lytSleepAvg.sleepTrendValue.text = trendValue
                lytSleepAvg.sleepTrendImv.loadImage(
                    lytSleepAvg.sleepTrendImv.context,
                    R.drawable.ic_trend_down
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
                viewModel.convertIntToChartModel(sleep.value),
                ArrayList(),
                ArrayList(),
                20,
                true
            )
        } else {

            lytSleepAvg.tvAvgThisWeek.visible()
            lytSleepAvg.tvSleepScore.text = "--"

            lytSleepAvg.tvDaysAvg.gone()
            lytSleepAvg.sleepLineChart.gone()
            lytSleepAvg.sleepLine.root.gone()
            lytSleepAvg.sleepTrendImv.gone()
            lytSleepAvg.sleepTrendValue.gone()
            lytSleepAvg.tvSleepFromLast.gone()
        }

        if (activity.activityScore != null && activity.activityScore >= 0) {

            lytSleepAvg.tvActivityScore.text = activity.activityScore.toString()
            lytSleepAvg.activityLineChart.updateDataWithMaxMin(
                viewModel.convertIntToChartModel(activity.value),
                ArrayList(),
                ArrayList(),
                20,
                true
            )
            lytSleepAvg.tvActAvgThisWeek.gone()
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
                    lytSleepAvg.sleepTrendImv.context,
                    R.drawable.ic_trend_up
                )
                lytSleepAvg.activityTrendImv.visible()
                lytSleepAvg.activityTrendValue.visible()
                lytSleepAvg.tvActivityFrom.visible()
            } else if (activity.trend != null && activity.trend < 0) {
                lytSleepAvg.activityTrendValue.text = trendValue
                lytSleepAvg.activityTrendImv.loadImage(
                    lytSleepAvg.sleepTrendImv.context,
                    R.drawable.ic_trend_down
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


            lytSleepAvg.tvActAvgThisWeek.visible()
            lytSleepAvg.tvDaysAvg1.gone()
            lytSleepAvg.activityLineChart.gone()
            lytSleepAvg.activityLine.root.gone()
            lytSleepAvg.activityTrendImv.gone()
            lytSleepAvg.activityTrendValue.gone()
            lytSleepAvg.tvActivityFrom.gone()
        }


        binding.root.setOnClickListener {
            //   itemClickListener?.invoke(it, data, position)
        }
    }

    private fun setWorkoutUI(workouts: List<OActivityListModal>?) {
        val lytWorkouts = binding.contentMain.lytWorkouts

        lytWorkouts.tvEmptyMsg.gone()
        lytWorkouts.rvWorkouts.layoutManager =
            LinearLayoutManager(
                lytWorkouts.rvWorkouts.context,
                LinearLayoutManager.VERTICAL,
                false
            )
        val adapter1 = OreoRWorkoutAdapter(object : OreoRWorkoutAdapter.OnItemClickListener {
            override fun onItemClick(data: OActivityListModal, position: Int) {
                navigate(R.id.oWorkoutDetailsFragment, Bundle().apply {
                    putString("workoutName", data.getFormattedActivityName())
                    putString("workoutId", data.id ?: "")
                    putInt("position", position)
                })
            }
        })


        if (viewModel.ringDataStore.getRingDevice() != null) {
            lytWorkouts.viewAddWorkout.visible()
            lytWorkouts.root.visible()
        } else {
            lytWorkouts.viewAddWorkout.gone()
            if (workouts.isNullOrEmpty()) {
                lytWorkouts.root.gone()
            } else {
                lytWorkouts.root.visible()
            }
        }


        lytWorkouts.rvWorkouts.apply {
            adapter = adapter1
        }
        adapter1.setData(workouts ?: ArrayList())
        lytWorkouts.viewAddWorkout.setOnClickListener {
            navigate(R.id.addWorkoutFragment)
        }

        lytWorkouts.ivViewAll.setOnClickListener {
            navigate(R.id.oActivityListFragment)
        }

    }

    private fun setHearRateCardUi(data: OHealthOverview.HeartRate) {
        val lytHeartRate = binding.contentMain.lytHeartRate
        val chart = lytHeartRate.candleChart

        OCombineChartUtils.setChart(chart, data.xLabelList, data.axisMinimum, data.average)

        val combinedData = CombinedData()



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
                    text = "Measuring.."
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
        }
        if (data.lineData.first.isNotEmpty() && data.lineData.first.size > 1) {
            combinedData.setData(
                OCombineChartUtils.generateLineData(
                    data.lineData.first,
                    lytHeartRate.candleChart,
                    data.lineData.second,
                    data.axisMinimum
                )
            )
            combinedData.setData(
                OCombineChartUtils.generateCandleData(
                    data.candleValue,
                    R.color.o_heart_bg
                )
            )
            chart.data = combinedData
            chart.invalidate()
        }

        lytHeartRate.imvHrMeasure.setOnClickListener {

            if (data.measureState == TapMeasureState.MEASURING || data.measureState == TapMeasureState.NO_DEVICE) {
                return@setOnClickListener
            }

            if (viewModel.sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess) {
                return@setOnClickListener
            }

            viewModel.measureHr(true)
            return@setOnClickListener
        }
    }

    private fun stateBluetoothOff() {
        binding.lytHeader.batteryStatus.gone()
        binding.lytHeader.lottieAnimView.gone()
        binding.lytHeader.oreoStatus.visible()
        binding.lytHeader.tvHeaderStatus.gone()

        binding.lytHeader.oreoStatus.loadImage(
            requireContext(),
            R.drawable.ic_luna_state_bt_off
        )
        binding.lytHeader.oreoStatus.setBackgroundResource(R.drawable.back_modal_new_round)
    }

    private fun setConnectingState(connecting: Boolean) {
        binding.lytHeader.batteryStatus.isIndeterminate = connecting

        if (viewModel.sessionManager.bluetoothStateDash.value == false) {
            stateBluetoothOff()
        } else {

            binding.lytHeader.tvHeaderStatus.apply {
                text = context.getString(R.string.text_connecting_dot)
                visible()
            }

            binding.lytHeader.batteryStatus.invisible()
            binding.lytHeader.lottieAnimView.visible()
            binding.lytHeader.oreoStatus.visible()
        }

    }

    private fun setStateConnected(noiseFitDevice: ColorFitDevice) {
        binding.lytHeader.batteryStatus.visible()
        binding.lytHeader.ivExclamation.gone()
        binding.lytHeader.lottieAnimView.gone()
        binding.lytHeader.oreoStatus.visible()

        if (binding.lytHeader.tvHeaderStatus.text.equals(getString(R.string.text_connecting_dot))) {
            binding.lytHeader.tvHeaderStatus.gone()
        }

        val batteryPercentage = viewModel.watchDataStore.getBatteryPercentRing()
        binding.lytHeader.batteryStatus.progress = batteryPercentage
        if (batteryPercentage < 20) {
            binding.lytHeader.oreoStatus.setBackgroundResource(R.drawable.back_modal_red_circle)
            binding.lytHeader.batteryStatus.setIndicatorColor(resources.getColor(R.color.color_error))
        } else {
            binding.lytHeader.oreoStatus.setBackgroundResource(R.drawable.back_modal_new_round)
            binding.lytHeader.batteryStatus.setIndicatorColor(resources.getColor(R.color.white))
        }

        if (viewModel.sessionManager.isRingCharging.value == true) {
            binding.lytHeader.oreoStatus.loadImage(
                requireContext(),
                R.drawable.ic_luna_state_charge
            )
        } else {
            binding.lytHeader.oreoStatus.loadImage(
                requireContext(),
                R.drawable.ic_ring_default_silver
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
        viewModel.initData()

//        viewModel.getRecentWorkoutList()
    }


}