package com.oreo.ui.stress

import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import com.noisefit.luna.databinding.FragmentOStressMovementBinding
import com.noisefit_commans.ui.BaseFragment


class OStressMovementFragment :
    BaseFragment<FragmentOStressMovementBinding>(FragmentOStressMovementBinding::inflate) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun initListener() {
        binding.btnClickMe.setOnClickListener {
            val rotate = RotateAnimation(
                0f,
                180f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                1f
            )
            rotate.duration = 1000
            rotate.interpolator = AccelerateDecelerateInterpolator()
            binding.lytTicker.ivTicker.startAnimation(rotate)
        }

    }

    override fun subscribeObservers() {

    }

}