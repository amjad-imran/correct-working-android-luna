package com.noisefit.ui

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentTestBinding
import com.noisefit_commans.ui.BaseFragment


class TestFragment : BaseFragment<FragmentTestBinding>(FragmentTestBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.animationView.setAnimation(R.raw.loading_swipe_anim)
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


}