package com.oreo.ui.workout.detect

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentDetectWorkoutListBinding
import com.noisefit.ui.common.bottomSheet.ALERT_REQUEST_KEY
import com.noisefit_commans.ui.BaseFragment
import com.oreo.ui.activity.OreoDMAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DetectWorkoutListFragment :
    BaseFragment<FragmentDetectWorkoutListBinding>(FragmentDetectWorkoutListBinding::inflate) {

    private val viewModel: DetectWorkoutViewModel by viewModels()
    private val dmGraphAdapter: OreoDMAdapter by lazy {
        OreoDMAdapter()
    }

    private val detectWorkoutAdapter: DetectWorkoutAdapter by lazy {
        DetectWorkoutAdapter(object : DetectWorkoutListener {
            override fun onIdentifyWorkout() {

            }

            override fun onDismissWorkout() {

                setFragmentResultListener(ALERT_REQUEST_KEY) { _, bundle ->
                    val allow = bundle.getBoolean("allow")
                    if (allow) {

                    }
                }
                navigate(
                    DetectWorkoutListFragmentDirections.actionDetectWorkoutListFragmentToAlertTextBottomSheet(
                        getString(R.string.text_dismiss_activity_title),
                        getString(R.string.text_dismiss_activity_desc), "", ""
                    )
                )
            }

        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()

    }

    private fun setAdapter() {
        with(binding.rvDMGraph) {
            adapter = dmGraphAdapter
        }

        with(binding.rv) {
            adapter = detectWorkoutAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        dmGraphAdapter.setData(viewModel.getDMData())
        detectWorkoutAdapter.setData(viewModel.getDetectList())
    }


    override fun initListener() {
        binding.lytToolbar.apply {
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
            tvTitle.text = getString(R.string.text_detected_workouts)

        }
    }

    override fun subscribeObservers() {

    }


}