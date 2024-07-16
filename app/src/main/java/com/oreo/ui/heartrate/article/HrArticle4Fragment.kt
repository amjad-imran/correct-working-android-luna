package com.oreo.ui.heartrate.article

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.noisefit.luna.databinding.FragmentHrArticle4Binding
import com.noisefit_commans.ui.BaseFragment


class HrArticle4Fragment :
    BaseFragment<FragmentHrArticle4Binding>(FragmentHrArticle4Binding::inflate) {

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.tvTitle.text = "Heart rate during sleep"

    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }


}