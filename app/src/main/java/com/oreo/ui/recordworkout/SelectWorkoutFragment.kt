package com.oreo.ui.recordworkout

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
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

        binding.toolbar.tvTitle.text = getString(R.string.text_select_workout)

        setRecycler()

        viewModel.getWorkoutList()
    }


    private fun setRecycler() {
        with(binding.rvSelectWorkout) {
            adapter = selectWorkoutAdapter
        }

    }

    override fun initListener() {

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                selectWorkoutAdapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}

        })

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