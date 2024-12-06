package com.oreo.ui.chatGpt.functions

import android.os.Bundle
import android.view.View
import com.noisefit.luna.databinding.FragmentAiWorkoutDetailBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AiWorkoutDetailFragment :
    BaseFragment<FragmentAiWorkoutDetailBinding>(FragmentAiWorkoutDetailBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvWorkoutName.text = "Flat Bench Press"
        binding.tvSetsData.text = "3 sets 12 reps"
    }

    override fun initListener() {

        binding.tvNextWorkout.setOnClickListener {
            onNextClicked()
        }
        binding.ivNext.setOnClickListener {
            onNextClicked()
        }

    }

    fun onNextClicked() {

    }

    override fun subscribeObservers() {

    }
}