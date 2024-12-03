package com.oreo.ui.chatGpt.summary

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAiSummaryDataBinding
import com.noisefit_commans.ui.BaseFragment


class AiSummaryDataFragment :
    BaseFragment<FragmentAiSummaryDataBinding>(FragmentAiSummaryDataBinding::inflate) {

    companion object {
        fun getInstance(text: String): AiSummaryDataFragment {
            return AiSummaryDataFragment().apply {
                this.arguments = Bundle().apply {
                    this.putString("text", text)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val text = arguments?.getString("text")
        binding.tvText.text = text

        binding.rootView.setBackgroundResource(R.color.colorRed2)
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}