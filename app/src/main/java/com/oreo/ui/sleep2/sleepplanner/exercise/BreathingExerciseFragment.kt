package com.oreo.ui.sleep2.sleepplanner.exercise

import android.animation.Animator
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentBreathingExerciseBinding
import com.noisefit_commans.common.setTextGradient
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BreathingExerciseFragment :
    BaseFragment<FragmentBreathingExerciseBinding>(FragmentBreathingExerciseBinding::inflate) {

    private val viewModel: BreathingExerciseViewModel by viewModels()

    override fun onResume() {
        super.onResume()

        restartAnim()

        /*if(viewModel.navigateBack){
            navigateUpSafe()
        }else{
            binding.lottieAnimationView.repeatCount = LottieDrawable.INFINITE
            binding.lottieAnimationView.setAnimation(R.raw.anim_breathing_exercise)
            binding.lottieAnimationView.playAnimation()
            viewModel.startTimer()
        }*/
    }

    private fun restartAnim() {
        binding.imageBack.visible()
        binding.lytStateStart.root.visible()
        binding.lytStateCompleted.root.gone()
        binding.lottieAnimationView.gone()
        binding.tvTimer.gone()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTextGradient(binding.lytStateStart.textBreathing)
        setTextGradient(binding.lytStateStart.text478)
        setTextGradient(binding.lytStateCompleted.textGreat)
        setTextGradient(binding.lytStateCompleted.textSessionComplete)

    }

    private fun setTextGradient(textView: TextView) {
        textView.post {
            textView.setTextColor(Color.parseColor("#EBDDFF"))
            val textShader: Shader = LinearGradient(
                0f,
                0f,
                0f,
                textView.height.toFloat(),
                intArrayOf(
                    Color.parseColor("#EBDDFF"),
                    Color.parseColor("#C5A8ED"),
                    Color.parseColor("#C5A8ED"),
                ),
                floatArrayOf(0f,0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            textView.paint.shader = textShader
        }
    }

    override fun initListener() {
        binding.lytStateStart.ivStart.setOnClickListener {
            binding.lytStateStart.root.gone()
            binding.imageBack.gone()
            startLottie()
        }

        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.lytStateCompleted.btnBack.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytStateCompleted.btnRestart.setOnClickListener {
            binding.lytStateCompleted.root.gone()
            startLottie()
        }
    }

    private fun startLottie() {
        binding.lottieAnimationView.visible()
        binding.tvTimer.visible()

        binding.lottieAnimationView.repeatCount = 5
        binding.lottieAnimationView.setAnimation(R.raw.anim_breathing_exercise)
        //binding.lottieAnimationView.setAnimation(R.raw.anim_ai_mic)

        binding.lottieAnimationView.removeAllAnimatorListeners()
        binding.lottieAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                binding.lottieAnimationView.gone()
                binding.imageBack.visible()
                binding.tvTimer.gone()
                binding.lytStateCompleted.root.visible()
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
        binding.lottieAnimationView.playAnimation()
        viewModel.startTimer()
    }

    override fun subscribeObservers() {
        viewModel.timerRunning.observe(this) {
            if (it == null) {
                binding.tvTimer.text = ""
                return@observe
            }

            if (it == 0L) {
                /*navigateUpSafe()
                return@observe*/
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