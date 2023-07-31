package com.noisefit.ui.dashboard.healthOverview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hookedonplay.decoviewlib.events.DecoEvent
import com.noisefit.luna.R
import com.noisefit_commans.data.model.HealthOverview
import com.noisefit_commans.data.model.HealthOverviewData
import com.noisefit.luna.databinding.FragmentHealthOverviewBinding
import com.noisefit_commans.ui.BaseFragment

import com.noisefit_commans.ui.visible
import com.noisefit.ui.dashboard.summary.RING_ANIMATION
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.MarginItemDecoration
import com.noisefit_commans.data.enums.HealthOverViewHistoryType
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HealthOverviewFragment :
    BaseFragment<FragmentHealthOverviewBinding>(FragmentHealthOverviewBinding::inflate) {

    private val viewViewModel: HealthOverviewViewModel by viewModels()
    private val healthOverviewAdapter by lazy {
        HealthOverviewAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
    }

    private fun setAdapter() {

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = healthOverviewAdapter
            addItemDecoration(MarginItemDecoration(32))
        }
        healthOverviewAdapter.itemClickListener = { view, item, position ->
            when (item) {
                is HealthOverview.BloodOxygen -> {
                    logInsiderAppEvent(InsiderAppEvents.HEALTH_OVERVIEW_BLOOD_OXYGEN_CLICK)
                    navigate(R.id.bloodOxygenDetailsFragment)
                }
                is HealthOverview.Distance -> {
                    logInsiderAppEvent(InsiderAppEvents.HEALTH_OVERVIEW_DISTANCE_CLICK)
                    navigate(
                        HealthOverviewFragmentDirections.actionHealthOverviewFragmentToStepsDetailsFragment(
                            HealthOverViewHistoryType.Distance.name
                        )
                    )
                }
                is HealthOverview.HeartRate -> {
                    logInsiderAppEvent(InsiderAppEvents.HEALTH_OVERVIEW_HEART_RATE_CLICK)
                    navigate(R.id.heartRateDetailsFragment)
                }
                is HealthOverview.Sleep -> {
                    logInsiderAppEvent(InsiderAppEvents.HEALTH_OVERVIEW_SLEEP_CLICK)
                    navigate(R.id.sleepDetailsFragment)
                }
                is HealthOverview.Steps -> {
                    logInsiderAppEvent(InsiderAppEvents.HEALTH_OVERVIEW_STEP_COUNT_CLICK)
                    navigate(
                        HealthOverviewFragmentDirections.actionHealthOverviewFragmentToStepsDetailsFragment(
                            HealthOverViewHistoryType.Steps.name
                        )
                    )
                }
                is HealthOverview.Stress -> {
                    logInsiderAppEvent(InsiderAppEvents.HEALTH_OVERVIEW_STRESS_CLICK)
                    navigate(R.id.stressDetailsFragment)
                }
                is HealthOverview.Calories -> {
                    logInsiderAppEvent(InsiderAppEvents.HEALTH_OVERVIEW_CALORIES_CLICK)
                    navigate(
                        HealthOverviewFragmentDirections.actionHealthOverviewFragmentToStepsDetailsFragment(
                            HealthOverViewHistoryType.Calories.name
                        )
                    )
                }
                is HealthOverview.BodyTemp -> {
                    logInsiderAppEvent(InsiderAppEvents.HEALTH_OVERVIEW_BODY_TEMPREATURE_CLICK)
                    navigate(R.id.bodyTemperatureDetailsFragment)
                }
            }

        }
    }


    private fun logInsiderAppEvent(eventName: String) {
        viewViewModel.sessionManager.logInsiderAppEvent(
            eventName
        )
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.toolbar.tvTitle.text = getString(R.string.text_health_overview)
    }

    override fun subscribeObservers() {
        viewViewModel.getLoading().observe(this) {
            uiController.displayProgressBar(it, "")
        }
        viewViewModel.summary.healthOverviewData.observe(this) { healthOverviewData ->
            if (healthOverviewData != null) {
                setOverviewData(healthOverviewData)
            }
        }

    }

    private fun setOverviewData(healthOverviewData: HealthOverviewData) {
        binding.lytSummaryHeader.root.visible()
        binding.lytSummaryHeader.apply {


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

            dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithInset(
                    requireActivity(), 100f, 100f, R.color.distance_arc_bg, 80f, 32f
                )
            )
            val distanceIndex: Int = dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithInset(
                    requireActivity(), 0f, 100f, R.color.distance_arc, 80f, 32f
                )
            )

            dynamicArcView.addEvent(
                DecoEvent.Builder(distanceValue)
                    .setDuration(RING_ANIMATION)
                    .setIndex(distanceIndex).build()
            )

            val caloriesValue = healthOverviewData.caloriesGoalProgress ?: 0f

            dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithoutInset(
                    requireActivity(), 100f, 100f, R.color.calories_arc_bg, 32f
                )
            )
            val caloriesIndex: Int = dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithoutInset(
                    requireActivity(),
                    0f,
                    100f,
                    R.color.calories_arc,
                    32f
                )
            )
            dynamicArcView.addEvent(
                DecoEvent.Builder(caloriesValue).setIndex(caloriesIndex).setDuration(RING_ANIMATION)
                    .build()
            )

            val stepsValue = healthOverviewData.stepsGoalProgress ?: 0f
            dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithInset(
                    requireActivity(), 100f, 100f, R.color.steps_arc_bg, 40f, 32f
                )
            )
            val stepIndex: Int = dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithInset(
                    requireActivity(), 0f, 100f, R.color.steps_arc, 40f, 32f
                )
            )
            dynamicArcView.addEvent(
                DecoEvent.Builder(stepsValue).setDuration(RING_ANIMATION).setIndex(stepIndex)
                    .build()
            )

            healthOverviewData.healthOverviewList?.let {
                healthOverviewAdapter.items = it
                healthOverviewAdapter.devicePaired = viewViewModel.summary.connectedDevice != null

            }


        }
    }


}