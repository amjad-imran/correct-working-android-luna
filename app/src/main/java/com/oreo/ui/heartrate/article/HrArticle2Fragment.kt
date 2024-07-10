package com.oreo.ui.heartrate.article

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentHrArticle2Binding
import com.noisefit_commans.ui.BaseFragment


class HrArticle2Fragment :
    BaseFragment<FragmentHrArticle2Binding>(FragmentHrArticle2Binding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.tvTitle.text = getString(R.string.text_hra2_title)

    }


    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }
}