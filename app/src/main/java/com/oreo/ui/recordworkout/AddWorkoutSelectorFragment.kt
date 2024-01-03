package com.oreo.ui.recordworkout

import com.noisefit.luna.databinding.FragmentAddWorkoutSelctorBinding
import com.noisefit_commans.ui.BaseFragment


class AddWorkoutSelectorFragment :
    BaseFragment<FragmentAddWorkoutSelctorBinding>(FragmentAddWorkoutSelctorBinding::inflate) {
    override fun initListener() {

        binding.tvAddWorkout.setOnClickListener {

        }
        binding.tvRecordWorkout.setOnClickListener {

        }
    }

    override fun subscribeObservers() {

    }

}