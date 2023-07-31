package com.noisefit.ui.feeds.create.styles.health

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.hookedonplay.decoviewlib.events.DecoEvent
import com.noisefit.R
import com.noisefit.databinding.FragmentRingStyle2Binding
import com.noisefit.ui.dashboard.summary.RING_ANIMATION
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.model.HealthOverviewData
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RingStyle2Fragment :
    BaseFragment<FragmentRingStyle2Binding>(FragmentRingStyle2Binding::inflate) {

    private val viewModel: HealthDataViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRing()
        viewModel.getInitialOfflineData()
    }


    private fun setRing() {
        binding.apply {
            dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithInset(
                    requireActivity(), 100f, 100f, R.color.distance_arc_bg, 25f, 12f
                )
            )

            dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithInset(
                    requireActivity(), 100f, 100f, R.color.calories_arc_bg, 50f,12f
                )
            )
            dynamicArcView.addSeries(
                ApplicationUtils.seriesItemWithoutInset(
                    requireActivity(), 100f, 100f, R.color.steps_arc_bg, 12f
                )
            )
        }

    }

    private fun setOverviewData(healthOverviewData: HealthOverviewData) {

        binding.apply {


            tvCalorieCurrent.text = if (healthOverviewData.calories == 0) {
                "--"
            } else {
                healthOverviewData.calories.toString()
            }

            tvStepCurrent.text = if (healthOverviewData.steps == 0) {
                "--"
            } else {
                healthOverviewData.steps.toString()
            }

            val distance = try {
                healthOverviewData.distance?.toFloat()
            } catch (exp: Exception) {
                0.0f
            }

            tvDistanceCurrent.text = if (distance == 0.0f) {
                "--"
            } else {
                healthOverviewData.distance.toString()
            }


            val distanceValue = healthOverviewData.distanceGoalProgress ?: 0f

            if (viewModel.distanceProgressCompleted != distanceValue) {
                viewModel.distanceProgressCompleted = distanceValue
                LOGS.d("update_distance_value $distanceValue")

                val distanceIndex: Int = dynamicArcView.addSeries(
                    ApplicationUtils.seriesItemWithInset(
                        requireActivity(), 0f, 100f, R.color.distance_arc, 25f, 12f
                    )
                )

                dynamicArcView.addEvent(
                    DecoEvent.Builder(distanceValue).setIndex(distanceIndex)
                        .setDuration(RING_ANIMATION).build()
                )
            }


            val caloriesValue = healthOverviewData.caloriesGoalProgress ?: 0f

            if (viewModel.caloriesProgressCompleted != caloriesValue) {
                viewModel.caloriesProgressCompleted = caloriesValue

                val caloriesIndex: Int = dynamicArcView.addSeries(
                    ApplicationUtils.seriesItemWithInset(
                        requireActivity(), 0f, 100f, R.color.calories_arc, 50f,12f
                    )
                )
                dynamicArcView.addEvent(
                    DecoEvent.Builder(caloriesValue).setIndex(caloriesIndex)
                        .setDuration(RING_ANIMATION).build()
                )
            }


            val stepsValue = healthOverviewData.stepsGoalProgress ?: 0f
            if (viewModel.stepsProgressCompleted != stepsValue) {
                viewModel.stepsProgressCompleted = stepsValue

                val stepIndex: Int = dynamicArcView.addSeries(
                    ApplicationUtils.seriesItemWithoutInset(
                        requireActivity(), 0f, 100f, R.color.steps_arc,  12f
                    )
                )
                dynamicArcView.addEvent(
                    DecoEvent.Builder(stepsValue).setIndex(stepIndex).setDuration(RING_ANIMATION)
                        .build()
                )
            }
        }
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {
        viewModel.healthOverviewData.observe(this) { healthOverviewData ->
            if (healthOverviewData != null) {
                setOverviewData(healthOverviewData)
            }
        }
    }
}