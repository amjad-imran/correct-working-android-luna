package com.noisefit.ui.onboarding.setup

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSetupSuccessBinding
import com.noisefit.oreo.OreoMainActivity
import com.noisefit.ui.onboarding.AllDoneActivity
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.loadImage

class SetupSuccessFragment :
    BaseFragment<FragmentSetupSuccessBinding>(FragmentSetupSuccessBinding::inflate) {
    private val viewModel: DeviceSetupSharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.updateProgress3.postValue(100)

        binding.ivRingImage.loadImage(requireContext(), viewModel.getRingImage2())
        startUpdateSuccessLottie(onFinish = {

        })
    }

    private fun startUpdateSuccessLottie(onFinish: () -> Unit) {
        binding.animView.repeatCount = 0
        binding.animView.setAnimation(R.raw.anim_bottom_filled_colored)
        binding.animView.playAnimation()
        binding.animView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                onFinish.invoke()
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }

        })
    }


    override fun initListener() {
        binding.btnLetsGo.setOnClickListener {
            startActivity(OreoMainActivity.getStartIntent(requireContext()).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }

    }

    override fun subscribeObservers() {

    }


}