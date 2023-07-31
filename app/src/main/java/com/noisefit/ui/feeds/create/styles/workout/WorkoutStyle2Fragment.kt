package com.noisefit.ui.feeds.create.styles.workout

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.luna.databinding.FragmentWorkoutStyle2Binding
import com.noisefit.util.ImageUtil
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.models.Units
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.displayToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutStyle2Fragment :
    BaseFragment<FragmentWorkoutStyle2Binding>(FragmentWorkoutStyle2Binding::inflate) {

    @Inject
    lateinit var dataUnitConverter: DataUnitConverter


    companion object {
        val SELECTED_WORKOUT = "SELECTED_WORKOUT"

        @JvmStatic
        fun newInstance(selectedWorkout: SportsModeResponse) =
            WorkoutStyle2Fragment().apply {
                arguments = Bundle().apply {
                    this.putParcelable(SELECTED_WORKOUT, selectedWorkout)
                }
            }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val workout =
            arguments?.getParcelable(SELECTED_WORKOUT) as? SportsModeResponse


        workout?.let {
            if ((it.steps ?: 0) == 0 && (it.calories?.toInt() ?: 0) == 0 && (it.distance?.toInt()
                    ?: 0) == 0
            ) {
                requireActivity().displayToast(R.string.text_you_cannot_share_without_any_activity_progress)
                navigateUpSafe()
            }
            binding.tvWorkoutName.text = it.getFormattedActivityName()
            binding.tvCalorie.text = if ((it.calories ?: 0L) == 0L) {
                "--"
            } else
                "${it.calories ?: 0}"

            val distance = dataUnitConverter.formatDistance(
                it.distance?.toInt() ?: 0,
                Units.METRIC
            )
            binding.tvDistance.text = if ((it.distance ?: 0L) == 0L) {
                "--"
            } else
                distance
            binding.tvSteps.text = if ((it.steps ?: 0) == 0) {
                "--"
            } else
                "${it.steps ?: 0}"


            val activityName = it.type ?: it.activityType
        }
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}