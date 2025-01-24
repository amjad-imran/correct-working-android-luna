package com.oreo.ui.sleep2.sleepplanner.exercise

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentBreathingExerciseBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BreathingExerciseFragment :
    BaseFragment<FragmentBreathingExerciseBinding>(FragmentBreathingExerciseBinding::inflate) {

    private val viewModel: BreathingExerciseViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        binding.lottieAnimationView.repeatCount = LottieDrawable.INFINITE
        binding.lottieAnimationView.setAnimation(R.raw.anim_breathing_exercise)
        binding.lottieAnimationView.playAnimation()
        viewModel.startTimer()
    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        viewModel.timerRunning.observe(this) {
            if (it == null) return@observe

            if (it == 0L) {
                navigateUpSafe()
                return@observe
            } else {
                val min = it / 60
                val second = it % 60

                binding.tvTimer.text = String.format("%d:%02d", min, second)
            }
        }

    }

    override fun onPause() {
        super.onPause()
        viewModel.cancelTimer()
    }


}