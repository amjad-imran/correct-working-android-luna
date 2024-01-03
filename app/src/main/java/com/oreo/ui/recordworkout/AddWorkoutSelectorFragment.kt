package com.oreo.ui.recordworkout

import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.FragmentAddWorkoutSelctorBinding
import com.noisefit_commans.ui.BaseFragment


const val ADD_WORKOUT_SELECTOR = "ADD_WORKOUT_SELECTOR"

class AddWorkoutSelectorFragment :
    BaseFragment<FragmentAddWorkoutSelctorBinding>(FragmentAddWorkoutSelctorBinding::inflate) {
    override fun initListener() {

        binding.tvAddWorkout.setOnClickListener {
            navigateUpSafe()
            requireActivity().supportFragmentManager.setFragmentResult(
                ADD_WORKOUT_SELECTOR,
                bundleOf("selected" to "addWorkout")
            )
        }

        binding.tvRecordWorkout.setOnClickListener {
            navigateUpSafe()
            requireActivity().supportFragmentManager.setFragmentResult(
                ADD_WORKOUT_SELECTOR,
                bundleOf("selected" to "recordWorkout")
            )
        }

        binding.ivWorkoutClose.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

}