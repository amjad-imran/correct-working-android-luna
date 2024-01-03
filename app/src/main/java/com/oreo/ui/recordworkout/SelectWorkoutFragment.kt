package com.oreo.ui.recordworkout

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.noisefit.luna.databinding.FragmentSelectWorkoutBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.data.model.OWorkoutListModal
import com.oreo.ui.workout.add.OSelectWorkoutAdapter
import com.oreo.ui.workout.add.SELECT_REQUEST_KEY
import dagger.hilt.android.AndroidEntryPoint

const val SELECT_RECORD_WORKOUT = "SELECT_RECORD_WORKOUT"

@AndroidEntryPoint
class SelectWorkoutFragment :
    BaseFragment<FragmentSelectWorkoutBinding>(FragmentSelectWorkoutBinding::inflate) {

    val viewModel: SelectWorkoutViewModel by viewModels()

    private val selectWorkoutAdapter by lazy {
        OSelectWorkoutAdapter(object : OSelectWorkoutAdapter.OSelectWorkoutInteraction {
            override fun onWorkoutSelected(oWorkoutListModal: OWorkoutListModal) {
                navigateUpSafe()
                requireActivity().supportFragmentManager.setFragmentResult(
                    SELECT_RECORD_WORKOUT,
                    bundleOf("workout" to oWorkoutListModal)
                )

            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecycler()

        viewModel.getWorkoutList()
    }


    private fun setRecycler() {
        with(binding.rvSelectWorkout) {
            adapter = selectWorkoutAdapter
        }

    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.oWorkoutListModalResponse.observe(this) {
            it?.let {
                selectWorkoutAdapter.setData(it)
            }
        }
    }

}