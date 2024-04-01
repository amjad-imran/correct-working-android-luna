package com.oreo.ui.home.summary.paginate

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.CombinedData
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSummaryDataBinding
import com.noisefit.oreo.BottomNavOption
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.ServerUserHealthData
import com.oreo.ui.home.summary.OSummaryHealthOverviewAdapter
import com.oreo.ui.home.summary.OSummaryHealthOverviewClickEnum
import com.oreo.ui.home.summary.OreoRWorkoutAdapter
import com.oreo.ui.sleep.scoredetails.ClickViewType
import com.oreo.ui.sleep.scoredetails.SharedOSCDViewModel
import com.oreo.ui.sleep.scoredetails.ViewItemClickType
import com.oreo.util.graph.OCombineChartUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SummaryDataFragment :
    BaseFragment<FragmentSummaryDataBinding>(FragmentSummaryDataBinding::inflate) {

    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val viewModel: SummaryDataViewModel by viewModels()
    private val mSharedViewModel: SharedOSCDViewModel by activityViewModels()

    private val ARGS_DATE = "ARGS_DATE"

    private val TAG = "SummaryDataFragment"


    companion object {

        @JvmStatic
        fun newInstance(date: String) = SummaryDataFragment().apply {
            arguments = Bundle().apply {
                putString(ARGS_DATE, date)
            }
        }
    }


    private val healthOverviewAdapter by lazy {
        OSummaryHealthOverviewAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setAdapter()

        val date = arguments?.getString("ARGS_DATE")
        viewModel.date = date
        viewModel.user = mainViewModel.user
        LOGS.d("CREATED_WITH_DATE $date")
        loadData()


    }

    override fun onResume() {
        super.onResume()

        mainViewModel.dataReload.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                LOGS.d(TAG, "data reload")
                loadData()
            }
        }
    }

    private fun loadData() {
        viewModel.date?.let {
            mainViewModel.getDashBoardData(it)?.let { dash ->
                setUi(dash.first)
            }
        }
    }

    private fun setUi(data: ServerUserHealthData) {
        viewModel.parseHealthData(data)
    }


    private fun setAdapter() {
        binding.rvHealthData.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = healthOverviewAdapter
        }


        healthOverviewAdapter.itemClickListener = { type ->
            when (type) {

                is OSummaryHealthOverviewClickEnum.WorkoutAlertWhatisThis -> {}

                is OSummaryHealthOverviewClickEnum.WorkoutAlertIdentify -> {}

                is OSummaryHealthOverviewClickEnum.AutoSportsDelete -> {
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
                }

                is OSummaryHealthOverviewClickEnum.TextRingCareClicked -> {

                }

                OSummaryHealthOverviewClickEnum.TextWelcomeRingClicked -> {
                }

                is OSummaryHealthOverviewClickEnum.OnNapClicked -> {
                    navigate(R.id.napDetails, bundleOf("napId" to type.napId))
                }
            }
        }

    }


    override fun initListener() {

        binding.lytHeartRate.bInfo.setOnClickListener {
            viewModel.getContributorInfo("hr")
        }

    }

    override fun subscribeObservers() {

        viewModel.hrInfo.observe(this) {
            it.getContent()?.let {
                navigate(R.id.bottomSheetDataMetrics, Bundle().apply {
                    this.putString("infoData", it)
                })
            }
        }

        viewModel.activityScoreInfo.observe(this) {
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

        viewModel.readinessScoreInfo.observe(this) {
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
        viewModel.sleepScoreInfo.observe(this) {
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


        viewModel.healthOverviewData.observe(viewLifecycleOwner) {
            healthOverviewAdapter.items = it
            healthOverviewAdapter.refreshPosition = null
        }


        viewModel.stateHeartRateCard.observe(viewLifecycleOwner) {
            if (it != null) {
                setHearRateCardUi(it)
            }
        }

        viewModel.stateWorkouts.observe(this) {
            setWorkoutUI(it)
        }
    }


    private fun setWorkoutUI(workouts: List<OActivityListModal>?) {
        val lytWorkouts = binding.lytWorkouts
        lytWorkouts.root.visible()


        lytWorkouts.textView66.text = "Workouts"
        if (workouts.isNullOrEmpty()) {
            lytWorkouts.tvEmptyMsg.text =
                getString(R.string.text_you_haven_t_added_any_workouts_for_this_day)
            lytWorkouts.tvEmptyMsg.visible()
        } else {
            lytWorkouts.tvEmptyMsg.gone()
        }

        lytWorkouts.viewAddWorkout.gone()

        lytWorkouts.rvWorkouts.layoutManager = LinearLayoutManager(
            lytWorkouts.rvWorkouts.context, LinearLayoutManager.VERTICAL, false
        )
        val adapter1 = OreoRWorkoutAdapter(object : OreoRWorkoutAdapter.OnItemClickListener {
            override fun onItemClick(data: OActivityListModal, position: Int) {
                navigate(R.id.oWorkoutDetailsFragmentV2, Bundle().apply {
                    putString("workoutId", data.id ?: "")
                    putInt("position", position)
                })
            }
        })


        lytWorkouts.rvWorkouts.apply {
            adapter = adapter1
        }
        adapter1.setData(workouts ?: ArrayList())


        lytWorkouts.root.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_homepage_workouts_entry_click)
            navigate(R.id.oActivityListFragment)
        }

    }

    private fun setHearRateCardUi(data: OHealthOverview.HeartRate) {
        val lytHeartRate = binding.lytHeartRate
        lytHeartRate.root.visible()
        val chart = lytHeartRate.candleChart

        OCombineChartUtils.setChart(chart, data.xLabelList, data.axisMinimum, data.average)

        val combinedData = CombinedData()

        lytHeartRate.lottieAnimView.gone()
        lytHeartRate.imvHrMeasure.gone()

        lytHeartRate.groupValue.gone()
        lytHeartRate.tvEmptyConnect.gone()
        lytHeartRate.tvHeartValue.gone()

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
                    data.candleValue, R.color.o_heart_bg
                )
            )
            chart.data = combinedData
            chart.invalidate()
        }

    }
}