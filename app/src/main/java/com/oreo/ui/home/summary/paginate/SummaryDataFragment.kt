package com.oreo.ui.home.summary.paginate

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.CombinedData
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSummaryDataBinding
import com.noisefit.oreo.BottomNavOption
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.receiver.service.FeedbackSubmitService
import com.noisefit.ui.common.bottomSheet.DELETE_REQ_REQUEST_KEY
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.FirebaseLunaAppEvents
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.AlertType
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.TapMeasureState
import com.oreo.data.model.health.ODashboardActivityScoreModel
import com.oreo.data.model.health.ODashboardReadinessScoreModel
import com.oreo.data.model.health.ODashboardSleepScoreModel
import com.oreo.data.model.health.OreoDashboardResponseModel
import com.oreo.ui.home.summary.AlertClickListener
import com.oreo.ui.home.summary.HomeRecyclerViewHolder
import com.oreo.ui.home.summary.OSummaryHealthOverviewAdapter
import com.oreo.ui.home.summary.OSummaryHealthOverviewClickEnum
import com.oreo.ui.home.summary.OreoRWorkoutAdapter
import com.oreo.util.graph.OCombineChartUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SummaryDataFragment :
    BaseFragment<FragmentSummaryDataBinding>(FragmentSummaryDataBinding::inflate) {

    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val viewModel: SummaryDataViewModel by viewModels()
    private val ARGS_DATE = "ARGS_DATE"


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

        date?.let {
            mainViewModel.getDashBoardData(it)?.let { dash ->
                setUi(dash)
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
                    mainViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_HOMEPAGE_ACTIVITY_CLICK)
                }

                OSummaryHealthOverviewClickEnum.ReadinessDetailsWorkoutClick -> {
                    mainViewModel.navigateTo(BottomNavOption.READINESS)
                    mainViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_HOMEPAGE_READINESS_CLICK)
                }

                OSummaryHealthOverviewClickEnum.SleepDetailsWorkoutClick -> {
                    mainViewModel.navigateTo(BottomNavOption.SLEEP)
                    mainViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_HOMEPAGE_SLEEP_CLICK)
                }

                is OSummaryHealthOverviewClickEnum.VideoInfoClicked -> {
                }

                is OSummaryHealthOverviewClickEnum.TextRingCareClicked -> {

                }

                OSummaryHealthOverviewClickEnum.TextWelcomeRingClicked -> {
                }
            }
        }

    }


    override fun initListener() {

        binding.lytHeartRate.bInfo.setOnClickListener {
            /*viewModel.getContributorInfo("hr")*/
        }
    }

    override fun subscribeObservers() {


        viewModel.healthOverviewData.observe(viewLifecycleOwner) {
            healthOverviewAdapter.items = it
            healthOverviewAdapter.refreshPosition = null
        }


        viewModel.stateHeartRateCard.observe(viewLifecycleOwner) {
            if (it != null) {
                setHearRateCardUi(it)
            }
        }
    }


    private fun setWorkoutUI(workouts: List<OActivityListModal>?) {
        val lytWorkouts = binding.lytWorkouts

        lytWorkouts.tvEmptyMsg.gone()
        lytWorkouts.rvWorkouts.layoutManager = LinearLayoutManager(
            lytWorkouts.rvWorkouts.context, LinearLayoutManager.VERTICAL, false
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


        lytWorkouts.rvWorkouts.apply {
            adapter = adapter1
        }
        adapter1.setData(workouts ?: ArrayList())

        lytWorkouts.viewAddWorkout.gone()

        lytWorkouts.ivViewAll.setOnClickListener {
            viewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_HOMEPAGE_WORKOUTS_ENTRY_CLICK)
            navigate(R.id.oActivityListFragment)
        }

    }

    private fun setHearRateCardUi(data: OHealthOverview.HeartRate) {
        val lytHeartRate = binding.lytHeartRate
        lytHeartRate.root.visible()
        val chart = lytHeartRate.candleChart

        OCombineChartUtils.setChart(chart, data.xLabelList, data.axisMinimum, data.average)

        val combinedData = CombinedData()

        lytHeartRate.lottieAnimView.invisible()
        lytHeartRate.imvHrMeasure.invisible()

        lytHeartRate.groupValue.invisible()
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