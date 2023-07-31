package com.noisefit.ui.feeds.create

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetWorkoutPickerBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.dashboard.summary.RecentWorkoutsAdapter
import com.noisefit.ui.profile.BottomSheetImagePicker
import com.noisefit.ui.watchface.REQUEST_LAUNCH_LIBRARY
import com.noisefit.ui.workout.ActivityShareFragmentDirections
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetWorkoutPicker : BaseBottomSheetWithTransparent<BottomSheetWorkoutPickerBinding>(
    BottomSheetWorkoutPickerBinding::inflate
) {
    private val viewModel: PostExternalDataViewModel by viewModels()

    private val recentWorkoutsAdapter by lazy {
        RecentWorkoutsAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.rvWorkouts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentWorkoutsAdapter
        }

        recentWorkoutsAdapter.setOnRecentWorkoutsInteractionListener(object :
            RecentWorkoutsAdapter.RecentWorkoutsInteractionListener {
            override fun onWorkoutSelected(sportsModeResponse: SportsModeResponse) {
                setFragmentResult(
                    WORKOUT_PICKER_RESULT,
                    bundleOf("workout" to sportsModeResponse)
                )
                navigateUpSafe()
            }
        })
        viewModel.getWorkouts()
    }


    companion object {
        const val WORKOUT_PICKER_RESULT = "WORKOUT_PICKER_RESULT"
    }

    override fun initListener() {
        binding.btnDone.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.visible()
            } else {
                binding.progressBar.gone()
            }
        }

        viewModel.getApiErrors().observe(this) {
            it.getContent()?.let {
                context.showShortToast(getString(R.string.text_something_went_wrong))
            }
        }

        viewModel.workouts.observe(this) {
            recentWorkoutsAdapter.setDataSet(it ?: ArrayList())
            if (it.isNullOrEmpty()) {
                binding.tvNoData.text = getString(R.string.text_no_workout)
                binding.tvNoData.visible()
                binding.btnDone.visible()
            } else {
                binding.tvNoData.gone()
                binding.btnDone.gone()
            }
        }
    }
}